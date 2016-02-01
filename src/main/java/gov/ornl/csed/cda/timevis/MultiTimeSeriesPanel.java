package gov.ornl.csed.cda.timevis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class MultiTimeSeriesPanel extends JComponent implements MouseMotionListener, MouseListener {
    private final static Logger log = LoggerFactory.getLogger(MultiTimeSeriesPanel.class);

    static JScrollPane scroller;
    private ArrayList<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
    private Instant startInstant;
    private Instant endInstant;
    private ChronoUnit chronoUnit;

    // layout stuff
    private int plotHeight = 60;
    private int plotInfoBarHeight = 14;
    private int plotNameBarHeight = 14;
    private Insets margins = new Insets(2, 4, 2, 4);
    private int plotChronoUnitWidth = 1;
    private int plotLeft;
    private int plotRight;
    private int plotTop;
    private int plotBottom;
    private int timeInfoBarHeight = 14;
    private Rectangle timeInfoBarRectangle;
    private int dragHandleWidth = 12;
    private ArrayList<Rectangle> dragHandleRectangles = new ArrayList<Rectangle>();
    private ArrayList<Rectangle> timeSeriesRectangles = new ArrayList<>();
    private ArrayList<Rectangle> removeButtonRectangles = new ArrayList<>();

    // mouse hover stuff
    private int hoverX;
    private int hoverTimeSeriesIndex = -1;
    private ArrayList<Double> hoverTimeSeriesValues = new ArrayList<Double>();
    private Instant hoverInstant = null;
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Rectangle hoverDragHandleRectangle = null;
    private Rectangle hoverRemoveButtonRectangle = null;

    // general dragging stuff
    private Point startDragPoint = new Point();
    private Point endDragPoint = new Point();

    // drag time series stuff
    private int dragPlotNewIndex;
    private boolean draggingTimeSeries = false;
    private int draggingYOffset;
    private TimeSeriesSelection hoverTimeSeriesSelection;

    // time series translation stuff
    private boolean translatingTimeSeries = false;

    // drag time range selection stuff
    private TimeSeriesSelection draggingTimeSeriesSelection;
    private boolean draggingSelection = false;
    private HashMap<TimeSeries, ArrayList<TimeSeriesSelection>> timeSeriesSelectionMap = new HashMap<TimeSeries, ArrayList<TimeSeriesSelection>>();
    private DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private ArrayList<TimeSeriesPlotInfo> timeSeriesPlotInfoList;

    // color preferences
    private Color timeInfoBarColor = new Color(255, 255, 255, 150);
    private Color gridLineColor = new Color(230, 230, 230);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    private Color dataColor = new Color(80, 80, 130, 180);
    private Color rangeColor = new Color(140, 140, 160, 100);

    public MultiTimeSeriesPanel() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public static void main (String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {

                    Color dataColor = new Color(80, 80, 110, 140);
                    Color rangeColor = new Color(140, 140, 150, 100);

                    int numTimeSeries = 3;
                    int numTimeSeriesRecords = 50400;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    MultiTimeSeriesPanel timeSeriesPanel = new MultiTimeSeriesPanel();
                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeSeriesRecords + 50, ChronoUnit.MINUTES);
                    timeSeriesPanel.setDateTimeRange(startInstant.minus(120, ChronoUnit.MINUTES), endInstant, ChronoUnit.MINUTES);

                    // add some bar graph time series
                    for (int i = 0; i < numTimeSeries; i++) {
                        double value = 0.;
                        double uncertaintyValue = 0.;
                        TimeSeries timeSeries = new TimeSeries("V"+i);
                        for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                            Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                            value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
                            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                        }
                        timeSeriesPanel.addTimeSeries(timeSeries);
                    }

                    // add some line graph with range time series
                    double maxValue = Double.MIN_VALUE;
                    double minValue = Double.MAX_VALUE;
                    Random rand = new Random();
                    for (int i = 0; i < numTimeSeries; i++) {
                        double value = 0.;
                        double upperRange = 0.;
                        double lowerRange = 0.;

                        TimeSeries timeSeries = new TimeSeries("V"+i);
                        for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                            if (rand.nextBoolean()) {
                                itime += 100;
                                continue;
                            }
                            Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                            value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
                            double range = Math.abs(value) * .25;
                            upperRange = value + range;
                            lowerRange = value - range;
                            timeSeries.addRecord(instant, value, upperRange, lowerRange);
                            maxValue = Math.max(maxValue, Math.max(upperRange, value));
                            minValue = Math.min(minValue, Math.min(lowerRange, value));
                        }
                        timeSeriesPanel.addTimeSeries(timeSeries);
                    }

                    scroller = new JScrollPane(timeSeriesPanel);
                    scroller.getVerticalScrollBar().setUnitIncrement(10);
                    scroller.getHorizontalScrollBar().setUnitIncrement(10);
                    ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                    frame.setSize(1000, 300);
                    frame.setVisible(true);


