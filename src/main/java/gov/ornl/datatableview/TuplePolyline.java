package gov.ornl.datatableview;

import gov.ornl.datatable.Tuple;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TuplePolyline {
    public static Logger log = Logger.getLogger(TuplePolyline.class.getName());

    private Tuple tuple;
    private Color color;
    private double[] xPoints;
    private double[] yPoints;

    public TuplePolyline(Tuple tuple) {
        this.tuple = tuple;
    }

    public Color getColor() { return color; }

    public void setColor(Color c) { this.color = c; }

    public Tuple getTuple() { return tuple; }

    public void layout(ArrayList<Axis> axisList) {
        xPoints = new double[tuple.getElementCount()];
        yPoints = new double[tuple.getElementCount()];

        for (int i = 0; i < tuple.getElementCount(); i++) {
            Axis axis = axisList.get(i);
            if (axis instanceof TemporalAxis) {
//                PCPTemporalAxis temporalAxis = (PCPTemporalAxis)axis;
//                Instant instant = (Instant)tuple.getElement(i);
//                double yPosition = GraphicsUtil.mapValue(instant,
//                        ((TemporalColumn)axis.getColumn()).getStatistics().getStartInstant(),
//                        ((TemporalColumn)axis.getColumn()).getStatistics().getEndInstant(),
//                        temporalAxis.getFocusBottomY(),
//                        temporalAxis.getFocusTopY());
//                xPoints[i] = temporalAxis.getCenterX();
//                yPoints[i] = yPosition;
            } else if (axis instanceof DoubleAxis) {
                DoubleAxis doubleAxis = (DoubleAxis)axis;
                double value = (Double)tuple.getElement(i);
                double yPosition = GraphicsUtil.mapValue(value,
                        doubleAxis.doubleColumn().getStatistics().getMinValue(),
                        doubleAxis.doubleColumn().getStatistics().getMaxValue(),
                        doubleAxis.getFocusMinPosition(), doubleAxis.getFocusMaxPosition());
                xPoints[i] = axis.getCenterX();
                yPoints[i] = yPosition;
            } else if (axis instanceof CategoricalAxis) {
//                CategoricalAxis categoricalAxis = (CategoricalAxis)axis;
//                String category = (String)tuple.getElement(i);
////                Rectangle rectangle;
////                if (!axis.dataModel.getActiveQuery().hasColumnSelections()) {
////                    log.info("nothing is queried");
////                    rectangle = categoricalAxis.getCategoryRectangle(category);
////                } else {
////                    if (tuple.getQueryFlag()) {
////                        log.info("tuple queried");
////                        rectangle = categoricalAxis.getQueryCategoryRectangle(category);
////                    } else {
////                        log.info("tuple nonqueried");
////                        rectangle = categoricalAxis.getNonQueryCategoryRectangle(category);
////                    }
////                }
//                Rectangle rectangle = categoricalAxis.getCategoryRectangle(category);
//                xPoints[i] = axis.getCenterX();
//                yPoints[i] = rectangle.getY() + (rectangle.getHeight() / 2.);
            }
        }
    }

    public double[] getXPoints() { return xPoints; }

    public double[] getYPoints() { return yPoints; }
}
