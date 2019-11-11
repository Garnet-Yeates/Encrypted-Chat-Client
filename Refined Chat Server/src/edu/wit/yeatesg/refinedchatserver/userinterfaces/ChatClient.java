package edu.wit.yeatesg.refinedchatserver.userinterfaces;

import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.Timer;
import edu.wit.yeatesg.refinedchatserver.packets.Packet;
import edu.wit.yeatesg.refinedchatserver.other.Color;
import edu.wit.yeatesg.refinedchatserver.packets.ErrorPacket;
import edu.wit.yeatesg.refinedchatserver.packets.ExitPacket;
import edu.wit.yeatesg.refinedchatserver.packets.MessagePacket;

public class ChatClient extends Console
{
	private static final long serialVersionUID = 1300780296619415702L;

	private String clientName;
	
	private Socket clientSocket;
	
	private DataOutputStream outputStream;
	
	private String connectedServerPort;
	private String connectedServerName;
	
	private Color clientColor;
	
	public ChatClient(String clientName, Color clientColor, Socket clientSocket, DataInputStream inputStream, DataOutputStream outputStream, String serverName, String serverPort)
	{
		super();
		setTitle(clientName);
		this.clientSocket = clientSocket;
		this.outputStream = outputStream;
		this.connectedServerName = serverName;
		this.connectedServerPort = serverPort;
		this.clientName = clientName;
		this.clientColor = clientColor;
		if (validName(clientName))
		{
			try
			{
				logToConsole(new MessagePacket("Connected to a server with the name \"" + connectedServerName +"\" on port " + connectedServerPort));
	
				Thread inputThread = new Thread(() ->
				{
					try
					{
						Packet packetReceiving = null;
						do
						{
							packetReceiving = Packet.parsePacket(inputStream.readUTF());
							onPacketReceive(packetReceiving);
						}
						while (packetReceiving != null);						
					}
					catch (IOException e)
					{
						System.out.println(e.getStackTrace());
					}
				});

				inputThread.start();
			}
			catch (Exception e)
			{
				terminateWithDelay("Connection refused: connect", 1500);
			}
		}
	}
	
	@Override
	protected void onSend(String consoleMessage)
	{
		switch (consoleMessage.toLowerCase())
		{
		case "/end": case "/exit": case "/quit":
			ExitPacket pack = new ExitPacket(clientName);
			pack.setDataStream(outputStream);
			pack.send();
			break;
		default:
			MessagePacket msgPack = new MessagePacket(clientName, consoleMessage, Color.BLACK, clientColor);
			msgPack.setDataStream(outputStream);
			msgPack.send();
		}
	}
	
	private void onPacketReceive(Packet packet)
	{
		switch (packet.getClass().getSimpleName())
		{
		case "MessagePacket": // In this case the server is either sending a msg directly to this client or relaying other client messages to this client
			MessagePacket smp = (MessagePacket) packet;
			logToConsole(smp);
			break;
		case "ErrorPacket": // In this case there was some type of error that results in this client needing to be terminated
			ErrorPacket errPacket = (ErrorPacket) packet;
			terminateWithDelay(errPacket.getErrorMessage(), 3000);
			break;
		case "ExitPacket": // In this case the server closed all of its connections or this client sent an exit packet and got one back
			terminateWithDelay(3000);
			break;
		}
	}
	
	@Override
	protected void onWindowClose()
	{
		ExitPacket pack = new ExitPacket(clientName);
		if (outputStream != null)
		{
			pack.setDataStream(outputStream);
			pack.send();
		}
		terminateWithDelay(50);			
	}
	
	public void terminateWithDelay(int delay)
	{
		Timer t = new Timer(delay, (ActionEvent e) -> terminate()); // TODO THIS SHOULDNT JUST EXIT THE SYSTEM MAKE THIS DO THE HTING IT SHOULD OD LATER HEAR ME CUNT?
		t.setInitialDelay(delay);
		t.start();
	}
	
	public void terminateWithDelay(String message, int delay)
	{
		logToConsole(new MessagePacket("Info", message, Color.BLACK, Color.RED));
		terminateWithDelay(delay);
	}
	
	public void terminate()
	{
		try { clientSocket.close(); }
		catch (Exception e) { }
		System.exit(0);
	}

	public static boolean validName(String clientName)
	{
		return !(clientName == null) &&
				!clientName.equals("") && 
				!clientName.toLowerCase().equals("you") &&
				!clientName.toLowerCase().contains("server") &&
				!clientName.toLowerCase().equals("null") &&
				!clientName.contains("`");
	}
}
