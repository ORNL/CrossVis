package gov.ornl.csed.cda.hypervis;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.Tuple;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by csg on 2/26/15.
 */
public class BivariateSeries {
//    private final static Color DEFAULT_HIGH_COUNT_COLOR = new Color(230, 85, 13);
//    private final static Color DEFAULT_LOW_COUNT_COLOR = new Color(254, 230, 206);
    private final static Color DEFAULT_HIGH_COUNT_COLOR = new Color(0,0,0);
    private final static Color DEFAULT_LOW_COUNT_COLOR = new Color(250,250,250);
    private Column xColumn;
    private Column yColumn;
    private double xMinValue;
    private double xMaxValue;
    private double yMinValue;
    private double yMaxValue;
    private int numRows;
    private int numCols;
    private DataModel dataModel;
    private int width;
    private int height;
    private Insets insets;

    private Rectangle plotRectangle;
    private int binWidth;
    private int binHeight;

    private int[][] binCounts;
    private Rectangle[][] binRectangles;
    private Color[][] binColors;
    private int maxBinCount = 0;

    // PCP drawing items
//    private int pcAxisTop, pcAxisBottom, pcAxis0_X, pcAxis1_X;

    private Path2D.Double[][] pcpBinPaths;
    private ArrayList<Line2D.Double> pcpLines;
//    private int pcAxisHeight;

    public BivariateSeries(Column xColumn, Column yColumn, DataModel dataModel, int numRows, int numCols, int width,
                           int height, Insets insets) {
        this.xColumn = xColumn;
        this.yColumn = yColumn;
        this.dataModel = dataModel;
        this.numCols = numCols;
        this.numRows = numRows;
        this.width = width;
        this.height = height;
        this.insets = insets;

        binCounts = new int[numCols][numRows];
        for (int i = 0; i < numCols; i++) {
            Arrays.fill(binCounts[i], 0);
        }
        binColors = new Color[numCols][numRows];
        binRectangles = new Rectangle[numCols][numRows];
        pcpBinPaths = new Path2D.Double[numCols][numRows];

        layoutPlots();
        calculateBins();
//        calculatePCPLines();
    }

    public void calculatePCPLines() {
        int axis0ColumnIndex = dataModel.getColumnIndex(xColumn);
        int axis1ColumnIndex = dataModel.getColumnIndex(yColumn);

        double axis0MinValue = xColumn.getSummaryStats().getMin();
        double axis0MaxValue = xColumn.getSummaryStats().getMax();
        double axis1MinValue = yColumn.getSummaryStats().getMin();
        double axis1MaxValue = yColumn.getSummaryStats().getMax();

        ArrayList<Tuple> tuples;
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            tuples = dataModel.getQueriedTuples();
        } else {
            tuples = dataModel.getTuples();
        }

        pcpLines = new ArrayList<Line2D.Double>();

        for (Tuple tuple: tuples) {
            double value = tuple.getElement(axis0ColumnIndex);
            double norm = (value - axis0MinValue) / (axis0MaxValue - axis0MinValue);
//            double axis0_Y = pcAxisTop + ((1. - norm) * pcAxisHeight);
            double axis0_Y = plotRectangle.y + ((1. - norm) * plotRectangle.height);

            value = tuple.getElement(axis1ColumnIndex);
            norm = (value - axis1MinValue) / (axis1MaxValue - axis1MinValue);
            double axis1_Y = plotRectangle.y + ((1. - norm) * plotRectangle.height);

            Line2D.Double line = new Line2D.Double(plotRectangle.x, axis0_Y, plotRectangle.x + plotRectangle.width, axis1_Y);
            pcpLines.add(line);
        }
    }

    public void calculateBins() {
        maxBinCount = 0;
        int xColumnIndex = dataModel.getColumnIndex(xColumn);
        int yColumnIndex = dataModel.getColumnIndex(yColumn);

        ArrayList<Tuple> tuples;
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            tuples = dataModel.getQueriedTuples();
        } else {
            tuples = dataModel.getTuples();
        }

        for (Tuple tuple : tuples) {
            double xValue = tuple.getElement(xColumnIndex);
//            double norm = (xValue - xColumn.getSummaryStats().getMin()) / (xColumn.getSummaryStats().getMax() - xColumn.getSummaryStats().getMin());
            double norm = (xValue - xMinValue) / (xMaxValue - xMinValue);
            int col = (int)Math.floor(norm * numCols);

            double yValue = tuple.getElement(yColumnIndex);
//            norm = (yValue - yColumn.getSummaryStats().getMin()) / (yColumn.getSummaryStats().getMax() - yColumn.getSummaryStats().getMin());
            norm = (yValue - yMinValue) / (yMaxValue - yMinValue);
            int row = (int)((1. - norm) * numRows);

            binCounts[col][row]++;

            if (binCounts[col][row] > maxBinCount) {
                maxBinCount = binCounts[col][row];
            }
        }

        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                // calculate color of the bin
                float value = (float)binCounts[col][row] / maxBinCount;
