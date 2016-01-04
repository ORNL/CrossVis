package gov.ornl.csed.cda.hypervis;

import gov.ornl.csed.cda.coalesce.Utilities;
import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.ColumnSelection;
import gov.ornl.csed.cda.datatable.ColumnSelectionRange;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.SummaryStats;
import gov.ornl.csed.cda.pcvis.PCAxisSelection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by csg on 8/11/14.
 */
public class HyperVariateAxis {
    private final static Logger log = LoggerFactory.getLogger(HyperVariateAxis.class);

    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0#######");
    public final static float DEFAULT_VALUE_FONT_SIZE = 10f;
    public final static double PLOT_CONTEXT_PROPORTION = .05;

    public final static int DEFAULT_CORRELATION_INDICATOR_WIDTH = 10;
    public final static int DEFAULT_INFO_REGION_WIDTH = 80;

    public final static Color DEFAULT_AXIS_COLOR = new Color(120,120,120);
    public final static Color DEFAULT_HISTOGRAM_FILL_COLOR = new Color(140, 140, 150);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL_COLOR = new Color(60, 60, 70);

//    public final static Color DEFAULT_HISTOGRAM_FILL_COLOR = new Color(107, 174, 214);
//    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL_COLOR = new Color(8, 81, 156);

    public final static Color DEFAULT_DISPERSION_BOX_FILL_COLOR = new Color(220, 220, 220);
    public final static Color DEFAULT_QUERY_DISPERSION_BOX_FILL_COLOR = new Color(180, 180, 220, 150);
    public final static Color DEFAULT_DISPERSION_BOX_LINE_COLOR = new Color(107, 174, 214);
    public final static Color DEFAULT_QUERY_DISPERSION_BOX_LINE_COLOR = new Color(8, 81, 156);

    public Column column;
    public DataModel dataModel;

    public Rectangle fullPlotRectangle;
    public RoundRectangle2D fullPlotRoundedRectangle;
    public Rectangle drawableRegionRectangle;
    public Rectangle histogramPlotRegionRectangle;
    public Rectangle nameRectangle;
    public Rectangle minValueLabelRectangle;
    public Rectangle maxValueLabelRectangle;
    public Rectangle upperPlotLabelBarRectangle;
    public Rectangle overallHistogramRectangle;
    public Rectangle heatmapRectangle;
//    public Rectangle timeSeriesRectangle;
    public Rectangle pcpRectangle;

//    public Rectangle queryHistogramRectangle;
    public Rectangle overallStandardDeviationRectangle;
    public Rectangle queryStandardDeviationRectangle;
    public Rectangle overallIQRRectangle;
    public Rectangle queryIQRRectangle;
    public Rectangle overallIQRWhiskerRectangle;
    public Rectangle queryIQRWhiskerRectangle;
    public RoundRectangle2D dragHandleRoundRectangle;

    public int plotLeft;
    public int plotRight;
    public int medianPosition;
    public int queryMedianPosition;
    public int meanPosition;
    public int queryMeanPosition;
    public int axisPlotDividerLinePosition;

    public ArrayList<Rectangle2D.Double> overallHistogramBinRectangles;
    public ArrayList<Color> overallHistogramBinColors;
    public ArrayList<Rectangle2D.Double> queryHistogramBinRectangles;
    public ArrayList<Color> queryHistogramBinColors;

    public ArrayList<PCAxisSelection> axisSelectionList = new ArrayList<PCAxisSelection>();
    public Rectangle2D correlationIndicatorRectangle;
    public Rectangle2D queryCorrelationIndicatorRectangle;

    private BufferedImage dragHandleImage;
    public Rectangle dragHandleRectangle;

    public HyperVariateAxis(Column column, DataModel dataModel) {
        this.column = column;
        this.dataModel = dataModel;
        dragHandleImage = createDragHandle();
    }

