import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.*;

/**
 * Created by Joeri on 15-5-2014.
 * Create class for merging all outputs from different solvers
 */
public class DataMergerSensitivityAnalysis2 {

    private static int nrOfInstances;

    private static double[][] lambdas;
    private static int[] K;
    private static int[] D;
    private static int[] P;
    private static int[] NBListSize;
    private static double[] beta;
    private static double[] delta;

    private static int[] K2;
    private static int[] D2;
    private static int[] P2;
    private static double[] delta2;

//    private static double[][][][][][][][][][][][] solutionValues;
    private static double[][][] solutionValues2;
//    private static double[][][][][][][][][][][][] runningTimes;
    private static double[][][] runningTimes2;

//    private static double[][][][][][][][][][][] meanGapSolutionValues;
    private static double[][] meanGapSolutionValues2;
//    private static double[][][][][][][][][][][] meanRunningTimes;
    private static double[][] meanRunningTimes2;

    private static int[][] bestFile;
    private static double[] bestSolutionValues;

    /**
     * Merge all outputs from different solvers
     *
     * @param args input
     */
    public static void main(String[] args) {

        lambdas = new double[2][];
        lambdas[0] = new double[]{0.6, 1.0, 1.4};
        lambdas[1] = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
        K = new int[]{1, 2, 3, 5, 7, 9};
        D = new int[]{5, 10, 20, 30, 40, 50};
        P = new int[]{1, 2, 3, 4};
        NBListSize = new int[]{20, 30, 40, 50};
        beta = new double[]{0.4, 0.6, 0.8, 1.0};
        delta = new double[]{0.005, 0.01, 0.015};
        K2 = new int[]{1, 2, 3, 5};
        D2 = new int[]{1, 2, 5, 10, 20};
        P2 = new int[]{1, 2, 3, 4};
        delta2 = new double[]{0.005, 0.01, 0.015};

        // Function to find best parameters (lowest gap)

        int[] paramsJoeri = new int[] {1, 4, 1, 1, 1, 3, 1, 1, 1, 1, 1}; //

        int[] paramsLi = new int[] {0, 3, 3, 1, 2, 1, 1, 3, 2, 1, 1}; // 0.81 - 1.94

        int[] paramsGroeer = new int[] {1, 3, 3, 1, 1, 3, 1, 3, 2, 1, 1}; // 0.75 - 5.53

        int[] paramsLiGro = new int[] {0, 3, 3, 1, 1, 3, 1, 3, 2, 1, 1}; // 0.87 - 3.51

        int[] paramsLiGro2 = new int[] {1, 3, 3, 1, 2, 1, 1, 3, 2, 1, 1}; // 0.65 - 2.15

        int[] params3 = new int[] {1, 4, 3, 1, 2, 1, 1, 3, 2, 1, 1}; // 0.58 - 2.39

        int[] params4 = new int[] {1, 4, 3, 1, 2, 1, 1, 1, 2, 1, 1}; // 0.60 - 1.66

        int[] params5 = new int[] {1, 4, 2, 1, 2, 1, 1, 1, 2, 1, 1}; // 0.60 - 1.30

        int[] params = params5;

        nrOfInstances = 20;

//        solutionValues = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][K2.length][D2.length][P2.length][delta2.length][nrOfInstances];
        solutionValues2 = new double[11][10][nrOfInstances];
//        runningTimes = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][K2.length][D2.length][P2.length][delta2.length][nrOfInstances];
        runningTimes2 = new double[11][10][nrOfInstances];

//        meanGapSolutionValues = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][K2.length][D2.length][delta2.length][P2.length];
        meanGapSolutionValues2 = new double[11][nrOfInstances];
//        meanRunningTimes = new double[lambdas.length][K.length][D.length][P.length][NBListSize.length][beta.length][delta.length][K2.length][D2.length][delta2.length][P2.length];
        meanRunningTimes2 = new double[11][nrOfInstances];

        bestSolutionValues = new double[nrOfInstances];

        readDataFile();

        // Function to create latex tables with sensitivity analysis for given parameters (create table for each parameter)
        writeToLatexLambda(0, "$\\lambda$", lambdas, params);
        writeToLatexInt(1, "$K$", K, params);
        writeToLatexInt(2, "$D$", D, params);
        writeToLatexInt(3, "$P$", P, params);
        writeToLatexInt(4, "$NB$", NBListSize, params);
        writeToLatexDouble(5, "$\\gamma$", beta, params);
        writeToLatexDouble(6, "$\\delta$", delta, params);
        writeToLatexInt(7, "$K2$", K2, params);
        writeToLatexInt(8, "$D2$", D2, params);
        writeToLatexInt(9, "$P2$", P2, params);
        writeToLatexDouble(10, "$\\delta 2$", delta2, params);

        saveDataFile();

        System.out.println("Mean gap: " + meanGapSolutionValues2[0][params[0]]);
        System.out.println("Mean time: " + meanRunningTimes2[0][params[0]]);

    }

