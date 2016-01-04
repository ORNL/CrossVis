package gov.ornl.csed.cda.pcvis;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.Tuple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCLineRenderer extends Renderer {
	private final Logger log = LoggerFactory.getLogger(PCLineRenderer.class);

	public static final Ellipse2D.Float DOT_SHAPE = new Ellipse2D.Float(0f, 0f,
			6f, 6f);
	public static final int DOT_SHAPE_WIDTH_HALF = (int) (DOT_SHAPE.width / 2.);

	private DataModel dataModel;
	private int width;
	private int height;
	private int axisBarWidth;
	private int axisBarWidthHalf;
	private boolean showAxesAsBars;
	private int axisSpacing;
	private Random rand;
	private Color lineColor;
	private boolean showFocusLines;
	private boolean showContextLines;
	private ArrayList<Point[]> tupleLines;
	private boolean antialias;
	private int lineSize;

	public PCLineRenderer(DataModel dataModel, int width, int height,
			int axisBarWidth, int axisSpacing, boolean showAxesAsBars,
			Color lineColor, boolean showFocusLines, boolean showContextLines,
			boolean antialias, ArrayList<Point[]> tupleLines, int lineSize) {
		this.showFocusLines = showFocusLines;
		this.showContextLines = showContextLines;
		this.dataModel = dataModel;
		this.width = width;
		this.height = height;
		this.antialias = antialias;
		this.axisSpacing = axisSpacing / 6;
		rand = new Random(System.currentTimeMillis());
		this.lineColor = lineColor;
		this.tupleLines = tupleLines;
		this.axisBarWidth = axisBarWidth;
		this.showAxesAsBars = showAxesAsBars;
		this.lineSize = lineSize;
		if (showAxesAsBars) {
			axisBarWidthHalf = axisBarWidth / 2;
		} else {
			axisBarWidthHalf = 0;
		}
	}

	public void run() {
		// log.debug("Starting rendering function " + this.getId());
		isRunning = true;

		int linesDrawn = 0;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		if (!isRunning)
			return;

		Graphics2D g2 = (Graphics2D) image.getGraphics();
		if (antialias) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		g2.setColor(lineColor);
		g2.setStroke(new BasicStroke(lineSize));

		// for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
		for (int i = 0; i < tupleLines.size(); i++) {
			if (!isRunning) {
				// log.debug("Exited rendering loop " + this.getId());
				return;
			}

			Tuple tuple = dataModel.getTuple(i);
//			log.debug("tuple query flag is " + tuple.getQueryFlag());

			if ((tuple.getQueryFlag() && !showFocusLines) || (!tuple.getQueryFlag() && !showContextLines)) {
//				log.debug("skipping tuple drawing");
				continue;
			}

			
			Point[] tuplePoints = tupleLines.get(i);
			int x0 = tuplePoints[0].x + axisBarWidthHalf;
			int y0 = tuplePoints[0].y;
			for (int j = 1; j < tuplePoints.length; j++) {
				int x1 = tuplePoints[j].x - axisBarWidthHalf;
				int y1 = tuplePoints[j].y;
				g2.drawLine(x0, y0, x1, y1);
				x0 = tuplePoints[j].x + axisBarWidthHalf;
				y0 = y1;
			}
			
			linesDrawn++;
		}

		if (isRunning) {
			isRunning = false;
			fireRendererFinished();
		}
		
//		log.debug("drew " + linesDrawn + " lines [showFocusLines = " + showFocusLines + "]");
	}
}