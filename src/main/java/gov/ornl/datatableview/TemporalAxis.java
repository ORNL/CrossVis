package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class TemporalAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(TemporalAxis.class.getName());

    // histogram bin rectangles
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup= new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Color histogramFill = DEFAULT_HISTOGRAM_FILL;
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

    private Group overallInteriorHistogramRectangleGroup = new Group();
    private Group queryInteriorHistogramRectangleGroup = new Group();

    public TemporalAxis(DataTableView dataTableView, Column column) {
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

        focusStartInstantValue = new SimpleObjectProperty<>(temporalColumn().getStartScaleValue());
        focusStartInstantText = new Text(focusStartInstantValue.get().toString());
        focusStartInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusStartInstantText.setSmooth(true);
        focusStartInstantText.setTextOrigin(VPos.TOP);
        focusStartInstantText.setTranslateY(2.);
        focusStartInstantText.setMouseTransparent(true);

        focusEndInstantValue = new SimpleObjectProperty<>(temporalColumn().getEndScaleValue());
        focusEndInstantText = new Text(focusEndInstantValue.get().toString());
        focusEndInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusEndInstantText.setSmooth(true);
        focusEndInstantText.setTextOrigin(VPos.BOTTOM);
        focusEndInstantText.setTranslateY(-3.);
        focusEndInstantText.setMouseTransparent(true);

        getAxisBar().setWidth(DEFAULT_BAR_WIDTH);

        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

//        getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
//        getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        getGraphicsGroup().getChildren().addAll(startInstantText,
                endInstantText, focusStartInstantText, focusEndInstantText, overallInteriorHistogramRectangleGroup,
                queryInteriorHistogramRectangleGroup);

        overallInteriorHistogramRectangleGroup.setMouseTransparent(true);
        queryInteriorHistogramRectangleGroup.setMouseTransparent(true);
        registerListeners();

//        rebuildAxisInteriorSummaryData();
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
        if (instant.isAfter(getFocusEndInstant())) {
            return GraphicsUtil.mapValue(instant, getFocusEndInstant(), temporalColumn().getEndScaleValue(),
                    getFocusMaxPosition(), getUpperContextBar().getY());
//            return GraphicsUtil.mapValue(instant, getFocusEndInstant(), temporalColumn().getStatistics().getEndInstant(),
//                    getFocusMaxPosition(), getUpperContextBar().getY());
        } else if (instant.isBefore(getFocusStartInstant())) {
            return GraphicsUtil.mapValue(instant, getFocusStartInstant(), temporalColumn().getStartScaleValue(),
                    getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight());
//            return GraphicsUtil.mapValue(instant, getFocusStartInstant(), temporalColumn().getStatistics().getStartInstant(),
//                    getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight());
        }

        return GraphicsUtil.mapValue(instant, getFocusStartInstant(), getFocusEndInstant(),
                getFocusMinPosition(), getFocusMaxPosition());
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        if (axisPosition < getFocusMaxPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getFocusMaxPosition(), getUpperContextBar().getY(),
                    getFocusEndInstant(), temporalColumn().getEndScaleValue());
//            return GraphicsUtil.mapValue(axisPosition, getFocusMaxPosition(), getUpperContextBar().getY(),
//                    getFocusEndInstant(), temporalColumn().getStatistics().getEndInstant());
        } else if (axisPosition > getFocusMinPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getUpperContextBar().getY() + getUpperContextBar().getHeight(),
                    getFocusStartInstant(), temporalColumn().getStartScaleValue());
//            return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getUpperContextBar().getY() + getUpperContextBar().getHeight(),
//                    getFocusStartInstant(), temporalColumn().getStatistics().getStartInstant());
        }

        return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getFocusMaxPosition(),
                getFocusStartInstant(), getFocusEndInstant());
    }

//    public void rebuildAxisInteriorSummaryData() {
//        Tuple tuples[];
//        Instant values[];
//
//        if (getDataTable().getActiveQuery().hasColumnSelections()) {
//            Set<Tuple> tupleList = getDataTable().getActiveQuery().getQueriedTuples();
//            tuples = new Tuple[tupleList.size()];
//            int idx = 0;
//            for (Tuple tuple : tupleList) {
//                tuples[idx++] = tuple;
//            }
//            values = temporalColumn().getQueriedValues();
//        } else {
//            List<Tuple> tupleList = getDataTable().getTuples();
//            tuples = new Tuple[tupleList.size()];
//            for (int i = 0; i < tupleList.size(); i++) {
//                tuples[i] = tupleList.get(i);
//            }
//            values = temporalColumn().getValues();
//        }
//
//        axisInteriorHistogram = new TemporalHistogram("interior", values, tuples,
//                (int)(getAxisBar().getHeight() / 2.), getFocusStartInstant(), getFocusEndInstant());
////        TemporalHistogram interiorHistogram = new TemporalHistogram("interior", )
//    }

