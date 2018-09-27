package gov.ornl.correlationview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
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
import java.util.DoubleSummaryStatistics;
import java.util.logging.Logger;

public class CorrelationMatrixView extends Region implements DataTableListener {
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public final static Color DEFAULT_ZERO_COLOR = Color.GHOSTWHITE;
    public final static Color DEFAULT_POSITIVE_COLOR = Color.DODGERBLUE;
    public final static Color DEFAULT_NEGATIVE_COLOR = Color.DARKRED;
    public final static Color DEFAULT_DIAGONAL_COLOR = Color.LIGHTGRAY;
    public final static double DEFAULT_TEXT_SIZE = 10.;
    public final static double DEFAULT_MAX_AXIS_SIZE = 50.;

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

    private int colorScaleSize = 30;

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

    public CorrelationMatrixView () {
        initialize();
        registerListeners();
    }

    public BooleanProperty showQueryCorrelationsProperty() { return showQueryCorrelations; }

    public void setShowQueryCorrelations(boolean enabled) {
        if (getShowQueryCorrelations() != enabled) {
            showQueryCorrelations.set(enabled);
        }
    }

    public boolean getShowQueryCorrelations() { return showQueryCorrelations.get(); }

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
        colorScaleRectangle.setStroke(Color.BLACK);
        colorScaleRectangle.setMouseTransparent(true);

        colorScaleNegativeText = new Text("-1");
        colorScaleNegativeText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScaleNegativeText.setFill(textColor.get());

        colorScalePositiveText = new Text("1");
        colorScalePositiveText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScalePositiveText.setFill(textColor.get());

        colorScaleZeroText = new Text("0");
        colorScaleZeroText.setFont(Font.font(DEFAULT_TEXT_SIZE));
        colorScaleZeroText.setFill(textColor.get());

        pane = new Pane();
        this.setBackground(new Background(new BackgroundFill(backgroundColor.get(), new CornerRadii(0), Insets.EMPTY)));
        pane.getChildren().addAll(cellGraphics, titleGraphics, colorScaleRectangle,
                colorScaleZeroText, colorScalePositiveText, colorScaleNegativeText);
//        pane.getChildren().addAll(cellGraphics, titleGraphics, viewRegionRectangle, plotRegionRectangle, xAxisRegionRectangle, yAxisRegionRectangle);
//        pane.getChildren().add(viewRegionRectangle);

        getChildren().add(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resizeView());
        heightProperty().addListener(o -> resizeView());

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
                    Color fillColor;
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
                yColumnTitleText.setTextOrigin(VPos.CENTER);
                yColumnTitles.add(yColumnTitleText);
                titleGraphics.getChildren().add(yColumnTitleText);

                ArrayList<CorrelationMatrixCell> cells = new ArrayList<>();

                for (int xColumnIndex = 0; xColumnIndex < doubleColumns.size(); xColumnIndex++) {
                    DoubleColumn xColumn = doubleColumns.get(xColumnIndex);

                    if (yColumnIndex == 0) {
                        Text xColumnTitleText = new Text(xColumn.getName());
//                        xColumnTitleText.setTextOrigin(VPos.BOTTOM);
                        xColumnTitleText.setTextOrigin(VPos.TOP);
//                        xColumnTitleText.setRotate(-90.);
                        xColumnTitleText.getTransforms().add(new Rotate(-90.));
                        xColumnTitles.add(xColumnTitleText);
                        titleGraphics.getChildren().add(xColumnTitleText);
                    }

                    double correlation = 0;
                    if (getShowQueryCorrelations() && dataTable.getActiveQuery().hasColumnSelections()) {
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

        double xAxisHeight = longestColumnTitle ;
        double yAxisWidth = longestColumnTitle;
        double plotRegionWidth = viewRegionBounds.getWidth() - yAxisWidth;
        double plotRegionHeight = viewRegionBounds.getHeight() - (xAxisHeight + (colorScaleSize + 4));
        double plotRegionSize = plotRegionWidth < plotRegionHeight ? plotRegionWidth : plotRegionHeight;
        double xAxisTopOffset = (viewRegionBounds.getHeight() - (plotRegionSize + xAxisHeight + (colorScaleSize + 4))) / 2.;
        double yAxisLeftOffset = ((viewRegionBounds.getWidth() - plotRegionSize) - yAxisWidth) / 2.;

        colorScaleRegionBounds = new BoundingBox(viewRegionBounds.getMinX() + yAxisLeftOffset + yAxisWidth,
                viewRegionBounds.getMinY() + (viewRegionBounds.getHeight() - colorScaleSize), plotRegionSize,
                colorScaleSize);
        colorScaleRegionRectangle.setX(colorScaleRegionBounds.getMinX());
        colorScaleRegionRectangle.setY(colorScaleRegionBounds.getMinY());
        colorScaleRegionRectangle.setWidth(colorScaleRegionBounds.getWidth());
        colorScaleRegionRectangle.setHeight(colorScaleRegionBounds.getHeight());

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

        colorScaleRectangle.setX(colorScaleRegionBounds.getMinX());
        colorScaleRectangle.setY(colorScaleRegionBounds.getMinY());
        colorScaleRectangle.setWidth(colorScaleRegionBounds.getWidth());
        colorScaleRectangle.setHeight(colorScaleRegionBounds.getHeight() - 12);

        Stop[] stops = new Stop[] { new Stop(0, negativeColor.get()), new Stop(0.5, zeroColor.get()),
                new Stop(1, positiveColor.get())};
        LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        colorScaleRectangle.setFill(linearGradient);

        colorScaleZeroText.setX((colorScaleRegionBounds.getMinX() + (colorScaleRegionBounds.getWidth() / 2.)) - (colorScaleZeroText.getLayoutBounds().getWidth() / 2.));
        colorScaleZeroText.setY(colorScaleRegionBounds.getMaxY() - 2);

        colorScalePositiveText.setX(colorScaleRegionBounds.getMaxX() - (colorScalePositiveText.getLayoutBounds().getWidth() + 2));
        colorScalePositiveText.setY(colorScaleZeroText.getY());

        colorScaleNegativeText.setX(colorScaleRegionBounds.getMinX() + 2);
        colorScaleNegativeText.setY(colorScaleZeroText.getY());


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
    public void dataModelReset(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelStatisticsChanged(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataTable dataModel) {

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
    public void dataModelColumnSelectionAdded(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelTuplesRemoved(DataTable dataModel, int numTuplesRemoved) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnDisabled(DataTable dataModel, Column disabledColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnEnabled(DataTable dataModel, Column enabledColumn) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnOrderChanged(DataTable dataModel) {
        clearView();
        initView();
        resizeView();
    }

    @Override
    public void dataModelColumnNameChanged(DataTable dataModel, Column column) {
        clearView();
        initView();
        resizeView();
    }
}
