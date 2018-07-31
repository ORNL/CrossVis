package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.TemporalColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;

import java.time.Instant;

public class TemporalAxis extends Axis {

    public TemporalAxis(DataFrameView dataFrameView, Column column, Orientation orientation) {
        super(dataFrameView, column, orientation);
    }

    public TemporalColumn temporalColumn() { return (TemporalColumn)column; }

    public Instant getStartInstant() { return temporalColumn().getStartInstant(); }

    public Instant getEndInstant() { return temporalColumn().getEndInstant(); }

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
