import java.util.ArrayList;

/**
 * Created by Joeri on 19-5-2014.
 */
public class Route {

    private boolean allowed;
    private double costs;
    private int weight;
    private Edge[] inEdges;
    private Edge[] outEdges;
    private Customer[] customers;
    private ArrayList<Edge> edges;
    private int routeNumber;

    public Route(int nrOfNodes, int routeNumber) {

        this.inEdges = new Edge[nrOfNodes];
        this.outEdges = new Edge[nrOfNodes];
        this.customers = new Customer[nrOfNodes];
        this.edges = new ArrayList();
        this.routeNumber = routeNumber;

    }

    public void addEdge(Edge e) {
        // Add in and out edges
        inEdges[e.getTo().getId()] = e;
        outEdges[e.getFrom().getId()] = e;
        edges.add(e);

        // Check if customers in edge need to be added
        if (customers[e.getTo().getId()] == null) {
            customers[e.getTo().getId()] = e.getTo();
            weight += e.getTo().getDemand();
            customers[e.getTo().getId()].setRoute(routeNumber);
        }
        if (customers[e.getFrom().getId()] == null) {
            customers[e.getFrom().getId()] = e.getFrom();
            weight += e.getFrom().getDemand();
            customers[e.getFrom().getId()].setRoute(routeNumber);
        }

        // Add costs
        costs += e.getDistance();
    }

    public void removeEdge(Edge e) {
        // Check whether edge does exist
        if (!inEdges[e.getTo().getId()].equals(e) || !outEdges[e.getFrom().getId()].equals(e)) {
            // TODO check if .equals is correct
            System.out.println("Edge to remove does not exist");
        } else {
            // Remove edge from lists
            inEdges[e.getTo().getId()] = null;
            outEdges[e.getFrom().getId()] = null;
            edges.remove(e);

            // Check if customers in edge also need to be removed
            if (inEdges[e.getTo().getId()] == null && outEdges[e.getTo().getId()] == null) {
                customers[e.getTo().getId()] = null;
                weight -= e.getTo().getDemand();
            }
            if (inEdges[e.getFrom().getId()] == null && outEdges[e.getFrom().getId()] == null) {
                customers[e.getFrom().getId()] = null;
                weight -= e.getFrom().getDemand();
            }

            // Remove costs of edge
            costs -= e.getDistance();
        }
    }

    public boolean merge(Route other, Saving saving) {

        Customer customerI = saving.getI();
        Customer customerJ = saving.getJ();

        // Only merge when depot is between the customers
        if (beforeDepot(customerI) && other.afterDepot(customerJ)) {
            // Remove edges from/to depot
            removeEdge(new Edge(customerI, customers[0]));
            other.removeEdge(new Edge(customers[0], customerJ));
            // Add all other edges from second route into this one
            for (Edge e : other.getEdges()) {
                addEdge(e);
            }
            // Add new edge from saving
            addEdge(new Edge(customerI, customerJ));

            return true;
        } else if (afterDepot(customerI) && other.beforeDepot(customerJ)) {
            // Remove edges from/to depot
            removeEdge(new Edge(customerI, customers[0]));
            other.removeEdge(new Edge(customers[0], customerJ));
            // Add all other edges from second route into this one
            for (Edge e : other.getEdges()) {
                addEdge(e);
            }
            // Add new edge from saving
            addEdge(new Edge(customerJ, customerI));

            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether customer is after depot in the route
     *
     * @param c the customer to be checked
     * @return true if customer after depot in the route, false otherwise
     */
    public boolean afterDepot(Customer c) {
        if (inEdges[c.getId()].getFrom().getId() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether customer is before depot in the route
     *
     * @param c the customer to be checked
     * @return true if customer before depot in the route, false otherwise
     */
    public boolean beforeDepot(Customer c) {
        if (outEdges[c.getId()].getTo().getId() == 0) {
            return true;
        } else {
            return false;
        }
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
