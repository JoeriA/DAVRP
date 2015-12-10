import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * H3 with advanced parameters
 */
public class RecordToRecordDAVRPH4MTFMaster implements Solver {

    private double[] lambdas;
    private int NBListSize;
    private double beta;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MTFMaster() {
        // Parameters
        NBListSize = 40;
        beta = 0.6;
//        lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
//        lambdas = new double[]{0.6, 1.4, 1.6};
        lambdas = new double[]{1.0};

    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();

        // Get some data from data set
        int o = dataSet.getNumberOfScenarios();

        Long start = System.currentTimeMillis();

        // Create place to store intermediate solutions
        RouteSet[] solutions = new RouteSet[o];

        // Create a basis solution to start with
        RecordToRecordH3MTFMaster rtr = new RecordToRecordH3MTFMaster(lambdas, NBListSize, beta);
        Solution solutionBasis = rtr.solve(dataSet);

        if (o > 1) {
            RouteSet routeSetBasis = solutionBasis.getRoutes()[0];

            solution.setAssignments(routeSetBasis.assignments());

            double objectiveValue = 0.0;

            ExecutorService pool = Executors.newFixedThreadPool(4);
            Future[] futures = new Future[o];

            for (int scenario = 0; scenario < o; scenario++) {
                futures[scenario] = pool.submit(new RecordToRecordDAVRPH4MTF(dataSet.getCopy(), routeSetBasis.getCopy(), scenario));
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
        } else {
            solution = solutionBasis;
        }
        solution.setName("RTR_DAVRP_H4_MT_F");

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

}
