package gov.ornl.pcpview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.DoubleColumnSummaryStats;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;

public abstract class PCPAxis {
    public final static int DEFAULT_MAX_HISTOGRAM_BIN_WIDTH = 30;
    public final static Color DEFAULT_HISTOGRAM_FILL = new Color(Color.LIGHTSTEELBLUE.getRed(), Color.LIGHTSTEELBLUE.getGreen(), Color.LIGHTSTEELBLUE.getBlue(), 0.8d);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.8d);
    public final static Color DEFAULT_HISTOGRAM_STROKE = Color.DARKGRAY;
    public final static Color DEFAULT_CORRELATION_INDICATOR_FILL_COLOR = DEFAULT_QUERY_HISTOGRAM_FILL;
    public final static Color DEFAULT_CORRELATION_INDICATOR_HOVER_FILL_COLOR = Color.BLACK;

    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;

    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 14d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    public final static double DEFAULT_CORRELATION_INDICATOR_HEIGHT = 24d;
    public final static double DEFAULT_CORRELATION_INDICATOR_WIDTH = 4.;
    public final static double DEFAULT_CORRELATION_INDICATOR_PADDING = 1.;

    protected DataModel dataModel;
    protected Column column;

    protected double centerX;
    protected Rectangle bounds;

    protected Group graphicsGroup;

    protected Text nameText;
    protected DoubleProperty nameTextRotation;
    protected BooleanProperty highlighted;

    protected Color labelColor = DEFAULT_LABEL_COLOR;

    protected Pane pane;

    protected PCPView pcpView;

    private double barTopY;
    private double barBottomY;
    private double focusTopY;
    private double focusBottomY;

    private double contextRegionHeight = DEFAULT_CONTEXT_HEIGHT;

    //    private Group graphicsGroup;
    private Line topCrossBarLine;
    private Line bottomCrossBarLine;
    private Line topFocusCrossBarLine;
    private Line bottomFocusCrossBarLine;

    private Rectangle axisBar;

    private Rectangle correlationCoefficientIndicatorRectangle;
    private Line correlationCoefficientIndicatorZeroLine;
//    private ArrayList<Line> correlationCoefficientIndicatorList = new ArrayList<>();
    private Group correlationCoefficientIndicatorGroup = new Group();

    // axis column name label
