package gov.ornl.csed.cda.histogram;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by csg on 1/5/16.
 */
public class Histogram {
    private final static Logger log = LoggerFactory.getLogger(Histogram.class);

    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0#######");

    public final static float DEFAULT_NAME_FONT_SIZE = 12f;
    public final static float DEFAULT_VALUE_FONT_SIZE = 10f;

    public final static Color DEFAULT_AXIS_COLOR = new Color(120,120,120);
    public final static Color DEFAULT_DISPERSION_BOX_FILL_COLOR = new Color(220, 220, 220);
    public final static Color DEFAULT_QUERY_DISPERSION_BOX_FILL_COLOR = new Color(180, 180, 220, 150);
    public final static Color DEFAULT_DISPERSION_BOX_LINE_COLOR = new Color(107, 174, 214);
    public final static Color DEFAULT_QUERY_DISPERSION_BOX_LINE_COLOR = new Color(8, 81, 156);

    public final static Color DEFAULT_HISTOGRAM_FILL_COLOR = new Color(140, 140, 150);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL_COLOR = new Color(60, 60, 70);

    private String name;
    private double values[];
    private int binCount;

    public Rectangle fullPlotRectangle;
    public Rectangle nameRectangle;
    public Rectangle dragHandleRectangle;
    public Rectangle drawableRegionRectangle;

    int nameLabelHeight = 16;
    int infoBarHeight = 14;

    private EmpiricalDistribution distributionStats;
    private DescriptiveStatistics descriptiveStats;

    private BufferedImage dragHandleImage;
    private RoundRectangle2D dragHandleRoundRectangle;
    private Rectangle upperPlotLabelBarRectangle;
    private int axisPlotDividerLinePosition;
    private Rectangle histogramPlotRegionRectangle;
    private int plotLeft;
    private int plotRight;
    private int meanPosition;
    private int medianPosition;
    private Rectangle standardDeviationRectangle;
    private Rectangle IQRRectangle;
    private Rectangle minValueLabelRectangle;
    private Rectangle maxValueLabelRectangle;

    private Rectangle histogramRectangle;
    private ArrayList<Rectangle2D.Double> histogramBinRectangles;
    private ArrayList<Color> histogramBinColors;

    public Histogram(String name, double values[], int binCount) {
        this.name = name;
        this.values = values;
        this.binCount = binCount;
        dragHandleImage = createDragHandleImage();
        calculateStatistics();
    }

    public Histogram(String name, Collection<Double> valueCollection, int binCount) {
        values = new double[valueCollection.size()];
        int i = 0;
        for (double value : valueCollection) {
            values[i++] = value;
        }

        this.name = name;
        this.binCount = binCount;
        dragHandleImage = createDragHandleImage();
        calculateStatistics();
    }

    public EmpiricalDistribution getDistributionStats() {
        return distributionStats;
    }

    public DescriptiveStatistics getDescriptiveStats() {
        return descriptiveStats;
    }

    public void setBinCount (int binCount) {
        this.binCount = binCount;
        calculateStatistics();
    }

