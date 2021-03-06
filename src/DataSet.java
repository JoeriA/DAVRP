/**
 * Created by Joeri on 16-4-2014.
 * <p/>
 * Class for storing data about problem instance
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

    private int maxDuration;
    private int dropTime;

    /**
     * Create empty dataset
     */
    public DataSet() {

    }

    public DataSet getCopy() {
        DataSet copy = new DataSet();
        copy.setAlpha(alpha);
        copy.setDropTime(dropTime);
        copy.setInstance(instance);
        copy.setMaxDuration(maxDuration);
        copy.setNumberOfCustomers(numberOfCustomers);
        copy.setNumberOfScenarios(numberOfScenarios);
        copy.setNumberOfVehicles(numberOfVehicles);
        copy.setScenarioProbabilities(scenarioProbabilities);
        copy.setTravelCosts(travelCosts);
        copy.setVehicleCapacity(vehicleCapacity);
        // Copy customers
        Customer[] customersCopy = new Customer[customers.length];
        for (int i = 0; i < customers.length; i++) {
            customersCopy[i] = customers[i].getCopy();
        }
        copy.setCustomers(customersCopy);
        return copy;
    }

    public int getDropTime() {
        return dropTime;
    }

    public void setDropTime(int dropTime) {
        this.dropTime = dropTime;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
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
     * Get number of customers (without depot)
     *
     * @return number of customers (without depot)
     */
    public int getNumberOfCustomers() {
        return numberOfCustomers;
    }

    /**
     * Set number of customers (without depot)
     *
     * @param numberOfCustomers number of customers (without depot)
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

    /**
     * Get name of the instance
     *
     * @return name of the instance
     */
    public String getInstance() {
        return instance;
    }

    /**
     * Set name of the instance
     *
     * @param instance name of the instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }
}
