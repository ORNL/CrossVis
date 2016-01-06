package gov.ornl.csed.cda.hypervis;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.ColumnSelectionRange;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.DataModelListener;
import gov.ornl.csed.cda.datatable.SummaryStats;
import gov.ornl.csed.cda.datatable.Tuple;
import gov.ornl.csed.cda.pcvis.PCAxis;
import gov.ornl.csed.cda.pcvis.PCAxisSelection;
import gov.ornl.csed.cda.pcvis.ScatterPlotFrame;
import gov.ornl.csed.cda.pcvis.ScatterplotConfiguration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by csg on 8/9/14.
 */
public class HyperVariatePanel extends JPanel implements DataModelListener, ComponentListener, MouseWheelListener, MouseMotionListener, MouseListener, KeyListener {
    private final static Logger log = LoggerFactory.getLogger(HyperVariatePanel.class);

    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0#######");
    public final static int DEFAULT_AXIS_LABEL_HEIGHT = 16;
    public final static int DEFAULT_AXIS_INFOBAR_HEIGHT = 14;
    public final static float DEFAULT_NAME_FONT_SIZE = 12f;
    public final static float DEFAULT_VALUE_FONT_SIZE = 10f;

    public final static Color DEFAULT_AXIS_COLOR = new Color(120,120,120);
    public final static Color DEFAULT_HISTOGRAM_FILL_COLOR = new Color(107, 174, 214);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL_COLOR = new Color(8, 81, 156);
    public final static Color DEFAULT_QUERY_TUPLE_LINE_COLOR = new Color(8, 81, 156, 70);

    private HashMap<Column, BufferedImage> columnImageMap = new HashMap<Column, BufferedImage>();

    private Timer waitingTimer;

    private int axisHeight = 76;
    private Insets axisMargins = new Insets(4, 2, 6, 2);
    private DataModel dataModel;
    private ArrayList<HyperVariateAxis> axisList;
    private HashMap<Column, HyperVariateAxis>columnAxisMap = new HashMap<Column, HyperVariateAxis>();
    private int imageHeight;
    private int imageWidth;
    private Rectangle plotRectangle = null;

    private ArrayList<HyperVariatePanelListener> listeners = new ArrayList<HyperVariatePanelListener>();
    private TreeMap<String, Integer> highlightedAxisMap = new TreeMap<String, Integer>();

    // mouse drag/hover variables
    Point startDragPoint = new Point();
    Point endDragPoint = new Point();
    boolean dragging = false;
    private PCAxisSelection mouseOverAxisSelection = null;
    private PCAxisSelection draggingAxisSelection = null;
    HyperVariateAxis mouseOverAxis = null;
    private boolean mouseOverAxisName = false;
    private boolean mouseOverAxisHistogramPlot = false;
    private Point mouseOverAxisPoint = null;
    private double mouseOverAxisPointValue = Double.NaN;
    private boolean mouseOverAxisDragHandle;
    private boolean draggingAxis = false;
    private int draggingNewAxisIndex;
    private int draggingAxisYOffset = 0;
    private boolean mouseOverAxisCorrelationIndicator;
    private boolean mouseOverAxisQueryCorrelationIndicator;
//    private boolean mouseOverAxisTimeSeries;
//    private double mouseOverAxisTimeSeriesValue;

    private ArrayList<Column> selectedColumns = new ArrayList<Column>();
    private HashMap<Column, BufferedImage> pcpImageMap = new HashMap<Column, BufferedImage>();
    private HashMap<Column, BivariateSeries> bivariateSeriesHashMap = new HashMap<Column, BivariateSeries>();
    private HashMap<Column, BufferedImage> heatmapImageMap = new HashMap<Column, BufferedImage>();
//    private HashMap<Column, TimeSeries> timeSeriesMap = new HashMap<Column, TimeSeries>();
//    private HashMap<Column, BufferedImage> timeSeriesImageMap = new HashMap<Column, BufferedImage>();
    private boolean mouseOverAxisHeatmap = false;
    private Rectangle2D mouseOverOverallHistogramBinRectangle;
    private Rectangle2D mouseOverQueryHistogramBinRectangle;

    private boolean drawQueriedDataLines = false;
    private ArrayList<Path2D> tupleLineList = new ArrayList<Path2D>();
//    private TimeSeriesRecord mouseOverAxisTimeSeriesRecord;
    private boolean mouseOverPCP = false;


    public HyperVariatePanel(DataModel dataModel) {
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.dataModel = dataModel;
        this.dataModel.addDataModelListener(this);

        initialize();
    }

    public void setDrawQueriedDataLines(boolean drawQueriedDataLines) {
        if (this.drawQueriedDataLines != drawQueriedDataLines) {
            this.drawQueriedDataLines = drawQueriedDataLines;
            generateQueriedDataLines();
            repaint();
        }
    }

    public boolean getDrawQueriedDataLines() {
        return drawQueriedDataLines;
    }

