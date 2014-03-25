package a1p2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientServer {
	public static final int PEER_PORT_NUMBER = 28866; // set port number to 2000 + student id last digits
	public static final int MASTER_CLIENTSERVER_PORT_NUMBER = 9899; 
	public int num_connections = 0; // used for identifying incoming client connections
	
	String player_name;
	public Game _game;
	public Game.Player _player1;
	public Game.Player _player2;
	
    public static void main(String[] args) {
    	try {
			new ClientServer().start();
		} catch (Exception e) {
			System.out.println("Something failed: " + e.getMessage());
			e.printStackTrace();
		}
    }
    
    public void start() throws Exception {
        new Thread(new MasterServerCommunication()).start();
        
    	ServerSocket peer_socket = null;
        try {
        	peer_socket = new ServerSocket(PEER_PORT_NUMBER);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + PEER_PORT_NUMBER + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        
        // start listening to a connection and accept it.  Start a new handler and thread for every new connection
        while (true) {
        	try {
        		num_connections++;
        		System.out.println("Now accepting connections to your port: "+PEER_PORT_NUMBER);
        		new Thread(new ConnectionRequestHandler(peer_socket.accept(),num_connections)).start();
        	} catch (IOException e) {
        		System.out.println(e);
    	    } finally {
    	    	//peer_socket.close();
    	    }
        }
    }
    
    public class ConnectionRequestHandler implements Runnable {
    	private Socket socket = null;
    	private PrintWriter out = null;
    	private BufferedReader in = null;
    	private BufferedReader stdIn = null;
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
	            stdIn = new BufferedReader(new InputStreamReader(System.in));
	            String user_input;
	            String client_input = in.readLine();
	            if (client_input.equalsIgnoreCase("INVITATION")) {
                	System.out.println("A player has invited you to a game, would you like to play? (yes or no)");
                	user_input = stdIn.readLine();
                	out.println(user_input);
                	if (!user_input.equalsIgnoreCase("yes")) {
                		System.out.println("You declined the invite.");
                	}
                }
	            
	            while((client_input= in.readLine()) != null){
	            	if (client_input.startsWith("GAMESTART")) {
		            	String challenger = client_input.split("\\s+")[1];
	            		int challenger_port = Integer.parseInt(client_input.split("\\s+")[2]);
	            		
	            		// begin game
	            		System.out.println("Starting game... challenged by: "+challenger+":"+challenger_port);
	            		Game game = new Game(client_input.split("\\s+")[3]);
	            		
	            		Game.Player player1 = game.new Player(challenger, challenger_port);
	            		Game.Player player2 = game.new Player(player_name, PEER_PORT_NUMBER);
	            		_game = game;
	            		_player1 = player2;
	            		_player2 = player1;
	            		_game.guessLetter(_player1, client_input.split("\\s+")[4].charAt(0));
	            		
	            		System.out.println("\n\n-----------------GUESS THE WORD! BY GUESSING A LETTER!-----------------\n\n");
	            		System.out.println("The word so far is: "+ _game.word_sofar);
	            		System.out.println("\nYour turn to guess a letter!");
	            		user_input = stdIn.readLine();
	            		System.out.println(_game.guessLetter(_player1, user_input.charAt(0)));
	            		
	            		out.println("YOURMOVE "+user_input);
	            		
		            }
		            
		            if (client_input.startsWith("YOURMOVE")) {
		            	if (_game.word_sofar.equalsIgnoreCase(_game.word)) {
		            		System.out.println("Game has ended!");
		            		if (_player1.score > _player2.score) {
		            			System.out.println(_player1.name+" ("+_player1.score+" points) has won! ");
		            			out.println("GAMEEND 1");
		            		} else if (_player1.score < _player2.score) {
		            			System.out.println(_player2.name+" ("+_player2.score+" points) has won! ");
		            			out.println("GAMEEND 2");
		            		} else {
		            			System.out.println("Its a tie!");
		            			out.println("GAMEEND 0");
		            		}
		            		socket.close();
		            	} else {
			            	_game.guessLetter(_player2, client_input.split("\\s+")[1].charAt(0)); // simulate previous player's move
			            	System.out.println("\nYour turn to guess a letter!");
			            	System.out.println("The word so far is: "+ _game.word_sofar);
			            	user_input = stdIn.readLine();
			            	System.out.println(_game.guessLetter(_player1, user_input.charAt(0)));
			            	out.println("YOURMOVE "+user_input);
		            	}
		            }
		            // end game 
		            if (client_input.startsWith("GAMEEND")) {
	            		System.out.println("Game has ended!");
	            		if (client_input.split("\\s+")[1].equals("1")) {
	            			System.out.println(_player1.name+" ("+_player1.score+" points) has won! ");
	            		} else if (client_input.split("\\s+")[1].equals("2")) {
	            			System.out.println(_player2.name+" ("+_player2.score+" points) has won! ");
	            		} else {
	            			System.out.println("Its a tie!");
	            		}
	            		socket.close();
	            		break;
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
    
    public class MasterServerCommunication implements Runnable {
    	public MasterServerCommunication() {
    	}
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader stdIn = null;
        PrintWriter out2 = null;
        BufferedReader in2 = null;
        BufferedReader stdIn2 = null;
        
        public void run() {
        	System.out.println("Hi! Please provide a command: JOIN, LEAVE, LIST, INVITE <player_name> <port>");
            try {
        	   	InetAddress host = InetAddress.getLocalHost();
                socket = new Socket(host.getHostName(), MASTER_CLIENTSERVER_PORT_NUMBER);
                
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
            			player_name = in.readLine();
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
                	
                	if (user_input.startsWith("INVITE") || user_input.startsWith("invite")) {
                		if (user_input.split("\\s+").length < 3) {
                			System.out.println("Please enter a player name to invite (find players from the LIST command) and their port number (ie. player3 28866).");
                		} else {
    	            		String invited_player = user_input.split("\\s+")[1];
    	            		int invited_player_port = Integer.parseInt(user_input.split("\\s+")[2]);
    	            		System.out.println("Sending invitation request to " + invited_player);
    	            		out.println("INVITE "+invited_player);
    	            		String server_response_address = in.readLine();
    	            		if (server_response_address.equals("-1")) {
    	            			System.out.println(invited_player + " is offline!");
    	            		} else {   	            			
    	            			// get invited_player's connection info from server
    	            			System.out.println("Establishing connection to "+invited_player+" at "+server_response_address+":"+invited_player_port);
    	            			socket = new Socket(server_response_address, invited_player_port);
    		            		out2 = new PrintWriter(socket.getOutputStream(), true);
    		                    in2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		                    stdIn2 = new BufferedReader(new InputStreamReader(System.in));
    		                    String user_input2;
    		                    
    		                    // send out actual invitation to the online player
    		                    out2.println("INVITATION");
    		                    System.out.println("Awaiting reply from "+invited_player);
    		                    user_input = in2.readLine();
    		                    if (user_input.equalsIgnoreCase("yes")) {
                    				// initiate game mechanics
        	        	        	System.out.println("Invite has been accepted!  Starting game...");
        	        	        	
        	        	        	//player1 is always the person that sent the invite
        	        	        	Game game = new Game();
			                		Game.Player player1 = game.new Player(player_name, PEER_PORT_NUMBER);
			                		Game.Player player2 = game.new Player(invited_player, invited_player_port);
			                		_game = game;
			                		_player1 = player1;
			                		_player2 = player2;
			                		
			                		System.out.println("\n\n-----------------GUESS THE WORD! BY GUESSING A LETTER!-----------------\n\n");
			                		System.out.println("The word so far is: "+ _game.word_sofar);
			                		System.out.println("\nGuess a letter!");
			                		user_input2 = stdIn2.readLine();
			                		System.out.println(_game.guessLetter(_player1, user_input2.charAt(0)));
			                		
			                		out2.println("GAMESTART "+player_name+" "+PEER_PORT_NUMBER+" "+_game.word+" "+user_input2);
                    			} else { // add another if statement if invited player is in a game already
                    				System.out.println(invited_player + " has declined the invitation!");
                    			}
    		                    
    		                    while((user_input= in2.readLine()) != null) {
	    		                    if (user_input.startsWith("YOURMOVE")) {
	    				            	_game.guessLetter(_player2, user_input.split("\\s+")[1].charAt(0)); // simulate previous player's move
	    				            	if (_game.word_sofar.equalsIgnoreCase(_game.word)) {
	    				            		System.out.println("Game has ended!");
	    				            		if (_player1.score > _player2.score) {
	    				            			System.out.println(_player1.name+" ("+_player1.score+" points) has won! ");
	    				            			out2.println("GAMEEND 1");
	    				            		} else if (_player1.score < _player2.score) {
	    				            			System.out.println(_player2.name+" ("+_player2.score+" points) has won! ");
	    				            			out2.println("GAMEEND 2");
	    				            		} else {
	    				            			System.out.println("Its a tie!");
	    				            			out2.println("GAMEEND 0");
	    				            		}
	    				            		socket = new Socket(host.getHostName(), MASTER_CLIENTSERVER_PORT_NUMBER);
	    				            		break;
	    				            	} else {
	    				            		System.out.println("\nYour turn to guess a letter!");
	    				            		System.out.println("The word so far is: "+ _game.word_sofar);
	        				            	user_input = stdIn2.readLine();
	        				            	System.out.println(_game.guessLetter(_player1, user_input.charAt(0)));
	        				            	out2.println("YOURMOVE "+user_input);
	    				            	}
	    				            }
	    		                    
	    		                    if (user_input.startsWith("GAMEEND")) {
	    			            		System.out.println("Game has ended!");
	    			            		if (user_input.split("\\s+")[1].equals("1")) {
	    			            			System.out.println(_player1.name+" ("+_player1.score+" points) has won! ");
	    			            		} else if (user_input.split("\\s+")[1].equals("2")) {
	    			            			System.out.println(_player2.name+" ("+_player2.score+" points) has won! ");
	    			            		} else {
	    			            			System.out.println("Its a tie!");
	    			            		}
    				            		socket = new Socket(host.getHostName(), MASTER_CLIENTSERVER_PORT_NUMBER);
	    			            		break;
	    				            }
    		                   }
    	            		}
                		}
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
    	        	out2.close();
    				in2.close();
    				stdIn2.close();
            	} catch(Exception e) { 
    				System.out.println("Couldn't close I/O streams");
    			}
            }
        }
    }
}