package gov.ornl.csed.cda.histogram;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by csg on 2/15/16.
 */
public class HistogramPanel extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
    public static final int SUMMARY_STATS_DEFAULT_SIZE = 14;
    private final static Logger log = LoggerFactory.getLogger(HistogramPanel.class);
    // mouse hover objects
    Rectangle2D hoverBinRect = null;
    private int binCount = 20;
    private Histogram histogram;
    private int highlightBinCounts[];
    private ArrayList<Double> highlightValues;

    private Rectangle fullPlotRectangle;
    private Rectangle histogramPlotRectangle;
    private Rectangle summaryStatsRectangle;
    private double binRectangleSize;
    private ArrayList<Rectangle2D.Double> histogramBinRectangles;
    private ArrayList<Color> histogramBinColors;
    private ArrayList<Rectangle2D.Double> highlightHistogramBinRectangles;
    private ArrayList<Color> highlightHistogramBinColors;
    
    private ORIENTATION orientation;
    private STATISTICS_MODE statisticsMode;

    private Shape meanShape;
    private Shape medianShape;
    private Shape stdevRangeShape;
    private Shape IQRShape;



    private double maxOverallBinSize;
    private double maxHighlightBinSize;

    public HistogramPanel (ORIENTATION orientation, STATISTICS_MODE statisticsMode) {
        this.orientation = orientation;
        this.statisticsMode = statisticsMode;
        setMinimumSize(new Dimension(40,40));
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Table table = null;
                try {
                    table = new CSVTableReader().readTable("data/csv/cars.csv");
                } catch (DataIOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                JFrame frame = new JFrame();
                frame.setSize(800, 200);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                HistogramPanel horizontalHistogramPanel = new HistogramPanel(ORIENTATION.HORIZONTAL, STATISTICS_MODE.MEAN_BASED);
                horizontalHistogramPanel.setBackground(Color.white);
                horizontalHistogramPanel.setBorder(BorderFactory.createTitledBorder("Horizontal Histogram"));

                HistogramPanel verticalHistogramPanel = new HistogramPanel(ORIENTATION.VERTICAL, STATISTICS_MODE.MEDIAN_BASED);
                verticalHistogramPanel.setBackground(Color.white);
                verticalHistogramPanel.setBorder(BorderFactory.createTitledBorder("Vertical Histogram"));

                JPanel mainPanel = (JPanel)frame.getContentPane();
                mainPanel.setLayout(new GridLayout(1, 2));
                mainPanel.add(horizontalHistogramPanel);
                mainPanel.add(verticalHistogramPanel);

                frame.setVisible(true);

                int binCount = (int) Math.floor(Math.sqrt(table.getTupleCount()));
                if (binCount < 1) {
                    binCount = 1;
                }

                Column column = table.getColumn(4);
                double values[] = new double[column.getRowCount()];
                for (int i = 0; i < column.getRowCount(); i++) {
                    double value = column.getDouble(i);
                    if (!Double.isNaN(value)) {
                        values[i] = value;
                    }
                }
                Histogram histogram = new Histogram(table.getColumnName(4), values, binCount);

                horizontalHistogramPanel.setHistogram(histogram);
                verticalHistogramPanel.setHistogram(histogram);

                ArrayList<Double> highlightedValues = new ArrayList<Double>();
                for (int i = 0; i < column.getRowCount(); i++) {
                    double testValue = column.getDouble(i);
                    if (testValue < histogram.getDescriptiveStats().getMean()) {
                        highlightedValues.add(testValue);
                    }
                }

                horizontalHistogramPanel.setHighlightValues(highlightedValues);
                verticalHistogramPanel.setHighlightValues(highlightedValues);
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
        hoverBinRect = null;

        if (histogramPlotRectangle.contains(e.getPoint())) {
            // mouse is over the histogram plot

            // see if the mouse is over a histogram bin
            int binIndex = -1;
            if (orientation == ORIENTATION.HORIZONTAL) {
                binIndex = (int) ((e.getX() - histogramPlotRectangle.x) / binRectangleSize);
            } else {
                binIndex = (int) ((histogramPlotRectangle.getMaxY() - e.getY()) / binRectangleSize);
            }

            if (binIndex >= 0 && binIndex < histogramBinRectangles.size()) {
                hoverBinRect = histogramBinRectangles.get(binIndex);
                setToolTipText("bin count: " + histogram.getDistributionStats().getBinStats().get(binIndex).getN() +
                ", min: " + histogram.getDistributionStats().getBinStats().get(binIndex).getMin() + ", max: " +
                histogram.getDistributionStats().getBinStats().get(binIndex).getMax());

            }
        }

        repaint();
    }

    public int getBinCount() {
        return binCount;
    }

    public void setBinCount(int binCount) {
        this.binCount = binCount;

        if (histogram != null) {
            histogram.setBinCount(binCount);
        }

        // TODO: Update highlighted values bin counts
        layoutPanel();
    }

    public STATISTICS_MODE getStatisticsMode () {
        return statisticsMode;
    }

    public void setHistogram (Histogram histogram) {
        this.histogram = histogram;
        this.histogram.setBinCount(binCount);

        maxOverallBinSize = 0.;
        for (SummaryStatistics summaryStatistics : histogram.getDistributionStats().getBinStats()) {
            if (summaryStatistics.getN() > maxOverallBinSize) {
                maxOverallBinSize = summaryStatistics.getN();
            }
        }

        layoutPanel();
    }

    public void setHighlightValues (Collection<Double> highlightValueCollection) {
        highlightValues = new ArrayList<>(highlightValueCollection);
        highlightBinCounts = new int[binCount];
        Arrays.fill(highlightBinCounts, 0);

        double binValueRange = (histogram.getDescriptiveStats().getMax() - histogram.getDescriptiveStats().getMin()) / binCount;
        for (double highlightValue : highlightValues) {
            int binIndex = (int) ((highlightValue - histogram.getDescriptiveStats().getMin()) / binValueRange);
            highlightBinCounts[binIndex]++;
            if ((highlightValue < histogram.getDistributionStats().getBinStats().get(binIndex).getMin()) ||
                    (highlightValue > histogram.getDistributionStats().getBinStats().get(binIndex).getMax())) {
                log.debug("highlightValue: " + highlightValue + " binMax: " + histogram.getDistributionStats().getBinStats().get(binIndex).getMax() + " binMin: " + histogram.getDistributionStats().getBinStats().get(binIndex).getMin());
            }
        }

        layoutPanel();
    }

    private double mapValue(double value, double currentMin, double currentMax, double newMin, double newMax) {
        double norm = (value - currentMin) / (currentMax - currentMin);
        return (norm * (newMax - newMin)) + newMin;
    }

    private void layoutPanel() {
        if (histogram != null) {

            int plotLeft = getInsets().left;
            int plotTop = getInsets().top;
            int plotHeight = getHeight() - (getInsets().bottom + getInsets().top);
            int plotWidth = getWidth() - (getInsets().left + getInsets().right);
            fullPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotHeight);
            
            histogramBinRectangles = new ArrayList<>();
            histogramBinColors = new ArrayList<>();
            highlightHistogramBinRectangles = new ArrayList<>();
            highlightHistogramBinColors = new ArrayList<>();

            if (orientation == ORIENTATION.HORIZONTAL) {
                // horizontal histogram
                int histogramPlotHeight = (int) (fullPlotRectangle.height - SUMMARY_STATS_DEFAULT_SIZE);
                histogramPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, histogramPlotHeight);
                binRectangleSize = (double) histogramPlotRectangle.width / (double) binCount;

                summaryStatsRectangle = new Rectangle(plotLeft, (int) histogramPlotRectangle.getMaxY(), plotWidth, SUMMARY_STATS_DEFAULT_SIZE);

                // calculate bin rectangles
                int binNumber = 0;
                for (SummaryStatistics summaryStatistics : histogram.getDistributionStats().getBinStats()) {
                    double x = histogramPlotRectangle.x + (binNumber * binRectangleSize);
                    double binHeight = mapValue(summaryStatistics.getN(), 0., maxOverallBinSize, 0., histogramPlotRectangle.height);
                    double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - binHeight;
                    Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binRectangleSize, binHeight);
                    Color binColor = Color.gray;
                    histogramBinRectangles.add(binRect);
                    histogramBinColors.add(binColor);

                    binNumber++;
                }

                // calculate highlighted bin rectangles
                if (highlightBinCounts != null) {
                    for (int binIndex = 0; binIndex < binCount; binIndex++) {
                        double x = histogramPlotRectangle.x + (binIndex * binRectangleSize);
                        double binHeight = mapValue(highlightBinCounts[binIndex], 0., maxOverallBinSize, 0., histogramPlotRectangle.height);
                        double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - binHeight;
                        Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binRectangleSize, binHeight);
                        Color binColor = Color.gray;
                        highlightHistogramBinRectangles.add(binRect);
                        highlightHistogramBinColors.add(binColor);
                    }
                }

                double centerY = summaryStatsRectangle.getCenterY();

                // calculate mean position
                double meanX = mapValue(histogram.getDescriptiveStats().getMean(), histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(),
                        histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                meanShape = new Ellipse2D.Double(meanX-2., centerY-2., 4., 4.);

                // calculate median position
                double medianX = mapValue(histogram.getDescriptiveStats().getPercentile(50.0), histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(),
                        histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                medianShape = new Ellipse2D.Double(medianX-2., centerY-2., 4., 4.);

                // calculate standard deviation range
                double stdevRangeLeft = mapValue(histogram.getDescriptiveStats().getMean() - histogram.getDescriptiveStats().getStandardDeviation(),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                double stdevRangeRight = mapValue(histogram.getDescriptiveStats().getMean() + histogram.getDescriptiveStats().getStandardDeviation(),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                stdevRangeShape = new Line2D.Double(stdevRangeLeft, centerY, stdevRangeRight, centerY);

                // calculate IQR
                double IQRLeft = mapValue(histogram.getDescriptiveStats().getPercentile(25.0),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                double IQRRight = mapValue(histogram.getDescriptiveStats().getPercentile(75.0),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                IQRShape = new Line2D.Double(IQRLeft, centerY, IQRRight, centerY);
            } else {
                // vertical histogram
                int histogramPlotWidth = (int)(fullPlotRectangle.width - SUMMARY_STATS_DEFAULT_SIZE);

                summaryStatsRectangle = new Rectangle(plotLeft, plotTop, SUMMARY_STATS_DEFAULT_SIZE, fullPlotRectangle.height);
                histogramPlotRectangle = new Rectangle((summaryStatsRectangle.x + summaryStatsRectangle.width),
                        plotTop, histogramPlotWidth, fullPlotRectangle.height);

                binRectangleSize = (double) histogramPlotRectangle.height / (double) binCount;

                // calculate bin rectangles
                int binNumber = 0;
                for (SummaryStatistics summaryStatistics : histogram.getDistributionStats().getBinStats()) {
                    double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - ((binNumber + 1) * binRectangleSize);
                    double binWidth = mapValue(summaryStatistics.getN(), 0., maxOverallBinSize, 0., histogramPlotRectangle.width);
                    double x = histogramPlotRectangle.x;
                    Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binWidth, binRectangleSize);
                    Color binColor = Color.gray;
                    histogramBinRectangles.add(binRect);
                    histogramBinColors.add(binColor);

                    binNumber++;
                }

                if (highlightBinCounts != null) {
                    // calculate highlight histogram bin rectangles
                    for (int binIndex = 0; binIndex < binCount; binIndex++) {
                        double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - ((binIndex + 1) * binRectangleSize);
                        double binWidth = mapValue(highlightBinCounts[binIndex], 0., maxOverallBinSize, 0., histogramPlotRectangle.width);
                        double x = histogramPlotRectangle.x;
                        Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binWidth, binRectangleSize);
                        Color binColor = Color.gray;
                        highlightHistogramBinRectangles.add(binRect);
                        highlightHistogramBinColors.add(binColor);
                    }
                }

                double centerX = summaryStatsRectangle.getCenterX();

                // calculate mean position
                double meanY = mapValue(histogram.getDescriptiveStats().getMean(), histogram.getDescriptiveStats().getMin(),
                        histogram.getDescriptiveStats().getMax(),
                        histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                meanShape = new Ellipse2D.Double(centerX-2., meanY-2., 4., 4.);

                // calculate median position
                double medianY = mapValue(histogram.getDescriptiveStats().getPercentile(50.0), histogram.getDescriptiveStats().getMin(),
                        histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                medianShape = new Ellipse2D.Double(centerX-2., medianY-2., 4., 4.);

                // calculate standard deviation range
                double stdevRangeLower = mapValue(histogram.getDescriptiveStats().getMean() - histogram.getDescriptiveStats().getStandardDeviation(),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                double stdevRangeUpper = mapValue(histogram.getDescriptiveStats().getMean() + histogram.getDescriptiveStats().getStandardDeviation(),
                        histogram.getDescriptiveStats().getMin(), histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                stdevRangeShape = new Line2D.Double(centerX, stdevRangeLower, centerX, stdevRangeUpper);

                // calculate IQR
                double IQRLower = mapValue(histogram.getDescriptiveStats().getPercentile(25.0), histogram.getDescriptiveStats().getMin(),
                        histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                double IQRUpper = mapValue(histogram.getDescriptiveStats().getPercentile(75.0), histogram.getDescriptiveStats().getMin(),
                        histogram.getDescriptiveStats().getMax(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                IQRShape = new Line2D.Double(centerX, IQRLower, centerX, IQRUpper);
            }
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (histogram != null) {
//            g2.setColor(Color.orange);
//            g2.draw(histogramPlotRectangle);
//            g2.setColor(Color.cyan);
//            g2.draw(summaryStatsRectangle);

            // draw histogram bins
            for (int i = 0; i < histogramBinRectangles.size(); i++) {
                Rectangle2D binRect = histogramBinRectangles.get(i);
                g2.setColor(histogramBinColors.get(i));
                g2.fill(binRect);
                g2.setColor(Color.darkGray);

                g2.draw(binRect);
            }

            // draw highlight histogram bins
            for (int i = 0; i < highlightHistogramBinRectangles.size(); i++) {
                Rectangle2D binRect = highlightHistogramBinRectangles.get(i);
                g2.setColor(highlightHistogramBinColors.get(i).darker());
                g2.fill(binRect);
                g2.setColor(Color.black);
                g2.draw(binRect);
            }

            g2.setStroke(new BasicStroke(2.f));

            if (hoverBinRect != null) {
                g2.setColor(Color.ORANGE);
                g2.draw(hoverBinRect);
            }

            g2.setColor(Color.darkGray);
            if (statisticsMode == STATISTICS_MODE.MEAN_BASED) {
                if (meanShape != null) {
                    g2.fill(meanShape);
                    g2.draw(meanShape);
                }

                if (stdevRangeShape != null) {
                    g2.draw(stdevRangeShape);
                }
            } else {
                if (medianShape != null) {
                    g2.fill(medianShape);
                    g2.draw(medianShape);
                }

                if (IQRShape != null) {
                    g2.draw(IQRShape);
                }
            }
        }
    }

    public void setSummaryStatisticsMode(STATISTICS_MODE statisticsMode) {
        if (this.statisticsMode != statisticsMode) {
            this.statisticsMode = statisticsMode;
            repaint();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

    public enum ORIENTATION {VERTICAL, HORIZONTAL}

    public enum STATISTICS_MODE {MEAN_BASED, MEDIAN_BASED}
}
