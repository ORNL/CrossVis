package gov.ornl.csed.cda.datatable;

import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnSelectionRange {
    private static final Logger log = LoggerFactory.getLogger(ColumnSelectionRange.class);

    private ObjectProperty<Column> column;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;

    public ColumnSelectionRange(Column column, double minValue, double maxValue) {
        this.column = new SimpleObjectProperty<Column>(column);
        this.minValue = new SimpleDoubleProperty(minValue);
        this.maxValue = new SimpleDoubleProperty(maxValue);

        registerListeners();
    }

    private void registerListeners() {
//        minValue.addListener((observable, oldValue, newValue) -> {
//            log.debug("In minValue change handler " + newValue);
//            minValue.set(GraphicsUtil.constrain(newValue.doubleValue(), getColumn().getMinValue(), getColumn().getMaxValue()));
//        });
    }

    public final Column getColumn() { return column.get(); }

    public final void setColumn(Column column) { this.column.set(column); }

    public ObjectProperty<Column> columnProperty() { return column; }

    public final double getMinValue() { return minValue.get(); }

    public final void setMinValue(double minValue) {
        this.minValue.set(GraphicsUtil.constrain(minValue, getColumn().getMinValue(), getColumn().getMaxValue()));
//        this.minValue.set(minValue);
    }

    public DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }

    public final void setMaxValue(double maxValue) {
        this.maxValue.set(GraphicsUtil.constrain(maxValue, getColumn().getMinValue(), getColumn().getMaxValue()));
//        this.maxValue.set(maxValue);
    }

    public DoubleProperty maxValueProperty() { return maxValue; }

//    public double getMaxValue() {
//        return maxValue;
//    }
//
//    public ColumnSelection getColumnSelection () {
//        return columnSelection;
//    }
//
//    public void setMaxValue(double maxValue) {
//        this.maxValue = maxValue;
//    }
//
//    public double getMinValue() {
//        return minValue;
//    }
//
//    public void setMinValue(double minValue) {
//        this.minValue = minValue;
//    }
}