    private static BufferedImage createDragHandle() {
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

        // code below draws a horizontal drag handle
//        BufferedImage image = new BufferedImage(17, 6, BufferedImage.TYPE_INT_ARGB);
//        image.setRGB(2, 1, lightColor.getRGB());
//        image.setRGB(5, 1, lightColor.getRGB());
//        image.setRGB(8, 1, lightColor.getRGB());
//        image.setRGB(11, 1, lightColor.getRGB());
//        image.setRGB(14, 1, lightColor.getRGB());
//
//        image.setRGB(1, 3, lightColor.getRGB());
//        image.setRGB(4, 3, lightColor.getRGB());
//        image.setRGB(7, 3, lightColor.getRGB());
//        image.setRGB(10, 3, lightColor.getRGB());
//        image.setRGB(13, 3, lightColor.getRGB());
//
//        image.setRGB(3, 2, darkColor.getRGB());
//        image.setRGB(6, 2, darkColor.getRGB());
//        image.setRGB(9, 2, darkColor.getRGB());
//        image.setRGB(12, 2, darkColor.getRGB());
//        image.setRGB(15, 2, darkColor.getRGB());
//
//        image.setRGB(2, 4, darkColor.getRGB());
//        image.setRGB(5, 4, darkColor.getRGB());
//        image.setRGB(8, 4, darkColor.getRGB());
//        image.setRGB(11, 4, darkColor.getRGB());
//        image.setRGB(14, 4, darkColor.getRGB());

        return image;
    }

    private int valueToAxisY(double value, boolean clamp) {
        double normValue = (value - column.getSummaryStats().getMin()) / (column.getSummaryStats().getMax() - column.getSummaryStats().getMin());
        int screenPosition = plotLeft + (int)(normValue * (plotRight - plotLeft));

        if (clamp) {
            screenPosition = screenPosition < plotLeft ? plotLeft : screenPosition;
            screenPosition = screenPosition > plotRight ? plotRight : screenPosition;
        }

        return screenPosition;
    }

