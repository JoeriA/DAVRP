/**
 * Created by Joeri on 6-6-2014.
 * Class to check whether a solution is feasible
 */
public class SolutionChecker {

    public SolutionChecker() {

    }

    public boolean checkRoutes(Solution solution, DataSet dataSet) {

        double epsilon = 0.005;

        if (solution.getRoutes() == null) {
            System.out.println("Routes is null");
            return false;
        }
        if (solution.getAssignments() == null) {
            System.out.println("No assignment");
            return false;
        }

        int[] assignments = solution.getAssignments();
        int weight;
        int assignedCustomers;
        int assignedCustomersMax;
        RouteSet routeSet;
        double totalLength;
        int inEdges, outEdges;
        for (int i = 0; i < dataSet.getNumberOfScenarios(); i++) {
            totalLength = 0;
            routeSet = solution.getRoutes()[i];
            if (routeSet.getRoutes() == null) {
                System.out.println("Route is null");
                return false;
            }
            // Check weights of the route and the assignments
            for (Route route : routeSet.getRoutes()) {
                if (route != null) {
                    weight = 0;
                    assignedCustomers = 0;
                    // Sum all weights and count number of assigned customers in this route
                    for (Customer customer : route.getCustomers()) {
                        if (customer != null && customer.getId() != 0) {
                            weight += customer.getDemand();
                            if (assignments[customer.getId()] == route.getRouteNumber()) {
                                assignedCustomers++;
                            }
                        }
                    }
                    assignedCustomersMax = 0;
                    // Count initial assigned customers
                    for (int j = 1; j < assignments.length; j++) {
                        if (assignments[j] == route.getRouteNumber()) {
                            assignedCustomersMax++;
                        }
                    }
                    if (assignedCustomers < assignedCustomersMax * dataSet.getAlpha()) {
                        System.out.println("Alpha exceeded");
                        return false;
                    }
                    if (weight > dataSet.getVehicleCapacity()) {
                        System.out.println("Vehicle capacity exceeded");
                        return false;
                    }
                    if (weight != route.getWeight()) {
                        System.out.println("Route is feasible, but weight is not correctly calculated");
                    }
                    for (Edge e : route.getEdges()) {
                        totalLength += e.getDistance();
                    }
                }
            }
            if (totalLength > routeSet.getRouteLength() + epsilon && totalLength < routeSet.getRouteLength() - epsilon) {
                System.out.println("Route is feasible, but length is not correctly calculated");
            }
            // Check whether each customer has an ingoing edge and outgoing edge in the same route
            for (Customer customer : routeSet.getCustomers()) {
                inEdges = 0;
                outEdges = 0;
                for (Route route : routeSet.getRoutes()) {
                    if (route != null) {
                        if (route.getEdgeFrom(customer) != null && route.getEdgeTo(customer) != null) {
                            outEdges++;
                            inEdges++;
                        }
                    }
                }
                if (customer.getId() > 0 && inEdges != 1 && outEdges != 1) {
                    System.out.println("Not one ingoing edge and one outgoing edge");
                    return false;
                }
                if (customer.getId() == 0 && inEdges != outEdges) {
                    System.out.println("Ingoing and outgoing edges do not match for depot");
                    return false;
                }
            }
        }

        return true;
    }
}
