package gov.ornl.csed.cda.Talon;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by whw on 3/9/16.
 */
public class DistanceIndicatorPanel extends JComponent{
    // ========== CLASS FIELDS ==========
    private TreeMap<Double, Double> segmentDistanceMap;
    private Double maxDistance;
    private Double averageDistance;

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
            temp += entry.getValue();
        }
        temp /= segmentDistanceMap.size();
        return temp;
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
            for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                if (entry.getValue() < averageDistance) {
                    g2.setColor(new Color(0, 1, 0, (float) (entry.getValue() / averageDistance)));
                } else {
                    g2.setColor(new Color(1, 0, 0, (float) ((entry.getValue() - averageDistance) / (maxDistance - averageDistance))));
                }

                g2.fillRect(0, (int) ((segmentDistanceMap.lastKey() - entry.getKey()) * segmentDistanceMap.size() / count), 15, 5);
                count++;
            }
        }
    }
}
