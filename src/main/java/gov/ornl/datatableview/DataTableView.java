package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.scatterplot.Scatterplot;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class DataTableView extends Region implements DataTableListener {
    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static Color DEFAULT_OVERALL_SUMMARY_FILL_COLOR = new Color(Color.LIGHTSTEELBLUE.getRed(), Color.LIGHTSTEELBLUE.getGreen(), Color.LIGHTSTEELBLUE.getBlue(), 0.6d);
    public final static Color DEFAULT_QUERY_SUMMARY_FILL_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.6d);
    public final static Color DEFAULT_OVERALL_SUMMARY_STROKE_COLOR = Color.DARKGRAY.darker();
    public final static Color DEFAULT_QUERY_SUMMARY_STROKE_COLOR = Color.BLACK;
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private final static double DEFAULT_LINE_OPACITY = 0.5;
    private final static Color DEFAULT_SELECTED_ITEMS_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_LINE_OPACITY);
    private final static Color DEFAULT_UNSELECTED_ITEMS_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_LINE_OPACITY);
    private final static int DEFAULT_CORRELATION_RECTANGLE_HEIGHT = 14;
    private final static int DEFAULT_CORRELATION_RECTANGLE_WIDTH = 24;
    private final static POLYLINE_DISPLAY_MODE DEFAULT_POLYLINE_DISPLAY_MODE = POLYLINE_DISPLAY_MODE.POLYLINES;

    private final Logger log = Logger.getLogger(DataTableView.class.getName());

    private TuplePolylineRenderer selectedTuplesTimer;
    private TuplePolylineRenderer unselectedTuplesTimer;

    private DoubleProperty drawingProgressProperty;

    private Pane pane;

    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;

    private DoubleProperty dataItemsOpacity;

    private ObjectProperty<Color> selectedItemsColor;
    private ObjectProperty<Color> unselectedItemsColor;
    private ObjectProperty<Color> overallSummaryFillColor;
    private ObjectProperty<Color> querySummaryFillColor;
    private ObjectProperty<Color> overallSummaryStrokeColor;
    private ObjectProperty<Color> querySummaryStrokeColor;
    private ObjectProperty<Color> backgroundColor;
    private ObjectProperty<Color> textColor;

    private BooleanProperty showSelectedItems;
    private BooleanProperty showUnselectedItems;

    private double axisSpacing = 40d;
//    private Group histogramGroup = new Group();

    private DataTable dataTable;
    private ArrayList<Axis> axisList = new ArrayList<>();
    private ArrayList<Scatterplot> scatterplotList = new ArrayList<>();
//    private ArrayList<CorrelationIndicatorRectangle> correlationRectangleList = new ArrayList<>();
    private Group correlationRectangleGroup = new Group();

    private Bounds correlationRegionBounds;
//    private Rectangle correlationRegionBoundsRectangle;

    private ArrayList<TuplePolyline> tuplePolylines;
    private HashSet<TuplePolyline> unselectedTuplePolylines = new HashSet<>();
    private HashSet<TuplePolyline> selectedTuplePolylines = new HashSet<>();
//    private ArrayList<PCPTuple> tupleList;
//    private HashSet<PCPTuple> unselectedTupleSet = new HashSet<>();
//    private HashSet<PCPTuple> selectedTupleSet = new HashSet<>();

    private ObjectProperty<POLYLINE_DISPLAY_MODE> polylineDisplayMode;
    private ObjectProperty<STATISTICS_DISPLAY_MODE> summaryStatisticsDisplayMode = new SimpleObjectProperty<>(STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT);

    private BooleanProperty showSummaryStatistics = new SimpleBooleanProperty(true);
    private BooleanProperty showHistograms = new SimpleBooleanProperty(false);
    private BooleanProperty showScatterplots = new SimpleBooleanProperty(true);
    private BooleanProperty showPolylines = new SimpleBooleanProperty(true);
    private BooleanProperty showCorrelations = new SimpleBooleanProperty(true);

    private BooleanProperty fitToWidth = new SimpleBooleanProperty(true);
    private DoubleProperty nameTextRotation;
//    private ArrayList<PCPBinSet> PCPBinSetList;

    private BoundingBox plotRegionBounds;
    private BoundingBox pcpRegionBounds;
    private BoundingBox scatterplotRegionBounds;
    private double plotRegionPadding = 4.;

//    private Rectangle plotRegionRectangle;
//    private Rectangle pcpRegionRectangle;
//    private Rectangle scatterplotRegionRectangle;


    private ObjectProperty<Axis> highlightedAxis = new SimpleObjectProperty<>(null);

