import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Joeri on 16-4-2014.
 * Class for reading an input file from CMT
 */
class DataReaderCMT {

    /**
     * Create datareader
     */
    public DataReaderCMT() {

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
            reader = new BufferedReader(new FileReader("Test Instances/Christofides/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Read number of customers
            s = reader.readLine();
            split = s.split(" ");
            int numberOfCustomers = Integer.parseInt(split[1]);
            newData.setNumberOfCustomers(numberOfCustomers);
            // Set number of vehicles equal to number of customers
            newData.setNumberOfVehicles(numberOfCustomers);

            // Read vehicle capacity
            int vehicleCapacity = Integer.parseInt(split[2]);
            newData.setVehicleCapacity(vehicleCapacity);

            // Read maximum route time
            int maxDuration = Integer.parseInt(split[3]);
            newData.setMaxDuration(maxDuration);

            // Read drop time
            int dropTime = Integer.parseInt(split[4]);
            newData.setDropTime(dropTime);

            // Set number of scenarios
            newData.setNumberOfScenarios(1);

            // Read locations of customers
            Customer[] customers = new Customer[numberOfCustomers + 1];
            Customer tempCustomer;
            double xCoordinate, yCoordinate;
            for (int i = 0; i <= numberOfCustomers; i++) {
                s = reader.readLine();
                split = s.split(" ");
                tempCustomer = new Customer(i);
                xCoordinate = Double.parseDouble(split[1]);
                tempCustomer.setxCoordinate(xCoordinate);
                yCoordinate = Double.parseDouble(split[2]);
                tempCustomer.setyCoordinate(yCoordinate);
                if (i == 0) {
                    tempCustomer.setDemandPerScenario(new int[]{0});
                    tempCustomer.setDemand(0);
                } else {
                    int demand = Integer.parseInt(split[3]);
                    tempCustomer.setDemandPerScenario(new int[]{demand});
                    tempCustomer.setDemand(demand);
                }
                customers[i] = tempCustomer;
            }

            // Save customers
            newData.setCustomers(customers);

            // Read travel costs
            double[][] travelCosts = new double[numberOfCustomers + 1][numberOfCustomers + 1];
            for (int i = 0; i <= numberOfCustomers; i++) {
                for (int j = i + 1; j <= numberOfCustomers; j++) {
                    travelCosts[i][j] = customers[i].getDistance(customers[j]);
                    travelCosts[j][i] = travelCosts[i][j];
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
