import java.util.ArrayList;

/**
 * Created by Joeri on 19-5-2014.
 * <p/>
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
     * Add an edge to this route
     *
     * @param e edge to be added
     */
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
        }
        if (inEdges[e.getFrom().getId()] == null && outEdges[e.getFrom().getId()] == null) {
            customers[e.getFrom().getId()] = null;
            weight -= e.getFrom().getDemand();
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
        }
        if (inEdges[i.getId()] == null && outEdges[i.getId()] == null) {
            customers[i.getId()] = null;
            weight -= i.getDemand();
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
            addEdge(new Edge(customerI, customerJ, c[customerI.getId()][customerJ.getId()]));

            return true;
        } else if (afterDepot(customerI) && other.beforeDepot(customerJ)) {
            // Remove edges from/to depot
            removeEdge(customers[0], customerI);
            other.removeEdge(customerJ, customers[0]);
            // Add all other edges from second route into this one
            for (Edge e : other.getEdges()) {
                addEdge(e);
            }
            // Add new edge from saving
            addEdge(new Edge(customerJ, customerI, c[customerJ.getId()][customerI.getId()]));

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
