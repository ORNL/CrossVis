package gov.ornl.pcpview;

import gov.ornl.util.GraphicsUtil;
import gov.ornl.datatable.*;
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
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup= new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Color histogramFill = DEFAULT_HISTOGRAM_FILL;
    private Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    private Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;
    
    public PCPTemporalAxis(PCPView pcpView, Column column, DataTable dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);
        
        minValueText.setText(((TemporalColumn)column).getStatistics().getStartInstant().toString());
        maxValueText.setText(((TemporalColumn)column).getStatistics().getEndInstant().toString());

        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        if (getPCPView().isShowingHistograms()) {
            graphicsGroup.getChildren().add(0, overallHistogramGroup);
            graphicsGroup.getChildren().add(1, queryHistogramGroup);
        }

        registerListeners();
    }

    public double getAxisPositionForValue(Instant instant) {
        double position = GraphicsUtil.mapValue(instant, temporalColumn().getStatistics().getStartInstant(),
                temporalColumn().getStatistics().getEndInstant(), getFocusBottomY(), getFocusTopY());
        return position;
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

        TemporalColumnSelectionRange temporalColumnSelection = (TemporalColumnSelectionRange)columnSelection;

        double selectionMinValuePosition = getAxisPositionForValue(temporalColumnSelection.getStartInstant());
        double selectionMaxValuePosition = getAxisPositionForValue(temporalColumnSelection.getEndInstant());

        PCPTemporalAxisSelection newAxisSelection = new PCPTemporalAxisSelection(this, temporalColumnSelection,
                selectionMinValuePosition, selectionMaxValuePosition, dataModel);
        axisSelectionGraphicsGroup.getChildren().add(newAxisSelection.getGraphicsGroup());

        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        Instant value = GraphicsUtil.mapValue(axisPosition, getFocusBottomY(), getFocusTopY(),
                temporalColumn().getStatistics().getStartInstant(), temporalColumn().getStatistics().getEndInstant());
        return value;
    }

    @Override
    public void removeAllGraphics(Pane pane) {
        pane.getChildren().remove(graphicsGroup);
//        pane.getChildren().remove(histogramBinRectangleGroup);
//        pane.getChildren().remove(queryHistogramBinRectangleGroup);

        if (!getAxisSelectionList().isEmpty()) {
            for (PCPAxisSelection axisSelection : getAxisSelectionList()) {
                pane.getChildren().remove(axisSelection.getGraphicsGroup());
            }
        }
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
            draggingSelection = new PCPTemporalAxisSelection(this, selectionRange, selectionMinY, selectionMaxY, dataModel);
            axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
        } else {
            draggingSelection.update(selectionStartInstant, selectionEndInstant, selectionMinY, selectionMaxY);
        }
    }

    @Override
    protected void handleAxisBarMouseReleased() {
        if (draggingSelection != null) {
//            getAxisSelectionList().add(draggingSelection);
            dataModel.addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelectionRange());
            dragging = false;
            axisSelectionGraphicsGroup.getChildren().remove(draggingSelection.getGraphicsGroup());
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

        getPCPView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue) {
                    graphicsGroup.getChildren().add(0, overallHistogramGroup);
                    graphicsGroup.getChildren().add(1, queryHistogramGroup);
//                    graphicsGroup.getChildren().add(2, nonqueryHistogramGroup);
                } else {
                    graphicsGroup.getChildren().remove(overallHistogramGroup);
                    graphicsGroup.getChildren().remove(queryHistogramGroup);
//                    graphicsGroup.getChildren().remove(nonqueryHistogramGroup);
                }

                resize(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            }
        });
    }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        if (!dataModel.isEmpty()) {
            if (getPCPView().isShowingHistograms()) {
                // resize histogram bin information
                TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
                double binHeight = (getFocusBottomY() - getFocusTopY()) / histogram.getNumBins();

                overallHistogramGroup.getChildren().clear();
                overallHistogramRectangles.clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                    double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                    double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                    rectangle.setStroke(histogramFill.darker());
                    rectangle.setFill(histogramFill);
                    overallHistogramRectangles.add(rectangle);
                    overallHistogramGroup.getChildren().add(rectangle);
                }

                queryHistogramGroup.getChildren().clear();
                queryHistogramRectangles.clear();

                if (dataModel.getActiveQuery().hasColumnSelections()) {
//                    TemporalColumnSummaryStats queryColumnSummaryStats = (TemporalColumnSummaryStats) dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    // resize query histogram bins
                    TemporalHistogram queryHistogram = ((TemporalColumnSummaryStats) dataModel.getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

                    if (queryHistogram != null) {
                        if (dataModel.getCalculateQueryStatistics()) {
                            if (histogram.getNumBins() != queryHistogram.getNumBins()) {
                                log.info("query histogram and overall histogram have different bin sizes");
                            }

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                if (queryHistogram.getBinCount(i) > 0) {
                                    double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                                    double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                            0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2,
                                            DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
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
                }
            }
        }
    }
}
