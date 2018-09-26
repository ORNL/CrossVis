package gov.ornl.histogram;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DoubleHistogramDataModel extends HistogramDataModel {
    private final static double EPSILON = 0.000001;

    private ArrayList<Double> values = new ArrayList<>();
    private double binSize;
    private double minValue;
    private double maxValue;
    private double mean;
    private double standardDeviation;
    private double median;
    private double IQR;
    private double percentile25;
    private double percentile75;


    public DoubleHistogramDataModel(Collection<Double> values) {
        this(values, -1, Double.NaN, Double.NaN);
    }

    public DoubleHistogramDataModel(Collection<Double> values, int numBins) {
        this(values, numBins, Double.NaN, Double.NaN);
    }

    public DoubleHistogramDataModel(Collection<Double> values, int numBins, double minValue, double maxValue) {
        super();
        setValues(values, numBins, minValue, maxValue);
    }

    public double getMean() {
        return mean;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public double getMedian() {
        return median;
    }

    public double getIQR() {
        return IQR;
    }

    public double getPercentile25() {
        return percentile25;
    }

    public double getPercentile75() {
        return percentile75;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMinValue(double min) {
        this.minValue = min;
        calculateStatistics(getNumBins());
    }

    public void setMaxValue(double max) {
        this.maxValue = max;
        calculateStatistics(getNumBins());
    }

    public void setMinMaxValue(double min, double max) {
        this.minValue = min;
        this.maxValue = max;
        calculateStatistics(getNumBins());
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(Collection<Double> values, int numBins, double minValue, double maxValue) {
        this.values.addAll(values);
        this.minValue = minValue;
        this.maxValue = maxValue;
        calculateStatistics(numBins);
    }

    public void setValues(Collection<Double> values, int numBins) {
        setValues(values, numBins, Double.NaN, Double.NaN);
    }

    public void setValues(Collection<Double> values) {
        setValues(values, -1, Double.NaN, Double.NaN);
    }

    @Override
    public void setNumBins(int binCount) {
        calculateStatistics(binCount);
    }

    @Override
    public int getBinIndex(Object value) {
        return getBinIndex((double)value);
    }

    public double getBinLowerBound(int i) {
        return minValue + (i * binSize);
    }

    public double getBinUpperBound(int i) {
        return minValue + ((i + 1) * binSize);
    }

    private void calculateStatistics(int numBins) {
        double valueArray[] = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            valueArray[i] = values.get(i);
        }
        DescriptiveStatistics statistics = new DescriptiveStatistics(valueArray);

        mean = statistics.getMean();
        median = statistics.getPercentile(50.);
        standardDeviation = statistics.getStandardDeviation();
        IQR = statistics.getPercentile(75.) - statistics.getPercentile(25.);
        percentile25 = statistics.getPercentile(25.);
        percentile75 = statistics.getPercentile(75.);

        if (numBins == -1) {
            numBins = (int) Math.floor(Math.sqrt(values.size()));
        }

        boolean findMinValue = Double.isNaN(minValue);
        boolean findMaxValue = Double.isNaN(maxValue);
        if (findMaxValue || findMinValue) {
            if (findMinValue) {
                minValue = values.get(0);
            }
            if (findMaxValue) {
                maxValue = values.get(0);
            }
            for (double value : values) {
                if (findMinValue) {
                    minValue = value < minValue ? value : minValue;
                }
                if (findMaxValue) {
                    maxValue = value > maxValue ? value : maxValue;
                }
            }
        }

        reallocateBinCountsArray(numBins);

        binSize = (maxValue - minValue) / numBins;

        for (double value : values) {
            int binIndex = getBinIndex(value);

            if (binIndex < 0) {

            } else if (binIndex >= getNumBins()) {
                // if value is equal to max then increment last bin
                // TODO: add a bin for values outside min and max value range
                if ((Math.abs(value - maxValue)) <= EPSILON) {
                    setBinCount(numBins-1, getBinCount(numBins-1) + 1);
                }
            } else {
                setBinCount(binIndex, getBinCount(binIndex) + 1);
            }
        }
    }

    public int getBinIndex(double value) {
        int binIndex = (int)((value - minValue) / binSize);
        return binIndex;
    }

    public static void main (String args[]) {
        ArrayList<Double>valueList = new ArrayList<>();
        double values[] = new double[] {162, 168, 177, 147, 189, 171, 173, 168, 178,
                184, 165, 173, 179, 166, 168, 165, 140, 190};
        for (double value : values) {
            valueList.add(value);
        }

        DoubleHistogramDataModel histogram = new DoubleHistogramDataModel(valueList, 5, 140, 190);
        for (int i = 0; i < histogram.getNumBins(); i++) {
            System.out.println(i + ": " + histogram.getBinCount(i) + " [" + histogram.getBinLowerBound(i) +
                    ", " + histogram.getBinUpperBound(i) + "]");
        }
    }
}
