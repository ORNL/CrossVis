package gov.ornl.datatable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DoubleColumn extends Column {
    private DoubleColumnSummaryStats summaryStats;
    private DoubleProperty minimumScaleValue = new SimpleDoubleProperty(Double.NaN);
    private DoubleProperty maximumScaleValue = new SimpleDoubleProperty(Double.NaN);
    private DoubleProperty minimumFocusValue = new SimpleDoubleProperty(Double.NaN);
    private DoubleProperty maximumFocusValue = new SimpleDoubleProperty(Double.NaN);

    public DoubleColumn(String name) {
        super(name);
    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new DoubleColumnSummaryStats(this, getDataModel().getNumHistogramBins(), null);
        }
        summaryStats.setValues(getValues(), getDataModel().getNumHistogramBins());

        if (Double.isNaN(getMinimumFocusValue())) {
            setMinimumFocusValue(summaryStats.getMinValue());
        }

        if (Double.isNaN(getMaximumScaleValue())) {
            setMaximumFocusValue(summaryStats.getMaxValue());
        }

        if (Double.isNaN(getMinimumScaleValue())) {
            setMinimumScaleValue(summaryStats.getMinValue());
        }

        if (Double.isNaN(getMaximumScaleValue())) {
            setMaximumScaleValue(summaryStats.getMaxValue());
        }
    }

    public double getMaximumFocusValue() { return maximumFocusValue.get(); }

    protected void setMaximumFocusValue(double value) { maximumFocusValue.set(value); }

    public ReadOnlyDoubleProperty maximumFocusValueProperty() { return maximumFocusValue; }

    public double getMinimumFocusValue() { return minimumFocusValue.get(); }

    protected void setMinimumFocusValue(double value) { minimumFocusValue.set(value); }

    public ReadOnlyDoubleProperty minimumFocusValueProperty() { return minimumFocusValue; }

    public double getMinimumScaleValue() { return minimumScaleValue.get(); }

    protected void setMinimumScaleValue(double value) {
        minimumScaleValue.set(value);
        if (getMinimumFocusValue() < getMinimumScaleValue()) {
            setMinimumFocusValue(value);
        }
    }

    public ReadOnlyDoubleProperty minimumScaleValueProperty() { return minimumScaleValue; }

    public double getMaximumScaleValue() { return maximumScaleValue.get(); }

    protected void setMaximumScaleValue(double value) {
        maximumScaleValue.set(value);
        if (getMaximumFocusValue() > getMaximumScaleValue()) {
            setMaximumFocusValue(value);
        }
    }

    public ReadOnlyDoubleProperty maximumScaleValueProperty() { return maximumScaleValue; }

    public double[] getValues() {
        int columnIndex = getDataModel().getColumnIndex(this);
        if (columnIndex == -1) {
            columnIndex = getDataModel().getDisabledColumns().indexOf(this);
            double values[] = new double[getDataModel().getTupleCount()];
            for (int i = 0; i < getDataModel().getTupleCount(); i++) {
                values[i] = (double) getDataModel().getDisabledTuple(i).getElement(columnIndex);
            }

            return values;
        } else {
            double values[] = new double[getDataModel().getTupleCount()];
            for (int i = 0; i < getDataModel().getTupleCount(); i++) {
                values[i] = (double) getDataModel().getTuple(i).getElement(columnIndex);
            }

            return values;
        }
    }

    public List<Double> getValuesAsList() {
        int columnIndex = getDataModel().getColumnIndex(this);

        ArrayList<Double> valuesList = new ArrayList<>();
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            valuesList.add((double)getDataModel().getTuple(i).getElement(columnIndex));
        }

        return valuesList;
    }

    public double[] getQueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataModel().getActiveQuery().getQueriedTuples();
        double values[] = new double[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (Double) tuple.getElement(columnIndex);
        }

        return values;
    }

    public double[] getNonqueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> nonqueriedTuples = getDataModel().getActiveQuery().getNonQueriedTuples();
        double values[] = new double[nonqueriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : nonqueriedTuples) {
            values[counter++] = (Double) tuple.getElement(columnIndex);
        }

        return values;
    }

    public DoubleColumnSummaryStats getStatistics() { return summaryStats; }
}
