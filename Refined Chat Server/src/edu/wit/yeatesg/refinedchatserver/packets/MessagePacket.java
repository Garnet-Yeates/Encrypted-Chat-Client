package edu.wit.yeatesg.refinedchatserver.packets;

import java.util.LinkedList;

import edu.wit.yeatesg.refinedchatserver.other.Color;

public class MessagePacket extends Packet
{
	private String sender;
	private String message;
	private Color color;
	private Color senderColor;
	
	private boolean isPrivate;

	public MessagePacket(String sender, String message, boolean isPrivate, Color color, Color senderColor)
	{
		if (sender == null)
			sender = "Info";
		this.sender = sender;
		this.message = message;
		this.isPrivate = isPrivate;
		this.color = color;
		this.senderColor = senderColor;
	}
	
	public MessagePacket(String sender, String message, Color color, Color senderColor)
	{

		this(sender, message, false, color, senderColor);
	}
	
	public MessagePacket(String message, Color color, Color senderColor)
	{
		this(null, message, false, Color.BLACK, Color.INFO);
	}
	
	public MessagePacket(String sender, String message, boolean isPrivate) 
	{
		this(sender, message, isPrivate, Color.BLACK, Color.BLACK);
	}
	
	public MessagePacket(String message, Color color)
	{
		this(null, message, false, color, Color.INFO);
	}
	
	public MessagePacket(String sender, String message)
	{
		this(sender, message, false, Color.BLACK, Color.BLACK);
	}


	public MessagePacket(String message)
	{
		this(null, message, Color.BLACK, Color.INFO);
	}
	

	public boolean isPrivateMessage()
	{
		return isPrivate;
	}

	public void setSender(String newSender)
	{
		sender = newSender;
	}

	public String getSender()
	{
		return sender;
	}

	public void setMessage(String newMsg)
	{
		message = newMsg;
	}

	public String getMessage()
	{
		return message;
	}

	public void setTextColor(Color newColor)
	{
		color = newColor;
	}

	public Color getTextColor()
	{
		return color;
	}

	public Color getPrefixColor()
	{
		return senderColor;
	}

	// hello
	// 01234
	
	public String removeExtraSpaces(String s)
	{
		while (s.substring(s.length() - 1) == " ") // Remove trailing spaces first
		{
			s = s.substring(0, s.length() - 1);
			if (s.length() == 1)
				break;
		}
		
		LinkedList<Character> newString = new LinkedList<>();
		for (int i = 0, j = i + 1; i < s.length(); i++, j++)
		{
			char curr = s.charAt(i);
			char next = j < s.length() ? s.charAt(j) : 't';
			
			if (curr != ' ' || next != ' ')
			{
				newString.add(curr);
			}
			else // curr and next are both ' '.
			{
				while (next == ' ')
				{
					i++;
					j++;
					curr = s.charAt(i);
					next = s.charAt(j); // Should never throw exception here because trailing spaces removed
				}
				newString.add(curr);
			}
		}
		
		char[] charSequence = new char[newString.size()];
		int i = 0;
		for (char c : newString)
		{
			charSequence[i] = c;
			i++;
		}
		s = new String(charSequence);
		
		return s;
	}

	public String getHTMLMessageWithLineBreaks(int maxCharsPerLine, int tabSize)
	{
		String rawMessage = removeExtraSpaces(message);
		
		StringBuilder message = new StringBuilder();

		String prefixCol = fontCol(senderColor);
		String blackCol = fontCol(Color.BLACK);
		String prefix = "<b>" + blackCol + "[" + prefixCol + sender + blackCol + "]</b><br>";

		String tab = tabSize > 0 ? " " : "";
		String nbsp = "";
		for (int i = 0; i < tabSize - 1; i++)
			nbsp += "&nbsp";
		tab = nbsp + tab;

		message.append(prefix);
		int currentSpaceLeft = maxCharsPerLine - tabSize;
		for (int startIndex = 0, endIndex = 0; endIndex < rawMessage.length();)
		{
			String thisLine = "";
			if (currentSpaceLeft == 0) // If we r at last char in line
			{
				currentSpaceLeft = maxCharsPerLine - tabSize;
				int nextStartIndex = endIndex + 1;
				char nextChar = rawMessage.length() >= endIndex + 1 ? rawMessage.charAt(endIndex + 1): ' '; // Rare case, EOL and EOF
				char currChar = rawMessage.charAt(endIndex);
				if (rawMessage.charAt(endIndex) == ' ')
				{
					thisLine = rawMessage.substring(startIndex, endIndex);
				}
				else if (nextChar == ' ')
				{

					thisLine = rawMessage.substring(startIndex, endIndex + 1);
					nextStartIndex++;
				}
				else
				{					
					// Go back till hit space and substring to the space exclusive
					// Startindex shud now space index + 1
					int spaceIndex = endIndex;
					int counter = maxCharsPerLine - tabSize;
					while (currChar != ' ' && counter > 0)
					{
						spaceIndex--;
						currChar = rawMessage.charAt(spaceIndex);
						counter--;	
					}
					if (counter == 0)
						spaceIndex += maxCharsPerLine - tabSize + 1;
					thisLine = rawMessage.substring(startIndex, spaceIndex);
					nextStartIndex = spaceIndex + 1 - (counter == 0 ? 1: 0);
					endIndex = nextStartIndex;
				}
				
				startIndex = nextStartIndex;
								
				message.append(tab + thisLine + "<br>");
				continue;

			}
			else if (endIndex == rawMessage.length() - 1)
			{
				thisLine = rawMessage.substring(startIndex, rawMessage.length());
				message.append(tab + thisLine + "<br>");
				break;
			}
			
			currentSpaceLeft--;
			endIndex++;
		}
		return message.toString();
	}

	public static String fontCol(Color col)
	{
		return "<font color=\"" + col + "\">";
	}

	@Override
	public String getUTF()
	{
		return "<MessagePacket>" + sender + "`" + message + "`" + isPrivate + "`" + color + "`" + senderColor;
	}
}