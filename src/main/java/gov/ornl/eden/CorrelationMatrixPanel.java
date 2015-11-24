package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DataModelListener;
import gov.ornl.datatable.Tuple;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class CorrelationMatrixPanel extends JPanel implements
		MouseMotionListener, MouseListener, ComponentListener,
		DataModelListener, RendererListener, WindowListener {
	public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"##0.0###");
	// public static final Font CORR_COEF_FONT = new Font("Dialog", Font.PLAIN,
	// 10);
	public static final Font CORR_COEF_FONT = new Font("Dialog", Font.PLAIN, 12);

	private final Logger log = LoggerFactory
			.getLogger(CorrelationMatrixPanel.class);

	private DataModel dataModel;
	private MatrixRenderer matrixRenderer = null;
	private BufferedImage matrixImage = null;

	private int cellSize;
	private Rectangle matrixRect;
	private int highlightedRow = -1;
	private int highlightedCol = -1;
	private boolean useQueryCorrelations = true;
	private Color coefRectFillColor = new Color(240, 240, 250);

	private Color focusPointColor = PCPanel.DEFAULT_FOCUS_LINE_COLOR;
	private Color contextPointColor = PCPanel.DEFAULT_CONTEXT_LINE_COLOR;

	private ArrayList<Column> matrixColumns;
	private ArrayList<ScatterPlotFrame> scatterplotFrameList = new ArrayList<ScatterPlotFrame>();

	public CorrelationMatrixPanel(DataModel dataModel) {
		this.dataModel = dataModel;
		this.dataModel.addDataModelListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public void setScatterplotFocusPointColor(Color color) {
		focusPointColor = color;
	}

	public void setScaterplotContextPointColor(Color color) {
		contextPointColor = color;
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		matrixColumns = null;
		layoutMatrix();
		startMatrixRenderer();
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		layoutMatrix();
		startMatrixRenderer();
	}

	public void setUseQueryCorrelationCoefficients(boolean useQueryCorrelations) {
		if (this.useQueryCorrelations != useQueryCorrelations) {
			this.useQueryCorrelations = useQueryCorrelations;
			this.startMatrixRenderer();
		}
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	private void layoutMatrix() {
		highlightedRow = highlightedCol = -1;

		if (dataModel.getColumnCount() == 0) {
			return;
		}

		if (matrixColumns == null) {
			matrixColumns = new ArrayList<Column>();

			for (int i = 0; i < dataModel.getColumnCount(); i++) {
				Column column = dataModel.getColumn(i);
				if (column.isEnabled()) {
					matrixColumns.add(column);
				}
			}
		}

		cellSize = Math.min(getWidth() / matrixColumns.size(), getHeight()
				/ matrixColumns.size());
		int width = cellSize * matrixColumns.size();
		int startX = (getWidth() - width) / 2;
		int height = cellSize * matrixColumns.size();
		int startY = 0;

		// cellSize = Math.min(getWidth()/dataModel.getColumnCount(),
		// getHeight()/dataModel.getColumnCount());
		// int width = cellSize * dataModel.getColumnCount();
		// int startX = (getWidth() - width) / 2;
		// int height = cellSize * dataModel.getColumnCount();
		// int startY = 0;

		matrixRect = new Rectangle(startX, startY, width, height);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setFont(CORR_COEF_FONT);

		if (highlightedRow != -1 && highlightedCol != -1) {

			// Column column = dataModel.getColumn(highlightedRow);
			Column column = matrixColumns.get(highlightedRow);
			float corrCoef;

			if (this.useQueryCorrelations && (dataModel.getActiveQuery().getColumnQuerySummaryStats(column) != null)) {
				corrCoef = dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getCorrelationCoefficients().get(highlightedCol);
//				corrCoef = column.getQueryCorrelationCoefficients().get(highlightedCol);
			} else {
				corrCoef = column.getSummaryStats().getCorrelationCoefficients().get(highlightedCol);
			}

			// String labelStr = dataModel.getColumn(highlightedCol).getName() +
			// ", " + column.getName();
			String labelStr = matrixColumns.get(highlightedCol).getName()
					+ ", " + column.getName();
			String corrCoefStr = "r = " + DECIMAL_FORMAT.format(corrCoef);

			int stringWidth1 = g2.getFontMetrics().stringWidth(labelStr);
			int stringWidth2 = g2.getFontMetrics().stringWidth(corrCoefStr);

			int maxStringWidth = Math.max(stringWidth1, stringWidth2);

			int x = matrixRect.width - stringWidth1 - 2;
			int y = g2.getFontMetrics().getHeight() * 2;

			// Rectangle boxRect = new
			// Rectangle(matrixRect.width-maxStringWidth-4, 0, maxStringWidth+4,
			// g2.getFontMetrics().getHeight()*4);
			// g2.setColor(coefRectFillColor);
			// g2.fill(boxRect);
			g2.setColor(Color.DARK_GRAY);
			// g2.drawString(corrCoefStr, boxRect.x+2,
			// boxRect.y+(boxRect.height/4)+(g2.getFontMetrics().getHeight()/2));

			g2.drawString(labelStr, x, y);
			x = matrixRect.width - stringWidth2 - 2;
			y = g2.getFontMetrics().getHeight() * 3;
			g2.drawString(corrCoefStr, x, y);

			g2.fillRect(matrixRect.x + (highlightedCol * cellSize),
					matrixRect.y + (highlightedRow * cellSize), cellSize + 1,
					cellSize + 1);
		}

		// The code below will draw the grid lines between the cells
		/*
		 * if (matrixRect != null) { g2.setColor(Color.gray);
		 * 
		 * for (int ix = matrixRect.x; ix <= matrixRect.x+matrixRect.width; ix
		 * += cellSize) { g2.drawLine(ix, matrixRect.y, ix,
		 * matrixRect.y+matrixRect.height); } for (int iy = matrixRect.y; iy <=
		 * matrixRect.y+matrixRect.height; iy += cellSize) {
		 * g2.drawLine(matrixRect.x, iy, matrixRect.y+matrixRect.width, iy); } }
		 */

		if (matrixImage != null) {
			g2.drawImage(matrixImage, matrixRect.x, matrixRect.y, null);
		}
	}

	private void startMatrixRenderer() {
		if (matrixColumns == null || matrixColumns.isEmpty()) {
			return;
		}

		if (matrixRenderer != null) {
			matrixRenderer.isRunning = false;
			matrixRenderer.removeRendererListener(this);
		}
		matrixRenderer = new MatrixRenderer(dataModel, cellSize, matrixRect,this.useQueryCorrelations, matrixColumns);
		matrixRenderer.addRendererListener(this);
		matrixRenderer.start();
	}

	@Override
	public void rendererFinished(Renderer renderer) {
		if (renderer == matrixRenderer) {
			matrixImage = renderer.getRenderedImage();
			repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			if (event.getClickCount() == 2) {
				// if the mouse is over a row / column intersection, show a
				// scatterplot frame for the two variables
				int row = ((event.getPoint().y - matrixRect.y) / cellSize);
				int column = (event.getPoint().x - matrixRect.x) / cellSize;
				log.debug("mouse clicked at x=" + event.getPoint().x + " y="
						+ event.getPoint().y + " row=" + row + " column="
						+ column);
				log.debug("row variable is " + matrixColumns.get(row).getName());
				log.debug("column variable is "
						+ matrixColumns.get(column).getName());

				if (row > column) {
					log.debug("will plot this one");
					Column xColumn = matrixColumns.get(column);
					Column yColumn = matrixColumns.get(row);
					ScatterplotConfiguration focusConfig = new ScatterplotConfiguration();
					focusConfig.pointColor = focusPointColor;
					focusConfig.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
					focusConfig.showTickMarks = true;
					focusConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);
					ScatterplotConfiguration contextConfig = new ScatterplotConfiguration();
					contextConfig.pointColor = contextPointColor;
					contextConfig.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
					contextConfig.showTickMarks = true;
					contextConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);

					ScatterPlotFrame frame = new ScatterPlotFrame(dataModel,
							xColumn, yColumn, focusConfig, contextConfig);
					frame.setVisible(true);
					frame.setColumns(xColumn, yColumn);
					scatterplotFrameList.add(frame);
					frame.addWindowListener(this);
				}
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
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		if (matrixRect != null) {
			if (matrixRect.contains(event.getPoint())) {
				int row = (int) ((float) (event.getPoint().y - matrixRect.y) / cellSize);
				int col = (int) ((float) (event.getPoint().x - matrixRect.x) / cellSize);

				if (col < row) {
					highlightedRow = row;
					highlightedCol = col;
					// log.debug("matrix cell row = " + row + " col = " + col);
				}
				repaint();
			}
		}
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
		// TODO Handle highlighted column change
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		// layoutMatrix();
		startMatrixRenderer();
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
		startMatrixRenderer();
	}

	@Override
	public void columnDisabled(DataModel dataModel, Column disabledColumn) {
		matrixColumns = null;
		layoutMatrix();
		startMatrixRenderer();
	}

	@Override
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
		matrixColumns = null;
		layoutMatrix();
		startMatrixRenderer();
	}

	@Override
	public void columnEnabled(DataModel dataModel, Column enabledColumn) {
		matrixColumns = null;
		layoutMatrix();
		startMatrixRenderer();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
		if (event.getSource() instanceof ScatterPlotFrame) {
			this.scatterplotFrameList.remove((ScatterPlotFrame) event
					.getSource());
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		startMatrixRenderer();
	}
	
	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		startMatrixRenderer();
	}
}
