import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Joeri on 19-5-2014.
 * Implementation record-to-record heuristic
 * Now with second 2-opt
 */
public class RecordToRecordH2 implements Solver {

    private double epsilon = Math.pow(10.0, -10.0);
    private double record;
    private double deviation;
    private int K;
    private int I;
    private int M;
    private int NBListSize;
    private double[][] c;
    private int Q;
    private double beta;
    private RouteSet routeSet;

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordH2() {
        // Parameters
        I = 30;
        K = 5;
        M = 1;
        NBListSize = 40;
        beta = 0.6;
    }

    /**
     * Implementation of record-to-record heuristic for the DAVRP
     */
    public RecordToRecordH2(int K, int I, int M, int NBListSize, double beta) {
        // Parameters
        this.K = K;
        this.I = I;
        this.M = M;
        this.NBListSize = NBListSize;
        this.beta = beta;
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
        solution.setName("Record2Record_H2");

        // Get some data from data set
        int n = dataSet.getNumberOfCustomers() + 1;
        Q = dataSet.getVehicleCapacity();
        c = dataSet.getTravelCosts();
//        M = (int) Math.max((n - 1) / 2.0, 30);

        routeSet = new RouteSet();
        routeSet.setCustomers(dataSet.getCustomers());

        Long start = System.currentTimeMillis();

        // Create neighbor lists
        for (Customer customer : routeSet.getCustomers()) {
            if (customer.getId() != 0) {
                ArrayList<Neighbor> neighborList = new ArrayList<Neighbor>(n - 2);
                for (Customer neighbor : routeSet.getCustomers()) {
                    if (neighbor.getId() != 0 && neighbor.getId() != customer.getId()) {
                        neighborList.add(new Neighbor(customer.getId(), neighbor.getId(), c));
                    }
                }
                Collections.sort(neighborList);
                int neighborListLength = Math.min(NBListSize - 1, n - 3);
                while (neighborList.size() > neighborListLength) {
                    neighborList.remove(0);
                }
                double criticalValue = neighborList.get(0).getDistance() * beta;
                while (neighborList.get(0).getDistance() > criticalValue) {
                    neighborList.remove(0);
                }
                Collections.sort(neighborList, Neighbor.neighborAscending);
                Collections.sort(neighborList, Neighbor.distanceAscending);
                customer.setNeighbors(neighborList);
            }
        }

        ClarkeWright cw = new ClarkeWright();

        RouteSet bestRouteSet = new RouteSet();
        bestRouteSet.setRouteLength(Double.POSITIVE_INFINITY);

        double[] lambdas = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
//        double[] lambdas = new double[]{0.6, 1.4, 1.6,};

        for (double lambda : lambdas) {
            cw.setLambda(lambda);
            Solution cwSolution;
            if (scenario == 0) {
                cwSolution = cw.solve(dataSet);
                routeSet = cwSolution.getRoutes()[0];
            } else {
                cwSolution = cw.solve(dataSet, scenario);
                routeSet = cwSolution.getRoutes()[scenario - 1];
            }
            record = cwSolution.getObjectiveValue();
            double globalRecord = record;
            routeSet.setRouteLength(record);
            deviation = 0.01 * record;
            double globalDeviation = deviation;
            RouteSet globalSolution = routeSet.getCopy();

            for (int m = 0; m < M; m++) {
                double initial = routeSet.getRouteLength();
                optimizationLoop();
                if (routeSet.getRouteLength() < globalRecord) {
                    globalRecord = routeSet.getRouteLength();
                    globalDeviation = 0.01 * globalRecord;
                    globalSolution = routeSet.getCopy();
                }
                // Create copy before perturbing
                RouteSet routeCopy = routeSet.getCopy();
                // Perturb the solution
                perturb();
                // Update record when necessary
                if (routeSet.getRouteLength() < globalRecord) {
                    globalRecord = routeSet.getRouteLength();
                    globalDeviation = 0.01 * globalRecord;
                    globalSolution = routeSet.getCopy();
                }
                optimizationLoop();
                // Update record when necessary
                if (routeSet.getRouteLength() < globalRecord) {
                    globalRecord = routeSet.getRouteLength();
                    globalDeviation = 0.01 * globalRecord;
                    globalSolution = routeSet.getCopy();
                }
                // Restore copy if perturbing results in worse solution
                if (globalRecord + globalDeviation < routeSet.getRouteLength()) {
                    routeSet = routeCopy;
                }
                // Check if this solution is better than best solution
                if (globalSolution.getRouteLength() < bestRouteSet.getRouteLength()) {
                    bestRouteSet = globalSolution.getCopy();
                }
                if (routeSet.getRouteLength() >= initial) {
                    break;
                }
            }
            mergeRoutes();
//            System.out.println("Lambda: " + lambda + ", value: " + globalSolution.getRouteLength());
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

    private void optimizationLoop() {
        // Start improvement iterations
        for (int k = 0; k < K; k++) {
            for (int count = 0; count < I; count++) {
                // Moves with record to record
                boolean moveMade = false;
                for (Customer i : routeSet.getCustomers()) {
                    if (i.getId() != 0) {
                        boolean onePoint = findOnePointMove(i, true);
                        boolean twoPoint = findTwoPointMove(i, true);
                        Route r = routeSet.getRoutes()[i.getRoute()];
                        boolean twoOpt = findTwoOptMoveNew(r.getEdgeFrom(i), true);
                        if (!moveMade && (onePoint || twoPoint || twoOpt)) {
                            moveMade = true;
                        }
                    }
                }
                if (!moveMade) {
                    break;
                }
                // Update record when necessary
                if (routeSet.getRouteLength() < record) {
                    record = routeSet.getRouteLength();
                    deviation = 0.01 * record;
                }
            }
            // Downhill moves
            for (Customer i : routeSet.getCustomers()) {
                if (i.getId() != 0) {
                    findOnePointMove(i, false);
                    findTwoPointMove(i, false);
                    Route r = routeSet.getRoutes()[i.getRoute()];
                    findTwoOptMoveNew(r.getEdgeFrom(i), false);
                }
            }
            // Update record when necessary
            if (routeSet.getRouteLength() < record) {
                record = routeSet.getRouteLength();
                deviation = 0.01 * record;
            }
        }
    }

    private void mergeRoutes() {
        for (Route r : routeSet.getRoutes()) {
            if (r != null) {
                for (Route s : routeSet.getRoutes()) {
                    if (s != null && r != s) {
                        if (r.getWeight() + s.getWeight() <= Q) {
//                            System.out.println("Merge possible");
                        }
                    }
                }
            }
        }
    }

    /**
     * Perturb the solution
     */
    private void perturb() {
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
            saving += applyBestInsertion(customer);
        }
        routeSet.setRouteLength(routeSet.getRouteLength() - saving);
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
                // Only if customer i is not already in edge e and new tour is allowed
                if (e.getFrom().getId() != i.getId() && e.getTo().getId() != i.getId() && i.getDemand() + rN.getWeight() <= Q) {
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
                        } else if (twoOptMoveFeasible(e, f, 1)) {
                            twoOptMode = 1;
                        } else {
                            continue;
                        }
                    } else {
                        if (twoOptMoveFeasible(e, f, 1)) {
                            twoOptMode = 1;
                        } else if (twoOptMoveFeasible(e, f, 2)) {
                            twoOptMode = 2;
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
        if (mode == 0) {
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
        if (mode == 1) {
            next = f.getTo();
            while (next.getId() != 0) {
                edgesR2.add(rF.getEdgeFrom(next));
                next = rF.getEdgeFrom(next).getTo();
            }
        } else if (mode == 2) {
            next = f.getFrom();
            while (next.getId() != 0) {
                edgesR2.add(rF.getEdgeTo(next));
                next = rF.getEdgeTo(next).getFrom();
            }
        } else {
            System.out.println("Mode two-opt move not correct");
        }
        // Remove edges
        rE.removeEdge(e);
        rF.removeEdge(f);
        for (Edge edge : edgesR1) {
            rE.removeEdge(edge);
        }
        for (Edge edge : edgesR2) {
            rF.removeEdge(edge);
        }
        // Add edges
        if (mode == 1) {
            for (Edge edge : edgesR2) {
                rE.addEdge(edge);
            }
            for (Edge edge : edgesR1) {
                rF.addEdge(edge);
            }
            rE.addEdge(new Edge(e.getFrom(), f.getTo(), c));
            rF.addEdge(new Edge(f.getFrom(), e.getTo(), c));
        } else if (mode == 2) {
            for (Edge edge : edgesR2) {
                rE.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            for (Edge edge : edgesR1) {
                rF.addEdge(new Edge(edge.getTo(), edge.getFrom(), edge.getDistance()));
            }
            rE.addEdge(new Edge(e.getFrom(), f.getFrom(), c));
            rF.addEdge(new Edge(e.getTo(), f.getTo(), c));
        } else {
            System.out.println("Mode two-opt move not correct");
        }

        // Remove a route if necessary
        if (f.getFrom() == e.getTo() && mode == 1) {
            routeSet.getRoutes()[f.getRoute()] = null;
        }
        if (e.getFrom() == f.getTo() && mode == 1) {
            routeSet.getRoutes()[e.getRoute()] = null;
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
        if (mode == 1 && (e.getTo() == f.getTo() || e.getFrom() == f.getFrom())) {
            return false;
        }
        if (mode == 2 && (e.getTo() == f.getFrom() || f.getTo() == e.getFrom())) {
            return false;
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
        if (mode == 1) {
            // Add edges to be removed from f and added to e to a list
            next = f.getTo();
            while (next.getId() != 0) {
                q1 += next.getDemand();
                q2 -= next.getDemand();
                next = rF.getEdgeFrom(next).getTo();
            }
        } else if (mode == 2) {
            // Add edges to be removed from f and added to e to a list
            next = f.getFrom();
            while (next.getId() != 0) {
                q1 += next.getDemand();
                q2 -= next.getDemand();
                next = rF.getEdgeTo(next).getFrom();
            }
        } else {
            System.out.println("Mode two-opt move not correct");
        }
        // Check if move is feasible
        return !(q1 > Q || q2 > Q);
    }
}
