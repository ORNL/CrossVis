package gov.ornl.pcpview;

import gov.ornl.util.GraphicsUtil;
import gov.ornl.datatable.*;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.NumberStringConverter;

import java.util.ArrayList;
import java.util.logging.Logger;

public class PCPDoubleAxis extends PCPUnivariateAxis {
    public static final Logger log = Logger.getLogger(PCPDoubleAxis.class.getName());

    public final static Color DEFAULT_OVERALL_DISPERSION_FILL = DEFAULT_HISTOGRAM_FILL;
    public final static Color DEFAULT_QUERY_DISPERSION_FILL = DEFAULT_QUERY_HISTOGRAM_FILL;
    public final static Color DEFAULT_DISPERSION_STROKE = DEFAULT_HISTOGRAM_STROKE;
    public final static Color DEFAULT_OVERALL_TYPICAL_STROKE = Color.DARKBLUE.darker();
    public final static Color DEFAULT_QUERY_TYPICAL_STROKE = Color.DARKBLUE;

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

    private Color overallDispersionFill = DEFAULT_OVERALL_DISPERSION_FILL;
    private Color queryDispersionFill = DEFAULT_QUERY_DISPERSION_FILL;
    private Color dispersionStroke = DEFAULT_DISPERSION_STROKE;
    private Color overallTypicalStroke = DEFAULT_OVERALL_TYPICAL_STROKE;
    private Color queryTypicalStroke = DEFAULT_QUERY_TYPICAL_STROKE;

    private PCPDoubleAxisSelection draggingSelection;

//    private double focusTopValue;
//    private double focusBottomValue;

