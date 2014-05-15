/**
 * Created by Joeri on 16-4-2014.
 */

public class DataSet {

    private int numberOfCustomers;
    private int vehicleCapacity;
    private double alpha;
    private int numberOfScenarios;
    private double[] scenarioProbabilities;
    private Customer[] customers;
    private double[][] travelCosts;
    private int numberOfVehicles;
    private String instance;

    public DataSet() {

    }

    /**
     * Get number of vehicles
     *
     * @return number of vehicles
     */
    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

    /**
     * Set number of vehicles
     *
     * @param numberOfVehicles number of vehicles
     */
    public void setNumberOfVehicles(int numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    /**
     * Get matrix with travel costs between customers
     *
     * @return matrix with travel costs between customers
     */
    public double[][] getTravelCosts() {
        return travelCosts;
    }

    /**
     * Set matrix with travel costs between customers
     *
     * @param travelCosts matrix with travel costs between customers
     */
    public void setTravelCosts(double[][] travelCosts) {
        this.travelCosts = travelCosts;
    }

    /**
     * Get set of customers
     *
     * @return set of customers
     */
    public Customer[] getCustomers() {
        return customers;
    }

    /**
     * Set set of customers
     *
     * @param customers set of customers
     */
    public void setCustomers(Customer[] customers) {
        this.customers = customers;
    }

    /**
     * Get number of customers
     *
     * @return number of customers
     */
    public int getNumberOfCustomers() {
        return numberOfCustomers;
    }

    /**
     * Set number of customers
     *
     * @param numberOfCustomers number of customers
     */
    public void setNumberOfCustomers(int numberOfCustomers) {
        this.numberOfCustomers = numberOfCustomers;
    }

    /**
     * Get capacity of a vehicle
     *
     * @return capacity of a vehicle
     */
    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    /**
     * Set capacity of a vehicle
     *
     * @param vehicleCapacity capacity of a vehicle
     */
    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    /**
     * Get percentage of assigned customers a driver has to visit
     *
     * @return percentage of assigned customers a driver has to visit
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Set percentage of assigned customers a driver has to visit
     *
     * @param alpha percentage of assigned customers a driver has to visit
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Set number of demand scenarios
     *
     * @return number of demand scenarios
     */
    public int getNumberOfScenarios() {
        return numberOfScenarios;
    }

    /**
     * Set number of demand scenarios
     *
     * @param numberOfScenarios number of demand scenarios
     */
    public void setNumberOfScenarios(int numberOfScenarios) {
        this.numberOfScenarios = numberOfScenarios;
    }

    /**
     * Set array of probabilities for each demand scenario
     *
     * @return array of probabilities for each demand scenario
     */
    public double[] getScenarioProbabilities() {
        return scenarioProbabilities;
    }

    /**
     * Set array of probabilities for each demand scenario
     *
     * @param scenarioProbabilities array of probabilities for each demand scenario
     */
    public void setScenarioProbabilities(double[] scenarioProbabilities) {
        this.scenarioProbabilities = scenarioProbabilities;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
