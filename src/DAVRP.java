import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Joeri on 11-4-2014.
 *
 * Main file for the DAVRP problem solver
 */
class DAVRP {

    public static void main(String[] args) {


//        Solver solver = new SolverGurobi();
//        Solver solver = new SolverCplex();
//        Solver solver = new SolverClustering();
//        Solver solver = new SolverClusteringLargest();
        Solver solver = new ClarkeWright();

        int start = 1;
        int end = 65;

        for (int i = start; i <= end; i++) {
            String instance = "DAVRPInstance" + i;
            solveSilent(instance, solver);
        }

    }

    private static void solve(String instance, Solver solver) {
        try {

            Frame frame = new Frame();
            DataReader dataReader = new DataReader();
            DataSet test = dataReader.readFile(instance);
            frame.createMap(test);
            Solution solution = solver.solve(test);
            frame.drawResults(solution);
            writeToFile(instance, solution);

        } catch (GRBException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private static void solveSilent(String instance, Solver solver) {
        try {
            System.out.println("Solving " + instance);
            DataReader dataReader = new DataReader();
            DataSet test = dataReader.readFile(instance);
            Solution solution = solver.solve(test);
            writeToFile(instance, solution);

        } catch (GRBException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(String instance, Solution solution) {
        // Write y
        try {
            // Create file
            FileWriter fstream = new FileWriter("Temp/" + instance + "_results_" + solution.getName() + ".txt");
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