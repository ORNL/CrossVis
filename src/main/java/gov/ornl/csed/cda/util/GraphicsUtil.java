package gov.ornl.csed.cda.util;


import javafx.scene.paint.Color;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by csg on 3/10/16.
 */
public class GraphicsUtil {
    static public final java.awt.Color lerpColor(java.awt.Color c1, java.awt.Color c2, double amount) {
        int opacity = (int)Math.round(c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * amount);
        int red = (int)Math.round(c1.getRed() + (c2.getRed() - c1.getRed()) * amount);
        int green = (int)Math.round(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * amount);
        int blue = (int)Math.round(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * amount);

        return new java.awt.Color (red, green, blue, opacity);
    }

    static public final Color lerpColorFX(Color c1, Color c2, double amount) {
        double opacity = (c1.getOpacity() + (c2.getOpacity() - c1.getOpacity()) * amount);
        double red = (c1.getRed() + (c2.getRed() - c1.getRed()) * amount);
        double green = (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * amount);
        double blue = (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * amount);

        return new Color (red, green, blue, opacity);
    }

    public static double mapValue(double value, double currentMin, double currentMax, double newMin, double newMax) {
        return newMin + ((newMax - newMin) * ((value - currentMin) / (currentMax - currentMin)));
//        double norm = (value - currentMin) / (currentMax - currentMin);
//        return (norm * (newMax - newMin)) + newMin;
    }

    public static double mapValue(Instant instant, Instant startInstant, Instant endInstant, double newMin, double newMax) {
        Duration totalDuration = Duration.between(startInstant, endInstant);
        Duration instantDuration = Duration.between(startInstant, instant);
        double norm = (double)instantDuration.toMillis() / (double)totalDuration.toMillis();
        return newMin + (norm * (newMax - newMin));
    }

    public static Instant mapValue(double value, double currentMin, double currentMax, Instant newStartInstant, Instant newEndInstant) {
        double norm = (value - currentMin) / (currentMax - currentMin);
        long deltaMillis = (long)(norm * Duration.between(newStartInstant, newEndInstant).toMillis());
        return newStartInstant.plusMillis(deltaMillis);
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
        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }

    static public final double lerp(double start, double stop, double amt) {
        return start + (stop-start) * amt;
    }

    static public final double norm(double value, double start, double stop) {
        return (value - start) / (stop - start);
    }

    static public final double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(((x2-x1)*(x2-x1)) + ((y2-y1)*(y2-y1)));
    }

    static public final double constrain(double amt, double low, double high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    static public final int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }
}
