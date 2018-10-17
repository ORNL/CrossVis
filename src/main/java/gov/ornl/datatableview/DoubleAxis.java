package gov.ornl.datatableview;

import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.DoubleColumnSummaryStats;
import gov.ornl.datatable.DoubleHistogram;
import gov.ornl.pcpview.PCPView;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;

import java.util.ArrayList;

public class DoubleAxis extends UnivariateAxis {
    public final static Color DEFAULT_OVERALL_DISPERSION_FILL = DEFAULT_HISTOGRAM_FILL;
    public final static Color DEFAULT_QUERY_DISPERSION_FILL = DEFAULT_QUERY_HISTOGRAM_FILL;
    public final static Color DEFAULT_DISPERSION_STROKE = DEFAULT_HISTOGRAM_STROKE;
    public final static Color DEFAULT_OVERALL_TYPICAL_STROKE = Color.DARKBLUE.darker();
    public final static Color DEFAULT_QUERY_TYPICAL_STROKE = Color.DARKBLUE;

    private Color overallDispersionFill = DEFAULT_OVERALL_DISPERSION_FILL;
    private Color queryDispersionFill = DEFAULT_QUERY_DISPERSION_FILL;
    private Color dispersionStroke = DEFAULT_DISPERSION_STROKE;
    private Color overallTypicalStroke = DEFAULT_OVERALL_TYPICAL_STROKE;
    private Color queryTypicalStroke = DEFAULT_QUERY_TYPICAL_STROKE;

    // summary statistics objects
    private Group overallSummaryStatisticsGroup = new Group();
    private Rectangle overallDispersionRectangle;
    private Line overallTypicalLine;
    private Group querySummaryStatisticsGroup = new Group();
    private Rectangle queryDispersionRectangle;
    private Line queryTypicalLine;
    private Group nonquerySummaryStatisticsGroup = new Group();
    private Rectangle nonqueryDispersionRectangle;
    private Line nonqueryTypicalLine;

    // histogram bin rectangles
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup = new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();
    private Group nonqueryHistogramGroup = new Group();
    private ArrayList<Rectangle> nonqueryHistogramRectangles = new ArrayList<>();

    private Text minValueText;
    private Text maxValueText;

    public DoubleAxis(DataTableView dataTableView, DoubleColumn column) {
        super(dataTableView, column);

        minValueText = new Text();
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.setFill(overallDispersionFill);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.setStroke(overallTypicalStroke);

        overallSummaryStatisticsGroup.getChildren().addAll(overallDispersionRectangle, overallTypicalLine);

        queryDispersionRectangle = new Rectangle();
        Color dispersionFill = new Color(dataTableView.getSelectedItemsColor().getRed(), dataTableView.getSelectedItemsColor().getGreen(),
                dataTableView.getSelectedItemsColor().getBlue(), 1.);
        queryDispersionRectangle.setFill(dispersionFill);
        queryDispersionRectangle.setStroke(Color.DARKGRAY);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.setStroke(queryTypicalStroke);

        querySummaryStatisticsGroup.getChildren().addAll(queryDispersionRectangle, queryTypicalLine);

        nonqueryDispersionRectangle = new Rectangle();
        dispersionFill = new Color(dataTableView.getUnselectedItemsColor().getRed(), dataTableView.getUnselectedItemsColor().getGreen(),
                dataTableView.getUnselectedItemsColor().getBlue(), 1.);
        nonqueryDispersionRectangle.setFill(dispersionFill);
        nonqueryDispersionRectangle.setStroke(Color.DARKGRAY);
        nonqueryDispersionRectangle.setSmooth(true);
        nonqueryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        nonqueryTypicalLine = makeLine();
        nonqueryTypicalLine.setStroke(queryTypicalStroke);

        nonquerySummaryStatisticsGroup.getChildren().addAll(nonqueryDispersionRectangle, nonqueryTypicalLine);

        overallSummaryStatisticsGroup.setMouseTransparent(true);
        querySummaryStatisticsGroup.setMouseTransparent(true);
        nonquerySummaryStatisticsGroup.setMouseTransparent(true);
        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);
        nonqueryHistogramGroup.setMouseTransparent(true);

