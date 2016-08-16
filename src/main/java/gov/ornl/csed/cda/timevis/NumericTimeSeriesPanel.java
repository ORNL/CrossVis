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
public class NumericTimeSeriesPanel extends TimeSeriesPanel {
    private final static Logger log = LoggerFactory.getLogger(NumericTimeSeriesPanel.class);

    // Default colors
    public static final Color DEFAULT_POINT_COLOR = new Color(80, 80, 130, 180);
    public static final Color DEFAULT_LINE_COLOR = DEFAULT_POINT_COLOR.brighter();
    public static final Color DEFAULT_STANDARD_DEVIATION_RANGE_COLOR = new Color(140, 140, 160, 100);
    public static final Color DEFAULT_MINMAX_RANGE_COLOR = DEFAULT_STANDARD_DEVIATION_RANGE_COLOR.brighter();
    public static final Color DEFAULT_SPECTRUM_NEGATIVE_COLOR = Color.red;
    public static final Color DEFAULT_SPECTRUM_POSITIVE_COLOR = Color.blue;

//    private ConcurrentSkipListMap<Instant, ArrayList<Point2D.Double>> plotPointMap = new ConcurrentSkipListMap<>();

    // value axis range
    private double valueAxisMax;
    private double valueAxisMin;

    // moving range mode variables
    protected MovingRangeDisplayOption movingRangeDisplayOption = MovingRangeDisplayOption.NOT_SHOWN;
    private TimeSeries movingRangeTimeSeries;
    private int minMovingRangeAlpha = 80;

    // moving range hover variables
    private TimeSeriesRecord hoverMovingRangeRecord = null;
    private TimeSeriesBin hoverMovingRangeBin = null;

    private Color pointColor = DEFAULT_POINT_COLOR;
    private Color lineColor = DEFAULT_LINE_COLOR;
    private Color standardDeviationRangeColor = DEFAULT_STANDARD_DEVIATION_RANGE_COLOR;
    private Color minmaxRangeColor = DEFAULT_MINMAX_RANGE_COLOR;
    private Color spectrumNegativeColor = DEFAULT_SPECTRUM_NEGATIVE_COLOR;
    private Color spectrumPositiveColor = DEFAULT_SPECTRUM_POSITIVE_COLOR;


    // fixed plot width constructor constructor
    public NumericTimeSeriesPanel(int plotUnitWidth, ChronoUnit plotChronoUnit, PlotDisplayOption plotDisplayOption) {
        super(plotUnitWidth, plotChronoUnit);
        this.plotDisplayOption = plotDisplayOption;
    }


