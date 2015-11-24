package gov.ornl.eden;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class NewGridPanel extends JPanel implements ComponentListener,
		MouseListener, MouseMotionListener, MouseWheelListener {
	private final Logger log = LoggerFactory.getLogger(NewGridPanel.class);

	private static final int AREA_SELECTED_EVENT_ID = 0;
	private int rows;
	private int cols;
	private double gridSpacing;

	private Point startDragPoint = new Point();
	private Point endDragPoint = new Point();
	private boolean dragging = false;
	private Rectangle dragRect;

	private Rectangle gridRect = new Rectangle();
	private Rectangle visibleRect = new Rectangle();
	private Rectangle selectedRect;
	private boolean selectionEnabled = false;

	private Color selectedAreaColor = new Color(0, 103, 253);
	private Color gridLineColor = new Color(220, 220, 220);
	private Color gridFillColor = new Color(250, 250, 250);
	private Color gridBorderColor = new Color(150, 150, 150);
	private Color queryBoxOutlineColor = new Color(50, 50, 50);
	private Color queryBoxFillColor = new Color(252, 248, 137, 150);

	private BufferedImage backgroundImage = null;

	private ArrayList<NewGridPanelListener> listeners = new ArrayList<NewGridPanelListener>();

	private boolean showGraticule = true;
	private int zoomFactor = 1;

	public NewGridPanel(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		setDoubleBuffered(true);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);
	}

	public void setShowGraticule(boolean enabled) {
		if (enabled != showGraticule) {
			showGraticule = enabled;
			repaint();
		}
	}

	public boolean getShowGraticule() {
		return showGraticule;
	}

	public void addNewGridPanelListener(NewGridPanelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeNewGridPanelListener(NewGridPanelListener listener) {
		listeners.remove(listener);
	}

	private void fireAreaSelected() {
		for (NewGridPanelListener listener : listeners) {
			listener.gridAreaSelected();
		}
	}

	public void clearSelectedRectangle() {
		this.selectedRect = null;
		this.selectionEnabled = false;
		repaint();
		this.fireAreaSelected();
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public double getGridSpacing() {
		return gridSpacing;
	}

	public Rectangle getSelectedRect() {
		// Rectangle rect = new Rectangle((selectedRect.x*gridSpacing),
		// (selectedRect.y*gridSpacing), selectedRect.width*gridSpacing,
		// selectedRect.height*gridSpacing);
		// return rect;
		if (selectedRect != null) {
			return (Rectangle) selectedRect.clone();
		}
		return null;
	}

	private void layoutMap() {

	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		double gridXSpacing = (double) getWidth() / cols;
		double gridYSpacing = (double) getHeight() / rows;

		gridSpacing = Math.min(gridXSpacing, gridYSpacing);

		int width = (int) (gridSpacing * cols);
		int startX = (getWidth() - width) / 2;

		int height = (int) (gridSpacing * rows);
		int startY = (getHeight() - height) / 2;

		gridRect = new Rectangle(startX, startY, width - 1, height - 1);

		if (backgroundImage != null) {
			g2.drawImage(backgroundImage, startX, startY, width, height, null);
		} else {
			g2.setColor(gridFillColor);
			g2.fill(gridRect);
		}

		g2.translate(gridRect.x, gridRect.y);
		if (selectionEnabled) {
			// g2.setColor(selectedAreaColor);
			g2.setColor(queryBoxFillColor);
			g2.fillRect((int) (selectedRect.x * gridSpacing),
					(int) (selectedRect.y * gridSpacing),
					(int) (selectedRect.width * gridSpacing),
					(int) (selectedRect.height * gridSpacing));
			g2.setColor(queryBoxOutlineColor);
			g2.drawRect((int) (selectedRect.x * gridSpacing),
					(int) (selectedRect.y * gridSpacing),
					(int) (selectedRect.width * gridSpacing),
					(int) (selectedRect.height * gridSpacing));
		}

		// if (showGraticule) {
		// g2.setColor(gridLineColor);
		// for (int ix = gridSpacing; ix < width; ix += gridSpacing) {
		// g2.drawLine(ix, 0, ix, height);
		// }
		// for (int iy = gridSpacing; iy < height; iy += gridSpacing) {
		// g2.drawLine(0, iy, width, iy);
		// }
		// }

		g2.translate(-gridRect.x, -gridRect.y);

		g2.setColor(gridBorderColor);
		g2.draw(gridRect);

		if (dragging) {
			// g2.setXORMode(getBackground());
			g2.setColor(queryBoxFillColor);
			g2.fill(dragRect);
			g2.setColor(queryBoxOutlineColor);
			g2.draw(dragRect);
		}
	}

	public void setBasemapImage(BufferedImage image) {
		backgroundImage = image;
		repaint();
	}

	public static void main(String args[]) throws Exception {
		JFrame frame = new JFrame();

		NewGridPanel map = new NewGridPanel(180, 360);
		map.setPreferredSize(new Dimension(800, 400));

		BufferedImage backgroundImage = ImageIO.read(new File(
				"resources/rasters/HYP_50M_SR_W/HYP_50M_SR_W.png"));
		map.setBasemapImage(backgroundImage);
		map.setShowGraticule(false);

		JPanel framePanel = (JPanel) frame.getContentPane();
		framePanel.setLayout(new BorderLayout());
		framePanel.add(map, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		// if (dragging) {
		dragging = true;
		endDragPoint.setLocation(event.getPoint());
		dragRect = new Rectangle(
				startDragPoint.x < endDragPoint.x ? startDragPoint.x
						: endDragPoint.x,
				startDragPoint.y < endDragPoint.y ? startDragPoint.y
						: endDragPoint.y, Math.abs(startDragPoint.x
						- endDragPoint.x), Math.abs(startDragPoint.y
						- endDragPoint.y));

		repaint();
		// }
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			if (event.getClickCount() == 1) {
				this.clearSelectedRectangle();
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
		startDragPoint.setLocation(event.getPoint());
	}

	public void setSelectedRectangle(int left, int right, int bottom, int top) {
		log.debug("left=" + left + " right=" + right + " bottom=" + bottom
				+ " top=" + top);
		selectedRect = new Rectangle(left, top, right - left, bottom - top);
		selectionEnabled = true;

		log.debug("selectedRect: " + selectedRect);
		repaint();
		fireAreaSelected();
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (dragging) {
			dragging = false;

			Rectangle adjustedDragRect = ((Rectangle) dragRect.clone());
			adjustedDragRect.translate(-gridRect.x, -gridRect.y);

			int selectRectLeft = (int) (adjustedDragRect.x / gridSpacing);
			int selectRectRight = (int) (((adjustedDragRect.x + adjustedDragRect.width) / gridSpacing) + 1);
			int selectRectBottom = (int) (adjustedDragRect.y / gridSpacing);
			int selectRectTop = (int) (((adjustedDragRect.y + adjustedDragRect.height) / gridSpacing) + 1);
			selectedRect = new Rectangle(selectRectLeft, selectRectBottom,
					selectRectRight - selectRectLeft, selectRectTop
							- selectRectBottom);
			selectionEnabled = true;

			log.debug("selectedRect: " + selectedRect);

			Graphics2D g2 = (Graphics2D) getGraphics();
			g2.setXORMode(getBackground());
			g2.draw(dragRect);

			repaint();
			fireAreaSelected();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}
}
