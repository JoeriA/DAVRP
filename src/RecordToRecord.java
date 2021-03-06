import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 */
public class RecordToRecord implements Solver {

    private double record;
    private double deviation;
    private boolean[][] neighborList;
    private double K;
    private double alpha;
    private RouteSet routeSet;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecord() {
        // Parameters
        K = 5;
        alpha = 0.6;
    }

    /**
     * Solve VRP for this data set
     *
     * @param dataSet data set to be solved
     */
    public Solution solve(DataSet dataSet) {

        return solve(dataSet, 0);
    }

    /**
     * Solve VRP for this scenario
     *
     * @param dataSet  data set to be solved
     * @param scenario number of scenario (starts with 1)
     * @return solution to the data set
     */
    public Solution solve(DataSet dataSet, int scenario) {

        Solution solution = new Solution();
        solution.setName("Record2Record");

        // Get some data from data set
        int n = dataSet.getNumberOfCustomers() + 1;
        int Q = dataSet.getVehicleCapacity();
        double[][] c = dataSet.getTravelCosts();

        routeSet = new RouteSet();
        routeSet.setCustomers(dataSet.getCustomers());

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
                if (c[i][j] <= alpha * largestDistance) {
                    neighborList[i][j] = true;
                    neighborList[j][i] = true;
                }
            }
        }

        ClarkeWright cw = new ClarkeWright();

        RouteSet bestRouteSet = new RouteSet();
        bestRouteSet.setRouteLength(Double.POSITIVE_INFINITY);
        double initialRecord;
        Route r;

        for (double lambda = 0.6; lambda <= 2.0; lambda += 0.2) {
            Solution cwSolution;
            if (scenario == 0) {
                cwSolution = cw.solve(dataSet, lambda);
                routeSet = cwSolution.getRoutes()[0];
            } else {
                cwSolution = cw.solve(dataSet, lambda, scenario);
                routeSet = cwSolution.getRoutes()[scenario - 1];
            }
            record = cwSolution.getObjectiveValue();
            deviation = 0.01 * record;

            routeSet.setRouteLength(record);
            boolean onePoint, twoPoint, twoOpt;

            // Start improvement iterations
            for (int k = 0; k < K; k++) {
                initialRecord = record;
                // Moves with record to record
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        onePoint = findOnePointMove(i, Q, c, true);
                        twoPoint = findTwoPointMove(i, Q, c, true);
                        r = routeSet.getRoutes()[i.getRoute()];
                        twoOpt = findTwoOptMoveNew(r.getEdgeFrom(i), Q, c, true);
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
                        findOnePointMove(i, Q, c, false);
                        findTwoPointMove(i, Q, c, false);
                        r = routeSet.getRoutes()[i.getRoute()];
                        findTwoOptMoveNew(r.getEdgeFrom(i), Q, c, false);
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
            // Create copy before perturbing
            RouteSet routeCopy = routeSet.getCopy();
            // Perturb the solution
            perturb(Q, c);
            // Restore copy if perturbing results in worse solution
            if (routeCopy.getRouteLength() < routeSet.getRouteLength()) {
                routeSet = routeCopy;
            }
            // Check if this solution is better than best solution
            if (routeSet.getRouteLength() < bestRouteSet.getRouteLength()) {
                bestRouteSet = routeSet.getCopy();
            }
        }
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(bestRouteSet.getRouteLength());
        solution.setAssignments(bestRouteSet.assignments());
        RouteSet[] sol = new RouteSet[dataSet.getNumberOfScenarios()];
        if (scenario == 0) {
            for (int i = 0; i < sol.length; i++) {
                sol[i] = bestRouteSet;
            }
        } else {
            sol[scenario - 1] = bestRouteSet;
        }
        solution.setRoutes(sol);

        SolutionChecker checker = new SolutionChecker();
        if (!checker.checkRoutes(solution, dataSet)) {
            System.out.println("Solution is not feasible");
            throw new IllegalStateException("Solution is not feasible");
        }

        return solution;
    }

    /**
     * Perturb the solution
     *
     * @param Q vehicle capacity
     * @param c distance matrix
     */
    private void perturb(int Q, double[][] c) {
        ArrayList<Customer> perturbC = new ArrayList<Customer>();
        Route route;
        Customer customer;
        double sI;
        for (int i = 1; i < routeSet.getCustomers().length; i++) {
            sI = 0.0;
            customer = routeSet.getCustomers()[i];
            route = routeSet.getRoutes()[routeSet.getCustomers()[i].getRoute()];
            sI += route.getEdgeTo(customer).getDistance();
            sI += route.getEdgeFrom(customer).getDistance();
            sI -= c[route.getEdgeTo(customer).getFrom().getId()][route.getEdgeFrom(customer).getTo().getId()];
            customer.setR(customer.getDemand() / sI);
            perturbC.add(customer);
        }
        Collections.sort(perturbC);

        int m = Math.min(20, routeSet.getCustomers().length / 10);
        double saving = 0.0;
        for (int j = 0; j < m; j++) {
            customer = perturbC.get(j);
            saving += applyBestInsertion(customer, Q, c);
        }
        routeSet.setRouteLength(routeSet.getRouteLength() - saving);
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
     * @param Q   vehicle capacity
     * @param c   distance matrix
     * @param rtr true if record to record must be applied, false for only downhill moves
     * @return true if a move is made, false otherwise
     */
    private boolean findOnePointMove(Customer i, int Q, double[][] c, boolean rtr) {
        double saving;
        double largestSaving = Double.NEGATIVE_INFINITY;
        Edge largestSavingEdge = null;
        Route iRoute = routeSet.getRoutes()[i.getRoute()];
        for (Route r : routeSet.getRoutes()) {
            if (r != null) {
                for (Edge e : r.getEdges()) {
                    if (neighborList[i.getId()][e.getFrom().getId()] || neighborList[i.getId()][e.getTo().getId()]) {
                        // Only if customer i is not already in edge e and new tour is allowed
                        if (e.getFrom().getId() != i.getId() && e.getTo().getId() != i.getId() && i.getDemand() + r.getWeight() <= Q) {
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
    private boolean findTwoPointMove(Customer i, int Q, double[][] c, boolean rtr) {
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
                        // If i and j in different routes and new routes do not exceed vehicle capacity
                    } else if (iRoute.getWeight() + j.getDemand() - i.getDemand() <= Q && jRoute.getWeight() + i.getDemand() - j.getDemand() <= Q) {
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
    private boolean findTwoOptMoveNew(Edge e, int Q, double[][] c, boolean rtr) {
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
                        if (saving >= 0.0 && twoOptMoveFeasible(e, f, Q)) {
                            twoOptMove(e, f, c);
                            routeSet.setRouteLength(routeSet.getRouteLength() - saving);
                            return true;
                        } else if (saving > largestSaving && twoOptMoveFeasible(e, f, Q)) {
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

        if (f.getFrom() == e.getTo()) {
            routeSet.getRoutes()[f.getRoute()] = null;
        }

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
    private boolean twoOptMoveFeasible(Edge e, Edge f, int Q) {
        if (e.equals(f)) {
            return false;
        }
        if (e.getRoute() == f.getRoute()) {
            return true;
        }
        Route rE = routeSet.getRoutes()[e.getRoute()];
        Route rF = routeSet.getRoutes()[f.getRoute()];
        int q1 = rE.getWeight();
        int q2 = rF.getWeight();
        Customer next = e.getTo();
        // Add edges to be removed from e and added to f to a list
        while (next.getId() != 0) {
            q1 -= next.getDemand();
            q2 += next.getDemand();
            next = rE.getEdgeFrom(next).getTo();
        }
        // Add edges to be removed from f and added to e to a list
        next = f.getTo();
        while (next.getId() != 0) {
            q1 += next.getDemand();
            q2 -= next.getDemand();
            next = rF.getEdgeFrom(next).getTo();
        }
        // Check if move is feasible
        return !(q1 > Q || q2 > Q);
    }
}
