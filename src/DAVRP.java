import gurobi.GRBException;

/**
 * Created by Joeri on 11-4-2014.
 */
class DAVRP {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader();
        DataSet test = dataReader.readFile("Test Instances/DAVRPInstance1");
        SolverGurobi solver = new SolverGurobi();
        try {
            solver.solve(test);
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}