//    private Text nameText;
//    private DoubleProperty nameTextRotation;

    // value labels
    protected Text maxValueText;
    protected Text minValueText;
    protected Text focusMaxValueText;
    protected Text focusMinValueText;

    // axis relocation stuff
    private WritableImage dragImage;
    private ImageView dragImageView;
    private Group axisDraggingGraphicsGroup;

    protected Color histogramFill = DEFAULT_HISTOGRAM_FILL;
    protected Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    protected Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;
    protected double maxHistogramBinWidth = DEFAULT_MAX_HISTOGRAM_BIN_WIDTH;

    private ArrayList<PCPAxisSelection> axisSelectionList = new ArrayList<>();

    // dragging variables
    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging = false;

    private Line draggingTopCrossBarLine;
    private Line draggingBottomCrossBarLine;
    private Line draggingTopFocusCrossBarLine;
    private Line draggingBottomFocusCrossBarLine;
    private Rectangle draggingAxisBar;
    private Text draggingNameText;

    public PCPAxis (PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        this.pcpView = pcpView;
        this.column = column;
        this.dataModel = dataModel;
        this.pane = pane;

        centerX = 0d;
        bounds = new Rectangle();
        barTopY = 0d;
        barBottomY = 0d;
        focusTopY = 0d;
        focusBottomY = 0d;

        nameTextRotation = new SimpleDoubleProperty(0.0);
        highlighted = new SimpleBooleanProperty(false);

        nameText = new Text(column.getName());
//        nameText.textProperty().bindBidirectional(column.nameProperty());
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(column.nameProperty());
        Tooltip.install(nameText, tooltip);
        nameText.setFont(new Font(DEFAULT_NAME_TEXT_SIZE));
        nameText.setSmooth(true);
        nameText.setFill(labelColor);
        nameText.rotateProperty().bindBidirectional(nameTextRotation);

        correlationCoefficientIndicatorRectangle = new Rectangle();
        correlationCoefficientIndicatorRectangle.setStroke(Color.BLACK);
        correlationCoefficientIndicatorRectangle.setFill(Color.TRANSPARENT);
        correlationCoefficientIndicatorRectangle.setHeight(DEFAULT_CORRELATION_INDICATOR_HEIGHT);

        correlationCoefficientIndicatorZeroLine = new Line();
        correlationCoefficientIndicatorZeroLine.setStroke(Color.DARKGRAY);
        correlationCoefficientIndicatorZeroLine.setStrokeWidth(1.);
        correlationCoefficientIndicatorGroup.getChildren().add(correlationCoefficientIndicatorZeroLine);

        minValueText = new Text();
//        minValueText.textProperty().bindBidirectional(column.minValueProperty(), new NumberStringConverter());
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
//        maxValueText.textProperty().bindBidirectional(column.maxValueProperty(), new NumberStringConverter());
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        focusMinValueText = new Text();
        focusMinValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusMinValueText.setSmooth(true);

        focusMaxValueText = new Text();
        focusMaxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        focusMaxValueText.setSmooth(true);

        axisBar = new Rectangle();
        axisBar.setStroke(Color.DARKGRAY);
        axisBar.setFill(Color.WHITESMOKE);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        topCrossBarLine = makeLine();
        bottomCrossBarLine = makeLine();
        topFocusCrossBarLine = makeLine();
        bottomFocusCrossBarLine = makeLine();

        graphicsGroup = new Group(nameText, axisBar, topCrossBarLine, bottomCrossBarLine, topFocusCrossBarLine,
                bottomFocusCrossBarLine, minValueText, maxValueText, focusMinValueText, focusMaxValueText);

        if (getColumn() instanceof DoubleColumn) {
            graphicsGroup.getChildren().addAll(correlationCoefficientIndicatorZeroLine, correlationCoefficientIndicatorGroup);
        }

        registerListeners();
    }

    public ArrayList<PCPAxisSelection> getAxisSelectionList() {
        return axisSelectionList;
    }

    protected abstract void handleAxisBarMousePressed();
    protected abstract void handleAxisBarMouseDragged(MouseEvent event);
    protected abstract void handleAxisBarMouseReleased();
    public abstract Group getHistogramBinRectangleGroup();
    public abstract Group getQueryHistogramBinRectangleGroup();

    private void makeAxisDraggingGraphicsGroup() {
        draggingAxisBar = new Rectangle(getAxisBar().getX(), getAxisBar().getY(), getAxisBar().getWidth(), getAxisBar().getHeight());
        draggingAxisBar.setStroke(getAxisBar().getStroke());
        draggingAxisBar.setFill(getAxisBar().getFill());

        draggingBottomCrossBarLine = new Line(bottomCrossBarLine.getStartX(), bottomCrossBarLine.getStartY(),
                bottomCrossBarLine.getEndX(), bottomCrossBarLine.getEndY());
        draggingBottomCrossBarLine.setStroke(bottomCrossBarLine.getStroke());
        draggingTopCrossBarLine = new Line(topCrossBarLine.getStartX(), topCrossBarLine.getStartY(),
                topCrossBarLine.getEndX(), topCrossBarLine.getEndY());
        draggingTopCrossBarLine.setStroke(topCrossBarLine.getStroke());
        draggingBottomFocusCrossBarLine = new Line(bottomFocusCrossBarLine.getStartX(), bottomFocusCrossBarLine.getStartY(),
                bottomFocusCrossBarLine.getEndX(), bottomFocusCrossBarLine.getEndY());
        draggingBottomFocusCrossBarLine.setStroke(bottomFocusCrossBarLine.getStroke());
        draggingTopFocusCrossBarLine = new Line(topFocusCrossBarLine.getStartX(), topFocusCrossBarLine.getStartY(),
                topFocusCrossBarLine.getEndX(), topFocusCrossBarLine.getEndY());
        draggingTopFocusCrossBarLine.setStroke(topFocusCrossBarLine.getStroke());

        draggingNameText = new Text(nameText.getText());
        draggingNameText.setX(nameText.getX());
        draggingNameText.setY(nameText.getY());
        draggingNameText.setFont(nameText.getFont());

        axisDraggingGraphicsGroup = new Group(draggingNameText, draggingAxisBar, draggingBottomCrossBarLine,
                draggingTopCrossBarLine, draggingBottomFocusCrossBarLine, draggingTopFocusCrossBarLine);
        axisDraggingGraphicsGroup.setTranslateY(5);
    }

    private void registerListeners() {
        nameText.textProperty().addListener((observable, oldValue, newValue) -> {
            nameText.setX(bounds.getX() + ((bounds.getWidth() - nameText.getLayoutBounds().getWidth()) / 2.));
            nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
        });

        nameText.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (dataModel.getHighlightedColumn() == getColumn()) {
                    dataModel.setHighlightedColumn(null);
                } else {
                    dataModel.setHighlightedColumn(getColumn());
                }
            }
        });

        nameText.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!dragging) {
                    dragging = true;
                    makeAxisDraggingGraphicsGroup();
                    axisDraggingGraphicsGroup.setEffect(new DropShadow());
                    pane.getChildren().add(axisDraggingGraphicsGroup);

                    dragStartPoint = new Point2D(event.getX(), event.getY());
                    /*
                    SnapshotParameters snapshotParameters = new SnapshotParameters();
                    snapshotParameters.setFill(Color.TRANSPARENT);
                    dragImage = pane.snapshot(snapshotParameters, null);

                    dragImageView = new ImageView(dragImage);
                    dragImageView.setViewport(new Rectangle2D(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
                    dragImageView.setX(centerX - (dragImageView.getLayoutBounds().getWidth() / 2d));
                    dragImageView.setY(graphicsGroup.getLayoutY() + 5);
//                    log.debug("GraphicsGroup.getLayout() = " + graphicsGroup.getLayoutBounds());

                    // blur everything except the drag image view
                    for (Node node : pane.getChildren()) {
                        node.setEffect(new GaussianBlur());
                    }

//                    makeAxisDraggingGraphicsGroup();
//                    pane.getChildren().add(axisDraggingGraphicsGroup);
                    dragImageView.setEffect(new DropShadow());
                    pane.getChildren().add(dragImageView);
                    */
                }

                dragEndPoint = new Point2D(event.getX(), event.getY());
                axisDraggingGraphicsGroup.setTranslateX(event.getX() - dragStartPoint.getX());
