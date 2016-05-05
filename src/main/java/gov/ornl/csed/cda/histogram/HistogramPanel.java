package gov.ornl.csed.cda.histogram;

import gov.ornl.csed.cda.util.GraphicsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by csg on 2/15/16.
 */
public class HistogramPanel extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
    public static final int SUMMARY_STATS_DEFAULT_SIZE = 14;
    public static final int DEFAULT_RANGE_LABEL_HEIGHT = 14;
    public final static float DEFAULT_LABEL_FONT_SIZE = 10f;
    public final static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("####0.0#######");
    public final static DecimalFormat PERCENT_FORMATTER = new DecimalFormat("##0.0%");

    private final static Logger log = LoggerFactory.getLogger(HistogramPanel.class);

    // mouse hover objects
    Rectangle2D hoverBinRect = null;
    private int binCount = 20;
    private Histogram histogram;
    private Histogram highlightHistogram;

    // variables for highlighted values and summaries
    private Shape highlightMeanShape;
    private Shape highlightMedianShape;
    private Shape highlightStdevRangeShape;
    private Shape highlightIQRShape;

    private Rectangle fullPlotRectangle;
    private Rectangle histogramPlotRectangle;
    private Rectangle summaryStatsRectangle;
    private Rectangle valueRangeLabelsRectangle;
    private Rectangle maxLabelRectangle;
    private Rectangle minLabelRectangle;

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

    // count axis variables
    private int countAxisMax;
    private int countAxisMin = 0;
    private Rectangle countAxisMaxLabelBounds;
    private Rectangle countAxisMinLabelBounds;
    private Rectangle countAxisRectangle;
    private boolean mouseOverMaxCountLabel = false;
    private boolean mouseOverMinCountLabel = false;

