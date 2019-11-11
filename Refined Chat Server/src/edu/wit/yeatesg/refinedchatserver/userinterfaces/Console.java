package edu.wit.yeatesg.refinedchatserver.userinterfaces;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;

import edu.wit.yeatesg.refinedchatserver.other.Color;
import edu.wit.yeatesg.refinedchatserver.packets.MessagePacket;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.JTextField;

import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;

public abstract class Console extends JFrame
{
	private static final long serialVersionUID = -7875757212589746372L;

	protected JPanel innerPanel;
	protected JScrollPane scrollPane;
	protected JTextPane displayPane;
	protected JTextField inputField;

	public Console()
	{	
		setLookAndFeel();
		initFrame();
		setVisible(true);
	}

	protected abstract void onSend(String consoleMessage);

	protected abstract void onWindowClose();

	private int line_length = 40;

	StringBuilder consoleText = new StringBuilder("<html></html>");

	protected void logToConsole(String string)
	{
		logToConsole(new MessagePacket(null, string, false));
	}

	protected void logToConsole(MessagePacket packet)
	{
		JScrollBar vertical = scrollPane.getVerticalScrollBar();

		String string = packet.getHTMLMessageWithLineBreaks(line_length, 2);
		Color textColor = packet.getTextColor();
		consoleText = consoleText.replace(consoleText.length() - 7 , consoleText.length(), ""); // "</html>".length = 7		
		String textStyle = "<font face=\"consolas\" color=\"" + textColor + "\" size=5>";
		consoleText.append(textStyle + string + "</html>");
		displayPane.setText(consoleText + "");
		displayPane.setContentType("text/html");

		System.out.println(inputField.hasFocus());
		if (!displayPane.hasFocus())
		{
			EventQueue.invokeLater(() ->
			{
				vertical.setValue(vertical.getMaximum());
			});
		}	
	}

	protected void logToConsole(String msg, Color col)
	{
		logToConsole(new MessagePacket(null, msg, col, Color.INFO));
	}

	public void initFrame()
	{
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 502, 347);

		addWindowListener(new WindowListener()
		{	
			@Override
			public void windowClosing(WindowEvent e)
			{
				onWindowClose();
			}

			public void windowOpened(WindowEvent arg0) { }			
			public void windowIconified(WindowEvent arg0) { }
			public void windowDeiconified(WindowEvent arg0) { }
			public void windowDeactivated(WindowEvent arg0) { }
			public void windowClosed(WindowEvent arg0) { }
			public void windowActivated(WindowEvent arg0) { }
		});

		innerPanel = new JPanel();
		innerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(innerPanel);
		innerPanel.setLayout(null);

		scrollPane = new JScrollPane(displayPane = new JTextPane());
		displayPane.setContentType("text/html");
		scrollPane.setBounds(10, 10, 466, 254);
		innerPanel.add(scrollPane);

		inputField = new JTextField();
		inputField.setBounds(10, 275, 385, 20);
		inputField.setColumns(10);
		inputField.addKeyListener(new KeyListener()
		{	
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					attemptSend(inputField.getText());
				}
			}

			public void keyTyped(KeyEvent e) { }
			public void keyReleased(KeyEvent e) { }
		});
		innerPanel.add(inputField);

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener((e) ->
		{
			attemptSend(inputField.getText());
		});
		sendButton.setBounds(398, 274, 78, 23);
		innerPanel.add(sendButton);
	}

	public void attemptSend(String text)
	{
		text = text.replaceAll("`", "");
		if (text != null && text != "" && !containsOnlySpaces(text))
		{
			text = removeTrailingSpaces(text);
			onSend(text);	
		}
		inputField.setText("");
	}

	public static String removeTrailingSpaces(String text)
	{
		while (text.substring(text.length() - 1).equals(" "))
			text = text.substring(0, text.length() - 1);
		return text;
	}

	public static boolean containsOnlySpaces(String s)
	{
		for (char c : s.toCharArray())
			if (c != ' ')
				return false;
		return true;
	}

	public static void setLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
