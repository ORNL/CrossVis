package gov.ornl.histogram;

import gov.ornl.util.GraphicsUtil;
import javafx.geometry.BoundingBox;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.text.DecimalFormat;

public class HistogramView extends Region {
    private Pane pane;
    private HistogramDataModel histogramDataModel;
    private PlotAxis valueAxis;
    private PlotAxis binCountAxis;

    private double xAxisHeight = 20;
    private double yAxisWidth = 40;

    private Text titleText;

    private Group binGraphicsGroup;
    private Group axisGraphicsGroup;

    private Orientation orientation;
    private boolean showAxes;

    public HistogramView(Orientation orientation) {
        this(orientation, true);
    }

    public HistogramView(Orientation orientation, boolean showAxes) {
        this.orientation = orientation;
        this.showAxes = showAxes;
        initialize();
        registerListeners();
    }

    public void setTitle(String title) {
        titleText.setText(title);
        titleText.setX(getWidth() / 2. - (titleText.getLayoutBounds().getWidth() / 2.));
        titleText.setY(getInsets().getTop());
    }

    public void setShowAxes(boolean showAxes) {
        if (this.showAxes != showAxes) {
            this.showAxes = showAxes;
            layoutView();
        }
    }

    private void registerListeners() {
        widthProperty().addListener(o -> layoutView());
        heightProperty().addListener(o -> layoutView());
    }

    private void initialize() {
        titleText = new Text();
        titleText.setTextOrigin(VPos.BOTTOM);

        pane = new Pane();
        binGraphicsGroup = new Group();
        axisGraphicsGroup = new Group();
        pane.getChildren().addAll(binGraphicsGroup, axisGraphicsGroup, titleText);
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

        axisGraphicsGroup.getChildren().clear();

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

            axisGraphicsGroup.getChildren().addAll(valueAxis.getGraphicsGroup(), binCountAxis.getGraphicsGroup());

            layoutView();
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

            axisGraphicsGroup.getChildren().addAll(valueAxis.getGraphicsGroup(), binCountAxis.getGraphicsGroup());

            layoutView();
        }
    }

    public HistogramDataModel getHistogramDataModel() {
        return histogramDataModel;
    }

    private void layoutView() {
        binGraphicsGroup.getChildren().clear();

        if (histogramDataModel != null) {
            double plotWidth = getWidth() - (getInsets().getLeft() + getInsets().getRight() + yAxisWidth);
            double plotHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom() + xAxisHeight);

            
            if (plotWidth > 0 && plotHeight > 0) {
                pane.setPrefSize(getWidth(), getHeight());
                pane.setMinWidth(getWidth());
                pane.setMinHeight(getHeight());

                double plotLeft = getInsets().getLeft() + yAxisWidth;
                double plotRight = plotLeft + plotWidth;
                double plotTop = getInsets().getTop() + titleText.getLayoutBounds().getHeight();
                double plotBottom = plotTop + plotHeight;

                titleText.setX(getWidth() / 2. - (titleText.getLayoutBounds().getWidth() / 2.));
                titleText.setY(getInsets().getTop());

                BoundingBox yAxisBounds = new BoundingBox(getInsets().getLeft(), plotTop, yAxisWidth, plotHeight);
                BoundingBox xAxisBounds = new BoundingBox(plotLeft, plotBottom, plotWidth, xAxisHeight);

                if (orientation == Orientation.HORIZONTAL) {
                    valueAxis.layout(xAxisBounds);
                    binCountAxis.layout(yAxisBounds);

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
                    valueAxis.layout(yAxisBounds);
                    binCountAxis.layout(xAxisBounds);

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
