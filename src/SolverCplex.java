/**
 * Created by Joeri on 25-4-2014.
 */

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class SolverCplex {

    public SolverCplex() {

    }

    public void solve(DataSet dataSet) throws IloException {
        // Get some data from dataset
        int n = dataSet.getNumberOfCustomers();
        int m = dataSet.getNumberOfVehicles();
        int o = dataSet.getNumberOfScenarios();
        int Q = dataSet.getVehicleCapacity();
        Customer[] customers = dataSet.getCustomers();
        double[] p = dataSet.getScenarioProbabilities();
        double[][] c = dataSet.getTravelCosts();
        Customer customer;
        int[] demands;
        int demand;
        double alpha = dataSet.getAlpha();
        // Create environment
        IloCplex cplex = new IloCplex();
        //GRBEnv env = new GRBEnv("mip.log");
        // Create model
        //GRBModel model = new GRBModel(env);
        // Create variables
//        GRBVar[][] a = new GRBVar[n][m];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
//                a[i][k] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "a" + i + "_" + k);
            }
        }
//        GRBVar[][][] d = new GRBVar[n][m][o];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                for (int omega = 0; omega < o; omega++) {
//                    d[i][k][omega] = model.addVar(0.0, (double) Q, 0.0, GRB.CONTINUOUS, "d" + i + "_" + k + "^" + omega);
                }
            }
        }
//        GRBVar[][][][] f = new GRBVar[n][n][m][o];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
//                        f[i][j][k][omega] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "f" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
//        GRBVar[][][][] x = new GRBVar[n][n][m][o];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
//                        x[i][j][k][omega] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        // Integrate new variables
//        model.update();
        // Set objective
//        GRBLinExpr expr = new GRBLinExpr();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < m; k++) {
                    for (int omega = 0; omega < o; omega++) {
//                        expr.addTerm(p[omega] * c[i][j], x[i][j][k][omega]);
                    }
                }
            }
        }
//        model.setObjective(expr, GRB.MINIMIZE);
        // Add restrictions
        // 2
        for (int i = 1; i < n; i++) {
//            expr = new GRBLinExpr();
            for (int k = 0; k < m; k++) {
//                expr.addTerm(1.0, a[i][k]);
            }
//            model.addConstr(expr, GRB.EQUAL, 1.0, "c2" + i);
        }
        // 3
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
//                expr = new GRBLinExpr();
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
//                        expr.addTerm(1.0, x[i][j][k][omega]);
                    }
                }
//                model.addConstr(expr, GRB.EQUAL, 1.0, "c3" + i + "^" + omega);
            }
        }
        // 4
        for (int omega = 0; omega < o; omega++) {
            for (int k = 0; k < m; k++) {
//                expr = new GRBLinExpr();
                for (int j = 1; j < n; j++) {
//                    expr.addTerm(1.0, x[0][j][k][omega]);
                }
//                model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "c4" + k + "^" + omega);
            }
        }
        // 5
        for (int i = 0; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
//                    expr = new GRBLinExpr();
                    for (int j = 0; j < n; j++) {
//                        expr.addTerm(1.0, x[i][j][k][omega]);
//                        expr.addTerm(-1.0, x[j][i][k][omega]);
                    }
//                    model.addConstr(expr, GRB.EQUAL, 0.0, "c5" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 6
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
//                    expr = new GRBLinExpr();
                    for (int j = 0; j < n; j++) {
//                        expr.addTerm(1.0, f[j][i][k][omega]);
//                        expr.addTerm(-1.0, f[i][j][k][omega]);
//                        expr.addTerm(-(double) customers[i].getDemandPerScenario()[omega], x[i][j][k][omega]);
                    }
//                    model.addConstr(expr, GRB.EQUAL, 0.0, "c6" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 7
        for (int i = 0; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    for (int j = 0; j < n; j++) {
//                        expr = new GRBLinExpr();
//                        expr.addTerm(1.0, f[i][j][k][omega]);
//                        expr.addTerm((double) (customers[i].getDemandPerScenario()[omega]-Q), x[i][j][k][omega]);
//                        model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "c7" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        // 8
        for (int j = 0; j < n; j++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
                    for (int i = 0; i < n; i++) {
//                        expr = new GRBLinExpr();
//                        expr.addTerm(1.0, f[i][j][k][omega]);
//                        expr.addTerm(-(double) customers[j].getDemandPerScenario()[omega], x[i][j][k][omega]);
//                        model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, "c8" + i + "_" + j + "_" + k + "^" + omega);
                    }
                }
            }
        }
        // 9
        for (int i = 1; i < n; i++) {
            for (int omega = 0; omega < o; omega++) {
                for (int k = 0; k < m; k++) {
//                    expr = new GRBLinExpr();
//                    expr.addTerm(1.0, a[i][k]);
                    for (int j = 0; j < n; j++) {
//                        expr.addTerm(-1.0, x[i][j][k][omega]);
                    }
//                    model.addConstr(expr, GRB.LESS_EQUAL, d[i][k][omega], "c9" + i + "_" + k + "^" + omega);
                }
            }
        }
        // 10
        for (int omega = 0; omega < o; omega++) {
            for (int k = 0; k < m; k++) {
//                expr = new GRBLinExpr();
                for (int i = 1; i < n; i++) {
//                    expr.addTerm(1.0, d[i][k][omega]);
//                    expr.addTerm(alpha - 1.0, a[i][k]);
                }
//                model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "c10" + k + "^" + omega);
            }
        }
//        model.write("DAVRP.lp");
        // Optimize model
//        model.optimize();
//        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
//        model.dispose();
//        env.dispose();
    }
}
