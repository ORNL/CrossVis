package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
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

    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;

    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;

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
                nameText.setEffect(new Glow());
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
        barTopY = top + DEFAULT_NAME_LABEL_HEIGHT;
        barBottomY = bounds.getY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
        focusTopY = top + DEFAULT_NAME_LABEL_HEIGHT + contextRegionHeight;
        focusBottomY = barBottomY - contextRegionHeight;

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

        minValueText.setX(bounds.getX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(barBottomY + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(bounds.getX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(barTopY - 4d);

        if (!axisSelectionList.isEmpty()) {
            for (PCPAxisSelection pcpAxisSelection : axisSelectionList) {
                pcpAxisSelection.relayout();
            }
        }
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
