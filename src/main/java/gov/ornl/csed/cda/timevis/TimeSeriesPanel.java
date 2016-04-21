package gov.ornl.csed.cda.timevis;

import gov.ornl.csed.cda.util.GraphicsUtil;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by csg on 12/2/15.
 */
public class TimeSeriesPanel extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
    private final static Logger log = LoggerFactory.getLogger(TimeSeriesPanel.class);

    private TimeSeries timeSeries;

    private Instant startInstant;
    private Instant endInstant;
    private Instant plotLeftInstant;
    private Instant plotRightInstant;
    private ChronoUnit plotChronoUnit;

    private Rectangle plotRectangle;
    private int timeInfoBarTop;
    private int timeInfoBarBottom;
    private int valueInfoBarTop;
    private int valueInfoBarBottom;

    private Duration totalDuration;

    private int plotNameBarHeight = 14;
    private int timeBarHeight = 14;



    private int valueBarHeight = 14;

    private int plotUnitWidth = 1;
    private int numPlotUnits = 0;
    private int plotUnitDurationMillis = 0;

    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private int hoverX;
    private Instant hoverInstant;
    private double hoverValue = Double.NaN;
    private TimeSeriesSelection hoverTimeSeriesSelection = null;

    private boolean shrinkToFit = false;
    private PlotDisplayOption plotDisplayOption = PlotDisplayOption.POINT;

    private TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap = new TreeMap<>();
    private TimeSeriesSummaryInfo summaryInfoArray[];

    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    private Color dataColor = new Color(80, 80, 130, 180);
    private Color rangeColor = new Color(140, 140, 160, 100);

    private Color selectedRegionFillColor = Color.white;

    private Instant startHighlightInstant;
    private Instant endHighlightInstant;
    private Rectangle2D.Double highlightRectangle;

    private boolean showTimeRangeLabels = true;

    // range selection dragging stuff
    private Point startDragPoint = new Point();
    private Point endDragPoint = new Point();
    private boolean draggingSelection;
    private TimeSeriesSelection draggingTimeSeriesSelecton;

    // range selection data
    private ArrayList<TimeSeriesSelection> selectionList = new ArrayList<>();
    private ArrayList<TimeSeriesPanelSelectionListener> selectionListeners = new ArrayList<>();



    // value axis range
    private double valueAxisMax;
    private double valueAxisMin;

    // not shrink to fit constructor
    public TimeSeriesPanel (int plotUnitWidth, ChronoUnit plotChronoUnit, PlotDisplayOption plotDisplayOption) {
        this.plotUnitWidth = plotUnitWidth;
        this.plotChronoUnit = plotChronoUnit;
        this.plotDisplayOption = plotDisplayOption;
        shrinkToFit = false;

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public double getValueAxisMax() {
        return valueAxisMax;
    }

    public void setValueAxisMax(double valueAxisMax) {
        this.valueAxisMax = valueAxisMax;
        layoutPanel();
    }

    public double getValueAxisMin() {
        return valueAxisMin;
    }

    public void setValueAxisMin(double valueAxisMin) {
        this.valueAxisMin = valueAxisMin;
        layoutPanel();
    }

    public int getValueBarHeight() {
        return valueBarHeight;
    }

    public void setValueBarHeight(int valueBarHeight) {
        this.valueBarHeight = valueBarHeight;
    }

    public int getTimeBarHeight() {
        return timeBarHeight;
    }

    public void setTimeBarHeight(int timeBarHeight) {
        this.timeBarHeight = timeBarHeight;
    }

    public void addTimeSeriesPanelSelectionListener (TimeSeriesPanelSelectionListener listener) {
        if (!selectionListeners.contains(listener)) {
            selectionListeners.add(listener);
        }
    }

    public void removeTimeSeriesPanelSelectionListener (TimeSeriesPanelSelectionListener listener) {
        selectionListeners.remove(listener);
    }

    private void fireSelectionCreated(TimeSeriesSelection selection) {
        for (TimeSeriesPanelSelectionListener listener : selectionListeners) {
            listener.selectionCreated(this, selection);
        }
    }

    private void fireSelectionMoved(TimeSeriesSelection selection) {
        for (TimeSeriesPanelSelectionListener listener : selectionListeners) {
            listener.selectionMoved(this, selection);
        }
    }

    private void fireSelectionDeleted(TimeSeriesSelection selection) {
        for (TimeSeriesPanelSelectionListener listener : selectionListeners) {
            listener.selectionDeleted(this, selection);
        }
    }

    public boolean removeTimeSeriesSelection(TimeSeriesSelection timeSeriesSelection) {
        if (selectionList.remove(timeSeriesSelection)) {
            repaint();
            return true;
        }
        return false;
    }

    // shrink to fit constructor
    public TimeSeriesPanel (int plotUnitWidth, PlotDisplayOption plotDisplayOption) {
        this.plotUnitWidth = plotUnitWidth;
        this.shrinkToFit = true;
        this.plotDisplayOption = plotDisplayOption;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Random random = new Random(System.currentTimeMillis());

//                    int numTimeRecords = 50400;
                    int numTimeRecords = 86400/8;
//                    int numTimeRecords = 1200;
                    int plotUnitWidth = 10;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    TimeSeriesPanel detailsTimeSeriesPanel = new TimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, PlotDisplayOption.LINE);
                    detailsTimeSeriesPanel.setBackground(Color.white);

                    TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(plotUnitWidth, PlotDisplayOption.LINE);
                    overviewTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                    overviewTimeSeriesPanel.setBackground(Color.white);
                    Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                    overviewTimeSeriesPanel.setBorder(border);


                    JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
                    scroller.getVerticalScrollBar().setUnitIncrement(10);
                    scroller.getHorizontalScrollBar().setUnitIncrement(10);
                    scroller.setBackground(frame.getBackground());
                    scroller.setBorder(border);

                    ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                    ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                    ((JPanel)frame.getContentPane()).add(overviewTimeSeriesPanel, BorderLayout.SOUTH);

                    frame.setSize(1000, 400);
                    frame.setVisible(true);

                    TimeSeries timeSeries = new TimeSeries("Test");

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords+120, ChronoUnit.SECONDS);

                    double value = 0.;

                    for (int i = 120; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
                        instant = instant.plusMillis(random.nextInt(1000));
                        value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
                        double range = Math.abs(value) * .25;
                        double upperRange = value + range;
                        double lowerRange = value - range;
                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
                    }

                    overviewTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
                    detailsTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);

                    scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            JScrollBar scrollBar = (JScrollBar)e.getSource();
                            double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();

                            double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                            double deltaTime = norm * Duration.between(overviewTimeSeriesPanel.getStartInstant(), overviewTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant startHighlightInstant = overviewTimeSeriesPanel.getStartInstant().plusMillis((long)deltaTime);

                            int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                            norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                            deltaTime = norm * Duration.between(overviewTimeSeriesPanel.getStartInstant(), overviewTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant endHighlightInstant = overviewTimeSeriesPanel.getEndInstant().minusMillis((long) deltaTime);

                            overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public Color getDataColor() {
        return dataColor;
    }

    public void setDataColor(Color dataColor) {
        this.dataColor = dataColor;
        repaint();
    }

    public int getChronoUnitWidth() {
        return plotUnitWidth;
    }

    public void setChronoUnitWidth(int width) {
        this.plotUnitWidth = width;
        layoutPanel();
    }

    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(TimeSeries timeSeries, Instant startInstant, Instant endInstant) {
        this.timeSeries = timeSeries;
        this.startInstant = startInstant;
        this.endInstant = endInstant;
        totalDuration = Duration.between(startInstant, endInstant);
        valueAxisMin = timeSeries.getMinValue();
        valueAxisMax = timeSeries.getMaxValue();
        layoutPanel();
    }

    public void setDisplayTimeRange (Instant startInstant, Instant endInstant) {
        this.startInstant = Instant.from(startInstant);
        this.endInstant = Instant.from(endInstant);
        totalDuration = Duration.between(this.startInstant, this.endInstant);
        layoutPanel();
    }

    public void setPlotChronoUnit(ChronoUnit chronoUnit) {
        this.plotChronoUnit = chronoUnit;
        layoutPanel();
    }

    public void removeTimeSeries() {
        timeSeries = null;
        startHighlightInstant = null;
        endHighlightInstant = null;
        highlightRectangle = null;
        layoutPanel();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (hoverTimeSeriesSelection != null) {
                selectionList.remove(hoverTimeSeriesSelection);
                fireSelectionDeleted(hoverTimeSeriesSelection);
                hoverTimeSeriesSelection = null;
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startDragPoint.setLocation(e.getX(), e.getY());
        endDragPoint.setLocation(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggingSelection) {
            fireSelectionCreated(draggingTimeSeriesSelecton);
            draggingSelection = false;
            draggingTimeSeriesSelecton = null;
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
        if (hoverTimeSeriesSelection != null) {
            int deltaX = e.getX() - endDragPoint.x;
            endDragPoint.setLocation(e.getPoint());
            hoverTimeSeriesSelection.setStartScreenLocation(hoverTimeSeriesSelection.getStartScreenLocation() + deltaX);
            hoverTimeSeriesSelection.setEndScreenLocation(hoverTimeSeriesSelection.getEndScreenLocation() + deltaX);

            // clamp start location to plot left
            int plotLeft = plotRectangle.x;
            if (hoverTimeSeriesSelection.getStartScreenLocation() < plotLeft) {
                deltaX = plotLeft - hoverTimeSeriesSelection.getStartScreenLocation();
                hoverTimeSeriesSelection.setStartScreenLocation(plotLeft);
                hoverTimeSeriesSelection.setEndScreenLocation(hoverTimeSeriesSelection.getEndScreenLocation() + deltaX);
            }

            // clamp end location to plot right
            int plotRight = plotRectangle.x + plotRectangle.width;
            if (hoverTimeSeriesSelection.getEndScreenLocation() > plotRight) {
                deltaX = hoverTimeSeriesSelection.getEndScreenLocation() - plotRight;
                hoverTimeSeriesSelection.setStartScreenLocation(hoverTimeSeriesSelection.getStartScreenLocation() - deltaX);
                hoverTimeSeriesSelection.setEndScreenLocation(plotRight);
            }

            double selectionStartMillis = GraphicsUtil.mapValue(hoverTimeSeriesSelection.getStartScreenLocation(), plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionStart = Instant.ofEpochMilli((long) selectionStartMillis);
            double selectionEndMillis = GraphicsUtil.mapValue(hoverTimeSeriesSelection.getEndScreenLocation(), plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionEnd = Instant.ofEpochMilli((long) selectionEndMillis);

            hoverTimeSeriesSelection.setStartInstant(selectionStart);
            hoverTimeSeriesSelection.setEndInstant(selectionEnd);

            fireSelectionMoved(hoverTimeSeriesSelection);
        } else {
            draggingSelection = true;
            endDragPoint.setLocation(e.getPoint());

            int leftPosition = startDragPoint.x < endDragPoint.x ? startDragPoint.x : endDragPoint.x;
            if (leftPosition < plotRectangle.x) {
                leftPosition = plotRectangle.x;
            }

            int rightPosition = startDragPoint.x > endDragPoint.x ? startDragPoint.x : endDragPoint.x;
            if (rightPosition > (plotRectangle.x + plotRectangle.width)) {
                rightPosition = plotRectangle.x + plotRectangle.width;
            }

            log.debug("leftPosition: " + leftPosition + " rightPosition: " + rightPosition);

            double selectionStartMillis = GraphicsUtil.mapValue(leftPosition, plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionStart = Instant.ofEpochMilli((long) selectionStartMillis);
            double selectionEndMillis = GraphicsUtil.mapValue(rightPosition, plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionEnd = Instant.ofEpochMilli((long) selectionEndMillis);
            log.debug("Selection start: " + dtFormatter.format(selectionStart) + " end: " + dtFormatter.format(selectionEnd));

            if (draggingTimeSeriesSelecton == null) {
                draggingTimeSeriesSelecton = new TimeSeriesSelection(selectionStart, selectionEnd, leftPosition, rightPosition);
                selectionList.add(draggingTimeSeriesSelecton);
            } else {
                draggingTimeSeriesSelecton.setStartInstant(selectionStart);
                draggingTimeSeriesSelecton.setEndInstant(selectionEnd);
            }

            draggingTimeSeriesSelecton.setStartScreenLocation(leftPosition);
            draggingTimeSeriesSelecton.setEndScreenLocation(rightPosition);
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoverInstant = null;
        hoverX = -1;
        hoverValue = Double.NaN;
        hoverTimeSeriesSelection = null;

        if ((plotRectangle != null) && (plotRectangle.contains(e.getPoint()))) {
            hoverX = e.getX();

            double norm = (double)(hoverX - plotRectangle.x) / (plotRectangle.width);
            long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            hoverInstant = startInstant.plusMillis(deltaTimeMillis);

            if (shrinkToFit) {
                int summaryInfoIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
//                TimeSeriesSummaryInfo summaryInfo = summaryInfoList.get(summaryInfoIndex);
                TimeSeriesSummaryInfo summaryInfo = summaryInfoArray[summaryInfoIndex];
                if (summaryInfo != null) {
                    hoverValue = summaryInfo.meanValue;
                }
            } else {
                Instant rangeStartInstant = hoverInstant.truncatedTo(plotChronoUnit).minus(1, plotChronoUnit);
                Instant rangeEndInstant = hoverInstant.truncatedTo(plotChronoUnit).plus(1, plotChronoUnit);

                ArrayList<TimeSeriesRecord> records = timeSeries.getRecordsBetween(rangeStartInstant, rangeEndInstant);
                if (records != null && !records.isEmpty()) {
                    TimeSeriesRecord nearestRecord = null;
                    Duration nearestRecordDuration = null;
                    // find nearest record to time instant
                    for (TimeSeriesRecord record : records) {
                        Duration duration = Duration.between(hoverInstant, record.instant);
                        if (nearestRecord == null) {
                            nearestRecord = record;
                            nearestRecordDuration = duration;
                        } else if (duration.abs().toMillis() < nearestRecordDuration.abs().toMillis()) {
                            nearestRecord = record;
                            nearestRecordDuration = duration;
                        }
                    }
                    if (nearestRecord != null) {
                        hoverValue = nearestRecord.value;
                    }
                }
            }

            if (!selectionList.isEmpty()) {
                for (TimeSeriesSelection selection : selectionList) {
                    if (e.getX() >= selection.getStartScreenLocation() &&
                            e.getX() <= selection.getEndScreenLocation()) {
                        hoverTimeSeriesSelection = selection;
                        break;
                    }
                }
            }
        }

        repaint();
    }

    public boolean getShrinkToFitOption() {
        return shrinkToFit;
    }

    public void setShrinkToFitOption (boolean enabled) {
        if (this.shrinkToFit != enabled) {
            this.shrinkToFit = enabled;
            layoutPanel();
        }
    }

    public int getPlotUnitWidth () {
        return plotUnitWidth;
    }

    public void setPlotUnitWidth (int width) {
        plotUnitWidth = width;
        layoutPanel();
    }

    public PlotDisplayOption getPlotDisplayOption () {
        return plotDisplayOption;
    }

    public void setPlotDisplayOption (PlotDisplayOption plotDisplayOption) {
        this.plotDisplayOption = plotDisplayOption;
        repaint();
    }

    public Duration getPlotUnitDuration() {
        if (shrinkToFit) {
            return Duration.ofMillis(plotUnitDurationMillis);
        } else {
            return plotChronoUnit.getDuration();
        }
    }

    public void setHighlightRange(Instant startHighlightInstant, Instant endHighlightInstant) {
        Instant start = startHighlightInstant;
        if (start.isBefore(startInstant)) {
            start = startInstant;
        }
        Instant end = endHighlightInstant;
        if (end.isAfter(endInstant)) {
            end = endInstant;
        }

        if (shrinkToFit) {
            long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);

            long deltaTime = ChronoUnit.MILLIS.between(startInstant, start);
            double normTime = (double)deltaTime / totalPlotDeltaTime;
            double highlightLeft = plotRectangle.x + ((double)plotRectangle.width * normTime);

            deltaTime = ChronoUnit.MILLIS.between(startInstant, end);
            normTime = (double)deltaTime / totalPlotDeltaTime;
            double highlightRight = plotRectangle.x + ((double)plotRectangle.width * normTime);



//            long deltaTimeMillis = Duration.between(startInstant, start).toMillis();
//            int summaryUnitIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
//            int highlightLeft = plotRectangle.x + (summaryUnitIndex * plotUnitWidth);
//            deltaTimeMillis = Duration.between(startInstant, end).toMillis();
//            summaryUnitIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
//            int highlightRight = plotRectangle.x + (summaryUnitIndex * plotUnitWidth);
            if (highlightLeft > plotRectangle.x) {
                highlightLeft = highlightLeft--;
            }
            if (highlightRight < (plotRectangle.x + plotRectangle.width)) {
                highlightRight++;
            }
            highlightRectangle = new Rectangle2D.Double(highlightLeft, plotRectangle.y, highlightRight-highlightLeft, plotRectangle.height);
        } else {

        }

        repaint();
    }

    public void setShowTimeRangeLabels(boolean showTimeRangeLabels) {
        if (this.showTimeRangeLabels != showTimeRangeLabels) {
            this.showTimeRangeLabels = showTimeRangeLabels;
            repaint();
        }
    }

    public void layoutPanel() {
        if (timeSeries != null) {
            int plotLeft = getInsets().left;
            int plotTop = getInsets().top + timeBarHeight;
            int plotBottom = getHeight() - (getInsets().bottom + valueBarHeight);
            timeInfoBarTop = getInsets().top;
            timeInfoBarBottom = timeInfoBarTop + timeBarHeight;
            valueInfoBarTop = plotBottom;
            valueInfoBarBottom = valueInfoBarTop + valueBarHeight;

            if (shrinkToFit) {
                int plotWidth = getWidth() - (getInsets().left + getInsets().right);
                numPlotUnits = plotWidth / plotUnitWidth;
                double plotUnitDurationReal = (double)totalDuration.toMillis() / numPlotUnits;
                plotUnitDurationMillis = (int)Math.ceil(plotUnitDurationReal);
                plotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);
                plotLeftInstant = startInstant;
                plotRightInstant = startInstant.plusMillis(plotUnitDurationMillis * numPlotUnits);
//                log.debug("plotWidth = " + plotWidth + " plotUnitDurationMillis = " + plotUnitDurationMillis);
            } else {
                int plotWidth = (int) (plotChronoUnit.between(startInstant, endInstant) * plotUnitWidth);
                plotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);
                setPreferredSize(new Dimension(plotWidth + (getInsets().left + getInsets().right),
                        getPreferredSize().height));
                revalidate();
            }

            calculatePlotPoints();

            repaint();
        }
    }

    public Instant getStartInstant () {
        return startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    private void calculatePlotPoints() {
        if (shrinkToFit) {
            if (numPlotUnits > 0) {
                summaryInfoArray = new TimeSeriesSummaryInfo[numPlotUnits];

                // the overall min and max value for all statistics
                double overallMaxValue = 0.;
                double overallMinValue = 0.;

                for (int i = 0; i < numPlotUnits; i++) {
                    // determine the start and end time instants for the current time unit
                    Instant unitStartInstant = startInstant.plusMillis(i * plotUnitDurationMillis);
                    Instant unitEndInstant = unitStartInstant.plusMillis(plotUnitDurationMillis);

                    // get values between start (inclusive) and end time instants (exclusive)
                    ArrayList<TimeSeriesRecord> records = timeSeries.getRecordsBetween(unitStartInstant, unitEndInstant);
                    if (records != null && !records.isEmpty()) {
                        // calculate mean value for records in plot time unit
                        SummaryStatistics stats = new SummaryStatistics();

                        for (TimeSeriesRecord record : records) {
                            stats.addValue(record.value);
                        }

                        TimeSeriesSummaryInfo summaryInfo = new TimeSeriesSummaryInfo();
                        summaryInfo.instant = unitStartInstant;
                        summaryInfo.meanValue = stats.getMean();
                        summaryInfo.maxValue = stats.getMax();
                        summaryInfo.minValue = stats.getMin();
                        summaryInfo.standardDeviationValue = stats.getStandardDeviation();

                        summaryInfoArray[i] = summaryInfo;

                        // find overall min and max values
                        double currentMinValue = Math.min(summaryInfo.minValue, summaryInfo.meanValue - summaryInfo.standardDeviationValue);
                        double currentMaxValue = Math.max(summaryInfo.maxValue, summaryInfo.meanValue + summaryInfo.standardDeviationValue);
                        if (i == 0) {
                            overallMinValue = currentMinValue;
                            overallMaxValue = currentMaxValue;
                        } else {
                            overallMinValue = Math.min(overallMinValue, currentMinValue);
                            overallMaxValue = Math.max(overallMaxValue, currentMaxValue);
                        }
                    }
                }

                for (int i = 0; i < summaryInfoArray.length; i++) {
                    TimeSeriesSummaryInfo summaryInfo = summaryInfoArray[i];
                    if (summaryInfo != null) {
                        // calculate mean point
                        int x = (int) ((i * plotUnitWidth) + (plotUnitWidth / 2.));
                        //                        double norm = (stats.getMean() - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
                        //                        double yOffset = norm * (plotRectangle.getHeight());
                        //                        double y = (plotRectangle.getHeight() - yOffset);
                        double y = GraphicsUtil.mapValue(summaryInfo.meanValue, overallMinValue,
                                overallMaxValue, plotRectangle.height, 0.);
                        summaryInfo.meanPoint = new Point2D.Double(x, y);

                        // calculate max value point
                        y = GraphicsUtil.mapValue(summaryInfo.maxValue, overallMinValue,
                                overallMaxValue, plotRectangle.height, 0.);
                        summaryInfo.maxPoint = new Point2D.Double(x, y);

                        // calculate min value point
                        y = GraphicsUtil.mapValue(summaryInfo.minValue, overallMinValue,
                                overallMaxValue, plotRectangle.height, 0.);
                        summaryInfo.minPoint = new Point2D.Double(x, y);

                        // calculate standard deviation upper and lower range points
                        y = GraphicsUtil.mapValue(summaryInfo.meanValue + summaryInfo.standardDeviationValue, overallMinValue,
                                overallMaxValue, plotRectangle.height, 0.);
                        summaryInfo.upperStandardDeviationRangePoint = new Point2D.Double(x, y);
                        y = GraphicsUtil.mapValue(summaryInfo.meanValue - summaryInfo.standardDeviationValue, overallMinValue,
                                overallMaxValue, plotRectangle.height, 0.);
                        summaryInfo.lowerStandardDeviationRangePoint = new Point2D.Double(x, y);
                    }
                }
            }
        } else {
            plotPointMap.clear();
            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
            if (records != null) {
                long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
                for (TimeSeriesRecord record : records) {
                    long deltaTime = ChronoUnit.MILLIS.between(startInstant, record.instant);
                    double normTime = (double)deltaTime / totalPlotDeltaTime;
                    double x = plotRectangle.x + ((double)plotRectangle.width * normTime);

//                    double norm = (record.value - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
                    double norm = (record.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
                    double yOffset = norm * (plotRectangle.height);
                    double y = plotRectangle.height - yOffset;

                    Point2D.Double point = new Point2D.Double(x, y);

                    ArrayList<Point2D.Double> instantPoints = plotPointMap.get(record.instant);
                    if (instantPoints == null) {
                        instantPoints = new ArrayList<>();
                        plotPointMap.put(record.instant, instantPoints);
                    }
                    instantPoints.add(point);
                }
            }
        }
    }

    public int getXForInstant(Instant instant) {
        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
        long deltaTime = ChronoUnit.MILLIS.between(startInstant, instant);
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        double x = plotRectangle.x + ((double)plotRectangle.width * normTime);
        return (int)x;
    }

    private void drawTimeSeries(Graphics2D g2, Instant startClipInstant, Instant endClipInstant) {
        // debugging drawing
        g2.setColor(gridLineColor);
        g2.draw(plotRectangle);

        g2.translate(plotRectangle.x, plotRectangle.y);

        if (shrinkToFit) {
            TimeSeriesRenderer.renderAsOverview(g2, timeSeries, plotRectangle.width, plotRectangle.height,
                    plotUnitWidth, plotChronoUnit, plotDisplayOption, gridLineColor, dataColor, dataColor.darker(),
                    rangeColor.brighter(), rangeColor, summaryInfoArray);
        } else {
            if (plotPointMap != null && !plotPointMap.isEmpty()) {
                TimeSeriesRenderer.renderAsDetailed(g2, timeSeries, startClipInstant, endClipInstant,
                        plotRectangle.width, plotRectangle.height, plotUnitWidth, plotChronoUnit, plotDisplayOption,
                        gridLineColor, dataColor, dataColor, rangeColor, plotPointMap);
            }
        }
//        // draw zero line if min < 0 and max > 0
//        if (timeSeries.getMinValue() < 0. && timeSeries.getMaxValue() > 0.) {
//            double norm = (0. - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
//            double yOffset = norm * (plotRectangle.height);
//            double zeroY = (plotRectangle.height - yOffset);
//            g2.setColor(gridLineColor);
//            Line2D.Double line = new Line2D.Double(0, zeroY, plotRectangle.width, zeroY);
//            g2.draw(line);
//        }
//
//        if (shrinkToFit) {
//            g2.setColor(dataColor);
//            for (int i = 0; i < summaryInfoList.size(); i++) {
//                TimeSeriesSummaryInfo summaryInfo = summaryInfoList.get(i);
//
//                if (plotDisplayOption == PlotDisplayOption.POINT) {
//                    Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - plotUnitWidth / 2., summaryInfo.meanPoint.getY() - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
//                    g2.draw(ellipse);
//                } else if (plotDisplayOption == PlotDisplayOption.LINE) {
//                    if (i > 0) {
//                        Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
//                        TimeSeriesSummaryInfo lastSummary = summaryInfoList.get(i - 1);
//                        Line2D.Double line = new Line2D.Double(lastSummary.meanPoint.getX(), lastSummary.meanPoint.getY(), summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                        g2.draw(line);
//                        g2.draw(ellipse);
//                    }
//                } else if (plotDisplayOption == PlotDisplayOption.STEPPED_LINE) {
//                    if (i > 0) {
//                        Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
//                        TimeSeriesSummaryInfo lastSummary = summaryInfoList.get(i - 1);
//                        Line2D.Double line1 = new Line2D.Double(lastSummary.meanPoint.getX(), lastSummary.meanPoint.getY(), summaryInfo.meanPoint.getX(), lastSummary.meanPoint.getY());
//                        Line2D.Double line2 = new Line2D.Double(summaryInfo.meanPoint.getX(), lastSummary.meanPoint.getY(), summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                        g2.draw(ellipse);
//                        g2.draw(line1);
//                        g2.draw(line2);
//                    }
//                } else if (plotDisplayOption == PlotDisplayOption.BAR) {
//
//                }
//            }
//        } else {
//            if (plotPointMap != null && !plotPointMap.isEmpty()) {
//                g2.setColor(dataColor);
//
//                Instant start = plotPointMap.firstKey();
//                if (startClipInstant.isAfter(start)) {
//                    start = plotPointMap.lowerKey(startClipInstant);
//                }
//                Instant end = plotPointMap.lastKey();
//                if (endClipInstant.isBefore(end)) {
//                    end = plotPointMap.higherKey(endClipInstant);
//                }
//                NavigableMap<Instant, ArrayList<Point2D.Double>> clipMap = plotPointMap.subMap(start, true, end, true);
//                if (clipMap.isEmpty()) {
//                    log.debug("No records in clip range - nothing to draw");
//                } else {
//                    Point2D.Double lastDrawnPoint = null;
//                    int numPointsDrawn = 0;
//                    for (ArrayList<Point2D.Double> instantPoints : clipMap.values()) {
//                        for (Point2D.Double point : instantPoints) {
//                            if (plotDisplayOption == PlotDisplayOption.POINT) {
//                                Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - plotUnitWidth / 2.,
//                                        point.y - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
//                                g2.draw(ellipse);
//                            } else if (plotDisplayOption == PlotDisplayOption.LINE) {
//                                if (lastDrawnPoint != null) {
//                                    Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - 1,
//                                            point.y - 1, 2., 2.);
//                                    Line2D.Double line = new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, point.x, point.y);
//                                    g2.draw(line);
//                                    g2.draw(ellipse);
//                                }
//                            } else if (plotDisplayOption == PlotDisplayOption.STEPPED_LINE) {
//                                if (lastDrawnPoint != null) {
//                                    Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - 1, point.y - 1, 2., 2.);
//                                    Line2D.Double line1 = new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, point.x, lastDrawnPoint.y);
//                                    Line2D.Double line2 = new Line2D.Double(point.x, lastDrawnPoint.y, point.x, point.y);
//                                    g2.draw(ellipse);
//                                    g2.draw(line1);
//                                    g2.draw(line2);
//                                }
//                            }
//                            lastDrawnPoint = point;
//                            numPointsDrawn++;
//                        }
//                    }
//                }
//            }
//        }
        g2.translate(-plotRectangle.x, -plotRectangle.y);
    }

    private void drawTimeInfoBar(Graphics2D g2) {
        int strY = timeInfoBarBottom - 2;
        g2.setColor(Color.black);

        if (shrinkToFit) {
            int hoverInstantStringLeft = -1;
            int hoverInstantStringRight = -1;
            if (hoverInstant != null) {
                String instantString = dtFormatter.format(hoverInstant);
                int strWidth = g2.getFontMetrics().stringWidth(instantString);
                hoverInstantStringLeft = (int) (hoverX - strWidth/2.);
                if (hoverInstantStringLeft < plotRectangle.x) {
                    hoverInstantStringLeft = plotRectangle.x;
                }
                hoverInstantStringRight = hoverInstantStringLeft + strWidth;
                if (hoverInstantStringRight > plotRectangle.getMaxX()) {
                    hoverInstantStringLeft -= hoverInstantStringRight - plotRectangle.getMaxX();
                    hoverInstantStringRight = hoverInstantStringLeft + strWidth;
                }

                g2.drawString(instantString, hoverInstantStringLeft, strY);
            }

            if (showTimeRangeLabels) {
                String instantString = dtFormatter.format(plotLeftInstant);
                int strWidth = g2.getFontMetrics().stringWidth(instantString);
                if (hoverInstant == null || (hoverInstantStringLeft > (plotRectangle.x + strWidth))) {
                    g2.drawString(instantString, plotRectangle.x, strY);
                }

                instantString = dtFormatter.format(plotRightInstant);
                strWidth = g2.getFontMetrics().stringWidth(instantString);
                if (hoverInstant == null || (hoverInstantStringRight < ((plotRectangle.x + plotRectangle.width) - strWidth))) {
                    g2.drawString(instantString, (plotRectangle.x + plotRectangle.width) - strWidth, strY);
                }
            }

            if (!Double.isNaN(hoverValue)) {
                g2.setColor(Color.black);
                String strValue = String.valueOf(hoverValue);
                int strWidth = g2.getFontMetrics().stringWidth(strValue);
                int strX = hoverX - strWidth/2;
                if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                    strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                }
                if (strX < plotRectangle.x) {
                    strX += (plotRectangle.x - strX);
                }
                g2.drawString(strValue, strX, valueInfoBarBottom - 2);
            }
        } else {
            Rectangle clipBounds = g2.getClipBounds();
            Instant leftInstant;
            Instant rightInstant;
            int infoBarLeft;
            int infoBarRight;

            if (clipBounds.x <= plotRectangle.x) {
                leftInstant = startInstant;
                infoBarLeft = plotRectangle.x;
            } else {
                double deltaTime = (double)(clipBounds.x - plotRectangle.x) / plotUnitWidth;
                leftInstant = startInstant.plus((long)deltaTime, plotChronoUnit);
                infoBarLeft = clipBounds.x;
            }

            if (clipBounds.getMaxX() >= plotRectangle.getMaxX()) {
                rightInstant = endInstant;
                infoBarRight = (int) plotRectangle.getMaxX();
            } else {
                double deltaTime = (double)(clipBounds.x + clipBounds.width - plotRectangle.x) / plotUnitWidth;
                rightInstant = startInstant.plus((long)deltaTime, plotChronoUnit);
                infoBarRight = clipBounds.x + clipBounds.width;
            }

            int hoverInstantStringLeft = -1;
            int hoverInstantStringRight = -1;
            if (hoverInstant != null) {
                String instantString = dtFormatter.format(hoverInstant);
                int strWidth = g2.getFontMetrics().stringWidth(instantString);
                hoverInstantStringLeft = (int)(hoverX - strWidth/2.);
                if (hoverInstantStringLeft < clipBounds.x) {
                    hoverInstantStringLeft = clipBounds.x;
                }
                if (hoverInstantStringLeft < plotRectangle.x) {
                    hoverInstantStringLeft = plotRectangle.x;
                }
                hoverInstantStringRight = hoverInstantStringLeft + strWidth;
                if (hoverInstantStringRight > (clipBounds.x + clipBounds.width)) {
                    hoverInstantStringLeft -= hoverInstantStringRight - (clipBounds.x + clipBounds.width);
                    hoverInstantStringRight = hoverInstantStringLeft + strWidth;
                }
                if (hoverInstantStringRight > (plotRectangle.x + plotRectangle.width)) {
                    hoverInstantStringLeft -= hoverInstantStringRight - (plotRectangle.x + plotRectangle.width);
                    hoverInstantStringRight = hoverInstantStringLeft + strWidth;
                }

                g2.drawString(instantString, hoverInstantStringLeft, strY);
            }

            if (showTimeRangeLabels) {
                // draw left date time string
                String instantString = dtFormatter.format(leftInstant);
                int strWidth = g2.getFontMetrics().stringWidth(instantString);
                if (hoverInstant == null || (hoverInstantStringLeft > (infoBarLeft + strWidth))) {
                    g2.drawString(instantString, infoBarLeft, strY);
                }

                // draw right date time string
                instantString = dtFormatter.format(rightInstant);
                strWidth = g2.getFontMetrics().stringWidth(instantString);
                if (hoverInstant == null || (hoverInstantStringRight < (infoBarRight - strWidth))) {
                    g2.drawString(instantString, infoBarRight - strWidth, strY);
                }
            }

            if (!Double.isNaN(hoverValue)) {
                g2.setColor(Color.black);
                String strValue = String.valueOf(hoverValue);
                int strWidth = g2.getFontMetrics().stringWidth(strValue);
                int strX = hoverX - strWidth/2;
                if ((strX + strWidth) > (clipBounds.x + clipBounds.width)) {
                    strX -= (strX+strWidth) - (clipBounds.x + clipBounds.width);
                }
                if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                    strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                }
                if (strX < clipBounds.x) {
                    strX += (clipBounds.x - strX);
                }
                if (strX < plotRectangle.x) {
                    strX += (plotRectangle.x - strX);
                }
                g2.drawString(strValue, strX, valueInfoBarBottom - 2);
            }
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (timeSeries != null) {
            g2.setColor(Color.black);
            g2.draw(plotRectangle);

            Rectangle clipBounds = g2.getClipBounds();

            double norm = (double)(clipBounds.x - plotRectangle.x) / (plotRectangle.width);
            long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipStartInstant = startInstant.plusMillis(deltaTimeMillis);
            norm = (double)((clipBounds.x + clipBounds.width) - plotRectangle.x) / (plotRectangle.width);
            deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipEndInstant = startInstant.plusMillis(deltaTimeMillis);

            g2.setFont(g2.getFont().deriveFont(12.f));

            if (highlightRectangle != null) {
                g2.setColor(unselectedRegionFillColor);
                g2.fill(plotRectangle);
                g2.setColor(selectedRegionFillColor);
                g2.fill(highlightRectangle);
            }

            drawTimeSeries(g2, clipStartInstant, clipEndInstant);
            drawTimeInfoBar(g2);
            drawTimeSelections(g2);

            if (hoverInstant != null) {
                g2.setColor(hoverLineColor);
                g2.drawLine(hoverX, 0, hoverX, getHeight());
            }
        }
    }

    private void drawTimeSelections(Graphics2D g2) {
        if (selectionList != null && !selectionList.isEmpty()) {
            for (TimeSeriesSelection selection : selectionList) {
                RoundRectangle2D.Float selectionRect = new RoundRectangle2D.Float(selection.getStartScreenLocation(),
                        plotRectangle.y, selection.getEndScreenLocation() - selection.getStartScreenLocation(),
                        plotRectangle.height, 2.f, 2.f);
                RoundRectangle2D.Float selectionRectOutline = new RoundRectangle2D.Float(selectionRect.x - 1,
                        selectionRect.y-1, selectionRect.width + 2, selectionRect.height + 2, 2f, 2f);

//				g2.setStroke(new BasicStroke(2.f));
                g2.setColor(Color.darkGray);
                g2.draw(selectionRectOutline);
                g2.setColor(Color.orange);
                g2.draw(selectionRect);
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
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

    public enum PlotDisplayOption {
        STEPPED_LINE, LINE, BAR, POINT
    }
}