//                    Timer timer = new Timer(40, new ActionListener() {
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            if (startTime < 0) {
//                                startTime = System.currentTimeMillis();
//                                range = scroller.getViewport().getView().getPreferredSize().width - scroller.getWidth();
//                            }
//                            long duration = System.currentTimeMillis() - startTime;
//                            float progress = 1f;
//                            if (duration >= runningTime) {
//                                startTime = -1;
//                                direction *= -1;
//                                // Make the progress equal the maximum range for the new direction
//                                // This will prevent it from "bouncing"
//                                if (direction < 0) {
//                                    progress = 1f;
//                                } else {
//                                    progress = 0f;
//                                }
//                            } else {
//                                progress = (float) duration / (float) runningTime;
//                                if (direction < 0) {
//                                    progress = 1f - progress;
//                                }
//                            }
//
//                            int xPos = (int) (range * progress);
//
//                            scroller.getViewport().setViewPosition(new Point(xPos, 0));
//
//                        }
//                    });
//                    timer.setRepeats(true);
//                    timer.setCoalesce(true);
//                    timer.start();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }
            }
        });
    }

    public void setDataColor (Color dataColor) {
        this.dataColor = dataColor;
        repaint();
    }

    public Color getDataColor() {
        return dataColor;
    }

    public int getPlotHeight() {
        return this.plotHeight;
    }

    public void setPlotHeight(int plotHeight) {
        this.plotHeight = plotHeight;
        layoutPanel();
        repaint();
    }

    public int getChronoUnitWidth() {
        return plotChronoUnitWidth;
    }

    public void setChronoUnitWidth(int chronoUnitWidth) {
        plotChronoUnitWidth = chronoUnitWidth;
        layoutPanel();
        repaint();
    }

    public boolean hasTimeSeries (String timeSeriesName) {
        for (TimeSeries timeSeries : timeSeriesList) {
            if (timeSeries.getName().equals(timeSeriesName)) {
                return true;
            }
        }
        return false;
    }

    public TimeSeries getTimeSeries(int index) {
        return timeSeriesList.get(index);
    }

    public TimeSeries getTimeSeries(String timeSeriesName) {
        for (TimeSeries timeSeries : timeSeriesList) {
            if (timeSeries.getName().equals(timeSeriesName)) {
                return timeSeries;
            }
        }

        return null;
    }

    public int getTimeSeriesCount() {
        return timeSeriesList.size();
    }

    public TimeSeries removeTimeSeries (String timeSeriesName) {
        for (TimeSeries timeSeries : timeSeriesList) {
            if (timeSeries.getName().equals(timeSeriesName)) {
                removeTimeSeries(timeSeries);
            }
        }
        return null;
    }

    public TimeSeries removeTimeSeries (TimeSeries timeSeries) {
        timeSeriesList.remove(timeSeries);
        resetTimeRange();
        layoutPanel();
        return timeSeries;
    }

    private void resetTimeRange() {
        if (timeSeriesList.isEmpty()) {
            startInstant = null;
            endInstant = null;
        } else {
            startInstant = timeSeriesList.get(0).getStartInstant();
            endInstant = timeSeriesList.get(0).getEndInstant();
            for (int i = 0; i < timeSeriesList.size(); i++) {
                TimeSeries timeSeries = timeSeriesList.get(i);
                if (timeSeries.getStartInstant().isBefore(startInstant)) {
                    startInstant = Instant.from(timeSeries.getStartInstant());
                }
                if (timeSeries.getEndInstant().isAfter(endInstant)) {
                    endInstant = Instant.from(timeSeries.getEndInstant());
                }
            }
        }
    }

    public void addTimeSeries(TimeSeries timeSeries) {
        timeSeriesList.add(timeSeries);
        resetTimeRange();
        layoutPanel();
    }

    private void drawRemoveButton(Graphics2D g2, boolean highlighted) {
        Color lightColor = new Color(255,255,255);
        Color darkColor = new Color(147,147,147);

        g2.setColor(new Color(220, 220, 220));
        RoundRectangle2D.Float removeButtonShape = new RoundRectangle2D.Float(0, 0, dragHandleWidth, plotNameBarHeight, 6.f, 6.f);
        g2.fill(removeButtonShape);
        if (highlighted) {
            g2.setColor(Color.black);
            g2.draw(removeButtonShape);
        }

        g2.setColor(darkColor);
        int top = 3;
        int bottom = plotNameBarHeight - 3;
        int left = 3;
        int right = dragHandleWidth - 3;

        g2.drawLine(left, top, right, bottom);
        g2.drawLine(right, top, left, bottom);
    }

    private void drawDragHandle(Graphics2D g2, boolean highlighted) {
        Color lightColor = new Color(255,255,255);
        Color darkColor = new Color(147,147,147);

        g2.setColor(new Color(220, 220, 220));
        RoundRectangle2D.Float dragHandleRoundRect = new RoundRectangle2D.Float(0, 1, dragHandleWidth, plotHeight-2, 6.f, 6.f);
        g2.fill(dragHandleRoundRect);
        if (highlighted) {
            g2.setColor(Color.black);
            g2.draw(dragHandleRoundRect);
        }

        g2.setColor(lightColor);
        int x0 = (dragHandleWidth / 2) - 1;
        int y0 = (plotHeight / 2) - 7;
        for (int ix = 0; ix < 2; ix++) {
            int x = x0 + (ix * 2);
            for (int iy = 0; iy < 5; iy++) {
                int y = y0 + (iy * 3) + ix;
                g2.drawLine(x, y, x, y);
            }
        }

        g2.setColor(darkColor);
        x0 = (dragHandleWidth / 2) - 2;
        y0 = (plotHeight / 2) - 7;
        for (int ix = 0; ix < 2; ix++) {
            int x = x0 + (ix * 2);
            for (int iy = 0; iy < 5; iy++) {
                int y = y0 + (iy * 3) + ix;
                g2.drawLine(x, y, x, y);
            }
        }
    }

    private void drawTimeInfoBar(Graphics2D g2) {
        Rectangle clipBounds = g2.getClipBounds();
        int timeInfoBarLeft, timeInfoBarRight;
        Instant rightInstant;
        Instant leftInstant;
        int hoverInstantStringLeft = -1;
        int hoverInstantStringRight = -1;
        int strY = clipBounds.y + timeInfoBarHeight-1;
        int strX;
        int strWidth;

        // draw plot left date time string and plot right date time string for clip bounds
        if (clipBounds.x <= plotLeft) {
            leftInstant = startInstant;
            timeInfoBarLeft = plotLeft;
        } else {
            double deltaTime = (double)(clipBounds.x-plotLeft) / plotChronoUnitWidth;
            leftInstant = startInstant.plus((long)deltaTime, chronoUnit);
            timeInfoBarLeft = clipBounds.x;
        }

        if ((clipBounds.x + clipBounds.width) >= plotRight) {
            rightInstant = endInstant;
            timeInfoBarRight = plotRight;
        } else {
            double deltaTime = (double)(clipBounds.x + clipBounds.width -plotLeft) / plotChronoUnitWidth;
            rightInstant = startInstant.plus((long)deltaTime, chronoUnit);
            timeInfoBarRight = clipBounds.x + clipBounds.width;
        }

        // fill the rectangle area for the time info bar
        g2.setColor(timeInfoBarColor);
        g2.fillRect(timeInfoBarLeft, clipBounds.y, timeInfoBarRight-timeInfoBarLeft, timeInfoBarHeight);

        g2.setColor(Color.black);

        // draw mouse hover date time string
        if (hoverInstant != null) {
            String hoverInstantString = dtFormatter.format(hoverInstant);
            strWidth = g2.getFontMetrics().stringWidth(hoverInstantString);
            hoverInstantStringLeft = (int) (hoverX - strWidth/2.);
            if (hoverInstantStringLeft < clipBounds.x) {
                hoverInstantStringLeft = clipBounds.x;
            }
            hoverInstantStringRight = hoverInstantStringLeft + strWidth;
            if (hoverInstantStringRight > (clipBounds.x + clipBounds.width)) {
                hoverInstantStringLeft -= hoverInstantStringRight - (clipBounds.x + clipBounds.width);
                hoverInstantStringRight = hoverInstantStringLeft + strWidth;
            }

            g2.drawString(hoverInstantString, hoverInstantStringLeft, strY);
        }

        // draw left date time string
        String instantString = dtFormatter.format(leftInstant);
        strWidth = g2.getFontMetrics().stringWidth(instantString);
        if (hoverInstant == null) {
            g2.drawString(instantString, timeInfoBarLeft, strY);
        } else if (hoverInstantStringLeft > (timeInfoBarLeft + strWidth)) {
            g2.drawString(instantString, timeInfoBarLeft, strY);
        }

        // draw right date time string
        instantString = dtFormatter.format(rightInstant);
        strWidth = g2.getFontMetrics().stringWidth(instantString);
        if (hoverInstant == null) {
            g2.drawString(instantString, timeInfoBarRight-strWidth, strY);
        } else if (hoverInstantStringRight < (timeInfoBarRight - strWidth)) {
            g2.drawString(instantString, timeInfoBarRight-strWidth, strY);
        }
    }

    private void drawTimeSeries(Graphics2D g2, TimeSeries timeSeries, double hoverValue, boolean highlightDragHandle,
                                boolean highlightRemoveButton, Instant clipStartInstant, Instant clipEndInstant,
                                TimeSeriesPlotInfo plotInfo) {

        Rectangle clipBounds = g2.getClipBounds();

        g2.translate(plotLeft, 0);
        int nameX = clipBounds.x > 4 ? clipBounds.x : 4;
        g2.setColor(Color.black);
//        g2.drawString(timeSeries.getName(), nameX, plotHeight + plotInfoBarHeight-4);
        g2.drawString(timeSeries.getName(), nameX, plotNameBarHeight-2);

        g2.translate(0, plotNameBarHeight);
        TimeSeriesRenderer.renderAsDetailed(g2, timeSeries, clipStartInstant, clipEndInstant, plotRight-plotLeft,
                plotHeight, plotChronoUnitWidth, chronoUnit, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE, gridLineColor,
                dataColor, dataColor, rangeColor, plotInfo.plotPointMap);
//        timeSeries.draw(g2, startInstant, endInstant, clipStartInstant, clipEndInstant, plotHeight, plotChronoUnitWidth);

        if (!Double.isNaN(hoverValue)) {
            g2.setColor(Color.black);
            String strValue = String.valueOf(hoverValue);
            int strWidth = g2.getFontMetrics().stringWidth(strValue);
            int strX = hoverX - strWidth/2;
            if (strX + strWidth > (clipBounds.x + clipBounds.width)) {
                strX -= (strX+strWidth) - (clipBounds.x + clipBounds.width);
            }
            if (strX < clipBounds.x) {
                strX += (clipBounds.x - strX);
            }
            g2.drawString(strValue, strX, (plotHeight + plotInfoBarHeight) - 2);
        }
        g2.translate(-plotLeft, -plotNameBarHeight);

        g2.translate(margins.left, plotNameBarHeight);
        drawDragHandle(g2, highlightDragHandle);
        g2.translate(-margins.left, -plotNameBarHeight);

        g2.translate(margins.left, 0);
        drawRemoveButton(g2, highlightRemoveButton);
        g2.translate(-margins.left, 0);

        g2.translate(0, plotNameBarHeight);
        ArrayList<TimeSeriesSelection> timeSeriesSelections = timeSeriesSelectionMap.get(timeSeries);
        if (timeSeriesSelections != null && !timeSeriesSelections.isEmpty()) {
            for (TimeSeriesSelection selection : timeSeriesSelections) {
                RoundRectangle2D.Float selectionRect = new RoundRectangle2D.Float(selection.getStartScreenLocation(),
                        0, selection.getEndScreenLocation() - selection.getStartScreenLocation(), plotHeight, 2.f, 2.f);
                RoundRectangle2D.Float selectionRectOutline = new RoundRectangle2D.Float(selectionRect.x - 1,
                        selectionRect.y-1, selectionRect.width + 2, selectionRect.height + 2, 2f, 2f);

//				g2.setStroke(new BasicStroke(2.f));
                g2.setColor(Color.darkGray);
                g2.draw(selectionRectOutline);
                g2.setColor(Color.orange);
                g2.draw(selectionRect);
            }
        }
        g2.translate(0, -plotNameBarHeight);
    }

    public void removeAllTimeSeries() {
        timeSeriesList.clear();
        timeSeriesSelectionMap.clear();
        dragHandleRectangles.clear();
        timeSeriesRectangles.clear();
        startInstant = null;
        endInstant = null;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public void setChronoUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
        layoutPanel();
    }

    public Instant getEndDateTime() {
        return endInstant;
    }

    public Instant getStartDateTime() {
        return startInstant;
    }

    private void layoutPanel() {
        hoverTimeSeriesValues.clear();
        hoverInstant = null;

        long totalTimeUnits = chronoUnit.between(startInstant, endInstant);
        int totalPlotWidth = ((int)totalTimeUnits + 1) * plotChronoUnitWidth;
        plotLeft = margins.left + dragHandleWidth;
//        plotLeft = margins.left;
        plotRight = plotLeft + totalPlotWidth;
        plotTop = margins.top + timeInfoBarHeight;
        plotBottom = plotTop + (timeSeriesList.size() * (plotHeight + plotInfoBarHeight + plotNameBarHeight));

        timeInfoBarRectangle = new Rectangle(margins.left, margins.top, totalPlotWidth, timeInfoBarHeight);

        dragHandleRectangles.clear();
        removeButtonRectangles.clear();
        timeSeriesRectangles.clear();

        timeSeriesPlotInfoList = new ArrayList<>();
        for (int i = 0; i < timeSeriesList.size(); i++) {
            Rectangle timeSeriesRect = new Rectangle(margins.left + dragHandleWidth,
                    margins.top + timeInfoBarHeight + (i * (plotHeight + plotInfoBarHeight + plotNameBarHeight)),
                    totalPlotWidth, plotHeight + plotInfoBarHeight + plotNameBarHeight);
            timeSeriesRectangles.add(timeSeriesRect);

            Rectangle dragHandleRect = new Rectangle(margins.left,
                    margins.top + timeInfoBarHeight + (i * (plotHeight + plotInfoBarHeight + plotNameBarHeight)) + plotNameBarHeight,
                    dragHandleWidth, plotHeight);
            Rectangle removeRect = new Rectangle(margins.left,
                    margins.top + timeInfoBarHeight + (i * (plotHeight + plotInfoBarHeight + plotNameBarHeight)),
                    dragHandleWidth, plotNameBarHeight);
            dragHandleRectangles.add(dragHandleRect);
            removeButtonRectangles.add(removeRect);

            TimeSeriesPlotInfo plotInfo = new TimeSeriesPlotInfo();
            plotInfo.plotRectangle = new Rectangle(timeSeriesRect.x, timeSeriesRect.y + plotNameBarHeight, timeSeriesRect.width, plotHeight);
            plotInfo.plotPointMap = TimeSeriesRenderer.calculateDetailedPlotPoints(timeSeriesList.get(i),
                    chronoUnit, plotChronoUnitWidth, plotInfo.plotRectangle, startInstant);

            timeSeriesPlotInfoList.add(plotInfo);
        }


//        setPreferredSize(new Dimension(totalPlotWidth+(margins.left+margins.right),
//                timeInfoBarHeight + (margins.top + margins.bottom) + (timeSeriesList.size() * (plotHeight + plotInfoBarHeight))));
        setPreferredSize(new Dimension(totalPlotWidth+(margins.left+margins.right) + dragHandleWidth,
                timeInfoBarHeight + (margins.top + margins.bottom) + (timeSeriesList.size() * (plotHeight + plotInfoBarHeight + plotNameBarHeight))));
        revalidate();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (hoverTimeSeriesSelection != null) {
                TimeSeries timeSeries = timeSeriesList.get(hoverTimeSeriesIndex);
                ArrayList<TimeSeriesSelection> selectionList = timeSeriesSelectionMap.get(timeSeries);
                selectionList.remove(hoverTimeSeriesSelection);
                hoverTimeSeriesSelection = null;
                repaint();
            } else if (hoverRemoveButtonRectangle != null) {
                // remove the hover time series from the panel
                if (hoverTimeSeriesIndex != -1) {
                    removeTimeSeries(timeSeriesList.get(hoverTimeSeriesIndex));
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (hoverDragHandleRectangle != null) {
            draggingTimeSeries = true;
            endDragPoint.setLocation(e.getX(), e.getY());

            dragPlotNewIndex = (e.getY() - (margins.top + timeInfoBarHeight)) / (plotHeight + plotInfoBarHeight);
            if (dragPlotNewIndex < 0) {
                dragPlotNewIndex = 0;
            } else if (dragPlotNewIndex > timeSeriesList.size() - 1) {
                dragPlotNewIndex = timeSeriesList.size() - 1;
            }

            repaint();
//        } else if (translatingTimeSeries) {
//            double dx = (double)(e.getX() - endDragPoint.x) / plotChronoUnitWidth;
//            dx = dx + timeSeriesList.get(hoverTimeSeriesIndex).getChronoOffset();
//            endDragPoint.setLocation(e.getPoint());
//            timeSeriesList.get(hoverTimeSeriesIndex).setChronoOffset((int)dx);
//            repaint();
        } else if (hoverTimeSeriesIndex != -1) {
            draggingSelection  = true;
            if (hoverTimeSeriesSelection != null) {
                // translate the time series selection
                int deltaX = e.getX() - endDragPoint.x;
                endDragPoint.setLocation(e.getPoint());
                hoverTimeSeriesSelection.setStartScreenLocation(hoverTimeSeriesSelection.getStartScreenLocation() + deltaX);
                hoverTimeSeriesSelection.setEndScreenLocation(hoverTimeSeriesSelection.getEndScreenLocation() + deltaX);

                // clamp start location to plot left
                if (hoverTimeSeriesSelection.getStartScreenLocation() < plotLeft) {
                    deltaX = plotLeft - hoverTimeSeriesSelection.getStartScreenLocation();
                    hoverTimeSeriesSelection.setStartScreenLocation(plotLeft);
                    hoverTimeSeriesSelection.setEndScreenLocation(hoverTimeSeriesSelection.getEndScreenLocation() + deltaX);
                }

                // clamp end location to plot right
                if (hoverTimeSeriesSelection.getEndScreenLocation() > plotRight) {
                    deltaX = hoverTimeSeriesSelection.getEndScreenLocation() - plotRight;
                    hoverTimeSeriesSelection.setStartScreenLocation(hoverTimeSeriesSelection.getStartScreenLocation() - deltaX);
                    hoverTimeSeriesSelection.setEndScreenLocation(plotRight);
                }

                double deltaTime = (double)(hoverTimeSeriesSelection.getStartScreenLocation()-plotLeft) / plotChronoUnitWidth;
                Instant selectionStartInstant = startInstant.plus((long)deltaTime, chronoUnit);
                deltaTime = (double)(hoverTimeSeriesSelection.getEndScreenLocation()-plotLeft) / plotChronoUnitWidth;
                Instant selectionEndInstant = startInstant.plus((long)deltaTime, chronoUnit);

                hoverTimeSeriesSelection.setStartInstant(selectionStartInstant);
                hoverTimeSeriesSelection.setEndInstant(selectionEndInstant);
            } else {
                endDragPoint.setLocation(e.getPoint());

                int startLocation = startDragPoint.x < endDragPoint.x ? startDragPoint.x : endDragPoint.x;
//				startLocation -= (dragHandleWidth+margins.left);
                // clamp start position to plot right
                if (startLocation < plotLeft) {
                    startLocation = plotLeft;
                }

                int endLocation = startDragPoint.x > endDragPoint.x ? startDragPoint.x : endDragPoint.x;
//				endLocation -= (dragHandleWidth+margins.left);
                // clamp end position to plot left
                if (endLocation > plotRight) {
                    endLocation = plotRight;
                }

                double deltaTime = (double)(startLocation-plotLeft) / plotChronoUnitWidth;
                Instant selectionStartInstant = startInstant.plus((long)deltaTime, chronoUnit);
                deltaTime = (double)(endLocation-plotLeft) / plotChronoUnitWidth;
                Instant selectionEndInstant = startInstant.plus((long)deltaTime, chronoUnit);

                if (draggingTimeSeriesSelection == null) {
                    draggingTimeSeriesSelection = new TimeSeriesSelection(selectionStartInstant,
                            selectionEndInstant, startLocation, endLocation);
                    TimeSeries timeSeries = timeSeriesList.get(hoverTimeSeriesIndex);
                    ArrayList<TimeSeriesSelection> selections = timeSeriesSelectionMap.get(timeSeries);
                    if (selections == null) {
                        selections = new ArrayList<TimeSeriesSelection>();
                        timeSeriesSelectionMap.put(timeSeries, selections);
                    }
                    selections.add(draggingTimeSeriesSelection);
                } else {
                    draggingTimeSeriesSelection.setStartInstant(selectionStartInstant);
                    draggingTimeSeriesSelection.setEndInstant(selectionEndInstant);
                }

                draggingTimeSeriesSelection.setStartScreenLocation(startLocation);
                draggingTimeSeriesSelection.setEndScreenLocation(endLocation);
            }
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int translatedY = e.getY() - (margins.top + timeInfoBarHeight);
        int timeSeriesIndex = translatedY / (plotHeight + plotInfoBarHeight + plotNameBarHeight);
        if (translatedY < 0) {
            timeSeriesIndex = -1;
        } else if (timeSeriesIndex >= timeSeriesList.size()) {
            timeSeriesIndex = -1;
        }

        hoverInstant = null;
        hoverDragHandleRectangle = null;
        hoverTimeSeriesSelection = null;
        hoverRemoveButtonRectangle = null;

        if (timeSeriesIndex != -1) {
            hoverTimeSeriesIndex = timeSeriesIndex;

            if (e.getX() >= plotLeft && e.getX() <= plotRight) {
                hoverX = e.getX();
                double deltaTime = (double)(hoverX-plotLeft) / plotChronoUnitWidth;
                hoverInstant = startInstant.plus((long)deltaTime, chronoUnit);

                Instant rangeStartInstant = hoverInstant.truncatedTo(chronoUnit).minus(1, chronoUnit);
                Instant rangeEndInstant = hoverInstant.truncatedTo(chronoUnit).plus(1, chronoUnit);

                hoverTimeSeriesValues.clear();
                for (int i = 0; i < timeSeriesList.size(); i++) {
                    TimeSeries timeSeries = timeSeriesList.get(i);

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
                            hoverTimeSeriesValues.add(nearestRecord.value);
                        } else {
                            hoverTimeSeriesValues.add(Double.NaN);
                        }
                    } else {
                        hoverTimeSeriesValues.add(Double.NaN);
                    }

//                    TimeSeriesRecord record = timeSeries.recordAt(hoverInstant);
//                    if (record != null) {
//                        hoverTimeSeriesValues.add(record.value);
//                    } else {
//                        hoverTimeSeriesValues.add(Double.NaN);
//                    }
                }

//                log.debug("hoverTimeSeriesValues.size() " + hoverTimeSeriesValues.size() + " timeSeriesList.size() " + timeSeriesList.size());

                ArrayList<TimeSeriesSelection> timeSeriesSelections = timeSeriesSelectionMap.get(timeSeriesList.get(timeSeriesIndex));
                if (timeSeriesSelections != null && !timeSeriesSelections.isEmpty()) {
                    for (TimeSeriesSelection selection : timeSeriesSelections) {
//						int translatedX = e.getX() - (margins.left + dragHandleWidth);
                        int translatedX = e.getX() - margins.left;
                        if (translatedX >= selection.getStartScreenLocation() &&
                                translatedX <= selection.getEndScreenLocation()) {
                            hoverTimeSeriesSelection = selection;
                            break;
                        }
                    }
                }
            } else {
                if (hoverTimeSeriesIndex != -1) {
                    Rectangle dragHandleRect = dragHandleRectangles.get(hoverTimeSeriesIndex);
                    if (dragHandleRect.contains(e.getPoint())) {
                        hoverDragHandleRectangle = dragHandleRect;
                    }
                    Rectangle removeButtonRect = removeButtonRectangles.get(hoverTimeSeriesIndex);
                    if (removeButtonRect.contains(e.getPoint())) {
                        hoverRemoveButtonRectangle = removeButtonRect;
                    }
                }
            }
        }

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startDragPoint.setLocation(e.getX(), e.getY());
        endDragPoint.setLocation(e.getX(), e.getY());

        if (hoverDragHandleRectangle != null) {
            draggingYOffset = startDragPoint.y - hoverDragHandleRectangle.y;
        }

//        else if (e.isControlDown() && hoverTimeSeriesIndex != -1) {
//            log.debug("Translating " + timeSeriesList.get(hoverTimeSeriesIndex).getName() + " time series.");
//            translatingTimeSeries = true;
//        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggingSelection) {
            draggingSelection = false;
            draggingTimeSeriesSelection = null;
        } else if (translatingTimeSeries) {
            translatingTimeSeries = false;
        } else if (draggingTimeSeries) {
            int originalIndex = dragHandleRectangles.indexOf(hoverDragHandleRectangle);
            int newIndex = (e.getY() - (margins.top + timeInfoBarHeight)) / (plotHeight + plotInfoBarHeight);
            if (newIndex < 0) {
                newIndex = 0;
            } else if (newIndex > timeSeriesList.size()-1) {
                newIndex = timeSeriesList.size()-1;
            }

            if (newIndex != originalIndex) {
                TimeSeries timeSeries = timeSeriesList.remove(originalIndex);
                timeSeriesList.add(newIndex, timeSeries);
            }

            draggingTimeSeries = false;
            hoverDragHandleRectangle = null;
            repaint();
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (!timeSeriesList.isEmpty()) {
            // calculate the start and end instants for the visible clip region
            Rectangle clipBounds = g2.getClipBounds();
            double deltaTime = (double)(clipBounds.x-plotLeft) / plotChronoUnitWidth;
            Instant clipStartInstant = startInstant.plus((long)deltaTime, chronoUnit);
//			log.debug("deltaTime is " + deltaTime);

            deltaTime = (double)((clipBounds.x+clipBounds.width)-plotLeft) / plotChronoUnitWidth;
            Instant clipEndInstant = startInstant.plus((long)deltaTime, chronoUnit);
//			log.debug("deltaTime is " + deltaTime);

            g2.setFont(g2.getFont().deriveFont(12.f));

//            timeSeriesNameRectangles.clear();

            for (int i = 0; i < timeSeriesList.size(); i++) {
                if (i == hoverTimeSeriesIndex && this.draggingTimeSeries) {
                    continue;
                }

                Rectangle timeSeriesRect = timeSeriesRectangles.get(i);
                if (!g2.hitClip(timeSeriesRect.x, timeSeriesRect.y, timeSeriesRect.width, timeSeriesRect.height)) {
                    continue;
                }

//                g2.setColor(Color.blue);
                g2.setColor(gridLineColor);
                g2.draw(timeSeriesPlotInfoList.get(i).plotRectangle);

                TimeSeries timeSeries = timeSeriesList.get(i);
                double hoverValue = Double.NaN;
                if (!hoverTimeSeriesValues.isEmpty()) {
                    hoverValue = hoverTimeSeriesValues.get(i);
                }

                g2.translate(0, timeSeriesRect.y);

                boolean highlightDragHandle = false;
                if (hoverDragHandleRectangle != null) {
                    if (this.dragHandleRectangles.get(i) == hoverDragHandleRectangle) {
                        highlightDragHandle = true;
                    }
                }

                boolean highlightRemoveButton = false;
                if (hoverRemoveButtonRectangle != null) {
                    if (this.removeButtonRectangles.get(i) == hoverRemoveButtonRectangle) {
                        highlightRemoveButton = true;
                    }
                }



                drawTimeSeries(g2, timeSeries, hoverValue, highlightDragHandle, highlightRemoveButton,
                        clipStartInstant, clipEndInstant, timeSeriesPlotInfoList.get(i));

                g2.setStroke(new BasicStroke(1.f));
//                g2.translate(-translateFactor, 0);
                g2.translate(0, -timeSeriesRect.y);

//                g2.draw(dragHandleRectangles.get(i));
            }

            if (draggingTimeSeries) {
                TimeSeries timeSeries = timeSeriesList.get(hoverTimeSeriesIndex);
                g2.translate(0, endDragPoint.y - draggingYOffset);
                drawTimeSeries(g2, timeSeries, Double.NaN, false, false,
                        clipStartInstant, clipEndInstant,
                        timeSeriesPlotInfoList.get(hoverTimeSeriesIndex));
//                drawTimeSeries(g2, timeSeries, Double.NaN, false, false, clipStartInstant, clipEndInstant);
                g2.translate(0, -(endDragPoint.y - draggingYOffset));
            }

            drawTimeInfoBar(g2);

            if (hoverX != -1) {
                g2.setColor(hoverLineColor);
                g2.drawLine(hoverX, 0, hoverX, getHeight());
                g2.setFont(g2.getFont().deriveFont(9.f));
            }
        }
    }

    public void setDateTimeRange (Instant startDT, Instant endDT, ChronoUnit chronoUnit) {
        this.startInstant = startDT;
        this.endInstant = endDT;
        this.chronoUnit = chronoUnit;
        layoutPanel();
    }
}

