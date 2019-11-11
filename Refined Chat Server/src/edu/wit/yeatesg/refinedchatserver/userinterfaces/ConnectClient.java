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
	
	private void onAttemptConnect()
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
			
			// Inputs valid, try to connect

			Socket clientSocket = new Socket(field_ip.getText(), Integer.parseInt(field_port.getText()));
			DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

			DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
			Packet requestPacket = new MessagePacket(field_name.getText(), null, true);
			requestPacket.setDataStream(outputStream);
			requestPacket.send();

			Packet responsePacket = Packet.parsePacket(inputStream.readUTF());
			
			if (responsePacket instanceof ErrorPacket)
			{
				ErrorPacket errPacket = (ErrorPacket) responsePacket;
				label_errorMessage.setForeground(Color.DARK_RED);
				label_errorMessage.setText(errPacket.getErrorMessage());
				clientSocket.close();
			}
			else
			{	
				MessagePacket serverResponse = (MessagePacket) responsePacket;
				String connectedServerName = serverResponse.getSender();
				String connectedServerPort = serverResponse.getMessage();
				Color clientColor = serverResponse.getTextColor();
				
				new ChatClient(field_name.getText(), clientColor, clientSocket, inputStream, outputStream, connectedServerName, connectedServerPort);
				dispose();
			}
		}
		catch (Exception e)
		{
			label_errorMessage.setForeground(Color.DARK_RED);
			label_errorMessage.setText(errMsg);
		}
	}

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
				onAttemptConnect();
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
