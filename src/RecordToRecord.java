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
//        double I = 30;
        double K = 5;

        // Get some data from dataset
        int n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        double[][] c = dataSet.getTravelCosts();
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


        // TODO check whether clarke wright with different lambdas give different (better) solutions
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
        double initialRecord;

        // Start improvement iterations
        for (int k = 0; k < K; k++) {
            initialRecord = record;
            // One point moves with record to record
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findOnePointMove(i, tourLength, Q, c, true);
                }
            }
            // Two point moves with record to record
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findTwoPointMove(i, tourLength, customers, Q, c, true);
                }
            }
            // Two opt moves with record to record
            for (Route r : routes) {
                if (r != null) {
                    for (Edge e : r.getEdges()) {
                        tourLength = findTwoOptMove(e, tourLength, Q, c, true);
                    }
                }
            }
            // Update record when necessary
            if (tourLength < record) {
                record = tourLength;
                deviation = 0.01 * record;
            }
            // One point moves downhill
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findOnePointMove(i, tourLength, Q, c, false);
                }
            }
            // Two point moves downhill
            for (Customer i : customers) {
                if (i.getId() != 0) {
                    tourLength = findTwoPointMove(i, tourLength, customers, Q, c, false);
                }
            }
            // Two opt moves downhill
            for (Route r : routes) {
                if (r != null) {
                    for (Edge e : r.getEdges()) {
                        tourLength = findTwoOptMove(e, tourLength, Q, c, false);
                    }
                }
            }
            // Stop loop when no new record is produced
            if (tourLength == initialRecord) {
                break;
            }
        }

        // TODO perturb solution (maybe also try in loop)

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

    private double findOnePointMove(Customer i, double tourLength, int Q, double[][] c, boolean rtr) {
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
//                        saving -= iRoute.getEdgeTo(i).getFrom().getDistance(iRoute.getEdgeFrom(i).getTo());
                        saving -= c[iRoute.getEdgeTo(i).getFrom().getId()][iRoute.getEdgeFrom(i).getTo().getId()];
                        // Insert customer i in edge e
                        saving += e.getDistance();
//                        saving -= i.getDistance(e.getFrom());
                        saving -= c[i.getId()][e.getFrom().getId()];
//                        saving -= i.getDistance(e.getTo());
                        saving -= c[i.getId()][e.getTo().getId()];
                        if (saving >= 0.0) {
                            onePointMove(i, e, c);
                            return tourLength - saving;
                        } else if (saving > largestSaving) {
                            largestSaving = saving;
                            largestSavingEdge = e;
                        }
                    }
                }
            }
        }
        if (tourLength - largestSaving <= record + deviation && rtr) {
            onePointMove(i, largestSavingEdge, c);
            return tourLength - largestSaving;
        }
        return tourLength;
    }

    private void onePointMove(Customer i, Edge e, double[][] c) {
        Route iRoute = routes[i.getRoute()];
        Route r = routes[e.getRoute()];
        Customer to, from;
        // Remove customer i from its current route
        from = iRoute.getEdgeTo(i).getFrom();
        to = iRoute.getEdgeFrom(i).getTo();
        iRoute.removeEdgeTo(i);
        iRoute.removeEdgeFrom(i);
        iRoute.addEdge(new Edge(from, to, c[from.getId()][to.getId()]));
        // Insert customer i in edge e
        r.removeEdge(e);
        r.addEdge(new Edge(e.getFrom(), i, c[e.getFrom().getId()][i.getId()]));
        r.addEdge(new Edge(i, e.getTo(), c[i.getId()][e.getTo().getId()]));
    }

    private double findTwoPointMove(Customer i, double tourLength, Customer[] customers, int Q, double[][] c, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Customer largestSavingCustomer = null;
        Route iRoute = routes[i.getRoute()];
        Route jRoute;

        // For all customers
        for (Customer j : customers) {
            // Not if customers are the same or other customer is a depot
            if (i.getId() != j.getId() && j.getId() != 0) {
                jRoute = routes[j.getRoute()];
                saving = 0.0;
                // Save info
                Customer beforeI = iRoute.getEdgeTo(i).getFrom();
                Customer afterI = iRoute.getEdgeFrom(i).getTo();
                Customer beforeJ = jRoute.getEdgeTo(j).getFrom();
                Customer afterJ = jRoute.getEdgeFrom(j).getTo();
                // Check whether customers are in the same route
                if (iRoute.getRouteNumber() == jRoute.getRouteNumber()) {
                    // Check whether i and j are succeeding
                    if (afterI.getId() == j.getId()) {
                        // Delete i and j from route
                        saving += iRoute.getEdgeTo(i).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        saving += jRoute.getEdgeFrom(j).getDistance();
                        // Create connections again
//                        saving -= beforeI.getDistance(j);
                        saving -= c[beforeI.getId()][j.getId()];
//                        saving -= j.getDistance(i);
                        saving -= c[j.getId()][i.getId()];
//                        saving -= i.getDistance(afterJ);
                        saving -= c[i.getId()][afterJ.getId()];
                    } else if (afterJ.getId() == i.getId()) {
                        // Delete i and j from route
                        saving += jRoute.getEdgeTo(j).getDistance();
                        saving += jRoute.getEdgeFrom(j).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        // Create connections again
//                        saving -= beforeJ.getDistance(i);
                        saving -= c[beforeJ.getId()][i.getId()];
//                        saving -= i.getDistance(j);
                        saving -= c[i.getId()][j.getId()];
//                        saving -= j.getDistance(afterI);
                        saving -= c[j.getId()][afterI.getId()];
                    } else {
                        // Delete i from route i
                        saving += iRoute.getEdgeTo(i).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        // Adding j to route i
//                        saving -= beforeI.getDistance(j);
                        saving -= c[beforeI.getId()][j.getId()];
//                        saving -= afterI.getDistance(j);
                        saving -= c[afterI.getId()][j.getId()];
                        // Delete j from route j
                        saving += jRoute.getEdgeTo(j).getDistance();
                        saving += jRoute.getEdgeFrom(j).getDistance();
                        // Adding i to route j
//                        saving -= beforeJ.getDistance(i);
                        saving -= c[beforeJ.getId()][i.getId()];
//                        saving -= afterJ.getDistance(i);
                        saving -= c[afterJ.getId()][i.getId()];
                    }
                    // If i and j in different routes and new routes do not exceed vehicle capacity
                } else if (iRoute.getWeight() + j.getDemand() - i.getDemand() <= Q && jRoute.getWeight() + i.getDemand() - j.getDemand() <= Q) {
                    // Delete i from route i
                    saving += iRoute.getEdgeTo(i).getDistance();
                    saving += iRoute.getEdgeFrom(i).getDistance();
                    // Adding j to route i
//                        saving -= beforeI.getDistance(j);
                    saving -= c[beforeI.getId()][j.getId()];
//                        saving -= afterI.getDistance(j);
                    saving -= c[afterI.getId()][j.getId()];
                    // Delete j from route j
                    saving += jRoute.getEdgeTo(j).getDistance();
                    saving += jRoute.getEdgeFrom(j).getDistance();
                    // Adding i to route j
//                        saving -= beforeJ.getDistance(i);
                    saving -= c[beforeJ.getId()][i.getId()];
//                        saving -= afterJ.getDistance(i);
                    saving -= c[afterJ.getId()][i.getId()];

                } else {
                    saving = Double.NEGATIVE_INFINITY;
                }
                if (saving >= 0.0) {
                    twoPointMove(i, j, c);
                    return tourLength - saving;
                } else if (saving > largestSaving) {
                    largestSaving = saving;
                    largestSavingCustomer = j;
                }
            }
        }
        if (tourLength - largestSaving <= record + deviation && rtr) {
            twoPointMove(i, largestSavingCustomer, c);
            return tourLength - largestSaving;
        }
        return tourLength;
    }

    private void twoPointMove(Customer i, Customer j, double[][] c) {
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
            iRoute.addEdge(new Edge(beforeI, j, c[beforeI.getId()][j.getId()]));
            iRoute.addEdge(new Edge(j, i, c[j.getId()][i.getId()]));
            jRoute.addEdge(new Edge(i, afterJ, c[i.getId()][afterJ.getId()]));
        } else if (afterJ.getId() == i.getId()) {
            // Delete i and j from route
            iRoute.removeEdgeTo(j);
            iRoute.removeEdgeFrom(j);
            jRoute.removeEdgeFrom(i);
            // Create connections again
            iRoute.addEdge(new Edge(beforeJ, i, c[beforeJ.getId()][i.getId()]));
            iRoute.addEdge(new Edge(i, j, c[i.getId()][j.getId()]));
            jRoute.addEdge(new Edge(j, afterI, c[j.getId()][afterI.getId()]));
        } else {
            // Delete i from route i
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            // Delete j from route j
            jRoute.removeEdgeTo(j);
            jRoute.removeEdgeFrom(j);
            // Adding j to route i
            iRoute.addEdge(new Edge(beforeI, j, c[beforeI.getId()][j.getId()]));
            iRoute.addEdge(new Edge(j, afterI, c[j.getId()][afterI.getId()]));
            // Adding i to route j
            jRoute.addEdge(new Edge(beforeJ, i, c[beforeJ.getId()][i.getId()]));
            jRoute.addEdge(new Edge(i, afterJ, c[i.getId()][afterJ.getId()]));
        }
    }

    private double findTwoOptMove(Edge e, double tourLength, int Q, double[][] c, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Customer startE = e.getFrom(), endE = e.getTo(), startF, endF;
        for (Edge f : routes[e.getRoute()].getEdges()) {
            saving = 0.0;
            startF = f.getFrom();
            endF = f.getTo();
            saving += e.getDistance();
            saving += f.getDistance();
            saving += c[startE.getId()][endF.getId()];
            saving -= c[startF.getId()][endE.getId()];
            if (saving >= 0.0) {
                twoOptMove(e, f, c);
                return tourLength - saving;
            }
        }
        // TODO check if 2-opt between routes is promising (routes must be combined, check vehicle capacity)
        if (tourLength - largestSaving <= record + deviation && rtr) {
            twoOptMove(e, largestSavingEdge, c);
            return tourLength - largestSaving;
        }
        return tourLength;
    }

    private void twoOptMove(Edge e, Edge f, double[][] c) {
        // TODO twoOptMove
    }

}
