package gov.ornl.csed.cda.experimental;

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
 */
public class SegmentedTimeSeriesPanel extends JComponent {
    // ========== CLASS FIELDS ==========
    private ChronoUnit chronoUnit;      // Unit of time for visualization
    private TreeMap<Double, TimeSeries> timeSeriesMap;  // Map that holds the segmented time series
    private double maxTimeSeriesValue;                  //
    private double minTimeSeriesValue;                  //
    private int largestPlotWidth;

    public int getPlotTimeUnitWidth() {
        return plotTimeUnitWidth;
    }

    public void setPlotTimeUnitWidth(int plotTimeUnitWidth) {
        if (this.plotTimeUnitWidth != plotTimeUnitWidth) {
            this.plotTimeUnitWidth = plotTimeUnitWidth;
            setPreferredSize(new Dimension(((largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth),
                    (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom))));
            repaint();
        }
    }

    private int plotTimeUnitWidth = 2;                  // maybe let (A second is represented by two pixels)
    private Insets margins = new Insets(4, 4, 4, 4);    // the user (Visual buffer surrounding the vis)
    private int timeSeriesLabelWidth = 80;              // set (Pixel width of the plot labels)

    public int getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(int plotHeight) {
        if (this.plotHeight != plotHeight) {
            this.plotHeight = plotHeight;
            repaint();
        }
    }

    private int plotHeight = 60;                        // The total height of a plotted series segment
    private int plotSpacing = plotHeight + 10;          // Adds buffer to plot height for in between plotted series segments
    private DecimalFormat df = new DecimalFormat("#,##0.00");      // Decimal formatter for build height
    ArrayList<Map.Entry<Double, TimeSeries>> entryList;


    // ========== CONSTRUCTOR ==========
    SegmentedTimeSeriesPanel (ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;

    }

    // ========== METHODS ==========
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

            // Create ArrayList of entries from the segmented time series
            ArrayList<Map.Entry<Double, TimeSeries>> entryList = new ArrayList<>(timeSeriesMap.entrySet());
            Collections.reverse(entryList); // probably don't wan't to sort this every time you draw... move to setter method

//            for (Map.Entry<Double, TimeSeries> entry : timeSeriesMap.entrySet()) {
            for (Map.Entry<Double, TimeSeries> entry : entryList) {
                double buildHeight = entry.getKey();
//                double normHeight = (buildHeight - minBuildHeight) / (maxBuildHeight - minBuildHeight);
//                double yOffset = normHeight * getHeight();
//                double plotBaselineY = getHeight() - yOffset;

                double plotBaselineY = plotSpacing * timeseriesCounter;

                TimeSeries timeSeries = entry.getValue();

                String label = df.format(buildHeight);
                int stringWidth = g2.getFontMetrics().stringWidth(label) + 8;
                g2.drawString(label, timeSeriesLabelWidth - stringWidth, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)));

                g2.translate(timeSeriesLabelWidth, plotBaselineY);
                drawTimeSeries(timeSeries, g2);
                g2.translate(-timeSeriesLabelWidth, -plotBaselineY);

                timeseriesCounter++;
            }
            g2.translate(-margins.left, -margins.top);
        }
    }

    private void drawTimeSeries (TimeSeries timeSeries, Graphics2D g2) {
        long totalTimeUnits = chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());
        int plotWidth = ((int) totalTimeUnits + 1) * plotTimeUnitWidth;
        Rectangle plotRectangle = new Rectangle(0, 0, plotWidth, plotHeight);

        Point2D.Double lastPoint = null;
        for (TimeSeriesRecord record : timeSeries.getAllRecords()) {
            long deltaTime = chronoUnit.between(timeSeries.getStartInstant(), record.instant);
            double x = (double) (deltaTime * plotTimeUnitWidth) + (plotTimeUnitWidth / 2.);

            double norm = (record.value - minTimeSeriesValue) / (maxTimeSeriesValue - minTimeSeriesValue);
            double yOffset = norm * plotRectangle.height;
            double y = plotRectangle.height - yOffset;

            Point2D.Double point = new Point2D.Double(x, y);

            if (lastPoint != null) {
                Line2D.Double line = new Line2D.Double(lastPoint.x, lastPoint.y, point.x, lastPoint.y);
                g2.draw(line);
                line = new Line2D.Double(point.x, lastPoint.y, point.x, point.y);
                g2.draw(line);
//                Line2D.Double line = new Line2D.Double(lastPoint, point);
//                g2.draw(line);
            }

            Ellipse2D.Double circle = new Ellipse2D.Double(point.x - 1., point.y - 1., 2., 2.);
            g2.draw(circle);

            lastPoint = point;
        }
    }

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
                    if (timeSeries.getMaxValue() > maxTimeSeriesValue) {
                        maxTimeSeriesValue = timeSeries.getMaxValue();
                    }
                    if (timeSeries.getMinValue() < minTimeSeriesValue) {
                        minTimeSeriesValue = timeSeries.getMinValue();
                    }
                }

                int totalTimeUnits = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;
//                int totalPlotWidth = ((int)totalTimeUnits + 1) * plotTimeUnitWidth;
//
//                if (totalPlotWidth > largestPlotWidth) {
//                    largestPlotWidth = totalPlotWidth;
//                }
                if (totalTimeUnits > largestPlotWidth) {
                    largestPlotWidth = totalTimeUnits;
                }
            }
        }

        Dimension dim = new Dimension(((largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth),
                (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom)));
        setPreferredSize(dim);

        System.out.println(dim.toString());

        invalidate();

        repaint();
    }

    public void clearTimeSeries() {
        timeSeriesMap = null;
        maxTimeSeriesValue = Double.NaN;
        minTimeSeriesValue = Double.NaN;
        largestPlotWidth = 0;
        repaint();
    }
}
