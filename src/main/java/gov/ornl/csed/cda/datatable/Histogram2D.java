package gov.ornl.csed.cda.datatable;

import java.util.Arrays;
import java.util.logging.Logger;

public class Histogram2D {
    private final static Logger log = Logger.getLogger(Histogram2D.class.getName());

    private Histogram2DDimension xDimension;
    private Histogram2DDimension yDimension;
//    private int numBins;
    private int binCounts[][];
    private int maxBinCount;

    public Histogram2D(Histogram2DDimension xDimension, Histogram2DDimension yDimension) {
        this.xDimension = xDimension;
        this.yDimension = yDimension;
//        this.numBins = numBins;

        calculateStatistics();
    }

//    public int getNumBins() { return numBins; }

    public int getBinCount(int x, int y) { return binCounts[x][y]; }

    public Histogram2DDimension getXDimension() {
        return xDimension;
    }

    public Histogram2DDimension getYDimension() {
        return yDimension;
    }

    public void setNumBins (int numBins) {
//        this.numBins = numBins;
        xDimension.setNumBins(numBins);
        yDimension.setNumBins(numBins);
        calculateStatistics();
    }

    public int getMaxBinCount() { return maxBinCount; }

    private void calculateStatistics() {
        binCounts = new int[xDimension.getNumBins()][yDimension.getNumBins()];
        for (int i = 0; i < xDimension.getNumBins(); i++) {
            Arrays.fill(binCounts[i], 0);
        }
        maxBinCount = 0;
//        xBinSize = (xMaxValue - xMinValue) / numBins;
//        yBinSize = (yMaxValue - yMinValue) / numBins;

        for (int i = 0; i < xDimension.size(); i++) {
//            double xValue = xData[i];
//            int xBinIndex = (int)((xValue - xMinValue) / xBinSize);
            int xBinIndex = xDimension.getBinIndex(xDimension.getValue(i));
            if (xBinIndex < 0) {
                log.info("xBinIndex is < 0 so ignoring this value");
                continue;
            } else if (xBinIndex >= xDimension.getNumBins()) {
                log.info("xBinindex is >= numBins so ignoring this value");
                continue;
//                if ((Math.abs(xValue - xMaxValue)) <= EPSILON) {
//                    xBinIndex = numBins - 1;
//                } else {
//                    log.info("xBinindex is >= numBins so ignoring this value");
//                    continue;
//                }
            }

//            double yValue = yData[i];
//            int yBinIndex = (int)((yValue - yMinValue) / yBinSize);
            int yBinIndex = yDimension.getBinIndex(yDimension.getValue(i));
            if (yBinIndex < 0) {
                log.info("yBinIndex is < 0 so ignoring this value");
                continue;
            } else if (yBinIndex >= yDimension.getNumBins()) {
                log.info("yBinIndex is >= numBins so ignoring this value");
                continue;
//                if ((Math.abs(yValue - yMaxValue)) <= EPSILON) {
//                    yBinIndex = numBins - 1;
//                } else {
//                    log.info("yBinIndex is >= numBins so ignoring this value");
//                    continue;
//                }
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
//        Histogram2DOLD histogram = new Histogram2DOLD("Test", xData, yData, 5, 140, 190, 140, 190);
        Histogram2DDimension.Double xDimension = new Histogram2DDimension.Double(xData, 3, 1, 4);
        Histogram2DDimension.Double yDimension = new Histogram2DDimension.Double(yData, 3, 11, 14);

//        Histogram2D histogram = new Histogram2D("Test", xData, yData, 3, 1, 4, 11, 14);
        Histogram2D histogram = new Histogram2D(xDimension, yDimension);
        for (int iy = 0; iy < histogram.getYDimension().getNumBins(); iy++) {
            String line = "";
            for (int ix = 0; ix < histogram.getXDimension().getNumBins(); ix++) {
                line += histogram.getBinCount(ix, iy) + " ";
            }
            System.out.println(line);
        }
    }
}