    private void generateQueriedDataLines() {
        tupleLineList.clear();
        if (drawQueriedDataLines) {

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                ArrayList<Tuple> tuples = dataModel.getQueriedTuples();

                for (Tuple tuple : tuples) {
                    Path2D.Double tupleLine = new Path2D.Double();

                    for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                        HyperVariateAxis axis = axisList.get(iaxis);
                        double yPosition = axis.axisPlotDividerLinePosition;
                        //                    SummaryStats columnQueryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column);
                        double elementValue = tuple.getElement(iaxis);
                        double normValue = (elementValue - axis.column.getSummaryStats().getMin()) /
                                (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
                        double xPosition = axis.histogramPlotRegionRectangle.getX() + (normValue * axis.histogramPlotRegionRectangle.getWidth());
                        if (iaxis == 0) {
                            tupleLine.moveTo(xPosition, yPosition);
                        } else {
                            tupleLine.lineTo(xPosition, yPosition);
                        }
                    }

                    tupleLineList.add(tupleLine);
                }
            }
        }
    }

    private void fireAxisClicked (int axisIndex) {
        for (HyperVariatePanelListener listener : listeners) {
            listener.hyperVariatePanelAxisClicked(this, axisIndex);
        }
    }

    private void firePolylineClicked (int x, int y) {
        for (HyperVariatePanelListener listener : listeners) {
            listener.hyperVariatePanelPolylineClicked(this, x, y);
        }
    }

    public void addHyperPCPanelListener (HyperVariatePanelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeHyperPCPanelListener (HyperVariatePanelListener listener) {
        listeners.remove(listener);
    }

    private void initialize() {
        layoutAxes();
        renderAxes();
    }

    private void renderAxes() {
        if (dataModel.isEmpty()) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            log.debug("Height or width of component is zero so ignoring panel image rendering");
            return;
        }

        for (HyperVariateAxis axis : axisList) {
            BufferedImage axisImage = axis.draw();
            columnImageMap.put(axis.column, axisImage);
        }
    }

    private BufferedImage renderAxesOld() {
        if (dataModel.isEmpty()) {
            return null;
        }

        if(getWidth() == 0 || getHeight() == 0) {
            log.debug("Height or width of component is zero so ignoring panel image rendering");
            return null;
        }

        BufferedImage image = new BufferedImage(imageWidth+1, imageHeight+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (HyperVariateAxis axis : axisList) {
            BufferedImage axisImage = axis.draw();
            columnImageMap.put(axis.column, axisImage);
            g2.drawImage(axisImage, null, axis.fullPlotRectangle.x, axis.fullPlotRectangle.y);
            /*
            // draw axis bar and edge lines
            g2.setColor(DEFAULT_AXIS_COLOR);
            g2.setStroke(new BasicStroke(2.f));
            g2.drawLine(axis.plotLeft-1, axis.axisPlotDividerLinePosition, axis.plotRight+1, axis.axisPlotDividerLinePosition);
            g2.drawLine(axis.plotLeft-1, axis.upperPlotLabelBarRectangle.y, axis.plotLeft-1,
                    axis.drawableRegionRectangle.y+axis.drawableRegionRectangle.height);
            g2.drawLine(axis.plotRight+1, axis.upperPlotLabelBarRectangle.y, axis.plotRight+1,
                    axis.drawableRegionRectangle.y+axis.drawableRegionRectangle.height);

            // draw overall variable dispersion box and typical value
            g2.setStroke(new BasicStroke(1.f));
            g2.setColor(DEFAULT_DISPERSION_BOX_FILL_COLOR);
            g2.fill(axis.overallStandardDeviationRectangle);
            g2.setColor(DEFAULT_DISPERSION_BOX_LINE_COLOR);
            g2.draw(axis.overallStandardDeviationRectangle);
            g2.setStroke(new BasicStroke(3.f));
            g2.drawLine(axis.meanPosition, axis.overallStandardDeviationRectangle.y, axis.meanPosition,
                    axis.overallStandardDeviationRectangle.y + axis.overallStandardDeviationRectangle.height);

            // draw overall histograms
            g2.setStroke(new BasicStroke(1.f));
            g2.setColor(DEFAULT_HISTOGRAM_FILL_COLOR);
            for (int i = 0; i < axis.overallHistogramBinRectangles.size(); i++) {
                Rectangle2D binRect = axis.overallHistogramBinRectangles.get(i);
                g2.fill(binRect);
            }

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                // draw query dispersion box and typical value
                g2.setStroke(new BasicStroke(1.f));
                g2.setColor(DEFAULT_QUERY_DISPERSION_BOX_FILL_COLOR);
                g2.fill(axis.queryStandardDeviationRectangle);
                g2.setColor(DEFAULT_QUERY_DISPERSION_BOX_LINE_COLOR);
                g2.draw(axis.queryStandardDeviationRectangle);
                g2.setStroke(new BasicStroke(3.f));
                g2.drawLine(axis.queryMeanPosition, axis.queryStandardDeviationRectangle.y, axis.queryMeanPosition,
                        axis.queryStandardDeviationRectangle.y + axis.queryStandardDeviationRectangle.height);

                // draw query histograms
                g2.setStroke(new BasicStroke(1.f));
                g2.setColor(DEFAULT_QUERY_HISTOGRAM_FILL_COLOR);
                for (int i = 0; i < axis.queryHistogramBinRectangles.size(); i++) {
                    Rectangle2D binRect = axis.queryHistogramBinRectangles.get(i);
                    g2.fill(binRect);
                }
            }

            //draw min value string
            // calculate rectangle for min value string label
            g2.setFont(g2.getFont().deriveFont(DEFAULT_VALUE_FONT_SIZE));
            String valueString = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMin());
            int stringWidth = g2.getFontMetrics().stringWidth(valueString);
            axis.minValueLabelRectangle = new Rectangle(axis.upperPlotLabelBarRectangle.x+2, axis.upperPlotLabelBarRectangle.y,
                    stringWidth, axis.upperPlotLabelBarRectangle.height);

            //draw max value string
            // calculate rectangle for max value string label
            valueString = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMax());
            stringWidth = g2.getFontMetrics().stringWidth(valueString);
            axis.maxValueLabelRectangle = new Rectangle((axis.upperPlotLabelBarRectangle.x+axis.upperPlotLabelBarRectangle.width) - stringWidth - 2,
                    axis.upperPlotLabelBarRectangle.y, stringWidth, axis.upperPlotLabelBarRectangle.height);
                    */
        }

        return image;
    }

    private int valueToAxisY(double value, PCAxis axis, boolean clamp) {
        double normValue = (value - axis.column.getSummaryStats().getMin()) / (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin());
        int screenPosition = axis.leftPosition + (int)(normValue * (axis.rightPosition - axis.leftPosition));

        if (clamp) {
            screenPosition = screenPosition < axis.leftPosition ? axis.leftPosition : screenPosition;
            screenPosition = screenPosition > axis.rightPosition ? axis.rightPosition : screenPosition;
        }

        return screenPosition;
    }

    private void layoutAxes() {
        if (dataModel.isEmpty()) {
            return;
        }

        if (axisList == null) {
            axisList = new ArrayList<HyperVariateAxis>();
            for (int i = 0; i < dataModel.getColumnCount(); i++) {
                Column column = dataModel.getColumn(i);
                if (column.isEnabled()) {
                    HyperVariateAxis axis = new HyperVariateAxis(column, dataModel);
                    axisList.add(axis);
                    columnAxisMap.put(column, axis);
                }
            }
        }

        // calculate and set the height of the offscreen image
        Insets insets = getInsets();
        imageWidth = getWidth() - (insets.left + insets.right);
        imageHeight = (axisList.size() * axisHeight) + (insets.top + insets.bottom);
        setPreferredSize(new Dimension(imageWidth, imageHeight));
        revalidate();

        // calculate common dimensions for all axes
        plotRectangle = new Rectangle(insets.left, insets.top, getWidth() - (insets.left + insets.right), (axisList.size() * axisHeight)); // full visualization region
//        Insets axisMargins = new Insets(axisMargin, axisMargin, axisMargin, axisMargin);

        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            HyperVariateAxis axis = axisList.get(iaxis);

            SummaryStats columnQueryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column);

            Rectangle axisFullPlotRectangle = new Rectangle(0, (iaxis * axisHeight), plotRectangle.width, axisHeight);

            axis.layoutAxis(axisFullPlotRectangle, axisMargins, DEFAULT_AXIS_LABEL_HEIGHT, DEFAULT_AXIS_INFOBAR_HEIGHT);
        }
    }

    public void putHighlightedAxis (int axisIndex, String label) {
        highlightedAxisMap.put(label, axisIndex);
        repaint();
    }

    public void removeHighlightedAxis (String label) {
        highlightedAxisMap.remove(label);
        repaint();
    }

    public void removeAllHighlightedAxes () {
        highlightedAxisMap.clear();
        repaint();
    }

    private void drawVariablePlots(Graphics2D g2, HyperVariateAxis axis) {
        // if this is the highlighted variable, draw background with emphasis
        if (dataModel.getHighlightedColumn() == axis.column) {
            g2.setColor(new Color(150,192,230));
            g2.draw(axis.fullPlotRoundedRectangle);
        }

        // draw histogram plot image
        BufferedImage image = columnImageMap.get(axis.column);
        g2.drawImage(image, axis.fullPlotRectangle.x, axis.fullPlotRectangle.y, this);

        // draw time series image
//        BufferedImage timeSeriesImage = timeSeriesImageMap.get(axis.column);
//        if (timeSeriesImage != null) {
//            g2.drawImage(timeSeriesImage, axis.timeSeriesRectangle.x, axis.timeSeriesRectangle.y, this);
//        }

        // draw binned scatterplot image
        BufferedImage heatmapImage = heatmapImageMap.get(axis.column);
        if (heatmapImage != null) {
            g2.drawImage(heatmapImage, axis.heatmapRectangle.x, axis.heatmapRectangle.y, this);
            g2.setColor(Color.darkGray);
            g2.drawString(dataModel.getHighlightedColumn().getName(), axis.heatmapRectangle.x + 1, axis.heatmapRectangle.y+axis.heatmapRectangle.height+4);
        }

        // draw binned pcp image
        BufferedImage pcpImage = pcpImageMap.get(axis.column);
        if (pcpImage != null) {
            g2.drawImage(pcpImage, axis.pcpRectangle.x, axis.pcpRectangle.y, this);
        }

        // draw variable name
        if (dataModel.getHighlightedColumn() == axis.column) {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, DEFAULT_NAME_FONT_SIZE));
            //                g2.setColor(Color.gray);
            //                g2.fill(axis.nameRectangle);
            g2.setColor(Color.BLACK);
            g2.drawString(axis.column.getName(), axis.nameRectangle.x + 1, (axis.nameRectangle.y + axis.nameRectangle.height) - g2.getFontMetrics().getDescent());
        } else {
            g2.setFont(g2.getFont().deriveFont(DEFAULT_NAME_FONT_SIZE));
            g2.setColor(Color.black);
            g2.drawString(axis.column.getName(), axis.nameRectangle.x + 1, (axis.nameRectangle.y + axis.nameRectangle.height) - g2.getFontMetrics().getDescent());
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        Font normalFont = g2.getFont().deriveFont(Font.PLAIN, 10.f);
        g2.setFont(normalFont);

        g2.translate(getInsets().left, getInsets().top);
        if (!columnImageMap.isEmpty()) {
            for (HyperVariateAxis axis : axisList) {
                if ( !(draggingAxis && mouseOverAxis == axis) ) {
                    g2.setFont(normalFont);
                    drawVariablePlots(g2, axis);
                }
            }
        }

        if (!tupleLineList.isEmpty()) {
            g2.setColor(DEFAULT_QUERY_TUPLE_LINE_COLOR);
            g2.setStroke(new BasicStroke(1.f));
            for (Path2D tupleLine : tupleLineList) {
                g2.draw(tupleLine);
            }
        }

        g2.translate(-getInsets().left, -getInsets().top);

        // TODO: make the code below work without translating back the insets
        boolean drawMinLabel = true;
        boolean drawMaxLabel = true;
        if (mouseOverAxis != null) {
            if (mouseOverAxisHistogramPlot) {
                if (mouseOverAxisPoint != null && !dragging) {
                    g2.setColor(Color.black);
                    g2.drawLine(mouseOverAxisPoint.x, mouseOverAxis.histogramPlotRegionRectangle.y + getInsets().top,
                            mouseOverAxisPoint.x,
                            mouseOverAxis.histogramPlotRegionRectangle.y+mouseOverAxis.histogramPlotRegionRectangle.height + getInsets().top);
                    String valueString = DECIMAL_FORMAT.format(mouseOverAxisPointValue);
                    g2.setFont(g2.getFont().deriveFont(DEFAULT_VALUE_FONT_SIZE));
                    int stringWidth = g2.getFontMetrics().stringWidth(valueString);
                    int strX = mouseOverAxisPoint.x - getInsets().left - (stringWidth/2);
                    if (strX < mouseOverAxis.upperPlotLabelBarRectangle.x) {
                        strX = mouseOverAxis.upperPlotLabelBarRectangle.x;
                    } else if ((mouseOverAxisPoint.x + (stringWidth/2)) > (mouseOverAxis.upperPlotLabelBarRectangle.x + mouseOverAxis.upperPlotLabelBarRectangle.width)) {
                        strX = (mouseOverAxis.upperPlotLabelBarRectangle.x + mouseOverAxis.upperPlotLabelBarRectangle.width) - stringWidth;
                    }

                    // determine if the mouse over value will overlap min or max label
                    // if it does, don't draw the min and/or max label
                    if ((strX - getInsets().left) < (mouseOverAxis.minValueLabelRectangle.x + mouseOverAxis.minValueLabelRectangle.width - getInsets().left)) {
                        drawMinLabel = false;
                    } else if ((strX + stringWidth - getInsets().left) > (mouseOverAxis.maxValueLabelRectangle.x - getInsets().left)) {
                        drawMaxLabel = false;
                    }

                    g2.drawString(valueString, strX + getInsets().left,
                            (mouseOverAxis.upperPlotLabelBarRectangle.y + mouseOverAxis.upperPlotLabelBarRectangle.height) + getInsets().top - 4);
                }
//            } else if (mouseOverAxisTimeSeries) {
//                g2.setColor(Color.black);
//
//                // determine translation distances to account for insets and time series plot location
//                double translateX = getInsets().left + mouseOverAxis.timeSeriesRectangle.getX();
//                double translateY = getInsets().top + mouseOverAxis.timeSeriesRectangle.getY();
//
//                // draw vertical line of the time series for which the mouse cursor is hovering over
//                // (note this snaps to the nearest integer time value)
//                // TODO: How to handle time more generally like real numbers or DateTime objects
//                Line2D.Double mouseCursorVerticalLine = new Line2D.Double(
//                        mouseOverAxisTimeSeriesRecord.verticalLine.getX1() + translateX,
//                        mouseOverAxisTimeSeriesRecord.verticalLine.getY1() + translateY,
//                        mouseOverAxisTimeSeriesRecord.verticalLine.getX2() + translateX,
//                        mouseOverAxisTimeSeriesRecord.verticalLine.getY2() + translateY);
//                g2.draw(mouseCursorVerticalLine);
//
//                // draw the information about the time record for the mouse hover location
//                String cursorString = String.valueOf(mouseOverAxisTimeSeriesRecord.time);
//                g2.setFont(g2.getFont().deriveFont(DEFAULT_VALUE_FONT_SIZE));
//                int stringWidth = g2.getFontMetrics().stringWidth(cursorString);
//                int strX = (int)(mouseCursorVerticalLine.getX1() - (stringWidth/2.));
//                if (strX < mouseOverAxis.timeSeriesRectangle.x + getInsets().left) {
//                    strX = mouseOverAxis.timeSeriesRectangle.x + getInsets().left;
//                } else if ((strX + stringWidth) > (mouseOverAxis.timeSeriesRectangle.x + mouseOverAxis.timeSeriesRectangle.width + getInsets().left)) {
//                    strX = (mouseOverAxis.timeSeriesRectangle.x + mouseOverAxis.timeSeriesRectangle.width + getInsets().left) - stringWidth;
////                } else if ((mouseCursorVerticalLine.getX1() + (stringWidth/2)) > (mouseOverAxis.timeSeriesRectangle.x + mouseOverAxis.timeSeriesRectangle.width)) {
////                    strX = (mouseOverAxis.timeSeriesRectangle.x + mouseOverAxis.timeSeriesRectangle.width) - stringWidth;
//                }
//                g2.drawString(cursorString, strX,
//                        (mouseOverAxis.upperPlotLabelBarRectangle.y + mouseOverAxis.upperPlotLabelBarRectangle.height) + getInsets().top - 4);
            }
        }

        g2.translate(getInsets().left, getInsets().top);

        if (axisList != null) {
            for (int i = 0; i < axisList.size(); i++) {
                HyperVariateAxis axis = axisList.get(i);

                if ((draggingAxis && mouseOverAxis == axis)) {
                    continue;
                }

                if (!axis.axisSelectionList.isEmpty()) {
                    for (PCAxisSelection axisSelection : axis.axisSelectionList) {
                        //                    log.debug("drawing axis selection");
                        //                    log.debug("Selection range translation:  minPosition: " + axisSelection.getMinPosition()+ " maxPosition: " + axisSelection.getMaxPosition());

                        RoundRectangle2D.Double queryRect = new RoundRectangle2D.Double(axisSelection.getMinPosition(), axis.histogramPlotRegionRectangle.y - 2,
                                axisSelection.getMaxPosition() - axisSelection.getMinPosition(),
                                axis.histogramPlotRegionRectangle.height + 4, 2., 2.);
                        RoundRectangle2D.Double queryRectOutline = new RoundRectangle2D.Double(queryRect.x - 1,
                                queryRect.y - 1, queryRect.width + 2, queryRect.height + 2, 2., 2.);

                        g2.setStroke(new BasicStroke(2.f));
                        g2.setColor(Color.darkGray);
                        g2.draw(queryRectOutline);
                        g2.setColor(Color.orange);
                        g2.draw(queryRect);
                    }
                }

                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, DEFAULT_VALUE_FONT_SIZE));
                g2.setColor(Color.darkGray);
                // draw min value string
                if (mouseOverAxis != axis || drawMinLabel) {
                    String valueString = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMin());
                    g2.drawString(valueString, axis.minValueLabelRectangle.x,
                            axis.minValueLabelRectangle.y + axis.minValueLabelRectangle.height - 4);
                }

                // draw max value string
                if (mouseOverAxis != axis || drawMaxLabel) {
                    String valueString = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMax());
                    g2.drawString(valueString, axis.maxValueLabelRectangle.x,
                            axis.maxValueLabelRectangle.y + axis.maxValueLabelRectangle.height - 4);
                }
            }

            if ((draggingAxis)) {
                g2.translate(0, endDragPoint.y - draggingAxisYOffset - mouseOverAxis.fullPlotRectangle.y);
                drawVariablePlots(g2, mouseOverAxis);
//                BufferedImage axisImage = columnImageMap.get(mouseOverAxis.column);
//                g2.drawImage(axisImage, getInsets().left, endDragPoint.y - draggingAxisYOffset, this);
            } else if (mouseOverAxisDragHandle) {
                g2.setColor(Color.darkGray);
                g2.draw(mouseOverAxis.dragHandleRoundRectangle);
            }
