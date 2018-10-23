package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import javafx.scene.Group;

public abstract class AxisSelection {
    private Axis axis;
    private Group graphicsGroup = new Group();
    private ColumnSelection columnSelection;

    public AxisSelection(Axis axis, ColumnSelection columnSelection) {
        this.axis = axis;
        this.columnSelection = columnSelection;
    }

    public Axis getAxis() { return axis; }

    public ColumnSelection getColumnSelection() { return columnSelection; }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public abstract void resize();
}
