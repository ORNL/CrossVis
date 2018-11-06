package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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

    private Text startInstantText;
    private Text endInstantText;

    private Text focusStartInstantText;
    private Text focusEndInstantText;

    private ObjectProperty<Instant> focusStartInstantValue;
    private ObjectProperty<Instant> focusEndInstantValue;

    private TemporalAxisSelection draggingSelection;

    private Instant draggingStartInstant;
    private Instant draggingEndInstant;
    private Text draggingContextInstantText;
    private Line draggingContextLine;

    public TemporalAxis(DataTableView dataTableView, Column column) {
        super(dataTableView, column);

        draggingContextLine = new Line();
        draggingContextLine.setStroke(Color.BLACK);
        draggingContextLine.setStrokeWidth(2.);

        draggingContextInstantText = new Text();
        draggingContextInstantText.setFill(Color.BLACK);
        draggingContextInstantText.setFont(Font.font(DEFAULT_TEXT_SIZE));

        startInstantText = new Text(temporalColumn().getStatistics().getStartInstant().toString());
        startInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        startInstantText.setSmooth(true);

        endInstantText = new Text(temporalColumn().getStatistics().getEndInstant().toString());
        endInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        endInstantText.setSmooth(true);

        focusStartInstantValue = new SimpleObjectProperty<>(temporalColumn().getStatistics().getStartInstant());
        focusStartInstantText = new Text(focusStartInstantValue.get().toString());
        focusStartInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusStartInstantText.setSmooth(true);

        focusEndInstantValue = new SimpleObjectProperty<>(temporalColumn().getStatistics().getEndInstant());
        focusEndInstantText = new Text(focusEndInstantValue.get().toString());
        focusEndInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusEndInstantText.setSmooth(true);

        getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);

//        startInstantText.setText(((TemporalColumn)column).getStatistics().getStartInstant().toString());
//        endInstantText.setText(((TemporalColumn)column).getStatistics().getEndInstant().toString());

        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        if (getDataTableView().isShowingHistograms()) {
            getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
            getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        }

        getGraphicsGroup().getChildren().addAll(startInstantText, endInstantText, focusStartInstantText, focusEndInstantText);

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
            startInstantText.setText(newValue.toString());
        });

        ((TemporalColumn)getColumn()).getStatistics().endInstantProperty().addListener((observable, oldValue, newValue) -> {
            endInstantText.setText(newValue.toString());
        });

        focusStartInstantValue.addListener((observable, oldValue, newValue) -> {
            focusStartInstantText.setText(newValue.toString());
        });

        focusEndInstantValue.addListener((observable, oldValue, newValue) -> {
            focusEndInstantText.setText(newValue.toString());
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

//            Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getFocusMaxPosition(), getFocusMinPosition(),
//                    temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());
//            Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
//                    temporalColumn().getStatistics().getEndInstant(), temporalColumn().getStatistics().getStartInstant());
            Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getFocusMaxPosition(), getFocusMinPosition(),
                    getFocusEndInstant(), getFocusStartInstant());
            Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
                    getFocusEndInstant(), getFocusStartInstant());

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

        getLowerContextBar().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getBarLeftX());
                draggingContextLine.setEndX(getBarRightX());
                draggingContextLine.setStartY(getFocusMinPosition());
                draggingContextLine.setEndY(getFocusMinPosition());
                draggingContextLine.setTranslateY(0);
                draggingContextInstantText.setX(focusStartInstantText.getX());
                draggingContextInstantText.setY(focusStartInstantText.getY());
                draggingContextInstantText.setTranslateY(0);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextInstantText);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            Instant newFocusStartInstant;
            if (y > getFocusMinPosition()) {
                // above the focus start instant in lower context region (use context range)
                newFocusStartInstant = GraphicsUtil.mapValue(y, getFocusMinPosition(), getLowerContextBar().getLayoutBounds().getMaxY(),
                        getFocusStartInstant(), temporalColumn().getStatistics().getStartInstant());
                if (newFocusStartInstant.isBefore(temporalColumn().getStatistics().getStartInstant())) {
                    newFocusStartInstant = Instant.from(temporalColumn().getStatistics().getStartInstant());
                    dy = getUpperContextBar().getHeight();
                }
            } else {
                // inside focus region (use focus start and end)
                newFocusStartInstant = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
                        getFocusEndInstant(), getFocusStartInstant());
                if (newFocusStartInstant.isAfter(getFocusEndInstant())) {
                    newFocusStartInstant = Instant.from(getFocusEndInstant());
                    dy = -getAxisBar().getHeight();
                }
            }

            draggingStartInstant = Instant.from(newFocusStartInstant);
            draggingContextInstantText.setText(draggingStartInstant.toString());
            draggingContextInstantText.setTranslateY(dy);

            draggingContextLine.setTranslateY(dy);
        });

        getLowerContextBar().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextInstantText, draggingContextLine);
                if (!getFocusStartInstant().equals(draggingStartInstant)) {
                    setFocusStartInstant(draggingStartInstant);
                    getDataTableView().resizeView();
                }
            }
        });

        getUpperContextBar().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getBarLeftX());
                draggingContextLine.setEndX(getBarRightX());
                draggingContextLine.setStartY(getFocusMaxPosition());
                draggingContextLine.setEndY(getFocusMaxPosition());
                draggingContextLine.setTranslateY(0);
                draggingContextInstantText.setX(focusEndInstantText.getX());
                draggingContextInstantText.setY(focusEndInstantText.getY());
                draggingContextInstantText.setTranslateY(0);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextInstantText);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            Instant newFocusEndInstant;
            if (y < getFocusMaxPosition()) {
                // above the focus end instant in upper context region (use context range)
                newFocusEndInstant = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getUpperContextBar().getY(),
                        getFocusEndInstant(), temporalColumn().getStatistics().getEndInstant());
                if (newFocusEndInstant.isAfter(temporalColumn().getStatistics().getEndInstant())) {
                    newFocusEndInstant = Instant.from(temporalColumn().getStatistics().getEndInstant());
                    dy = -getUpperContextBar().getHeight();
                }
            } else {
                // inside focus region (use focus start and end)
                newFocusEndInstant = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
                        getFocusEndInstant(), getFocusStartInstant());
                if (newFocusEndInstant.isBefore(getFocusStartInstant())) {
                    newFocusEndInstant = Instant.from(getFocusStartInstant());
                    dy = getAxisBar().getHeight();
                }
            }

            draggingEndInstant = Instant.from(newFocusEndInstant);
            draggingContextInstantText.setText(draggingEndInstant.toString());
            draggingContextInstantText.setTranslateY(dy);

            draggingContextLine.setTranslateY(dy);
        });

        getUpperContextBar().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextInstantText, draggingContextLine);
                if (!getFocusEndInstant().equals(draggingEndInstant)) {
                    setFocusEndInstant(draggingEndInstant);
                    getDataTableView().resizeView();
                }
            }
        });
    }

    public Instant getFocusEndInstant() { return focusEndInstantValue.get(); }

    public void setFocusEndInstant(Instant instant) { focusEndInstantValue.set(instant); }

    public ObjectProperty<Instant> focusEndInstantProperty() { return focusEndInstantValue; }

    public Instant getFocusStartInstant() { return focusStartInstantValue.get(); }

    public void setFocusStartInstant(Instant instant) { focusStartInstantValue.set(instant); }

    public ObjectProperty<Instant> focusStartInstantProperty() { return focusStartInstantValue; }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        startInstantText.setX(getBounds().getMinX() + ((width - startInstantText.getLayoutBounds().getWidth()) / 2.));
        startInstantText.setY(getLowerContextBar().getLayoutBounds().getMaxY() + MINMAX_VALUE_TEXT_HEIGHT);