//    private Group summaryShapeGroup;

    public DataTableView() {
        initialize();
        registerListeners();
    }

    public boolean isShowingCorrelations() { return showCorrelations.get(); }

    public void setShowCorrelations(boolean show) {
        if (isShowingCorrelations() != show) {
            showCorrelations.set(show);
        }
    }

    public BooleanProperty showCorrelationsProperty() { return showCorrelations; }

    public WritableImage getSnapshot(int scaleFactor) {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setTransform(new Scale(scaleFactor, scaleFactor));
        return pane.snapshot(snapshotParameters, null);
    }

    public boolean isShowingScatterplots() { return showScatterplots.get(); }

    public void setShowScatterplots(boolean show) {
        if (isShowingScatterplots() != show) {
            showScatterplots.set(show);
        }
    }

    public BooleanProperty showScatterplotsProperty() { return showScatterplots; }

    public boolean isShowingHistograms() { return showHistograms.get(); }

    public void setShowHistograms(boolean showHistograms) {
        if (isShowingHistograms() != showHistograms) {
            this.showHistograms.set(showHistograms);
        }
    }

    public BooleanProperty showHistogramsProperty() { return showHistograms; }

    public STATISTICS_DISPLAY_MODE getSummaryStatisticsDisplayMode() { return summaryStatisticsDisplayMode.get(); }

    public void setSummaryStatisticsDisplayMode(STATISTICS_DISPLAY_MODE newMode) {
        if (newMode != getSummaryStatisticsDisplayMode()) {
            summaryStatisticsDisplayMode.set(newMode);
        }
    }

    public ObjectProperty<STATISTICS_DISPLAY_MODE> summaryStatisticsDisplayModeProperty() {
        return summaryStatisticsDisplayMode;
    }

    public boolean isShowingSummaryStatistics() {
        return showSummaryStatistics.get();
    }

    public void setShowSummaryStatistics(boolean show) {
        if (isShowingSummaryStatistics() != show) {
            showSummaryStatistics.set(show);
        }
    }

    public BooleanProperty showSummaryStatisticsProperty() {
        return showSummaryStatistics;
    }

    public boolean isShowingPolylines() {
        return showPolylines.get();
    }

    public void setShowPolylines(boolean show) {
        if (isShowingPolylines() != show) {
            showPolylines.set(show);
        }
    }

    public BooleanProperty showPolylinesProperty() { return showPolylines; }

    public boolean getFitToWidth() { return fitToWidth.get(); }

    public void setFitToWidth(boolean enabled) {
        if (getFitToWidth() != enabled) {
            fitToWidth.set(enabled);
        }
    }

    public BooleanProperty fitToWidthProperty() { return fitToWidth; }

    private void registerListeners() {
        widthProperty().addListener(o -> resizeView());

        heightProperty().addListener(o -> resizeView());

        fitToWidth.addListener(observable -> resizeView());

        backgroundColor.addListener((observable, oldValue, newValue) -> {
            pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        });

        textColor.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                for (Axis axis : axisList) {
                    axis.setTextColor(newValue);
                }
            }
        });

        dataItemsOpacity.addListener((observable, oldValue, newValue) -> {
            setSelectedItemsColor(new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                    getSelectedItemsColor().getBlue(), getOpacity()));
            setUnselectedItemsColor(new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                    getUnselectedItemsColor().getBlue(), getOpacity()));
            if (!scatterplotList.isEmpty()) {
                for (Scatterplot scatterplot : scatterplotList) {
                    scatterplot.setPointStrokeOpacity(getDataItemsOpacity());
                }
            }
            redrawView();
        });

        selectedItemsColor.addListener((observable, oldValue, newValue) -> {
            if (!scatterplotList.isEmpty()) {
                for (Scatterplot scatterplot : scatterplotList) {
                    scatterplot.setSelectedPointStrokeColor(getSelectedItemsColor());
                }
            }
            redrawView();
        });

        unselectedItemsColor.addListener((observable, oldValue, newValue) -> {
            if (!scatterplotList.isEmpty()) {
                for (Scatterplot scatterplot : scatterplotList) {
                    scatterplot.setUnselectedPointStrokeColor(getUnselectedItemsColor());
                }
            }
            redrawView();
        });

        showSelectedItems.addListener(((observable, oldValue, newValue) -> {
            if (!scatterplotList.isEmpty()) {
                for (Scatterplot scatterplot : scatterplotList) {
                    scatterplot.setShowSelectedPoints(isShowingSelectedItems());
                }
            }
            redrawView();
        }));

        showUnselectedItems.addListener(((observable, oldValue, newValue) -> {
            if (!scatterplotList.isEmpty()) {
                for (Scatterplot scatterplot : scatterplotList) {
                    scatterplot.setShowUnselectedPoints(isShowingUnselectedItems());
                }
            }
            redrawView();
        }));

        showScatterplots.addListener(observable -> {
            if (isShowingScatterplots()) {
                reinitializeScatterplots();
            } else {
                if (!scatterplotList.isEmpty()) {
                    for (Scatterplot scatterplot : scatterplotList) {
                        pane.getChildren().remove(scatterplot.getGraphicsGroup());
                    }
                    scatterplotList.clear();
                }
            }

            resizeView();
        });

//        showPolylines.addListener(observable -> {
//            if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
//                selectedTuplesTimer.stop();
//            }
//            if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
//                unselectedTuplesTimer.stop();
//            }
//
//            if (isShowingPolylines()) {
//                fillTupleSets();
//            } else {
//                selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(),
//                        selectedCanvas.getHeight());
//                unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, unselectedCanvas.getWidth(),
//                        unselectedCanvas.getHeight());
//            }
//
//            redrawView();
//        });

        showSummaryStatistics.addListener(observable -> {
            resizeView();
        });

        showCorrelations.addListener(observable -> {
            if (isShowingCorrelations()) {
                pane.getChildren().add(correlationRectangleGroup);
                reinitializeCorrelationRectangles();
                setCorrelationRectangleValues();
            } else {
                pane.getChildren().remove(correlationRectangleGroup);
            }

            resizeView();
        });

        highlightedAxis.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                if (oldValue != null) {
                    oldValue.setHighlighted(false);
                }
                if (newValue != null) {
                    newValue.setHighlighted(true);
                }
                resizeView();
            }
        });

