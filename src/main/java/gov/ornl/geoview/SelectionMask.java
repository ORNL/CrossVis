package gov.ornl.geoview;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class SelectionMask {
    private double westLongitude;
    private double eastLongitude;
    private double northLatitude;
    private double southLatitude;

    private Rectangle screenRectangle;
    private Group graphicsGroup = new Group();

    public SelectionMask(double westLongitude, double northLatitude, double eastLongitude, double southLatitude) {
        this.westLongitude = westLongitude;
        this.eastLongitude = eastLongitude;
        this.southLatitude = southLatitude;
        this.northLatitude = northLatitude;

        screenRectangle = new Rectangle();
        screenRectangle.setMouseTransparent(true);
        screenRectangle.setStroke(Color.BLACK);
        screenRectangle.setStrokeWidth(2);
        screenRectangle.setStrokeType(StrokeType.OUTSIDE);
        screenRectangle.setFill(Color.GHOSTWHITE.deriveColor(1., 1., 1., 0.2));

        graphicsGroup.getChildren().add(screenRectangle);
    }

    public boolean intersects (Bounds bounds) {
        return screenRectangle.intersects(bounds);
    }

    public double getScreenLeft() {
        return screenRectangle.getLayoutX() + screenRectangle.getX();
    }

    public double getScreenRight() {
        return screenRectangle.getLayoutX() + screenRectangle.getX() + screenRectangle.getWidth();
    }

    public double getScreenTop() {
        return screenRectangle.getLayoutY() + screenRectangle.getY();
    }

    public double getScreenBottom() {
        return screenRectangle.getLayoutY() + screenRectangle.getY() + screenRectangle.getHeight();
    }

    public boolean contains (double x, double y) {
        return screenRectangle.contains(x, y);
    }

    public Bounds getIntersection(Bounds bounds) {
        double left = bounds.getMinX() < screenRectangle.getX() ? screenRectangle.getX() : bounds.getMinX();
        double right = bounds.getMaxX() > screenRectangle.getX() + screenRectangle.getWidth() ? screenRectangle.getX() + screenRectangle.getWidth() : bounds.getMaxX();
        double top = bounds.getMinY() < screenRectangle.getY() ? screenRectangle.getY() : bounds.getMinY();
        double bottom = bounds.getMaxY() > screenRectangle.getY() + screenRectangle.getHeight() ? screenRectangle.getY() + screenRectangle.getHeight() : bounds.getMaxY();

        return new BoundingBox(left, top, right - left, bottom - top);
    }

    public void setScreenCoordinates(double translateX, double translateY, double screenleft, double screenTop, double screenRight, double screenBottom) {
        screenRectangle.setTranslateX(translateX);
        screenRectangle.setTranslateY(translateY);
        screenRectangle.setX(screenleft);
        screenRectangle.setY(screenTop);
        screenRectangle.setWidth(screenRight - screenleft);
        screenRectangle.setHeight(screenBottom - screenTop);
        Polygon area = new Polygon();
    }

    public Group getGraphicsGroup() {
        return graphicsGroup;
    }

    public double getWestLongitude() {
        return westLongitude;
    }

    public void setWestLongitude(double westLongitude) {
        this.westLongitude = westLongitude;
    }

    public double getEastLongitude() {
        return eastLongitude;
    }

    public void setEastLongitude(double eastLongitude) {
        this.eastLongitude = eastLongitude;
    }

    public double getNorthLatitude() {
        return northLatitude;
    }

    public void setNorthLatitude(double northLatitude) {
        this.northLatitude = northLatitude;
    }

    public double getSouthLatitude() {
        return southLatitude;
    }

    public void setSouthLatitude(double southLatitude) {
        this.southLatitude = southLatitude;
    }
}
