package gov.ornl.csed.cda.overviewbar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class OverviewBar extends JPanel implements ComponentListener, MouseListener, AdjustmentListener{
    private final static Logger log = LoggerFactory.getLogger(OverviewBar.class);

    private static final int BAR_WIDTH = 20;
    private static final int SCROLLBAR_VISIBLE_INDICATOR_WIDTH = 4;
    private static final int MARKER_HEIGHT = 6;
	private static final int MARKER_WIDTH = BAR_WIDTH - SCROLLBAR_VISIBLE_INDICATOR_WIDTH - 2;
//	private static final int MARKER_WIDTH = BAR_WIDTH - SCROLLBAR_VISIBLE_INDICATOR_WIDTH - 2;

	private JScrollPane scrollPane;
	private ArrayList<OverviewBarMarker> markerList;
    private ArrayList<OverviewBarListener> listeners = new ArrayList<OverviewBarListener>();
    private RoundRectangle2D scrollBarVisibleRectangle;
    private boolean fillVisibleRectangle = false;
    private double plotTopPosition = 0.;
    private double plotBottomPosition = 0.;
    private double plotHeight = 0.;
    private OverviewBarMarker highlightedMarker = null;

    public OverviewBar(JScrollPane scrollPane) {
		markerList = new ArrayList<OverviewBarMarker>();

		this.scrollPane = scrollPane;
        this.scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

		addComponentListener(this);
		addMouseListener(this);
		
		setMinimumSize(new Dimension(BAR_WIDTH, scrollPane.getHeight()));
		setPreferredSize(new Dimension(BAR_WIDTH, scrollPane.getHeight()));
		setMaximumSize(new Dimension(BAR_WIDTH, scrollPane.getHeight()));

        layoutBar();
	}

    public void setHighlightedMarker (OverviewBarMarker marker) {
        if (highlightedMarker != null) {
            highlightedMarker.setHighlighted(false);
        }
        highlightedMarker = marker;
        repaint();
    }

    public void addMarkers (Collection<OverviewBarMarker> markers) {
        markerList.addAll(markers);
        layoutBar();
        repaint();
    }

    public void addMarker(OverviewBarMarker marker) {
//        ListOverviewBarMarker marker = new ListOverviewBarMarker(position, color, color);
        Shape markerShape = getShapeForMarker(marker);
        marker.setMarkerShape(markerShape);
        if (marker.isHighlighted()) {
            if (highlightedMarker != null) {
                highlightedMarker.setHighlighted(false);
            }
            highlightedMarker = marker;
            highlightedMarker.setHighlighted(true);
        }
        markerList.add(marker);
        repaint();
    }

    public void removeMarker(OverviewBarMarker marker) {
        if (marker == highlightedMarker) {
            highlightedMarker = null;
        }
        markerList.remove(marker);
        layoutBar();
        repaint();
    }
	
	public void removeAllMarkers() {
		markerList.clear();
        highlightedMarker = null;
		repaint();
	}

    private Shape getShapeForMarker (OverviewBarMarker marker) {
        double markerCenterY = plotTopPosition + (marker.position * plotHeight);
        double markerTopY = markerCenterY - ((double)MARKER_HEIGHT/2.);
        RoundRectangle2D.Double markerShape = new RoundRectangle2D.Double(SCROLLBAR_VISIBLE_INDICATOR_WIDTH + 1,
                markerTopY, MARKER_WIDTH, MARKER_HEIGHT, 6., 6.);

//        double screenY = marker.position * (getHeight() - MARKER_HEIGHT);
//            Ellipse2D.Double markerShape = new Ellipse2D.Double(SCROLLBAR_VISIBLE_INDICATOR_WIDTH + 1, screenY, MARKER_WIDTH, MARKER_HEIGHT);
//        RoundRectangle2D.Double markerShape = new RoundRectangle2D.Double(SCROLLBAR_VISIBLE_INDICATOR_WIDTH + 1, screenY, MARKER_WIDTH, MARKER_HEIGHT, 4., 4.);
        return markerShape;
    }

    public void layoutBar() {
        plotTopPosition = (double)MARKER_HEIGHT/2.;
        plotHeight = getHeight() - MARKER_HEIGHT;
        plotBottomPosition = plotTopPosition + plotHeight;

        for (OverviewBarMarker marker : markerList) {
            Shape markerShape = getShapeForMarker(marker);
            marker.setMarkerShape(markerShape);
        }
    }
	
	public void paintComponent(Graphics g) {
//		System.out.println("ListOverviewBar.paint(): getWidth() = " + getWidth() + " getHeight() = " + getHeight());
		Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

        Color thumbColor = (Color)UIManager.get("ScrollBar.track");
        g2.setColor(thumbColor);
//        g2.draw(scrollBarVisibleRectangle);
        g2.fill(scrollBarVisibleRectangle);
        g2.setColor(Color.darkGray);
        g2.draw(scrollBarVisibleRectangle);
//        Line2D.Double line = new Line2D.Double(scrollBarVisibleRectangle.getX(), scrollBarVisibleRectangle.getY(), scrollBarVisibleRectangle.getX(), scrollBarVisibleRectangle.getMaxY());
//        g2.draw(line);

        Iterator<OverviewBarMarker> iter = markerList.iterator();
		while (iter.hasNext()) {
			OverviewBarMarker marker = iter.next();
            drawMarker(g2, marker);
//            log.debug("marker position: " + marker.position);
//
//			float screenY = marker.position * (getHeight() - BAR_HEIGHT);
//			Rectangle2D.Float markerRectangle = new Rectangle2D.Float(0.f, screenY, BAR_WIDTH, BAR_HEIGHT);
////			int y_pos = (int)(0.5f + (marker.position * (getHeight()));
//			g2.setColor(marker.color);
////			g2.fillRect(1, y_pos-(BAR_HEIGHT/2), BAR_WIDTH-2, BAR_HEIGHT);
//			g2.fill(markerRectangle);
//			g2.setColor(Color.black);
//			g2.draw(markerRectangle);
//			g2.drawRect(1, y_pos-(BAR_HEIGHT/2), BAR_WIDTH-2, BAR_HEIGHT);
		}

//        g2.setColor(Color.black);
//        g2.drawLine(0, (int)plotTopPosition, getWidth(), (int)plotTopPosition);
//        g2.drawLine(0, (int)plotBottomPosition, getWidth(), (int)plotBottomPosition);
    }

    private void drawMarker(Graphics2D g2, OverviewBarMarker marker) {
//        double screenY = marker.position * (getHeight() - MARKER_HEIGHT);
//        Ellipse2D.Double markerShape = new Ellipse2D.Double(SCROLLBAR_VISIBLE_INDICATOR_WIDTH + 1, screenY, MARKER_WIDTH, MARKER_HEIGHT);

        g2.setColor(marker.fillColor);
        g2.fill(marker.getMarkerShape());

        g2.setColor(marker.outlineColor);
        if (marker.isSelected()) {
            if (marker.isHighlighted()) {
                g2.setStroke(new BasicStroke(2.f));
            } else {
                g2.setStroke(new BasicStroke(1.f));
            }
            g2.draw(marker.getMarkerShape());
        }

        if (!marker.isVisible()) {
            Line2D.Double line = new Line2D.Double(marker.getMarkerShape().getBounds2D().getMinX(),
                    marker.getMarkerShape().getBounds2D().getMaxY(), marker.getMarkerShape().getBounds().getMaxX(),
                    marker.getMarkerShape().getBounds2D().getMinY());
            g2.draw(line);
        }
    }

	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
        layoutBar();
        repaint();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

    public void addOverviewBarListener(OverviewBarListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeOverviewBarListener(OverviewBarListener listener) {
        listeners.remove(listener);
    }

    private void fireOverviewBarMarkerClicked (OverviewBarMarker marker) {
        for (OverviewBarListener listener : listeners) {
            listener.overviewBarMarkerClicked(this, marker);
        }
    }

    private void fireOverviewBarMarkerDoubleClicked (OverviewBarMarker marker) {
        for (OverviewBarListener listener : listeners) {
            listener.overviewBarMarkerDoubleClicked(this, marker);
        }
    }

    private void fireOverviewBarMarkerControlClicked (OverviewBarMarker marker) {
        for (OverviewBarListener listener : listeners) {
            listener.overviewBarMarkerControlClicked(this, marker);
        }
    }

	@Override
	public void mouseClicked(MouseEvent event) {
        int y = event.getPoint().y;
        float pos = (float)y/getHeight();

        OverviewBarMarker mouseOverMarker = null;
        for (OverviewBarMarker marker : markerList) {
            if (marker.getMarkerShape().contains(event.getX(), event.getY())) {
                mouseOverMarker = marker;
                break;
            }
        }

        if (mouseOverMarker == null) {
            return;
        }

        if (event.getClickCount() == 1) {
            if (event.isControlDown()) {
                fireOverviewBarMarkerControlClicked(mouseOverMarker);
            } else {
                fireOverviewBarMarkerClicked(mouseOverMarker);
            }
        } else if (event.getClickCount() >= 2) {
            fireOverviewBarMarkerDoubleClicked(mouseOverMarker);
//			int y = event.getPoint().y;
//			float pos = (float)y/getHeight();
//			int scrollViewHeight = scrollPane.getVerticalScrollBar().getMaximum();
//			int scrollY =  (int)(pos * scrollViewHeight) - (scrollPane.getVerticalScrollBar().getVisibleAmount()/2);
//			scrollPane.getVerticalScrollBar().setValue(scrollY);
        }

        repaint();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == scrollPane.getVerticalScrollBar()) {
//            log.debug("Scrollbar changed");

            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

//            log.debug("model.value = " + scrollBar.getModel().getValue() + "  model.extent = " + scrollBar.getModel().getExtent());
            double scrollBarModelHeight = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
            double normTop = (double)scrollBar.getModel().getValue() / scrollBarModelHeight;
            double normHeight = (double)scrollBar.getModel().getExtent() / scrollBarModelHeight;

            double top = normTop * getHeight();
            double height = normHeight * getHeight();

//            log.debug("normTop = " + normTop + " top = " + top + " normHeight = " + normHeight + " height = " + height);
//            scrollBarVisibleRectangle = new Rectangle2D.Double(0., top, getWidth(), height);
//            scrollBarVisibleRectangle = new Rectangle2D.Double(0., top, SCROLLBAR_VISIBLE_INDICATOR_WIDTH-1, height);

            scrollBarVisibleRectangle = new RoundRectangle2D.Double(0., top, SCROLLBAR_VISIBLE_INDICATOR_WIDTH-1, height, 4., 4.);
//            scrollBarVisibleRectangle = new RoundRectangle2D.Double(1., top, getWidth()-1, height, 10., 10.);

            repaint();
        }
    }
}
