package gov.ornl.csed.cda.util;

import javafx.scene.paint.Color;

/**
 * Created by csg on 3/10/16.
 */
public class GraphicsUtil {
    public static double mapValue(double value, double currentMin, double currentMax, double newMin, double newMax) {
        double norm = (value - currentMin) / (currentMax - currentMin);
        return (norm * (newMax - newMin)) + newMin;
    }

    public static java.awt.Color convertToAWTColor(javafx.scene.paint.Color color) {
        int r = (int)(color.getRed() * 255.0);
        int g = (int)(color.getGreen() * 255.0);
        int b = (int)(color.getBlue() * 255.0);
        int a = (int)(color.getOpacity() * 255.0);
        return new java.awt.Color(r, g, b, a);
    }

    public static javafx.scene.paint.Color convertToJavaFXColor (java.awt.Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        double opacity = a / 255.;
        return Color.rgb(r, g, b, opacity);
    }
}