//        polylineDisplayMode.addListener(((observable, oldValue, newValue) -> {
//            if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
//                selectedTuplesTimer.stop();
//            }
//            if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
//                unselectedTuplesTimer.stop();
//            }
//
//            if (newValue == POLYLINE_DISPLAY_MODE.POLYLINES) {
//                fillTupleSets();
//            }
//
//            resizeView();
//        }));
    }

    private void fillTupleSets() {
        unselectedTuplePolylines.clear();
        selectedTuplePolylines.clear();
        if ((tuplePolylines != null) && (!tuplePolylines.isEmpty())) {
            if (dataTable.getActiveQuery().hasColumnSelections()) {
                for (TuplePolyline pcpTuple : tuplePolylines) {
                    if (pcpTuple.getTuple().getQueryFlag()) {
                        selectedTuplePolylines.add(pcpTuple);
                    } else {
                        unselectedTuplePolylines.add(pcpTuple);
                    }
                }
            } else {
                selectedTuplePolylines.addAll(tuplePolylines);
            }
        }
    }

    private void initialize() {
        nameTextRotation = new SimpleDoubleProperty(0.0);
        selectedItemsColor = new SimpleObjectProperty<>(DEFAULT_SELECTED_ITEMS_COLOR);
        unselectedItemsColor = new SimpleObjectProperty<>(DEFAULT_UNSELECTED_ITEMS_COLOR);
        dataItemsOpacity = new SimpleDoubleProperty(DEFAULT_LINE_OPACITY);

        overallSummaryFillColor = new SimpleObjectProperty<>(DEFAULT_OVERALL_SUMMARY_FILL_COLOR);
        overallSummaryStrokeColor = new SimpleObjectProperty<>(DEFAULT_OVERALL_SUMMARY_STROKE_COLOR);
        querySummaryFillColor = new SimpleObjectProperty<>(DEFAULT_QUERY_SUMMARY_FILL_COLOR);
        querySummaryStrokeColor = new SimpleObjectProperty<>(DEFAULT_QUERY_SUMMARY_STROKE_COLOR);

        showSelectedItems = new SimpleBooleanProperty(true);
        showUnselectedItems = new SimpleBooleanProperty(true);
        polylineDisplayMode = new SimpleObjectProperty<>(DEFAULT_POLYLINE_DISPLAY_MODE);

        drawingProgressProperty = new SimpleDoubleProperty(0d);

        selectedCanvas = new Canvas(getWidth(), getHeight());
        unselectedCanvas = new Canvas(getWidth(), getHeight());

        textColor = new SimpleObjectProperty<>(DEFAULT_LABEL_COLOR);
        backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);

//        summaryShapeGroup = new Group();

//        plotRegionRectangle = new Rectangle();
//        plotRegionRectangle.setStroke(Color.DARKBLUE);
//        plotRegionRectangle.setFill(Color.TRANSPARENT);
//        plotRegionRectangle.setMouseTransparent(true);
//        plotRegionRectangle.setStrokeWidth(1.5);
//
//        pcpRegionRectangle = new Rectangle();
//        pcpRegionRectangle.setStroke(Color.ORANGE);
//        pcpRegionRectangle.setStrokeWidth(1.5);
//        pcpRegionRectangle.setFill(Color.TRANSPARENT);
//        pcpRegionRectangle.setMouseTransparent(true);
//
//        scatterplotRegionRectangle = new Rectangle();
//        scatterplotRegionRectangle.setStroke(Color.RED);
//        scatterplotRegionRectangle.setStrokeWidth(1.5);
//        scatterplotRegionRectangle.setFill(Color.TRANSPARENT);
//        scatterplotRegionRectangle.setMouseTransparent(true);

