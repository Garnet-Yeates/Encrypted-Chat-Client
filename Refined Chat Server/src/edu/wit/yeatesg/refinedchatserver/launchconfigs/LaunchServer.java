package edu.wit.yeatesg.refinedchatserver.launchconfigs;

import edu.wit.yeatesg.refinedchatserver.userinterfaces.ChatServer;

public class LaunchServer
{
	public static void main(String[] args)
	{
		Thread serverThread = new Thread(() ->
		{
			new ChatServer("Server");
		});
		
		serverThread.start();
	}
}
