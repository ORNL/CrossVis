package gov.ornl.histogram;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class HistogramBin {
    protected Rectangle binRectangle;
    protected int count;
    protected Group graphicsGroup;

    public HistogramBin (int count) {
        this.count = count;

        binRectangle = new Rectangle();
        binRectangle.setStrokeWidth(1.);
        binRectangle.setStroke(Color.BLACK);
        binRectangle.setFill(Color.STEELBLUE);

        graphicsGroup = new Group();
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public void layout(double left, double top, double width, double height) {
        binRectangle.setX(left);
        binRectangle.setY(top);
        binRectangle.setWidth(width);
        binRectangle.setHeight(height);

        if (graphicsGroup.getChildren().isEmpty()) {
            graphicsGroup.getChildren().add(binRectangle);
        }
    }
}