//        correlationRegionBoundsRectangle = new Rectangle();
//        correlationRegionBoundsRectangle.setStroke(Color.RED);
//        correlationRegionBoundsRectangle.setStrokeWidth(1.5);
//        correlationRegionBoundsRectangle.setFill(Color.TRANSPARENT);
//        correlationRegionBoundsRectangle.setMouseTransparent(true);

        pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        pane.getChildren().addAll(unselectedCanvas, selectedCanvas, correlationRectangleGroup);

        getChildren().add(pane);
    }

    public int getAxisSpacing () {return (int)axisSpacing;}

    public void setAxisSpacing(double axisSpacing) {
        this.axisSpacing = axisSpacing;
        resizeView();
    }

    public final Axis getAxis(int index) {
        return axisList.get(index);
    }

    public final int getAxisCount() { return axisList.size(); }

    private void drawTuplePolylines() {
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        selectedCanvas.getGraphicsContext2D().setLineDashes(null);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        unselectedCanvas.getGraphicsContext2D().setLineDashes(null);

        if ((isShowingUnselectedItems()) && (unselectedTuplePolylines != null) && (!unselectedTuplePolylines.isEmpty())) {
            if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
                unselectedTuplesTimer.stop();
            }

            Color lineColor = new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                    getUnselectedItemsColor().getBlue(), getDataItemsOpacity());
            unselectedTuplesTimer = new TuplePolylineRenderer(unselectedCanvas, unselectedTuplePolylines,
                    axisList, lineColor, 100);
            unselectedTuplesTimer.start();
        }

        if ((isShowingSelectedItems()) && (selectedTuplePolylines != null) && (!selectedTuplePolylines.isEmpty())) {
            if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
                selectedTuplesTimer.stop();
            }

            Color lineColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                    getSelectedItemsColor().getBlue(), getDataItemsOpacity());
            selectedTuplesTimer = new TuplePolylineRenderer(selectedCanvas, selectedTuplePolylines,
                    axisList, lineColor, 100);
            selectedTuplesTimer.start();
        }
    }

    private void redrawView() {
        if (selectedTuplesTimer != null && selectedTuplesTimer.isRunning()) {
            selectedTuplesTimer.stop();
        }

        if (unselectedTuplesTimer != null && unselectedTuplesTimer.isRunning()) {
            unselectedTuplesTimer.stop();
        }

        if (isShowingPolylines()) {
            if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.POLYLINES) {
                drawTuplePolylines();
            } else if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.BINNED_POLYLINES) {
                drawPCPBins();
            }
        }
    }

    private void resizeView() {
        if (dataTable != null && !dataTable.isEmpty()) {
            plotRegionBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(),
                    getWidth() - (getInsets().getLeft() + getInsets().getRight()),
                    getHeight() - (getInsets().getTop() + getInsets().getBottom()));
//            plotRegionRectangle.setX(plotRegionBounds.getMinX());
//            plotRegionRectangle.setY(plotRegionBounds.getMinY());
//            plotRegionRectangle.setWidth(plotRegionBounds.getWidth());
//            plotRegionRectangle.setHeight(plotRegionBounds.getHeight());

            double plotWidth;
            double width;

            if (getFitToWidth()) {
                width = getWidth();
                plotWidth = width - (getInsets().getLeft() + getInsets().getRight());
//                axisSpacing = plotWidth / dataTable.getColumnCount();
                axisSpacing = plotWidth / axisList.size();
            } else {
//                plotWidth = axisSpacing * dataTable.getColumnCount();
                plotWidth = axisSpacing * axisList.size();
                width = (getInsets().getLeft() + getInsets().getRight()) + plotWidth;
            }

            double plotHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

            if (plotWidth > 0 && plotHeight > 0) {
                double pcpHeight = plotHeight * .7;
                double scatterplotSize = 0.;
                double correlationIndicatorHeight = 0.;

                if (isShowingScatterplots()) {
                    scatterplotSize = plotHeight - pcpHeight;

                    if (scatterplotSize > (axisSpacing * .8)) {
                        scatterplotSize = axisSpacing * .8;
                    }
                }

                if (isShowingCorrelations()) {
                    correlationIndicatorHeight = DEFAULT_CORRELATION_RECTANGLE_HEIGHT;
                }

                pane.setPrefSize(width, getHeight());
                pane.setMinWidth(width);

                selectedCanvas.setWidth(width);
                selectedCanvas.setHeight(getHeight());
                unselectedCanvas.setWidth(width);
                unselectedCanvas.setHeight(getHeight());

                if (axisList != null) {
                    pcpRegionBounds = new BoundingBox(plotRegionBounds.getMinX(), plotRegionBounds.getMinY(),
                            plotRegionBounds.getWidth(), plotHeight - (correlationIndicatorHeight + scatterplotSize + plotRegionPadding));
//                    pcpRegionRectangle.setX(pcpRegionBounds.getMinX());
//                    pcpRegionRectangle.setY(pcpRegionBounds.getMinY());
//                    pcpRegionRectangle.setWidth(pcpRegionBounds.getWidth());
//                    pcpRegionRectangle.setHeight(pcpRegionBounds.getHeight());

                    correlationRegionBounds = new BoundingBox(plotRegionBounds.getMinX(), pcpRegionBounds.getMaxY() + plotRegionPadding,
                            plotRegionBounds.getWidth(), correlationIndicatorHeight);
//                    correlationRegionBoundsRectangle.setX(correlationRegionBounds.getMinX());
//                    correlationRegionBoundsRectangle.setY(correlationRegionBounds.getMinY());
//                    correlationRegionBoundsRectangle.setWidth(correlationRegionBounds.getWidth());
//                    correlationRegionBoundsRectangle.setHeight(correlationRegionBounds.getHeight());

                    scatterplotRegionBounds = new BoundingBox(plotRegionBounds.getMinX(), correlationRegionBounds.getMaxY() + plotRegionPadding,
                            plotRegionBounds.getWidth(), scatterplotSize);
//                    scatterplotRegionRectangle.setX(scatterplotRegionBounds.getMinX());
//                    scatterplotRegionRectangle.setY(scatterplotRegionBounds.getMinY());
//                    scatterplotRegionRectangle.setWidth(scatterplotRegionBounds.getWidth());
//                    scatterplotRegionRectangle.setHeight(scatterplotRegionBounds.getHeight());

                    for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                        Axis axis = axisList.get(iaxis);
                        double axisLeft = plotRegionBounds.getMinX() + (iaxis * axisSpacing);
                        axis.resize(axisLeft, pcpRegionBounds.getMinY(), axisSpacing, pcpRegionBounds.getHeight());
                    }

                    if (isShowingCorrelations()) {
//                        for (int i = 0; i < correlationRectangleList.size(); i++) {
//                            CorrelationIndicatorRectangle correlationIndicatorRectangle = correlationRectangleList.get(i);
//                            correlationIndicatorRectangle.setY(correlationRegionBounds.getMinY());
//                            if (dataTable.getHighlightedColumn() != null) {
//                                correlationIndicatorRectangle.setX(correlationIndicatorRectangle.getAxis1().getCenterX() - (correlationIndicatorRectangle.getWidth() / 2.));
//                            } else {
//                                correlationIndicatorRectangle.setX(((correlationIndicatorRectangle.getAxis1().getCenterX() + correlationIndicatorRectangle.getAxis2().getCenterX()) / 2.) - (correlationIndicatorRectangle.getWidth() / 2.));
//                            }
//                        }
                    }

                    if (isShowingScatterplots()) {
//                        for (int i = 0; i < scatterplotList.size(); i++) {
//                            Scatterplot scatterplot = scatterplotList.get(i);
//                            PCPUnivariateAxis yAxis = null;
//                            PCPUnivariateAxis xAxis = null;
//                            for (PCPUnivariateAxis axis : axisList) {
//                                if (axis.getColumn() == scatterplot.getYColumn()) {
//                                    yAxis = axis;
//                                } else if (axis.getColumn() == scatterplot.getXColumn()) {
//                                    xAxis = axis;
//                                }
//
//                                if (yAxis != null && xAxis != null) {
//                                    break;
//                                }
//                            }
//
//                            if (dataTable.getHighlightedColumn() == null) {
//                                double centerX = (yAxis.getCenterX() + xAxis.getCenterX()) / 2.;
//                                double left = centerX - (scatterplotSize / 2.) - (scatterplot.getAxisSize() / 2.);
//                                scatterplot.resize(left, scatterplotRegionBounds.getMinY(), scatterplotSize, scatterplotSize);
//                            } else {
//                                double centerX = yAxis.getCenterX();
//                                double left = centerX - (scatterplotSize / 2.) - (scatterplot.getAxisSize() / 2.);
//                                scatterplot.resize(left, scatterplotRegionBounds.getMinY(), scatterplotSize, scatterplotSize);
//                            }
//                        }
                    }

                    // add tuples polylines from data model
                    if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.POLYLINES) {
                        if (tuplePolylines != null) {
                            for (TuplePolyline pcpTuple : tuplePolylines) {
                                pcpTuple.layout(axisList);
                            }
                        }
                    } else if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.BINNED_POLYLINES) {
//                        // resize PCPBins
//                        for (PCPBinSet PCPBinSet : PCPBinSetList) {
//                            PCPBinSet.layoutBins();
//                        }
                    }
                }
            }

            redrawView();
        }
    }

    private void removeAllAxisSelectionGraphics() {
//        for (PCPUnivariateAxis pcpAxis : axisList) {
//            if (!pcpAxis.getAxisSelectionList().isEmpty()) {
//                for (UnivariateAxisSelection pcpAxisSelection : pcpAxis.getAxisSelectionList()) {
//                    pane.getChildren().remove(pcpAxisSelection.getGraphicsGroup());
//                }
//                pcpAxis.getAxisSelectionList().clear();
//            }
//        }
    }

    public void clearQuery() {
        dataTable.removeColumnSelectionsFromActiveQuery();
        removeAllAxisSelectionGraphics();
        handleQueryChange();
    }

