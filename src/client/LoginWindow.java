package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginWindow extends JFrame {
	private JLabel label1, label2;	// labels to display some welcome text and enter username
	private JPanel panel1, panel2;	// panels to sort the container
	private JTextField tField;		// text field to enter username
	
	public LoginWindow() {
		super("Login");
		
		// creates labels to welcome the user and tell them to enter their usernmae
		label1 = new JLabel("Welcome to Aaron's Live Chat!");
		label2 = new JLabel("Enter Username:");
		
		// adds labels to panel 1
		panel1 = new JPanel();
		panel1.add(label1);
		
		
		// creates a text field to enter the username and adds an event handler
		TFieldHandler tFieldHandler = new TFieldHandler();
		tField = new JTextField(10);
		tField.addActionListener(tFieldHandler);
		
		// adds text field to panel 2
		panel2= new JPanel();
		panel2.add(label2);
		panel2.add(tField);
		
		
		// retrieves the current container, changes the layout and adds the panels to it
		Container container = getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.add(panel1, BorderLayout.CENTER);
		container.add(panel2, BorderLayout.SOUTH);
		
		// sets the size, location, visibility and default close operation of the JFrame
		setSize(350, 350);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// event handler for the text field
	private class TFieldHandler implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String username = event.getActionCommand();
			
			// checks to see if the user entered anything in the text field
			if(username.length() != 0) {
				dispose();	// gets rid of the current login window
				
				// starts a new chat client thread with the entered username
				Thread t = new Thread(new ChatClient(username));
				t.start();
			}
			// otherwise is displays a warning message
			else {
				JOptionPane.showMessageDialog(null, "Please enter a username!");
			}
		}
	}
}