    public BufferedImage draw () {
        BufferedImage image = new BufferedImage(fullPlotRectangle.width, fullPlotRectangle.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.translate(-fullPlotRectangle.x, -fullPlotRectangle.y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
        g2.fill(overallStandardDeviationRectangle);
        g2.setColor(DEFAULT_DISPERSION_BOX_LINE_COLOR);
        g2.draw(overallStandardDeviationRectangle);
        g2.setStroke(new BasicStroke(3.f));
        g2.drawLine(meanPosition, overallStandardDeviationRectangle.y, meanPosition,
                overallStandardDeviationRectangle.y + overallStandardDeviationRectangle.height);

        // draw overall histograms
        g2.setStroke(new BasicStroke(1.f));
        g2.setColor(DEFAULT_HISTOGRAM_FILL_COLOR);
        for (int i = 0; i < overallHistogramBinRectangles.size(); i++) {
            Rectangle2D binRect = overallHistogramBinRectangles.get(i);
            g2.fill(binRect);
        }

        // draw query details if a query is set
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            // draw query dispersion box and typical value
            g2.setStroke(new BasicStroke(1.f));
            g2.setColor(DEFAULT_QUERY_DISPERSION_BOX_FILL_COLOR);
            g2.fill(queryStandardDeviationRectangle);
            g2.setColor(DEFAULT_QUERY_DISPERSION_BOX_LINE_COLOR);
            g2.draw(queryStandardDeviationRectangle);
            g2.setStroke(new BasicStroke(3.f));
            g2.drawLine(queryMeanPosition, queryStandardDeviationRectangle.y, queryMeanPosition,
                    queryStandardDeviationRectangle.y + queryStandardDeviationRectangle.height);

            // draw query histograms
            g2.setStroke(new BasicStroke(1.f));
            g2.setColor(DEFAULT_QUERY_HISTOGRAM_FILL_COLOR);
            for (int i = 0; i < queryHistogramBinRectangles.size(); i++) {
                Rectangle2D binRect = queryHistogramBinRectangles.get(i);
                g2.fill(binRect);
            }
        }

        // calculate rectangle for min value string label
        g2.setFont(g2.getFont().deriveFont(DEFAULT_VALUE_FONT_SIZE));
        String valueString = DECIMAL_FORMAT.format(column.getSummaryStats().getMin());
        int stringWidth = g2.getFontMetrics().stringWidth(valueString);
        minValueLabelRectangle = new Rectangle(upperPlotLabelBarRectangle.x+2, upperPlotLabelBarRectangle.y, stringWidth, upperPlotLabelBarRectangle.height);

        // calculate rectangle for max value string label
        valueString = DECIMAL_FORMAT.format(column.getSummaryStats().getMax());
        stringWidth = g2.getFontMetrics().stringWidth(valueString);
        maxValueLabelRectangle = new Rectangle((upperPlotLabelBarRectangle.x+ upperPlotLabelBarRectangle.width) - stringWidth - 2,
                upperPlotLabelBarRectangle.y, stringWidth, upperPlotLabelBarRectangle.height);

        // draw the correlation indicators
        if (dataModel.getHighlightedColumn() != null) {
            int columnIndex = dataModel.getColumnIndex(column);
            double correlationCoefficient = dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(columnIndex);

            if (dataModel.getHighlightedColumn() != column) {
                Color fillColor = Utilities.getColorForCorrelationCoefficient(correlationCoefficient, 1.);
                g2.setColor(fillColor);
                g2.fill(correlationIndicatorRectangle);
                g2.setColor(DEFAULT_AXIS_COLOR);
                g2.draw(correlationIndicatorRectangle);
            }

            // if a query is set, fill the query correlation box
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                SummaryStats highlightedColumnQueryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(dataModel.getHighlightedColumn());

                correlationCoefficient = highlightedColumnQueryStats.getCorrelationCoefficients().get(columnIndex);

                if (dataModel.getHighlightedColumn() != column) {
                    Color fillColor = Utilities.getColorForCorrelationCoefficient(correlationCoefficient, 1.);
                    g2.setColor(fillColor);
                    g2.fill(queryCorrelationIndicatorRectangle);
                }


            }

            if (dataModel.getHighlightedColumn() != column) {
                g2.setColor(DEFAULT_AXIS_COLOR);
                g2.draw(queryCorrelationIndicatorRectangle);
            }
        } else {
            g2.setColor(Color.white);
            g2.fill(correlationIndicatorRectangle);
            g2.fill(queryCorrelationIndicatorRectangle);
//            g2.setStroke(new BasicStroke(2.f));
            g2.setColor(DEFAULT_AXIS_COLOR);
            g2.draw(correlationIndicatorRectangle);
            g2.draw(queryCorrelationIndicatorRectangle);
        }

//        g2.setColor(Color.blue);
////        g2.draw(infoRegionRectangle);
//        g2.draw(nameRectangle);
//        g2.draw(heatmapRectangle);
//        g2.setColor(Color.red);
//        g2.draw(pcpRectangle);
//
//        g2.draw(histogramPlotRegionRectangle);
//        g2.draw(upperPlotLabelBarRectangle);
//        if (timeSeriesRectangle != null) {
//            g2.draw(timeSeriesRectangle);
//        }
//        g2.draw(queryCorrelationIndicatorRectangle);
//        g2.draw(correlationIndicatorRectangle);
//
//        g2.setColor(Color.red);
//        g2.draw(drawableRegionRectangle);

        return image;
    }

