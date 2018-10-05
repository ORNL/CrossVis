package gov.ornl.pcpview;

import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.TemporalColumn;
import gov.ornl.datatable.Tuple;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PCPTuple {
    public static Logger log = Logger.getLogger(PCPTuple.class.getName());

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

    public void layout(ArrayList<PCPUnivariateAxis> axisList) {
        xPoints = new double[tuple.getElementCount()];
        yPoints = new double[tuple.getElementCount()];

        for (int i = 0; i < tuple.getElementCount(); i++) {
            PCPUnivariateAxis axis = axisList.get(i);
            if (axis instanceof PCPTemporalAxis) {
                PCPTemporalAxis temporalAxis = (PCPTemporalAxis)axis;
                Instant instant = (Instant)tuple.getElement(i);
                double yPosition = GraphicsUtil.mapValue(instant,
                        ((TemporalColumn)axis.getColumn()).getStatistics().getStartInstant(),
                        ((TemporalColumn)axis.getColumn()).getStatistics().getEndInstant(),
                        temporalAxis.getFocusBottomY(),
                        temporalAxis.getFocusTopY());
                xPoints[i] = temporalAxis.getCenterX();
                yPoints[i] = yPosition;
            } else if (axis instanceof PCPDoubleAxis) {
                PCPDoubleAxis quantitativeAxis = (PCPDoubleAxis)axis;
                double value = (Double)tuple.getElement(i);
                double yPosition = GraphicsUtil.mapValue(value,
                        ((DoubleColumn) axis.getColumn()).getStatistics().getMinValue(),
                        ((DoubleColumn) axis.getColumn()).getStatistics().getMaxValue(),
                        quantitativeAxis.getFocusBottomY(), quantitativeAxis.getFocusTopY());
                xPoints[i] = axis.getCenterX();
                yPoints[i] = yPosition;
            } else if (axis instanceof PCPCategoricalAxis) {
                PCPCategoricalAxis categoricalAxis = (PCPCategoricalAxis)axis;
                String category = (String)tuple.getElement(i);
//                Rectangle rectangle;
//                if (!axis.dataModel.getActiveQuery().hasColumnSelections()) {
//                    log.info("nothing is queried");
//                    rectangle = categoricalAxis.getCategoryRectangle(category);
//                } else {
//                    if (tuple.getQueryFlag()) {
//                        log.info("tuple queried");
//                        rectangle = categoricalAxis.getQueryCategoryRectangle(category);
//                    } else {
//                        log.info("tuple nonqueried");
//                        rectangle = categoricalAxis.getNonQueryCategoryRectangle(category);
//                    }
//                }
                Rectangle rectangle = categoricalAxis.getCategoryRectangle(category);
                xPoints[i] = axis.getCenterX();
                yPoints[i] = rectangle.getY() + (rectangle.getHeight() / 2.);
            }
        }
    }

    public double[] getXPoints() { return xPoints; }

    public double[] getYPoints() { return yPoints; }
}
