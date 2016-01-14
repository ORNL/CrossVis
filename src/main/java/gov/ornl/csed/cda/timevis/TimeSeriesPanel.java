package gov.ornl.csed.cda.timevis;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableMap;
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
    private int timeInfoBarHeight = 14;
    private int plotValueBarHeight = 14;

    private int plotUnitWidth = 1;
    private int numPlotUnits = 0;
    private int plotUnitDurationMillis = 0;

    private DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private int hoverX;
    private Instant hoverInstant;
    private double hoverValue = Double.NaN;

    private boolean shrinkToFit = false;
    private PlotDisplayOption plotDisplayOption = PlotDisplayOption.POINT;

    private TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap = new TreeMap<>();
    private ArrayList<TimeSeriesSummaryInfo> summaryInfoList = new ArrayList<>();

    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    private Color dataColor = new Color(80, 80, 130, 180);
    private Color rangeColor = new Color(140, 140, 160, 100);

    private Color selectedRegionFillColor = Color.white;

    private Instant startHighlightInstant;
    private Instant endHighlightInstant;
    private Rectangle highlightRectangle;


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

    // shrink to fit constructor
    public TimeSeriesPanel (int plotUnitWidth, PlotDisplayOption plotDisplayOption) {
        this.plotUnitWidth = plotUnitWidth;
        this.shrinkToFit = true;
        this.plotDisplayOption = plotDisplayOption;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public TimeSeries getTimeSeries() {
        return timeSeries;
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



    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
//                    int numTimeRecords = 50400;
                    int numTimeRecords = 12000;
//                    int numTimeRecords = 1200;
                    int plotUnitWidth = 4;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    TimeSeriesPanel detailsTimeSeriesPanel = new TimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, PlotDisplayOption.STEPPED_LINE);
                    detailsTimeSeriesPanel.setBackground(Color.white);

                    TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(plotUnitWidth, PlotDisplayOption.STEPPED_LINE);
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

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
//                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords, ChronoUnit.SECONDS);

                    double value = 0.;

                    for (int i = 0; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
                        value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
                        double range = Math.abs(value) * .25;
                        double upperRange = value + range;
                        double lowerRange = value - range;
                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
                    }

                    overviewTimeSeriesPanel.setTimeSeries(timeSeries);
                    detailsTimeSeriesPanel.setTimeSeries(timeSeries);

                    scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            JScrollBar scrollBar = (JScrollBar)e.getSource();
                            double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
                            double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                            double deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                            Instant startHighlightInstant = timeSeries.getStartInstant().plusMillis((long)deltaTime);
                            int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                            norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                            deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                            Instant endHighlightInstant = timeSeries.getEndInstant().minusMillis((long) deltaTime);
                            overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoverInstant = null;
        hoverX = -1;
        hoverValue = Double.NaN;

        if ( (plotRectangle != null) && (plotRectangle.contains(e.getPoint())) ) {
            hoverX = e.getX();

            double norm = (double)(hoverX - plotRectangle.x) / (plotRectangle.width);
            long deltaTimeMillis = (long) (norm * totalDuration.toMillis());
            hoverInstant = startInstant.plusMillis(deltaTimeMillis);

            if (shrinkToFit) {
                int summaryInfoIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
                TimeSeriesSummaryInfo summaryInfo = summaryInfoList.get(summaryInfoIndex);
                hoverValue = summaryInfo.meanValue;
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

    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
        startInstant = timeSeries.getStartInstant();
        endInstant = timeSeries.getEndInstant();

        totalDuration = Duration.between(startInstant, endInstant);
        layoutPanel();
    }

    public void setPlotDisplayOption (PlotDisplayOption plotDisplayOption) {
        this.plotDisplayOption = plotDisplayOption;
        repaint();
    }

    public PlotDisplayOption getPlotDisplayOption () {
        return plotDisplayOption;
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
            long deltaTimeMillis = Duration.between(startInstant, start).toMillis();
            int summaryUnitIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
            int highlightLeft = plotRectangle.x + (summaryUnitIndex * plotUnitWidth);
            deltaTimeMillis = Duration.between(startInstant, end).toMillis();
            summaryUnitIndex = (int) (deltaTimeMillis / plotUnitDurationMillis);
            int highlightRight = plotRectangle.x + (summaryUnitIndex * plotUnitWidth);
            highlightRectangle = new Rectangle(highlightLeft, plotRectangle.y, highlightRight-highlightLeft, plotRectangle.height);
        } else {

        }

        repaint();
    }

    public void layoutPanel() {
        if (timeSeries != null) {
            int plotLeft = getInsets().left;
            int plotTop = getInsets().top + timeInfoBarHeight;
            int plotBottom = getHeight() - (getInsets().bottom + plotValueBarHeight);
            timeInfoBarTop = getInsets().top;
            timeInfoBarBottom = timeInfoBarTop + timeInfoBarHeight;
            valueInfoBarTop = plotBottom;
            valueInfoBarBottom = valueInfoBarTop + plotValueBarHeight;

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

            repaint();
        }
    }

    private void calculatePlotPoints() {
        if (shrinkToFit) {
//            summaryInfoList.clear();
//            for (int i = 0; i < numPlotUnits; i++) {
//                // determine the start and end time instants for the current time unit
//                Instant unitStartInstant = startInstant.plusMillis(i * plotUnitDurationMillis);
//                Instant unitEndInstant = unitStartInstant.plusMillis(plotUnitDurationMillis);
//
//                // get values between start (inclusive) and end time instants (exclusive)
//                ArrayList<TimeSeriesRecord> records = timeSeries.getRecordsBetween(unitStartInstant, unitEndInstant);
//                if (records != null && !records.isEmpty()) {
//                    // calculate mean value for records in plot time unit
//                    SummaryStatistics stats = new SummaryStatistics();
//
//                    for (TimeSeriesRecord record : records) {
//                        stats.addValue(record.value);
//                    }
//
//                    int x = i * plotUnitWidth;
//                    double norm = (stats.getMean() - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
//                    double yOffset = norm * (plotRectangle.getHeight());
//                    double y = (plotRectangle.getHeight() - yOffset);
//
//                    Point2D.Double meanPoint = new Point2D.Double(x, y);
//                    TimeSeriesSummaryInfo summaryInfo = new TimeSeriesSummaryInfo();
//                    summaryInfo.instant = unitStartInstant;
//                    summaryInfo.meanValue = stats.getMean();
//                    summaryInfo.maxValue = stats.getMax();
//                    summaryInfo.minValue = stats.getMin();
//                    summaryInfo.meanPoint = meanPoint;
//                    summaryInfoList.add(summaryInfo);
//                }
//            }
            summaryInfoList = TimeSeriesRenderer.calculateOverviewPlotPoints(timeSeries, plotRectangle, numPlotUnits,
                    plotUnitWidth, plotUnitDurationMillis, startInstant);
        } else {
            plotPointMap = TimeSeriesRenderer.calculateDetailedPlotPoints(timeSeries, plotChronoUnit, plotUnitWidth,
                    plotRectangle, startInstant);
//            plotPointMap.clear();
//            int numPointsCalculated = 0;
//            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
//            if (records != null) {
//                for (TimeSeriesRecord record : records) {
//                    long deltaTime = plotChronoUnit.between(startInstant, record.instant);
//                    int x = (int)deltaTime * plotUnitWidth;
//
//                    double norm = (record.value - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
//                    double yOffset = norm * (plotRectangle.height);
//                    double y = plotRectangle.height - yOffset;
//
//                    Point2D.Double point = new Point2D.Double(x, y);
//
//                    ArrayList<Point2D.Double> instantPoints = plotPointMap.get(record.instant);
//                    if (instantPoints == null) {
//                        instantPoints = new ArrayList<>();
//                        plotPointMap.put(record.instant, instantPoints);
//                    }
//                    instantPoints.add(point);
//                    numPointsCalculated++;
//                }
//            }
        }
    }

    private void drawTimeSeries(Graphics2D g2, Instant startClipInstant, Instant endClipInstant) {
        // debugging drawing
        g2.setColor(gridLineColor);
        g2.draw(plotRectangle);

        g2.translate(plotRectangle.x, plotRectangle.y);

        if (shrinkToFit) {
            TimeSeriesRenderer.renderAsOverview(g2, timeSeries, plotRectangle.width, plotRectangle.height,
                    plotUnitWidth, plotChronoUnit, plotDisplayOption, gridLineColor, dataColor, dataColor,
                    rangeColor, summaryInfoList);
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

            String instantString = dtFormatter.format(plotLeftInstant);
            int strWidth = g2.getFontMetrics().stringWidth(instantString);
            if (hoverInstant == null || (hoverInstantStringLeft > (plotRectangle.x+strWidth))) {
                g2.drawString(instantString, plotRectangle.x, strY);
            }

            instantString = dtFormatter.format(plotRightInstant);
            strWidth = g2.getFontMetrics().stringWidth(instantString);
            if (hoverInstant == null || (hoverInstantStringRight < ((plotRectangle.x+plotRectangle.width) - strWidth))) {
                g2.drawString(instantString, (plotRectangle.x+plotRectangle.width)-strWidth, strY);
            }

            if (!Double.isNaN(hoverValue)) {
                g2.setColor(Color.black);
                String strValue = String.valueOf(hoverValue);
                strWidth = g2.getFontMetrics().stringWidth(strValue);
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

            // draw left date time string
            String instantString = dtFormatter.format(leftInstant);
            int strWidth = g2.getFontMetrics().stringWidth(instantString);
            if (hoverInstant == null || (hoverInstantStringLeft > (infoBarLeft+strWidth))) {
                g2.drawString(instantString, infoBarLeft, strY);
            }

            // draw right date time string
            instantString = dtFormatter.format(rightInstant);
            strWidth = g2.getFontMetrics().stringWidth(instantString);
            if (hoverInstant == null || (hoverInstantStringRight < (infoBarRight - strWidth))) {
                g2.drawString(instantString, infoBarRight-strWidth, strY);
            }

            if (!Double.isNaN(hoverValue)) {
                g2.setColor(Color.black);
                String strValue = String.valueOf(hoverValue);
                strWidth = g2.getFontMetrics().stringWidth(strValue);
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

            if (hoverInstant != null) {
                g2.setColor(hoverLineColor);
                g2.drawLine(hoverX, 0, hoverX, getHeight());
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
