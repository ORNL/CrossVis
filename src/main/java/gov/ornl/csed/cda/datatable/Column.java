package gov.ornl.csed.cda.datatable;

import javafx.beans.property.*;

import java.io.Serializable;

public class Column implements Serializable {

    // TODO: Remove the summary stats objects and fully adopt the bean property model for stats
    private SummaryStats summaryStats = new SummaryStats();
    private SummaryStats focusSummaryStats = null;

    private boolean isDiscrete = false;

    private StringProperty name;
    private BooleanProperty enabled;

    private DoubleProperty meanValue;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty standardDeviationValue;
    private DoubleProperty queryMeanValue;
    private DoubleProperty queryStandardDeviationValue;

    public Column(String name) {
        this.name = new SimpleStringProperty(name);
        enabled = new SimpleBooleanProperty(true);
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

    public final boolean getEnabled() { return enabled.get(); }

    public final void setEnabled (boolean enabled) { this.enabled.set(enabled); }

    public ReadOnlyBooleanProperty enabledProperty() { return enabled; }

    public final String getName() { return name.get(); }

    public final void setName(String name) {
        this.name.setValue(name);
    }

    public StringProperty nameProperty() { return name; }

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

    public String toString() {
        return getName();
    }
}
