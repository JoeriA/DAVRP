import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Joeri on 15-5-2014.
 * Datamerger4 with combination rtr-hd and rtr-c
 */
public class DataMerger5 {

    private static String[][] instanceData;
    private static String[] solverNames = new String[]{"Exact method (CPLEX)", "H1", "H2", "RTR_DAVRP_H4_MT", "RTR_DAVRP_2_MT"};
    private static double correction = 0.76;
    private static int solverSensitivity = 4; // solver for which sensitivity must be calculated
    private static double[][] solutionData;
    private static double[][] runTimeData;
    private static String folder = "";

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
        sensitivityTable();
        writeComparisonH();
        writeComparisonH1();
        writeComparisonH2();
        writeComparisonH3();
        writeSensitivityN();
        writeSensitivityNRTRHD();
        writeSensitivityNRTRC();
        writeSensitivityAlpha();
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

    private static int[] getNrWins(int start, int end) {
        int[] nrWins = new int[solverNames.length + 1];
        for (int instance = start; instance <= end; instance++) {
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                double epsilon = Math.pow(10.0, -5.0);
                if (solutionData[instance][solver] <= solutionData[instance][0] + epsilon && solutionData[instance][solver] >= solutionData[instance][0] - epsilon) {
                    nrWins[solver] += 1;
                }
            }
        }
        return nrWins;
    }

    private static double gapCombined(int start, int end) {
        double gap2 = 0.0;
        for (int instance = start; instance <= end; instance++) {
            double best = solutionData[instance][solverNames.length - 1];
            if (solutionData[instance][solverNames.length] > 0.0 && solutionData[instance][solverNames.length] < best) {
                best = solutionData[instance][solverNames.length];
            }
            gap2 += (best - solutionData[instance][0]) / solutionData[instance][0] * 100.0;

        }
        return gap2 / (end + 1 - start);
    }

    private static int nrWinsCombined(int start, int end) {
        int nrWins2 = 0;
        for (int instance = start; instance <= end; instance++) {
            double epsilon = Math.pow(10.0, -5.0);
            double best = solutionData[instance][solverNames.length - 1];
            if (solutionData[instance][solverNames.length] > 0.0 && solutionData[instance][solverNames.length] < best) {
                best = solutionData[instance][solverNames.length];
            }
            if (best <= solutionData[instance][0] + epsilon && best >= solutionData[instance][0] - epsilon) {
                nrWins2 += 1;
            }

        }
        return nrWins2;
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
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 1));
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
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 1));
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
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 1));
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
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 1));
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
            readFileRemy2("DAVRPCFRSStatistics_" + fileName + "_H" + i, instance, (i + 1));
        }
        for (int i = 3; i < solverNames.length; i++) {
            readFile("DAVRPInstance" + fileName + "_results_" + solverNames[i], instance, (i + 1));
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
            reader = new BufferedReader(new FileReader("Test Output New/" + folder + fileName + ".txt"));

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
            reader = new BufferedReader(new FileReader("Test Output New/" + fileName + ".txt"));

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
                runTimeData[instance][solver] = Double.parseDouble(split[split.length - 1]) * correction;
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
            reader = new BufferedReader(new FileReader("Test Output New/" + fileName + ".txt"));

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
                runTimeData[instance][solver] += Double.parseDouble(split[split.length - 1]) * correction;
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
            FileWriter fstream = new FileWriter("../Latex/tex/Merged output3.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            // Title
            line += "Instance\t# Customers\t# Scenarios\tAlpha\tBest";
            for (String solverName : solverNames) {
                line += "\t" + solverName;
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
            double[] gaps = getGaps(0, instanceData.length - 1);
            line += "Avg. gap (%)\t\t\t\t";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "\t" + gaps[solver];
            }
            // Write number of best results
            int[] wins = getNrWins(0, instanceData.length - 1);
            line += "# best results\t\t\t\t";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "\t" + wins[solver];
            }
            line += "\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, instanceData.length - 1);
            line += "Average runtime (s)\t\t\t\t";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "\t" + runtimes[solver];
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

            line += "\\begin{longtable}{rrrrr";
            for (String ignored : solverNames) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            line += "Instance & n & |$\\Omega$| & $\\alpha$ & Best known";
            for (int solver = 0; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + (solver + 1) + "}";
            }
            line += "\\\\\r\n\\midrule\r\n\\endhead\r\n\\bottomrule\r\n\\insertTableNotes\r\n\\endfoot\r\n";

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
            double[] gaps = getGaps(0, instanceData.length - 1);
            line += "\\multicolumn{4}{l}{Avg. gap (\\%)} & ";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + round(gaps[solver]);
            }
            line += "\\\\\r\n";
            // Write number of best results
            int[] wins = getNrWins(0, instanceData.length - 1);
            line += "\\multicolumn{4}{l}{\\# of best results} & ";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + wins[solver];
            }
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, instanceData.length - 1);
            line += "\\multicolumn{4}{l}{Average runtime (s)} & ";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + round(runtimes[solver]);
            }

            line += "\\\\\r\n\\end{longtable}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\tnote", "RTR-C\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
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
    private static void writeComparisonH() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/comparisonRTR.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{lrrrrrr}\r\n\\toprule\r\n";
            // Title
            for (int solver = 0; solver < 5; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + (solver + 1) + "}";
            }
            line += " & Combined\\tnote{3}";
            line += "\\\\\r\n\\midrule\r\n";

            // Write gaps
            double[] gaps = getGaps(0, instanceData.length - 1);
            line += "Average gap (\\%)";
            for (int solver = 1; solver < 6; solver++) {
                line += " & " + round(gaps[solver]);
            }
            line += " & " + round(gapCombined(0, instanceData.length - 1));
            line += "\\\\\r\n";
            // Write number of best results
            int[] wins = getNrWins(0, instanceData.length - 1);
            line += "Number of best results";
            for (int solver = 1; solver < 6; solver++) {
                line += " & " + wins[solver];
            }
            line += " & " + nrWinsCombined(0,instanceData.length - 1);
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, instanceData.length - 1);
            line += "Average runtime (s)";
            for (int solver = 1; solver < 6; solver++) {
                line += " & " + round(runtimes[solver]);
            }
            line += " & " + round(runtimes[4] + runtimes[5]);
            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\tnote", "RTR-C\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
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

            line += "\\begin{tabular}{l";
            for (String ignored : solverNames) {
                line += "r";
            }
            line += "r}\r\n\\toprule\r\n";
            // Title
            for (int solver = 0; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + (solver + 1) + "}";
            }
            line += " & Combined\\tnote{6}";
            line += "\\\\\r\n\\midrule\r\n";

            // Write gaps
            double[] gaps = getGaps(0, 9);
            line += "Average gap (\\%)";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + round(gaps[solver]);
            }
            line += " & " + round(gapCombined(0, 9));
            line += "\\\\\r\n";
            // Write number of best results
            int[] wins = getNrWins(0, 9);
            line += "Nr. of best results";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + wins[solver];
            }
            line += " & " + nrWinsCombined(0,9);
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(0, 9);
            line += "Average runtime (s)";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += " & " + round(runtimes[solver]);
            }
            line += " & " + round(runtimes[4] + runtimes[5]);
            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
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

            line += "\\begin{tabular}{l";
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += "r";
            }
            line += "r}\r\n\\toprule\r\n";
            // Title
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + solver + "}";
            }
            line += " & Combined\\tnote{5}";
            line += "\\\\\r\n\\midrule\r\n";

            // Write gaps
            double[] gaps = getGaps(10, 44);
            line += "Average gap (\\%)";
            for (int solver = 2; solver < solverNames.length + 1; solver++) {
                line += " & " + round(gaps[solver]);
            }
            line += " & " + round(gapCombined(10, 44));
            line += "\\\\\r\n";
            // Write number of best results
            int[] wins = getNrWins(10, 44);
            line += "Number of best results";
            for (int solver = 2; solver < solverNames.length + 1; solver++) {
                line += " & " + wins[solver];
            }
            line += " & " + nrWinsCombined(10,44);
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(10, 44);
            line += "Average runtime (s)";
            for (int solver = 2; solver < solverNames.length + 1; solver++) {
                line += " & " + round(runtimes[solver]);
            }
            line += " & " + round(runtimes[4] + runtimes[5]);
            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
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

            line += "\\begin{tabular}{l";
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int solver = 2; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + (solver - 1) + "}";
            }
            line += " & Combined\\tnote{4}";
            line += "\\\\\r\n\\midrule\r\n";

            // Write gaps
            double[] gaps = getGaps(45, 64);
            line += "Average gap (\\%)";
            for (int solver = 3; solver < solverNames.length + 1; solver++) {
                line += " & " + round(gaps[solver]);
            }
            line += " & " + round(gapCombined(45, 64));
            line += "\\\\\r\n";
            // Write number of best results
            int[] wins = getNrWins(45, 64);
            line += "Number of best results";
            for (int solver = 3; solver < solverNames.length + 1; solver++) {
                line += " & " + wins[solver];
            }
            line += " & " + nrWinsCombined(45,64);
            line += "\\\\\r\n";
            // Write times
            double[] runtimes = getRuntimes(45, 64);
            line += "Average runtime (s)";
            for (int solver = 3; solver < solverNames.length + 1; solver++) {
                line += " & " + round(runtimes[solver]);
            }
            line += " & " + round(runtimes[4] + runtimes[5]);
            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }


    private static void writeSensitivityN() {
        double[][] sensitivityTable = new double[3][3];
        for (int n = 0; n < 3; n++) {
            for (int i = 0; i < 10; i++) {
                for (int omega = 0; omega < 3; omega++) {
                    int indexN = 65 + n * 10 * 3 + i * 3 + omega;
                    sensitivityTable[n][omega] += runTimeData[indexN][solverSensitivity]/10;
                }
            }
        }

        int[] nCustomer = new int[] {30, 50, 70};
        int[] nScenarios = new int[] {10, 50, 100};

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityN.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{lrrr";
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int omega = 0; omega < 3; omega++) {
                line += " & " + nScenarios[omega] + " scenarios";
            }
            line += "\\\\\r\n\\midrule\r\n";
            for (int n = 0; n < 3; n++) {
                line += nCustomer[n] + " customers";
                for (int omega = 0; omega < 3; omega++) {
                    line += " & " + round(sensitivityTable[n][omega]);
                }
                line += "\\\\\r\n";
            }
            line += "\\bottomrule\r\n\\end{tabular}";
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static void writeSensitivityNRTRHD() {
        double[][] sensitivityTable = new double[3][3];
        for (int n = 0; n < 3; n++) {
            for (int i = 0; i < 10; i++) {
                for (int omega = 0; omega < 3; omega++) {
                    int indexN = 65 + n * 10 * 3 + i * 3 + omega;
                    sensitivityTable[n][omega] += getGaps(indexN, indexN)[solverSensitivity]/10;
                }
            }
        }

        int[] nCustomer = new int[] {30, 50, 70};
        int[] nScenarios = new int[] {10, 50, 100};

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityRTRHD.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{lrrr";
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int omega = 0; omega < 3; omega++) {
                line += " & " + nScenarios[omega] + " scenarios";
            }
            line += "\\\\\r\n\\midrule\r\n";
            for (int n = 0; n < 3; n++) {
                line += nCustomer[n] + " customers";
                for (int omega = 0; omega < 3; omega++) {
                    line += " & " + round(sensitivityTable[n][omega]);
                }
                line += "\\\\\r\n";
            }
            line += "\\bottomrule\r\n\\end{tabular}";
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static void writeSensitivityNRTRC() {
        double[][] sensitivityTable = new double[3][3];
        for (int n = 0; n < 3; n++) {
            for (int i = 0; i < 10; i++) {
                for (int omega = 0; omega < 3; omega++) {
                    int indexN = 65 + n * 10 * 3 + i * 3 + omega;
                    sensitivityTable[n][omega] += getGaps(indexN, indexN)[solverSensitivity + 1]/10;
                }
            }
        }

        int[] nCustomer = new int[] {30, 50, 70};
        int[] nScenarios = new int[] {10, 50, 100};

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityRTRC.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{lrrr";
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int omega = 0; omega < 3; omega++) {
                line += " & " + nScenarios[omega] + " scenarios";
            }
            line += "\\\\\r\n\\midrule\r\n";
            for (int n = 0; n < 3; n++) {
                line += nCustomer[n] + " customers";
                for (int omega = 0; omega < 3; omega++) {
                    if (sensitivityTable[n][omega] >= 0) {
                        line += " & " + round(sensitivityTable[n][omega]);
                    } else {
                        line += " & - ";
                    }
                }
                line += "\\\\\r\n";
            }
            line += "\\bottomrule\r\n\\end{tabular}";
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static void writeSensitivityAlpha() {
        double[][] sensitivityTable = new double[3][solverNames.length + 2];

        for (int alpha = 0; alpha < 3; alpha++) {
            for (int instance = 0; instance < 20; instance++) {
                int indexN = 165 + instance * 3 + alpha;
                double[] gaps = getGaps(indexN, indexN);
                for (int solver = 0; solver < gaps.length; solver++) {
                    sensitivityTable[alpha][solver] += gaps[solver] / 20;
                }
                sensitivityTable[alpha][solverNames.length + 1] += Double.min(gaps[solverNames.length], gaps[solverNames.length - 1]) / 20;
            }
        }

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityAlpha.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{l";
            for (int solver = 1; solver < solverNames.length + 1; solver++) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int solver = 1; solver < solverNames.length; solver++) {
                line += " & " + solverNames[solver] + "\\tnote{" + (solver) + "}";
            }
            line += " & Combined\\tnote{5}";
            line += "\\\\\r\n\\midrule\r\n";

            double[] alphas = new double[]{0.0, 0.75, 1.0};
            for (int alpha = 0; alpha < 3; alpha++) {
                line += "$\\alpha$ = " + alphas[alpha];
                for (int solver = 2; solver < solverNames.length + 2; solver++) {
                    line += " & " + round(sensitivityTable[alpha][solver]);
                }
                line += "\\\\\r\n";
            }
            line += "\\bottomrule\r\n\\end{tabular}";
            line = line.replace("_", "\\_");
            line = line.replace("Exact method (CPLEX)\\tnote", "Exact\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_H4\\_MT\\tnote", "RTR-HD\\tnote");
            line = line.replace("RTR\\_DAVRP\\_2\\_MT\\tnote", "RTR-C\\tnote");
            out.write(line);
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static void sensitivityTable() {
        HashMap<Integer, double[][]> map = new HashMap();
        for (int i = 0; i < 65; i++) {
            int nrOfCustomers = Integer.parseInt(instanceData[i][1]);
            double[][] runtimes;
            if (map.containsKey(nrOfCustomers)) {
                runtimes = map.get(nrOfCustomers);
            } else {
                runtimes = new double[solverNames.length][2];
            }
            for (int j = 0; j < solverNames.length; j++) {
                double value = runTimeData[i][j+1];
                if (value > 0) {
                    runtimes[j][0] += value;
                    runtimes[j][1] += 1;
                }
            }
            map.put(nrOfCustomers, runtimes);
        }
        double[][] results = new double[map.size()][1 + solverNames.length];
        int i = 0;
        for (int key : map.keySet()) {
            results[i][0] = key;
            double[][] values = map.get(key);
            for (int j = 0; j < solverNames.length; j++) {
                results[i][j + 1] = values[j][0] / values[j][1];
            }
            i++;
        }

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Runtimes.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            for (int j = 0; j < results.length; j++) {
                for (int k = 0; k < results[0].length; k++) {
                    line += results[j][k] + "\t";
                }
                line += "\r\n";
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing runtimes file: " + e.getMessage());
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
            reader = new BufferedReader(new FileReader("Test Instances/Spliet/DAVRPInstance" + fileName + ".txt"));
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
