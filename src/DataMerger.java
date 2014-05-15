import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 */
public class DataMerger {

    private static String[][][] mergedData;

    public static void main(String[] args) {

        int nrOfInstances = 65 + 60 * 3;
        mergedData = new String[nrOfInstances][3][5];

        // Get
        int nrArray = 0;
        for (int i = 1; i <= 65; i++) {
            readInstance("DAVRPInstance" + i, nrArray);
            nrArray++;
        }
        for (int i = 65; i < 125; i++) {
            for (int j = 1; j <= 3; j++) {
                readInstance("DAVRPInstance" + i + "_" + j, nrArray);
                nrArray++;
            }
        }

        writeToFile();

    }

    private static void readInstance(String fileName, int instance) {
        // Write info
        String[] instances = {"Exact method (CPLEX)", "ClusteringRemy", "ClusteringLargest"};
        for (int i = 0; i < instances.length; i++) {
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = instances[i];
            readFile(fileName + "_results_" + instances[i], instance, i);
        }
    }

    private static void readFile(String fileName, int instance, int solver) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("Test Output/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Save runtime
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver][2] = split[split.length - 1];
            // Save runtime
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver][3] = split[split.length - 1];
            // Save runtime
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver][4] = split[split.length - 1];

        } catch (FileNotFoundException e) {
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
    }

    private static void writeToFile() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Temp/Merged output.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            // Title
            line += "Instance\tSolver\tRuntime\tObjective value\tGap\r\n";

            // For every instance and solver, create a line with all info
            for (int i = 0; i < mergedData.length; i++) {
                for (int j = 0; j < mergedData[i].length; j++) {
                    for (int k = 0; k < mergedData[i][j].length; k++) {
                        line += mergedData[i][j][k] + "\t";
                    }
                    line += "\r\n";
                }
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

}
