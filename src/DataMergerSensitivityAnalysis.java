import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMergerSensitivityAnalysis {

    private static int nrOfInstances;
    private static int nrOfInstancesGolden;

    private static double[][] lambdas;
    private static int[] K;
    private static int[] D;
    private static int[] P;
    private static int[] NBListSize;
    private static double[] beta;
    private static double[] delta;

    private static double[][][][][][][][] solutionValues;
    private static double[][][][][][][][] runningTimes;

    private static double[][][][][][][] meanGapSolutionValues;
    private static double[][][][][][][] meanRunningTimes;

    private static double[][][][][][][][] solutionValuesGolden;
    private static double[][][][][][][][] runningTimesGolden;

    private static double[][][][][][][] meanGapSolutionValuesGolden;
    private static double[][][][][][][] meanRunningTimesGolden;

    private static double[] bestSolutionValues;
    private static double[] bestSolutionValuesGolden;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        lambdas = new double[2][];
        lambdas[0] = new double[]{0.6, 1.0, 1.4};
        lambdas[1] = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
        K = new int[]{3, 5, 7, 9};
        D = new int[]{20, 30, 40, 50};
        P = new int[]{1, 2, 3, 4};
        NBListSize = new int[]{20, 30, 40, 50, 60};
        beta = new double[]{0.4, 0.6, 0.8, 1.0};
        delta = new double[]{0.005, 0.01, 0.015, 0.02};

        // Function to find best parameters (lowest gap)
//        int[] bestParams = findBest();
        int[] bestParams = new int[] {1, 2, 0, 1, 2, 3, 2}; // Slow, very good for CMT but bad for Golden
        int[] bestParams2 = new int[] {0, 0, 0, 0, 3, 0, 0}; // Quick, but very bad for Golden
        int[] defaultParams = new int[] {0, 1, 1, 1, 2, 1, 1}; // Rather fast, below average results (1.45 - 0.84, 2.17 - 7.7)
        int[] defaultParams2 = new int[] {1, 1, 1, 1, 2, 1, 1}; // Ok (0.82 - 2.4, 1.88 - 15.6)
        int[] newParams = new int[] {1, 1, 2, 1, 2, 1, 1}; // Good balance (0.77 - 2.8, 1.77 - 19)
        int[] newParams2 = new int[] {1, 1, 2, 1, 2, 2, 1}; // (0.74 - 5, 1.54 - 30)
        int[] newParams3 = new int[] {1, 1, 2, 2, 2, 2, 1}; // (0.69- 6, 1.56 - 40)
        int[] newParams4 = new int[] {1, 2, 2, 2, 2, 2, 1}; // Very slow, but best results (0.74 - 8.3, 1.4 - 50)
        int[] params = newParams3;

        nrOfInstances = 7;
        nrOfInstancesGolden = 12;

        solutionValues = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][nrOfInstances];
        runningTimes = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][nrOfInstances];

        meanGapSolutionValues = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length];
        meanRunningTimes = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length];

        bestSolutionValues = new double[nrOfInstances];

        solutionValuesGolden = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][nrOfInstancesGolden];
        runningTimesGolden = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][nrOfInstancesGolden];

        meanGapSolutionValuesGolden = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length];
        meanRunningTimesGolden = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length];

        bestSolutionValuesGolden = new double[nrOfInstancesGolden];

        readDataFile();
        readDataFileGolden();

        // Get info
        int nrArray = 0;
        for (int instance = 1; instance <= 5; instance++) {
            for (int nrLambda = 0; nrLambda < lambdas.length; nrLambda++) {
                for (int nrK = 0; nrK < K.length; nrK++) {
                    for (int nrD = 0; nrD < D.length; nrD++) {
                        for (int nrP = 0; nrP < P.length; nrP++) {
                            for (int nrNBL = 0; nrNBL < NBListSize.length; nrNBL++) {
                                for (int nrBeta = 0; nrBeta < beta.length; nrBeta++) {
                                    for (int nrDelta = 0; nrDelta < delta.length; nrDelta++) {
                                        readFile(nrLambda, nrK, nrD, nrP, nrNBL, nrBeta, nrDelta, instance, nrArray, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            nrArray++;
        }
        for (int instance = 11; instance <= 12; instance++) {
            for (int nrLambda = 0; nrLambda < lambdas.length; nrLambda++) {
                for (int nrK = 0; nrK < K.length; nrK++) {
                    for (int nrD = 0; nrD < D.length; nrD++) {
                        for (int nrP = 0; nrP < P.length; nrP++) {
                            for (int nrNBL = 0; nrNBL < NBListSize.length; nrNBL++) {
                                for (int nrBeta = 0; nrBeta < beta.length; nrBeta++) {
                                    for (int nrDelta = 0; nrDelta < delta.length; nrDelta++) {
                                        readFile(nrLambda, nrK, nrD, nrP, nrNBL, nrBeta, nrDelta, instance, nrArray, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            nrArray++;
        }

        int nrArrayGolden = 0;
        for (int instance = 9; instance <= 20; instance++) {
            for (int nrLambda = 0; nrLambda < lambdas.length; nrLambda++) {
                for (int nrK = 0; nrK < K.length; nrK++) {
                    for (int nrD = 0; nrD < D.length; nrD++) {
                        for (int nrP = 0; nrP < P.length; nrP++) {
                            for (int nrNBL = 0; nrNBL < NBListSize.length; nrNBL++) {
                                for (int nrBeta = 0; nrBeta < beta.length; nrBeta++) {
                                    for (int nrDelta = 0; nrDelta < delta.length; nrDelta++) {
                                        readFileGolden(nrLambda, nrK, nrD, nrP, nrNBL, nrBeta, nrDelta, instance, nrArrayGolden, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            nrArrayGolden++;
        }

        // Function to create latex tables with sensitivity analysis for given parameters (create table for each parameter)
        writeToLatexLambda(0, "$\\lambda$", lambdas, params);
        writeToLatexInt(1, "$K$", K, params);
        writeToLatexInt(2, "$D$", D, params);
        writeToLatexInt(3, "$P$", P, params);
        writeToLatexInt(4, "$NB$", NBListSize, params);
        writeToLatexDouble(5, "$\\gamma$", beta, params);
        writeToLatexDouble(6, "$\\delta$", delta, params);

        System.out.println("Mean gap: " + meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]]);
        System.out.println("Mean time: " + meanRunningTimes[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]]);

        System.out.println("Mean gap: " + meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]]);
        System.out.println("Mean time: " + meanRunningTimesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]]);

    }

    /**
     * Create one master table with all results
     */
    private static void writeToLatexLambda(int paramIndex, String paramName, double[][] paramValues, int[] defaultParams) {

        // Write y
        try {
            // Create file
            String fileName = paramName;
            fileName = fileName.replace("$", "");
            fileName = fileName.replace("\\", "");
            FileWriter fstream = new FileWriter("../Latex/tex/resultsSensitivityAnalysis_" + fileName + ".tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{ll";
            for (double[] ignored : paramValues) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            line += " & Different $\\lambda$'s";
            for (double[] paramValue : paramValues) {
                line += " & " + paramValue.length;
            }
            line += "\\\\\r\n\\midrule\r\n";

            line += "Christofides instances & ";
            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 1; instance <= 5; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                    for (int instance = 11; instance <= 12; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "& Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\midrule\n";

            line += "Golden instances & ";
            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 9; instance <= 20; instance++) {
                        readFileGolden(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "&Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";

            line += "\r\n\\caption{Sensitivity analysis of " + paramName + ", with ";
            line += "$K=" + K[defaultParams[1]];
            line += "$, $D=" + D[defaultParams[2]];
            line += "$, $P=" + P[defaultParams[3]];
            line += "$, $NB=" + NBListSize[defaultParams[4]];
            line += "$, $\\gamma=" + beta[defaultParams[5]];
            line += "$, $\\delta=" + delta[defaultParams[6]];
            line += "$}";
            line = line.replace("_", "\\_");
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
    private static void writeToLatexInt(int paramIndex, String paramName, int[] paramValues, int[] defaultParams) {

        // Write y
        try {
            // Create file
            String fileName = paramName;
            fileName = fileName.replace("$", "");
            fileName = fileName.replace("\\", "");
            FileWriter fstream = new FileWriter("../Latex/tex/resultsSensitivityAnalysis_" + fileName + ".tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{l";
            for (int ignored : paramValues) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            line += paramName;
            for (int paramValue : paramValues) {
                line += " & " + paramValue;
            }
            line += "\\\\\r\n\\midrule\r\n";

            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 1; instance <= 5; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                    for (int instance = 11; instance <= 12; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\midrule\n";

            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 9; instance <= 20; instance++) {
                        readFileGolden(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";

            line += "\r\n\\caption{Sensitivity analysis of " + paramName + ", with ";
            line += lambdas[defaultParams[0]].length + " $\\lambda$'s, ";
            if (paramIndex != 1) {
                line += "$K=" + K[defaultParams[1]];
            }
            if (paramIndex != 2) {
                if (paramIndex == 1) {
                    line += "$D=" + D[defaultParams[2]];
                } else {
                    line += "$, $D=" + D[defaultParams[2]];
                }
            }
            if (paramIndex != 3) {
                line += "$, $P=" + P[defaultParams[3]];
            }
            if (paramIndex != 4) {
                line += "$, $NB=" + NBListSize[defaultParams[4]];
            }
            line += "$, $\\gamma=" + beta[defaultParams[5]];
            line += "$, $\\delta=" + delta[defaultParams[6]];
            line += "$}";

            line = line.replace("_", "\\_");
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
    private static void writeToLatexDouble(int paramIndex, String paramName, double[] paramValues, int[] defaultParams) {

        // Write y
        try {
            // Create file
            String fileName = paramName;
            fileName = fileName.replace("$", "");
            fileName = fileName.replace("\\", "");
            FileWriter fstream = new FileWriter("../Latex/tex/resultsSensitivityAnalysis_" + fileName + ".tex");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "\\begin{tabular}{l";
            for (double ignored : paramValues) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            line += paramName;
            for (double paramValue : paramValues) {
                line += " & " + paramValue;
            }
            line += "\\\\\r\n\\midrule\r\n";

            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 1; instance <= 5; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                    for (int instance = 11; instance <= 12; instance++) {
                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValues[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\midrule\n";

            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]] == 9999) {
                    int nrArray = 0;
                    for (int instance = 9; instance <= 20; instance++) {
                        readFileGolden(params[0], params[1], params[2], params[3], params[4], params[5], params[6], instance, nrArray, true);
                        nrArray += 1;
                    }
                }
                line += " & " + round(meanGapSolutionValuesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimesGolden[params[0]][params[1]][params[2]][params[3]][params[4]][params[5]][params[6]],2);
            }

            line += "\\\\\r\n\\bottomrule\r\n\\end{tabular}";

            line += "\r\n\\caption{Sensitivity analysis of " + paramName + ", with ";
            line += lambdas[defaultParams[0]].length + " $\\lambda$'s, ";
            line += "$K=" + K[defaultParams[1]];
            line += "$, $D=" + D[defaultParams[2]];
            line += "$, $P=" + P[defaultParams[3]];
            line += "$, $NB=" + NBListSize[defaultParams[4]];
            if (paramIndex != 5) {
                line += "$, $\\gamma=" + beta[defaultParams[5]];
            }
            if (paramIndex != 6) {
                line += "$, $\\delta=" + delta[defaultParams[6]];
            }
            line += "$}";

            line = line.replace("_", "\\_");
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

    private static int[] findBest() {
        double bestGap = Double.MAX_VALUE;
        int bestLambda = -1, bestK = -1, bestD = -1, bestP = -1, bestNBL = -1, bestBeta = -1, bestDelta = -1;

        for (int nrLambda = 0; nrLambda < lambdas.length; nrLambda++) {
            for (int nrK = 0; nrK < K.length; nrK++) {
                for (int nrD = 0; nrD < D.length; nrD++) {
                    for (int nrP = 0; nrP < P.length; nrP++) {
                        for (int nrNBL = 0; nrNBL < NBListSize.length; nrNBL++) {
                            for (int nrBeta = 0; nrBeta < beta.length; nrBeta++) {
                                for (int nrDelta = 0; nrDelta < delta.length; nrDelta++) {
                                    if (meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta]  < bestGap) {
                                        bestGap = meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] ;
                                        bestLambda = nrLambda;
                                        bestK = nrK;
                                        bestD = nrD;
                                        bestP = nrP;
                                        bestNBL = nrNBL;
                                        bestBeta = nrBeta;
                                        bestDelta = nrDelta;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Best: nrLambda = " + lambdas[bestLambda].length + ", K = " + K[bestK] + ", D = " + D[bestD] + ", P = " + P[bestP] + ", NBL = " + NBListSize[bestNBL] + ", beta = " + beta[bestBeta] + ", delta = " + delta[bestDelta]);
        return new int[] {bestLambda, bestK, bestD, bestP, bestNBL, bestBeta, bestDelta};
    }

    /**
     * Read data file of cmt instances
     */
    private static void readDataFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("DataCMT.txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Skip first line
            reader.readLine();

            // Save data
            for (int line = 0; line < nrOfInstances; line++) {
                s = reader.readLine();
                split = s.split("\t");
                bestSolutionValues[line] = Double.parseDouble(split[2]);
            }
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
     * Read data file of golden instances
     */
    private static void readDataFileGolden() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("DataGolden.txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Skip first line
            reader.readLine();

            // Save data
            for (int line = 0; line < nrOfInstancesGolden; line++) {
                s = reader.readLine();
                split = s.split("\t");
                bestSolutionValuesGolden[line] = Double.parseDouble(split[2]);
            }
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
     * Read an output file for one instance for one solver
     */
    private static void readFile(int nrLambda, int nrK, int nrD, int nrP, int nrNBL, int nrBeta, int nrDelta, int instance, int nrArray, boolean forceSolve) {
        BufferedReader reader = null;
        String suffix = " - NrLambda=" + lambdas[nrLambda].length + ", K=" + K[nrK] + ", D=" + D[nrD] + ", P=" + P[nrP] + ", NBL=" + NBListSize[nrNBL] + ", beta=" + beta[nrBeta] + ", delta=" + delta[nrDelta];
        File f = new File("Test Output/Sensitivity Analysis/vrpnc" + instance + "_results_Record2Record_H3_MT" + suffix + ".txt");
        if(!f.exists()){
            solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
            meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta]= 9999;
            solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
            meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] = 9999;
            if (forceSolve) {
                DataReaderCMT dataReaderCMT = new DataReaderCMT();
                String instr = "vrpnc" + instance;
                DataSet dataSet = dataReaderCMT.readFile(instr);
                Solver solver = new RecordToRecordH3MTMaster(lambdas[nrLambda], K[nrK], D[nrD], P[nrP], NBListSize[nrNBL], beta[nrBeta], delta[nrDelta]);
                try {
                    Solution solution = solver.solve(dataSet);
                    writeToFile(instr, solution, suffix);
                } catch (GRBException | IloException e1) {
                    e1.printStackTrace();
                }
            }
        }
            try {
                reader = new BufferedReader(new FileReader("Test Output/Sensitivity Analysis/vrpnc" + instance + "_results_Record2Record_H3_MT" + suffix + ".txt"));

                // Initialize temporary variables
                String s;
                String[] split;

                // Save runtime
                s = reader.readLine();
                split = s.split("\t");
                runningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = Double.parseDouble(split[split.length - 1]);
                meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] += runningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] / nrOfInstances;
                // Save value
                s = reader.readLine();
                split = s.split("\t");
                solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = Double.parseDouble(split[split.length - 1]);
                meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] += ((solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] - bestSolutionValues[nrArray]) / bestSolutionValues[nrArray]) * 100 / nrOfInstances;
            } catch (IOException e) {
                solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
                meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] = 9999;
                solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
                meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] = 9999;
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
     */
    private static void readFileGolden(int nrLambda, int nrK, int nrD, int nrP, int nrNBL, int nrBeta, int nrDelta, int instance, int nrArray, boolean forceSolve) {
        BufferedReader reader = null;
        String suffix = " - NrLambda=" + lambdas[nrLambda].length + ", K=" + K[nrK] + ", D=" + D[nrD] + ", P=" + P[nrP] + ", NBL=" + NBListSize[nrNBL] + ", beta=" + beta[nrBeta] + ", delta=" + delta[nrDelta];
        String instr;
        if (instance < 10) {
            instr = "kelly0" + instance;
        } else {
            instr = "kelly" + instance;
        }
        try {

            reader = new BufferedReader(new FileReader("Test Output/Sensitivity Analysis/" + instr + "_results_Record2Record_H3_MT" + suffix + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Save runtime
            s = reader.readLine();
            split = s.split("\t");
            runningTimesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = Double.parseDouble(split[split.length - 1]);
            meanRunningTimesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] += runningTimesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray]/nrOfInstancesGolden;
            // Save value
            s = reader.readLine();
            split = s.split("\t");
            solutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = Double.parseDouble(split[split.length - 1]);
            meanGapSolutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] += ((solutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] - bestSolutionValuesGolden[nrArray])/bestSolutionValuesGolden[nrArray])*100/nrOfInstancesGolden;
        } catch (IOException e) {
            solutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
            meanRunningTimesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta]= 9999;
            solutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrArray] = 9999;
            meanGapSolutionValuesGolden[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta] = 9999;
            if (forceSolve) {
                DataReaderGolden dataReaderGolden = new DataReaderGolden();
                DataSet dataSet = dataReaderGolden.readFile(instr);
                Solver solver = new RecordToRecordH3MTMaster(lambdas[nrLambda], K[nrK], D[nrD], P[nrP], NBListSize[nrNBL], beta[nrBeta], delta[nrDelta]);
                try {
                    Solution solution = solver.solve(dataSet);
                    writeToFile(instr, solution, suffix);
                } catch (GRBException | IloException e1) {
                    e1.printStackTrace();
                }
            }
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

    private static String round(double before, int precision) {
        double factor = Math.pow(10.0, (double) precision);
        double after = Math.round(factor * before) / factor;
        return ("" + after);
    }

    /**
     * Write a solution to a data file
     *
     * @param instance name of the instance (number with prefix)
     * @param solution solution to the problem
     */
    private static void writeToFile(String instance, Solution solution, String suffix) {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Test Output/Sensitivity Analysis/" + instance + "_results_" + solution.getName() + suffix + ".txt");
//            FileWriter fstream = new FileWriter("Temp/" + instance + "_results_" + solution.getName() + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "Runtime:\t" + solution.getRunTime();
            line += "\r\nObjective value:\t" + solution.getObjectiveValue();
            line += "\r\nGap:\t" + solution.getGap();

            line += "\r\n";
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}