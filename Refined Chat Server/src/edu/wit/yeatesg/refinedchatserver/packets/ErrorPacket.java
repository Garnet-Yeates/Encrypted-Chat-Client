package edu.wit.yeatesg.refinedchatserver.packets;

/**
 * When a client receives this packet from the server, the client should close its connection to the server
 * @author yeatesg
 *
 */
public class ErrorPacket extends Packet
{	
	private String errMsg;
	
	public ErrorPacket(String errMsg)
	{
		this.errMsg = errMsg;
	}
	
	public String getErrorMessage()
	{
		return errMsg;
	}
	
	@Override
	public String getUTF()
	{
		return "<ErrorPacket>" + errMsg;		
	}
}
