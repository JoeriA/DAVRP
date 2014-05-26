import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMerger {

    private static String[][][] mergedData;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        int nrOfInstances = 65 + 60 * 3;
        mergedData = new String[nrOfInstances][][];

        // Get
        int nrArray = 0;
        for (int i = 1; i <= 15; i++) {
            readInstanceSingle("" + i, nrArray);
            nrArray++;
        }
        for (int i = 16; i <= 25; i++) {
            readInstanceThree("" + i, nrArray);
            nrArray++;
        }
        for (int i = 26; i <= 65; i++) {
            readInstanceH("" + i, nrArray);
            nrArray++;
        }
        for (int i = 66; i <= 95; i++) {
            for (int j = 1; j <= 3; j++) {
                readInstanceSingle2(i + "_" + j, nrArray);
                nrArray++;
            }
        }
        for (int i = 96; i <= 105; i++) {
            readInstanceSingle2(i + "_1", nrArray);
            nrArray++;
        }
        for (int i = 106; i <= 125; i++) {
            for (int j = 1; j <= 3; j++) {
                readInstanceSingle2(i + "_" + j, nrArray);
                nrArray++;
            }
        }

        writeToFile();

    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingle(String fileName, int instance) {
        // Write info
        String[] solvers = {"Exact method (CPLEX)", "CPLEX largest", "Clarke-Wright heuristic", "Record2Record"};
        mergedData[instance] = new String[solvers.length][];
        mergedData[instance][0] = new String[5];
        mergedData[instance][0][0] = fileName;
        mergedData[instance][0][1] = solvers[0];
        readFileRemy1("DAVRPInCPLEXStatistics_" + fileName, instance, 0);
        for (int i = 1; i < solvers.length; i++) {
            mergedData[instance][i] = new String[5];
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = solvers[i];
            readFile("DAVRPInstance" + fileName + "_results_" + solvers[i], instance, i);
        }
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingle2(String fileName, int instance) {
        // Write info
        String[] solvers = {"Remy", "CPLEX largest", "Clarke-Wright heuristic", "Record2Record"};
        mergedData[instance] = new String[solvers.length][];
        mergedData[instance][0] = new String[5];
        mergedData[instance][0][0] = fileName;
        mergedData[instance][0][1] = solvers[0];
        readFileRemy1("DAVRPCFRSStatistics_" + fileName, instance, 0);
        for (int i = 1; i < solvers.length; i++) {
            mergedData[instance][i] = new String[5];
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = solvers[i];
            readFile("DAVRPInstance" + fileName + "_results_" + solvers[i], instance, i);
        }
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceThree(String fileName, int instance) {
        // Write info
        String[] solvers = {"Remy11", "Remy12", "Remy13", "Remy21", "Remy22", "Remy23", "Remy31", "Remy32", "Remy33", "Clarke-Wright heuristic", "Record2Record"};
        mergedData[instance] = new String[solvers.length][];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mergedData[instance][i * 3 + j] = new String[5];
                mergedData[instance][i * 3 + j][0] = fileName;
                mergedData[instance][i * 3 + j][1] = solvers[i * 3 + j];
                readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_" + (i + 1) + "_" + (j + 1), instance, i * 3 + j);
            }
        }
        for (int i = 9; i < solvers.length; i++) {
            mergedData[instance][i] = new String[5];
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = solvers[i];
            readFile("DAVRPInstance" + fileName + "_results_" + solvers[i], instance, i);
        }
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceH(String fileName, int instance) {
        // Write info
        String[] solvers = {"RemyH1", "RemyH2", "Clarke-Wright heuristic", "Record2Record"};
        mergedData[instance] = new String[solvers.length][];
        for (int i = 0; i < 2; i++) {
            mergedData[instance][i] = new String[5];
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = solvers[i];
            readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H" + (i + 1), instance, i);
        }
        for (int i = 2; i < solvers.length; i++) {
            mergedData[instance][i] = new String[5];
            mergedData[instance][i][0] = fileName;
            mergedData[instance][i][1] = solvers[i];
            readFile("DAVRPInstance" + fileName + "_results_" + solvers[i], instance, i);
        }
    }

    /**
     * Read an output file for one instance for one solver
     *
     * @param fileName file name of the file (complete name with location)
     * @param instance number of the instance
     * @param solver   solver used to optimize (defined in 'readInstance')
     */
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
            // Save value
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver][3] = split[split.length - 1];
            // Save gap
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver][4] = split[split.length - 1];
        } catch (IOException e) {
            mergedData[instance][solver] = null;
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

    /**
     * Read an output file for one instance for one solver
     *
     * @param fileName file name of the file (complete name with location)
     * @param instance number of the instance
     * @param solver   solver used to optimize (defined in 'readInstance')
     */
    private static void readFileRemy1(String fileName, int instance, int solver) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("Test Output/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            s = reader.readLine();
            split = s.split(" ");
            if (Integer.parseInt(split[split.length - 1]) > 0) {
                // Save value
                s = reader.readLine();
                split = s.split(" ");
                mergedData[instance][solver][3] = split[split.length - 1];
                // Save gap
                s = reader.readLine();
                split = s.split(" ");
                mergedData[instance][solver][4] = split[split.length - 1];
                // Save runtime
                s = reader.readLine();
                split = s.split(" ");
                mergedData[instance][solver][2] = split[split.length - 1];
            } else {
                mergedData[instance][solver] = null;
            }
        } catch (IOException e) {
            mergedData[instance][solver] = null;
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

    /**
     * Read an output file for one instance for one solver
     *
     * @param fileName file name of the file (complete name with location)
     * @param instance number of the instance
     * @param solver   solver used to optimize (defined in 'readInstance')
     */
    private static void readFileRemy2(String fileName, int instance, int solver) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("Test Output/" + fileName + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            s = reader.readLine();
            split = s.split(" ");
            if (Integer.parseInt(split[split.length - 1]) > 0) {
                // Save value
                s = reader.readLine();
                split = s.split(" ");
                mergedData[instance][solver][3] = split[split.length - 1];
                // Save runtime
                reader.readLine();
                reader.readLine();
                s = reader.readLine();
                split = s.split(" ");
                mergedData[instance][solver][2] = split[split.length - 1];
            } else {
                mergedData[instance][solver] = null;
            }
        } catch (IOException e) {
            mergedData[instance][solver] = null;
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

    /**
     * Create one master table with all results
     */
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
            for (String[][] instance : mergedData) {
                if (instance != null) {
                    for (String[] solver : instance) {
                        if (solver != null) {
                            for (String value : solver) {
                                line += value + "\t";
                            }
                            line += "\r\n";
                        }
                    }
                }
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

}
