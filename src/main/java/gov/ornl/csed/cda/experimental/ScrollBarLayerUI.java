package gov.ornl.csed.cda.experimental;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;

/**
 * Created by csg on 4/22/16.
 */
public class ScrollBarLayerUI extends LayerUI<JComponent> {
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);

        Graphics2D g2 = (Graphics2D) g.create();

        int w = c.getWidth();
        int h = c.getHeight();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, .5f));
        g2.setPaint(new GradientPaint(0, 0, Color.yellow, 0, h, Color.red));
        g2.fillRect(0, 0, w, h);

        g2.dispose();
    }
}
