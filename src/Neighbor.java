/**
 * Created by Joeri on 11-6-2014.
 */
public class Neighbor implements Comparable<Neighbor> {

    private int neighbor;
    private int host;
    private double distance;

    public Neighbor(int host, int neighbor, double[][] c) {
        this.host = host;
        this.neighbor = neighbor;
        this.distance = c[host][neighbor];
    }

    public int getNeighbor() {
        return neighbor;
    }

    public int getHost() {
        return host;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Neighbor other) {
        if (other.getDistance() < this.distance) {
            return -1;
        } else if (other.getDistance() == this.distance) {
            return 0;
        } else {
            return 1;
        }
    }

}
