/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package kahoot;

import kahoot.game_logic.QuestionSet;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Random;
import static kahoot.Kahoot.game_state;
import kahoot.server_logic.Server;

/**
 *
 * @author Sanjida Orpi and Jenesis Blancaflor
 */
public class Kahoot {
    
    static int game_pin;
            
    // game states: setup > waiting > play > gameover
    static String game_state = "setup";
    
    // UI
    static JFrame jf;
    static  JPanel panel1;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // make game pin for players, random 4 digit number
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            game_pin = r.nextInt(9000) + 1000;
        }
        
        // if question set could be created, game state can start
        QuestionSet set = new QuestionSet();
        while (set.isCreated() == false) {
            set = new QuestionSet();
        }
        
        // set up waiting room page UI and start server
        game_state = "waiting";
        Server server = new Server(game_pin); // this should take in game_pin
        
        
        if ( game_state.equals("waiting")) {
            WaitingRoom page1 = new WaitingRoom();
            page1.setVisible(true);
            
        } else if (game_state.equals("play")) {
            
            
            
        }
        
        
        
    }
    
}

class WaitingRoom extends JFrame {
    
    private JFrame JFrame;
    private JPanel jp;
    private JButton start;
    private JLabel title_label;
    private JLabel pin_label;
    private JLabel state_label;
    
    public WaitingRoom() {
        setTitle("Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLayout(new BorderLayout());
        
        Font h_font = new Font("SansSerif", Font.BOLD, 48);
        Font p_font = new Font("SansSerif", Font.PLAIN, 28);
        Font btn_font = new Font("SansSerif", Font.BOLD, 28);
        
        // title
        title_label = new JLabel("Java Kahoot!");
        title_label.setForeground(Color.WHITE); 
        
        // pin
        pin_label = new JLabel("Game PIN: " + Kahoot.game_pin);
        pin_label.setForeground(Color.WHITE);
        
        // waiting for players
        state_label = new JLabel("Waiting for participants");
        state_label.setForeground(Color.WHITE);   
        
        start = new JButton("Start");
        //start.setBackground(Color.gray.brighter());
        start.setForeground(Color.BLACK);
        
        start.setFont(h_font);
        title_label.setFont(h_font);
        pin_label.setFont(p_font);
        state_label.setFont(p_font);
        start.setFont(btn_font);
        
        jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        
        jp.add(title_label);
        jp.add(pin_label);
        jp.add(state_label);
        jp.add(start);
        
        add(jp, BorderLayout.CENTER);
        jp.setBackground(new Color(70, 23, 143));
        setVisible(true);
    }
    
}

class Round extends JFrame {
    
}


class ButtonListener implements ActionListener{
    @Override
    
    public void actionPerformed(ActionEvent e) {
        
        if (game_state == "waiting" && e.getSource() == "Start") {
            // only start game if at least 2 players joined
            
        }
        
    }
}