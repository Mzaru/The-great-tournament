import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Pair {
    private Address lhs, rhs;

    public Pair(Address lhs, Address rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static List<Pair> getPairs(List<Address> list) {
        List<Pair> pairs = new ArrayList<>();
        if (list.size() % 2 == 1) {
            list.add(new Address(null, null)); //Adding a dull if there is odd number of players
        } else if (list.size() == 2) {
            pairs.add(new Pair(list.get(0), list.get(1)));
            return pairs;
        }
        LinkedList<Address> queue = new LinkedList<>();
        for (Address address : list) {
            queue.add(address);
        }
        Address[][] addresses = new Address[2][list.size() / 2];
        for (int i = 0; i < addresses.length; i++) {
            for (int j = 0; j < addresses[i].length; j++) {
                addresses[i][j] = queue.pop();
            }
        }
        for (int i = 0; i < list.size() - 1; ++i) {
            for (int j = 0; j < addresses[0].length; ++j) {
                pairs.add(new Pair(addresses[0][j], addresses[1][j]));
            }
            rotate(addresses);
        }
        return pairs;
    }

    public static <T> void rotate(T[][] elements) { // Helper method for Round Robin algorithm
        T last = elements[0][elements[0].length - 1];
        T first = elements[1][0];
        for (int i = elements[0].length - 1; i >= 2; --i) {
            elements[0][i] = elements[0][i - 1];
        }
        elements[0][1] = first;
        for (int i = 0; i < elements[1].length - 1; ++i) {
            elements[1][i] = elements[1][i + 1];
        }
        elements[1][elements[1].length - 1] = last;
    }

    public Address getLhs() {
        return lhs;
    }

    public Address getRhs() {
        return rhs;
    }

    public boolean contains(Address address) {
        return lhs.equals(address) || rhs.equals(address);
    }

    public boolean isDull() {
        return lhs.isDull() || rhs.isDull();
    }

    public Address getOpponent(Address address) {
        if (lhs.equals(address)) {
            return rhs;
        } else {
            return lhs;
        }
    }

    @Override
    public String toString() {
        return String.format("Pair{%s, %s}", lhs.toString(), rhs.toString());
    }
}
