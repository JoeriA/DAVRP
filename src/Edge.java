/**
 * Created by Joeri on 19-5-2014.
 * <p/>
 * Class for an edge
 */
public class Edge {

    private Customer from, to;
    private double distance;

    /**
     * Create an edge between two customers
     *
     * @param i start customer of edge
     * @param j end customer of edge
     */
    public Edge(Customer i, Customer j) {

        this.from = i;
        this.to = j;

        // Calculate distance from coordinates in customers
        double x2 = Math.pow(from.getxCoordinate() - to.getxCoordinate(), 2.0);
        double y2 = Math.pow(from.getyCoordinate() - to.getyCoordinate(), 2.0);
        this.distance = Math.sqrt(x2 + y2);
    }

    /**
     * Create an edge between two customers
     *
     * @param i        start customer of edge
     * @param j        end customer of edge
     * @param distance length of the edge
     */
    public Edge(Customer i, Customer j, double distance) {

        this.from = i;
        this.to = j;
        this.distance = distance;
    }

    /**
     * Get start customer of edge
     *
     * @return start customer of edge
     */
    public Customer getFrom() {
        return from;
    }

    /**
     * Get end customer of edge
     *
     * @return end customer of edge
     */
    public Customer getTo() {
        return to;
    }

    /**
     * Get length of the edge
     *
     * @return length of the edge
     */
    public double getDistance() {
        return distance;
    }
}
