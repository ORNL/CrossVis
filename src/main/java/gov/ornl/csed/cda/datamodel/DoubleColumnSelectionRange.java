package gov.ornl.csed.cda.datamodel;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DoubleColumnSelectionRange extends ColumnSelectionRange {
    private ListProperty<Double> rangeValues;

    public DoubleColumnSelectionRange(DoubleColumn column, double minValue, double maxValue) {
        super(column);
        rangeValues = new SimpleListProperty<>();
        ObservableList<Double> observableList = FXCollections.observableArrayList(minValue, maxValue);
        rangeValues.set(observableList);
    }

    public final double getMinValue() { return rangeValues.get(0); }

    public final void setMinValue(double minValue) { rangeValues.set(0, minValue); }

    public final double getMaxValue() { return rangeValues.get(1); }

    public final void setMaxValue(double maxValue) { rangeValues.set(1, maxValue); }

    public final void setRangeValues(double minValue, double maxValue) {
        rangeValues.setAll(minValue, maxValue);
    }

    public ListProperty<Double> rangeValuesProperty() {return rangeValues; }
}
