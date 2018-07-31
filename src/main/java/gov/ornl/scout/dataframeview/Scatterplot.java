package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.DoubleColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.util.HashSet;
import java.util.List;

public class Scatterplot {
    private static final double DEFAULT_POINT_STROKE_OPACITY = 0.5;
    private static final Color DEFAULT_AXIS_STROKE_COLOR = Color.LIGHTGRAY;
    private static final Color DEFAULT_AXIS_TEXT_COLOR = Color.BLACK;
    private static final Color DEFAULT_SELECTED_POINT_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_POINT_STROKE_OPACITY);
    private static final Color DEFAULT_UNSELECTED_POINT_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_POINT_STROKE_OPACITY);

    private Axis xAxis;
    private Axis yAxis;

    Group graphicsGroup = new Group();

    public Bounds plotBounds;
    public Bounds xAxisBounds;
    public Bounds yAxisBounds;

    private Rectangle plotRectangle;
    private Text xAxisText;
    private Text yAxisText;

    private Rectangle yAxisRectangle;

    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;

    private HashSet<double[]> points = new HashSet<>();
    private HashSet<double[]> selectedPoints = new HashSet<>();
    private HashSet<double[]> unselectedPoints = new HashSet<>();

    private ObjectProperty<Color> axisStrokeColor = new SimpleObjectProperty<>(DEFAULT_AXIS_STROKE_COLOR);
    private ObjectProperty<Color> axisTextColor = new SimpleObjectProperty<>(DEFAULT_AXIS_TEXT_COLOR);
    private ObjectProperty<Color> selectedPointStrokeColor = new SimpleObjectProperty<>(DEFAULT_SELECTED_POINT_COLOR);
    private ObjectProperty<Color> unselectedPointStrokeColor = new SimpleObjectProperty<>(DEFAULT_UNSELECTED_POINT_COLOR);

    private DoubleProperty axisSize = new SimpleDoubleProperty(12.);
    private DoubleProperty pointSize = new SimpleDoubleProperty(4.);
    private DoubleProperty pointStrokeWidth = new SimpleDoubleProperty(1.2);
    private DoubleProperty pointStrokeOpacity = new SimpleDoubleProperty(DEFAULT_POINT_STROKE_OPACITY);

    private BooleanProperty showSelectedPoints = new SimpleBooleanProperty(true);
    private BooleanProperty showUnselectedPoints = new SimpleBooleanProperty(true);

    public Scatterplot(Axis xAxis, Axis yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;

        selectedCanvas = new Canvas();
        unselectedCanvas = new Canvas();

        plotRectangle = new Rectangle();
        plotRectangle.setStroke(getAxisStrokeColor());
        plotRectangle.setFill(Color.TRANSPARENT);

        yAxisRectangle = new Rectangle();
        yAxisRectangle.setStroke(Color.BLUE);
        yAxisRectangle.setFill(Color.TRANSPARENT);

        xAxisText = new Text(xAxis.column.getTitle());
        xAxisText.setFill(getAxisTextColor());
        xAxisText.setFont(Font.font(10.));
        xAxisText.setTextOrigin(VPos.CENTER);

        yAxisText = new Text(yAxis.column.getTitle());
        yAxisText.setFill(getAxisTextColor());
        yAxisText.setFont(Font.font(10.));
        yAxisText.setTextOrigin(VPos.CENTER);

        graphicsGroup.getChildren().addAll(selectedCanvas, plotRectangle, xAxisText, yAxisText);

        registerListeners();
    }

    private void registerListeners() {
        pointStrokeOpacity.addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                setSelectedPointStrokeColor(new Color(getSelectedPointStrokeColor().getRed(), getSelectedPointStrokeColor().getGreen(),
                        getSelectedPointStrokeColor().getBlue(), newValue.doubleValue()));
                setUnselectedPointStrokeColor(new Color(getUnselectedPointStrokeColor().getRed(), getUnselectedPointStrokeColor().getGreen(),
                        getUnselectedPointStrokeColor().getBlue(), newValue.doubleValue()));
            }
        });

        selectedPointStrokeColor.addListener((observable, oldValue, newValue) -> drawPoints());
        unselectedPointStrokeColor.addListener(((observable, oldValue, newValue) -> drawPoints()));

        showSelectedPoints.addListener(observable -> drawPoints());
        showUnselectedPoints.addListener(observable -> drawPoints());
    }

    public boolean getShowSelectedPoints() { return showSelectedPoints.get(); }

    public void setShowSelectedPoints(boolean showSelectedPoints) { this.showSelectedPoints.set(showSelectedPoints); }

    public BooleanProperty showSelectedPointsProperty() { return showSelectedPoints; }

    public boolean getShowUnselectedPoints() { return showUnselectedPoints.get(); }

    public void setShowUnselectedPoints(boolean showUnselectedPoints) { this.showUnselectedPoints.set(showUnselectedPoints); }

    public BooleanProperty showUnselectedPointsProperty() { return showUnselectedPoints; }

    public double getPointStrokeOpacity() { return pointStrokeOpacity.get(); }

    public void setPointStrokeOpacity(double opacity) { pointStrokeOpacity.set(opacity); }

    public DoubleProperty pointStrokeOpacityProperty() { return pointStrokeOpacity; }

    public Color getSelectedPointStrokeColor () { return selectedPointStrokeColor.get(); }

    public void setSelectedPointStrokeColor(Color color) { selectedPointStrokeColor.set(color); }

    public ObjectProperty<Color> selectedPointStrokeColorProperty() { return selectedPointStrokeColor; }

    public Color getUnselectedPointStrokeColor () { return unselectedPointStrokeColor.get(); }

    public void setUnselectedPointStrokeColor(Color color) { unselectedPointStrokeColor.set(color); }

    public ObjectProperty<Color> unselectedPointStrokeColorProperty() { return unselectedPointStrokeColor; }

    public double getPointSize () { return pointSize.get(); }

    public void setPointSize(double size) { pointSize.set(size); }

    public DoubleProperty pointSizeProperty() { return pointSize; }

    public double getPointStrokeWidth() { return pointStrokeWidth.get(); }

    public void setPointStrokeWidth(double width) { pointStrokeWidth.set(width); }

    public DoubleProperty pointStrokeWidth() { return pointStrokeWidth; }

    public double getAxisSize () { return axisSize.get(); }

    public void setAxisSize(double size) { axisSize.set(size); }

    public DoubleProperty axisSizeProperty() { return axisSize; }

    public Color getAxisStrokeColor() { return axisStrokeColor.get(); }

    public void setAxisStrokeColor(Color color) { axisStrokeColor.set(color); }

    public ObjectProperty<Color> axisStrokeColorProperty() { return axisStrokeColor; }

    public Color getAxisTextColor() { return axisTextColor.get(); }

    public void setAxisTextColor(Color color) { axisTextColor.set(color); }

    public ObjectProperty<Color> axisTextColorProperty () { return axisTextColor; }

    public Axis getXAxis() { return xAxis; }

    public void setXAxis(Axis xAxis) {
        this.xAxis = xAxis;
    }

    public Axis getYAxis() { return yAxis; }

    public void setYAxis(Axis yAxis) {
        this.yAxis = yAxis;
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public void layout(double left, double top, double width, double height) {
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

        calculatePoints();
        drawPoints();
    }

    private void calculatePoints() {
        points.clear();

        if (xAxis instanceof DoubleAxis && yAxis instanceof DoubleAxis) {
            List<Double> xValues = ((DoubleAxis)xAxis).column.getValues();
            List<Double> yValues = ((DoubleAxis)yAxis).column.getValues();

            double xRangePadding = (((DoubleColumn)xAxis.column).getMaxValue() - ((DoubleColumn)xAxis.column).getMinValue()) * .05;
            double xMaxValue = ((DoubleColumn)xAxis.column).getMaxValue() + xRangePadding;
            double xMinValue = ((DoubleColumn)xAxis.column).getMinValue() - xRangePadding;

            double yRangePadding = (((DoubleColumn)yAxis.column).getMaxValue() - ((DoubleColumn)yAxis.column).getMinValue()) * .05;
            double yMaxValue = ((DoubleColumn)yAxis.column).getMaxValue() + yRangePadding;
            double yMinValue = ((DoubleColumn)yAxis.column).getMinValue() - yRangePadding;

            for (int i = 0; i < xValues.size(); i++) {
                double point[] = new double[2];
                point[0] = GraphicsUtil.mapValue(xValues.get(i), xMinValue, xMaxValue, 0, plotBounds.getWidth());
                point[1] = GraphicsUtil.mapValue(yValues.get(i), yMinValue, yMaxValue, plotBounds.getHeight(), 0.);
                points.add(point);
            }
        }
    }

    public void drawPoints() {
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(getPointStrokeWidth());

        Color color = new Color(getSelectedPointStrokeColor().getRed(), getSelectedPointStrokeColor().getGreen(),
                getSelectedPointStrokeColor().getBlue(), getPointStrokeOpacity());
        for (double[] point : points) {
            selectedCanvas.getGraphicsContext2D().setStroke(color);
            selectedCanvas.getGraphicsContext2D().strokeOval(point[0] - getPointSize()/2.,
                    point[1] - getPointSize()/2., getPointSize(), getPointSize());
        }
    }
}
