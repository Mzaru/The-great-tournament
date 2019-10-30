import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class DataClientThread implements Runnable {
    private User user;
    private Socket dataSocket;

    public DataClientThread(User user, Socket dataSocket) {
        this.user = user;
        this.dataSocket = dataSocket;
    }

    @Override
    public void run() {
        while (user.isPlaying()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
        BufferedReader in;
        PrintWriter out;
        String request = null;
        try {
            in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            out = new PrintWriter(dataSocket.getOutputStream(), true);
            request = in.readLine();
            if (request.equalsIgnoreCase("join")) {
                out.println(Match.pack(user.getMatches()));
                System.out.println("Join served");
            } else if (request.split(" ")[0].equalsIgnoreCase("new")) {
                user.getMatches().add(new Match(new Address(request.split(" ")[1], request.split(" ")[2])));
                System.out.println("New served");
            } else if (request.split(" ")[0].equalsIgnoreCase("delete")) {
                Match match = new Match(new Address(request.split(" ")[1], request.split(" ")[2]));
                user.getMatches().remove(match);
                System.out.println("Delete served");
            }
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