//
//            if (mouseOverAxisCorrelationIndicator && (dataModel.getHighlightedColumn() != null)) {
//                // draw the correlation coefficent
//            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() == 1) {
                if (mouseOverAxisSelection != null) {
                    mouseOverAxis.axisSelectionList.remove(mouseOverAxisSelection);
                    dataModel.clearColumnSelectionRange(mouseOverAxisSelection.getColumnSelectionRange());
                    mouseOverAxisSelection = null;
                    dataModel.setQueriedTuples();
                } else if (mouseOverAxisName) {
                    dataModel.setHighlightedColumn(mouseOverAxis.column);
                }
            } else if (e.getClickCount() == 2) {
                if (mouseOverAxisCorrelationIndicator) {
                    if (dataModel.getHighlightedColumn() != null) {
                        ScatterplotConfiguration focusConfig = new ScatterplotConfiguration();
                        focusConfig.pointColor = DEFAULT_QUERY_HISTOGRAM_FILL_COLOR;
                        focusConfig.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
                        focusConfig.showTickMarks = true;
                        focusConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);
                        ScatterplotConfiguration contextConfig = new ScatterplotConfiguration();
                        contextConfig.pointColor = DEFAULT_HISTOGRAM_FILL_COLOR;
                        contextConfig.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
                        contextConfig.showTickMarks = true;
                        contextConfig.labelFont = new Font("Dialog", Font.PLAIN, 10);
                        ScatterPlotFrame scatterPlotFrame = new ScatterPlotFrame(dataModel, mouseOverAxis.column, dataModel.getHighlightedColumn(), focusConfig, contextConfig);
                        scatterPlotFrame.setVisible(true);
                        scatterPlotFrame.setColumns(mouseOverAxis.column, dataModel.getHighlightedColumn());
                    }
                } else if (mouseOverOverallHistogramBinRectangle != null) {
                    // add a selection for the bin range to the variable in the current active query

//                } else {
                    // show details panel for variable double clicked
//                    VariableDetailFrame variableDetailFrame = new VariableDetailFrame();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startDragPoint.setLocation(e.getX() - getInsets().left, e.getY() - getInsets().top);
        endDragPoint.setLocation(startDragPoint.x, startDragPoint.y);

        if (mouseOverAxisDragHandle) {
            draggingAxisYOffset = startDragPoint.y - mouseOverAxis.fullPlotRectangle.y;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            dragging = false;
            dataModel.setQueriedTuples();
            draggingAxisSelection = null;
            mouseMoved(e);
        } else if (mouseOverAxisDragHandle) {
            draggingAxis = false;
            mouseOverAxisDragHandle = false;
            setCursor(Cursor.getDefaultCursor());
            int originalAxisIndex = dataModel.getColumnIndex(mouseOverAxis.column);

            int newAxisIndex = endDragPoint.y / axisHeight;
            if (newAxisIndex < 0) {
                newAxisIndex = 0;
            }
            if (newAxisIndex > axisList.size()-1) {
                newAxisIndex = axisList.size() - 1;
            }

            if (originalAxisIndex != newAxisIndex) {
                ArrayList<Column> columns = new ArrayList<Column>();
                for (int i = 0; i < dataModel.getColumnCount(); i++) {
                    columns.add(dataModel.getColumn(i));
                }
                Column column = columns.remove(originalAxisIndex);
                columns.add(newAxisIndex, column);
                dataModel.changeColumnOrder(columns);
            } else {
                repaint();
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseOverAxisHistogramPlot) {
            dragging = true;

            // translate mouse point to account for insets offset
            Point translatedMousePoint = new Point(e.getX() - getInsets().left, e.getY() - getInsets().top);

            if (mouseOverAxisSelection != null) {
                // mouse over an axis selection so translate the selection range based on mouse movements
                int deltaX = translatedMousePoint.x - endDragPoint.x;
                endDragPoint.setLocation(translatedMousePoint);

                mouseOverAxisSelection.setMaxPosition(mouseOverAxisSelection.getMaxPosition() + deltaX);
                mouseOverAxisSelection.setMinPosition(mouseOverAxisSelection.getMinPosition() + deltaX);

                if (mouseOverAxisSelection.getMaxPosition() > mouseOverAxis.plotRight) {
                    deltaX = mouseOverAxis.plotRight - mouseOverAxisSelection.getMaxPosition();
                    mouseOverAxisSelection.setMaxPosition(mouseOverAxis.plotRight);
                    mouseOverAxisSelection.setMinPosition(mouseOverAxisSelection.getMinPosition() + deltaX);
                }

                if (mouseOverAxisSelection.getMinPosition() < mouseOverAxis.plotLeft) {
                    deltaX = mouseOverAxisSelection.getMinPosition() - mouseOverAxis.plotLeft;
                    mouseOverAxisSelection.setMaxPosition(mouseOverAxisSelection.getMaxPosition() - deltaX);
                    mouseOverAxisSelection.setMinPosition(mouseOverAxis.plotLeft);
                }

                int dx = mouseOverAxisSelection.getMaxPosition() - mouseOverAxis.histogramPlotRegionRectangle.x;
                double normValue = (double) dx / (double) mouseOverAxis.histogramPlotRegionRectangle.width;
                double maxValue = (normValue * (mouseOverAxis.column.getSummaryStats().getMax() -
                        mouseOverAxis.column.getSummaryStats().getMin())) +
                        mouseOverAxis.column.getSummaryStats().getMin();

                dx = mouseOverAxisSelection.getMinPosition() - mouseOverAxis.histogramPlotRegionRectangle.x;
                normValue = (double) dx / (double) mouseOverAxis.histogramPlotRegionRectangle.width;
                double minValue = (normValue * (mouseOverAxis.column.getSummaryStats().getMax() -
                        mouseOverAxis.column.getSummaryStats().getMin())) +
                        mouseOverAxis.column.getSummaryStats().getMin();

                mouseOverAxisSelection.getColumnSelectionRange().setMinValue(minValue);
                mouseOverAxisSelection.getColumnSelectionRange().setMaxValue(maxValue);

//                log.debug("Selection range translation: deltaX: " + deltaX + " minValue:" + minValue + " maxValue:" + maxValue + " minPosition: " + mouseOverAxisSelection.getMinPosition()+ " maxPosition: " + mouseOverAxisSelection.getMaxPosition());

            } else {
                // an axis selection is being created so update extents of dragging selection
                endDragPoint.setLocation(translatedMousePoint);

                int queryMaxPosition = startDragPoint.x > endDragPoint.x ? startDragPoint.x : endDragPoint.x;
                if (queryMaxPosition > mouseOverAxis.plotRight) {
                    queryMaxPosition = mouseOverAxis.plotRight;
                }

                int queryMinPosition = startDragPoint.x < endDragPoint.x ? startDragPoint.x : endDragPoint.x;
                if (queryMinPosition < mouseOverAxis.plotLeft) {
                    queryMinPosition = mouseOverAxis.plotLeft;
                }

                int dx = queryMaxPosition - mouseOverAxis.histogramPlotRegionRectangle.x;
                double normValue = (double) dx / (double) mouseOverAxis.histogramPlotRegionRectangle.width;
                double maxValue = (normValue * (mouseOverAxis.column.getSummaryStats().getMax() -
                        mouseOverAxis.column.getSummaryStats().getMin())) +
                        mouseOverAxis.column.getSummaryStats().getMin();

                dx = queryMinPosition - mouseOverAxis.histogramPlotRegionRectangle.x;
                normValue = (double) dx / (double) mouseOverAxis.histogramPlotRegionRectangle.width;
                double minValue = (normValue * (mouseOverAxis.column.getSummaryStats().getMax() -
                        mouseOverAxis.column.getSummaryStats().getMin())) +
                        mouseOverAxis.column.getSummaryStats().getMin();

                if (draggingAxisSelection == null) {
                    ColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(mouseOverAxis.column, minValue, maxValue);
                    draggingAxisSelection = new PCAxisSelection(selectionRange);
                    mouseOverAxis.axisSelectionList.add(draggingAxisSelection);
//                    log.debug("created new dragging axis selection");
                } else {
                    draggingAxisSelection.getColumnSelectionRange().setMaxValue(maxValue);
                    draggingAxisSelection.getColumnSelectionRange().setMinValue(minValue);
                }

                draggingAxisSelection.setMinPosition(queryMinPosition);
                draggingAxisSelection.setMaxPosition(queryMaxPosition);
//                log.debug("updated dragging axis selection");
            }
        } else if (mouseOverAxisDragHandle) {
            // mouse is dragging over an axis relocation handle so render axis image as mouse moves
            draggingAxis = true;

            // translate mouse point to account for insets offset
            Point translatedMousePoint = new Point(e.getX() - getInsets().left, e.getY() - getInsets().top);
            endDragPoint.setLocation(translatedMousePoint);

            draggingNewAxisIndex = endDragPoint.y / axisHeight;
            if (draggingNewAxisIndex < 0) {
                draggingNewAxisIndex = 0;
            }
            if (draggingNewAxisIndex > axisList.size()-1) {
                draggingNewAxisIndex = axisList.size() - 1;
            }
        }

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (axisList == null || axisList.isEmpty()) {
            return;
        }

        Cursor newCursor = getCursor();

        mouseOverAxis = null;
        mouseOverAxisName = false;
        mouseOverAxisSelection = null;
        mouseOverAxisHistogramPlot = false;
        mouseOverAxisPoint = null;
        mouseOverAxisPointValue = Double.NaN;
        mouseOverAxisDragHandle = false;
        mouseOverAxisCorrelationIndicator = false;
        mouseOverAxisQueryCorrelationIndicator = false;
        mouseOverAxisHeatmap = false;
        mouseOverOverallHistogramBinRectangle = null;
        mouseOverQueryHistogramBinRectangle = null;
//        mouseOverAxisTimeSeries = false;
//        mouseOverAxisTimeSeriesValue = Float.NaN;
        mouseOverPCP = false;
        setToolTipText("");

        Point translatedMousePoint = e.getPoint();
        translatedMousePoint.translate(-getInsets().left, -getInsets().top);

        for (HyperVariateAxis axis : axisList) {
            if (axis.fullPlotRectangle.contains(translatedMousePoint)) {
                mouseOverAxis = axis;
                mouseOverAxisPoint = e.getPoint();

                if (axis.nameRectangle.contains(translatedMousePoint)) {
                    // Mouse hovering over the axis name
                    mouseOverAxisName = true;
                    newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                } else if (axis.drawableRegionRectangle.contains(translatedMousePoint)) {

                    // Is mouse hovering over the histogram plot of the axis?
                    if (axis.histogramPlotRegionRectangle.contains(translatedMousePoint)) {
//                    if (translatedMousePoint.y >= axis.upperPlotLabelBarRectangle.y &&
//                            translatedMousePoint.y <= (axis.histogramPlotRegionRectangle.y + axis.histogramPlotRegionRectangle.height)) {
                        mouseOverAxisHistogramPlot = true;
                        int dx = translatedMousePoint.x - axis.histogramPlotRegionRectangle.x;
                        double normValue = (double) dx / (double) axis.histogramPlotRegionRectangle.width;
                        mouseOverAxisPointValue = (normValue * (axis.column.getSummaryStats().getMax() - axis.column.getSummaryStats().getMin())) +
                                axis.column.getSummaryStats().getMin();

                        // determine if mouse is hovering over a query axis selection
                        if (!mouseOverAxis.axisSelectionList.isEmpty()) {
                            for (int i = mouseOverAxis.axisSelectionList.size() - 1; i >= 0; i--) {
                                PCAxisSelection selection = mouseOverAxis.axisSelectionList.get(i);
                                if (translatedMousePoint.x >= selection.getMinPosition() &&
                                        translatedMousePoint.x <= selection.getMaxPosition()) {
                                    mouseOverAxisSelection = selection;
                                    break;
                                }
                            }
                        }

                        // determine if mouse if hovering over a histogram bin
                        for (int ibin = 0; ibin < axis.overallHistogramBinRectangles.size(); ibin++) {
                            if (axis.overallHistogramBinRectangles.get(ibin).contains(translatedMousePoint)) {
                                mouseOverOverallHistogramBinRectangle = axis.overallHistogramBinRectangles.get(ibin);
                                setToolTipText("bin count " + (int) axis.column.getSummaryStats().getHistogram().getArray()[ibin]);
                                break;
                            }
                        }

                        // determine if mouse is hovering over a query histogram bin
                        if (axis.queryHistogramBinRectangles != null) {
                            for (int ibin = 0; ibin < axis.queryHistogramBinRectangles.size(); ibin++) {
                                if (axis.queryHistogramBinRectangles.get(ibin).contains(translatedMousePoint)) {
                                    mouseOverQueryHistogramBinRectangle = axis.queryHistogramBinRectangles.get(ibin);
                                    setToolTipText("query bin count " + (int) dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column).getHistogram().getArray()[ibin]);
                                    break;
                                }
                            }
                        }

                        newCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
//                    } else if (axis.column != dataModel.getTimeColumn() && axis.timeSeriesRectangle != null && axis.timeSeriesRectangle.contains(translatedMousePoint)) {
//                        // mouse is hovering over the time series plot
////                        log.debug("Mouse over time series");
//                        mouseOverAxisTimeSeries = true;
//                        int dx = translatedMousePoint.x - axis.timeSeriesRectangle.x;
//                        double normValue = (double) dx / (double) axis.timeSeriesRectangle.width;
//                        mouseOverAxisTimeSeriesValue = (normValue * (dataModel.getTimeColumn().getSummaryStats().getMax() - dataModel.getTimeColumn().getSummaryStats().getMin())) +
//                                dataModel.getTimeColumn().getSummaryStats().getMin();
//                        mouseOverAxisTimeSeriesRecord = timeSeriesMap.get(axis.column).getTimeSeriesRecord((float)mouseOverAxisTimeSeriesValue);
//                        setToolTipText("mean: " + mouseOverAxisTimeSeriesRecord.value + " stdev: " + mouseOverAxisTimeSeriesRecord.statistics.getStandardDeviation());
                    } else if (axis.heatmapRectangle != null && axis.heatmapRectangle.contains(translatedMousePoint)) {
                        // mouse is hovering over the binned scatterplot / heatmap
                        mouseOverAxisHeatmap = true;
                        //TODO: find bin mouse is hovering over and show bin count as a tooltip
                    } else if (axis.pcpRectangle != null && axis.pcpRectangle.contains(translatedMousePoint)) {
                        mouseOverPCP = true;
                        // TODO: find PCP bin mouse is hovering over and show bin count as a tooltip
                    } else if (axis.correlationIndicatorRectangle.contains(translatedMousePoint) && (dataModel.getHighlightedColumn() != null)) {
                        // Mouse hovering over the correlation indicator rectangle
                        mouseOverAxisCorrelationIndicator = true;
                        newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                        int columnIdx = dataModel.getColumnIndex(axis.column);
                        setToolTipText("r=" + dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(columnIdx));
                    } else if (axis.queryCorrelationIndicatorRectangle.contains(translatedMousePoint) &&
                            (dataModel.getHighlightedColumn() != null) &&
                            (dataModel.getActiveQuery().hasColumnSelections())) {
                        // mouse hovering over the query correlation indicator rectangle
                        mouseOverAxisQueryCorrelationIndicator = true;
                        newCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                        int columnIdx = dataModel.getColumnIndex(axis.column);
                        double correlationCoef = dataModel.getActiveQuery().getColumnQuerySummaryStats(dataModel.getHighlightedColumn()).getCorrelationCoefficients().get(columnIdx);
                        setToolTipText("r = " + correlationCoef);
//                        setToolTipText("r=" + dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(columnIdx));
                    }
                } else if (axis.dragHandleRectangle.contains(translatedMousePoint)) {
//                } else if (translatedMousePoint.x >= axis.dragHandleRectangle.x &&
//                        translatedMousePoint.x <= (axis.dragHandleRectangle.x + axis.dragHandleRectangle.width)) {
                    // mouse over the axis relocation/drag handles
//                    log.debug("mouse hovering over axis drag handle");
                    mouseOverAxisDragHandle = true;
                    newCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                }

                if (newCursor != getCursor()) {
                    setCursor(newCursor);
                }
                repaint();
                break;
            }
        }
    }