//    public double getPCPVerticalBarHeight() {
//        if (axisList != null && !axisList.isEmpty()) {
//            return axisList.get(0).getAxisBar().getHeight();
//        }
//        return Double.NaN;
//    }

    private void drawPCPBins() {
        /*
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);

        if ((PCPBinSetList != null) && (!PCPBinSetList.isEmpty())) {
            if (dataTable.getActiveQuery().hasColumnSelections()) {
                if (isShowingUnselectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            if (bin.queryCount == 0) {
                                Color binColor = new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                                        getUnselectedItemsColor().getBlue(), bin.fillColor.getOpacity());
                                unselectedCanvas.getGraphicsContext2D().setFill(binColor);

                                double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                                double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};

                                unselectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                            }
                        }
                    }
                }

                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            if (bin.queryCount > 0) {
                                Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                        getSelectedItemsColor().getBlue(), bin.queryFillColor.getOpacity());

                                selectedCanvas.getGraphicsContext2D().setFill(binColor);

                                double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                                double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};

                                selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                            }
                        }
                    }
                }
            } else {
                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                    getSelectedItemsColor().getBlue(), bin.fillColor.getOpacity());

                            selectedCanvas.getGraphicsContext2D().setFill(binColor);

                            double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                            double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};

                            selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);
                        }
                    }
                }
            }
        }
        */
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        dataTable.addDataModelListener(this);
        clearView();
        initView();
    }

    public double getNameTextRotation() {
        return nameTextRotation.get();
    }

    public void setNameTextRotation(double rotation) {
        nameTextRotation.set(rotation);
    }

    public DoubleProperty nameTextRotationProperty() {
        return nameTextRotation;
    }

    public Color getBackgroundColor() {
        return backgroundColor.get();
    }

    public void setBackgroundColor(Color newBackgroundColor) {
        this.backgroundColor.set(newBackgroundColor);
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
    }

    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public Color getTextColor() {
        return textColor.get();
    }

    public void setTextColor(Color textColor) {
        this.textColor.set(textColor);
    }

    public ObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    public ObjectProperty<POLYLINE_DISPLAY_MODE> polylineDisplayModeProperty() { return polylineDisplayMode; }

    public final POLYLINE_DISPLAY_MODE getPolylineDisplayMode() { return polylineDisplayMode.get(); }

    public final void setPolylineDisplayMode(POLYLINE_DISPLAY_MODE displayMode) { polylineDisplayMode.set(displayMode); }

    public final boolean isShowingSelectedItems() { return showSelectedItems.get(); }

    public final void setShowSelectedItems(boolean enabled) { showSelectedItems.set(enabled); }

    public BooleanProperty showSelectedItemsProperty() { return showSelectedItems; }

    public final boolean isShowingUnselectedItems() { return showUnselectedItems.get(); }

    public final void setShowUnselectedItems(boolean enabled) { showUnselectedItems.set(enabled); }

    public BooleanProperty showUnselectedItemsProperty() { return showUnselectedItems; }

    public final double getDataItemsOpacity() { return dataItemsOpacity.get(); }

    public final void setDataItemsOpacity(double opacity) {
        dataItemsOpacity.set(opacity);
    }

    public DoubleProperty dataItemsOpacityProperty() { return dataItemsOpacity; }

    public final Color getSelectedItemsColor() { return selectedItemsColor.get(); }

    public final void setSelectedItemsColor(Color color) {
        selectedItemsColor.set(color);
    }

    public ObjectProperty<Color> selectedItemsColorProperty() { return selectedItemsColor; }

    public final Color getUnselectedItemsColor() { return unselectedItemsColor.get(); }

    public final void setUnselectedItemsColor(Color color) {
        unselectedItemsColor.set(color);
    }

    public ObjectProperty<Color> unselectedItemsColorProperty() { return unselectedItemsColor; }

    public final Color getOverallSummaryFillColor () { return overallSummaryFillColor.get(); }

    public final void setOverallSummaryFillColor (Color color) { overallSummaryFillColor.set(color); }

    public ObjectProperty<Color> overallSummaryFillColorProperty() { return overallSummaryFillColor; }

    public final Color getQuerySummaryFillColor () { return querySummaryFillColor.get(); }

    public final void setQuerySummaryFillColor (Color color) { querySummaryFillColor.set(color); }

    public ObjectProperty<Color> overallQueryFillColorProperty() { return querySummaryFillColor; }

    public final Color getQuerySummaryStrokeColor () { return querySummaryStrokeColor.get(); }

    public final void setQuerySummaryStrokeColor (Color color) { querySummaryStrokeColor.set(color); }

    public ObjectProperty<Color> querySummaryStrokeColor () { return querySummaryStrokeColor; }

    public final Color getOverallSummaryStrokeColor () { return overallSummaryStrokeColor.get(); }

    public final void setOverallSummaryStrokeColor (Color color) { overallSummaryStrokeColor.set(color); }

    public ObjectProperty<Color> overallSummaryStrokeColor () { return overallSummaryStrokeColor; }
