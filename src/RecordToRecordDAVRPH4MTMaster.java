import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * H3 with advanced parameters
 */
public class RecordToRecordDAVRPH4MTMaster implements Solver {

    private double epsilon = Math.pow(10.0, -10.0);
    private double record;
    private double deviation;
    private int K;
    private int D;
    private int P;
    private int K2;
    private int D2;
    private int NBListSize;
    private double[][] c;
    private int Q;
    private double alpha;
    private double beta;
    private RouteSet routeSet;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MTMaster() {
        // Parameters
        K = 5;
        D = 30;
        P = 2;
        K2 = 5;
        D2 = 5;
        NBListSize = 40;
        beta = 0.6;
    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();
        solution.setName("RTR_DAVRP_H4_MT");

        // Get some data from data set
        int o = dataSet.getNumberOfScenarios();
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        alpha = dataSet.getAlpha();

        Long start = System.currentTimeMillis();

        // Create place to store intermediate solutions
        RouteSet[] solutions = new RouteSet[o];

        // Create a basis solution to start with
        RecordToRecordH3MTMaster rtr = new RecordToRecordH3MTMaster(D, K, P, NBListSize, beta);
        Solution solutionBasis = rtr.solve(dataSet);
        RouteSet routeSetBasis = solutionBasis.getRoutes()[0];

        solution.setAssignments(routeSetBasis.assignments());

        double objectiveValue = 0.0;

        ExecutorService pool = Executors.newFixedThreadPool(4);
        Future[] futures = new Future[o];

        for (int scenario = 0; scenario < o; scenario++) {
            futures[scenario] = pool.submit(new RecordToRecordDAVRPH4MT(dataSet.getCopy(), routeSetBasis.getCopy(), scenario, K2, D2));
        }

        for (int scenario = 0; scenario < o; scenario++) {
            try {
                RouteSet temp = (RouteSet) futures[scenario].get();
                solutions[scenario] = temp;
                objectiveValue += temp.getRouteLength() * dataSet.getScenarioProbabilities()[scenario];
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
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

}
