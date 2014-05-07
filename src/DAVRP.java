import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Joeri on 11-4-2014.
 */
class DAVRP {
    public static void main(String[] args) {

        String instance = "DAVRPInstance1";

        DataReader dataReader = new DataReader();
        DataSet test = dataReader.readFile("Test Instances/" + instance);
//        Solver solver = new SolverGurobi();
//        Solver solver = new SolverCplex();
//        Solver solver = new SolverClustering();
        Solver solver = new SolverClusteringLargest();
        try {
            solver.solve(test);
            System.out.println("Runtime: " + solver.getRunTime() + " seconds");
            System.out.println("Objective value: " + solver.getObjectiveValue());
            System.out.println("Gap: " + solver.getGap());
            writeToFile(instance, solver);

        } catch (GRBException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }

    }

    private static void writeToFile(String instance, Solver solver) {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Test Instances/" + instance + "_results_" + solver.getName() + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);

            String line = "";

            line += "Runtime:\t" + solver.getRunTime();
            line += "\r\nObjective value:\t" + solver.getObjectiveValue();
            line += "\r\nGap:\t" + solver.getGap();

            line += "\r\n";
            out.write(line);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
}