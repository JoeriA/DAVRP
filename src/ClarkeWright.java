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

    }

    /**
     * Solve VRP for this dataset for scenario with all largest demands
     * Run for all lambdas and return best solution
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
//        double[] lambdas = new double[]{0.6, 1.0, 1.4};
//        double[] lambdas = new double[]{0.4, 1.0};

        for (double lambda : lambdas) {
            Solution sol = solve(dataSet, lambda);
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

    /**
     * Solve VRP for this dataset for scenario with all largest demands
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet, double lambda) {

        this.lambda = lambda;

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

        return solve(false);
    }

    /**
     * Solve a VRP with Clarke-Wright for a certain scenario
     *
     * @param dataSet  dataset to be solved
     * @param scenario scenario number (starting with 1)
     * @return solution to the problem
     */
    public Solution solve(DataSet dataSet, double lambda, int scenario) {

        this.lambda = lambda;

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        // Get demands
        for (Customer c : customers) {
            c.setDemand(c.getDemandPerScenario()[scenario - 1]);
        }

        return solve(false);
    }

    public Solution solve(DataSet dataSet, double lambda, Solution solutionClustering) {
        this.lambda = lambda;

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        nrOfScenarios = dataSet.getNumberOfScenarios();
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        Solution solution = new Solution();
        solution.setName("Clarke-Wright with pre-clustering");
        solution.setRoutes(new RouteSet[nrOfScenarios]);

        Long start = System.currentTimeMillis();

        double[][] assignments = solutionClustering.getzSol();
        double[][][] skips = solutionClustering.getzSolSkip();

        double costs = 0.0;

        for (int scenario = 0; scenario < nrOfScenarios; scenario++) {
            // Set demands
            for (Customer c : customers) {
                c.setDemand(c.getDemandPerScenario()[scenario]);
                c.setAssignedRoute(0);
            }
            // Assign all routes
            for (int i = 0; i < assignments.length; i++) {
                for (int j = 0; j < assignments[i].length; j++) {
                    if (skips[i][j][scenario] == 1) {
                        customers[i].setAssignedRoute(j);
                    }
                }
            }
            solution.getRoutes()[scenario] = solve(true).getRoutes()[scenario].getCopy();
            costs += solution.getRoutes()[scenario].getRouteLength() * dataSet.getScenarioProbabilities()[scenario];
        }

        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(costs);

        return solution;
    }

    public Solution solve(DataSet dataSet, Solution solutionClustering) {

        // Get some data from dataset
        n = dataSet.getNumberOfCustomers() + 1;
        nrOfScenarios = dataSet.getNumberOfScenarios();
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
        customers = dataSet.getCustomers();

        Solution solution = new Solution();
        solution.setName("Clarke-Wright with pre-clustering");
        solution.setRoutes(new RouteSet[nrOfScenarios]);

        Long start = System.currentTimeMillis();

        double[][] assignments = solutionClustering.getzSol();
        double[][][] skips = solutionClustering.getzSolSkip();

        double[] lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
        double record = Double.POSITIVE_INFINITY;
        for (double lambda : lambdas) {
            this.lambda = lambda;
            double costs = 0.0;
            RouteSet[] routeSets = new RouteSet[nrOfScenarios];
            for (int scenario = 0; scenario < nrOfScenarios; scenario++) {
                // Set demands
                for (Customer c : customers) {
                    c.setDemand(c.getDemandPerScenario()[scenario]);
                    c.setAssignedRoute(0);
                }
                // Assign all routes
                for (int i = 0; i < assignments.length; i++) {
                    for (int j = 0; j < assignments[i].length; j++) {
                        if (skips[i][j][scenario] == 1) {
                            customers[i].setAssignedRoute(j);
                        }
                    }
                }
                routeSets[scenario] = solve(true).getRoutes()[scenario].getCopy();
                costs += routeSets[scenario].getRouteLength() * dataSet.getScenarioProbabilities()[scenario];
            }
//            System.out.println("Lambda: " + lambda + ", value: " + costs);
            if (costs < record) {
                solution.setRoutes(routeSets);
                solution.setObjectiveValue(costs);
                record = costs;
            }
        }

        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(record);

        return solution;
    }

    /**
     * Solve the VRP
     *
     * @return solution to the VRP
     */
    private Solution solve(boolean sameRoute) {

        Solution solution = new Solution();
        solution.setName("Clarke-Wright heuristic");

        Long start = System.currentTimeMillis();

        Route[] routes = new Route[n];

        for (int i = 1; i < n; i++) {
            // Create route
            routes[i] = new Route(n, i);
            // Add edge from depot to customer
            routes[i].addEdge(new Edge(customers[0], customers[i], c));
            // Add edge from customer to depot
            routes[i].addEdge(new Edge(customers[i], customers[0], c));
        }

        // Calculate all possible savings
        ArrayList<Saving> savingsList = computeSavings(customers, c);

        // Do all savings that are meaningful
        while (!savingsList.isEmpty()) {

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
                if (!sameRoute || customerI.getAssignedRoute() == customerJ.getAssignedRoute()) {
                    boolean success;
                    if (sameRoute && intRouteJ == customerJ.getAssignedRoute()) {
                        success = routeJ.merge(routeI, new Saving(saving.getSaving(), saving.getJ(), saving.getI()), c);
                        if (success) {
                            routes[intRouteI] = null;
                        }
                    } else {
                        success = routeI.merge(routeJ, saving, c);
                        if (success) {
                            routes[intRouteJ] = null;
                        }
                    }
                    // If merge is successful, delete other route

                }
            }

            // Remove saving
            savingsList.remove(0);
        }

        // Correct the route numbers (to correspond to the assigned number)
        if (sameRoute) {
            // Add routes to temporary list
            ArrayList<Route> temp = new ArrayList<Route>();
            for (Route route : routes) {
                if (route != null) {
                    temp.add(route);
                }
            }
            ArrayList<Route> empties = new ArrayList<Route>();
            // Create new list to store correct order
            routes = new Route[n];
            for (Route route : temp) {
                int routeNr = -1;
                int customer = 1;
                // Find real route number
                while (routeNr < 0) {
                    if (route.getCustomers()[customer] != null) {
                            routeNr = route.getCustomers()[customer].getAssignedRoute();
                    }
                    customer++;
                }

                // Correct location in array of the route
                // Store routes with skipped customers separately (because there can be more than one)
                if (routeNr == 0) {
                    empties.add(route);
                } else {
                    // Correct route number
                    route.setRouteNumber(routeNr);
                    routes[routeNr] = route;
                }
            }
            // Add route(s) with skipped customers
            for (Route route : empties) {
                int nr = 0;
                while (routes[nr] != null) {
                    nr++;
                }
                route.setRouteNumber(nr);
                route.emptyAssignment();
                routes[nr] = route;
            }
            for (Route route : routes) {
                if (route != null) {
                    // Correct current route number for customers
                    for (Customer c : route.getCustomers()) {
                        if (c != null && c.getId() != 0) {
                            c.setRoute(route.getRouteNumber());
                        }
                    }
                }
            }
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
