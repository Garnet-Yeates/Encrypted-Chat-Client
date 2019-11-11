package edu.wit.yeatesg.refinedchatserver.userinterfaces;

import static edu.wit.yeatesg.refinedchatserver.other.Color.*;

import java.awt.event.ActionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import javax.swing.Timer;

import edu.wit.yeatesg.refinedchatserver.other.Color;
import edu.wit.yeatesg.refinedchatserver.packets.ErrorPacket;
import edu.wit.yeatesg.refinedchatserver.packets.ExitPacket;
import edu.wit.yeatesg.refinedchatserver.packets.MessagePacket;
import edu.wit.yeatesg.refinedchatserver.packets.Packet;
import edu.wit.yeatesg.refinedchatserver.userinterfaces.ChatServer.PacketReceiveThread;

public class ChatServer extends Console
{
	private static final Random R = new Random();
	private static final long serialVersionUID = -1455481211578890895L;

	private ServerSocket ss;

	private String serverName;

	private ClientList clientList;
	private ArrayList<Color> availableColors;

	public ChatServer(String name)
	{	
		super();
		
		setTitle(serverName);
		serverName = name;
		clientList = new ClientList();
		availableColors = new ArrayList<>();
		Color[] cols = new Color[] // cols.length == server capacity (each client has a unique color)
		{
			CLIENT_RED, CLIENT_ORANGE, CLIENT_YELLOW,
			CLIENT_LIME, CLIENT_GREEN, CLIENT_TEAL,
			CLIENT_CYAN, CLIENT_BLUE, CLIENT_NAVY_BLUE,
			CLIENT_VIOLET, CLIENT_PURPLE, CLIENT_MAGENTA
		};
		for (Color col : cols)
			availableColors.add(col);
		
		try
		{
			ss = new ServerSocket(8122);
			logToConsole(new MessagePacket("Server Started. Waiting for connections."));

			while (true)
			{
				final Socket s = ss.accept(); // Handle clients in separate threads to allow for multi connecting
				Thread packetReceiveThread = new Thread(new PacketReceiveThread(s));
				packetReceiveThread.start();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("\nSomething went wrong during server socket creation or during serversocket.accept() (server closed?)");
			System.exit(1);
		}
	}

	/**
	 * Every time a new Client is connected, a new thread is created with this as the Runnable. The
	 * reason for using multi-threading is so the server can handle multiple client connections at once
	 * (otherwise the server would only be able to communicate with one client at a time because when a
	 * socket is waiting for a message the code is trapped/interrupted
	 * @author yeatesg
	 *
	 */
	class PacketReceiveThread implements Runnable
	{
		private boolean active = true;
		private Client client;
		private Socket s;

		public PacketReceiveThread(Socket s)
		{
			this.s = s;
		}

		@Override
		public void run()
		{
			try
			{
				DataInputStream inputStream = new DataInputStream(s.getInputStream());
				DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());

				// Obtain the client's request packet, which the server uses to determine whether or not the client can connect
				MessagePacket clientRequest = (MessagePacket) Packet.parsePacket(inputStream.readUTF());		
				String clientName = clientRequest.getSender();

				boolean connectedClientIsDuplicate = clientList.contains(clientName);
				boolean noSpaceLeft = availableColors.size() == 0;

				if (!connectedClientIsDuplicate && !noSpaceLeft)
				{
					client = new Client(clientName, availableColors.remove(R.nextInt(availableColors.size())), s, outputStream, this);
					clientList.add(client);

					MessagePacket responsePacket = new MessagePacket(serverName, ss.getLocalPort() + "", true, client.getClientColor(), Color.BLACK);
					responsePacket.setDataStream(outputStream);
					responsePacket.send();

					logToConsole(new MessagePacket("Accepted a connection from " + s.getRemoteSocketAddress().toString().substring(1)));

					// Now that the client is fully connected we are going to notify all clients, including
					// the one that just connected, by sending the message "<clientName> has joined the server"
					MessagePacket notifyPacket = new MessagePacket(serverName, clientName + " has joined the server", Color.BLUE, Color.BLUE);
					for (DataOutputStream os : clientList.getAllOutputStreams())
					{
						notifyPacket.setDataStream(os);
						notifyPacket.send();
					}
					logToConsole(notifyPacket);

					// Continually receive packets from this client in this while loop
					Packet packetReceiving;
					while (active)
					{
						String input = inputStream.readUTF();
						packetReceiving = Packet.parsePacket(input);
						onPacketReceive(packetReceiving);
					}
				}
				else if (noSpaceLeft) // The server is full
				{
					logToConsole("Denied connection from " + s.getRemoteSocketAddress().toString().substring(1) + " (a client with the same name already exists on this server)", Color.PURPLE);
					ErrorPacket responsePacket = new ErrorPacket("This server is full");
					responsePacket.setDataStream(outputStream);
					responsePacket.send();
				}
				else if (connectedClientIsDuplicate) // There is already a connected client with the same name
				{
					logToConsole("Denied connection from " + s.getRemoteSocketAddress().toString().substring(1) + " (a client with the same name already exists on this server)", Color.PURPLE);
					ErrorPacket responsePacket = new ErrorPacket("A client with the same name already exists on this server");
					responsePacket.setDataStream(outputStream);
					responsePacket.send();	
				}
			}
			catch (Exception e)
			{
				clientList.remove(client);
			}	
		}

		public void stopRunning()
		{
			active = false;
		}
	}

