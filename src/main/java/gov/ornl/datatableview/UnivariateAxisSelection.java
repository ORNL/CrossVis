package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

import java.util.logging.Logger;

public abstract class UnivariateAxisSelection extends AxisSelection {
    private final static Logger log = Logger.getLogger(UnivariateAxisSelection.class.getName());

//    private DataTable dataModel;
//    private Pane pane;

    private Rectangle rectangle;
//    private Line leftLine;
//    private Line rightLine;
    private Polyline topCrossbar;
    private Polyline bottomCrossbar;

    // dragging variables
    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging;

    public UnivariateAxisSelection(UnivariateAxis axis, ColumnSelection columnSelection) {
        super(axis, columnSelection);

        rectangle = null;
    }

    public UnivariateAxisSelection(UnivariateAxis axis, ColumnSelection columnSelection, double minValueY, double maxValueY) {
        this(axis, columnSelection);

        double top = Math.min(minValueY, maxValueY);
        double bottom = Math.max(minValueY, maxValueY);
        double left = axis.getAxisBar().getX();
        double right = axis.getAxisBar().getX() + axis.getAxisBar().getWidth();
//        double left = rectangle.getX() - 4;
//        double right = rectangle.getX() + rectangle.getWidth() + 4;

        rectangle = new Rectangle(left, top, right-left, bottom-top);
//        rectangle = new Rectangle(univariateAxis.getAxisBar().getX(), top, univariateAxis.getAxisBar().getWidth(), bottom - top);
        rectangle.setFill(Axis.DEFAULT_SELECTION_FILL_COLOR);

        // make top and bottom crossbars

        topCrossbar = new Polyline(left, (top + 2d), left, top, right, top, right, (top + 2d));
        topCrossbar.setStroke(Color.BLACK);
        topCrossbar.setStrokeWidth(2d);
        topCrossbar.setCursor(Cursor.V_RESIZE);

        bottomCrossbar = new Polyline(left, (bottom - 2d), left, bottom, right, bottom, right, (bottom - 2d));
        bottomCrossbar.setStroke(topCrossbar.getStroke());
        bottomCrossbar.setStrokeWidth(topCrossbar.getStrokeWidth());
        bottomCrossbar.setCursor(Cursor.V_RESIZE);

//        leftLine = new Line(left+1, top, left+1, bottom);
//        leftLine.setStroke(DEFAULT_SELECTION_FILL_COLOR);
//        leftLine.setStrokeWidth(3);
////        leftLine.setStrokeType(StrokeType.CENTERED);
//        rightLine = new Line(right-1, top, right-1, bottom);
//        rightLine.setStroke(DEFAULT_SELECTION_FILL_COLOR);
//        rightLine.setStrokeWidth(3);
//        rightLine.setStrokeType(StrokeType.);

        getGraphicsGroup().getChildren().addAll(rectangle, topCrossbar, bottomCrossbar);
//        graphicsGroup = new Group(leftLine, rightLine, topCrossbar, bottomCrossbar);

        registerListeners();
    }

    protected UnivariateAxis univariateAxis() { return (UnivariateAxis)getAxis(); }

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

    protected void layoutGraphics(double bottomY, double topY) {
        if (rectangle != null) {
            double top = Math.min(bottomY, topY);
            double bottom = Math.max(bottomY, topY);

            double left = univariateAxis().getAxisBar().getX();
            double right = univariateAxis().getAxisBar().getX() + univariateAxis().getAxisBar().getWidth();
//            double left = rectangle.getX() - 4;
//            double right = rectangle.getX() + rectangle.getWidth() + 4;

            rectangle.setY(top);
            rectangle.setHeight(bottom - top);
//            rectangle.setX(univariateAxis.getAxisBar().getX());
            rectangle.setX(left);
            rectangle.setWidth(right - left);
//            rectangle.setWidth(univariateAxis.getAxisBar().getWidth());

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

    public Rectangle getRectangle() { return rectangle; }
}
