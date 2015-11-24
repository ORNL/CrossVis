package gov.ornl.eden;

/**
 * Created by csg on 11/25/14.
 */
public class QuerySpec {
    private float min;
    private float max;
    private float mean;
    private float median;
    private float variance;
    private float standardDeviation;
    private float quantile1;
    private float quantile3;
    private float skewness;
    private float kurtosis;
    private float upperWhisker;
    private float lowerWhisker;

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public float getMedian() {
        return median;
    }

    public void setMedian(float median) {
        this.median = median;
    }

    public float getVariance() {
        return variance;
    }

    public void setVariance(float variance) {
        this.variance = variance;
    }

    public float getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public float getQuantile1() {
        return quantile1;
    }

    public void setQuantile1(float quantile1) {
        this.quantile1 = quantile1;
    }

    public float getQuantile3() {
        return quantile3;
    }

    public void setQuantile3(float quantile3) {
        this.quantile3 = quantile3;
    }

    public float getSkewness() {
        return skewness;
    }

    public void setSkewness(float skewness) {
        this.skewness = skewness;
    }

    public float getKurtosis() {
        return kurtosis;
    }

    public void setKurtosis(float kurtosis) {
        this.kurtosis = kurtosis;
    }

    public float getUpperWhisker() {
        return upperWhisker;
    }

    public void setUpperWhisker(float upperWhisker) {
        this.upperWhisker = upperWhisker;
    }

    public float getLowerWhisker() {
        return lowerWhisker;
    }

    public void setLowerWhisker(float lowerWhisker) {
        this.lowerWhisker = lowerWhisker;
    }
}
