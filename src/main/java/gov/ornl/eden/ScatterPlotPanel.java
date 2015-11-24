package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DataModelListener;
import gov.ornl.datatable.Tuple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterPlotPanel extends JComponent implements MouseMotionListener,
		MouseListener, ComponentListener, DataModelListener, RendererListener {
	private final Logger log = LoggerFactory.getLogger(ScatterPlotPanel.class);

	public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"##0.0###");

	private final static int BORDER = 2;
	private final static int BORDER2 = BORDER * 2;
	private final static int AXIS_SIZE = 40;

	private BufferedImage axesImage = null;
	private BufferedImage pointsImage = null;
	private BufferedImage filteredPointsImage = null;

	private DataModel dataModel;
	private Column xColumn;
	private Column yColumn;

	private boolean showFilteredData = true;
	private int panelWidth;
	private int panelHeight;
	private Rectangle plotRectangle;

	private int plotSize;

	private boolean mousePressed = false;

	private ScatterplotPointsRenderer plotRenderer;
	private ScatterplotPointsRenderer filteredPlotRenderer;

	private int plotOffsetY;
	private int plotOffsetX;

	private Point mousePoint = null;
	private Point startDragPoint = new Point();
	private Point endDragPoint = new Point();
	private boolean dragging = false;
	private Rectangle dragRect;

	ScatterplotConfiguration queryScatterPlotConfig = new ScatterplotConfiguration();
	ScatterplotConfiguration nonqueryScatterPlotConfig = new ScatterplotConfiguration();

	private ScatterplotAxesRenderer axesPlotRenderer;
	private SimpleRegression simpleRegression;
	private int axisBufferSize;
	private int axisSize;
	private Rectangle centeredRectangle;

	boolean showCorrelationIndicator = true;
	boolean useQueryCorrelation = true;
	boolean showRegressionLine = true;
	boolean useQueryRegressionLine = true;
	boolean antialiasEnabled = true;
	
	PCAxisSelection xAxisSelection;
	PCAxisSelection yAxisSelection;

	public ScatterPlotPanel(DataModel dataModel,
			ScatterplotConfiguration queryScatterPlotConfig,
			ScatterplotConfiguration nonqueryScatterPlotConfig) {
		this.dataModel = dataModel;
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		dataModel.addDataModelListener(this);

		this.queryScatterPlotConfig = queryScatterPlotConfig;
		this.nonqueryScatterPlotConfig = nonqueryScatterPlotConfig;

		showCorrelationIndicator = queryScatterPlotConfig.showCorrelationIndicator;
		showRegressionLine = queryScatterPlotConfig.showRegressionLine;
		useQueryCorrelation = queryScatterPlotConfig.useQueryCorrelationCoefficient;
		useQueryRegressionLine = queryScatterPlotConfig.useQueryRegressionLine;
	}

	public void setAntialiasEnabled(boolean antialiasEnabled) {
		if (this.antialiasEnabled != antialiasEnabled) {
			this.antialiasEnabled = antialiasEnabled;
			startScatterplotRenderer();
		}
	}

	public void setUseQueryCorrelation(boolean useQueryCorrelation) {
		if (this.useQueryCorrelation != useQueryCorrelation) {
			this.useQueryCorrelation = useQueryCorrelation;
			queryScatterPlotConfig.useQueryCorrelationCoefficient = useQueryCorrelation;
			startScatterplotRenderer();
		}
	}

	public boolean getUseQueryCorrelation() {
		return useQueryCorrelation;
	}

	public void setShowCorrelationIndicator(boolean showCorrelationIndicator) {
		if (this.showCorrelationIndicator != showCorrelationIndicator) {
			this.showCorrelationIndicator = showCorrelationIndicator;
			queryScatterPlotConfig.showCorrelationIndicator = showCorrelationIndicator;
			startScatterplotRenderer();
		}
	}

	public boolean getShowCorrelationIndicator() {
		return showCorrelationIndicator;
	}

	public void setShowRegressionLine(boolean showRegressionLine) {
		if (this.showRegressionLine != showRegressionLine) {
			this.showRegressionLine = showRegressionLine;
			queryScatterPlotConfig.showRegressionLine = showRegressionLine;
			calculateStatistics();
			startScatterplotRenderer();
		}
	}

	public boolean getShowRegressionLine() {
		return showRegressionLine;
	}

	public void setUseQueryRegressionLine(boolean useQueryRegressionLine) {
		if (this.useQueryRegressionLine != useQueryRegressionLine) {
			this.useQueryRegressionLine = useQueryRegressionLine;
			queryScatterPlotConfig.useQueryRegressionLine = useQueryRegressionLine;
			calculateStatistics();
			startScatterplotRenderer();
		}
	}

	public boolean getUseQueryRegressionLine() {
		return useQueryRegressionLine;
	}

	public void setShowingFilteredData(boolean showFilteredData) {
		if (this.showFilteredData != showFilteredData) {
			this.showFilteredData = showFilteredData;
			startScatterplotRenderer();
		}
	}

	public boolean getShowingFilteredData() {
		return showFilteredData;
	}

	// public void setUseQueryCorrelationCoefficient(boolean
	// useQueryCorrelation) {
	// this.useQueryCorrelation = useQueryCorrelation;
	// startScatterplotRenderer();
	// }

	public void setPointAlphaValue(int alphaValue) {
		if (alphaValue >= 0 && alphaValue <= 255) {
			Color pointColor = new Color(
					queryScatterPlotConfig.pointColor.getRed(),
					queryScatterPlotConfig.pointColor.getGreen(),
					queryScatterPlotConfig.pointColor.getBlue(), alphaValue);
			queryScatterPlotConfig.pointColor = pointColor;

			pointColor = new Color(
					nonqueryScatterPlotConfig.pointColor.getRed(),
					nonqueryScatterPlotConfig.pointColor.getGreen(),
					nonqueryScatterPlotConfig.pointColor.getBlue(), alphaValue);
			nonqueryScatterPlotConfig.pointColor = pointColor;

			startScatterplotRenderer();
		}
	}

	public void setFocusShapeColor(Color color) {
		queryScatterPlotConfig.pointColor = color;
		startScatterplotRenderer();
	}

	public void setContextShapeColor(Color color) {
		nonqueryScatterPlotConfig.pointColor = color;
		startScatterplotRenderer();
	}

	public void setAxes(Column xColumn, Column yColumn) {
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		layoutPlot();
		calculateStatistics();
		startScatterplotRenderer();
	}

	private void calculateStatistics() {
		if (xColumn != null && yColumn != null) {
			simpleRegression = new SimpleRegression();
			int xColumnIdx = dataModel.getColumnIndex(xColumn);
			int yColumnIdx = dataModel.getColumnIndex(yColumn);

			for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
				Tuple tuple = dataModel.getTuple(iTuple);

				if (useQueryRegressionLine && tuple.getQueryFlag()) {
					simpleRegression.addData(tuple.getElement(xColumnIdx),
							tuple.getElement(yColumnIdx));
				} else if (!useQueryRegressionLine) {
					simpleRegression.addData(tuple.getElement(xColumnIdx),
							tuple.getElement(yColumnIdx));
				}
			}
		}
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (xColumn == null && yColumn == null) {
			return;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setStroke(new BasicStroke(2.f));

		if (filteredPointsImage != null && showFilteredData) {
			g2.drawImage(filteredPointsImage, plotRectangle.x, plotRectangle.y,
					this);
		}
		if (pointsImage != null) {
			g2.drawImage(pointsImage, plotRectangle.x, plotRectangle.y, this);
		}
		if (axesImage != null) {
			g2.drawImage(axesImage, centeredRectangle.x, centeredRectangle.y,
					this);
		}

		if (mousePoint != null) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(plotRectangle.x, mousePoint.y, plotRectangle.x
					+ plotRectangle.width, mousePoint.y);
			g2.drawLine(mousePoint.x, plotRectangle.y, mousePoint.x,
					plotRectangle.y + plotRectangle.height);
		}

		if (dragging) {
			g2.setXORMode(getBackground());
			g2.draw(dragRect);
		}