    /**
     * Create one master table with all results
     */
    private static void writeToLatexLambda(int paramIndex, String paramName, double[][] paramValues, int[] defaultParams) {

        System.out.println("Solving parameter " + paramName);

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
            for (double[] ignored : paramValues) {
                line += "r";
            }
            line += "}\r\n\\toprule\r\n";
            // Title
            line += "Different $\\lambda$'s";
            for (double[] paramValue : paramValues) {
                line += " & " + paramValue.length;
            }
            line += "\\\\\r\n\\midrule\r\n";

            line += "Average gap (\\%)";
            for (int i = 0; i < paramValues.length; i++) {
                System.out.println("\t" + paramValues[i].length);
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanRunningTimes2[paramIndex][params[paramIndex]] == 0) {
                    for (int instance = 0; instance < nrOfInstances; instance++) {
//                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9], params[10], instance, true);
                        readFile2(paramIndex, params, instance, true);
                    }
                }
                line += " & " + round(meanGapSolutionValues2[paramIndex][params[paramIndex]],2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes2[paramIndex][params[paramIndex]],2);
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

    /**
     * Create one master table with all results
     */
    private static void writeToLatexInt(int paramIndex, String paramName, int[] paramValues, int[] defaultParams) {

        System.out.println("Solving parameter " + paramName);

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
                System.out.println("\t" + paramValues[i]);
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanRunningTimes2[paramIndex][params[paramIndex]] == 0) {
                    for (int instance = 0; instance < nrOfInstances; instance++) {
//                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9], params[10], instance, true);
                        readFile2(paramIndex, params, instance, true);
                    }
                }
                line += " & " + round(meanGapSolutionValues2[paramIndex][params[paramIndex]], 2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes2[paramIndex][params[paramIndex]],2);
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

    /**
     * Create one master table with all results
     */
    private static void writeToLatexDouble(int paramIndex, String paramName, double[] paramValues, int[] defaultParams) {

        System.out.println("Solving parameter " + paramName);

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
                System.out.println("\t" + paramValues[i]);
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                if (meanRunningTimes2[paramIndex][params[paramIndex]] == 0) {
                    for (int instance = 0; instance < nrOfInstances; instance++) {
//                        readFile(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9], params[10], instance, true);
                        readFile2(paramIndex, params, instance, true);
                    }
                }
                line += " & " + round(meanGapSolutionValues2[paramIndex][params[paramIndex]], 2);
            }

            line += "\\\\\r\n";

            line += "Average runtime (s)";
            for (int i = 0; i < paramValues.length; i++) {
                int[] params = new int[defaultParams.length];
                System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
                params[paramIndex] = i;
                line += " & " + round(meanRunningTimes2[paramIndex][params[paramIndex]],2);
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

    /**
     * Read data file of cmt instances
     */
    private static void readDataFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("BestJoeri.txt"));

            bestFile = new int[nrOfInstances][3];

            // Initialize temporary variables
            String s;
            String[] split;

            // Save data
            for (int line = 0; line < nrOfInstances; line++) {
                s = reader.readLine();
                split = s.split("\t");
                for (int i = 0; i < 3; i++) {
                    bestFile[line][i] = Integer.parseInt(split[i]);
                }
                bestSolutionValues[line] = Double.parseDouble(split[3]);
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
     * Create one master table with all results
     */
    private static void saveDataFile() {

        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("BestJoeri.txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            for (int i = 0; i < nrOfInstances; i++) {
                for (int j = 0; j < 3; j++) {
                    line += bestFile[i][j] + "\t";
                }
                line += bestSolutionValues[i] + "\r\n";
            }

            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error in writing file: " + e.getMessage());
        }
    }

//    /**
//     * Read an output file for one instance for one solver
//     */
//    private static void readFile(int nrLambda, int nrK, int nrD, int nrP, int nrNBL, int nrBeta, int nrDelta, int nrK2, int nrD2, int nrP2, int nrDelta2, int instance, boolean forceSolve) {
//        BufferedReader reader = null;
//        String prefix = "DAVRPinstance " + bestFile[instance][0] + " " + bestFile[instance][1] + " " + bestFile[instance][2];
//        String suffix = " - NrLambda=" + lambdas[nrLambda].length + ", K=" + K[nrK] + ", D=" + D[nrD] + ", P=" + P[nrP] + ", NBL=" + NBListSize[nrNBL] + ", beta=" + beta[nrBeta] + ", delta=" + delta[nrDelta] + ", K2=" + K2[nrK2] + ", D2=" + D2[nrD2] + ", P2=" + P2[nrP2] + ", delta2=" + delta2[nrDelta2];
//        String suffixOld = " - NrLambda=" + lambdas[nrLambda].length + ", K=" + K[nrK] + ", D=" + D[nrD] + ", P=" + P[nrP] + ", NBL=" + NBListSize[nrNBL] + ", beta=" + beta[nrBeta] + ", delta=" + delta[nrDelta] + ", K2=" + K2[nrK2] + ", D2=" + D2[nrD2] + ", P2=" + P2[nrP2];
//
//        File f = new File("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffix + ".txt");
//        File fOld = new File("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffixOld + ".txt");
//
//        if(!f.exists()){
//            if (delta2[nrDelta2]==0.01 && fOld.exists()) {
//                suffix = suffixOld;
//            } else {
//                solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = 9999;
//                meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2]= 9999;
//                solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = 9999;
//                meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2] = 9999;
//                if (forceSolve) {
//                    DataReaderJoeri dataReaderJoeri = new DataReaderJoeri();
//                    DataSet dataSet = dataReaderJoeri.readFile(prefix);
//                    Solver solver = new RecordToRecordDAVRPH4MTMaster(lambdas[nrLambda], K[nrK], D[nrD], P[nrP], NBListSize[nrNBL], beta[nrBeta], delta[nrDelta], K2[nrK2], D2[nrD2], P2[nrP2], delta2[nrDelta2]);
//                    try {
//                        Solution solution = solver.solve(dataSet);
//                        writeToFile(prefix, solution, suffix);
//                    } catch (GRBException | IloException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        }
//        try {
//            reader = new BufferedReader(new FileReader("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffix + ".txt"));
//
//            // Initialize temporary variables
//            String s;
//            String[] split;
//
//            // Save runtime
//            s = reader.readLine();
//            split = s.split("\t");
//            runningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = Double.parseDouble(split[split.length - 1]);
//            meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2] += runningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] / nrOfInstances;
//            // Save value
//            s = reader.readLine();
//            split = s.split("\t");
//            double solutionValue = Double.parseDouble(split[split.length - 1]);
//            solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = solutionValue;
//            if (solutionValue < bestSolutionValues[instance]) {
//                bestSolutionValues[instance] = solutionValue;
//            }
//            meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2] += ((solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] - bestSolutionValues[instance]) / bestSolutionValues[instance]) * 100 / nrOfInstances;
//        } catch (IOException e) {
//            solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = 9999;
//            meanRunningTimes[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2] = 9999;
//            solutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2][instance] = 9999;
//            meanGapSolutionValues[nrLambda][nrK][nrD][nrP][nrNBL][nrBeta][nrDelta][nrK2][nrD2][nrP2][nrDelta2] = 9999;
//        } finally {
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException e) {
//                System.out.println("Error closing the reader");
//            }
//        }
//    }

    /**
     * Read an output file for one instance for one solver
     */
    private static void readFile2(int paramIndex, int[] params, int instance, boolean forceSolve) {
        BufferedReader reader = null;
        String prefix = "DAVRPinstance " + bestFile[instance][0] + " " + bestFile[instance][1] + " " + bestFile[instance][2];
        String suffix = " - NrLambda=" + lambdas[params[0]].length + ", K=" + K[params[1]] + ", D=" + D[params[2]] + ", P=" + P[params[3]] + ", NBL=" + NBListSize[params[4]] + ", beta=" + beta[params[5]] + ", delta=" + delta[params[6]] + ", K2=" + K2[params[7]] + ", D2=" + D2[params[8]] + ", P2=" + P2[params[9]] + ", delta2=" + delta2[params[10]];
        String suffixOld = " - NrLambda=" + lambdas[params[0]].length + ", K=" + K[params[1]] + ", D=" + D[params[2]] + ", P=" + P[params[3]] + ", NBL=" + NBListSize[params[4]] + ", beta=" + beta[params[5]] + ", delta=" + delta[params[6]] + ", K2=" + K2[params[7]] + ", D2=" + D2[params[8]] + ", P2=" + P2[params[9]];

        File f = new File("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffix + ".txt");
        File fOld = new File("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffixOld + ".txt");

        if(!f.exists()){
            if (delta2[params[10]]==0.01 && fOld.exists()) {
                suffix = suffixOld;
            } else {
                solutionValues2[paramIndex][params[paramIndex]][instance] = 9999;
                meanRunningTimes2[paramIndex][params[paramIndex]]= 9999;
                solutionValues2[paramIndex][params[paramIndex]][instance] = 9999;
                meanGapSolutionValues2[paramIndex][params[paramIndex]] = 9999;
                if (forceSolve) {
                    DataReaderJoeri dataReaderJoeri = new DataReaderJoeri();
                    DataSet dataSet = dataReaderJoeri.readFile(prefix);
                    Solver solver = new RecordToRecordDAVRPH4MTMaster(lambdas[params[0]], K[params[1]], D[params[2]], P[params[3]], NBListSize[params[4]], beta[params[5]], delta[params[6]], K2[params[7]], D2[params[8]], P2[params[9]], delta2[params[10]]);
                    try {
                        Solution solution = solver.solve(dataSet);
                        writeToFile(prefix, solution, suffix);
                    } catch (GRBException | IloException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        try {
            reader = new BufferedReader(new FileReader("Test Output New/Sensitivity Analysis/" + prefix + "_results_RTR_DAVRP_H4_MT" + suffix + ".txt"));

            // Initialize temporary variables
            String s;
            String[] split;

            // Save runtime
            s = reader.readLine();
            split = s.split("\t");
            runningTimes2[paramIndex][params[paramIndex]][instance] = Double.parseDouble(split[split.length - 1]);
            meanRunningTimes2[paramIndex][params[paramIndex]] += runningTimes2[paramIndex][params[paramIndex]][instance] / nrOfInstances;
            // Save value
            s = reader.readLine();
            split = s.split("\t");
            double solutionValue = Double.parseDouble(split[split.length - 1]);
            solutionValues2[paramIndex][params[paramIndex]][instance] = solutionValue;
            if (solutionValue < bestSolutionValues[instance]) {
                bestSolutionValues[instance] = solutionValue;
            }
            meanGapSolutionValues2[paramIndex][params[paramIndex]] += ((solutionValues2[paramIndex][params[paramIndex]][instance] - bestSolutionValues[instance]) / bestSolutionValues[instance]) * 100 / nrOfInstances;
        } catch (IOException e) {
            solutionValues2[paramIndex][params[paramIndex]][instance] = 9999;
            meanRunningTimes2[paramIndex][params[paramIndex]] = 9999;
            solutionValues2[paramIndex][params[paramIndex]][instance] = 9999;
            meanGapSolutionValues2[paramIndex][params[paramIndex]] = 9999;
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
            FileWriter fstream = new FileWriter("Test Output New/Sensitivity Analysis/" + instance + "_results_" + solution.getName() + suffix + ".txt");
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