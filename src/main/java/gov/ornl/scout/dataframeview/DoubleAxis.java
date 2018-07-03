package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.DoubleColumn;
import javafx.geometry.Orientation;

public class DoubleAxis extends Axis {

    public DoubleAxis(DataFrameView dataTableView, Column column, Orientation orientation) {
        super(dataTableView, column, orientation);
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);
    }

    public double getMinValue() { return doubleColumn().getMinValue(); }

    public double getMaxValue() { return doubleColumn().getMaxValue(); }

    public double getMaxPosition() {
        if (orientation == Orientation.HORIZONTAL) {
            return boundingBox.getMinY();
        } else {
            return boundingBox.getMaxX();
        }
    }

    public double getMinPosition() {
        if (orientation == Orientation.HORIZONTAL) {
            return boundingBox.getMaxY();
        } else {
            return boundingBox.getMinX();
        }
    }

    private DoubleColumn doubleColumn() { return (DoubleColumn)column; }
}