//		 g2.setColor(Color.blue);
//		 g2.draw(plotRectangle);
//		 g2.setColor(Color.red);
//		 g2.draw(centeredRectangle);
	}

	private void layoutPlot() {
		panelHeight = getHeight() - 1;
		panelWidth = getWidth() - 1;

		// calculate size of the axis (for left and bottom axes)
		// this covers the label and ticks, plus the border size
		axisSize = AXIS_SIZE;

		plotSize = Math.min(panelHeight, panelWidth);
		plotSize = plotSize - (BORDER2);

		plotOffsetY = (int) ((double) (panelHeight - plotSize) / 2.);
		plotOffsetX = (int) ((double) (panelWidth - plotSize) / 2.);

		centeredRectangle = new Rectangle(plotOffsetX, plotOffsetY, plotSize,
				plotSize);
		plotRectangle = new Rectangle(plotOffsetX + axisSize, plotOffsetY,
				plotSize - axisSize, plotSize - axisSize);
	}

	@Override
	public void rendererFinished(Renderer renderer) {
		if (renderer == plotRenderer) {
			// if (renderer instanceof ScatterplotPointsRenderer) {
			pointsImage = renderer.getRenderedImage();

			// log.debug("Renderer thread " + renderer.getId() + " finished");
			// try {
			// ImageIO.write(pointsImage, "png", new
			// File("/Users/csg/Desktop/mustangImages/img" + renderer.getId() +
			// ".png"));
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			repaint();
		} else if (renderer == filteredPlotRenderer) {
			filteredPointsImage = renderer.getRenderedImage();
			repaint();
		} else if (renderer == axesPlotRenderer) {
			axesImage = renderer.getRenderedImage();
			repaint();
		}
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		// axesImage = filteredPointsImage = pointsImage = null;
		// layoutPlot();
		// recalculatePoints();
		// Columns may have been re-created
		// if (xColumn != null) {
		// int icol = dataModel.getColumnIndex(xColumn);
		// if (icol == -1) {
		// xColumn = dataModel.getColumn(xColumn.getName());
		// }
		// }
		//
		// if (yColumn != null) {
		// int icol = dataModel.getColumnIndex(yColumn);
		// if (icol != -1) {
		// yColumn = dataModel.getColumn(yColumn.getName());
		// }
		// }
		//
		// layoutPlot();
		calculateStatistics();
		startScatterplotRenderer();
	}

	private void startScatterplotRenderer() {
		if (xColumn == null || yColumn == null || plotSize == 0) {
			return;
		}

		if (plotRenderer != null) {
			plotRenderer.isRunning = false;
			plotRenderer.removeRendererListener(this);
		}

		plotRenderer = new ScatterplotPointsRenderer(dataModel, xColumn,
				yColumn, plotRectangle.width+axisSize, axisSize, queryScatterPlotConfig,
				simpleRegression, true, false, antialiasEnabled);
		plotRenderer.addRendererListener(this);
		plotRenderer.start();

		if (filteredPlotRenderer != null) {
			filteredPlotRenderer.isRunning = false;
			filteredPlotRenderer.removeRendererListener(this);
		}

		filteredPlotRenderer = new ScatterplotPointsRenderer(dataModel,
				xColumn, yColumn, plotRectangle.width+axisSize, axisSize,
				nonqueryScatterPlotConfig, simpleRegression, false, true,
				antialiasEnabled);
		filteredPlotRenderer.addRendererListener(this);
		filteredPlotRenderer.start();

		if (axesPlotRenderer != null) {
			axesPlotRenderer.isRunning = false;
			axesPlotRenderer.removeRendererListener(this);
		}

		axesPlotRenderer = new ScatterplotAxesRenderer(dataModel, xColumn,
				yColumn, plotRectangle.width+axisSize, axisSize, queryScatterPlotConfig,
				antialiasEnabled);
		axesPlotRenderer.addRendererListener(this);
		axesPlotRenderer.start();
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		// axesImage = filteredPointsImage = pointsImage = null;
		layoutPlot();
		startScatterplotRenderer();
	}

	@Override
	public void componentShown(ComponentEvent event) {
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			// TODO: Reenable mouse selections
//			if (xColumn.isQuerySet()) {
//				xColumn.setQueryFlag(false);
//				xColumn.setMaxQueryValue(xColumn.getMaxValue());
//				xColumn.setMinQueryValue(xColumn.getMinValue());
//			}
//			if (yColumn.isQuerySet()) {
//				yColumn.setQueryFlag(false);
//				yColumn.setMaxQueryValue(yColumn.getMaxValue());
//				yColumn.setMinQueryValue(yColumn.getMinValue());
//			}
			dataModel.setQueriedTuples();
			dragRect = null;
			startScatterplotRenderer();
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
		startDragPoint.setLocation(event.getPoint());
		if (xAxisSelection != null) {
			dataModel.clearColumnSelectionRange(xAxisSelection.getColumnSelectionRange());
			xAxisSelection = null;
		}
		if (yAxisSelection != null) {
			dataModel.clearColumnSelectionRange(yAxisSelection.getColumnSelectionRange());
			yAxisSelection = null;
		}
		dataModel.setQueriedTuples();
		startScatterplotRenderer();
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (dragging) {
			dragging = false;

			Graphics2D g2 = (Graphics2D) getGraphics();
			g2.setXORMode(getBackground());
			g2.draw(dragRect);

			updateQueryPoints();

			repaint();
		}
	}

	private float XPositionToValue(int x) {
		float normVal = (float) (x - plotRectangle.x) / (float) (plotRectangle.width);
		float val = xColumn.getSummaryStats().getMin() + (normVal * (xColumn.getSummaryStats().getMax() - xColumn.getSummaryStats().getMin()));
		return val;
	}

	private float YPositionToValue(int y) {
		float normVal = (float) (y - plotRectangle.y) / (float) (plotRectangle.height);
		float val = yColumn.getSummaryStats().getMax() - (normVal * (yColumn.getSummaryStats().getMax() - yColumn.getSummaryStats().getMin()));
		return val;
	}

	private void updateQueryPoints() {
		float minXQueryValue = XPositionToValue(dragRect.x);
		float maxXQueryValue = XPositionToValue(dragRect.x + dragRect.width);
		float maxYQueryValue = YPositionToValue(dragRect.y);
		float minYQueryValue = YPositionToValue(dragRect.y + dragRect.height);
		log.debug("minXValue = " + minXQueryValue + " maxXValue = " + maxXQueryValue);
		log.debug("minYValue = " + minYQueryValue + " maxYValue = " + maxYQueryValue);
		
		// set a column selection for x and y columns
		ColumnSelectionRange xSelectionRange = dataModel.addColumnSelectionRangeToActiveQuery(xColumn, minXQueryValue, maxXQueryValue);
		xAxisSelection = new PCAxisSelection(xSelectionRange);
		ColumnSelectionRange ySelectionRange = dataModel.addColumnSelectionRangeToActiveQuery(yColumn, minYQueryValue, maxYQueryValue);
		yAxisSelection = new PCAxisSelection(ySelectionRange);
		
//		xColumn.setMinQueryValue(XPositionToValue(dragRect.x));
//		xColumn.setMaxQueryValue(XPositionToValue(dragRect.x + dragRect.width));
//		xColumn.setQueryFlag(true);
//		yColumn.setMaxQueryValue(YPositionToValue(dragRect.y));
//		yColumn.setMinQueryValue(YPositionToValue(dragRect.y + dragRect.height));
//		yColumn.setQueryFlag(true);
//		log.debug("query updated xmax=" + xColumn.getMaxQueryValue() + " xmin="
//				+ xColumn.getMinQueryValue() + " ymax="
//				+ yColumn.getMaxQueryValue() + " ymin="
//				+ yColumn.getMinQueryValue());
		dataModel.setQueriedTuples();
		startScatterplotRenderer();
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		dragging = true;
		endDragPoint.setLocation(event.getPoint());
		dragRect = new Rectangle(
				startDragPoint.x < endDragPoint.x ? startDragPoint.x
						: endDragPoint.x,
				startDragPoint.y < endDragPoint.y ? startDragPoint.y
						: endDragPoint.y, Math.abs(startDragPoint.x
						- endDragPoint.x), Math.abs(startDragPoint.y
						- endDragPoint.y));
		setMousePoint(event.getPoint());
//		updateQueryPoints();
		repaint();
	}

	private void setMousePoint(Point point) {
		if (mousePoint == null) {
			mousePoint = new Point(point.x, point.y);
		}

		if (point.x < plotRectangle.x) {
			mousePoint.x = plotRectangle.x;
		} else if (point.x > (plotRectangle.x + plotRectangle.width)) {
			mousePoint.x = (plotRectangle.x + plotRectangle.width);
		} else {
			mousePoint.x = point.x;
		}

		if (point.y < plotRectangle.y) {
			mousePoint.y = plotRectangle.y;
		} else if (point.y > (plotRectangle.y + plotRectangle.height)) {
			mousePoint.y = (plotRectangle.y + plotRectangle.height);
		} else {
			mousePoint.y = point.y;
		}

		// float xVal = XPositionToValue(mousePoint.x);
		// float yVal = YPositionToValue(mousePoint.y);
		// log.debug("mouse x="+ xVal + " y="+ yVal);
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		setMousePoint(event.getPoint());
		// mousePoint = new Point(event.getX(), event.getY());
		//
		// if (event.getX() < plotRectangle.x) {
		// mousePoint.x = plotRectangle.x;
		// } else if (event.getX() > (plotRectangle.x + plotRectangle.width)) {
		// mousePoint.x = (plotRectangle.x + plotRectangle.width);
		// } else {
		// mousePoint.x = event.getX();
		// }
		//
		// if (event.getY() < plotRectangle.y) {
		// mousePoint.y = plotRectangle.y;
		// } else if (event.getY() > (plotRectangle.y + plotRectangle.height)) {
		// mousePoint.y = (plotRectangle.y + plotRectangle.height);
		// } else {
		// mousePoint.y = event.getY();
		// }

		repaint();
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		// layoutPlot();
		// recalculatePoints();
		calculateStatistics();
		startScatterplotRenderer();
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
		// TODO Auto-generated method stub
		calculateStatistics();
		startScatterplotRenderer();
	}

	@Override
	public void columnDisabled(DataModel dataModel, Column disabledColumn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void columnEnabled(DataModel dataModel, Column enabledColumn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		calculateStatistics();
		startScatterplotRenderer();
	}
	
	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		calculateStatistics();
		startScatterplotRenderer();
	}
}
