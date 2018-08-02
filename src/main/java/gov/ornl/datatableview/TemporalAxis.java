package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.TemporalColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;

import java.time.Instant;

public class TemporalAxis extends Axis {

    public TemporalAxis(DataTableView dataFrameView, Column column, Orientation orientation) {
        super(dataFrameView, column, orientation);
    }

    public TemporalColumn temporalColumn() { return (TemporalColumn)column; }

    public Instant getStartInstant() { return temporalColumn().getStatistics().getStartInstant(); }

    public Instant getEndInstant() { return temporalColumn().getStatistics().getEndInstant(); }

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

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        return getInstantForAxisPosition(axisPosition);
    }

    public Instant getInstantForAxisPosition(double axisPosition) {
        return GraphicsUtil.mapValue(axisPosition, getMinPosition(), getMaxPosition(), getStartInstant(), getEndInstant());
    }

    @Override
    protected void handleAxisBarMousePressed(MouseEvent event) {

    }

    @Override
    protected void handleAxisBarMouseDragged(MouseEvent event) {

    }

    @Override
    protected void handleAxisBarMouseReleased(MouseEvent event) {

    }
}