//
//    public void setHighlightedAxis(Axis axis) { highlightedAxis.set(axis); }
//
//    public Axis getHighlightedAxis() { return highlightedAxis.get(); }
//
//    public ObjectProperty<Axis> highlightedAxisProperty() { return highlightedAxis; }

    private void initView() {
        if (axisList.isEmpty()) {
            for (int i = 0; i < dataTable.getColumnCount(); i++) {
                Axis axis = null;
                if (dataTable.getColumn(i) instanceof TemporalColumn) {
                    axis = new TemporalAxis(this, dataTable.getColumn(i));
                } else if (dataTable.getColumn(i) instanceof CategoricalColumn) {
                    axis = new CategoricalAxis(this, dataTable.getColumn(i));
                } else if (dataTable.getColumn(i) instanceof DoubleColumn){
                    axis = new DoubleAxis(this, (DoubleColumn)dataTable.getColumn(i));
                } else if (dataTable.getColumn(i) instanceof BivariateColumn) {
                    axis = new BivariateAxis(this, (BivariateColumn)dataTable.getColumn(i));
                }

                if (axis != null) {
//                    pcpAxis.titleTextRotationProperty().bind(nameTextRotationProperty());
                    pane.getChildren().add(axis.getGraphicsGroup());
                    axisList.add(axis);
                }
            }
        } else {
            ArrayList<Axis> newAxisList = new ArrayList<>();
            for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
                Column column = dataTable.getColumn(icolumn);
                for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                    Axis pcpAxis = axisList.get(iaxis);
                    if (pcpAxis.getColumn() == column) {
                        newAxisList.add(pcpAxis);
                        break;
                    }
                }
            }

            axisList = newAxisList;
        }

//        reinitializeScatterplots();
//
//        reinitializeCorrelationRectangles();

        // add tuples polylines from data model
        tuplePolylines = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataTable.getTupleCount(); iTuple++) {
            Tuple tuple = dataTable.getTuple(iTuple);
            TuplePolyline tuplePolyline = new TuplePolyline(tuple);
            tuplePolylines.add(tuplePolyline);
//            PCPTuple pcpTuple = new PCPTuple(tuple);
//            tupleList.add(pcpTuple);
        }

        fillTupleSets();

        // create PCPBinSets for axis configuration
//        PCPBinSetList = new ArrayList<>();
//        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
//            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataTable);
//            PCPBinSetList.add(binSet);
//        }

        resizeView();
    }

    private void reinitializeScatterplots() {
        if (!scatterplotList.isEmpty()) {
            for (Scatterplot scatterplot : scatterplotList) {
                pane.getChildren().remove(scatterplot.getGraphicsGroup());
            }
            scatterplotList.clear();
        }

//        if (isShowingScatterplots()) {
//            PCPUnivariateAxis highlightedAxis = getHighlightedAxis();
//            if (highlightedAxis != null) {
//                for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
//                    PCPUnivariateAxis currentAxis = axisList.get(iaxis);
//                    if (highlightedAxis != currentAxis) {
//                        Scatterplot scatterplot = new Scatterplot(highlightedAxis.getColumn(), currentAxis.getColumn());
//                        scatterplotList.add(scatterplot);
//                        pane.getChildren().add(scatterplot.getGraphicsGroup());
//                    }
//                }
//            } else {
//                for (int iaxis = 1; iaxis < axisList.size(); iaxis++) {
//                    PCPUnivariateAxis xAxis = axisList.get(iaxis);
//                    PCPUnivariateAxis yAxis = axisList.get(iaxis - 1);
//                    Scatterplot scatterplot = new Scatterplot(xAxis.getColumn(), yAxis.getColumn());
//                    scatterplotList.add(scatterplot);
//                    pane.getChildren().add(scatterplot.getGraphicsGroup());
//                }
//            }
//        }
    }

    private void reinitializeCorrelationRectangles() {
//        correlationRectangleGroup.getChildren().clear();
//        correlationRectangleList.clear();
//
//        if (isShowingCorrelations()) {
//            PCPUnivariateAxis highlightedAxis = getHighlightedAxis();
//            if (highlightedAxis != null) {
//                if (highlightedAxis instanceof PCPDoubleAxis) {
//                    for (int i = 0; i < axisList.size(); i++) {
//                        PCPUnivariateAxis axis = axisList.get(i);
//
//                        if (axis instanceof PCPDoubleAxis && axis != highlightedAxis) {
//                            CorrelationIndicatorRectangle correlationRectangle = new CorrelationIndicatorRectangle(axis, highlightedAxis);
//                            correlationRectangle.setStroke(Color.DARKGRAY);
//                            correlationRectangle.setWidth(DEFAULT_CORRELATION_RECTANGLE_WIDTH);
//                            correlationRectangle.setHeight(DEFAULT_CORRELATION_RECTANGLE_HEIGHT);
//                            correlationRectangleList.add(correlationRectangle);
//                            correlationRectangleGroup.getChildren().add(correlationRectangle);
//                        }
//                    }
//                }
//            } else {
//                for (int i = 1; i < axisList.size(); i++) {
//                    if (axisList.get(i) instanceof PCPDoubleAxis && axisList.get(i-1) instanceof PCPDoubleAxis) {
//                        CorrelationIndicatorRectangle correlationRectangle = new CorrelationIndicatorRectangle(axisList.get(i - 1),
//                                axisList.get(i));
//                        correlationRectangle.setStroke(Color.DARKGRAY);
//                        correlationRectangle.setWidth(DEFAULT_CORRELATION_RECTANGLE_WIDTH);
//                        correlationRectangle.setHeight(DEFAULT_CORRELATION_RECTANGLE_HEIGHT);
//                        correlationRectangleList.add(correlationRectangle);
//                        correlationRectangleGroup.getChildren().add(correlationRectangle);
//                    }
//                }
//            }
//
//            setCorrelationRectangleValues();
//        }
    }

    private void setCorrelationRectangleValues() {
//        if (isShowingCorrelations()) {
//            for (CorrelationIndicatorRectangle corrRect : correlationRectangleList) {
//                int axis2Index = getAxisIndex(corrRect.getAxis2());
//
//                double corr;
//                if (dataTable.getActiveQuery().hasColumnSelections() && dataTable.getCalculateQueryStatistics()) {
//                    corr = ((DoubleColumnSummaryStats)dataTable.getActiveQuery().getColumnQuerySummaryStats(corrRect.getAxis1().getColumn())).getCorrelationCoefficientList().get(axis2Index);
//                } else {
//                    corr = ((DoubleColumnSummaryStats)corrRect.getAxis1().getColumn().getStatistics()).getCorrelationCoefficientList().get(axis2Index);
//                }
//
//                corrRect.setCorrelation(corr);
//            }
//        }
    }

    private Axis getHighlightedAxis() {
        if (dataTable.getHighlightedColumn() != null) {
            for (Axis axis : axisList) {
                if (axis.getColumn() == dataTable.getHighlightedColumn()) {
                    return axis;
                }
            }
        }

        return null;
    }

    protected Pane getPane() { return pane; }

    public DataTable getDataTable() { return dataTable; }

    private int getAxisIndex(Axis axis) {
        return axisList.indexOf(axis);
//        for (int i = 0; i < axisList.size(); i++) {
//            if (axis == axisList.get(i)) {
//                return i;
//            }
//        }
//
//        return -1;
    }

    private int getAxisIndex(Column column) {
        for (int i = 0; i < axisList.size(); i++) {
            if (axisList.get(i).getColumn() == column) {
                return i;
            }
        }

        return -1;
    }

