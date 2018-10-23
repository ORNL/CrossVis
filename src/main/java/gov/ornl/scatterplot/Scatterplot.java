package gov.ornl.scatterplot;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class Scatterplot {
    private static final Logger log = Logger.getLogger(Scatterplot.class.getName());

    private static final double DEFAULT_POINT_STROKE_OPACITY = 0.5;
    private static final Color DEFAULT_AXIS_STROKE_COLOR = Color.LIGHTGRAY;
    private static final Color DEFAULT_AXIS_TEXT_COLOR = Color.BLACK;
    private static final Color DEFAULT_SELECTED_POINT_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_POINT_STROKE_OPACITY);
    private static final Color DEFAULT_UNSELECTED_POINT_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_POINT_STROKE_OPACITY);

    private Column xColumn;
    private Column yColumn;
    private DataTable dataTable;

    Group graphicsGroup = new Group();

    public Bounds plotBounds;
    public Bounds xAxisBounds;
    public Bounds yAxisBounds;

    private Rectangle plotRectangle;
    private Text xAxisText;
    private Text yAxisText;

    private Rectangle yAxisRectangle;
    private Rectangle xAxisRectangle;

    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;

    private ArrayList<double[]> points = new ArrayList<>();
    private HashSet<double[]> selectedPoints = new HashSet<>();
    private HashSet<double[]> unselectedPoints = new HashSet<>();

    private Color axisStrokeColor = DEFAULT_AXIS_STROKE_COLOR;
    private Color axisTextColor = DEFAULT_AXIS_TEXT_COLOR;
    private Color selectedPointStrokeColor = DEFAULT_SELECTED_POINT_COLOR;
    private Color unselectedPointStrokeColor = DEFAULT_UNSELECTED_POINT_COLOR;

    private double axisSize = 12.;
    private double pointSize = 4.;
    private double pointStrokeWidth = 1.2;
    private double pointStrokeOpacity = DEFAULT_POINT_STROKE_OPACITY;

    private boolean showSelectedPoints = true;
    private boolean showUnselectedPoints = true;

    private boolean dragging = false;
    private Point2D dragStartPoint;
    private Point2D dragEndPoint;
    private Rectangle dragRectangle;
    private BoundingBox dragBounds;

    private Bounds dataBounds;

    // range values for double crossvis
    private double xAxisMinDoubleValue;
    private double xAxisMaxDoubleValue;
    private double yAxisMinDoubleValue;
    private double yAxisMaxDoubleValue;

    // range values for temporal crossvis
    private Instant xAxisStartInstant;
    private Instant xAxisEndInstant;
    private Instant yAxisStartInstant;
    private Instant yAxisEndInstant;

//    private ArrayList<EventHandler> selectionEventHandlers = new ArrayList<>();

    public Scatterplot(Column xColumn, Column yColumn) {
        this.xColumn = xColumn;
        this.yColumn = yColumn;
        this.dataTable = xColumn.getDataModel();

        selectedCanvas = new Canvas();
        unselectedCanvas = new Canvas();

        plotRectangle = new Rectangle();
        plotRectangle.setStroke(getAxisStrokeColor());
        plotRectangle.setFill(Color.TRANSPARENT);

        yAxisRectangle = new Rectangle();
        yAxisRectangle.setStroke(Color.BLUE);
        yAxisRectangle.setFill(Color.TRANSPARENT);

        xAxisText = new Text(xColumn.getName());
        xAxisText.setFill(getAxisTextColor());
        xAxisText.setFont(Font.font(10.));
        xAxisText.setTextOrigin(VPos.CENTER);

        yAxisText = new Text(yColumn.getName());
        yAxisText.setFill(getAxisTextColor());
        yAxisText.setFont(Font.font(10.));
        yAxisText.setTextOrigin(VPos.CENTER);

        graphicsGroup.getChildren().addAll(unselectedCanvas, selectedCanvas, plotRectangle, xAxisText, yAxisText);

        dragRectangle = new Rectangle();
        dragRectangle.setStroke(Color.DARKGRAY);
        dragRectangle.setFill(Color.gray(0.7, 0.2));
        dragRectangle.setMouseTransparent(true);

        Button button = new Button();
        button.setOnAction(event -> {

        });
        registerListeners();
    }

