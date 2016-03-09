package gov.ornl.csed.cda.Talon;

import javax.swing.*;
import java.awt.*;

/**
 * Created by whw on 3/9/16.
 */
public class DistanceIndicatorPanel extends JComponent{
    // ========== CLASS FIELDS ==========

    // ========== CONSTRUCTOR ==========
    public DistanceIndicatorPanel () {

    }

    // ========== METHODS ==========
    // Getters/Setters

    public void paintComponent(Graphics g) {

        // You will pretty much always do this for a vis
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
