package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.scatterplot.Scatterplot;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.Instant;
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
                TemporalAxis temporalAxis = (TemporalAxis)axis;
                Instant instant = (Instant)tuple.getElement(i);
                double yPosition = GraphicsUtil.mapValue(instant,
                        ((TemporalColumn)axis.getColumn()).getStatistics().getStartInstant(),
                        ((TemporalColumn)axis.getColumn()).getStatistics().getEndInstant(),
                        temporalAxis.getFocusMinPosition(),
                        temporalAxis.getFocusMaxPosition());
                xPoints[i] = temporalAxis.getCenterX();
                yPoints[i] = yPosition;
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
                CategoricalAxis categoricalAxis = (CategoricalAxis)axis;
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
            } else if (axis instanceof BivariateAxis) {
                BivariateAxis biAxis = (BivariateAxis)axis;
                Object values[] = (Object[])tuple.getElement(i);

                xPoints[i] = biAxis.getScatterplot().getPlotBounds().getMinX();
//                Column xColumn = ((BivariateColumn)biAxis.getColumn()).getColumn1();

//                if (xColumn instanceof DoubleColumn) {
//                    DoubleColumn xDoubleColumn = (DoubleColumn)xColumn;
//                    double xValue = (double)values[0];
//                    xPoints[i] = GraphicsUtil.mapValue(xValue,
//                            (double)biAxis.getScatterplot().getXAxisMinValue(),
//                            (double)biAxis.getScatterplot().getXAxisMaxValue(),
//                            biAxis.getScatterplot().getPlotBounds().getMinX(),
//                            biAxis.getScatterplot().getPlotBounds().getMaxX());
//                } else {
//                    xPoints[i] = biAxis.getCenterX();
//                }

                Column yColumn = ((BivariateColumn)biAxis.getColumn()).getColumn2();

                if (yColumn instanceof DoubleColumn) {
                    DoubleColumn doubleColumn = (DoubleColumn)yColumn;
                    double yValue = (double)values[1];
                    yPoints[i] = GraphicsUtil.mapValue(yValue,
                            (double)biAxis.getScatterplot().getYAxisMinValue(),
                            (double)biAxis.getScatterplot().getYAxisMaxValue(),
                            biAxis.getScatterplot().getPlotBounds().getMaxY(),
                            biAxis.getScatterplot().getPlotBounds().getMinY());
                } else {
                    yPoints[i] = biAxis.getCenterY();
                }
            } else {
                double x = axis.getCenterX();
                double y = axis.getCenterY();
                xPoints[i] = x;
                yPoints[i] = y;
            }
        }
    }

    public double[] getXPoints() { return xPoints; }

    public double[] getYPoints() { return yPoints; }
}
