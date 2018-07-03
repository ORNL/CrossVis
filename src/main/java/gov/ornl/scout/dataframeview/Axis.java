package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.Column;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.logging.Logger;

public abstract class Axis {
    public final static Logger log = Logger.getLogger(Axis.class.getName());

    protected DataFrameView dataTableView;
    protected Column column;

    protected Orientation orientation;

    protected double centerX;
    protected double centerY;
    protected BoundingBox boundingBox;
//    protected BoundingBox plotBoundingBox;

    protected Group graphicsGroup;

//    protected Text titleText;
    protected Line axisLine;

//    private ObjectProperty<Color> titleTextColor = new SimpleObjectProperty<>(DataFrameViewDefaultSettings.DEFAULT_LABEL_COLOR);

    protected BooleanProperty highlighted;

    public Axis(DataFrameView dataTableView, Column column, Orientation orientation) {
        this.dataTableView = dataTableView;
        this.column = column;
        this.orientation = orientation;

        axisLine = new Line();
        axisLine.setStroke(Color.DARKGRAY);
        axisLine.setStrokeWidth(2.);

        graphicsGroup = new Group(axisLine);

        registerListeners();
    }

    public Bounds getBounds() { return boundingBox; }

    public double getCenterX() { return centerX; }

    public double getCenterY() { return centerY; }

    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
//            layout();
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    protected void registerListeners() {

    }

    public void resize(double left, double top, double width, double height) {
        boundingBox = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

//        double plotTop = top;
//        plotBoundingBox = new BoundingBox(left, plotTop, width, height - plotTop);

        layout();
    }

    protected void layout() {
        if (orientation == Orientation.HORIZONTAL) {
            axisLine.setStartX(centerX);
            axisLine.setStartY(boundingBox.getMinY());
            axisLine.setEndX(centerX);
            axisLine.setEndY(boundingBox.getMaxY());
        } else {
            axisLine.setStartX(boundingBox.getMinX());
            axisLine.setStartY(centerY);
            axisLine.setEndX(boundingBox.getMaxX());
            axisLine.setEndY(centerY);
        }
    }
}
