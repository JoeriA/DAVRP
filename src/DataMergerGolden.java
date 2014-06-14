import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMergerGolden {

    private static String[][] mergedData;
    private static String[] solverNames = {"Record2Record", "Record2Record_asc", "Record2Record_desc", "Clarke-Wright heuristic"};
    private static double[][] runTimeData;
    private static int colsBefore;
    private static int nrOfInstances;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        nrOfInstances = 12;
        colsBefore = 5;

        mergedData = new String[nrOfInstances + 2][4 + solverNames.length];
        runTimeData = new double[2][solverNames.length];

        readDataFile();
        // Get info
        int nrArray = 0;
        readInstanceSingle("09", nrArray);
        nrArray++;
        for (int i = 10; i <= 20; i++) {
            readInstanceSingle("" + i, nrArray);
            nrArray++;
        }

        calculateBestValues();
        calculateMeanRuntimes();
        writeToFile();
        writeToLatex();

    }

    /**
     * Read data file of golden instances
     */
    private static void readDataFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("DataGolden.txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Skip first line
            reader.readLine();

            // Save data
            for (int line = 0; line < nrOfInstances; line++) {
                s = reader.readLine();
                split = s.split("\t");
                System.arraycopy(split, 0, mergedData[line], 0, 3);
                mergedData[line][3] = split[4];
            }
            s = reader.readLine();
            split = s.split("\t");
            mergedData[mergedData.length - 1][3] = split[4];
        } catch (IOException e) {
            System.out.println("Error reading data file");
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
     * Read an output files for one instance
     *
     * @param fileName filename of the file (file location with prefix of file name)
     * @param instance number of the instance
     */
    private static void readInstanceSingle(String fileName, int instance) {
        // Write info
        for (int i = 0; i < solverNames.length; i++) {
            readFile("kelly" + fileName + "_results_" + solverNames[i], instance, (i + colsBefore - 1));
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
            runTimeData[0][solver - colsBefore - 1] += Double.parseDouble(split[split.length - 1]);
            runTimeData[1][solver - colsBefore - 1] += 1.0;
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
     * Calculate mean running time for each solver
     */
    private static void calculateMeanRuntimes() {
        mergedData[mergedData.length - 1][0] = "Average runtime (s)";
        for (int i = 0; i < solverNames.length; i++) {
            mergedData[mergedData.length - 1][i + colsBefore - 1] = "" + (runTimeData[0][i] / runTimeData[1][i]);
        }
    }

    /**
     * Calculate best solution for each instance and calculate average gap for each solver
     */
    private static void calculateBestValues() {
        double[] gaps = new double[solverNames.length + 1];
        for (int i = 0; i < mergedData.length - 2; i++) {
            for (int j = colsBefore - 2; j < mergedData[i].length; j++) {
                if (mergedData[i][j] != null) {
                    double bestValue = Double.parseDouble(mergedData[i][2]);
                    gaps[j - colsBefore - 2] += (Double.parseDouble(mergedData[i][j]) - bestValue) / bestValue;
                }
            }
        }
        mergedData[mergedData.length - 2][0] = "Avg. gap (%)";
        for (int i = 0; i < gaps.length; i++) {
            mergedData[mergedData.length - 2][i + colsBefore - 2] = "" + gaps[i] / (double) nrOfInstances * 100.0;
        }
    }

    /**
     * Create one master table with all results
     */
    private static void writeToFile() {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Merged output Golden.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            // Title
            line += "Problem\t# Customers\tBest solution\tVRTR";
            for (String s : solverNames) {
                line += "\t" + s;
            }
            line += "\r\n";

            // Print results solvers
            for (int lineNr = 0; lineNr < nrOfInstances; lineNr++) {
                for (int col = 0; col < 2; col++) {
                    line += mergedData[lineNr][col];
                    line += "\t";
                }
                for (int col = 2; col < mergedData[lineNr].length; col++) {
                    line += round(mergedData[lineNr][col], 2);
                    line += "\t";
                }
                line += "\r\n";
            }
            // Print average gaps
            line += mergedData[mergedData.length - 2][0];
            line += "\t\t\t";
            for (int col = colsBefore - 2; col < mergedData[mergedData.length - 2].length; col++) {
                line += round(mergedData[mergedData.length - 2][col], 2);
                line += "\t";
            }
            line += "\r\n";
            line += mergedData[mergedData.length - 1][0];
            line += "\t\t\t";
            for (int col = colsBefore - 2; col < mergedData[mergedData.length - 1].length; col++) {
                line += round(mergedData[mergedData.length - 1][col], 1);
                line += "\t";
            }
            line += "\r\n";

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
            FileWriter fstream = new FileWriter("Merged output Golden.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{table}[h]\r\n\\renewcommand{\\arraystretch}{1.2}\r\n{\r\n\\begin{tabular}{rrrr";
            for (String ignored : solverNames) {
                line += "r";
            }
            line += "}\r\n\\hline\r\n";
            // Title
            line += "Problem & \\# Customers & Best solution & VRTR";
            for (String s : solverNames) {
                line += " & " + s;
            }
            line += "\\\\\r\n\\hline";

            // Print results solvers
            for (int lineNr = 0; lineNr < nrOfInstances; lineNr++) {
                for (int col = 0; col < 2; col++) {
                    line += mergedData[lineNr][col];
                    line += " & ";
                }
                for (int col = 2; col < mergedData[lineNr].length - 1; col++) {
                    line += round(mergedData[lineNr][col], 2);
                    line += " & ";
                }
                line += round(mergedData[lineNr][mergedData[lineNr].length - 1], 2);
                line += "\\\\\r\n";
            }
            // Print average gaps
            line += "\\multicolumn{2}{l}{Avg. gap (\\%)} &  & ";
            for (int col = 3; col < mergedData[mergedData.length - 2].length - 1; col++) {
                line += round(mergedData[mergedData.length - 2][col], 2);
                line += " & ";
            }
            line += round(mergedData[mergedData.length - 2][mergedData[mergedData.length - 2].length - 1], 2);
            line += "\\\\\r\n";
            line += "\\multicolumn{2}{l}{Average runtime (s)} &  & ";
            for (int col = 3; col < mergedData[mergedData.length - 1].length - 1; col++) {
                line += round(mergedData[mergedData.length - 1][col], 1);
                line += " & ";
            }
            line += round(mergedData[mergedData.length - 1][mergedData[mergedData.length - 1].length - 1], 1);
            line += "\\\\\r\n\\hline\r\n\\end{tabular}\r\n}\r\n";
            line += "\\caption{Results record-to-record algorithm on \\cite{Golden1998} test instances}";
            line += "\r\n\\label{tab:rtrGolden}\r\n\\end{table}\r\n";
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static String round(String s, int precision) {
        double factor = Math.pow(10.0, (double) precision);
        double before = Double.parseDouble(s);
        double after = Math.round(factor * before) / factor;
        return ("" + after);
    }

}
