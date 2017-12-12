package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PCPTemporalAxis extends PCPAxis {
    private static final Logger log = Logger.getLogger(PCPTemporalAxis.class.getName());

    private PCPTemporalAxisSelection draggingSelection;
    // histogram bin rectangles
    private Group histogramBinRectangleGroup;
    private ArrayList<Rectangle> histogramBinRectangleList;
    private Group queryHistogramBinRectangleGroup;
    private ArrayList<Rectangle> queryHistogramBinRectangleList;

    private Color histogramFill = DEFAULT_HISTOGRAM_FILL;
    private Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    private Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;
    
    public PCPTemporalAxis(PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        minValueText.setText(((TemporalColumn)column).getStatistics().getStartInstant().toString());
        maxValueText.setText(((TemporalColumn)column).getStatistics().getEndInstant().toString());
        registerListeners();
    }

    private TemporalColumn temporalColumn() {
        return (TemporalColumn)getColumn();
    }

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
//            log.debug("selectionMaxY = " + selectionMaxY + " getBarTopY() = " + getFocusTopY());
//        }

//                Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
//                Instant end = start.plusSeconds(60);
//                Instant testInstant = GraphicsUtil.mapValue(getBarBottomY(), getBarTopY(), getBarBottomY(),
//                        temporalColumn().getEndInstant(), temporalColumn().getStartInstant());

        Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getFocusTopY(), getFocusBottomY(),
                temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());
        Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusTopY(), getFocusBottomY(),
                temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());

//        log.debug("selectionMaxY: " + selectionMaxY + "  selectionEndInstant: " + selectionEndInstant);
//        log.debug("selectionMinY: " + selectionMinY + "  selectionStartInstant: " + selectionStartInstant);


        if (draggingSelection == null) {
//                    DoubleColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(column, minSelectionValue, maxSelectionValue);
            TemporalColumnSelectionRange selectionRange = new TemporalColumnSelectionRange(temporalColumn(), selectionStartInstant, selectionEndInstant);
            draggingSelection = new PCPTemporalAxisSelection(this, selectionRange, selectionMinY, selectionMaxY, pane, dataModel);
        } else {
            draggingSelection.update(selectionStartInstant, selectionEndInstant, selectionMinY, selectionMaxY);
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
        ((TemporalColumn)getColumn()).getStatistics().startInstantProperty().addListener((observable, oldValue, newValue) -> {
            minValueText.setText(newValue.toString());
        });
        ((TemporalColumn)getColumn()).getStatistics().endInstantProperty().addListener((observable, oldValue, newValue) -> {
            maxValueText.setText(newValue.toString());
        });
    }

    public void layout(double center, double top, double width, double height) {
        super.layout(center, top, width, height);

        if (!dataModel.isEmpty()) {
            // layout histogram bin information
            TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
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
                TemporalColumnSummaryStats queryColumnSummaryStats = (TemporalColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                // layout query histogram bins
                TemporalHistogram queryHistogram = ((TemporalColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

                if (histogram.getNumBins() != queryHistogram.getNumBins()) {
                    log.info("query histogram and overall histogram have different bin sizes");
                }

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
//            } else {
//                graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
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
