package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.DoubleColumnSelectionRange;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import java.util.logging.Logger;


public class DoubleAxis extends Axis {
    private static final Logger log = Logger.getLogger(DoubleAxis.class.getName());

    DoubleAxisRangeSelection draggingAxisRangeSelection;

    public DoubleAxis(DataTableView dataTableView, Column column, Orientation orientation) {
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

            if (draggingAxisRangeSelection == null) {
                DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
                draggingAxisRangeSelection = new DoubleAxisRangeSelection(this, selectionRange, selectionMinY, selectionMaxY);
                getGraphicsGroup().getChildren().add(draggingAxisRangeSelection.getGraphicsGroup());
            } else {
                draggingAxisRangeSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
            }
        } else {
            double selectionMinX = dragStartPoint.getX() < dragEndPoint.getX() ? dragStartPoint.getX() : dragEndPoint.getX();
            double selectionMaxX = dragStartPoint.getX() > dragEndPoint.getX() ? dragStartPoint.getX() : dragEndPoint.getX();

            selectionMinX = selectionMinX < getMinPosition() ? getMinPosition() : selectionMinX;
            selectionMaxX = selectionMaxX > getMaxPosition() ? getMaxPosition() : selectionMaxX;

            double maxSelectionValue = getDoubleValueForAxisPosition(selectionMaxX);
            double minSelectionValue = getDoubleValueForAxisPosition(selectionMinX);

            if (draggingAxisRangeSelection == null) {
                DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
                draggingAxisRangeSelection = new DoubleAxisRangeSelection(this, selectionRange,
                        selectionMinX, selectionMaxX);
                getGraphicsGroup().getChildren().add(draggingAxisRangeSelection.getGraphicsGroup());
            } else {
                draggingAxisRangeSelection.update(minSelectionValue, maxSelectionValue, selectionMinX, selectionMaxX);
            }
        }
    }

    @Override
    protected void handleAxisBarMouseReleased(MouseEvent event) {
        if (draggingAxisRangeSelection != null) {
            getAxisSelectionList().add(draggingAxisRangeSelection);
            getDataTable().addColumnSelectionRangeToActiveQuery(draggingAxisRangeSelection.getColumnSelectionRange());
            dragging = false;
            draggingAxisRangeSelection = null;
        }
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);
    }

    public double getMinValue() { return doubleColumn().getStatistics().getMinValue(); }

    public double getMaxValue() { return doubleColumn().getStatistics().getMaxValue(); }

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
