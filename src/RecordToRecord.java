/**
 * Created by Joeri on 19-5-2014.
 * Implementation Clarke Wright heuristic
 */
public class RecordToRecord implements Solver {

    private Route[] routes;
    private double record;
    private double deviation;

    /**
     * Implementation of Clarke-Wright heuristic for the DAVRP
     */
    public RecordToRecord() {

    }

    /**
     * Solve VRP for this dataset
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();
        solution.setName("Record2Record");

        // Parameters
        double I = 30;
        double K = 5;

        // Get some data from dataset
        int n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        //double[][] c = dataSet.getTravelCosts();
        Customer[] customers = dataSet.getCustomers();

        // Get largest demands
        int[] demands = new int[n];
        int highestDemand;
        for (int i = 1; i < demands.length; i++) {
            highestDemand = 0;
            // Get highest demand of all scenarios
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

        ClarkeWright cw = new ClarkeWright();

//        double[] lambdas = {0.6,1.0,1.4,1.6};
//        for (int lambda = 0; lambda < lambdas.length; lambda++) {
//            cw.setLambda(lambdas[lambda]);
//            System.out.println("Lambda: " + lambdas[lambda] + ", solution: " + cw.solve(dataSet).getObjectiveValue());
//        }
        Solution cwSolution = cw.solve(dataSet);
        routes = cwSolution.getRoutes();
        record = cwSolution.getObjectiveValue();
        deviation = 0.01 * record;

        double tourLength = record;

        // Start improvement iterations
        for (int k = 0; k < K; k++) {
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findOnePointMove(i, tourLength, Q);
                }
            }
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findTwoPointMove(i, tourLength, customers, Q);
                }
            }
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

    private double findOnePointMove(Customer i, double tourLength, int Q) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Route iRoute = routes[i.getRoute()];
        for (Route r : routes) {
            if (r != null) {
                for (Edge e : r.getEdges()) {
                    // Only if customer i is not already in edge e and new tour is allowed
                    if (e.getFrom().getId() != i.getId() && e.getTo().getId() != i.getId() && i.getDemand() + r.getWeight() <= Q) {
                        saving = 0.0;
                        // Remove customer i from its current route
                        saving += iRoute.getEdgeTo(i).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        saving -= iRoute.getEdgeTo(i).getFrom().getDistance(iRoute.getEdgeFrom(i).getTo());
                        // Insert customer i in edge e
                        saving += e.getDistance();
                        saving -= i.getDistance(e.getFrom());
                        saving -= i.getDistance(e.getTo());
                        if (saving >= 0.0) {
                            onePointMove(i, e);
                            return tourLength - saving;
                        } else if (saving > largestSaving) {
                            largestSaving = saving;
                            largestSavingEdge = e;
                        }
                    }
                }
            }
        }
        if (tourLength - largestSaving <= record + deviation) {
            onePointMove(i, largestSavingEdge);
            return tourLength - largestSaving;
        }
        return tourLength;
    }

    private void onePointMove(Customer i, Edge e) {
        Route iRoute = routes[i.getRoute()];
        Route r = routes[e.getRoute()];
        Customer to, from;
        // Remove customer i from its current route
        from = iRoute.getEdgeTo(i).getFrom();
        to = iRoute.getEdgeFrom(i).getTo();
        iRoute.removeEdgeTo(i);
        iRoute.removeEdgeFrom(i);
        iRoute.addEdge(new Edge(from, to));
        // Insert customer i in edge e
        r.removeEdge(e);
        r.addEdge(new Edge(e.getFrom(), i));
        r.addEdge(new Edge(i, e.getTo()));
    }

    private double findTwoPointMove(Customer i, double tourLength, Customer[] customers, int Q) {
        double saving = 0.0;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Customer largestSavingCustomer = null;
        Route iRoute = routes[i.getRoute()];
        Route jRoute;

        // For all customers
        for (Customer j : customers) {
            // Not if customers are the same or other customer is a depot
            if (i.getId() != j.getId() && j.getId() != 0) {
                jRoute = routes[j.getRoute()];
                // If new routes do not exceed vehicle capacity
                if (iRoute.getWeight() + j.getDemand() - i.getDemand() <= Q && jRoute.getWeight() + i.getDemand() - j.getDemand() <= Q) {
                    saving = 0.0;
                    // Delete i from route i
                    saving += iRoute.getEdgeTo(i).getDistance();
                    saving += iRoute.getEdgeFrom(i).getDistance();
                    // Adding j to route i
                    saving -= iRoute.getEdgeTo(i).getFrom().getDistance(j);
                    saving -= iRoute.getEdgeFrom(i).getTo().getDistance(j);
                    // Delete j from route j
                    saving += jRoute.getEdgeTo(j).getDistance();
                    saving += jRoute.getEdgeFrom(j).getDistance();
                    // Adding i to route j
                    saving -= jRoute.getEdgeTo(j).getFrom().getDistance(i);
                    saving -= jRoute.getEdgeFrom(j).getTo().getDistance(i);
                    if (saving >= 0.0) {
                        twoPointMove(i, j);
                        return tourLength - saving;
                    } else if (saving > largestSaving) {
                        largestSaving = saving;
                        largestSavingCustomer = j;
                    }
                }
            }
        }
        if (tourLength - largestSaving <= record + deviation) {
            twoPointMove(i, largestSavingCustomer);
            return tourLength - largestSaving;
        }
        return tourLength;
    }

    private void twoPointMove(Customer i, Customer j) {
        Route iRoute = routes[i.getRoute()];
        Route jRoute = routes[j.getRoute()];
        Customer beforeI, afterI, beforeJ, afterJ;

        // Save info
        beforeI = iRoute.getEdgeTo(i).getFrom();
        afterI = iRoute.getEdgeFrom(i).getTo();
        beforeJ = jRoute.getEdgeTo(j).getFrom();
        afterJ = jRoute.getEdgeFrom(j).getTo();
        // Check whether i and j are succeeding
        if (afterI.getId() == j.getId()) {
            // Delete i and j from route
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            jRoute.removeEdgeFrom(j);
            // Create connections again
            iRoute.addEdge(new Edge(beforeI, j));
            iRoute.addEdge(new Edge(j, i));
            jRoute.addEdge(new Edge(i, afterJ));
        } else if (afterJ.getId() == i.getId()) {
            // Delete i and j from route
            iRoute.removeEdgeTo(j);
            iRoute.removeEdgeFrom(j);
            jRoute.removeEdgeFrom(i);
            // Create connections again
            iRoute.addEdge(new Edge(beforeJ, i));
            iRoute.addEdge(new Edge(i, j));
            jRoute.addEdge(new Edge(j, afterI));
        } else {
            // Delete i from route i
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            // Delete j from route j
            jRoute.removeEdgeTo(j);
            jRoute.removeEdgeFrom(j);
            // Adding j to route i
            iRoute.addEdge(new Edge(beforeI, j));
            iRoute.addEdge(new Edge(j, afterI));
            // Adding i to route j
            jRoute.addEdge(new Edge(beforeJ, i));
            jRoute.addEdge(new Edge(i, afterJ));
        }
    }

}
