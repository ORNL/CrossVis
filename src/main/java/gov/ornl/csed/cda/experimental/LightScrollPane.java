package gov.ornl.csed.cda.experimental;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Created by csg on 4/22/16.
 */
public class LightScrollPane extends JComponent {

    private static final int SCROLL_BAR_ALPHA_ROLLOVER = 150;
    private static final int SCROLL_BAR_ALPHA = 100;
    private static final int THUMB_BORDER_SIZE = 2;
    private static final int THUMB_SIZE = 8;
    private static final Color THUMB_COLOR = Color.BLACK;

    private final JScrollPane scrollPane;
    private final JScrollBar verticalScrollBar;
    private final JScrollBar horizontalScrollBar;

    public LightScrollPane(JComponent component) {
        scrollPane = new JScrollPane(component);
        verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setVisible(false);
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUI(new MyScrollBarUI());

        horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setVisible(false);
        horizontalScrollBar.setOpaque(false);
        horizontalScrollBar.setUI(new MyScrollBarUI());

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayer(verticalScrollBar, JLayeredPane.PALETTE_LAYER);
        layeredPane.setLayer(horizontalScrollBar, JLayeredPane.PALETTE_LAYER);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                viewport.setBounds(0, 0, getWidth(), getHeight());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        displayScrollBarsIfNecessary(viewport);
                    }
                });
            }
        });

        layeredPane.add(horizontalScrollBar);
        layeredPane.add(verticalScrollBar);
        layeredPane.add(scrollPane);

        setLayout(new BorderLayout() {
            @Override
            public void layoutContainer(Container target) {
                super.layoutContainer(target);
                int width = getWidth();
                int height = getHeight();
                scrollPane.setBounds(0, 0, width, height);

                int scrollBarSize = 12;
                int cornerOffset = verticalScrollBar.isVisible() &&
                        horizontalScrollBar.isVisible() ? scrollBarSize : 0;
                if (verticalScrollBar.isVisible()) {
                    verticalScrollBar.setBounds(width - scrollBarSize, 0,
                            scrollBarSize, height - cornerOffset);
                }
                if (horizontalScrollBar.isVisible()) {
                    horizontalScrollBar.setBounds(0, height - scrollBarSize,
                            width - cornerOffset, scrollBarSize);
                }
            }
        });
        add(layeredPane, BorderLayout.CENTER);
        layeredPane.setBackground(Color.BLUE);
    }

    private void displayScrollBarsIfNecessary(JViewport viewPort) {
        displayVerticalScrollBarIfNecessary(viewPort);
        displayHorizontalScrollBarIfNecessary(viewPort);
    }

    private void displayVerticalScrollBarIfNecessary(JViewport viewPort) {
        Rectangle viewRect = viewPort.getViewRect();
        Dimension viewSize = viewPort.getViewSize();
        boolean shouldDisplayVerticalScrollBar =
                viewSize.getHeight() > viewRect.getHeight();
        verticalScrollBar.setVisible(shouldDisplayVerticalScrollBar);
    }

    private void displayHorizontalScrollBarIfNecessary(JViewport viewPort) {
        Rectangle viewRect = viewPort.getViewRect();
        Dimension viewSize = viewPort.getViewSize();
        boolean shouldDisplayHorizontalScrollBar =
                viewSize.getWidth() > viewRect.getWidth();
        horizontalScrollBar.setVisible(shouldDisplayHorizontalScrollBar);
    }

    private static class MyScrollBarButton extends JButton {
        private MyScrollBarButton() {
            setOpaque(false);
            setFocusable(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
        }
    }

    private static class MyScrollBarUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new MyScrollBarButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new MyScrollBarButton();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
            int orientation = scrollbar.getOrientation();
            int arc = THUMB_SIZE;
            int x = thumbBounds.x + THUMB_BORDER_SIZE;
            int y = thumbBounds.y + THUMB_BORDER_SIZE;

            int width = orientation == JScrollBar.VERTICAL ?
                    THUMB_SIZE : thumbBounds.width - (THUMB_BORDER_SIZE * 2);
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ?
                    thumbBounds.height - (THUMB_BORDER_SIZE * 2) : THUMB_SIZE;
            height = Math.max(height, THUMB_SIZE);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(THUMB_COLOR.getRed(),
                    THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
            graphics2D.fillRoundRect(x, y, width, height, arc, arc);
            graphics2D.dispose();
        }
    }
}
