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

    /**
     * Create a customer
     *
     * @param id number of the customer
     */
    public Customer(int id) {
        this.id = id;
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
}
