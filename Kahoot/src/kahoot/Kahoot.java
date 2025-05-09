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
        
        int rounds = set.rounds;
        Timer q_timer = new Timer();
        Timer a_timer = new Timer();
        Timer l_timer = new Timer();
        Timer go_timer = new Timer();
        
        game_state = "play";
        
        for (int i = 0; i < rounds; i++) {
            
            int start_time = i * 12000;
            q_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    QuestionScreen q_screen = new QuestionScreen();
                    mainPanel.add(q_screen, "question");
                    change_state("question");
                }
            }, start_time);

            a_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    QAScreen qa_screen = new QAScreen();
                    mainPanel.add(qa_screen, "answers");
                    change_state("answers");
                }
            }, start_time + 5000);
        }
        
        int total_time = rounds * 12000;
        go_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GameOver over_screen = new GameOver();
                mainPanel.add(over_screen, "gameover");
                change_state("gameover");
                game_state = "gameover";
            }
        }, total_time);
    }
    
}

class WaitingRoom extends JPanel {

    public WaitingRoom() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color p = Color.decode("#46178f");
        setBackground(p);

        JLabel title = new JLabel("Java Kahoot", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton start = new JButton("Start Game");
        start.setActionCommand("start");
        start.addActionListener(new ButtonListener()); 
        start.setFont(new Font("SansSerif", Font.BOLD, 28));
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setAlignmentY(Component.CENTER_ALIGNMENT);

        add(title);
        add(start); 
    }
    
}

class QuestionScreen extends JPanel {

    public QuestionScreen() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);
        JLabel q_label = new JLabel(set.getQuestion(), SwingConstants.CENTER);
        q_label.setSize(q_label.getPreferredSize());

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


class Leaderboard extends JPanel {
    
    public Leaderboard() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);
        JLabel q_label = new JLabel("scoreboard", SwingConstants.CENTER);
        q_label.setFont(new Font("SansSerif", Font.BOLD, 48));
        q_label.setForeground(Color.WHITE);
        add(q_label, BorderLayout.CENTER);
    }
    
}


class GameOver extends JPanel {
    
    public GameOver() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);
        JLabel q_label = new JLabel("Top Scorers", SwingConstants.CENTER);
        q_label.setFont(new Font("SansSerif", Font.BOLD, 48));
        q_label.setForeground(Color.WHITE);
        add(q_label, BorderLayout.CENTER);
    }
    
}

class ButtonListener implements ActionListener{
    
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
