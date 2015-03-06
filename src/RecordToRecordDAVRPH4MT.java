import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * H3 with advanced parameters
 */
public class RecordToRecordDAVRPH4MT implements Callable<RouteSet> {

    private double epsilon = Math.pow(10.0, -10.0);
    private double record;
    private double deviation;
    private int K2;
    private int D2;
    private double[][] c;
    private int Q;
    private double alpha;
    private RouteSet routeSet;
    private int scenario;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRPH4MT(DataSet dataSet, RouteSet routeSet, int scenario, int K2, int D2) {
        // Parameters
        this.routeSet = routeSet;
        this.scenario = scenario;
        this.K2 = K2;
        this.D2 = D2;
        this.Q = dataSet.getVehicleCapacity();
        this.c = dataSet.getTravelCosts();
        this.alpha = dataSet.getAlpha();
    }

    /**
     * Solve VRP for this data set
     */
    public RouteSet call() {

        // Set demands of this scenario
        for (Customer customer : routeSet.getCustomers()) {
            customer.setDemand(customer.getDemandPerScenario()[scenario]);
        }
        for (Route route : routeSet.getRoutes()) {
            if (route != null) {
                route.recalculateWeight();
            }
        }

        // Insert skipped customers in better location
        for (Customer customer : routeSet.getCustomers()) {
            if (customer.getId() != 0 && customer.getAssignedRoute() != customer.getRoute()) {
                double saving = applyBestInsertion(customer);
                routeSet.setRouteLength(routeSet.getRouteLength() - saving);
            }
        }
        // Remove empty routes
        for (int i = 0; i < routeSet.getRoutes().length; i++) {
            if (routeSet.getRoutes()[i] != null && routeSet.getRoutes()[i].getEdges().size() == 0) {
                routeSet.getRoutes()[i] = null;
            }
        }

        record = routeSet.getRouteLength();
        deviation = 0.01 * record;
        RouteSet recordSet = routeSet.getCopy();

        int k = 0;
        while (k < K2) {
//                System.out.println("p: " + p);
//                System.out.println("k: " + k);
            // Start improvement iterations
            for (int d = 0; d < D2; d++) {
                // Moves with record to record
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        findOnePointMove(i, true);
                        findTwoPointMove(i, true);
                        Route r = routeSet.getRoutes()[i.getRoute()];
                        findTwoOptMoveNew(r.getEdgeFrom(i), true);
                    }
                }
            }
            // Downhill moves
            boolean moveMade;
            do {
                moveMade = false;
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        boolean onePoint = findOnePointMove(i, false);
                        boolean twoPoint = findTwoPointMove(i, false);
                        Route r = routeSet.getRoutes()[i.getRoute()];
                        boolean twoOpt = findTwoOptMoveNew(r.getEdgeFrom(i), false);
                        if (onePoint || twoPoint || twoOpt) {
                            moveMade = true;
                        }
                    }
                }
            } while (moveMade);
            // Update record when necessary
            if (routeSet.getRouteLength() < record - epsilon) {
                record = routeSet.getRouteLength();
                deviation = 0.01 * record;
                recordSet = routeSet.getCopy();
                k = 0;
//                    System.out.println("record: " + record);
            }
            k++;
        }

        return recordSet;
    }

    /**
     * Apply cheapest insertion of a customer into any route
     *
     * @param customer customer to insert
     * @return saving of inserting this customer into a route (negative costs)
     */
    private double applyBestInsertion(Customer customer) {

        double totalSaving = 0.0;
        // Remove customer from its route
        Route r = routeSet.getRoutes()[customer.getRoute()];
        Customer from = r.getEdgeTo(customer).getFrom();
        Customer to = r.getEdgeFrom(customer).getTo();
        totalSaving += r.getEdgeTo(customer).getDistance();
        totalSaving += r.getEdgeFrom(customer).getDistance();
        totalSaving -= c[from.getId()][to.getId()];
        r.removeEdgeTo(customer);
        r.removeEdgeFrom(customer);
        r.addEdge(new Edge(from, to, c));

        // Find cheapest insertion
        double bestSaving = Double.NEGATIVE_INFINITY;
        double saving;
        Edge bestEdge = null;
        // Iterate over all edges to find cheapest insertion
        for (Route route : routeSet.getRoutes()) {
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
        assert bestEdge != null;
        r = routeSet.getRoutes()[bestEdge.getRoute()];
        r.removeEdge(bestEdge);
        r.addEdge(new Edge(bestEdge.getFrom(), customer, c));
        r.addEdge(new Edge(customer, bestEdge.getTo(), c));
        return totalSaving + bestSaving;
    }

    /**
     * Find one point move
     *
     * @param i   point to move
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findOnePointMove(Customer i, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        for (Neighbor neighbor : i.getNeighbors()) {
            Customer cN = routeSet.getCustomers()[neighbor.getNeighbor()];
            Route rN = routeSet.getRoutes()[cN.getRoute()];
            Edge[] edges = new Edge[2];
            edges[0] = rN.getEdgeTo(cN);
            edges[1] = rN.getEdgeFrom(cN);
            for (Edge e : edges) {
                // Only if customer i is not already in edge e
                if (e.getFrom().getId() != i.getId() && e.getTo().getId() != i.getId()) {
                    // If the move is feasible
                    if (rN.addCustomerFeasible(i, Q) && iRoute.removeCustomerFeasible(i, alpha)) {
                        saving = 0.0;
                        // Remove customer i from its current route
                        saving += iRoute.getEdgeTo(i).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        saving -= c[iRoute.getEdgeTo(i).getFrom().getId()][iRoute.getEdgeFrom(i).getTo().getId()];
                        // Insert customer i in edge e
                        saving += e.getDistance();
                        saving -= c[i.getId()][e.getFrom().getId()];
                        saving -= c[i.getId()][e.getTo().getId()];
                        if (saving > epsilon) {
                            onePointMove(i, e);
                            routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                            return true;
                        } else if (saving > largestSaving) {
                            largestSaving = saving;
                            largestSavingEdge = e;
                        }
                    }
                }
            }
        }
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            onePointMove(i, largestSavingEdge);
            routeSet.setRouteLength(routeSet.getRouteLength() - largestSaving);
            return true;
        }
        return false;
    }

    /**
     * Perform one point move
     *
     * @param i point to move
     * @param e edge to insert customer in
     */
    private void onePointMove(Customer i, Edge e) {
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        Route r = routeSet.getRoutes()[e.getRoute()];
        Customer to, from;
        // Remove customer i from its current route
        from = iRoute.getEdgeTo(i).getFrom();
        to = iRoute.getEdgeFrom(i).getTo();
        if (from.getId() == 0 && to.getId() == 0) {
            routeSet.getRoutes()[i.getRoute()] = null;
        } else {
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            iRoute.addEdge(new Edge(from, to, c));
        }
        // Insert customer i in edge e
        r.removeEdge(e);
        r.addEdge(new Edge(e.getFrom(), i, c));
        r.addEdge(new Edge(i, e.getTo(), c));
    }

    /**
     * Find a two point move
     *
     * @param i   first customer to move
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findTwoPointMove(Customer i, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Customer largestSavingCustomer = null;
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        Route jRoute;

        // For all customers
        for (Neighbor neighbor : i.getNeighbors()) {
            Customer j = routeSet.getCustomers()[neighbor.getNeighbor()];
            // Not if customers are the same or other customer is a depot
            if (i.getId() != j.getId() && j.getId() != 0) {
                jRoute = routeSet.getRoutes()[j.getRoute()];
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
                        saving -= c[beforeI.getId()][j.getId()];
                        saving -= c[j.getId()][i.getId()];
                        saving -= c[i.getId()][afterJ.getId()];
                    } else if (afterJ.getId() == i.getId()) {
                        // Delete i and j from route
                        saving += jRoute.getEdgeTo(j).getDistance();
                        saving += jRoute.getEdgeFrom(j).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        // Create connections again
                        saving -= c[beforeJ.getId()][i.getId()];
                        saving -= c[i.getId()][j.getId()];
                        saving -= c[j.getId()][afterI.getId()];
                    } else {
                        // Delete i from route i
                        saving += iRoute.getEdgeTo(i).getDistance();
                        saving += iRoute.getEdgeFrom(i).getDistance();
                        // Adding j to route i
                        saving -= c[beforeI.getId()][j.getId()];
                        saving -= c[afterI.getId()][j.getId()];
                        // Delete j from route j
                        saving += jRoute.getEdgeTo(j).getDistance();
                        saving += jRoute.getEdgeFrom(j).getDistance();
                        // Adding i to route j
                        saving -= c[beforeJ.getId()][i.getId()];
                        saving -= c[afterJ.getId()][i.getId()];
                    }
                    // If i and j in different routes and new routes are feasible
                } else if (iRoute.swapCustomersFeasible(i, j, alpha, Q) && jRoute.swapCustomersFeasible(j, i, alpha, Q)) {
                    // Delete i from route i
                    saving += iRoute.getEdgeTo(i).getDistance();
                    saving += iRoute.getEdgeFrom(i).getDistance();
                    // Adding j to route i
                    saving -= c[beforeI.getId()][j.getId()];
                    saving -= c[afterI.getId()][j.getId()];
                    // Delete j from route j
                    saving += jRoute.getEdgeTo(j).getDistance();
                    saving += jRoute.getEdgeFrom(j).getDistance();
                    // Adding i to route j
                    saving -= c[beforeJ.getId()][i.getId()];
                    saving -= c[afterJ.getId()][i.getId()];

                } else {
                    saving = Double.NEGATIVE_INFINITY;
                }
                if (saving > epsilon) {
                    twoPointMove(i, j);
                    routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                    return true;
                } else if (saving > largestSaving) {
                    largestSaving = saving;
                    largestSavingCustomer = j;
                }
            }
        }
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            twoPointMove(i, largestSavingCustomer);
            routeSet.setRouteLength(routeSet.getRouteLength() - largestSaving);
            return true;
        }
        return false;
    }

    /**
     * Perform a two point move
     *
     * @param i first customer to move
     * @param j second customer to move
     */
    private void twoPointMove(Customer i, Customer j) {
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        Route jRoute = routeSet.getRoutes()[j.getRoute()];
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
            iRoute.addEdge(new Edge(beforeI, j, c));
            iRoute.addEdge(new Edge(j, i, c));
            jRoute.addEdge(new Edge(i, afterJ, c));
        } else if (afterJ.getId() == i.getId()) {
            // Delete i and j from route
            iRoute.removeEdgeTo(j);
            iRoute.removeEdgeFrom(j);
            jRoute.removeEdgeFrom(i);
            // Create connections again
            iRoute.addEdge(new Edge(beforeJ, i, c));
            iRoute.addEdge(new Edge(i, j, c));
            jRoute.addEdge(new Edge(j, afterI, c));
        } else {
            // Delete i from route i
            iRoute.removeEdgeTo(i);
            iRoute.removeEdgeFrom(i);
            // Delete j from route j
            jRoute.removeEdgeTo(j);
            jRoute.removeEdgeFrom(j);
            // Adding j to route i
            iRoute.addEdge(new Edge(beforeI, j, c));
            iRoute.addEdge(new Edge(j, afterI, c));
            // Adding i to route j
            jRoute.addEdge(new Edge(beforeJ, i, c));
            jRoute.addEdge(new Edge(i, afterJ, c));
        }
    }

    /**
     * Find two opt move
     *
     * @param e   edge to find move with
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findTwoOptMoveNew(Edge e, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        int twoOptMode;
        int largestTwoOptMode = -1;
        Edge largestSavingEdge = null;
        Customer startE = e.getFrom(), endE = e.getTo(), startF, endF;
        ArrayList<Neighbor> mergedNeighbors = new ArrayList<Neighbor>();
        if (e.getFrom().getId() != 0) {
            for (Neighbor neighbor : e.getFrom().getNeighbors()) {
                mergedNeighbors.add(neighbor);
            }
        }
        if (e.getTo().getId() != 0) {
            for (Neighbor neighbor : e.getTo().getNeighbors()) {
                mergedNeighbors.add(neighbor);
            }
        }
        // Calculate saving for two opt move with each other edge in the route
        for (Neighbor neighbor : mergedNeighbors) {
            Customer cN = routeSet.getCustomers()[neighbor.getNeighbor()];
            Route rN = routeSet.getRoutes()[cN.getRoute()];
            Edge[] edges = new Edge[2];
            edges[0] = rN.getEdgeTo(cN);
            edges[1] = rN.getEdgeFrom(cN);
            for (Edge f : edges) {
                saving = 0.0;
                startF = f.getFrom();
                endF = f.getTo();
                saving += e.getDistance();
                saving += f.getDistance();
                if (e.getRoute() == f.getRoute() && twoOptMoveFeasible(e, f, 0)) {
                    saving -= c[startE.getId()][startF.getId()];
                    saving -= c[endE.getId()][endF.getId()];
                    twoOptMode = 0;
                } else {
                    double saving2 = saving;
                    saving -= c[startE.getId()][endF.getId()];
                    saving -= c[endE.getId()][startF.getId()];
                    saving2 -= c[startE.getId()][startF.getId()];
                    saving2 -= c[endE.getId()][endF.getId()];
                    if (saving2 > saving) {
                        if (twoOptMoveFeasible(e, f, 2)) {
                            twoOptMode = 2;
                            saving = saving2;
                        } else if (twoOptMoveFeasible(e, f, 3)) {
                            twoOptMode = 3;
                            saving = saving2;
                        } else if (twoOptMoveFeasible(e, f, 1)) {
                            twoOptMode = 1;
                        } else if (twoOptMoveFeasible(e, f, 4)) {
                            twoOptMode = 4;
                        } else {
                            continue;
                        }
                    } else {
                        if (twoOptMoveFeasible(e, f, 1)) {
                            twoOptMode = 1;
                        } else if (twoOptMoveFeasible(e, f, 4)) {
                            twoOptMode = 4;
                        } else if (twoOptMoveFeasible(e, f, 2)) {
                            twoOptMode = 2;
                            saving = saving2;
                        } else if (twoOptMoveFeasible(e, f, 3)) {
                            twoOptMode = 3;
                            saving = saving2;
                        } else {
                            continue;
                        }
                    }
                }
                // If it is profitable, perform the move
                if (saving > epsilon) {
                    twoOptMove(e, f, twoOptMode);
                    routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                    return true;
                } else if (saving > largestSaving) {
                    largestSaving = saving;
                    largestSavingEdge = f;
                    largestTwoOptMode = twoOptMode;
                }
            }
        }
        // Perform least expensive move if record to record is true
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            twoOptMove(e, largestSavingEdge, largestTwoOptMode);
            routeSet.setRouteLength(routeSet.getRouteLength() - largestSaving);
            return true;
        }
        return false;
    }

    /**
     * Perform a two opt move
     *
     * @param e first edge
     * @param f second edge
     * @return true if a move is made, false otherwise
     */
    private boolean twoOptMove(Edge e, Edge f, int mode) {
        if (e.getRoute() == f.getRoute()) {
            routeSet.getRoutes()[e.getRoute()].twoOptMove(e, f, c);
            return true;
        }
        Route rE = routeSet.getRoutes()[e.getRoute()];
        Route rF = routeSet.getRoutes()[f.getRoute()];
        ArrayList<Edge> eToF = new ArrayList<Edge>();
        ArrayList<Edge> fToE = new ArrayList<Edge>();
        if (mode == 1 || mode == 2) {
            Customer next = e.getTo();
            // Add edges to be removed from e and added to f to a list
            while (next.getId() != 0) {
                eToF.add(rE.getEdgeFrom(next));
                next = rE.getEdgeFrom(next).getTo();
            }
        } else if (mode == 3 || mode == 4) {
            Customer next = e.getFrom();
            // Add edges to be removed from e and added to f to a list
            while (next.getId() != 0) {
                eToF.add(rE.getEdgeTo(next));
                next = rE.getEdgeTo(next).getFrom();
            }
        }
        if (mode == 1 || mode == 3) {
            // Add edges to be removed from f and added to e to a list
            Customer next = f.getTo();
            while (next.getId() != 0) {
                fToE.add(rF.getEdgeFrom(next));
                next = rF.getEdgeFrom(next).getTo();
            }
        } else if (mode == 2 || mode == 4) {
            // Add edges to be removed from f and added to e to a list
            Customer next = f.getFrom();
            while (next.getId() != 0) {
                fToE.add(rF.getEdgeTo(next));
                next = rF.getEdgeTo(next).getFrom();
            }
        }
        // Perform move
        rE.removeEdge(e);
        rF.removeEdge(f);
        for (Edge edge : eToF) {
            rE.removeEdge(edge);
        }
        for (Edge edge : fToE) {
            rF.removeEdge(edge);
        }
        if (mode == 1) {
            for (Edge edge : fToE) {
                rE.addEdge(edge);
            }
            for (Edge edge : eToF) {
                rF.addEdge(edge);
            }
            rE.addEdge(new Edge(e.getFrom(), f.getTo(), c));
            rF.addEdge(new Edge(f.getFrom(), e.getTo(), c));
        } else if (mode == 2) {
            for (Edge edge : fToE) {
                rE.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            for (Edge edge : eToF) {
                rF.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            rE.addEdge(new Edge(e.getFrom(), f.getFrom(), c));
            rF.addEdge(new Edge(e.getTo(), f.getTo(), c));
        } else if (mode == 3) {
            for (Edge edge : fToE) {
                rE.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            for (Edge edge : eToF) {
                rF.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            rE.addEdge(new Edge(f.getTo(), e.getTo(), c));
            rF.addEdge(new Edge(f.getFrom(), e.getFrom(), c));
        } else if (mode == 4) {
            for (Edge edge : fToE) {
                rE.addEdge(edge);
            }
            for (Edge edge : eToF) {
                rF.addEdge(edge);
            }
            rE.addEdge(new Edge(f.getFrom(), e.getTo(), c));
            rF.addEdge(new Edge(e.getFrom(), f.getTo(), c));
        }

        return true;
    }

    /**
     * Check whether specified two opt move is feasible
     *
     * @param e first edge
     * @param f second edge
     * @return true if a move is made, false otherwise
     */
    private boolean twoOptMoveFeasible(Edge e, Edge f, int mode) {
        if (e.equals(f)) {
            return false;
        }
        if (e.getRoute() == f.getRoute()) {
            return true;
        }
        if ((mode == 1 || mode == 4) && (e.getTo() == f.getTo() || e.getFrom() == f.getFrom())) {
            return false;
        }
        if ((mode == 2 || mode == 3) && (e.getTo() == f.getFrom() || f.getTo() == e.getFrom())) {
            return false;
        }
        Route rE = routeSet.getRoutes()[e.getRoute()];
        Route rF = routeSet.getRoutes()[f.getRoute()];
        ArrayList<Customer> eToF = new ArrayList<Customer>();
        ArrayList<Customer> fToE = new ArrayList<Customer>();
        if (mode == 1 || mode == 2) {
            Customer next = e.getTo();
            // Add edges to be removed from e and added to f to a list
            while (next.getId() != 0) {
                eToF.add(next);
                next = rE.getEdgeFrom(next).getTo();
            }
        } else if (mode == 3 || mode == 4) {
            Customer next = e.getFrom();
            // Add edges to be removed from e and added to f to a list
            while (next.getId() != 0) {
                eToF.add(next);
                next = rE.getEdgeTo(next).getFrom();
            }
        }
        if (mode == 1 || mode == 3) {
            // Add edges to be removed from f and added to e to a list
            Customer next = f.getTo();
            while (next.getId() != 0) {
                fToE.add(next);
                next = rF.getEdgeFrom(next).getTo();
            }
        } else if (mode == 2 || mode == 4) {
            // Add edges to be removed from f and added to e to a list
            Customer next = f.getFrom();
            while (next.getId() != 0) {
                fToE.add(next);
                next = rF.getEdgeTo(next).getFrom();
            }
        }

        // Check if move is feasible
        return (rE.swapCustomersFeasible(eToF, fToE, alpha, Q) && rF.swapCustomersFeasible(fToE, eToF, alpha, Q));
    }
}
