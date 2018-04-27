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

public class PCPDoubleAxis extends PCPAxis {
    public static final Logger log = Logger.getLogger(PCPDoubleAxis.class.getName());

    public final static Color DEFAULT_OVERALL_DISPERSION_FILL = DEFAULT_HISTOGRAM_FILL;
    public final static Color DEFAULT_QUERY_DISPERSION_FILL = DEFAULT_QUERY_HISTOGRAM_FILL;
    public final static Color DEFAULT_DISPERSION_STROKE = DEFAULT_HISTOGRAM_STROKE;
    public final static Color DEFAULT_OVERALL_TYPICAL_STROKE = Color.DARKBLUE.darker();
    public final static Color DEFAULT_QUERY_TYPICAL_STROKE = Color.DARKBLUE;

    // summary statistics objects
    private Group overallSummaryStatisticsGroup;
    private Rectangle overallDispersionRectangle;
    private Line overallTypicalLine;
    private Group querySummaryStatisticsGroup;
    private Rectangle queryDispersionRectangle;
    private Line queryTypicalLine;
    private Rectangle nonqueryDispersionRectangle;
    private Line nonqueryTypicalLine;

    // histogram bin rectangles
    private Group histogramBinRectangleGroup;
    private ArrayList<Rectangle> histogramBinRectangleList;
    private Group queryHistogramBinRectangleGroup;
    private ArrayList<Rectangle> queryHistogramBinRectangleList;

    private Color overallDispersionFill = DEFAULT_OVERALL_DISPERSION_FILL;
    private Color queryDispersionFill = DEFAULT_QUERY_DISPERSION_FILL;
    private Color dispersionStroke = DEFAULT_DISPERSION_STROKE;
    private Color overallTypicalStroke = DEFAULT_OVERALL_TYPICAL_STROKE;
    private Color queryTypicalStroke = DEFAULT_QUERY_TYPICAL_STROKE;

    private PCPDoubleAxisSelection draggingSelection;

    public PCPDoubleAxis(PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.setFill(overallDispersionFill);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.setStroke(overallTypicalStroke);
        overallSummaryStatisticsGroup = new Group(overallDispersionRectangle, overallTypicalLine);
        overallSummaryStatisticsGroup.setMouseTransparent(true);
        graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);

        queryDispersionRectangle = new Rectangle();
        queryDispersionRectangle.setFill(pcpView.getSelectedItemsColor());
        queryDispersionRectangle.setStroke(Color.DARKGRAY);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.setStroke(queryTypicalStroke);

        nonqueryDispersionRectangle = new Rectangle();
        nonqueryDispersionRectangle.setFill(pcpView.getUnselectedItemsColor());
        nonqueryDispersionRectangle.setStroke(Color.DARKGRAY);
        nonqueryDispersionRectangle.setSmooth(true);
        nonqueryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        nonqueryTypicalLine = makeLine();
        nonqueryTypicalLine.setStroke(queryTypicalStroke);

        querySummaryStatisticsGroup = new Group(nonqueryDispersionRectangle, nonqueryTypicalLine,
                queryDispersionRectangle, queryTypicalLine);
        querySummaryStatisticsGroup.setMouseTransparent(true);

        registerListeners();
    }

    public Rectangle getOverallDispersionRectangle() { return overallDispersionRectangle; }

    public Rectangle getQueryDispersionRectangle () { return queryDispersionRectangle; }

    public Rectangle getNonqueryDispersionRectangle () { return nonqueryDispersionRectangle; }

    public Line getOverallTypicalLine () { return overallTypicalLine; }

    public Line getQueryTypicalLine () { return queryTypicalLine; }

    public Line getNonqueryTypicalLine () { return nonqueryTypicalLine; }

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

