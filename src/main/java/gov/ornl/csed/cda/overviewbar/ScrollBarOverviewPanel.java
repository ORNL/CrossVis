package gov.ornl.csed.cda.overviewbar;

import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.geometry.Orientation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

/**
 * Created by csg on 4/22/16.
 */
public class ScrollBarOverviewPanel extends JComponent implements ComponentListener, MouseListener,
        MouseMotionListener, AdjustmentListener {

    private JScrollBar scrollBar;
    private int size = 20;
    private int markerSize = 6;

    private RoundRectangle2D scrollbarViewportBounds;
    private Rectangle paintRegionBounds;
    private Insets margins = new Insets(3,3,3,3);

    private ArrayList<OverviewBarMarker> markerList = new ArrayList<>();
    private ArrayList<OverviewBarListener> listeners = new ArrayList<>();

    public ScrollBarOverviewPanel (JScrollBar scrollBar) {
        this.scrollBar = scrollBar;

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        scrollBar.addAdjustmentListener(this);

        if (scrollBar.getOrientation() == JScrollBar.HORIZONTAL) {
            setPreferredSize(new Dimension(getWidth(), size));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, size));
            setMinimumSize(new Dimension(size, size));
        } else {
            setPreferredSize(new Dimension(size, getHeight()));
            setMaximumSize(new Dimension(size, Integer.MAX_VALUE));
            setMinimumSize(new Dimension(size, size));
        }

        layoutPanel();
    }

    private void layoutPanel() {
        paintRegionBounds = new Rectangle(margins.left + getInsets().left, margins.top + getInsets().top,
                getWidth() - (margins.left + margins.right + getInsets().left + getInsets().right),
                getHeight() - (margins.top + margins.right + getInsets().top + getInsets().bottom));
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        Color thumbColor = (Color)UIManager.get("ScrollBar.track");
        g2.setColor(thumbColor);
        g2.fill(scrollbarViewportBounds);
//        g2.setColor(Color.darkGray);
//        g2.draw(scrollbarViewportBounds);

//        g2.setColor(Color.blue);
//        g2.draw(paintRegionBounds);

        g2.dispose();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

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

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (scrollBar.getOrientation() == JScrollBar.HORIZONTAL) {
            double modelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
            double left = GraphicsUtil.mapValue(scrollBar.getModel().getValue(),
                    scrollBar.getModel().getMinimum(), scrollBar.getModel().getMaximum(),
                    paintRegionBounds.getMinX(), paintRegionBounds.getMaxX());
            double right = GraphicsUtil.mapValue(scrollBar.getModel().getValue()+scrollBar.getModel().getExtent(),
                    scrollBar.getModel().getMinimum(), scrollBar.getModel().getMaximum(),
                    paintRegionBounds.getMinX(), paintRegionBounds.getMaxX());
            scrollbarViewportBounds = new RoundRectangle2D.Double(left, 0., right-left, 8., 8., 8.);
            repaint();
        } else {
            double modelHeight = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
