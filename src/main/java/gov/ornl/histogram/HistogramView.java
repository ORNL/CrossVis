package gov.ornl.histogram;

import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.text.DecimalFormat;

public class HistogramView extends Region {
    private Pane pane;
    private HistogramDataModel histogramDataModel;
    private PlotAxis valueAxis;
    private PlotAxis binCountAxis;

    private DoubleProperty xAxisHeight = new SimpleDoubleProperty(20.);
    private DoubleProperty yAxisWidth = new SimpleDoubleProperty(30.);
    private DoubleProperty axisFontSize = new SimpleDoubleProperty(10.);
    private DoubleProperty titleFontSize = new SimpleDoubleProperty(12.);

    private Text titleText;

    private Group binGraphicsGroup;
    private Group axisGraphicsGroup;

//    private Rectangle plotAreaRectangle;
//    private Rectangle yAxisRectangle;
//    private Rectangle xAxisRectangle;
    private BoundingBox plotBounds;

    private Orientation orientation;
    private boolean showAxes;
    private boolean showTitle;

    private Point2D startDragPoint;
    private Point2D endDragPoint;
    private boolean dragging;
    private Rectangle draggingRectangle;

    public HistogramView(Orientation orientation) {
        this(orientation, true, true);
    }

    public HistogramView(Orientation orientation, boolean showAxes, boolean showTitle) {
        this.orientation = orientation;
        this.showAxes = showAxes;
        this.showTitle = showTitle;
        initialize();
        registerListeners();
    }

    public void setTitle(String title) {
//        showTitle = true;
        titleText.setText(title);
        titleText.setX(getInsets().getLeft());
        titleText.setY(getInsets().getTop() + titleText.getLayoutBounds().getHeight() - 2);
//        layoutView();
    }

    public boolean getShowAxes() {
        return showAxes;
    }

    public boolean getShowTitle() {
        return showTitle;
    }

    public void setShowAxes(boolean showAxes) {
        if (this.showAxes != showAxes) {
            this.showAxes = showAxes;
            if (showAxes) {
                pane.getChildren().add(axisGraphicsGroup);
            } else {
                pane.getChildren().remove(axisGraphicsGroup);
            }
            layoutView();
        }
    }

    public void setShowTitle(boolean showTitle) {
        if (this.showTitle != showTitle) {
            this.showTitle = showTitle;
            if (showTitle) {
                pane.getChildren().add(titleText);
            } else {
                pane.getChildren().remove(titleText);
            }
            layoutView();
        }
    }