    private static BufferedImage createDragHandleImage() {
        Color lightColor = new Color(255,255,255);
        Color darkColor = new Color(147,147,147);

        BufferedImage image = new BufferedImage(6, 17, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(2, 1, lightColor.getRGB());
        image.setRGB(2, 4, lightColor.getRGB());
        image.setRGB(2, 7, lightColor.getRGB());
        image.setRGB(2, 10, lightColor.getRGB());
        image.setRGB(2, 13, lightColor.getRGB());

        image.setRGB(4, 2, lightColor.getRGB());
        image.setRGB(4, 5, lightColor.getRGB());
        image.setRGB(4, 8, lightColor.getRGB());
        image.setRGB(4, 11, lightColor.getRGB());
        image.setRGB(4, 14, lightColor.getRGB());

        image.setRGB(1, 2, darkColor.getRGB());
        image.setRGB(1, 5, darkColor.getRGB());
        image.setRGB(1, 8, darkColor.getRGB());
        image.setRGB(1, 11, darkColor.getRGB());
        image.setRGB(1, 14, darkColor.getRGB());

        image.setRGB(3, 3, darkColor.getRGB());
        image.setRGB(3, 6, darkColor.getRGB());
        image.setRGB(3, 9, darkColor.getRGB());
        image.setRGB(3, 12, darkColor.getRGB());
        image.setRGB(3, 15, darkColor.getRGB());
        return image;
    }

    public String getName() {
        return name;
    }

    public void calculateStatistics() {
//        double data[] = new double[column.getRowCount()];
//        for (int i = 0; i < data.length; i++) {
//            data[i] = column.getDouble(i);
//        }

        distributionStats = new EmpiricalDistribution(binCount);
        distributionStats.load(values);
        descriptiveStats = new DescriptiveStatistics(values);

    }

    public void layoutAxis(int width, int height) {
        fullPlotRectangle = new Rectangle(0, 0, width, height);

        dragHandleRectangle = new Rectangle(fullPlotRectangle.x, fullPlotRectangle.y, dragHandleImage.getWidth() + 4,
                fullPlotRectangle.height);
        dragHandleRoundRectangle = new RoundRectangle2D.Double(dragHandleRectangle.getX(), dragHandleRectangle.getY(),
                dragHandleRectangle.getWidth(), dragHandleRectangle.getHeight(), 6., 6.);

        nameRectangle = new Rectangle(dragHandleRectangle.x + dragHandleRectangle.width + 4,
                fullPlotRectangle.y,
                fullPlotRectangle.width - (dragHandleRectangle.width + 4), nameLabelHeight);

        drawableRegionRectangle = new Rectangle(nameRectangle.x, nameRectangle.y + nameRectangle.height,
                nameRectangle.width, dragHandleRectangle.height - nameRectangle.height);

        plotLeft = drawableRegionRectangle.x;
        plotRight = drawableRegionRectangle.x + drawableRegionRectangle.width;

        upperPlotLabelBarRectangle = new Rectangle(plotLeft, nameRectangle.y+nameRectangle.height,
                plotRight-plotLeft, infoBarHeight);

        int axisPlotTopPosition = upperPlotLabelBarRectangle.y + upperPlotLabelBarRectangle.height;
        int axisPlotBottomPosition = drawableRegionRectangle.y + drawableRegionRectangle.height;
        int axisPlotHeight = axisPlotBottomPosition - axisPlotTopPosition;
//        int axisPlotHeightHalf = axisPlotHeight/2;
        int axisPlotHeightThird = axisPlotHeight/3;
        axisPlotDividerLinePosition = axisPlotBottomPosition - axisPlotHeightThird;

        histogramPlotRegionRectangle = new Rectangle(upperPlotLabelBarRectangle.x, axisPlotTopPosition,
                upperPlotLabelBarRectangle.width, axisPlotHeight);

        // calculate mean and median locations
        meanPosition = valueToY(descriptiveStats.getMean(), false);
        medianPosition = valueToY(descriptiveStats.getPercentile(0.5), false);

        // calculate 2 standard deviation rectangle centered on mean value
        double lowValue = descriptiveStats.getMean() - descriptiveStats.getStandardDeviation();
        int boxLeft = valueToY(lowValue, true);
        double highValue = descriptiveStats.getMean() + descriptiveStats.getStandardDeviation();
        int boxRight = valueToY(highValue, true);
        standardDeviationRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                boxRight-boxLeft, axisPlotHeightThird);

        // calculate IQR range box
        lowValue = descriptiveStats.getPercentile(0.25);
        boxLeft = valueToY(lowValue, true);
        highValue = descriptiveStats.getPercentile(0.75);
        boxRight = valueToY(highValue, true);
        IQRRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition, boxRight-boxLeft, axisPlotHeightThird);


        // calculate histogram bin rectangles
//        log.debug("Column " + name);
//        log.debug("bin count = " + binCount + " max: " + distributionStats.getSampleStats().getN());
//        int counter = 0;
        histogramRectangle = new Rectangle(histogramPlotRegionRectangle.x, axisPlotTopPosition,
                histogramPlotRegionRectangle.width, axisPlotDividerLinePosition - axisPlotTopPosition);
        histogramBinRectangles = new ArrayList<Rectangle2D.Double>();
        histogramBinColors = new ArrayList<Color>();
        double binRectangleWidth = (double) histogramRectangle.width / (double) binCount;

