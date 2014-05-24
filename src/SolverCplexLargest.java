/**
 * Created by Joeri on 25-4-2014.
 *
 * Class for solving the DAVRP exactly with CPlex
 */

import gurobi.GRBException;
import ilog.concert.*;
import ilog.cplex.IloCplex;

public class SolverCplexLargest implements Solver {

    /**
     * Create the exact solver with cplex
     */
    public SolverCplexLargest() {

    }

    /**
     * Solve a problem with this solver
     *
     * @param dataSet data set containing information about problem instance
     * @return the solution of the solver
     * @throws gurobi.GRBException       error when Gurobi cannot solve
     * @throws ilog.concert.IloException error when CPlex cannot solve
     */
    public Solution solve(DataSet dataSet) throws GRBException, IloException {
        Solution solution = new Solution();
        solution.setName("CPLEX largest");

        // Get some data from dataset
        int n = dataSet.getNumberOfCustomers() + 1;
        int m = dataSet.getNumberOfVehicles();
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        Customer[] customers = dataSet.getCustomers();
        double[] p = dataSet.getScenarioProbabilities();
        double[][] c = dataSet.getTravelCosts();
        double alpha = dataSet.getAlpha();

        // Get largest demands
        int[] demands = new int[n];
        int highestDemand;
        for (int i = 1; i < demands.length; i++) {
            highestDemand = 0;
            // Get highest demand of all scenarios
            for (int k = 0; k < o; k++) {
                if (customers[i].getDemandPerScenario()[k] > highestDemand) {
                    highestDemand = customers[i].getDemandPerScenario()[k];
                }
            }
            demands[i] = highestDemand;
            customers[i].setDemand(highestDemand);
        }
        customers[0].setDemand(0);

        // Create environment
        IloCplex model = new IloCplex();
        // Create variables
        IloNumVar[][][] f = new IloNumVar[n][n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    f[i][j][k] = model.numVar(0.0, (double) Q, IloNumVarType.Float, "f" + i + "_" + j + "_" + k);
                }
            }
        }
        IloNumVar[][][] x = new IloNumVar[n][n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    x[i][j][k] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "x" + i + "_" + j + "_" + k);
                }
            }
        }
        // Set objective
        IloLinearNumExpr expr = model.linearNumExpr();
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(0.0, f[i][j][k]);
                        expr.addTerm(c[i][j], x[i][j][k]);
                    }
            }
        }
        IloObjective obj = model.minimize(expr);
        model.add(obj);

        // Add restrictions
        // 3
        for (int i = 1; i < n; i++) {
                expr = model.linearNumExpr();
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, x[i][j][k]);
                    }
                }
            model.addEq(expr, 1.0, "c3" + i);
        }
        // 4
            for (int k = 0; k < m; k++) {
                expr = model.linearNumExpr();
                for (int j = 1; j < n; j++) {
                    expr.addTerm(1.0, x[0][j][k]);
                }
                model.addLe(expr, 1.0, "c4" + k);
            }
        // 5
        for (int i = 0; i < n; i++) {
                for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, x[i][j][k]);
                        expr.addTerm(-1.0, x[j][i][k]);
                    }
                    model.addEq(expr, 0.0, "c5" + i + "_" + k);
                }
        }
        // 6
        for (int i = 1; i < n; i++) {
                for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, f[j][i][k]);
                        expr.addTerm(-1.0, f[i][j][k]);
                        expr.addTerm(-(double) customers[i].getDemand(), x[i][j][k]);
                    }
                    model.addEq(expr, 0.0, "c6" + i + "_" + k);
                }
        }
        // 7
        for (int i = 0; i < n; i++) {
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
                        expr = model.linearNumExpr();
                        expr.addTerm(1.0, f[i][j][k]);
                        expr.addTerm((double) (customers[i].getDemand() - Q), x[i][j][k]);
                        model.addLe(expr, 0.0, "c7" + i + "_" + j + "_" + k);
                    }
                }
        }
        // 8
        for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int i = 0; i < n; i++) {
                        expr = model.linearNumExpr();
                        expr.addTerm(1.0, f[i][j][k]);
                        expr.addTerm(-(double) customers[j].getDemand(), x[i][j][k]);
                        model.addGe(expr, 0.0, "c8" + i + "_" + j + "_" + k);
                    }
                }
        }
        // Valid inequalities
        int sum;
            sum = 0;
            expr = model.linearNumExpr();
            for (int i = 1; i < n; i++) {
                for (int k = 0; k < m; k++) {
                    expr.addTerm(1.0, x[0][i][k]);
                }
                sum += customers[i].getDemand();
            }
        model.addGe(expr, Math.ceil((double) sum / (double) Q), "v1");
        for (int k = 0; k < m - 1; k++) {
                expr = model.linearNumExpr();
                for (int i = 0; i < n; i++) {
                    expr.addTerm(1.0, x[0][i][k]);
                    expr.addTerm(-1.0, x[0][i][k + 1]);
                }
            model.addGe(expr, 0.0, "v3" + k);
        }
        // Disallowed variables
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                expr.addTerm(1.0, x[j][j][k]);
                    model.addEq(expr, 0.0, "d" + j);
            }
        }
        // Optimize model
        model.setParam(IloCplex.DoubleParam.TiLim, 3000.0);
        model.setParam(IloCplex.DoubleParam.EpGap, 0.0001);
        Long start = System.currentTimeMillis();
        model.solve();
        Long end = System.currentTimeMillis();
        solution.setRunTime((end - start) / 1000.0);
        solution.setObjectiveValue(model.getObjValue());
        solution.setGap(model.getMIPRelativeGap() * 100.0);
        double[][][][] xSol = new double[n][n][m][1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    xSol[i][j][k][0] = model.getValue(x[i][j][k]);
                }
            }
        }
        solution.setxSol(xSol);

        model.clearModel();

        return solution;
    }
}
