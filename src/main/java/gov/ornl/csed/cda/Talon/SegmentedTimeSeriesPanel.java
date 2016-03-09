package gov.ornl.csed.cda.Talon;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by whw on 2/29/16.
 * halseywh@ornl.gov
 */
public class SegmentedTimeSeriesPanel extends JComponent {
    // ========== CLASS FIELDS ==========
    private ChronoUnit chronoUnit;      // Unit of time for visualization (currently hard-coded to seconds)
    private TreeMap<Double, TimeSeries> timeSeriesMap;  // Map that holds the segmented time series
    private double maxTimeSeriesValue;                  // Max value across all time series segments
    private double minTimeSeriesValue;                  // Min value across all time series segments
    private int largestPlotWidth;                       // Longest time series across all time series segments
    private int plotTimeUnitWidth = 2;                  // A second is represented by two pixels (now has a spinner to adjust this)
    private Insets margins = new Insets(4, 4, 4, 4);    // Visual buffer surrounding the vis
    private int timeSeriesLabelWidth = 80;              // Pixel width of the plot labels (now has a spinner to adjust this)
    private int plotHeight = 60;                        // The total height of a plotted series segment
    private int plotSpacing = plotHeight + 10;          // Adds buffer to plot height for in between plotted series segments
    private DecimalFormat df = new DecimalFormat("#,##0.00");       // Decimal formatter for build height
    private ArrayList<Map.Entry<Double, TimeSeries>> entryList;     // Holds the timeSeriesMap series in reverse order

