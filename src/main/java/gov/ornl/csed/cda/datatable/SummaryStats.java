package gov.ornl.csed.cda.datatable;

import java.util.ArrayList;

/**
 * Created by csg on 11/25/14.
 */
public class SummaryStats {
    private double min;
    private double max;
    private double mean;
    private double median;
    private double variance;
    private double standardDeviation;
    private double quantile1;
    private double quantile3;
    private double skewness;
    private double kurtosis;
    private double upperWhisker;
    private double lowerWhisker;
    private int numNaN = 0;
    private ArrayList<Double> corrCoefs = new ArrayList<>();
    private Histogram histogram;

    public ArrayList<Double> getCorrelationCoefficients() {
        return corrCoefs;
    }

    public double getIQR() {
        return quantile3 - quantile1;
    }

    public void setCorrelationCoefficients(ArrayList<Double> corrCoefs) {
        this.corrCoefs = corrCoefs;
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public void setHistogram(Histogram histogram) {
        this.histogram = histogram;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getQuantile1() {
        return quantile1;
    }

    public void setQuantile1(double quantile1) {
        this.quantile1 = quantile1;
    }

    public double getQuantile3() {
        return quantile3;
    }

    public void setQuantile3(double quantile3) {
        this.quantile3 = quantile3;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    public double getKurtosis() {
        return kurtosis;
    }

    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    public double getUpperWhisker() {
        return upperWhisker;
    }

    public void setUpperWhisker(double upperWhisker) {
        this.upperWhisker = upperWhisker;
    }

    public double getLowerWhisker() {
        return lowerWhisker;
    }

    public void setLowerWhisker(double lowerWhisker) {
        this.lowerWhisker = lowerWhisker;
    }
}
