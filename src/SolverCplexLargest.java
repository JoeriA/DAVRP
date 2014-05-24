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
        solution.setName("Exact method (CPLEX)");

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
        IloNumVar[][] a = new IloNumVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                a[i][k] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "a" + i + "_" + k);
            }
        }
        IloNumVar[][][] d = new IloNumVar[n][m][o];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                for (int omega = 0; omega < o; omega++) {
                    d[i][k][omega] = model.numVar(0.0, 1.0, IloNumVarType.Float, "d" + i + "_" + k + "^" + omega);
                }
            }
        }
        IloNumVar[][][][] f = new IloNumVar[n][n][m][o];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
                        f[i][j][k][omega] = model.numVar(0.0, (double) Q, IloNumVarType.Float, "f" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        IloNumVar[][][][] x = new IloNumVar[n][n][m][o];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
                        x[i][j][k][omega] = model.numVar(0.0, 1.0, IloNumVarType.Bool, "x" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        double deviationCosts = 0.001 / (n * m * o);
        // Set objective
        IloLinearNumExpr expr = model.linearNumExpr();
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                expr.addTerm(0.0, a[i][k]);
                for (int omega = 0; omega < o; omega++) {
                    expr.addTerm(deviationCosts, d[i][k][omega]);
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(0.0, f[i][j][k][omega]);
                        expr.addTerm(p[omega] * c[i][j], x[i][j][k][omega]);
                    }
                }
            }
        }
        IloObjective obj = model.minimize(expr);
        model.add(obj);

        // Add restrictions
        // 2
        for (int i = 1; i < n; i++) {
            expr = model.linearNumExpr();
            for (int k = 0; k < m; k++) {
                expr.addTerm(1.0, a[i][k]);
            }
            model.addEq(expr, 1.0, "c2" + i);
        }
        // 3
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                expr = model.linearNumExpr();
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, x[i][j][k][omega]);
                    }
                }
                model.addEq(expr, 1.0, "c3" + i + "^" + omega);
            }
        }
        // 4
        for (int omega = 0; omega < o; omega++) {
            for (int k = 0; k < m; k++) {
                expr = model.linearNumExpr();
                for (int j = 1; j < n; j++) {
                    expr.addTerm(1.0, x[0][j][k][omega]);
                }
                model.addLe(expr, 1.0, "c4" + k + "^" + omega);
            }
        }
        // 5
        for (int i = 0; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, x[i][j][k][omega]);
                        expr.addTerm(-1.0, x[j][i][k][omega]);
                    }
                    model.addEq(expr, 0.0, "c5" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 6
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(1.0, f[j][i][k][omega]);
                        expr.addTerm(-1.0, f[i][j][k][omega]);
                        expr.addTerm(-(double) customers[i].getDemandPerScenario()[omega], x[i][j][k][omega]);
                    }
                    model.addEq(expr, 0.0, "c6" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 7
        for (int i = 0; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
                        expr = model.linearNumExpr();
                        expr.addTerm(1.0, f[i][j][k][omega]);
                        expr.addTerm((double) (customers[i].getDemandPerScenario()[omega] - Q), x[i][j][k][omega]);
                        model.addLe(expr, 0.0, "c7" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        // 8
        for (int j = 0; j < n; j++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    for (int i = 0; i < n; i++) {
                        expr = model.linearNumExpr();
                        expr.addTerm(1.0, f[i][j][k][omega]);
                        expr.addTerm(-(double) customers[j].getDemandPerScenario()[omega], x[i][j][k][omega]);
                        model.addGe(expr, 0.0, "c8" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        // 9
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    expr = model.linearNumExpr();
                    expr.addTerm(1.0, a[i][k]);
                    for (int j = 0; j < n; j++) {
                        expr.addTerm(-1.0, x[i][j][k][omega]);
                    }
                    model.addLe(expr, d[i][k][omega], "c9" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 10
        for (int omega = 0; omega < o; omega++) {
            for (int k = 0; k < m; k++) {
                expr = model.linearNumExpr();
                for (int i = 1; i < n; i++) {
                    expr.addTerm(1.0, d[i][k][omega]);
                    expr.addTerm(alpha - 1.0, a[i][k]);
                }
                model.addLe(expr, 0.0, "c10" + k + "^" + omega);
            }
        }
        // Valid inequalities
        int sum;
        for (int omega = 0; omega < o; omega++) {
            sum = 0;
            expr = model.linearNumExpr();
            for (int i = 1; i < n; i++) {
                for (int k = 0; k < m; k++) {
                    expr.addTerm(1.0, x[0][i][k][omega]);
                }
                sum += customers[i].getDemandPerScenario()[omega];
            }
            model.addGe(expr, Math.ceil((double) sum / (double) Q), "v1" + omega);
        }
        for (int k = 0; k < m - 1; k++) {
            expr = model.linearNumExpr();
            for (int i = 1; i < n; i++) {
                expr.addTerm(1.0, a[i][k]);
                expr.addTerm(-1.0, a[i][k + 1]);
            }
            model.addGe(expr, 0.0, "v2" + k);
        }
        for (int k = 0; k < m - 1; k++) {
            for (int omega = 0; omega < o; omega++) {
                expr = model.linearNumExpr();
                for (int i = 0; i < n; i++) {
                    expr.addTerm(1.0, x[0][i][k][omega]);
                    expr.addTerm(-1.0, x[0][i][k + 1][omega]);
                }
                model.addGe(expr, 0.0, "v3" + k + "^" + omega);
            }
        }
        // Disallowed variables
        for (int j = 0; j < n; j++) {
            for (int k = 0; k < m; k++) {
                for (int omega = 0; omega < o; omega++) {
                    expr = model.linearNumExpr();
                    expr.addTerm(1.0, x[j][j][k][omega]);
                    model.addEq(expr, 0.0, "d" + j);
                }
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
        double[][] aSol = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                aSol[i][k] = model.getValue(a[i][k]);
            }
        }
        solution.setaSol(aSol);
        double[][][][] xSol = new double[n][n][m][o];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
                        xSol[i][j][k][omega] = model.getValue(x[i][j][k][omega]);
                    }
                }
            }
        }
        solution.setxSol(xSol);

        model.clearModel();

        return solution;
    }
}