//    private double maxOverallBinSize;
    private double maxHighlightBinSize;
    private boolean mouseOverMaxValueLabel = false;
    private boolean mouseOverMinValueLabel = false;

    private ArrayList<HistogramPanelListener> listeners = new ArrayList<>();

    public HistogramPanel (ORIENTATION orientation, STATISTICS_MODE statisticsMode) {
        this.orientation = orientation;
        this.statisticsMode = statisticsMode;
        setMinimumSize(new Dimension(40,40));
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void addHistogramPanelListener(HistogramPanelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean removeHistogramPanelListener(HistogramPanelListener listener) {
        return listeners.remove(listener);
    }

    private void fireHistogramPanelLowerLimitChanged() {
        for (HistogramPanelListener listener : listeners) {
            listener.histogramPanelLowerLimitChanged(this, histogram.getMinValue());
        }
    }

    private void fireHistogramPanelUpperLimitChanged() {
        for (HistogramPanelListener listener : listeners) {
            listener.histogramPanelUpperLimitChanged(this, histogram.getMaxValue());
        }
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public Histogram getHighlightHistogram() {
        return highlightHistogram;
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
                horizontalHistogramPanel.setBackground(Color.yellow);
//                horizontalHistogramPanel.setBorder(BorderFactory.createTitledBorder("Horizontal Histogram"));
//                horizontalHistogramPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));

                HistogramPanel verticalHistogramPanel = new HistogramPanel(ORIENTATION.VERTICAL, STATISTICS_MODE.MEAN_BASED);
                verticalHistogramPanel.setBackground(Color.cyan);
//                verticalHistogramPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 10));
//                verticalHistogramPanel.setBorder(BorderFactory.createTitledBorder("Vertical Histogram"));

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
                    if (testValue < histogram.getMean()) {
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
        if (mouseOverMinValueLabel) {
            String minValueString = DECIMAL_FORMATTER.format(histogram.getMinValue());
            String newValue = JOptionPane.showInputDialog(null,
                    "Enter Minimum Histogram Value:",
                    minValueString);
            if (newValue != null && !newValue.equals(minValueString)) {
                double value = Double.parseDouble(newValue);
                histogram.setMinValue(value);
                if (highlightHistogram != null) {
                    highlightHistogram.setMinValue(value);
                }
                countAxisMax = histogram.getMaxBinCount();
                layoutPanel();
                fireHistogramPanelLowerLimitChanged();
            }
        } else if (mouseOverMaxValueLabel) {
            String maxValueString = DECIMAL_FORMATTER.format(histogram.getMaxValue());
            String newValue = JOptionPane.showInputDialog(null,
                    "Enter Maximum Histogram Value:",
                    maxValueString);
            if (newValue != null && !newValue.equals(maxValueString)) {
                double value = Double.parseDouble(newValue);
                histogram.setMaxValue(value);
                countAxisMax = histogram.getMaxBinCount();
                if (highlightHistogram != null) {
                    highlightHistogram.setMaxValue(value);
                }
                layoutPanel();
                fireHistogramPanelUpperLimitChanged();
            }
        } else if (mouseOverMaxCountLabel) {
            String maxCountString = String.valueOf(countAxisMax);
            String newValue = JOptionPane.showInputDialog(null,
                    "Enter New Histogram Bin Count Upper Limit:",
                    maxCountString);
            if (newValue != null && !newValue.equals(maxCountString)) {
                countAxisMax = Integer.parseInt(newValue);
                layoutPanel();
//                fireHistogramPanelUpperLimitChanged();
            }
        } else if (mouseOverMinCountLabel) {

        }
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
        mouseOverMinValueLabel = false;
        mouseOverMaxValueLabel = false;
        mouseOverMaxCountLabel = false;
        mouseOverMinCountLabel = false;
        setToolTipText("");

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
                StringBuffer buffer = new StringBuffer();
                hoverBinRect = histogramBinRectangles.get(binIndex);
                buffer.append("<html><b>Bin Count:</b> " + histogram.getBinCount(binIndex) +
                ", <b>Min:</b> " + histogram.getBinLowerBound(binIndex) + ", <b>Max:</b> " +
                histogram.getBinUpperBound(binIndex));
                if (highlightHistogram != null) {
                    double percentage = (double)highlightHistogram.getBinCount(binIndex) / histogram.getBinCount(binIndex);
                    String percentString = PERCENT_FORMATTER.format(percentage);
                    buffer.append("<br/><b>Highlight Bin Count:</b> " + highlightHistogram.getBinCount(binIndex) +
                            ", <b>Percent of Overall:</b> " + percentString);
                }
                buffer.append("</html>");
                setToolTipText(buffer.toString());
            }
        } else if (summaryStatsRectangle.contains(e.getPoint())) {
            // mouse is over the summary stats plot

            // show the summary stats
            StringBuffer buffer = new StringBuffer();
            if (statisticsMode == STATISTICS_MODE.MEAN_BASED) {
                buffer.append("<html><b>Mean:</b> " + histogram.getMean() + ", <b>2*StDev:</b> " + (2 * histogram.getStDev()));
                if (highlightHistogram != null) {
                    buffer.append("<br/><b>Highlight Mean:</b> " + highlightHistogram.getMean() + ", <b>2*StDev:</b> " + (2 * highlightHistogram.getStDev()));
                }
                buffer.append("</html>");
            } else {
                buffer.append("<html><b>Median:</b> " + histogram.getMedian() + ", <b>IQR:</b> " + histogram.getIQR());
                if (highlightHistogram != null) {
                    buffer.append("<br/><b>Highlight Median:</b> " + highlightHistogram.getMedian() + ", <b>IQR:</b> " +
                            (highlightHistogram.getPercentile75() - highlightHistogram.getPercentile25()));
                }
                buffer.append("</html>");
            }
            setToolTipText(buffer.toString());
        } else {
            mouseOverMinValueLabel = minLabelRectangle.contains(e.getPoint());
            mouseOverMaxValueLabel = maxLabelRectangle.contains(e.getPoint());
            mouseOverMaxCountLabel = countAxisMaxLabelBounds.contains(e.getPoint());
            mouseOverMinCountLabel = countAxisMinLabelBounds.contains(e.getPoint());
        }

        repaint();
    }

    public int getBinCount() {
        return binCount;
    }

    public void setBinCount(int binCount) {
        this.binCount = binCount;

        if (histogram != null) {
            histogram.setNumBins(binCount);
        }

        if (highlightHistogram != null) {
            highlightHistogram.setNumBins(binCount);
        }

        layoutPanel();
    }

    public STATISTICS_MODE getStatisticsMode () {
        return statisticsMode;
    }

    public void setHistogram (Histogram histogram) {
        this.histogram = histogram;
        this.histogram.setNumBins(binCount);
        this.countAxisMax = histogram.getMaxBinCount();
        log.debug("countAxisMax = " + countAxisMax);

//        maxOverallBinSize = 0.;
//        for (int i = 0; i < histogram.getNumBins(); i++) {
//            if (histogram.getBinCount(i) > maxOverallBinSize) {
//                maxOverallBinSize = histogram.getBinCount(i);
//            }
//        }

        layoutPanel();
    }



    public void setHighlightValues (double highlightValues[]) {
        highlightHistogram = new Histogram(histogram.getName(), highlightValues, histogram.getNumBins(),
                histogram.getMinValue(), histogram.getMaxValue());
//        this.highlightValues = Arrays.copyOf(highlightValues, highlightValues.length);
//
//        highlightBinCounts = new int[binCount];
//        Arrays.fill(highlightBinCounts, 0);
//
//        highlightStatistics = new DescriptiveStatistics(this.highlightValues);
//
//        double binValueRange = (histogram.getMaxValue() - histogram.getMinValue()) / binCount;
//        for (double highlightValue : highlightValues) {
//            int binIndex = (int) ((highlightValue - histogram.getMinValue()) / binValueRange);
//            if (binIndex >= highlightBinCounts.length) {
//                log.debug("Out of bounds with value: " + highlightValue + " max is " + histogram.getDescriptiveStats().getMax());
//                binIndex = highlightBinCounts.length - 1;
//            }
//            highlightBinCounts[binIndex]++;
//            if ((highlightValue < histogram.getDistributionStats().getBinStats().get(binIndex).getMin()) ||
//                    (highlightValue > histogram.getDistributionStats().getBinStats().get(binIndex).getMax())) {
//                log.debug("highlightValue: " + highlightValue + " binMax: " + histogram.getDistributionStats().getBinStats().get(binIndex).getMax() + " binMin: " + histogram.getDistributionStats().getBinStats().get(binIndex).getMin());
//            }
//        }

        layoutPanel();
    }

//    private double mapValue(double value, double currentMin, double currentMax, double newMin, double newMax) {
//        double norm = (value - currentMin) / (currentMax - currentMin);
//        return (norm * (newMax - newMin)) + newMin;
//    }

    private void layoutPanel() {
        if (histogram != null) {
            Graphics2D g2 = (Graphics2D)getGraphics();
            if (g2 == null) {
                return;
            }

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, DEFAULT_LABEL_FONT_SIZE));

            histogramBinRectangles = new ArrayList<>();
            histogramBinColors = new ArrayList<>();
            highlightHistogramBinRectangles = new ArrayList<>();
            highlightHistogramBinColors = new ArrayList<>();

            if (orientation == ORIENTATION.HORIZONTAL) {
                // horizontal histogram
                int plotTop = getInsets().top;
                int plotHeight = getHeight() - (getInsets().bottom + getInsets().top) - 1;
                int histogramPlotHeight = (int) (plotHeight - (SUMMARY_STATS_DEFAULT_SIZE + DEFAULT_RANGE_LABEL_HEIGHT));

                // layout the countAxisRectangle
                String labelString = String.valueOf(countAxisMax);
                int stringWidth = g2.getFontMetrics().stringWidth(labelString);
                countAxisRectangle = new Rectangle(getInsets().left, plotTop, stringWidth+4, histogramPlotHeight);

                int stringX = (countAxisRectangle.x + countAxisRectangle.width) - stringWidth - 2;
                countAxisMaxLabelBounds = new Rectangle(stringX, countAxisRectangle.y,
                        stringWidth, g2.getFontMetrics().getHeight());

                labelString = String.valueOf(countAxisMin);
                stringWidth = g2.getFontMetrics().stringWidth(labelString);
                stringX = (countAxisRectangle.x + countAxisRectangle.width) - stringWidth - 2;
                countAxisMinLabelBounds = new Rectangle(stringX,
                        (countAxisRectangle.y + countAxisRectangle.height) - g2.getFontMetrics().getHeight() - 2,
                        stringWidth, g2.getFontMetrics().getHeight());

                int plotLeft = countAxisRectangle.x + countAxisRectangle.width;
                int plotWidth = getWidth() - (getInsets().left + getInsets().right) - countAxisRectangle.width - 1;
                fullPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotHeight);

                histogramPlotRectangle = new Rectangle(plotLeft, fullPlotRectangle.y, plotWidth, histogramPlotHeight);
                binRectangleSize = (double) histogramPlotRectangle.width / (double) binCount;
                summaryStatsRectangle = new Rectangle(plotLeft, (histogramPlotRectangle.y + histogramPlotRectangle.height),
                        plotWidth, SUMMARY_STATS_DEFAULT_SIZE);
                valueRangeLabelsRectangle = new Rectangle(plotLeft, (summaryStatsRectangle.y + summaryStatsRectangle.height),
                        plotWidth, DEFAULT_RANGE_LABEL_HEIGHT);

                // calculate the max value label bounds
                labelString = String.valueOf(histogram.getMaxValue());
                stringWidth = g2.getFontMetrics().stringWidth(labelString);
                int strX = (valueRangeLabelsRectangle.x + valueRangeLabelsRectangle.width) - (stringWidth + 1);
                maxLabelRectangle = new Rectangle(strX, (valueRangeLabelsRectangle.y - 2), stringWidth,
                        valueRangeLabelsRectangle.height);

                // calculate the min value label bounds
                labelString = String.valueOf(histogram.getMinValue());
                stringWidth = g2.getFontMetrics().stringWidth(labelString);
                int strY = (valueRangeLabelsRectangle.y + valueRangeLabelsRectangle.height) - 4;
                minLabelRectangle = new Rectangle(valueRangeLabelsRectangle.x, (valueRangeLabelsRectangle.y - 2),
                        stringWidth, valueRangeLabelsRectangle.height);


                // calculate bin rectangles
                int binNumber = 0;
                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double x = histogramPlotRectangle.x + (binNumber * binRectangleSize);
                    double binHeight = GraphicsUtil.mapValue(histogram.getBinCount(i), countAxisMin, countAxisMax,
                            0., histogramPlotRectangle.height);
                    double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - binHeight;
                    Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binRectangleSize, binHeight);
                    Color binColor = Color.lightGray;
                    histogramBinRectangles.add(binRect);
                    histogramBinColors.add(binColor);

                    binNumber++;
                }

                // calculate highlighted bin rectangles
                if (highlightHistogram != null) {
                    for (int binIndex = 0; binIndex < binCount; binIndex++) {
                        double x = histogramPlotRectangle.x + (binIndex * binRectangleSize);
                        double binHeight = GraphicsUtil.mapValue(highlightHistogram.getBinCount(binIndex), countAxisMin,
                                countAxisMax, 0., histogramPlotRectangle.height);
                        double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - binHeight;
                        Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binRectangleSize, binHeight);
                        Color binColor = Color.gray;
                        highlightHistogramBinRectangles.add(binRect);
                        highlightHistogramBinColors.add(binColor);
                    }
                }

                double yPosition = summaryStatsRectangle.getMaxY() - ((summaryStatsRectangle.getMaxY() - summaryStatsRectangle.getMinY()) / 3.);

                // calculate mean position
                double meanX = GraphicsUtil.mapValue(histogram.getMean(), histogram.getMinValue(), histogram.getMaxValue(),
                        histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                meanShape = new Ellipse2D.Double(meanX-2., yPosition-2., 4., 4.);

                // calculate median position
                double medianX = GraphicsUtil.mapValue(histogram.getMedian(), histogram.getMinValue(), histogram.getMaxValue(),
                        histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                medianShape = new Ellipse2D.Double(medianX-2., yPosition-2., 4., 4.);

                // calculate standard deviation range
                double stdevRangeLeft = GraphicsUtil.mapValue(histogram.getMean() - histogram.getStDev(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                if (stdevRangeLeft < histogramPlotRectangle.x) {
                    stdevRangeLeft = histogramPlotRectangle.x;
                }
                double stdevRangeRight = GraphicsUtil.mapValue(histogram.getMean() + histogram.getStDev(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                if (stdevRangeRight > histogramPlotRectangle.getMaxX()) {
                    stdevRangeRight = histogramPlotRectangle.getMaxX();
                }
                stdevRangeShape = new Line2D.Double(stdevRangeLeft, yPosition, stdevRangeRight, yPosition);

                // calculate IQR
                double IQRLeft = GraphicsUtil.mapValue(histogram.getPercentile25(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                if (IQRLeft < histogramPlotRectangle.x) {
                    IQRLeft = histogramPlotRectangle.x;
                }
                double IQRRight = GraphicsUtil.mapValue(histogram.getPercentile75(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                if (IQRRight > histogramPlotRectangle.getMaxX()) {
                    IQRRight = histogramPlotRectangle.getMaxX();
                }
                IQRShape = new Line2D.Double(IQRLeft, yPosition, IQRRight, yPosition);

                // calculate shapes for highlight statistics
                if (highlightHistogram != null) {
                    yPosition = summaryStatsRectangle.getMinY() + ((summaryStatsRectangle.getMaxY() - summaryStatsRectangle.getMinY()) / 3.);

                    // calculate mean position
                    double highlightMeanX = GraphicsUtil.mapValue(highlightHistogram.getMean(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                    highlightMeanShape = new Ellipse2D.Double(highlightMeanX-2., yPosition-2., 4., 4.);

                    // calculate median position
                    double highlightMedianX = GraphicsUtil.mapValue(highlightHistogram.getMedian(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.getMaxX());
                    highlightMedianShape = new Ellipse2D.Double(highlightMedianX-2., yPosition-2., 4., 4.);

                    // calculate standard deviation range
                    double highlightStdevRangeLeft = GraphicsUtil.mapValue(highlightHistogram.getMean() - highlightHistogram.getStDev(),
                            histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(),
                            histogramPlotRectangle.width);
                    if (highlightStdevRangeLeft < histogramPlotRectangle.x) {
                        highlightStdevRangeLeft = histogramPlotRectangle.x;
                    }
                    double highlightStdevRangeRight = GraphicsUtil.mapValue(highlightHistogram.getMean() + highlightHistogram.getStDev(),
                            histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMinX(),
                            histogramPlotRectangle.width);
                    if (highlightStdevRangeRight > histogramPlotRectangle.getMaxX()) {
                        highlightStdevRangeRight = histogramPlotRectangle.getMaxX();
                    }
                    highlightStdevRangeShape = new Line2D.Double(highlightStdevRangeLeft, yPosition, highlightStdevRangeRight, yPosition);

                    // calculate IQR
                    double highlightIQRLeft = GraphicsUtil.mapValue(highlightHistogram.getPercentile25(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                    if (highlightIQRLeft < histogramPlotRectangle.x) {
                        highlightIQRLeft = histogramPlotRectangle.x;
                    }
                    double highlightIQRRight = GraphicsUtil.mapValue(highlightHistogram.getPercentile75(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMinX(), histogramPlotRectangle.width);
                    if (highlightIQRRight > histogramPlotRectangle.getMaxX()) {
                        highlightIQRRight = histogramPlotRectangle.getMaxX();
                    }
                    highlightIQRShape = new Line2D.Double(highlightIQRLeft, yPosition, highlightIQRRight, yPosition);
                }
            } else {
                // vertical histogram
                int plotLeft = getInsets().left;
                int plotTop = getInsets().top;
                int plotHeight = getHeight() - (getInsets().bottom + getInsets().top) - g2.getFontMetrics().getHeight() - 1;
                int plotWidth = getWidth() - (getInsets().left + getInsets().right) - 1;
                fullPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotHeight);

                int histogramPlotWidth = (int)(fullPlotRectangle.width - (SUMMARY_STATS_DEFAULT_SIZE + DEFAULT_RANGE_LABEL_HEIGHT));

                valueRangeLabelsRectangle = new Rectangle(plotLeft, plotTop, DEFAULT_RANGE_LABEL_HEIGHT,
                        fullPlotRectangle.height);
                summaryStatsRectangle = new Rectangle((valueRangeLabelsRectangle.x + valueRangeLabelsRectangle.width),
                        plotTop, SUMMARY_STATS_DEFAULT_SIZE, fullPlotRectangle.height);
                histogramPlotRectangle = new Rectangle((summaryStatsRectangle.x + summaryStatsRectangle.width),
                        plotTop, histogramPlotWidth, fullPlotRectangle.height);

                // layout the countAxisRectangle
                countAxisRectangle = new Rectangle(histogramPlotRectangle.x, (histogramPlotRectangle.y + histogramPlotRectangle.height),
                        histogramPlotRectangle.width, g2.getFontMetrics().getHeight());

                String string = String.valueOf(countAxisMin);
                int stringWidth = g2.getFontMetrics().stringWidth(string);
                countAxisMinLabelBounds = new Rectangle(countAxisRectangle.x + 1, (countAxisRectangle.y - 2),
                        stringWidth, g2.getFontMetrics().getHeight());

                string = String.valueOf(countAxisMax);
                stringWidth = g2.getFontMetrics().stringWidth(string);
                countAxisMaxLabelBounds = new Rectangle((countAxisRectangle.x + countAxisRectangle.width) - stringWidth - 1,
                        (countAxisRectangle.y - 2), stringWidth, g2.getFontMetrics().getHeight());

                binRectangleSize = (double) histogramPlotRectangle.height / (double) binCount;

                // calculate bin rectangles
                int binNumber = 0;
                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - ((binNumber + 1) * binRectangleSize);
                    double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), countAxisMin, countAxisMax, 0., histogramPlotRectangle.width);
                    double x = histogramPlotRectangle.x;
                    Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binWidth, binRectangleSize);
                    Color binColor = Color.lightGray;
                    histogramBinRectangles.add(binRect);
                    histogramBinColors.add(binColor);

                    binNumber++;
                }

                if (highlightHistogram != null) {
                    // calculate highlight histogram bin rectangles
                    for (int binIndex = 0; binIndex < binCount; binIndex++) {
                        double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - ((binIndex + 1) * binRectangleSize);
                        double binWidth = GraphicsUtil.mapValue(highlightHistogram.getBinCount(binIndex), countAxisMin,
                                countAxisMax, 0., histogramPlotRectangle.width);
                        double x = histogramPlotRectangle.x;
                        Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binWidth, binRectangleSize);
                        Color binColor = Color.gray;
                        highlightHistogramBinRectangles.add(binRect);
                        highlightHistogramBinColors.add(binColor);
                    }
                }

                double xPosition = summaryStatsRectangle.getMinX() + ((summaryStatsRectangle.getMaxX() - summaryStatsRectangle.getMinX()) / 3.);

                // calculate mean position
                double meanY = GraphicsUtil.mapValue(histogram.getMean(), histogram.getMinValue(),
                        histogram.getMaxValue(),
                        histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                meanShape = new Ellipse2D.Double(xPosition-2., meanY-2., 4., 4.);

                // calculate median position
                double medianY = GraphicsUtil.mapValue(histogram.getMedian(), histogram.getMinValue(),
                        histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                medianShape = new Ellipse2D.Double(xPosition-2., medianY-2., 4., 4.);

                // calculate standard deviation range
                double stdevRangeLower = GraphicsUtil.mapValue(histogram.getMean() - histogram.getStDev(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                double stdevRangeUpper = GraphicsUtil.mapValue(histogram.getMean() + histogram.getStDev(),
                        histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMaxY(),
                        histogramPlotRectangle.getMinY());
                stdevRangeShape = new Line2D.Double(xPosition, stdevRangeLower, xPosition, stdevRangeUpper);

                // calculate IQR
                double IQRLower = GraphicsUtil.mapValue(histogram.getPercentile25(), histogram.getMinValue(),
                        histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                double IQRUpper = GraphicsUtil.mapValue(histogram.getPercentile75(), histogram.getMinValue(),
                        histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                IQRShape = new Line2D.Double(xPosition, IQRLower, xPosition, IQRUpper);

                // calculate shapes for highlight statistics
                if (highlightHistogram != null) {
                    xPosition = summaryStatsRectangle.getMaxX() - ((summaryStatsRectangle.getMaxX() - summaryStatsRectangle.getMinX()) / 3.);

                    // calculate mean position
                    double highlightMeanY = GraphicsUtil.mapValue(highlightHistogram.getMean(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                    highlightMeanShape = new Ellipse2D.Double(xPosition-2., highlightMeanY-2., 4., 4.);

                    // calculate median position
                    double highlightMedianY = GraphicsUtil.mapValue(highlightHistogram.getMedian(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                    highlightMedianShape = new Ellipse2D.Double(xPosition-2., highlightMedianY-2., 4., 4.);

                    // calculate standard deviation range
                    double highlightStdevRangeLower = GraphicsUtil.mapValue(highlightHistogram.getMean() - highlightHistogram.getStDev(),
                            histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMaxY(),
                            histogramPlotRectangle.getMinY());
                    double highlightStdevRangeUpper = GraphicsUtil.mapValue(highlightHistogram.getMean() + highlightHistogram.getStDev(),
                            histogram.getMinValue(), histogram.getMaxValue(), histogramPlotRectangle.getMaxY(),
                            histogramPlotRectangle.getMinY());
                    highlightStdevRangeShape = new Line2D.Double(xPosition, highlightStdevRangeLower, xPosition, highlightStdevRangeUpper);

                    // calculate IQR
                    double highlightIQRLower = GraphicsUtil.mapValue(highlightHistogram.getPercentile25(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                    double highlightIQRUpper = GraphicsUtil.mapValue(highlightHistogram.getPercentile75(), histogram.getMinValue(),
                            histogram.getMaxValue(), histogramPlotRectangle.getMaxY(), histogramPlotRectangle.getMinY());
                    highlightIQRShape = new Line2D.Double(xPosition, highlightIQRLower, xPosition, highlightIQRUpper);
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
        g2.fillRect(0, 0, getWidth(), getHeight());
//        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (histogram != null) {
//            if (valueRangeLabelsRectangle != null) {
//                g2.setColor(Color.blue);
//                g2.draw(valueRangeLabelsRectangle);
//            }

            // draw histogram bins
            if (histogramBinRectangles != null) {
                for (int i = 0; i < histogramBinRectangles.size(); i++) {
                    Rectangle2D binRect = histogramBinRectangles.get(i);
                    g2.setColor(histogramBinColors.get(i));
                    g2.fill(binRect);
                    g2.setColor(Color.darkGray);
                    g2.draw(binRect);
                }
            } else {
                return;
            }

            // draw highlight histogram bins
            if (highlightHistogramBinRectangles != null) {
                for (int i = 0; i < highlightHistogramBinRectangles.size(); i++) {
                    Rectangle2D binRect = highlightHistogramBinRectangles.get(i);
                    g2.setColor(highlightHistogramBinColors.get(i).darker());
                    g2.fill(binRect);
                    g2.setColor(Color.black);
                    g2.draw(binRect);
                }
            }

            g2.setStroke(new BasicStroke(2.f));

            if (hoverBinRect != null) {
                g2.setColor(Color.ORANGE);
                g2.draw(hoverBinRect);
            }

            g2.setColor(Color.GRAY);
            if (statisticsMode == STATISTICS_MODE.MEAN_BASED) {
                if (meanShape != null) {
                    g2.fill(meanShape);
                    g2.draw(meanShape);
                }

                if (stdevRangeShape != null) {
                    g2.draw(stdevRangeShape);
                }

                g2.setColor(Color.DARK_GRAY);
                if (highlightMeanShape != null) {
                    g2.fill(highlightMeanShape);
                    g2.draw(highlightMeanShape);
                }

                if (highlightStdevRangeShape != null) {
                    g2.draw(highlightStdevRangeShape);
                }
            } else {
                if (medianShape != null) {
                    g2.fill(medianShape);
                    g2.draw(medianShape);
                }

                if (IQRShape != null) {
                    g2.draw(IQRShape);
                }

                g2.setColor(Color.DARK_GRAY);
                if (highlightMedianShape != null) {
                    g2.fill(highlightMedianShape);
                    g2.draw(highlightMedianShape);
                }

                if (highlightIQRShape != null) {
                    g2.draw(highlightIQRShape);
                }
            }

            // draw min/max labels
            if (orientation == ORIENTATION.HORIZONTAL) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, DEFAULT_LABEL_FONT_SIZE));


                g2.setColor(Color.darkGray);

                // draw line to mark min and max extents
//                g2.drawLine(fullPlotRectangle.x, summaryStatsRectangle.y, fullPlotRectangle.x,
//                        (fullPlotRectangle.y + fullPlotRectangle.height));
//                g2.drawLine((fullPlotRectangle.x + fullPlotRectangle.width), summaryStatsRectangle.y,
//                        (fullPlotRectangle.x + fullPlotRectangle.width), (fullPlotRectangle.y + fullPlotRectangle.height));

                // draw the minimum value string
                String minValueString = DECIMAL_FORMATTER.format(histogram.getMinValue());
//                int strWidth = g2.getFontMetrics().stringWidth(minValueString);
//                int strY = (valueRangeLabelsRectangle.y + valueRangeLabelsRectangle.height) - 4;
//                minLabelRectangle = new Rectangle(valueRangeLabelsRectangle.x, valueRangeLabelsRectangle.y, strWidth, valueRangeLabelsRectangle.height);
                if (mouseOverMinValueLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(minValueString, minLabelRectangle.x, minLabelRectangle.y + minLabelRectangle.height);
//                g2.drawString(minValueString, valueRangeLabelsRectangle.x + 1, strY);

                // draw the maximum value string
                String maxValueString = DECIMAL_FORMATTER.format(histogram.getMaxValue());
//                strWidth = g2.getFontMetrics().stringWidth(maxValueString);
//                int strX = (valueRangeLabelsRectangle.x + valueRangeLabelsRectangle.width) - (strWidth + 1);
//                maxLabelRectangle = new Rectangle(strX, valueRangeLabelsRectangle.y, strWidth, valueRangeLabelsRectangle.height);
                if (mouseOverMaxValueLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(maxValueString, maxLabelRectangle.x, maxLabelRectangle.y + minLabelRectangle.height);
//                g2.drawString(maxValueString, strX, strY);

                // draw the count axis max value (upper limit)
                String maxCountString = String.valueOf(countAxisMax);
//                int strWidth = g2.getFontMetrics().stringWidth(maxCountString);
//                int strX = (int) (countAxisMaxLabelBounds.getMaxX() - (strWidth + 2));
                if (mouseOverMaxCountLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(maxCountString, countAxisMaxLabelBounds.x, countAxisMaxLabelBounds.y + countAxisMaxLabelBounds.height);
//                g2.drawString(maxCountString, strX, countAxisMaxLabelBounds.y + countAxisMaxLabelBounds.height);
//                g2.draw(countAxisMaxLabelBounds);

                // draw the count axis min value (lower limit)
                String minCountString = String.valueOf(countAxisMin);
//                strWidth = g2.getFontMetrics().stringWidth(minCountString);
//                strX = (int) (countAxisMinLabelBounds.getMaxX() - (strWidth + 2));
                if (mouseOverMinCountLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(minCountString, countAxisMinLabelBounds.x, countAxisMinLabelBounds.y + countAxisMinLabelBounds.height);
//                g2.drawString(minCountString, strX, countAxisMinLabelBounds.y + countAxisMinLabelBounds.height);
//                g2.draw(countAxisMinLabelBounds);

                g2.setColor(Color.gray);
                g2.drawLine(histogramPlotRectangle.x, histogramPlotRectangle.y, histogramPlotRectangle.x,
                        histogramPlotRectangle.y + histogramPlotRectangle.height);


//                g2.setColor(Color.orange);
//                g2.draw(countAxisRectangle);
//                g2.setColor(Color.blue);
//                g2.draw(fullPlotRectangle);
//                g2.draw(maxLabelRectangle);
//                g2.draw(minLabelRectangle);
            } else if (orientation == ORIENTATION.VERTICAL) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, DEFAULT_LABEL_FONT_SIZE));
                g2.setColor(Color.darkGray);

                // draw the maximum value string
                String maxValueString = DECIMAL_FORMATTER.format(histogram.getMaxValue());
                int strWidth = g2.getFontMetrics().stringWidth(maxValueString);
                int strX = fullPlotRectangle.x + 4;
                int strY = fullPlotRectangle.y + 1;
                maxLabelRectangle = new Rectangle(valueRangeLabelsRectangle.x, valueRangeLabelsRectangle.y, valueRangeLabelsRectangle.width, strWidth);
                g2.rotate(Math.toRadians(90.), strX, strY);
                if (mouseOverMaxValueLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(maxValueString, strX, strY);
                g2.rotate(-Math.toRadians(90.), strX, strY);

                // draw the minimum value string;
                String minValueString = DECIMAL_FORMATTER.format(histogram.getMinValue());
                strWidth = g2.getFontMetrics().stringWidth(minValueString);
                strY = (fullPlotRectangle.y + fullPlotRectangle.height) - (strWidth + 1);
                minLabelRectangle = new Rectangle(valueRangeLabelsRectangle.x, strY, valueRangeLabelsRectangle.width, strWidth);
                g2.rotate(Math.toRadians(90.), strX, strY);
                if (mouseOverMinValueLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(minValueString, strX, strY);
                g2.rotate(-Math.toRadians(90.), strX, strY);

//
//                if (countAxisRectangle != null) {
//                    g2.setColor(Color.blue);
//                    g2.draw(countAxisRectangle);
//                }

                // draw the count axis max value (upper limit)
                String maxCountString = String.valueOf(countAxisMax);
                if (mouseOverMaxCountLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(maxCountString, countAxisMaxLabelBounds.x, countAxisMaxLabelBounds.y + countAxisMaxLabelBounds.height);
//                g2.draw(countAxisMaxLabelBounds);

                // draw the count axis min value (lower limit)
                String minCountString = String.valueOf(countAxisMin);
                if (mouseOverMinCountLabel) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.darkGray);
                }
                g2.drawString(minCountString, countAxisMinLabelBounds.x, countAxisMinLabelBounds.y + countAxisMinLabelBounds.height);
//                g2.draw(countAxisMinLabelBounds);

                g2.setColor(Color.gray);
                g2.drawLine(countAxisRectangle.x, countAxisRectangle.y,
                        countAxisRectangle.x + countAxisRectangle.width,
                        countAxisRectangle.y);

//                g2.setColor(Color.orange);
//                g2.draw(countAxisRectangle);
//                g2.setColor(Color.blue);
//                g2.draw(fullPlotRectangle);

//                g2.setColor(Color.blue);
//                g2.draw(fullPlotRectangle);
//                g2.draw(maxLabelRectangle);
//                g2.draw(minLabelRectangle);
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
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {}

    public enum ORIENTATION {VERTICAL, HORIZONTAL}

    public enum STATISTICS_MODE {MEAN_BASED, MEDIAN_BASED}
}
