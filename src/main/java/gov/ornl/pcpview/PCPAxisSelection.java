package gov.ornl.pcpview;

import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;

public abstract class PCPAxisSelection {

    private final static Logger log = LoggerFactory.getLogger(PCPAxisSelection.class);

    public final static Color DEFAULT_TEXT_FILL = Color.BLACK;
    public final static double DEFAULT_TEXT_SIZE = 8d;
    public final static Color DEFAULT_SELECTION_RECTANGLE_FILL_COLOR = new Color(Color.YELLOW.getRed(),
            Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 0.3);

    private DataModel dataModel;
    private PCPAxis pcpAxis;
    private Pane pane;
    private ColumnSelectionRange selectionRange;

    private Rectangle rectangle;
    private Polyline topCrossbar;
    private Polyline bottomCrossbar;
    private Group graphicsGroup;

    // dragging variables
    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging;

    public PCPAxisSelection(PCPAxis pcpAxis, ColumnSelectionRange selectionRange, Pane pane, DataModel dataModel) {
        this.pcpAxis = pcpAxis;
        this.selectionRange = selectionRange;
        this.pane = pane;
        this.dataModel = dataModel;

        rectangle = null;
    }

    public PCPAxisSelection(PCPAxis pcpAxis, ColumnSelectionRange selectionRange, double minValueY, double maxValueY, Pane pane, DataModel dataModel) {
        this(pcpAxis, selectionRange, pane, dataModel);
//        this.pcpAxis = pcpAxis;
//        this.selectionRange = selectionRange;
//        this.pane = pane;
//        this.dataModel = dataModel;

        double top = Math.min(minValueY, maxValueY);
        double bottom = Math.max(minValueY, maxValueY);
        rectangle = new Rectangle(pcpAxis.getAxisBar().getX(), top, pcpAxis.getAxisBar().getWidth(), bottom - top);
        rectangle.setFill(DEFAULT_SELECTION_RECTANGLE_FILL_COLOR);

        // make top and bottom crossbars
        double left = rectangle.getX();
        double right = rectangle.getX() + rectangle.getWidth();
        topCrossbar = new Polyline(left, (top + 2d), left, top, right, top, right, (top + 2d));
        topCrossbar.setStroke(Color.BLACK);
        topCrossbar.setStrokeWidth(2d);
        bottomCrossbar = new Polyline(left, (bottom - 2d), left, bottom, right, bottom, right, (bottom - 2d));
        bottomCrossbar.setStroke(topCrossbar.getStroke());
        bottomCrossbar.setStrokeWidth(topCrossbar.getStrokeWidth());

        graphicsGroup = new Group(topCrossbar, bottomCrossbar, rectangle);
        pane.getChildren().add(graphicsGroup);
//        graphicsGroup.toBack();

        registerListeners();
    }

    protected abstract void handleRectangleMouseEntered();
    protected abstract void handleRectangleMouseExited();
    protected abstract void handleRectangleMouseDragged(MouseEvent event);
    protected abstract void handleRectangleMousePressed(MouseEvent event);
    protected abstract void handleRectangleMouseReleased();

    protected abstract void handleBottomCrossbarMouseEntered();
    protected abstract void handleBottomCrossbarMouseExited();
    protected abstract void handleBottomCrossbarMouseDragged(MouseEvent event);
    protected abstract void handleBottomCrossbarMousePressed();
    protected abstract void handleBottomCrossbarMouseReleased();

    protected abstract void handleTopCrossbarMouseEntered();
    protected abstract void handleTopCrossbarMouseExited();
    protected abstract void handleTopCrossbarMouseDragged(MouseEvent event);
    protected abstract void handleTopCrossbarMousePressed();
    protected abstract void handleTopCrossbarMouseReleased();

    private void registerListeners() {
        rectangle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.V_RESIZE);
                handleRectangleMouseEntered();
            }
        });

        rectangle.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.DEFAULT);
                handleRectangleMouseExited();
            }
        });

        bottomCrossbar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.V_RESIZE);
                handleBottomCrossbarMouseEntered();
            }
        });

        bottomCrossbar.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.DEFAULT);
                handleBottomCrossbarMouseExited();
            }
        });

        topCrossbar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.V_RESIZE);
                handleTopCrossbarMouseEntered();
            }
        });

        topCrossbar.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                pane.getScene().setCursor(Cursor.DEFAULT);
                handleTopCrossbarMouseExited();
            }
        });

        topCrossbar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());
                handleTopCrossbarMousePressed();
            }
        });

        topCrossbar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleBottomCrossbarMouseDragged(event);
            }
        });

        topCrossbar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleTopCrossbarMouseReleased();
            }
        });

        bottomCrossbar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());

                handleBottomCrossbarMousePressed();
            }
        });

        bottomCrossbar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleBottomCrossbarMouseDragged(event);
            }
        });

        bottomCrossbar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleBottomCrossbarMouseReleased();
            }
        });

        rectangle.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());

                handleRectangleMousePressed(event);
            }
        });

        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleRectangleMouseDragged(event);
            }
        });

        rectangle.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleRectangleMouseReleased();
            }
        });
    }

    public abstract void relayout();

    protected void layoutGraphics(double bottomY, double topY) {
        if (rectangle != null) {
            double top = Math.min(bottomY, topY);
            double bottom = Math.max(bottomY, topY);

            rectangle.setY(top);
            rectangle.setHeight(bottom - top);
            rectangle.setX(pcpAxis.getAxisBar().getX());
            rectangle.setWidth(pcpAxis.getAxisBar().getWidth());

            double left = rectangle.getX();
            double right = rectangle.getX() + rectangle.getWidth();
            topCrossbar.getPoints().set(0, left);
            topCrossbar.getPoints().set(1, top + 2d);
            topCrossbar.getPoints().set(2, left);
            topCrossbar.getPoints().set(3, top);
            topCrossbar.getPoints().set(4, right);
            topCrossbar.getPoints().set(5, top);
            topCrossbar.getPoints().set(6, right);
            topCrossbar.getPoints().set(7, top + 2d);

            bottomCrossbar.getPoints().set(0, left);
            bottomCrossbar.getPoints().set(1, bottom - 2d);
            bottomCrossbar.getPoints().set(2, left);
            bottomCrossbar.getPoints().set(3, bottom);
            bottomCrossbar.getPoints().set(4, right);
            bottomCrossbar.getPoints().set(5, bottom);
            bottomCrossbar.getPoints().set(6, right);
            bottomCrossbar.getPoints().set(7, bottom - 2d);
        }
    }

    public double getTopY () {
        if (rectangle != null) {
            return rectangle.getY();
        }
        return Double.NaN;
    }

    public double getBottomY() {
        if (rectangle != null) {
            return rectangle.getY() + rectangle.getHeight();
        }
        return Double.NaN;
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public Rectangle getRectangle() { return rectangle; }

    public ColumnSelectionRange getColumnSelectionRange() {
        return selectionRange;
    }

    public PCPAxis getPCPAxis () { return pcpAxis; }

    public Pane getPane() { return pane; }

    public DataModel getDataModel() {
        return dataModel;
    }
}
