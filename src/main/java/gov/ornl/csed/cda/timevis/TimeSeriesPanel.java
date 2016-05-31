package gov.ornl.csed.cda.timevis;

import gov.ornl.csed.cda.util.GraphicsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by csg on 12/2/15.
 */
public class TimeSeriesPanel extends JComponent implements TimeSeriesListener, ComponentListener, MouseListener, MouseMotionListener {
    private final static Logger log = LoggerFactory.getLogger(TimeSeriesPanel.class);

    public static final Color DEFAULT_POINT_COLOR = new Color(80, 80, 130, 180);
    public static final Color DEFAULT_LINE_COLOR = DEFAULT_POINT_COLOR.brighter();
    public static final Color DEFAULT_STANDARD_DEVIATION_RANGE_COLOR = new Color(140, 140, 160, 100);
    public static final Color DEFAULT_MINMAX_RANGE_COLOR = DEFAULT_STANDARD_DEVIATION_RANGE_COLOR.brighter();
    public static final Color DEFAULT_SPECTRUM_NEGATIVE_COLOR = Color.red;
    public static final Color DEFAULT_SPECTRUM_POSITIVE_COLOR = Color.blue;
    public static final Color DEFAULT_SPECTRUM_ZERO_COLOR = Color.gray;

    public static final int HIGHLIGHT_RANGE_MIN_SIZE = 8;

    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private TimeSeries timeSeries;
    private BinnedTimeSeries binnedTimeSeries;
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

    // mouse hover variables
    private int hoverX;
    private Instant hoverInstant;
    private double hoverValue = Double.NaN;
    private TimeSeriesSelection hoverTimeSeriesSelection = null;
    private boolean mouseOverPlot = false;
    private boolean mouseOverTimeBar = false;
    private boolean mouseOverValueBar = false;

    // pin marker variables
    private ArrayList<PinMarkerInfo> pinMarkerList = new ArrayList<>();
    private boolean pinningEnabled = true;

    private boolean shrinkToFit = false;
    private PlotDisplayOption plotDisplayOption = PlotDisplayOption.POINT;

//    private ConcurrentSkipListMap<Instant, ArrayList<Point2D.Double>> plotPointMap = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> plotPointRecordMap = new ConcurrentSkipListMap<>();

    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    private Color selectedRegionFillColor = Color.white;

    private Color pointColor = DEFAULT_POINT_COLOR;
    private Color lineColor = DEFAULT_LINE_COLOR;
    private Color standardDeviationRangeColor = DEFAULT_STANDARD_DEVIATION_RANGE_COLOR;
    private Color minmaxRangeColor = DEFAULT_MINMAX_RANGE_COLOR;
    private Color spectrumNegativeColor = DEFAULT_SPECTRUM_NEGATIVE_COLOR;
    private Color spectrumPositiveColor = DEFAULT_SPECTRUM_POSITIVE_COLOR;
    private Color spectrumZeroColor = DEFAULT_SPECTRUM_ZERO_COLOR;

    private Instant startHighlightInstant;
    private Instant endHighlightInstant;
    private Rectangle2D.Double highlightRectangle;

    private boolean showTimeRangeLabels = true;

    // range selection dragging stuff
    private Point startDragPoint = new Point();
    private Point endDragPoint = new Point();
    private boolean draggingSelection;
    private TimeSeriesSelection draggingTimeSeriesSelecton;
    private boolean interactiveSelectionEnabled = true;

    // range selection data
    private ArrayList<TimeSeriesSelection> selectionList = new ArrayList<>();
    private ArrayList<TimeSeriesPanelSelectionListener> selectionListeners = new ArrayList<>();

    // value axis range
    private double valueAxisMax;
    private double valueAxisMin;

    // moving range mode variables
    MovingRangeDisplayOption movingRangeDisplayOption = MovingRangeDisplayOption.NOT_SHOWN;
//    boolean movingRangeModeEnabled = false;
    private TimeSeries movingRangeTimeSeries;