//    public void setAxisPosition(Axis axis, int position) {
//        if (axisList.contains(axis)) {
//            int newPosition = position < 0 ? 0 : position >= axisList.size() ? axisList.size()-1 : position;
//
//            int currentPosition = axisList.indexOf(axis);
//
//            if (currentPosition != newPosition) {
//                axisList.remove(axis);
//                axisList.add(newPosition, axis);
//                resizeView();
//            }
//        }
//    }

    private void addUnivariateAxis(Column column, int position) {
        Axis pcpAxis = null;
        if (column instanceof DoubleColumn) {
            pcpAxis = new DoubleAxis(this, (DoubleColumn)column);
        } else if (column instanceof TemporalColumn) {
//            pcpAxis = new PCPTemporalAxis(this, column);
        } else if (column instanceof CategoricalColumn) {
//            pcpAxis = new PCPCategoricalAxis(this, column);
        }

        if (position >= axisList.size()) {
            axisList.add(axisList.size(), pcpAxis);
        } else if (position <= 0) {
            axisList.add(0, pcpAxis);
        } else {
            axisList.add(position, pcpAxis);
        }

        axisList.add(pcpAxis);
//        pcpAxis.titleTextRotationProperty().bind(nameTextRotationProperty());
        pane.getChildren().add(pcpAxis.getGraphicsGroup());
    }

    private void addBivariateAxis(BivariateColumn bivariateColumn, int position) {
        BivariateAxis bivariateAxis = new BivariateAxis(this, bivariateColumn);

        if (position >= axisList.size()) {
            axisList.add(axisList.size(), bivariateAxis);
        } else if (position < 0) {
            axisList.add(0, bivariateAxis);
        } else {
            axisList.add(position, bivariateAxis);
        }

        pane.getChildren().add(bivariateAxis.getGraphicsGroup());

        resizeView();
    }

    private void handleQueryChange() {
        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            Axis pcpAxis = axisList.get(iaxis);

            double axisLeft = plotRegionBounds.getMinX() + (iaxis * axisSpacing);
            pcpAxis.resize(axisLeft, pcpRegionBounds.getMinY(), axisSpacing, pcpRegionBounds.getHeight());
        }

        if (isShowingScatterplots()) {
            for (Scatterplot scatterplot : scatterplotList) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }
        }

        if (isShowingCorrelations()) {
            setCorrelationRectangleValues();
        }

        if (isShowingPolylines()) {
            if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.POLYLINES) {
                fillTupleSets();
                redrawView();
//            } else if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.BINNED_POLYLINES) {
//                for (PCPBinSet PCPBinSet : PCPBinSetList) {
//                    PCPBinSet.layoutBins();
//                }
//                redrawView();
            }
        }
    }

    private void clearView() {
        removeAllAxisSelectionGraphics();
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());

        if (axisList != null && !axisList.isEmpty()) {
            for (Axis pcpAxis : axisList) {
                pane.getChildren().removeAll(pcpAxis.getGraphicsGroup());
            }
            axisList.clear();
        }

        for (Scatterplot scatterplot : scatterplotList) {
            pane.getChildren().remove(scatterplot.getGraphicsGroup());
        }
        scatterplotList.clear();
    }

    @Override
    public void dataModelReset(DataTable dataModel) {
        clearView();

        // Logic to prevent PCPView from drawing full details when data is large
        if (dataModel.getTupleCount() > 500000) {
            setShowPolylines(false);
            setShowSummaryStatistics(true);
        }

        initView();
    }

    @Override
    public void dataModelStatisticsChanged(DataTable dataModel) {
        handleQueryChange();
    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataTable dataModel) {
        if (isShowingHistograms()) {
            for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                Axis pcpAxis = axisList.get(iaxis);
                double axisLeft = plotRegionBounds.getMinX() + (iaxis * axisSpacing);
                pcpAxis.resize(axisLeft, pcpRegionBounds.getMinY(), axisSpacing, pcpRegionBounds.getHeight());
            }
        }

