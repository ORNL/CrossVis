package gov.ornl.eden;

import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.Tuple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewLinesRenderer extends Renderer {
	private static final Logger log = LoggerFactory.getLogger(Utilities.class);
	public static final Color START_LINE_COLOR = Color.blue;
	public static final Color END_LINE_COLOR = PCPanel.DEFAULT_FOCUS_LINE_COLOR;
	private DataModel dataModel;
	private int width;
	private int height;
	private int axisBarWidth;
	private int axisBarWidthHalf;
	private boolean showAxesAsBars;
	private ArrayList<Tuple> newTuples;
	private int fadeSteps = 20;
	private int fadeCounter = 10;

	public NewLinesRenderer(DataModel dataModel, int width, int height,
			int axisBarWidth, boolean showAxisAsBars, ArrayList<Tuple> newTuples) {
		this.dataModel = dataModel;
		this.width = width;
		this.height = height;
		this.axisBarWidth = axisBarWidth;
		axisBarWidthHalf = axisBarWidth / 2;
		this.showAxesAsBars = showAxesAsBars;
		this.newTuples = newTuples;
	}

	public int getFadeCounter() {
		return fadeCounter;
	}

	public Color getColor(int currentStep, int numSteps) {
		float norm = (float) currentStep / numSteps;
		Color c1 = START_LINE_COLOR;
		Color c0 = END_LINE_COLOR;

		int r = c0.getRed() + (int) (norm * (c1.getRed() - c0.getRed()));
		int green = c0.getGreen()
				+ (int) (norm * (c1.getGreen() - c0.getGreen()));
		int b = c0.getBlue() + (int) (norm * (c1.getBlue() - c0.getBlue()));
		// int alpha = c0.getAlpha() + (int)(norm*(c1.getAlpha() -
		// c0.getAlpha()));
		int alpha = (int) (norm * (255));
		// Color c = new Color(r, green, b, alpha);
		Color c = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), alpha);
		// log.debug("norm = " + norm + " currentStep = " + currentStep +
		// " numSteps = " + numSteps + c.toString());
		// log.debug("alpha = " + alpha);
		return c;
	}

	public void run() {
		isRunning = true;
		// log.debug("started rendering");

		for (fadeCounter = fadeSteps; fadeCounter >= 0; fadeCounter--) {
			BufferedImage newImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);

			if (!isRunning)
				return;

			Graphics2D g2 = (Graphics2D) newImage.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getColor(fadeCounter, fadeSteps));
			g2.setStroke(new BasicStroke(2.f));

			for (int iTuple = 0; iTuple < newTuples.size(); iTuple++) {
				if (!isRunning) {
					return;
				}

				Tuple tuple = newTuples.get(iTuple);

				/*
				 * if (showAxesAsBars) { int x0 = tuple.getValueXPosition(0) +
				 * axisBarWidthHalf; int y0 = tuple.getValueYPosition(0); for
				 * (int i = 1; i < tuple.getElementCount(); i++) { int x1 =
				 * tuple.getValueXPosition(i) - axisBarWidthHalf; int y1 =
				 * tuple.getValueYPosition(i); g2.drawLine(x0, y0, x1, y1); x0 =
				 * tuple.getValueXPosition(i) + axisBarWidthHalf; y0 = y1; } }
				 * else { Path2D.Float polyline = tuple.getPCPolyline();
				 * g2.draw(polyline); }
				 */
			}

			if (isRunning) {
				image = newImage;
				// log.debug("firing");
				fireRendererFinished();
			}

			try {
				sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		isRunning = false;
	}
}
