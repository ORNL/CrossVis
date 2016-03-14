package gov.ornl.csed.cda.Talon;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.abs;

/**
 * Created by whw on 3/9/16.
 */
public class DistanceIndicatorPanel extends JComponent{
    // ========== CLASS FIELDS ==========
    private TreeMap<Double, Double> segmentDistanceMap;
    private Double maxDistance;
    private Double averageDistance;
    private int tickMarksize = 3;
    private int tickMarkSpacing;

    // ========== CONSTRUCTOR ==========
    public DistanceIndicatorPanel () {

    }

    // ========== METHODS ==========
    // Getters/Setters
    public void setDistanceMap(TreeMap<Double, Double> segmentDistanceMap) {
        this.segmentDistanceMap = segmentDistanceMap;
        maxDistance = findMaxDistance(segmentDistanceMap);
        averageDistance = findAveragedDistance(segmentDistanceMap);
        repaint();
    }

    private Double findAveragedDistance(TreeMap<Double, Double> segmentDistanceMap) {
        Double temp = 0.0;
        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            if (entry.getValue() == maxDistance) {
                continue;
            }
            temp += entry.getValue();
        }
        temp /= segmentDistanceMap.size()-1;
        return temp;
    }

    private Double findMaxDistance(TreeMap<Double, Double> segmentDistanceMap) {
        Double temp = 0.0;
        Double temp2 = 0.0;
        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            temp = (entry.getValue() > temp) ? entry.getValue() : temp;
        }
        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            temp2 = (entry.getValue() > temp2 && entry.getValue() != temp) ? entry.getValue() : temp2;
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

            tickMarkSpacing = this.getHeight()/segmentDistanceMap.size();

            // In order to print a tick mark per level tickMarkSpacing ≥ tickMarkSize
            if (tickMarkSpacing < tickMarksize) {

                tickMarkSpacing = tickMarksize;
                double max = 0;
                int combine = 1;

                // TODO: Must combine multiple build height "distances" into a single tick mark. Will probably choose to do maximum magnitude from average distance
                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
                    max = (abs(max) < abs(averageDistance - entry.getValue())) ? entry.getValue() : max;

                    if (combine % (segmentDistanceMap.size()/this.getHeight()) != 0) {
                        combine++;
                        continue;
                    }

                    if (max < averageDistance) {
                        g2.setColor(new Color(0, 1, 0, (float) ((averageDistance - max) / averageDistance)));
                    } else {
                        if (max > maxDistance) {
                            g2.setColor(new Color(1, 0, 0, 0));
                        } else {
                            g2.setColor(new Color(1, 0, 0, (float) ((max - averageDistance) / (maxDistance - averageDistance))));
                        }
                    }

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);
                    max = 0;
                    combine++;
                    count++;
                }

            } else {

                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                    if (entry.getValue() < averageDistance) {
                        g2.setColor(new Color(0, 1, 0, (float) ((averageDistance - entry.getValue()) / averageDistance)));
                    } else {
                        if (entry.getValue() > maxDistance) {
                            g2.setColor(new Color(1, 0, 0, 0));
                        } else {
                            g2.setColor(new Color(1, 0, 0, (float) ((entry.getValue() - averageDistance) / (maxDistance - averageDistance))));
                        }
                    }

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);
                    count++;
                }

            }
        }
    }
}
