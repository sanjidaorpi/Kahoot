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
    static int game_pin;
    
    public Server(int game_pin) {
        Server.game_pin = game_pin;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            ServerSocket ss = new ServerSocket(5190);
            
            while(true){
                
                Socket s = ss.accept();
                
                Scanner sin = new Scanner(s.getInputStream());
                PrintStream sout = new PrintStream(s.getOutputStream());

                String username = sin.nextLine().trim();
                String game_pin = sin.nextLine().trim();

                if (!game_pin.equals(Integer.toString(Server.game_pin))) {
                    sout.println("Invalid game PIN");
                    s.close();
                    continue;
                }

                // restricting more than 4 players from joining the game
                synchronized (clients) {
                    
                    if (clients.size() >= 4) {
                        System.out.println("Game is full");
                        s.close();
                        continue;
                    }

                    int num_players = clients.size() + 1;
                    ProcessConnection connection = new ProcessConnection(s, num_players, username, sout, sin);
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
    Scanner sin;

    ProcessConnection(Socket s, int i, String username, PrintStream sout, Scanner sin) {
        this.s = s;
        this.i = i;
        this.username = username;
        this.sout = sout;
        this.sin = sin;
    }
    
    @Override
    public void run(){
        System.out.println(username + " connected");
        while (s.isConnected() && sin.hasNextLine()) {
            String message = sin.nextLine();
            for (ProcessConnection client : clients) {
                client.sout.println(username + ": " + message);
            }
        }
        try {
            s.close();
            
        } catch (IOException ignored) {
        }
        clients.remove(this);
    }
}