        if (dataTableView.isShowingSummaryStatistics()) {
            getGraphicsGroup().getChildren().add(overallSummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(nonquerySummaryStatisticsGroup);
        }

        if (dataTableView.isShowingHistograms()) {
            getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
            getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
            getGraphicsGroup().getChildren().add(2, nonqueryHistogramGroup);
        }

        getGraphicsGroup().getChildren().addAll(minValueText, maxValueText);

//        if ((getStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) ||
//                (getStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT)) {
//            graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(nonquerySummaryStatisticsGroup);
//        } else if (getStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.HISTOGRAM) {
//            graphicsGroup.getChildren().add(0, overallHistogramGroup);
//            graphicsGroup.getChildren().add(1, queryHistogramGroup);
//            graphicsGroup.getChildren().add(nonqueryHistogramGroup);
//        }

        registerListeners();
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        double value = GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getFocusMaxPosition(),
                doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue());
        return value;
    }

    private double getAxisPositionForValue(double value) {
        double position = GraphicsUtil.mapValue(value, doubleColumn().getStatistics().getMinValue(),
                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
        return position;
    }

    public DoubleColumn doubleColumn () {
        return (DoubleColumn)getColumn();
    }

    public DoubleAxis doubleAxis() { return (DoubleAxis)this; }

    private void registerListeners() {
        minValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().minValueProperty(),
                new NumberStringConverter());

        maxValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().maxValueProperty(),
                new NumberStringConverter());

        getDataTableView().selectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            queryDispersionRectangle.setFill(newValue);
        });

        getDataTableView().unselectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            nonqueryDispersionRectangle.setFill(newValue);
        });

        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue) {
                    getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
                    getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
                    getGraphicsGroup().getChildren().add(2, nonqueryHistogramGroup);
                } else {
                    getGraphicsGroup().getChildren().remove(overallHistogramGroup);
                    getGraphicsGroup().getChildren().remove(queryHistogramGroup);
                    getGraphicsGroup().getChildren().remove(nonqueryHistogramGroup);
                }

                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });

        getDataTableView().showSummaryStatisticsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());

                if (!newValue) {
                    getGraphicsGroup().getChildren().remove(overallSummaryStatisticsGroup);
                    getGraphicsGroup().getChildren().remove(querySummaryStatisticsGroup);
                    getGraphicsGroup().getChildren().remove(nonquerySummaryStatisticsGroup);
                }
            }
        });

        getDataTableView().summaryStatisticsDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
//                // remove graphics from previous setting
//                if (oldValue == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT || oldValue == PCPView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT) {
//                    graphicsGroup.getChildren().remove(overallSummaryStatisticsGroup);
//                    graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
//                    graphicsGroup.getChildren().remove(nonquerySummaryStatisticsGroup);
//                } else if (oldValue == PCPView.STATISTICS_DISPLAY_MODE.HISTOGRAM) {
//                    graphicsGroup.getChildren().remove(overallHistogramGroup);
//                    graphicsGroup.getChildren().remove(queryHistogramGroup);
//                    graphicsGroup.getChildren().remove(nonqueryHistogramGroup);
//                }
//
//                if ((newValue == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) ||
//                        (newValue == PCPView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT)) {
//                    graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);
//                    graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
//                    graphicsGroup.getChildren().add(nonquerySummaryStatisticsGroup);
//                } else if (newValue == PCPView.STATISTICS_DISPLAY_MODE.HISTOGRAM) {
//                    graphicsGroup.getChildren().add(0, overallHistogramGroup);
//                    graphicsGroup.getChildren().add(1, queryHistogramGroup);
//                    graphicsGroup.getChildren().add(nonqueryHistogramGroup);
//                }

                // calculate graphics for new setting
                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });

