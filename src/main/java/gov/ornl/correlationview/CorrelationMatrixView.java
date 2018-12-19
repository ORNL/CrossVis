package gov.ornl.correlationview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CorrelationMatrixView extends Region implements DataTableListener {
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public final static Color DEFAULT_ZERO_COLOR = Color.GHOSTWHITE;
    public final static Color DEFAULT_POSITIVE_COLOR = Color.DODGERBLUE;
    public final static Color DEFAULT_NEGATIVE_COLOR = Color.DARKRED;
    public final static Color DEFAULT_DIAGONAL_COLOR = Color.LIGHTGRAY;
    public final static double DEFAULT_TEXT_SIZE = 10.;
    public final static double DEFAULT_MAX_AXIS_SIZE = 80.;
    public final static double DEFAULT_COLOR_SCALE_SIZE = 30;

    private final static Logger log = Logger.getLogger(CorrelationMatrixView.class.getName());

    private Pane pane;
    private DataTable dataTable;

    private BoundingBox viewRegionBounds;
    private BoundingBox plotRegionBounds;
    private BoundingBox xAxisRegionBounds;
    private BoundingBox yAxisRegionBounds;
    private BoundingBox colorScaleRegionBounds;

    private Rectangle viewRegionRectangle;
    private Rectangle plotRegionRectangle;
    private Rectangle xAxisRegionRectangle;
    private Rectangle yAxisRegionRectangle;
    private Rectangle colorScaleRegionRectangle;

    private Rectangle colorScaleRectangle;
    private Text colorScaleNegativeText;
    private Text colorScalePositiveText;
    private Text colorScaleZeroText;
    private Group colorScaleGraphicsGroup = new Group();

    private ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(DEFAULT_BACKGROUND_COLOR);
    private ObjectProperty<Color> textColor = new SimpleObjectProperty<>(DEFAULT_TEXT_COLOR);
    private ObjectProperty<Color> zeroColor = new SimpleObjectProperty<>(DEFAULT_ZERO_COLOR);
    private ObjectProperty<Color> positiveColor = new SimpleObjectProperty<>(DEFAULT_POSITIVE_COLOR);
    private ObjectProperty<Color> negativeColor = new SimpleObjectProperty<>(DEFAULT_NEGATIVE_COLOR);
    private ObjectProperty<Color> diagonalColor = new SimpleObjectProperty<>(DEFAULT_DIAGONAL_COLOR);
    private DoubleProperty maxAxisSize = new SimpleDoubleProperty(DEFAULT_MAX_AXIS_SIZE);
    private BooleanProperty showQueryCorrelations = new SimpleBooleanProperty(false);

    private ArrayList<Text> yColumnTitles = new ArrayList<>();
    private ArrayList<Text> xColumnTitles = new ArrayList<>();
    private ArrayList<ArrayList<CorrelationMatrixCell>> cellRows = new ArrayList<>();
    private ArrayList<DoubleColumn> doubleColumns = new ArrayList<>();

    Group cellGraphics = new Group();
    Group titleGraphics = new Group();

    private ObjectProperty<Orientation> colorScaleOrientation = new SimpleObjectProperty<>(Orientation.VERTICAL);
    private BooleanProperty showColorScale = new SimpleBooleanProperty(true);

    private Stop colorScaleGradientStops[];

    private boolean dragging = false;
    private Point2D dragStartPoint;
    private Point2D dragEndPoint;

    public CorrelationMatrixView () {
        setMinSize(200, 200);
        initialize();
        registerListeners();
    }

    public Orientation getColorScaleOrientation() { return colorScaleOrientation.get(); }

    public void setColorScaleOrientation(Orientation newOrientation) {
        if (newOrientation != getColorScaleOrientation()) {
            colorScaleOrientation.set(newOrientation);
        }
    }

    public ObjectProperty<Orientation> colorScaleOrientationProperty () { return colorScaleOrientation; }

    public boolean isShowingColorScale() { return showColorScale.get(); }

    public void setShowColorScale(boolean show) {
        if (isShowingColorScale() != show) {
            showColorScale.set(show);
        }
    }

    public BooleanProperty showColorScaleProperty() { return showColorScale; }

    public BooleanProperty showQueryCorrelationsProperty() { return showQueryCorrelations; }

    public void setShowQueryCorrelations(boolean enabled) {
        if (isShowingQueryCorrelations() != enabled) {
            showQueryCorrelations.set(enabled);
        }
    }

    public boolean isShowingQueryCorrelations() { return showQueryCorrelations.get(); }

    public void setDataTable(DataTable dataTable) {
        clearView();
        this.dataTable = dataTable;
        dataTable.addDataModelListener(this);
        initView();
        resizeView();
    }

    private void initialize() {
        viewRegionRectangle = new Rectangle();
        viewRegionRectangle.setStroke(Color.DARKBLUE);
        viewRegionRectangle.setFill(Color.TRANSPARENT);
        viewRegionRectangle.setMouseTransparent(true);
        viewRegionRectangle.setStrokeWidth(2);

        plotRegionRectangle = new Rectangle();
        plotRegionRectangle.setStroke(Color.gray(0.3));
        plotRegionRectangle.setFill(Color.TRANSPARENT);
        plotRegionRectangle.setMouseTransparent(true);
        plotRegionRectangle.setStrokeWidth(1.5);

        xAxisRegionRectangle = new Rectangle();
        xAxisRegionRectangle.setStroke(Color.BLUE);
        xAxisRegionRectangle.setFill(Color.TRANSPARENT);
        xAxisRegionRectangle.setMouseTransparent(true);
        xAxisRegionRectangle.setStrokeWidth(1);

        yAxisRegionRectangle = new Rectangle();
        yAxisRegionRectangle.setStroke(Color.ORANGE);
        yAxisRegionRectangle.setFill(Color.TRANSPARENT);
        yAxisRegionRectangle.setMouseTransparent(true);
        yAxisRegionRectangle.setStrokeWidth(1);

        colorScaleRegionRectangle = new Rectangle();
        colorScaleRegionRectangle.setStroke(Color.CORNFLOWERBLUE);
        colorScaleRegionRectangle.setFill(Color.TRANSPARENT);
        colorScaleRegionRectangle.setMouseTransparent(true);
        colorScaleRegionRectangle.setStrokeWidth(1);

        colorScaleRectangle = new Rectangle();
        colorScaleRectangle.setStroke(Color.gray(0.3));
        colorScaleRectangle.setMouseTransparent(true);
        colorScaleGradientStops = new Stop[]{new Stop(0, negativeColor.get()),
                new Stop(0.5, zeroColor.get()),
                new Stop(1, positiveColor.get())};
        if (getColorScaleOrientation() == Orientation.HORIZONTAL) {
            colorScaleRectangle.setFill(new LinearGradient(0, 0, 1, 0, true,
                    CycleMethod.NO_CYCLE, colorScaleGradientStops));
        } else {
            colorScaleRectangle.setFill(new LinearGradient(0, 1, 0, 0, true,
                    CycleMethod.NO_CYCLE, colorScaleGradientStops));
        }

        colorScaleNegativeText = new Text("-1");
        colorScaleNegativeText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScaleNegativeText.setFill(textColor.get());

        colorScalePositiveText = new Text("1");
        colorScalePositiveText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScalePositiveText.setFill(textColor.get());

        colorScaleZeroText = new Text("0");
        colorScaleZeroText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScaleZeroText.setFill(textColor.get());

        colorScaleGraphicsGroup.getChildren().addAll(colorScaleRectangle, colorScaleNegativeText, colorScalePositiveText,
                colorScaleZeroText);

        pane = new Pane();
        this.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        pane.getChildren().addAll(cellGraphics, titleGraphics);

        if (isShowingColorScale()) { pane.getChildren().add(colorScaleGraphicsGroup); }

//        pane.getChildren().addAll(cellGraphics, titleGraphics, viewRegionRectangle, plotRegionRectangle, xAxisRegionRectangle, yAxisRegionRectangle);
//        pane.getChildren().add(viewRegionRectangle);

        getChildren().add(pane);
    }

    private void registerListeners() {

        widthProperty().addListener(o -> resizeView());
        heightProperty().addListener(o -> resizeView());

        showColorScale.addListener(observable -> {
            if (isShowingColorScale()) {
                pane.getChildren().add(colorScaleGraphicsGroup);
            } else {
                pane.getChildren().remove(colorScaleGraphicsGroup);
            }
            resizeView();
        });

        colorScaleOrientation.addListener(observable -> {
            if (getColorScaleOrientation() == Orientation.HORIZONTAL) {
                colorScaleRectangle.setFill(new LinearGradient(0, 0, 1, 0, true,
                        CycleMethod.NO_CYCLE, colorScaleGradientStops));
            } else {
                colorScaleRectangle.setFill(new LinearGradient(0, 1, 0, 0, true,
                        CycleMethod.NO_CYCLE, colorScaleGradientStops));
            }

            if (isShowingColorScale()) {
                resizeView();
            }
        });

        backgroundColor.addListener((observable, oldValue, newValue) -> {
            this.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        });

        textColor.addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//            }
        });

        showQueryCorrelations.addListener(observable -> {
            clearView();
            initView();
            resizeView();
        });