//        startInstantText.setY(getFocusMinPosition() + startInstantText.getLayoutBounds().getHeight());

        endInstantText.setX(getBounds().getMinX() + ((width - endInstantText.getLayoutBounds().getWidth()) / 2.));
        endInstantText.setY(getUpperContextBar().getLayoutBounds().getMinY() - 4);
//        endInstantText.setY(getFocusMaxPosition() - 4);

        focusStartInstantText.setX(getBarRightX() + 4);
        focusStartInstantText.setY(getFocusMinPosition() + focusStartInstantText.getFont().getSize());

        focusEndInstantText.setX(getBarRightX() + 4);
        focusEndInstantText.setY(getFocusMaxPosition());

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                // resize histogram bin information
                TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

                overallHistogramGroup.getChildren().clear();
                overallHistogramRectangles.clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    Instant binLowerBound = histogram.getBinLowerBound(i);
                    Instant binUpperBound = histogram.getBinUpperBound(i);

                    if (!(binLowerBound.isBefore(getFocusStartInstant())) && !(binUpperBound.isAfter(getFocusEndInstant()))) {
                        double binLowerY = GraphicsUtil.mapValue(binLowerBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());
                        double binUpperY = GraphicsUtil.mapValue(binUpperBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());

//                        double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                        double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                        double x = getBounds().getMinX() + ((width - binWidth) / 2.);
//                        Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                        rectangle.setStroke(histogramFill.darker());
                        rectangle.setFill(histogramFill);
                        overallHistogramRectangles.add(rectangle);
                        overallHistogramGroup.getChildren().add(rectangle);
                    }
                }

                queryHistogramGroup.getChildren().clear();
                queryHistogramRectangles.clear();

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
//                    TemporalColumnSummaryStats queryColumnSummaryStats = (TemporalColumnSummaryStats) dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    // resize query histogram bins
                    TemporalHistogram queryHistogram = ((TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

                    if (queryHistogram != null) {
                        if (getDataTable().getCalculateQueryStatistics()) {
//                            if (histogram.getNumBins() != queryHistogram.getNumBins()) {
//                                log.info("query histogram and overall histogram have different bin sizes");
//                            }

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                if (queryHistogram.getBinCount(i) > 0) {
                                    Instant binLowerBound = queryHistogram.getBinLowerBound(i);
                                    Instant binUpperBound = queryHistogram.getBinUpperBound(i);

                                    if (!(binLowerBound.isBefore(getFocusStartInstant())) && !(binUpperBound.isAfter(getFocusEndInstant()))) {
                                        double binLowerY = GraphicsUtil.mapValue(binLowerBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());
                                        double binUpperY = GraphicsUtil.mapValue(binUpperBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());

                                        //                                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                                        double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                                0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2,
                                                DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                                        double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                                        //                                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
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
}