//        if (selectionMaxY == getFocusTopY()) {
//            log.debug("selectionMaxY = " + selectionMaxY + " getFocusTopY() = " + getFocusTopY());
//        }

        double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getFocusTopY(), getFocusBottomY(),
                doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());
        double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getFocusTopY(), getFocusBottomY(),
                doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());

        if (draggingSelection == null) {
            DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
            draggingSelection = new PCPDoubleAxisSelection(this, selectionRange, selectionMinY, selectionMaxY, pane, dataModel);
        } else {
            draggingSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
        }
    }

    @Override
    protected void handleAxisBarMouseReleased() {
        if (draggingSelection != null) {
            getAxisSelectionList().add(draggingSelection);
            dataModel.addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelectionRange());
            dragging = false;
            draggingSelection = null;
        }
    }

    private void registerListeners() {
        minValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().minValueProperty(),
                new NumberStringConverter());
        maxValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().maxValueProperty(),
                new NumberStringConverter());
        pcpView.selectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            queryDispersionRectangle.setFill(newValue);
        });
        pcpView.unselectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            nonqueryDispersionRectangle.setFill(newValue);
        });
    }

    private DoubleColumn doubleColumn() {
        return (DoubleColumn)getColumn();
    }

    public void layout(double center, double top, double width, double height) {
        super.layout(center, top, width, height);

        if (!dataModel.isEmpty()) {
            // layout summary statistics
            double typicalValueY = GraphicsUtil.mapValue(doubleColumn().getStatistics().getMeanValue(),
                    doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                    getFocusBottomY(), getFocusTopY());
            overallTypicalLine.setStartX(getBarLeftX());
            overallTypicalLine.setEndX(getBarRightX());
            overallTypicalLine.setStartY(typicalValueY);
            overallTypicalLine.setEndY(typicalValueY);

            double topValue = doubleColumn().getStatistics().getMeanValue() + doubleColumn().getStatistics().getStandardDeviationValue();
            double overallDispersionTop = GraphicsUtil.mapValue(topValue, doubleColumn().getStatistics().getMinValue(),
                    doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
            overallDispersionTop = overallDispersionTop < getFocusTopY() ? getFocusTopY() : overallDispersionTop;
            double bottomValue = doubleColumn().getStatistics().getMeanValue() - doubleColumn().getStatistics().getStandardDeviationValue();
            double overallDispersionBottom = GraphicsUtil.mapValue(bottomValue, doubleColumn().getStatistics().getMinValue(),
                    doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
            overallDispersionBottom = overallDispersionBottom > getFocusBottomY() ? getFocusBottomY() : overallDispersionBottom;
            overallDispersionRectangle.setX(overallTypicalLine.getStartX());
            overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
            overallDispersionRectangle.setY(overallDispersionTop);
            overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);

            // layout histogram bin information
            DoubleHistogram histogram = doubleColumn().getStatistics().getHistogram();
            double binHeight = (getFocusBottomY() - getFocusTopY()) / histogram.getNumBins();
            histogramBinRectangleList = new ArrayList<>();

            // remove previously shown bin geometries
            if (histogramBinRectangleGroup != null) {
                pane.getChildren().remove(histogramBinRectangleGroup);
            }
            histogramBinRectangleGroup = new Group();

            for (int i = 0; i < histogram.getNumBins(); i++) {
                double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                double x = getBounds().getX() + ((width - binWidth) / 2.);
                Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                rectangle.setStroke(histogramFill.darker());
                rectangle.setFill(histogramFill);
                histogramBinRectangleList.add(rectangle);
                histogramBinRectangleGroup.getChildren().add(rectangle);
            }

            queryHistogramBinRectangleList = new ArrayList<>();
            if (queryHistogramBinRectangleGroup != null) {
                pane.getChildren().remove(queryHistogramBinRectangleGroup);
            }
            queryHistogramBinRectangleGroup = new Group();

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                // draw query statistics shapes
                DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                if (queryColumnSummaryStats == null) {
                    log.info("queryColumnSummaryStats is null for column " + getColumn().getName());
                }
                typicalValueY = GraphicsUtil.mapValue(queryColumnSummaryStats.getMeanValue(),
                        doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                        getFocusBottomY(), getFocusTopY());
                queryTypicalLine.setStartX(centerX - 4.);
                queryTypicalLine.setEndX(centerX);
                queryTypicalLine.setStartY(typicalValueY);
                queryTypicalLine.setEndY(typicalValueY);

                double value = queryColumnSummaryStats.getMeanValue() + queryColumnSummaryStats.getStandardDeviationValue();
                double queryDispersionTop = GraphicsUtil.mapValue(value, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                queryDispersionTop = queryDispersionTop < getFocusTopY() ? getFocusTopY() : queryDispersionTop;
                value = queryColumnSummaryStats.getMeanValue() - queryColumnSummaryStats.getStandardDeviationValue();
                double queryDispersionBottom = GraphicsUtil.mapValue(value, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                queryDispersionBottom = queryDispersionBottom > getFocusBottomY() ? getFocusBottomY() : queryDispersionBottom;
                queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                queryDispersionRectangle.setY(queryDispersionTop);
                queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);

                // draw nonquery statistics shapes
                DoubleColumnSummaryStats nonqueryColumnSummaryStats = (DoubleColumnSummaryStats)dataModel.getActiveQuery().getColumnNonquerySummaryStats(getColumn());

                if (nonqueryColumnSummaryStats == null) {
                    log.info("nonqueryColumnSummaryStats is null for column " + getColumn().getName());
                }
                typicalValueY = GraphicsUtil.mapValue(nonqueryColumnSummaryStats.getMeanValue(),
                        doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                        getFocusBottomY(), getFocusTopY());
                nonqueryTypicalLine.setStartX(centerX);
                nonqueryTypicalLine.setEndX(centerX + 4.);
                nonqueryTypicalLine.setStartY(typicalValueY);
                nonqueryTypicalLine.setEndY(typicalValueY);

                double dispersionValue = nonqueryColumnSummaryStats.getMeanValue() + nonqueryColumnSummaryStats.getStandardDeviationValue();
                double nonqueryDispersionTop = GraphicsUtil.mapValue(dispersionValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                nonqueryDispersionTop = nonqueryDispersionTop < getFocusTopY() ? getFocusTopY() : nonqueryDispersionTop;
                dispersionValue = nonqueryColumnSummaryStats.getMeanValue() - nonqueryColumnSummaryStats.getStandardDeviationValue();
                double nonqueryDispersionBottom = GraphicsUtil.mapValue(dispersionValue, doubleColumn().getStatistics().getMinValue(),
                        doubleColumn().getStatistics().getMaxValue(), getFocusBottomY(), getFocusTopY());
                nonqueryDispersionBottom = nonqueryDispersionBottom > getFocusBottomY() ? getFocusBottomY() : nonqueryDispersionBottom;
                nonqueryDispersionRectangle.setX(nonqueryTypicalLine.getStartX());
                nonqueryDispersionRectangle.setWidth(nonqueryTypicalLine.getEndX() - nonqueryTypicalLine.getStartX());
                nonqueryDispersionRectangle.setY(nonqueryDispersionTop);
                nonqueryDispersionRectangle.setHeight(nonqueryDispersionBottom - nonqueryDispersionTop);

                if (!graphicsGroup.getChildren().contains(querySummaryStatisticsGroup)) {
                    graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
                }

                // layout query histogram bins
                DoubleHistogram queryHistogram = ((DoubleColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(doubleColumn())).getHistogram();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    if (queryHistogram.getBinCount(i) > 0) {
                        double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                        double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                        double x = getBounds().getX() + ((width - binWidth) / 2.);
                        Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                        rectangle.setStroke(queryHistogramFill.darker());
                        rectangle.setFill(queryHistogramFill);

                        queryHistogramBinRectangleList.add(rectangle);
                        queryHistogramBinRectangleGroup.getChildren().add(rectangle);
                    }
                }
            } else {
                graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
            }
        }
    }

    @Override
    public Group getHistogramBinRectangleGroup() {
        return histogramBinRectangleGroup;
    }

    @Override
    public Group getQueryHistogramBinRectangleGroup() {
        return queryHistogramBinRectangleGroup;
    }
}
