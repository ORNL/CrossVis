package gov.ornl.csed.cda.histogram;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.Arrays;

/**
 * Created by csg on 1/5/16.
 */
public class Histogram {

    private static double EPSILON = 0.000001;
    private String name;
    private double values[];
    private int numBins;
    private double binSize;

    private int binCounts[];
    private int maxBinCount;

    private double minValue;
    private double maxValue;
    private double mean;
    private double median;
    private double stDev;
    private double IQR;
    private double percentile25;
    private double percentile75;

    public Histogram(String name, double values[], int numBins) {
        this.name = name;
        this.values = values;
        this.numBins = numBins;
        minValue = Double.NaN;
        maxValue = Double.NaN;

        calculateStatistics();
    }

    public Histogram(String name, double values[], int numBins, double min, double max) {
        this.name = name;
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

    public double getMean() {
        return mean;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMedian() {
        return median;
    }

    public double getStDev() {
        return stDev;
    }

    public double getIQR() {
        return IQR;
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

    public double getPercentile25() {
        return percentile25;
    }

    public double getPercentile75() {
        return percentile75;
    }

    public String getName() {
        return name;
    }

    public void calculateStatistics() {
        DescriptiveStatistics statistics = new DescriptiveStatistics(values);

        if (Double.isNaN(minValue)) {
            minValue = statistics.getMin();
        }

        if (Double.isNaN(maxValue)) {
            maxValue = statistics.getMax();
        }

        mean = statistics.getMean();
        median = statistics.getPercentile(50.);
        stDev = statistics.getStandardDeviation();
        IQR = statistics.getPercentile(75.) - statistics.getPercentile(25.);
        percentile25 = statistics.getPercentile(25.);
        percentile75 = statistics.getPercentile(75.);

        binCounts = new int[numBins];
        Arrays.fill(binCounts, 0);
        maxBinCount = 0;
        binSize = (maxValue - minValue) / numBins;

        for (double value : values) {
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
        Histogram histogram = new Histogram("Test", values, 5, 140, 190);
        for (int i = 0; i < histogram.getNumBins(); i++) {
            System.out.println(i + ": " + histogram.getBinCount(i) + " [" + histogram.getBinLowerBound(i) +
            ", " + histogram.getBinUpperBound(i) + "]");
        }
    }
}
