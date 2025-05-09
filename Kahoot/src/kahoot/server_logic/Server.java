/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kahoot.server_logic;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import kahoot.game_logic.QuestionSet;

/**
 *
 * @author sanjidaorpi
 */
public class Server {

    static String gameState = "join"; // "join", "waiting", "play", "end"
    static int game_pin = 1234;

    static ArrayList<ProcessConnection> clients = new ArrayList<>();
    static QuestionSet questionSet = new QuestionSet();
    static int currentPhase = 0;

    // leaderboard state
    // referenced for concurrency control with hashmaps:
    // https://www.baeldung.com/java-synchronizedmap-vs-concurrenthashmap
    static Map<String, Integer> scores = Collections.synchronizedMap(new HashMap<>());
    static List<String> responderOrder = Collections.synchronizedList(new ArrayList<>());
    static boolean acceptingAnswers = false;

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(5190);
            System.out.println("Server running at http://localhost:5190");

            while (true) {
                Socket s = ss.accept();
                Scanner sin = new Scanner(s.getInputStream());
                PrintStream sout = new PrintStream(s.getOutputStream());

                if (!sin.hasNextLine()) {
                    s.close();
                    continue;
                }

                String line = sin.nextLine();

                // join the game screen 
                if (line.startsWith("GET / ") || line.startsWith("GET /HTTP")) {
                    sendHTML(sout, """
                        <h2>Join the Game</h2>
                        <form action="/join" method="get">
                          Username: <input type="text" name="username"><br>
                          Game PIN: <input type="text" name="pin"><br>
                          <input type="submit" value="Join">
                        </form>
                    """, false);
                    s.close();
                    continue;
                }

                // player joined 
                if (line.startsWith("GET") && line.contains("/join")) {
                    String username = extractUserInfo(line, "username");
                    String pin = extractUserInfo(line, "pin");

                    // game pin is wrong
                    if (!pin.equals(Integer.toString(game_pin))) {
                        sendHTML(sout, "<h3>Invalid Game PIN</h3>", false);
                    } else {
                        sout.print("HTTP/1.1 302 Found\r\n");
                        sout.print("Location: /play?username=" + username + "\r\n");
                        sout.print("\r\n");

                        System.out.println(username + " joined.");
                        // initialize score
                        scores.put(username, 0);

                        // connect and add user as a new thread
                        ProcessConnection conn = new ProcessConnection(username);
                        clients.add(conn);
                        conn.start();

                        // update state for player
                        if (gameState.equals("join")) {
                            gameState = "waiting";
                        }
                    }

                    s.close();
                    continue;
                }

                // start the game
                if (line.startsWith("GET") && line.contains("/start")) {
                    if (gameState.equals("waiting")) {
                        gameState = "play"; // update state
                        new Thread(Server::runGameCycle).start(); // start game cycle
                        System.out.println("Game started by Kahoot.java");
                    }
                    sendHTML(sout, "<h3>Game is starting...</h3>", false);
                    s.close();
                    continue;
                }

                // answer submission (does not interrupt timer)
                if (line.startsWith("GET") && line.contains("/answer")) {
                    String username = extractUserInfo(line, "username");
                    String choiceStr = extractUserInfo(line, "choice");
                    try {
                        int choice = Integer.parseInt(choiceStr);
                        int correct = (int) questionSet.getAnswer();
                        if (acceptingAnswers && choice == correct) {

                            // synchronized function for responseOrder to ensure concurrecy control
                            synchronized (responderOrder) {
                                if (!responderOrder.contains(username)) {
                                    responderOrder.add(username);
                                    int place = responderOrder.size();
                                    int pts = (place == 1) ? 5
                                            : (place == 2) ? 3
                                                    : (place == 3) ? 1
                                                            : 0;
                                    scores.put(username, scores.get(username) + pts);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    // 204 to avoid page reload
                    sout.print("HTTP/1.1 204 No Content\r\n\r\n");
                    s.close();
                    continue;
                }

                // runGameCycle is playing the game and outputting choices
                if (line.startsWith("GET") && line.contains("/play")) {
                    String username = extractUserInfo(line, "username");

                    if (gameState.equals("waiting")) {
                        sendHTML(sout, "<h3>Waiting for other players to join...</h3>", true);

                    } else if (gameState.equals("play")) {
                        if (!questionSet.isCreated()) { // check to see if file is loaded
                            sendHTML(sout, "<h3>No question file loaded.</h3>", false);

                        } else if (currentPhase == 1) {
                            JSONArray choices = questionSet.getChoices();

                            String html = """                                  
                                <iframe name="hiddenFrame" style="display:none;"></iframe>
                                <style>
                                    .choice-button {
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                        height: 300px;
                                        width: 100%;
                                        font-size: 32px;
                                        font-family: sans-serif;
                                        font-weight: bold;
                                        color: white;
                                    }
                                    .grid-container {
                                        display: grid;
                                        justify-content: center;
                                        grid-template-columns: 1fr 1fr;
                                        grid-gap: 10px;
                                        padding: 10px;
                                    }
                                </style>
                                <div class="grid-container">
                            """;

                            String[] colors = {"#c60929", "#0542b9", "#ffc00a", "#26890c"};
                            for (int i = 0; i < choices.size(); i++) {
                                String color = colors[i % colors.length];
                                String text = choices.get(i).toString();
                                html += "<form action='/answer' method='get' target='hiddenFrame'>"
                                        + "<input type='hidden' name='username' value='" + username + "'>"
                                        + "<input type='hidden' name='choice' value='" + i + "'>"
                                        + "<button class='choice-button' style='background-color:" + color + ";'>"
                                        + text + "</button></form>";
                            }
                            html += "</div>";
                            sendHTML(sout, html, true);

                        } else {
                            sendHTML(sout, "<h3>Get ready...</h3>", true);
                        }

                    } else if (gameState.equals("end")) {
                        // show sorted leaderboard
                        String lb = "<h2>Game Over!</h2><h3>Leaderboard:</h3><ul>";
                        List<Map.Entry<String, Integer>> list = new ArrayList<>(scores.entrySet());
                        // sort the list of scores 
                        list.sort((a, b) -> b.getValue() - a.getValue());
                        // output the points
                        for (Map.Entry<String, Integer> e : list) {
                            // name: points format for scoreboard
                            lb += "<ul>" + e.getKey() + ": " + e.getValue() + " pts</ul>";
                        }
                        lb += "</ul>";
                        sendHTML(sout, lb, false);
                    }
                    s.close();
                }

                // get the scores 
                if (line.startsWith("GET") && line.contains("/scores")) {
                    String lb = "<h2>Leaderboard:</h2><ul>";
                    List<Map.Entry<String, Integer>> list = new ArrayList<>(scores.entrySet());
                    list.sort((a, b) -> b.getValue() - a.getValue());
                    for (Map.Entry<String, Integer> e : list) {
                        lb += "<li>" + e.getKey() + ": " + e.getValue() + " pts</li>";
                    }
                    lb += "</ul>";
                    sendHTML(sout, lb, false);
                    s.close();
                    continue;
                }

            }
        } catch (IOException ex) {
        }
    }

    // runs through the game cycle of waiting then showing the choices
    static void runGameCycle() {
        try {
            int total = questionSet.rounds;
            for (int i = 0; i < total; i++) {
                currentPhase = 0;
                acceptingAnswers = false;
                responderOrder.clear();
                Thread.sleep(5000);

                currentPhase = 1;
                acceptingAnswers = true;
                Thread.sleep(7000);

                questionSet.changeRound();
            }
            gameState = "end";
            System.out.println("Game finished.");
        } catch (InterruptedException ignored) {
        }
    }

    // outputs any HTML to screen and updates screen
    // refresh bool to indicate if screen needs to be refreshed to update the visuals
    static void sendHTML(PrintStream sout, String bodyContent, boolean refresh) {
        String html = """
    <html>
    <head>
        <style>
            body {
                background-color: #46178f;
                color: white;
                font-family: 'SansSerif', sans-serif;
                font-weight: bold;
                text-align: center;
                padding: 50px;
                margin: 0;
            }
            h2, h3 {
                font-size: 36px;
            }
            form input[type="text"] {
                padding: 10px;
                font-size: 18px;
                width: 250px;
                margin: 10px 0;
            }
            form input[type="submit"] {
                padding: 12px 24px;
                font-size: 20px;
                background-color: #ffc00a;
                color: #000;
                font-weight: bold;
                border: none;
                cursor: pointer;
                margin-top: 20px;
            }
            .choice-button {
                display: flex;
                align-items: center;
                justify-content: center;
                height: 150px;
                font-size: 32px;
                font-family: sans-serif;
                font-weight: bold;
                color: white;
            }
            .grid-container {
                display: grid;
                grid-template-columns: 1fr 1fr;
                grid-gap: 10px;
                padding: 10px;
            }
                      
            ul{ 
               font-size: 30px;
               } 
        </style>"""
                + (refresh ? "<meta http-equiv=\"refresh\" content=\"2\">" : "")
                + """
    </head>
    <body>
    """ + bodyContent + """
    </body>
    </html>
    """;

        sout.print("HTTP/1.1 200 OK\r\n");
        sout.print("Content-Type: text/html\r\n");
        sout.print("Content-Length: " + html.length() + "\r\n");
        sout.print("\r\n");
        sout.print(html);
    }

    // extract the username and the gamepin from the user input
    static String extractUserInfo(String line, String key) {
        int start = line.indexOf(key + "=");
        if (start == -1) {
            return "";
        }
        int end = line.indexOf("&", start);
        if (end == -1) {
            end = line.indexOf(" ", start);
        }
        return line.substring(start + key.length() + 1, end);
    }
}

// make a new thread for every user joining 
class ProcessConnection extends Thread {

    String username;

    ProcessConnection(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        System.out.println("Thread started for user: " + username);
    }
}
