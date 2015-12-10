import gurobi.GRBException;
import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Created by Joeri on 4-10-2015.
 */
public class SensitivityN {

    public static void main(String[] args) {

//        Solver solver = new RecordToRecordDAVRPH4MTMaster(); // Heuristic for DAVRP
        Solver solver = new RecordToRecordDAVRP2MTMaster(); // Heuristic for DAVRP with cplex-clustering

        int[] nrOfCustomers = new int[] {15, 15, 100};
        int[] nrOfScenarios = new int[] {3};

        for (int n = 0; n < nrOfCustomers.length; n++) {
            for (int omega = 0; omega < nrOfScenarios.length; omega++) {
                for (int i = 1; i <= 10; i++) {
                    String instance = "DAVRPInstance " + nrOfCustomers[n] + " " + nrOfScenarios[omega] + " " + i;
                    try {
                        System.out.print("Solving " + instance);
                        DataReaderJoeri dataReader = new DataReaderJoeri();
                        DataSet dataSet = dataReader.readFile(instance);
                        Solution solution = solver.solve(dataSet);
                        if (solution != null) {
                            System.out.println("\tValue: " + round(solution.getObjectiveValue(), 2) + "\tTime: " + solution.getRunTime());
                            writeToFile(instance, solution);
                        }
                    } catch (IloException | GRBException e) {
                        e.printStackTrace();
                    }
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
            FileWriter fstream = new FileWriter("Test Output New/SensitivityN/" + instance + "_results_" + solution.getName() + ".txt");
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