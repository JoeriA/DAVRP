/**
 * Created by Joeri on 16-4-2014.
 * Class for a customer
 */
public class Customer {

    private final int id;
    private double xCoordinate;
    private double yCoordinate;
    private int[] demandPerScenario;
    private int demand;
    private int route;
    private double r;

    /**
     * Create a customer
     *
     * @param id number of the customer
     */
    public Customer(int id) {
        this.id = id;
    }

    private Customer(int id, double xCoordinate, double yCoordinate, int[] demandPerScenario, int demand, int route, double r) {
        this.id = id;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.demandPerScenario = demandPerScenario;
        this.demand = demand;
        this.route = route;
        this.r = r;
    }

    public Customer getCopy() {
        int[] demandPerScenarioCopy = new int[demandPerScenario.length];
        for (int i = 0; i < demandPerScenario.length; i++) {
            demandPerScenarioCopy[i] = demandPerScenario[i];
        }
        Customer copy = new Customer(id, xCoordinate, yCoordinate, demandPerScenarioCopy, demand, route, r);
        return copy;
    }

    public double getR() {
        return r;
    }

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
}
