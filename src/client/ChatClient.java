package client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.time.LocalDate;
import javax.swing.*;
import server.ChatServer;

class ChatClient extends JFrame implements Runnable {
	private String username;											// client username
	private String defaultPicFilename = "src/images/default_user.jpg";	// file location for the default profile pic
	private File selectedPicFile;										// image file which the user selects
	private File newPicFile;											// image file which is saved 
	private String colours[] = {"Colour", "Black", "Blue", "Cyan", "Grey", "Green", "Magenta", "Orange", "Pink", "Red", "White", "Yellow"};	// array of colours
	private ImageIcon pic;												// profile pic ImageIcon
	private JLabel profilePic;											// profie pic label that the icon is attached to
	private JTextField tField;											// text field that the user enters
	private JTextArea displayArea;										// text area that display incoming messages
	private JButton profileBtn, uploadBtn, sendBtn;						// profile button to select profile pic, upload button to change profile pic, send button to send message
	private JComboBox<String> colourCBox;								// colour ComboBox to select background colour
	private Panel panel1, panel2, panel3;								// three panels in the container
	private Container container;										// container
	private Socket server;												// connection to the server
	private BufferedReader in;											// reader for receiving messages
	private BufferedWriter out;											// reader for sending messages
	
	// constructor
	public ChatClient(String username) {
		super("Chat Client: " + username);
		this.username = username;	
		
		pic = new ImageIcon(defaultPicFilename);			// creates default profile pic

		profilePic = new JLabel(scaleImage(pic));			// scales profile pic and attaches it to the label

		tField = new JTextField();							// creates text field to enter messages
		tField.setPreferredSize(new Dimension(700, 50));

		// adds profile pic label and text field to panel 1
		panel1 = new Panel();
		panel1.add(profilePic);
		panel1.add(tField);

		
		displayArea = new JTextArea();							// creates text area to display messages
		displayArea.setPreferredSize(new Dimension(750, 650));
		
		// adds display area to panel 2
		panel2 = new Panel();
		panel2.add(new JScrollPane(displayArea));

		
		// creates the ComboBox to change the background colour and adds an event handler
		ColourCBoxHandler colourCBoxHandler = new ColourCBoxHandler();
		colourCBox = new JComboBox<String>(colours);
		colourCBox.addItemListener(colourCBoxHandler);
		
		// creates the button to select a profile pic and adds an event handler
		ProfileHandler profileBtnHandler = new ProfileHandler();
		profileBtn = new JButton("Profile");
		profileBtn.addActionListener(profileBtnHandler);
		
		// creates the button to change a profile pic and adds an event handler
		UploadHandler uploadBtnHandler = new UploadHandler();
		uploadBtn = new JButton("Upload");
		uploadBtn.addActionListener(uploadBtnHandler);
		
		// creates the button to send messages and adds an event handler
		SendHandler sendHandler = new SendHandler();
		sendBtn = new JButton("Send");
		sendBtn.addActionListener(sendHandler);
			
		
		// adds the ComboBox and three buttons to panel 3
		panel3 = new Panel(new GridLayout(1, 4));
		panel3.add(colourCBox);
		panel3.add(profileBtn);
		panel3.add(uploadBtn);
		panel3.add(sendBtn);

		
		// retrieves the current container and adds the three panels
		container = getContentPane();
		container.add(panel1, BorderLayout.NORTH);
		container.add(panel2, BorderLayout.CENTER);
		container.add(panel3, BorderLayout.SOUTH);
		
		// sets the size, location, visibility and default close operation of the JFrame
		setSize(800, 800);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			// creates a new connection to the server and creates the reader and writer
			server = new Socket("localhost", ChatServer.PORT);
			in = new BufferedReader(new InputStreamReader(server.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
			
			// sends a message to the server with the username and address, telling it the user has connected
			out.write(username + " connected to the server from " + InetAddress.getLocalHost());
			out.newLine();
			out.flush();
		}
		catch(UnknownHostException e) {
			displayArea.append(e.toString() + "\n");
			e.printStackTrace();
		}
		catch(IOException e) {
			displayArea.append(e.toString() + "\n");
			e.printStackTrace();
		}
	}

	// This method is always running is this thread so the client is always receiving messages from the server
	@Override
	public void run() {
		while(true) {
			String messageReceived = null;	// received message

			try {
				// checks to see if a message is received from the server
				while ((messageReceived = in.readLine()) != null) {
					// changes the profile pic if the message contains the "Sending image to the client: " string
					if(messageReceived.contains("Sending image to the client: ")) {
						String parts[] = messageReceived.split(" ");
						
						profilePic.setIcon(scaleImage(new ImageIcon(parts[5])));
					}
					// otherwise it displays the message
					else {
						displayArea.append(messageReceived  + "\n");
					}
				}
			} catch (IOException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}
		}
	}
	
	// method to scale the image to 50 x 50
	public ImageIcon scaleImage(ImageIcon img) {
		return new ImageIcon(img.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT));
	}
	
