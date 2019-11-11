package edu.wit.yeatesg.refinedchatserver.packets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

import edu.wit.yeatesg.refinedchatserver.other.Color;

public abstract class Packet
{
	private DataOutputStream outputStream;
	
	public static Packet parsePacket(String utfData)
	{
		int openIndex = utfData.indexOf("<");
		int closeIndex = utfData.indexOf(">");
		if (openIndex == 0 && openIndex < closeIndex && closeIndex != utfData.length() - 1)
		{
			 String packetType = utfData.substring(1, closeIndex);
			 boolean hasPacketData = closeIndex != utfData.length() - 1;
			 String packetData = hasPacketData ? utfData.substring(closeIndex + 1) : null;
			 switch (packetType)
			 {
			 case "MessagePacket":
				 String[] msgPacketParams = packetData.split("`");
				 String msgPacketName = msgPacketParams[0];
				 String msgPacketMsg = msgPacketParams[1];
				 boolean msgPacketIsPrivate = Boolean.parseBoolean(msgPacketParams[2]);
				 Color msgPacketMsgColor = Color.parseColor(msgPacketParams[3]);
				 Color msgPacketSenderColor = Color.parseColor(msgPacketParams[4]);
				 return new MessagePacket(msgPacketName, msgPacketMsg, msgPacketIsPrivate, msgPacketMsgColor, msgPacketSenderColor);
			 case "ExitPacket":
				 return new ExitPacket(packetData);
			 case "ErrorPacket":
				 return new ErrorPacket(packetData);
			 default:
				 throw new RuntimeException("Invalid packet data, packet type cannot be determined");
			 }
		}
		return null;
	}
	
	public void sendMultiple(Collection<DataOutputStream> streams)
	{
		for (DataOutputStream os : streams)
		{
			setDataStream(os);
			send();
		}
	}

	public void send()
	{
		try
		{
			outputStream.writeUTF(getUTF());
		}
		catch (IOException e)
		{
			System.out.println("Something went wrong trying to write a packet in UTF format on an outputstream");
		}
	}
	
	public void setDataStream(DataOutputStream stream)
	{
		outputStream = stream;
	}
	
	public abstract String getUTF();
}
