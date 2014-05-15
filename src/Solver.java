import gurobi.GRBException;
import ilog.concert.IloException;

/**
 * Created by Joeri on 29-4-2014.
 */

interface Solver {

    public void solve(DataSet dataSet) throws GRBException, IloException;

    public Solution getSolution();

}