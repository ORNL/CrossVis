package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.logging.Logger;

public class DoubleAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(DoubleAxis.class.getName());

    public final static Color DEFAULT_OVERALL_DISPERSION_FILL = new Color(Color.SILVER.getRed(),
            Color.SILVER.getGreen(), Color.SILVER.getBlue(), 0.8);
    public final static Color DEFAULT_TYPICAL_LINE_STROKE = Color.DARKBLUE;

    private SimpleObjectProperty<Paint> overallDispersionFill = new SimpleObjectProperty<>(DEFAULT_OVERALL_DISPERSION_FILL);
    private SimpleObjectProperty<Paint> overallDispersionStroke = new SimpleObjectProperty<>(DEFAULT_OVERALL_DISPERSION_FILL.darker());
    private SimpleObjectProperty<Paint> typicalValueLineStroke = new SimpleObjectProperty<>(DEFAULT_TYPICAL_LINE_STROKE);

    private SimpleObjectProperty<Paint> queryDispersionFill;
    private SimpleObjectProperty<Paint> queryDispersionStroke;
    private SimpleObjectProperty<Paint> nonqueryDispersionFill;
    private SimpleObjectProperty<Paint> nonqueryDispersionStroke;

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
//    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup = new Group();
//    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Text minValueText;
    private Text maxValueText;

    private Text minFocusValueText;
    private Text maxFocusValueText;

