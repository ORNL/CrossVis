package gov.ornl.datatableview;

import javafx.scene.Group;

import java.util.logging.Logger;

public abstract class AxisSelection {
    private final static Logger log = Logger.getLogger(AxisSelection.class.getName());

    protected Axis axis;
    protected Group graphicsGroup = new Group();

//    public AxisSelection(Axis axis) {
//        this.axis = axis;
//    }

    public abstract void layout();

    public Group getGraphicsGroup() { return graphicsGroup; }

    public Axis getAxis() { return axis; }
}
