import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMerger2 {

    private static String[][] mergedData;
    private static String[] solverNames = {"Exact method (CPLEX)", "H1", "H2", "CPLEX largest", "Clarke-Wright heuristic", "Record2Record", "RTR_DAVRP_H"};
    private static double[][] runTimeData;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        int nrOfInstances = 75 + 50 * 3;
        mergedData = new String[nrOfInstances + 2][5 + solverNames.length];
        runTimeData = new double[2][solverNames.length];

        // Get info
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
                readInstanceSingleH2(i + "_" + j, nrArray);
                nrArray++;
            }
        }
        for (int i = 96; i <= 105; i++) {
            readInstanceSingleH2(i + "_1", nrArray);
            nrArray++;
        }
        for (int i = 106; i <= 125; i++) {
            for (int j = 1; j <= 3; j++) {
                readInstanceSingleH3(i + "_" + j, nrArray);
                nrArray++;
            }
        }

        calculateBestValues();
        calculateMeanRuntimes();
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
        mergedData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy1("DAVRPInCPLEXStatistics_" + fileName, instance, 5);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H1", instance, 6);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 7);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 5));
        }
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingleH2(String fileName, int instance) {
        // Write info
        mergedData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName, instance, 7);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 5));
        }
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingleH3(String fileName, int instance) {
        // Write info
        mergedData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName, instance, 6);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 7);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 5));
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
        mergedData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_1_2", instance, 6);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 7);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 5));
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
        mergedData[instance][0] = fileName;
        readInfo(fileName, instance);
        for (int i = 1; i <= 2; i++) {
            readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H" + i, instance, (i + 5));
        }
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 5));
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
            runTimeData[0][solver - 5] += Double.parseDouble(split[split.length - 1]);
            runTimeData[1][solver - 5] += 1.0;
            // Save value
            s = reader.readLine();
            split = s.split("\t");
            mergedData[instance][solver] = split[split.length - 1];
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
                mergedData[instance][solver] = split[split.length - 1];
                reader.readLine();
                // Save runtime
                s = reader.readLine();
                split = s.split(" ");
                runTimeData[0][solver - 5] += Double.parseDouble(split[split.length - 1]);
                runTimeData[1][solver - 5] += 1.0;
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
                mergedData[instance][solver] = split[split.length - 1];
                // Save runtime
                reader.readLine();
                reader.readLine();
                s = reader.readLine();
                split = s.split(" ");
                runTimeData[0][solver - 5] += Double.parseDouble(split[split.length - 1]);
                runTimeData[1][solver - 5] += 1.0;
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
     * Calculate mean running time for each solver
     */
    private static void calculateMeanRuntimes() {
        mergedData[mergedData.length - 1][0] = "Average runtime (s)";
        for (int i = 0; i < solverNames.length; i++) {
            mergedData[mergedData.length - 1][i + 5] = "" + (runTimeData[0][i] / runTimeData[1][i]);
        }
    }

    /**
     * Calculate best solution for each instance and calculate average gap for each solver
     */
    private static void calculateBestValues() {
        double bestValue;
        double[] gaps = new double[solverNames.length];
        for (int i = 0; i < mergedData.length - 2; i++) {
            bestValue = Double.POSITIVE_INFINITY;
            for (int j = 5; j < mergedData[i].length; j++) {
                if (mergedData[i][j] != null && Double.parseDouble(mergedData[i][j]) < bestValue) {
                    bestValue = Double.parseDouble(mergedData[i][j]);
                }
            }
            mergedData[i][4] = "" + bestValue;
            for (int j = 5; j < mergedData[i].length; j++) {
                if (mergedData[i][j] != null) {
                    gaps[j - 5] += (Double.parseDouble(mergedData[i][j]) - bestValue) / bestValue;
                }
            }
        }
        mergedData[mergedData.length - 2][0] = "Avg. gap (%)";
        for (int i = 0; i < gaps.length; i++) {
            mergedData[mergedData.length - 2][i + 5] = "" + gaps[i] / runTimeData[1][i] * 100.0;
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeToFile() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Merged output2.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            // Title
            line += "Instance\t# Customers\t# Scenarios\tAlpha\tBest";
            for (String s : solverNames) {
                line += "\t" + s;
            }
            line += "\r\n";

            // For every instance and solver, create a line with all info
            for (String[] instance : mergedData) {
                if (instance != null) {
                    for (String value : instance) {
                        if (value != null) {
                            line += value;
                        }
                        line += "\t";
                    }
                    line += "\r\n";
                }
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    /**
     * Read information about an instance
     *
     * @param fileName String with instance number
     * @param instance row number in the data matrix
     */
    private static void readInfo(String fileName, int instance) {
        // Initialize temporary variables
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("Test Instances/DAVRPInstance" + fileName + ".txt"));
            String s;
            String[] split;

            // Read number of customers
            s = reader.readLine();
            split = s.split(" ");
            mergedData[instance][1] = split[split.length - 1];

            // Read vehicle capacity
            reader.readLine();

            // Read alpha
            s = reader.readLine();
            split = s.split(" ");
            mergedData[instance][3] = split[split.length - 1];

            // Read number of scenarios
            s = reader.readLine();
            split = s.split(" ");
            mergedData[instance][2] = split[split.length - 1];

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
    }

}
