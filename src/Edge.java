/**
 * Created by Joeri on 19-5-2014.
 */
public class Edge {

    private Customer from, to;
    private double distance;

    public Edge(Customer i, Customer j) {

        this.from = i;
        this.to = j;
        double x2 = Math.pow(from.getxCoordinate() - to.getxCoordinate(), 2.0);
        double y2 = Math.pow(from.getyCoordinate() - to.getyCoordinate(), 2.0);
        this.distance = Math.sqrt(x2 + y2);
    }

    public Customer getFrom() {
        return from;
    }

    public Customer getTo() {
        return to;
    }

    public double getDistance() {
        return distance;
    }
}
