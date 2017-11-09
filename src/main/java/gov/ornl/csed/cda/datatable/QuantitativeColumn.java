package gov.ornl.csed.cda.datatable;

import javafx.beans.property.*;

import java.io.Serializable;

public class QuantitativeColumn extends Column{

    // TODO: Remove the summary stats objects and fully adopt the bean property model for stats
    private SummaryStats summaryStats = new SummaryStats();
    private SummaryStats focusSummaryStats = null;

    private boolean isDiscrete = false;

    private DoubleProperty meanValue;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty standardDeviationValue;
    private DoubleProperty queryMeanValue;
    private DoubleProperty queryStandardDeviationValue;

    public QuantitativeColumn(String name) {
        super(name);

        meanValue = new SimpleDoubleProperty(Double.NaN);
        minValue = new SimpleDoubleProperty(Double.NaN);
        maxValue = new SimpleDoubleProperty(Double.NaN);
        standardDeviationValue = new SimpleDoubleProperty(Double.NaN);
        queryMeanValue = new SimpleDoubleProperty(Double.NaN);
        queryStandardDeviationValue = new SimpleDoubleProperty(Double.NaN);
    }

    public final double getQueryMeanValue() { return queryMeanValue.get(); }

    public final void setQueryMeanValue(double value) { queryMeanValue.set(value); }

    public DoubleProperty queryMeanValueProperty() { return queryMeanValue; }

    public final double getQueryStandardDeviationValue() { return queryStandardDeviationValue.get(); }

    public final void setQueryStandardDeviationValue (double value) { queryStandardDeviationValue.set(value); }

    public DoubleProperty queryStandardDeviationValueProperty() { return queryStandardDeviationValue; }

    public SummaryStats getFocusSummaryStats () {
        return focusSummaryStats;
    }

    public void makeDiscrete() {
        isDiscrete = true;
    }

    public void makeContinuous() {
        isDiscrete = false;
    }

    public boolean isContinuous () {
        return !isDiscrete;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public void setFocusSummaryStats (SummaryStats focusSummaryStats) {
        this.focusSummaryStats = focusSummaryStats;
    }

    public SummaryStats getSummaryStats() { return summaryStats; }

    public void setSummaryStats(SummaryStats summaryStats) {
        this.summaryStats = summaryStats;
    }

    public final double getStandardDeviationValue() { return standardDeviationValue.get(); }

    public final void setStandardDeviationValue(double value) { standardDeviationValue.set(value); }

    public DoubleProperty standardDeviationValueProperty() { return standardDeviationValue; }

    public final double getMeanValue() { return meanValue.get(); }

    public final void setMeanValue(double value) { meanValue.set(value); }

    public DoubleProperty meanValueProperty() { return meanValue; }

    public final double getMinValue() { return minValue.get(); }

    public final void setMinValue (double value) { minValue.set(value); }

    public DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }

    public final void setMaxValue(double value) { maxValue.set(value); }

    public DoubleProperty maxValueProperty() { return maxValue; }
}
