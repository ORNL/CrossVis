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
public class SplitHistogramPanel extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
    public static final int SUMMARY_STATS_DEFAULT_SIZE = 8;
    private final static Logger log = LoggerFactory.getLogger(HistogramPanel.class);
    // mouse hover objects
    Rectangle2D hoverBinRect = null;
    private int binCount = 20;
    private Histogram histogram;

    // variables for highlighted values and summaries
    private int highlightBinCounts[];
    private double highlightValues[];
    private DescriptiveStatistics highlightStatistics;
    private Shape highlightMeanShape;
    private Shape highlightMedianShape;
    private Shape highlightStdevRangeShape;
    private Shape highlightIQRShape;

    private Rectangle fullPlotRectangle;
    private Rectangle overallHistogramRectangle;
    private Rectangle overallStatsRectangle;
    private Rectangle highlightHistogramRectangle;
    private Rectangle highlightStatsRectangle;

    private double binRectangleSize;
    private ArrayList<Rectangle2D.Double> histogramBinRectangles;
    private ArrayList<Color> histogramBinColors;
    private ArrayList<Rectangle2D.Double> highlightHistogramBinRectangles;
    private ArrayList<Color> highlightHistogramBinColors;
    
    private HistogramPanel.ORIENTATION orientation;
    private HistogramPanel.STATISTICS_MODE statisticsMode;

    private Shape meanShape;
    private Shape medianShape;
    private Shape stdevRangeShape;
    private Shape IQRShape;

    private double maxOverallBinSize;
    private double maxHighlightBinSize;

    public SplitHistogramPanel (HistogramPanel.ORIENTATION orientation, HistogramPanel.STATISTICS_MODE statisticsMode) {
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

                SplitHistogramPanel horizontalHistogramPanel = new SplitHistogramPanel(HistogramPanel.ORIENTATION.HORIZONTAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);
                horizontalHistogramPanel.setBackground(Color.white);
                horizontalHistogramPanel.setBorder(BorderFactory.createTitledBorder("Horizontal Histogram"));

                SplitHistogramPanel verticalHistogramPanel = new SplitHistogramPanel(HistogramPanel.ORIENTATION.VERTICAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);
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
                double highlightValueArray[] = new double[highlightedValues.size()];
                for (int i = 0; i < highlightedValues.size(); i++) {
                    highlightValueArray[i] = highlightedValues.get(i);
                }

                horizontalHistogramPanel.setHighlightValues(highlightValueArray);
                verticalHistogramPanel.setHighlightValues(highlightValueArray);
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
        hoverBinRect = null;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoverBinRect = null;
//
//        if (histogramPlotRectangle.contains(e.getPoint())) {
//            // mouse is over the histogram plot
//
//            // see if the mouse is over a histogram bin
//            int binIndex = -1;
//            if (orientation == HistogramPanel.ORIENTATION.HORIZONTAL) {
//                binIndex = (int) ((e.getX() - histogramPlotRectangle.x) / binRectangleSize);
//            } else {
//                binIndex = (int) ((histogramPlotRectangle.getMaxY() - e.getY()) / binRectangleSize);
//            }
//
//            if (binIndex >= 0 && binIndex < histogramBinRectangles.size()) {
//                hoverBinRect = histogramBinRectangles.get(binIndex);
//                setToolTipText("<html><b>Bin Count:</b> " + histogram.getDistributionStats().getBinStats().get(binIndex).getN() +
//                ", <b>Min:</b> " + histogram.getDistributionStats().getBinStats().get(binIndex).getMin() + ", <b>Max:</b> " +
//                histogram.getDistributionStats().getBinStats().get(binIndex).getMax());
//
//            }
//        } else if (summaryStatsRectangle.contains(e.getPoint())) {
//            // mouse is over the summary stats plot
//
//            // show the summary stats
//            StringBuffer buffer = new StringBuffer();
//            if (statisticsMode == HistogramPanel.STATISTICS_MODE.MEAN_BASED) {
//                buffer.append("<html><b>Mean:</b> " + histogram.getDescriptiveStats().getMean() + ", <b>2*StDev:</b> " + (2 * histogram.getDescriptiveStats().getStandardDeviation()));
//                if (highlightStatistics != null) {
//                    buffer.append("<br><b>Highlight Mean:</b> " + highlightStatistics.getMean() + ", <b>2*StDev:</b> " + (2 * highlightStatistics.getStandardDeviation()));
//                }
//                buffer.append("</html>");
//            } else {
//                buffer.append("<html><b>Median:</b> " + histogram.getDescriptiveStats().getPercentile(50.) + ", <b>IQR:</b> " +
//                        (histogram.getDescriptiveStats().getPercentile(75.) - histogram.getDescriptiveStats().getPercentile(25.)));
//                if (highlightStatistics != null) {
//                    buffer.append("<br><b>Highlight Median:</b> " + highlightStatistics.getPercentile(50.) + ", <b>IQR:</b> " +
//                            (highlightStatistics.getPercentile(75.) - highlightStatistics.getPercentile(25.)));
//                }
//                buffer.append("</html>");
//            }
//            setToolTipText(buffer.toString());
//        }

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

    public HistogramPanel.STATISTICS_MODE getStatisticsMode () {
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

    public void setHighlightValues (double highlightValues[]) {
        this.highlightValues = Arrays.copyOf(highlightValues, highlightValues.length);

        highlightBinCounts = new int[binCount];
        Arrays.fill(highlightBinCounts, 0);

        highlightStatistics = new DescriptiveStatistics(this.highlightValues);

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

            if (orientation == HistogramPanel.ORIENTATION.HORIZONTAL) {
                // layout for a horizontal histogram
                int splitY = (int) (fullPlotRectangle.getMaxY() - ((fullPlotRectangle.getMaxY() - fullPlotRectangle.getMinY()) / 3.));
                int overallHistogramPlotHeight = splitY - (plotTop + SUMMARY_STATS_DEFAULT_SIZE);
                overallHistogramRectangle = new Rectangle(plotLeft, plotTop + SUMMARY_STATS_DEFAULT_SIZE, plotWidth, overallHistogramPlotHeight);

                int highlightHistogramPlotHeight = ((fullPlotRectangle.y + fullPlotRectangle.height) - SUMMARY_STATS_DEFAULT_SIZE) - splitY;
                highlightHistogramRectangle = new Rectangle(plotLeft, overallHistogramRectangle.y + overallHistogramRectangle.height,
                        plotWidth, highlightHistogramPlotHeight);

                // calculate shapes for highlight statistics
                if (highlightStatistics != null) {

                }
            } else {
                // layout for a vertical histogram

                // calculate shapes for highlight statistics
                if (highlightStatistics != null) {

                }
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
            if (overallHistogramRectangle != null) {
                g2.setColor(Color.orange);
                g2.draw(overallHistogramRectangle);
                g2.setColor(Color.cyan);
                g2.draw(highlightHistogramRectangle);
            }
//            g2.setColor(Color.orange);
//            g2.draw(histogramPlotRectangle);
//            g2.setColor(Color.cyan);
//            g2.draw(summaryStatsRectangle);

//            // draw histogram bins
//            for (int i = 0; i < histogramBinRectangles.size(); i++) {
//                Rectangle2D binRect = histogramBinRectangles.get(i);
//                g2.setColor(histogramBinColors.get(i));
//                g2.fill(binRect);
//                g2.setColor(Color.darkGray);
//
//                g2.draw(binRect);
//            }
//
//            // draw highlight histogram bins
//            for (int i = 0; i < highlightHistogramBinRectangles.size(); i++) {
//                Rectangle2D binRect = highlightHistogramBinRectangles.get(i);
//                g2.setColor(highlightHistogramBinColors.get(i).darker());
//                g2.fill(binRect);
//                g2.setColor(Color.black);
//                g2.draw(binRect);
//            }
//
//            g2.setStroke(new BasicStroke(2.f));
//
//            if (hoverBinRect != null) {
//                g2.setColor(Color.ORANGE);
//                g2.draw(hoverBinRect);
//            }
//
//            g2.setColor(Color.GRAY);
//            if (statisticsMode == STATISTICS_MODE.MEAN_BASED) {
//                if (meanShape != null) {
//                    g2.fill(meanShape);
//                    g2.draw(meanShape);
//                }
//
//                if (stdevRangeShape != null) {
//                    g2.draw(stdevRangeShape);
//                }
//
//                g2.setColor(Color.DARK_GRAY);
//                if (highlightMeanShape != null) {
//                    g2.fill(highlightMeanShape);
//                    g2.draw(highlightMeanShape);
//                }
//
//                if (highlightStdevRangeShape != null) {
//                    g2.draw(highlightStdevRangeShape);
//                }
//            } else {
//                if (medianShape != null) {
//                    g2.fill(medianShape);
//                    g2.draw(medianShape);
//                }
//
//                if (IQRShape != null) {
//                    g2.draw(IQRShape);
//                }
//
//                g2.setColor(Color.DARK_GRAY);
//                if (highlightMedianShape != null) {
//                    g2.fill(highlightMedianShape);
//                    g2.draw(highlightMedianShape);
//                }
//
//                if (highlightIQRShape != null) {
//                    g2.draw(highlightIQRShape);
//                }
//            }
        }
    }

    public void setSummaryStatisticsMode(HistogramPanel.STATISTICS_MODE statisticsMode) {
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
}
