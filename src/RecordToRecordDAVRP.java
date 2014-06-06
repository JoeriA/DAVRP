import java.util.ArrayList;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 */
public class RecordToRecordDAVRP implements Solver {

    private double record;
    private double deviation;
    private boolean[][] neighborList;
    private double K;
    private double beta;
    private RouteSet routeSet;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordDAVRP() {
        // Parameters
        K = 5;
        beta = 0.6;
    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();
        solution.setName("RTR_DAVRP");

        // Get some data from data set
        int n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        double[][] c = dataSet.getTravelCosts();
        double alpha = dataSet.getAlpha();

        Long start = System.currentTimeMillis();

        // Create neighbor list
        neighborList = new boolean[n][n];
        double largestDistance;
        for (int i = 0; i < n; i++) {
            largestDistance = Double.NEGATIVE_INFINITY;
            for (int j = i + 1; j < n; j++) {
                if (c[i][j] > largestDistance) {
                    largestDistance = c[i][j];
                }
            }
            for (int j = i + 1; j < n; j++) {
                if (c[i][j] <= beta * largestDistance) {
                    neighborList[i][j] = true;
                    neighborList[j][i] = true;
                }
            }
        }

        // Create place to store intermediate solutions
        RouteSet[] solutions = new RouteSet[o];

        // Declare some variables
        double initialRecord;
        Route r;

        // Create a basis solution to start with
        RecordToRecord rtr = new RecordToRecord();
        Solution solutionBasis = rtr.solve(dataSet);
        RouteSet routeSetBasis = solutionBasis.getRoutes()[0];

        solution.setAssignments(routeSetBasis.assignments());

        double objectiveValue = 0.0;

        // Optimize basis solution for all scenarios
        for (int scenario = 0; scenario < o; scenario++) {

            // Set demands of this scenario
            routeSet = routeSetBasis.getCopy();
            for (Customer customer : routeSet.getCustomers()) {
                customer.setDemand(customer.getDemandPerScenario()[scenario]);
            }
            for (Route route : routeSet.getRoutes()) {
                if (route != null) {
                    route.recalculateWeight();
                }
            }

            // Set records
            record = routeSet.getRouteLength();
            deviation = 0.01 * record;

            routeSet.setRouteLength(record);
            boolean onePoint, twoPoint, twoOpt;

            // Start improvement iterations
            for (int k = 0; k < K; k++) {
                initialRecord = record;
                // Moves with record to record
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        onePoint = findOnePointMove(i, Q, c, alpha, true);
                        twoPoint = findTwoPointMove(i, Q, c, alpha, true);
                        r = routeSet.getRoutes()[i.getRoute()];
                        twoOpt = findTwoOptMoveNew(r.getEdgeFrom(i), Q, c, alpha, true);
                        if (!onePoint && !twoPoint && !twoOpt) {
                            break;
                        }
                        // Update record when necessary
                        if (routeSet.getRouteLength() < record) {
                            record = routeSet.getRouteLength();
                            deviation = 0.01 * record;
                        }
                    }
                }
                // Downhill moves
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        findOnePointMove(i, Q, c, alpha, false);
                        findTwoPointMove(i, Q, c, alpha, false);
                        r = routeSet.getRoutes()[i.getRoute()];
                        findTwoOptMoveNew(r.getEdgeFrom(i), Q, c, alpha, false);
                        // Update record when necessary
                        if (routeSet.getRouteLength() < record) {
                            record = routeSet.getRouteLength();
                            deviation = 0.01 * record;
                        }
                    }
                }
                // Stop loop when no new record is produced
                if (record == initialRecord) {
                    break;
                }
            }
            solutions[scenario] = routeSet.getCopy();
            objectiveValue += dataSet.getScenarioProbabilities()[scenario] * routeSet.getRouteLength();
        }
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setRoutes(solutions);
        solution.setObjectiveValue(objectiveValue);

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

    /**
     * Find one point move
     *
     * @param i   point to move
     * @param Q   vehicle capacity
     * @param c   distance matrix
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findOnePointMove(Customer i, int Q, double[][] c, double alpha, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        for (Route r : routeSet.getRoutes()) {
            if (r != null) {
                for (Edge e : r.getEdges()) {
                    // If edge is in neighbor list
                    if (neighborList[i.getId()][e.getFrom().getId()] || neighborList[i.getId()][e.getTo().getId()]) {
                        // Only if customer i is not already in edge e
                        if (e.getFrom().getId() != i.getId() && e.getTo().getId() != i.getId()) {
                            // If the move is feasible
                            if (r.addCustomerFeasible(i, Q) && iRoute.removeCustomerFeasible(i, alpha)) {
                                saving = 0.0;
                                // Remove customer i from its current route
                                saving += iRoute.getEdgeTo(i).getDistance();
                                saving += iRoute.getEdgeFrom(i).getDistance();
                                saving -= c[iRoute.getEdgeTo(i).getFrom().getId()][iRoute.getEdgeFrom(i).getTo().getId()];
                                // Insert customer i in edge e
                                saving += e.getDistance();
                                saving -= c[i.getId()][e.getFrom().getId()];
                                saving -= c[i.getId()][e.getTo().getId()];
                                if (saving >= 0.0) {
                                    onePointMove(i, e, c);
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
            }
        }
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            onePointMove(i, largestSavingEdge, c);
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
     * @param c distance matrix
     */
    private void onePointMove(Customer i, Edge e, double[][] c) {
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
     * @param Q   vehicle capacity
     * @param c   distance matrix
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findTwoPointMove(Customer i, int Q, double[][] c, double alpha, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Customer largestSavingCustomer = null;
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        Route jRoute;

        // For all customers
        for (Customer j : routeSet.getCustomers()) {
            if (neighborList[i.getId()][j.getId()]) {
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
                    if (saving >= 0.0) {
                        twoPointMove(i, j, c);
                        routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                        return true;
                    } else if (saving > largestSaving) {
                        largestSaving = saving;
                        largestSavingCustomer = j;
                    }
                }
            }
        }
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            twoPointMove(i, largestSavingCustomer, c);
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
     * @param c distance matrix
     */
    private void twoPointMove(Customer i, Customer j, double[][] c) {
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
     * @param Q   vehicle capacity
     * @param c   distance matrix
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findTwoOptMoveNew(Edge e, int Q, double[][] c, double alpha, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Customer startE = e.getFrom(), endE = e.getTo(), startF, endF;
        // Calculate saving for two opt move with each other edge in the route
        for (Route r : routeSet.getRoutes()) {
            if (r != null) {
                for (Edge f : r.getEdges()) {
                    if (neighborList[e.getFrom().getId()][f.getFrom().getId()] || neighborList[e.getFrom().getId()][f.getTo().getId()]) {
                        saving = 0.0;
                        startF = f.getFrom();
                        endF = f.getTo();
                        saving += e.getDistance();
                        saving += f.getDistance();
                        if (e.getRoute() == f.getRoute()) {
                            saving -= c[startE.getId()][startF.getId()];
                            saving -= c[endE.getId()][endF.getId()];
                        } else {
                            saving -= c[startE.getId()][endF.getId()];
                            saving -= c[endE.getId()][startF.getId()];
                        }
                        // If it is profitable, perform the move
                        if (saving >= 0.0 && twoOptMoveFeasible(e, f, Q, alpha)) {
                            twoOptMove(e, f, c);
                            routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                            return true;
                        } else if (saving > largestSaving && twoOptMoveFeasible(e, f, Q, alpha)) {
                            largestSaving = saving;
                            largestSavingEdge = f;
                        }
                    }
                }
            }
        }
        // Perform least expensive move if record to record is true
        if (routeSet.getRouteLength() - largestSaving <= record + deviation && rtr) {
            twoOptMove(e, largestSavingEdge, c);
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
     * @param c distance matrix
     * @return true if a move is made, false otherwise
     */
    private boolean twoOptMove(Edge e, Edge f, double[][] c) {
        if (e.getRoute() == f.getRoute()) {
            routeSet.getRoutes()[e.getRoute()].twoOptMove(e, f, c);
            return true;
        }
        Route rE = routeSet.getRoutes()[e.getRoute()];
        Route rF = routeSet.getRoutes()[f.getRoute()];
        ArrayList<Edge> edgesR1 = new ArrayList<Edge>();
        ArrayList<Edge> edgesR2 = new ArrayList<Edge>();
        Customer next = e.getTo();
        // Add edges to be removed from e and added to f to a list
        while (next.getId() != 0) {
            edgesR1.add(rE.getEdgeFrom(next));
            next = rE.getEdgeFrom(next).getTo();
        }
        // Add edges to be removed from f and added to e to a list
        next = f.getTo();
        while (next.getId() != 0) {
            edgesR2.add(rF.getEdgeFrom(next));
            next = rF.getEdgeFrom(next).getTo();
        }
        // Perform move
        rE.removeEdge(e);
        rF.removeEdge(f);
        for (Edge edge : edgesR1) {
            rE.removeEdge(edge);
        }
        for (Edge edge : edgesR2) {
            rF.removeEdge(edge);
        }
        for (Edge edge : edgesR1) {
            rF.addEdge(edge);
        }
        for (Edge edge : edgesR2) {
            rE.addEdge(edge);
        }
        rE.addEdge(new Edge(e.getFrom(), f.getTo(), c));
        rF.addEdge(new Edge(f.getFrom(), e.getTo(), c));

        return true;
    }

    /**
     * Check whether specified two opt move is feasible
     *
     * @param e first edge
     * @param f second edge
     * @param Q vehicle capacity
     * @return true if a move is made, false otherwise
     */
    private boolean twoOptMoveFeasible(Edge e, Edge f, int Q, double alpha) {
        if (e.equals(f)) {
            return false;
        }
        if (e.getRoute() == f.getRoute()) {
            return true;
        }
        Route rE = routeSet.getRoutes()[e.getRoute()];
        Route rF = routeSet.getRoutes()[f.getRoute()];
        ArrayList<Customer> eToF = new ArrayList<Customer>();
        ArrayList<Customer> fToE = new ArrayList<Customer>();
        Customer next = e.getTo();
        // Add edges to be removed from e and added to f to a list
        while (next.getId() != 0) {
            eToF.add(next);
            next = rE.getEdgeFrom(next).getTo();
        }
        // Add edges to be removed from f and added to e to a list
        next = f.getTo();
        while (next.getId() != 0) {
            fToE.add(next);
            next = rF.getEdgeFrom(next).getTo();
        }
        // Check if move is feasible
        return (rE.swapCustomersFeasible(eToF, fToE, alpha, Q) && rF.swapCustomersFeasible(fToE, eToF, alpha, Q));
    }
}