    // fixed plot width constructor constructor
    public TimeSeriesPanel (int plotUnitWidth, ChronoUnit plotChronoUnit, PlotDisplayOption plotDisplayOption) {
        this.plotUnitWidth = plotUnitWidth;
        this.plotChronoUnit = plotChronoUnit;
        this.plotDisplayOption = plotDisplayOption;
        shrinkToFit = false;

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // fit to panel width constructor
    public TimeSeriesPanel (int plotUnitWidth, PlotDisplayOption plotDisplayOption) {
        this.plotUnitWidth = plotUnitWidth;
        this.shrinkToFit = true;
        this.plotDisplayOption = plotDisplayOption;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public MovingRangeDisplayOption getMovingRangeDisplayOption () {
        return movingRangeDisplayOption;
    }

    public void setMovingRangeDisplayOption (MovingRangeDisplayOption movingRangeDisplayOption) {
        if (this.movingRangeDisplayOption != movingRangeDisplayOption) {
            this.movingRangeDisplayOption = movingRangeDisplayOption;
            if (movingRangeDisplayOption == MovingRangeDisplayOption.NOT_SHOWN) {
                movingRangeTimeSeries = null;
            } else if (movingRangeTimeSeries == null) {
                calculateMovingRangeTimeSeries();
            }

            calculatePlotPoints();
            repaint();
        }
    }

//    public boolean isMovingRangeModeEnabled () {
//        return movingRangeModeEnabled;
//    }
//
//    public void setMovingRangeModeEnabled (boolean enabled) {
//        if (this.movingRangeModeEnabled != enabled) {
//            this.movingRangeModeEnabled = enabled;
//            calculateMovingRangeTimeSeries();
//            calculatePlotPoints();
//            repaint();
//        }
//    }

    public boolean isInteractiveSelectionsEnabled () {
        return interactiveSelectionEnabled;
    }

    public void setInteractiveSelectionEnabled (boolean enabled) {
        if (interactiveSelectionEnabled != enabled) {
            interactiveSelectionEnabled = enabled;
        }
    }

    public boolean isPinningEnabled() {
        return pinningEnabled;
    }

    public void setPinningEnabled (boolean enabled) {
        if (pinningEnabled != enabled) {
            pinningEnabled = enabled;
            if (enabled == false) {
                pinMarkerList.clear();
                repaint();
            }
        }
    }

    public void clearAllPinMarkers () {
        pinMarkerList.clear();
        repaint();
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

    public boolean removeTimeSeriesPanelSelectionListener (TimeSeriesPanelSelectionListener listener) {
        return selectionListeners.remove(listener);
    }

    private void fireSelectionCreated(TimeSeriesSelection selection) {
        for (TimeSeriesPanelSelectionListener listener : selectionListeners) {
            listener.selectionCreated(this, selection);
        }
    }

    private void fireSelectionMoved(TimeSeriesSelection selection, Instant previousStartInstant, Instant previousEndInstant) {
        for (TimeSeriesPanelSelectionListener listener : selectionListeners) {
            listener.selectionMoved(this, selection, previousStartInstant, previousEndInstant);
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

    public TimeSeriesSelection updateTimeSeriesSelection(Instant previousStartInstant, Instant previousEndInstant,
                                                         Instant newStartInstant, Instant newEndInstant) {
        if (!selectionList.isEmpty()) {
            for (TimeSeriesSelection selection : selectionList) {
                if (selection.getStartInstant().equals(previousStartInstant) &&
                        selection.getEndInstant().equals(previousEndInstant)) {
                    Instant start;
                    Instant end;
                    double leftPosition;
                    double rightPosition;

                    // clamp selection start to panels range and calculate left x position
                    if (newStartInstant.isAfter(startInstant)) {
                        start = Instant.from(newStartInstant);
                        leftPosition = GraphicsUtil.mapValue(start.toEpochMilli(), startInstant.toEpochMilli(),
                                endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
                    } else {
                        start = Instant.from(startInstant);
                        leftPosition = plotRectangle.getX();
                    }

                    // clamp selection end to panel's range and calculate right x position
                    if (newEndInstant.isBefore(endInstant)) {
                        end = Instant.from(newEndInstant);
                        rightPosition = GraphicsUtil.mapValue(end.toEpochMilli(), startInstant.toEpochMilli(),
                                endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
                    } else {
                        end = Instant.from(endInstant);
                        rightPosition = plotRectangle.getX();
                    }

                    selection.setStartInstant(start);
                    selection.setEndInstant(end);
                    selection.setStartScreenLocation((int) leftPosition);
                    selection.setEndScreenLocation((int) rightPosition);

                    repaint();
                    return selection;
                }
            }
        }
        return null;
    }

    public boolean removeTimeSeriesSelection(Instant selectionStart, Instant selectionEnd) {
        if (!selectionList.isEmpty()) {
            TimeSeriesSelection selectionToRemove = null;
            for (TimeSeriesSelection selection : selectionList) {
                if (selection.getStartInstant().equals(selectionStart) &&
                        selection.getEndInstant().equals(selectionEnd)) {
                    selectionToRemove = selection;
                    break;
                }
            }

            if (selectionToRemove != null) {
                if (selectionList.remove(selectionToRemove)) {
                    repaint();
                    return true;
                }
            }
        }

        return false;
    }

    public void removeAllTimeSeriesSelections() {
        selectionList.clear();
        repaint();
    }

    public Color getPointColor() {
        return pointColor;
    }

    public void setPointColor(Color pointColor) {
        this.pointColor = pointColor;
        repaint();
    }

    public void setSpectrumPositiveColor (Color spectrumPositiveColor) {
        this.spectrumPositiveColor = spectrumPositiveColor;
        repaint();
    }

    public Color getSpectrumPositiveColor() {
        return spectrumPositiveColor;
    }

    public void setSpectrumZeroColor (Color spectrumZeroColor) {
        this.spectrumZeroColor = spectrumZeroColor;
        repaint();
    }

    public Color getSpectrumZeroColor() {
        return spectrumZeroColor;
    }

    public void setSpectrumNegativeColor (Color spectrumNegativeColor) {
        this.spectrumNegativeColor = spectrumNegativeColor;
        repaint();
    }

    public Color getSpectrumNegativeColor() {
        return spectrumNegativeColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        repaint();
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setStandardDeviationRangeColor (Color standardDeviationRangeColor) {
        this.standardDeviationRangeColor = standardDeviationRangeColor;
        repaint();
    }

    public Color getStandardDeviationRangeColor() {
        return standardDeviationRangeColor;
    }

    public void setMinMaxRangeColor (Color minmaxRangeColor) {
        this.minmaxRangeColor = minmaxRangeColor;
        repaint();
    }

    public Color getMinMaxRangeColor() {
        return minmaxRangeColor;
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
        if (this.timeSeries != null) {
            this.timeSeries.removeTimeSeriesListener(this);
        }

        this.timeSeries = timeSeries;
        timeSeries.addTimeSeriesListener(this);

        if (movingRangeDisplayOption != MovingRangeDisplayOption.NOT_SHOWN) {
            calculateMovingRangeTimeSeries();
        } else {
            movingRangeTimeSeries = null;
        }

        this.startInstant = startInstant;
        this.endInstant = endInstant;
        totalDuration = Duration.between(startInstant, endInstant);

        // Apply a small buffer to expand the min / max value range to prevent extreme points
        // from being drawn on the plot boundaries
        double buffer = 0.05 * (timeSeries.getMaxValue() - timeSeries.getMinValue());
        valueAxisMin = timeSeries.getMinValue() - buffer;
        valueAxisMax = timeSeries.getMaxValue() + buffer;
        layoutPanel();
    }

    @Override
    public void timeSeriesRecordAdded(TimeSeries timeSeries, TimeSeriesRecord record) {
        // if new record is outside the current start or end instants, update those instants,
        // layout the panel, recalculate the plot points, and repaint
        // else just calculate the new point location
        // TODO: How to handle overview summary points?  And Moving Range?
//        log.debug("in timeSeriesRecordAdded() " + plotPointMap.size());

        boolean doLayout = false;

        if (record.value < valueAxisMin) {
            valueAxisMin = record.value;
            doLayout = true;
        }
        if (record.value > valueAxisMax) {
            valueAxisMax = record.value;
            doLayout = true;
        }

        if (record.instant.isBefore(startInstant)) {
            startInstant = Instant.from(record.instant);
            doLayout = true;
        } else if (record.instant.isAfter(endInstant)) {
            endInstant = Instant.from(record.instant);
            doLayout = true;
        }

        if (doLayout) {
            layoutPanel();
        }

        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(record, null);

        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(record.instant);
        if (instantRecords == null) {
            instantRecords = new ArrayList<>();
            plotPointRecordMap.put(record.instant, instantRecords);
        }
        instantRecords.add(plotPointRecord);
        repaint();
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
            if (mouseOverValueBar || mouseOverTimeBar) {
                if (pinningEnabled) {
                    // user has clicked on the time info or value info bar
                    // pin the mouse over value so that it does not change
                    if (!pinMarkerList.isEmpty()) {
                        for (PinMarkerInfo pinMarker : pinMarkerList) {
                            if ((e.getX() >= (pinMarker.x - 3)) && (e.getX() <= (pinMarker.x + 3))) {
                                // delete the pin marker and return from the function
                                pinMarkerList.remove(pinMarker);
                                repaint();
                                return;
                            }
                        }
                    }

                    if (!Double.isNaN(hoverValue)) {
                        PinMarkerInfo pinMarker = new PinMarkerInfo();
                        pinMarker.instant = Instant.from(hoverInstant);
                        pinMarker.value = hoverValue;
                        pinMarker.x = hoverX;
                        pinMarkerList.add(pinMarker);
                        repaint();
                    }
                }
            } else if ((hoverTimeSeriesSelection != null) && interactiveSelectionEnabled) {
                selectionList.remove(hoverTimeSeriesSelection);
                fireSelectionDeleted(hoverTimeSeriesSelection);
                hoverTimeSeriesSelection = null;
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (interactiveSelectionEnabled) {
            startDragPoint.setLocation(e.getX(), e.getY());
            endDragPoint.setLocation(e.getX(), e.getY());
        }
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
        if (hoverTimeSeriesSelection != null && interactiveSelectionEnabled) {
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

            Instant previousStartInstant  = Instant.from(hoverTimeSeriesSelection.getStartInstant());
            Instant previousEndInstant = Instant.from(hoverTimeSeriesSelection.getEndInstant());
            hoverTimeSeriesSelection.setStartInstant(selectionStart);
            hoverTimeSeriesSelection.setEndInstant(selectionEnd);

            fireSelectionMoved(hoverTimeSeriesSelection, previousStartInstant, previousEndInstant);
        } else if (interactiveSelectionEnabled){
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

            double selectionStartMillis = GraphicsUtil.mapValue(leftPosition, plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionStart = Instant.ofEpochMilli((long) selectionStartMillis);
            double selectionEndMillis = GraphicsUtil.mapValue(rightPosition, plotRectangle.getX(), plotRectangle.getMaxX(), startInstant.toEpochMilli(), endInstant.toEpochMilli());
            Instant selectionEnd = Instant.ofEpochMilli((long) selectionEndMillis);

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

    private void calculateTimeSeriesSelectionBounds (TimeSeriesSelection selection) {
        double startLocation = GraphicsUtil.mapValue(selection.getStartInstant().toEpochMilli(), startInstant.toEpochMilli(),
                endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
        double endLocation = GraphicsUtil.mapValue(selection.getEndInstant().toEpochMilli(), startInstant.toEpochMilli(),
                endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
        selection.setStartScreenLocation((int)startLocation);
        selection.setEndScreenLocation((int)endLocation);
    }

    public void addTimeSeriesSelection(Instant selectionStart, Instant selectionEnd) {
        if (timeSeries != null) {
            Instant start;
            Instant end;
            double leftPosition;
            double rightPosition;

            // clamp selection start to panels range and calculate left x position
            if (selectionStart.isAfter(startInstant)) {
                start = Instant.from(selectionStart);
                leftPosition = GraphicsUtil.mapValue(start.toEpochMilli(), startInstant.toEpochMilli(),
                        endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
            } else {
                start = Instant.from(startInstant);
                leftPosition = plotRectangle.getX();
            }

            // clamp selection end to panel's range and calculate right x position
            if (selectionEnd.isBefore(endInstant)) {
                end = Instant.from(selectionEnd);
                rightPosition = GraphicsUtil.mapValue(end.toEpochMilli(), startInstant.toEpochMilli(),
                        endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
            } else {
                end = Instant.from(endInstant);
                rightPosition = plotRectangle.getX();
            }

            // create selection object and add to selection list
            TimeSeriesSelection selection = new TimeSeriesSelection(start, end, (int)leftPosition, (int)rightPosition);
            selectionList.add(selection);

            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoverInstant = null;
        hoverX = -1;
        hoverValue = Double.NaN;
        hoverTimeSeriesSelection = null;

        if ((plotRectangle != null)/* && ((e.getX() >= plotRectangle.x) && e.getX() <= (plotRectangle.x + plotRectangle.width))*/ ) {
            hoverX = e.getX();

            // clamp the x value to the plot bounds
            if (hoverX < plotRectangle.x) {
                hoverX = plotRectangle.x;
                hoverInstant = startInstant;
            } else if (hoverX > (plotRectangle.x + plotRectangle.width)) {
                hoverX = plotRectangle.x+plotRectangle.width;
                hoverInstant = endInstant;
            } else {
                double norm = (double) (hoverX - plotRectangle.x) / (plotRectangle.width);
                long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
                hoverInstant = startInstant.plusMillis(deltaTimeMillis);
            }

            if (shrinkToFit) {
                // TODO: Fix mouse hover for overview mode
                TimeSeriesBin bin = binnedTimeSeries.getBin(hoverInstant);
                if (bin != null) {
                    hoverValue = bin.getStatistics().getMean();
                }
            } else {
                Instant rangeStartInstant = hoverInstant.truncatedTo(plotChronoUnit).minus(1, plotChronoUnit);
                Instant rangeEndInstant = hoverInstant.truncatedTo(plotChronoUnit).plus(1, plotChronoUnit);

                ArrayList<TimeSeriesRecord> records = null;
                if (movingRangeDisplayOption == MovingRangeDisplayOption.PLOT_VALUE) {
                    records = movingRangeTimeSeries.getRecordsBetween(rangeStartInstant, rangeEndInstant);
                } else {
                    records = timeSeries.getRecordsBetween(rangeStartInstant, rangeEndInstant);
                }

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

            // check to see which region of the plot the mouse is hovering over
            // the time value display bar, the value display bar, or the plot
            if (e.getY() < plotRectangle.y) {
                mouseOverTimeBar = true;
                mouseOverValueBar = false;
                mouseOverPlot = false;
            } else if (e.getY() > (plotRectangle.y + plotRectangle.height)) {
                mouseOverValueBar = true;
                mouseOverTimeBar = false;
                mouseOverPlot = false;
            } else {
                mouseOverPlot = true;
                mouseOverTimeBar = false;
                mouseOverValueBar = false;
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

    private void calculateHighlightBounds () {
        if ( (startHighlightInstant != null) && (endHighlightInstant != null)) {
            double leftPosition = GraphicsUtil.mapValue(startHighlightInstant.toEpochMilli(), startInstant.toEpochMilli(),
                    endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());
            double rightPosition = GraphicsUtil.mapValue(endHighlightInstant.toEpochMilli(), startInstant.toEpochMilli(),
                    endInstant.toEpochMilli(), plotRectangle.getX(), plotRectangle.getMaxX());

            double width = rightPosition - leftPosition;
            if (width < HIGHLIGHT_RANGE_MIN_SIZE) {
                double middlePosition = leftPosition + (width / 2.);
                leftPosition = middlePosition - HIGHLIGHT_RANGE_MIN_SIZE / 2.;
                rightPosition = leftPosition + HIGHLIGHT_RANGE_MIN_SIZE;

                if (leftPosition < plotRectangle.x) {
                    leftPosition = plotRectangle.x;
                    rightPosition = leftPosition + HIGHLIGHT_RANGE_MIN_SIZE;
                }

                if (rightPosition > plotRectangle.getMaxX()) {
                    rightPosition = plotRectangle.getMaxX();
                    leftPosition = rightPosition - HIGHLIGHT_RANGE_MIN_SIZE;
                }
            }

            highlightRectangle = new Rectangle2D.Double(leftPosition, plotRectangle.y, rightPosition - leftPosition, plotRectangle.height);
        } else {
            highlightRectangle = null;
        }
    }

    public void setHighlightRange(Instant startHighlightInstant, Instant endHighlightInstant) {
        if (timeSeries != null) {
            Instant start = startHighlightInstant;
            if (start.isBefore(startInstant)) {
                start = startInstant;
            }
            Instant end = endHighlightInstant;
            if (end.isAfter(endInstant)) {
                end = endInstant;
            }

            this.startHighlightInstant = start;
            this.endHighlightInstant = end;

            calculateHighlightBounds();
            repaint();
        }
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
            } else {
                int plotWidth = (int) (plotChronoUnit.between(startInstant, endInstant) * plotUnitWidth);
                plotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);
                setPreferredSize(new Dimension(plotWidth + (getInsets().left + getInsets().right),
                        getPreferredSize().height));
                revalidate();
            }

            calculatePlotPoints();
            calculateHighlightBounds();

            for (TimeSeriesSelection selection : selectionList) {
                calculateTimeSeriesSelectionBounds(selection);
            }

            repaint();
        }
    }

    public Instant getStartInstant () {
        return startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    private void calculateMovingRangeTimeSeries() {
        if (timeSeries != null) {
            movingRangeTimeSeries = new TimeSeries(timeSeries.getName());

            TimeSeriesRecord lastRecord = null;
            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
            for (TimeSeriesRecord record : records) {
                if (lastRecord != null) {
                    double value = Math.abs(lastRecord.value - record.value);
                    Instant instant = Instant.from(record.instant);
                    movingRangeTimeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                } else {
                    movingRangeTimeSeries.addRecord(Instant.from(record.instant), 0., Double.NaN, Double.NaN);
                }
                lastRecord = record;
            }
        }
    }

    private void calculatePlotPoints() {
        if (shrinkToFit) {
            plotPointRecordMap.clear();
            if (timeSeries != null) {
                if (!timeSeries.isEmpty()) {
                    binnedTimeSeries = new BinnedTimeSeries(timeSeries, Duration.ofMillis(plotUnitDurationMillis));
                    Collection<TimeSeriesBin> bins = binnedTimeSeries.getAllBins();
                    for (TimeSeriesBin bin : bins) {
                        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(bin);
                        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(bin.getInstant());
                        if (instantRecords == null) {
                            instantRecords = new ArrayList<>();
                            plotPointRecordMap.put(bin.getInstant(), instantRecords);
                        }
                        instantRecords.add(plotPointRecord);
                    }
                }
            }
        } else {
            plotPointRecordMap.clear();
            if (timeSeries != null) {
                for (ArrayList<TimeSeriesRecord> valueRecordList : timeSeries.getRecordMap().values()) {
                    ArrayList<TimeSeriesRecord> movingRangeRecordList = null;
                    if (movingRangeTimeSeries != null) {
                        movingRangeRecordList = movingRangeTimeSeries.getRecordMap().get(valueRecordList.get(0).instant);
                    }

                    for (int i = 0; i < valueRecordList.size(); i++) {
                        TimeSeriesPlotPointRecord plotPointRecord = null;
                        if (movingRangeRecordList != null) {
                            plotPointRecord = calculatePlotPointRecord(valueRecordList.get(i), movingRangeRecordList.get(i));
                        } else {
                            plotPointRecord = calculatePlotPointRecord(valueRecordList.get(i), null);
                        }

                        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(plotPointRecord.valueRecord.instant);
                        if (instantRecords == null) {
                            instantRecords = new ArrayList<>();
                            plotPointRecordMap.put(plotPointRecord.valueRecord.instant, instantRecords);
                        }

                        instantRecords.add(plotPointRecord);
                    }

                }
//                ArrayList<TimeSeriesRecord> valueRecords = timeSeries.getAllRecords();

//                if (movingRangeModeEnabled) {
//                    records = movingRangeTimeSeries.getAllRecords();
//                } else {
//                    records = timeSeries.getAllRecords();
//                }


//
//                if (valueRecords != null) {
//                    for (int i = 0; i < valueRecords.size(); i++) {
//                        TimeSeriesRecord record = valueRecords.get(i);
//                        TimeSeriesRecord movingRangeRecord = null;
//                        if (movingRangeTimeSeries != null) {
//                            movingRangeRecord = movingRangeTimeSeries.getRecordsAt()
//                        }
//                        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(record);
//
//                        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(record.instant);
//                        if (instantRecords == null) {
//                            instantRecords = new ArrayList<>();
//                            plotPointRecordMap.put(record.instant, instantRecords);
//                        }
//                        instantRecords.add(plotPointRecord);
//                    }
//                }
            }
        }
    }
    /*
    private void calculatePlotPoints() {
        if (shrinkToFit) {
            if (numPlotUnits > 0) {

//                summaryInfoArray = new TimeSeriesSummaryInfo[numPlotUnits];

                // the overall min and max value for all statistics
//                double overallMaxValue = 0.;
//                double overallMinValue = 0.;
//
//                for (int i = 0; i < numPlotUnits; i++) {
//                    // determine the start and end time instants for the current time unit
//                    Instant unitStartInstant = startInstant.plusMillis(i * plotUnitDurationMillis);
//                    Instant unitEndInstant = unitStartInstant.plusMillis(plotUnitDurationMillis);
//
//                    // get values between start (inclusive) and end time instants (exclusive)
//                    ArrayList<TimeSeriesRecord> records = timeSeries.getRecordsBetween(unitStartInstant, unitEndInstant);
//                    if (records != null && !records.isEmpty()) {
//                        // calculate mean value for records in plot time unit
//                        SummaryStatistics stats = new SummaryStatistics();
//
//                        for (TimeSeriesRecord record : records) {
//                            stats.addValue(record.value);
//                        }
//
//                        TimeSeriesSummaryInfo summaryInfo = new TimeSeriesSummaryInfo();
//                        summaryInfo.instant = unitStartInstant;
//                        summaryInfo.meanValue = stats.getMean();
//                        summaryInfo.maxValue = stats.getMax();
//                        summaryInfo.minValue = stats.getMin();
//                        summaryInfo.standardDeviationValue = stats.getStandardDeviation();
//
//                        summaryInfoArray[i] = summaryInfo;
//
//                        // find overall min and max values
//                        double currentMinValue = Math.min(summaryInfo.minValue, summaryInfo.meanValue - summaryInfo.standardDeviationValue);
//                        double currentMaxValue = Math.max(summaryInfo.maxValue, summaryInfo.meanValue + summaryInfo.standardDeviationValue);
//                        if (i == 0) {
//                            overallMinValue = currentMinValue;
//                            overallMaxValue = currentMaxValue;
//                        } else {
//                            overallMinValue = Math.min(overallMinValue, currentMinValue);
//                            overallMaxValue = Math.max(overallMaxValue, currentMaxValue);
//                        }
//                    }
//                }

//                for (int i = 0; i < summaryInfoArray.length; i++) {
//                    TimeSeriesSummaryInfo summaryInfo = summaryInfoArray[i];
//                    if (summaryInfo != null) {
//                        // calculate mean point
//                        int x = (int) ((i * plotUnitWidth) + (plotUnitWidth / 2.));
//                        //                        double norm = (stats.getMean() - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
//                        //                        double yOffset = norm * (plotRectangle.getHeight());
//                        //                        double y = (plotRectangle.getHeight() - yOffset);
//                        double y = GraphicsUtil.mapValue(summaryInfo.meanValue, overallMinValue,
//                                overallMaxValue, plotRectangle.height, 0.);
//                        summaryInfo.meanPoint = new Point2D.Double(x, y);
//
//                        // calculate max value point
//                        y = GraphicsUtil.mapValue(summaryInfo.maxValue, overallMinValue,
//                                overallMaxValue, plotRectangle.height, 0.);
//                        summaryInfo.maxPoint = new Point2D.Double(x, y);
//
//                        // calculate min value point
//                        y = GraphicsUtil.mapValue(summaryInfo.minValue, overallMinValue,
//                                overallMaxValue, plotRectangle.height, 0.);
//                        summaryInfo.minPoint = new Point2D.Double(x, y);
//
//                        // calculate standard deviation upper and lower range points
//                        y = GraphicsUtil.mapValue(summaryInfo.meanValue + summaryInfo.standardDeviationValue, overallMinValue,
//                                overallMaxValue, plotRectangle.height, 0.);
//                        summaryInfo.upperStandardDeviationRangePoint = new Point2D.Double(x, y);
//                        y = GraphicsUtil.mapValue(summaryInfo.meanValue - summaryInfo.standardDeviationValue, overallMinValue,
//                                overallMaxValue, plotRectangle.height, 0.);
//                        summaryInfo.lowerStandardDeviationRangePoint = new Point2D.Double(x, y);
//                    }
//                }
            }
        } else {
            plotPointMap.clear();
            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
            if (records != null) {
//                long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
                for (TimeSeriesRecord record : records) {
//                    long deltaTime = ChronoUnit.MILLIS.between(startInstant, record.instant);
//                    double normTime = (double)deltaTime / totalPlotDeltaTime;
//                    double x = ((double)plotRectangle.width * normTime);
//
////                    double norm = (record.value - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
//                    double norm = (record.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
//                    double yOffset = norm * (plotRectangle.height);
//                    double y = plotRectangle.height - yOffset;
//
//                    Point2D.Double point = new Point2D.Double(x, y);
                    Point2D.Double point = calculatePlotPoint(record);

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
*/
    private TimeSeriesPlotPointRecord calculatePlotPointRecord (TimeSeriesBin bin) {
        TimeSeriesPlotPointRecord plotPointRecord = new TimeSeriesPlotPointRecord();
        plotPointRecord.bin = bin;

        // calculate x position
        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
        long deltaTime = ChronoUnit.MILLIS.between(startInstant, bin.getInstant());
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        plotPointRecord.x = ((double)plotRectangle.width * normTime) + (plotUnitWidth / 2.);

        // calculate mean y
        plotPointRecord.meanY = GraphicsUtil.mapValue(bin.getStatistics().getMean(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(),
                plotRectangle.height, 0.);

        // calculate standard deviation upper range y
        plotPointRecord.upperStdevRangeY = GraphicsUtil.mapValue(bin.getStatistics().getMean() + bin.getStatistics().getStandardDeviation(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(),
                plotRectangle.height, 0.);

        // calculate standard deviation lower range y
        plotPointRecord.lowerStdevRangeY = GraphicsUtil.mapValue(bin.getStatistics().getMean() - bin.getStatistics().getStandardDeviation(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(),
                plotRectangle.height, 0.);

        // calculate max y
        plotPointRecord.maxY = GraphicsUtil.mapValue(bin.getStatistics().getMax(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(),
                plotRectangle.height, 0.);

        // calculate min y
        plotPointRecord.minY = GraphicsUtil.mapValue(bin.getStatistics().getMin(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(),
                plotRectangle.height, 0.);

        return plotPointRecord;
    }


    private TimeSeriesPlotPointRecord calculatePlotPointRecord (TimeSeriesRecord valueRecord, TimeSeriesRecord movingRangeRecord) {
        TimeSeriesPlotPointRecord plotPointRecord = new TimeSeriesPlotPointRecord();
        plotPointRecord.valueRecord = valueRecord;
        plotPointRecord.movingRangeRecord = movingRangeRecord;

        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
        long deltaTime = ChronoUnit.MILLIS.between(startInstant, valueRecord.instant);
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        plotPointRecord.x = ((double)plotRectangle.width * normTime);

        if (plotDisplayOption == PlotDisplayOption.SPECTRUM) {
            if (movingRangeDisplayOption == MovingRangeDisplayOption.NOT_SHOWN) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(valueRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., plotRectangle.getHeight() / 2.);
                plotPointRecord.spectrumTopY = (plotRectangle.height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (plotRectangle.height / 2.) + lineHeightHalf;

                if (valueRecord.value < 0.) {
                    plotPointRecord.color = spectrumNegativeColor;
                } else {
                    plotPointRecord.color = spectrumPositiveColor;
                }
            } else if (movingRangeDisplayOption == MovingRangeDisplayOption.OPACITY) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(valueRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., plotRectangle.getHeight() / 2.);
                plotPointRecord.spectrumTopY = (plotRectangle.height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (plotRectangle.height / 2.) + lineHeightHalf;

                int alpha = (int)Math.round(GraphicsUtil.mapValue(movingRangeRecord.value, 0., movingRangeTimeSeries.getMaxValue(), 100., 255.));
                if (valueRecord.value < 0.) {
                    plotPointRecord.color = new Color(spectrumNegativeColor.getRed(), spectrumNegativeColor.getGreen(),
                            spectrumNegativeColor.getBlue(), alpha);
                } else {
                    plotPointRecord.color = new Color(spectrumPositiveColor.getRed(), spectrumPositiveColor.getGreen(),
                            spectrumPositiveColor.getBlue(), alpha);
                }
            } else if (movingRangeDisplayOption == MovingRangeDisplayOption.PLOT_VALUE) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(movingRangeRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., plotRectangle.getHeight() / 2.);
                plotPointRecord.spectrumTopY = (plotRectangle.height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (plotRectangle.height / 2.) + lineHeightHalf;

                plotPointRecord.color = pointColor;
            }
        } else {
            double norm = (valueRecord.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
            double yOffset = norm * (plotRectangle.height);
            plotPointRecord.valueY = plotRectangle.height - yOffset;
        }

        return plotPointRecord;
    }

//    private Point2D.Double calculatePlotPoint(TimeSeriesRecord record) {
//        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
//        long deltaTime = ChronoUnit.MILLIS.between(startInstant, record.instant);
//        double normTime = (double)deltaTime / totalPlotDeltaTime;
//        double x = ((double)plotRectangle.width * normTime);
//
//        if (plotDisplayOption == PlotDisplayOption.SPECTRUM) {
//            double y = GraphicsUtil.mapValue(Math.abs(record.value), 0., Math.max(valueAxisMax, Math.abs(valueAxisMin)), 0., plotRectangle.getHeight()/2.);
//            return new Point2D.Double(x, y);
//        } else {
//            double norm = (record.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
//            double yOffset = norm * (plotRectangle.height);
//            double y = plotRectangle.height - yOffset;
//
//            return new Point2D.Double(x, y);
//        }
//    }

    public int getXForInstant(Instant instant) {
        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
        long deltaTime = ChronoUnit.MILLIS.between(startInstant, instant);
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        double x = plotRectangle.x + ((double)plotRectangle.width * normTime);
        return (int)x;
    }

    private void drawTimeSeries(Graphics2D g2, Instant startClipInstant, Instant endClipInstant) {
//        g2.setColor(Color.blue);
//        g2.draw(plotRectangle);

        g2.translate(plotRectangle.x, plotRectangle.y);
//        log.debug("plotPointMap.size() = " + plotPointMap.size());

        if (shrinkToFit) {
//            TimeSeriesRenderer.renderAsOverview(g2, timeSeries, plotRectangle.width, plotRectangle.height,
//                    plotUnitWidth, plotChronoUnit, plotDisplayOption, gridLineColor, pointColor, pointColor.darker(),
//                    rangeColor.brighter(), rangeColor, summaryInfoArray, valueAxisMin, valueAxisMax);
//            if (summaryInfoArray != null) {
            if (!plotPointRecordMap.isEmpty()) {
                g2.setColor(gridLineColor);
                drawZeroLine(g2, plotRectangle.width, plotRectangle.height, valueAxisMin, valueAxisMax);

                Path2D.Double maxPath = null;
                Path2D.Double minPath = null;
                Path2D.Double upperStDevRangePath = null;
                Path2D.Double lowerStDevRangePath = null;
                Path2D.Double meanPath = null;

                g2.setColor(lineColor);
//                TimeSeriesSummaryInfo lastSummaryInfo = null;
                TimeSeriesPlotPointRecord lastPlotPointRecord = null;
                for (ArrayList<TimeSeriesPlotPointRecord> instantRecords : plotPointRecordMap.values()) {
                    for (TimeSeriesPlotPointRecord plotPointRecord : instantRecords) {
                        if (plotDisplayOption == PlotDisplayOption.POINT) {
                            Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x, plotPointRecord.meanY,
                                    plotUnitWidth, plotUnitWidth);
                            g2.draw(ellipse);
                        } else if (plotDisplayOption == PlotDisplayOption.LINE) {
                            if (meanPath == null) {
                                meanPath = new Path2D.Double();
                                meanPath.moveTo(plotPointRecord.x, plotPointRecord.meanY);
                                maxPath = new Path2D.Double();
                                maxPath.moveTo(plotPointRecord.x, plotPointRecord.maxY);
                                minPath = new Path2D.Double();
                                minPath.moveTo(plotPointRecord.x, plotPointRecord.minY);
                                upperStDevRangePath = new Path2D.Double();
                                upperStDevRangePath.moveTo(plotPointRecord.x, plotPointRecord.upperStdevRangeY);
                                lowerStDevRangePath = new Path2D.Double();
                                lowerStDevRangePath.moveTo(plotPointRecord.x, plotPointRecord.lowerStdevRangeY);
                            } else {
                                meanPath.lineTo(plotPointRecord.x, plotPointRecord.meanY);
                                maxPath.lineTo(plotPointRecord.x, plotPointRecord.maxY);
                                minPath.lineTo(plotPointRecord.x, plotPointRecord.minY);
                                upperStDevRangePath.lineTo(plotPointRecord.x, plotPointRecord.upperStdevRangeY);
                                lowerStDevRangePath.lineTo(plotPointRecord.x, plotPointRecord.lowerStdevRangeY);
                            }
                            g2.setColor(pointColor);
                            Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1, plotPointRecord.meanY - 1, 2., 2.);
                            g2.draw(ellipse);
                        } else if (plotDisplayOption == PlotDisplayOption.STEPPED_LINE) {

                        } else if (plotDisplayOption == PlotDisplayOption.BAR) {

                        }
                    }
                }
//                for (int i = 0; i < summaryInfoArray.length; i++) {
//                    TimeSeriesSummaryInfo summaryInfo = summaryInfoArray[i];

//                    if (summaryInfo != null) {
//                        if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.POINT) {
//                            Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - plotUnitWidth / 2., summaryInfo.meanPoint.getY() - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
//                            g2.draw(ellipse);
//                        } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.LINE) {
//                            if (meanPath == null) {
//                                meanPath = new Path2D.Double();
//                                meanPath.moveTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                                maxPath = new Path2D.Double();
//                                maxPath.moveTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                                minPath = new Path2D.Double();
//                                minPath.moveTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                                upperStDevRangePath = new Path2D.Double();
//                                upperStDevRangePath.moveTo(summaryInfo.upperStandardDeviationRangePoint.getX(), summaryInfo.upperStandardDeviationRangePoint.getY());
//                                lowerStDevRangePath = new Path2D.Double();
//                                lowerStDevRangePath.moveTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), summaryInfo.lowerStandardDeviationRangePoint.getY());
//                            } else {
//                                meanPath.lineTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                                maxPath.lineTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                                minPath.lineTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                                upperStDevRangePath.lineTo(summaryInfo.upperStandardDeviationRangePoint.getX(), summaryInfo.upperStandardDeviationRangePoint.getY());
//                                lowerStDevRangePath.lineTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), summaryInfo.lowerStandardDeviationRangePoint.getY());
//                            }
//                            g2.setColor(pointColor);
//                            Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
//                            g2.draw(ellipse);
//                        } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE) {
//                            if (lastSummaryInfo != null) {
//                                // draw mean line
//                                g2.setColor(lineColor);
//                                Line2D.Double line1 = new Line2D.Double(lastSummaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY(), summaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY());
//                                Line2D.Double line2 = new Line2D.Double(summaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY(), summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                                g2.draw(line1);
//                                g2.draw(line2);
//                            }
//                            g2.setColor(pointColor);
//                            Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
//                            g2.draw(ellipse);
//                        } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.BAR) {
//
//                        }
//                        lastSummaryInfo = summaryInfo;
//                    }
//                }

                if (meanPath != null) {
                    g2.setColor(minmaxRangeColor);
                    g2.draw(maxPath);
                    g2.draw(minPath);
                    g2.setColor(standardDeviationRangeColor);
                    g2.draw(upperStDevRangePath);
                    g2.draw(lowerStDevRangePath);
                    g2.setColor(lineColor);
                    g2.draw(meanPath);
                }
            }
        } else {
            if (plotPointRecordMap != null && !plotPointRecordMap.isEmpty()) {
//                TimeSeriesRenderer.renderAsDetailed(g2, timeSeries, startClipInstant, endClipInstant,
//                        plotRectangle.width, plotRectangle.height, plotUnitWidth, plotChronoUnit, plotDisplayOption,
//                        gridLineColor, pointColor, pointColor, rangeColor, plotPointMap,
//                        valueAxisMin, valueAxisMax);
                g2.setColor(gridLineColor);
                drawZeroLine(g2, plotRectangle.width, plotRectangle.height, valueAxisMin, valueAxisMax);

                Instant start = plotPointRecordMap.firstKey();
                if (startClipInstant.isAfter(start)) {
                    start = plotPointRecordMap.lowerKey(startClipInstant);
                }
                Instant end = plotPointRecordMap.lastKey();
                if (endClipInstant.isBefore(end)) {
                    end = plotPointRecordMap.higherKey(endClipInstant);
                }
                NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> clipMap = plotPointRecordMap.subMap(start, true, end, true);
                if (clipMap.isEmpty()) {
                    log.debug("No records in clip range - nothing to draw");
                } else {
                    TimeSeriesPlotPointRecord lastDrawnPointRecord = null;
                    int numPointsDrawn = 0;
                    for (ArrayList<TimeSeriesPlotPointRecord> instantRecords : clipMap.values()) {
                        for (TimeSeriesPlotPointRecord plotPointRecord : instantRecords) {
                            if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.POINT) {
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - plotUnitWidth / 2.,
                                        plotPointRecord.valueY - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
                                g2.setColor(pointColor);
                                g2.draw(ellipse);
                            } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.LINE) {
                                if (lastDrawnPointRecord != null) {
                                    Line2D.Double line = new Line2D.Double(lastDrawnPointRecord.x,
                                            lastDrawnPointRecord.valueY, plotPointRecord.x, plotPointRecord.valueY);
                                    g2.setColor(lineColor);
                                    g2.draw(line);
                                }
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1,
                                        plotPointRecord.valueY - 1, 2., 2.);
                                g2.setColor(pointColor);
                                g2.draw(ellipse);
                            } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE) {
                                if (lastDrawnPointRecord != null) {
                                    Line2D.Double line1 = new Line2D.Double(lastDrawnPointRecord.x, lastDrawnPointRecord.valueY, plotPointRecord.x, lastDrawnPointRecord.valueY);
                                    Line2D.Double line2 = new Line2D.Double(plotPointRecord.x, lastDrawnPointRecord.valueY, plotPointRecord.x, plotPointRecord.valueY);
                                    g2.setColor(lineColor);
                                    g2.draw(line1);
                                    g2.draw(line2);
                                }
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1, plotPointRecord.valueY - 1, 2., 2.);
                                g2.setColor(pointColor);
                                g2.draw(ellipse);
                            } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.SPECTRUM) {
//                                double currentTopY = (plotRectangle.height / 2.) - plotPointRecord.valueY;
//                                double currentBottomY = (plotRectangle.height / 2.) + plotPointRecord.valueY;
//                                if (lastDrawnPoint != null) {
//                                    g2.setColor(lineColor);
//
//                                    double lastTopY = (plotRectangle.height / 2.) - lastDrawnPoint.y;
//                                    Line2D.Double topLine = new Line2D.Double(lastDrawnPoint.x, lastTopY, point.x, currentTopY);
//                                    g2.draw(topLine);
//
//                                    double lastBottomY = (plotRectangle.height / 2.) + lastDrawnPoint.y;
//                                    Line2D.Double bottomLine = new Line2D.Double(lastDrawnPoint.x, lastBottomY, point.x, currentBottomY);
//                                    g2.draw(bottomLine);
//                                }

                                g2.setColor(plotPointRecord.color);
                                Line2D.Double line = new Line2D.Double(plotPointRecord.x, plotPointRecord.spectrumTopY, plotPointRecord.x, plotPointRecord.spectrumBottomY);
                                g2.draw(line);
//                                Ellipse2D.Double topEllipse = new Ellipse2D.Double(point.x - 1, currentTopY - 1, 2., 2.);
//                                g2.draw(topEllipse);
//                                Ellipse2D.Double bottomEllipse = new Ellipse2D.Double(point.x - 1, currentBottomY - 1, 2., 2.);
//                                g2.draw(bottomEllipse);
                            }
                            lastDrawnPointRecord = plotPointRecord;
                            numPointsDrawn++;
                        }
                    }
                }
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
//            g2.setColor(pointColor);
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
//                g2.setColor(pointColor);
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

    private void drawZeroLine(Graphics2D g2, int plotWidth, int plotHeight, double valueAxisMin,
                              double valueAxisMax) {
        if (plotDisplayOption == PlotDisplayOption.SPECTRUM) {
            double zeroY = (plotHeight / 2.);
            Line2D.Double line = new Line2D.Double(0, zeroY, plotWidth, zeroY);
            g2.draw(line);
        } else if (valueAxisMin < 0. && valueAxisMax > 0.) {
            double zeroY = GraphicsUtil.mapValue(0., valueAxisMin, valueAxisMax, plotHeight, 0);
            Line2D.Double line = new Line2D.Double(0, zeroY, plotWidth, zeroY);
            g2.draw(line);
        }
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

            if (!pinMarkerList.isEmpty()) {
                g2.setColor(Color.gray);
                for (PinMarkerInfo pinMarker : pinMarkerList) {
                    if ( (pinMarker.x >= plotRectangle.x) && pinMarker.x <= (plotRectangle.x + plotRectangle.width)) {
                        String strValue = String.valueOf(pinMarker.value);
                        int strWidth = g2.getFontMetrics().stringWidth(strValue);
                        int strX = pinMarker.x - strWidth / 2;
                        if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                            strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                        }
                        if (strX < plotRectangle.x) {
                            strX += (plotRectangle.x - strX);
                        }
                        g2.drawString(strValue, strX, valueInfoBarBottom - 2);
                    }
                }
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

            if (!pinMarkerList.isEmpty()) {
                g2.setColor(Color.gray);
                for (PinMarkerInfo pinMarker : pinMarkerList) {
                    if ((pinMarker.x >= clipBounds.x) && (pinMarker.x <= (clipBounds.x + clipBounds.width))) {
                        String strValue = String.valueOf(pinMarker.value);
                        int strWidth = g2.getFontMetrics().stringWidth(strValue);
                        int strX = pinMarker.x - strWidth / 2;
                        if ((strX + strWidth) > (clipBounds.x + clipBounds.width)) {
                            strX -= (strX + strWidth) - (clipBounds.x + clipBounds.width);
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
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (timeSeries != null) {
            g2.setColor(gridLineColor);
            g2.draw(plotRectangle);

            Rectangle clipBounds = g2.getClipBounds();

            double norm = (double)(clipBounds.x - plotRectangle.x) / (plotRectangle.width);
            long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipStartInstant = startInstant.plusMillis(deltaTimeMillis);
            norm = (double)((clipBounds.x + clipBounds.width) - plotRectangle.x) / (plotRectangle.width);
            deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipEndInstant = startInstant.plusMillis(deltaTimeMillis);

            g2.setFont(g2.getFont().deriveFont(12.f));

//            if (highlightRectangle != null) {
//                g2.setColor(unselectedRegionFillColor);
//                g2.fill(plotRectangle);
//                g2.setColor(selectedRegionFillColor);
//                g2.fill(highlightRectangle);
//            }

            drawTimeSeries(g2, clipStartInstant, clipEndInstant);
            drawTimeInfoBar(g2);
            drawTimeSelections(g2);

            if (highlightRectangle != null) {
                g2.setColor(Color.darkGray);
                g2.setStroke(new BasicStroke(2.f));
                Line2D.Double topLine = new Line2D.Double(highlightRectangle.x, highlightRectangle.y, highlightRectangle.getMaxX(), highlightRectangle.y);
                Line2D.Double bottomLine = new Line2D.Double(highlightRectangle.x, highlightRectangle.getMaxY(), highlightRectangle.getMaxX(), highlightRectangle.getMaxY());
                g2.draw(topLine);
                g2.draw(bottomLine);
            }

            g2.setColor(hoverLineColor);
            g2.setStroke(new BasicStroke(1.f));
            if (!pinMarkerList.isEmpty()) {
                for (PinMarkerInfo pinMarker : pinMarkerList) {
                    if ((pinMarker.x >= plotRectangle.x) && (pinMarker.x <= (plotRectangle.x + plotRectangle.width))) {
                        g2.drawLine(pinMarker.x, plotRectangle.y, pinMarker.x, (plotRectangle.y + plotRectangle.height));
                    }
                }
            }

            if (hoverInstant != null) {
                g2.drawLine(hoverX, plotRectangle.y, hoverX, (plotRectangle.y + plotRectangle.height));
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

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Random random = new Random(System.currentTimeMillis());

//                    int numTimeRecords = 50400;
                    int numTimeRecords = 86400;
//                    int numTimeRecords = 1200;
                    int plotUnitWidth = 10;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    TimeSeriesPanel detailsTimeSeriesPanel = new TimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, PlotDisplayOption.SPECTRUM);
                    detailsTimeSeriesPanel.setBackground(Color.white);
                    detailsTimeSeriesPanel.setMovingRangeDisplayOption(MovingRangeDisplayOption.PLOT_VALUE);

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

                    overviewTimeSeriesPanel.setPinningEnabled(false);
                    overviewTimeSeriesPanel.setInteractiveSelectionEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    class PinMarkerInfo {
        Instant instant;
        double value;
        int x;
    }

    public enum PlotDisplayOption {
        STEPPED_LINE, LINE, BAR, POINT, SPECTRUM
    }

    public enum MovingRangeDisplayOption {
        PLOT_VALUE, OPACITY, NOT_SHOWN
    }
}
