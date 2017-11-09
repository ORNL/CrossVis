package gov.ornl.csed.cda.datatable;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by csg on 8/31/16.
 */
public class Histogram2D {
    private final static Logger log = Logger.getLogger(Histogram2D.class.getName());
    private final static double EPSILON = 0.000001;

    private String name;
    private double xData[];
    private double yData[];
    private int numBins;

    private double xBinSize;
    private double yBinSize;

    private int binCounts[][];
    private int maxBinCount;

    private double xMinValue;
    private double xMaxValue;
    private double yMinValue;
    private double yMaxValue;

    public Histogram2D(String name, double xData[], double yData[], int numBins) {
        this.name = name;
        this.xData = xData;
        this.yData = yData;
        this.numBins = numBins;

        this.xMinValue = Double.NaN;
        this.xMaxValue = Double.NaN;

        this.yMinValue = Double.NaN;
        this.yMaxValue = Double.NaN;

        calculateStatistics();
    }

    public Histogram2D(String name, double xData[], double yData[], int numBins, double xMinValue,
                       double xMaxValue, double yMinValue, double yMaxValue) {
        this.name = name;
        this.xData = xData;
        this.yData = yData;
        this.numBins = numBins;

        this.xMinValue = xMinValue;
        this.xMaxValue = xMaxValue;

        this.yMinValue = yMinValue;
        this.yMaxValue = yMaxValue;

        calculateStatistics();
    }

    public int getNumBins() { return numBins; }

    public int getBinCount(int x, int y) { return binCounts[x][y]; }

    public double getBinXLowerBound(int ix) { return xMinValue + (ix * xBinSize); }

    public double getBinXUpperBound(int ix) { return xMinValue + ((ix + 1) * xBinSize); }

    public double getBinYLowerBound(int iy) { return yMinValue + (iy * yBinSize); }

    public double getBinYUpperBound(int iy) { return yMinValue + ((iy + 1) * yBinSize); }

    public double getXMinValue() { return xMinValue; }

    public double getXMaxValue() { return xMaxValue; }

    public double getYMinValue() { return yMinValue; }

    public double getYMaxValue() { return yMaxValue; }

    public void setNumBins (int numBins) {
        this.numBins = numBins;
        calculateStatistics();
    }

    public void setXData (double xData[]) {
        this.xData = xData;
        calculateStatistics();
    }

    public void setYData (double yData[]) {
        this.yData = yData;
        calculateStatistics();
    }

    public void setXMinValue (double xMinValue) {
        this.xMinValue = xMinValue;
        calculateStatistics();
    }

    public void setXMaxValue (double xMaxValue) {
        this.xMaxValue = xMaxValue;
        calculateStatistics();
    }

    public void setYMinValue (double yMinValue) {
        this.yMinValue = yMinValue;
        calculateStatistics();
    }

    public void setYMaxValue (double yMaxValue) {
        this.yMaxValue = yMaxValue;
        calculateStatistics();
    }

    public int getMaxBinCount() { return maxBinCount; }

    public String getName() { return name; }

    private void calculateStatistics() {
        if (Double.isNaN(xMinValue)) {
            xMinValue = xData[0];
            for (int i = 1; i < xData.length; i++) {
                if (xData[i] < xMinValue) {
                    xMinValue = xData[i];
                }
            }
        }

        if (Double.isNaN(xMaxValue)) {
            xMaxValue = xData[0];
            for (int i = 1; i < xData.length; i++) {
                if (xData[i] > xMaxValue) {
                    xMaxValue = xData[i];
                }
            }
        }

        if (Double.isNaN(yMinValue)) {
            yMinValue = yData[0];
            for (int i = 1; i < yData.length; i++) {
                if (yData[i] < yMinValue) {
                    yMinValue = yData[i];
                }
            }
        }

        if (Double.isNaN(yMaxValue)) {
            yMaxValue = yData[0];
            for (int i = 1; i < yData.length; i++) {
                if (yData[i] > yMaxValue) {
                    yMaxValue = yData[i];
                }
            }
        }

        binCounts = new int[numBins][numBins];
        for (int i = 0; i < numBins; i++) {
            Arrays.fill(binCounts[i], 0);
        }
        maxBinCount = 0;
        xBinSize = (xMaxValue - xMinValue) / numBins;
        yBinSize = (yMaxValue - yMinValue) / numBins;

//        float xValue = tuple.getElement(xColumnIndex);
////            float norm = (xValue - xColumn.getSummaryStats().getMin()) / (xColumn.getSummaryStats().getMax() - xColumn.getSummaryStats().getMin());
//        double norm = (xValue - xMinValue) / (xMaxValue - xMinValue);
//        int col = (int)Math.floor(norm * numCols);
//
//        float yValue = tuple.getElement(yColumnIndex);
////            norm = (yValue - yColumn.getSummaryStats().getMin()) / (yColumn.getSummaryStats().getMax() - yColumn.getSummaryStats().getMin());
//        norm = (yValue - yMinValue) / (yMaxValue - yMinValue);
//        int row = (int)((1. - norm) * numRows);
//
//        binCounts[col][row]++;
//
//        if (binCounts[col][row] > maxBinCount) {
//            maxBinCount = binCounts[col][row];
//        }

        for (int i = 0; i < xData.length; i++) {
            double xValue = xData[i];
            int xBinIndex = (int)((xValue - xMinValue) / xBinSize);
            if (xBinIndex < 0) {
                log.info("xBinIndex is < 0 so ignoring this value");
                continue;
            } else if (xBinIndex >= numBins) {
                if ((Math.abs(xValue - xMaxValue)) <= EPSILON) {
                    xBinIndex = numBins - 1;
                } else {
                    log.info("xBinindex is >= numBins so ignoring this value");
                    continue;
                }
            }

            double yValue = yData[i];
            int yBinIndex = (int)((yValue - yMinValue) / yBinSize);
            if (yBinIndex < 0) {
                log.info("yBinIndex is < 0 so ignoring this value");
                continue;
            } else if (yBinIndex >= numBins) {
                if ((Math.abs(yValue - yMaxValue)) <= EPSILON) {
                    yBinIndex = numBins - 1;
                } else {
                    log.info("yBinIndex is >= numBins so ignoring this value");
                    continue;
                }
            }

            binCounts[xBinIndex][yBinIndex]++;
            if (binCounts[xBinIndex][yBinIndex] > maxBinCount) {
                maxBinCount = binCounts[xBinIndex][yBinIndex];
            }
        }
    }

    public static void main (String args[]) {
//        double xData[] = new double[] {162, 168, 177, 147, 189, 171, 173, 168, 178,
//                184, 165, 173, 179, 166, 168, 165, 140, 190};
//        double yData[] = new double[] {162, 168, 177, 147, 189, 171, 173, 168, 178,
//                184, 165, 173, 179, 166, 168, 165, 140, 190};
        double xData[] = new double[] {1,3,2,4,4,1,2,2,4};
        double yData[] = new double[] {13,12,14,11,14,11,12,11,12};
//        Histogram2D histogram = new Histogram2D("Test", xData, yData, 5, 140, 190, 140, 190);
        Histogram2D histogram = new Histogram2D("Test", xData, yData, 3, 1, 4, 11, 14);
        for (int iy = 0; iy < histogram.getNumBins(); iy++) {
            String line = "";
            for (int ix = 0; ix < histogram.getNumBins(); ix++) {
                line += histogram.getBinCount(ix, iy) + " ";
            }
            log.info(line);
        }
    }
}
