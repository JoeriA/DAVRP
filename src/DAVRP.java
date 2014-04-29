import gurobi.GRBException;
import ilog.concert.IloException;

/**
 * Created by Joeri on 11-4-2014.
 */
class DAVRP {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader();
        DataSet test = dataReader.readFile("Test Instances/DAVRPInstance1");
        Solver solver = new SolverGurobi();
        //Solver solver = new SolverCplex();
        try {
            solver.solve(test);
        } catch (GRBException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }

    }
}