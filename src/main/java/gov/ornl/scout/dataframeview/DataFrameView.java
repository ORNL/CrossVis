package gov.ornl.scout.dataframeview;

import gov.ornl.scout.dataframe.*;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

public class DataFrameView extends Region {
    private final Logger log = Logger.getLogger(DataFrameView.class.getName());

    private DataFrame dataFrame;
    private DataFrame selectedDataFrame;
    private DataFrame unselectedDataFrame;

    private Pane pane;
    private Group titleTextGroup;

    private Canvas selectedCanvas;
    private Canvas unselectedCanvas;

    private BoundingBox titleTextBounds;
    private BoundingBox scatterplotRegionBounds;
    private BoundingBox pcpRegionBounds;

    private ArrayList<Axis> axisList = new ArrayList<>();
    private ArrayList<Text> axisTitleList = new ArrayList<>();

    private ArrayList<Scatterplot> scatterplotList = new ArrayList<>();

    private ObjectProperty<Color> unselectedItemsColor = new SimpleObjectProperty<>(DataFrameViewDefaultSettings.DEFAULT_UNSELECTED_ITEMS_COLOR);
    private ObjectProperty<Color> selectedItemsColor = new SimpleObjectProperty<>(DataFrameViewDefaultSettings.DEFAULT_SELECTED_ITEMS_COLOR);
    private ObjectProperty<Color> titleTextColor = new SimpleObjectProperty<>(DataFrameViewDefaultSettings.DEFAULT_LABEL_COLOR);
    private ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DataFrameViewDefaultSettings.DEFAULT_BACKGROUND_COLOR);
    private DoubleProperty polylineOpacity = new SimpleDoubleProperty(DataFrameViewDefaultSettings.DEFAULT_LINE_OPACITY);
    private BooleanProperty showSelectedItems = new SimpleBooleanProperty(true);
    private BooleanProperty showUnselectedItems = new SimpleBooleanProperty(true);
    private BooleanProperty fitToWidth = new SimpleBooleanProperty(true);
    private BooleanProperty fitToHeight = new SimpleBooleanProperty(true);

    private Orientation orientation;
    private double axisSpacing = 50.;
    private double polylineStrokeWidth = 1.5;
    private double plotAreaPadding = 4;

    private Rectangle plotAreaRectangle = new Rectangle();
    private Rectangle pcpAreaRectangle = new Rectangle();
    private Rectangle scatterplotAreaRectangle = new Rectangle();
    private Rectangle titleAreaRectangle = new Rectangle();

    private ArrayList<RowPolyline> polylines = new ArrayList<>();
    private HashSet<RowPolyline> selectedPolylineSet = new HashSet<>();
    private HashSet<RowPolyline> unselectedPolylineSet = new HashSet<>();

    private PolylineRendererTimer unselectedPolylineRendererTimer;
    private PolylineRendererTimer selectedPolylineRendererTimer;

    public DataFrameView(Orientation orientation) {
        this.orientation = orientation;
        initialize();
        registerListeners();
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

    public final double getPolylineOpacity() { return polylineOpacity.get(); }

    public final void setPolylineOpacity(double opacity) {
        polylineOpacity.set(opacity);
    }

    public DoubleProperty polylineOpacityProperty() { return polylineOpacity; }

    public Color getUnselectedItemsColor() { return unselectedItemsColor.get(); }

    public void setUnselectedItemsColor(Color color) { unselectedItemsColor.set(color); }

    public ObjectProperty<Color> unselectedItemsColorProperty() { return unselectedItemsColor; }

    public Color getSelectedItemsColor() { return selectedItemsColor.get(); }

    public void setSelectedItemsColor(Color color) { selectedItemsColor.set(color); }

    public ObjectProperty<Color> selectedItemsColorProperty() { return selectedItemsColor; }

    public final boolean isShowingSelectedItems() { return showSelectedItems.get(); }

    public final void setShowSelectedItems(boolean enabled) { showSelectedItems.set(enabled); }

    public BooleanProperty showSelectedItemsProperty() { return showSelectedItems; }

    public final boolean isShowingUnselectedItems() { return showUnselectedItems.get(); }

    public final void setShowUnselectedItems(boolean enabled) { showUnselectedItems.set(enabled); }

    public BooleanProperty showUnselectedItemsProperty() { return showUnselectedItems; }

    public DataFrame getDataFrame() {
        return dataFrame;
    }

    public void setDataFrame(DataFrame dataFrame) {
        // clear everything
        if (this.dataFrame != null) {
            clearView();
        }

        this.dataFrame = dataFrame;

        polylines.clear();
        for (int irow = 0; irow < dataFrame.getRowCount(); irow++) {
            RowPolyline polyline = new RowPolyline(dataFrame.getRow(irow));
            polylines.add(polyline);
        }

        fillPolylineSets();
        layoutView();
    }

    private void fillPolylineSets() {
        unselectedPolylineSet.clear();
        selectedPolylineSet.clear();

        if (polylines != null && !polylines.isEmpty()) {
            selectedPolylineSet.addAll(polylines);
        }
    }

    private void initialize() {
        titleTextGroup = new Group();

        plotAreaRectangle.setStroke(Color.BLUE);
        plotAreaRectangle.setFill(Color.TRANSPARENT);
        pcpAreaRectangle.setStroke(Color.ORANGE);
        pcpAreaRectangle.setFill(Color.TRANSPARENT);
        pcpAreaRectangle.setStrokeWidth(2.);
        scatterplotAreaRectangle.setStroke(Color.YELLOWGREEN);
        scatterplotAreaRectangle.setFill(Color.TRANSPARENT);
        scatterplotAreaRectangle.setStrokeWidth(2.);
        titleAreaRectangle.setStroke(Color.RED);
        titleAreaRectangle.setFill(Color.TRANSPARENT);

        selectedCanvas = new Canvas(getWidth(), getHeight());
        unselectedCanvas = new Canvas(getWidth(), getHeight());

        pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        pane.getChildren().addAll(unselectedCanvas, selectedCanvas, titleTextGroup, plotAreaRectangle, pcpAreaRectangle,
                scatterplotAreaRectangle, titleAreaRectangle);

        getChildren().add(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(observable -> layoutView());
        heightProperty().addListener(observable -> layoutView());

        backgroundColor.addListener(observable -> {
            pane.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        });

        polylineOpacity.addListener(((observable, oldValue, newValue) -> {
            setSelectedItemsColor(new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                    getSelectedItemsColor().getBlue(), newValue.doubleValue()));
            setUnselectedItemsColor(new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                    getUnselectedItemsColor().getBlue(), newValue.doubleValue()));
            redrawView();
        }));

        selectedItemsColor.addListener((observable, oldValue, newValue) -> redrawView());

        unselectedItemsColor.addListener((observable, oldValue, newValue) -> redrawView());

        showSelectedItems.addListener(observable -> redrawView());

        showUnselectedItems.addListener(observable -> redrawView());
    }

    public Orientation getOrientation() { return orientation; }

    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;

            for (Axis axis : axisList) {
                axis.setOrientation(this.orientation);
            }

            layoutView();
        }
    }

    public double getAxisSpacing() { return axisSpacing; }

    public void setAxisSpacing(double axisSpacing) {
        this.axisSpacing = axisSpacing;
        layoutView();
    }

    private Text createTitleText(Column column) {
        Text titleText = new Text(column.getTitle());
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(column.titleProperty());
        Tooltip.install(titleText, tooltip);
        titleText.setFont(new Font(DataFrameViewDefaultSettings.DEFAULT_COLUMN_TITLE_TEXT_SIZE));
        titleText.setSmooth(true);
        titleText.setFill(titleTextColor.get());
        return titleText;
    }

    protected void layoutView() {
        // clear the current window view
        if (dataFrame != null && dataFrame.getColumnCount() > 0) {
            if (axisList.isEmpty()) {
                titleTextGroup.getChildren().clear();
                for (int iaxis = 0; iaxis < dataFrame.getColumnCount(); iaxis++) {
                    Axis axis = null;
                    if (dataFrame.getColumn(iaxis) instanceof DoubleColumn) {
                        axis = new DoubleAxis(this, dataFrame.getColumn(iaxis), orientation);
                    } else if (dataFrame.getColumn(iaxis) instanceof TemporalColumn) {
                        axis = new TemporalAxis(this, dataFrame.getColumn(iaxis), orientation);
                    } else if (dataFrame.getColumn(iaxis) instanceof CategoricalColumn) {
                        axis = new CategoricalAxis(this, dataFrame.getColumn(iaxis), orientation);
                    }

                    if (axis != null) {
                        Text axisTitleText = createTitleText(dataFrame.getColumn(iaxis));
                        axisTitleText.setTextOrigin(VPos.CENTER);
                        titleTextGroup.getChildren().add(axisTitleText);
                        pane.getChildren().add(axis.getGraphicsGroup());
                        axisList.add(axis);
                        axisTitleList.add(axisTitleText);
                    }
                }
            }

            if (scatterplotList.isEmpty()) {
                for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
                    Axis yAxis = axisList.get(iaxis);
                    Axis xAxis = axisList.get(iaxis+1);
                    Scatterplot scatterplot = new Scatterplot(xAxis, yAxis);
                    scatterplot.pointStrokeOpacityProperty().bind(polylineOpacity);
                    scatterplot.selectedPointStrokeColorProperty().bind(selectedItemsColor);
                    scatterplot.unselectedPointStrokeColorProperty().bind(unselectedItemsColor);
                    scatterplotList.add(scatterplot);
                    pane.getChildren().add(scatterplot.getGraphicsGroup());
                }
            }

            plotAreaRectangle.setX(getInsets().getLeft());
            plotAreaRectangle.setY(getInsets().getTop());
            plotAreaRectangle.setWidth(getWidth() - (getInsets().getLeft() + getInsets().getRight()));
            plotAreaRectangle.setHeight(getHeight() - (getInsets().getTop() + getInsets().getBottom()));

            if (orientation == Orientation.HORIZONTAL) {
//                double plotHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());
                double plotWidth;
                double width;

                if (getFitToWidth()) {
                    width = getWidth();
                    plotWidth = width - (getInsets().getLeft() + getInsets().getRight());
                    axisSpacing = plotWidth / axisList.size();
                } else {
                    plotWidth = axisSpacing * axisList.size();
                    width = plotWidth + getInsets().getLeft() + getInsets().getRight();
                }

                double plotHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

                double scatterplotHeight = axisSpacing * .7;
                scatterplotHeight = scatterplotHeight > (plotHeight * .3) ? (plotHeight * .3) : scatterplotHeight;

                if (plotWidth > 0 && plotHeight > 0) {
                    pane.setPrefSize(width, getHeight());
                    pane.setMinWidth(width);

                    selectedCanvas.setWidth(width);
                    selectedCanvas.setHeight(getHeight());
                    unselectedCanvas.setWidth(width);
                    unselectedCanvas.setHeight(getHeight());

                    if (axisList != null) {
                        titleTextBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(), plotWidth,
                                axisTitleList.get(0).getLayoutBounds().getHeight() + 2);
                        titleAreaRectangle.setX(titleTextBounds.getMinX());
                        titleAreaRectangle.setY(titleTextBounds.getMinY());
                        titleAreaRectangle.setWidth(titleTextBounds.getWidth());
                        titleAreaRectangle.setHeight(titleTextBounds.getHeight());

                        pcpRegionBounds = new BoundingBox(getInsets().getLeft(),
                                titleTextBounds.getMaxY() + plotAreaPadding, plotWidth,
                                plotHeight-(titleTextBounds.getHeight() + scatterplotHeight + 2 * plotAreaPadding));
                        pcpAreaRectangle.setX(pcpRegionBounds.getMinX());
                        pcpAreaRectangle.setY(pcpRegionBounds.getMinY());
                        pcpAreaRectangle.setWidth(pcpRegionBounds.getWidth());
                        pcpAreaRectangle.setHeight(pcpRegionBounds.getHeight());

                        scatterplotRegionBounds = new BoundingBox(getInsets().getLeft(),
                                pcpRegionBounds.getMaxY() + plotAreaPadding, plotWidth, scatterplotHeight);
                        scatterplotAreaRectangle.setX(scatterplotRegionBounds.getMinX());
                        scatterplotAreaRectangle.setY(scatterplotRegionBounds.getMinY());
                        scatterplotAreaRectangle.setWidth(scatterplotRegionBounds.getWidth());
                        scatterplotAreaRectangle.setHeight(scatterplotRegionBounds.getHeight());

                        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                            double axisLeft = getInsets().getLeft() + (iaxis * axisSpacing);
                            Axis axis = axisList.get(iaxis);
                            axis.resize(axisLeft, pcpRegionBounds.getMinY(),
                                    axisSpacing, pcpRegionBounds.getHeight());
//                            axis.resize(axisLeft, titleTextBounds.getMaxY(),
//                                    axisSpacing, plotHeight-titleTextBounds.getHeight());

                            Text titleText = axisTitleList.get(iaxis);
                            titleText.setX(axisLeft + (axisSpacing / 2.) - (titleText.getLayoutBounds().getWidth() / 2.));
                            titleText.setY(titleTextBounds.getMinY() + (titleTextBounds.getHeight() / 2.));
                        }

                        for (int i = 0; i < scatterplotList.size(); i++) {
                            Scatterplot scatterplot = scatterplotList.get(i);
                            double centerX = (scatterplot.getYAxis().getCenterX() + scatterplot.getXAxis().getCenterX()) / 2.;
                            double left = centerX - (scatterplotHeight / 2.) - scatterplot.getAxisSize();
                            scatterplot.layout(left, scatterplotRegionBounds.getMinY(),
                                    scatterplotHeight, scatterplotHeight);
                        }
                    }
                }
            } else {
                double plotHeight;
                double plotWidth = getWidth() - (getInsets().getLeft() + getInsets().getRight());
                double height;

                if (getFitToHeight()) {
                    height = getHeight();
                    plotHeight = height - (getInsets().getTop() + getInsets().getBottom());
                    axisSpacing = plotHeight / axisList.size();
                } else {
                    plotHeight = axisSpacing * axisList.size();
                    height = plotHeight + getInsets().getLeft() + getInsets().getRight();
                }

                double scatterplotWidth = axisSpacing * .7;
                scatterplotWidth = scatterplotWidth > (plotWidth * .3) ? (plotWidth * .3) : scatterplotWidth;


                if (plotWidth > 0 && plotHeight > 0) {
                    pane.setPrefSize(getWidth(), height);
                    pane.setMinHeight(height);

                    selectedCanvas.setWidth(getWidth());
                    selectedCanvas.setHeight(height);

                    if (axisList != null) {
                        double longestTitle = 0.;
                        for (Text titleText : axisTitleList) {
                            if (titleText.getLayoutBounds().getWidth() > longestTitle) {
                                longestTitle = titleText.getLayoutBounds().getWidth();
                            }
                        }

                        titleTextBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(),
                                longestTitle + 2, plotHeight);
                        titleAreaRectangle.setX(titleTextBounds.getMinX());
                        titleAreaRectangle.setY(titleTextBounds.getMinY());
                        titleAreaRectangle.setWidth(titleTextBounds.getWidth());
                        titleAreaRectangle.setHeight(titleTextBounds.getHeight());

                        pcpRegionBounds = new BoundingBox(titleTextBounds.getMaxX() + plotAreaPadding, getInsets().getTop(),
                                plotWidth - (titleTextBounds.getWidth() + scatterplotWidth + (2 * plotAreaPadding)), plotHeight);
//                        pcpRegionBounds = new BoundingBox(getInsets().getLeft(),
//                                titleTextBounds.getMaxY() + plotAreaPadding, plotWidth,
//                                plotHeight-(titleTextBounds.getHeight() + scatterplotHeight + 2 * plotAreaPadding));
                        pcpAreaRectangle.setX(pcpRegionBounds.getMinX());
                        pcpAreaRectangle.setY(pcpRegionBounds.getMinY());
                        pcpAreaRectangle.setWidth(pcpRegionBounds.getWidth());
                        pcpAreaRectangle.setHeight(pcpRegionBounds.getHeight());

                        scatterplotRegionBounds = new BoundingBox(pcpRegionBounds.getMaxX() + plotAreaPadding,
                                getInsets().getTop(), scatterplotWidth, plotHeight);
                        scatterplotAreaRectangle.setX(scatterplotRegionBounds.getMinX());
                        scatterplotAreaRectangle.setY(scatterplotRegionBounds.getMinY());
                        scatterplotAreaRectangle.setWidth(scatterplotRegionBounds.getWidth());
                        scatterplotAreaRectangle.setHeight(scatterplotRegionBounds.getHeight());

                        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                            double axisTop = titleTextBounds.getMinY() + (iaxis * axisSpacing);

                            Axis axis = axisList.get(iaxis);
                            axis.resize(pcpRegionBounds.getMinX(), axisTop, pcpRegionBounds.getWidth(), axisSpacing);

                            Text axisTitleText = axisTitleList.get(iaxis);
                            axisTitleText.setX(titleTextBounds.getMaxX() - axisTitleText.getLayoutBounds().getWidth());
                            axisTitleText.setY(axis.getCenterY());

//                            double axisTop = getInsets().getTop() + (iaxis * axisSpacing);
//                            Axis axis = axisList.get(iaxis);
//                            axis.resize(titleTextBounds.getMaxX() + 4, axisTop,
//                                    plotWidth - titleTextBounds.getWidth() - 6, axisSpacing);
//                            Text axisTitleText = axisTitleList.get(iaxis);
//                            axisTitleText.setX(titleTextBounds.getMaxX() - axisTitleText.getLayoutBounds().getWidth());
//                            axisTitleText.setY(axis.getCenterY());
                        }

                        for (int i = 0; i < scatterplotList.size(); i++) {
                            Scatterplot scatterplot = scatterplotList.get(i);
                            double centerY = (scatterplot.getYAxis().getCenterY() + scatterplot.getXAxis().getCenterY()) / 2.;
                            double top = centerY - (scatterplotWidth / 2.) + scatterplot.getAxisSize();
//                                    centerX - (scatterplotHeight / 2.) - scatterplot.getAxisSize();
                            scatterplot.layout(scatterplotRegionBounds.getMinX(), top, scatterplotWidth, scatterplotWidth);
                        }
                    }
                }
            }


            for (RowPolyline rowPolyline : polylines) {
                rowPolyline.layout(axisList);
            }

            redrawView();
        }
    }

    protected void redrawView() {
        drawPolylines();
    }

    private void drawPolylines() {
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(polylineStrokeWidth);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, unselectedCanvas.getWidth(), unselectedCanvas.getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(polylineStrokeWidth);

        if ((isShowingUnselectedItems()) && (unselectedPolylineSet != null) && (!unselectedPolylineSet.isEmpty())) {
            if (unselectedPolylineRendererTimer != null && unselectedPolylineRendererTimer.isRunning()) {
                unselectedPolylineRendererTimer.stop();
            }

            Color lineColor = new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                    getUnselectedItemsColor().getBlue(), getPolylineOpacity());
            unselectedPolylineRendererTimer = new PolylineRendererTimer(unselectedCanvas, unselectedPolylineSet,
                    axisList, lineColor, 100, orientation);
            unselectedPolylineRendererTimer.start();
        }

        if ((isShowingSelectedItems()) && (selectedPolylineSet != null) && (!selectedPolylineSet.isEmpty())) {
            if (selectedPolylineRendererTimer != null && selectedPolylineRendererTimer.isRunning()) {
                selectedPolylineRendererTimer.stop();
            }

            Color lineColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                    getSelectedItemsColor().getBlue(), getPolylineOpacity());
            selectedPolylineRendererTimer = new PolylineRendererTimer(selectedCanvas, selectedPolylineSet,
                    axisList, lineColor, 100, orientation);
            selectedPolylineRendererTimer.start();
        }
    }

    public boolean getFitToWidth() { return fitToWidth.get(); }

    public void setFitToWidth(boolean fitToWidth) { this.fitToWidth.set(fitToWidth); }

    public BooleanProperty fitToWidthProperty() { return fitToWidth; }

    public boolean getFitToHeight() { return fitToHeight.get(); }

    public void setFitToHeight(boolean fitToHeight) { this.fitToHeight.set(fitToHeight); }

    public BooleanProperty fitToHeightProperty() { return fitToHeight; }

    protected void clearView() {
        for (Axis axis : axisList) {
            pane.getChildren().remove(axis.getGraphicsGroup());
        }
        axisList.clear();
        axisTitleList.clear();
        titleTextGroup.getChildren().clear();
        polylines.clear();
        selectedPolylineSet.clear();
        unselectedPolylineSet.clear();
    }
}