        double maxN = 0.;
        for (SummaryStatistics summaryStatistics : distributionStats.getBinStats()) {
            if (summaryStatistics.getN() > maxN) {
                maxN = summaryStatistics.getN();
            }
        }

//        log.debug("maxN " + maxN);
        int binNumber = 0;
        for (SummaryStatistics summaryStatistics : distributionStats.getBinStats()) {
            double x = histogramRectangle.x + (binNumber * binRectangleWidth);
            double normCount = summaryStatistics.getN() / maxN;
            double binHeight = normCount * histogramRectangle.height;
            double y = (histogramRectangle.y + histogramRectangle.height) - binHeight;
            Rectangle2D.Double binRectangle = new Rectangle2D.Double(x, y, binRectangleWidth, binHeight);
            Color binColor = Color.gray;
            histogramBinRectangles.add(binRectangle);
            histogramBinColors.add(binColor);

//            log.debug("bin " + binNumber + " count = " + summaryStatistics.getN() + " [" + summaryStatistics.getMin() + ", " + summaryStatistics.getMax() + "]");
            binNumber++;
//            counter++;
        }

//        for (double upperBound : distributionStats.getUpperBounds()) {
//            log.debug("upperBound = " + upperBound);
//        }
    }

    public void draw (Graphics2D g2) {
        if (fullPlotRectangle == null) {
            return;
        }

//        g2.setColor(Color.blue);
//        g2.draw(fullPlotRectangle);

//        g2.setColor(Color.blue);
//        g2.draw(nameRectangle);

//        g2.setColor(Color.green);
//        g2.draw(drawableRegionRectangle);

        // draw drag handle
        g2.setColor(new Color(237, 237, 237));
        g2.fill(dragHandleRoundRectangle);
        int imageYOffset = (dragHandleRectangle.height - dragHandleImage.getHeight())/2;
        int imageXOffset = (dragHandleRectangle.width - dragHandleImage.getWidth()) / 2;
        g2.drawImage(dragHandleImage, dragHandleRectangle.x+imageXOffset, dragHandleRectangle.y+imageYOffset, null);

        // draw axis lines
        g2.setColor(DEFAULT_AXIS_COLOR);
        g2.setStroke(new BasicStroke(2.f));
        g2.drawLine(plotLeft-1, axisPlotDividerLinePosition, plotRight+1, axisPlotDividerLinePosition);
        g2.drawLine(plotLeft-1, upperPlotLabelBarRectangle.y, plotLeft-1, drawableRegionRectangle.y+drawableRegionRectangle.height);
        g2.drawLine(plotRight+1, upperPlotLabelBarRectangle.y, plotRight+1, drawableRegionRectangle.y + drawableRegionRectangle.height);

        // draw overall variable dispersion box and typical value
        g2.setStroke(new BasicStroke(1.f));
        g2.setColor(DEFAULT_DISPERSION_BOX_FILL_COLOR);
        g2.fill(standardDeviationRectangle);
        g2.setColor(DEFAULT_DISPERSION_BOX_LINE_COLOR);
        g2.draw(standardDeviationRectangle);
        g2.setStroke(new BasicStroke(3.f));
        g2.drawLine(meanPosition, standardDeviationRectangle.y, meanPosition,
                standardDeviationRectangle.y + standardDeviationRectangle.height);

        // draw overall histograms
        g2.setStroke(new BasicStroke(1.f));
        g2.setColor(DEFAULT_HISTOGRAM_FILL_COLOR);
        for (int i = 0; i < histogramBinRectangles.size(); i++) {
            Rectangle2D binRect = histogramBinRectangles.get(i);
            g2.fill(binRect);
        }

        // draw variable name
        g2.setFont(g2.getFont().deriveFont(DEFAULT_NAME_FONT_SIZE));
        g2.setColor(Color.black);
        g2.drawString(name, nameRectangle.x + 1,
                (nameRectangle.y + nameRectangle.height) - g2.getFontMetrics().getDescent());

        // calculate rectangle for min value string label
        g2.setFont(g2.getFont().deriveFont(DEFAULT_VALUE_FONT_SIZE));
        String minString = DECIMAL_FORMAT.format(descriptiveStats.getMin());
        int stringWidth = g2.getFontMetrics().stringWidth(minString);
        minValueLabelRectangle = new Rectangle(upperPlotLabelBarRectangle.x+2, upperPlotLabelBarRectangle.y, stringWidth, upperPlotLabelBarRectangle.height);

        // calculate rectangle for max value string label
        String maxString = DECIMAL_FORMAT.format(descriptiveStats.getMax());
        stringWidth = g2.getFontMetrics().stringWidth(maxString);
        maxValueLabelRectangle = new Rectangle((upperPlotLabelBarRectangle.x+ upperPlotLabelBarRectangle.width) - stringWidth - 2, upperPlotLabelBarRectangle.y, stringWidth, upperPlotLabelBarRectangle.height);

        // draw min and max value string labels
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, DEFAULT_VALUE_FONT_SIZE));
        g2.setColor(Color.darkGray);
        g2.drawString(minString, minValueLabelRectangle.x, minValueLabelRectangle.y + minValueLabelRectangle.height - 4);
        g2.drawString(maxString, maxValueLabelRectangle.x, maxValueLabelRectangle.y + maxValueLabelRectangle.height - 4);
    }

    private int valueToY(double value, boolean clamp) {
        double normValue = (value - descriptiveStats.getMin()) / (descriptiveStats.getMax() - descriptiveStats.getMin());
        int screenPosition = plotLeft + (int)(normValue * (plotRight - plotLeft));

        if (clamp) {
            screenPosition = screenPosition < plotLeft ? plotLeft : screenPosition;
            screenPosition = screenPosition > plotRight ? plotRight : screenPosition;
        }

        return screenPosition;
    }
}
