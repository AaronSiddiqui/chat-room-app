package server;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class ChatServer extends JFrame implements Runnable {
	private JTextArea displayArea;				// text area to display the log of messages and connections
	private ServerSocket serverSock;			// server socket
	private Socket clientSock;					// single client socket
	private ArrayList<Socket> clients;			// ArrayList of client sockets
	private ArrayList<String> chatHistory;		// ArrayList of string that holds the total client chat history
	public static final int PORT = 4001;		// server port number
	
	// constructor
	public ChatServer() {
		super("Chat Server");
		
		// creates new ArrayLists
		clients = new ArrayList<Socket>();
		chatHistory = new ArrayList<String>();

		displayArea = new JTextArea();		// creates text area to display the log of messages and connections
		
		// retrieves the current container and adds the text area
		Container container = getContentPane();
		container.add(new JScrollPane(displayArea), BorderLayout.CENTER);
		
		// sets the size, location and default close operation of the JFrame
		setSize(800, 800);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// creates a new server socket
		try {
			serverSock = new ServerSocket(PORT);
		} catch (IOException e) {
			displayArea.append(e.toString() + "\n");
			e.printStackTrace();
		}
	}
	
	// This method is always running in the thread 
	@Override
	public void run() {
		while(true) {
			try {
				clientSock = serverSock.accept();	// waits for a client socket to connect
				clients.add(clientSock);			// adds the client socket to the ArrayList
				
				// creates a new thread for a client and starts
				Client c = new Client(clientSock);
				c.start();

				Thread.sleep(250);	// short wait between thread
			}
			catch (InterruptedException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}
			catch (IOException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}
		}
	}
	
	// client thread for each client socket that connects
	private class Client extends Thread {
		private String username;			// client's username
		private String userPicFilename;		// client's profile pic file location
		private String message;				// client's message
		private ArrayList<String> chat;		// client's current chat
		private Socket client;				// client's socket
		private BufferedReader in;			// client's reader to receive messages
		private BufferedWriter out;			// client's writer to send messages
		
		// constructor
		public Client(Socket client) {
			message = null;				// creates null message
			chat = new ArrayList<String>();		// creates new ArrayList
			this.client = client;		// sets the client's socket
			
			// creates reader and writer
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			} catch (IOException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}
		}
		
		// This method is always running in the thread so the server is always receiving and sending messages
		@Override
		public void run() {
			try {
				// checks to see if a message is received from the client
				while ((message = in.readLine()) != null) {
					displayArea.append(message + "\n");		// display the server it on the server
					
					String parts[];		// stores the divided message
					
					// checks to see if this is the first time the client is connecting to the server
					if(message.contains(" connected to the server from ")) {
						parts = message.split(" ");			// splits the message
						username = parts[0];				// saves the client's username
						message = username + " connected";	// alters the message
						
						// sends a welcome message to the client
						out.write("Welcome to the server!");
						out.newLine();
						out.flush();
						
						// sends any previous chat history to the client
						for(String msg: chatHistory) {
							out.write(msg);
							out.newLine();
							out.flush();
						}
						
						chat.add(message);			// adds the message to the current chat
						chatHistory.add(message);	// adds message to the chat history
					}
					// checks to see if the client is sending an image (profile pic)
					else if(message.contains("Sending image to the server: ")) {
						userPicFilename  = "";				// profile pic file location
						parts = message.split("\\\\");		// splits the message on the backslashes
						
						/*
						 * This for loop is used to set the profile pic location to the 3 last parts of the 
						 * message because when opening the file, the compiler can't interpret the the absolute 
						 * file location, but it can interpret the last 3 parts.
						 */
						for(int i = (parts.length - 3); i < parts.length; i++) {
							userPicFilename  += parts[i] + "/";		// adds a forward slash between the split parts
						}
						
						// sends a message to the client to change the profile pic to the new profile pic location
						out.write("Sending image to the client: " + userPicFilename);
						out.newLine();
						out.flush();
					}
					// otherwise it the adds the message to the current chat and chat history
					else {
						chat.add(message);
						chatHistory.add(message);
					}
					
					// sends all the messages in the current chat to all the connected client sockets
					while(chat.size() != 0) {
						for(Socket sock: clients) {
							BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
							out2.write(chat.get(0));
							out2.newLine();
							out2.flush();
						}
						
						// removes the messages from the current chat
						chat.remove(0);
					}
				}
			}
			catch(IOException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}
		}
	}
}
