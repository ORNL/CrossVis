package gov.ornl.csed.cda.datatable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.print.Doc;

/**
 * Created by csg on 11/25/14.
 */
public class DoubleColumnSummaryStats extends ColumnSummaryStats {
    private static final int DEFAULT_NUM_HISTOGRAM_BINS = 50;
    private static final int MAX_NUM_HISTOGRAM_BINS = 100;

    private double[] values;

    private DoubleProperty meanValue;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty standardDeviationValue;
    private DoubleProperty varianceValue;
    private DoubleProperty quartile1Value;
    private DoubleProperty quartile3Value;
    private DoubleProperty medianValue;
    private DoubleProperty skewnessValue;
    private DoubleProperty kurtosisValue;
    private DoubleProperty upperWhiskerValue;
    private DoubleProperty lowerWhiskerValue;

    private ObjectProperty<DoubleHistogram> histogram;

    private int numHistogramBins = DEFAULT_NUM_HISTOGRAM_BINS;

    public DoubleColumnSummaryStats(Column column, int numHistogramBins) {
        super(column);

        values = null;
        minValue = new SimpleDoubleProperty(Double.NaN);
        maxValue = new SimpleDoubleProperty(Double.NaN);
        meanValue = new SimpleDoubleProperty(Double.NaN);
        medianValue = new SimpleDoubleProperty(Double.NaN);
        standardDeviationValue = new SimpleDoubleProperty(Double.NaN);
        varianceValue = new SimpleDoubleProperty(Double.NaN);
        quartile1Value = new SimpleDoubleProperty(Double.NaN);
        quartile3Value = new SimpleDoubleProperty(Double.NaN);
        skewnessValue = new SimpleDoubleProperty(Double.NaN);
        kurtosisValue = new SimpleDoubleProperty(Double.NaN);
        upperWhiskerValue = new SimpleDoubleProperty(Double.NaN);
        lowerWhiskerValue = new SimpleDoubleProperty(Double.NaN);

        this.numHistogramBins = numHistogramBins;
    }

    public void setValues(double[] values, int numHistogramBins) {
        this.values = values;

        this.numHistogramBins = numHistogramBins;

        calculateStatistics();
    }

    public void setValues(double[] values) {
        this.values = values;

//        numHistogramBins = (int)Math.floor(Math.sqrt(values.length));
//        if (numHistogramBins > MAX_NUM_HISTOGRAM_BINS) {
//            numHistogramBins = MAX_NUM_HISTOGRAM_BINS;
//        }

        calculateStatistics();
    }

    public double[] getValues() {
        return values;
    }

    @Override
    public void calculateStatistics() {
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        
        setMinValue(stats.getMin());
        setMaxValue(stats.getMax());
        setMeanValue(stats.getMean());
        setMedianValue(stats.getPercentile(50));
        setVarianceValue(stats.getVariance());
        setStandardDeviationValue(stats.getStandardDeviation());
        setQuartile1Value(stats.getPercentile(25));
        setQuartile3Value(stats.getPercentile(75));
        setSkewnessValue(stats.getSkewness());
        setKurtosisValue(stats.getKurtosis());

        // calculate whiskers for box plot 1.5 of IQR
        double iqr_range = 1.5 * getIQR();
        double lowerFence = getQuartile1Value() - iqr_range;
        double upperFence = getQuartile3Value() + iqr_range;
        double sorted_data[] = stats.getSortedValues();

        // find upper datum that is not greater than upper fence
        if (upperFence >= getMaxValue()) {
            setUpperWhiskerValue(getMaxValue());
        } else {
            // find largest datum not larger than upper fence value
            for (int i = sorted_data.length - 1; i >= 0; i--) {
                if (sorted_data[i] <= upperFence) {
                    setUpperWhiskerValue(sorted_data[i]);
                    break;
                }
            }
        }

        if (lowerFence <= getMinValue()) {
            setLowerWhiskerValue(getMinValue());
        } else {
            // find smallest datum not less than lower fence value
            for (int i = 0; i < sorted_data.length; i++) {
                if (sorted_data[i] >= lowerFence) {
                    setLowerWhiskerValue(sorted_data[i]);
                    break;
                }
            }
        }

        calculateHistogram();
    }

