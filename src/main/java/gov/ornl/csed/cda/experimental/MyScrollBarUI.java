package gov.ornl.csed.cda.experimental;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Created by csg on 4/22/16.
 */
public class MyScrollBarUI extends BasicScrollBarUI {

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        super.paintTrack(g, c, trackBounds);
        // your code
        Graphics2D g2 = (Graphics2D)g;
        int top = (trackBounds.height / 2) - 1;
        for (int i = trackBounds.x; i < trackBounds.width; i += 4) {
            g2.setColor(Color.blue);
            g2.fillRect(i, top, 2, 2);
        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        // your code
//        super.paintThumb(g, c, thumbBounds);
    }
}
