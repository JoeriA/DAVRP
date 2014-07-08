/**
 * Created by Joeri on 12-5-2014.
 * Class for storing the solution of a solver
 */
public class Solution {

    private double[][] aSol;
    private double[][][][] xSol;
    private double[][] zSol;

    public double[][][] getzSolSkip() {
        return zSolSkip;
    }

    public void setzSolSkip(double[][][] zSolSkip) {
        this.zSolSkip = zSolSkip;
    }

    private double[][][] zSolSkip;
    private double objectiveValue, runTime, gap;
    private String name;
    private RouteSet[] routes;
    private int[] assignments;

    /**
     * Create an empty solution
     */
    public Solution() {

    }

    /**
     * Get assignments
     *
     * @return array with assignments
     */
    public int[] getAssignments() {
        return assignments;
    }

    /**
     * Set array with assignments
     *
     * @param assignments array with assignments
     */
    public void setAssignments(int[] assignments) {
        this.assignments = assignments;
    }

    /**
     * Get all routes in this solution
     *
     * @return all routes in this solution
     */
    public RouteSet[] getRoutes() {
        return routes;
    }

    /**
     * Set all routes in this solution
     *
     * @param routes all routes in this solution
     */
    public void setRoutes(RouteSet[] routes) {
        this.routes = routes;
    }

    /**
     * Get the solution of assignment problem in matrix notation
     *
     * @return the solution of assignment problem in matrix notation
     */
    public double[][] getaSol() {
        return aSol;
    }

    /**
     * Set the solution of assignment problem in matrix notation
     *
     * @param aSol the solution of assignment problem in matrix notation
     */
    public void setaSol(double[][] aSol) {
        this.aSol = aSol;
    }

    /**
     * Get the solution of the DAVRP in matrix notation
     *
     * @return the solution of the DAVRP in matrix notation
     */
    public double[][][][] getxSol() {
        return xSol;
    }

    /**
     * Set the solution of the DAVRP in matrix notation
     *
     * @param xSol the solution of the DAVRP in matrix notation
     */
    public void setxSol(double[][][][] xSol) {
        this.xSol = xSol;
    }

    /**
     * Set the solution of the assignment problem of the heuristic in matrix notation
     *
     * @return the solution of the assignment problem of the heuristic in matrix notation
     */
    public double[][] getzSol() {
        return zSol;
    }

    /**
     * Set the solution of the assignment problem of the heuristic in matrix notation
     *
     * @param zSol the solution of the assignment problem of the heuristic in matrix notation
     */
    public void setzSol(double[][] zSol) {
        this.zSol = zSol;
    }

    /**
     * Get the objective value of the solution
     *
     * @return the objective value of the solution
     */
    public double getObjectiveValue() {
        return objectiveValue;
    }

    /**
     * Set the objective value of the solution
     *
     * @param objectiveValue the objective value of the solution
     */
    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    /**
     * Get the running time of the solver
     *
     * @return the running time of the solver
     */
    public double getRunTime() {
        return runTime;
    }

    /**
     * Set the running time of the solver
     *
     * @param runTime the running time of the solver
     */
    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

    /**
     * Get the gap between the lower bound and the solution
     *
     * @return the gap between the lower bound and the solution (note: heuristics always return 0)
     */
    public double getGap() {
        return gap;
    }

    /**
     * Set the gap between the lower bound and the solution
     *
     * @param gap the gap between the lower bound and the solution
     */
    public void setGap(double gap) {
        this.gap = gap;
    }

    /**
     * Get the name of the solver that has produced this solution
     *
     * @return the name of the solver that has produced this solution
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the solver that has produced this solution
     *
     * @param name the name of the solver that has produced this solution
     */
    public void setName(String name) {
        this.name = name;
    }
}