    // fit to panel width constructor
    public NumericTimeSeriesPanel(int plotUnitWidth, PlotDisplayOption plotDisplayOption) {
        super(plotUnitWidth);
        this.plotDisplayOption = plotDisplayOption;
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


    public Color getPointColor() {
        return pointColor;
    }


    public void setPointColor(Color pointColor) {
        if (this.pointColor != pointColor) {
            this.pointColor = pointColor;
            calculatePlotPoints();
            repaint();
        }
    }


    public void setSpectrumPositiveColor (Color spectrumPositiveColor) {
        if (this.spectrumPositiveColor != spectrumPositiveColor) {
            this.spectrumPositiveColor = spectrumPositiveColor;
            calculatePlotPoints();
            repaint();
        }
    }


    public Color getSpectrumPositiveColor() {
        return spectrumPositiveColor;
    }


    public void setSpectrumNegativeColor (Color spectrumNegativeColor) {
        if (this.spectrumNegativeColor != spectrumNegativeColor) {
            this.spectrumNegativeColor = spectrumNegativeColor;
            calculatePlotPoints();
            repaint();
        }
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

    @Override
    protected void calculatePlotPoints(BinnedTimeSeries binnedTimeSeries, NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> pointMap) {
        Collection<TimeSeriesBin> bins = binnedTimeSeries.getAllBins();
        for (TimeSeriesBin bin : bins) {
            TimeSeriesPlotPointRecord pointRecord = calculatePlotPointRecord(bin);
            ArrayList<TimeSeriesPlotPointRecord> instantPointRecords = pointMap.get(bin.getInstant());
            if (instantPointRecords == null) {
                instantPointRecords = new ArrayList<>();
                pointMap.put(bin.getInstant(), instantPointRecords);
            }
            instantPointRecords.add(pointRecord);
        }
    }


    @Override
    protected void calculatePlotPoints(TimeSeries timeSeries, NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> pointMap) {
        for (ArrayList<TimeSeriesRecord> timeSeriesRecordList : timeSeries.getRecordMap().values()) {
            ArrayList<TimeSeriesRecord> movingRangeRecordList = null;
            if (movingRangeTimeSeries != null) {
                movingRangeRecordList = movingRangeTimeSeries.getRecordMap().get(timeSeriesRecordList.get(0).instant);
            }

            for (int i = 0; i < timeSeriesRecordList.size(); i++) {
                TimeSeriesPlotPointRecord pointRecord = null;
                if (movingRangeRecordList != null) {
                    pointRecord = calculatePlotPointRecord(timeSeriesRecordList.get(i), movingRangeRecordList.get(i));
                } else {
                    pointRecord = calculatePlotPointRecord(timeSeriesRecordList.get(i), null);
                }

                ArrayList<TimeSeriesPlotPointRecord> instantPointRecords = pointMap.get(pointRecord.valueRecord.instant);
                if (instantPointRecords == null) {
                    instantPointRecords = new ArrayList<>();
                    pointMap.put(pointRecord.valueRecord.instant, instantPointRecords);
                }
                instantPointRecords.add(pointRecord);
            }
        }
    }


    protected TimeSeriesPlotPointRecord calculatePlotPointRecord (TimeSeriesBin bin) {
        TimeSeriesPlotPointRecord plotPointRecord = new TimeSeriesPlotPointRecord();
        plotPointRecord.bin = bin;

        // calculate x position
        plotPointRecord.x = GraphicsUtil.mapValue(Duration.between(getStartInstant(), bin.getInstant()).toMillis(),
                0, Duration.between(getStartInstant(), getEndInstant()).toMillis(), 0, getPlotRectangle().width);

        // calculate mean y
        plotPointRecord.meanY = GraphicsUtil.mapValue(bin.getStatistics().getMean(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(), getPlotRectangle().height, 0.);

        // calculate standard deviation upper range y
        plotPointRecord.upperStdevRangeY = GraphicsUtil.mapValue(bin.getStatistics().getMean() + bin.getStatistics().getStandardDeviation(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(), getPlotRectangle().height, 0.);

        // calculate standard deviation lower range y
        plotPointRecord.lowerStdevRangeY = GraphicsUtil.mapValue(bin.getStatistics().getMean() - bin.getStatistics().getStandardDeviation(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(), getPlotRectangle().height, 0.);

        // calculate max y
        plotPointRecord.maxY = GraphicsUtil.mapValue(bin.getStatistics().getMax(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(), getPlotRectangle().height, 0.);

        // calculate min y
        plotPointRecord.minY = GraphicsUtil.mapValue(bin.getStatistics().getMin(),
                binnedTimeSeries.getMinRangeValue(), binnedTimeSeries.getMaxRangeValue(), getPlotRectangle().height, 0.);

        return plotPointRecord;
    }


    protected TimeSeriesPlotPointRecord calculatePlotPointRecord (TimeSeriesRecord valueRecord, TimeSeriesRecord movingRangeRecord) {
        TimeSeriesPlotPointRecord plotPointRecord = new TimeSeriesPlotPointRecord();
        plotPointRecord.valueRecord = valueRecord;
        plotPointRecord.movingRangeRecord = movingRangeRecord;

        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(getStartInstant(), getEndInstant());
        long deltaTime = ChronoUnit.MILLIS.between(getStartInstant(), valueRecord.instant);
        double normTime = (double)deltaTime / totalPlotDeltaTime;
        plotPointRecord.x = ((double)getPlotRectangle().width * normTime);

        if (plotDisplayOption == PlotDisplayOption.SPECTRUM) {
            if (movingRangeDisplayOption == MovingRangeDisplayOption.NOT_SHOWN) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(valueRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., getPlotRectangle().height / 2.);
                plotPointRecord.spectrumTopY = (getPlotRectangle().height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (getPlotRectangle().height / 2.) + lineHeightHalf;

                if (valueRecord.value < 0.) {
                    plotPointRecord.color = spectrumNegativeColor;
                } else {
                    plotPointRecord.color = spectrumPositiveColor;
                }
            } else if (movingRangeDisplayOption == MovingRangeDisplayOption.OPACITY) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(valueRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., getPlotRectangle().height / 2.);
                plotPointRecord.spectrumTopY = (getPlotRectangle().height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (getPlotRectangle().height / 2.) + lineHeightHalf;

                int alpha = 255;
                if (movingRangeRecord != null) {
                    alpha = (int) Math.round(GraphicsUtil.mapValue(movingRangeRecord.value, 0., movingRangeTimeSeries.getMaxValue(), minMovingRangeAlpha, 255.));
                }
                plotPointRecord.color = new Color(pointColor.getRed(), pointColor.getGreen(), pointColor.getBlue(), alpha);
//                int alpha = (int) Math.round(GraphicsUtil.mapValue(movingRangeRecord.value, 0., movingRangeTimeSeries.getMaxValue(), minMovingRangeAlpha, 255.));

                if (valueRecord.value < 0.) {
                    plotPointRecord.color = new Color(spectrumNegativeColor.getRed(), spectrumNegativeColor.getGreen(),
                            spectrumNegativeColor.getBlue(), alpha);
                } else {
                    plotPointRecord.color = new Color(spectrumPositiveColor.getRed(), spectrumPositiveColor.getGreen(),
                            spectrumPositiveColor.getBlue(), alpha);
                }
            } else if ((movingRangeDisplayOption == MovingRangeDisplayOption.PLOT_VALUE) && (movingRangeRecord != null)) {
                double lineHeightHalf = GraphicsUtil.mapValue(Math.abs(movingRangeRecord.value), 0., Math.max(valueAxisMax,
                        Math.abs(valueAxisMin)), 0., getPlotRectangle().getHeight() / 2.);
                plotPointRecord.spectrumTopY = (getPlotRectangle().height / 2.) - lineHeightHalf;
                plotPointRecord.spectrumBottomY = (getPlotRectangle().height / 2.) + lineHeightHalf;

                plotPointRecord.color = pointColor;
            }
        } else {
            if (movingRangeDisplayOption == MovingRangeDisplayOption.NOT_SHOWN) {
                double norm = (valueRecord.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
                double yOffset = norm * (getPlotRectangle().height);
                plotPointRecord.valueY = getPlotRectangle().height - yOffset;
                plotPointRecord.color = pointColor;
            } else if (movingRangeDisplayOption == MovingRangeDisplayOption.OPACITY) {
                double norm = (valueRecord.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
                double yOffset = norm * (getPlotRectangle().height);
                plotPointRecord.valueY = getPlotRectangle().height - yOffset;

                int alpha = 255;
                if (movingRangeRecord != null) {
                    alpha = (int) Math.round(GraphicsUtil.mapValue(movingRangeRecord.value, 0., movingRangeTimeSeries.getMaxValue(), minMovingRangeAlpha, 255.));
                }
                plotPointRecord.color = new Color(pointColor.getRed(), pointColor.getGreen(), pointColor.getBlue(), alpha);

            } else if ((movingRangeDisplayOption == MovingRangeDisplayOption.PLOT_VALUE) && (movingRangeRecord != null)) {
                double norm = (movingRangeRecord.value - valueAxisMin) / (valueAxisMax - valueAxisMin);
                double yOffset = norm * (getPlotRectangle().height);
                plotPointRecord.valueY = getPlotRectangle().height - yOffset;
                plotPointRecord.color = pointColor;
            }
        }

        return plotPointRecord;
    }


    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);

        hoverMovingRangeBin = null;
        hoverMovingRangeRecord = null;

        if (shrinkToFit) {
        } else {
            // TODO: Indicate here if more than one record is nearest
            if (movingRangeDisplayOption != NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN) {
                ArrayList<TimeSeriesRecord> nearMRRecords = movingRangeTimeSeries.getNearestRecordsFor(getHoverInstant(), Duration.of(2, plotChronoUnit));
                if (nearMRRecords != null && !nearMRRecords.isEmpty()) {
                    hoverMovingRangeRecord = nearMRRecords.get(0);
                }
            }
        }
    }


    @Override
    protected String getInfoBarString() {
        String infoBarString = super.getInfoBarString();
        if (infoBarString == null) {
            infoBarString = "";
        }

        if (shrinkToFit) {
            if (hoverMovingRangeBin != null) {
                return infoBarString + ", MR: " + String.valueOf(hoverMovingRangeBin.getStatistics().getMean());
            }
        } else {
            if (hoverMovingRangeRecord != null) {
                return infoBarString + ", MR: " + String.valueOf(hoverMovingRangeRecord.value);
            }
        }

        if (!infoBarString.isEmpty()) {
            return infoBarString;
        }

        return null;
    }

        protected void drawTimeSeries(Graphics2D g2, Instant startClipInstant, Instant endClipInstant) {
        g2.translate(getPlotRectangle().x, getPlotRectangle().y);

        if (shrinkToFit) {
            if (!getPointRecordMap().isEmpty()) {
                g2.setColor(getGridLineColor());
                drawZeroLine(g2, getPlotRectangle().width, getPlotRectangle().height, valueAxisMin, valueAxisMax);

                Path2D.Double maxPath = null;
                Path2D.Double minPath = null;
                Path2D.Double upperStDevRangePath = null;
                Path2D.Double lowerStDevRangePath = null;
                Path2D.Double meanPath = null;

                g2.setColor(lineColor);
                TimeSeriesPlotPointRecord lastPlotPointRecord = null;
                for (ArrayList<TimeSeriesPlotPointRecord> instantRecords : getPointRecordMap().values()) {
                    for (TimeSeriesPlotPointRecord plotPointRecord : instantRecords) {
                        if (plotDisplayOption == PlotDisplayOption.POINT) {
                            g2.setColor(pointColor);
                            Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1, plotPointRecord.meanY - 1, 2., 2.);
                            g2.draw(ellipse);
//                            Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x, plotPointRecord.meanY,
//                                    plotUnitWidth, plotUnitWidth);
//                            g2.draw(ellipse);
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
            if (getPointRecordMap() != null && !getPointRecordMap().isEmpty()) {
                g2.setColor(getGridLineColor());
                drawZeroLine(g2, getPlotRectangle().width, getPlotRectangle().height, valueAxisMin, valueAxisMax);

                Instant start = getPointRecordMap().firstKey();
                if (startClipInstant.isAfter(start)) {
                    start = getPointRecordMap().lowerKey(startClipInstant);
                }
                Instant end = getPointRecordMap().lastKey();
                if (endClipInstant.isBefore(end)) {
                    end = getPointRecordMap().higherKey(endClipInstant);
                }
                NavigableMap<Instant, ArrayList<TimeSeriesPlotPointRecord>> clipMap = getPointRecordMap().subMap(start, true, end, true);
                if (clipMap.isEmpty()) {
                    log.debug("No records in clip range - nothing to draw");
                } else {
                    TimeSeriesPlotPointRecord lastDrawnPointRecord = null;
                    int numPointsDrawn = 0;
                    for (ArrayList<TimeSeriesPlotPointRecord> instantRecords : clipMap.values()) {
                        for (TimeSeriesPlotPointRecord plotPointRecord : instantRecords) {
                            if (plotDisplayOption == NumericTimeSeriesPanel.PlotDisplayOption.POINT) {
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1,
                                        plotPointRecord.valueY - 1, 2., 2.);
                                g2.setColor(plotPointRecord.color);
                                g2.draw(ellipse);
//                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - plotUnitWidth / 2.,
//                                        plotPointRecord.valueY - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
//                                g2.setColor(plotPointRecord.color);
//                                g2.draw(ellipse);
                            } else if (plotDisplayOption == NumericTimeSeriesPanel.PlotDisplayOption.LINE) {
                                if (lastDrawnPointRecord != null) {
                                    Line2D.Double line = new Line2D.Double(lastDrawnPointRecord.x,
                                            lastDrawnPointRecord.valueY, plotPointRecord.x, plotPointRecord.valueY);
                                    g2.setColor(lineColor);
                                    g2.draw(line);
                                }
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1,
                                        plotPointRecord.valueY - 1, 2., 2.);
                                g2.setColor(plotPointRecord.color);
                                g2.draw(ellipse);
                            } else if (plotDisplayOption == NumericTimeSeriesPanel.PlotDisplayOption.STEPPED_LINE) {
                                if (lastDrawnPointRecord != null) {
                                    Line2D.Double line1 = new Line2D.Double(lastDrawnPointRecord.x, lastDrawnPointRecord.valueY, plotPointRecord.x, lastDrawnPointRecord.valueY);
                                    Line2D.Double line2 = new Line2D.Double(plotPointRecord.x, lastDrawnPointRecord.valueY, plotPointRecord.x, plotPointRecord.valueY);
                                    g2.setColor(lineColor);
                                    g2.draw(line1);
                                    g2.draw(line2);
                                }
                                Ellipse2D.Double ellipse = new Ellipse2D.Double(plotPointRecord.x - 1, plotPointRecord.valueY - 1, 2., 2.);
                                g2.setColor(plotPointRecord.color);
                                g2.draw(ellipse);
                            } else if (plotDisplayOption == NumericTimeSeriesPanel.PlotDisplayOption.SPECTRUM) {
                                g2.setColor(plotPointRecord.color);
                                Line2D.Double line = new Line2D.Double(plotPointRecord.x, plotPointRecord.spectrumTopY, plotPointRecord.x, plotPointRecord.spectrumBottomY);
                                g2.draw(line);
                            }
                            lastDrawnPointRecord = plotPointRecord;
                            numPointsDrawn++;
                        }
                    }
                }
            }
        }
        g2.translate(-getPlotRectangle().x, -getPlotRectangle().y);
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


    @Override
    public void setTimeSeries(TimeSeries timeSeries, Instant startInstant, Instant endInstant) {
        // Apply a small buffer to expand the min / max value range to prevent extreme points
        // from being drawn on the plot boundaries
        double buffer = 0.05 * (timeSeries.getMaxValue() - timeSeries.getMinValue());
        valueAxisMin = timeSeries.getMinValue() - buffer;
        valueAxisMax = timeSeries.getMaxValue() + buffer;

        super.setTimeSeries(timeSeries, startInstant, endInstant);

        if (movingRangeDisplayOption != NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN) {
            calculateMovingRangeTimeSeries();
        } else {
            movingRangeTimeSeries = null;
        }
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

                    NumericTimeSeriesPanel detailsNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, PlotDisplayOption.POINT);
                    detailsNumericTimeSeriesPanel.setBackground(Color.white);
                    detailsNumericTimeSeriesPanel.setMovingRangeDisplayOption(MovingRangeDisplayOption.OPACITY);

                    NumericTimeSeriesPanel overviewNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, PlotDisplayOption.LINE);
                    overviewNumericTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                    overviewNumericTimeSeriesPanel.setBackground(Color.white);
                    Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                    overviewNumericTimeSeriesPanel.setBorder(border);

                    JScrollPane scroller = new JScrollPane(detailsNumericTimeSeriesPanel);
                    scroller.getVerticalScrollBar().setUnitIncrement(10);
                    scroller.getHorizontalScrollBar().setUnitIncrement(10);
                    scroller.setBackground(frame.getBackground());
                    scroller.setBorder(border);

                    ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                    ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                    ((JPanel)frame.getContentPane()).add(overviewNumericTimeSeriesPanel, BorderLayout.SOUTH);

                    frame.setSize(1000, 400);
                    frame.setVisible(true);

                    TimeSeries timeSeries = new TimeSeries("Test");

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//                    Instant startInstant = Instant.now();
//                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords+120, ChronoUnit.SECONDS);

                    double value = 0.;

                    for (int i = 120; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
//                        instant = instant.plusMillis(random.nextInt(1000));
                        value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
                        double range = Math.abs(value) * .25;
                        double upperRange = value + range;
                        double lowerRange = value - range;
                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
                    }

                    overviewNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                    detailsNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());

                    scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            JScrollBar scrollBar = (JScrollBar)e.getSource();
                            double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();

                            double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                            double deltaTime = norm * Duration.between(overviewNumericTimeSeriesPanel.getStartInstant(), overviewNumericTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant startHighlightInstant = overviewNumericTimeSeriesPanel.getStartInstant().plusMillis((long)deltaTime);

                            int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                            norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                            deltaTime = norm * Duration.between(overviewNumericTimeSeriesPanel.getStartInstant(), overviewNumericTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant endHighlightInstant = overviewNumericTimeSeriesPanel.getEndInstant().minusMillis((long) deltaTime);

                            overviewNumericTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                        }
                    });

                    overviewNumericTimeSeriesPanel.setPinningEnabled(false);
                    overviewNumericTimeSeriesPanel.setInteractiveSelectionEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
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

    public enum PlotDisplayOption {
        STEPPED_LINE, LINE, BAR, POINT, SPECTRUM
    }

    public enum MovingRangeDisplayOption {
        PLOT_VALUE, OPACITY, NOT_SHOWN
    }
}
