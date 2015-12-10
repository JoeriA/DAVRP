import gurobi.GRBException;
import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * RTR DAVRP with clustering
 */
public class RecordToRecordDAVRP2MTMaster implements Solver {

    private int K2;
    private int D2;
    private int P2;
    private double delta;
    private int NBListSize;
    private double beta;
    private int n;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRP2MTMaster() {
        // Parameters
        K2 = 7;
        D2 = 20;
        P2 = 2;
        delta = 0.01;
        NBListSize = 40;
        beta = 0.6;
    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) throws IloException, GRBException {

        Solution solution = new Solution();
        solution.setName("RTR_DAVRP_2_MT");

        // Get some data from data set
        n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        double[][] c = dataSet.getTravelCosts();

        Long start = System.currentTimeMillis();

        RouteSet routeSet = new RouteSet();
        routeSet.setCustomers(dataSet.getCustomers());

        // Create neighbor lists
        for (Customer customer : routeSet.getCustomers()) {
            if (customer.getId() != 0) {
                ArrayList<Neighbor> neighborList = new ArrayList<>(n - 2);
                for (Customer neighbor : routeSet.getCustomers()) {
                    if (neighbor.getId() != 0 && neighbor.getId() != customer.getId()) {
                        neighborList.add(new Neighbor(customer.getId(), neighbor.getId(), c));
                    }
                }
                Collections.sort(neighborList);
                int neighborListLength = Math.min(NBListSize - 1, n - 3);
                while (neighborList.size() > neighborListLength) {
                    neighborList.remove(0);
                }
                double criticalValue = neighborList.get(0).getDistance() * beta;
                while (neighborList.get(0).getDistance() > criticalValue) {
                    neighborList.remove(0);
                }
                Collections.sort(neighborList, Neighbor.neighborAscending);
                Collections.sort(neighborList, Neighbor.distanceAscending);
                customer.setNeighbors(neighborList);
            }
        }

        // Obtain clustering results
        SolverClustering sc = new SolverClustering();
        Solution clusters = sc.solve(dataSet);

        if (clusters.getGap() > 0.5) {
            return null;
        }

        // Create routes with clusters
        ClarkeWright cw = new ClarkeWright();
        Solution basisSolution = cw.solve(dataSet, clusters);
        assignRoutes(basisSolution, clusters.getzSol());
        solution.setAssignments(basisSolution.getAssignments());

        // Create place to store intermediate solutions
        RouteSet[] solutions = new RouteSet[o];
        double objectiveValue = 0.0;

        ExecutorService pool = Executors.newFixedThreadPool(4);
        Future[] futures = new Future[o];

        for (int scenario = 0; scenario < o; scenario++) {
            futures[scenario] = pool.submit(new RecordToRecordDAVRPH4MT(dataSet.getCopy(), basisSolution.getRoutes()[scenario], scenario, K2, D2, P2, delta));
        }

        for (int scenario = 0; scenario < o; scenario++) {
            try {
                RouteSet temp = (RouteSet) futures[scenario].get();
                solutions[scenario] = temp;
                objectiveValue += temp.getRouteLength() * dataSet.getScenarioProbabilities()[scenario];
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setRoutes(solutions);
        solution.setObjectiveValue(objectiveValue);

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

    private void assignRoutes(Solution solution, double[][] zSol) {
        int[] assignments = new int[n];
        RouteSet[] routeSets = solution.getRoutes();
        for (RouteSet routeSet : routeSets) {
            for (int i = 1; i < zSol.length; i++) {
                for (int j = 1; j < zSol[i].length; j++) {
                    if (zSol[i][j] == 1) {
                        routeSet.getRoutes()[j].addAssignedCustomer(routeSet.getCustomers()[i]);
                        assignments[i] = j;
                    }
                }
            }
        }
        solution.setAssignments(assignments);
    }

}
