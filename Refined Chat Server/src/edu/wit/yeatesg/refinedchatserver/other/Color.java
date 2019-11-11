package edu.wit.yeatesg.refinedchatserver.other;

public class Color extends java.awt.Color
{
	public static final Color TEAL = new Color(0, 160, 100);
	public static final Color RED = new Color(200, 0, 0);
	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color BLUE = new Color(0, 0, 255);
	public static final Color PURPLE = new Color(153, 5, 227);
	public static final Color DARK_RED = new Color(160, 0, 0);
	public static final Color DARK_GREEN = new Color(0, 190, 0);

	public static final Color CLIENT_RED = new Color(191, 23, 23);
	public static final Color CLIENT_ORANGE = new Color(209, 119, 23);
	public static final Color CLIENT_YELLOW = new Color(191, 175, 31);
	public static final Color CLIENT_LIME = new Color(154, 191, 31);
	public static final Color CLIENT_GREEN = new Color(52, 156, 23);
	public static final Color CLIENT_TEAL = new Color(23, 156, 98);
	public static final Color CLIENT_CYAN = new Color(23, 149, 156);
	public static final Color CLIENT_BLUE = new Color(23, 94, 156);
	public static final Color CLIENT_NAVY_BLUE = new Color(23, 54, 156);
	public static final Color CLIENT_VIOLET = new Color(52, 23, 156);
	public static final Color CLIENT_PURPLE = new Color(118, 23, 156);
	public static final Color CLIENT_MAGENTA = new Color(138, 18, 118);
	
	public static final Color INFO = PURPLE;
	
	public static void main(String[] args) {
		System.out.println(Integer.toString(20, 16));
		System.out.println(Integer.parseInt("A4", 16));
	}
	
	private static final long serialVersionUID = -5596692486886621827L;

	public Color(int r, int g, int b)
	{
		super(r, g, b);
	}

	@Override
	public String toString()
	{
		String r = Integer.toString(getRed(), 16);
		String g = Integer.toString(getGreen(), 16);
		String b = Integer.toString(getBlue(), 16);
		r = r.length() < 2 ? "0" + r : r;
		g = g.length() < 2 ? "0" + g : g;
		b = b.length() < 2 ? "0" + b : b;
		return "#" + r + g + b;
	}
	
	public static Color parseColor(String colorString)
	{
		colorString = colorString.substring(1);
		int r = Integer.parseInt(colorString.substring(0, 2), 16);
		int g = Integer.parseInt(colorString.substring(2, 4), 16);
		int b = Integer.parseInt(colorString.substring(4, 6), 16);
		return new Color(r, g, b);
	}
}
