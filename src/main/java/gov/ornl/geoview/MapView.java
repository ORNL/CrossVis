package gov.ornl.geoview;

import gov.ornl.util.GraphicsUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MapView extends Region {
    private static final Logger log = Logger.getLogger(MapView.class.getName());

    private Image mapImage;
    private Canvas mapImageCanvas;
    private Pane pane = new Pane();
    private BoundingBox mapBounds;
    private Rectangle mapBoundsRectangle = new Rectangle();

    private ArrayList<Line> longitudeGridLines = new ArrayList<>();
    private ArrayList<Line> latitudeGridLines = new ArrayList<>();
    private double gridLineInterval = 30.;
    private Group gridLineGroup = new Group();

    private Rectangle selectionRectangle;
    private Rectangle selectionRectangleWrapped;

    private boolean dragging = false;
    private Point2D dragStartPoint;
    private Point2D dragEndPoint;
    private Rectangle dragRectangle = new Rectangle();
    private Rectangle dragRectangleWrapped;

    private ObjectProperty<Point2D> mouseHoverGeoLocation = new SimpleObjectProperty<>();
    private ObjectProperty<GeoRectangle> selectionGeoRectangle = new SimpleObjectProperty<>();

    private ObjectProperty<Color> gridLineColor = new SimpleObjectProperty<>(new Color(0.2, 0.2, 0.2, 0.2));

    private SelectionMask selectionMask;

    private Color selectionRectangleFillColor = Color.BLUE.deriveColor(1.,1.,1.,.25);

    public MapView() {
        mapImage = new Image(getClass().getClassLoader().getResourceAsStream("gov/ornl/geoview/HYP_50M_SR_W.png"));
        mapImageCanvas = new Canvas();

        setMinSize(180, 90);

        mapBoundsRectangle.setStroke(Color.DARKGRAY);
        mapBoundsRectangle.setFill(Color.TRANSPARENT);
        mapBoundsRectangle.setMouseTransparent(true);

        int numLatitudeGridLines= (int)(180. / gridLineInterval);
        for (int i = 0; i <= numLatitudeGridLines; i++) {
            double latitude = -90. + (i * gridLineInterval);
            Line line = new Line();
            line.setStroke(gridLineColor.get());
            line.setMouseTransparent(true);
            latitudeGridLines.add(line);
            gridLineGroup.getChildren().add(line);
        }

        int numLongitudeGridLines = (int)(360./gridLineInterval);
        for (int i = 0; i <= numLongitudeGridLines; i++) {
            Line line = new Line();
            line.setStroke(gridLineColor.get());
            line.setMouseTransparent(true);
            line.setStrokeLineCap(StrokeLineCap.BUTT);
            longitudeGridLines.add(line);
            gridLineGroup.getChildren().add(line);
        }

        pane.getChildren().addAll(mapImageCanvas, mapBoundsRectangle, gridLineGroup);
        getChildren().add(pane);

        registerListeners();
    }

    public void clearSelection() {
        if (selectionRectangleWrapped != null) {
            pane.getChildren().remove(selectionRectangleWrapped);
            selectionRectangleWrapped = null;
        }

        if (selectionRectangle != null) {
            pane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }

        selectionGeoRectangleProperty().set(null);
    }

    public void setSelection(double westLongitude, double eastLongitude, double southLatitude, double northLatitude) {
        if (dragging) {
            return;
        }

        // clear existing selections
        if (selectionRectangleWrapped != null) {
            pane.getChildren().remove(selectionRectangleWrapped);
            selectionRectangleWrapped = null;
        }

        if (selectionRectangle != null) {
            pane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;
        }

        double westLon = westLongitude;
        double eastLon = eastLongitude;
        double southLat = southLatitude;
        double northLat = northLatitude;
        if (selectionMask != null) {
            // constrain new selection to selection mask
            westLon = westLon < selectionMask.getWestLongitude() ? selectionMask.getWestLongitude() : westLon;
            eastLon = eastLon > selectionMask.getEastLongitude() ? selectionMask.getEastLongitude() : eastLon;
            southLat = southLat < selectionMask.getSouthLatitude() ? selectionMask.getSouthLatitude() : southLat;
            northLat = northLat > selectionMask.getNorthLatitude() ? selectionMask.getNorthLatitude() : northLat;
        }

        // calculate screen coordinates
        double left = GraphicsUtil.mapValue(westLon, -180, 180, mapImageCanvas.getLayoutBounds().getMinX(),
                mapImageCanvas.getLayoutBounds().getMaxX());
        double right = GraphicsUtil.mapValue(eastLon, -180, 180, mapImageCanvas.getLayoutBounds().getMinX(),
                mapImageCanvas.getLayoutBounds().getMaxX());
        double bottom = GraphicsUtil.mapValue(southLat, 90, -90, mapImageCanvas.getLayoutBounds().getMinY(),
                mapImageCanvas.getLayoutBounds().getMaxY());
        double top = GraphicsUtil.mapValue(northLat, 90, -90, mapImageCanvas.getLayoutBounds().getMinY(),
                mapImageCanvas.getLayoutBounds().getMaxY());

        if (left < mapImageCanvas.getLayoutBounds().getMinX()) {
            selectionRectangleWrapped = createRectangle(Color.gray(0.4));
            selectionRectangleWrapped.setX(mapImageCanvas.getLayoutBounds().getMaxX() - (mapImageCanvas.getLayoutBounds().getMinX() - left));
            selectionRectangleWrapped.setWidth(mapImageCanvas.getLayoutBounds().getMinX() - left);
            left = mapImageCanvas.getLayoutBounds().getMinX();
        } else if (right > mapImageCanvas.getLayoutBounds().getMaxX()) {
            selectionRectangleWrapped = createRectangle(Color.gray(0.4));
            selectionRectangleWrapped.setX(mapImageCanvas.getLayoutBounds().getMinX());
            selectionRectangleWrapped.setWidth(right - mapImageCanvas.getLayoutBounds().getMaxX());
            right = mapImageCanvas.getLayoutBounds().getMaxX();
        }

        if (selectionRectangleWrapped != null) {
            selectionRectangleWrapped.setFill(selectionRectangleFillColor);
            selectionRectangleWrapped.setY(top);
            selectionRectangleWrapped.setHeight(bottom - top);
            selectionRectangleWrapped.setTranslateX(mapImageCanvas.getLayoutX());
            selectionRectangleWrapped.setTranslateY(mapImageCanvas.getLayoutY());
            pane.getChildren().add(selectionRectangleWrapped);
        }

        // make selection rectangle and add to pane
        selectionRectangle = createRectangle(Color.gray(0.4));
        selectionRectangle.setFill(selectionRectangleFillColor);
        selectionRectangle.setX(left);
        selectionRectangle.setWidth(right - left);
        selectionRectangle.setY(top);
        selectionRectangle.setHeight(bottom - top);
        selectionRectangle.setTranslateX(mapImageCanvas.getLayoutX());
        selectionRectangle.setTranslateY(mapImageCanvas.getLayoutY());
        pane.getChildren().add(selectionRectangle);

        // set selection georectangle
        selectionGeoRectangle.setValue(new GeoRectangle(westLon, eastLon, southLat, northLat));
    }

    public void setSelectionMask(double westLongitude, double eastLongitude, double southLatitude, double northLatitude) {
        if (selectionMask != null) {
            pane.getChildren().remove(selectionMask.getGraphicsGroup());
        }
        selectionMask = new SelectionMask(westLongitude, northLatitude, eastLongitude, southLatitude);
        resizeView();
        pane.getChildren().add(selectionMask.getGraphicsGroup());
    }

    public void clearSelectionMask() {
        if (selectionMask != null) {
            pane.getChildren().remove(selectionMask.getGraphicsGroup());
            selectionMask = null;
        }
    }

    private Rectangle createRectangle(Color strokeColor) {
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(strokeColor);
        rectangle.setStrokeWidth(1);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setMouseTransparent(true);

        return rectangle;
    }

    public Point2D getMouseHoverGeoLocation() {
        return mouseHoverGeoLocation.get();
    }

    public ObjectProperty<Point2D> mouseHoverGeoLocationProperty() {
        return mouseHoverGeoLocation;
    }

    public GeoRectangle getSelectionGeoRectangle() {
        return selectionGeoRectangle.get();
    }

    public ObjectProperty<GeoRectangle> selectionGeoRectangleProperty() {
        return selectionGeoRectangle;
    }

    private void registerListeners() {
        widthProperty().addListener(observable -> { resizeView(); });
        heightProperty().addListener(observable -> { resizeView(); });

        mapImageCanvas.setOnMouseMoved(event -> {
            double lat = GraphicsUtil.mapValue(event.getY(), mapImageCanvas.getLayoutBounds().getMinY(),
                    mapImageCanvas.getLayoutBounds().getMaxY()-1, 90., -90.);
            double lon = GraphicsUtil.mapValue(event.getX(), mapImageCanvas.getLayoutBounds().getMinX(),
                    mapImageCanvas.getLayoutBounds().getMaxX()-1, -180., 180.);
            mouseHoverGeoLocation.set(new Point2D(lon, lat));
        });

        mapImageCanvas.setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
        });

        mapImageCanvas.setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;

                selectionRectangle = dragRectangle;
                selectionRectangle.setFill(selectionRectangleFillColor);

                if (dragRectangleWrapped != null) {
                    selectionRectangleWrapped = dragRectangleWrapped;
                    selectionRectangleWrapped.setFill(selectionRectangleFillColor);
                }

                dragRectangle = null;
                dragRectangleWrapped = null;

                double left;
                double right;
                if (selectionRectangleWrapped != null) {
                    if (selectionRectangleWrapped.getX() < selectionRectangle.getX()) {
                        left = selectionRectangle.getLayoutX() + selectionRectangle.getX();
                        right = left + selectionRectangle.getWidth() + selectionRectangleWrapped.getWidth();
                    } else {
                        left = selectionRectangleWrapped.getLayoutX() + selectionRectangleWrapped.getX();
                        right = left + selectionRectangleWrapped.getWidth() + selectionRectangle.getWidth();
                    }
                } else {
                    left = selectionRectangle.getLayoutX() + selectionRectangle.getX();
                    right = left + selectionRectangle.getWidth();
                }

                double bottom = selectionRectangle.getLayoutY() + selectionRectangle.getY() + selectionRectangle.getHeight();
                double top = selectionRectangle.getLayoutY() + selectionRectangle.getY();

                if (selectionMask != null) {
                    left = GraphicsUtil.constrain(left, selectionMask.getScreenLeft(), selectionMask.getScreenRight());
                    right = GraphicsUtil.constrain(right, selectionMask.getScreenLeft(), selectionMask.getScreenRight());
                    top = GraphicsUtil.constrain(top, selectionMask.getScreenTop(), selectionMask.getScreenBottom());
                    bottom = GraphicsUtil.constrain(bottom, selectionMask.getScreenTop(), selectionMask.getScreenBottom());
                    selectionRectangle.setX(left);
                    selectionRectangle.setWidth(right - left);
                    selectionRectangle.setY(top);
                    selectionRectangle.setHeight(bottom - top);
                    if (selectionRectangleWrapped != null) {
                        pane.getChildren().remove(selectionRectangleWrapped);
                        selectionRectangleWrapped = null;
                    }
                }

                log.info("left: " + left + " right: " + right + " bottom: " + bottom + " top: " + top);

                double leftLon = GraphicsUtil.mapValue(left,
                        mapImageCanvas.getLayoutBounds().getMinX(),
                        mapImageCanvas.getLayoutBounds().getMaxX(), -180., 180.);
                double rightLon = GraphicsUtil.mapValue(right,
                        mapImageCanvas.getLayoutBounds().getMinX(),
                        mapImageCanvas.getLayoutBounds().getMaxX(), -180., 180.);

                double bottomLat = GraphicsUtil.mapValue(bottom,
                        mapImageCanvas.getLayoutBounds().getMinY(),
                        mapImageCanvas.getLayoutBounds().getMaxY(), 90., -90.);
                double topLat = GraphicsUtil.mapValue(top,
                        mapImageCanvas.getLayoutBounds().getMinY(),
                        mapImageCanvas.getLayoutBounds().getMaxY(), 90., -90.);

                GeoRectangle geoRectangle = new GeoRectangle(leftLon, rightLon, bottomLat, topLat);
                selectionGeoRectangle.set(geoRectangle);
            } else {
                if (selectionRectangle != null) {
                    pane.getChildren().remove(selectionRectangle);
                    selectionRectangle = null;
                }

                if (selectionRectangleWrapped != null) {
                    pane.getChildren().remove(selectionRectangleWrapped);
                    selectionRectangleWrapped = null;
                }

                if (selectionGeoRectangle.get() != null) {
                    selectionGeoRectangle.set(null);
                }
            }
        });

        mapImageCanvas.setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;

                if (selectionRectangle != null) {
                    pane.getChildren().remove(selectionRectangle);
                    selectionRectangle = null;
                    selectionGeoRectangle.set(null);

                    if (selectionRectangleWrapped != null) {
                        pane.getChildren().remove(selectionRectangleWrapped);
                        selectionRectangleWrapped = null;
                    }
                }

                dragRectangle = createRectangle(Color.gray(0.4));
                dragRectangle.setTranslateY(mapImageCanvas.getLayoutY());
                dragRectangle.setTranslateX(mapImageCanvas.getLayoutX());
                pane.getChildren().add(dragRectangle);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            double selectionLeft;
            double selectionRight;
            double selectionTop;
            double selectionBottom;
            double wrappedLeft = 0;
            double wrappedRight = 0;
            boolean wrapped = false;

            if (dragStartPoint.getX() < dragEndPoint.getX()) {
                selectionLeft = dragStartPoint.getX();
                selectionRight = dragEndPoint.getX();
            } else {
                selectionLeft = dragEndPoint.getX();
                selectionRight = dragStartPoint.getX();
            }

            if (dragStartPoint.getY() < dragEndPoint.getY()) {
                selectionTop = dragStartPoint.getY();
                selectionBottom = dragEndPoint.getY();
            } else {
                selectionTop = dragEndPoint.getY();
                selectionBottom = dragStartPoint.getY();
            }

            if (selectionTop < 0) {
                selectionTop = 0;
            } else if (selectionBottom > mapImageCanvas.getHeight()) {
                selectionBottom = mapImageCanvas.getHeight();
            }

            if (selectionLeft < 0) {
                wrappedRight = mapImageCanvas.getWidth();
                wrappedLeft = mapImageCanvas.getWidth() - (0 - selectionLeft);

                if (wrappedLeft <= selectionRight) {
                    selectionRight = mapImageCanvas.getWidth();
                } else {
                    wrapped = true;
                }

                selectionLeft = 0;
            } else if (selectionRight > mapImageCanvas.getWidth()) {
                wrappedLeft = 0.;
                wrappedRight = selectionRight - mapImageCanvas.getWidth();

                if (wrappedRight >= selectionLeft) {
                    selectionLeft = 0.;
                } else {
                    wrapped = true;
                }

                selectionRight = mapImageCanvas.getWidth();
            }

            if (wrapped) {
                if (dragRectangleWrapped == null) {
                    dragRectangleWrapped = createRectangle(Color.gray(0.4));
                    dragRectangleWrapped.setTranslateY(mapImageCanvas.getLayoutY());
                    dragRectangleWrapped.setTranslateX(mapImageCanvas.getLayoutX());
                    pane.getChildren().add(dragRectangleWrapped);
                }
                dragRectangleWrapped.setX(wrappedLeft);
                dragRectangleWrapped.setWidth(wrappedRight - wrappedLeft);
                dragRectangleWrapped.setY(dragRectangle.getY());
                dragRectangleWrapped.setHeight(dragRectangle.getHeight());
            } else if (dragRectangleWrapped != null) {
                pane.getChildren().remove(dragRectangleWrapped);
                dragRectangleWrapped = null;
            }

            dragRectangle.setX(selectionLeft);
            dragRectangle.setY(selectionTop);
            dragRectangle.setWidth(selectionRight - selectionLeft);
            dragRectangle.setHeight(selectionBottom - selectionTop);
        });
    }

    private void resizeView() {
        double mapWidth = getWidth() - (getInsets().getLeft() + getInsets().getRight());
        double mapHeight = getHeight() - (getInsets().getBottom() + getInsets().getTop());

        double aspectRatio = mapWidth / mapHeight;
        if (aspectRatio < 2.) {
            mapHeight = mapWidth / 2.;
        } else {
            mapWidth = mapHeight * 2.;
        }

        double left = getInsets().getLeft() + ( (getWidth() - (getInsets().getLeft() + getInsets().getRight())) - mapWidth) / 2.;
        double top = getInsets().getTop() + ( (getHeight() - (getInsets().getTop() + getInsets().getBottom())) - mapHeight) / 2.;
        mapBounds = new BoundingBox(left, top, mapWidth, mapHeight);

        mapBoundsRectangle.setX(mapBounds.getMinX());
        mapBoundsRectangle.setY(mapBounds.getMinY());
        mapBoundsRectangle.setWidth(mapBounds.getWidth());
        mapBoundsRectangle.setHeight(mapBounds.getHeight());

        mapImageCanvas.setWidth(mapBounds.getWidth() - 2);
        mapImageCanvas.setHeight(mapBounds.getHeight() - 2);
        mapImageCanvas.getGraphicsContext2D().clearRect(0, 0, mapImageCanvas.getWidth(), mapImageCanvas.getHeight());
        mapImageCanvas.relocate(mapBounds.getMinX() + 1, mapBounds.getMinY() + 1);
        mapImageCanvas.getGraphicsContext2D().drawImage(mapImage, 0, 0, mapImageCanvas.getWidth(), mapImageCanvas.getHeight());

        if (selectionMask != null) {
            double maskLeft = GraphicsUtil.mapValue(selectionMask.getWestLongitude(), -180, 180, 0, mapImageCanvas.getWidth());
            double maskRight = GraphicsUtil.mapValue(selectionMask.getEastLongitude(), -180, 180, 0, mapImageCanvas.getWidth());
            double maskTop = GraphicsUtil.mapValue(selectionMask.getNorthLatitude(), -90, 90, mapImageCanvas.getHeight(), 0);
            double maskBottom = GraphicsUtil.mapValue(selectionMask.getSouthLatitude(), -90, 90, mapImageCanvas.getHeight(), 0);
            selectionMask.setScreenCoordinates(mapImageCanvas.getLayoutX(), mapImageCanvas.getLayoutY(), maskLeft, maskTop, maskRight, maskBottom);
        }

        for (int i = 0; i < latitudeGridLines.size(); i++) {
            double latitude = -90. + (i * gridLineInterval);
            double y = mapImageCanvas.getLayoutY() + GraphicsUtil.mapValue(latitude, -90, 90, mapImageCanvas.getHeight(), 0.);
            latitudeGridLines.get(i).setStartX(mapImageCanvas.getLayoutX() + mapImageCanvas.getLayoutBounds().getMinX());
            latitudeGridLines.get(i).setStartY(y);
            latitudeGridLines.get(i).setEndX(mapImageCanvas.getLayoutX() + mapImageCanvas.getLayoutBounds().getMaxX());
            latitudeGridLines.get(i).setEndY(y);
        }

        for (int i = 0; i < longitudeGridLines.size(); i++) {
            double longitude = -180. + (i * gridLineInterval);
            double x = mapImageCanvas.getLayoutX() +
                    GraphicsUtil.mapValue(longitude, -180, 180,
                            0, mapImageCanvas.getWidth());
            longitudeGridLines.get(i).setStartX(x);
            longitudeGridLines.get(i).setStartY(mapImageCanvas.getLayoutY() + mapImageCanvas.getLayoutBounds().getMinY());
            longitudeGridLines.get(i).setEndX(x);
            longitudeGridLines.get(i).setEndY(mapImageCanvas.getLayoutY() + mapImageCanvas.getLayoutBounds().getMaxY());
        }

//        if (selectionRectangle != null) {
//            selectionRectangle.setTranslateX(mapImageCanvas.getLayoutX());
//            selectionRectangle.setTranslateY(mapImageCanvas.getLayoutY());
//        }
    }

}
