package gov.ornl.geoview;

public class GeoRectangle {
    public double leftLongitude;
    public double rightLongitude;
    public double bottomLatitude;
    public double topLatitude;

    public GeoRectangle(double left, double right, double bottom, double top) {
        leftLongitude = left;
        rightLongitude = right;
        bottomLatitude = bottom;
        topLatitude = top;
    }
}
