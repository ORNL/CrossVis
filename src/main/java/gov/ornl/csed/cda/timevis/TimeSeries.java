package gov.ornl.csed.cda.timevis;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TimeSeries {
	private final static Logger log = LoggerFactory.getLogger(TimeSeries.class);

	private String name;
	private double maxValue = Double.NaN;
	private double minValue = Double.NaN;
    private Instant startInstant;
    private Instant endInstant;
	private TreeMap<Instant, TimeSeriesBin> binTreeMap = new TreeMap<>();
	
	public TimeSeries(String name) {
		this.name = name;
	}

	public void clear() {
        binTreeMap.clear();
        startInstant = null;
        endInstant = null;
        maxValue = Double.NaN;
        minValue = Double.NaN;
    }
	
	public Instant getStartInstant() {
		return startInstant;
	}
	
	public Instant getEndInstant() {
		return endInstant;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }
	
	public void addRecord(Instant instant, double value, double upperRange, double lowerRange) {
		TimeSeriesRecord record = new TimeSeriesRecord();
		record.instant = Instant.from(instant);
		record.value = value;
		record.upperRange = upperRange;
		record.lowerRange = lowerRange;

        if (binTreeMap.isEmpty()) {
            maxValue = value;
            minValue = value;
            startInstant = instant;
            endInstant = instant;
        } else {
            if (value > maxValue) {
                maxValue = value;
            }
            if (value < minValue) {
                minValue = value;
            }
            if (instant.isBefore(startInstant)) {
                startInstant = instant;
            }
            if (instant.isAfter(endInstant)) {
                endInstant = instant;
            }
        }

        TimeSeriesBin bin = binTreeMap.get(instant);
        if (bin == null) {
            bin = new TimeSeriesBin(instant);
            binTreeMap.put(instant, bin);
        }

        bin.records.add(record);
        Collections.sort(bin.records);
    }
	
	public ArrayList<TimeSeriesRecord> recordsAt(Instant instant) {
		if (binTreeMap.isEmpty()) {
			return null;
		}
		
		if (instant.isBefore(binTreeMap.firstKey()) || instant.isAfter(binTreeMap.lastKey())) {
			return null;
		}

        TimeSeriesBin bin = binTreeMap.get(instant);
        if (bin != null) {
            return bin.records;
        }

        return null;
	}

	public ArrayList<TimeSeriesRecord> getRecordsBetween(Instant start, Instant end) {
		ArrayList<TimeSeriesRecord> records = null;

		NavigableMap<Instant, TimeSeriesBin> subRecordMap = binTreeMap.subMap(start, true, end, true);
		if (!subRecordMap.isEmpty()) {
			records = new ArrayList<>();
			for (TimeSeriesBin bin : subRecordMap.values()) {
				for (TimeSeriesRecord record : bin.records) {
                    if (record.instant.isBefore(start)) {
                        continue;
                    } else if (record.instant.equals(end) || record.instant.isAfter(end)) {
                        break;
                    } else {
                        records.add(record);
                    }
				}
			}
		}

		return records;
	}

	public ArrayList<TimeSeriesRecord> getAllRecords() {
		ArrayList<TimeSeriesRecord> records = new ArrayList<>();
        for (TimeSeriesBin bin : binTreeMap.values()) {
            for (TimeSeriesRecord record : bin.records) {
                records.add(record);
            }
        }

        return records;
	}

	public int getRecordCount() {
		return binTreeMap.size();
	}

    public TreeMap<Instant, TimeSeriesBin> getBinTreeMap() {
        return binTreeMap;
    }

    public void draw(Graphics2D g2, Instant startInstant, Instant endInstant,
                     int plotHeight, int plotWidth, int plotChronoUnitWidth) {

    }
//    public void drawOverview(Graphics2D g2, Instant startInstant, Instant endInstant,
//                             int plotHeight, int plotWidth, int plotChronoUnitWidth) {
//        // draws an overview plot by condensing all records into the provided width
//        long totalDuration = ChronoUnit.MILLIS.between(startInstant, endInstant);
//        int numPlotUnits = plotWidth / plotChronoUnitWidth;
//        double plotUnitDurationReal = (double)totalDuration / numPlotUnits;
//        int plotUnitDuration = (int)Math.ceil(plotUnitDurationReal);
//
//        ArrayList<Double> plotValues = new ArrayList<>();
//        for (int i = 0; i < numPlotUnits; i++) {
//            // determine the start and end time instants for the current time unit
//            Instant unitStartInstant = startInstant.plusMillis(i * plotUnitDuration);
//            Instant unitEndInstant = unitStartInstant.plusMillis(plotUnitDuration);
//
//            // get values between start (inclusive) and end time instants (exclusive)
//            NavigableMap<Instant, TimeSeriesRecord> unitSubMap = binTreeMap.subMap(unitStartInstant, true, unitEndInstant, false);
//
//            if (!unitSubMap.isEmpty()) {
//                // calculate mean value for records in plot time unit
//                SummaryStatistics stats = new SummaryStatistics();
//
//                for (TimeSeriesRecord record : unitSubMap.values()) {
//                    stats.addValue(record.value);
//                }
//                plotValues.add(stats.getMean());
//            } else {
//                plotValues.add(Double.NaN);
//            }
//        }
//
//        // draw points
//        for (int i = 0; i < plotValues.size(); i++) {
//            double value = plotValues.get(i);
//
//            int x = i * plotChronoUnitWidth;
//
//            double norm = (value - minValue) / (maxValue - minValue);
//            double yOffset = norm * (plotHeight - 4);
//            double y = (plotHeight - yOffset);
//
//            log.debug("x = " + x + " y = " + y);
//            if (displayOption == DisplayOption.BAR) {
//                Rectangle2D.Double rect = new Rectangle2D.Double(x, y, plotChronoUnitWidth, yOffset);
//                g2.fill(rect);
//            } else if (displayOption == DisplayOption.POINT) {
//                Ellipse2D.Double ellipse = new Ellipse2D.Double(x - plotChronoUnitWidth / 2., y - plotChronoUnitWidth / 2., plotChronoUnitWidth, plotChronoUnitWidth);
//                g2.draw(ellipse);
//            }
//        }
//    }
//
//	public void draw(Graphics2D g2, Instant startInstant, Instant endInstant, Instant clipStartInstant,
//			Instant clipEndInstant, int plotHeight, int chartBarWidth) {
//		long totalTimeUnits = binChronoUnit.between(startInstant, endInstant);
//		int plotWidth = ((int)totalTimeUnits + 1) * chartBarWidth;
//		plotRectangle = new Rectangle(0, 0, plotWidth, plotHeight);
//
//		// draw zero line
//        if (minValue < 0. || maxValue < 0.) {
//            double norm = (0. - minValue) / (maxValue - minValue);
//            double yOffset = norm * (plotRectangle.height);
//            double zeroY = (plotRectangle.height - yOffset);
//            g2.setColor(gridLineColor);
//		    g2.drawLine(plotRectangle.x, (int) zeroY, plotRectangle.x + plotRectangle.width, (int) zeroY);
//        }
//
//		g2.setColor(plotOutlineColor);
//		g2.draw(plotRectangle);
//
//		if (!binTreeMap.isEmpty()) {
//            Instant start = binTreeMap.firstKey();
//            if (clipStartInstant.isAfter(start)) {
//                start = binTreeMap.lowerKey(clipStartInstant);
//            }
//            Instant end = binTreeMap.lastKey();
//            if (clipEndInstant.isBefore(end)) {
//                end = binTreeMap.higherKey(clipEndInstant);
//            }
//
//            NavigableMap<Instant, TimeSeriesRecord> subMap = binTreeMap.subMap(start, true, end, true);
//            if (subMap.isEmpty()) {
//                log.debug("No records in clip range. Nothing to draw.");
//            } else {
//                if (displayOption == DisplayOption.BAR.RANGE_LINE) {
//                    ArrayList<double[]> topRangePathPoints = new ArrayList<double[]>();
//                    ArrayList<double[]> bottomRangePathPoints = new ArrayList<double[]>();
//
//                    Path2D.Double middleValuePath = new Path2D.Double();
//
//                    for (TimeSeriesRecord record : subMap.values()) {
//                        long deltaStartDT = binChronoUnit.between(startInstant, record.instant);
//                        int x = (int) deltaStartDT * chartBarWidth;
//
//                        // calculate the middle value y position
//                        double norm = (record.value - minValue) / (maxValue - minValue);
//                        double yOffset = norm * (plotRectangle.height);
//                        double valueY = (plotRectangle.height - yOffset);
//
//                        // calculate top y position of range
//                        norm = (record.upperRange - minValue) / (maxValue - minValue);
//                        yOffset = norm * (plotRectangle.height);
//                        double rangeTop = (plotRectangle.height - yOffset);
//
//                        //calculate bottom y position of range
//                        norm = (record.lowerRange - minValue) / (maxValue - minValue);
//                        yOffset = norm * (plotRectangle.height);
//                        double rangeBottom = (plotRectangle.height - yOffset);
//
//                        // add point to the middle value path
//                        if (middleValuePath.getCurrentPoint() == null) {
//                            middleValuePath.moveTo(x, valueY);
//                            //                        topRangePath.moveTo(x, rangeTop);
//                            //                        bottomRangePath.moveTo(x, rangeBottom);
//                        } else {
//                            middleValuePath.lineTo(x, valueY);
//                            //                        topRangePath.lineTo(x, rangeTop);
//                            //                        bottomRangePath.lineTo(x, rangeBottom);
//                        }
//
//                        // add range path points
//                        topRangePathPoints.add(new double[]{x, rangeTop});
//                        bottomRangePathPoints.add(new double[]{x, rangeBottom});
//                    }
//
//                    // build range shape
//                    Path2D.Double rangePath = new Path2D.Double();
//                    for (int i = 0; i < topRangePathPoints.size(); i++) {
//                        double coord[] = topRangePathPoints.get(i);
//                        if (i == 0) {
//                            rangePath.moveTo(coord[0], coord[1]);
//                        } else {
//                            rangePath.lineTo(coord[0], coord[1]);
//                        }
//                    }
//                    for (int i = bottomRangePathPoints.size() - 1; i >= 0; i--) {
//                        double coord[] = bottomRangePathPoints.get(i);
//                        rangePath.lineTo(coord[0], coord[1]);
//                    }
//                    try {
//                        rangePath.closePath();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//
//                    //TODO: The problem drawing here is when the clip bounds excludes points outside the
//                    // visible range.  There are points, but the line starts with the first key value
//                    // which makes it look like not data is shown until the first instant/key value.
//                    // Solution is to find first key to the left of the start instant and next key after
//                    // the end instant.  It will draw off screen but will ensure that are no gaps that
//                    // don't actually exist.  Other option is to draw the mean + stdev differently.  Maybe
//                    // use a point with a rectangle for the range.  That would be more accurate.  Drawing
//                    // the line is misleading, shows data between real points as if data is there.
//                    g2.setColor(rangeFillColor);
//                    g2.fill(rangePath);
//                    g2.setColor(barColor);
//                    g2.draw(middleValuePath);
//                } else {
//                    for (TimeSeriesRecord record : subMap.values()) {
//                        long deltaStartDT = binChronoUnit.between(startInstant, record.instant);
//                        int x = (int) deltaStartDT * chartBarWidth;
//
//                        if (x < 0) {
//                            log.debug("x = " + x);
//                        }
//
//                        if (record.value < minValue) {
//                            log.debug("value is less than minValue");
//                        }
//
//                        double norm = (record.value - minValue) / (maxValue - minValue);
//                        double yOffset = norm * (plotRectangle.height - 4);
//                        double y = (plotRectangle.height - yOffset);
//
//                        if (y > plotRectangle.getMaxY()) {
//                            log.debug("y outside plot rectangle: " + y);
//                            log.debug("value is " + record.value + " max: " + maxValue);
//                        }
//
//                        if (Double.isNaN(record.uncertainty)) {
//                            g2.setColor(barColor);
//                        } else {
//                            g2.setColor(new Color(barColor.getRed(), barColor.getGreen(),
//                                    barColor.getBlue(), (int) (record.uncertainty * 255)));
//                        }
//
//                        if (displayOption == DisplayOption.BAR) {
//                            Rectangle2D.Double rect = new Rectangle2D.Double(x, y, chartBarWidth, yOffset);
//                            g2.fill(rect);
//                        } else if (displayOption == DisplayOption.POINT) {
//                            Ellipse2D.Double ellipse = new Ellipse2D.Double(x - chartBarWidth / 2., y - chartBarWidth / 2., chartBarWidth, chartBarWidth);
//                            g2.draw(ellipse);
//                        }
//                    }
//                }
//            }
//        }
//	}
}