//        if (getPolylineDisplayMode() == POLYLINE_DISPLAY_MODE.BINNED_POLYLINES) {
//            for (PCPBinSet PCPBinSet : PCPBinSetList) {
//                PCPBinSet.layoutBins();
//            }
//            redrawView();
//        }
    }

//    private UnivariateAxisSelection getAxisSelectionforColumnSelection(ColumnSelection columnSelection) {
//        if (axisList != null) {
//            for (PCPUnivariateAxis axis : axisList) {
//                if (axis.getColumn() == columnSelection.getColumn()) {
//                    for (UnivariateAxisSelection axisSelection : axis.getAxisSelectionList()) {
//                        if (axisSelection.getColumnSelectionRange() == columnSelection) {
//                            return axisSelection;
//                        }
//                    }
//                }
//            }
//        }
//
//        return null;
//    }

    private Axis getAxisForColumn(Column column) {
        for (Axis axis : axisList) {
            if (axis instanceof UnivariateAxis && ((UnivariateAxis)axis).getColumn() == column) {
                return axis;
            }
        }

        return null;
    }

    @Override
    public void dataModelColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelection) {
        Axis axis = getAxisForColumn(columnSelection.getColumn());
        if (axis != null) {
            // add it to the appropriate column axis (it is exists the axis will ignore the add request)
            axis.addAxisSelection(columnSelection);
        }

        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelection) {
        // find selection column axis and remove selection (if it doesn't exist, the axis will ignore the remove request)
        Axis axis = getAxisForColumn(columnSelection.getColumn());
        if (axis != null) {
            axis.removeAxisSelection(columnSelection);
        }

        handleQueryChange();
    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataModel) {
//        for (PCPUnivariateAxis axis : axisList) {
//            axis.removeAllAxisSelections();
//        }
//
//        handleQueryChange();
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataModel, Column column) {
//        PCPUnivariateAxis axis = getAxisForColumn(column);
//        if (axis != null) {
//            axis.removeAllAxisSelections();
//        }
//
//        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        if (dataModel.getHighlightedColumn() == null) {
            for (Axis axis : axisList) {
                axis.setHighlighted(false);
            }
        } else {
            for (Axis axis : axisList) {
                if (axis.getColumn() == newHighlightedColumn) {
                    axis.setHighlighted(true);
                } else if (axis.getColumn() == oldHighlightedColumn) {
                    axis.setHighlighted(false);
                }
            }
        }

        initView();
    }

    @Override
    public void dataModelTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples) {
//        // clear axis selections
//        for (PCPUnivariateAxis pcpAxis : axisList) {
//            pcpAxis.removeAllAxisSelections();
//        }
//
//        initView();
    }

    @Override
    public void dataModelTuplesRemoved(DataTable dataModel, int numTuplesRemoved) {
//        // clear axis selections
//        for (PCPUnivariateAxis pcpAxis : axisList) {
//            pcpAxis.removeAllAxisSelections();
//        }
//
//        initView();
    }

//    public void removeAxis(Axis axis) {
//        if (axisList.contains(axis)) {
//            axisList.remove(axis);
//            pane.getChildren().remove(axis.getGraphicsGroup());
//            reinitializeScatterplots();
//            reinitializeCorrelationRectangles();
//
//            //TODO: reinit bins and polylines, fill tuple line sets
//
//            resizeView();
//        }
//    }

    @Override
    public void dataModelColumnDisabled(DataTable dataModel, Column disabledColumn) {
//        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
//            PCPUnivariateAxis pcpAxis = axisList.get(iaxis);
//            if (pcpAxis.getColumn() == disabledColumn) {
//                axisList.remove(pcpAxis);
//
//                // remove axis graphics from pane
//                pcpAxis.removeAllGraphics(pane);
//
//                reinitializeScatterplots();
//
//                // create PCPBinSets for axis configuration
//                PCPBinSetList = new ArrayList<>();
//                for (int i = 0; i < axisList.size()-1; i++) {
//                    PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
//                    binSet.layoutBins();
//                    PCPBinSetList.add(binSet);
//                }
//
//                // add tuples polylines from data model
//                tupleList = new ArrayList<>();
//                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//                    Tuple tuple = dataModel.getTuple(iTuple);
//                    PCPTuple pcpTuple = new PCPTuple(tuple);
//                    tupleList.add(pcpTuple);
//                }
//                fillTupleSets();
//
//                resizeView();
//                break;
//            }
//        }
    }

    @Override
    public void dataModelColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataTable dataModel, Column enabledColumn) {
//        // add axis lines to the pane
//        addAxis(enabledColumn);
//
//        reinitializeScatterplots();
//
//        // add tuples polylines from data model
//        tupleList = new ArrayList<>();
//        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//            Tuple tuple = dataModel.getTuple(iTuple);
//            PCPTuple pcpTuple = new PCPTuple(tuple);
//            tupleList.add(pcpTuple);
//        }
//
//        fillTupleSets();
//
//        // create PCPBinSets for axis configuration
//        PCPBinSetList = new ArrayList<>();
//        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
//            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
//            PCPBinSetList.add(binSet);
//        }
//
//        resizeView();
    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn) {
        addBivariateAxis(bivariateColumn, axisList.size());
    }

    @Override
    public void dataModelColumnOrderChanged(DataTable dataModel) {
        initView();
    }

    @Override
    public void dataModelColumnNameChanged(DataTable dataModel, Column column) { }

    public enum POLYLINE_DISPLAY_MODE {POLYLINES, BINNED_POLYLINES}

    public enum STATISTICS_DISPLAY_MODE {MEDIAN_BOXPLOT, MEAN_BOXPLOT}
}