//    private DoubleProperty minFocusValue;
//    private DoubleProperty maxFocusValue;

    private DoubleAxisSelection draggingSelection;

    private double draggingMinFocusValue;
    private double draggingMaxFocusValue;
    private Text draggingContextValueText;
    private Line draggingContextLine;

    public DoubleAxis(DataTableView dataTableView, DoubleColumn column) {
        super(dataTableView, column);

        queryDispersionFill = new SimpleObjectProperty<>(new Color(dataTableView.getSelectedItemsColor().getRed(),
                dataTableView.getSelectedItemsColor().getGreen(),
                dataTableView.getSelectedItemsColor().getBlue(), ((Color)overallDispersionFill.get()).getOpacity()));
        queryDispersionStroke = new SimpleObjectProperty<>(((Color)queryDispersionFill.get()).darker());
        nonqueryDispersionFill = new SimpleObjectProperty<>(new Color(dataTableView.getUnselectedItemsColor().getRed(),
                dataTableView.getUnselectedItemsColor().getGreen(),
                dataTableView.getUnselectedItemsColor().getBlue(), ((Color)overallDispersionFill.get()).getOpacity()));
        nonqueryDispersionStroke = new SimpleObjectProperty<>(((Color)nonqueryDispersionFill.get()).darker());

        draggingContextLine = new Line();
        draggingContextLine.setStroke(getLowerContextBarHandle().getStroke());
        draggingContextLine.setStrokeWidth(getLowerContextBarHandle().getStrokeWidth());
        draggingContextLine.setStrokeLineCap(StrokeLineCap.ROUND);

        draggingContextValueText = new Text();
        draggingContextValueText.setFill(Color.BLACK);
        draggingContextValueText.setFont(Font.font(DEFAULT_TEXT_SIZE));

//        minFocusValue = new SimpleDoubleProperty(column.getMinimumScaleValue());
//        maxFocusValue = new SimpleDoubleProperty(column.getMaximumScaleValue());

        minValueText = new Text(String.valueOf(column.getMinimumScaleValue()));
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);
        minValueText.setTextOrigin(VPos.TOP);
        minValueText.setTranslateY(1.);

        maxValueText = new Text(String.valueOf(column.getMaximumScaleValue()));
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);
        maxValueText.setTextOrigin(VPos.BOTTOM);
        maxValueText.setTranslateY(-2.);

        minFocusValueText = new Text(String.valueOf(column.getMinimumFocusValue()));
        minFocusValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minFocusValueText.setSmooth(true);
        minFocusValueText.setTextOrigin(VPos.TOP);
        minFocusValueText.setTranslateY(2.);
        minFocusValueText.setMouseTransparent(true);

        maxFocusValueText = new Text(String.valueOf(column.getMaximumFocusValue()));
        maxFocusValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxFocusValueText.setSmooth(true);
        maxFocusValueText.setTextOrigin(VPos.BOTTOM);
        maxFocusValueText.setTranslateY(-3.);
        maxFocusValueText.setMouseTransparent(true);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.fillProperty().bind(overallDispersionFill);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.strokeProperty().bind(typicalValueLineStroke);

        overallSummaryStatisticsGroup.getChildren().addAll(overallDispersionRectangle, overallTypicalLine);

        queryDispersionRectangle = new Rectangle();
        queryDispersionRectangle.fillProperty().bind(queryDispersionFill);
        queryDispersionRectangle.strokeProperty().bind(queryDispersionStroke);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.strokeProperty().bind(typicalValueLineStroke);

        querySummaryStatisticsGroup.getChildren().addAll(queryDispersionRectangle, queryTypicalLine);

        nonqueryDispersionRectangle = new Rectangle();
        nonqueryDispersionRectangle.fillProperty().bind(nonqueryDispersionFill);
        nonqueryDispersionRectangle.strokeProperty().bind(nonqueryDispersionStroke);
        nonqueryDispersionRectangle.setSmooth(true);
        nonqueryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        nonqueryTypicalLine = makeLine();
        nonqueryTypicalLine.strokeProperty().bind(typicalValueLineStroke);

        nonquerySummaryStatisticsGroup.getChildren().addAll(nonqueryDispersionRectangle, nonqueryTypicalLine);

        overallSummaryStatisticsGroup.setMouseTransparent(true);
        querySummaryStatisticsGroup.setMouseTransparent(true);
        nonquerySummaryStatisticsGroup.setMouseTransparent(true);
        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
        getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        getGraphicsGroup().getChildren().addAll(minValueText,
                maxValueText, minFocusValueText, maxFocusValueText, overallSummaryStatisticsGroup,
                querySummaryStatisticsGroup, nonquerySummaryStatisticsGroup);

        registerListeners();
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        if (axisPosition < getMaxFocusPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getMaxFocusPosition(), getUpperContextBar().getY(),
                    doubleColumn().getMaximumFocusValue(), doubleColumn().getMaximumScaleValue());
        } else if (axisPosition > getMinFocusPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight(),
                    doubleColumn().getMinimumFocusValue(), doubleColumn().getMinimumScaleValue());
        }

        return GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getMaxFocusPosition(),
                doubleColumn().getMinimumFocusValue(), doubleColumn().getMaximumFocusValue());
    }

    private double getAxisPositionForValue(double value) {
        if (value > doubleColumn().getMaximumFocusValue()) {
            return GraphicsUtil.mapValue(value, doubleColumn().getMaximumFocusValue(),
                    doubleColumn().getMaximumScaleValue(),
                    getMaxFocusPosition(), getUpperContextBar().getY());
        } else if (value < doubleColumn().getMinimumFocusValue()) {
            return GraphicsUtil.mapValue(value, doubleColumn().getMinimumFocusValue(),
                    doubleColumn().getMinimumScaleValue(),
                    getMinFocusPosition(),
                    getLowerContextBar().getY() + getLowerContextBar().getHeight());
        }

        return GraphicsUtil.mapValue(value, doubleColumn().getMinimumFocusValue(),
                doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
    }

    public DoubleColumn doubleColumn () {
        return (DoubleColumn)getColumn();
    }

    public DoubleAxis doubleAxis() { return (DoubleAxis)this; }

    public String toString() {
        return doubleColumn().getName();
    }

    private void registerListeners() {
        doubleColumn().minimumScaleValueProperty().addListener(observable -> {
            minValueText.setText(String.valueOf(doubleColumn().getMinimumScaleValue()));
//            if (doubleColumn().getMinimumFocusValue() < doubleColumn().getMinimumScaleValue()) {
//                setMinFocusValue(doubleColumn().getMinimumScaleValue());
//            }
        });

        doubleColumn().maximumScaleValueProperty().addListener(observable -> {
            maxValueText.setText(String.valueOf(doubleColumn().getMaximumScaleValue()));
//            if (getMaxFocusValue() > doubleColumn().getMaximumScaleValue()) {
//                setMaxFocusValue(doubleColumn().getMaximumScaleValue());
//            }
        });
//        minValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().minValueProperty(),
//                new NumberStringConverter());
//
//        maxValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().maxValueProperty(),
//                new NumberStringConverter());
//
//        doubleColumn().getStatistics().minValueProperty().addListener(observable -> {
//            if (getMinFocusValue() < doubleColumn().getStatistics().getMinValue()) {
//                setMinFocusValue(doubleColumn().getStatistics().getMinValue());
//            }
//        });
//
//        doubleColumn().getStatistics().maxValueProperty().addListener(observable -> {
//            if (getMaxFocusValue() > doubleColumn().getStatistics().getMaxValue()) {
//                setMaxFocusValue(doubleColumn().getStatistics().getMaxValue());
//            }
//        });

        doubleColumn().minimumFocusValueProperty().addListener(observable -> {
            minFocusValueText.setText(String.valueOf(doubleColumn().getMinimumFocusValue()));
        });

        doubleColumn().maximumFocusValueProperty().addListener(observable -> {
            maxFocusValueText.setText(String.valueOf(doubleColumn().getMaximumFocusValue()));
        });
//        minFocusValueText.textProperty().bind(Bindings.convert(minFocusValue));
//
//        maxFocusValueText.textProperty().bind(Bindings.convert(maxFocusValue));

        getDataTableView().selectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            queryDispersionFill.set(new Color(newValue.getRed(), newValue.getGreen(), newValue.getBlue(),
                    ((Color)overallDispersionFill.get()).getOpacity()));
            queryDispersionStroke.set(((Color)queryDispersionFill.get()).darker());
        });

        getDataTableView().unselectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            nonqueryDispersionFill.set(new Color(newValue.getRed(), newValue.getGreen(), newValue.getBlue(),
                    ((Color)overallDispersionFill.get()).getOpacity()));
            nonqueryDispersionStroke.set(((Color)nonqueryDispersionFill.get()).darker());
        });

        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {

                if (newValue) {
                    resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
                }

                overallHistogramGroup.setVisible(newValue);
                queryHistogramGroup.setVisible(newValue);
            }
        });

        getDataTableView().showSummaryStatisticsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                overallSummaryStatisticsGroup.setVisible(newValue);

                if (newValue) {
                    resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
                    querySummaryStatisticsGroup.setVisible(getDataTable().getCalculateQueryStatistics());
                    nonquerySummaryStatisticsGroup.setVisible(getDataTable().getCalculateNonQueryStatistics());
                } else {
                    querySummaryStatisticsGroup.setVisible(false);
                    nonquerySummaryStatisticsGroup.setVisible(false);
                }
            }
        });

        getDataTableView().summaryStatisticsDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                // calculate graphics for new setting
                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
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
                draggingContextValueText.setX(getCenterX());
                draggingContextValueText.setY(maxFocusValueText.getY());
                draggingContextValueText.setTextOrigin(VPos.BOTTOM);
                draggingContextValueText.setTranslateY(0);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
                getUpperContextBarHandle().setVisible(false);
                maxFocusValueText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            double newMaxFocusValue;
            if (y < getMaxFocusPosition()) {
                // above the max position (use context range)
                newMaxFocusValue = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getUpperContextBar().getY(),
                        doubleColumn().getMaximumFocusValue(), doubleColumn().getMaximumScaleValue());
                if (newMaxFocusValue > doubleColumn().getMaximumScaleValue()) {
                    newMaxFocusValue = doubleColumn().getMaximumScaleValue();
                }
                if (y < getUpperContextBar().getY()) {
                    dy = getUpperContextBar().getY() - dragStartPoint.getY();
                }
            } else {
                // in focus region (use focus min / max)
                newMaxFocusValue = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getMinFocusPosition(),
                        doubleColumn().getMaximumFocusValue(), doubleColumn().getMinimumFocusValue());
                if (newMaxFocusValue < doubleColumn().getMinimumFocusValue()) {
                    newMaxFocusValue = doubleColumn().getMinimumFocusValue();
                    dy = getAxisBar().getHeight();
                }
            }

            draggingMaxFocusValue = newMaxFocusValue;
            draggingContextValueText.setText(String.valueOf(draggingMaxFocusValue));
            draggingContextValueText.setTranslateY(dy - 2);
            draggingContextValueText.setTranslateX(-draggingContextValueText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
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
                draggingContextValueText.setX(getCenterX());
                draggingContextValueText.setY(minFocusValueText.getY());
                draggingContextValueText.setTranslateY(0);
                draggingContextValueText.setTextOrigin(VPos.TOP);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
                getLowerContextBarHandle().setVisible(false);
                minFocusValueText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            double newMinFocusValue;
            if (y > getMinFocusPosition()) {
                // below the min position (use context range)
                newMinFocusValue = GraphicsUtil.mapValue(y, getMinFocusPosition(),
                        getLowerContextBar().getLayoutBounds().getMaxY(), doubleColumn().getMinimumFocusValue(),
                        doubleColumn().getMinimumScaleValue());
                if (newMinFocusValue < doubleColumn().getMinimumScaleValue()) {
                    newMinFocusValue = doubleColumn().getMinimumScaleValue();
                }
                if (y > getLowerContextBar().getLayoutBounds().getMaxY()) {
                    dy = getLowerContextBar().getLayoutBounds().getMaxY() - dragStartPoint.getY();
                }
            } else {
                // in focus region (use focus min / max)
                newMinFocusValue = GraphicsUtil.mapValue(y, getMaxFocusPosition(), getMinFocusPosition(),
                        doubleColumn().getMaximumFocusValue(), doubleColumn().getMinimumFocusValue());
                if (newMinFocusValue > doubleColumn().getMaximumFocusValue()) {
                    newMinFocusValue = doubleColumn().getMaximumFocusValue();
                    dy = -getAxisBar().getHeight();
                }
            }

            draggingMinFocusValue = newMinFocusValue;
            draggingContextValueText.setText(String.valueOf(draggingMinFocusValue));
            draggingContextValueText.setTranslateY(dy + 2);
            draggingContextValueText.setTranslateX(-draggingContextValueText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
        });

        getLowerContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
                getLowerContextBarHandle().setVisible(true);
                minFocusValueText.setVisible(true);
                if (doubleColumn().getMinimumFocusValue() != draggingMinFocusValue) {
                    getDataTable().setDoubleColumnFocusExtents(doubleColumn(), draggingMinFocusValue, doubleColumn().getMaximumFocusValue());
//                    setMinFocusValue(draggingMinFocusValue);
//                    getDataTableView().resizeView();

//                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
//                    for (AxisSelection axisSelection : getAxisSelectionList()) {
//                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
//                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
//                                doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
//                            selectionsToRemove.add(doubleAxisSelection);
//                        }
//                    }
//
//                    if (!selectionsToRemove.isEmpty()) {
//                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
//                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
//                        }
//                    }
                }
            }
        });

        getUpperContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
                getUpperContextBarHandle().setVisible(true);
                maxFocusValueText.setVisible(true);
                if (doubleColumn().getMaximumFocusValue() != draggingMaxFocusValue) {
                    getDataTable().setDoubleColumnFocusExtents(doubleColumn(), doubleColumn().getMinimumFocusValue(),
                            draggingMaxFocusValue);
//                    setMaxFocusValue(draggingMaxFocusValue);
//                    getDataTableView().resizeView();

//                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
//                    for (AxisSelection axisSelection : getAxisSelectionList()) {
//                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
//                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
//                                doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
//                            selectionsToRemove.add(doubleAxisSelection);
//                        }
//                    }
//
//                    if (!selectionsToRemove.isEmpty()) {
//                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
//                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
//                        }
//                    }
                }
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

            selectionMaxY = selectionMaxY < getMaxFocusPosition() ? getMaxFocusPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getMinFocusPosition() ? getMinFocusPosition() : selectionMinY;

            double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getMaxFocusPosition(), getMinFocusPosition(),
                    doubleColumn().getMaximumFocusValue(), doubleColumn().getMinimumFocusValue());
            double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getMaxFocusPosition(), getMinFocusPosition(),
                    doubleColumn().getMaximumFocusValue(), doubleColumn().getMinimumFocusValue());
