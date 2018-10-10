package gov.ornl.pcpview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.DataTable;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class PCPUnivariateAxis extends PCPAxis {
    public final static Logger log = Logger.getLogger(PCPUnivariateAxis.class.getName());

    public final static int DEFAULT_MAX_HISTOGRAM_BIN_WIDTH = 30;
    public final static Color DEFAULT_HISTOGRAM_FILL = Color.SILVER.deriveColor(1, 1, 1, .8);
    public final static Color DEFAULT_QUERY_HISTOGRAM_FILL = Color.SLATEGRAY.deriveColor(1., 1., 1., 0.8);
    public final static Color DEFAULT_HISTOGRAM_STROKE = Color.DARKGRAY;

    public final static double DEFAULT_CONTEXT_HEIGHT = 10d;
    public final static double DEFAULT_BAR_WIDTH = 20d;
    public final static double DEFAULT_NARROW_BAR_WIDTH = 6d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double HOVER_TEXT_SIZE = 8d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;

    private Column column;

    protected Group axisSelectionGraphicsGroup = new Group();

    protected Text hoverValueText;

    private double contextRegionHeight = DEFAULT_CONTEXT_HEIGHT;

    private Rectangle topContexBar;
    private Rectangle bottomContextBar;
    private Rectangle axisBar;

    // value labels
    protected Text maxValueText;
    protected Text minValueText;
    protected Text focusMaxValueText;
    protected Text focusMinValueText;

    // axis relocation stuff
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

    private Rectangle draggingTopContextBar;
    private Rectangle draggingBottomContextBar;
    private Rectangle draggingAxisBar;
    private Text draggingNameText;

    public PCPUnivariateAxis(PCPView pcpView, Column column) {
        super(pcpView, column.getName());

        this.column = column;

        highlighted = new SimpleBooleanProperty(getDataTable().getHighlightedColumn() == this.column);

//        titleText = new Text(column.getName());
//        Tooltip tooltip = new Tooltip();
//        tooltip.textProperty().bindBidirectional(column.nameProperty());
//        Tooltip.install(titleText, tooltip);
//        titleText.setFont(new Font(DEFAULT_NAME_TEXT_SIZE));
//        titleText.setSmooth(true);
//        titleText.rotateProperty().bindBidirectional(titleTextRotation);
//        titleText.setFill(labelColor);
//        titleText.setMouseTransparent(true);

//        titleTextRectangle = new Rectangle();
//        titleTextRectangle.setStrokeWidth(3.);
//        titleTextRectangle.setStroke(Color.TRANSPARENT);
//        titleTextRectangle.setFill(Color.TRANSPARENT);
//        titleTextRectangle.setArcWidth(6.);
//        titleTextRectangle.setArcHeight(6.);

//        overallCorrelationCoefficientIndicatorRectangle = new Rectangle();
//        overallCorrelationCoefficientIndicatorRectangle.setStroke(Color.BLACK);
//        overallCorrelationCoefficientIndicatorRectangle.setFill(Color.TRANSPARENT);
//        overallCorrelationCoefficientIndicatorRectangle.setHeight(DEFAULT_CORRELATION_INDICATOR_HEIGHT);
//        overallCorrelationCoefficientIndicatorRectangle.setWidth(DEFAULT_CORRELATION_INDICATOR_WIDTH);
//
//        queryCorrelationCoefficientIndicatorRectangle = new Rectangle();
//        queryCorrelationCoefficientIndicatorRectangle.setStroke(Color.BLACK);
//        queryCorrelationCoefficientIndicatorRectangle.setFill(Color.TRANSPARENT);
//        queryCorrelationCoefficientIndicatorRectangle.setHeight(DEFAULT_CORRELATION_INDICATOR_HEIGHT);
//        queryCorrelationCoefficientIndicatorRectangle.setWidth(DEFAULT_CORRELATION_INDICATOR_WIDTH/2);
//
//        nonqueryCorrelationCoefficientIndicatorRectangle = new Rectangle();
//        nonqueryCorrelationCoefficientIndicatorRectangle.setStroke(Color.BLACK);
//        nonqueryCorrelationCoefficientIndicatorRectangle.setFill(Color.TRANSPARENT);
//        nonqueryCorrelationCoefficientIndicatorRectangle.setHeight(DEFAULT_CORRELATION_INDICATOR_HEIGHT);
//        nonqueryCorrelationCoefficientIndicatorRectangle.setWidth(DEFAULT_CORRELATION_INDICATOR_WIDTH/2);
//
//        correlationCoefficientIndicatorGroup.getChildren().add(overallCorrelationCoefficientIndicatorRectangle);

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
        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        topContexBar = new Rectangle();
        topContexBar.setStroke(Color.DARKGRAY);
        topContexBar.setFill(Color.WHITESMOKE);
        topContexBar.setSmooth(true);
        topContexBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        bottomContextBar = new Rectangle();
        bottomContextBar.setStroke(Color.DARKGRAY);
        bottomContextBar.setFill(Color.WHITESMOKE);
        bottomContextBar.setSmooth(true);
        bottomContextBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        hoverValueText = new Text();
        hoverValueText.setFont(new Font(HOVER_TEXT_SIZE));
        hoverValueText.setSmooth(true);
        hoverValueText.setVisible(false);
        hoverValueText.setFill(DEFAULT_LABEL_COLOR);
        hoverValueText.setTextOrigin(VPos.BOTTOM);
        hoverValueText.setMouseTransparent(true);
        getPCPView().getPane().getChildren().add(hoverValueText);

//        topCrossBarLine = makeLine();
//        bottomCrossBarLine = makeLine();
//        topFocusCrossBarLine = makeLine();
//        bottomFocusCrossBarLine = makeLine();

        graphicsGroup.getChildren().addAll(topContexBar, bottomContextBar, axisBar, minValueText, maxValueText,
                focusMinValueText, focusMaxValueText, axisSelectionGraphicsGroup);

//        if (getColumn() instanceof DoubleColumn && pcpView.isShowingCorrelations()) {
//            graphicsGroup.getChildren().addAll(correlationCoefficientIndicatorGroup);
//        }

        registerListeners();
    }

    public ArrayList<PCPAxisSelection> getAxisSelectionList() {
        return axisSelectionList;
    }

    protected abstract PCPAxisSelection addAxisSelection(ColumnSelection columnSelection);

    protected boolean removeAxisSelection(ColumnSelection columnSelection) {
        // find the axis selection for the given column selection
        PCPAxisSelection axisSelection = getAxisSelection(columnSelection);
        if (axisSelection != null) {
            // remove the axis selection's graphics
            axisSelectionGraphicsGroup.getChildren().remove(axisSelection.getGraphicsGroup());
            return getAxisSelectionList().remove(axisSelection);
        }

        return false;
    }

    protected void removeAllAxisSelections() {
        axisSelectionGraphicsGroup.getChildren().clear();
//        for (PCPAxisSelection axisSelection : axisSelectionList) {
//            pane.getChildren().remove(axisSelection.getGraphicsGroup());
//        }
        axisSelectionList.clear();
    }

    protected abstract Object getValueForAxisPosition(double axisPosition);

    public abstract void removeAllGraphics(Pane pane);

    protected abstract void handleAxisBarMousePressed();

    protected abstract void handleAxisBarMouseDragged(MouseEvent event);

    protected abstract void handleAxisBarMouseReleased();

    protected PCPAxisSelection getAxisSelection(ColumnSelection columnSelection) {
        for (PCPAxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelectionRange() == columnSelection) {
                return axisSelection;
            }
        }

        return null;
    }

    private void makeAxisDraggingGraphicsGroup() {
        draggingAxisBar = new Rectangle(getAxisBar().getX(), getAxisBar().getY(), getAxisBar().getWidth(), getAxisBar().getHeight());
        draggingAxisBar.setStroke(getAxisBar().getStroke());
        draggingAxisBar.setFill(getAxisBar().getFill());

        draggingTopContextBar = new Rectangle(topContexBar.getX(), topContexBar.getY(),
                topContexBar.getWidth(), topContexBar.getHeight());
        draggingTopContextBar.setStroke(topContexBar.getStroke());
        draggingTopContextBar.setFill(topContexBar.getFill());
        
        draggingBottomContextBar = new Rectangle(bottomContextBar.getX(), bottomContextBar.getY(),
                bottomContextBar.getWidth(), bottomContextBar.getHeight());
        draggingBottomContextBar.setStroke(bottomContextBar.getStroke());
        draggingBottomContextBar.setFill(bottomContextBar.getFill());

        draggingNameText = new Text(titleText.getText());
        draggingNameText.setX(titleText.getX());
        draggingNameText.setY(titleText.getY());
        draggingNameText.setFont(titleText.getFont());

        axisDraggingGraphicsGroup = new Group(draggingNameText, draggingTopContextBar, draggingBottomContextBar,
                draggingAxisBar);
        axisDraggingGraphicsGroup.setTranslateY(5);
    }

    private void registerListeners() {
        titleTextRectangle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (getDataTable().getHighlightedColumn() == getColumn()) {
                    getDataTable().setHighlightedColumn(null);
                } else {
                    getDataTable().setHighlightedColumn(getColumn());
                }
            }
        });

        titleTextRectangle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!dragging) {
                    dragging = true;
                    makeAxisDraggingGraphicsGroup();
                    axisDraggingGraphicsGroup.setEffect(new DropShadow());
                    getPCPView().getPane().getChildren().add(axisDraggingGraphicsGroup);

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

        titleTextRectangle.setOnMousePressed(new EventHandler<MouseEvent>() {
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
                            getDataTable().disableColumn(column);
                        }
                    });
                    closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            contextMenu.hide();
                        }
                    });
                    contextMenu.show(getPCPView(), event.getScreenX(), event.getScreenY());
                }
            }
        });

        titleTextRectangle.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (dragging) {
                    getPCPView().getPane().getChildren().remove(axisDraggingGraphicsGroup);

//                    pane.getChildren().remove(dragImageView);

                    dragging = false;

                    // calculate the new index position for the column associated with this axis
//                    double dragImageCenterX = dragImageView.getX() + (dragImageView.getLayoutBounds().getWidth() / 2.);
//                    int newColumnIndex = (int) dragImageCenterX / pcpView.getAxisSpacing();
                    int newColumnIndex = (int)dragEndPoint.getX() / getPCPView().getAxisSpacing();
                    newColumnIndex = GraphicsUtil.constrain(newColumnIndex, 0, getDataTable().getColumnCount() - 1);

                    if (!(newColumnIndex == getDataTable().getColumnIndex(column))) {
                        getDataTable().changeColumnOrder(getColumn(), newColumnIndex);
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

        axisBar.setOnMouseMoved(event -> {
            Object value = getValueForAxisPosition(event.getY());
            if (value != null) {
                hoverValueText.setText(getValueForAxisPosition(event.getY()).toString());
                hoverValueText.setY(event.getY());
                hoverValueText.setX(getCenterX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
            } else {
                hoverValueText.setText("");
            }
//            hoverValueText.toFront();
        });

        axisBar.setOnMouseEntered(event -> {
//            log.info("mouse entered axis Bar");
            hoverValueText.setVisible(true);
            hoverValueText.toFront();
        });

        axisBar.setOnMouseExited(event -> {
//            log.info("mouse exited axis bar");
            hoverValueText.setVisible(false);
        });

        highlighted.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
//                titleTextRectangle.setStroke(Color.GRAY);
                titleTextRectangle.setFill(Color.web("#ffc14d"));
//                titleText.setFill(labelColor);
//                titleText.setFill(Color.BLUE);
//                titleTextGraphicsGroup.getChildren().set(0, titleTextRectangle);
//                titleText.setFont(Font.font(titleText.getFont().getFamily(), FontWeight.BOLD, DEFAULT_NAME_TEXT_SIZE));
//                titleText.setEffect(new DropShadow());
            } else {
//                titleTextRectangle.setStroke(Color.TRANSPARENT);
                titleTextRectangle.setFill(Color.TRANSPARENT);
//                titleText.setFill(labelColor);
//                titleText.setFill(DEFAULT_LABEL_COLOR);
//                titleTextGraphicsGroup.getChildren().remove(titleTextRectangle);
//                titleText.setFont(Font.font(titleText.getFont().getFamily(), FontWeight.NORMAL, DEFAULT_NAME_TEXT_SIZE));
//                titleText.setEffect(null);
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

    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);

//        overallCorrelationCoefficientIndicatorRectangle.setY(titleText.getLayoutBounds().getMaxY() + 4);
//        overallCorrelationCoefficientIndicatorRectangle.setX(bounds.getMinX());
//        overallCorrelationCoefficientIndicatorRectangle.setWidth(bounds.getWidth());

        double barTopY = titleText.getLayoutBounds().getMaxY() + minValueText.getLayoutBounds().getHeight() + 4;
//        if (pcpView.isShowingCorrelations()) {
//            barTopY += overallCorrelationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY();
//        }
//        double barTopY = correlationCoefficientIndicatorRectangle.getLayoutBounds().getMaxY() + minValueText.getLayoutBounds().getHeight() + 4;
        double barBottomY = bounds.getMinY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
        double focusTopY = barTopY + contextRegionHeight;
        double focusBottomY = barBottomY - contextRegionHeight;
//        barTopY = top + DEFAULT_NAME_LABEL_HEIGHT;
//        barBottomY = bounds.getY() + bounds.getHeight() - maxValueText.getLayoutBounds().getHeight();
//        focusTopY = top + DEFAULT_NAME_LABEL_HEIGHT + contextRegionHeight;
//        focusBottomY = barBottomY - contextRegionHeight;

        minValueText.setX(bounds.getMinX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
//        minValueText.setX(titleText.getX());
        minValueText.setY(barBottomY + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(bounds.getMinX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(barTopY - 4d);

        maxHistogramBinWidth = bounds.getWidth() / 2;

        axisBar.setX(centerX - (axisBar.getWidth() / 2.));
        axisBar.setY(focusTopY);
//        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setHeight(focusBottomY - focusTopY);

        topContexBar.setX(axisBar.getX());
        topContexBar.setWidth(axisBar.getWidth());
        topContexBar.setY(barTopY);
        topContexBar.setHeight(contextRegionHeight);

        bottomContextBar.setX(axisBar.getX());
        bottomContextBar.setWidth(axisBar.getWidth());
        bottomContextBar.setY(focusBottomY);
        bottomContextBar.setHeight(contextRegionHeight);

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
            titleText.setFill(labelColor);
//            minValueText.setFill(labelColor);
//            maxValueText.setFill(labelColor);
        }
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public Column getColumn() { return column; }

    public double getBarLeftX() { return axisBar.getX(); }
    public double getBarRightX() { return axisBar.getX() + axisBar.getWidth(); }


    public Rectangle getAxisBar() { return axisBar; }

    //    public DoubleColumn getColumn() { return column; }
    public int getColumnDataModelIndex() { return getDataTable().getColumnIndex(getColumn()); }

    public double getFocusTopY() { return topContexBar.getY() + topContexBar.getHeight()/*focusTopY*/; }
    public double getFocusBottomY() { return bottomContextBar.getY()/*focusBottomY*/; }
//    public double getUpperContextTopY() { return axisBar.getY()/*barTopY*/; }
//    public double getUpperContextBottomY() { return focusTopY; }
//    public double getLowerContextTopY() { return focusBottomY; }
//    public double getLowerContextBottomY() { return barBottomY; }

//    public double getVerticalBarTop() { return barTopY; }
//    public double getVerticalBarBottom() { return barBottomY; }

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