    public void layoutAxis(Rectangle fullPlotRectangle, Insets margins, int nameLabelHeight, int labelBarHeight) {
        SummaryStats columnQueryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(column);

        this.fullPlotRectangle = fullPlotRectangle;
        fullPlotRoundedRectangle = new RoundRectangle2D.Double(fullPlotRectangle.getX(), fullPlotRectangle.getY(),
                fullPlotRectangle.getWidth(), fullPlotRectangle.getHeight(), 6., 6.);

        dragHandleRectangle = new Rectangle(fullPlotRectangle.x + margins.left,
                fullPlotRectangle.y + margins.top, dragHandleImage.getWidth() + 4,
                fullPlotRectangle.height - (margins.top + margins.bottom));

        dragHandleRoundRectangle = new RoundRectangle2D.Double(dragHandleRectangle.getX(), dragHandleRectangle.getY(),
                dragHandleRectangle.getWidth(), dragHandleRectangle.getHeight(), 6., 6.);

        nameRectangle = new Rectangle(dragHandleRectangle.x + dragHandleRectangle.width + 4,
                fullPlotRectangle.y + margins.top,
                fullPlotRectangle.width - (margins.left+margins.right) - (dragHandleRectangle.width+4),
                nameLabelHeight);

        drawableRegionRectangle = new Rectangle(nameRectangle.x, nameRectangle.y + nameRectangle.height,
                nameRectangle.width,
                dragHandleRectangle.height - nameRectangle.height);
//                fullPlotRectangle.height - (margins.top + margins.bottom) - nameRectangle.height);

        correlationIndicatorRectangle = new Rectangle2D.Double(drawableRegionRectangle.getMaxX() - DEFAULT_CORRELATION_INDICATOR_WIDTH,
                nameRectangle.y,
                DEFAULT_CORRELATION_INDICATOR_WIDTH,
                (drawableRegionRectangle.getHeight() + nameRectangle.getHeight()) /2.);
        queryCorrelationIndicatorRectangle = new Rectangle2D.Double(drawableRegionRectangle.getMaxX() - DEFAULT_CORRELATION_INDICATOR_WIDTH,
                correlationIndicatorRectangle.getMaxY(),
                DEFAULT_CORRELATION_INDICATOR_WIDTH,
                correlationIndicatorRectangle.getHeight());
//        correlationIndicatorRectangle = new Rectangle2D.Double(nameRectangle.x,
//                nameRectangle.y + nameRectangle.height + 2,
//                nameRectangle.width/2., nameLabelHeight-2);
//
//        queryCorrelationIndicatorRectangle = new Rectangle2D.Double(correlationIndicatorRectangle.getMaxX(),
//                correlationIndicatorRectangle.getY(),
//                correlationIndicatorRectangle.getWidth(),
//                nameLabelHeight-2);

//        infoRegionRectangle = new Rectangle(dragHandleRectangle.x+dragHandleRectangle.width + 4,
//                fullPlotRectangle.y + margins.top + nameLabelHeight + 2, DEFAULT_INFO_REGION_WIDTH,
//                fullPlotRectangle.height - (margins.top + margins.bottom) - (nameLabelHeight + 2));

        int heatmapSize = fullPlotRectangle.height - (margins.top + margins.bottom) - 4;
        heatmapRectangle = new Rectangle((int)correlationIndicatorRectangle.getX() - heatmapSize - 4,
                nameRectangle.y, heatmapSize, heatmapSize);

        pcpRectangle = new Rectangle(heatmapRectangle.x - heatmapSize - 4, heatmapRectangle.y, heatmapSize, heatmapSize);
//        heatmapRectangle = new Rectangle(drawableRegionRectangle.x + drawableRegionRectangle.width - drawableRegionRectangle.height,
//                drawableRegionRectangle.y, drawableRegionRectangle.height, drawableRegionRectangle.height);


//        plotLeft = infoRegionRectangle.x + infoRegionRectangle.width + 4;

        plotLeft = drawableRegionRectangle.x;

//        if (dataModel.getTimeColumn() != null) {
//            int middleWidth = (pcpRectangle.x - 4) - plotLeft;
////            plotRight = plotLeft + (middleWidth/2);
//            plotRight = plotLeft + (2*middleWidth/3);
//        } else {
            plotRight = pcpRectangle.x - 4;
//        }

        upperPlotLabelBarRectangle = new Rectangle(plotLeft, nameRectangle.y+nameRectangle.height,
                plotRight-plotLeft, labelBarHeight);

        // reset the min and max label rectangles to force their layout again in HyperPCPanel
        // note it cannot be computed here because we don't have font metrics to get string widths
        minValueLabelRectangle = null;
        maxValueLabelRectangle = null;

        int axisPlotTopPosition = upperPlotLabelBarRectangle.y + upperPlotLabelBarRectangle.height;
        int axisPlotBottomPosition = drawableRegionRectangle.y + drawableRegionRectangle.height;
        int axisPlotHeight = axisPlotBottomPosition - axisPlotTopPosition;
//        int axisPlotHeightHalf = axisPlotHeight/2;
        int axisPlotHeightThird = axisPlotHeight/3;
        axisPlotDividerLinePosition = axisPlotBottomPosition - axisPlotHeightThird;
//        axisPlotCenterPosition = axisPlotTopPosition + axisPlotHeightHalf;

        histogramPlotRegionRectangle = new Rectangle(upperPlotLabelBarRectangle.x, axisPlotTopPosition,
                upperPlotLabelBarRectangle.width, axisPlotHeight);

//        if (dataModel.getTimeColumn() != null) {
//            int timeSeriesPlotLeft = plotRight + 4;
//            int timeSeriesPlotRight = pcpRectangle.x - 4;
//            timeSeriesRectangle = new Rectangle(timeSeriesPlotLeft, axisPlotTopPosition,
//                    timeSeriesPlotRight - timeSeriesPlotLeft, axisPlotHeight);
//        }

//        // calculate the lower context region rectangle
//        double contextRegionWidth = histogramPlotRegionRectangle.width * PLOT_CONTEXT_PROPORTION;
//        lowerContextPlotRegionRectangle = new Rectangle2D.Double(histogramPlotRegionRectangle.getX(), histogramPlotRegionRectangle.getY(),
//                contextRegionWidth, histogramPlotRegionRectangle.getHeight());
//
//        // calculate the focus plot area rectangle
//        double focusRegionWidth = histogramPlotRegionRectangle.getWidth() - (2. * contextRegionWidth);
//        focusPlotRegionRectangle = new Rectangle2D.Double(lowerContextPlotRegionRectangle.getMaxX(), histogramPlotRegionRectangle.getY(),
//                focusRegionWidth, histogramPlotRegionRectangle.getHeight());
//
//        // calculate the upper context region rectangle
//        upperContextPlotRegionRectangle = new Rectangle2D.Double(focusPlotRegionRectangle.getMaxX(), histogramPlotRegionRectangle.getY(),
//                contextRegionWidth, histogramPlotRegionRectangle.getHeight());

        // calculate the mean position
        meanPosition = valueToAxisY(column.getSummaryStats().getMean(), false);

        // calculate the query mean position
        if (columnQueryStats != null) {
            queryMeanPosition = valueToAxisY(columnQueryStats.getMean(), false);
        }

        // calculate the median position
        medianPosition = valueToAxisY(column.getSummaryStats().getMedian(), false);

        // calculate the query median position
        if (columnQueryStats != null) {
            queryMedianPosition = valueToAxisY(columnQueryStats.getMedian(), false);
        }

        // calculate mean-centered standard deviation range box
        double lowValue = column.getSummaryStats().getMean() - column.getSummaryStats().getStandardDeviation();
        int boxLeft = valueToAxisY(lowValue, true);
        double highValue = column.getSummaryStats().getMean() + column.getSummaryStats().getStandardDeviation();
        int boxRight = valueToAxisY(highValue, true);
        overallStandardDeviationRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                boxRight-boxLeft, axisPlotHeightThird);
//        overallStandardDeviationRectangle = new Rectangle(boxLeft, axisPlotTopPosition + axisPlotHeightHalf,
//                boxRight-boxLeft, axisPlotHeightHalf);

