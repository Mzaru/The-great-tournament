import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class Address implements Comparable<Address> {
    private String dataAddress, gameAddress;

    public Address(String dataAddress, String gameAddress) {
        this.dataAddress = dataAddress;
        this.gameAddress = gameAddress;
    }

    public Address(InetAddress dataIp, int dataPort, InetAddress gameIp, int gamePort) {
        dataAddress = String.format("%s:%s", dataIp.toString().split("/")[1], String.valueOf(dataPort));
        gameAddress = String.format("%s:%s", gameIp.toString().split("/")[1], String.valueOf(gamePort));
    }

    public InetAddress getDataInet() throws UnknownHostException {
        return InetAddress.getByName(dataAddress.split(":")[0]);
    }

    public int getDataPort() {
        return Integer.parseInt(dataAddress.split(":")[1]);
    }

    public InetAddress getGameInet() throws UnknownHostException {
        return InetAddress.getByName(gameAddress.split(":")[0]);
    }

    public int getGamePort() {
        return Integer.parseInt(gameAddress.split(":")[1]);
    }

    public String getDataAddress() {
        return dataAddress;
    }

    public String getGameAddress() {
        return gameAddress;
    }

    public boolean isDull() { //This is used for recognizing a dull
        if (dataAddress == null && gameAddress == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return dataAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(dataAddress, address.dataAddress) &&
                Objects.equals(gameAddress, address.gameAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataAddress, gameAddress);
    }

    @Override
    public int compareTo(Address o) {
        return this.dataAddress.compareTo(o.dataAddress);
    }
}
