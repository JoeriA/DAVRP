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
            int nrOfCustomers = Integer.parseInt(split[split.length - 1]);
            newData.setNumberOfCustomers(nrOfCustomers);

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
