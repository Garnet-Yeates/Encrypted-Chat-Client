package edu.wit.yeatesg.refinedchatserver.launchconfigs;

import edu.wit.yeatesg.refinedchatserver.userinterfaces.ConnectClient;

public class LaunchClient
{
	public static void main(String[] args)
	{
		Thread clientThread = new Thread(() ->
		{
			new ConnectClient();
		});
		
		clientThread.start();
	}
}