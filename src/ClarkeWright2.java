/**
 * Created by Joeri on 19-5-2014.
 * Implementation Clarke Wright heuristic
 */
public class ClarkeWright2 implements Solver {

    private int nrOfScenarios;

    /**
     * Implementation of Clarke-Wright heuristic for the DAVRP
     */
    public ClarkeWright2() {

    }

    /**
     * Solve VRP for this dataset for scenario with all largest demands
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet) {

        // Get some data from dataset
        nrOfScenarios = dataSet.getNumberOfScenarios();

        // Get largest demands
        int highestDemand;
        for (Customer c : dataSet.getCustomers()) {
            highestDemand = 0;
            // Get highest demand of all scenarios
            for (int k = 0; k < nrOfScenarios; k++) {
                if (c.getDemandPerScenario()[k] > highestDemand) {
                    highestDemand = c.getDemandPerScenario()[k];
                }
            }
            c.setDemand(highestDemand);
        }

        Solution solution = new Solution();
        solution.setName("Clarke-Wright heuristic2");

        Long start = System.currentTimeMillis();

        RouteSet bestRouteSet = new RouteSet();
        bestRouteSet.setRouteLength(Double.POSITIVE_INFINITY);

        double[] lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
//        double[] lambdas = new double[]{0.4, 1.0};

        for (double lambda : lambdas) {
            ClarkeWright cw = new ClarkeWright();
            cw.setLambda(lambda);
            Solution sol = cw.solve(dataSet);
            RouteSet rs = sol.getRoutes()[0];
            if (rs.getRouteLength() < bestRouteSet.getRouteLength()) {
                bestRouteSet = rs.getCopy();
            }
        }


        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(bestRouteSet.getRouteLength());
        RouteSet[] scenarioRoutes = new RouteSet[nrOfScenarios];
        for (int i = 0; i < nrOfScenarios; i++) {
            scenarioRoutes[i] = bestRouteSet;
        }
        solution.setRoutes(scenarioRoutes);

        return solution;
    }

}
