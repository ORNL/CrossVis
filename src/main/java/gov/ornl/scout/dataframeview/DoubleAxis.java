package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.DoubleColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;


public class DoubleAxis extends Axis {

    public DoubleAxis(DataFrameView dataTableView, Column column, Orientation orientation) {
        super(dataTableView, column, orientation);
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        return getDoubleValueForAxisPosition(axisPosition);
    }

    public double getDoubleValueForAxisPosition(double axisPosition) {
        return GraphicsUtil.mapValue(axisPosition, getMinPosition(), getMaxPosition(), getMinValue(), getMaxValue());
    }

    @Override
    protected void handleAxisBarMousePressed(MouseEvent event) {

    }

    @Override
    protected void handleAxisBarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;
        }

        dragEndPoint = new Point2D(event.getX(), event.getY());

        if (orientation == Orientation.HORIZONTAL) {
//            double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
//            double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());
            double selectionMaxY = dragStartPoint.getY() < dragEndPoint.getY() ? dragStartPoint.getY() : dragEndPoint.getY();
            double selectionMinY = dragStartPoint.getY() > dragEndPoint.getY() ? dragStartPoint.getY() : dragEndPoint.getY();

            selectionMaxY = selectionMaxY < getMaxPosition() ? getMaxPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getMinPosition() ? getMinPosition() : selectionMinY;

            double maxSelectionValue = getDoubleValueForAxisPosition(selectionMaxY);
            double minSelectionValue = getDoubleValueForAxisPosition(selectionMinY);
        } else {
            double selectionMinX = dragStartPoint.getX() < dragEndPoint.getX() ? dragStartPoint.getX() : dragEndPoint.getX();
            double selectionMaxX = dragStartPoint.getX() > dragEndPoint.getX() ? dragStartPoint.getX() : dragEndPoint.getX();

            selectionMinX = selectionMinX < getMinPosition() ? getMinPosition() : selectionMinX;
            selectionMaxX = selectionMaxX > getMaxPosition() ? getMaxPosition() : selectionMaxX;

            double maxSelectionValue = getDoubleValueForAxisPosition(selectionMaxX);
            double minSelectionValue = getDoubleValueForAxisPosition(selectionMinX);
        }
    }

    @Override
    protected void handleAxisBarMouseReleased(MouseEvent event) {

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
