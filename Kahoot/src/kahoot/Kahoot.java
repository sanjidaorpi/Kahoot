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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;


/**
 *
 * @author Sanjida Orpi and Jenesis Blancaflor
 */
public class Kahoot {

    static int game_pin;
    // game states: setup > waiting > play > gameover
    static String game_state = "setup";
    static QuestionSet set = new QuestionSet();
    
    // start the server
    static Server server = new Server();

    // UI
    static JFrame frame;
    static CardLayout cardLayout;
    static JPanel mainPanel;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException {

        // keep trying to create question set
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

        // start at lobby
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
        Timer end_timer = new Timer();
        
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
                    set.changeRound();
                }
            }, start_time + 5000);
            
        }
        
        int total_time = rounds * 12000;
        end_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String lb = server.getLeaderboard();
                System.out.println(lb);
                //server = null;
                
                
                GameOver over_screen = new GameOver();
                mainPanel.add(over_screen, "gameover");
                change_state("gameover");
                game_state = "gameover";
            }
        }, total_time);
    }
    
}

class WaitingRoom extends JPanel {

    public WaitingRoom() throws UnknownHostException {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Color p = Color.decode("#46178f");
        setBackground(p);
        
        JPanel top_panel = new JPanel();
        
        top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.PAGE_AXIS));
        top_panel.setPreferredSize(new Dimension(500, 10));
        top_panel.setOpaque(false);
        
        InetAddress localHost = InetAddress.getLocalHost();
        JLabel instruction = new JLabel("Please open your browser and go to: " + localHost.getHostAddress() + ":5190/", SwingConstants.CENTER);
        instruction.setFont(new Font("SansSerif", Font.BOLD, 25));
        instruction.setForeground(Color.WHITE);
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        Border inst_padding = BorderFactory.createEmptyBorder(50, 0, 0, 0);
        instruction.setBorder(inst_padding);
        
        JLabel game_pin = new JLabel("Game PIN: 1234", SwingConstants.CENTER);
        game_pin.setFont(new Font("SansSerif", Font.BOLD, 25));
        game_pin.setForeground(Color.WHITE);
        game_pin.setAlignmentX(Component.CENTER_ALIGNMENT);
        Border pin_padding = BorderFactory.createEmptyBorder(20, 0, 0, 0);
        game_pin.setBorder(pin_padding);
        
        top_panel.add(instruction);
        top_panel.add(game_pin);
        
        JLabel title = new JLabel("Kahoot!", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 70));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        

        JPanel middle_panel = new JPanel();
        middle_panel.setLayout(new BoxLayout(middle_panel, BoxLayout.Y_AXIS));
        middle_panel.setOpaque(false);
        
        JLabel message = new JLabel("Waiting for players...", SwingConstants.CENTER);
        //message.setOpaque(true);
        //message.setBackground(Color.decode("#25076b"));
        message.setFont(new Font("SansSerif", Font.BOLD, 25));
        message.setForeground(Color.LIGHT_GRAY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        Border border = message.getBorder();
        Border margin = new EmptyBorder(10,10,10,10);
        message.setBorder(new CompoundBorder(border, margin));
        Border mess_padding = BorderFactory.createEmptyBorder(50, 0, 50, 0);
        message.setBorder(mess_padding);
        
        JButton start = new JButton("Start");
        start.setSize(new Dimension(200, 200));
        middle_panel.setPreferredSize(new Dimension(500, 100));
        start.setActionCommand("start");
        start.addActionListener(new ButtonListener()); 
        start.setFont(new Font("SansSerif", Font.BOLD, 40));
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        middle_panel.add(message);
        middle_panel.add(start);
        
        top_panel.add( Box.createVerticalGlue() );
        middle_panel.add( Box.createVerticalGlue() );
        message.add( Box.createVerticalGlue() );
        
        add(title);
        add(top_panel);
        add(middle_panel);
        
        Border padding = BorderFactory.createEmptyBorder(80, 0, 150, 0);
        setBorder(padding);
    }
    
}

class QuestionScreen extends JPanel {

    public QuestionScreen() {
        setLayout(new BorderLayout());
        Color p = Color.decode("#46178f");
        setBackground(p);
        JLabel q_label = new JLabel("<html><center>"+ set.getQuestion() +"<center></html>", SwingConstants.CENTER);
        
        Border border = q_label.getBorder();
        Border margin = new EmptyBorder(0,100,0,100);
        q_label.setBorder(new CompoundBorder(border, margin));
        
        q_label.setFont(new Font("SansSerif", Font.BOLD, 35));
        q_label.setForeground(Color.WHITE);
        add(q_label, BorderLayout.CENTER);
    }
    
}

class QAScreen extends JPanel {

    public QAScreen() {
        setLayout(new BorderLayout());
        setBackground(new Color(70, 23, 143));
        
        JSONArray choices = set.getChoices();

        JLabel q_label = new JLabel("<html>"+ set.getQuestion() +"</html>", SwingConstants.CENTER);
        Border border = q_label.getBorder();
        Border margin = new EmptyBorder(50,10,50,10);
        q_label.setBorder(new CompoundBorder(border, margin));
        q_label.setFont(new Font("SansSerif", Font.BOLD, 28));
        q_label.setForeground(Color.WHITE);

        JPanel answer_panel = new JPanel();
        answer_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        answer_panel.setLayout(new GridLayout(2, 2, 10, 10));

        Color r = Color.decode("#c60929");
        Color b = Color.decode("#0542b9");
        Color y = Color.decode("#ffc00a");
        Color g = Color.decode("#26890c");

        JLabel choice1 = new JLabel("<html>"+(String) choices.get(0)+"</html>", SwingConstants.CENTER);
        choice1.setFont(new Font("SansSerif", Font.BOLD, 35));
        choice1.setBackground(r);
        choice1.setForeground(Color.white);
        choice1.setOpaque(true);

        JLabel choice2 = new JLabel("<html>"+(String) choices.get(1)+"</html>", SwingConstants.CENTER);
        choice2.setFont(new Font("SansSerif", Font.BOLD, 35));
        choice2.setBackground(b);
        choice2.setForeground(Color.white);
        choice2.setOpaque(true);

        JLabel choice3 = new JLabel("<html>"+(String) choices.get(2)+"</html>", SwingConstants.CENTER);
        choice3.setFont(new Font("SansSerif", Font.BOLD, 35));
        choice3.setForeground(Color.white);
        choice3.setBackground(y);
        choice3.setOpaque(true);

        JLabel choice4 = new JLabel("<html>"+(String) choices.get(3)+"</html>", SwingConstants.CENTER);
        choice4.setFont(new Font("SansSerif", Font.BOLD, 35));
        choice4.setForeground(Color.white);
        choice4.setBackground(g);
        choice4.setOpaque(true);

        answer_panel.add(choice1);
        answer_panel.add(choice2);
        answer_panel.add(choice3);
        answer_panel.add(choice4);

        add(q_label, BorderLayout.NORTH);
        add(answer_panel, BorderLayout.CENTER);
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
