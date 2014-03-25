package a1p1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
	final static int PORT_NUMBER = 28866; // set port number to 2000 + student id last digits
	public int num_connections = 0; // used for identifying incoming client connections
	static List <String> players = new ArrayList <String>();
	
    public static void main(String[] args) {        
        try {
			new Server().start();
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}
    }
    
    // declares and assigns server socket
    public void start() throws Exception {
    	System.out.println("Server started.  Connected on port:" + PORT_NUMBER);
    	ServerSocket server_socket = null;
        try {
        	server_socket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + PORT_NUMBER + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        
        // start listening to a connection and accept it.  Start a new handler and thread for every new connection
        while (true) {
        	try {
        		num_connections++;
        		new Thread(new ConnectionRequestHandler(server_socket.accept(), num_connections)).start();
        	} catch (IOException e) {
        		System.out.println(e);
    	    } finally {
    	    	server_socket.close();
    	    }
        }
    }
    
    // We use 'implements Runnable' over 'extends Thread' because we are not overriding/specializing any thread behaviour
    public class ConnectionRequestHandler implements Runnable {
    	private Socket socket = null;
    	private PrintWriter out = null;
    	private BufferedReader in = null;
    	private String player_name;
    	
    	public ConnectionRequestHandler(Socket socket, int player_id){
    		this.socket = socket;
    		this.player_name = "Player"+player_id;
    		System.out.println(player_name +" ("+socket.getLocalAddress() +") has connected.");
    	}
    	   
    	public void run() {
    		try {
	    		out = new PrintWriter(socket.getOutputStream(), true);                   
	            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String client_input;
	            
	            // Read input from client and execute on commands
	            while ((client_input = in.readLine()) != null) {
	            	if (client_input.equalsIgnoreCase("JOIN")) {
	                	if (players.contains(player_name)) {
	                		out.println("You are already on the list!");
	                	} else {
	                		System.out.println("New player: "+ player_name +" ("+ socket.getLocalAddress() +") has joined the list of players.");
		                	players.add(player_name);
		                	out.println("Successfully joined list of players.");
	                	}
	                } else if (client_input.equalsIgnoreCase("LEAVE")) {
	                	if (players.contains(player_name)) {
	                		System.out.println(player_name +" ("+ socket.getLocalAddress() +") has left the list of players.");
		                	players.remove(player_name);
		                	out.println("You've left the list of players.");
	                	} else {
	                		out.println("You are not on the list, please JOIN first.");
	                	}
	                } else if (client_input.equalsIgnoreCase("LIST")) {
	                	out.println(players.toString());
	                } else {
	                	System.out.println(client_input);
	                }
	            }
    		} catch (IOException e) {
				e.printStackTrace();
			} finally { //In case anything goes wrong we need to close our I/O streams and sockets.
				try {
					socket.close();
					out.close();
					in.close();
				} catch(Exception e) { 
					System.out.println("Couldn't close I/O streams");
				}
			}
    	}
    }
}