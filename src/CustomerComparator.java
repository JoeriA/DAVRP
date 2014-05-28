import java.util.Comparator;

/**
 * Created by Joeri on 27-5-2014.
 */
public class CustomerComparator implements Comparator<Customer> {
    @Override
    public int compare(Customer c1, Customer c2) {
        if (c1.getR() < c2.getR()) {
            return -1;
        } else if (c1.getR() == c2.getR()) {
            return 0;
        } else {
            return 1;
        }
    }
}
