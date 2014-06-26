import java.io.*;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMerger3 {

    private static String[][] instanceData;
    private static String[] solverNames = {"Exact method (CPLEX)", "H1", "H2","RTR_DAVRP_H4"};
    private static double[][] solutionData;
    private static double[][] runTimeData;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        Locale.setDefault(new Locale("en", "US"));

        int nrOfInstances = 75 + 50 * 3;
        instanceData = new String[nrOfInstances][4];
        runTimeData = new double[nrOfInstances][solverNames.length + 1];
        solutionData = new double[nrOfInstances][solverNames.length + 1];

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
        writeToFile();
        writeToLatex();
        writeComparisonH1();
        writeComparisonH2();
        writeComparisonH3();
    }

    private static double[] getRuntimes(int start, int end) {
        double[][] runtimes = new double[2][solverNames.length + 1];
        for (int instance = start; instance <= end; instance++) {
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                if (solutionData[instance][solver] > 0.0) {
                    runtimes[0][solver] += runTimeData[instance][solver];
                    runtimes[1][solver] += 1.0;
                }
            }
        }
        for (int solver = 1; solver < solverNames.length + 1; solver++) {
            runtimes[0][solver] = runtimes[0][solver] / runtimes[1][solver];
        }
        return runtimes[0];
    }

    private static double[] getGaps(int start, int end) {
        double[][] gaps = new double[2][solverNames.length + 1];
        for (int instance = start; instance <= end; instance++) {
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                if (solutionData[instance][solver] > 0.0) {
                    gaps[0][solver] += (solutionData[instance][solver] - solutionData[instance][0]) / solutionData[instance][0] * 100.0;
                    gaps[1][solver] += 1.0;
                }
            }
        }
        for (int solver = 1; solver < solverNames.length + 1; solver++) {
            gaps[0][solver] = gaps[0][solver] / gaps[1][solver];
        }
        return gaps[0];
    }

    /**
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingle(String fileName, int instance) {
        // Write info
        instanceData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy1("DAVRPInCPLEXStatistics_" + fileName, instance, 1);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H1", instance, 2);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 3);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i+ 1));
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
        instanceData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName, instance, 3);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i+ 1));
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
        instanceData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName, instance, 2);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 3);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i+ 1));
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
        instanceData[instance][0] = fileName;
        readInfo(fileName, instance);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_1_2", instance, 2);
        readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H2", instance, 3);
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i+ 1));
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
        instanceData[instance][0] = fileName;
        readInfo(fileName, instance);
        for (int i = 1; i <= 2; i++) {
            readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H" + i, instance, (i+ 1));
        }
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i+ 1));
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
            runTimeData[instance][solver] = Double.parseDouble(split[split.length - 1]);
            // Save value
            s = reader.readLine();
            split = s.split("\t");
            solutionData[instance][solver] = Double.parseDouble(split[split.length - 1]);
        } catch (IOException e) {
            solutionData[instance][solver] = -1.0;
            runTimeData[instance][solver] = -1.0;
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
                solutionData[instance][solver] = Double.parseDouble(split[split.length - 1]);
                reader.readLine();
                // Save runtime
                s = reader.readLine();
                split = s.split(" ");
                runTimeData[instance][solver] = Double.parseDouble(split[split.length - 1]);
            } else {
                solutionData[instance][solver] = -1.0;
                runTimeData[instance][solver] = -1.0;
            }
        } catch (IOException e) {
            solutionData[instance][solver] = -1.0;
            runTimeData[instance][solver] = -1.0;
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
                solutionData[instance][solver] = Double.parseDouble(split[split.length - 1]);
                // Save runtime
                reader.readLine();
                reader.readLine();
                s = reader.readLine();
                split = s.split(" ");
                runTimeData[instance][solver] += Double.parseDouble(split[split.length - 1]);
            } else {
                solutionData[instance][solver] = -1.0;
                runTimeData[instance][solver] = -1.0;
            }
        } catch (IOException e) {
            solutionData[instance][solver] = -1.0;
            runTimeData[instance][solver] = -1.0;
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
     * Calculate best solution for each instance
     */
    private static void calculateBestValues() {
        for (int instance = 0; instance < solutionData.length; instance++) {
            double bestValue = Double.POSITIVE_INFINITY;
            for (int solver = 1; solver < solutionData[instance].length; solver++) {
                if (solutionData[instance][solver] > 0 && solutionData[instance][solver] < bestValue) {
                    bestValue = solutionData[instance][solver];
                }
            }
            solutionData[instance][0] = bestValue;
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeToFile() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Merged output3.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            // Title
            line += "Instance\t# Customers\t# Scenarios\tAlpha\tBest";
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += "\t" + solverNames[solver];
            }
            line += "\r\n";

            for (int instance = 0; instance < instanceData.length; instance++) {
                for (int column = 0; column < instanceData[instance].length; column++) {
                    line += instanceData[instance][column] + "\t";
                }
                for (int solver = 0; solver < solutionData[instance].length - 1; solver++) {
                    if (solutionData[instance][solver] > 0.0) {
                        line += "" + solutionData[instance][solver];
                    }
                    line += "\t";
                }
                if (solutionData[instance][solutionData[instance].length - 1] > 0.0) {
                    line += round(solutionData[instance][solutionData[instance].length - 1]);
                }
                line += "\r\n";
            }
            // Write gaps
            double[] gaps = getGaps(0,instanceData.length - 1);
            line += "Avg. gap (%)\t\t\t\t";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "\t"+ gaps[solver];
            }
            line += "\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, instanceData.length - 1);
            line += "Average runtime (s)\t\t\t\t";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "\t"+ runtimes[solver];
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeToLatex() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/resultsDAVRP.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{ThreePartTable}\r\n\\begin{TableNotes}";
            for (int j = 0; j < solverNames.length; j++) {
                line += "\r\n\\item ["+ (j+1) +"] " + solverNames[j] + ".";
            }
            line += "\r\n\\end{TableNotes}\r\n\\begin{longtable}{rrrrr";
            for (String ignored : solverNames) {
                line += "r";
            }
            line += "}\r\n\\hline\r\n";
            // Title
            line += "Instance & n & |$\\Omega$| & $\\alpha$ & Best known";
            for (int solver = 0; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{"+(solver+1)+"}";
            }
            line += "\\\\\r\n\\hline\r\n\\endhead\r\n\\hline\r\n\\insertTableNotes\r\n\\endfoot\r\n";

            // For every instance and solver, create a line with all info
            for (int instance = 0; instance < instanceData.length; instance++) {
                for (int column = 0; column < instanceData[instance].length; column++) {
                    line += instanceData[instance][column] + " & ";
                }
                for (int solver = 0; solver < solutionData[instance].length - 1; solver++) {
                    if (solutionData[instance][solver] > 0.0) {
                        line += round(solutionData[instance][solver]);
                    }
                    line += " & ";
                }
                if (solutionData[instance][solutionData[instance].length - 1] > 0.0) {
                    line += round(solutionData[instance][solutionData[instance].length - 1]);
                }
                line += "\\\\\r\n";
            }
            // Write gaps
            double[] gaps = getGaps(0,instanceData.length - 1);
            line += "\\multicolumn{4}{l}{Avg. gap (\\%)} & ";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(gaps[solver]);
            }
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, instanceData.length - 1);
            line += "\\multicolumn{4}{l}{Average runtime (s)} & ";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(runtimes[solver]);
            }

            line += "\\\\\r\n\\end{longtable}\r\n\\end{ThreePartTable}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTRDAVRP\\tnote");
            line = line.replace("Exact method (CPLEX).", "Exact algorithm from \\citet{Spliet2013}.");
            line = line.replace("H1.", "Heuristic with exact routing from \\citet{Spliet2013}.");
            line = line.replace("H2.", "Heuristic with approximate routing from \\citet{Spliet2013}.");
            line = line.replace("RTR\\_DAVRP\\_H4.", "Record-to-record DAVRP algorithm.");
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeComparisonH1() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/comparisonSmall.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{r";
            for (String ignored : solverNames) {
                line += "r";
            }
            line += "}\r\n\\hline\r\n";
            // Title
            for (int solver = 0; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{"+(solver+1)+"}";
            }
            line += "\\\\\r\n\\hline\r\n";

            // Write gaps
            double[] gaps = getGaps(0,9);
            line += "Average gap with best known (\\%)";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(gaps[solver]);
            }
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0,9);
            line += "Average runtime (s)";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(runtimes[solver]);
            }
            line += "\\\\\r\n\\hline\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTRDAVRP\\tnote");
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeComparisonH2() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/comparisonMedium.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{r";
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += "r";
            }
            line += "}\r\n\\hline\r\n";
            // Title
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{"+solver+"}";
            }
            line += "\\\\\r\n\\hline\r\n";

            // Write gaps
            double[] gaps = getGaps(0,44);
            line += "Average gap with best known (\\%)";
            for (int solver = 2; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(gaps[solver]);
            }
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0,44);
            line += "Average runtime (s)";
            for (int solver = 2; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(runtimes[solver]);
            }
            line += "\\\\\r\n\\hline\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTRDAVRP\\tnote");
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeComparisonH3() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/comparisonLarge.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{r";
            for (int solver = 2; solver < solverNames.length; solver++) {
                line += "r";
            }
            line += "}\r\n\\hline\r\n";
            // Title
            for (int solver = 2; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{"+(solver-1)+"}";
            }
            line += "\\\\\r\n\\hline\r\n";

            // Write gaps
            double[] gaps = getGaps(0,64);
            line += "Average gap with best known (\\%)";
            for (int solver = 3; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(gaps[solver]);
            }
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0,64);
            line += "Average runtime (s)";
            for (int solver = 3; solver < solverNames.length + 1; solver++) {
                line += " & "+ round(runtimes[solver]);
            }
            line += "\\\\\r\n\\hline\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTRDAVRP\\tnote");
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static String round(double s) {
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(s);
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
            instanceData[instance][1] = split[split.length - 1];

            // Read vehicle capacity
            reader.readLine();

            // Read alpha
            s = reader.readLine();
            split = s.split(" ");
            instanceData[instance][3] = split[split.length - 1];

            // Read number of scenarios
            s = reader.readLine();
            split = s.split(" ");
            instanceData[instance][2] = split[split.length - 1];

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
