/**
 * Created by Joeri on 12-5-2014.
 */
public class Solution {

    private double[][] aSol;
    private double[][][] dSol;
    private double[][][][] xSol;
    private double[][] zSol;
    private double objectiveValue, runTime, gap;
    private String name;

    public Solution() {

    }

    public double[][] getaSol() {
        return aSol;
    }

    public void setaSol(double[][] aSol) {
        this.aSol = aSol;
    }

    public double[][][] getdSol() {
        return dSol;
    }

    public void setdSol(double[][][] dSol) {
        this.dSol = dSol;
    }

    public double[][][][] getxSol() {
        return xSol;
    }

    public void setxSol(double[][][][] xSol) {
        this.xSol = xSol;
    }

    public double[][] getzSol() {
        return zSol;
    }

    public void setzSol(double[][] zSol) {
        this.zSol = zSol;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    public double getRunTime() {
        return runTime;
    }

    public void setRunTime(double runTime) {
        this.runTime = runTime;
    }

    public double getGap() {
        return gap;
    }

    public void setGap(double gap) {
        this.gap = gap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
