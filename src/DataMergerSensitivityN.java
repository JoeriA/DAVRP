import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.*;

/**
 * Created by Joeri on 4-10-2015.
 */
public class DataMergerSensitivityN {

    private static double[][] runningTimes;
    private static double[][] solutionValues;

    public static void main(String[] args) {

        int[] nrOfCustomers = new int[] {10, 15,20,25,30,40,50,100,300};
        int[] nrOfScenarios = new int[] {3};

        runningTimes = new double[nrOfCustomers.length][nrOfScenarios.length];
        solutionValues = new double[nrOfCustomers.length][nrOfScenarios.length];


        for (int n = 0; n < nrOfCustomers.length; n++) {
            for (int omega = 0; omega < nrOfScenarios.length; omega++)
                for (int i = 1; i <= 10; i++) {
                    String instance = "DAVRPInstance " + nrOfCustomers[n] + " " + nrOfScenarios[omega] + " " + i;
                    File f = new File("Test Output New/SensitivityN/" + instance + "_results_RTR_DAVRP_H4_MT" + ".txt");
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(f));

                        // Initialize temporary variables
                        String s;
                        String[] split;

                        // Save runtime
                        s = reader.readLine();
                        split = s.split("\t");
                        runningTimes[n][omega] += Double.parseDouble(split[split.length - 1]) / 10;
                        // Save value
                        s = reader.readLine();
                        split = s.split("\t");
                        solutionValues[n][omega] += Double.parseDouble(split[split.length - 1]) / 10;
                    } catch (IOException e) {
                        System.out.println(instance + " not found");
                    } finally {
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            System.out.println("Error closing file reader");
                        }
                    }
                }
        }


        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityN2.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{l";
            for (int ignored : nrOfCustomers) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int paramValue : nrOfCustomers) {
                line += " & $n=" + paramValue + "$";
            }
            line += "\\\\\r\n\\midrule\r\n";

            for (int omega = 0; omega < nrOfScenarios.length; omega++) {
                line += "$|\\Omega|=" + nrOfScenarios[omega] + "$";
                for (int i = 0; i < nrOfCustomers.length; i++) {
                    line += " & " + round(runningTimes[i][omega],2);
                }
                if (omega < nrOfScenarios.length - 1) {
                    line += "\\\\\r\n";
                }
            }

            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";

            line = line.replace("_", "\\_");
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }

        try {
            // Create file
            FileWriter fstream = new FileWriter("../Latex/tex/sensitivityN2_values.tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{l";
            for (int ignored : nrOfCustomers) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            for (int paramValue : nrOfCustomers) {
                line += " & $n=" + paramValue + "$";
            }
            line += "\\\\\r\n\\midrule\r\n";

            for (int omega = 0; omega < nrOfScenarios.length; omega++) {
                line += "$|\\Omega|=" + nrOfScenarios[omega] + "$";
                for (int i = 0; i < nrOfCustomers.length; i++) {
                    line += " & " + round(solutionValues[i][omega],2);
                }
                if (omega < nrOfScenarios.length - 1) {
                    line += "\\\\\r\n";
                }
            }

            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";

            line = line.replace("_", "\\_");
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }

    }

    private static String round(double before, int precision) {
        double factor = Math.pow(10.0, (double) precision);
        double big = Math.round(factor * before);
        double after = big / factor;
        if (big % 10 == 0) {
            return ("" + after + "0");
        } else {
            return ("" + after);
        }
    }
}