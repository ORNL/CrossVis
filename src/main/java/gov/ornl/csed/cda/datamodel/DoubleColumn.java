package gov.ornl.csed.cda.datamodel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Arrays;

public class DoubleColumn extends Column {
    private DoubleProperty mean;
    private DoubleProperty min;
    private DoubleProperty max;
    private DoubleProperty standardDeviation;
    private DoubleProperty queryMean;
    private DoubleProperty queryStandardDeviation;

//    private DescriptiveStatistics statistics;
    private ArrayList<Double> values;

    public DoubleColumn(String name) {
        super(name);
//        statistics = new DescriptiveStatistics();
        values = new ArrayList<>();
    }

    public double getValueAt(int index) {
//        return statistics.getElement(index);
        return values.get(index);
    }

    public void addValue(double value) {
//        statistics.addValue(value);
        values.add(value);
//        setStatisticsProperties();
    }

    public void addValues(double newValues[]) {
//        for (double value : values) {
//            statistics.addValue(value);
//        }
        for (double value : values) {
            values.add(value);
        }
//        setStatisticsProperties();
//        values.add(value);
    }

//    public void setValueAt(int index, double value) {
//        statistics.set
//        values.set(index, value);
//    }

    public void clearValues() {
//        statistics.clear();
        values.clear();
//        setStatisticsProperties();
    }

    public long getSize() {
//        return statistics.getN();
        return values.size();
    }

    @Override
    protected void calculateStatistics() {
        if (getSize() == 0) {
            setMax(Double.NaN);
            setMin(Double.NaN);
            setMean(Double.NaN);
            setStandardDeviation(Double.NaN);
        } else {
            double valueArray[] = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                valueArray[i] = values.get(i);
            }
            DescriptiveStatistics statistics = new DescriptiveStatistics(valueArray);
            setMax(statistics.getMax());
            setMin(statistics.getMin());
            setMean(statistics.getMean());
            setStandardDeviation(statistics.getStandardDeviation());
        }
    }

//    @Override
//    protected void calculateStatistics() {
//        double valueArray[] = new double[values.size()];
//        for (int i = 0; i < values.size(); i++) {
//            valueArray[i] = values.get(i);
//        }
//
//        DescriptiveStatistics statistics = new DescriptiveStatistics(valueArray);
//
//        setMax(statistics.getMax());
//        setMin(statistics.getMin());
//        setMean(statistics.getMean());
//        setStandardDeviation(statistics.getStandardDeviation());
//    }

    public DoubleProperty queryStandardDeviationProperty() {
        if (queryStandardDeviation == null) {
            queryStandardDeviation = new SimpleDoubleProperty(Double.NaN);
        }
        return queryStandardDeviation;
    }

    public void setQueryStandardDeviationValue(double queryStandardDeviation) {
        queryStandardDeviationProperty().set(queryStandardDeviation);
    }

    public double getQueryStandardDeviation() {
        return queryStandardDeviationProperty().get();
    }
    
    public DoubleProperty queryMeanProperty() {
        if (queryMean == null) {
            queryMean = new SimpleDoubleProperty(Double.NaN);
        }
        return queryMean;
    }

    public void setQueryMeanValue(double queryMean) {
        queryMeanProperty().set(queryMean);
    }

    public double getQueryMean() {
        return queryMeanProperty().get();
    }
    
    public DoubleProperty standardDeviationProperty() {
        if (standardDeviation == null) {
            standardDeviation = new SimpleDoubleProperty(Double.NaN);
        }
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        standardDeviationProperty().set(standardDeviation);
    }

    public double getStandardDeviation() {
        return standardDeviationProperty().get();
    }
    
    public DoubleProperty maxProperty() {
        if (max == null) {
            max = new SimpleDoubleProperty(Double.NaN);
        }
        return max;
    }

    public void setMax(double max) {
        maxProperty().set(max);
    }

    public double getMax() {
        return maxProperty().get();
    }
    
    public DoubleProperty minProperty() {
        if (min == null) {
            min = new SimpleDoubleProperty(Double.NaN);
        }
        return min;
    }

    public void setMin(double min) {
        minProperty().set(min);
    }

    public double getMin() {
        return minProperty().get();
    }
    
    public DoubleProperty meanProperty() {
        if (mean == null) {
            mean = new SimpleDoubleProperty(Double.NaN);
        }
        return mean;
    }

    public void setMean(double mean) {
        meanProperty().set(mean);
    }
    
    public double getMean() {
        return meanProperty().get();
    }
}
