package gov.ornl.csed.cda.timevis;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by csg on 1/12/16.
 */
public class TimeSeriesRenderer {
    private final static Logger log = LoggerFactory.getLogger(TimeSeriesRenderer.class);

    public static void renderAsOverview(Graphics2D g2, TimeSeries timeSeries, int plotWidth, int plotHeight,
                                        int plotUnitWidth, ChronoUnit plotChronoUnit, TimeSeriesPanel.PlotDisplayOption plotDisplayOption,
                                        Color gridColor, Color lineColor, Color pointColor, Color rangeColor, Color stdevRangeColor,
                                        TimeSeriesSummaryInfo summaryInfoArray[]) {

        if (summaryInfoArray != null) {
            g2.setColor(gridColor);
            TimeSeriesRenderer.drawZeroLine(g2, timeSeries, plotWidth, plotHeight);

            Path2D.Double maxPath = null;
            Path2D.Double minPath = null;
            Path2D.Double upperStDevRangePath = null;
            Path2D.Double lowerStDevRangePath = null;
            Path2D.Double meanPath = null;

            g2.setColor(lineColor);
            TimeSeriesSummaryInfo lastSummaryInfo = null;
            for (int i = 0; i < summaryInfoArray.length; i++) {
                TimeSeriesSummaryInfo summaryInfo = summaryInfoArray[i];


                if (summaryInfo != null) {
                    if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.POINT) {
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - plotUnitWidth / 2., summaryInfo.meanPoint.getY() - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
                        g2.draw(ellipse);
                    } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.LINE) {
                        if (meanPath == null) {
                            meanPath = new Path2D.Double();
                            meanPath.moveTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
                            maxPath = new Path2D.Double();
                            maxPath.moveTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
                            minPath = new Path2D.Double();
                            minPath.moveTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
                            upperStDevRangePath = new Path2D.Double();
                            upperStDevRangePath.moveTo(summaryInfo.upperStandardDeviationRangePoint.getX(), summaryInfo.upperStandardDeviationRangePoint.getY());
                            lowerStDevRangePath = new Path2D.Double();
                            lowerStDevRangePath.moveTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), summaryInfo.lowerStandardDeviationRangePoint.getY());
                        } else {
                            meanPath.lineTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
                            maxPath.lineTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
                            minPath.lineTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
                            upperStDevRangePath.lineTo(summaryInfo.upperStandardDeviationRangePoint.getX(), summaryInfo.upperStandardDeviationRangePoint.getY());
                            lowerStDevRangePath.lineTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), summaryInfo.lowerStandardDeviationRangePoint.getY());
                        }
