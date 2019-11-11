package edu.wit.yeatesg.refinedchatserver.userinterfaces;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.wit.yeatesg.refinedchatserver.other.Color;
import edu.wit.yeatesg.refinedchatserver.packets.ErrorPacket;
import edu.wit.yeatesg.refinedchatserver.packets.MessagePacket;
import edu.wit.yeatesg.refinedchatserver.packets.Packet;

import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * This GUI is the first one that is created when a user opens ChatClient.jar. This is a simple
 * GUI that consists of 2 text fields where the user enters the IP address and the port of the
 * server, and an additional text field where they enter a desired display name. When they press
 * the connect button, this GUI will attempt to connect them to the server with the given
 * information. If it is successful, a new {@link ChatClient} GUI is created and opened up and
 * the user will be able to chat. If the connection is unsuccessful, an error message will be
 * displayed.
 * @author yeatesg
 *
 */
public class ConnectClient extends JFrame
{
	private static final long serialVersionUID = 1454653210967675986L;

	private JPanel contentPane;
	
	private JTextField field_ip;
	private JTextField field_port;
	private JTextField field_name;
	private JButton button_connect;
	private JLabel label_errorMessage;

	public ConnectClient()
	{
		initFrame();
		Console.setLookAndFeel();
		setTitle("Connect");
		setVisible(true);
	}
	
	/**
	 * Attempts to connect to the server with the user specified IP address and port. If the user input
	 * is erroneous then it will display an error message and cancel the connection attempt. Otherwise,
	 * it will attempt to connect to the server. If the connection fails then it displays "connection
	 * refused" on this GUI. If the connection succeeds but the server is full or the supplied user name
	 * is taken, then an appropriate error message is displayed.
	 */
	private void attemptConnect()
	{
		String errMsg = "";
		try
		{
			errMsg = "Invalid Port";
			Integer.parseInt(field_port.getText());

			errMsg = "Invalid IP Address";
			if (!field_ip.getText().equals("localhost") && !field_ip.getText().contains("."))
				throw new RuntimeException();

			errMsg = "Invalid Client Name";
			if (!ChatClient.validName(field_name.getText()))
				throw new RuntimeException();

			errMsg = "Connection Refused";
			
			// Inputs are not erroneous, try to connect

			Socket clientSocket = new Socket(field_ip.getText(), Integer.parseInt(field_port.getText()));
			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			
			// Send a request packet to the server
			Packet requestPacket = new MessagePacket(field_name.getText(), null, true);
			requestPacket.setDataStream(outputStream);
			requestPacket.send();

			Packet responsePacket = Packet.parsePacket(inputStream.readUTF());

			if (responsePacket instanceof ErrorPacket) // If the server responds with an error (connection was denied for some reason)
			{
				ErrorPacket errPacket = (ErrorPacket) responsePacket;
				label_errorMessage.setForeground(Color.DARK_RED);
				label_errorMessage.setText(errPacket.getErrorMessage());
				clientSocket.close();
			}
			else // If the server responds with a message (connection was accepted)
			{	
				// The server's response to our request contains the server's name and port so the client can store this information
				MessagePacket serverResponse = (MessagePacket) responsePacket;
				String connectedServerName = serverResponse.getSender();
				String connectedServerPort = serverResponse.getMessage();
				Color clientColor = serverResponse.getTextColor();
				
				new ChatClient(field_name.getText(), clientColor, clientSocket, inputStream, outputStream, connectedServerName, connectedServerPort);
				dispose(); // We are now done with this GUI
			}
		}
		catch (Exception e)
		{
			label_errorMessage.setForeground(Color.DARK_RED); // We ran into some error on the way, print the error message
			label_errorMessage.setText(errMsg);
		}
	}

	/**
	 * Initializes the frame by adding 3 text fields and respective labels for user name, ip address, and port.
	 * This also adds a button called 'Connect' with an action listener that calls {@link #attemptConnect()}.
	 */
	public void initFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 236, 160);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		field_ip = new JTextField();
		field_ip.setHorizontalAlignment(SwingConstants.LEFT);
		field_ip.setBounds(10, 29, 108, 20);
		contentPane.add(field_ip);
		field_ip.setColumns(10);

		field_port = new JTextField();
		field_port.setHorizontalAlignment(SwingConstants.LEFT);
		field_port.setColumns(10);
		field_port.setBounds(142, 29, 68, 20);
		contentPane.add(field_port);

		field_name = new JTextField();
		field_name.setColumns(10);
		field_name.setBounds(10, 70, 108, 20);
		contentPane.add(field_name);

		JLabel label_name = new JLabel("Name");
		label_name.setFont(new Font("Tahoma", Font.BOLD, 11));
		label_name.setBounds(10, 55, 61, 14);
		contentPane.add(label_name);

		button_connect = new JButton("Connect");
		button_connect.setBounds(124, 69, 86, 23);
		contentPane.add(button_connect);
		button_connect.addActionListener((e) ->
		{
			label_errorMessage.setForeground(Color.DARK_GREEN);
			label_errorMessage.setText("Connecting...");
			EventQueue.invokeLater(() -> // Make sure the label says "Connecting" before the thread freezes waiting to connect
			{
				attemptConnect();
			});
		});

		label_errorMessage = new JLabel("");
		label_errorMessage.setForeground(Color.DARK_RED);
		label_errorMessage.setBounds(10, 95, 189, 14);
		contentPane.add(label_errorMessage);

		JLabel label_ipAddress = new JLabel("IP Address");
		label_ipAddress.setFont(new Font("Tahoma", Font.BOLD, 11));
		label_ipAddress.setBounds(10, 11, 61, 14);
		contentPane.add(label_ipAddress);

		JLabel label_port = new JLabel("Port");
		label_port.setFont(new Font("Tahoma", Font.BOLD, 11));
		label_port.setBounds(142, 11, 53, 14);
		contentPane.add(label_port);

		JLabel lblNewLabel = new JLabel(":\r\n");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 32));
		lblNewLabel.setBounds(124, 16, 13, 36);
		contentPane.add(lblNewLabel);
	}
}