    public PCPDoubleAxis(PCPView pcpView, Column column) {
        super(pcpView, column);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.setFill(overallDispersionFill);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.setStroke(overallTypicalStroke);

        overallSummaryStatisticsGroup.getChildren().addAll(overallDispersionRectangle, overallTypicalLine);

        queryDispersionRectangle = new Rectangle();
        Color dispersionFill = new Color(pcpView.getSelectedItemsColor().getRed(), pcpView.getSelectedItemsColor().getGreen(),
                pcpView.getSelectedItemsColor().getBlue(), 1.);
        queryDispersionRectangle.setFill(dispersionFill);
        queryDispersionRectangle.setStroke(Color.DARKGRAY);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.setStroke(queryTypicalStroke);

        querySummaryStatisticsGroup.getChildren().addAll(queryDispersionRectangle, queryTypicalLine);

        nonqueryDispersionRectangle = new Rectangle();
        dispersionFill = new Color(pcpView.getUnselectedItemsColor().getRed(), pcpView.getUnselectedItemsColor().getGreen(),
                pcpView.getUnselectedItemsColor().getBlue(), 1.);
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

        if (getPCPView().isShowingSummaryStatistics()) {
            graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
//            graphicsGroup.getChildren().add(nonquerySummaryStatisticsGroup);
        }

        if (getPCPView().isShowingHistograms()) {
            graphicsGroup.getChildren().add(0, overallHistogramGroup);
            graphicsGroup.getChildren().add(1, queryHistogramGroup);
            graphicsGroup.getChildren().add(2, nonqueryHistogramGroup);
        }

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
    protected PCPAxisSelection addAxisSelection(ColumnSelection columnSelection) {
        // see if an axis selection already exists for the column selection
        for (PCPAxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelectionRange() == columnSelection) {
                // an axis selection already exists for the given column selection so abort
                return null;
            }
        }

        DoubleColumnSelectionRange doubleColumnSelection = (DoubleColumnSelectionRange)columnSelection;

        double selectionMinValuePosition = getAxisPositionForValue(doubleColumnSelection.getMinValue());
        double selectionMaxValuePosition = getAxisPositionForValue(doubleColumnSelection.getMaxValue());

        PCPDoubleAxisSelection newAxisSelection = new PCPDoubleAxisSelection(this, doubleColumnSelection,
                selectionMinValuePosition, selectionMaxValuePosition, getDataTable());

        axisSelectionGraphicsGroup.getChildren().add(newAxisSelection.getGraphicsGroup());
        axisSelectionGraphicsGroup.toFront();

        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    @Override
    public void removeAllGraphics(Pane pane) {
        pane.getChildren().remove(graphicsGroup);

        if (!getAxisSelectionList().isEmpty()) {
            for (PCPAxisSelection axisSelection : getAxisSelectionList()) {
                pane.getChildren().remove(axisSelection.getGraphicsGroup());
            }
        }
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        double value = GraphicsUtil.mapValue(axisPosition, getFocusBottomY(), getFocusTopY(),
                doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue());
        return value;
    }

    private double getAxisPositionForValue(double value) {
        double position = GraphicsUtil.mapValue(value, doubleColumn().getStatistics().getMinValue(),
                doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
        return position;
    }

    public Rectangle getOverallDispersionRectangle() { return overallDispersionRectangle; }

    public Rectangle getQueryDispersionRectangle () { return queryDispersionRectangle; }

    public Rectangle getNonQueryDispersionRectangle () { return nonqueryDispersionRectangle; }

    public Line getOverallTypicalLine () { return overallTypicalLine; }

    public Line getQueryTypicalLine () { return queryTypicalLine; }

    public Line getNonQueryTypicalLine () { return nonqueryTypicalLine; }

    @Override
    protected void handleAxisBarMousePressed() {

    }

    @Override
    protected void handleAxisBarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;
        }

        dragEndPoint = new Point2D(event.getX(), event.getY());

        double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
        double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

        selectionMaxY = selectionMaxY < getFocusTopY() ? getFocusTopY() : selectionMaxY;
        selectionMinY = selectionMinY > getFocusBottomY() ? getFocusBottomY() : selectionMinY;

        double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getFocusTopY(), getFocusBottomY(),
                doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());
        double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getFocusTopY(), getFocusBottomY(),
                doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());

        if (draggingSelection == null) {
            DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
            draggingSelection = new PCPDoubleAxisSelection(this, selectionRange, selectionMinY, selectionMaxY, getDataTable());
            axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
            axisSelectionGraphicsGroup.toFront();
        } else {
            draggingSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
        }
    }

    @Override
    protected void handleAxisBarMouseReleased() {
        if (draggingSelection != null) {
//            getAxisSelectionList().add(draggingSelection);
            axisSelectionGraphicsGroup.getChildren().remove(draggingSelection.getGraphicsGroup());
            getDataTable().addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelectionRange());
            dragging = false;
            draggingSelection = null;
        }
    }

    private void registerListeners() {
        minValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().minValueProperty(),
                new NumberStringConverter());

        maxValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().maxValueProperty(),
                new NumberStringConverter());

        getPCPView().selectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            queryDispersionRectangle.setFill(newValue);
        });

        getPCPView().unselectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            nonqueryDispersionRectangle.setFill(newValue);
        });

        getPCPView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue) {
                    graphicsGroup.getChildren().add(0, overallHistogramGroup);
                    graphicsGroup.getChildren().add(1, queryHistogramGroup);
                    graphicsGroup.getChildren().add(2, nonqueryHistogramGroup);
                } else {
                    graphicsGroup.getChildren().remove(overallHistogramGroup);
                    graphicsGroup.getChildren().remove(queryHistogramGroup);
                    graphicsGroup.getChildren().remove(nonqueryHistogramGroup);
                }

                resize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            }
        });

        getPCPView().showSummaryStatisticsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                resize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());

                if (!newValue) {
                    graphicsGroup.getChildren().remove(overallSummaryStatisticsGroup);
                    graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
                    graphicsGroup.getChildren().remove(nonquerySummaryStatisticsGroup);
                }
            }
        });

        getPCPView().summaryStatisticsDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
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
                resize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
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

    private DoubleColumn doubleColumn() {
        return (DoubleColumn)getColumn();
    }

    public void resize(double left, double top, double width, double height) {
        if (getPCPView().isShowingSummaryStatistics()) {
            getAxisBar().setWidth(DEFAULT_BAR_WIDTH);
        } else {
            getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);
        }

        super.resize(left, top, width, height);

        if (!getDataTable().isEmpty()) {
            if (getPCPView().isShowingHistograms()) {
                DoubleHistogram histogram = doubleColumn().getStatistics().getHistogram();
                double binHeight = (getFocusBottomY() - getFocusTopY()) / histogram.getNumBins();

                overallHistogramRectangles.clear();
                overallHistogramGroup.getChildren().clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
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
                                double y = getFocusTopY() + ((queryHistogram.getNumBins() - i - 1) * binHeight);
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
                graphicsGroup.getChildren().removeAll(queryHistogramGroup, overallHistogramGroup);
            }

            if (getPCPView().isShowingSummaryStatistics()) {
                double overallTypicalValue;
                double overallDispersionTopValue;
                double overallDispersionBottomValue;

                if (getPCPView().getSummaryStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
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
                        getFocusBottomY(), getFocusTopY());
                overallTypicalLine.setStartX(getBarLeftX());
                overallTypicalLine.setEndX(getBarRightX());
                overallTypicalLine.setStartY(typicalValueY);
                overallTypicalLine.setEndY(typicalValueY);

                double overallDispersionTop = GraphicsUtil.mapValue(overallDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                overallDispersionTop = overallDispersionTop < getFocusTopY() ? getFocusTopY() : overallDispersionTop;
                double overallDispersionBottom = GraphicsUtil.mapValue(overallDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                overallDispersionBottom = overallDispersionBottom > getFocusBottomY() ? getFocusBottomY() : overallDispersionBottom;
                overallDispersionRectangle.setX(overallTypicalLine.getStartX());
                overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
                overallDispersionRectangle.setY(overallDispersionTop);
                overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);

                if (!graphicsGroup.getChildren().contains(overallSummaryStatisticsGroup)) {
                    graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);
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

                        if (getPCPView().getSummaryStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
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
                                getFocusBottomY(), getFocusTopY());
                        queryTypicalLine.setStartX(centerX - 4.);
                        queryTypicalLine.setEndX(centerX);
                        queryTypicalLine.setStartY(typicalValueY);
                        queryTypicalLine.setEndY(typicalValueY);

                        double queryDispersionTop = GraphicsUtil.mapValue(queryDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                        queryDispersionTop = queryDispersionTop < getFocusTopY() ? getFocusTopY() : queryDispersionTop;
                        double queryDispersionBottom = GraphicsUtil.mapValue(queryDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                        queryDispersionBottom = queryDispersionBottom > getFocusBottomY() ? getFocusBottomY() : queryDispersionBottom;
                        queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                        queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                        queryDispersionRectangle.setY(queryDispersionTop);
                        queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);

                        if (!graphicsGroup.getChildren().contains(querySummaryStatisticsGroup)) {
                            graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
                        }
                    } else {
                        graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
                    }

                    // draw nonquery statistics shapes
                    DoubleColumnSummaryStats nonqueryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnNonquerySummaryStats(getColumn());

                    if (nonqueryColumnSummaryStats != null) {
                        double nonqueryTypicalValue;
                        double nonqueryDispersionTopValue;
                        double nonqueryDispersionBottomValue;

                        if (getPCPView().getSummaryStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
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
                                getFocusBottomY(), getFocusTopY());
                        nonqueryTypicalLine.setStartX(centerX);
                        nonqueryTypicalLine.setEndX(centerX + 4.);
                        nonqueryTypicalLine.setStartY(typicalValueY);
                        nonqueryTypicalLine.setEndY(typicalValueY);

                        double nonqueryDispersionTop = GraphicsUtil.mapValue(nonqueryDispersionTopValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                        nonqueryDispersionTop = nonqueryDispersionTop < getFocusTopY() ? getFocusTopY() : nonqueryDispersionTop;
                        double nonqueryDispersionBottom = GraphicsUtil.mapValue(nonqueryDispersionBottomValue, doubleColumn().getStatistics().getMinValue(),
                                doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                        nonqueryDispersionBottom = nonqueryDispersionBottom > getFocusBottomY() ? getFocusBottomY() : nonqueryDispersionBottom;
                        nonqueryDispersionRectangle.setX(nonqueryTypicalLine.getStartX());
                        nonqueryDispersionRectangle.setWidth(nonqueryTypicalLine.getEndX() - nonqueryTypicalLine.getStartX());
                        nonqueryDispersionRectangle.setY(nonqueryDispersionTop);
                        nonqueryDispersionRectangle.setHeight(nonqueryDispersionBottom - nonqueryDispersionTop);

                        if (!graphicsGroup.getChildren().contains(nonquerySummaryStatisticsGroup)) {
                            graphicsGroup.getChildren().add(nonquerySummaryStatisticsGroup);
                        }
                    } else {
                        graphicsGroup.getChildren().remove(nonquerySummaryStatisticsGroup);
                    }

                    if (graphicsGroup.getChildren().contains(axisSelectionGraphicsGroup)) {
                        int idx = graphicsGroup.getChildren().indexOf(overallSummaryStatisticsGroup);
                        graphicsGroup.getChildren().remove(axisSelectionGraphicsGroup);
                        graphicsGroup.getChildren().add(idx+1, axisSelectionGraphicsGroup);
                    }
                } else {
                    graphicsGroup.getChildren().removeAll(querySummaryStatisticsGroup, nonquerySummaryStatisticsGroup);
                }
            }
        }
    }
}
