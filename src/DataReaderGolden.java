import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Joeri on 16-4-2014.
 * Class for reading an input file from CMT
 */
class DataReaderGolden {

    /**
     * Create datareader
     */
    public DataReaderGolden() {

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
            reader = new BufferedReader(new FileReader("Test Instances/Golden/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Read number of customers
            s = reader.readLine();
            split = s.split(" ");
            int car = 0;
            while (split[car].equals("")) {
                car++;
            }
            int numberOfCustomers = Integer.parseInt(split[car]);
            newData.setNumberOfCustomers(numberOfCustomers);
            // Set number of vehicles equal to number of customers
            newData.setNumberOfVehicles(numberOfCustomers);

            // Read vehicle capacity
            int vehicleCapacity = Integer.parseInt(split[car + 1]);
            newData.setVehicleCapacity(vehicleCapacity);

            // Read maximum route time
            int maxDuration = Integer.parseInt(split[car + 2]);
            newData.setMaxDuration(maxDuration);

            // Read drop time
            int dropTime = Integer.parseInt(split[car + 3]);
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
                car = 0;
                while (split[car].equals("")) {
                    car++;
                }
                xCoordinate = Double.parseDouble(split[car]);
                tempCustomer.setxCoordinate(xCoordinate);
                car++;
                while (split[car].equals("")) {
                    car++;
                }
                yCoordinate = Double.parseDouble(split[car]);
                tempCustomer.setyCoordinate(yCoordinate);
                if (i == 0) {
                    tempCustomer.setDemandPerScenario(new int[]{0});
                    tempCustomer.setDemand(0);
                } else {
                    car++;
                    while (split[car].equals("")) {
                        car++;
                    }
                    int demand = Integer.parseInt(split[car]);
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
