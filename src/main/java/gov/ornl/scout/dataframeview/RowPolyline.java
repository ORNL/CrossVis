package gov.ornl.scout.dataframeview;

import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.scene.paint.Paint;

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
                            doubleAxis.getMaxPosition(), doubleAxis.getMinPosition());
                    xValues[i] = doubleAxis.getCenterX();
                } else {
                    xValues[i] = GraphicsUtil.mapValue(value, doubleAxis.getMinValue(), doubleAxis.getMaxValue(),
                            doubleAxis.getMinPosition(), doubleAxis.getMaxPosition());
                    yValues[i] = doubleAxis.getCenterY();
                }
            }
        }
    }

    public double[] getXValues() { return xValues; }

    public double[] getyValues() { return yValues; }
}
