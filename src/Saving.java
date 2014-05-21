/**
 * Created by Joeri on 19-5-2014.
 */
public class Saving implements Comparable<Saving> {

    private Customer i, j;
    private double saving;

    public Saving(double saving, Customer i, Customer j) {
        this.i = i;
        this.j = j;
        this.saving = saving;
    }

    public Customer getI() {
        return i;
    }

    public void setI(Customer i) {
        this.i = i;
    }

    public Customer getJ() {
        return j;
    }

    public void setJ(Customer j) {
        this.j = j;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

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
