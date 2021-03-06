import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * Now with second 2-opt
 */
public class RecordToRecordH3MTMaster implements Solver {

    double epsilon = Math.pow(10.0, -10.0);
    private int D;
    private int K;
    private int P;
    private int NBListSize;
    private double beta;
    private double[] lambdas;
    private double delta;
    private String solverName;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordH3MTMaster() {
        // Parameters balanced
//        solverName = "Record2Record_H3_MT";
//        K = 7;
//        D = 20;
//        P = 2;
//        NBListSize = 40;
//        beta = 0.6;
//        lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
////        lambdas = new double[]{0.6, 1.4, 1.6};
//        delta = 0.01;

//        // Parameters Li
//        solverName = "Record2Record_H3_MT_Li";
//        K = 5;
//        D = 30;
//        P = 2;
//        NBListSize = 40;
//        beta = 0.6;
//        lambdas = new double[]{0.6, 1.4, 1.6};
//        delta = 0.01;

        // Parameters Groër
        solverName = "Record2Record_H3_MT_Groeer";
        K = 5;
        D = 30;
        P = 2;
        NBListSize = 30;
        beta = 1.0;
        lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
        delta = 0.01;
    }

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordH3MTMaster(double[] lambdas, int K, int D, int P, int NBListSize, double beta, double delta) {
        // Parameters
        solverName = "Record2Record_H3_MT";
        this.lambdas = lambdas;
        this.K = K;
        this.D = D;
        this.P = P;
        this.NBListSize = NBListSize;
        this.beta = beta;
        this.delta = delta;
    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        return solve(dataSet, 0);
    }

    /**
     * Solve VRP for this scenario
     *
     * @param dataSet  data set to be solved
     * @param scenario number of scenario (starts with 1)
     * @return solution to the data set
     */
    public Solution solve(DataSet dataSet, int scenario) {

        Solution solution = new Solution();
        solution.setName(solverName);

        // Get some data from data set
        int n = dataSet.getNumberOfCustomers() + 1;
        double[][] c = dataSet.getTravelCosts();
//        P = (int) Math.max((n - 1) / 2.0, 30);

        RouteSet routeSet = new RouteSet();
        routeSet.setCustomers(dataSet.getCustomers());

        Long start = System.currentTimeMillis();

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
                while (neighborList.size() > 0 && neighborList.get(0).getDistance() > criticalValue) {
                    neighborList.remove(0);
                }
                Collections.sort(neighborList, Neighbor.neighborAscending);
                Collections.sort(neighborList, Neighbor.distanceAscending);
                customer.setNeighbors(neighborList);
            }
        }

        RouteSet bestRouteSet = new RouteSet();
        bestRouteSet.setRouteLength(Double.POSITIVE_INFINITY);

        ExecutorService pool = Executors.newFixedThreadPool(4);
        Future[] futures = new Future[lambdas.length];

        for (int i = 0; i < lambdas.length; i++) {
            futures[i] = pool.submit(new RecordToRecordH3MT(dataSet.getCopy(), lambdas[i], D, K, P, delta));
        }

        for (Future future : futures) {
            try {
                Solution temp = (Solution) future.get();
                if (temp.getRoutes()[0].getRouteLength() < bestRouteSet.getRouteLength()) {
                    bestRouteSet = temp.getRoutes()[0];
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(bestRouteSet.getRouteLength());
        solution.setAssignments(bestRouteSet.assignments());
        RouteSet[] sol = new RouteSet[dataSet.getNumberOfScenarios()];
        if (scenario == 0) {
            for (int i = 0; i < sol.length; i++) {
                sol[i] = bestRouteSet;
            }
        } else {
            sol[scenario - 1] = bestRouteSet;
        }
        solution.setRoutes(sol);

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

}
