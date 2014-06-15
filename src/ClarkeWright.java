import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation Clarke Wright heuristic
 */
public class ClarkeWright implements Solver {

    private double lambda;
    private Customer[] customers;
    private int n;
    private int nrOfScenarios;
    private int Q;
    private double[][] c;

    /**
     * Implementation of Clarke-Wright heuristic for the DAVRP
     */
    public ClarkeWright() {

        lambda = 1.0;
    }

    /**
     * Set lambda parameter
     *
     * @param lambda lambda parameter
     */
    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    /**
     * Solve VRP for this dataset for scenario with all largest demands
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet) {

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        nrOfScenarios = dataSet.getNumberOfScenarios();
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        // Get largest demands
        int highestDemand;
        for (Customer c : customers) {
            highestDemand = 0;
            // Get highest demand of all scenarios
            for (int k = 0; k < nrOfScenarios; k++) {
                if (c.getDemandPerScenario()[k] > highestDemand) {
                    highestDemand = c.getDemandPerScenario()[k];
                }
            }
            c.setDemand(highestDemand);
        }

        return solve();
    }

    /**
     * Solve a VRP with Clarke-Wright for a certain scenario
     *
     * @param dataSet  dataset to be solved
     * @param scenario scenario number (starting with 1)
     * @return solution to the problem
     */
    public Solution solve(DataSet dataSet, int scenario) {

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        // Get demands
        for (Customer c : customers) {
            c.setDemand(c.getDemandPerScenario()[scenario - 1]);
        }

        return solve();
    }

    /**
     * Solve the VRP
     *
     * @return solution to the VRP
     */
    private Solution solve() {

        Solution solution = new Solution();
        solution.setName("Clarke-Wright heuristic");

        Long start = System.currentTimeMillis();

        Route[] routes = new Route[n - 1];

        for (int i = 1; i < n; i++) {
            // Create route
            routes[i - 1] = new Route(n, i - 1);
            // Add edge from depot to customer
            routes[i - 1].addEdge(new Edge(customers[0], customers[i], c));
            // Add edge from customer to depot
            routes[i - 1].addEdge(new Edge(customers[i], customers[0], c));
        }

        // Calculate all possible savings
        ArrayList<Saving> savingsList = computeSavings(customers, c);

        // Do all savings that are meaningful
        while (!savingsList.isEmpty() && savingsList.get(0).getSaving() > 0.0) {

            // Get first saving in the list (the best saving)
            Saving saving = savingsList.get(0);
            Customer customerI = saving.getI();
            Customer customerJ = saving.getJ();
            int intRouteI = customerI.getRoute();
            int intRouteJ = customerJ.getRoute();
            Route routeI = routes[intRouteI];
            Route routeJ = routes[intRouteJ];

            // Only use saving if customers are in different routes and new route is feasible
            if (intRouteI != intRouteJ && routeI.getWeight() + routeJ.getWeight() <= Q) {
                boolean success = routeI.merge(routeJ, saving, c);
                // If merge is successful, delete other route
                if (success) {
                    routes[intRouteJ] = null;
                }
            }

            // Remove saving
            savingsList.remove(0);
        }

        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        // Calculate costs
        double costs = 0.0;
        for (Route route : routes) {
            if (route != null) {
                costs += route.getCosts();
            }
        }
        solution.setObjectiveValue(costs);
        RouteSet[] scenarioRoutes = new RouteSet[nrOfScenarios];
        RouteSet sol = new RouteSet();
        sol.setCustomers(customers);
        sol.setRoutes(routes);
        sol.setRouteLength(costs);
        for (int i = 0; i < nrOfScenarios; i++) {
            scenarioRoutes[i] = sol;
        }
        solution.setRoutes(scenarioRoutes);

        return solution;
    }

    /**
     * Calculate all possible savings
     *
     * @return all possible savings
     */
    private ArrayList<Saving> computeSavings(Customer[] customers, double[][] c) {

        // Saving is done by driving from i to j instead of i to depot to j
        ArrayList<Saving> savingsList = new ArrayList<Saving>();
        for (int i = 1; i < customers.length; i++) {
            for (int j = i + 1; j < customers.length; j++) {
                savingsList.add(new Saving(c[0][i] + c[j][0] - lambda * c[i][j], customers[i], customers[j]));
            }
        }

        // Sort on value of saving
        Collections.sort(savingsList);
        return savingsList;
    }

}
