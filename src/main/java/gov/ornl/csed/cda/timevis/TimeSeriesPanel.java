package gov.ornl.csed.cda.timevis;

import gov.ornl.csed.cda.util.GraphicsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by csg on 6/21/16.
 */
public abstract class TimeSeriesPanel extends JComponent
        implements ComponentListener, MouseListener, MouseMotionListener {


    // Default settings
    public static final int HIGHLIGHT_RANGE_MIN_SIZE = 8;
    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    // time series objects
    protected TimeSeries timeSeries;
    protected BinnedTimeSeries binnedTimeSeries;

    // plot layout parameters
    protected ChronoUnit plotChronoUnit;
    protected int plotUnitWidth = 1;
    protected boolean shrinkToFit = false;
    protected NumericTimeSeriesPanel.PlotDisplayOption plotDisplayOption = NumericTimeSeriesPanel.PlotDisplayOption.POINT;

    // plot drawing objects
    private ConcurrentSkipListMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> plotPointRecordMap = new ConcurrentSkipListMap<>();

    // instant range objects
    private Instant startInstant;
    private Instant endInstant;
    private Instant plotLeftInstant;
    private Instant plotRightInstant;
    private Rectangle plotRectangle;
    private int timeInfoBarTop;
    private int timeInfoBarBottom;
    private int valueInfoBarTop;
    private int valueInfoBarBottom;
    private Duration totalDuration;
    private int plotNameBarHeight = 14;
    private int timeBarHeight = 14;
    private int valueBarHeight = 14;
    private int numPlotUnits = 0;
    private int plotUnitDurationMillis = 0;

    // mouse hovering and interaction variables
    private int hoverX;
    private Instant hoverInstant;
    private TimeSeriesRecord hoverRecord = null;
    private TimeSeriesBin hoverBin = null;
//    private double hoverValue = Double.NaN;
//    private double hoverMRValue = Double.NaN;
    private TimeSeriesSelection hoverTimeSeriesSelection = null;
    private boolean mouseOverPlot = false;
    private boolean mouseOverTimeBar = false;
    private boolean mouseOverValueBar = false;

    // pin marker variables
    private ArrayList<PinMarkerInfo> pinMarkerList = new ArrayList<>();
    private boolean pinningEnabled = true;
    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
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
    private boolean interactiveSelectionEnabled = true;

    // range selection data
    private ArrayList<TimeSeriesSelection> selectionList = new ArrayList<>();
    private ArrayList<TimeSeriesPanelSelectionListener> selectionListeners = new ArrayList<>();


    public TimeSeriesPanel(int plotUnitWidth, ChronoUnit plotChronoUnit) {
        this.plotUnitWidth = plotUnitWidth;
        this.plotChronoUnit = plotChronoUnit;
        shrinkToFit = false;

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public TimeSeriesPanel(int plotUnitWidth) {
        this.plotUnitWidth = plotUnitWidth;
        shrinkToFit = true;

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // abstract methods
    protected abstract void drawTimeSeries(Graphics2D g2, Instant startClipInstant, Instant endClipInstant);
    protected abstract void calculatePlotPoints(BinnedTimeSeries binnedTimeSeries, NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> pointMap);
    protected abstract void calculatePlotPoints(TimeSeries timeSeries, NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> pointMap);

    protected ConcurrentSkipListMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> getPointRecordMap() {
        return plotPointRecordMap;
    }

    public boolean isInteractiveSelectionsEnabled() {
        return interactiveSelectionEnabled;
    }


    public void setGridLineColor(Color color) {
        gridLineColor = color;
        repaint();
    }

    public Color getGridLineColor() {
        return gridLineColor;
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


    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (timeSeries != null) {

            Rectangle clipBounds = g2.getClipBounds();

            double norm = (double)(clipBounds.x - plotRectangle.x) / (plotRectangle.width);
            long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipStartInstant = startInstant.plusMillis(deltaTimeMillis);
            norm = (double)((clipBounds.x + clipBounds.width) - plotRectangle.x) / (plotRectangle.width);
            deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            Instant clipEndInstant = startInstant.plusMillis(deltaTimeMillis);

            g2.setFont(g2.getFont().deriveFont(12.f));

            drawTimeSeries(g2, clipStartInstant, clipEndInstant);

            // the code below hides points that are scaled outside the drawing bounds of the plot in case any are
            // not hidden in the drawing code itself
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), timeInfoBarBottom);
            g2.fillRect(0, valueInfoBarTop, getWidth(), getHeight() - valueInfoBarTop);

            g2.setColor(gridLineColor);
            g2.draw(plotRectangle);

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

    protected void drawTimeSelections(Graphics2D g2) {
        if (selectionList != null && !selectionList.isEmpty()) {
            for (TimeSeriesSelection selection : selectionList) {
                RoundRectangle2D.Float selectionRect = new RoundRectangle2D.Float(selection.getStartScreenLocation(),
                        plotRectangle.y, selection.getEndScreenLocation() - selection.getStartScreenLocation(),
                        plotRectangle.height, 2.f, 2.f);
                RoundRectangle2D.Float selectionRectOutline = new RoundRectangle2D.Float(selectionRect.x - 1,
                        selectionRect.y-1, selectionRect.width + 2, selectionRect.height + 2, 2f, 2f);

                g2.setColor(Color.darkGray);
                g2.draw(selectionRectOutline);
                g2.setColor(Color.orange);
                g2.draw(selectionRect);
            }
        }
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


    public int getChronoUnitWidth() {
        return plotUnitWidth;
    }

    public void setChronoUnitWidth(int width) {
        this.plotUnitWidth = width;
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

                    if (hoverRecord != null) {
                        PinMarkerInfo pinMarker = new PinMarkerInfo();
                        pinMarker.instant = Instant.from(hoverInstant);
                        pinMarker.record = hoverRecord;
                        pinMarker.bin = null;
                        pinMarker.x = hoverX;
                        pinMarkerList.add(pinMarker);
                        repaint();
                    } else if (hoverBin != null) {
                        PinMarkerInfo pinMarker = new PinMarkerInfo();
                        pinMarker.instant = Instant.from(hoverInstant);
                        pinMarker.record = null;
                        pinMarker.bin = hoverBin;
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


    protected Instant getHoverInstant() {
        return hoverInstant;
    }


    @Override
    public void mouseMoved(MouseEvent e) {
        hoverInstant = null;
        hoverX = -1;
        hoverRecord = null;
        hoverBin = null;
        hoverTimeSeriesSelection = null;

        if ((plotRectangle != null)) {
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
                TimeSeriesBin bin = binnedTimeSeries.getNearestBin(hoverInstant);
                if (bin != null) {
                    hoverBin = bin;
                }
            } else {
                ArrayList<TimeSeriesRecord> nearRecords = timeSeries.getNearestRecordsFor(hoverInstant, Duration.of(2, plotChronoUnit));
                if (nearRecords != null && !nearRecords.isEmpty()) {
                    // TODO: Indicate here if more than one record is nearest
                    hoverRecord = nearRecords.get(0);
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


    protected void calculatePlotPoints() {
        plotPointRecordMap.clear();

        if (timeSeries != null && !timeSeries.isEmpty()) {
            if (shrinkToFit) {
                binnedTimeSeries = new BinnedTimeSeries(timeSeries, Duration.ofMillis(plotUnitDurationMillis));
                calculatePlotPoints(binnedTimeSeries, plotPointRecordMap);
            } else {
                calculatePlotPoints(timeSeries, plotPointRecordMap);
            }
        }
    }

//    protected void calculatePlotPoints() {
//        if (shrinkToFit) {
//            plotPointRecordMap.clear();
//            if (timeSeries != null) {
//                if (!timeSeries.isEmpty()) {
//                    binnedTimeSeries = new BinnedTimeSeries(timeSeries, Duration.ofMillis(plotUnitDurationMillis));
//                    Collection<TimeSeriesBin> bins = binnedTimeSeries.getAllBins();
//                    for (TimeSeriesBin bin : bins) {
//                        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(bin);
//                        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(bin.getInstant());
//                        if (instantRecords == null) {
//                            instantRecords = new ArrayList<>();
//                            plotPointRecordMap.put(bin.getInstant(), instantRecords);
//                        }
//                        instantRecords.add(plotPointRecord);
//                    }
//                }
//            }
//        } else {
//            plotPointRecordMap.clear();
//            if (timeSeries != null) {
//                for (ArrayList<TimeSeriesRecord> valueRecordList : timeSeries.getRecordMap().values()) {
////                    ArrayList<TimeSeriesRecord> movingRangeRecordList = null;
////                    if (movingRangeTimeSeries != null) {
////                        movingRangeRecordList = movingRangeTimeSeries.getRecordMap().get(valueRecordList.get(0).instant);
////                    }
//
//                    for (int i = 0; i < valueRecordList.size(); i++) {
//                        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(valueRecordList.get(i));
////                        if (movingRangeRecordList != null) {
////                            plotPointRecord = calculatePlotPointRecord(valueRecordList.get(i), movingRangeRecordList.get(i));
////                        } else {
////                            plotPointRecord = calculatePlotPointRecord(valueRecordList.get(i), null);
////                        }
//
//                        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(plotPointRecord.valueRecord.instant);
//                        if (instantRecords == null) {
//                            instantRecords = new ArrayList<>();
//                            plotPointRecordMap.put(plotPointRecord.valueRecord.instant, instantRecords);
//                        }
//
//                        instantRecords.add(plotPointRecord);
//                    }
//                }
//            }
//        }
//    }


    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(TimeSeries timeSeries, Instant startInstant, Instant endInstant) {
//        if (this.timeSeries != null) {
//            this.timeSeries.removeTimeSeriesListener(this);
//        }

        this.timeSeries = timeSeries;
//        timeSeries.addTimeSeriesListener(this);

//        if (movingRangeDisplayOption != NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN) {
//            calculateMovingRangeTimeSeries();
//        } else {
//            movingRangeTimeSeries = null;
//        }

        // compute a time buffer to add to start and end of given start and end instants that ensures all values
        // will be visible
        long timeBufferMillis = (long) (Duration.between(startInstant, endInstant).toMillis() * 0.0001);
        timeBufferMillis = Math.max(timeBufferMillis, 100l);

        this.startInstant = startInstant.minusMillis(timeBufferMillis);
        this.endInstant = endInstant.plusMillis(timeBufferMillis);
        totalDuration = Duration.between(this.startInstant, this.endInstant);

        // Apply a small buffer to expand the min / max value range to prevent extreme points
        // from being drawn on the plot boundaries
//        double buffer = 0.05 * (timeSeries.getMaxValue() - timeSeries.getMinValue());
//        valueAxisMin = timeSeries.getMinValue() - buffer;
//        valueAxisMax = timeSeries.getMaxValue() + buffer;

        // TODO: this should be removed if we override the method (don't want to layout twice if we can avoid it)
        layoutPanel();
    }

//    @Override
//    public void timeSeriesRecordAdded(TimeSeries timeSeries, TimeSeriesRecord record) {
//        // if new record is outside the current start or end instants, update those instants,
//        // layout the panel, recalculate the plot points, and repaint
//        // else just calculate the new point location
//        // TODO: How to handle overview summary points?  And Moving Range?
////        log.debug("in timeSeriesRecordAdded() " + plotPointMap.size());
//
//        boolean doLayout = false;
//
////        if (record.value < valueAxisMin) {
////            valueAxisMin = record.value;
////            doLayout = true;
////        }
////        if (record.value > valueAxisMax) {
////            valueAxisMax = record.value;
////            doLayout = true;
////        }
//
//        if (record.instant.isBefore(startInstant)) {
//            startInstant = Instant.from(record.instant);
//            doLayout = true;
//        } else if (record.instant.isAfter(endInstant)) {
//            endInstant = Instant.from(record.instant);
//            doLayout = true;
//        }
//
//        if (doLayout) {
//            layoutPanel();
//        }
//
//        TimeSeriesPlotPointRecord plotPointRecord = calculatePlotPointRecord(record);
//
//        ArrayList<TimeSeriesPlotPointRecord> instantRecords = plotPointRecordMap.get(record.instant);
//        if (instantRecords == null) {
//            instantRecords = new ArrayList<>();
//            plotPointRecordMap.put(record.instant, instantRecords);
//        }
//        instantRecords.add(plotPointRecord);
//        repaint();
//    }

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


    public int getPlotUnitWidth () {
        return plotUnitWidth;
    }

    public void setPlotUnitWidth (int width) {
        plotUnitWidth = width;
        layoutPanel();
    }

    public NumericTimeSeriesPanel.PlotDisplayOption getPlotDisplayOption () {
        return plotDisplayOption;
    }

    public void setPlotDisplayOption (NumericTimeSeriesPanel.PlotDisplayOption plotDisplayOption) {
        if (this.plotDisplayOption != plotDisplayOption) {
            boolean recalculatePlotPoints = true;
            if ((plotDisplayOption != NumericTimeSeriesPanel.PlotDisplayOption.SPECTRUM) && (this.plotDisplayOption != NumericTimeSeriesPanel.PlotDisplayOption.SPECTRUM)) {
                recalculatePlotPoints = false;
            }

            this.plotDisplayOption = plotDisplayOption;

            if (recalculatePlotPoints) {
                calculatePlotPoints();
            }
            repaint();
        }
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

    protected void layoutPanel() {
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

    protected Rectangle getPlotRectangle() {
        return plotRectangle;
    }

    public int getXForInstant(Instant instant) {
        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(startInstant, endInstant);
        long deltaTime = ChronoUnit.MILLIS.between(startInstant, instant);
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        double x = plotRectangle.x + ((double)plotRectangle.width * normTime);
        return (int)x;
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

            String valueString = getInfoBarString();

            if (valueString != null) {
                g2.setColor(Color.black);
                int strWidth = g2.getFontMetrics().stringWidth(valueString);
                int strX = hoverX - (strWidth / 2);
                if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                    strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                }
                if (strX < plotRectangle.x) {
                    strX += (plotRectangle.x - strX);
                }
                g2.drawString(valueString, strX, valueInfoBarBottom - 2);
            }

            if (!pinMarkerList.isEmpty()) {
                g2.setColor(Color.gray);
                for (PinMarkerInfo pinMarker : pinMarkerList) {
                    if ( (pinMarker.x >= plotRectangle.x) && pinMarker.x <= (plotRectangle.x + plotRectangle.width)) {
                        String pinInfoString = getPinInfoString(pinMarker);
                        if ((pinInfoString != null) && !(pinInfoString.isEmpty())) {
                            int strWidth = g2.getFontMetrics().stringWidth(pinInfoString);
                            int strX = pinMarker.x - (strWidth / 2);
                            if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                                strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                            }
                            if (strX < plotRectangle.x) {
                                strX += (plotRectangle.x - strX);
                            }
                            g2.drawString(pinInfoString, strX, valueInfoBarBottom - 2);
                        }
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

            String valueString = getInfoBarString();

            if (valueString != null) {
                g2.setColor(Color.black);
                int strWidth = g2.getFontMetrics().stringWidth(valueString);
                int strX = hoverX - (strWidth / 2);
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
                g2.drawString(valueString, strX, valueInfoBarBottom - 2);
            }

            if (!pinMarkerList.isEmpty()) {
                g2.setColor(Color.gray);
                for (PinMarkerInfo pinMarker : pinMarkerList) {
                    if ((pinMarker.x >= clipBounds.x) && (pinMarker.x <= (clipBounds.x + clipBounds.width))) {
                        String pinInfoString = getPinInfoString(pinMarker);
                        if ((pinInfoString != null) && !(pinInfoString.isEmpty())) {
                            int strWidth = g2.getFontMetrics().stringWidth(pinInfoString);
                            int strX = pinMarker.x - (strWidth / 2);
                            if ((strX + strWidth) > (plotRectangle.x + plotRectangle.width)) {
                                strX -= (strX + strWidth) - (plotRectangle.x + plotRectangle.width);
                            }
                            if (strX < plotRectangle.x) {
                                strX += (plotRectangle.x - strX);
                            }
                            g2.drawString(pinInfoString, strX, valueInfoBarBottom - 2);
                        }
                    }
                }
            }
        }
    }

    protected String getInfoBarString() {
        if (shrinkToFit) {
            if (hoverBin != null) {
                return "V: " + String.valueOf(hoverBin.getStatistics().getMean());
            }
        } else {
            if (hoverRecord != null) {
                return "V: " + String.valueOf(hoverRecord.value);
            }
        }

        return null;
    }

    protected String getPinInfoString(PinMarkerInfo pinMarker) {
        if (pinMarker.bin != null) {
            return "V: " + String.valueOf(pinMarker.bin.getStatistics().getMean());
        } else if (pinMarker.record != null){
            return "V: " + String.valueOf(pinMarker.record.value);
        }

        return null;
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


    protected class PinMarkerInfo {
        Instant instant;
        TimeSeriesRecord record;
        TimeSeriesBin bin;
        int x;
    }
}
