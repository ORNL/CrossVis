package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.QuantitativeColumn;
import gov.ornl.csed.cda.datatable.Tuple;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;

/**
 * Created by csg on 8/24/16.
 */
public class PCPTuple {
    public static final Color DEFAULT_LINE_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.2);

    private Tuple tuple;
    private Paint color;
    private double[] xPoints;
    private double[] yPoints;

    public PCPTuple(Tuple tuple) {
        this.tuple = tuple;
        this.color = DEFAULT_LINE_COLOR;
    }

    public Paint getColor() { return color; }

    public void setStroke(Paint strokePaint) {
        this.color = strokePaint;
    }

    public Tuple getTuple () { return tuple; }

    public void layout(ArrayList<PCPQuantitativeAxis> axisList) {
        xPoints = new double[tuple.getElementCount()];
        yPoints = new double[tuple.getElementCount()];

        for (int i = 0; i < tuple.getElementCount(); i++) {
            double value = tuple.getElement(i);
            PCPQuantitativeAxis axis = axisList.get(i);
            double yPosition = GraphicsUtil.mapValue(value, ((QuantitativeColumn)axis.getColumn()).getSummaryStats().getMin(),
                    ((QuantitativeColumn)axis.getColumn()).getSummaryStats().getMax(), axis.getFocusBottomY(), axis.getFocusTopY());
            xPoints[i] = axis.getCenterX();
            yPoints[i] = yPosition;
        }
    }

    public double[] getXPoints() { return xPoints; }
    public double[] getYPoints() { return yPoints; }
}
