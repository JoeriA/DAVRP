import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Joeri on 16-4-2014.
 */
public class DataReader {

    public DataReader() {

    }

    public DataSet readFile(String fileName) {

        DataSet newData = new DataSet();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Read number of customers
            s = reader.readLine();
            split = s.split(" ");
            int numberOfCustomers = Integer.parseInt(split[split.length - 1]);
            newData.setNumberOfCustomers(numberOfCustomers);

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
            }
        }
        return newData;
    }
}
