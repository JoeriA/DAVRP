import gurobi.GRBException;
import ilog.concert.*;
import ilog.cplex.IloCplex;

/**
 * Created by Joeri on 7-5-2014.
 * <p/>
 * Solver implementing the clustering algorithm of Spliet(2013)
 */

public class SolverClustering implements Solver {

    private double beta = 7.1;
//    private double beta = 0.0;

    /**
     * Create the solver clustering
     */
    public SolverClustering() {

    }

    /**
     * Solve a problem with this solver
     *
     * @param dataSet data set containing information about problem instance
     * @return the solution of the solver
     * @throws GRBException error when Gurobi cannot solve
     * @throws IloException error when CPlex cannot solve
     */
    public Solution solve(DataSet dataSet) throws GRBException, IloException {

        Solution solution = new Solution();
        solution.setName("ClusteringRemy");

        // Get some data from dataset
        int n = dataSet.getNumberOfCustomers() + 1;
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        Customer[] customers = dataSet.getCustomers();
        double[] p = dataSet.getScenarioProbabilities();
        double[][] c = dataSet.getTravelCosts();
        double alpha = dataSet.getAlpha();

        // Create environment
        IloCplex model = new IloCplex();

        // Create variables
        IloNumVar[] y = new IloNumVar[n];
        for (int j = 1; j < n; j++) {
            y[j] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "y" + j);
        }
        IloNumVar[][] z = new IloNumVar[n][n];
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                z[i][j] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "z" + i + "_" + j);
            }
        }
        IloNumVar[][][] z2 = new IloNumVar[n][n][o];
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                for (int k = 0; k < o; k++) {
                    z2[i][j][k] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "z" + i + "_" + j + "^" + o);
                }
            }
        }

        // Set objective
        IloLinearNumExpr expr = model.linearNumExpr();
        for (int j = 1; j < n; j++) {
            expr.addTerm(2.0 * (c[0][j] + beta), y[j]);
            for (int i = 1; i < n; i++) {
                for (int omega = 0; omega < o; omega++) {
                    expr.addTerm(2.0 * p[omega] * c[i][j], z2[i][j][omega]);
                    expr.addTerm(2.0 * p[omega] * c[0][i], z[i][j]);
                    expr.addTerm(-2.0 * p[omega] * c[0][i], z2[i][j][omega]);
                }
            }
        }
        IloObjective obj = model.minimize(expr);
        model.add(obj);

        // Add restrictions
        // 2
        for (int i = 1; i < n; i++) {
            expr = model.linearNumExpr();
            for (int j = 1; j < n; j++) {
                expr.addTerm(1.0, z[i][j]);
            }
            model.addEq(expr, 1.0, "c2" + i);
        }
        // 3
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                expr = model.linearNumExpr();
                expr.addTerm(1.0, z[i][j]);
                expr.addTerm(-1.0, y[j]);
                model.addLe(expr, 0.0, "c3" + i + "_" + j);
            }
        }
        // 4
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                for (int k = 0; k < o; k++) {
                    expr = model.linearNumExpr();
                    expr.addTerm(1.0, z2[i][j][k]);
                    expr.addTerm(-1.0, z[i][j]);
                    model.addLe(expr, 0.0, "c4" + i + "_" + j + "^" + k);
                }
            }
        }
        // 5
        for (int j = 1; j < n; j++) {
            for (int k = 0; k < o; k++) {
                expr = model.linearNumExpr();
                for (int i = 1; i < n; i++) {
                    expr.addTerm(customers[i].getDemandPerScenario()[k], z2[i][j][k]);
                }
                expr.addTerm(-Q, y[j]);
                model.addLe(expr, 0.0, "c5" + j + "^" + k);
            }
        }
        // 6
        for (int j = 1; j < n; j++) {
            for (int k = 0; k < o; k++) {
                expr = model.linearNumExpr();
                for (int i = 1; i < n; i++) {
                    expr.addTerm(alpha, z[i][j]);
                    expr.addTerm(-1.0, z2[i][j][k]);
                }
                model.addLe(expr, 0.0, "c5" + j + "^" + k);
            }
        }

        // Optimize model
        model.setParam(IloCplex.DoubleParam.TiLim, 3600.0);
        model.setParam(IloCplex.DoubleParam.EpGap, 0.05);
        model.setOut(null);
        Long start = System.currentTimeMillis();
        model.solve();
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(model.getObjValue());
        solution.setGap(model.getMIPRelativeGap());
        double[][] zSol = new double[n][n];
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                zSol[i][j] = model.getValue(z[i][j]);
            }
        }
        solution.setzSol(zSol);

        double[][][] skip = new double[n][n][o];
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                for (int omega = 0; omega < o; omega++) {
                    skip[i][j][omega] = model.getValue(z2[i][j][omega]);
                }

            }
        }
        solution.setzSolSkip(skip);

        model.clearModel();

        return solution;

    }
}
