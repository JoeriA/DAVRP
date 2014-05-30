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
    private int Q;
    private double[][] c;
    private Solution solution;

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
     * Solve VRP for this dataset
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet) {

        solution = new Solution();
        solution.setName("Clarke-Wright heuristic");

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        // Get largest demands
        int highestDemand;
        for (Customer c : customers) {
            highestDemand = 0;
            // Get highest demand of all scenarios
            for (int k = 0; k < o; k++) {
                if (c.getDemandPerScenario()[k] > highestDemand) {
                    highestDemand = c.getDemandPerScenario()[k];
                }
            }
            c.setDemand(highestDemand);
        }

        return solve();
    }

    public Solution solve(DataSet dataSet, int scenario) {

        solution = new Solution();
        solution.setName("Clarke-Wright heuristic");

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        // Get largest demands
        for (Customer c : customers) {
            c.setDemand(c.getDemandPerScenario()[scenario]);
        }

        return solve();
    }

    private Solution solve() {
        Long start = System.currentTimeMillis();

        Route[] routes = new Route[n - 1];

        for (int i = 1; i < n; i++) {
            // Create route
            routes[i - 1] = new Route(n, i - 1);
            // Add edge from depot to customer
            routes[i - 1].addEdge(new Edge(customers[0], customers[i], c[0][i]));
            // Add edge from customer to depot
            routes[i - 1].addEdge(new Edge(customers[i], customers[0], c[i][0]));
        }

        // Calculate all possible savings
        ArrayList<Saving> savingsList = computeSavings(customers, c);

        Saving saving;
        Customer customerI, customerJ;
        int intRouteI, intRouteJ;
        Route routeI, routeJ;

        // Do all savings that are meaningful
        while (!savingsList.isEmpty()) {

            // Get first saving in the list (the best saving)
            saving = savingsList.get(0);
            customerI = saving.getI();
            customerJ = saving.getJ();
            intRouteI = customerI.getRoute();
            intRouteJ = customerJ.getRoute();
            routeI = routes[intRouteI];
            routeJ = routes[intRouteJ];

            // Only use saving if it is useful, if customers are in different routes and new route is feasible
            if (saving.getSaving() > 0 && intRouteI != intRouteJ && routeI.getWeight() + routeJ.getWeight() <= Q) {
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
        double costs = 0;
        for (Route route : routes) {
            if (route != null) {
                costs += route.getCosts();
            }
        }
        solution.setObjectiveValue(costs);
        solution.setRoutes(routes);

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
