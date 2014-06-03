/**
 * Created by Joeri on 19-5-2014.
 * Class for savings (for Clarke-Wright heuristic)
 */
public class Saving implements Comparable<Saving> {

    private Customer i, j;
    private double saving;

    /**
     * Create a saving
     *
     * @param saving the saving in objective value that can be made
     * @param i      customer 1 affected by the saving
     * @param j      customer 2 affected by the saving
     */
    public Saving(double saving, Customer i, Customer j) {
        this.i = i;
        this.j = j;
        this.saving = saving;
    }

    /**
     * Get the first customer affected by the saving
     *
     * @return the first customer affected by the saving
     */
    public Customer getI() {
        return i;
    }

    /**
     * Get the first customer affected by the saving
     *
     * @return the second customer affected by the saving
     */
    public Customer getJ() {
        return j;
    }

    /**
     * Get the improvement in objective value when applying this saving
     *
     * @return the improvement in objective value when applying this saving
     */
    public double getSaving() {
        return saving;
    }

    /**
     * Compare two savings with each other on value
     *
     * @param o other saving
     * @return 1 if other saving is bigger, 0 if equal and -1 otherwise
     */
    @Override
    public int compareTo(Saving o) {
        if (o.getSaving() < this.saving) {
            return -1;
        } else if (o.getSaving() == this.saving) {
            return 0;
        } else {
            return 1;
        }
    }
}
