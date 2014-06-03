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


//        Solver solver = new SolverGurobi();
//        Solver solver = new SolverCplex();
//        Solver solver = new SolverCplexLargest();
//        Solver solver = new SolverClustering();
//        Solver solver = new SolverClusteringLargest();
//        Solver solver = new ClarkeWright();
        Solver solver = new RecordToRecord();
//        Solver solver = new RecordToRecordLowerBound();
//        Solver solver = new RecordToRecordDAVRP();

        int start = 65;
        int end = 65;

        boolean silent = true;

        DataReader dataReader = new DataReader();
        DataSet dataSet;

        for (int i = start; i <= end; i++) {
            if (i <= 65) {
                String instance = "DAVRPInstance" + i;
                try {
                    System.out.println("Solving " + instance);
                    Frame frame = new Frame();
                    dataSet = dataReader.readFile(instance);
                    if (!silent) {
                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                    if (!silent) {
                        frame.drawResults(solution);
                    }
                    writeToFile(instance, solution);
                } catch (GRBException e) {
                    e.printStackTrace();
                } catch (IloException e) {
                    e.printStackTrace();
                }
            } else if (i <= 95 || i >= 106) {
                for (int j = 1; j <= 3; j++) {
                    String instance = "DAVRPInstance" + i + "_" + j;
                    try {
                        System.out.println("Solving " + instance);
                        Frame frame = new Frame();
                        dataSet = dataReader.readFile(instance);
                        if (!silent) {
                            frame.createMap(dataSet);
                        }
                        Solution solution = solver.solve(dataSet);
                        System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                        if (!silent) {
                            frame.drawResults(solution);
                        }
                        writeToFile(instance, solution);
                    } catch (GRBException e) {
                        e.printStackTrace();
                    } catch (IloException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String instance = "DAVRPInstance" + i + "_1";
                try {
                    System.out.println("Solving " + instance);
                    Frame frame = new Frame();
                    dataSet = dataReader.readFile(instance);
                    if (!silent) {
                        frame.createMap(dataSet);
                    }
                    Solution solution = solver.solve(dataSet);
                    System.out.println("\tValue: " + solution.getObjectiveValue() + "\tTime: " + solution.getRunTime());
                    if (!silent) {
                        frame.drawResults(solution);
                    }
                    writeToFile(instance, solution);
                } catch (GRBException e) {
                    e.printStackTrace();
                } catch (IloException e) {
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
            FileWriter fstream = new FileWriter("Test Output/" + instance + "_results_" + solution.getName() + ".txt");
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