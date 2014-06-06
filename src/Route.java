import java.util.ArrayList;

/**
 * Created by Joeri on 19-5-2014.
 * Class for storing all info of a route
 */
public class Route {

    private double costs;
    private int weight;
    private Edge[] inEdges;
    private Edge[] outEdges;
    private Customer[] customers;
    private ArrayList<Edge> edges;
    private int routeNumber;
    private int assignedCustomersInRoute;

    private ArrayList<Customer> assignedCustomers;


    /**
     * Create an empty route
     *
     * @param nrOfNodes   number of customers in total (plus depot)
     * @param routeNumber number of this route
     */
    public Route(int nrOfNodes, int routeNumber) {

        this.inEdges = new Edge[nrOfNodes];
        this.outEdges = new Edge[nrOfNodes];
        this.customers = new Customer[nrOfNodes];
        this.edges = new ArrayList<Edge>();
        this.routeNumber = routeNumber;
    }

    /**
     * Create a route (used for creating a copy of this route)
     *
     * @param costs       costs of this route (total distance)
     * @param weight      sum of demands of customers in this route
     * @param inEdges     array of incoming edges for each customer
     * @param outEdges    array of outgoing edges for each customer
     * @param customers   array of customer in this route
     * @param edges       arraylist of edges in this route
     * @param routeNumber number of this route
     */
    private Route(double costs, int weight, Edge[] inEdges, Edge[] outEdges, Customer[] customers, ArrayList<Edge> edges, ArrayList<Customer> assignedCustomers, int routeNumber, int assignedCustomersInRoute) {
        this.costs = costs;
        this.weight = weight;
        this.inEdges = inEdges;
        this.outEdges = outEdges;
        this.customers = customers;
        this.edges = edges;
        this.assignedCustomers = assignedCustomers;
        this.routeNumber = routeNumber;
        this.assignedCustomersInRoute = assignedCustomersInRoute;
    }

    /**
     * Function to recalculate new weight (needed after resetting demands for new scenario)
     */
    public void recalculateWeight() {
        int newWeight = 0;
        for (Customer c : customers) {
            if (c != null) {
                newWeight += c.getDemand();
            }
        }
        weight = newWeight;
    }

    /**
     * Method to assign all current customers to this route
     */
    public void assignCurrentCustomers() {
        assignedCustomersInRoute = 0;
        assignedCustomers = new ArrayList<Customer>();
        for (Customer c : customers) {
            if (c != null && c.getId() != 0) {
                c.setAssignedRoute(routeNumber);
                assignedCustomers.add(c);
                assignedCustomersInRoute++;
            }
        }
    }

    /**
     * Check whether removing a certain customer is feasible
     *
     * @param remove the customer to remove
     * @param alpha  percentage of assigned customers that should be visited
     * @return true if removal is feasible
     */
    public boolean removeCustomerFeasible(Customer remove, double alpha) {
        int nrOfAssignedCustomers = assignedCustomersInRoute;
        if (remove.getAssignedRoute() == routeNumber) {
            nrOfAssignedCustomers--;
        }
        return nrOfAssignedCustomers >= alpha * assignedCustomers.size();
    }

    /**
     * Check whether adding a certain customer is feasible
     *
     * @param add customer to add
     * @param Q   vehicle capacity
     * @return true if adding this customer is feasible
     */
    public boolean addCustomerFeasible(Customer add, int Q) {
        return weight + add.getDemand() <= Q;
    }

    /**
     * Check whether swapping two customers is feasible
     *
     * @param remove the customer to remove
     * @param add    customer to add
     * @param alpha  percentage of assigned customers that should be visited
     * @param Q      vehicle capacity
     * @return true if swapping is feasible
     */
    public boolean swapCustomersFeasible(Customer remove, Customer add, double alpha, int Q) {
        int nrOfAssignedCustomers = assignedCustomersInRoute;
        if (remove.getAssignedRoute() == routeNumber) {
            nrOfAssignedCustomers--;
        }
        if (add.getAssignedRoute() == routeNumber) {
            nrOfAssignedCustomers++;
        }
        return (nrOfAssignedCustomers >= alpha * assignedCustomers.size()) && (weight - remove.getDemand() + add.getDemand() <= Q);
    }

