package gov.ornl.csed.cda.pcvis;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.ColumnSelectionRange;
import gov.ornl.csed.cda.datatable.ColumnSortRecord;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.DataModelListener;
import gov.ornl.csed.cda.datatable.Histogram;
import gov.ornl.csed.cda.datatable.Histogram.BinInfo;
import gov.ornl.csed.cda.datatable.SummaryStats;
import gov.ornl.csed.cda.datatable.Tuple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCPanel extends JComponent implements ActionListener,
		RendererListener, DataModelListener, ComponentListener, MouseListener,
		MouseMotionListener, WindowListener {
	private final Logger log = LoggerFactory.getLogger(PCPanel.class);
	private final int DELAY = 100;
	public static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 10);

	public static final int MEAN_DISPERSION_BOX_MODE = 0;
	public static final int MEDIAN_DISPERSION_BOX_MODE = 1;

	public static final int RENDER_TUPLES_AS_DOTS = 0;
	public static final int RENDER_TUPLES_AS_LINES = 1;

	public final static Color DEFAULT_CONTEXT_LINE_COLOR = new Color(210, 210, 210, 40);
	public final static Color DEFAULT_FOCUS_LINE_COLOR = new Color(20, 20, 80, 40);
	private final static int BORDER_SIZE = 5;
	// private final static int AXIS_WIDTH = 30;
	private final static int AXIS_BAR_WIDTH = 20;
	private final static int AXIS_BAR_WIDTH_WITH_FREQUENCY = AXIS_BAR_WIDTH * 3;
	private final static int QUERY_BOX_WIDTH = AXIS_BAR_WIDTH;
	private final static Color QUERY_RANGE_LABEL_BACKGROUND = new Color(230, 230, 250, 140);

	private final static int MIN_AXIS_SPACING = 80;

	private int axisSpacing;
	private int axisHeight;
	private int axisTop;
	private int axisBottom;
    private int axisFocusHeight;
    private int axisFocusTop;
    private int axisFocusBottom;
	private int screenWidth;
	private int screenHeight;
	// private int rangeLabelSize = 30;
	private int scatterplotSize;
	private int axisBarWidth = AXIS_BAR_WIDTH;
	private boolean showFilteredData = true;
	private boolean showAxesAsBars = false;
	private boolean drawQueryLimits = true;
	private boolean showFrequencyInfo = false;
	private BufferedImage axesImage = null;
	private BufferedImage contextImage = null;
	private BufferedImage focusImage = null;
	private Color queryBoxOutlineColor = new Color(50, 50, 50);
	private Color queryBoxDisabledFillColor = new Color(100, 100, 100, 50);
	private Color queryBoxEnabledFillColor = new Color(252, 248, 137, 80);
	private Color queryLineColor = new Color(20, 20, 80);

	private ArrayList<PCAxis> axisList;
	private PCAxesRenderer axesRenderer = null;
	private PCLineRenderer contextLineRenderer = null;
	private PCLineRenderer focusLineRenderer = null;
	private DataModel dataModel;
	private Point startDragPoint = new Point(), endDragPoint = new Point();
	private boolean dragging = false;
	private Rectangle dragRect;
	private PCAxis mouseOverAxis = null;
	private PCAxis mouseOverLabelAxis = null;
	private boolean mouseOverAxisQuery = false;
	private boolean draggingAxis = false;
	private int newAxisPosition;
	private int scatterplotOffset;
	private Color focusLineColor = DEFAULT_FOCUS_LINE_COLOR;
	private Color contextLineColor = DEFAULT_CONTEXT_LINE_COLOR;
	private int scatterplotAxisSize = /* 12 */14;
	private int contextRegionHeight = 14;

	// HashMaps for scatterplot images
	private HashMap<String, HashMap<String, BufferedImage>> rowPointsSPlotMap = new HashMap<String, HashMap<String, BufferedImage>>();
	private HashMap<String, HashMap<String, BufferedImage>> rowContextSPlotMap = new HashMap<String, HashMap<String, BufferedImage>>();
	private HashMap<String, HashMap<String, BufferedImage>> rowAxesSPlotMap = new HashMap<String, HashMap<String, BufferedImage>>();

	ScatterplotConfiguration focusScatterPlotConfig;
	ScatterplotConfiguration contextScatterPlotConfig;

	private ArrayList<Rectangle> scatterplotRectList;
	private int mouseOverScatterplotRectIndex = -1;
	private Timer waitingTimer;
	private BufferedImage newLinesImage;

	private int tupleDisplayMode = RENDER_TUPLES_AS_LINES;

	private ArrayList<ScatterPlotFrame> scatterplotFrameList = new ArrayList<ScatterPlotFrame>();
	private ArrayList<Point[]> tupleLines;
	private boolean showQueriedData = true;
	private boolean showPolylines = true;

	private int dispersionDisplayMode = MEAN_DISPERSION_BOX_MODE;
	private BinInfo mouseOverAxisHighlightedBinInfo;

	private boolean showCorrelationIndicators = true;
	private boolean useQueryCorrelations = true;
	private boolean useQueryFrequencyData = true;
	private boolean antialiasEnabled = true;

	private int correlationIndicatorHeight = 10;
	private Font titleFont = new Font("Dialog", Font.BOLD, 12);
	private Font secondaryFont = new Font("Dialog", Font.PLAIN, 10);
	private int pcLineSize = 2;
	
	private PCAxisSelection mouseOverAxisSelection;
    private PCAxisSelection draggingAxisSelection;

	public PCPanel(DataModel dataModel) {
		focusScatterPlotConfig = new ScatterplotConfiguration();
		focusScatterPlotConfig.showAxisLabels = false;
		focusScatterPlotConfig.showAxisNames = true;
		focusScatterPlotConfig.showTickMarks = true;
		focusScatterPlotConfig.pointColor = focusLineColor;
		focusScatterPlotConfig.showCorrelationIndicator = false;
		contextScatterPlotConfig = new ScatterplotConfiguration();
		contextScatterPlotConfig.showAxisLabels = false;
		contextScatterPlotConfig.showAxisNames = true;
		contextScatterPlotConfig.showTickMarks = true;
		contextScatterPlotConfig.pointColor = contextLineColor;

		this.dataModel = dataModel;
		this.dataModel.addDataModelListener(this);
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setAntialiasEnabled(boolean antialiasEnabled) {
		if (this.antialiasEnabled != antialiasEnabled) {
			this.antialiasEnabled = antialiasEnabled;
			startFocusLineRenderer();
			startContextLineRenderer();
			startAxesImageRenderer();
		}
	}

	public boolean getAntialiasEnabled() {
		return antialiasEnabled;
	}

	public void setShowCorrelationIndicators(boolean showCorrelationIndicators) {
		if (this.showCorrelationIndicators != showCorrelationIndicators) {
			this.showCorrelationIndicators = showCorrelationIndicators;
			this.startAxesImageRenderer();
		}
	}

	public boolean getShowCorrelationIndicators() {
		return showCorrelationIndicators;
	}

	public void setUseQueryCorrelationCoefficients(boolean useQueryCorrelations) {
		if (this.useQueryCorrelations != useQueryCorrelations) {
			this.useQueryCorrelations = useQueryCorrelations;
			this.startAxesImageRenderer();
		}
	}

	public void setUseQueryFrequencyData(boolean useQueryFrequencyData) {
		if (this.useQueryFrequencyData != useQueryFrequencyData) {
			this.useQueryFrequencyData = useQueryFrequencyData;
			startAxesImageRenderer();
		}
	}

	public boolean getUseQueryCorrelations() {
		return useQueryCorrelations;
	}

	public void setDispersionDisplayMode(int dispersionDisplayMode) {
		if (this.dispersionDisplayMode != dispersionDisplayMode) {
			this.dispersionDisplayMode = dispersionDisplayMode;
			startAxesImageRenderer();
		}
	}

	public int getDispersionDisplayMode() {
		return dispersionDisplayMode;
	}

	public int getAxisBarWidth() {
		return axisBarWidth;
	}

	public void setAxisBarWidth(int width) {
		if (axisBarWidth != width) {
			this.axisBarWidth = width;
			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public int getCorrelationBoxHeight() {
		return correlationIndicatorHeight;
	}

	public void setCorrelationBoxSize(int size) {
		if (correlationIndicatorHeight != size) {
			this.correlationIndicatorHeight = size;
			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public void setTitleFontSize(int fontSize) {
		if (fontSize != titleFont.getSize()) {
			titleFont = new Font("Dialog", Font.BOLD, fontSize);
			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public void setSecondaryFontSize(int fontSize) {
		if (fontSize != secondaryFont.getSize()) {
			secondaryFont = new Font("Dialog", Font.PLAIN, fontSize);
			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public void setAlphaValue(int alphaValue) {
		if (alphaValue >= 0 && alphaValue <= 255) {
			focusLineColor = new Color(focusLineColor.getRed(),
					focusLineColor.getGreen(), focusLineColor.getBlue(),
					alphaValue);
			contextLineColor = new Color(contextLineColor.getRed(),
					contextLineColor.getGreen(), contextLineColor.getBlue(),
					alphaValue);
			focusScatterPlotConfig.pointColor = focusLineColor;
			contextScatterPlotConfig.pointColor = contextLineColor;
			startFocusLineRenderer();
			startContextLineRenderer();
			startScatterplotRenderers();
			for (ScatterPlotFrame spFrame : scatterplotFrameList) {
				spFrame.setPointAlphaValue(alphaValue);
			}
		}
	}

	public void setLineSize(int newSize) {
		if (newSize != pcLineSize) {
			pcLineSize = newSize;
			startFocusLineRenderer();
			startContextLineRenderer();
			startScatterplotRenderers();
		}
	}

	public int getAlphaValue() {
		return focusLineColor.getAlpha();
	}

	public Color getFocusLineColor() {
		return focusLineColor;
	}

	public void setFocusLineColor(Color color) {
		focusLineColor = color;
		focusScatterPlotConfig.pointColor = focusLineColor;
		startFocusLineRenderer();
		startScatterplotRenderers();
	}

	public Color getContextLineColor() {
		return contextLineColor;
	}

	public void setContextLineColor(Color color) {
		contextLineColor = color;
		contextScatterPlotConfig.pointColor = contextLineColor;
		startContextLineRenderer();
		startScatterplotRenderers();
	}

	public void setScatterplotConfiguration(ScatterplotConfiguration SPConfig) {
		this.focusScatterPlotConfig = SPConfig;
	}

	public ScatterplotConfiguration getScatterplotConfiguration() {
		return focusScatterPlotConfig;
	}

	public boolean isShowingAxesAsBars() {
		return showAxesAsBars;
	}

	public boolean isShowingQueryLimits() {
		return drawQueryLimits;
	}

	public void setShowingQueryLimits(boolean showQueryLimits) {
		if (this.drawQueryLimits != showQueryLimits) {
			this.drawQueryLimits = showQueryLimits;
			repaint();
		}
	}

	public void setShowingAxesAsBars(boolean showAxesAsBars) {
		if (this.showAxesAsBars != showAxesAsBars) {
			this.showAxesAsBars = showAxesAsBars;
			startFocusLineRenderer();
			startContextLineRenderer();
			startAxesImageRenderer();
		}
	}

	public boolean isShowingFrequencyInfo() {
		return this.showFrequencyInfo;
	}

	public void setShowFrequencyInfo(boolean showFrequencyInfo) {
		if (this.showFrequencyInfo != showFrequencyInfo) {
			this.showFrequencyInfo = showFrequencyInfo;
			layoutAxes();
			startFocusLineRenderer();
			startContextLineRenderer();
			startAxesImageRenderer();
		}
	}

	public boolean isShowingFilteredData() {
		return showFilteredData;
	}

	public boolean isShowingQueriedData() {
		return showQueriedData;
	}

	public void setShowingQueriedData(boolean showQueriedData) {
		if (this.showQueriedData != showQueriedData) {
			this.showQueriedData = showQueriedData;

			if (showQueriedData) {
				startFocusLineRenderer();
			} else {
				repaint();
			}
		}
	}

	public void setShowingPolylines(boolean showPolylines) {
		if (this.showPolylines != showPolylines) {
			this.showPolylines = showPolylines;
			if (showPolylines) {
				startFocusLineRenderer();
				startContextLineRenderer();
			} else {
				repaint();
			}
		}
	}

	public boolean isShowingPolylines() {
		return showPolylines;
	}

	public void setShowingFilteredData(boolean showFilteredData) {
		if (this.showFilteredData != showFilteredData) {
			this.showFilteredData = showFilteredData;

			if (showFilteredData) {
				startContextLineRenderer();
			} else {
				repaint();
			}
		}
	}

	public int getTupleDisplayMode() {
		return tupleDisplayMode;
	}

	public void setTupleDisplayMode(int tupleDisplayMode) {
		if (this.tupleDisplayMode != tupleDisplayMode) {
			this.tupleDisplayMode = tupleDisplayMode;
			startFocusLineRenderer();
			startContextLineRenderer();
			startAxesImageRenderer();
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (antialiasEnabled) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		if (showPolylines) {
			if (contextImage != null && showFilteredData) {
				g2.drawImage(contextImage, 0, 0, contextImage.getWidth(),
						contextImage.getHeight(), null);
			}

			if (focusImage != null && showQueriedData) {
				g2.drawImage(focusImage, 0, 0, focusImage.getWidth(),
						focusImage.getHeight(), null);
			}

			if (this.newLinesImage != null) {
				g2.drawImage(newLinesImage, 0, 0, newLinesImage.getWidth(),
						newLinesImage.getHeight(), null);
			}
		}

		if (axesImage != null) {
			g2.drawImage(axesImage, 0, 0, axesImage.getWidth(),
					axesImage.getHeight(), null);
		}

		g2.setStroke(new BasicStroke(2.f));
		if (axisList != null) {
			for (PCAxis axis : axisList) {
				// Draw label name for axis
				if (axis.column == dataModel.getHighlightedColumn()) {
					g2.setColor(Color.BLUE);
				} else {
					g2.setColor(Color.DARK_GRAY);
				}

				g2.setFont(titleFont);
				if (axis.labelRectangle != null) {
					if (axis.column == dataModel
							.getOLSMultipleLinearRegressionDependentColumn()) {
						Color fontColor = g2.getColor();
						g2.setColor(Color.DARK_GRAY);
						g2.drawRect(axis.labelRectangle.x - 2,
								axis.labelRectangle.y,
								axis.labelRectangle.width + 4,
								axis.labelRectangle.height + 2);
						g2.setColor(fontColor);
					}

					g2.drawString(axis.column.getName(), axis.labelRectangle.x,
							axis.labelRectangle.y + axis.labelRectangle.height - g2.getFontMetrics().getDescent());
					// g2.draw(axis.labelRectangle);
				}


				if (!axis.axisSelectionList.isEmpty()) {
                    for (PCAxisSelection axisSelection : axis.axisSelectionList) {
                        Rectangle queryRect = new Rectangle(axis.xPosition - (QUERY_BOX_WIDTH / 2), axisSelection.getMaxPosition(),
                                QUERY_BOX_WIDTH, axisSelection.getMinPosition() - axisSelection.getMaxPosition());
                        if (drawQueryLimits) {
                            String minLabel = PCAxesRenderer.DECIMAL_FORMAT.format(axisSelection.getColumnSelectionRange().getMinValue());
                            String maxLabel = PCAxesRenderer.DECIMAL_FORMAT.format(axisSelection.getColumnSelectionRange().getMaxValue());

                            g2.setFont(secondaryFont);
                            int fontHeight = g2.getFontMetrics().getHeight();

                            int stringWidth = g2.getFontMetrics().stringWidth(maxLabel);
                            Rectangle labelRect = new Rectangle(axis.xPosition
                                    - (stringWidth / 2), axisSelection.getMaxPosition() - 1
                                    - fontHeight, stringWidth, fontHeight);
                            g2.setColor(QUERY_RANGE_LABEL_BACKGROUND);
                            g2.fill(labelRect);
                            g2.setColor(Color.BLACK);
                            g2.drawString(maxLabel, axis.xPosition
                                    - (stringWidth / 2), axisSelection.getMaxPosition() - 3);

                            stringWidth = g2.getFontMetrics().stringWidth(minLabel);
                            labelRect = new Rectangle(axis.xPosition
                                    - (stringWidth / 2), axisSelection.getMinPosition() + 2,
                                    stringWidth, fontHeight);
                            g2.setColor(QUERY_RANGE_LABEL_BACKGROUND);
                            g2.fill(labelRect);
                            g2.setColor(Color.BLACK);
                            g2.drawString(minLabel, axis.xPosition
                                    - (stringWidth / 2), axisSelection.getMinPosition()
                                    + fontHeight);
                        }

                        g2.setColor(queryBoxEnabledFillColor);
                        g2.fill(queryRect);

                        g2.setColor(queryBoxOutlineColor);

                        g2.drawLine(queryRect.x, queryRect.y, queryRect.x + queryRect.width, queryRect.y);
                        g2.drawLine(queryRect.x, queryRect.y, queryRect.x, queryRect.y + 3);
                        g2.drawLine(queryRect.x + queryRect.width, queryRect.y, queryRect.x + queryRect.width, queryRect.y + 3);
                        g2.drawLine(queryRect.x, queryRect.y + queryRect.height, queryRect.x + queryRect.width, queryRect.y + queryRect.height);
                        g2.drawLine(queryRect.x, queryRect.y + queryRect.height, queryRect.x, queryRect.y + queryRect.height - 3);
                        g2.drawLine(queryRect.x + queryRect.width, queryRect.y + queryRect.height, queryRect.x + queryRect.width, queryRect.y + queryRect.height - 3);
                    }
                }
			}
		}

		if (dataModel.getHighlightedColumn() != null
				&& dataModel.getHighlightedColumn().isEnabled()) {

			HashMap<String, BufferedImage> rowPointsSPlots = rowPointsSPlotMap
					.get(dataModel.getHighlightedColumn().getName());
			HashMap<String, BufferedImage> rowContextSPlots = rowContextSPlotMap
					.get(dataModel.getHighlightedColumn().getName());
			HashMap<String, BufferedImage> rowAxesSPlots = rowAxesSPlotMap
					.get(dataModel.getHighlightedColumn().getName());

			if (rowPointsSPlots != null) {
				for (int icolumn = 0; icolumn < axisList.size(); icolumn++) {
					PCAxis currentAxis = axisList.get(icolumn);
					Column currentColumn = axisList.get(icolumn).column;
					if (dataModel.getHighlightedColumn() == currentColumn) {
						continue;
					}

					Rectangle spRect = scatterplotRectList.get(icolumn);
					// g2.setColor(Color.blue);
					// g2.draw(spRect);

					int x = spRect.x;
					int y = spRect.y + 2;

					String columnName = currentColumn.getName();
					// int x = (icolumn * axisSpacing) + scatterplotOffset;
					// int y = this.axisBottom + secondaryFont.getSize() + 20;
					// int y = axisList.get(icolumn).scatterplot_y0;

					// draw scatterplot axes
					if (rowAxesSPlots != null) {
						BufferedImage image = rowAxesSPlots.get(columnName);
						if (image != null) {
							g2.drawImage(image, x, y, null);
						}
					}

					// draw context scatterplot
					if (showFilteredData && rowContextSPlots != null) {
						BufferedImage image = rowContextSPlots.get(columnName);
						if (image != null) {
							g2.drawImage(image, x + this.scatterplotAxisSize,
									y, null);
						}
					}

					// draw focus scatterplot
					BufferedImage image = rowPointsSPlots.get(columnName);
					if (image != null) {
						g2.drawImage(image, x + this.scatterplotAxisSize, y,
								null);
					}
				}
			}
		}

		if (draggingAxis) {
			g2.setColor(Color.blue);
			g2.drawLine(endDragPoint.x, endDragPoint.y, endDragPoint.x,
					endDragPoint.y + mouseOverLabelAxis.axisHeight);
			PCAxis axis = axisList.get(newAxisPosition);
			g2.drawRect(axis.labelRectangle.x, axis.labelRectangle.y,
					axis.labelRectangle.width,
					(axis.bottomPosition - axis.labelRectangle.y));
		}

		g2.setColor(Color.DARK_GRAY);
		g2.setFont(secondaryFont);
		String statusString = "Showing " + dataModel.getTupleCount()
				+ " tuples";
		if (dataModel.getQueriedTupleCount() > 0) {
			statusString += " (" + dataModel.getQueriedTupleCount()
					+ " selected)";
		}
		int statusStringWidth = g2.getFontMetrics().stringWidth(statusString);
		g2.drawString(statusString,
				((getWidth() / 2) - (statusStringWidth / 2)), getHeight()
						- g2.getFontMetrics().getDescent());
	}

	private void startScatterplotRenderers() {
		if (dataModel.getColumnCount() == 0) {
			return;
		}

		if (scatterplotSize == 0) {
			return;
		}

		for (int ix = 0; ix < axisList.size(); ix++) {
			for (int iy = 0; iy < axisList.size(); iy++) {
				ScatterplotPointsRenderer renderer = new ScatterplotPointsRenderer(
						dataModel, axisList.get(ix).column,
						axisList.get(iy).column, scatterplotSize,
						scatterplotAxisSize, focusScatterPlotConfig, null,
						true, false, antialiasEnabled);
				renderer.addRendererListener(this);
				renderer.setName("focus");
				renderer.start();
				ScatterplotPointsRenderer contextRenderer = new ScatterplotPointsRenderer(
						dataModel, axisList.get(ix).column,
						axisList.get(iy).column, scatterplotSize,
						scatterplotAxisSize, contextScatterPlotConfig, null,
						false, true, antialiasEnabled);
				contextRenderer.addRendererListener(this);
				contextRenderer.setName("context");
				contextRenderer.start();
				ScatterplotAxesRenderer axesRenderer = new ScatterplotAxesRenderer(
						dataModel, axisList.get(ix).column,
						axisList.get(iy).column, scatterplotSize,
						scatterplotAxisSize, focusScatterPlotConfig,
						antialiasEnabled);
				axesRenderer.addRendererListener(this);
				axesRenderer.start();
			}
		}
	}

	private void startAxesImageRenderer() {
		if (screenWidth == 0 && screenHeight == 0) {
			log.debug("PCPanel.startAxesImageRenderer(): width and height are zero so aborting render.");
			return;
		}

		if (axesRenderer != null) {
			axesRenderer.isRunning = false;
			axesRenderer.removeRendererListener(this);
		}

		axesRenderer = new PCAxesRenderer(dataModel, screenWidth, screenHeight,
				axisBarWidth, showAxesAsBars, showFrequencyInfo,
				dispersionDisplayMode, axisList, axisSpacing,
				showCorrelationIndicators, useQueryCorrelations,
				useQueryFrequencyData, antialiasEnabled, titleFont,
				secondaryFont, correlationIndicatorHeight);
		axesRenderer.addRendererListener(this);
		axesRenderer.start();
	}

	private void startContextLineRenderer() {
		if (!showFilteredData || !showPolylines) {
			return;
		}

		if (contextLineRenderer != null) {
			contextLineRenderer.isRunning = false;
			contextLineRenderer.removeRendererListener(this);
		}
		if (screenWidth == 0 && screenHeight == 0) {
			log.debug("contextLineRenderer(): width and height are zero so aborting render.");
			return;
		}

		contextLineRenderer = new PCLineRenderer(dataModel, screenWidth,
				screenHeight, this.axisBarWidth, this.axisSpacing,
				this.showAxesAsBars, contextLineColor, false, true,
				antialiasEnabled, tupleLines, pcLineSize);

		contextLineRenderer.addRendererListener(this);
		log.debug("Starting context line renderer "
				+ contextLineRenderer.getId());
		contextLineRenderer.start();
	}

	private void startNewLinesRenderer(ArrayList<Tuple> newTuples) {
		if (screenWidth == 0 && screenHeight == 0) {
			return;
		}
		NewLinesRenderer newLineRenderer = new NewLinesRenderer(dataModel,
				screenWidth, screenHeight, this.axisBarWidth,
				this.showAxesAsBars, newTuples);
		newLineRenderer.addRendererListener(this);
		newLineRenderer.start();
	}

	private void startFocusLineRenderer() {
		if (!showQueriedData || !showPolylines) {
			return;
		}

		if (focusLineRenderer != null) {
			focusLineRenderer.isRunning = false;
			focusLineRenderer.removeRendererListener(this);
		}
		if (screenWidth == 0 && screenHeight == 0) {
			log.debug("focusLineRenderer(): width and height are zero so aborting render.");
			return;
		}
		focusLineRenderer = new PCLineRenderer(dataModel, screenWidth,
				screenHeight, this.axisBarWidth, this.axisSpacing,
				this.showAxesAsBars, focusLineColor, true, false,
				antialiasEnabled, tupleLines, pcLineSize);
		focusLineRenderer.addRendererListener(this);
		log.debug("Starting focus line renderer " + focusLineRenderer.getId());
		focusLineRenderer.start();
	}

	private void recalculatePolylines() {
		if (dataModel.getTupleCount() == 0) {
			return;
		}
		
		tupleLines = new ArrayList<Point[]>();

		for (int ituple = 0; ituple < dataModel.getTupleCount(); ituple++) {
			Tuple currentTuple = dataModel.getTuple(ituple);

			Point tuplePoints[] = new Point[dataModel.getColumnCount()];
			for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
				PCAxis axis = axisList.get(iaxis);
				int xPosition = axis.xPosition;
				double currentValue = currentTuple.getElement(axis.dataModelIndex);
				double normValue = (currentValue - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//				int currentYPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                int currentYPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
				tuplePoints[iaxis] = new Point(xPosition, currentYPosition);
			}

			tupleLines.add(tuplePoints);
		}
	}

	protected void layoutAxes() {
		if (dataModel.getColumnCount() == 0) {
			return;
		}

		scatterplotRectList = new ArrayList<Rectangle>();

		if (axisList == null) {
			axisList = new ArrayList<PCAxis>();
			for (int icolumn = 0; icolumn < dataModel.getColumnCount(); icolumn++) {
				Column column = dataModel.getColumn(icolumn);
				if (column.isEnabled()) {
					PCAxis axis = new PCAxis(column, icolumn);
					axisList.add(axis);
				}
			}
		} else {
			for (int icolumn = 0; icolumn < dataModel.getColumnCount(); icolumn++) {
				Column column = dataModel.getColumn(icolumn);
				for (PCAxis axis : axisList) {
					if (axis.column == column) {
						axis.dataModelIndex = icolumn;
                        break;
					}
				}
			}
		}

		// axisBarWidth = AXIS_BAR_WIDTH;

		screenWidth = getWidth() - 1;
		screenHeight = getHeight() - 1;

		axisSpacing = (screenWidth - (BORDER_SIZE * 2)) / axisList.size();

		int pcpHeight = (int) (screenHeight * .7);
		scatterplotSize = screenHeight - pcpHeight - (secondaryFont.getSize() * 2);

        if (scatterplotSize > (int)(axisSpacing * .8)) {
            scatterplotSize = (int)(axisSpacing * .8);
            pcpHeight = screenHeight - scatterplotSize - (int)(secondaryFont.getSize()*1.5);
        }

//		scatterplotSize = (int)((axisSpacing - (secondaryFont.getSize())));
//		int pcpHeight =  screenHeight - scatterplotSize - (int)(secondaryFont.getSize()*1.5);
//
//		/* Keep the scatterplot size from becoming too large */
//		if (scatterplotSize > (.5 * pcpHeight)) {
//			scatterplotSize = (int)(.5 * pcpHeight);
//			pcpHeight = screenHeight - scatterplotSize - (int)(secondaryFont.getSize()*1.5);
//		}

		scatterplotOffset = (axisSpacing - scatterplotSize) / 2;

		axisTop = BORDER_SIZE + titleFont.getSize() + secondaryFont.getSize()
				+ correlationIndicatorHeight + 14;
		axisBottom = pcpHeight - secondaryFont.getSize() - 2;
		axisHeight = axisBottom - axisTop;
        axisFocusTop = axisTop + contextRegionHeight;
        axisFocusBottom = axisBottom - contextRegionHeight;
        axisFocusHeight = axisFocusBottom - axisFocusTop;

		/*
		 * scatterplotSize = (int)(axisSpacing * .7); if (scatterplotSize >
		 * (screenHeight * .3)) { scatterplotSize = (int)(screenHeight * .3); }
		 * scatterplotOffset = (axisSpacing - scatterplotSize) / 2;
		 * 
		 * // axisTop = BORDER_SIZE + rangeLabelSize + 14; axisTop = BORDER_SIZE
		 * + titleFont.getSize() + secondaryFont.getSize() +
		 * correlationIndicatorHeight + 12; axisBottom = screenHeight -
		 * (BORDER_SIZE + secondaryFont.getSize() + scatterplotSize + 35); //
		 * axisBottom = screenHeight - BORDER_SIZE - rangeLabelSize -
		 * scatterplotSize; axisHeight = axisBottom - axisTop;
		 */
		for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
			PCAxis axis = axisList.get(iaxis);

			Column column = axis.column;
            SummaryStats columnQueryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(column);

			axis.xPosition = BORDER_SIZE + (axisSpacing / 2)
					+ (iaxis * axisSpacing);
			axis.topPosition = axisTop;
			axis.bottomPosition = axisBottom;
			axis.axisHeight = axisHeight;
            axis.focusTop = axisFocusTop;
            axis.focusBottom = axisFocusBottom;
            axis.focusHeight = axisFocusHeight;

			axis.axisWidth = axisBarWidth;
			axis.scatterplot_x0 = axis.xPosition + scatterplotOffset;
			axis.scatterplot_y0 = pcpHeight;

			axis.rectangle = new Rectangle(axis.xPosition - (axisSpacing / 2), axis.topPosition, axis.axisWidth,
                    axis.axisHeight);
			axis.axisBarRectangle = new Rectangle(axis.xPosition - (axisBarWidth / 2), axis.topPosition, axisBarWidth,
					axisHeight);
            axis.focusRectangle = new Rectangle(axis.xPosition - (axisSpacing / 2), axis.focusTop, axis.axisWidth,
                    axis.focusHeight);
            axis.upperContextRectangle = new Rectangle(axis.xPosition - (axisSpacing / 2), axis.topPosition, axis.axisWidth,
                    contextRegionHeight);
            axis.lowerContextRectangle = new Rectangle(axis.xPosition - (axisSpacing / 2), axis.focusBottom, axis.axisWidth,
                    contextRegionHeight);

			// calculate scatterplot rectangle
			int scatterplotX = (iaxis * axisSpacing) + scatterplotOffset;
			int scatterplotY = pcpHeight;
			Rectangle scatterplotRect = new Rectangle(scatterplotX, scatterplotY, scatterplotSize, scatterplotSize);
			scatterplotRectList.add(scatterplotRect);

			// calculate the mean position
            double normValue = (axis.column.getSummaryStats().getMean() - axis.column.getSummaryStats().getMin())
                    / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            axis.meanPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            axis.meanPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);

            // calculate the query mean position
            if (columnQueryStats != null) {
                normValue = (columnQueryStats.getMean() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                axis.queryMeanPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                axis.queryMeanPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
            }
            
			// calculate mean-centered standard deviation range box
            double lowValue = axis.column.getSummaryStats().getMean() - axis.column.getSummaryStats().getStandardDeviation();
            normValue = (lowValue - axis.column.getSummaryStats().getMin()) /
                    (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int lowValueY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            int lowValueY = axis.focusBottom - (int) (normValue * axis.focusHeight);
            lowValueY = lowValueY > axis.focusBottom ? axis.focusBottom : lowValueY;
            double highValue = axis.column.getSummaryStats().getMean() + axis.column.getSummaryStats().getStandardDeviation();
            normValue = (highValue - axis.column.getSummaryStats().getMin()) /
                    (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int highValueY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//            highValueY = highValueY < axis.topPosition ? axis.topPosition : highValueY;
            int highValueY = axis.focusBottom - (int) (normValue * axis.focusHeight);
            highValueY = highValueY < axis.focusTop ? axis.focusTop : highValueY;
            axis.standardDeviationRangeRectangle = new Rectangle(axis.axisBarRectangle.x + 3, highValueY,
                    axis.axisBarRectangle.width - 6, lowValueY - highValueY);

			// calculate query mean-centered standard deviation range box
            if (columnQueryStats != null) {
                double queryLowValue = columnQueryStats.getMean() - columnQueryStats.getStandardDeviation();
                normValue = (queryLowValue - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                int queryLowValueY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//                queryLowValueY = queryLowValueY > axis.bottomPosition ? axis.bottomPosition : queryLowValueY;
                int queryLowValueY = axis.focusBottom - (int) (normValue * axis.focusHeight);
                queryLowValueY = queryLowValueY > axis.focusBottom ? axis.focusBottom : queryLowValueY;
                double queryHighValue = columnQueryStats.getMean() + columnQueryStats.getStandardDeviation();
                normValue = (queryHighValue - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
                int queryHighValueY = axis.focusBottom - (int) (normValue * axis.focusHeight);
                queryHighValueY = queryHighValueY < axis.focusTop ? axis.focusTop : queryHighValueY;
//                int queryHighValueY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//                queryHighValueY = queryHighValueY < axis.topPosition ? axis.topPosition : queryHighValueY;
                axis.queryStandardDeviationRangeRectangle = new Rectangle(axis.axisBarRectangle.x + 7, queryHighValueY,
                        axis.axisBarRectangle.width - 14, queryLowValueY - queryHighValueY);
            }

            // calculate the median line position
            normValue = (axis.column.getSummaryStats().getMedian() - axis.column.getSummaryStats().getMin()) /
                    (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
            axis.medianPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
//            axis.medianPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);

            // calculate the query median line position
            if (columnQueryStats != null) {
                normValue = (columnQueryStats.getMedian() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
                axis.queryMedianPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
//                axis.queryMedianPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            }
            
			if (this.showFrequencyInfo) {
//				axis.frequencyDisplayRectangle = new Rectangle(axis.axisBarRectangle);
                axis.frequencyDisplayRectangle = new Rectangle(axis.focusRectangle);
			}

			// calculate IQR range box
            normValue = (axis.column.getSummaryStats().getQuantile1() - axis.column.getSummaryStats().getMin())
                    / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int q1Y = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            int q1Y = axis.focusBottom - (int) (normValue * axis.focusHeight);
            normValue = (axis.column.getSummaryStats().getQuantile3() - axis.column.getSummaryStats().getMin())
                    / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int q3Y = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//            axis.IQRBoxRectangle = new Rectangle(axis.axisBarRectangle.x + 3,
//                    q3Y, axis.axisBarRectangle.width - 6, q1Y - q3Y);
            int q3Y = axis.focusBottom - (int) (normValue * axis.focusHeight);
            axis.IQRBoxRectangle = new Rectangle(axis.axisBarRectangle.x + 3,
                    q3Y, axis.axisBarRectangle.width - 6, q1Y - q3Y);

            // calculate Query IQR range box
            if (columnQueryStats != null) {
                normValue = (columnQueryStats.getQuantile1() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                int queryQ1Y = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                int queryQ1Y = axis.focusBottom - (int) (normValue * axis.focusHeight);
                normValue = (columnQueryStats.getQuantile3() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                int queryQ3Y = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                int queryQ3Y = axis.focusBottom - (int) (normValue * axis.focusHeight);
                axis.QueryIQRBoxRectangle = new Rectangle(axis.axisBarRectangle.x + 7, queryQ3Y, axis.axisBarRectangle.width - 14, queryQ1Y - queryQ3Y);
            }
            
            // calculate IQR whiskers
            normValue = (axis.column.getSummaryStats().getLowerWhisker() - axis.column.getSummaryStats().getMin())
                    / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int lowerWhiskerY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            int lowerWhiskerY = axis.focusBottom - (int) (normValue * axis.focusHeight);
            normValue = (axis.column.getSummaryStats().getUpperWhisker() - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//            int upperWhiskerY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
            int upperWhiskerY = axis.focusBottom - (int) (normValue * axis.focusHeight);
            axis.IQRWhiskerRectangle = new Rectangle(axis.IQRBoxRectangle.x, upperWhiskerY, axis.IQRBoxRectangle.width, lowerWhiskerY - upperWhiskerY);

            // calculate Query IQR whiskers
            if (columnQueryStats != null) {
                normValue = (columnQueryStats.getLowerWhisker() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                int queryLowerWhiskerY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                int queryLowerWhiskerY = axis.focusBottom - (int) (normValue * axis.focusHeight);
                normValue = (columnQueryStats.getUpperWhisker() - axis.column.getSummaryStats().getMin())
                        / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
                int queryUpperWhiskerY = axis.focusBottom - (int) (normValue * axis.focusHeight);
//                int queryUpperWhiskerY = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                axis.QueryIQRWhiskerRectangle = new Rectangle(axis.QueryIQRBoxRectangle.x, queryUpperWhiskerY,
                        axis.QueryIQRBoxRectangle.width, queryLowerWhiskerY - queryUpperWhiskerY);
            }
            
//            axis.axisSelectionList.clear();
//            ColumnSelection columnSelection = dataModel.getActiveQuery().getColumnSelection(axis.column);
//            if (columnSelection != null) {
//            	for (ColumnSelectionRange selectionRange : columnSelection.getColumnSelectionRanges()) {
//            		PCAxisSelection axisSelection = new PCAxisSelection(selectionRange);
//            		axis.axisSelectionList.add(axisSelection);
//                    normValue = (axisSelection.getColumnSelectionRange().getMaxValue() - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                    int maxPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//                    axisSelection.setMaxPosition(maxPosition);
//                    normValue = (axisSelection.getColumnSelectionRange().getMinValue() - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                    int minPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
//                    axisSelection.setMinPosition(minPosition);
//            	}
//            }
            
            // if there are axis selections for the axis, adjust the selection box locations
            if (!axis.axisSelectionList.isEmpty()) {	
//            	log.debug("axis + " + axis.column.getName() + " has " + axis.axisSelectionList.size() + " selections");
            	// for each axis selection, adjust min and max locations
                for (PCAxisSelection axisSelection : axis.axisSelectionList) {
                    normValue = (axisSelection.getColumnSelectionRange().getMaxValue() - axis.column.getSummaryStats().getMin())
                            / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                    int maxPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                    int maxPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
                    axisSelection.setMaxPosition(maxPosition);
                    normValue = (axisSelection.getColumnSelectionRange().getMinValue() - axis.column.getSummaryStats().getMin())
                            / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
//                    int minPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
                    int minPosition = axis.focusBottom - (int) (normValue * axis.focusHeight);
                    axisSelection.setMinPosition(minPosition);
                }
            }
		}
	}

	// protected void layoutAxesOld () {
	// if (dataModel.getColumnCount() == 0) {
	// return;
	// }
	//
	// axisList = new ArrayList<PCPAxis>();
	// scatterplotRectList = new ArrayList<Rectangle>();
	//
	// if (this.showFrequencyInfo) {
	// axisBarWidth = AXIS_BAR_WIDTH_WITH_FREQUENCY;
	// } else {
	// axisBarWidth = AXIS_BAR_WIDTH;
	// }
	//
	// screenWidth = getWidth()-1;
	// screenHeight = getHeight()-14;
	// axisSpacing = (screenWidth - (BORDER_SIZE*2)) /
	// dataModel.getEnabledColumnCount();
	//
	// scatterplotSize = (int)(axisSpacing * .7);
	// scatterplotOffset = (axisSpacing - scatterplotSize) / 2;
	// axisTop = BORDER_SIZE + rangeLabelSize + 14;
	// axisBottom = screenHeight - BORDER_SIZE - rangeLabelSize -
	// scatterplotSize;
	// axisHeight = axisBottom - axisTop;
	//
	// for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
	// Column column = dataModel.getColumn(iaxis);
	//
	// PCPAxis axis = new PCPAxis(column);
	// axisList.add(axis);
	//
	// if (!column.isEnabled()) {
	// continue;
	// }
	//
	// axis.xPosition = BORDER_SIZE + (axisSpacing/2) + (iaxis * axisSpacing);
	// axis.topPosition = axisTop;
	// axis.bottomPosition = axisBottom;
	// axis.axisHeight = axisHeight;
	// axis.axisWidth = AXIS_WIDTH;
	// axis.scatterplot_x0 = axis.xPosition + scatterplotOffset;
	// axis.scatterplot_y0 = axis.bottomPosition + scatterplotSize + 20;
	// axis.rectangle = new Rectangle(axis.xPosition - (axis.axisWidth/2),
	// axis.topPosition, axis.axisWidth, axis.axisHeight);
	//
	// axis.axisBarRectangle = new Rectangle(axis.xPosition-(axisBarWidth/2),
	// axis.topPosition, axisBarWidth, axisHeight);
	//
	// // calculate scatterplot rectangle
	// int scatterplotX = (iaxis * axisSpacing) + scatterplotOffset;
	// int scatterplotY = axisBottom + 20;
	// Rectangle scatterplotRect = new Rectangle(scatterplotX, scatterplotY,
	// scatterplotSize, scatterplotSize);
	// scatterplotRectList.add(scatterplotRect);
	//
	// // calculate IQR range box
	// float normValue = (axis.column.getQ1() - axis.column.getMinValue()) /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// int q1Y = axis.bottomPosition - (int)(normValue * axis.axisHeight);
	// normValue = (axis.column.getQ3() - axis.column.getMinValue()) /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// int q3Y = axis.bottomPosition - (int)(normValue * axis.axisHeight);
	// if (this.showFrequencyInfo) {
	// axis.IQRBoxRectangle = new
	// Rectangle(axis.xPosition-(axis.axisBarRectangle.width/8), q3Y,
	// axis.axisBarRectangle.width/4, q1Y-q3Y);
	// } else {
	// axis.IQRBoxRectangle = new Rectangle(axis.axisBarRectangle.x, q3Y,
	// axis.axisBarRectangle.width, q1Y-q3Y);
	// }
	//
	// // calculate IQR whiskers
	// normValue = (axis.column.getLowerWhisker() - axis.column.getMinValue()) /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// int lowerWhiskerY = axis.bottomPosition - (int)(normValue *
	// axis.axisHeight);
	// normValue = (axis.column.getUpperWhisker() - axis.column.getMinValue()) /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// int upperWhiskerY = axis.bottomPosition - (int)(normValue *
	// axis.axisHeight);
	// axis.IQRWhiskerRectangle = new Rectangle(axis.axisBarRectangle.x,
	// upperWhiskerY, axis.axisBarRectangle.width, lowerWhiskerY-upperWhiskerY);
	//
	// //calculate the median line position
	// normValue = (axis.column.getMedian() - axis.column.getMinValue()) /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// axis.medianPosition = axis.bottomPosition - (int)(normValue *
	// axis.axisHeight);
	//
	// if (column.isQuerySet()) {
	// normValue = (axis.column.getMaxQueryValue() - axis.column.getMinValue())
	// /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// axis.queryMaxPosition = axis.bottomPosition - (int)(normValue *
	// axis.axisHeight);
	// normValue = (axis.column.getMinQueryValue() - axis.column.getMinValue())
	// /
	// (axis.column.getMaxValue() - axis.column.getMinValue());
	// axis.queryMinPosition = axis.bottomPosition - (int)(normValue *
	// axis.axisHeight);
	// }
	// }
	// }

	@Override
	public void mouseDragged(MouseEvent event) {
		if (mouseOverAxis != null) {
			dragging = true;
			if (mouseOverAxisQuery) {
				// translate query box
				int deltaY = event.getPoint().y - endDragPoint.y;

				endDragPoint.setLocation(event.getPoint());

				mouseOverAxisSelection.setMaxPosition(mouseOverAxisSelection.getMaxPosition() + deltaY);
	            mouseOverAxisSelection.setMinPosition(mouseOverAxisSelection.getMinPosition() + deltaY);

                if (mouseOverAxisSelection.getMaxPosition() < mouseOverAxis.focusTop) {
                    deltaY = mouseOverAxis.focusTop - mouseOverAxisSelection.getMaxPosition();
                    mouseOverAxisSelection.setMaxPosition(mouseOverAxis.focusTop);
                    mouseOverAxisSelection.setMinPosition(mouseOverAxisSelection.getMinPosition() + deltaY);
                }
//	            if (mouseOverAxisSelection.getMaxPosition() < mouseOverAxis.topPosition) {
//                    deltaY = mouseOverAxis.topPosition - mouseOverAxisSelection.getMaxPosition();
//                    mouseOverAxisSelection.setMaxPosition(mouseOverAxis.topPosition);
//                    mouseOverAxisSelection.setMinPosition(mouseOverAxisSelection.getMinPosition() + deltaY);
//                }

                if (mouseOverAxisSelection.getMinPosition() > mouseOverAxis.focusBottom) {
                    deltaY = mouseOverAxisSelection.getMinPosition() - mouseOverAxis.focusBottom;
                    mouseOverAxisSelection.setMaxPosition(mouseOverAxisSelection.getMaxPosition() - deltaY);
                    mouseOverAxisSelection.setMinPosition(mouseOverAxis.focusBottom);
                }
//                if (mouseOverAxisSelection.getMinPosition() > mouseOverAxis.bottomPosition) {
//                    deltaY = mouseOverAxisSelection.getMinPosition() - mouseOverAxis.bottomPosition;
//                    mouseOverAxisSelection.setMaxPosition(mouseOverAxisSelection.getMaxPosition() - deltaY);
//                    mouseOverAxisSelection.setMinPosition(mouseOverAxis.bottomPosition);
//                }
                
                double normPos = (mouseOverAxisSelection.getMaxPosition() - mouseOverAxis.focusTop)
                        / (double) mouseOverAxis.focusHeight;
//                float normPos = (mouseOverAxisSelection.getMaxPosition() - mouseOverAxis.topPosition)
//                        / (float) mouseOverAxis.axisHeight;
                double maxValue = mouseOverAxis.column.getSummaryStats().getMax() -
                        (normPos * (mouseOverAxis.column.getSummaryStats().getMax() - mouseOverAxis.column.getSummaryStats().getMin()));

//                normPos = (mouseOverAxisSelection.getMinPosition() - mouseOverAxis.topPosition)
//                        / (float) mouseOverAxis.axisHeight;
                normPos = (mouseOverAxisSelection.getMinPosition() - mouseOverAxis.focusTop)
                        / (double) mouseOverAxis.focusHeight;
                double minValue = mouseOverAxis.column.getSummaryStats().getMax() -
                        (normPos * (mouseOverAxis.column.getSummaryStats().getMax() - mouseOverAxis.column.getSummaryStats().getMin()));

                mouseOverAxisSelection.getColumnSelectionRange().setMinValue(minValue);
                mouseOverAxisSelection.getColumnSelectionRange().setMaxValue(maxValue);
			} else {
				endDragPoint.setLocation(event.getPoint());
//              mouseOverAxis.column.setQueryFlag(true);

                int queryMaxPosition = startDragPoint.y < endDragPoint.y ? startDragPoint.y : endDragPoint.y;
                if (queryMaxPosition < mouseOverAxis.focusTop) {
                  queryMaxPosition = mouseOverAxis.focusTop;
                }
//                if (queryMaxPosition < mouseOverAxis.topPosition) {
//                    queryMaxPosition = mouseOverAxis.topPosition;
//                }

                int queryMinPosition = startDragPoint.y > endDragPoint.y ? startDragPoint.y : endDragPoint.y;
                if (queryMinPosition > mouseOverAxis.focusBottom) {
                  queryMinPosition = mouseOverAxis.focusBottom;
                }
//                if (queryMinPosition > mouseOverAxis.bottomPosition) {
//                    queryMinPosition = mouseOverAxis.bottomPosition;
//                }

//                float normPos = (queryMaxPosition - mouseOverAxis.topPosition) / (float) mouseOverAxis.axisHeight;
                double normPos = (queryMaxPosition - mouseOverAxis.focusTop) / (double) mouseOverAxis.focusHeight;
                double maxValue = mouseOverAxis.column.getSummaryStats().getMax() -
                        (normPos * (mouseOverAxis.column.getSummaryStats().getMax() - mouseOverAxis.column.getSummaryStats().getMin()));
                //              mouseOverAxis.column.setMaxQueryValue(mouseOverAxis.column.getMaxValue() - (normPos * (mouseOverAxis.column.getMaxValue() - mouseOverAxis.column.getMinValue())));

//                normPos = (queryMinPosition - mouseOverAxis.topPosition) / (float) mouseOverAxis.axisHeight;
                normPos = (queryMinPosition - mouseOverAxis.focusTop) / (double) mouseOverAxis.focusHeight;
                double minValue = mouseOverAxis.column.getSummaryStats().getMax() -
                        (normPos * (mouseOverAxis.column.getSummaryStats().getMax() - mouseOverAxis.column.getSummaryStats().getMin()));
                //              mouseOverAxis.column.setMinQueryValue(mouseOverAxis.column.getMaxValue() - (normPos * (mouseOverAxis.column.getMaxValue() - mouseOverAxis.column.getMinValue())));


              if (draggingAxisSelection == null) {
            	  draggingAxisSelection = new PCAxisSelection();
                  ColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(mouseOverAxis.column, minValue, maxValue);
//                  draggingAxisSelection = new PCAxisSelection(selectionRange);
                  draggingAxisSelection.setColumnSelectionRange(selectionRange);
                  mouseOverAxis.axisSelectionList.add(draggingAxisSelection);
              } else {
                  draggingAxisSelection.getColumnSelectionRange().setMaxValue(maxValue);
                  draggingAxisSelection.getColumnSelectionRange().setMinValue(minValue);
              }

              draggingAxisSelection.setMinPosition(queryMinPosition);
              draggingAxisSelection.setMaxPosition(queryMaxPosition);
			}

			repaint();
		} else if (mouseOverLabelAxis != null) {
			draggingAxis = true;
			endDragPoint.setLocation(event.getPoint());
			newAxisPosition = (endDragPoint.x - (axisSpacing / 2) + (QUERY_BOX_WIDTH / 2)) / axisSpacing;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		if (axisList == null) {
			return;
		}

		Cursor newCursor = Cursor.getDefaultCursor();
		mouseOverAxis = null;
		mouseOverLabelAxis = null;
		mouseOverAxisQuery = false;
		mouseOverScatterplotRectIndex = -1;
		mouseOverAxisHighlightedBinInfo = null;

		for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
			PCAxis axis = axisList.get(iaxis);

			if (axis.axisBarRectangle.contains(event.getPoint())) {
				mouseOverAxis = axis;
				
				if (!mouseOverAxis.axisSelectionList.isEmpty()) {
                    for (PCAxisSelection selection : mouseOverAxis.axisSelectionList) {
                        if ((event.getY() <= selection.getMinPosition()) &&
                                event.getY() >= selection.getMaxPosition()) {
                            newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                            mouseOverAxisQuery = true;
                            mouseOverAxisSelection = selection;
                            return;
                        }
                    }
                }
				
				newCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			}

			if (axis.labelRectangle != null) {
				if (axis.labelRectangle.contains(event.getPoint())) {
					mouseOverLabelAxis = axis;
					newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				}
			}

			if (scatterplotRectList != null) {
				if (scatterplotRectList.get(iaxis).contains(event.getPoint())) {
					mouseOverScatterplotRectIndex = iaxis;
				}
			}
		}
		setCursor(newCursor);
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (SwingUtilities.isLeftMouseButton(event)) {
			if (mouseOverAxis != null) {
				if (event.getClickCount() == 2 &&
                        mouseOverAxis.frequencyDisplayRectangle != null &&
                        mouseOverAxis.frequencyDisplayRectangle.contains(event.getPoint())) {
					double normPosition = (double) (event.getPoint().y - mouseOverAxis.axisBarRectangle.y)
							/ (double) mouseOverAxis.axisBarRectangle.height;
					double valueAtMousePoint = mouseOverAxis.column.getSummaryStats().getMax() - (normPosition * (mouseOverAxis.column.getSummaryStats().getMax() - mouseOverAxis.column.getSummaryStats().getMin()));
                    Histogram.BinInfo binInfo = mouseOverAxis.column.getSummaryStats().getHistogram().findBin(valueAtMousePoint);
                    mouseOverAxisHighlightedBinInfo = binInfo;
//					double valueAtMousePoint = mouseOverAxis.column
//							.getMaxValue()
//							- (normPosition * (mouseOverAxis.column
//									.getMaxValue() - mouseOverAxis.column
//									.getMinValue()));
//					Histogram.BinInfo binInfo = mouseOverAxis.column
//							.getHistogram().findBin(valueAtMousePoint);
//					mouseOverAxisHighlightedBinInfo = binInfo;
//					mouseOverAxis.column.setQueryFlag(true);
//					mouseOverAxis.column
//							.setMaxQueryValue((float) binInfo.highEdge);
//					mouseOverAxis.column
//							.setMinQueryValue((float) binInfo.lowEdge);
					dataModel.setQueriedTuples();
					// startFocusLineRenderer();
					// startContextLineRenderer();
					// startScatterplotRenderers();
					// repaint();
				} else if (event.getClickCount() == 1) {
					if (mouseOverAxisSelection != null) {
                        log.debug("Mouse clicked on axis selection");
//                        mouseOverAxis.axisSelectionList.remove(mouseOverAxisSelection);
                        dataModel.clearColumnSelectionRange(mouseOverAxisSelection.getColumnSelectionRange());
                        mouseOverAxisSelection = null;
                        mouseOverAxisQuery = false;
//                        dataModel.setQueriedTuples();
                    }
//					if (mouseOverAxis.column.isQuerySet()) {
//						mouseOverAxis.column.setQueryFlag(false);
//						mouseOverAxis.column
//								.setMaxQueryValue(mouseOverAxis.column
//										.getMaxValue());
//						mouseOverAxis.column
//								.setMinQueryValue(mouseOverAxis.column
//										.getMinValue());
//						dataModel.setQueriedTuples();
//						// startFocusLineRenderer();
//						// startScatterplotRenderers();
//						// repaint();
//					}
				}
			} else if (mouseOverScatterplotRectIndex != -1) {
				if (event.getClickCount() == 2) {
					// show the scatterplot in a new scatterplotframe
					Column xColumn = dataModel.getHighlightedColumn();
					Column yColumn = axisList
							.get(mouseOverScatterplotRectIndex).column;
					ScatterplotConfiguration focusConfig = new ScatterplotConfiguration();
					focusConfig.pointColor = this.focusLineColor;
					focusConfig.pointShape = new Ellipse2D.Double(0, 0, 6, 6);
					focusConfig.showTickMarks = true;
					focusConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);
					ScatterplotConfiguration contextConfig = new ScatterplotConfiguration();
					contextConfig.pointColor = this.contextLineColor;
					contextConfig.pointShape = new Ellipse2D.Double(0, 0, 6, 6);
					contextConfig.showTickMarks = true;
					contextConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);

					ScatterPlotFrame frame = new ScatterPlotFrame(dataModel,
							xColumn, yColumn, focusConfig, contextConfig);
					frame.setVisible(true);
					frame.setColumns(xColumn, yColumn);
					scatterplotFrameList.add(frame);
					frame.addWindowListener(this);
				}
			} else if (mouseOverLabelAxis != null) {
				dataModel.setHighlightedColumn(mouseOverLabelAxis.column);
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
		endDragPoint.setLocation(event.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (dragging) {
			dragging = false;
			dataModel.setQueriedTuples();
			draggingAxisSelection = null;
			// startFocusLineRenderer();
			// repaint();
		} else if (mouseOverLabelAxis != null) {
			int currentAxisPosition = axisList.indexOf(mouseOverLabelAxis);

			newAxisPosition = (endDragPoint.x - (axisSpacing / 2) + (QUERY_BOX_WIDTH / 2))
					/ axisSpacing;
			draggingAxis = false;

			axisList.set(currentAxisPosition, axisList.get(newAxisPosition));

			if (currentAxisPosition == newAxisPosition) {
				return;
			}

			if (currentAxisPosition < newAxisPosition) {
				for (int i = currentAxisPosition; i < newAxisPosition; i++) {
					axisList.set(i, axisList.get(i + 1));
				}
			} else {
				for (int i = currentAxisPosition; i > newAxisPosition; i--) {
					axisList.set(i, axisList.get(i - 1));
				}
			}
			axisList.set(newAxisPosition, mouseOverLabelAxis);

			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startFocusLineRenderer();
			startContextLineRenderer();
		}
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent event) {
		if (this.waitingTimer == null) {
			// Start waiting for DELAY to elapse.
			this.waitingTimer = new Timer(DELAY, this);
			this.waitingTimer.start();
		} else {
			// Event came to soon, swallow it by resetting the timer.
			this.waitingTimer.restart();
		}
	}

	private void recalculateQueryBoxes() {
		// recalculate the positions for the query boxes if set
		for (PCAxis axis : axisList) {
			if (dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections()) {
//			if (axis.column.isQuerySet()) {
//				float normValue = (axis.column.getMaxQueryValue() - axis.column
//						.getMinValue())
//						/ (axis.column.getMaxValue() - axis.column
//								.getMinValue());
//				axis.queryMaxPosition = axis.bottomPosition
//						- (int) (normValue * axis.axisHeight);
//
//				normValue = (axis.column.getMinQueryValue() - axis.column
//						.getMinValue())
//						/ (axis.column.getMaxValue() - axis.column
//								.getMinValue());
//				axis.queryMinPosition = axis.bottomPosition
//						- (int) (normValue * axis.axisHeight);
			}
		}
	}

	public void applyResize() {
		if (axisList == null) {
			return;
		}

		axesImage = focusImage = contextImage = null;
		layoutAxes();

		recalculateQueryBoxes();
		recalculatePolylines();
		startScatterplotRenderers();
		startAxesImageRenderer();
		startContextLineRenderer();
		startFocusLineRenderer();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		axesImage = contextImage = focusImage = null;
		axisList = null;
		layoutAxes();
		recalculatePolylines();
		dataModel.setQueriedTuples();

		if (dataModel.getTupleCount() > 0) {
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	@Override
	public void columnDisabled(DataModel dataModel, Column disabledColumn) {
		for (PCAxis axis : axisList) {
			if (axis.column == disabledColumn) {
				axisList.remove(axis);
				layoutAxes();
				recalculatePolylines();
				dataModel.setQueriedTuples();
				if (dataModel.getTupleCount() > 0) {
					startAxesImageRenderer();
					startContextLineRenderer();
					startFocusLineRenderer();
					startScatterplotRenderers();
				}
				return;
			}
		}
	}

	@Override
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
		ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();

		for (PCAxis axis : axisList) {
			if (!disabledColumns.contains(axis.column)) {
				newAxisList.add(axis);
			}
		}

		axisList = newAxisList;

		layoutAxes();
		recalculatePolylines();
		dataModel.setQueriedTuples();
		if (dataModel.getTupleCount() > 0) {
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	@Override
	public void columnEnabled(DataModel dataModel, Column enabledColumn) {
		int dataModelIndex = dataModel.getColumnIndex(enabledColumn);
		axisList.add(new PCAxis(enabledColumn, dataModelIndex));
		layoutAxes();
		recalculatePolylines();
		dataModel.setQueriedTuples();
		if (dataModel.getTupleCount() > 0) {
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
		return;
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
		// start a thread to show new tuples in a prominent color and fade out
		// to normal color
		// when finished rebuild focus, context, and scatterplot images.
		dataModelChanged(dataModel);
		startNewLinesRenderer(newTuples);
	}

	@Override
	public void rendererFinished(Renderer renderer) {
		if (renderer == axesRenderer) {
			axesImage = renderer.getRenderedImage();
			repaint();
		} else if (renderer == contextLineRenderer) {
			contextImage = renderer.getRenderedImage();
			repaint();
		} else if (renderer == focusLineRenderer) {
			focusImage = renderer.getRenderedImage();
			repaint();

		} else if (renderer instanceof NewLinesRenderer) {
			newLinesImage = renderer.getRenderedImage();
			// repaint();
			if (((NewLinesRenderer) renderer).getFadeCounter() == 0) {
				newLinesImage = null;
			}
			repaint();
		} else if (renderer instanceof ScatterplotPointsRenderer) {
			if (renderer.getName().equals("focus")) {
				String xColumnName = ((ScatterplotPointsRenderer) renderer)
						.getXColumn().getName();
				String yColumnName = ((ScatterplotPointsRenderer) renderer)
						.getYColumn().getName();

				HashMap<String, BufferedImage> rowPlots = rowPointsSPlotMap
						.get(xColumnName);
				if (rowPlots == null) {
					rowPlots = new HashMap<String, BufferedImage>();
					rowPointsSPlotMap.put(xColumnName, rowPlots);
				}

				rowPlots.put(yColumnName, renderer.getRenderedImage());
				repaint();
			} else if (renderer.getName().equals("context")) {
				String xColumnName = ((ScatterplotPointsRenderer) renderer)
						.getXColumn().getName();
				String yColumnName = ((ScatterplotPointsRenderer) renderer)
						.getYColumn().getName();

				HashMap<String, BufferedImage> rowSPlots = rowContextSPlotMap
						.get(xColumnName);
				if (rowSPlots == null) {
					rowSPlots = new HashMap<String, BufferedImage>();
					rowContextSPlotMap.put(xColumnName, rowSPlots);
				}

				rowSPlots.put(yColumnName, renderer.getRenderedImage());
				repaint();
			}
		} else if (renderer instanceof ScatterplotAxesRenderer) {
			String xColumnName = ((ScatterplotAxesRenderer) renderer)
					.getXColumn().getName();
			String yColumnName = ((ScatterplotAxesRenderer) renderer)
					.getYColumn().getName();

			HashMap<String, BufferedImage> rowAxesSPlots = rowAxesSPlotMap
					.get(xColumnName);
			if (rowAxesSPlots == null) {
				rowAxesSPlots = new HashMap<String, BufferedImage>();
				rowAxesSPlotMap.put(xColumnName, rowAxesSPlots);
			}

			rowAxesSPlots.put(yColumnName, renderer.getRenderedImage());
			repaint();
		}
	}

	public void arrangeColumnsByCorrelation(Column compareColumn,
			boolean useQueryCorrelations) {
		int compareColumnIndex = dataModel.getColumnIndex(compareColumn);

		ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();

		ArrayList<ColumnSortRecord> positiveColumnList = new ArrayList<ColumnSortRecord>();
		ArrayList<ColumnSortRecord> negativeColumnList = new ArrayList<ColumnSortRecord>();

		for (int i = 0; i < axisList.size(); i++) {
			PCAxis axis = axisList.get(i);
			if (axis.column == compareColumn) {
				continue;
			}

			double corrCoefficient;
			if (useQueryCorrelations) {
				corrCoefficient = dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column).getCorrelationCoefficients().get(compareColumnIndex);
			} else {
				corrCoefficient = axis.column.getSummaryStats().getCorrelationCoefficients().get(compareColumnIndex);
			}

			ColumnSortRecord columnRecord = new ColumnSortRecord(axis.column,
					corrCoefficient);
			if (corrCoefficient < 0.) {
				negativeColumnList.add(columnRecord);
			} else {
				positiveColumnList.add(columnRecord);
			}
		}

		if (!negativeColumnList.isEmpty()) {
			Object sortedRecords[] = negativeColumnList.toArray();
			Arrays.sort(sortedRecords);

			for (int i = 0; i < sortedRecords.length; i++) {
				ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
				newAxisList.add(new PCAxis(sortRecord.column, dataModel
						.getColumnIndex(sortRecord.column)));
			}
		}

		newAxisList.add(new PCAxis(compareColumn, compareColumnIndex));

		if (!positiveColumnList.isEmpty()) {
			Object sortedRecords[] = positiveColumnList.toArray();
			Arrays.sort(sortedRecords);

			for (int i = 0; i < sortedRecords.length; i++) {
				ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
				newAxisList.add(new PCAxis(sortRecord.column, dataModel
						.getColumnIndex(sortRecord.column)));
			}
		}

		axisList = newAxisList;

		layoutAxes();
		recalculatePolylines();
		startAxesImageRenderer();
		startContextLineRenderer();
		startFocusLineRenderer();
		startScatterplotRenderers();
	}

	public void arrangeColumnsByTypical(boolean useQueryLines) {
		ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();

		ArrayList<ColumnSortRecord> sortList = new ArrayList<ColumnSortRecord>();
		for (int i = 0; i < axisList.size(); i++) {
			PCAxis axis = axisList.get(i);

			double typical = Double.NaN;
			if (this.getDispersionDisplayMode() == PCPanel.MEAN_DISPERSION_BOX_MODE) {
				if (useQueryLines) {
					typical = axis.queryMeanPosition;
				} else {
					typical = axis.meanPosition;
				}
			} else if (getDispersionDisplayMode() == MEDIAN_DISPERSION_BOX_MODE) {
				if (useQueryLines) {
					typical = axis.queryMedianPosition;
				} else {
					typical = axis.medianPosition;
				}
			}
			if (!Double.isNaN(typical)) {
				sortList.add(new ColumnSortRecord(axis.column, typical));
			} else {
				sortList.add(new ColumnSortRecord(axis.column, Double.MAX_VALUE));
			}
		}

		Object sortedRecords[] = sortList.toArray();
		Arrays.sort(sortedRecords);

		for (int i = 0; i < sortedRecords.length; i++) {
			ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
			newAxisList.add(new PCAxis(sortRecord.column, dataModel
					.getColumnIndex(sortRecord.column)));
		}

		axisList = newAxisList;

		layoutAxes();
		recalculatePolylines();
		startAxesImageRenderer();
		startContextLineRenderer();
		startFocusLineRenderer();
		startScatterplotRenderers();
	}

	public void arrangeColumnsByTypicalDifference() {
		if (dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections()) {
			ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();

			ArrayList<ColumnSortRecord> sortList = new ArrayList<ColumnSortRecord>();
			for (int i = 0; i < axisList.size(); i++) {
				PCAxis axis = axisList.get(i);

				double typicalDiff = Double.NaN;
				if (this.getDispersionDisplayMode() == PCPanel.MEAN_DISPERSION_BOX_MODE) {
					typicalDiff = axis.meanPosition - axis.queryMeanPosition;
				} else if (getDispersionDisplayMode() == MEDIAN_DISPERSION_BOX_MODE) {
					typicalDiff = axis.medianPosition - axis.queryMedianPosition;
				}
				if (!Double.isNaN(typicalDiff)) {
					sortList.add(new ColumnSortRecord(axis.column, typicalDiff));
				} else {
					sortList.add(new ColumnSortRecord(axis.column, Double.MAX_VALUE));
				}
			}

			Object sortedRecords[] = sortList.toArray();
			Arrays.sort(sortedRecords);

			for (int i = 0; i < sortedRecords.length; i++) {
				ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
				newAxisList.add(new PCAxis(sortRecord.column, dataModel
						.getColumnIndex(sortRecord.column)));
			}

			axisList = newAxisList;

			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public void arrangeColumnsByDispersionDifference() {
		if (dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections()) {
	
			ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();
	
			ArrayList<ColumnSortRecord> sortList = new ArrayList<ColumnSortRecord>();
			for (int i = 0; i < axisList.size(); i++) {
				PCAxis axis = axisList.get(i);
	
				double dispersionDiff = Double.NaN;
				if (this.getDispersionDisplayMode() == PCPanel.MEAN_DISPERSION_BOX_MODE) {
					dispersionDiff = axis.standardDeviationRangeRectangle.height
							- axis.queryStandardDeviationRangeRectangle.height;
				} else if (getDispersionDisplayMode() == MEDIAN_DISPERSION_BOX_MODE) {
					dispersionDiff = axis.IQRBoxRectangle.height
							- axis.QueryIQRBoxRectangle.height;
				}
				if (!Double.isNaN(dispersionDiff)) {
					sortList.add(new ColumnSortRecord(axis.column, dispersionDiff));
				} else {
					sortList.add(new ColumnSortRecord(axis.column, Double.MAX_VALUE));
				}
			}
	
			Object sortedRecords[] = sortList.toArray();
			Arrays.sort(sortedRecords);
	
			for (int i = 0; i < sortedRecords.length; i++) {
				ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
				newAxisList.add(new PCAxis(sortRecord.column, dataModel
						.getColumnIndex(sortRecord.column)));
			}
	
			axisList = newAxisList;
	
			layoutAxes();
			recalculatePolylines();
			startAxesImageRenderer();
			startContextLineRenderer();
			startFocusLineRenderer();
			startScatterplotRenderers();
		}
	}

	public void arrangeColumnsByDispersion(boolean useQueryLines) {
		ArrayList<PCAxis> newAxisList = new ArrayList<PCAxis>();

		ArrayList<ColumnSortRecord> sortList = new ArrayList<ColumnSortRecord>();
		for (int i = 0; i < axisList.size(); i++) {
			PCAxis axis = axisList.get(i);

			double dispersion = Double.NaN;
			if (this.getDispersionDisplayMode() == PCPanel.MEAN_DISPERSION_BOX_MODE) {
				if (useQueryLines) {
					dispersion = axis.queryStandardDeviationRangeRectangle.height;
				} else {
					dispersion = axis.standardDeviationRangeRectangle.height;
				}
			} else if (getDispersionDisplayMode() == MEDIAN_DISPERSION_BOX_MODE) {
				if (useQueryLines) {
					dispersion = axis.QueryIQRBoxRectangle.height;
				} else {
					dispersion = axis.IQRBoxRectangle.height;
				}
			}
			if (!Double.isNaN(dispersion)) {
				sortList.add(new ColumnSortRecord(axis.column, dispersion));
			} else {
				sortList.add(new ColumnSortRecord(axis.column, Double.MAX_VALUE));
			}
		}

		Object sortedRecords[] = sortList.toArray();
		Arrays.sort(sortedRecords);

		for (int i = 0; i < sortedRecords.length; i++) {
			ColumnSortRecord sortRecord = (ColumnSortRecord) sortedRecords[i];
			newAxisList.add(new PCAxis(sortRecord.column, dataModel
					.getColumnIndex(sortRecord.column)));
		}

		axisList = newAxisList;

		layoutAxes();
		recalculatePolylines();
		startAxesImageRenderer();
		startContextLineRenderer();
		startFocusLineRenderer();
		startScatterplotRenderers();
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
		startAxesImageRenderer();
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		axesImage = focusImage = contextImage = null;

		layoutAxes();
		recalculateQueryBoxes();
		startAxesImageRenderer();
		startFocusLineRenderer();
		startScatterplotRenderers();
		startContextLineRenderer();
	}
	
	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		if (draggingAxisSelection != null) {
			// mouse drag method will add new pc axis selection
			return;
		}
		
		// find axis for column of column selection range
		PCAxis axis = null;
		for (PCAxis currentAxis : axisList) {
			if (currentAxis.column == columnSelectionRange.getColumnSelection().getColumn()) {
				axis = currentAxis;
				break;
			}
		}
		
		if (axis == null) {
			log.debug("Didn't find axis for column of selection range");
			return;
		}
		
		// add new pc axis selection
		PCAxisSelection axisSelection = new PCAxisSelection(columnSelectionRange);
		axis.axisSelectionList.add(axisSelection);
        double normValue = (axisSelection.getColumnSelectionRange().getMaxValue() - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
        int maxPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
        axisSelection.setMaxPosition(maxPosition);
        normValue = (axisSelection.getColumnSelectionRange().getMinValue() - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
        int minPosition = axis.bottomPosition - (int) (normValue * axis.axisHeight);
        axisSelection.setMinPosition(minPosition);
        
        layoutAxes();
		recalculateQueryBoxes();
		startAxesImageRenderer();
		startFocusLineRenderer();
		startScatterplotRenderers();
		startContextLineRenderer();
	}
	
	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		log.debug("entered dataModelColumnSelectionRemoved");
		for (PCAxis axis : axisList) {
			if (axis.column == columnSelectionRange.getColumnSelection().getColumn()) {
				if (!axis.axisSelectionList.isEmpty()) {
					for (PCAxisSelection axisSelection : axis.axisSelectionList) {
						if (axisSelection.getColumnSelectionRange() == columnSelectionRange) {
							log.debug("Removing axis selection");
							axis.axisSelectionList.remove(axisSelection);
							startAxesImageRenderer();
							startFocusLineRenderer();
							startScatterplotRenderers();
							startContextLineRenderer();
							return;
						}
					}
					log.debug("Something strange happened. A columne selection was removed "
							+ "but I cannot find it in the axis selection list. Possibly an error.");
				}
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent event) {
		// Timer finished?
		if (event.getSource() == this.waitingTimer) {
			// Stop timer
			this.waitingTimer.stop();
			this.waitingTimer = null;
			// Resize
			this.applyResize();
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
		if (event.getSource() instanceof ScatterPlotFrame) {
			scatterplotFrameList.remove((ScatterPlotFrame) event.getSource());
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
}
