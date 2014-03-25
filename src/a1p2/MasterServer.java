package a1p2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MasterServer {
	final static int MASTER_CLIENTSERVER_PORT_NUMBER = 9899; 
	public int num_connections = 0; // used for identifying incoming client connections
	static Map<String, Socket> players = new HashMap<String, Socket>(); // map where key = player_name and value = socket data
	
    public static void main(String[] args) {        
        try {
			new MasterServer().start();
		} catch (Exception e) {
			System.out.println("I/O failure: " + e.getMessage());
			e.printStackTrace();
		}
    }
    
    // declares and assigns server socket
    public void start() throws Exception {
    	System.out.println("Server started.  Connected on port:" + MASTER_CLIENTSERVER_PORT_NUMBER);
    	ServerSocket server_socket = null;
        try {
        	server_socket = new ServerSocket(MASTER_CLIENTSERVER_PORT_NUMBER);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + MASTER_CLIENTSERVER_PORT_NUMBER + " or listening for a connection");
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
    	    	//server_socket.close();
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
    		System.out.println(player_name +" ("+socket.getLocalAddress() +":"+socket.getLocalPort()+") has connected.");
    	}
    	   
    	public void run() {
    		try {
	    		out = new PrintWriter(socket.getOutputStream(), true);                   
	            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            String client_input;
	            
	            // Read input from client and execute on commands
	            while ((client_input = in.readLine()) != null) {
	            	if (client_input.equalsIgnoreCase("JOIN")) {
	                	if (players.containsKey(player_name)) {
	                		out.println("You are already on the list!");
	                	} else {
	                		System.out.println("New player: "+ player_name +" ("+ socket.getLocalAddress() +":"+socket.getLocalPort()+") has joined the list of players.");
	                		players.put(player_name, socket);
		                	out.println("Successfully joined list of players, welcome "+player_name+"!");
		                	out.println(player_name);
	                	}
	                } else if (client_input.equalsIgnoreCase("LEAVE")) {
	                	if (players.containsKey(player_name)) {
	                		System.out.println(player_name +" ("+ socket.getLocalAddress() +":"+socket.getLocalPort()+") has left the list of players.");
		                	players.remove(player_name);
		                	out.println("You've left the list of players.");
	                	} else {
	                		out.println("You are not on the list, please JOIN first.");
	                	}
	                } else if (client_input.equalsIgnoreCase("LIST")) {
	                	out.println(players.toString());
	                } else if (client_input.startsWith("INVITE")) { // this class acts as a central server only for player lookup in the invitation scheme
	                	String invited_player = client_input.split("\\s+")[1];
	                	
	                	if (players.containsKey(invited_player)) {
	                		out.println(players.get(invited_player).getLocalAddress().getHostName());
	                	} else {
	                		out.println(-1); // no such player exists in player list, therefore they are offline
	                	}
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