//                        if (lastSummaryInfo != null) {
//                            Line2D.Double line = new Line2D.Double(lastSummaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY(), summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                            g2.draw(line);
//                        }
                        g2.setColor(pointColor);
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
                        g2.draw(ellipse);
                    } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE) {
//                        if (meanPath.getCurrentPoint() == null) {
//                            meanPath.moveTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                            maxPath.moveTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                            minPath.moveTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                        } else {
//                            Point2D lastPoint = meanPath.getCurrentPoint();
//                            meanPath.lineTo(summaryInfo.meanPoint.getX(), lastPoint.getY());
//                            meanPath.lineTo(summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
//                            lastPoint = maxPath.getCurrentPoint();
//                            maxPath.lineTo(summaryInfo.maxPoint.getX(), lastPoint.getY());
//                            maxPath.lineTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                            lastPoint = minPath.getCurrentPoint();
//                            minPath.lineTo(summaryInfo.minPoint.getX(), lastPoint.getY());
//                            minPath.lineTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                        }
                        if (lastSummaryInfo != null) {
//                            Path2D.Double path = new Path2D.Double();
//                            path.moveTo(lastSummaryInfo.maxPoint.getX(), lastSummaryInfo.maxPoint.getY());
//                            path.lineTo(summaryInfo.maxPoint.getX(), lastSummaryInfo.maxPoint.getY());
//                            path.lineTo(summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                            path.lineTo(summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                            path.lineTo(summaryInfo.minPoint.getX(), lastSummaryInfo.minPoint.getY());
//                            path.lineTo(lastSummaryInfo.minPoint.getX(), lastSummaryInfo.minPoint.getY());
//                            path.closePath();
//                            g2.setColor(rangeColor);
//                            g2.fill(path);
//                            g2.draw(path);
//
//                            path = new Path2D.Double();
//                            path.moveTo(lastSummaryInfo.upperStandardDeviationRangePoint.getX(), lastSummaryInfo.upperStandardDeviationRangePoint.getY());
//                            path.lineTo(summaryInfo.upperStandardDeviationRangePoint.getX(), lastSummaryInfo.upperStandardDeviationRangePoint.getY());
//                            path.lineTo(summaryInfo.upperStandardDeviationRangePoint.getX(), summaryInfo.upperStandardDeviationRangePoint.getY());
//                            path.lineTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), summaryInfo.lowerStandardDeviationRangePoint.getY());
//                            path.lineTo(summaryInfo.lowerStandardDeviationRangePoint.getX(), lastSummaryInfo.lowerStandardDeviationRangePoint.getY());
//                            path.lineTo(lastSummaryInfo.lowerStandardDeviationRangePoint.getX(), lastSummaryInfo.lowerStandardDeviationRangePoint.getY());
//                            path.closePath();
//                            g2.setColor(rangeColor);
//                            g2.fill(path);
//                            g2.draw(path);

//                            // draw max line
//                            g2.setColor(lineColor);
//                            Line2D.Double line1 = new Line2D.Double(lastSummaryInfo.maxPoint.getX(), lastSummaryInfo.maxPoint.getY(), summaryInfo.maxPoint.getX(), lastSummaryInfo.maxPoint.getY());
////                            Line2D.Double line2 = new Line2D.Double(summaryInfo.maxPoint.getX(), lastSummaryInfo.maxPoint.getY(), summaryInfo.maxPoint.getX(), summaryInfo.maxPoint.getY());
//                            g2.draw(line1);
////                            g2.draw(line2);
//
//                            // draw max line
//                            g2.setColor(lineColor);
//                            line1 = new Line2D.Double(lastSummaryInfo.minPoint.getX(), lastSummaryInfo.minPoint.getY(), summaryInfo.minPoint.getX(), lastSummaryInfo.minPoint.getY());
////                            line2 = new Line2D.Double(summaryInfo.minPoint.getX(), lastSummaryInfo.minPoint.getY(), summaryInfo.minPoint.getX(), summaryInfo.minPoint.getY());
//                            g2.draw(line1);
////                            g2.draw(line2);

                            // draw mean line
                            g2.setColor(lineColor);
                            Line2D.Double line1 = new Line2D.Double(lastSummaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY(), summaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY());
                            Line2D.Double line2 = new Line2D.Double(summaryInfo.meanPoint.getX(), lastSummaryInfo.meanPoint.getY(), summaryInfo.meanPoint.getX(), summaryInfo.meanPoint.getY());
                            g2.draw(line1);
                            g2.draw(line2);
                        }
                        g2.setColor(pointColor);
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(summaryInfo.meanPoint.getX() - 1, summaryInfo.meanPoint.getY() - 1, 2., 2.);
                        g2.draw(ellipse);
                    } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.BAR) {

                    }

                    lastSummaryInfo = summaryInfo;
                }
            }

            if (meanPath != null) {
                g2.setColor(rangeColor);
                g2.draw(maxPath);
                g2.draw(minPath);
                g2.setColor(stdevRangeColor);
                g2.draw(upperStDevRangePath);
                g2.draw(lowerStDevRangePath);
                g2.setColor(lineColor);
                g2.draw(meanPath);
            }

            //            g2.setColor(rangeColor);