        if (columnQueryStats != null) {
            lowValue = columnQueryStats.getMean() - columnQueryStats.getStandardDeviation();
            boxLeft = valueToAxisY(lowValue, true);
            highValue = columnQueryStats.getMean() + columnQueryStats.getStandardDeviation();
            boxRight = valueToAxisY(highValue, true);
            queryStandardDeviationRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                    boxRight-boxLeft, axisPlotHeightThird/2);
//            queryStandardDeviationRectangle = new Rectangle(boxLeft, axisPlotTopPosition+ axisPlotHeightHalf,
//                    boxRight-boxLeft, axisPlotHeightHalf/2);
        }

        // calculate IQR range box
        lowValue = column.getSummaryStats().getQuantile1();
        boxLeft = valueToAxisY(lowValue, true);
        highValue = column.getSummaryStats().getQuantile3();
        boxRight = valueToAxisY(highValue, true);
        overallIQRRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                boxRight-boxLeft, axisPlotHeightThird);
//        overallIQRRectangle = new Rectangle(boxLeft, axisPlotTopPosition+ axisPlotHeightHalf,
//                boxRight-boxLeft, axisPlotHeightHalf);

        // calculate Query IQR Range Box
        if (columnQueryStats != null) {
            lowValue = columnQueryStats.getQuantile1();
            boxLeft = valueToAxisY(lowValue, true);
            highValue = columnQueryStats.getQuantile3();
            boxRight = valueToAxisY(highValue, true);
            queryIQRRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                    boxRight-boxLeft, axisPlotHeightThird/2);
