import javax.crypto.KeyGenerator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GamingThread implements Runnable {
    private User user;

    public GamingThread(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        while (!user.wantsQuit()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println(e.getLocalizedMessage());
            }
            if (user.getMatches().size() == 1) {
                continue;
            }
            user.setPlaying(true);
            List<Pair> pairs = Pair.getPairs(Match.getAddresses(user.getMatches())).stream().filter(pair -> pair.contains(user.getAddress())).collect(Collectors.toList());
            System.out.println("Checking if there is someone to play with");
            Match currentMatch = null;
            Socket connection = null;

            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = null;
            PrintWriter out = null;

            String userValue = null, responseValue = null;
            String userKey, responseKey;
            String enemyName;

            int userRoll = 0, responseRoll = 0;
            try {
                for (Pair pair : pairs) {
                    for (Match match : user.getMatches()) {
                        if (match.getOpponent().equals(pair.getOpponent(user.getAddress()))) {
                            currentMatch = match;
                        }
                    }
                    if (pair.isDull() || currentMatch.wasPlayed()) {
                        continue;
                    } else if (pair.getLhs().equals(user.getAddress())) {
                        connection = new Socket(currentMatch.getOpponent().getGameInet(), currentMatch.getOpponent().getGamePort());
                        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        out = new PrintWriter(connection.getOutputStream(), true);
                        userKey = Base64.getEncoder().encodeToString(KeyGenerator.getInstance("AES").generateKey().getEncoded());
                        do {
                            System.out.println("To play enter any natural number: ");
                            userValue = stdIn.readLine();
                        } while (!isNumeric(userValue));
                        userRoll = ThreadLocalRandom.current().nextInt(1, 101);
                        out.println(User.encrypt(userValue + " " + userRoll + " " + user.getName(), userKey));
                        responseValue = in.readLine();
                        out.println(userKey);

                    } else if (pair.getRhs().equals(user.getAddress())) {
                        connection = user.getGameServer().accept();
                        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        out = new PrintWriter(connection.getOutputStream(), true);
                        userKey = Base64.getEncoder().encodeToString(KeyGenerator.getInstance("AES").generateKey().getEncoded());
                        do {
                            System.out.println("To play enter any natural number: ");
                            userValue = stdIn.readLine();
                        } while (!isNumeric(userValue));
                        userRoll = ThreadLocalRandom.current().nextInt(1, 101);
                        responseValue = in.readLine();
                        out.println(User.encrypt(userValue + " " + userRoll + " " + user.getName(), userKey));
                        out.println(userKey);
                    }
                    responseKey = in.readLine();
                    responseValue = User.decrypt(responseValue, responseKey);
                    responseRoll = Integer.parseInt(responseValue.split(" ")[1]);
                    enemyName = responseValue.split(" ")[2];
                    responseValue = responseValue.split(" ")[0];

                    int myNum = Integer.parseInt(userValue), hisNum = Integer.parseInt(responseValue);
                    if ((myNum + hisNum) % 2 == 0) {
                        currentMatch.play(userRoll < responseRoll);
                    } else {
                        currentMatch.play(userRoll > responseRoll);
                    }
                    if (userRoll < responseRoll) {
                        System.out.println("Counting starts from your enemy");
                    } else if(userRoll > responseRoll){
                        System.out.println("Counting starts from you");
                    } else {
                        System.out.println("Draw in rolling numbers");
                        continue;
                    }
                    if (currentMatch.isVictory()) {
                        System.out.printf("You won in a game with %s, your number: %d, %s's number: %d%n", enemyName, myNum, enemyName, hisNum);
                    } else {
                        System.out.printf("You lost a game to %s, your number: %d, %s's number: %d%n", enemyName, myNum, enemyName, hisNum);
                    }
                }
                connection.close();
                in.close();
                out.close();
            } catch (Exception e) {

            }
            user.setPlaying(false);
        }
    }

    public static boolean isNumeric(String str) {
        try {
            int d = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