	// event handler for the colour ComboBox
	private class ColourCBoxHandler implements ItemListener {
		private Color colour;		// background Color object

		@Override
		public void itemStateChanged(ItemEvent event) {
			String colourName = colours[colourCBox.getSelectedIndex()];	// background colour name
			Component components[] = container.getComponents();			// array of components in the container (three panels)
			
			// changes the Color object based on the colour name selected by the user
			switch(colourName) {
			case "Black": colour = Color.BLACK;
			break;
			case "Blue": colour = Color.BLUE;
			break;
			case "Cyan": colour = Color.CYAN;
			break;
			case "Grey": colour = Color.GRAY;
			break;
			case "Green": colour = Color.GREEN;
			break;
			case "Magenta": colour = Color.MAGENTA;
			break;
			case "Orange": colour = Color.ORANGE;
			break;
			case "Pink": colour = Color.PINK;
			break;
			case "Red": colour = Color.RED;
			break;
			case "White": colour = Color.WHITE;
			break;
			case "Yellow": colour = Color.YELLOW;
			break;
			default: colour = null;
			break;
			}
			
			// changes the components in the container to the new colour
			for(Component comp: components) {
				comp.setBackground(colour);
			}

		}
	}
	
	// event handler for the profile button
	private class ProfileHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			JFileChooser fc = new JFileChooser();	// creates a new file chooser object
			fc.showOpenDialog(null);				// opents the file chooser
			selectedPicFile = fc.getSelectedFile();	// retrieves the selected file
			
			// returns an error message while the file type is not jpg, png or gif
			while(!selectedPicFile.getName().endsWith(".jpg")) {
				JOptionPane.showMessageDialog(null, "Please select an jpg image!");
				fc.showOpenDialog(null);
				selectedPicFile = fc.getSelectedFile();
			}
		}
	}
	
	// event handler for the upload button
	private class UploadHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			// checks to see if the user selected a file
			if(selectedPicFile != null) {
				try {
					String filePath = selectedPicFile.getAbsolutePath();						// file path
					String extension = "." + filePath.substring(filePath.lastIndexOf(".") + 1);	// file extension obtained from path
					
					newPicFile = new File("src/images/" + generateID() + "_" + LocalDate.now().toString() + extension);	// generates a new file location in the images folder
					Files.copy(selectedPicFile.toPath(), newPicFile.toPath(), StandardCopyOption.REPLACE_EXISTING);		// copies the selected file to the new file location
					
					// sends the file path to the server
					out.write("Sending image to the server: " + newPicFile.getAbsolutePath());
					out.newLine();
					out.flush();
				}
				catch(IOException e) {
					displayArea.append(e.toString() + "\n");
					e.printStackTrace();
				}
			}
			// otherwise it displays a pane to notify the user
			else {
				JOptionPane.showMessageDialog(null, "Please select an image first!");
			}
		}
		
		// generates a random integer between 1 - 1000
		public String generateID() {
			int num = (int)(Math.random() * 1000);
			return Integer.toString(num);
		}
	}
	
	// event handler for the send button
	private class SendHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			String messageSent = tField.getText();				// retrieves the text in the text field

			try {
				// send the message to the server
				out.write(messageSent + "\t- " + username);
				out.newLine();
				out.flush();

				displayArea.setCaretPosition(displayArea.getText().length());
				tField.setText("");												// resets the text
			}
			catch(IOException e) {
				displayArea.append(e.toString() + "\n");
				e.printStackTrace();
			}

		}
	}
}
