package gov.ornl.csed.cda.datatable;

public class ColumnSelectionRange {
    private ColumnSelection columnSelection;
    private double minValue;
    private double maxValue;

    public ColumnSelectionRange(ColumnSelection columnSelection, double minValue, double maxValue) {
        this.columnSelection = columnSelection;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public ColumnSelection getColumnSelection () {
        return columnSelection;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }
}