    // ========== CONSTRUCTOR ==========
    SegmentedTimeSeriesPanel (ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    // ========== METHODS ==========
    // Getters/Setters
    public int getPlotTimeUnitWidth() {
        return plotTimeUnitWidth;
    }

    // When changing plotTimeUnitWidth change the preferred size and redraw
    public void setPlotTimeUnitWidth(int plotTimeUnitWidth) {
        if (this.plotTimeUnitWidth != plotTimeUnitWidth) {
            this.plotTimeUnitWidth = plotTimeUnitWidth;
            setPreferredSize(new Dimension(((largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth),
                    (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom))));
            repaint();
        }
    }

    public int getPlotHeight() {
        return plotHeight;
    }

    // When changing plotHeight change the preferred dimensions and redraw
    public void setPlotHeight(int plotHeight) {
        if (this.plotHeight != plotHeight) {
            this.plotHeight = plotHeight;
            this.plotSpacing = plotHeight + 10;
            setPreferredSize(new Dimension(((largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth),
                    (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom))));
            repaint();
        }
    }

    // Draws everything in the segment panel
    // Triggered by calling repaint();
    public void paintComponent(Graphics g) {

        // You will pretty much always do this for a vis
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw if segmented time series exists
        if (timeSeriesMap != null && !timeSeriesMap.isEmpty()) {
            double minBuildHeight = timeSeriesMap.firstKey();
            double maxBuildHeight = timeSeriesMap.lastKey();

            // Set color and translate pen away from the corner
            g2.setColor(Color.darkGray);
            g2.translate(margins.left, margins.top);
            int timeseriesCounter = 0;

//            for (Map.Entry<Double, TimeSeries> entry : timeSeriesMap.entrySet()) {                        // old

            // for every time series in the (reverse order) entry list
            for (Map.Entry<Double, TimeSeries> entry : entryList) {
                // get current build height
                double buildHeight = entry.getKey();
//                double normHeight = (buildHeight - minBuildHeight) / (maxBuildHeight - minBuildHeight);   // old
//                double yOffset = normHeight * getHeight();                                                // old
//                double plotBaselineY = getHeight() - yOffset;                                             // old

                // Set the base line for the current build height time series
                double plotBaselineY = plotSpacing * timeseriesCounter;

                // Get time series for current build height
                TimeSeries timeSeries = entry.getValue();

                // Create label for the current build height; Set width for label and draw
                String label = df.format(buildHeight);
                int stringWidth = g2.getFontMetrics().stringWidth(label) + 8;
                g2.drawString(label, timeSeriesLabelWidth - stringWidth, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)));

                // put g2 past the label and at the bottom of the time series baseline and draw time series
                g2.translate(timeSeriesLabelWidth, plotBaselineY);
                drawTimeSeries(timeSeries, g2);
                g2.translate(-timeSeriesLabelWidth, -plotBaselineY);

                timeseriesCounter++;
            }
            g2.translate(-margins.left, -margins.top);
        }
    }

    // Actually draws the point and lines of the time series for the current build height
    private void drawTimeSeries (TimeSeries timeSeries, Graphics2D g2) {
        // find the "time length" of the current time series
        long totalTimeUnits = chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());

        // translate the "time length" to physical length of the current timeseries
        int plotWidth = ((int) totalTimeUnits + 1) * plotTimeUnitWidth;

        // Create a rectangle around the draw area
        Rectangle plotRectangle = new Rectangle(0, 0, plotWidth, plotHeight);

        Point2D.Double lastPoint = null;

        long totalPlotDeltaTime = ChronoUnit.MILLIS.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());

        // for each record in the current time series
        for (TimeSeriesRecord record : timeSeries.getAllRecords()) {

            // find amount of time since beginning of time series for current build height and set 'x' coordinate
//            long deltaTime = chronoUnit.between(timeSeries.getStartInstant(), record.instant);
            long deltaTime = ChronoUnit.MILLIS.between(timeSeries.getStartInstant(), record.instant);
            double normTime = (double) deltaTime / totalPlotDeltaTime;
//            double x = (double) (deltaTime * plotTimeUnitWidth) + (plotTimeUnitWidth / 2.);         // WHAT DOES THIS DO?
            double x = normTime * plotWidth;

            // normalize the start point and set drawing start point and set 'y' coordinate
            double norm = (record.value - minTimeSeriesValue) / (maxTimeSeriesValue - minTimeSeriesValue);
            double yOffset = norm * plotRectangle.height;
            double y = plotRectangle.height - yOffset;

            // Create a new point at the (x, y) coordinate
            Point2D.Double point = new Point2D.Double(x, y);

            // Draw lines connecting the old and new point
            if (lastPoint != null) {
                // Draws step-wise line
                Line2D.Double line = new Line2D.Double(lastPoint.x, lastPoint.y, point.x, lastPoint.y);
                g2.draw(line);
                line = new Line2D.Double(point.x, lastPoint.y, point.x, point.y);
                g2.draw(line);
//                Line2D.Double line = new Line2D.Double(lastPoint, point);                         // old
//                g2.draw(line);                                                                    // old
            }

            // Draw circle at new point
            Ellipse2D.Double circle = new Ellipse2D.Double(point.x - 1., point.y - 1., 2., 2.);
            g2.draw(circle);

            lastPoint = point;
        }
    }

    // sets timeSeriesMap and sets/resets min/maxTimeSeriesValue and largestPlotWidth
    public void setTimeSeries(TreeMap<Double, TimeSeries> timeSeriesMap) {
        this.timeSeriesMap = timeSeriesMap;
        maxTimeSeriesValue = Double.NaN;
        minTimeSeriesValue = Double.NaN;
        largestPlotWidth = 0;

        if ((timeSeriesMap != null) && (!timeSeriesMap.isEmpty())) {
            for (Map.Entry<Double, TimeSeries> timeSeriesEntry : timeSeriesMap.entrySet()) {
                TimeSeries timeSeries = timeSeriesEntry.getValue();

                if (Double.isNaN(maxTimeSeriesValue)) {
                    maxTimeSeriesValue = timeSeries.getMaxValue();
                    minTimeSeriesValue = timeSeries.getMinValue();
                } else {
                    maxTimeSeriesValue = (timeSeries.getMaxValue() > maxTimeSeriesValue) ? timeSeries.getMaxValue() : maxTimeSeriesValue;
                    minTimeSeriesValue = (timeSeries.getMinValue() < minTimeSeriesValue) ? timeSeries.getMinValue() : minTimeSeriesValue;

                    // CONVERTED THESE STATEMENTS INTO THE TERNARY EXPRESSIONS ABOVE
//                    if (timeSeries.getMaxValue() > maxTimeSeriesValue) {
//                        maxTimeSeriesValue = timeSeries.getMaxValue();
//                    }
//                    if (timeSeries.getMinValue() < minTimeSeriesValue) {
//                        minTimeSeriesValue = timeSeries.getMinValue();
//                    }
                }

                int totalTimeUnits = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;
//                int totalPlotWidth = ((int)totalTimeUnits + 1) * plotTimeUnitWidth;       // old
//
//                if (totalPlotWidth > largestPlotWidth) {                                  // old
//                    largestPlotWidth = totalPlotWidth;                                    // old
//                }

                largestPlotWidth = (totalTimeUnits > largestPlotWidth) ? totalTimeUnits : largestPlotWidth;

                // CONVERTED THIS STATEMENT INTO THE TERNARY EXPRESSION ABOVE
//                if (totalTimeUnits > largestPlotWidth) {
//                    largestPlotWidth = totalTimeUnits;
//                }
            }
        }

        Dimension dim = new Dimension(((largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth),
                (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom)));
        setPreferredSize(dim);

        // Create arraylist of time series entries and reverse so bottom levels of build are on the bottom
        entryList = new ArrayList<>(timeSeriesMap.entrySet());
        Collections.reverse(entryList);

        System.out.println(dim.toString());

        revalidate();

        repaint();
    }

    // setting timeSeriesMap to null and repainting leaves an empty segment panel
    public void clearTimeSeries() {
        timeSeriesMap = null;
        maxTimeSeriesValue = Double.NaN;
        minTimeSeriesValue = Double.NaN;
        largestPlotWidth = 0;
        repaint();
    }
}
