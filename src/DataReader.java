import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Joeri on 16-4-2014.
 * Class for reading an input file
 */
class DataReader {

    /**
     * Create datareader
     */
    public DataReader() {

    }

    /**
     * Create dataset from file defined in filename
     *
     * @param fileName string with file location and name
     * @return dataset from file
     */
    public DataSet readFile(String fileName) {

        DataSet newData = new DataSet();

        newData.setInstance(fileName);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("Test Instances/Spliet/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Read number of customers
            s = reader.readLine();
            split = s.split(" ");
            int numberOfCustomers = Integer.parseInt(split[split.length - 1]);
            newData.setNumberOfCustomers(numberOfCustomers);
            // Set number of vehicles equal to number of customers
            newData.setNumberOfVehicles(numberOfCustomers);

            // Read vehicle capacity
            s = reader.readLine();
            split = s.split(" ");
            int vehicleCapacity = Integer.parseInt(split[split.length - 1]);
            newData.setVehicleCapacity(vehicleCapacity);

            // Read alpha
            s = reader.readLine();
            split = s.split(" ");
            double alpha = Double.parseDouble(split[split.length - 1]);
            newData.setAlpha(alpha);

            // Read number of scenarios
            s = reader.readLine();
            split = s.split(" ");
            int numberOfScenarios = Integer.parseInt(split[split.length - 1]);
            newData.setNumberOfScenarios(numberOfScenarios);

            // Skip two lines
            reader.readLine();
            reader.readLine();

            // Read locations of customers
            Customer[] customers = new Customer[numberOfCustomers + 1];
            Customer tempCustomer;
            int id;
            double xCoordinate, yCoordinate;
            for (int i = 0; i <= numberOfCustomers; i++) {
                s = reader.readLine();
                split = s.split(" ");
                id = Integer.parseInt(split[0]);
                if (id != i) {
                    throw new AssertionError("Customers are not succeeding in coordinates");
                }
                tempCustomer = new Customer(id);
                xCoordinate = Double.parseDouble(split[1]);
                tempCustomer.setxCoordinate(xCoordinate);
                yCoordinate = Double.parseDouble(split[2]);
                tempCustomer.setyCoordinate(yCoordinate);
                customers[i] = tempCustomer;
            }

            // Skip two lines
            reader.readLine();
            reader.readLine();

            // Read demand scenarios for each customer
            int[] demandScenario;
            int numberOfCustomer;
            for (int i = 1; i <= numberOfCustomers; i++) {
                demandScenario = new int[numberOfScenarios];
                s = reader.readLine();
                split = s.split(" ");
                numberOfCustomer = Integer.parseInt(split[0]);
                if (numberOfCustomer != i) {
                    throw new AssertionError("Customers are not succeeding in demand scenarios");
                }
                for (int j = 0; j < numberOfScenarios; j++) {
                    demandScenario[j] = Integer.parseInt(split[j + 1]);
                }
                customers[i].setDemandPerScenario(demandScenario);
            }
            // Set demands of depot to zero
            demandScenario = new int[numberOfScenarios];
            for (int i = 0; i < numberOfScenarios; i++) {
                demandScenario[i] = 0;
            }
            customers[0].setDemandPerScenario(demandScenario);

            // Save customers
            newData.setCustomers(customers);

            // Skip two lines
            reader.readLine();
            reader.readLine();

            // Read probabilities of each scenario occurring
            s = reader.readLine();
            split = s.split(" ");
            double[] scenarioProbabilities = new double[numberOfScenarios];
            for (int i = 0; i < numberOfScenarios; i++) {
                scenarioProbabilities[i] = Double.parseDouble(split[i]);
            }
            newData.setScenarioProbabilities(scenarioProbabilities);

            // Skip two lines
            reader.readLine();
            reader.readLine();

            // Read travel costs
            double[][] travelCosts = new double[numberOfCustomers + 1][numberOfCustomers + 1];
            for (int i = 0; i <= numberOfCustomers; i++) {
                s = reader.readLine();
                split = s.split(" ");
                for (int j = 0; j <= numberOfCustomers; j++) {
                    travelCosts[i][j] = Double.parseDouble(split[j]);
                }
            }
            newData.setTravelCosts(travelCosts);

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error reading file" + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing the reader");
            }
        }

        return newData;
    }
}
