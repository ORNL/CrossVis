package gov.ornl.scout.dataframeview;

import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RowPolyline {
    public static Logger log = Logger.getLogger(RowPolyline.class.getName());

    private double[] xValues;
    private double[] yValues;
    private Object[] row;
    private Paint color = DataFrameViewDefaultSettings.DEFAULT_ROW_POLYLINE_COLOR;

    public RowPolyline(Object[] row) {
        this.row = row;
        xValues = new double[row.length];
        yValues = new double[row.length];
    }

    public Paint getColor() { return color; }

    public void setStroke(Paint paint) { this.color = paint; }

    public Object[] getRow() { return row; }

    public void layout(ArrayList<Axis> axisList) {
        for (int i = 0; i < row.length; i++) {
            if (axisList.get(i) instanceof DoubleAxis) {
                DoubleAxis doubleAxis = (DoubleAxis)axisList.get(i);
                double value = (Double)row[i];
                if (doubleAxis.getOrientation() == Orientation.HORIZONTAL) {
                    yValues[i] = GraphicsUtil.mapValue(value, doubleAxis.getMinValue(), doubleAxis.getMaxValue(),
                            doubleAxis.getMinPosition(), doubleAxis.getMaxPosition());
                    xValues[i] = doubleAxis.getCenterX();
                } else {
                    xValues[i] = GraphicsUtil.mapValue(value, doubleAxis.getMinValue(), doubleAxis.getMaxValue(),
                            doubleAxis.getMinPosition(), doubleAxis.getMaxPosition());
                    yValues[i] = doubleAxis.getCenterY();
                }
            } else if (axisList.get(i) instanceof TemporalAxis) {
                TemporalAxis temporalAxis = (TemporalAxis)axisList.get(i);
                Instant instantValue = (Instant)row[i];
                if (temporalAxis.getOrientation() == Orientation.HORIZONTAL) {
                    yValues[i] = GraphicsUtil.mapValue(instantValue, temporalAxis.getStartInstant(),
                            temporalAxis.getEndInstant(), temporalAxis.getMinPosition(), temporalAxis.getMaxPosition());
                    xValues[i] = temporalAxis.getCenterX();
                } else {
                    xValues[i] = GraphicsUtil.mapValue(instantValue, temporalAxis.getStartInstant(),
                            temporalAxis.getEndInstant(), temporalAxis.getMinPosition(), temporalAxis.getMaxPosition());
                    yValues[i] = temporalAxis.getCenterY();
                }
            } else if (axisList.get(i) instanceof CategoricalAxis) {
                CategoricalAxis categoricalAxis = (CategoricalAxis)axisList.get(i);
                String categoryValue = (String)row[i];
                Rectangle rectangle = categoricalAxis.getCategoryRectangle(categoryValue);
                if (categoricalAxis.getOrientation() == Orientation.HORIZONTAL) {
                    yValues[i] = rectangle.getY() + (rectangle.getHeight() / 2.);
                    xValues[i] = categoricalAxis.getCenterX();
                } else if (categoricalAxis.getOrientation() == Orientation.VERTICAL) {
                    xValues[i] = rectangle.getX() + (rectangle.getWidth() / 2.);
                    yValues[i] = categoricalAxis.getCenterY();
                }
            }
        }
    }

    public double[] getXValues() { return xValues; }

    public double[] getyValues() { return yValues; }
}