    /**
     * Check whether swapping a list of customers with another list of customer is feasible
     *
     * @param removeList list of customers to remove
     * @param addList    list of customers to add
     * @param alpha      percentage of assigned customers that should be visited
     * @param Q          vehicle capacity
     * @return true if swap is feasible
     */
    public boolean swapCustomersFeasible(ArrayList<Customer> removeList, ArrayList<Customer> addList, double alpha, int Q) {
        int nrOfAssignedCustomers = assignedCustomersInRoute;
        int newWeight = weight;
        for (Customer remove : removeList) {
            if (remove.getAssignedRoute() == routeNumber) {
                nrOfAssignedCustomers--;
            }
            newWeight -= remove.getDemand();
        }
        for (Customer add : addList) {
            if (add.getAssignedRoute() == routeNumber) {
                nrOfAssignedCustomers++;
            }
            newWeight += add.getDemand();
        }
        return (nrOfAssignedCustomers >= alpha * assignedCustomers.size()) && (newWeight <= Q);
    }

    /**
     * Get a copy of this route (hard copy without references)
     *
     * @return copy of this route (hard copy without references)
     */
    public Route getCopy(Customer[] newCustomers) {
        int n = customers.length;
        // Create copy of all customers
        Customer[] customersCopy = new Customer[n];
        for (int i = 0; i < n; i++) {
            if (customers[i] != null) {
                customersCopy[i] = newCustomers[i];
            }
        }
        // Create copy of all edges
        Edge[] inEdgesCopy = new Edge[n];
        Edge[] outEdgesCopy = new Edge[n];
        ArrayList<Edge> edgesCopy = new ArrayList<Edge>(edges.size());
        Edge newEdge;
        Customer from, to;
        for (Edge e : edges) {
            from = customersCopy[e.getFrom().getId()];
            to = customersCopy[e.getTo().getId()];
            newEdge = new Edge(from, to, e.getDistance());
            newEdge.setRoute(routeNumber);
            edgesCopy.add(newEdge);
            inEdgesCopy[to.getId()] = newEdge;
            outEdgesCopy[from.getId()] = newEdge;
        }
        // Copy assignments
        ArrayList<Customer> assignedCustomersCopy = null;
        if (assignedCustomers != null) {
            assignedCustomersCopy = new ArrayList<Customer>(assignedCustomers.size());
            for (Customer c : assignedCustomers) {
                assignedCustomersCopy.add(customersCopy[c.getId()]);
            }
        }
        return new Route(costs, weight, inEdgesCopy, outEdgesCopy, customersCopy, edgesCopy, assignedCustomersCopy, routeNumber, assignedCustomersInRoute);
    }

    /**
     * Get array of customer in this route
     *
     * @return array of customer in this route
     */
    public Customer[] getCustomers() {
        return customers;
    }

    /**
     * Get number of this route
     *
     * @return number of this route
     */
    public int getRouteNumber() {
        return routeNumber;
    }

    /**
     * Add an edge to this route
     *
     * @param e edge to be added
     */
    public void addEdge(Edge e) {
        // Add in and out edges
        inEdges[e.getTo().getId()] = e;
        outEdges[e.getFrom().getId()] = e;
        edges.add(e);
        e.setRoute(routeNumber);

        // Check if customers in edge need to be added
        if (customers[e.getTo().getId()] == null) {
            customers[e.getTo().getId()] = e.getTo();
            weight += e.getTo().getDemand();
            e.getTo().setRoute(routeNumber);
            if (e.getTo().getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute++;
            }
        }
        if (customers[e.getFrom().getId()] == null) {
            customers[e.getFrom().getId()] = e.getFrom();
            weight += e.getFrom().getDemand();
            e.getFrom().setRoute(routeNumber);
            if (e.getFrom().getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute++;
            }
        }

        // Add costs
        costs += e.getDistance();
    }

