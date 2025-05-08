/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kahoot.server_logic;


import java.net.*;
import java.util.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import static kahoot.server_logic.Server.clients;


/**
 *
 * @author sanjidaorpi
 */
public class Server {

    /**
     * @param args the command line arguments
     */
    
    static ArrayList<ProcessConnection> clients = new ArrayList<>();
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            ServerSocket ss = new ServerSocket(5190);
            
            while(true){
                
                Socket s = ss.accept();
                // restricting more than 4 players from joining the game
                synchronized (clients) {
                    if (clients.size() >= 4) {
                        System.out.println("Game is full");
                        s.close();
                        continue;
                    }

                    int num_players = clients.size() + 1;
                    ProcessConnection connection = new ProcessConnection(s, num_players);
                    clients.add(connection);
                    connection.start();
                }
                
            }
            
        } catch (IOException ex) {
            System.out.println("IOException: "+ex.toString());
        }
    }
}

class ProcessConnection extends Thread{
    Socket s;
    int i;
    String username;
    PrintStream sout;
    ProcessConnection(Socket news, int newi){s = news; i = newi;}
    
    public void run(){
        try {
            Scanner sin = new Scanner(s.getInputStream());
            sout = new PrintStream(s.getOutputStream());
            if (sin.hasNextLine()) {
                username = sin.nextLine().trim();
            }
            
            if (clients.size() < 4) {
                clients.add(this);
                System.out.println(username+" connected");
            }
            
            
            while (s.isConnected() && sin.hasNextLine()) {
                String message = sin.nextLine();
                for (ProcessConnection client : clients) {
                    client.sout.println(username + ": " + message);
                }
            }

        } catch (IOException ex) {
            System.out.println("IOException in client "+i+": "+ex.toString());
            
        } finally {
            
            try {
                s.close();
                
            } catch (IOException ignored) {
            }
            
            clients.remove(this);
        }
    }
}