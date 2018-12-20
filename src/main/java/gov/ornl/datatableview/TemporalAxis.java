package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.logging.Logger;

public class TemporalAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(TemporalAxis.class.getName());

    private Text startInstantText;
    private Text endInstantText;

    private Text focusStartInstantText;
    private Text focusEndInstantText;

    private TemporalAxisSelection draggingSelection;

    private Instant draggingStartInstant;
    private Instant draggingEndInstant;
    private Text draggingContextInstantText;
    private Line draggingContextLine;

    private Group overallInteriorHistogramRectangleGroup = new Group();
    private Group queryInteriorHistogramRectangleGroup = new Group();

    public TemporalAxis(DataTableView dataTableView, TemporalColumn column) {
        super(dataTableView, column);

        draggingContextLine = new Line();
        draggingContextLine.setStroke(getLowerContextBarHandle().getStroke());
        draggingContextLine.setStrokeWidth(getLowerContextBarHandle().getStrokeWidth());
        draggingContextLine.setStrokeLineCap(getLowerContextBarHandle().getStrokeLineCap());

        draggingContextInstantText = new Text();
        draggingContextInstantText.setFill(Color.BLACK);
        draggingContextInstantText.setFont(Font.font(DEFAULT_TEXT_SIZE));

        startInstantText = new Text(temporalColumn().getStatistics().getStartInstant().toString());
        startInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        startInstantText.setSmooth(true);
        startInstantText.setTextOrigin(VPos.TOP);
        startInstantText.setTranslateY(1.);

        endInstantText = new Text(temporalColumn().getStatistics().getEndInstant().toString());
        endInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        endInstantText.setSmooth(true);
        endInstantText.setTextOrigin(VPos.BOTTOM);
        endInstantText.setTranslateY(-2.);

        focusStartInstantText = new Text(column.getStartFocusValue().toString());
        focusStartInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusStartInstantText.setSmooth(true);
        focusStartInstantText.setTextOrigin(VPos.TOP);
        focusStartInstantText.setTranslateY(2.);
        focusStartInstantText.setMouseTransparent(true);

        focusEndInstantText = new Text(column.getEndFocusValue().toString());
        focusEndInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusEndInstantText.setSmooth(true);
        focusEndInstantText.setTextOrigin(VPos.BOTTOM);
        focusEndInstantText.setTranslateY(-3.);
        focusEndInstantText.setMouseTransparent(true);

        getAxisBar().setWidth(DEFAULT_BAR_WIDTH);

        getGraphicsGroup().getChildren().addAll(startInstantText, endInstantText, focusStartInstantText,
                focusEndInstantText, overallInteriorHistogramRectangleGroup, queryInteriorHistogramRectangleGroup);

        overallInteriorHistogramRectangleGroup.setMouseTransparent(true);
        queryInteriorHistogramRectangleGroup.setMouseTransparent(true);

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
        if (instant.isAfter(temporalColumn().getEndFocusValue())) {
            return GraphicsUtil.mapValue(instant, temporalColumn().getEndFocusValue(), temporalColumn().getEndScaleValue(),
                    getMaxFocusPosition(), getUpperContextBar().getY());
        } else if (instant.isBefore(temporalColumn().getStartFocusValue())) {
            return GraphicsUtil.mapValue(instant, temporalColumn().getStartFocusValue(), temporalColumn().getStartScaleValue(),
                    getMinFocusPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight());
        }

        return GraphicsUtil.mapValue(instant, temporalColumn().getStartFocusValue(),
                temporalColumn().getEndFocusValue(),
                getMinFocusPosition(), getMaxFocusPosition());
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        if (axisPosition < getMaxFocusPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getMaxFocusPosition(), getUpperContextBar().getY(),
                    temporalColumn().getEndFocusValue(), temporalColumn().getEndScaleValue());
        } else if (axisPosition > getMinFocusPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getUpperContextBar().getY() + getUpperContextBar().getHeight(),
                    temporalColumn().getStartFocusValue(), temporalColumn().getStartScaleValue());
        }

        return GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getMaxFocusPosition(),
                temporalColumn().getStartFocusValue(), temporalColumn().getEndFocusValue());
    }

    private void registerListeners() {

        temporalColumn().startScaleValueProperty().addListener(observable -> {
            startInstantText.setText(temporalColumn().getStartScaleValue().toString());

        });

        temporalColumn().endScaleValueProperty().addListener(observable -> {
            endInstantText.setText(temporalColumn().getEndScaleValue().toString());

        });

        temporalColumn().startFocusValueProperty().addListener(observable -> {
            focusStartInstantText.setText(temporalColumn().getStartFocusValue().toString());
        });

        temporalColumn().endFocusValueProperty().addListener(observable -> {
            focusEndInstantText.setText(temporalColumn().getEndFocusValue().toString());
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

            selectionMaxY = selectionMaxY < getMaxFocusPosition() ? getMaxFocusPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getMinFocusPosition() ? getMinFocusPosition() : selectionMinY;

            Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getMaxFocusPosition(), getMinFocusPosition(),
                    temporalColumn().getEndFocusValue(), temporalColumn().getStartFocusValue());
            Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getMaxFocusPosition(), getMinFocusPosition(),
                    temporalColumn().getEndFocusValue(), temporalColumn().getStartFocusValue());

            if (draggingSelection == null) {
                TemporalColumnSelectionRange selectionRange = new TemporalColumnSelectionRange(temporalColumn(), selectionStartInstant, selectionEndInstant);
                draggingSelection = new TemporalAxisSelection(this, selectionRange, selectionMinY, selectionMaxY);
                axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
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
            hoverValueText.toFront();
        });

        getLowerContextBarHandle().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getLowerContextBarHandle().getStartX());
                draggingContextLine.setEndX(getLowerContextBarHandle().getEndX());
                draggingContextLine.setStartY(getLowerContextBarHandle().getStartY());
                draggingContextLine.setEndY(getLowerContextBarHandle().getEndY());
                draggingContextLine.setTranslateY(0);
                draggingContextInstantText.setX(getCenterX());
                draggingContextInstantText.setY(focusStartInstantText.getY());
                draggingContextInstantText.setTranslateY(0);
                draggingContextInstantText.setTextOrigin(VPos.TOP);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextInstantText);
                getLowerContextBarHandle().setVisible(false);
                focusStartInstantText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            Instant newFocusStartInstant;
            if (y > getMinFocusPosition()) {
                // above the focus start instant in lower context region (use context range)
                newFocusStartInstant = GraphicsUtil.mapValue(y, getMinFocusPosition(),
                        getLowerContextBar().getLayoutBounds().getMaxY(),
                        temporalColumn().getStartFocusValue(), temporalColumn().getStartScaleValue());
                if (newFocusStartInstant.isBefore(temporalColumn().getStartScaleValue())) {
                    newFocusStartInstant = Instant.from(temporalColumn().getStartScaleValue());
                }
                if (y > getLowerContextBar().getLayoutBounds().getMaxY()) {
                    dy = getLowerContextBar().getLayoutBounds().getMaxY() - dragStartPoint.getY();
                }
            } else {
                // inside focus region (use focus start and end)
                newFocusStartInstant = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getMinFocusPosition(),
                        temporalColumn().getEndFocusValue(), temporalColumn().getStartFocusValue());
                if (newFocusStartInstant.isAfter(temporalColumn().getEndFocusValue())) {
                    newFocusStartInstant = Instant.from(temporalColumn().getEndFocusValue());
                    dy = -getAxisBar().getHeight();
                }
            }

            draggingStartInstant = Instant.from(newFocusStartInstant);
            draggingContextInstantText.setText(draggingStartInstant.toString());
            draggingContextInstantText.setTranslateY(dy + 2);
            draggingContextInstantText.setTranslateX(-draggingContextInstantText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
        });

        getLowerContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextInstantText, draggingContextLine);
                getLowerContextBarHandle().setVisible(true);
                focusStartInstantText.setVisible(true);

                if (!temporalColumn().getStartFocusValue().equals(draggingStartInstant)) {
                    getDataTable().setTemporalColumnFocusExtents(temporalColumn(), draggingStartInstant,
                            temporalColumn().getEndFocusValue());
                }
            }
        });

        getUpperContextBarHandle().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getUpperContextBarHandle().getStartX());
                draggingContextLine.setEndX(getUpperContextBarHandle().getEndX());
                draggingContextLine.setStartY(getUpperContextBarHandle().getStartY());
                draggingContextLine.setEndY(getUpperContextBarHandle().getEndY());
                draggingContextLine.setTranslateY(0);
                draggingContextInstantText.setX(getCenterX());
                draggingContextInstantText.setY(focusEndInstantText.getY());
                draggingContextInstantText.setTextOrigin(VPos.BOTTOM);
                draggingContextInstantText.setTranslateY(0);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextInstantText);
                getUpperContextBarHandle().setVisible(false);
                focusEndInstantText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            Instant newFocusEndInstant;
            if (y < getMaxFocusPosition()) {
                // above the focus end instant in upper context region (use context range)
                newFocusEndInstant = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getUpperContextBar().getY(),
                        temporalColumn().getEndFocusValue(), temporalColumn().getEndScaleValue());
                if (newFocusEndInstant.isAfter(temporalColumn().getEndScaleValue())) {
                    newFocusEndInstant = Instant.from(temporalColumn().getEndScaleValue());
                }
                if (y < getUpperContextBar().getY()) {
                    dy = getUpperContextBar().getY() - dragStartPoint.getY();
                }
            } else {
                // inside focus region (use focus start and end)
                newFocusEndInstant = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getMinFocusPosition(),
                        temporalColumn().getEndFocusValue(), temporalColumn().getStartFocusValue());
                if (newFocusEndInstant.isBefore(temporalColumn().getStartFocusValue())) {
                    newFocusEndInstant = Instant.from(temporalColumn().getStartFocusValue());
                    dy = getAxisBar().getHeight();
                }
            }

            draggingEndInstant = Instant.from(newFocusEndInstant);
            draggingContextInstantText.setText(draggingEndInstant.toString());
            draggingContextInstantText.setTranslateY(dy - 2);
            draggingContextInstantText.setTranslateX(-draggingContextInstantText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
        });

        getUpperContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextInstantText, draggingContextLine);
                getUpperContextBarHandle().setVisible(true);
                focusEndInstantText.setVisible(true);
                if (!temporalColumn().getEndFocusValue().equals(draggingEndInstant)) {
                    getDataTable().setTemporalColumnFocusExtents(temporalColumn(), temporalColumn().getStartFocusValue(),
                            draggingEndInstant);
                }
            }
        });
    }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        startInstantText.setX(getBounds().getMinX() + ((width - startInstantText.getLayoutBounds().getWidth()) / 2.));
        startInstantText.setY(getLowerContextBar().getY() + getLowerContextBar().getHeight());

        endInstantText.setX(getBounds().getMinX() + ((width - endInstantText.getLayoutBounds().getWidth()) / 2.));
        endInstantText.setY(getUpperContextBar().getY());

        focusStartInstantText.setX(getBounds().getMinX() + ((width - focusStartInstantText.getLayoutBounds().getWidth()) / 2.));
        focusStartInstantText.setY(getAxisBar().getY() + getAxisBar().getHeight());

        focusEndInstantText.setX(getBounds().getMinX() + ((width - focusEndInstantText.getLayoutBounds().getWidth()) / 2.));
        focusEndInstantText.setY(getAxisBar().getY());

        if (!getDataTable().isEmpty()) {
            TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
            TemporalHistogram queryHistogram = null;
            if (getDataTable().getActiveQuery().hasColumnSelections()) {
                TemporalColumnSummaryStats stats = (TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn());
                if (stats != null) {
                    queryHistogram = stats.getHistogram();
                }
            }

            overallInteriorHistogramRectangleGroup.getChildren().clear();
            queryInteriorHistogramRectangleGroup.getChildren().clear();

            Line zeroLine = new Line(getCenterX(), getMaxFocusPosition(), getCenterX(), getMinFocusPosition());
            zeroLine.strokeProperty().bind(overallHistogramStroke);

            overallInteriorHistogramRectangleGroup.getChildren().add(zeroLine);

            for (int i = 0; i < histogram.getNumBins(); i++) {
                Instant binLowerBound = histogram.getBinLowerBound(i);
                Instant binUpperBound = histogram.getBinUpperBound(i);

                if (!(binLowerBound.isBefore(temporalColumn().getStartFocusValue())) && !(binUpperBound.isAfter(temporalColumn().getEndFocusValue()))) {
                    double binLowerY = GraphicsUtil.mapValue(binLowerBound, temporalColumn().getStartFocusValue(),
                            temporalColumn().getEndFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                    double binUpperY = GraphicsUtil.mapValue(binUpperBound, temporalColumn().getStartFocusValue(),
                            temporalColumn().getEndFocusValue(), getMinFocusPosition(), getMaxFocusPosition());

                    if (histogram.getBinCount(i) > 0) {
                        double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0,
                                histogram.getMaxBinCount(), 1, getAxisBar().getWidth());
                        double x = getCenterX() - (binWidth / 2.);
                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                        rectangle.fillProperty().bind(overallHistogramFill);
                        rectangle.strokeProperty().bind(overallHistogramStroke);
                        overallInteriorHistogramRectangleGroup.getChildren().add(rectangle);

                        if (queryHistogram != null) {
                            binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                    0, histogram.getMaxBinCount(), 1.,
                                    getAxisBar().getWidth());
                            x = getCenterX() - (binWidth / 2.);
                            rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                            rectangle.strokeProperty().bind(queryHistogramStroke);
                            rectangle.fillProperty().bind(queryHistogramFill);
                            queryInteriorHistogramRectangleGroup.getChildren().add(rectangle);
                        }
                    }
                }
            }
        }
    }
}
