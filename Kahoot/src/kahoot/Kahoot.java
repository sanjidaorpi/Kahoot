/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package kahoot;

import kahoot.game_logic.QuestionSet;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import static kahoot.Kahoot.game_state;
import static kahoot.Kahoot.set;
import kahoot.server_logic.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

// for the server file 
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;


/**
 *
 * @author Sanjida Orpi and Jenesis Blancaflor
 */
public class Kahoot {

    static int game_pin;
    // game states: setup > waiting > play > gameover
    static String game_state = "setup";
    static QuestionSet set = new QuestionSet();

    // UI
    static JFrame frame;
    static CardLayout cardLayout;
    static JPanel mainPanel;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // make game pin for players, random 4 digit number
        Random r = new Random();
        game_pin = r.nextInt(9000) + 1000;

        // if question set could be created, game state can start
        while (set.isCreated() == false) {
            set = new QuestionSet();
        }

        frame = new JFrame("Java Kahoot");
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        WaitingRoom waitingRoom = new WaitingRoom();
        mainPanel.add(waitingRoom, "waiting");

        QAScreen answersScreen = new QAScreen();
        mainPanel.add(answersScreen, "answers");

        frame.add(mainPanel);
        frame.setSize(1280, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // lobby
        game_state = "waiting";
        cardLayout.show(mainPanel, "waiting");

    }

    public static void change_state(String state) {
        game_state = state;
        cardLayout.show(mainPanel, state);
    }

    public static void game_manager() {
        // if entered play state
        // for the number of rounds
        // call QuestionScreen for # of seconds
        // call QAScreen
        // call LBScreen

        game_state = "play";
        int totalRounds = set.rounds;
        int[] roundIndex = {0};

        Timer timer = new Timer();
        TimerTask gameTask = new TimerTask() {
            boolean showingQuestion = true;

            @Override
            public void run() {
                if (roundIndex[0] >= totalRounds) {
                    timer.cancel();
                    game_state = "gameover";
                    return;
                }

                if (showingQuestion) {
                    QuestionScreen qScreen = new QuestionScreen();
                    mainPanel.add(qScreen, "question");
                    change_state("question");
                    showingQuestion = false;

                } else {
                    QAScreen qaScreen = new QAScreen();
                    mainPanel.add(qaScreen, "answers");
                    change_state("answers");
                    showingQuestion = true;
                    set.changeRound();
                    roundIndex[0]++;
                }
            }
        };

        timer.scheduleAtFixedRate(gameTask, 0, 10000);
    }

}

class WaitingRoom extends JPanel {

    public WaitingRoom() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);

        JLabel titleLabel = new JLabel("Java Kahoot!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);

        JButton startButton = new JButton("Start Game");
        startButton.setActionCommand("start");
        startButton.addActionListener(new ButtonListener());
        startButton.setFont(new Font("SansSerif", Font.BOLD, 28));

        add(titleLabel, BorderLayout.NORTH);
        add(startButton, BorderLayout.SOUTH);
    }
}

class QuestionScreen extends JPanel {

    public QuestionScreen() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);
        JLabel q_label = new JLabel(set.getQuestion(), SwingConstants.CENTER);
        q_label.setSize(q_label.getPreferredSize());

        q_label.setFont(new Font("SansSerif", Font.BOLD, 48));
        q_label.setForeground(Color.WHITE);
        add(q_label, BorderLayout.CENTER);
    }
}

class QAScreen extends JPanel {

    public QAScreen() {
        setLayout(new BorderLayout());
        setBackground(new Color(70, 23, 143));

        JSONArray choices = set.getChoices();

        JLabel roundLabel = new JLabel(set.getQuestion(), SwingConstants.CENTER);
        roundLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        roundLabel.setForeground(Color.WHITE);

        JPanel answer_panel = new JPanel();
        answer_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        answer_panel.setLayout(new GridLayout(2, 2, 10, 10));

        Color r = Color.decode("#c60929");
        Color b = Color.decode("#0542b9");
        Color y = Color.decode("#ffc00a");
        Color g = Color.decode("#26890c");

        JLabel choice1 = new JLabel((String) choices.get(0), SwingConstants.CENTER);
        choice1.setFont(new Font("SansSerif", Font.BOLD, 28));
        choice1.setBackground(r);
        choice1.setForeground(Color.white);
        choice1.setOpaque(true);

        JLabel choice2 = new JLabel((String) choices.get(1), SwingConstants.CENTER);
        choice2.setFont(new Font("SansSerif", Font.BOLD, 28));
        choice2.setBackground(b);
        choice2.setForeground(Color.white);
        choice2.setOpaque(true);

        JLabel choice3 = new JLabel((String) choices.get(2), SwingConstants.CENTER);
        choice3.setFont(new Font("SansSerif", Font.BOLD, 28));
        choice3.setForeground(Color.white);
        choice3.setBackground(y);
        choice3.setOpaque(true);

        JLabel choice4 = new JLabel((String) choices.get(3), SwingConstants.CENTER);
        choice4.setFont(new Font("SansSerif", Font.BOLD, 28));
        choice4.setForeground(Color.white);
        choice4.setBackground(g);
        choice4.setOpaque(true);

        answer_panel.add(choice1);
        answer_panel.add(choice2);
        answer_panel.add(choice3);
        answer_panel.add(choice4);

        add(roundLabel, BorderLayout.NORTH);
        add(answer_panel, BorderLayout.CENTER);
    }
}

class ButtonListener implements ActionListener {

    @Override

    public void actionPerformed(ActionEvent e) {

        if ("start".equals(e.getActionCommand())) {
            game_state = "play";
            Kahoot.game_manager();
            
            // let server know button to start is pressed
            try {
                Socket socket = new Socket("localhost", 5190);
                PrintStream out = new PrintStream(socket.getOutputStream());
                out.println("GET /start HTTP/1.1");
                out.println("Host: localhost");
                out.println();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }
}