//    public void setOnSelectionDragged(EventHandler handler) {
//        if (!selectionEventHandlers.contains(handler)) {
//            selectionEventHandlers.add(handler);
//        }
//    }

    public Rectangle getPlotRectangle() { return plotRectangle; }

    public Bounds getPlotBounds() { return plotBounds; }

    public Bounds getXAxisBounds() { return xAxisBounds; }

    public Bounds getYAxisBounds() { return yAxisBounds; }

    public Object getXAxisMaxValue() {
        if (xColumn instanceof DoubleColumn) {
            return xAxisMaxDoubleValue;
        } else if (xColumn instanceof TemporalColumn) {
            return xAxisEndInstant;
        }

        return null;
    }


    public Object getXAxisMinValue() {
        if (xColumn instanceof DoubleColumn) {
            return xAxisMinDoubleValue;
        } else if (xColumn instanceof TemporalColumn) {
            return xAxisStartInstant;
        }

        return null;
    }

    public Object getYAxisMaxValue() {
        if (yColumn instanceof DoubleColumn) {
            return yAxisMaxDoubleValue;
        } else if (yColumn instanceof TemporalColumn) {
            return yAxisEndInstant;
        }

        return null;
    }


    public Object getYAxisMinValue() {
        if (yColumn instanceof DoubleColumn) {
            return yAxisMinDoubleValue;
        } else if (yColumn instanceof TemporalColumn) {
            return yAxisStartInstant;
        }

        return null;
    }

    public double getxAxisMaxDoubleValue() {
        return xAxisMaxDoubleValue;
    }

    private void registerListeners() {
//        pointStrokeOpacity.addListener((observable, oldValue, newValue) -> {
//            if (newValue != oldValue) {
//                setSelectedPointStrokeColor(new Color(getSelectedPointStrokeColor().getRed(), getSelectedPointStrokeColor().getGreen(),
//                        getSelectedPointStrokeColor().getBlue(), newValue.doubleValue()));
//                setUnselectedPointStrokeColor(new Color(getUnselectedPointStrokeColor().getRed(), getUnselectedPointStrokeColor().getGreen(),
//                        getUnselectedPointStrokeColor().getBlue(), newValue.doubleValue()));
//            }
//        });

//        selectedPointStrokeColor.addListener((observable, oldValue, newValue) -> drawPoints());
//        unselectedPointStrokeColor.addListener(((observable, oldValue, newValue) -> drawPoints()));

//        showSelectedPoints.addListener(observable -> drawPoints());
//        showUnselectedPoints.addListener(observable -> drawPoints());

//        plotRectangle.setOnMouseClicked(event -> {
//            if (!event.isDragDetect()) {
//                log.info("Removing x and y column selections");
//                // if there are column selections for either the x or y column, remove them
//                if (dataTable.getActiveQuery().hasColumnSelections()) {
//                    dataTable.getActiveQuery().removeColumnSelectionRanges(xColumn);
//                    dataTable.getActiveQuery().removeColumnSelectionRanges(yColumn);
//                }
//            }
//        });

        plotRectangle.setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
        });

        plotRectangle.setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                graphicsGroup.getChildren().add(dragRectangle);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            double boundsLeft;
            double boundsRight;
            double boundsTop;
            double boundsBottom;
            if (dragStartPoint.getX() < dragEndPoint.getX()) {
                boundsLeft = dragStartPoint.getX();
                boundsRight = dragEndPoint.getX();
            } else {
                boundsLeft = dragEndPoint.getX();
                boundsRight = dragStartPoint.getX();
            }
            if (dragStartPoint.getY() < dragEndPoint.getY()) {
                boundsTop = dragStartPoint.getY();
                boundsBottom = dragEndPoint.getY();
            } else {
                boundsTop = dragEndPoint.getY();
                boundsBottom = dragStartPoint.getY();
            }

            if (boundsLeft < plotBounds.getMinX()) {
                boundsLeft = plotBounds.getMinX();
            }
            if (boundsRight > plotBounds.getMaxX()) {
                boundsRight = plotBounds.getMaxX();
            }
            if (boundsTop < plotBounds.getMinY()) {
                boundsTop = plotBounds.getMinY();
            }
            if (boundsBottom > plotBounds.getMaxY()) {
                boundsBottom = plotBounds.getMaxY();
            }

            dragBounds = new BoundingBox(boundsLeft, boundsTop, boundsRight - boundsLeft, boundsBottom - boundsTop);
            dragRectangle.setX(dragBounds.getMinX());
            dragRectangle.setY(dragBounds.getMinY());
            dragRectangle.setWidth(dragBounds.getWidth());
            dragRectangle.setHeight(dragBounds.getHeight());
        });

        plotRectangle.setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                graphicsGroup.getChildren().remove(dragRectangle);

                ColumnSelection xColumnSelection = null;
                if (xColumn instanceof DoubleColumn) {
                    // get x column min and max values
                    double minXValue = GraphicsUtil.mapValue(dragBounds.getMinX(), plotBounds.getMinX(), plotBounds.getMaxX(),
                            xAxisMinDoubleValue, xAxisMaxDoubleValue);
                    double maxXValue = GraphicsUtil.mapValue(dragBounds.getMaxX(), plotBounds.getMinX(), plotBounds.getMaxX(),
                            xAxisMinDoubleValue, xAxisMaxDoubleValue);
                    xColumnSelection = new DoubleColumnSelectionRange((DoubleColumn) xColumn, minXValue, maxXValue);
                } else if (xColumn instanceof TemporalColumn) {
                    Instant startInstant = GraphicsUtil.mapValue(dragBounds.getMinX(), plotBounds.getMinX(), plotBounds.getMaxX(),
                            xAxisStartInstant, xAxisEndInstant);
                    Instant endInstant = GraphicsUtil.mapValue(dragBounds.getMaxX(), plotBounds.getMinX(), plotBounds.getMaxX(),
                            xAxisStartInstant, xAxisEndInstant);
                    xColumnSelection = new TemporalColumnSelectionRange((TemporalColumn)xColumn,
                            startInstant, endInstant);
                }

                if (xColumnSelection != null) { dataTable.addColumnSelectionRangeToActiveQuery(xColumnSelection); }

                ColumnSelection yColumnSelection = null;
                if (yColumn instanceof DoubleColumn) {
                    // get y column min and max values
                    double minYValue = GraphicsUtil.mapValue(dragBounds.getMaxY(), plotBounds.getMinY(), plotBounds.getMaxY(),
                            yAxisMaxDoubleValue, yAxisMinDoubleValue);
                    double maxYValue = GraphicsUtil.mapValue(dragBounds.getMinY(), plotBounds.getMinY(), plotBounds.getMaxY(),
                            yAxisMaxDoubleValue, yAxisMinDoubleValue);
                    yColumnSelection = new DoubleColumnSelectionRange((DoubleColumn) yColumn, minYValue, maxYValue);
                } else if (yColumn instanceof TemporalColumn) {
                    Instant startInstant = GraphicsUtil.mapValue(dragBounds.getMaxY(), plotBounds.getMinY(), plotBounds.getMaxY(),
                            yAxisEndInstant, yAxisStartInstant);
                    Instant endInstant = GraphicsUtil.mapValue(dragBounds.getMinY(), plotBounds.getMinY(), plotBounds.getMaxY(),
                            yAxisEndInstant, yAxisStartInstant);
                    yColumnSelection = new TemporalColumnSelectionRange((TemporalColumn)yColumn, startInstant, endInstant);
                }

                if (yColumnSelection != null) { dataTable.addColumnSelectionRangeToActiveQuery(yColumnSelection); }
            } else {
                log.info("Removing x and y column selections");
                // if there are column selections for either the x or y column, remove them
                if (dataTable.getActiveQuery().hasColumnSelections()) {
                    dataTable.removeColumnSelectionsFromActiveQuery(xColumn);
                    dataTable.removeColumnSelectionsFromActiveQuery(yColumn);
                }
            }
        });
    }

