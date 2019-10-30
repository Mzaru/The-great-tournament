import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class User {
    private ServerSocket dataServer, gameServer;
    private Socket dataSocket;
    private List<Match> matches;
    private volatile boolean isPlaying;
    private volatile boolean disconnect;
    private String name;

    private User(ServerSocket dataServer, ServerSocket gameServer, Socket dataSocket, Socket gameSocket, List<Match> matches) {
        this.dataServer = dataServer;
        this.gameServer = gameServer;
        this.dataSocket = dataSocket;
        this.matches = matches;
    }

    public static void main(String[] args) {
        // If the user is not the first one joining then in args[0] will be the "Address" of the one he needs to join
        // And ports on which he creates servers and in args[1] and args[2] accordingly
        // in the following format:
        // dataInet:dataPort&port1 port2
        //P.S. First user always has ports 10000 and 10001
        //For example first user has nothing in args
        //But second user has localhost:10000 10002 10003
        User user = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input;
        try {
            if (args.length == 3) {
                user = User.init(args);
            } else {
                user = User.initFirst();
            }
            user.dataServer.setSoTimeout(5000);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

        new Thread(new GamingThread(user)).start();
        Socket client = null;

        while (!user.disconnect) {
            try {
                if (in.ready()) {
                    input = in.readLine();
                    if (input.equalsIgnoreCase("quit")) {
                        user.disconnect = true;
                    }
                }
                client = user.dataServer.accept();
                new Thread(new DataClientThread(user, client)).start();
            } catch (IOException e) {

            }
        }
        System.out.println("Disconnect pending");
        user.sendQuit();

    }

    private static User init(String[] args) throws IOException {
        InetAddress ip = InetAddress.getByName(args[0].split(":")[0]);
        int port = Integer.parseInt(args[0].split(":")[1]);
        User user = new User(new ServerSocket(Integer.parseInt(args[1])), new ServerSocket(Integer.parseInt(args[2])), new Socket(ip, port), null, new ArrayList<Match>());
        System.out.println("User created");
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String input;
        System.out.println("Please enter a name: ");
        input = stdIn.readLine();
        user.name = input;

        do {
            System.out.println("Enter \"join\" to play the game");
            input = stdIn.readLine();
        } while (!input.equalsIgnoreCase("join"));

        user.requestJoin();
        return user;
    }

    private static User initFirst() throws IOException {
        User user = new User(new ServerSocket(10000), new ServerSocket(10001), null, null, new ArrayList<Match>());
        user.matches.add(new Match(user.getAddress()));
        System.out.println("User created");
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String input;
        System.out.println("Please enter a name: ");
        input = stdIn.readLine();
        user.name = input;

        do {
            System.out.println("Enter \"join\" to play the game");
            input = stdIn.readLine();
        } while (!input.equalsIgnoreCase("join"));
        Socket dataUser = user.dataServer.accept();
        BufferedReader in;
        PrintWriter out;
        String request = null;
        in = new BufferedReader(new InputStreamReader(dataUser.getInputStream()));
        out = new PrintWriter(dataUser.getOutputStream(), true);
        while (true) {
            request = in.readLine();
            if (request.equalsIgnoreCase("join")) {
                System.out.println("Join received");
                break;
            }
        }
        out.println(Match.pack(user.matches));
        System.out.println("Addresses sent");
        //The one who sent join after receiving all the addresses (info about all the players) sends everyone his address in format:
        //new dataInet:dataPort gameInet:gamePort
        Socket notify = user.dataServer.accept();
        in = new BufferedReader(new InputStreamReader(notify.getInputStream()));
        while (true) {
            request = in.readLine();
            if (request.split(" ")[0].equalsIgnoreCase("new")) {
                System.out.println("New received");
                break;
            }
        }
        user.matches.add(new Match(new Address(request.split(" ")[1], request.split(" ")[2])));
        return user;
    }

    public Address getAddress() {
        return new Address(dataServer.getInetAddress(), dataServer.getLocalPort(), gameServer.getInetAddress(), gameServer.getLocalPort());
    }

    public void requestJoin() {
        BufferedReader in = null;
        PrintWriter out = null;
        String response = null;
        try {
            in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            out = new PrintWriter(dataSocket.getOutputStream(), true);
            out.println("join");
            System.out.println("Join sent");
            response = in.readLine();
            this.matches = Match.parseOpponents(response);
            System.out.println("Addresses received");
            Socket notifyAll;
            for (Match match : matches) {
                notifyAll = new Socket(match.getOpponent().getDataInet(), match.getOpponent().getDataPort());
                out = new PrintWriter(notifyAll.getOutputStream(), true);
                out.println("new " + this.getAddress().getDataAddress() + " " + this.getAddress().getGameAddress());
            }
            System.out.println("Notifications sent");
            this.matches.add(new Match(this.getAddress()));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void sendQuit() {
        PrintWriter out = null;
        try {
            Socket notifyAll;
            for (Match match : matches) {
                notifyAll = new Socket(match.getOpponent().getDataInet(), match.getOpponent().getDataPort());
                out = new PrintWriter(notifyAll.getOutputStream(), true);
                out.println("delete " + this.getAddress().getDataAddress() + " " + this.getAddress().getGameAddress());
            }
        } catch (IOException e) {

        }
    }

    public ServerSocket getGameServer() {
        return gameServer;
    }

    public static String encrypt(String message, String key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String message, String key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean wantsQuit() {
        return disconnect;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public String getName() {
        return name;
    }
}