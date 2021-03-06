import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Joeri on 11-4-2014.
 * <p/>
 * Main file for the DAVRP problem solver
 */
class DAVRP {

    /**
     * Run main program
     *
     * @param args input arguments
     */
    public static void main(String[] args) {

//        Solver solver = new SolverCplex();
//        Solver solver = new SolverGurobi();
//        Solver solver = new SolverCplexLargest();
//        Solver solver = new SolverClustering();
//        Solver solver = new SolverClusteringLargest();
//        Solver solver = new ClarkeWright();
//        Solver solver = new RecordToRecordH3MTMaster(); // Heuristic for VRP
//        Solver solver = new RecordToRecordDAVRPH4MTMaster(); // Heuristic for DAVRP
        Solver solver = new RecordToRecordDAVRP2MTMaster(); // Heuristic for DAVRP with cplex-clustering
//        Solver solver = new RecordToRecordDAVRPH5MTMaster(); // Heuristic for DAVRP

        int start = 86; // min is 1
        int end = 95; // max is 125
        boolean testCMT = false;
        boolean testGolden = false;

        boolean silent = true;
        boolean warmUp = true;

        if (warmUp && end >= start) {
            for (int i = 1; i <= 10; i++) {
                String instance = "DAVRPInstance" + i;
                try {
                    System.out.print("Solving " + instance);
                    DataReader dataReader = new DataReader();
                    DataSet dataSet = dataReader.readFile(instance);
                    Solution solution = solver.solve(dataSet);
                    if (solution != null) {
                        System.out.println("\tValue: " + round(solution.getObjectiveValue(),2) + "\tTime: " + solution.getRunTime());
                    }
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = start; i <= end; i++) {
            if (i <= 65) {
                String instance = "DAVRPInstance" + i;
                try {
                    System.out.print("Solving " + instance);
                    Frame frame = new Frame();
                    DataReader dataReader = new DataReader();
                    DataSet dataSet = dataReader.readFile(instance);
                    if (!silent) {
                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    if (solution != null) {
                        System.out.println("\tValue: " + round(solution.getObjectiveValue(),2) + "\tTime: " + solution.getRunTime());
                        if (!silent) {
                            frame.drawResults(solution);
                        }
                        writeToFile(instance, solution);
                    }
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            } else if (i <= 95 || i >= 106) {
                for (int j = 3; j <= 3; j++) {
                    String instance = "DAVRPInstance" + i + "_" + j;
                    try {
                        System.out.print("Solving " + instance);
                        Frame frame = new Frame();
                        DataReader dataReader = new DataReader();
                        DataSet dataSet = dataReader.readFile(instance);
                        if (!silent) {
                            frame.createMap(dataSet);
                        }
                        Solution solution = solver.solve(dataSet);
                        if (solution != null) {
                            System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                            if (!silent) {
                                frame.drawResults(solution);
                            }
                            writeToFile(instance, solution);
                        }
                    } catch (IloException | GRBException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String instance = "DAVRPInstance" + i + "_1";
                try {
                    System.out.print("Solving " + instance);
                    Frame frame = new Frame();
                    DataReader dataReader = new DataReader();
                    DataSet dataSet = dataReader.readFile(instance);
                    if (!silent) {
                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    if (solution != null) {
                        System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                        if (!silent) {
                            frame.drawResults(solution);
                        }
                        writeToFile(instance, solution);
                    }
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        if (testCMT && warmUp) {
            DataReaderCMT dataReaderCMT = new DataReaderCMT();
            for (int i = 1; i <= 3; i++) {
                String instance = "vrpnc" + i;
                try {
                    System.out.print("Solving " + instance);
                    DataSet dataSet = dataReaderCMT.readFile(instance);
                    if (dataSet.getDropTime() != 0 || dataSet.getMaxDuration() != 999999) {
                        System.out.println("Problem contains not implemented restrictions");
                        continue;
                    }
                    Solution solution = solver.solve(dataSet);
                    System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        if (testCMT) {
            DataReaderCMT dataReaderCMT = new DataReaderCMT();
            for (int i = 1; i <= 14; i++) {
                String instance = "vrpnc" + i;
                try {
                    System.out.print("Solving " + instance);
//                    Frame frame = new Frame();
                    DataSet dataSet = dataReaderCMT.readFile(instance);
                    if (dataSet.getDropTime() != 0 || dataSet.getMaxDuration() != 999999) {
                        System.out.println("Problem contains not implemented restrictions");
                        continue;
                    }
                    if (!silent) {
//                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    if (!silent) {
//                        frame.drawResults(solution);
                    }
                    System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                    writeToFile(instance, solution);
                } catch (IloException | GRBException e) {
                    e.printStackTrace();
                }
            }
        }

        if (testGolden && !testCMT && warmUp) {
            DataReaderGolden dataReaderGolden = new DataReaderGolden();
            String instance = "kelly09";
            try {
                System.out.print("Solving " + instance);
                DataSet dataSet = dataReaderGolden.readFile(instance);
                Solution solution = solver.solve(dataSet);
                System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
            } catch (IloException | GRBException e) {
                e.printStackTrace();
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
                    System.out.print("Solving " + instance);
                    Frame frame = new Frame();
                    DataSet dataSet = dataReaderGolden.readFile(instance);
                    if (dataSet.getDropTime() != 0 || dataSet.getMaxDuration() != 999999) {
                        System.out.println("Problem contains not implemented restrictions");
                        continue;
                    }
                    if (!silent) {
                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    if (!silent) {
//                        frame.drawResults(solution);
                    }
                    System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                    writeToFile(instance, solution);
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
    private static void writeToFile(String instance, Solution solution) {
        // Write y
        try {
            // Create file
//            FileWriter fstream = new FileWriter("Test Output/New param/" + instance + "_results_" + solution.getName() + ".txt");
            FileWriter fstream = new FileWriter("Test Output New/" + instance + "_results_" + solution.getName() + ".txt");
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