//                draggingAxisBar.setX(event.getX() - draggingAxisBar.getWidth()/2d);

//                axisDraggingGraphicsGroup.setTranslateX(event.getX());

//                graphicsGroup.setTranslateX(event.getX());

//                axisDraggingGraphicsGroup.relocate(event.getX() - axisDraggingGraphicsGroup.getLayoutBounds().getWidth()/2d, verticalBar.getY() + 10);
//                log.debug("event.getX()= " + event.getX() + " event.getSceneX()= " + event.getSceneX() + " event.getScreenX()= " + event.getScreenX());
//                log.debug("dragImage.getFitWidth() = " + dragImageView.getFitWidth() + " dragImage.getWidth() = " + dragImage.getWidth());
//                log.debug("dragImageView.getLayoutBounds().getWidth() = " + dragImageView.getLayoutBounds().getWidth());
//                dragImageView.setX(event.getX() - (dragImageView.getLayoutBounds().getWidth() / 2d));
            }
        });

        nameText.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    final ContextMenu contextMenu = new ContextMenu();
                    MenuItem hideMenuItem = new MenuItem("Hide Axis");
                    MenuItem closeMenuItem = new MenuItem("Close Popup");
                    contextMenu.getItems().addAll(hideMenuItem, closeMenuItem);
                    hideMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            dataModel.disableColumn(column);
                        }
                    });
                    closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            contextMenu.hide();
                        }
                    });
                    contextMenu.show(pcpView, event.getScreenX(), event.getScreenY());
                }
            }
        });

        nameText.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (dragging) {
                    pane.getChildren().remove(axisDraggingGraphicsGroup);

                    pane.getChildren().remove(dragImageView);

                    dragging = false;

                    // calculate the new index position for the column associated with this axis
//                    double dragImageCenterX = dragImageView.getX() + (dragImageView.getLayoutBounds().getWidth() / 2.);
//                    int newColumnIndex = (int) dragImageCenterX / pcpView.getAxisSpacing();
                    int newColumnIndex = (int)dragEndPoint.getX() / pcpView.getAxisSpacing();
                    newColumnIndex = GraphicsUtil.constrain(newColumnIndex, 0, dataModel.getColumnCount() - 1);

                    if (!(newColumnIndex == dataModel.getColumnIndex(column))) {
                        dataModel.changeColumnOrder(getColumn(), newColumnIndex);
                    }
                }
            }
        });

        axisBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());
                handleAxisBarMousePressed();
            }
        });

        axisBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleAxisBarMouseDragged(event);
            }
        });

        axisBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleAxisBarMouseReleased();
            }
        });

        highlighted.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                nameText.setFont(Font.font(nameText.getFont().getFamily(), FontWeight.BOLD, DEFAULT_NAME_TEXT_SIZE));
                nameText.setEffect(new DropShadow());
            } else {
                nameText.setFont(Font.font(nameText.getFont().getFamily(), FontWeight.NORMAL, DEFAULT_NAME_TEXT_SIZE));
                nameText.setEffect(null);
            }
        });
    }

    public boolean isHighlighted() {
        return highlightedProperty().get();
    }

    public void setHighlighted(boolean highlighted) {
        if (isHighlighted() != highlighted) {
            highlightedProperty().set(highlighted);
        }
    }

    public BooleanProperty highlightedProperty() {
        return highlighted;
    }

    public void layout(double center, double top, double width, double height) {
        this.centerX = center;
        double left = centerX - (width / 2.);
        bounds = new Rectangle(left, top, width, height);

        nameText.setText(column.getName());
        if (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                nameText.setText(nameText.getText().substring(0, nameText.getText().length() - 1));
            }
        }
        nameText.setX(bounds.getX() + ((width - nameText.getLayoutBounds().getWidth()) / 2.));
        nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
        nameText.setRotate(getNameTextRotation());

        correlationCoefficientIndicatorRectangle.setY(nameText.getLayoutBounds().getMaxY() + 4);
        correlationCoefficientIndicatorRectangle.setX(bounds.getLayoutBounds().getMinX());
        correlationCoefficientIndicatorRectangle.setWidth(bounds.getLayoutBounds().getWidth());

        barTopY = correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY() + minValueText.getLayoutBounds().getHeight() + 4;
        barBottomY = bounds.getY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
        focusTopY = barTopY + contextRegionHeight;
        focusBottomY = barBottomY - contextRegionHeight;
