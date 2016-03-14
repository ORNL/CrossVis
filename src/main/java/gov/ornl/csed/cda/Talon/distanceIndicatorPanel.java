package gov.ornl.csed.cda.Talon;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.abs;

/**
 * Created by whw on 3/9/16.
 */
public class DistanceIndicatorPanel extends JComponent implements MouseListener {
    // ========== CLASS FIELDS ==========
    private TreeMap<Double, Double> segmentDistanceMap;
    private Double upperQuantile;
    private Double medianDistance;
    private Double lowerQuantile;
    private int tickMarksize = 3;
    private int tickMarkSpacing;
    private HashMap <Integer, Map.Entry<Double, Double>> displayedDistances;

    // ========== CONSTRUCTOR ==========
    public DistanceIndicatorPanel () {

    }


    // ========== METHODS ==========
    // Getters/Setters
    public void setDistanceMap(TreeMap<Double, Double> segmentDistanceMap) {
        this.segmentDistanceMap = segmentDistanceMap;
        setStats(segmentDistanceMap);
        displayedDistances.clear();
        repaint();
    }

    private void setStats(TreeMap<Double, Double> segmentDistanceMap) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            stats.addValue(entry.getValue());
        }

        upperQuantile = stats.getPercentile(75);
        medianDistance = stats.getPercentile(50);
        lowerQuantile = stats.getPercentile(25);

    }

    private Double findMaxDistance(TreeMap<Double, Double> segmentDistanceMap) {
        Double temp = 0.0;
        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            temp = (entry.getValue() > temp) ? entry.getValue() : temp;
        }
        System.out.println("max distance is: " + temp);
        return temp;
    }

    public void paintComponent(Graphics g) {

        // You will pretty much always do this for a vis
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());


        if (segmentDistanceMap != null && !segmentDistanceMap.isEmpty()) {
            int count = 1;

            double upperBound = upperQuantile + (upperQuantile-lowerQuantile)*1.5;
            double lowerBound = lowerQuantile - (upperQuantile-lowerQuantile)*1.5;
            lowerBound = (lowerBound < 0) ? 0 : lowerBound;

            tickMarkSpacing = this.getHeight()/segmentDistanceMap.size();

            // In order to print a tick mark per level tickMarkSpacing ≥ tickMarkSize
            if (tickMarkSpacing < tickMarksize) {

                tickMarkSpacing = tickMarksize;
                double max = 0;
                double maxKey = 0;
                int combine = 1;

                // TODO: Must combine multiple build height "distances" into a single tick mark. Will probably choose to do maximum magnitude from average distance
                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
                    if (abs(max) < abs(medianDistance - entry.getValue())) {
                        max = entry.getValue();
                        maxKey = entry.getKey();
                    }

                    if (combine % (segmentDistanceMap.size()/this.getHeight()) != 0) {
                        combine++;
                        continue;
                    }

                    g2.setColor(getColor(medianDistance, lowerBound, upperBound, entry.getValue()));

                    displayedDistances.put(this.getHeight() - tickMarkSpacing*count, segmentDistanceMap.ceilingEntry(maxKey));

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);
                    max = 0;
                    combine++;
                    count++;
                }

            } else {

                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                    g2.setColor(getColor(medianDistance, lowerBound, upperBound, entry.getValue()));

                    displayedDistances.put(this.getHeight() - tickMarkSpacing*count, entry);

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);
                    count++;
                }

            }
        }
    }

    public static Color getColor(double midpoint, double lowerThreshold, double upperThreshold, double value) {
        Color c;
        double norm;

        if (value == Double.NaN) {
            return null;
        }

        if (value > midpoint) {
            Color c0 = new Color(211, 37, 37); // high pos. corr.
            Color c1 = new Color(240, 240, 240); // low pos. corr.

            value = (value > upperThreshold) ? upperThreshold : value;
            norm = abs(value - midpoint) / abs(upperThreshold - midpoint);

            int r = c0.getRed()
                    + (int) (norm * (c1.getRed() - c0.getRed()));
            int green = c0.getGreen()
                    + (int) (norm * (c1.getGreen() - c0.getGreen()));
            int b = c0.getBlue()
                    + (int) (norm * (c1.getBlue() - c0.getBlue()));
            c = new Color(r, green, b);

        } else {
            Color c0 = new Color(44, 110, 211/* 177 */); // high neg. corr.
            Color c1 = new Color(240, 240, 240);// low neg. corr.

            value = (value < lowerThreshold) ? lowerThreshold : value;
            norm = abs(value - midpoint) / abs(lowerThreshold - midpoint);

            int r = c0.getRed()
                    + (int) (norm * (c1.getRed() - c0.getRed()));
            int green = c0.getGreen()
                    + (int) (norm * (c1.getGreen() - c0.getGreen()));
            int b = c0.getBlue()
                    + (int) (norm * (c1.getBlue() - c0.getBlue()));
            c = new Color(r, green, b);

        }

        return c;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setToolTipText("");

        int location = e.getY();
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
