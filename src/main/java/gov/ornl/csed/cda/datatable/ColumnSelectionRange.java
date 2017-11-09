package gov.ornl.csed.cda.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ColumnSelectionRange {
    private QuantitativeColumn column;
    private ListProperty<Double> rangeValues;
//    private DoubleProperty minValue;
//    private DoubleProperty maxValue;

    public ColumnSelectionRange(QuantitativeColumn column, double minValue, double maxValue) {
        this.column = column;
        rangeValues = new SimpleListProperty<>();
        ObservableList<Double> observableList = FXCollections.observableArrayList(minValue, maxValue);
        rangeValues.set(observableList);
        
//        this.minValue = new SimpleDoubleProperty(minValue);
//        this.maxValue = new SimpleDoubleProperty(maxValue);
    }

    public final QuantitativeColumn getColumn() { return column; }

    public final double getMinValue() { return rangeValues.get(0); }

    public final void setMinValue(double minValue) { rangeValues.set(0, minValue); }

    public final double getMaxValue() { return rangeValues.get(1); }

    public final void setMaxValue(double maxValue) { rangeValues.set(1, maxValue); }

    public final void setRangeValues(double minValue, double maxValue) {
        rangeValues.setAll(minValue, maxValue);
    }
    public ListProperty<Double> rangeValuesProperty() {return rangeValues; }
}
