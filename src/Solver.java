import gurobi.GRBException;
import ilog.concert.IloException;

/**
 * Created by Joeri on 29-4-2014.
 * <p/>
 * Class defining how solvers should behave
 */

interface Solver {

    /**
     * Solve a problem with this solver
     *
     * @param dataSet data set containing information about problem instance
     * @return the solution of the solver
     * @throws GRBException error when Gurobi cannot solve
     * @throws IloException error when CPlex cannot solve
     */
    public Solution solve(DataSet dataSet) throws GRBException, IloException;

}