/**
 * Class to store all routes for a specific scenario
 * This is needed as customers in one set of routes must be the same
 * Created by Joeri on 2-6-2014.
 */
public class RouteSet {

    private Customer[] customers;
    private Route[] routes;
    private double routeLength;

    /**
     * Create new empty set of routes
     */
    public RouteSet() {

    }

    /**
     * Get the customers in this route set
     *
     * @return array with customers in this route set
     */
    public Customer[] getCustomers() {
        return customers;
    }

    /**
     * Set customers in this route set
     *
     * @param customers array with customers in this route set
     */
    public void setCustomers(Customer[] customers) {
        this.customers = customers;
    }

    /**
     * Get all routes in this route set
     *
     * @return array with all routes in this route set
     */
    public Route[] getRoutes() {
        return routes;
    }

    /**
     * Set all routes in this route set
     *
     * @param routes array with all routes in this route set
     */
    public void setRoutes(Route[] routes) {
        this.routes = routes;
    }

    /**
     * Get length of all routes together
     *
     * @return double with length of all routes together
     */
    public double getRouteLength() {
        return routeLength;
    }

    /**
     * Set length of all routes together
     *
     * @param routeLength double with length of all routes together
     */
    public void setRouteLength(double routeLength) {
        this.routeLength = routeLength;
    }

    /**
     * Get a copy of this route set
     *
     * @return new routeset that is a copy of this route set
     */
    public RouteSet getCopy() {
        RouteSet copy = new RouteSet();
        copy.setRouteLength(routeLength);

        // Copy customers
        Customer[] customersCopy = new Customer[customers.length];
        for (int i = 0; i < customers.length; i++) {
            customersCopy[i] = customers[i].getCopy();
        }
        copy.setCustomers(customersCopy);

        // Copy routes using the copied customers
        Route[] routesCopy = new Route[routes.length];
        for (int i = 0; i < routes.length; i++) {
            if (routes[i] != null) {
                routesCopy[i] = routes[i].getCopy(customersCopy);
            }
        }
        copy.setRoutes(routesCopy);

        return copy;
    }
}