	private void onPacketReceive(Packet packet)
	{
		switch (packet.getClass().getSimpleName())
		{
		case "MessagePacket":

			MessagePacket msgPacket = (MessagePacket) packet;

			// Print message in server console
			logToConsole(msgPacket);

			// Relay message to other clients if it isn't private
			Client sender = clientList.get(msgPacket.getSender());
			if (!msgPacket.isPrivateMessage())
			{
				for (Client connectedClient : clientList)
				{
					if (!connectedClient.equals(sender))
					{
						msgPacket.setDataStream(connectedClient.getOutputStream());
						msgPacket.send();
					}
				}
			}

			// Bounce message back to client so they can see the message they typed
			msgPacket.setDataStream(sender.getOutputStream());
			msgPacket.send();
			break;
		case "ExitPacket":

			ExitPacket exitPacket = (ExitPacket) packet;
			Client exiter = clientList.get(exitPacket.getSender());

			MessagePacket exitMsg = new MessagePacket("You have disconnected from the server. Auto-exiting in 3 seconds...");
			closeConnection(exiter, exitMsg);

			// Notify other clients that the client disconnected
			MessagePacket newMsgPacket = new MessagePacket(serverName, exiter.getClientName() + " has left the server", Color.BLUE, Color.BLUE);
			for (Client connectedClient : clientList)
			{
				newMsgPacket.setDataStream(connectedClient.getOutputStream());
				newMsgPacket.send();
			}
			break;
		}
	}

	@Override
	protected void onSend(String consoleMessage)
	{
		MessagePacket msgPack = new MessagePacket(serverName, consoleMessage, Color.BLACK, Color.BLUE);
		logToConsole(msgPack); // Log packet to display so the server can see what they sent

		switch (consoleMessage.toLowerCase())
		{
		case "/end": case "/exit": case "/quit": // End all connections and shutdown case
			closeAllConnections();
			terminate(1500);
			break;
		case "/kickall": // Remove all clients from server case
			kickAll();
			break;
		default: // Other cases below
			if (consoleMessage.length() >= 7 && consoleMessage.substring(0, 6).toLowerCase().equals("/kick ")) // kick command case
			{
				String clientKickingName = consoleMessage.substring(6);
				if (clientList.contains(clientKickingName))
					kick(clientList.get(clientKickingName));
			}
			else // In this case the server is just sending a regular message to all the clients
			{
				msgPack.setSender(serverName);
				msgPack.sendMultiple(getAllDataStreams());
			}
		}
	}
	
	private void kick(Client kicking)
	{
		if (clientList.contains(kicking))
		{
			MessagePacket youHaveBeenKicked = new MessagePacket(serverName, "You have been kicked from the server!", Color.RED, Color.RED);
			
			closeConnection(kicking, youHaveBeenKicked); // Close connection

			MessagePacket youKickedClient = new MessagePacket("Kicked " + kicking.getClientName() + " from the server.", Color.PURPLE, Color.PURPLE);
			logToConsole(youKickedClient); // Server notifies itself that it kicked a client

			MessagePacket clientWasKicked = new MessagePacket(serverName, kicking.getClientName() + " has been kicked from the server.", Color.BLUE, Color.BLUE);
			clientWasKicked.sendMultiple(getAllDataStreams()); // Notify other clients that a client was kicked
		}
	}
	
	private void kickAll()
	{
		for (Client client : clientList)
			kick(client);
	}
	