//                Color binColor = ColorUtil.getColorForValue(value, DEFAULT_HIGH_COUNT_COLOR, DEFAULT_LOW_COUNT_COLOR);
                Color binColor = new Color(DEFAULT_HIGH_COUNT_COLOR.getRed(), DEFAULT_HIGH_COUNT_COLOR.getGreen(), DEFAULT_HIGH_COUNT_COLOR.getBlue(), value);
                binColors[col][row] = binColor;
            }
        }
    }

    public void drawPCPLines (Graphics2D g2) {
        // draw pcp lines
        g2.setColor(new Color(100, 100, 100, 40));
        for (Line2D.Double line : pcpLines) {
            g2.draw(line);
        }

        // draw axes
        g2.setColor(Color.darkGray);
        g2.drawLine(plotRectangle.x, plotRectangle.y, plotRectangle.x, plotRectangle.y + plotRectangle.height);
        g2.drawLine(plotRectangle.x+plotRectangle.width, plotRectangle.y, plotRectangle.x+plotRectangle.width, plotRectangle.y + plotRectangle.height);
    }

    public void drawPCPBins (Graphics2D g2) {
        // draw pcp tuple bins
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                g2.setColor(binColors[col][row]);
                g2.fill(pcpBinPaths[col][row]);
            }
        }

        // draw axes
        g2.setColor(Color.darkGray);
        g2.drawLine(plotRectangle.x, plotRectangle.y, plotRectangle.x, plotRectangle.y + plotRectangle.height);
        g2.drawLine(plotRectangle.x + plotRectangle.width, plotRectangle.y, plotRectangle.x + plotRectangle.width, plotRectangle.y + plotRectangle.height);

//        g2.draw(plotRectangle);
    }

    public void drawHeatmap (Graphics2D g2) {

        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++) {
                g2.setColor(binColors[col][row]);
                g2.fill(binRectangles[col][row]);
            }
        }

        g2.setColor(Color.gray);
        g2.draw(plotRectangle);
    }

    public void layoutPlots() {
        // layout heatmap plot
        double binValueWidth = (xColumn.getSummaryStats().getMax() - xColumn.getSummaryStats().getMin()) / numCols;
        xMinValue = xColumn.getSummaryStats().getMin() - (binValueWidth / 2.);
        xMaxValue = xColumn.getSummaryStats().getMax() + (binValueWidth / 2.);
        double binValueHeight = (yColumn.getSummaryStats().getMax() - yColumn.getSummaryStats().getMin()) / numRows;
        yMinValue = yColumn.getSummaryStats().getMin() - (binValueHeight / 2.);
        yMaxValue = yColumn.getSummaryStats().getMax() + (binValueHeight / 2.);

        int plotWidth = width - (insets.left + insets.right);
        binWidth = plotWidth / numCols;
        plotWidth = binWidth * numCols; // to make all bin widths equal and layout correct
        int plotHeight = height - (insets.top + insets.bottom);
        binHeight = plotHeight / numRows;
        plotHeight = binHeight * numRows; // to make all bin height equal and layout correct

        plotRectangle = new Rectangle(insets.left, insets.top, plotWidth, plotHeight);

        // create heatmap bin rectangles
        for (int row = 0; row < numRows; row++) {
            int y = plotRectangle.y + (row * binHeight);
            for (int col = 0; col < numCols; col++) {
                int x = plotRectangle.x + (col * binWidth);
                Rectangle binRect = new Rectangle(x, y, binWidth, binHeight);
                binRectangles[col][row] = binRect;
            }
        }

        // layout pcp plot
//        pcAxisHeight = plotHeight;
//        int pcAxisSpacing = width - (insets.left + insets.right);
//
//        pcAxisTop = insets.top;
//        pcAxisBottom = pcAxisTop + pcAxisHeight;
//        pcAxis0_X = insets.left;
//        pcAxis1_X = pcAxis0_X + pcAxisSpacing;

        double axis0_X = plotRectangle.x;
        double axis1_X = plotRectangle.x + plotRectangle.width;

        // create pcp bin polygons
        for (int row = 0; row < numRows; row++) {
//            int axis0_bin_top = plotRectangle.y + (row * binHeight);
//            int axis0_bin_bottom = plotRectangle.y + ((row+1) * binHeight);
            int axis0_bin_top = (plotRectangle.y + plotRectangle.height) - ((row+1) * binHeight);
            int axis0_bin_bottom = (plotRectangle.y + plotRectangle.height) - (row * binHeight);

            for (int col = 0; col < numCols; col++) {
                int axis1_bin_top = plotRectangle.y + (col * binHeight);
                int axis1_bin_bottom = plotRectangle.y + ((col+1) * binHeight);

                Path2D.Double pcpBinPath = new Path2D.Double();
                pcpBinPath.moveTo(axis0_X, axis0_bin_top);
                pcpBinPath.lineTo(axis1_X, axis1_bin_top);
                pcpBinPath.lineTo(axis1_X, axis1_bin_bottom);
                pcpBinPath.lineTo(axis0_X, axis0_bin_bottom);
                pcpBinPath.lineTo(axis0_X, axis0_bin_top);

                pcpBinPaths[row][col] = pcpBinPath;
            }
        }
    }
}
