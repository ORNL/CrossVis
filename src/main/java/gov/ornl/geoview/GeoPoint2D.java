package gov.ornl.geoview;

import javafx.geometry.Point2D;

public class GeoPoint2D extends Point2D {

    public GeoPoint2D(double longitude, double latitude) {
        super(longitude, latitude);
    }

    public double getLongitude() {
        return getX();
    }

    public double getLatitude() {
        return getY();
    }
}