	private void closeConnection(Client client, MessagePacket msg)
	{		
		// If a message was supplied for this closeConnection method, send to the client that is being closed
		if (msg != null)
		{
			msg.setDataStream(client.getOutputStream());
			msg.send();
		}

		// Send the client an exit packet
		ExitPacket exitPacket = new ExitPacket(serverName);
		exitPacket.setDataStream(client.getOutputStream());
		exitPacket.send();

		// End client thread, remove client from clientList, log to console that the connection was closed
		client.getThread().stopRunning();
		try
		{ 
			client.getOutputStream().close();
			client.getSocket().close();
		}
		catch (IOException e) { }
		clientList.remove(client);
		logToConsole(client.getClientName() + " has left the server.", Color.BLUE);		
		logToConsole("Closed a socket connection with client \"" + client.getClientName() + "\" because they disconnected", Color.PURPLE);
	}

	private void closeAllConnections()
	{
		MessagePacket msg = new MessagePacket(serverName, "Server Closed", false);
		ArrayList<Client> copyOfClientList = new ArrayList<>();
		for (Client client : clientList)
			copyOfClientList.add(client);
		for (Client client : copyOfClientList)
			closeConnection(client, msg);
	}

	private Collection<DataOutputStream> getAllDataStreams()
	{
		return clientList.getAllOutputStreams();
	}
	
	@Override
	protected void onWindowClose()
	{
		closeAllConnections();
		logToConsole(new MessagePacket("Closing Server"));
		terminate(1000);
	}

	private void terminate(int delay)
	{
		Timer t = new Timer(delay, (ActionEvent e) -> terminate()); // TODO THIS SHOULDNT JUST EXIT THE SYSTEM MAKE THIS DO THE HTING IT SHOULD OD LATER HEAR ME CUNT?
		t.setInitialDelay(delay);
		t.start();
	}

	private void terminate()
	{
		try
		{ 
			ss.close();
			System.exit(0);
		}
		catch (IOException ex) { }
	}
	
	/**
	 * This class is essentially one that the server uses to reference/store information about any client that is connected to
	 * it. Every time a {@link ChatClient} connects to this server, a Client class is created. The client class is used to store
	 * information such as the connected ChatClient's name and color for identification purposes, and for sending/receiving messages.
	 * It also stores information that the server uses for sending/receiving messages, such as a reference to the ChatClient's socket,
	 * outputStream, and thread.
	 * @author yeatesg 
	 *
	 */
	static class Client
	{
		private String clientName;
		private Socket socket;
		private DataOutputStream outputStream;
		private PacketReceiveThread thread;
		private Color clientColor;

		public Client(String clientName, Color clientColor, Socket socket, DataOutputStream os, PacketReceiveThread thread)
		{
			this.clientName = clientName;
			this.socket = socket;
			this.thread = thread;
			this.clientColor = clientColor;
			outputStream = os;
		}

		public String getClientName()
		{
			return clientName;
		}

		public Color getClientColor()
		{
			return clientColor;
		}

		public Socket getSocket()
		{
			return socket;
		}

		public DataOutputStream getOutputStream()
		{
			return outputStream;
		}

		public PacketReceiveThread getThread()
		{
			return thread;
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof Client && ((Client) obj).clientName.equals(clientName);
		}
	}

	class ClientList implements Iterable<Client>
	{
		private ArrayList<Client> internal = new ArrayList<Client>();

		public void add(Client data)
		{
			internal.add(data);
		}

		public Client get(String clientName)
		{
			for (Client data : internal)
				if (data.getClientName().equalsIgnoreCase(clientName))
					return data;
			return null;
		}

		public ArrayList<DataOutputStream> getAllOutputStreams()
		{
			ArrayList<DataOutputStream> list = new ArrayList<DataOutputStream>();
			for (Client data : internal)
				list.add(data.getOutputStream());
			return list;
		}

		public ArrayList<String> getListOfClientNames()
		{
			ArrayList<String> list = new ArrayList<String>();
			for (Client data : internal)
				list.add(data.getClientName());
			return list;
		}

		public boolean contains(Client data)
		{
			return contains(data.getClientName());
		}

		public boolean contains(String clientName)
		{
			for (Client data : internal)
				if (data.getClientName().equalsIgnoreCase(clientName))
					return true;
			return false;
		}

		public boolean remove(String clientName)
		{
			Client remove = null;
			for (Client data : internal)
				if (data.getClientName().equalsIgnoreCase(clientName))
					remove = data;
			if (remove != null)
			{
				internal.remove(remove);
				return true;
			}
			return false;
		}

		public boolean remove(Client client)
		{
			boolean ret = internal.remove(client);
			onRemove(client);
			return ret;
		}

		public void onRemove(Client client)
		{
			if (!availableColors.contains(client.getClientColor()))
				availableColors.add(client.getClientColor());
		}

		@Override
		public Iterator<Client> iterator()
		{
			return internal.iterator();
		}
	}
}