//    private void layoutTimeSeriesPlots() {
//        if (!timeSeriesMap.isEmpty()) {
//            timeSeriesImageMap.clear();
//
//            for (HyperVariateAxis axis : axisList) {
//                if (axis.column == dataModel.getTimeColumn()) {
//                    continue;
//                }
//
//                TimeSeries timeSeries = timeSeriesMap.get(axis.column);
//                timeSeries.layoutPlot(axis.timeSeriesRectangle.width, axis.timeSeriesRectangle.height);
//
//                BufferedImage image = new BufferedImage(axis.timeSeriesRectangle.width, axis.timeSeriesRectangle.height,
//                        BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g2 = (Graphics2D)image.getGraphics();
//                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                timeSeries.draw(g2);
//                timeSeriesImageMap.put(axis.column, image);
//            }
//        }
//    }

//    private void createTimeSeriesPlots() {
//        timeSeriesMap.clear();
//        timeSeriesImageMap.clear();
//        Column timeColumn = dataModel.getTimeColumn();
//        if (timeColumn == null) {
//            return;
//        }
//
//        for (HyperVariateAxis axis : axisList) {
//            Column column = axis.column;
//            if (column != timeColumn) {
//                TimeSeries timeSeries = new TimeSeries(timeColumn, column, dataModel, axis.timeSeriesRectangle.width,
//                        axis.timeSeriesRectangle.height, new Insets(2,2,2,2));
//                BufferedImage image = new BufferedImage(axis.timeSeriesRectangle.width, axis.timeSeriesRectangle.height,
//                        BufferedImage.TYPE_INT_ARGB);
//                Graphics2D g2 = (Graphics2D)image.getGraphics();
//                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                timeSeries.draw(g2);
//                timeSeriesMap.put(column, timeSeries);
//                timeSeriesImageMap.put(column, image);
//            }
//        }
//    }

    private void createBivariateSets() {
        bivariateSeriesHashMap.clear();
        heatmapImageMap.clear();
        pcpImageMap.clear();

        if (dataModel.getHighlightedColumn() != null) {
            for (HyperVariateAxis axis : axisList) {
                Column column = axis.column;
                if (column != dataModel.getHighlightedColumn()) {
//                    log.debug("creating heatmap for column '" + column.getName() + "'");
                    BivariateSeries bivariateSeries = new BivariateSeries(dataModel.getHighlightedColumn(), column, dataModel, 10, 10,
                            axis.heatmapRectangle.width, axis.heatmapRectangle.height, new Insets(2, 2, 2, 2));
                    bivariateSeriesHashMap.put(column, bivariateSeries);

                    // create heatmap image
                    BufferedImage heatmapImage = new BufferedImage(axis.heatmapRectangle.width, axis.heatmapRectangle.height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = (Graphics2D) heatmapImage.getGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    bivariateSeries.drawHeatmap(g2);
                    heatmapImageMap.put(column, heatmapImage);

                    // create pcpPlot image
                    BufferedImage pcpImage = new BufferedImage(axis.pcpRectangle.width, axis.pcpRectangle.height, BufferedImage.TYPE_INT_ARGB);
                    g2 = (Graphics2D) pcpImage.getGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    bivariateSeries.drawPCPBins(g2);
//                    bivariateSeries.drawPCPLines(g2);
                    pcpImageMap.put(column, pcpImage);
                }
            }
//            log.debug("created " + heatmapImageMap.size() + " heatmaps");
        }
    }

    public ArrayList<Column> getColumnOrder() {
        ArrayList<Column> columns = new ArrayList<Column>();
        for (HyperVariateAxis axis : axisList) {
            columns.add(axis.column);
        }
        return columns;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        log.debug("HyperPCPanel component resized");
        System.out.println("HyperVariatePanel resized");
        layoutAxes();
        renderAxes();
//        layoutTimeSeriesPlots();
        generateQueriedDataLines();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
//        log.debug("KeyEvent is " + e.getKeyCode());
//        log.debug("VK_UP is " + KeyEvent.VK_UP);
//        if (!PCPanelDataModel.isEmpty()) {
//            if (e.getKeyCode() == KeyEvent.VK_UP) {
//                if (PCPanelDataModel.getSelectedImageSliceIndex() < PCPanelDataModel.getNumberOfImageSlices()) {
//                    PCPanelDataModel.setSelectedImageSliceIndex(PCPanelDataModel.getSelectedImageSliceIndex()+1);
//                }
//            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                if (PCPanelDataModel.getSelectedImageSliceIndex() > 0) {
//                    PCPanelDataModel.setSelectedImageSliceIndex(PCPanelDataModel.getSelectedImageSliceIndex()-1);
//                }
//            }
//        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void dataModelChanged(DataModel dataModel) {
        axisList = null;
        layoutAxes();
        renderAxes();
        createBivariateSets();
//        if (dataModel.getTimeColumn() != null) {
//            createTimeSeriesPlots();
//        } else {
//            timeSeriesMap.clear();
//            timeSeriesImageMap.clear();
//        }
        generateQueriedDataLines();
        repaint();
    }



    @Override
    public void queryChanged(DataModel dataModel) {
//        log.debug("query changed in HyperPCPanel entered");
        if (!dragging) {
            layoutAxes();
            renderAxes();
            createBivariateSets();
            generateQueriedDataLines();
        }
        repaint();
    }

    @Override
    public void highlightedColumnChanged(DataModel dataModel) {
        renderAxes();
        createBivariateSets();
        repaint();
    }

    @Override
    public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {

    }

    private void removeDisabledColumnAxes() {
        columnAxisMap.clear();
        ArrayList<HyperVariateAxis> newAxisList = new ArrayList<HyperVariateAxis>();
        for (HyperVariateAxis axis : axisList) {
            if (axis.column.isEnabled()) {
                newAxisList.add(axis);
                columnAxisMap.put(axis.column, axis);
            }
        }
        axisList = newAxisList;
    }

    @Override
    public void columnDisabled(DataModel dataModel, Column disabledColumn) {
        removeDisabledColumnAxes();
        layoutAxes();
        renderAxes();
        createBivariateSets();
        generateQueriedDataLines();
        repaint();
    }

    @Override
    public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {
        removeDisabledColumnAxes();
        layoutAxes();
        renderAxes();
        createBivariateSets();
        generateQueriedDataLines();
        repaint();
    }

    @Override
    public void columnEnabled(DataModel dataModel, Column enabledColumn) {
        HyperVariateAxis axis = new HyperVariateAxis(enabledColumn, dataModel);
        axisList.add(axis);
        columnAxisMap.put(enabledColumn, axis);
        layoutAxes();
        renderAxes();
        createBivariateSets();
        generateQueriedDataLines();
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        
    }

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		if (!dragging) {
            layoutAxes();
            renderAxes();
            createBivariateSets();
            generateQueriedDataLines();
        }
        repaint();
	}

	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		if (!dragging) {
            layoutAxes();
            renderAxes();
            createBivariateSets();
            generateQueriedDataLines();
        }
        repaint();
	}
}