package gov.ornl.csed.cda.util;

/**
 * Created by csg on 3/10/16.
 */
public class GraphicsUtil {
    public static double mapValue(double value, double currentMin, double currentMax, double newMin, double newMax) {
        double norm = (value - currentMin) / (currentMax - currentMin);
        return (norm * (newMax - newMin)) + newMin;
    }
}
