package gov.ornl.csed.cda.datatable;

import java.util.Arrays;
import java.util.logging.Logger;

public class Histogram2D {
    private final static Logger log = Logger.getLogger(Histogram2D.class.getName());

    private Histogram2DDimension xDimension;
    private Histogram2DDimension yDimension;
    private int binCounts[][];
    private int maxBinCount;

    public Histogram2D(Histogram2DDimension xDimension, Histogram2DDimension yDimension) {
        this.xDimension = xDimension;
        this.yDimension = yDimension;
        calculateStatistics();
    }

    public int getBinCount(int x, int y) { return binCounts[x][y]; }

    public Histogram2DDimension getXDimension() {
        return xDimension;
    }

    public Histogram2DDimension getYDimension() {
        return yDimension;
    }

    public void setNumBins (int numBins) {
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

        for (int i = 0; i < xDimension.size(); i++) {
            int xBinIndex = xDimension.getBinIndex(xDimension.getValue(i));
            if (xBinIndex < 0) {
                log.info("xBinIndex is < 0 so ignoring this value");
                continue;
            } else if (xBinIndex >= xDimension.getNumBins()) {
                log.info("xBinindex is >= numBins so ignoring this value");
                continue;
            }

            int yBinIndex = yDimension.getBinIndex(yDimension.getValue(i));
            if (yBinIndex < 0) {
                log.info("yBinIndex is < 0 so ignoring this value");
                continue;
            } else if (yBinIndex >= yDimension.getNumBins()) {
                log.info("yBinIndex is >= numBins so ignoring this value");
                continue;
            }

            binCounts[xBinIndex][yBinIndex]++;
            if (binCounts[xBinIndex][yBinIndex] > maxBinCount) {
                maxBinCount = binCounts[xBinIndex][yBinIndex];
            }
        }
    }

    public static void main (String args[]) {
        double xData[] = new double[] {1,3,2,4,4,1,2,2,4};
        double yData[] = new double[] {13,12,14,11,14,11,12,11,12};
        Histogram2DDimension.Double xDimension = new Histogram2DDimension.Double(xData, 3, 1, 4);
        Histogram2DDimension.Double yDimension = new Histogram2DDimension.Double(yData, 3, 11, 14);

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
