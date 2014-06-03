/**
 * Class to store all routes for a specific scenario
 * Created by Joeri on 2-6-2014.
 */
public class RouteSet {

    private Customer[] customers;
    private Route[] routes;
    private double routeLength;

    public RouteSet() {

    }

    public Customer[] getCustomers() {
        return customers;
    }

    public void setCustomers(Customer[] customers) {
        this.customers = customers;
    }

    public Route[] getRoutes() {
        return routes;
    }

    public void setRoutes(Route[] routes) {
        this.routes = routes;
    }

    public double getRouteLength() {
        return routeLength;
    }

    public void setRouteLength(double routeLength) {
        this.routeLength = routeLength;
    }

    public RouteSet getCopy() {
        RouteSet copy = new RouteSet();
        copy.setRouteLength(routeLength);

        // Copy customers
        Customer[] customersCopy = new Customer[customers.length];
        for (int i = 0; i < customers.length; i++) {
            customersCopy[i] = customers[i].getCopy();
        }
        copy.setCustomers(customersCopy);

        // Copy routes
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