//            queryIQRRectangle = new Rectangle(boxLeft, axisPlotTopPosition+ axisPlotHeightHalf,
//                    boxRight-boxLeft, axisPlotHeightHalf/2);
        }

        // calculate IQR whiskers
        lowValue = column.getSummaryStats().getLowerWhisker();
        boxLeft = valueToAxisY(lowValue, true);
        highValue = column.getSummaryStats().getUpperWhisker();
        boxRight = valueToAxisY(highValue, true);
        overallIQRWhiskerRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                boxRight-boxLeft, axisPlotHeightThird);
//        overallIQRWhiskerRectangle = new Rectangle(boxLeft, axisPlotTopPosition+ axisPlotHeightHalf,
//                boxRight-boxLeft, axisPlotHeightHalf);

        // calculate Query IQR Whiskers
        if (columnQueryStats != null) {
            lowValue = columnQueryStats.getQuantile1();
            boxLeft = valueToAxisY(lowValue, true);
            highValue = columnQueryStats.getQuantile3();
            boxRight = valueToAxisY(highValue, true);
            queryIQRWhiskerRectangle = new Rectangle(boxLeft, axisPlotDividerLinePosition,
                    boxRight-boxLeft, axisPlotHeightThird/2);
//            queryIQRWhiskerRectangle = new Rectangle(boxLeft, axisPlotTopPosition+ axisPlotHeightHalf,
//                    boxRight-boxLeft, axisPlotHeightHalf/2);
        }

        // calculate histogram bin rectangles
        overallHistogramRectangle = new Rectangle(histogramPlotRegionRectangle.x, axisPlotTopPosition,
                histogramPlotRegionRectangle.width, axisPlotDividerLinePosition - axisPlotTopPosition);
        overallHistogramBinRectangles = new ArrayList<Rectangle2D.Double>();
        overallHistogramBinColors = new ArrayList<Color>();

        if (column.getSummaryStats().getHistogram() != null) {
            double freqData[] = column.getSummaryStats().getHistogram().getArray();
            double maxFreq = freqData[0];
            for (int i = 1; i < freqData.length; i++) {
                if (freqData[i] > maxFreq) {
                    maxFreq = freqData[i];
                }
            }

            double binRectangleWidth = (double) overallHistogramRectangle.width / (double) freqData.length;
            for (int i = 0; i < freqData.length; i++) {
                // calculate bin mapping frequency to height of bin rectangle (color will be constant)
                double x = overallHistogramRectangle.x + (i * binRectangleWidth);
                double normValue = freqData[i] / maxFreq;
                double binHeight = normValue * overallHistogramRectangle.height;
                double y = (overallHistogramRectangle.y + overallHistogramRectangle.height) - binHeight;
                Rectangle2D.Double binRectangle = new Rectangle2D.Double(x, y, binRectangleWidth, binHeight);
                Color binColor = Color.gray;
                overallHistogramBinRectangles.add(binRectangle);
                overallHistogramBinColors.add(binColor);

                // calculate bin rectangle and color
                //            double x = overallHistogramRectangle.x + (i * binRectangleWidth);
                //            double binHeight = overallHistogramRectangle.height;
                //            Rectangle2D.Double binRectangle = new Rectangle2D.Double(x, overallHistogramRectangle.y, binRectangleWidth, binHeight);
                //            overallHistogramBinRectangles.add(binRectangle);
                //            Color binColor = ColorUtil.getColorForValue(freqData[i]/maxFreq, DEFAULT_HISTOGRAM_HIGH_COLOR, DEFAULT_HISTOGRAM_LOW_COLOR);
                //            overallHistogramBinColors.add(binColor);
            }

            // calculate query histogram rectangles
            if (columnQueryStats != null) {
                //            queryHistogramRectangle = new Rectangle(drawableRegionRectangle.x,
                //                    axisPlotCenterPosition - (axisPlotHeightHalf/2),
                //                    drawableRegionRectangle.width, axisPlotHeightHalf/2);
                freqData = columnQueryStats.getHistogram().getArray();
                //            maxFreq = freqData[0];
                //            for (int i = 1; i < freqData.length; i++) {
                //                if (freqData[i] > maxFreq) {
                //                    maxFreq = freqData[i];
                //                }
                //            }
                queryHistogramBinRectangles = new ArrayList<Rectangle2D.Double>();
                queryHistogramBinColors = new ArrayList<Color>();
                binRectangleWidth = (double) overallHistogramRectangle.width / (double) freqData.length;

                for (int i = 0; i < freqData.length; i++) {
                    // calculate bin mapping frequency to height of bin rectangle (color will be constant)
                    double x = overallHistogramRectangle.x + (i * binRectangleWidth);
                    double normValue = freqData[i] / maxFreq;
                    double binHeight = normValue * overallHistogramRectangle.height;
                    double y = (overallHistogramRectangle.y + overallHistogramRectangle.height) - binHeight;
                    Rectangle2D.Double binRectangle = new Rectangle2D.Double(x, y, binRectangleWidth, binHeight);
                    Color binColor = Color.darkGray;
                    queryHistogramBinRectangles.add(binRectangle);
                    queryHistogramBinColors.add(binColor);

                    //                // calculate bin rectangle and color
                    //                double x = queryHistogramRectangle.x + (i * binRectangleWidth);
                    //                double binHeight = queryHistogramRectangle.height;
                    //                Rectangle2D.Double binRectangle = new Rectangle2D.Double(x, queryHistogramRectangle.y, binRectangleWidth, binHeight);
                    //                queryHistogramBinRectangles.add(binRectangle);
                    //                Color binColor = ColorUtil.getColorForValue(freqData[i]/maxFreq, DEFAULT_HISTOGRAM_HIGH_COLOR, DEFAULT_HISTOGRAM_LOW_COLOR);
                    //                queryHistogramBinColors.add(binColor);
                }
            }
        }

        axisSelectionList.clear();
        ColumnSelection columnSelection = dataModel.getActiveQuery().getColumnSelection(column);
        if (columnSelection != null) {
            for (ColumnSelectionRange selectionRange : columnSelection.getColumnSelectionRanges()) {
                PCAxisSelection axisSelection = new PCAxisSelection(selectionRange);
                // find min and max position of selection range and create new axis selection range object
                double normValue = (selectionRange.getMaxValue() - column.getSummaryStats().getMin()) / (column.getSummaryStats().getMax() - column.getSummaryStats().getMin());
                int maxPosition = histogramPlotRegionRectangle.x + (int) (normValue * histogramPlotRegionRectangle.width);
                axisSelection.setMaxPosition(maxPosition);
                normValue = (selectionRange.getMinValue() - column.getSummaryStats().getMin()) / (column.getSummaryStats().getMax() - column.getSummaryStats().getMin());
                int minPosition = histogramPlotRegionRectangle.x + (int) (normValue * histogramPlotRegionRectangle.width);
                axisSelection.setMinPosition(minPosition);
                axisSelectionList.add(axisSelection);
            }
        }