//            double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getMaxFocusPosition(), getMinFocusPosition(),
//                    doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());
//            double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getMaxFocusPosition(), getMinFocusPosition(),
//                    doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());

            if (draggingSelection == null) {
                DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
                draggingSelection = new DoubleAxisSelection(this, selectionRange, selectionMinY, selectionMaxY);
                axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
                axisSelectionGraphicsGroup.toFront();
            } else {
                draggingSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
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

        DoubleColumnSelectionRange doubleColumnSelection = (DoubleColumnSelectionRange)columnSelection;

        double selectionMinValuePosition = getAxisPositionForValue(doubleColumnSelection.getMinValue());
        double selectionMaxValuePosition = getAxisPositionForValue(doubleColumnSelection.getMaxValue());

        DoubleAxisSelection newAxisSelection = new DoubleAxisSelection(this, doubleColumnSelection,
                selectionMinValuePosition, selectionMaxValuePosition);

        axisSelectionGraphicsGroup.getChildren().add(newAxisSelection.getGraphicsGroup());
        axisSelectionGraphicsGroup.toFront();

        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    public void resize(double left, double top, double width, double height) {
        if (getDataTableView().isShowingSummaryStatistics()) {
            getAxisBar().setWidth(DEFAULT_BAR_WIDTH);
        } else {
            getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);
        }

        super.resize(left, top, width, height);

        minValueText.setX(getBounds().getMinX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(getLowerContextBar().getY() + getLowerContextBar().getHeight());

        maxValueText.setX(getBounds().getMinX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(getUpperContextBar().getY());

        minFocusValueText.setX(getBounds().getMinX() + ((width - minFocusValueText.getLayoutBounds().getWidth()) / 2));
        minFocusValueText.setY(getAxisBar().getY() + getAxisBar().getHeight());

        maxFocusValueText.setX(getBounds().getMinX() + ((width - maxFocusValueText.getLayoutBounds().getWidth()) / 2.));
        maxFocusValueText.setY(getAxisBar().getY());

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                DoubleHistogram histogram = doubleColumn().getStatistics().getHistogram();
                DoubleHistogram queryHistogram = null;
                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    queryHistogram = ((DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(doubleColumn())).getHistogram();
                }
//                double binHeight = (getMinFocusPosition() - getMaxFocusPosition()) / histogram.getNumBins();

//                overallHistogramRectangles.clear();
                overallHistogramGroup.getChildren().clear();
                queryHistogramGroup.getChildren().clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    if (histogram.getBinCount(i) > 0) {
                        double binLowerBound = histogram.getBinLowerBound(i);
                        double binUpperBound = histogram.getBinUpperBound(i);

                        if (!(binLowerBound < doubleColumn().getMinimumFocusValue()) && !(binUpperBound > doubleColumn().getMaximumFocusValue())) {
                            double binLowerY = GraphicsUtil.mapValue(binLowerBound, doubleColumn().getMinimumFocusValue(),
                                    doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                            double binUpperY = GraphicsUtil.mapValue(binUpperBound, doubleColumn().getMinimumFocusValue(),
                                    doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());

                            double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i),
                                    0, histogram.getMaxBinCount(),
                                    getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                            double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                            Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                            rectangle.strokeProperty().bind(overallHistogramStroke);
                            rectangle.fillProperty().bind(overallHistogramFill);
                            Line accentLineLeft = new Line(rectangle.getX() - 0.5, rectangle.getY(), rectangle.getX() - 0.5, rectangle.getY() + rectangle.getHeight() - 1);
                            accentLineLeft.setStroke(getDataTableView().getBackgroundColor());
                            accentLineLeft.setStrokeWidth(1.);
                            Line accentLineRight = new Line(rectangle.getLayoutBounds().getMaxX(), rectangle.getY(), rectangle.getLayoutBounds().getMaxX(), rectangle.getY() + rectangle.getHeight() - 1);
                            accentLineRight.setStroke(getDataTableView().getBackgroundColor());
                            accentLineRight.setStrokeWidth(1.);
                            overallHistogramGroup.getChildren().addAll(accentLineLeft, accentLineRight, rectangle);

                            if (queryHistogram != null) {
                                binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                        0, histogram.getMaxBinCount(),
                                        getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                                x = getBounds().getMinX() + ((width - binWidth) / 2.);
                                Rectangle queryRectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                                queryRectangle.strokeProperty().bind(queryHistogramStroke);
                                queryRectangle.fillProperty().bind(queryHistogramFill);
                                queryHistogramGroup.getChildren().add(queryRectangle);
                            }
                        }
                    }
                }

//                queryHistogramGroup.getChildren().clear();
//                queryHistogramRectangles.clear();
//
//                if (getDataTable().getActiveQuery().hasColumnSelections()) {
//                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());
//
//                    if (queryColumnSummaryStats != null) {
//                        if (getDataTable().getCalculateQueryStatistics()) {
//                            DoubleHistogram queryHistogram = ((DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(doubleColumn())).getHistogram();
//
//                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
//                                if (queryHistogram.getBinCount(i) > 0) {
//                                    double binLowerBound = queryHistogram.getBinLowerBound(i);
//                                    double binUpperBound = queryHistogram.getBinUpperBound(i);
//
//                                    if (binLowerBound >= doubleColumn().getMinimumFocusValue() &&
//                                            binUpperBound <= doubleColumn().getMaximumFocusValue()) {
//                                        double binLowerY = GraphicsUtil.mapValue(binLowerBound,
//                                                doubleColumn().getMinimumFocusValue(),
//                                                doubleColumn().getMaximumFocusValue(),
//                                                getMinFocusPosition(), getMaxFocusPosition());
//                                        double binUpperY = GraphicsUtil.mapValue(binUpperBound,
//                                                doubleColumn().getMinimumFocusValue(),
//                                                doubleColumn().getMaximumFocusValue(),
//                                                getMinFocusPosition(), getMaxFocusPosition());
//
//                                        double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
//                                                0, histogram.getMaxBinCount(),
//                                                getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
//                                        double x = getBounds().getMinX() + ((width - binWidth) / 2.);
//                                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
//                                        rectangle.strokeProperty().bind(queryHistogramStroke);
//                                        rectangle.fillProperty().bind(queryHistogramFill);
//                                        queryHistogramRectangles.add(rectangle);
//                                        queryHistogramGroup.getChildren().add(rectangle);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
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

                if (!(overallTypicalValue < doubleColumn().getMinimumFocusValue()) && !(overallTypicalValue > doubleColumn().getMaximumFocusValue())) {
                    double typicalValueY = GraphicsUtil.mapValue(overallTypicalValue,
                            doubleColumn().getMinimumFocusValue(), doubleColumn().getMaximumFocusValue(),
                            getMinFocusPosition(), getMaxFocusPosition());
                    overallTypicalLine.setStartX(doubleAxis().getBarLeftX());
                    overallTypicalLine.setEndX(doubleAxis().getBarRightX());
                    overallTypicalLine.setStartY(typicalValueY);
                    overallTypicalLine.setEndY(typicalValueY);
                    overallTypicalLine.setVisible(true);
                } else {
                    overallTypicalLine.setVisible(false);
                }

                if (!(overallDispersionBottomValue > doubleColumn().getMaximumFocusValue()) || !(overallDispersionTopValue < doubleColumn().getMinimumFocusValue())) {
                    double overallDispersionTop = GraphicsUtil.mapValue(overallDispersionTopValue, doubleColumn().getMinimumFocusValue(),
                            doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                    overallDispersionTop = overallDispersionTop < getMaxFocusPosition() ? getMaxFocusPosition() : overallDispersionTop;
                    double overallDispersionBottom = GraphicsUtil.mapValue(overallDispersionBottomValue, doubleColumn().getMinimumFocusValue(),
                            doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                    overallDispersionBottom = overallDispersionBottom > getMinFocusPosition() ? getMinFocusPosition() : overallDispersionBottom;
                    overallDispersionRectangle.setX(overallTypicalLine.getStartX());
                    overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
                    overallDispersionRectangle.setY(overallDispersionTop);
                    overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);
                    overallDispersionRectangle.setVisible(true);
                } else {
                    overallDispersionRectangle.setVisible(false);
                }

                querySummaryStatisticsGroup.setVisible(false);
                nonquerySummaryStatisticsGroup.setVisible(false);

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    double queryDispersionRectangleWidth = doubleAxis().getAxisBar().getWidth() / 4.;

                    if (queryColumnSummaryStats != null) {
                        querySummaryStatisticsGroup.setVisible(true);
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

                        if (!(queryTypicalValue < doubleColumn().getMinimumFocusValue()) && !(queryTypicalValue > doubleColumn().getMaximumFocusValue())) {
                            double typicalValueY = GraphicsUtil.mapValue(queryTypicalValue,
                                    doubleColumn().getMinimumFocusValue(), doubleColumn().getMaximumFocusValue(),
//                                    doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                                    getMinFocusPosition(), getMaxFocusPosition());
                            queryTypicalLine.setStartX(getCenterX() - (queryDispersionRectangleWidth + 1));
                            queryTypicalLine.setEndX(queryTypicalLine.getStartX() + queryDispersionRectangleWidth);
                            queryTypicalLine.setStartY(typicalValueY);
                            queryTypicalLine.setEndY(typicalValueY);
                            queryTypicalLine.setVisible(true);
                        } else {
                            queryTypicalLine.setVisible(false);
                        }

                        if (!(queryDispersionBottomValue > doubleColumn().getMaximumFocusValue()) || !(queryDispersionTopValue < doubleColumn().getMinimumFocusValue())) {
                            double queryDispersionTop = GraphicsUtil.mapValue(queryDispersionTopValue, doubleColumn().getMinimumFocusValue(),
                                    doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                            queryDispersionTop = queryDispersionTop < getMaxFocusPosition() ? getMaxFocusPosition() : queryDispersionTop;
                            double queryDispersionBottom = GraphicsUtil.mapValue(queryDispersionBottomValue, doubleColumn().getMinimumFocusValue(),
                                    doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                            queryDispersionBottom = queryDispersionBottom > getMinFocusPosition() ? getMinFocusPosition() : queryDispersionBottom;
                            queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                            queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                            queryDispersionRectangle.setY(queryDispersionTop);
                            queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);
                            queryDispersionRectangle.setVisible(true);
                        } else {
                            queryDispersionRectangle.setVisible(false);
                        }
                    }

                    // draw nonquery statistics shapes
                    DoubleColumnSummaryStats nonqueryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnNonquerySummaryStats(getColumn());

                    if (nonqueryColumnSummaryStats != null) {
                        nonquerySummaryStatisticsGroup.setVisible(true);
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

                        if (!(nonqueryTypicalValue < doubleColumn().getMinimumFocusValue()) && !(nonqueryTypicalValue > doubleColumn().getMaximumFocusValue())) {
                            double typicalValueY = GraphicsUtil.mapValue(nonqueryTypicalValue,
                                    doubleColumn().getMinimumFocusValue(), doubleColumn().getMaximumFocusValue(),
                                    getMinFocusPosition(), getMaxFocusPosition());
                            nonqueryTypicalLine.setStartX(getCenterX() + 1);
                            nonqueryTypicalLine.setEndX(nonqueryTypicalLine.getStartX() + queryDispersionRectangleWidth);
                            nonqueryTypicalLine.setStartY(typicalValueY);
                            nonqueryTypicalLine.setEndY(typicalValueY);
                            nonqueryTypicalLine.setVisible(true);
                        } else {
                            nonqueryTypicalLine.setVisible(false);
                        }

                        if (!(nonqueryDispersionBottomValue > doubleColumn().getMaximumFocusValue()) || !(nonqueryDispersionBottomValue < doubleColumn().getMinimumFocusValue())) {
                            double nonqueryDispersionTop = GraphicsUtil.mapValue(nonqueryDispersionTopValue, doubleColumn().getMinimumFocusValue(),
                                    doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                            nonqueryDispersionTop = nonqueryDispersionTop < getMaxFocusPosition() ? getMaxFocusPosition() : nonqueryDispersionTop;
                            double nonqueryDispersionBottom = GraphicsUtil.mapValue(nonqueryDispersionBottomValue,
                                    doubleColumn().getMinimumFocusValue(), doubleColumn().getMaximumFocusValue(), getMinFocusPosition(), getMaxFocusPosition());
                            nonqueryDispersionBottom = nonqueryDispersionBottom > getMinFocusPosition() ? getMinFocusPosition() : nonqueryDispersionBottom;

                            nonqueryDispersionRectangle.setX(nonqueryTypicalLine.getStartX());
                            nonqueryDispersionRectangle.setWidth(nonqueryTypicalLine.getEndX() - nonqueryTypicalLine.getStartX());
                            nonqueryDispersionRectangle.setY(nonqueryDispersionTop);
                            nonqueryDispersionRectangle.setHeight(nonqueryDispersionBottom - nonqueryDispersionTop);
                            nonqueryDispersionRectangle.setVisible(true);
                        } else {
                            nonqueryDispersionRectangle.setVisible(false);
                        }
                    }

                    if (getGraphicsGroup().getChildren().contains(axisSelectionGraphicsGroup)) {
                        int idx = getGraphicsGroup().getChildren().indexOf(overallSummaryStatisticsGroup);
                        getGraphicsGroup().getChildren().remove(axisSelectionGraphicsGroup);
                        getGraphicsGroup().getChildren().add(idx+1, axisSelectionGraphicsGroup);
                    }
                }
            }
        }
    }
}
