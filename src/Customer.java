/**
 * Created by Joeri on 16-4-2014.
 */
public class Customer {

    private final int id;
    private double xCoordinate;
    private double yCoordinate;
    private int[] demandPerScenario;
    private int demand;
    private int route;

    public Customer(int id) {
        this.id = id;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public int getRoute() {
        return route;
    }

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
}
