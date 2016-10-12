package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by csg on 8/23/16.
 */
public class PCPAxis {
    public final static Logger log = LoggerFactory.getLogger(PCPAxis.class);

    public final static int DEFAULT_MAX_HISTOGRAM_BIN_WIDTH = 30;
    public final static Paint DEFAULT_HISTOGRAM_FILL = new Color(Color.DARKGRAY.getRed(), Color.DARKGRAY.getGreen(), Color.DARKGRAY.getBlue(), 0.8d);
    public final static Paint DEFAULT_QUERY_HISTOGRAM_FILL = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.8d);
    public final static Paint DEFAULT_HISTOGRAM_STROKE = Color.DARKGRAY;


    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;

    private DataModel dataModel;
    private Column column;
//    private int dataModelIndex;

    private double centerX;
    private Rectangle bounds;
    private double barTopY;
    private double barBottomY;
    private double focusTopY;
    private double focusBottomY;

    private double contextRegionHeight = DEFAULT_CONTEXT_HEIGHT;

    private Group graphicsGroup;
    private Line topCrossBarLine;
    private Line bottomCrossBarLine;
    private Line topFocusCrossBarLine;
    private Line bottomFocusCrossBarLine;

    private Rectangle verticalBar;

    // axis column name label
    private Text nameText;

    // value labels
    private Text maxValueText;
    private Text minValueText;
    private Text focusMaxValueText;
    private Text focusMinValueText;

    // axis relocation stuff
    private WritableImage dragImage;
    private ImageView dragImageView;
    private Group axisDraggingGraphicsGroup;

    // summary statistics objects
    private Group overallSummaryStatisticsGroup;
    private Rectangle overallDispersionRectangle;
    private Line overallTypicalLine;
    private Group querySummaryStatisticsGroup;
    private Rectangle queryDispersionRectangle;
    private Line queryTypicalLine;

    // histogram bin rectangles
    private Group histogramBinRectangleGroup;
    private ArrayList<Rectangle> histogramBinRectangleList;
    private Group queryHistogramBinRectangleGroup;
    private ArrayList<Rectangle> queryHistogramBinRectangleList;

    private Paint histogramFill = DEFAULT_HISTOGRAM_FILL;
    private Paint queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;
    private Paint histogramStroke = DEFAULT_HISTOGRAM_STROKE;

    private int maxHistogramBinWidth = DEFAULT_MAX_HISTOGRAM_BIN_WIDTH;

    private Pane pane;

    private ArrayList<PCPAxisSelection> axisSelectionList = new ArrayList<>();

    // dragging variables
    Point2D dragStartPoint;
    Point2D dragEndPoint;
    PCPAxisSelection draggingSelection;
    boolean dragging = false;

    private PCPView pcpView;

    public PCPAxis(PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        this.pcpView = pcpView;
        this.column = column;
//        this.dataModelIndex = dataModelIndex;
        this.dataModel = dataModel;
        this.pane = pane;

        centerX = 0d;
        bounds = new Rectangle();
        barTopY = 0d;
        barBottomY = 0d;
        focusTopY = 0d;
        focusBottomY = 0d;

        nameText = new Text(column.getName());
        // TODO: bind the name to the column name property
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(column.nameProperty());
        Tooltip.install(nameText, tooltip);
        nameText.setFont(new Font(DEFAULT_NAME_TEXT_SIZE));
        nameText.setSmooth(true);

//        columnNameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));
        minValueText = new Text();
        minValueText.textProperty().bindBidirectional(column.minValueProperty(), new NumberStringConverter());
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
        maxValueText.textProperty().bindBidirectional(column.maxValueProperty(), new NumberStringConverter());
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        focusMinValueText = new Text();
        focusMinValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusMinValueText.setSmooth(true);

        focusMaxValueText = new Text();
        focusMaxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusMaxValueText.setSmooth(true);

        verticalBar = new Rectangle();
        verticalBar.setStroke(Color.DARKGRAY);
        verticalBar.setFill(Color.WHITESMOKE);
        verticalBar.setSmooth(true);
        verticalBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        topCrossBarLine = makeLine();
        bottomCrossBarLine = makeLine();
        topFocusCrossBarLine = makeLine();
        bottomFocusCrossBarLine = makeLine();

        graphicsGroup = new Group(verticalBar, topCrossBarLine, bottomCrossBarLine, topFocusCrossBarLine,
                bottomFocusCrossBarLine, nameText, minValueText, maxValueText, focusMinValueText, focusMaxValueText);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.setFill(Color.LIGHTSTEELBLUE);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.setStroke(Color.DARKGRAY.darker());
        overallSummaryStatisticsGroup = new Group(overallDispersionRectangle, overallTypicalLine);
        overallSummaryStatisticsGroup.setMouseTransparent(true);
        graphicsGroup.getChildren().add(overallSummaryStatisticsGroup);

        queryDispersionRectangle = new Rectangle();
        queryDispersionRectangle.setFill(Color.STEELBLUE);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.setStroke(Color.BLACK);
        querySummaryStatisticsGroup = new Group(queryDispersionRectangle, queryTypicalLine);
        querySummaryStatisticsGroup.setMouseTransparent(true);

        registerListeners();
    }

    public Group getHistogramBinRectangleGroup() { return histogramBinRectangleGroup; }
    public Group getQueryHistogramBinRectangleGroup() { return queryHistogramBinRectangleGroup; }

    private void makeAxisDraggingGraphicsGroup() {
        Rectangle rectangle = new Rectangle(verticalBar.getX(), verticalBar.getY(), verticalBar.getWidth(), verticalBar.getHeight());
        rectangle.setStroke(verticalBar.getStroke());
        rectangle.setFill(verticalBar.getFill());

        axisDraggingGraphicsGroup = new Group();
        axisDraggingGraphicsGroup.getChildren().add(rectangle);
    }

    private void registerListeners() {
        PCPAxis thisPCPAxis = this;

        nameText.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
//                dragAnchorX = event.getSceneX();

            }
        });

        nameText.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!dragging) {
                    dragging = true;
                    SnapshotParameters snapshotParameters = new SnapshotParameters();
                    snapshotParameters.setFill(Color.TRANSPARENT);
                    dragImage = pane.snapshot(snapshotParameters, null);

                    dragImageView = new ImageView(dragImage);
                    dragImageView.setViewport(new Rectangle2D(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
                    dragImageView.setX(centerX - (dragImageView.getLayoutBounds().getWidth() / 2d));
                    dragImageView.setY(graphicsGroup.getLayoutY() + 5);
//                    log.debug("GraphicsGroup.getLayout() = " + graphicsGroup.getLayoutBounds());

                    // blur everything except the drag image view
//                    for (Node node : pane.getChildren()) {
//                        node.setEffect(new GaussianBlur());
//                    }

//                    makeAxisDraggingGraphicsGroup();
//                    pane.getChildren().add(axisDraggingGraphicsGroup);
                    dragImageView.setEffect(new DropShadow());
                    pane.getChildren().add(dragImageView);
                }

//                axisDraggingGraphicsGroup.relocate(event.getX() - axisDraggingGraphicsGroup.getLayoutBounds().getWidth()/2d, verticalBar.getY() + 10);
//                log.debug("event.getX()= " + event.getX() + " event.getSceneX()= " + event.getSceneX() + " event.getScreenX()= " + event.getScreenX());
//                log.debug("dragImage.getFitWidth() = " + dragImageView.getFitWidth() + " dragImage.getWidth() = " + dragImage.getWidth());
//                log.debug("dragImageView.getLayoutBounds().getWidth() = " + dragImageView.getLayoutBounds().getWidth());
                dragImageView.setX(event.getX() - (dragImageView.getLayoutBounds().getWidth() / 2d));
            }
        });

        nameText.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (dragging) {
                    pane.getChildren().remove(dragImageView);
                    for (Node node : pane.getChildren()) {
                        node.setEffect(null);
                    }
                    dragging = false;

                    // calculate the new index position for the column associated with this axis
                    double dragImageCenterX = dragImageView.getX() + (dragImageView.getLayoutBounds().getWidth() / 2.);
                    int newColumnIndex = (int) dragImageCenterX / pcpView.getAxisSpacing();
                    newColumnIndex = GraphicsUtil.constrain(newColumnIndex, 0, dataModel.getColumnCount() - 1);

                    log.debug("newColumnIndex is " + newColumnIndex);
                    if (!(newColumnIndex == dataModel.getColumnIndex(column))) {
                        dataModel.changeColumnOrder(getColumn(), newColumnIndex);
                    }
                }
            }
        });

        verticalBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());
            }
        });

        verticalBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragging = true;
                dragEndPoint = new Point2D(event.getX(), event.getY());

                double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
                double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

                selectionMaxY = selectionMaxY < getFocusTopY() ? getFocusTopY() : selectionMaxY;
                selectionMinY = selectionMinY > getFocusBottomY() ? getFocusBottomY() : selectionMinY;

                if (selectionMaxY == getFocusTopY()) {
                    log.debug("selectionMaxY = " + selectionMaxY + " getFocusTopY() = " + getFocusTopY());
                }
                double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getFocusTopY(), getFocusBottomY(),
                        column.getSummaryStats().getMax(), column.getSummaryStats().getMin());
                double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getFocusTopY(), getFocusBottomY(),
                        column.getSummaryStats().getMax(), column.getSummaryStats().getMin());

                if (draggingSelection == null) {
                    ColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(column, minSelectionValue, maxSelectionValue);
                    draggingSelection = new PCPAxisSelection(thisPCPAxis, selectionRange, selectionMinY, selectionMaxY, pane, dataModel);
                } else {
                    draggingSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
                }

            }
        });

        verticalBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (draggingSelection != null) {
                    axisSelectionList.add(draggingSelection);
                    dragging = false;
                    draggingSelection = null;
                    dataModel.setQueriedTuples();
                }
            }
        });
    }

    public ArrayList<PCPAxisSelection> getAxisSelectionList() { return axisSelectionList; }

    private Line makeLine() {
        Line line = new Line();
        line.setStroke(Color.DARKGRAY);
        line.setSmooth(true);
        line.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        return line;
    }

    public void setHighlighted(boolean highlighted) {
        if (highlighted) {
            nameText.setFont(Font.font(nameText.getFont().getFamily(), FontWeight.BOLD, DEFAULT_TEXT_SIZE));
            nameText.setFill(Color.BLUE);
        } else {
            nameText.setFont(Font.font(nameText.getFont().getFamily(), FontWeight.NORMAL, DEFAULT_TEXT_SIZE));
            nameText.setFill(Color.BLACK);
        }
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public Text getNameText() { return nameText; }

    public void layout(double centerX, double topY, double width, double height) {
        this.centerX = centerX;
        double left = centerX - (width / 2.);
        bounds = new Rectangle(left, topY, width, height);
        barTopY = topY + DEFAULT_NAME_LABEL_HEIGHT;
        barBottomY = bounds.getY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
        focusTopY = topY + DEFAULT_NAME_LABEL_HEIGHT + contextRegionHeight;
        focusBottomY = barBottomY - contextRegionHeight;

        verticalBar.setX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        verticalBar.setY(barTopY);
        verticalBar.setWidth(DEFAULT_BAR_WIDTH);
        verticalBar.setHeight(barBottomY - barTopY);

        topCrossBarLine.setStartY(barTopY);
        topCrossBarLine.setEndY(barTopY);
        topCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        topCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        bottomCrossBarLine.setStartY(barBottomY);
        bottomCrossBarLine.setEndY(barBottomY);
        bottomCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        bottomCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        topFocusCrossBarLine.setStartY(focusTopY);
        topFocusCrossBarLine.setEndY(focusTopY);
        topFocusCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        topFocusCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        bottomFocusCrossBarLine.setStartY(focusBottomY);
        bottomFocusCrossBarLine.setEndY(focusBottomY);
        bottomFocusCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        bottomFocusCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

//        nameText.setFont(new Font(DEFAULT_TEXT_SIZE));
//        adjustTextSize(nameText, width, DEFAULT_TEXT_SIZE);
        nameText.setX(bounds.getX() + ((width - nameText.getLayoutBounds().getWidth()) / 2.));
        nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
//        nameText.setY(barTopY - (DEFAULT_NAME_LABEL_HEIGHT / 2.));
//        nameText.setRotate(-10.);

        minValueText.setX(bounds.getX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(barBottomY + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(bounds.getX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(barTopY - 4d);

        if (!axisSelectionList.isEmpty()) {
            for (PCPAxisSelection pcpAxisSelection : axisSelectionList) {
                pcpAxisSelection.relayout();
            }
        }

        if (!dataModel.isEmpty()) {
            // layout summary statistics
            double typicalValueY = GraphicsUtil.mapValue(column.getSummaryStats().getMean(), column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
            overallTypicalLine.setStartX(bottomCrossBarLine.getStartX());
            overallTypicalLine.setEndX(bottomCrossBarLine.getEndX());
            overallTypicalLine.setStartY(typicalValueY);
            overallTypicalLine.setEndY(typicalValueY);

            double topValue = column.getSummaryStats().getMean() + column.getSummaryStats().getStandardDeviation();
            double overallDispersionTop = GraphicsUtil.mapValue(topValue, column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
            overallDispersionTop = overallDispersionTop < focusTopY ? focusTopY : overallDispersionTop;
            double bottomValue = column.getSummaryStats().getMean() - column.getSummaryStats().getStandardDeviation();
            double overallDispersionBottom = GraphicsUtil.mapValue(bottomValue, column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
            overallDispersionBottom = overallDispersionBottom > focusBottomY ? focusBottomY : overallDispersionBottom;
            overallDispersionRectangle.setX(overallTypicalLine.getStartX());
            overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
            overallDispersionRectangle.setY(overallDispersionTop);
            overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);

            // layout histogram bin information
            Histogram histogram = column.getSummaryStats().getHistogram();

            double binHeight = (getFocusBottomY() - getFocusTopY()) / histogram.getNumBins();
            histogramBinRectangleList = new ArrayList<>();

            if (histogramBinRectangleGroup != null) {
                pane.getChildren().remove(histogramBinRectangleGroup);
            }

            histogramBinRectangleGroup = new Group();

            for (int i = 0; i < histogram.getNumBins(); i++) {
                double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                double x = left + ((width - binWidth) / 2.);
                Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                rectangle.setStroke(histogramStroke);
                rectangle.setFill(histogramFill);
                histogramBinRectangleList.add(rectangle);
                histogramBinRectangleGroup.getChildren().add(rectangle);
            }

            queryHistogramBinRectangleList = new ArrayList<>();
            if (queryHistogramBinRectangleGroup != null) {
                pane.getChildren().remove(queryHistogramBinRectangleGroup);
            }
            queryHistogramBinRectangleGroup = new Group();

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                // layer query summary statistics
                SummaryStats queryColumnSummaryStats = dataModel.getActiveQuery().getColumnQuerySummaryStats(column);
                typicalValueY = GraphicsUtil.mapValue(queryColumnSummaryStats.getMean(), column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
                queryTypicalLine.setStartX(centerX - 2.);
                queryTypicalLine.setEndX(centerX + 2.);
                queryTypicalLine.setStartY(typicalValueY);
                queryTypicalLine.setEndY(typicalValueY);

                double value = queryColumnSummaryStats.getMean() + queryColumnSummaryStats.getStandardDeviation();
                double queryDispersionTop = GraphicsUtil.mapValue(value, column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
                queryDispersionTop = queryDispersionTop < focusTopY ? focusTopY : queryDispersionTop;
                value = queryColumnSummaryStats.getMean() - queryColumnSummaryStats.getStandardDeviation();
                double queryDispersionBottom = GraphicsUtil.mapValue(value, column.getSummaryStats().getMin(), column.getSummaryStats().getMax(), getFocusBottomY(), getFocusTopY());
                queryDispersionBottom = queryDispersionBottom > focusBottomY ? focusBottomY : queryDispersionBottom;
                queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                queryDispersionRectangle.setY(queryDispersionTop);
                queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);

                if (!graphicsGroup.getChildren().contains(querySummaryStatisticsGroup)) {
                    graphicsGroup.getChildren().add(querySummaryStatisticsGroup);
                }

                // layout query histogram bins
                Histogram queryHistogram = dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getHistogram();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    if (queryHistogram.getBinCount(i) > 0) {
                        double y = getFocusTopY() + ((histogram.getNumBins() - i - 1) * binHeight);
                        double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                        double x = left + ((width - binWidth) / 2.);
                        Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                        rectangle.setStroke(histogramStroke);
                        rectangle.setFill(queryHistogramFill);
                        queryHistogramBinRectangleList.add(rectangle);
                        queryHistogramBinRectangleGroup.getChildren().add(rectangle);
                    }
                }
            } else {
                graphicsGroup.getChildren().remove(querySummaryStatisticsGroup);
            }
        }
    }

    public double getBarLeftX() { return verticalBar.getX(); }
    public double getBarRightX() { return verticalBar.getX() + verticalBar.getWidth(); }

    public double getCenterX() { return centerX; }
    public Rectangle getBounds() { return bounds; }

    public Rectangle getVerticalBar() { return verticalBar; }

    public Column getColumn() { return column; }
    public int getColumnDataModelIndex() { return dataModel.getColumnIndex(getColumn()); }

    public double getFocusTopY() { return focusTopY; }
    public double getFocusBottomY() { return focusBottomY; }
    public double getUpperContextTopY() { return barTopY; }
    public double getUpperContextBottomY() { return focusTopY; }
    public double getLowerContextTopY() { return focusBottomY; }
    public double getLowerContextBottomY() { return barBottomY; }

    public double getVerticalBarTop() { return barTopY; }
    public double getVerticalBarBottom() { return barBottomY; }

    private void adjustTextSize(Text text, double maxWidth, double fontSize) {
        String fontName = text.getFont().getName();
        while (text.getLayoutBounds().getWidth() > maxWidth && fontSize > 0) {
            fontSize -= 0.005;
            text.setFont(new Font(fontName, fontSize));
        }
    }
}