//    private void fireSelectionEventHandlers() {
//        for (EventHandler eventHandler : selectionEventHandlers) {
//            eventHandler.handle(new ActionEvent(this, null));
//        }
//    }

    public void resize(double left, double top, double width, double height) {
        yAxisBounds = new BoundingBox(left, top, getAxisSize(), height - getAxisSize());
        xAxisBounds = new BoundingBox(yAxisBounds.getMaxX(), yAxisBounds.getMaxY(), width - getAxisSize(), getAxisSize());
        plotBounds = new BoundingBox(xAxisBounds.getMinX(), yAxisBounds.getMinY(), xAxisBounds.getWidth(), yAxisBounds.getHeight());

        yAxisRectangle.setX(yAxisBounds.getMinX());
        yAxisRectangle.setY(yAxisBounds.getMinY());
        yAxisRectangle.setWidth(yAxisBounds.getWidth());
        yAxisRectangle.setHeight(yAxisBounds.getHeight());

        plotRectangle.setX(plotBounds.getMinX());
        plotRectangle.setY(plotBounds.getMinY());
        plotRectangle.setWidth(plotBounds.getWidth());
        plotRectangle.setHeight(plotBounds.getHeight());

        double textX = (xAxisBounds.getMinX() + xAxisBounds.getWidth() / 2.) - xAxisText.getLayoutBounds().getWidth() / 2.;
        double textY = (xAxisBounds.getMinY() + xAxisBounds.getHeight() / 2.);
        xAxisText.setLayoutX(textX);
        xAxisText.setLayoutY(textY);

        textX = (yAxisBounds.getMinX() + yAxisBounds.getWidth() / 2.) - yAxisText.getLayoutBounds().getWidth() / 2.;
        textY = (yAxisBounds.getMinY() + yAxisBounds.getHeight() / 2.);
        yAxisText.setLayoutX(textX);
        yAxisText.setLayoutY(textY);
        yAxisText.setRotate(-90.);

        selectedCanvas.setWidth(plotBounds.getWidth());
        selectedCanvas.setHeight(plotBounds.getHeight());
        selectedCanvas.setLayoutX(plotBounds.getMinX());
        selectedCanvas.setLayoutY(plotBounds.getMinY());

        unselectedCanvas.setWidth(plotBounds.getWidth());
        unselectedCanvas.setHeight(plotBounds.getHeight());
        unselectedCanvas.setLayoutX(plotBounds.getMinX());
        unselectedCanvas.setLayoutY(plotBounds.getMinY());

        calculatePoints();
        drawPoints();
    }

    public void fillSelectionPointSets() {
        selectedPoints.clear();
        unselectedPoints.clear();

        if (xColumn.getDataModel().getActiveQuery().hasColumnSelections()) {
            for (int i = 0; i < points.size(); i++) {
                if (xColumn.getDataModel().getTuple(i).getQueryFlag()) {
                    selectedPoints.add(points.get(i));
                } else {
                    unselectedPoints.add(points.get(i));
                }
            }
        } else {
            selectedPoints.addAll(points);
        }
    }

    private void calculatePoints() {
        points.clear();

        double xMinValuePosition = 0;
        double xMaxValuePosition = 0;
        double yMinValuePosition = 0;
        double yMaxValuePosition = 0;

        if (xColumn instanceof DoubleColumn) {
            double xRangePadding = (((DoubleColumn) xColumn).getStatistics().getMaxValue() -
                    ((DoubleColumn) xColumn).getStatistics().getMinValue()) * .05;
            xAxisMaxDoubleValue = ((DoubleColumn) xColumn).getStatistics().getMaxValue() + xRangePadding;
            xAxisMinDoubleValue = ((DoubleColumn) xColumn).getStatistics().getMinValue() - xRangePadding;

            xMinValuePosition = GraphicsUtil.mapValue(((DoubleColumn)xColumn).getStatistics().getMinValue(), xAxisMinDoubleValue, xAxisMaxDoubleValue, 0, plotBounds.getWidth());
            xMaxValuePosition = GraphicsUtil.mapValue(((DoubleColumn)xColumn).getStatistics().getMaxValue(), xAxisMinDoubleValue, xAxisMaxDoubleValue, 0, plotBounds.getWidth());
        } else if (xColumn instanceof TemporalColumn) {
            long xRangePaddingMillis = (long)(Duration.between(((TemporalColumn)xColumn).getStatistics().getStartInstant(),
                    ((TemporalColumn)xColumn).getStatistics().getEndInstant()).toMillis() * .05);
            xAxisStartInstant = ((TemporalColumn)xColumn).getStatistics().getStartInstant().minusMillis(xRangePaddingMillis);
            xAxisEndInstant = ((TemporalColumn)xColumn).getStatistics().getEndInstant().plusMillis(xRangePaddingMillis);

            xMinValuePosition = GraphicsUtil.mapValue(((TemporalColumn)xColumn).getStatistics().getStartInstant(), xAxisStartInstant, xAxisEndInstant, 0, plotBounds.getWidth());
            xMaxValuePosition = GraphicsUtil.mapValue(((TemporalColumn)xColumn).getStatistics().getEndInstant(), xAxisStartInstant, xAxisEndInstant, 0, plotBounds.getWidth());
        }

        if (yColumn instanceof DoubleColumn) {
            double yRangePadding = (((DoubleColumn) yColumn).getStatistics().getMaxValue() -
                    ((DoubleColumn) yColumn).getStatistics().getMinValue()) * .05;
            yAxisMaxDoubleValue = ((DoubleColumn) yColumn).getStatistics().getMaxValue() + yRangePadding;
            yAxisMinDoubleValue = ((DoubleColumn) yColumn).getStatistics().getMinValue() - yRangePadding;

            yMinValuePosition = GraphicsUtil.mapValue(((DoubleColumn)yColumn).getStatistics().getMinValue(), yAxisMinDoubleValue, yAxisMaxDoubleValue, 0, plotBounds.getHeight());
            yMaxValuePosition = GraphicsUtil.mapValue(((DoubleColumn)yColumn).getStatistics().getMaxValue(), yAxisMinDoubleValue, yAxisMaxDoubleValue, 0, plotBounds.getHeight());
        } else if (yColumn instanceof TemporalColumn) {
            long yRangePaddingMillis = (long)(Duration.between(((TemporalColumn)yColumn).getStatistics().getStartInstant(),
                    ((TemporalColumn)yColumn).getStatistics().getEndInstant()).toMillis() * .05);
            yAxisStartInstant = ((TemporalColumn)yColumn).getStatistics().getStartInstant().minusMillis(yRangePaddingMillis);
            yAxisEndInstant = ((TemporalColumn)yColumn).getStatistics().getEndInstant().plusMillis(yRangePaddingMillis);

            yMinValuePosition = GraphicsUtil.mapValue(((TemporalColumn)yColumn).getStatistics().getStartInstant(), yAxisStartInstant, yAxisEndInstant, 0, plotBounds.getHeight());
            yMaxValuePosition = GraphicsUtil.mapValue(((TemporalColumn)yColumn).getStatistics().getEndInstant(), yAxisStartInstant, yAxisEndInstant, 0, plotBounds.getHeight());
        }

        dataBounds = new BoundingBox(xMinValuePosition, yMinValuePosition, xMaxValuePosition - xMinValuePosition,
                yMaxValuePosition - yMinValuePosition);

        if (xColumn instanceof DoubleColumn && yColumn instanceof DoubleColumn) {
            double xValues[] = ((DoubleColumn)xColumn).getValues();
            double yValues[] = ((DoubleColumn)yColumn).getValues();

            for (int i = 0; i < xValues.length; i++) {
                double point[] = new double[2];
                point[0] = GraphicsUtil.mapValue(xValues[i], xAxisMinDoubleValue, xAxisMaxDoubleValue, 0, plotBounds.getWidth());
                point[1] = GraphicsUtil.mapValue(yValues[i], yAxisMinDoubleValue, yAxisMaxDoubleValue, plotBounds.getHeight(), 0.);
                points.add(point);
            }
        } else if (xColumn instanceof TemporalColumn && yColumn instanceof TemporalColumn) {
            Instant xValues[] = ((TemporalColumn)xColumn).getValues();
            Instant yValues[] = ((TemporalColumn)yColumn).getValues();

            for (int i = 0; i < xValues.length; i++) {
                double point[] = new double[2];
                point[0] = GraphicsUtil.mapValue(xValues[i], xAxisStartInstant, xAxisEndInstant, 0, plotBounds.getWidth());
                point[1] = GraphicsUtil.mapValue(yValues[i], yAxisStartInstant, yAxisEndInstant, plotBounds.getHeight(), 0.);
                points.add(point);
            }
        } else if (xColumn instanceof DoubleColumn && yColumn instanceof TemporalColumn) {
            double xValues[] = ((DoubleColumn) xColumn).getValues();
            Instant yValues[] = ((TemporalColumn) yColumn).getValues();

            for (int i = 0; i < xValues.length; i++) {
                double point[] = new double[2];
                point[0] = GraphicsUtil.mapValue(xValues[i], xAxisMinDoubleValue, xAxisMaxDoubleValue, 0, plotBounds.getWidth());
                point[1] = GraphicsUtil.mapValue(yValues[i], yAxisStartInstant, yAxisEndInstant, plotBounds.getHeight(), 0.);
                points.add(point);
            }
        } else if (xColumn instanceof TemporalColumn && yColumn instanceof DoubleColumn) {
            Instant xValues[] = ((TemporalColumn) xColumn).getValues();
            double yValues[] = ((DoubleColumn) yColumn).getValues();

            for (int i = 0; i < xValues.length; i++) {
                double point[] = new double[2];
                point[0] = GraphicsUtil.mapValue(xValues[i], xAxisStartInstant, xAxisEndInstant, 0, plotBounds.getWidth());
                point[1] = GraphicsUtil.mapValue(yValues[i], yAxisMinDoubleValue, yAxisMaxDoubleValue, plotBounds.getHeight(), 0.);
                points.add(point);
            }
        }

        fillSelectionPointSets();
    }

    public Bounds getDataBounds() { return dataBounds; }

    public void drawPoints() {
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, unselectedCanvas.getWidth(), unselectedCanvas.getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(getPointStrokeWidth());
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(getPointStrokeWidth());

        if (getShowUnselectedPoints()) {
            Color color = new Color(getUnselectedPointStrokeColor().getRed(), getUnselectedPointStrokeColor().getGreen(),
                    getUnselectedPointStrokeColor().getBlue(), getPointStrokeOpacity());
            for (double[] point : unselectedPoints) {
                unselectedCanvas.getGraphicsContext2D().setStroke(color);
                unselectedCanvas.getGraphicsContext2D().strokeOval(point[0] - getPointSize() / 2.,
                        point[1] - getPointSize() / 2., getPointSize(), getPointSize());
            }
        }

        if (getShowSelectedPoints()) {
            Color color = new Color(getSelectedPointStrokeColor().getRed(), getSelectedPointStrokeColor().getGreen(),
                    getSelectedPointStrokeColor().getBlue(), getPointStrokeOpacity());
            for (double[] point : selectedPoints) {
                selectedCanvas.getGraphicsContext2D().setStroke(color);
                selectedCanvas.getGraphicsContext2D().strokeOval(point[0] - getPointSize() / 2.,
                        point[1] - getPointSize() / 2., getPointSize(), getPointSize());
            }
        }
    }

    public boolean getShowSelectedPoints() { return showSelectedPoints; }

    public void setShowSelectedPoints(boolean showSelectedPoints) {
        this.showSelectedPoints = showSelectedPoints;
        drawPoints();
    }

