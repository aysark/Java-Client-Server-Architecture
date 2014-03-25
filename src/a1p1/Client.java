package a1p1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static final int PORT_NUMBER = 28866; // set port number to 2000 + student id last digits
	
    public static void main(String[] args) {
    	try {
			new Client().start();
		} catch (Exception e) {
			System.out.println("Something falied: " + e.getMessage());
			e.printStackTrace();
		}
    }
    
    public void start() throws IOException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader stdIn = null;
        
        System.out.println("Hi! Please provide a command: JOIN, LEAVE, LIST");
       try {
    	   	InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host.getHostName(), PORT_NUMBER);
            
            // Setup communication streams between client and server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            String user_input;
            
            while ((user_input = stdIn.readLine()) != null) {
            	if (user_input.equalsIgnoreCase("JOIN")) {
            		System.out.println("Requesting to join...");
            		out.println("JOIN");
        			System.out.println("Server - " + in.readLine());
            	}
            	
            	if (user_input.equalsIgnoreCase("LEAVE")) {
            		System.out.println("Leaving the server...");
            		out.println("LEAVE");
            		System.out.println("Server - " + in.readLine());
            	}
            	
            	if (user_input.equalsIgnoreCase("LIST")) {
            		System.out.println("Getting list of players...");
            		out.println("LIST");
            		System.out.println("Server - " + in.readLine());
            	}
            	
            	
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host of client.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection.");
            System.exit(1);
        } finally { //In case anything goes wrong we need to close our I/O streams and sockets.
        	try {
	        	socket.close();
	        	in.close();
	        	out.close();
	        	stdIn.close();
        	} catch(Exception e) { 
				System.out.println("Couldn't close I/O streams");
			}
        }
    }
}