//            g2.draw(minPath);
//            g2.draw(maxPath);
//            Path2D.Double rangePath = new Path2D.Double(maxPath);
//            rangePath.append(minPath, true);
//            g2.setColor(Color.blue);
//            g2.fill(rangePath);
//
//            g2.setColor(lineColor);
//            g2.draw(meanPath);
        }
    }

    public static void renderAsDetailed(Graphics2D g2, TimeSeries timeSeries, Instant clipStartInstant,
                                        Instant clipEndInstant,
                                        int plotWidth, int plotHeight, int plotUnitWidth,
                                        ChronoUnit plotChronoUnit,
                                        TimeSeriesPanel.PlotDisplayOption plotDisplayOption, Color gridColor,
                                        Color lineColor, Color pointColor, Color rangeColor,
                                        TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap) {

        g2.setColor(gridColor);
        TimeSeriesRenderer.drawZeroLine(g2, timeSeries, plotWidth, plotHeight);

        Instant start = plotPointMap.firstKey();
        if (clipStartInstant.isAfter(start)) {
            start = plotPointMap.lowerKey(clipStartInstant);
        }
        Instant end = plotPointMap.lastKey();
        if (clipEndInstant.isBefore(end)) {
            end = plotPointMap.higherKey(clipEndInstant);
        }
        NavigableMap<Instant, ArrayList<Point2D.Double>> clipMap = plotPointMap.subMap(start, true, end, true);
        if (clipMap.isEmpty()) {
            log.debug("No records in clip range - nothing to draw");
        } else {
            Point2D.Double lastDrawnPoint = null;
            int numPointsDrawn = 0;
            for (ArrayList<Point2D.Double> instantPoints : clipMap.values()) {
                for (Point2D.Double point : instantPoints) {
                    if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.POINT) {
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - plotUnitWidth / 2.,
                                point.y - plotUnitWidth / 2., plotUnitWidth, plotUnitWidth);
                        g2.setColor(pointColor);
                        g2.draw(ellipse);
                    } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.LINE) {
                        if (lastDrawnPoint != null) {
                            Line2D.Double line = new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, point.x, point.y);
                            g2.setColor(lineColor);
                            g2.draw(line);
                        }
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - 1,
                                point.y - 1, 2., 2.);
                        g2.setColor(pointColor);
                        g2.draw(ellipse);
                    } else if (plotDisplayOption == TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE) {
                        if (lastDrawnPoint != null) {
                            Line2D.Double line1 = new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, point.x, lastDrawnPoint.y);
                            Line2D.Double line2 = new Line2D.Double(point.x, lastDrawnPoint.y, point.x, point.y);
                            g2.setColor(lineColor);
                            g2.draw(line1);
                            g2.draw(line2);
                        }
                        Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - 1, point.y - 1, 2., 2.);
                        g2.setColor(pointColor);
                        g2.draw(ellipse);
                    }
                    lastDrawnPoint = point;
                    numPointsDrawn++;
                }
            }
        }
    }

    private static void drawZeroLine(Graphics2D g2, TimeSeries timeSeries, int plotWidth, int plotHeight) {
        // draw the zero value line if min < 0 and max > 0
        if (timeSeries.getMinValue() < 0. && timeSeries.getMaxValue() > 0.) {
            double norm = (0. - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
            double yOffset = norm * plotHeight;
            double zeroY = plotHeight - yOffset;
            Line2D.Double line = new Line2D.Double(0, zeroY, plotWidth, zeroY);
            g2.draw(line);
        }
    }

    public static ArrayList<TimeSeriesSummaryInfo> calculateOverviewPlotPoints(TimeSeries timeSeries, Rectangle plotRectangle,
                                                                         int numPlotUnits, int plotUnitWidth,
                                                                         int plotUnitDurationMillis,
                                                                         Instant startInstant) {
        ArrayList<TimeSeriesSummaryInfo> summaryInfoList = new ArrayList<>();
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

                int x = i * plotUnitWidth;
                double norm = (stats.getMean() - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
                double yOffset = norm * (plotRectangle.getHeight());
                double y = (plotRectangle.getHeight() - yOffset);

                Point2D.Double meanPoint = new Point2D.Double(x, y);
                TimeSeriesSummaryInfo summaryInfo = new TimeSeriesSummaryInfo();
                summaryInfo.instant = unitStartInstant;
                summaryInfo.meanValue = stats.getMean();
                summaryInfo.maxValue = stats.getMax();
                summaryInfo.minValue = stats.getMin();
                summaryInfo.meanPoint = meanPoint;
                summaryInfoList.add(summaryInfo);
            }
        }
        return summaryInfoList;
    }

    public static TreeMap<Instant, ArrayList<Point2D.Double>> calculateDetailedPlotPoints(TimeSeries timeSeries,
                                                                                    ChronoUnit plotChronoUnit,
                                                                                    int plotUnitWidth,
                                                                                    Rectangle plotRectangle,
                                                                                    Instant startInstant) {
        TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap = new TreeMap<>();

        plotPointMap.clear();
        ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
        if (records != null) {
            for (TimeSeriesRecord record : records) {
                long deltaTime = plotChronoUnit.between(startInstant, record.instant);
                int x = (int)deltaTime * plotUnitWidth;

                double norm = (record.value - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
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

        return plotPointMap;
    }
}
