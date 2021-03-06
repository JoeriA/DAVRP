import java.util.ArrayList;

/**
 * Created by Joeri on 16-4-2014.
 * Class for a customer
 */
public class Customer implements Comparable<Customer> {

    private final int id;
    private double xCoordinate;
    private double yCoordinate;
    private int[] demandPerScenario;
    private int demand;
    private int route;
    private int assignedRoute;
    private double r;
    private ArrayList<Neighbor> neighbors;

    /**
     * Create a customer
     *
     * @param id number of the customer
     */
    public Customer(int id) {
        this.id = id;
    }

    /**
     * Create a new customer (used for creating a copy)
     *
     * @param id                identification number of customer
     * @param xCoordinate       x-coordinate of customer
     * @param yCoordinate       y-coordinate of customer
     * @param demandPerScenario array of demand per scenario
     * @param demand            demand of this customer in a particular scenario
     * @param route             the number of the route this customer is in
     * @param assignedRoute     the number of the route this customer is assigned to
     * @param r                 parameter for record to record algorithm
     */
    private Customer(int id, double xCoordinate, double yCoordinate, int[] demandPerScenario, int demand, int route, int assignedRoute, double r, ArrayList<Neighbor> neighbors) {
        this.id = id;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.demandPerScenario = demandPerScenario;
        this.demand = demand;
        this.route = route;
        this.assignedRoute = assignedRoute;
        this.r = r;
        this.neighbors = neighbors;
    }

    /**
     * Get a copy of this customer (hard copy without references)
     *
     * @return a copy of this customer (hard copy without references)
     */
    public Customer getCopy() {
        return new Customer(id, xCoordinate, yCoordinate, demandPerScenario, demand, route, assignedRoute, r, neighbors);
    }

    public ArrayList<Neighbor> getNeighbors() {
        if (this.id==0) {
            System.out.print("Warning: getNeighbors queried on depot");
        }
        return neighbors;
    }

    public void setNeighbors(ArrayList<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    /**
     * Get route/driver this customer is assigned to
     *
     * @return route/driver this customer is assigned to
     */
    public int getAssignedRoute() {
        return assignedRoute;
    }

    /**
     * Set route/driver this customer is assigned to
     *
     * @param assignedRoute route/driver this customer is assigned to
     */
    public void setAssignedRoute(int assignedRoute) {
        this.assignedRoute = assignedRoute;
    }

    /**
     * Get the parameter for record to record algorithm
     *
     * @return parameter for record to record algorithm
     */
    public double getR() {
        return r;
    }

    /**
     * Set parameter for record to record algorithm
     *
     * @param r parameter for record to record algorithm
     */
    public void setR(double r) {
        this.r = r;
    }

    /**
     * Get the demand of this customer in a particular scenario
     *
     * @return demand of this customer in a particular scenario
     */
    public int getDemand() {
        return demand;
    }

    /**
     * Set the demand of this customer in a particular scenario
     *
     * @param demand demand of this customer in a particular scenario
     */
    public void setDemand(int demand) {
        this.demand = demand;
    }

    /**
     * Get the number of the route this customer is in
     *
     * @return the number of the route this customer is in
     */
    public int getRoute() {
        if (this.id==0) {
            System.out.print("Critical warning: getRoute queried on depot\n");
        }
        return route;
    }

    /**
     * Set the number of the route this customer is in
     *
     * @param route the number of the route this customer is in
     */
    public void setRoute(int route) {
        this.route = route;
    }

    /**
     * Get unique number of customer
     *
     * @return unique number of customer
     */
    public int getId() {
        return id;
    }

    /**
     * Get x-coordinate of customer
     *
     * @return x-coordinate of customer
     */
    public double getxCoordinate() {
        return xCoordinate;
    }

    /**
     * Set x-coordinate of customer
     *
     * @param xCoordinate x-coordinate of customer
     */
    public void setxCoordinate(double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    /**
     * Get y-coordinate of customer
     *
     * @return y-coordinate of customer
     */
    public double getyCoordinate() {
        return yCoordinate;
    }

    /**
     * Set y-coordinate of customer
     *
     * @param yCoordinate y-coordinate of customer
     */
    public void setyCoordinate(double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Get array of demand per scenario
     *
     * @return array of demand per scenario
     */
    public int[] getDemandPerScenario() {
        return demandPerScenario;
    }

    /**
     * Set array of demand per scenario
     *
     * @param demandPerScenario array of demand per scenario
     */
    public void setDemandPerScenario(int[] demandPerScenario) {
        this.demandPerScenario = demandPerScenario;
    }

    /**
     * Get the distance between this customer and another one
     *
     * @param c the other customer
     * @return the distance between this customer and another one
     */
    public double getDistance(Customer c) {
        double x2 = Math.pow(getxCoordinate() - c.getxCoordinate(), 2.0);
        double y2 = Math.pow(getyCoordinate() - c.getyCoordinate(), 2.0);
        return Math.sqrt(x2 + y2);
    }

    /**
     * Custom compareTo to sort the savings on the value of the saving
     */
    public int compareTo(Customer other) {
        if (other.getR() < this.r) {
            return -1;
        } else if (other.getR() == this.r) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Override equals. Checks whether a given customer is the same
     *
     * @param other other customer to check
     * @return true when customer is the same, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Customer) {
            Customer c = (Customer) other;
            if (id == c.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Override hashcode (because of changed equals)
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return id;
    }
}
