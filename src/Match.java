import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Match {
    private Address opponent;
    private boolean wasPlayed;
    private boolean isVictory;

    public Match(Address opponent) {
        this.opponent = opponent;
        this.wasPlayed = false;
        this.isVictory = false;
    }

    public Match(Address opponent, boolean wasPlayed, boolean isVictory) {
        this.opponent = opponent;
        this.wasPlayed = wasPlayed;
        this.isVictory = isVictory;
    }

    public void play(boolean isVictory) {
        this.isVictory = isVictory;
        this.wasPlayed = true;
    }

    public static List<Match> parseOpponents(String opponents) {
        //The addresses are sent in the following format:
        //dataAddress gameAddress //(User1)
        //dataAddress gameAddress //(User2)

        List<Match> list = new ArrayList<>();
        String[] arr = opponents.split("&");
        for (String addr : arr) {
            list.add(new Match(new Address(addr.split(" ")[0], addr.split(" ")[1])));
        }
        return list;
    }

    public static String pack(List<Match> matches) {
        StringBuilder packed = new StringBuilder();
        for (Match match : matches) {
            packed.append(match.opponent.getDataAddress() + " " + match.opponent.getGameAddress() + "&");
        }
        return packed.toString();
    }

    public static List<Address> getAddresses(List<Match> list) {
        return list.stream().map(match -> match.opponent).collect(Collectors.toList());
    }

    public Address getOpponent() {
        return opponent;
    }

    public boolean wasPlayed() {
        return wasPlayed;
    }

    public boolean isVictory() {
        return isVictory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(opponent, match.opponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opponent);
    }

    @Override
    public String toString() {
        return String.format("%s wasPlayed:%s isVictory:%s", opponent, wasPlayed, isVictory);
    }
}