//    public boolean showSelectedPointsProperty() { return showSelectedPoints; }

    public boolean getShowUnselectedPoints() { return showUnselectedPoints; }

    public void setShowUnselectedPoints(boolean showUnselectedPoints) {
        this.showUnselectedPoints = showUnselectedPoints;
        drawPoints();
    }

    public boolean showUnselectedPointsProperty() { return showUnselectedPoints; }

    public double getPointStrokeOpacity() { return pointStrokeOpacity; }

    public void setPointStrokeOpacity(double opacity) {
        pointStrokeOpacity = opacity;
        setSelectedPointStrokeColor(new Color(getSelectedPointStrokeColor().getRed(), getSelectedPointStrokeColor().getGreen(),
                getSelectedPointStrokeColor().getBlue(), opacity));
        setUnselectedPointStrokeColor(new Color(getUnselectedPointStrokeColor().getRed(), getUnselectedPointStrokeColor().getGreen(),
                getUnselectedPointStrokeColor().getBlue(), opacity));
        drawPoints();
    }

//    public DoubleProperty pointStrokeOpacityProperty() { return pointStrokeOpacity; }

    public Color getSelectedPointStrokeColor () { return selectedPointStrokeColor; }

    public void setSelectedPointStrokeColor(Color color) {
        selectedPointStrokeColor = color;
        drawPoints();
    }