    public void setNumHistogramBins(int numBins) {
        if (numBins != numHistogramBins) {
            numHistogramBins = numBins;
            calculateHistogram();
        }
    }

    public int getNumHistogramBins() {
        return numHistogramBins;
    }

    private DoubleColumn doubleColumn() {
        return (DoubleColumn)getColumn();
    }

    @Override
    public void calculateHistogram() {
        setHistogram(new DoubleHistogram(column.getName(), values, numHistogramBins,
                doubleColumn().getStatistics().getMinValue(),
                doubleColumn().getStatistics().getMaxValue()));
    }

    public DoubleHistogram getHistogram() {
        return histogramProperty().get();
    }

    public void setHistogram(DoubleHistogram histogram) {
        histogramProperty().set(histogram);
    }

    public ObjectProperty<DoubleHistogram> histogramProperty() {
        if (histogram == null) {
            histogram = new SimpleObjectProperty<>(this, "histogram");
        }
        return histogram;
    }

    public double getIQR() {
        return getQuartile3Value() - getQuartile1Value();
    }

    public void setMinValue(double value) {
        minValueProperty().set(value);
    }

    public double getMinValue() {
        return minValueProperty().get();
    }

    public DoubleProperty minValueProperty() {
        return minValue;
    }

    public void setMaxValue(double value) {
        maxValueProperty().set(value);
    }

    public double getMaxValue() {
        return maxValueProperty().get();
    }

    public DoubleProperty maxValueProperty() {
        return maxValue;
    }

    public void setMedianValue(double value) {
        medianValueProperty().set(value);
    }

    public double getMedianValue() {
        return medianValueProperty().get();
    }

    public DoubleProperty medianValueProperty() {
        return medianValue;
    }

    public void setMeanValue(double value) {
        meanValueProperty().set(value);
    }

    public double getMeanValue() {
        return meanValueProperty().get();
    }

    public DoubleProperty meanValueProperty() {
        return meanValue;
    }
    
    public void setStandardDeviationValue(double value) {
        standardDeviationValueProperty().set(value);
    }

    public double getStandardDeviationValue() {
        return standardDeviationValueProperty().get();
    }

    public DoubleProperty standardDeviationValueProperty() {
        return standardDeviationValue;
    }

    public void setVarianceValue(double value) {
        varianceValueProperty().set(value);
    }

    public double getVarianceValue() {
        return varianceValueProperty().get();
    }

    public DoubleProperty varianceValueProperty() {
        return varianceValue;
    }

    public void setQuartile1Value(double value) {
        quartile1ValueProperty().set(value);
    }

    public double getQuartile1Value() {
        return quartile1ValueProperty().get();
    }

    public DoubleProperty quartile1ValueProperty() {
        return quartile1Value;
    }

    public void setQuartile3Value(double value) {
        quartile3ValueProperty().set(value);
    }

    public double getQuartile3Value() {
        return quartile3ValueProperty().get();
    }

    public DoubleProperty quartile3ValueProperty() {
        return quartile3Value;
    }

    public void setUpperWhiskerValue(double value) {
        upperWhiskerValueProperty().set(value);
    }

    public double getUpperWhiskerValue() {
        return upperWhiskerValueProperty().get();
    }

    public DoubleProperty upperWhiskerValueProperty() {
        return upperWhiskerValue;
    }

    public void setLowerWhiskerValue(double value) {
        lowerWhiskerValueProperty().set(value);
    }

    public double getLowerWhiskerValue() {
        return lowerWhiskerValueProperty().get();
    }

    public DoubleProperty lowerWhiskerValueProperty() {
        return lowerWhiskerValue;
    }

    public void setKurtosisValue(double value) {
        kurtosisValueProperty().set(value);
    }

    public double getKurtosisValue() {
        return kurtosisValueProperty().get();
    }

    public DoubleProperty kurtosisValueProperty() {
        return kurtosisValue;
    }

    public void setSkewnessValue(double value) {
        skewnessValueProperty().set(value);
    }

    public double getSkewnessValue() {
        return skewnessValueProperty().get();
    }

    public DoubleProperty skewnessValueProperty() {
        return skewnessValue;
    }
}
