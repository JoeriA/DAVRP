import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation Clarke Wright heuristic
 */
public class RecordToRecord implements Solver {

    private Route[] routes;
    private double record;
    private double deviation;
    private double tourLength;

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

        // Parameter
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

        Route[] bestRoutes = null;
        double bestValue = Double.POSITIVE_INFINITY;
        double initialRecord;
        Route r;

        for (double lambda = 0.6; lambda <= 2.0; lambda += 0.2) {
            cw.setLambda(lambda);
            Solution cwSolution = cw.solve(dataSet);
            routes = cwSolution.getRoutes();
            record = cwSolution.getObjectiveValue();
            deviation = 0.01 * record;

            tourLength = record;
            boolean onePoint, twoPoint, twoOpt;

            // Start improvement iterations
            for (int k = 0; k < K; k++) {
                initialRecord = record;
                // Moves with record to record
                loopI:
                for (Customer i : customers) {
                    if (i.getId() != 0) {
                        onePoint = findOnePointMove(i, Q, c, true);
                        twoPoint = findTwoPointMove(i, customers, Q, c, true);
                        r = routes[i.getRoute()];
                        twoOpt = findTwoOptMove(r.getEdgeFrom(i), Q, c, true);
                        if (!onePoint && !twoPoint && !twoOpt) {
                            break loopI;
                        }
                        // Update record when necessary
                        if (tourLength < record) {
                            record = tourLength;
                            deviation = 0.01 * record;
                        }
                    }
                }
                // Downhill moves
                for (Customer i : customers) {
                    if (i.getId() != 0) {
                        findOnePointMove(i, Q, c, false);
                        findTwoPointMove(i, customers, Q, c, false);
                        r = routes[i.getRoute()];
                        findTwoOptMove(r.getEdgeFrom(i), Q, c, false);
                        // Update record when necessary
                        if (tourLength < record) {
                            record = tourLength;
                            deviation = 0.01 * record;
                        }
                    }
                }
                // Stop loop when no new record is produced
                if (record == initialRecord) {
                    break;
                }
            }
            // Create copy before perturbing
            Route[] routeCopy = copyRoutes();
            double tourLengthCopy = tourLength;
            // Perturb the solution
            tourLength = perturb(customers, tourLength, Q, c);
            // Restore copy if perturbing results in worse solution
            if (tourLengthCopy < tourLength) {
                routes = routeCopy;
                tourLength = tourLengthCopy;
            }
            // Check if this solution is better than best solution
            if (tourLength < bestValue) {
                bestRoutes = copyRoutes();
                bestValue = tourLength;
            }
        }
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        // Calculate costs
        double costs = 0;
        for (Route route : bestRoutes) {
            if (route != null) {
                costs += route.getCosts();
            }
        }
        solution.setObjectiveValue(costs);
        solution.setRoutes(bestRoutes);