//        barTopY = top + DEFAULT_NAME_LABEL_HEIGHT;
//        barBottomY = bounds.getY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
//        focusTopY = top + DEFAULT_NAME_LABEL_HEIGHT + contextRegionHeight;
//        focusBottomY = barBottomY - contextRegionHeight;

        minValueText.setX(bounds.getX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(barBottomY + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(bounds.getX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(barTopY - 4d);

        maxHistogramBinWidth = bounds.getWidth() / 2;

        axisBar.setX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        axisBar.setY(barTopY);
        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setHeight(barBottomY - barTopY);

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

        if (!axisSelectionList.isEmpty()) {
            for (PCPAxisSelection pcpAxisSelection : axisSelectionList) {
                pcpAxisSelection.relayout();
            }
        }

        // create graphics for correlation coefficient indicators
        if (this instanceof PCPDoubleAxis) {
            double correlationIndicatorRectangleWidth = (pcpView.getAxisCount() * DEFAULT_CORRELATION_INDICATOR_WIDTH) +
                    ((pcpView.getAxisCount() - 1) * DEFAULT_CORRELATION_INDICATOR_PADDING);
            double correlationIndicatorRectangleLeft = centerX - (correlationIndicatorRectangleWidth / 2.);
            double correlationIndicatorRectangleMiddleY = correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY() +
                    (correlationCoefficientIndicatorRectangle.getLayoutBounds().getHeight()/2.);

            correlationCoefficientIndicatorRectangle.setX(correlationIndicatorRectangleLeft);
            correlationCoefficientIndicatorRectangle.setWidth(correlationIndicatorRectangleWidth);
            correlationCoefficientIndicatorZeroLine.setStartX(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinX());
            correlationCoefficientIndicatorZeroLine.setStartY(correlationIndicatorRectangleMiddleY);
            correlationCoefficientIndicatorZeroLine.setEndX(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxX());
            correlationCoefficientIndicatorZeroLine.setEndY(correlationIndicatorRectangleMiddleY);

            correlationCoefficientIndicatorGroup.getChildren().clear();

            for (int iaxis = 0; iaxis < pcpView.getAxisCount(); iaxis++) {
                PCPAxis pcpAxis = pcpView.getAxis(iaxis);
                if (pcpAxis instanceof PCPDoubleAxis) {
                    double x = correlationIndicatorRectangleLeft + (iaxis * DEFAULT_CORRELATION_INDICATOR_WIDTH) +
                            (iaxis * DEFAULT_CORRELATION_INDICATOR_PADDING);

                    Rectangle rectangle = new Rectangle(x, 0, DEFAULT_CORRELATION_INDICATOR_WIDTH, 0);
//                    rectangle.setStroke(DEFAULT_CORRELATION_INDICATOR_FILL_COLOR);

                    if (this == pcpAxis) {
                        rectangle.setY(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY());
                        rectangle.setHeight(correlationCoefficientIndicatorRectangle.getLayoutBounds().getHeight());
                        rectangle.setFill(DEFAULT_CORRELATION_INDICATOR_FILL_COLOR.grayscale());
                        Tooltip.install(rectangle, new Tooltip(getColumn().getName() + " Correlation Indicators"));
                    } else {
                        double corrCoef;
                        if (dataModel.getActiveQuery().hasColumnSelections()) {
                            corrCoef = ((DoubleColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn())).getCorrelationCoefficientList().get(iaxis);
                        } else {
                            corrCoef = ((DoubleColumnSummaryStats) getColumn().getStatistics()).getCorrelationCoefficientList().get(iaxis);
                        }

                        if (corrCoef >= 0) {
                            double topY = GraphicsUtil.mapValue(corrCoef, 0d, 1d,
                                    correlationIndicatorRectangleMiddleY, correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY());
                            rectangle.setY(topY);
                            rectangle.setHeight(correlationIndicatorRectangleMiddleY - topY);
                        } else {
                            double bottomY = GraphicsUtil.mapValue(corrCoef, 0d, -1d,
                                    correlationIndicatorRectangleMiddleY, correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY());
                            rectangle.setY(correlationIndicatorRectangleMiddleY);
                            rectangle.setHeight(bottomY - correlationIndicatorRectangleMiddleY);
                        }

                        rectangle.setFill(DEFAULT_CORRELATION_INDICATOR_FILL_COLOR);
                        Tooltip.install(rectangle,
                                new Tooltip(this.column.getName() + " / " + pcpAxis.getColumn().getName() + " correlation = " + corrCoef));
                    }

                    rectangle.setOnMouseEntered(event -> {
                        rectangle.setStroke(Color.BLACK);
                    });

                    rectangle.setOnMouseExited(event -> {
                        rectangle.setStroke(null);
                    });

                    correlationCoefficientIndicatorGroup.getChildren().add(rectangle);
                }
            }
        }
/*
        double corrWidth = DEFAULT_CORRELATION_INDICATOR_STROKE_SPACING * (pcpView.getAxisCount() - 1);
        double corrLeft = centerX - ( corrWidth / 2);
        double corrMiddleY = correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY() +
                (correlationCoefficientIndicatorRectangle.getLayoutBounds().getHeight()/2.);
        correlationCoefficientIndicatorRectangle.setX(corrLeft);
        correlationCoefficientIndicatorRectangle.setWidth(corrWidth);
        correlationCoefficientIndicatorZeroLine.setStartX(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinX());
        correlationCoefficientIndicatorZeroLine.setStartY(corrMiddleY);
        correlationCoefficientIndicatorZeroLine.setEndX(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxX());
        correlationCoefficientIndicatorZeroLine.setEndY(corrMiddleY);

//        correlationCoefficientIndicatorList.clear();
        correlationCoefficientIndicatorGroup.getChildren().clear();
        if (this instanceof PCPDoubleAxis) {
            for (int iaxis = 0; iaxis < pcpView.getAxisCount(); iaxis++) {
                PCPAxis pcpAxis = pcpView.getAxis(iaxis);
                if (pcpAxis instanceof PCPDoubleAxis) {
                    double x = correlationCoefficientIndicatorRectangle.getX() + (iaxis * DEFAULT_CORRELATION_INDICATOR_STROKE_SPACING);

                    Line line = new Line(x, 0, x, 0);
                    line.setStrokeWidth(DEFAULT_CORRELATION_INDICATOR_STROKE_WIDTH);
                    line.setStrokeLineCap(StrokeLineCap.BUTT);

                    if (this == pcpAxis) {
                        line.setStartY(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY());
                        line.setEndY(correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY());
                        line.setStroke(DEFAULT_CORRELATION_INDICATOR_STROKE_COLOR.grayscale());
                        Tooltip.install(line, new Tooltip(getColumn().getName() + " Correlation Indicators"));
                    } else {
                        double corrCoef = ((DoubleColumnSummaryStats) getColumn().getStatistics()).getCorrelationCoefficientList().get(iaxis);
                        if (corrCoef >= 0) {
                            double lineStartY = GraphicsUtil.mapValue(corrCoef, 0d, 1d, corrMiddleY, correlationCoefficientIndicatorRectangle.getLayoutBounds().getMinY());
                            line.setStartY(lineStartY);
                            line.setEndY(corrMiddleY);
                        } else {
                            double lineEndY = GraphicsUtil.mapValue(corrCoef, 0d, -1d, corrMiddleY, correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY());
                            line.setStartY(corrMiddleY);
                            line.setEndY(lineEndY);
                        }

                        line.setStroke(DEFAULT_CORRELATION_INDICATOR_STROKE_COLOR);
                        Tooltip.install(line, new Tooltip(this.column.getName() + " / " + pcpAxis.getColumn().getName() + " correlation = " + corrCoef));
                    }

                    line.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            line.setEffect(new DropShadow());
                        }
                    });

                    line.setOnMouseExited(event -> { line.setEffect(null); });

//                    correlationCoefficientIndicatorList.add(line);
                    correlationCoefficientIndicatorGroup.getChildren().add(line);
                }
            }
        }
        */
    }

    public double getCenterX() {
        return centerX;
    }

    public void setLabelColor(Color labelColor) {
        if (this.labelColor != labelColor) {
            this.labelColor = labelColor;
            nameText.setFill(labelColor);
//            minValueText.setFill(labelColor);
//            maxValueText.setFill(labelColor);
        }
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public Column getColumn() { return column; }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public Text getNameText() { return nameText; }

    public final double getNameTextRotation() { return nameTextRotation.get(); }

    public final void setNameTextRotation(double value) { nameTextRotation.set(value); }

    public DoubleProperty nameTextRotationProperty() { return nameTextRotation; }

    public double getBarLeftX() { return axisBar.getX(); }
    public double getBarRightX() { return axisBar.getX() + axisBar.getWidth(); }

    public Rectangle getBounds() { return bounds; }

    public Rectangle getAxisBar() { return axisBar; }

    //    public DoubleColumn getColumn() { return column; }
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

    protected Line makeLine() {
        Line line = new Line();
        line.setStroke(Color.DARKGRAY);
        line.setSmooth(true);
        line.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        return line;
    }
}
