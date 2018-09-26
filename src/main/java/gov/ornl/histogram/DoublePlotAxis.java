package gov.ornl.histogram;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.text.NumberFormat;

public class DoublePlotAxis extends PlotAxis {
    private Text minText;
    private Text maxText;
    private Number minValue;
    private Number maxValue;
    private Line minTickLine;
    private Line maxTickLine;
    private double tickLineLength = 8;
    private NumberFormat numberFormat;

    public DoublePlotAxis(Orientation orientation, Number minValue, Number maxValue, NumberFormat numberFormat) {
        super(orientation);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.numberFormat = numberFormat;

        initialize();
    }

    private void initialize() {
        minText = new Text(numberFormat.format(minValue));
        minText.setFont(Font.font(fontSize));
        maxText = new Text(numberFormat.format(maxValue));
        maxText.setFont(Font.font(fontSize));
        if (orientation == Orientation.HORIZONTAL) {
            minText.setTextOrigin(VPos.TOP);
            maxText.setTextOrigin(VPos.TOP);
        } else if (orientation == Orientation.VERTICAL) {
            minText.setTextOrigin(VPos.CENTER);
            maxText.setTextOrigin(VPos.CENTER);
        }

        minTickLine = new Line();
        minTickLine.setStroke(Color.GRAY);
        maxTickLine = new Line();
        maxTickLine.setStroke(Color.GRAY);
    }

    public void layout(Bounds bounds) {
        super.layout(bounds);
        if (orientation == Orientation.HORIZONTAL) {
            minText.setX(bounds.getMinX() - (minText.getLayoutBounds().getWidth() / 2.));
            minText.setY(bounds.getMinY() + tickLineLength + 1);
            maxText.setX(bounds.getMaxX() - (maxText.getLayoutBounds().getWidth() / 2.));
            maxText.setY(bounds.getMinY() + tickLineLength + 1);

            minTickLine.setStartX(bounds.getMinX());
            minTickLine.setStartY(bounds.getMinY());
            minTickLine.setEndX(bounds.getMinX());
            minTickLine.setEndY(bounds.getMinY() + tickLineLength);

            maxTickLine.setStartX(bounds.getMaxX());
            maxTickLine.setStartY(bounds.getMinY());
            maxTickLine.setEndX(bounds.getMaxX());
            maxTickLine.setEndY(bounds.getMinY() + tickLineLength);
        } else {
            minText.setX(bounds.getMaxX() - (2 + tickLineLength + minText.getLayoutBounds().getWidth()));
            minText.setY(bounds.getMaxY());
            maxText.setX(bounds.getMaxX() - (2 + tickLineLength + maxText.getLayoutBounds().getWidth()));
            maxText.setY(bounds.getMinY());

            minTickLine.setStartX(bounds.getMaxX());
            minTickLine.setStartY(bounds.getMaxY());
            minTickLine.setEndX(bounds.getMaxX() - tickLineLength);
            minTickLine.setEndY(bounds.getMaxY());

            maxTickLine.setStartX(bounds.getMaxX());
            maxTickLine.setStartY(bounds.getMinY());
            maxTickLine.setEndX(bounds.getMaxX() - tickLineLength);
            maxTickLine.setEndY(bounds.getMinY());
        }

        if (graphicsGroup.getChildren().isEmpty()) {
            graphicsGroup.getChildren().addAll(axisLine, minTickLine, maxTickLine, minText, maxText);
        }
    }
}