        return solution;
    }

    /**
     * Create a copy of the routes (hard copy without references)
     *
     * @return copy of the routes (hard copy without references)
     */
    private Route[] copyRoutes() {
        Route[] newRoutes = new Route[routes.length];
        for (int i = 0; i < routes.length; i++) {
            if (routes[i] != null) {
                newRoutes[i] = routes[i].getCopy();
            }
        }
        return newRoutes;
    }

    /**
     * Perturb the solution
     *
     * @param customers  arraylist of customers
     * @param tourLength current length of the tour
     * @param Q          vehicle capacity
     * @param c          distance matrix
     * @return new length of the tour
     */
    private double perturb(Customer[] customers, double tourLength, int Q, double[][] c) {
        ArrayList<Customer> perturbC = new ArrayList<Customer>();
        Route route;
        Customer customer;
        double sI;
        for (int i = 1; i < customers.length; i++) {
            sI = 0.0;
            customer = customers[i];
            route = routes[customers[i].getRoute()];
            sI += route.getEdgeTo(customer).getDistance();
            sI += route.getEdgeFrom(customer).getDistance();
            sI -= c[route.getEdgeTo(customer).getFrom().getId()][route.getEdgeFrom(customer).getTo().getId()];
            customer.setR(customer.getDemand() / sI);
            perturbC.add(customer);
        }
        Collections.sort(perturbC, new CustomerComparator());

        int m = Math.min(20, customers.length / 10);
        double saving = 0.0;
        for (int j = 0; j < m; j++) {
            customer = perturbC.get(j);
            saving += applyBestInsertion(customer, Q, c);
        }
        tourLength -= saving;
        return tourLength - saving;
    }

    /**
     * Apply cheapest insertion of a customer into any route
     *
     * @param customer customer to insert
     * @param Q        vehicle capacity
     * @param c        distance matrix
     * @return saving of inserting this customer into a route (negative costs)
     */
    private double applyBestInsertion(Customer customer, int Q, double[][] c) {

        double totalSaving = 0.0;
        // Remove customer from its route
        Route r = routes[customer.getRoute()];
        Customer from = r.getEdgeTo(customer).getFrom();
        Customer to = r.getEdgeFrom(customer).getTo();
        totalSaving += r.getEdgeTo(customer).getDistance();
        totalSaving += r.getEdgeFrom(customer).getDistance();
        totalSaving -= c[from.getId()][to.getId()];
        r.removeEdgeTo(customer);
        r.removeEdgeFrom(customer);
        r.addEdge(new Edge(from, to, c[from.getId()][to.getId()]));

        // Find cheapest insertion
        double bestSaving = Double.NEGATIVE_INFINITY;
        double saving;
        Edge bestEdge = null;
        // Iterate over all edges to find cheapest insertion
        for (Route route : routes) {
            if (route != null) {
                for (Edge e : route.getEdges()) {
                    saving = 0.0;
                    saving += e.getDistance();
                    saving -= c[e.getFrom().getId()][customer.getId()];
                    saving -= c[customer.getId()][e.getTo().getId()];
                    if (saving > bestSaving && route.getWeight() + customer.getDemand() <= Q) {
                        bestSaving = saving;
                        bestEdge = e;
                    }
                }
            }
        }
        // Perform cheapest insertion
        r = routes[bestEdge.getRoute()];
        r.removeEdge(bestEdge);
        r.addEdge(new Edge(bestEdge.getFrom(), customer, c[bestEdge.getFrom().getId()][customer.getId()]));
        r.addEdge(new Edge(customer, bestEdge.getTo(), c[customer.getId()][bestEdge.getTo().getId()]));
        return totalSaving + bestSaving;
    }

    /**
     * Find one point move
     *
     * @param i          point to move
     * @param Q          vehicle capacity
     * @param c          distance matrix
     * @param rtr        true if record to record must be applied, false for only downhill moves
     * @return new tour length
     */
    private boolean findOnePointMove(Customer i, int Q, double[][] c, boolean rtr) {
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
                            tourLength -= saving;
                            return true;
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
            tourLength -= largestSaving;
            return true;
        }
        return false;
    }

    /**
     * Perform one point move
     *
     * @param i point to move
     * @param e edge to insert customer in
     * @param c distance matrix
     */
    private void onePointMove(Customer i, Edge e, double[][] c) {
        Route iRoute = routes[i.getRoute()];
        Route r = routes[e.getRoute()];
        Customer to, from;
        // Remove customer i from its current route
        from = iRoute.getEdgeTo(i).getFrom();
        to = iRoute.getEdgeFrom(i).getTo();
        if (from.getId() == 0 && to.getId() == 0) {
            routes[i.getRoute()] = null;
        } else {
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            iRoute.addEdge(new Edge(from, to, c[from.getId()][to.getId()]));
        }
        // Insert customer i in edge e
        r.removeEdge(e);
        r.addEdge(new Edge(e.getFrom(), i, c[e.getFrom().getId()][i.getId()]));
        r.addEdge(new Edge(i, e.getTo(), c[i.getId()][e.getTo().getId()]));
    }

    /**
     * Find a two point move
     *
     * @param i          first customer to move
     * @param customers  arraylist of customers
     * @param Q          vehicle capacity
     * @param c          distance matrix
     * @param rtr        true if record to record must be applied, false for only downhill moves
     * @return new length of the tour
     */
    private boolean findTwoPointMove(Customer i, Customer[] customers, int Q, double[][] c, boolean rtr) {
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
                    tourLength -= saving;
                    return true;
                } else if (saving > largestSaving) {
                    largestSaving = saving;
                    largestSavingCustomer = j;
                }
            }
        }
        if (tourLength - largestSaving <= record + deviation && rtr) {
            twoPointMove(i, largestSavingCustomer, c);
            tourLength -= largestSaving;
            return true;
        }
        return false;
    }

    /**
     * Perform a two point move
     *
     * @param i first customer to move
     * @param j second customer to move
     * @param c distance matrix
     */
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

    /**
     * Find two opt move in this route
     *
     * @param e          edge to find move with
     * @param Q          vehicle capacity
     * @param c          distance matrix
     * @param rtr        true if record to record must be applied, false for only downhill moves
     * @return new length of the tour
     */
    private boolean findTwoOptMove(Edge e, int Q, double[][] c, boolean rtr) {
//        for (Route route : routes) {
//            if (route != null && route.getRouteNumber() != routes[e.getRoute()].getRouteNumber()) {
//                if (route.getWeight() + routes[e.getRoute()].getWeight() <= Q) {
//                    System.out.println("Could perform two opt move between routes");
//                }
//            }
//        }
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Customer startE = e.getFrom(), endE = e.getTo(), startF, endF;
        // Calculate saving for two opt move with each other edge in the route
        for (Edge f : routes[e.getRoute()].getEdges()) {
            if (e != f) {
                saving = 0.0;
                startF = f.getFrom();
                endF = f.getTo();
                saving += e.getDistance();
                saving += f.getDistance();
                saving -= c[startE.getId()][startF.getId()];
                saving -= c[endE.getId()][endF.getId()];
                // If it is profitable, perform the move
                if (saving >= 0.0) {
                    routes[e.getRoute()].twoOptMove(e, f, c);
                    tourLength -= saving;
                    return true;
                } else if (saving > largestSaving) {
                    largestSaving = saving;
                    largestSavingEdge = f;
                }
            }
        }
        // Perform least expensive move if record to record is true
        if (tourLength - largestSaving <= record + deviation && rtr) {
            routes[e.getRoute()].twoOptMove(e, largestSavingEdge, c);
            tourLength -= largestSaving;
            return true;
        }
        return false;
    }
}