//        getAxisBar().setOnScroll(event -> {
//
//            double expandFactor = event.getDeltaY() / getAxisBar().getHeight();
//            double expandValue = expandFactor * (doubleColumn().getStatistics().getMaxValue() - doubleColumn().getStatistics().getMinValue());
//            log.info("Scroll: deltaY = " + event.getDeltaY() + " expandFactor = " + expandFactor + " expandValue = " + expandValue);
//            double newFocusTopValue = focusTopValue + expandValue;
//            double newFocusBottomValue = focusBottomValue - expandValue;
//            if (newFocusBottomValue < doubleColumn().getStatistics().getMinValue()) {
//                newFocusBottomValue = doubleColumn().getStatistics().getMinValue();
//            }
//            if (newFocusTopValue > doubleColumn().getStatistics().getMaxValue()) {
//                newFocusTopValue = doubleColumn().getStatistics().getMaxValue();
//            }
//            if (newFocusTopValue > newFocusBottomValue) {
//                focusTopValue = newFocusTopValue;
//                focusBottomValue = newFocusBottomValue;
//            }
//
//
//            log.info("[ " + focusTopValue + " -- " + focusBottomValue + " ]");
//        });

    }

    public void resize(double left, double top, double width, double height) {
        if (getDataTableView().isShowingSummaryStatistics()) {
            getAxisBar().setWidth(DEFAULT_BAR_WIDTH);
        } else {
            getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);
        }

        super.resize(left, top, width, height);

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                DoubleHistogram histogram = doubleColumn().getStatistics().getHistogram();
                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

                overallHistogramRectangles.clear();
                overallHistogramGroup.getChildren().clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                    double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i),
                            0, histogram.getMaxBinCount(),
                            getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                    double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                    rectangle.setStroke(histogramFill.darker());
                    rectangle.setFill(histogramFill);
                    overallHistogramGroup.getChildren().add(rectangle);
                    overallHistogramRectangles.add(rectangle);
                }

                queryHistogramGroup.getChildren().clear();
                queryHistogramRectangles.clear();

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    if (queryColumnSummaryStats != null) {
                        if (getDataTable().getCalculateQueryStatistics()) {
                            DoubleHistogram queryHistogram = ((DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(doubleColumn())).getHistogram();

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                double y = getFocusMaxPosition() + ((queryHistogram.getNumBins() - i - 1) * binHeight);
                                double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                        0, histogram.getMaxBinCount(),
                                        getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                                double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                                Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                                rectangle.setStroke(queryHistogramFill.darker());
                                rectangle.setFill(queryHistogramFill);

                                queryHistogramRectangles.add(rectangle);
                                queryHistogramGroup.getChildren().add(rectangle);
                            }
                        }
                    }
                }
            } else {
                getGraphicsGroup().getChildren().removeAll(queryHistogramGroup, overallHistogramGroup);
            }

            if (getDataTableView().isShowingSummaryStatistics()) {
                double overallTypicalValue;
                double overallDispersionTopValue;
                double overallDispersionBottomValue;

                if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                    overallTypicalValue = doubleColumn().getStatistics().getMeanValue();
                    overallDispersionTopValue = doubleColumn().getStatistics().getMeanValue() + doubleColumn().getStatistics().getStandardDeviationValue();
                    overallDispersionBottomValue = doubleColumn().getStatistics().getMeanValue() - doubleColumn().getStatistics().getStandardDeviationValue();
                } else {
                    overallTypicalValue = doubleColumn().getStatistics().getMedianValue();
                    overallDispersionTopValue = doubleColumn().getStatistics().getPercentile75Value();
                    overallDispersionBottomValue = doubleColumn().getStatistics().getPercentile25Value();
                }

                double typicalValueY = GraphicsUtil.mapValue(overallTypicalValue,
                        doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                        getFocusMinPosition(), getFocusMaxPosition());
                overallTypicalLine.setStartX(doubleAxis().getBarLeftX());
                overallTypicalLine.setEndX(doubleAxis().getBarRightX());
                overallTypicalLine.setStartY(typicalValueY);
                overallTypicalLine.setEndY(typicalValueY);

                double overallDispersionTop = GraphicsUtil.mapValue(overallDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                overallDispersionTop = overallDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : overallDispersionTop;
                double overallDispersionBottom = GraphicsUtil.mapValue(overallDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                overallDispersionBottom = overallDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : overallDispersionBottom;
                overallDispersionRectangle.setX(overallTypicalLine.getStartX());
                overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
                overallDispersionRectangle.setY(overallDispersionTop);
                overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);

                if (!getGraphicsGroup().getChildren().contains(overallSummaryStatisticsGroup)) {
                    getGraphicsGroup().getChildren().add(overallSummaryStatisticsGroup);
//                    int selectionIdx = graphicsGroup.getChildren().indexOf(axisSelectionGraphicsGroup);
//                    if (selectionIdx != -1) {
//                        graphicsGroup.getChildren().remove(axisSelectionGraphicsGroup);
//                        graphicsGroup.getChildren().add(axisSelectionGraphicsGroup);
//                    }
                }

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    if (queryColumnSummaryStats != null) {
                        double queryTypicalValue;
                        double queryDispersionTopValue;
                        double queryDispersionBottomValue;

                        if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                            queryTypicalValue = queryColumnSummaryStats.getMeanValue();
                            queryDispersionTopValue = queryColumnSummaryStats.getMeanValue() + queryColumnSummaryStats.getStandardDeviationValue();
                            queryDispersionBottomValue = queryColumnSummaryStats.getMeanValue() - queryColumnSummaryStats.getStandardDeviationValue();
                        } else {
                            queryTypicalValue = queryColumnSummaryStats.getMedianValue();
                            queryDispersionTopValue = queryColumnSummaryStats.getPercentile75Value();
                            queryDispersionBottomValue = queryColumnSummaryStats.getPercentile25Value();
                        }

                        typicalValueY = GraphicsUtil.mapValue(queryTypicalValue,
                                doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                                getFocusMinPosition(), getFocusMaxPosition());
                        queryTypicalLine.setStartX(getCenterX() - 4.);
                        queryTypicalLine.setEndX(getCenterX());
                        queryTypicalLine.setStartY(typicalValueY);
                        queryTypicalLine.setEndY(typicalValueY);

                        double queryDispersionTop = GraphicsUtil.mapValue(queryDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                        queryDispersionTop = queryDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : queryDispersionTop;
                        double queryDispersionBottom = GraphicsUtil.mapValue(queryDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                        queryDispersionBottom = queryDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : queryDispersionBottom;
                        queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                        queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                        queryDispersionRectangle.setY(queryDispersionTop);
                        queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);

                        if (!getGraphicsGroup().getChildren().contains(querySummaryStatisticsGroup)) {
                            getGraphicsGroup().getChildren().add(querySummaryStatisticsGroup);
                        }
                    } else {
                        getGraphicsGroup().getChildren().remove(querySummaryStatisticsGroup);
                    }

                    // draw nonquery statistics shapes
                    DoubleColumnSummaryStats nonqueryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnNonquerySummaryStats(getColumn());

                    if (nonqueryColumnSummaryStats != null) {
                        double nonqueryTypicalValue;
                        double nonqueryDispersionTopValue;
                        double nonqueryDispersionBottomValue;

                        if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                            nonqueryTypicalValue = nonqueryColumnSummaryStats.getMeanValue();
                            nonqueryDispersionTopValue = nonqueryColumnSummaryStats.getMeanValue() + nonqueryColumnSummaryStats.getStandardDeviationValue();
                            nonqueryDispersionBottomValue = nonqueryColumnSummaryStats.getMeanValue() - nonqueryColumnSummaryStats.getStandardDeviationValue();
                        } else {
                            nonqueryTypicalValue = nonqueryColumnSummaryStats.getMedianValue();
                            nonqueryDispersionTopValue = nonqueryColumnSummaryStats.getPercentile75Value();
                            nonqueryDispersionBottomValue = nonqueryColumnSummaryStats.getPercentile25Value();
                        }

                        typicalValueY = GraphicsUtil.mapValue(nonqueryTypicalValue,
                                doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                                getFocusMinPosition(), getFocusMaxPosition());
                        nonqueryTypicalLine.setStartX(getCenterX());
                        nonqueryTypicalLine.setEndX(getCenterX() + 4.);
                        nonqueryTypicalLine.setStartY(typicalValueY);
                        nonqueryTypicalLine.setEndY(typicalValueY);

                        double nonqueryDispersionTop = GraphicsUtil.mapValue(nonqueryDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                        nonqueryDispersionTop = nonqueryDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : nonqueryDispersionTop;
                        double nonqueryDispersionBottom = GraphicsUtil.mapValue(nonqueryDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
                        nonqueryDispersionBottom = nonqueryDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : nonqueryDispersionBottom;
                        nonqueryDispersionRectangle.setX(nonqueryTypicalLine.getStartX());
                        nonqueryDispersionRectangle.setWidth(nonqueryTypicalLine.getEndX() - nonqueryTypicalLine.getStartX());
                        nonqueryDispersionRectangle.setY(nonqueryDispersionTop);
                        nonqueryDispersionRectangle.setHeight(nonqueryDispersionBottom - nonqueryDispersionTop);

                        if (!getGraphicsGroup().getChildren().contains(nonquerySummaryStatisticsGroup)) {
                            getGraphicsGroup().getChildren().add(nonquerySummaryStatisticsGroup);
                        }
                    } else {
                        getGraphicsGroup().getChildren().remove(nonquerySummaryStatisticsGroup);
                    }

                    if (getGraphicsGroup().getChildren().contains(axisSelectionGraphicsGroup)) {
                        int idx = getGraphicsGroup().getChildren().indexOf(overallSummaryStatisticsGroup);
                        getGraphicsGroup().getChildren().remove(axisSelectionGraphicsGroup);
                        getGraphicsGroup().getChildren().add(idx+1, axisSelectionGraphicsGroup);
                    }
                } else {
                    getGraphicsGroup().getChildren().removeAll(querySummaryStatisticsGroup, nonquerySummaryStatisticsGroup);
                }
            }
        }
    }
}
