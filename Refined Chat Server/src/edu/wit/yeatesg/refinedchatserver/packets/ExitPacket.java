package edu.wit.yeatesg.refinedchatserver.packets;

public class ExitPacket extends Packet
{
	private String sender;
	
	public ExitPacket(String sender)
	{
		this.sender = sender;
	}
	
	public String getSender()
	{
		return sender;
	}
	
	@Override
	public String getUTF()
	{
		return "<ExitPacket>" + sender;
	}

}
