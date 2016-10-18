package gov.ornl.csed.cda.datatable;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ColumnSelectionRange {
    private ObjectProperty<Column> column;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;

    public ColumnSelectionRange(Column column, double minValue, double maxValue) {
        this.column = new SimpleObjectProperty<Column>(column);
        this.minValue = new SimpleDoubleProperty(minValue);
        this.maxValue = new SimpleDoubleProperty(maxValue);
    }

    public final Column getColumn() { return column.get(); }

//    public final void setColumn(Column column) { this.column.set(column); }

    public ObjectProperty<Column> columnProperty() { return column; }

    public final double getMinValue() { return minValue.get(); }

    public final void setMinValue(double minValue) { this.minValue.set(minValue); }

    public DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }

    public final void setMaxValue(double maxValue) { this.maxValue.set(maxValue); }

    public DoubleProperty maxValueProperty() { return maxValue; }
}
