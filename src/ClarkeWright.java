import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Joeri on 19-5-2014.
 */
public class ClarkeWright implements Solver {

    private Solution solution;
    private int n;
    private double[][] c;
    private Customer[] customers;

    public ClarkeWright() {
        solution = new Solution();
        solution.setName("Clarke-Wright heuristic");
    }

    public void solve(DataSet dataSet) {
        // Get some data from dataset
        this.n = dataSet.getNumberOfCustomers() + 1;
        int m = dataSet.getNumberOfVehicles();
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        this.customers = dataSet.getCustomers();
        this.c = dataSet.getTravelCosts();
        double alpha = dataSet.getAlpha();

        // Get largest demands
        int[] demands = new int[n];
        int highestDemand;
        for (int i = 1; i < demands.length; i++) {
            highestDemand = 0;
            for (int k = 0; k < o; k++) {
                if (customers[i].getDemandPerScenario()[k] > highestDemand) {
                    highestDemand = customers[i].getDemandPerScenario()[k];
                }
            }
            demands[i] = highestDemand;
            customers[i].setDemand(highestDemand);
        }
        customers[0].setDemand(0);

        Long start = System.currentTimeMillis();

        Route[] routes = new Route[n - 1];

        for (int i = 1; i < n; i++) {
            // Create route
            routes[i - 1] = new Route(n, i - 1);
            // Add edge from depot to customer
            routes[i - 1].addEdge(new Edge(customers[0], customers[i]));
            // Add edge from customer to depot
            routes[i - 1].addEdge(new Edge(customers[i], customers[0]));
        }

        ArrayList<Saving> savingsList = computeSavings();

        Saving saving;
        Customer customerI, customerJ;
        int intRouteI, intRouteJ;
        Route routeI, routeJ;

        while (!savingsList.isEmpty()) {

            // Get first saving in the list (the best saving)
            saving = savingsList.get(0);
            customerI = saving.getI();
            customerJ = saving.getJ();
            intRouteI = customerI.getRoute();
            intRouteJ = customerJ.getRoute();
            routeI = routes[intRouteI];
            routeJ = routes[intRouteJ];

            if (saving.getSaving() > 0 && intRouteI != intRouteJ && routeI.getWeight() + routeJ.getWeight() <= Q) {
                boolean success = routeI.merge(routeJ, saving);
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
        for (int i = 0; i < routes.length; i++) {
            if (routes[i] != null) {
                costs += routes[i].getCosts();
            }
        }
        solution.setObjectiveValue(costs);
        solution.setRoutes(routes);
    }

    private ArrayList<Saving> computeSavings() {

        ArrayList<Saving> savingsList = new ArrayList<Saving>();
        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                savingsList.add(new Saving(c[0][i] + c[j][0] - c[i][j], customers[i], customers[j]));
            }
        }
        Collections.sort(savingsList);
        return savingsList;
    }

    public Solution getSolution() {
        return solution;
    }

}
