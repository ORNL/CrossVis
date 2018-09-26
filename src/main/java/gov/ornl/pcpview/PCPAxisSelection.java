package gov.ornl.pcpview;

import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.DataTable;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.logging.Logger;

public abstract class PCPAxisSelection {
    private final static Logger log = Logger.getLogger(PCPAxisSelection.class.getName());

    public final static Color DEFAULT_TEXT_FILL = Color.BLACK;
    public final static double DEFAULT_TEXT_SIZE = 8d;
    public final static Color DEFAULT_SELECTION_RECTANGLE_FILL_COLOR = Color.ORANGE.deriveColor(1,1,1,0.2);

    private DataTable dataModel;
    private PCPAxis pcpAxis;
//    private Pane pane;
    private ColumnSelection selectionRange;

    private Rectangle rectangle;
//    private Line leftLine;
//    private Line rightLine;
    private Polyline topCrossbar;
    private Polyline bottomCrossbar;

    private Group graphicsGroup;

    // dragging variables
    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging;

    public PCPAxisSelection(PCPAxis pcpAxis, ColumnSelection selectionRange, DataTable dataModel) {
        this.pcpAxis = pcpAxis;
        this.selectionRange = selectionRange;
        this.dataModel = dataModel;
        rectangle = null;
    }

    public PCPAxisSelection(PCPAxis pcpAxis, ColumnSelection selectionRange, double minValueY, double maxValueY, DataTable dataModel) {
        this(pcpAxis, selectionRange, dataModel);
        double top = Math.min(minValueY, maxValueY);
        double bottom = Math.max(minValueY, maxValueY);
        double left = pcpAxis.getAxisBar().getX();
        double right = pcpAxis.getAxisBar().getX() + pcpAxis.getAxisBar().getWidth();
//        double left = rectangle.getX() - 4;
//        double right = rectangle.getX() + rectangle.getWidth() + 4;

        rectangle = new Rectangle(left, top, right-left, bottom-top);
//        rectangle = new Rectangle(pcpAxis.getAxisBar().getX(), top, pcpAxis.getAxisBar().getWidth(), bottom - top);
        rectangle.setFill(DEFAULT_SELECTION_RECTANGLE_FILL_COLOR);

        // make top and bottom crossbars

        topCrossbar = new Polyline(left, (top + 2d), left, top, right, top, right, (top + 2d));
        topCrossbar.setStroke(Color.BLACK);
        topCrossbar.setStrokeWidth(2d);
        bottomCrossbar = new Polyline(left, (bottom - 2d), left, bottom, right, bottom, right, (bottom - 2d));
        bottomCrossbar.setStroke(topCrossbar.getStroke());
        bottomCrossbar.setStrokeWidth(topCrossbar.getStrokeWidth());

//        leftLine = new Line(left+1, top, left+1, bottom);
//        leftLine.setStroke(DEFAULT_SELECTION_RECTANGLE_FILL_COLOR);
//        leftLine.setStrokeWidth(3);
////        leftLine.setStrokeType(StrokeType.CENTERED);
//        rightLine = new Line(right-1, top, right-1, bottom);
//        rightLine.setStroke(DEFAULT_SELECTION_RECTANGLE_FILL_COLOR);
//        rightLine.setStrokeWidth(3);
//        rightLine.setStrokeType(StrokeType.);

        graphicsGroup = new Group(rectangle, topCrossbar, bottomCrossbar);
//        graphicsGroup = new Group(leftLine, rightLine, topCrossbar, bottomCrossbar);

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
                handleRectangleMouseEntered();
            }
        });

        rectangle.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleRectangleMouseExited();
            }
        });

        bottomCrossbar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleBottomCrossbarMouseEntered();
            }
        });

        bottomCrossbar.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleBottomCrossbarMouseExited();
            }
        });

        topCrossbar.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleTopCrossbarMouseEntered();
            }
        });

        topCrossbar.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
                handleTopCrossbarMouseDragged(event);
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
            double left = pcpAxis.getAxisBar().getX();
            double right = pcpAxis.getAxisBar().getX() + pcpAxis.getAxisBar().getWidth();
//            double left = rectangle.getX() - 4;
//            double right = rectangle.getX() + rectangle.getWidth() + 4;

            rectangle.setY(top);
            rectangle.setHeight(bottom - top);
//            rectangle.setX(pcpAxis.getAxisBar().getX());
            rectangle.setX(left);
            rectangle.setWidth(right - left);
//            rectangle.setWidth(pcpAxis.getAxisBar().getWidth());

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
//
//            rightLine.setStartX(right-1);
//            rightLine.setStartY(top);
//            rightLine.setEndX(right-1);
//            rightLine.setEndY(bottom);
//
//            leftLine.setStartX(left+1);
//            leftLine.setStartY(top);
//            leftLine.setEndX(left+1);
//            leftLine.setEndY(bottom);
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

    public ColumnSelection getColumnSelectionRange() {
        return selectionRange;
    }

    public PCPAxis getPCPAxis () { return pcpAxis; }

    public DataTable getDataModel() {
        return dataModel;
    }
}
