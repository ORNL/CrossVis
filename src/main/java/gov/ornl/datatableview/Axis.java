package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataTable;
import gov.ornl.scout.dataframeview.DoubleAxisRangeSelection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class Axis {
    public final static Logger log = Logger.getLogger(Axis.class.getName());

    protected DataTableView dataTableView;
    protected Column column;

    protected Orientation orientation;

    protected double centerX;
    protected double centerY;
    protected BoundingBox boundingBox;
//    protected BoundingBox plotBoundingBox;

    protected Group graphicsGroup;

    private DoubleProperty axisBarSize = new SimpleDoubleProperty(DataTableViewDefaultSettings.DEFAULT_BAR_SIZE);

//    protected Text titleText;
    protected Rectangle axisBar;

    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging = false;

    private Text hoverValueText;

//    private ObjectProperty<Color> titleTextColor = new SimpleObjectProperty<>(DataTableViewDefaultSettings.DEFAULT_LABEL_COLOR);

    protected BooleanProperty highlighted;

    private ArrayList<AxisSelection> axisSelectionList = new ArrayList<>();


    public Axis(DataTableView dataTableView, Column column, Orientation orientation) {
        this.dataTableView = dataTableView;
        this.column = column;
        this.orientation = orientation;

        axisBar = new Rectangle();
        axisBar.setStroke(Color.DARKGRAY);
        axisBar.setFill(Color.WHITESMOKE);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DataTableViewDefaultSettings.DEFAULT_STROKE_WIDTH);

        hoverValueText = new Text();
        hoverValueText.setFont(new Font(DataTableViewDefaultSettings.HOVER_TEXT_SIZE));
        hoverValueText.setSmooth(true);
        hoverValueText.setVisible(false);
        hoverValueText.setFill(DataTableViewDefaultSettings.DEFAULT_LABEL_COLOR);
        hoverValueText.setTextOrigin(VPos.BOTTOM);
        hoverValueText.setMouseTransparent(true);

        graphicsGroup = new Group(axisBar, hoverValueText);

        registerListeners();
    }

    public DataTable getDataTable() {
        return dataTableView.getDataTable();
    }

    public List<AxisSelection> getAxisSelectionList() { return axisSelectionList; }

    public Column getColumn() { return column; }

    protected abstract Object getValueForAxisPosition(double axisPosition);

    protected abstract void handleAxisBarMousePressed(MouseEvent event);

    protected abstract void handleAxisBarMouseDragged(MouseEvent event);

    protected abstract void handleAxisBarMouseReleased(MouseEvent event);

    public void setAxisBarSize(double axisBarSize) { axisBarSizeProperty().set(axisBarSize); }

    public double getAxisBarSize() { return axisBarSizeProperty().get(); }

    public DoubleProperty axisBarSizeProperty() { return axisBarSize; }

    public Bounds getBounds() { return boundingBox; }

    public double getCenterX() { return centerX; }

    public double getCenterY() { return centerY; }

    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
//            resize();
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    protected void registerListeners() {
        axisBar.setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
            dragEndPoint = new Point2D(event.getX(), event.getY());
            handleAxisBarMousePressed(event);
        });

        axisBar.setOnMouseDragged(event -> handleAxisBarMouseDragged(event));

        axisBar.setOnMouseReleased(event -> handleAxisBarMouseReleased(event));

        axisBar.setOnMouseMoved(event -> {
//            log.info("mouse y position is " + event.getY() + " value is " + getValueForAxisPosition(event.getY()));
            Object value = getValueForAxisPosition(event.getY());
            if (value != null) {
                hoverValueText.setText(getValueForAxisPosition(event.getY()).toString());
                if (orientation == Orientation.HORIZONTAL) {
                    hoverValueText.setY(event.getY());
                    hoverValueText.setX(getCenterX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
                } else {
                    hoverValueText.setX(event.getX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
                    hoverValueText.setY(getCenterY() + hoverValueText.getLayoutBounds().getHeight() / 2.);
                }
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
    }

    public void resize(double left, double top, double width, double height) {
        boundingBox = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

//        double plotTop = top;
//        plotBoundingBox = new BoundingBox(left, plotTop, width, height - plotTop);

        layout();
    }

    protected void layout() {
        if (orientation == Orientation.HORIZONTAL) {
            axisBar.setX(centerX - (axisBarSize.get() / 2.));
            axisBar.setY(boundingBox.getMinY());
            axisBar.setWidth(axisBarSize.get());
            axisBar.setHeight(boundingBox.getHeight());
        } else {
            axisBar.setX(boundingBox.getMinX());
            axisBar.setY(centerY - (axisBarSize.get() / 2.));
            axisBar.setWidth(boundingBox.getWidth());
            axisBar.setHeight(axisBarSize.get());
//            axisLine.setStartX(boundingBox.getMinX());
//            axisLine.setStartY(centerY);
//            axisLine.setEndX(boundingBox.getMaxX());
//            axisLine.setEndY(centerY);
        }
    }

    public double getAxisBarLeft() {
        return axisBar.getX();
//        return axisBar.getLayoutBounds().getMinX();
    }

    public double getAxisBarTop() {
        return axisBar.getY();
//        return axisBar.getLayoutBounds().getMinY();
    }

    public double getAxisBarRight() {
        return axisBar.getX() + axisBar.getWidth();
//        return axisBar.getLayoutBounds().getMaxX();
    }

    public double getAxisBarBottom() {
        return axisBar.getY() + axisBar.getHeight();
//        return axisBar.getLayoutBounds().getMaxY();
    }

    public double getAxisBarWidth() { return axisBar.getWidth(); }

    public double getAxisBarHeight() { return axisBar.getHeight(); }
}