    private void registerListeners() {
        widthProperty().addListener(o -> layoutView());
        heightProperty().addListener(o -> layoutView());
        xAxisHeight.addListener(o -> layoutView());
        yAxisWidth.addListener(o -> layoutView());
        titleFontSize.addListener(o -> titleText.setFont(Font.font(titleFontSize.get())));
        axisFontSize.addListener(o -> {
            binCountAxis.setFontSize(axisFontSize.get());
            valueAxis.setFontSize(axisFontSize.get());
        });

        setOnMousePressed(event -> {
            if (plotBounds.contains(event.getX(), event.getY())) {
                startDragPoint = new Point2D(event.getX(), event.getY());
            }
        });

        setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                pane.getChildren().remove(draggingRectangle);
            }
        });

        setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                pane.getChildren().add(draggingRectangle);
            }

            endDragPoint = new Point2D(event.getX(), event.getY());

            // find min and max x for dragging points
            double minDragX = startDragPoint.getX() < endDragPoint.getX() ? startDragPoint.getX() : endDragPoint.getX();
            double maxDragX = startDragPoint.getX() > endDragPoint.getX() ? startDragPoint.getX() : endDragPoint.getX();

            // clamp to plot boundaries
            minDragX = minDragX < plotBounds.getMinX() ? plotBounds.getMinX() : minDragX;
            maxDragX = maxDragX > plotBounds.getMaxX() ? plotBounds.getMaxX() : maxDragX;

            draggingRectangle.setX(minDragX);
            draggingRectangle.setWidth(maxDragX - minDragX);
            draggingRectangle.setY(plotBounds.getMinY());
            draggingRectangle.setHeight(plotBounds.getHeight());
        });
    }

    private void initialize() {
        this.plotBounds = new BoundingBox(0, 0, 0, 0);

        draggingRectangle = new Rectangle();
        draggingRectangle.setStroke(Color.BLACK);
        draggingRectangle.setFill(Color.TRANSPARENT);

        titleText = new Text();
        titleText.setTextOrigin(VPos.BOTTOM);

        pane = new Pane();
        binGraphicsGroup = new Group();
        axisGraphicsGroup = new Group();

        // for debugging
//        plotAreaRectangle = new Rectangle();
//        plotAreaRectangle.setFill(Color.TRANSPARENT);
//        plotAreaRectangle.setStroke(Color.BLUE);
//        plotAreaRectangle.setStrokeWidth(2.);
//        xAxisRectangle = new Rectangle();
//        xAxisRectangle.setFill(Color.TRANSPARENT);
//        xAxisRectangle.setStroke(Color.RED);
//        yAxisRectangle = new Rectangle();
//        yAxisRectangle.setFill(Color.TRANSPARENT);
//        yAxisRectangle.setStroke(Color.RED);

        pane.getChildren().addAll(binGraphicsGroup, axisGraphicsGroup);
        if (showTitle) {
            pane.getChildren().add(titleText);
        }
        getChildren().add(pane);
    }

    public void clear() {
        binGraphicsGroup.getChildren().clear();
        axisGraphicsGroup.getChildren().clear();
        valueAxis = null;
        binCountAxis = null;
        titleText.setText("");
        histogramDataModel = null;
    }

    public void setHistogramDataModel(HistogramDataModel histogramDataModel) {
        this.histogramDataModel = histogramDataModel;
        createAxes();
        layoutView();
    }

    public double getAxisFontSize() { return axisFontSize.get(); }

    public void setAxisFontSize(double size) { axisFontSize.set(size); }

    public double getTitleFontSize() { return titleFontSize.get(); }

    public void setTitleFontSize(double size) { titleFontSize.set(size); }

    public double getXAxisHeight() { return xAxisHeight.get(); }

    public void setXAxisHeight(double height) { xAxisHeight.set(height); }

    public double getYAxisWidth() { return yAxisWidth.get(); }

    public void setYAxisWidth(double width) { yAxisWidth.set(width); }

    private void createAxes() {
        axisGraphicsGroup.getChildren().clear();

        if (histogramDataModel != null) {
            if (histogramDataModel instanceof DoubleHistogramDataModel) {
                Number minValueNumber = ((DoubleHistogramDataModel) histogramDataModel).getMinValue();
                Number maxValueNumber = ((DoubleHistogramDataModel) histogramDataModel).getMaxValue();
                DecimalFormat valueAxisNumberFormat = new DecimalFormat();

                Number minCountNumber = 0;
                Number maxCountNumber = histogramDataModel.getMaxBinCount();
                DecimalFormat countAxisNumberFormat = new DecimalFormat();
                countAxisNumberFormat.setParseIntegerOnly(true);

                if (orientation == Orientation.HORIZONTAL) {
                    valueAxis = new DoublePlotAxis(Orientation.HORIZONTAL, minValueNumber, maxValueNumber, valueAxisNumberFormat);
                    binCountAxis = new DoublePlotAxis(Orientation.VERTICAL, minCountNumber, maxCountNumber, countAxisNumberFormat);
                } else {
                    valueAxis = new DoublePlotAxis(Orientation.VERTICAL, minValueNumber, maxValueNumber, valueAxisNumberFormat);
                    binCountAxis = new DoublePlotAxis(Orientation.HORIZONTAL, minCountNumber, maxCountNumber, countAxisNumberFormat);
                }

                valueAxis.setFontSize(getAxisFontSize());
                binCountAxis.setFontSize(getAxisFontSize());

                axisGraphicsGroup.getChildren().addAll(valueAxis.getGraphicsGroup(), binCountAxis.getGraphicsGroup());
            } else if (histogramDataModel instanceof CategoricalHistogramDataModel) {
                Number minCountNumber = 0;
                Number maxCountNumber = histogramDataModel.getMaxBinCount();
                DecimalFormat countAxisNumberFormat = new DecimalFormat();
                countAxisNumberFormat.setParseIntegerOnly(true);

                if (orientation == Orientation.HORIZONTAL) {
                    valueAxis = new CategoricalPlotAxis(Orientation.HORIZONTAL,
                            ((CategoricalHistogramDataModel)histogramDataModel).getCategories());
                    binCountAxis = new DoublePlotAxis(Orientation.VERTICAL, minCountNumber, maxCountNumber, countAxisNumberFormat);
                } else {
                    valueAxis = new CategoricalPlotAxis(Orientation.VERTICAL,
                            ((CategoricalHistogramDataModel)histogramDataModel).getCategories());
                    binCountAxis = new DoublePlotAxis(Orientation.HORIZONTAL, minCountNumber, maxCountNumber, countAxisNumberFormat);
                }

                valueAxis.setFontSize(getAxisFontSize());
                binCountAxis.setFontSize(getAxisFontSize());

                axisGraphicsGroup.getChildren().addAll(valueAxis.getGraphicsGroup(), binCountAxis.getGraphicsGroup());
            }
        }
    }

    public HistogramDataModel getHistogramDataModel() {
        return histogramDataModel;
    }

    private void layoutView() {
        binGraphicsGroup.getChildren().clear();

        if (histogramDataModel != null) {
            double plotWidth = getWidth() - (getInsets().getLeft() + getInsets().getRight());
            double plotHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

            if (showAxes) {
                plotWidth -= getYAxisWidth();
                plotHeight -= getXAxisHeight();
            }

            if (showTitle) {
                plotHeight -= (titleText.getLayoutBounds().getHeight() + 2);
            }

            if (plotWidth > 0 && plotHeight > 0) {
                pane.setPrefSize(getWidth(), getHeight());
                pane.setMinWidth(getWidth());
                pane.setMinHeight(getHeight());

                double plotLeft = showAxes ? getInsets().getLeft() + getYAxisWidth() : getInsets().getLeft();
                double plotRight = plotLeft + plotWidth;
                double plotTop = showTitle ? getInsets().getTop() + (titleText.getLayoutBounds().getHeight() + 2) : getInsets().getTop();
//                double plotTop = getInsets().getTop() + titleText.getLayoutBounds().getHeight();
                double plotBottom = plotTop + plotHeight;

                plotBounds = new BoundingBox(plotLeft, plotTop, plotWidth, plotHeight);

//                plotAreaRectangle.setX(plotLeft);
//                plotAreaRectangle.setY(plotTop);
//                plotAreaRectangle.setWidth(plotRight - plotLeft);
//                plotAreaRectangle.setHeight(plotBottom - plotTop);

                if (showTitle) {
                    titleText.setX(plotLeft);
                    titleText.setY(getInsets().getTop() + titleText.getLayoutBounds().getHeight() - 2);
                }

                if (showAxes) {
                    BoundingBox yAxisBounds = new BoundingBox(getInsets().getLeft(), plotTop, getYAxisWidth(), plotHeight);
                    BoundingBox xAxisBounds = new BoundingBox(plotLeft, plotBottom, plotWidth, getXAxisHeight());

//                    xAxisRectangle.setX(xAxisBounds.getMinX());
//                    xAxisRectangle.setWidth(xAxisBounds.getWidth());
//                    xAxisRectangle.setY(xAxisBounds.getMinY());
//                    xAxisRectangle.setHeight(xAxisBounds.getHeight());
//
//                    yAxisRectangle.setX(yAxisBounds.getMinX());
//                    yAxisRectangle.setWidth(yAxisBounds.getWidth());
//                    yAxisRectangle.setY(yAxisBounds.getMinY());
//                    yAxisRectangle.setHeight(yAxisBounds.getHeight());

                    if (orientation == Orientation.HORIZONTAL) {
                        valueAxis.layout(xAxisBounds);
                        binCountAxis.layout(yAxisBounds);
                    } else {
                        valueAxis.layout(yAxisBounds);
                        binCountAxis.layout(xAxisBounds);
                    }
                }

                if (orientation == Orientation.HORIZONTAL) {
//                    if (showAxes) {
//                        valueAxis.resize(xAxisBounds);
//                        binCountAxis.resize(yAxisBounds);
//                    }

                    double binWidth = plotWidth / histogramDataModel.getNumBins();

                    for (int i = 0; i < histogramDataModel.getNumBins(); i++) {
                        int count = histogramDataModel.getBinCount(i);

                        double binTop = GraphicsUtil.mapValue(count, 0., histogramDataModel.getMaxBinCount(),
                                plotBottom, plotTop);
                        double binHeight = plotBottom - binTop;
                        double binLeft = GraphicsUtil.mapValue(i, 0, histogramDataModel.getNumBins(),
                                plotLeft, plotRight);
                        HistogramBin bin = new HistogramBin(count);
                        bin.layout(binLeft, binTop, binWidth, binHeight);

                        binGraphicsGroup.getChildren().add(bin.getGraphicsGroup());
                    }
                } else {
//                    if (showAxes) {
//                        valueAxis.resize(yAxisBounds);
//                        binCountAxis.resize(xAxisBounds);
//                    }

                    double binHeight = plotHeight / histogramDataModel.getNumBins();

                    for (int i = 0; i < histogramDataModel.getNumBins(); i++) {
                        int count = histogramDataModel.getBinCount(i);

                        double binLeft = plotLeft;
                        double binRight = GraphicsUtil.mapValue(count, 0., histogramDataModel.getMaxBinCount(),
                                plotLeft, plotRight);
                        double binWidth = binRight - binLeft;
                        double binBottom = GraphicsUtil.mapValue(i, 0, histogramDataModel.getNumBins(),
                                plotBottom, plotTop);
                        double binTop = binBottom - binHeight;

                        HistogramBin bin = new HistogramBin(count);
                        bin.layout(binLeft, binTop, binWidth, binHeight);

                        binGraphicsGroup.getChildren().add(bin.getGraphicsGroup());
                    }
                }
            }
        }
    }
}
