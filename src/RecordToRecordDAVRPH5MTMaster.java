import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * H4 with doing rtr for all clarke wright initial solutions
 */
public class RecordToRecordDAVRPH5MTMaster implements Solver {

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

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH5MTMaster() {
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
    public RecordToRecordDAVRPH5MTMaster(double[] lambdas, int K, int D, int P, int NBListSize, double beta, double delta, int K2, int D2, int P2, double delta2) {
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
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();
        double bestValue = Double.MAX_VALUE;

        // Get some data from data set
        int o = dataSet.getNumberOfScenarios();

        Long start = System.currentTimeMillis();

        int nrLambdas = lambdas.length;

        ExecutorService pool = Executors.newFixedThreadPool(4);
        Future[] futures = new Future[nrLambdas];

        for (int nrLambda = 0; nrLambda < nrLambdas; nrLambda++) {
            futures[nrLambda] = pool.submit(new RecordToRecordDAVRPH4MTMaster(dataSet, new double[] {lambdas[nrLambda]}, K, D, P, NBListSize, beta, delta, K2, D2, P2, delta2));
        }

        for (int nrLambda = 0; nrLambda < nrLambdas; nrLambda++) {
            try {
                Solution temp = (Solution) futures[nrLambda].get();
                if (temp.getObjectiveValue() < bestValue) {
                    solution = temp;
                    bestValue = temp.getObjectiveValue();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setName("RTR_DAVRP_H5_MT");

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

}