//    private void initAxisInteriorGraphics() {
//        axisInteriorRectangleGroup.getChildren().clear();
//
//        if (getDataTableView().getHighlightedAxis() != this) {
//            for (int ibin = axisInteriorHistogram.getNumBins() - 1; ibin >= 0; ibin--) {
//                double value = axisInteriorHistogram.getBinCount(ibin);
//                double binRectangleWidth = GraphicsUtil.mapValue(value, 0, axisInteriorHistogram.getMaxBinCount(),
//                        0, getAxisBar().getWidth());
//                double y = getFocusMaxPosition() + (ibin * 2.);
//                Rectangle binRectangle = new Rectangle(getCenterX() - (binRectangleWidth / 2.), y,
//                        binRectangleWidth, 2.);
//                binRectangle.setFill(Color.BLUE);
//                binRectangle.setStroke(Color.BLUE);
//                axisInteriorRectangleGroup.getChildren().add(binRectangle);
//            }
//        }
//    }

    private void registerListeners() {
//        getDataTableView().highlightedAxisProperty().addListener(observable -> {
//            initAxisInteriorGraphics();
//        });

        temporalColumn().startScaleValueProperty().addListener(observable -> {
            startInstantText.setText(temporalColumn().getStartScaleValue().toString());
            if (getFocusStartInstant().isBefore(temporalColumn().getStartScaleValue())) {
                setFocusStartInstant(temporalColumn().getStartScaleValue());
            }
        });

        temporalColumn().endScaleValueProperty().addListener(observable -> {
            endInstantText.setText(temporalColumn().getEndScaleValue().toString());
            if (getFocusEndInstant().isAfter(temporalColumn().getEndScaleValue())) {
                setFocusEndInstant(temporalColumn().getEndScaleValue());
            }
        });

        focusStartInstantValue.addListener((observable, oldValue, newValue) -> {
            focusStartInstantText.setText(newValue.toString());
        });

        focusEndInstantValue.addListener((observable, oldValue, newValue) -> {
            focusEndInstantText.setText(newValue.toString());
        });

//        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null && newValue != oldValue) {
//                if (newValue) {
//                    resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
//                }
//
//                overallHistogramGroup.setVisible(newValue);
//                queryHistogramGroup.setVisible(newValue);
//            }
//        });

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
                    getFocusEndInstant(), getFocusStartInstant());
            Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
                    getFocusEndInstant(), getFocusStartInstant());

            if (draggingSelection == null) {
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
            if (y > getFocusMinPosition()) {
                // above the focus start instant in lower context region (use context range)
                newFocusStartInstant = GraphicsUtil.mapValue(y, getFocusMinPosition(),
                        getLowerContextBar().getLayoutBounds().getMaxY(),
                        getFocusStartInstant(), temporalColumn().getStartScaleValue());
                if (newFocusStartInstant.isBefore(temporalColumn().getStartScaleValue())) {
                    newFocusStartInstant = Instant.from(temporalColumn().getStartScaleValue());
//                    dy = getUpperContextBar().getHeight();
                }
                if (y > getLowerContextBar().getLayoutBounds().getMaxY()) {
                    dy = getLowerContextBar().getLayoutBounds().getMaxY() - dragStartPoint.getY();
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

                if (!getFocusStartInstant().equals(draggingStartInstant)) {
                    setFocusStartInstant(draggingStartInstant);
                    getDataTableView().resizeView();

                    ArrayList<TemporalAxisSelection> selectionsToRemove = new ArrayList<>();
                    for (AxisSelection axisSelection : getAxisSelectionList()) {
                        TemporalAxisSelection temporalAxisSelection = (TemporalAxisSelection)axisSelection;
                        if (temporalAxisSelection.getTemporalColumnSelectionRange().getStartInstant().isBefore(getFocusStartInstant()) ||
                                temporalAxisSelection.getTemporalColumnSelectionRange().getEndInstant().isAfter(getFocusEndInstant())) {
                            selectionsToRemove.add(temporalAxisSelection);
                        }
                    }

                    if (!selectionsToRemove.isEmpty()) {
                        for (TemporalAxisSelection temporalAxisSelection : selectionsToRemove) {
                            getDataTable().removeColumnSelectionFromActiveQuery(temporalAxisSelection.getColumnSelection());
                        }
                    }
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
            if (y < getFocusMaxPosition()) {
                // above the focus end instant in upper context region (use context range)
                newFocusEndInstant = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getUpperContextBar().getY(),
                        getFocusEndInstant(), temporalColumn().getEndScaleValue());
                if (newFocusEndInstant.isAfter(temporalColumn().getEndScaleValue())) {
                    newFocusEndInstant = Instant.from(temporalColumn().getEndScaleValue());
//                    dy = -getUpperContextBar().getHeight();
                }
                if (y < getUpperContextBar().getY()) {
                    dy = getUpperContextBar().getY() - dragStartPoint.getY();
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
                if (!getFocusEndInstant().equals(draggingEndInstant)) {
                    setFocusEndInstant(draggingEndInstant);
                    getDataTableView().resizeView();

                    ArrayList<TemporalAxisSelection> selectionsToRemove = new ArrayList<>();
                    for (AxisSelection axisSelection : getAxisSelectionList()) {
                        TemporalAxisSelection temporalAxisSelection = (TemporalAxisSelection)axisSelection;
                        if (temporalAxisSelection.getTemporalColumnSelectionRange().getStartInstant().isBefore(getFocusStartInstant()) ||
                                temporalAxisSelection.getTemporalColumnSelectionRange().getEndInstant().isAfter(getFocusEndInstant())) {
                            selectionsToRemove.add(temporalAxisSelection);
                        }
                    }

                    if (!selectionsToRemove.isEmpty()) {
                        for (TemporalAxisSelection temporalAxisSelection : selectionsToRemove) {
                            getDataTable().removeColumnSelectionFromActiveQuery(temporalAxisSelection.getColumnSelection());
                        }
                    }
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

//        rebuildAxisInteriorSummaryData();

        startInstantText.setX(getBounds().getMinX() + ((width - startInstantText.getLayoutBounds().getWidth()) / 2.));
        startInstantText.setY(getLowerContextBar().getY() + getLowerContextBar().getHeight());

        endInstantText.setX(getBounds().getMinX() + ((width - endInstantText.getLayoutBounds().getWidth()) / 2.));
//        endInstantText.setY(getUpperContextBar().getLayoutBounds().getMinY() - 4);
        endInstantText.setY(getUpperContextBar().getY());

        focusStartInstantText.setX(getBounds().getMinX() + ((width - focusStartInstantText.getLayoutBounds().getWidth()) / 2.));
        focusStartInstantText.setY(getAxisBar().getY() + getAxisBar().getHeight());
//        focusStartInstantText.setX(getBarRightX() + 4);
//        focusStartInstantText.setY(getFocusMinPosition() + focusStartInstantText.getFont().getSize());

        focusEndInstantText.setX(getBounds().getMinX() + ((width - focusEndInstantText.getLayoutBounds().getWidth()) / 2.));
        focusEndInstantText.setY(getAxisBar().getY());
//        focusEndInstantText.setX(getBarRightX() + 4);
//        focusEndInstantText.setY(getFocusMaxPosition());

        if (!getDataTable().isEmpty()) {
            TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
            TemporalHistogram queryHistogram = null;
            if (getDataTable().getActiveQuery().hasColumnSelections()) {
                queryHistogram = ((TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();
            }

            overallInteriorHistogramRectangleGroup.getChildren().clear();
            queryInteriorHistogramRectangleGroup.getChildren().clear();

            Line zeroLine = new Line(getCenterX(), getFocusMaxPosition(), getCenterX(), getFocusMinPosition());
            zeroLine.setStroke(histogramFill.darker());
            overallInteriorHistogramRectangleGroup.getChildren().add(zeroLine);

            for (int i = 0; i < histogram.getNumBins(); i++) {
                Instant binLowerBound = histogram.getBinLowerBound(i);
                Instant binUpperBound = histogram.getBinUpperBound(i);

                if (!(binLowerBound.isBefore(getFocusStartInstant())) && !(binUpperBound.isAfter(getFocusEndInstant()))) {
                    double binLowerY = GraphicsUtil.mapValue(binLowerBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());
                    double binUpperY = GraphicsUtil.mapValue(binUpperBound, getFocusStartInstant(), getFocusEndInstant(), getFocusMinPosition(), getFocusMaxPosition());

                    if (histogram.getBinCount(i) > 0) {
//                        double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                        double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0,
                                histogram.getMaxBinCount(), 1, getAxisBar().getWidth());
                        double x = getCenterX() - (binWidth / 2.);
//                        Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                        rectangle.setStroke(histogramFill.darker());
                        rectangle.setFill(histogramFill);
                        overallInteriorHistogramRectangleGroup.getChildren().add(rectangle);

                        if (queryHistogram != null) {
                            binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                    0, histogram.getMaxBinCount(), 1.,
                                    getAxisBar().getWidth());
                            x = getCenterX() - (binWidth / 2.);
                            rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                            rectangle.setStroke(queryHistogramFill.darker());
                            rectangle.setFill(queryHistogramFill);
                            queryInteriorHistogramRectangleGroup.getChildren().add(rectangle);
                        }
                    }
                }
            }

            /*
            if (getDataTableView().isShowingHistograms()) {
                // resize histogram bin information
                histogram = temporalColumn().getStatistics().getHistogram();
//                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

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
                    queryHistogram = ((TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

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
            */
        }
    }
}