//    public ObjectProperty<Color> selectedPointStrokeColorProperty() { return selectedPointStrokeColor; }

    public Color getUnselectedPointStrokeColor () { return unselectedPointStrokeColor; }

    public void setUnselectedPointStrokeColor(Color color) {
        unselectedPointStrokeColor = color;
        drawPoints();
    }

//    public ObjectProperty<Color> unselectedPointStrokeColorProperty() { return unselectedPointStrokeColor; }

    public double getPointSize () { return pointSize; }

    public void setPointSize(double size) {
        pointSize = size;
        drawPoints();
    }

//    public DoubleProperty pointSizeProperty() { return pointSize; }

    public double getPointStrokeWidth() { return pointStrokeWidth; }

    public void setPointStrokeWidth(double width) {
        pointStrokeWidth = width;
        drawPoints();
    }

//    public DoubleProperty pointStrokeWidth() { return pointStrokeWidth; }

    public double getAxisSize () { return axisSize; }

    public void setAxisSize(double size) {
        axisSize = size;
        drawPoints();
    }

//    public DoubleProperty axisSizeProperty() { return axisSize; }

    public Color getAxisStrokeColor() { return axisStrokeColor; }

    public void setAxisStrokeColor(Color color) {
        axisStrokeColor = color;
        drawPoints();
    }

//    public ObjectProperty<Color> axisStrokeColorProperty() { return axisStrokeColor; }

    public Color getAxisTextColor() { return axisTextColor; }

    public void setAxisTextColor(Color color) {
        axisTextColor = color;
        drawPoints();
    }

//    public ObjectProperty<Color> axisTextColorProperty () { return axisTextColor; }

    public Column getXColumn() { return xColumn; }

    public void setXColumn(Column xColumn) {
        this.xColumn = xColumn;
        drawPoints();
    }

    public Column getYColumn() { return yColumn; }

    public void setYColumn(Column yColumn) {
        this.yColumn = yColumn;
        drawPoints();
    }

    public Group getGraphicsGroup() { return graphicsGroup; }
}