    /**
     * Remove an edge from this route
     *
     * @param e edge to be removed
     */
    public void removeEdge(Edge e) {
        // Check whether edge does exist
        if (!edges.contains(e)) {
            System.out.println("Edge does not exist");
        }

        // Remove edge from lists
        inEdges[e.getTo().getId()] = null;
        outEdges[e.getFrom().getId()] = null;
        edges.remove(e);

        // Check if customers in edge also need to be removed
        if (inEdges[e.getTo().getId()] == null && outEdges[e.getTo().getId()] == null) {
            customers[e.getTo().getId()] = null;
            weight -= e.getTo().getDemand();
            if (e.getTo().getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }
        if (inEdges[e.getFrom().getId()] == null && outEdges[e.getFrom().getId()] == null) {
            customers[e.getFrom().getId()] = null;
            weight -= e.getFrom().getDemand();
            if (e.getFrom().getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }

        // Remove costs of edge
        costs -= e.getDistance();

    }

    /**
     * Remove edge between two customers from this route
     *
     * @param i customer where the edge starts
     * @param j customer where the edge ends
     */
    public void removeEdge(Customer i, Customer j) {
        // Check whether edge does exist
        if (inEdges[j.getId()] == null || !inEdges[j.getId()].equals(outEdges[i.getId()])) {
            System.out.println("Edge does not exist");
        }

        // Remove costs of edge
        costs -= inEdges[j.getId()].getDistance();

        // Remove edge from lists
        edges.remove(inEdges[j.getId()]);
        inEdges[j.getId()] = null;
        outEdges[i.getId()] = null;

        // Check if customers in edge also need to be removed
        if (inEdges[j.getId()] == null && outEdges[j.getId()] == null) {
            customers[j.getId()] = null;
            weight -= j.getDemand();
            if (j.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }
        if (inEdges[i.getId()] == null && outEdges[i.getId()] == null) {
            customers[i.getId()] = null;
            weight -= i.getDemand();
            if (i.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }

    }

    /**
     * Remove edge between two customers from this route
     *
     * @param j customer where the edge ends
     */
    public void removeEdgeTo(Customer j) {
        // Check whether edge does exist
        if (inEdges[j.getId()] == null) {
            System.out.println("Edge does not exist");
        }

        Edge e = inEdges[j.getId()];

        Customer i = e.getFrom();


        // Remove costs of edge
        costs -= e.getDistance();

        // Remove edge from lists
        edges.remove(e);
        inEdges[j.getId()] = null;
        outEdges[i.getId()] = null;

        // Check if customers in edge also need to be removed
        if (inEdges[j.getId()] == null && outEdges[j.getId()] == null) {
            customers[j.getId()] = null;
            weight -= j.getDemand();
            if (j.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }
        if (inEdges[i.getId()] == null && outEdges[i.getId()] == null) {
            customers[i.getId()] = null;
            weight -= i.getDemand();
            if (i.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }

    }

    /**
     * Remove edge between two customers from this route
     *
     * @param i customer where the edge begins
     */
    public void removeEdgeFrom(Customer i) {
        // Check whether edge does exist
        if (outEdges[i.getId()] == null) {
            System.out.println("Edge does not exist");
        }

        Edge e = outEdges[i.getId()];
        Customer j = e.getTo();


        // Remove costs of edge
        costs -= e.getDistance();

        // Remove edge from lists
        edges.remove(e);
        inEdges[j.getId()] = null;
        outEdges[i.getId()] = null;

        // Check if customers in edge also need to be removed
        if (inEdges[j.getId()] == null && outEdges[j.getId()] == null) {
            customers[j.getId()] = null;
            weight -= j.getDemand();
            if (j.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }
        if (inEdges[i.getId()] == null && outEdges[i.getId()] == null) {
            customers[i.getId()] = null;
            weight -= i.getDemand();
            if (i.getAssignedRoute() == routeNumber) {
                assignedCustomersInRoute--;
            }
        }

    }

    /**
     * Merge two routes with maximal saving
     *
     * @param other  the other route to merge with this one
     * @param saving the saving that can be made by merging
     * @return true if merge is successful, false if no merge is possible with this saving
     */
    public boolean merge(Route other, Saving saving, double[][] c) {

        Customer customerI = saving.getI();
        Customer customerJ = saving.getJ();

        // Only merge when depot is between the customers
        if (beforeDepot(customerI) && other.afterDepot(customerJ)) {
            // Remove edges from/to depot
            removeEdge(customerI, customers[0]);
            other.removeEdge(customers[0], customerJ);
            // Add all other edges from second route into this one
            for (Edge e : other.getEdges()) {
                addEdge(e);
            }
            // Add new edge from saving
            addEdge(new Edge(customerI, customerJ, c));

            return true;
        } else if (afterDepot(customerI) && other.beforeDepot(customerJ)) {
            // Remove edges from/to depot
            removeEdge(customers[0], customerI);
            other.removeEdge(customerJ, customers[0]);
            // Add all other edges from second route into this one
            for (Edge e : other.getEdges()) {
                addEdge(e);
                e.setRoute(routeNumber);
            }
            // Add new edge from saving
            addEdge(new Edge(customerJ, customerI, c));

            return true;
        } else {
            return false;
        }
    }

    /**
     * Perform two opt move
     *
     * @param e first edge for move
     * @param f second edge for move
     * @param c distance matrix
     */
    public void twoOptMove(Edge e, Edge f, double[][] c) {
        Edge firstEdge, secondEdge;
        int nextCustomer = 0;
        // Find first edge to swap in route (starting at depot)
        while (nextCustomer != e.getFrom().getId() && nextCustomer != f.getFrom().getId()) {
            nextCustomer = outEdges[nextCustomer].getTo().getId();
        }
        // Determine which edge is found and what the next edge to find is
        if (nextCustomer == e.getFrom().getId()) {
            firstEdge = e;
            secondEdge = f;
        } else {
            firstEdge = f;
            secondEdge = e;
        }
        ArrayList<Customer> reverseCustomers = new ArrayList<Customer>();
        // Find next edge to swap. In meanwhile save all intermediate edges and customers
        while (nextCustomer != secondEdge.getFrom().getId()) {
            reverseCustomers.add(outEdges[nextCustomer].getTo());
            nextCustomer = outEdges[nextCustomer].getTo().getId();
        }
        // Remove edges that must be reversed
        removeEdge(e);
        removeEdge(f);
        for (int i = 0; i < reverseCustomers.size() - 1; i++) {
            removeEdge(reverseCustomers.get(i), reverseCustomers.get(i + 1));
        }
        // Create new edges
        addEdge(new Edge(firstEdge.getFrom(), secondEdge.getFrom(), c));
        for (int i = reverseCustomers.size() - 1; i > 0; i--) {
            addEdge(new Edge(reverseCustomers.get(i), reverseCustomers.get(i - 1), c));
        }
        addEdge(new Edge(firstEdge.getTo(), secondEdge.getTo(), c));
    }

    /**
     * Get the edge from a certain customer
     *
     * @param i customer where the edge starts
     * @return edge to the given customer
     */
    public Edge getEdgeFrom(Customer i) {
        return outEdges[i.getId()];
    }

    /**
     * Get the edge to a certain customer
     *
     * @param j customer where the edge ends
     * @return edge from the given customer
     */
    public Edge getEdgeTo(Customer j) {
        return inEdges[j.getId()];
    }

    /**
     * Check whether customer is after depot in the route
     *
     * @param c the customer to be checked
     * @return true if customer after depot in the route, false otherwise
     */
    public boolean afterDepot(Customer c) {
        return inEdges[c.getId()].getFrom().getId() == 0;
    }

    /**
     * Check whether customer is before depot in the route
     *
     * @param c the customer to be checked
     * @return true if customer before depot in the route, false otherwise
     */
    public boolean beforeDepot(Customer c) {
        return outEdges[c.getId()].getTo().getId() == 0;
    }

    /**
     * Get the costs of the route
     *
     * @return costs of the route
     */
    public double getCosts() {
        return costs;
    }

    /**
     * Get the load of the vehicle
     *
     * @return load of the vehicle
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Get all edges in this route
     *
     * @return all edges in this route
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }
}
