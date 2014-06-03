import java.util.Comparator;

/**
 * Class for comparing two customers with each other
 * Created by Joeri on 27-5-2014.
 */
public class CustomerComparator implements Comparator<Customer> {

    /**
     * Check which customer has a higher r
     *
     * @param c1 customer one
     * @param c2 customer two
     * @return 1 if customer two has a higher r, 0 when equal and -1 otherwise
     */
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
