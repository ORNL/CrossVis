package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TemporalAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(TemporalAxis.class.getName());

    // histogram bin rectangles
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup= new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Color histogramFill = DEFAULT_HISTOGRAM_FILL;
    private Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    private Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;

    private Text minValueText;
    private Text maxValueText;

    private TemporalAxisSelection draggingSelection;

    public TemporalAxis(DataTableView dataTableView, Column column) {
        super(dataTableView, column);

        minValueText = new Text();
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);

        minValueText.setText(((TemporalColumn)column).getStatistics().getStartInstant().toString());
        maxValueText.setText(((TemporalColumn)column).getStatistics().getEndInstant().toString());

        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        if (getDataTableView().isShowingHistograms()) {
            getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
            getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        }

        getGraphicsGroup().getChildren().addAll(minValueText, maxValueText);

        registerListeners();
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        // see if an axis selection already exists for the column selection
        for (AxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelection() == columnSelection) {
                // an axis selection already exists for the given column selection so abort
                return null;
            }
        }

        TemporalColumnSelectionRange temporalColumnSelection = (TemporalColumnSelectionRange)columnSelection;

        double selectionMinValuePosition = getAxisPositionForValue(temporalColumnSelection.getStartInstant());
        double selectionMaxValuePosition = getAxisPositionForValue(temporalColumnSelection.getEndInstant());

        TemporalAxisSelection newAxisSelection = new TemporalAxisSelection(this, temporalColumnSelection,
                selectionMinValuePosition, selectionMaxValuePosition);
        axisSelectionGraphicsGroup.getChildren().add(newAxisSelection.getGraphicsGroup());

        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    protected TemporalColumn temporalColumn() { return (TemporalColumn)getColumn(); }

    public double getAxisPositionForValue(Instant instant) {
        double position = GraphicsUtil.mapValue(instant, temporalColumn().getStatistics().getStartInstant(),
                temporalColumn().getStatistics().getEndInstant(), getFocusMinPosition(), getFocusMaxPosition());
        return position;
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        Instant value = GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getFocusMaxPosition(),
                temporalColumn().getStatistics().getStartInstant(), temporalColumn().getStatistics().getEndInstant());
        return value;
    }

    private void registerListeners() {
        ((TemporalColumn)getColumn()).getStatistics().startInstantProperty().addListener((observable, oldValue, newValue) -> {
            minValueText.setText(newValue.toString());
        });

        ((TemporalColumn)getColumn()).getStatistics().endInstantProperty().addListener((observable, oldValue, newValue) -> {
            maxValueText.setText(newValue.toString());
        });

        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue) {
                    getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
                    getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
//                    graphicsGroup.getChildren().add(2, nonqueryHistogramGroup);
                } else {
                    getGraphicsGroup().getChildren().remove(overallHistogramGroup);
                    getGraphicsGroup().getChildren().remove(queryHistogramGroup);
//                    graphicsGroup.getChildren().remove(nonqueryHistogramGroup);
                }

                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });

        getAxisBar().setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
        });

        getAxisBar().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
            double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

            selectionMaxY = selectionMaxY < getFocusMaxPosition() ? getFocusMaxPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getFocusMinPosition() ? getFocusMinPosition() : selectionMinY;

            Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getFocusMaxPosition(), getFocusMinPosition(),
                    temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());
            Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
                    temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());

            if (draggingSelection == null) {
//                    DoubleColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(column, minSelectionValue, maxSelectionValue);
                TemporalColumnSelectionRange selectionRange = new TemporalColumnSelectionRange(temporalColumn(), selectionStartInstant, selectionEndInstant);
                draggingSelection = new TemporalAxisSelection(this, selectionRange, selectionMinY, selectionMaxY);
                axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
                axisSelectionGraphicsGroup.toFront();
            } else {
                draggingSelection.update(selectionStartInstant, selectionEndInstant, selectionMinY, selectionMaxY);
            }
        });

        getAxisBar().setOnMouseReleased(event -> {
            if (draggingSelection != null) {
                axisSelectionGraphicsGroup.getChildren().remove(draggingSelection.getGraphicsGroup());
                getDataTable().addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelection());
                dragging = false;
                draggingSelection = null;
            }
        });

        getAxisBar().setOnMouseEntered(event -> {
            hoverValueText.setVisible(true);
            hoverValueText.toFront();
        });

        getAxisBar().setOnMouseExited(event -> {
            hoverValueText.setVisible(false);
        });

        getAxisBar().setOnMouseMoved(event -> {
            Object value = getValueForAxisPosition(event.getY());
            if (value != null) {
                hoverValueText.setText(getValueForAxisPosition(event.getY()).toString());
                hoverValueText.setY(event.getY());
                hoverValueText.setX(getCenterX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
            } else {
                hoverValueText.setText("");
            }
//            hoverValueText.toFront();
        });
    }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        minValueText.setX(getBounds().getMinX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(getFocusMinPosition() + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(getBounds().getMinX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(getFocusMaxPosition() - 4);

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                // resize histogram bin information
                TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

                overallHistogramGroup.getChildren().clear();
                overallHistogramRectangles.clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
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

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
//                    TemporalColumnSummaryStats queryColumnSummaryStats = (TemporalColumnSummaryStats) dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    // resize query histogram bins
                    TemporalHistogram queryHistogram = ((TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

                    if (queryHistogram != null) {
                        if (getDataTable().getCalculateQueryStatistics()) {
                            if (histogram.getNumBins() != queryHistogram.getNumBins()) {
                                log.info("query histogram and overall histogram have different bin sizes");
                            }

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                if (queryHistogram.getBinCount(i) > 0) {
                                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
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
