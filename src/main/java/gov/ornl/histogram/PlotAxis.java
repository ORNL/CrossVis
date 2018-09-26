package gov.ornl.histogram;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.HashSet;

public abstract class PlotAxis {
    protected Line axisLine;
    protected HashSet<Line> tickLines;
    protected double tickLineLength = 8;
    protected Orientation orientation;
    protected double fontSize = 10;

    protected Group graphicsGroup;

    public PlotAxis(Orientation orientation) {
        this.orientation = orientation;
        graphicsGroup = new Group();
        axisLine = new Line();
        axisLine.setStroke(Color.GRAY);
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public void layout(Bounds bounds) {
        if (orientation == Orientation.HORIZONTAL) {
            axisLine.setStartX(bounds.getMinX());
            axisLine.setStartY(bounds.getMinY());
            axisLine.setEndX(bounds.getMaxX());
            axisLine.setEndY(bounds.getMinY());
        } else {
            axisLine.setStartX(bounds.getMaxX());
            axisLine.setStartY(bounds.getMaxY());
            axisLine.setEndX(bounds.getMaxX());
            axisLine.setEndY(bounds.getMinY());
        }
    }

    public Group getGraphicsGroup() { return graphicsGroup; }
}