//
//        this.setOnZoomStarted(event -> {
//
//        });
//
//        this.setOnZoom(event -> {
//            log.info("onZoom(): zoomFactor=" + event.getZoomFactor() + "  totalZoomFactor=" + event.getTotalZoomFactor());
//        });
//
//
//        this.setOnMouseReleased(event -> {
//            log.info("mouseDragReleased()");
//        });
//
//        this.setOnMouseDragged(event -> {
//            log.info("onMouseDragged()");
//            if (!dragging) {
//                dragging = true;
//                dragStartPoint = new Point2D(event.getX(), event.getY());
//            }
//
//            dragEndPoint = new Point2D(event.getX(), event.getY());
//
//            pane.setTranslateX(dragEndPoint.getX() - dragStartPoint.getX());
//            pane.setTranslateY(dragEndPoint.getY() - dragStartPoint.getY());
//        });
//
//        this.setOnZoomFinished(event -> {
//
//        });
    }

    private void setCellColors() {
        for (int icol = 0; icol < doubleColumns.size(); icol++) {
            ArrayList<CorrelationMatrixCell> cells = cellRows.get(icol);
            for (int irow = 0; irow < doubleColumns.size(); irow++) {
                CorrelationMatrixCell cell = cells.get(irow);
                if (icol == irow) {
                    cell.getCellRectangle().setFill(getDiagonal());
                } else {
                    double correlation = cell.getCorrelation();
                    if (correlation > 0) {
                        cell.getCellRectangle().setFill(GraphicsUtil.lerpColorFX(getZeroColor(), getPositiveColor(), correlation));
                    } else {
                        cell.getCellRectangle().setFill(GraphicsUtil.lerpColorFX(getZeroColor(), getNegativeColor(), correlation * -1.));
                    }
                }
            }
        }
    }

    public Color getDiagonal() { return diagonalColor.get(); }
    public void setDiagonalColor(Color color) { diagonalColor.set(color); }
    public ObjectProperty<Color> diagonalColorProperty() { return diagonalColor; }

    public Color getZeroColor() { return zeroColor.get(); }
    public void setZeroColor(Color color) { zeroColor.set(color); }
    public ObjectProperty<Color> zeroColorProperty() { return zeroColor; }

    public Color getPositiveColor() { return positiveColor.get(); }
    public void setPositiveColor(Color color) { positiveColor.set(color); }
    public ObjectProperty<Color> positiveColorProperty() { return positiveColor; }

    public Color getNegativeColor() { return negativeColor.get(); }
    public void setNegativeColor(Color color) { negativeColor.set(color); }
    public ObjectProperty<Color> negativeColorProperty() { return negativeColor; }

    private void initView() {
        if (dataTable != null && !dataTable.isEmpty()) {
            doubleColumns = dataTable.getEnabledDoubleColumns();

            for (int yColumnIndex = 0; yColumnIndex < doubleColumns.size(); yColumnIndex++) {
                DoubleColumn yColumn = doubleColumns.get(yColumnIndex);

                Text yColumnTitleText = new Text(yColumn.getName());
                yColumnTitleText.setFont(Font.font(DEFAULT_TEXT_SIZE));
                yColumnTitleText.setTextOrigin(VPos.CENTER);
                yColumnTitles.add(yColumnTitleText);
                titleGraphics.getChildren().add(yColumnTitleText);

                ArrayList<CorrelationMatrixCell> cells = new ArrayList<>();

                for (int xColumnIndex = 0; xColumnIndex < doubleColumns.size(); xColumnIndex++) {
                    DoubleColumn xColumn = doubleColumns.get(xColumnIndex);

                    if (yColumnIndex == 0) {
                        Text xColumnTitleText = new Text(xColumn.getName());
                        xColumnTitleText.setFont(Font.font(DEFAULT_TEXT_SIZE));
                        xColumnTitleText.setTextOrigin(VPos.TOP);
                        xColumnTitleText.getTransforms().add(new Rotate(-90.));
                        xColumnTitles.add(xColumnTitleText);
                        titleGraphics.getChildren().add(xColumnTitleText);
                    }

                    double correlation = 0;
                    if (isShowingQueryCorrelations() && dataTable.getActiveQuery().hasColumnSelections()) {
                        correlation = ((DoubleColumnSummaryStats)dataTable.getActiveQuery().getColumnQuerySummaryStats(yColumn)).getCorrelationCoefficientList().get(dataTable.getColumnIndex(xColumn));
                    } else {
                        correlation = yColumn.getStatistics().getCorrelationCoefficientList().get(dataTable.getColumnIndex(xColumn));
                    }

                    CorrelationMatrixCell cell = new CorrelationMatrixCell(xColumn, yColumn, correlation);
                    cellGraphics.getChildren().add(cell.getCellRectangle());
                    cells.add(cell);
                }
                cellRows.add(cells);
            }

            setCellColors();
        }
    }

    private void clearView() {
        cellRows.clear();
        xColumnTitles.clear();
        yColumnTitles.clear();
        cellGraphics.getChildren().clear();
        titleGraphics.getChildren().clear();
    }

    private double findLongestColumnTitle() {
        double longestTitle = 0.;
        for (Text xColumnTitleText : xColumnTitles) {
            if (xColumnTitleText.getLayoutBounds().getWidth() > longestTitle) {
                longestTitle = xColumnTitleText.getLayoutBounds().getWidth() + 4.;
            }
        }

        if (longestTitle > getMaxAxisSize()) {
            longestTitle = getMaxAxisSize();
        }

        return longestTitle;
    }

    public double getMaxAxisSize() { return maxAxisSize.get(); }
    public void setMaxAxisSize(double size) { maxAxisSize.set(size); }
    public DoubleProperty maxAxisSizeProperty() { return maxAxisSize; }

    private void fitColumnTitleToAxisSize(Text titleText, double axisSize) {
        if (titleText.getLayoutBounds().getWidth() > axisSize) {
            // truncate the column name to fit axis bounds
            while (titleText.getLayoutBounds().getWidth() > axisSize) {
                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
            }
        }
    }

    private void resizeView() {
        viewRegionBounds = new BoundingBox(getInsets().getLeft(), getInsets().getTop(),
                getWidth() - (getInsets().getLeft() + getInsets().getRight()),
                getHeight() - (getInsets().getTop() + getInsets().getBottom()));
        viewRegionRectangle.setX(viewRegionBounds.getMinX());
        viewRegionRectangle.setY(viewRegionBounds.getMinY());
        viewRegionRectangle.setWidth(viewRegionBounds.getWidth());
        viewRegionRectangle.setHeight(viewRegionBounds.getHeight());

        double longestColumnTitle = findLongestColumnTitle();

        double colorScaleWidth = 0;
        double colorScaleHeight = 0;
        if (isShowingColorScale()) {
            if (getColorScaleOrientation() == Orientation.HORIZONTAL) {
                colorScaleHeight = DEFAULT_COLOR_SCALE_SIZE;
            } else {
                colorScaleWidth = DEFAULT_COLOR_SCALE_SIZE;
            }
        }

        double xAxisHeight = longestColumnTitle;
        double yAxisWidth = longestColumnTitle;
        double plotRegionWidth = viewRegionBounds.getWidth() - (yAxisWidth + colorScaleWidth);
        double plotRegionHeight = viewRegionBounds.getHeight() - (xAxisHeight + colorScaleHeight);
        double plotRegionSize = plotRegionWidth < plotRegionHeight ? plotRegionWidth : plotRegionHeight;
        double xAxisTopOffset = (viewRegionBounds.getHeight() - (plotRegionSize + xAxisHeight + colorScaleHeight)) / 2.;
        double yAxisLeftOffset = (viewRegionBounds.getWidth() - (plotRegionSize + yAxisWidth + colorScaleWidth)) / 2.;

        plotRegionBounds = new BoundingBox(viewRegionBounds.getMinX() + yAxisLeftOffset + yAxisWidth,
                viewRegionBounds.getMinY() + xAxisTopOffset + xAxisHeight, plotRegionSize, plotRegionSize);
        plotRegionRectangle.setX(plotRegionBounds.getMinX());
        plotRegionRectangle.setY(plotRegionBounds.getMinY());
        plotRegionRectangle.setWidth(plotRegionBounds.getWidth());
        plotRegionRectangle.setHeight(plotRegionBounds.getHeight());

        yAxisRegionBounds = new BoundingBox(viewRegionBounds.getMinX() + yAxisLeftOffset, plotRegionBounds.getMinY(),
                yAxisWidth, plotRegionSize);
//        yAxisRegionBounds = new BoundingBox(viewRegionBounds.getMinX(), viewRegionBounds.getMinY() + xAxisHeight,
//                yAxisWidth, plotRegionSize);
        yAxisRegionRectangle.setX(yAxisRegionBounds.getMinX());
        yAxisRegionRectangle.setY(yAxisRegionBounds.getMinY());
        yAxisRegionRectangle.setWidth(yAxisRegionBounds.getWidth());
        yAxisRegionRectangle.setHeight(yAxisRegionBounds.getHeight());

        xAxisRegionBounds = new BoundingBox(plotRegionBounds.getMinX(), viewRegionBounds.getMinY() + xAxisTopOffset,
                plotRegionSize, xAxisHeight);
//        xAxisRegionBounds = new BoundingBox(yAxisRegionBounds.getMaxX(), viewRegionBounds.getMinY(),
//                plotRegionSize, xAxisHeight);
        xAxisRegionRectangle.setX(xAxisRegionBounds.getMinX());
        xAxisRegionRectangle.setY(xAxisRegionBounds.getMinY());
        xAxisRegionRectangle.setWidth(xAxisRegionBounds.getWidth());
        xAxisRegionRectangle.setHeight(xAxisRegionBounds.getHeight());

        if (isShowingColorScale()) {
            if (getColorScaleOrientation() == Orientation.HORIZONTAL) {
                colorScaleRegionBounds = new BoundingBox(plotRegionBounds.getMinX(), plotRegionBounds.getMaxY() + 4,
                        plotRegionSize, colorScaleHeight - 4);
//                colorScaleRegionBounds = new BoundingBox(viewRegionBounds.getMinX() + yAxisLeftOffset + yAxisWidth,
//                        viewRegionBounds.getMinY() + (viewRegionBounds.getHeight() - colorScaleHeight),
//                        plotRegionSize, colorScaleHeight);

                colorScaleRectangle.setX(colorScaleRegionBounds.getMinX());
                colorScaleRectangle.setY(colorScaleRegionBounds.getMinY());
                colorScaleRectangle.setWidth(colorScaleRegionBounds.getWidth());
                colorScaleRectangle.setHeight(colorScaleRegionBounds.getHeight() - 12);

                colorScaleZeroText.setX((colorScaleRegionBounds.getMinX() + (colorScaleRegionBounds.getWidth() / 2.)) - (colorScaleZeroText.getLayoutBounds().getWidth() / 2.));
                colorScaleZeroText.setY(colorScaleRegionBounds.getMaxY() - 2);

                colorScalePositiveText.setX(colorScaleRegionBounds.getMaxX() - (colorScalePositiveText.getLayoutBounds().getWidth() + 2));
                colorScalePositiveText.setY(colorScaleZeroText.getY());

                colorScaleNegativeText.setX(colorScaleRegionBounds.getMinX() + 2);
                colorScaleNegativeText.setY(colorScaleZeroText.getY());
            } else {
                colorScaleRegionBounds = new BoundingBox(plotRegionBounds.getMaxX() + 4, plotRegionBounds.getMinY(),
                        colorScaleWidth - 4, plotRegionSize);
//                colorScaleRegionBounds = new BoundingBox(viewRegionBounds.getMaxX() - colorScaleWidth,
//                        viewRegionBounds.getMinY() + xAxisTopOffset + xAxisHeight, colorScaleWidth, plotRegionSize);

                colorScaleRectangle.setX(colorScaleRegionBounds.getMinX());
                colorScaleRectangle.setY(colorScaleRegionBounds.getMinY());
                colorScaleRectangle.setWidth(colorScaleRegionBounds.getWidth() - 12);
                colorScaleRectangle.setHeight(colorScaleRegionBounds.getHeight());

                colorScalePositiveText.setX(colorScaleRegionBounds.getMaxX() - 8);
//                colorScalePositiveText.setX(colorScaleRegionBounds.getMaxX() - (colorScalePositiveText.getLayoutBounds().getWidth() + 2));
                colorScalePositiveText.setY(colorScaleRegionBounds.getMinY() + colorScalePositiveText.getLayoutBounds().getHeight());

                colorScaleZeroText.setX(colorScaleRegionBounds.getMaxX() - 8);
                colorScaleZeroText.setY(colorScaleRegionBounds.getMinY() + (colorScaleRegionBounds.getHeight() / 2.) + (colorScaleZeroText.getLayoutBounds().getHeight() / 2.));

                colorScaleNegativeText.setX(colorScaleRegionBounds.getMaxX() - 8);
                colorScaleNegativeText.setY(colorScaleRegionBounds.getMaxY() - 4);
            }

//            colorScaleRegionRectangle.setX(colorScaleRegionBounds.getMinX());
//            colorScaleRegionRectangle.setY(colorScaleRegionBounds.getMinY());
//            colorScaleRegionRectangle.setWidth(colorScaleRegionBounds.getWidth());
//            colorScaleRegionRectangle.setHeight(colorScaleRegionBounds.getHeight());

            log.info("color scale width is " + colorScaleWidth);
        }


//
//        colorScaleZeroText.setX((colorScaleRegionBounds.getMinX() + (colorScaleRegionBounds.getWidth() / 2.)) - (colorScaleZeroText.getLayoutBounds().getWidth() / 2.));
//        colorScaleZeroText.setY(colorScaleRegionBounds.getMaxY() - 2);
//
//        colorScalePositiveText.setX(colorScaleRegionBounds.getMaxX() - (colorScalePositiveText.getLayoutBounds().getWidth() + 2));
//        colorScalePositiveText.setY(colorScaleZeroText.getY());
//
//        colorScaleNegativeText.setX(colorScaleRegionBounds.getMinX() + 2);
//        colorScaleNegativeText.setY(colorScaleZeroText.getY());

        double cellSize = plotRegionBounds.getWidth() / doubleColumns.size();

        for (int irow = 0; irow < cellRows.size(); irow++) {
            ArrayList<CorrelationMatrixCell> cells = cellRows.get(irow);
            double cellY = plotRegionBounds.getMinY() + (irow * cellSize);

            Text yColumnTitleText = yColumnTitles.get(irow);
            yColumnTitleText.setText(doubleColumns.get(irow).getName());
            fitColumnTitleToAxisSize(yColumnTitleText, yAxisRegionBounds.getWidth());
            yColumnTitleText.setX(yAxisRegionBounds.getMaxX() - 2. - yColumnTitleText.getLayoutBounds().getWidth());
            yColumnTitleText.setY(cellY + (cellSize / 2.));

            for (int icol = 0; icol < cells.size(); icol++) {
                double cellX = plotRegionBounds.getMinX() + (icol * cellSize);

                if (irow == 0) {
                    Text xColumnTitleText = xColumnTitles.get(icol);
                    xColumnTitleText.setText(doubleColumns.get(icol).getName());
                    fitColumnTitleToAxisSize(xColumnTitleText, xAxisRegionBounds.getHeight());
                    xColumnTitleText.relocate(cellX + (cellSize / 2.) - (xColumnTitleText.getLayoutBounds().getHeight() / 2.),
                            xAxisRegionBounds.getMaxY() - 2.);
//                    xColumnTitleText.setY(xAxisRegionBounds.getMaxY());
//                    xColumnTitleText.setX(cellX);
//                    xColumnTitleText.setX(cellX + (cellSize / 2.));
//                    xColumnTitleText.setY(xAxisRegionBounds.getMinY());
                }

                CorrelationMatrixCell cell = cells.get(icol);
                cell.getCellRectangle().setX(cellX);
                cell.getCellRectangle().setY(cellY);
                cell.getCellRectangle().setWidth(cellSize);
                cell.getCellRectangle().setHeight(cellSize);
            }
        }
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

    public Color getLabelsColor() {
        return textColor.get();
    }

    public void setLabelsColor(Color labelsColor) {
        this.textColor.set(labelsColor);
    }

    public ObjectProperty<Color> labelsColorProperty() {
        return textColor;
    }

    @Override
    public void dataTableReset(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableStatisticsChanged(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnExtentsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableColumnFocusExtentsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableNumHistogramBinsChanged(DataTable dataModel) {

    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataModel, Column column) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnSelectionAdded(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnSelectionRemoved(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableTuplesRemoved(DataTable dataModel, int numTuplesRemoved) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnDisabled(DataTable dataModel, Column disabledColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnEnabled(DataTable dataModel, Column enabledColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnOrderChanged(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataTableColumnNameChanged(DataTable dataModel, Column column) {
        clearView();
        initView();
        resizeView();
    }
}
