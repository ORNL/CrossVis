package gov.ornl.csed.cda.Talon;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

/**
 * Created by whw on 3/9/16.
 */
public class DistanceIndicatorPanel extends JComponent implements MouseMotionListener {
    // ========== CLASS FIELDS ==========
    private static TreeMap<Double, Double> segmentDistanceMap;
    private static double ninetyNinthQuantile;
    private static double median;
    private static int tickMarksize = 5;
    private static int tickMarkSpacing;

    private int option;
    private TreeMap <Integer, Map.Entry<Double, Double>> displayedDistances = new TreeMap<>();

    // ========== CONSTRUCTOR ==========
    public DistanceIndicatorPanel () {
        this.option = 0;
        addMouseMotionListener(this);
    }

    public DistanceIndicatorPanel (int option) {
        this.option = option;
        addMouseMotionListener(this);
    }


    // ========== METHODS ==========
    // Getters/Setters
    public static void setDistanceMap(TreeMap<Double, Double> segmentDistanceMap) {
        DistanceIndicatorPanel.segmentDistanceMap = segmentDistanceMap;
        setStats(segmentDistanceMap);
        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()){
            segmentDistanceMap.replace(entry.getKey(), entry.getValue(), entry.getValue()/ ninetyNinthQuantile);
        }
    }

    private static void setStats(TreeMap<Double, Double> segmentDistanceMap) {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
            stats.addValue(entry.getValue());
        }

        ninetyNinthQuantile = stats.getPercentile(99.9);
        median = stats.getPercentile(50);
//        ninetyNinthQuantile = segmentDistanceMap.get(segmentDistanceMap.lowerKey(segmentDistanceMap.));
//        System.out.println("second highest value is " + ninetyNinthQuantile);
    }

    public void resetDisplay() {
        if (displayedDistances != null && !displayedDistances.isEmpty()) {
            displayedDistances.clear();
        }
        repaint();
    }

    public void paintComponent(Graphics g) {

        // You will pretty much always do this for a vis
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        tickMarksize = 5;

        if (segmentDistanceMap != null && !segmentDistanceMap.isEmpty()) {
            int count = 1;

            tickMarkSpacing = this.getHeight()/segmentDistanceMap.size();

            // In order to print a tick mark per level tickMarkSpacing ≥ tickMarkSize
            if (tickMarkSpacing < tickMarksize) {

//                System.out.println("screen size = " + this.getHeight() + " but visible is " + this.getVisibleRect().getHeight() + " and  " + segmentDistanceMap.size() + " elements");
                tickMarkSpacing = tickMarksize;
                Map.Entry<Double, Double> me = segmentDistanceMap.firstEntry();
                double max = me.getValue();
                int combine = 1;

                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                    switch (option) {
                        case 1:
                            if (abs(max) < abs(entry.getValue())) {
                                max = entry.getValue();
                                me = entry;
                            }
                            g2.setColor(getColor(me.getValue()));
                            break;

                        case 2:
                            if (abs(max) > abs(entry.getValue())) {
                                max = entry.getValue();
                                me = entry;
                            }
                            g2.setColor(getColor(-me.getValue()));
                            break;

                        default:
                            break;
                    }

                    if (combine % (floor((float)segmentDistanceMap.size()/floor((float)this.getHeight()/tickMarksize))) != 0 && combine != segmentDistanceMap.size()) {
                        combine++;
                        continue;
                    }

                    displayedDistances.put(this.getHeight() - tickMarkSpacing*count, me);

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);

                    me = segmentDistanceMap.higherEntry(entry.getKey());

                    if (me != null) {
                        max = me.getValue();
                    }

                    combine++;
                    count++;
                }

            } else {

                for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                    switch (option) {
                        case 1:

                            g2.setColor(getColor(entry.getValue()));
                            break;

                        case 2:

                            g2.setColor(getColor(-entry.getValue()));
                            break;

                        default:
                            break;
                    }

                    displayedDistances.put(this.getHeight() - tickMarkSpacing*count, entry);

                    g2.fillRect(0, this.getHeight() - tickMarkSpacing*count, 15, tickMarksize);
                    count++;
                }

            }

//            System.out.println("displaying distances for " + count + " objects");
        }
    }

    public Color getColor(double value) {
        Color c;
        double norm;

        if (value == Double.NaN) {
            return null;
        }

        if (this.option == 1) {
            Color c1 = new Color(211, 37, 37); // high pos. corr.
            Color c0 = new Color(240, 240, 240); // low pos. corr.

            value = (value > 1) ? 1 : value;
            norm = value;

            int r = c0.getRed()
                    + (int) (norm * (c1.getRed() - c0.getRed()));
            int green = c0.getGreen()
                    + (int) (norm * (c1.getGreen() - c0.getGreen()));
            int b = c0.getBlue()
                    + (int) (norm * (c1.getBlue() - c0.getBlue()));
            c = new Color(r, green, b);

        } else {
            Color c1 = new Color(44, 110, 211/* 177 */); // high neg. corr.
            Color c0 = new Color(240, 240, 240);// low neg. corr.

            value = -ninetyNinthQuantile / median * value;
            value = (value > 1) ? 1 : value;
            norm = value;

            int r = c0.getRed()
                    + (int) ((1-norm) * (c1.getRed() - c0.getRed()));
            int green = c0.getGreen()
                    + (int) ((1-norm) * (c1.getGreen() - c0.getGreen()));
            int b = c0.getBlue()
                    + (int) ((1-norm) * (c1.getBlue() - c0.getBlue()));
            c = new Color(r, green, b);

        }

        return c;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setToolTipText("");

        int location = e.getY();

        if (displayedDistances.floorEntry(location) != null) {
            setToolTipText("Build Height: " + displayedDistances.floorEntry(location).getValue().getKey() + "\nDistance: " + displayedDistances.floorEntry(location).getValue().getValue());
        }
    }
}
