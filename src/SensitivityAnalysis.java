import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Joeri on 11-4-2014.
 * <p/>
 * Test sensitivity of RTR parameters
 */
class SensitivityAnalysis {

    /**
     * Run main program
     *
     * @param args input arguments
     */
    public static void main(String[] args) {

        boolean testCMT = true;
        boolean testGolden = false;

//        double[][] lambdas = new double[2][];
//        lambdas[0] = new double[]{0.6, 1.0, 1.4};
//        lambdas[1] = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
//        int[] K = new int[]{3, 5, 7};
//        int[] D = new int[]{20, 30, 40, 50};
//        int[] P = new int[]{1, 2, 3};
//        int[] NBListSize = new int[]{20, 30, 40, 50};
//        double[] beta = new double[]{0.4, 0.6, 1.0};
//        double[] delta = new double[]{0.005, 0.01, 0.015, 0.02};

        double[][] lambdas = new double[1][];
//        lambdas[0] = new double[]{0.6, 1.0, 1.4};
        lambdas[0] = new double[]{0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
        int[] K = new int[]{5, 7};
        int[] D = new int[]{20};
        int[] P = new int[]{1, 2};
        int[] NBListSize = new int[]{40, 50};
        double[] beta = new double[]{0.6, 0.8, 1.0};
        double[] delta = new double[]{0.01, 0.015, 0.02};

        if (testCMT) {
            DataReaderCMT dataReaderCMT = new DataReaderCMT();
            for (int i = 1; i <= 14; i++) {
                String instance = "vrpnc" + i;
                try {
                    System.out.println("Solving " + instance);
                    DataSet dataSet = dataReaderCMT.readFile(instance);
                    if (dataSet.getDropTime() != 0 || dataSet.getMaxDuration() != 999999) {
                        continue;
                    }
                    for (double[] aLambda : lambdas) {
                        for (int aK : K) {
                            for (int aD : D) {
                                for (int aP : P) {
                                    for (int aNBL : NBListSize) {
                                        for (double aBeta : beta) {
                                            for (double aDelta : delta) {
                                                String suffix = " - NrLambda=" + aLambda.length + ", K=" + aK + ", D=" + aD + ", P=" + aP + ", NBL=" + aNBL + ", beta=" + aBeta + ", delta=" + aDelta;
                                                File f = new File("Test Output/Sensitivity Analysis/" + instance + "_results_Record2Record_H3_MT" + suffix + ".txt");
                                                if(!f.exists()) {
                                                    Solver solver = new RecordToRecordH3MTMaster(aLambda, aK, aD, aP, aNBL, aBeta, aDelta);
                                                    Solution solution = solver.solve(dataSet);
                                                    writeToFile(instance, solution, suffix);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        if (testGolden) {
            DataReaderGolden dataReaderGolden = new DataReaderGolden();
            for (int i = 1; i <= 20; i++) {
                String instance;
                if (i < 10) {
                    instance = "kelly0" + i;
                } else {
                    instance = "kelly" + i;
                }
                try {
                    System.out.println("Solving " + instance);
                    DataSet dataSet = dataReaderGolden.readFile(instance);
                    if (dataSet.getDropTime() != 0 || dataSet.getMaxDuration() != 999999) {
                        continue;
                    }
                    for (double[] aLambda : lambdas) {
                        for (int aK : K) {
                            for (int aD : D) {
                                for (int aP : P) {
                                    for (int aNBL : NBListSize) {
                                        for (double aBeta : beta) {
                                            for (double aDelta : delta) {
                                                Solver solver = new RecordToRecordH3MTMaster(aLambda, aK, aD, aP, aNBL, aBeta, aDelta);
                                                Solution solution = solver.solve(dataSet);
                                                String suffix = " - NrLambda=" + aLambda.length + ", K=" + aK + ", D=" + aD + ", P=" + aP + ", NBL=" + aNBL + ", beta=" + aBeta + ", delta=" + aDelta;
                                                writeToFile(instance, solution, suffix);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }
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