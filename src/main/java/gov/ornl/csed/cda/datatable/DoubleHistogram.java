package gov.ornl.csed.cda.datatable;

import java.util.Arrays;

/**
 * Created by csg on 1/5/16.
 */
public class DoubleHistogram extends Histogram {
    private final static double EPSILON = 0.000001;

    private double values[];
    private int numBins;
    private double binSize;

    private int binCounts[];
    private int maxBinCount;

    private double minValue;
    private double maxValue;

    public DoubleHistogram(String name, double values[], int numBins) {
        super(name);

        this.values = values;
        this.numBins = numBins;
        minValue = Double.NaN;
        maxValue = Double.NaN;

        calculateStatistics();
    }

    public DoubleHistogram(String name, double values[], int numBins, double min, double max) {
        super(name);

        this.values = values;
        this.numBins = numBins;
        this.minValue = min;
        this.maxValue = max;

        calculateStatistics();
    }

    public int getBinCount(int i) {
        return binCounts[i];
    }

    public double getBinLowerBound(int i) {
        return minValue + (i * binSize);
    }

    public double getBinUpperBound(int i) {
        return minValue + ((i + 1) * binSize);
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public int getNumBins() {
        return numBins;
    }

    public void setNumBins(int numBins) {
        this.numBins = numBins;
        calculateStatistics();
    }

    public void setValues (double values[]) {
        this.values = values;
        calculateStatistics();
    }

    public void setMinValue(double min) {
        this.minValue = min;
        calculateStatistics();
    }

    public void setMaxValue(double max) {
        this.maxValue = max;
        calculateStatistics();
    }

    public int getMaxBinCount() {
        return maxBinCount;
    }

    public void calculateStatistics() {
        if (Double.isNaN(minValue)) {
            for (double value : values) {
                if (Double.isNaN(minValue)) {
                    minValue = value;
                } else if (value < minValue) {
                    minValue = value;
                }
            }
        }

        if (Double.isNaN(maxValue)) {
            for (double value : values) {
                if (Double.isNaN(value)) {
                    maxValue = value;
                } else if (value > maxValue) {
                    maxValue = value;
                }
            }
        }

        binCounts = new int[numBins];
        Arrays.fill(binCounts, 0);
        maxBinCount = 0;
        binSize = (maxValue - minValue) / numBins;

        for (double value : values) {
//            if (name.equals("MPG")) {
//                if (value >= 9d && value <= 11.5d) {
//                    System.out.println("value: " + value);
//                }
//            }

            int binIndex = (int)((value - minValue) / binSize);
            if (binIndex < 0) {
                // the value is smaller than the minValue
            } else if (binIndex >= numBins) {
                // if the value is equal to the max value increment the last bin
                if ((Math.abs(value - maxValue)) <= EPSILON) {
                    binCounts[numBins-1]++;
                    if (binCounts[numBins-1] > maxBinCount) {
                        maxBinCount = binCounts[numBins-1];
                    }
                }
            } else {
                binCounts[binIndex]++;
                if (binCounts[binIndex] > maxBinCount) {
                    maxBinCount = binCounts[binIndex];
                }
            }
        }
    }

    public static void main (String args[]) {
        double values[] = new double[] {162, 168, 177, 147, 189, 171, 173, 168, 178,
                184, 165, 173, 179, 166, 168, 165, 140, 190};

        DoubleHistogram histogram = new DoubleHistogram("Test", values, 5, 140, 190);
        for (int i = 0; i < histogram.getNumBins(); i++) {
            System.out.println(i + ": " + histogram.getBinCount(i) + " [" + histogram.getBinLowerBound(i) +
            ", " + histogram.getBinUpperBound(i) + "]");
        }
    }
}
