package gov.ornl.eden;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class ColorIcon implements Icon {
	private static final int WIDTH = 20;
	private static final int HEIGHT = 20;
	private Color color;

	public ColorIcon(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
		g.fillRect(x, y, WIDTH, HEIGHT);
		g.setColor(Color.black);
		g.drawRect(x, y, WIDTH, HEIGHT);
	}

	public int getIconWidth() {
		return WIDTH;
	}

	public int getIconHeight() {
		return HEIGHT;
	}
}
