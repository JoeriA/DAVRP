import java.util.concurrent.*;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * H3 with advanced parameters
 */
public class RecordToRecordDAVRPH4MTMaster implements Solver, Callable<Solution> {

    private double[] lambdas;
    private int K;
    private int D;
    private int P;
    private int K2;
    private int D2;
    private int P2;
    private int NBListSize;
    private double beta;
    private double delta;
    private double delta2;
    private DataSet dataSet;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MTMaster() {
        // Parameters
        K = 7;
        D = 20;
        P = 2;
        K2 = 2;
        D2 = 5;
        P2 = 2;
        NBListSize = 40;
        beta = 0.6;
        lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
//        lambdas = new double[]{0.6, 1.4, 1.6};
        delta = 0.01;
        delta2 = 0.01;
    }

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MTMaster(double[] lambdas, int K, int D, int P, int NBListSize, double beta, double delta, int K2, int D2, int P2, double delta2) {
        // Parameters
        this.lambdas = lambdas;
        this.K = K;
        this.D = D;
        this.P = P;
        this.NBListSize = NBListSize;
        this.beta = beta;
        this.delta = delta;
        this.K2 = K2;
        this.D2 = D2;
        this.P2 = P2;
        this.delta2 = delta2;
    }

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MTMaster(DataSet dataSet, double[] lambdas, int K, int D, int P, int NBListSize, double beta, double delta, int K2, int D2, int P2, double delta2) {
        // Parameters
        this.dataSet = dataSet;
        this.lambdas = lambdas;
        this.K = K;
        this.D = D;
        this.P = P;
        this.NBListSize = NBListSize;
        this.beta = beta;
        this.delta = delta;
        this.K2 = K2;
        this.D2 = D2;
        this.P2 = P2;
        this.delta2 = delta2;
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
        RecordToRecordH3MTMaster rtr = new RecordToRecordH3MTMaster(lambdas, D, K, P, NBListSize, beta, delta);
        Solution solutionBasis = rtr.solve(dataSet);

        if (o > 1) {
            RouteSet routeSetBasis = solutionBasis.getRoutes()[0];

            solution.setAssignments(routeSetBasis.assignments());

            double objectiveValue = 0.0;

            ExecutorService pool = Executors.newFixedThreadPool(4);
            Future[] futures = new Future[o];

            for (int scenario = 0; scenario < o; scenario++) {
                futures[scenario] = pool.submit(new RecordToRecordDAVRPH4MT(dataSet.getCopy(), routeSetBasis.getCopy(), scenario, K2, D2, P2, delta2));
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

        solution.setName("RTR_DAVRP_H4_MT");

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

    @Override
    public Solution call() throws Exception {
        return solve(dataSet);
    }
}