//        if (!axisSelectionList.isEmpty()) {
//            for (PCAxisSelection axisSelection : axisSelectionList) {
//                double normValue = (axisSelection.getColumnSelectionRange().getMaxValue() - column.getSummaryStats().getMin()) / (column.getSummaryStats().getMax() - column.getSummaryStats().getMin());
//                int maxPosition = histogramPlotRegionRectangle.x + (int) (normValue * histogramPlotRegionRectangle.width);
//                axisSelection.setMaxPosition(maxPosition);
//                normValue = (axisSelection.getColumnSelectionRange().getMinValue() - column.getSummaryStats().getMin()) / (column.getSummaryStats().getMax() - column.getSummaryStats().getMin());
//                int minPosition = histogramPlotRegionRectangle.x + (int) (normValue * histogramPlotRegionRectangle.width);
//                axisSelection.setMinPosition(minPosition);
//            }
//        }
//        overallHistogramRectangle = new Rectangle(drawableRegionRectangle.x, axisPlotTopPosition,
//                drawableRegionRectangle.width, axisPlotHeightHalf);
//        overallStandardDeviationRectangle = new Rectangle(drawableRegionRectangle.x,
//                axisPlotTopPosition+ axisPlotHeightHalf, drawableRegionRectangle.width, axisPlotHeightHalf);
    }
}
