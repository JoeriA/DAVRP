/**
 * Created by Joeri on 19-5-2014.
 * Implementation Clarke Wright heuristic
 */
public class RecordToRecordLowerBound implements Solver {

    /**
     * Implementation of record-to-record heuristic for the DAVRP to calculate lower bound
     */
    public RecordToRecordLowerBound() {

    }

    /**
     * Solve VRP for this dataset
     *
     * @param dataSet dataset to be solved
     */
    public Solution solve(DataSet dataSet) {

        Solution solution = new Solution();
        solution.setName("RTR_LB");
        RouteSet[] routes = new RouteSet[dataSet.getNumberOfScenarios()];
        RecordToRecord rtr = new RecordToRecord();
        Solution temp;
        double costs = 0.0;
        Long start = System.currentTimeMillis();
        for (int i = 0; i < dataSet.getNumberOfScenarios(); i++) {
            temp = rtr.solve(dataSet, i + 1);
            routes[i] = temp.getRoutes()[i];
            costs += dataSet.getScenarioProbabilities()[i] * temp.getObjectiveValue();
        }
        Long end = System.currentTimeMillis();
        solution.setRoutes(routes);
        solution.setObjectiveValue(costs);
        solution.setRunTime((end - start) / 1000.0);
        return solution;
    }
}