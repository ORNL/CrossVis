package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TuplePolyline {
    public static Logger log = Logger.getLogger(TuplePolyline.class.getName());

    private Tuple tuple;
    private Color color;
//    private ArrayList<Double> xPoints = new ArrayList<>();
//    private ArrayList<Double> yPoints = new ArrayList<>();
//    private boolean inContext = false;
    private double xPoints[];
    private double yPoints[];

    public TuplePolyline(Tuple tuple) {
        this.tuple = tuple;
    }

//    public boolean isInContext() { return inContext; }

    public Color getColor() { return color; }

    public void setColor(Color c) { this.color = c; }

    public Tuple getTuple() { return tuple; }

    public void layout(ArrayList<Axis> axisList) {
//        xPoints = new double[tuple.getElementCount()];
//        yPoints = new double[tuple.getElementCount()];
//        xPoints.clear();
//        yPoints.clear();
        xPoints = new double[axisList.size()];
        yPoints = new double[axisList.size()];

        for (int i = 0; i < tuple.getElementCount(); i++) {
//        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            Axis axis = axisList.get(i);
            if (axis instanceof TemporalAxis) {
                TemporalAxis temporalAxis = (TemporalAxis)axis;
//                Instant instant = (Instant)tuple.getElement(i);
                Instant instant = (Instant)tuple.getElement(i);

                double yPosition;
                if (instant.isBefore(temporalAxis.temporalColumn().getStartFocusValue())) {
                    // in lower context region
                    yPosition = temporalAxis.getLowerContextBar().getY() + (temporalAxis.getLowerContextBar().getHeight() / 2.);
//                    inContext = true;
                } else if (instant.isAfter(temporalAxis.temporalColumn().getEndFocusValue())) {
                    // in upper context region
                    yPosition = temporalAxis.getUpperContextBar().getY() + (temporalAxis.getUpperContextBar().getHeight() / 2.);
//                    inContext = true;
                } else {
                    // in focus region
                    yPosition = GraphicsUtil.mapValue(instant, temporalAxis.temporalColumn().getStartFocusValue(),
                            temporalAxis.temporalColumn().getEndFocusValue(), temporalAxis.getMinFocusPosition(),
                            temporalAxis.getMaxFocusPosition());
                }
//                double yPosition = GraphicsUtil.mapValue(instant,
//                        ((TemporalColumn)axis.getColumn()).getStatistics().getStartInstant(),
//                        ((TemporalColumn)axis.getColumn()).getStatistics().getEndInstant(),
//                        temporalAxis.getMinFocusPosition(),
//                        temporalAxis.getMaxFocusPosition());
                xPoints[i] = temporalAxis.getCenterX();
                yPoints[i] = yPosition;
//                xPoints.add(temporalAxis.getCenterX());
//                yPoints.add(yPosition);
            } else if (axis instanceof DoubleAxis) {
                DoubleAxis doubleAxis = (DoubleAxis)axis;
//                double value = (Double)tuple.getElement(i);
                double value = (Double)tuple.getElement(i);

                double yPosition;
//                if (value < doubleAxis.getMinFocusValue()) {
                if (value < doubleAxis.doubleColumn().getMinimumFocusValue()) {
                    // in lower context region
                    yPosition = doubleAxis.getLowerContextBar().getY() + (doubleAxis.getLowerContextBar().getHeight() / 2.);
//                    inContext = true;
                } else if (value > doubleAxis.doubleColumn().getMaximumFocusValue()) {
//                } else if (value > doubleAxis.getMaxFocusValue()) {
                    // in upper context region
                    yPosition = doubleAxis.getUpperContextBar().getY() + (doubleAxis.getUpperContextBar().getHeight() / 2.);
//                    inContext = true;
                } else {
                    // in focus region
                    yPosition = GraphicsUtil.mapValue(value, doubleAxis.doubleColumn().getMinimumFocusValue(),
                            doubleAxis.doubleColumn().getMaximumFocusValue(),
                            doubleAxis.getMinFocusPosition(),
                            doubleAxis.getMaxFocusPosition());
//                    yPosition = GraphicsUtil.mapValue(value, doubleAxis.getMinFocusValue(), doubleAxis.getMaxFocusValue(),
//                            doubleAxis.getMinFocusPosition(), doubleAxis.getMaxFocusPosition());
                }

//                double yPosition = GraphicsUtil.mapValue(value,
//                        doubleAxis.doubleColumn().getStatistics().getMinValue(),
//                        doubleAxis.doubleColumn().getStatistics().getMaxValue(),
//                        doubleAxis.getMinFocusPosition(), doubleAxis.getMaxFocusPosition());
                xPoints[i] = axis.getCenterX();
                yPoints[i] = yPosition;
//                xPoints.add(axis.getCenterX());
//                yPoints.add(yPosition);
            } else if (axis instanceof CategoricalAxis) {
                CategoricalAxis categoricalAxis = (CategoricalAxis) axis;
//                String category = (String) tuple.getElement(i);
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
//                Rectangle rectangle = categoricalAxis.getCategoryRectangle(category);
//                yPoints[i] = rectangle.getY() + (rectangle.getHeight() / 2.);
                xPoints[i] = axis.getCenterX();
                yPoints[i] = categoricalAxis.getAxisPositionForValue(category);
//                xPoints.add(axis.getCenterX());
//                yPoints.add(categoricalAxis.getAxisPositionForValue(category));
            } else if (axis instanceof BivariateAxis) {
                BivariateAxis biAxis = (BivariateAxis) axis;
//                Object values[] = (Object[])tuple.getElement(i);
                Object values[] = (Object[]) tuple.getElement(i);

                xPoints[i] = biAxis.getScatterplot().getPlotBounds().getMinX();
//                xPoints.add(biAxis.getScatterplot().getPlotBounds().getMinX());
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

                Column yColumn = ((BivariateColumn) biAxis.getColumn()).getColumn2();

                if (yColumn instanceof DoubleColumn) {
                    double yValue = (double) values[1];
                    yPoints[i] = GraphicsUtil.mapValue(yValue,
                            (double) biAxis.getScatterplot().getYAxisMinValue(),
                            (double) biAxis.getScatterplot().getYAxisMaxValue(),
                            biAxis.getScatterplot().getPlotBounds().getMaxY(),
                            biAxis.getScatterplot().getPlotBounds().getMinY());
//                    yPoints.add(GraphicsUtil.mapValue(yValue,
//                            (double)biAxis.getScatterplot().getYAxisMinValue(),
//                            (double)biAxis.getScatterplot().getYAxisMaxValue(),
//                            biAxis.getScatterplot().getPlotBounds().getMaxY(),
//                            biAxis.getScatterplot().getPlotBounds().getMinY()));
                } else {
                    yPoints[i] = biAxis.getCenterY();
//                    yPoints.add(biAxis.getCenterY());
                }
            } else if (axis instanceof ImageAxis) {
                ImageAxis imageAxis = (ImageAxis)axis;
                Pair<File,Image> imagePair = (Pair<File,Image>)tuple.getElement(i);
                xPoints[i] = axis.getCenterX();
                yPoints[i] = imageAxis.getAxisPositionForValue(imagePair);
            } else {
//                double x = axis.getCenterX();
//                double y = axis.getCenterY();
                xPoints[i] = axis.getCenterX();
                yPoints[i] = axis.getCenterY();
//                xPoints.add(axis.getCenterX());
//                yPoints.add(axis.getCenterY());
            }
        }
    }

    public double[] getXPoints() { return xPoints; }

    public double[] getYPoints() { return yPoints; }
}
