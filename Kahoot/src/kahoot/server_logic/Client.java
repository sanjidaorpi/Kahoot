/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kahoot.server_logic;


import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;

/**
 *
 * @author sanjidaorpi
 */
public class Client {

    /**
     * @param args the command line arguments
     */
    
    static JFrame jf;
    
    // For connection page
    static  JPanel panel1;
    static  JLabel serverLabel;
    static  JTextField server_input;
    static  JPanel panel2;
    static  JLabel userLabel;
    static  JTextField user_input;
    static  JButton connect;
    
    // For send message page
    static JTextArea messages;
    static JTextArea textMessage;
    static  JButton send;
    
    // For connection
    static String server = "";
    static String username = "";
    static Socket s;
    static Scanner sin;
    static PrintStream sout;
    
    public static void main(String[] args) {
        // TODO code application logic here
        
        jf = new JFrame("Chat Server");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(600, 500);
        jf.setLayout(new FlowLayout());
        
        panel1 = new JPanel(new FlowLayout());
        serverLabel = new JLabel("Server Name:");
        server_input = new JTextField(10);
        panel1.add(serverLabel);
        panel1.add(server_input);
        
        panel2 = new JPanel(new FlowLayout());
        userLabel = new JLabel("Username:");
        user_input = new JTextField(10);
        panel2.add(userLabel);
        panel2.add(user_input);
        
        connect = new JButton("Connect");
        connect.addActionListener(new ButtonListener());
        
        jf.add(panel1);
        jf.add(panel2);
        jf.add(connect);
        jf.setVisible(true);
    }
    
    static void changePage() {
        jf.getContentPane().removeAll();
        
        JPanel messagePanel = new JPanel();
        JPanel textPanel = new JPanel();
        jf.setLayout(new BorderLayout());
        
        messages = new JTextArea();
        messages.setEditable(false);
        JScrollPane messageArea = new JScrollPane(messages);
        messageArea.setPreferredSize(new Dimension(530, 410));
        messagePanel.add(messageArea);
        
        textMessage = new JTextArea();
        textMessage.setPreferredSize(new Dimension(450, 25));
        send = new JButton("SEND");
        
        textPanel.add(textMessage);
        textPanel.add(send);
        send.addActionListener(new ButtonListener()); 

        jf.add(messagePanel, BorderLayout.CENTER); 
        jf.add(textPanel, BorderLayout.SOUTH);
        
        jf.repaint();
        jf.setVisible(true);
    }
   
    
    static class ButtonListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            
            if (e.getSource() == connect) {
                server = server_input.getText();
                username = user_input.getText();
                changePage();
                
                try {
                    s = new Socket(server, 5190);
                    sin = new Scanner(s.getInputStream());
                    sout = new PrintStream(s.getOutputStream());
                    sout.println(username);
                    
                } catch (IOException ex) {
                    System.out.println("Exception thrown: "+ex.toString());
                }
                
                new Thread(() -> {
                    
                    while (sin.hasNextLine()) {
                        String message = sin.nextLine();
                        messages.append(message + "\n");
                    }
                    
                }).start();
                
            } else if (e.getSource() == send) {
                sout.print(textMessage.getText().trim() + "\n");
                textMessage.setText("");
            }
            
        }
    }
}