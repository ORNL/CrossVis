package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.Tuple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterplotPointsRenderer extends Renderer {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	protected Column xColumn;
	protected Column yColumn;
	protected int xColumnIndex;
	protected int yColumnIndex;
	protected int size;
	protected DataModel dataModel;
	protected ScatterplotConfiguration config;
	protected Graphics2D g2;
	protected int left, right, bottom, top;
	protected Rectangle correlationRect;
	protected Color correlationColor;
	protected SimpleRegression simpleRegression;
	protected boolean showFocusPoints;
	protected boolean showContextPoints;
	protected boolean antialias;

	public ScatterplotPointsRenderer(DataModel dataModel, Column xColumn,
			Column yColumn, int plotSize, int axisSize,
			ScatterplotConfiguration config, SimpleRegression simpleRegression,
			boolean showFocusPoints, boolean showContextPoints,
			boolean antialias) {
		this.antialias = antialias;
		this.showFocusPoints = showFocusPoints;
		this.showContextPoints = showContextPoints;
		this.config = config;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.size = plotSize - axisSize - 4;
		this.dataModel = dataModel;
		this.simpleRegression = simpleRegression;

		xColumnIndex = dataModel.getColumnIndex(xColumn);
		yColumnIndex = dataModel.getColumnIndex(yColumn);

		// setup image
		image = new BufferedImage(size + 4, size + 4, BufferedImage.TYPE_INT_ARGB);
		g2 = (Graphics2D) image.getGraphics();
		if (antialias) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		g2.setFont(config.labelFont);

		// calculate lower left point
		left = 2;
		// bottom = (plotSize-axisSize)-1;
		// bottom = plotSize - axisSize;
		bottom = size - 2;

		// calculate upper right point
		// right = (plotSize-axisSize)-1;
		// right = plotSize - axisSize - 1;
		right = size - 2;
		top = 2;
	}

	public Column getXColumn() {
		return xColumn;
	}

	public Column getYColumn() {
		return yColumn;
	}

	public int getSize() {
		return size;
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public ScatterplotConfiguration getConfig() {
		return config;
	}

	public void run() {
		isRunning = true;
		// log.debug("Renderer thread " + this.getId() + " running...");

		// Draw a center line for x and y (diagnostic)
		// g2.setColor(Color.blue);
		// g2.drawLine(left, size/2, right, size/2);
		// g2.drawLine(size/2, top, size/2, bottom);
		//

		g2.setStroke(new BasicStroke(2.f));
		g2.setColor(config.pointColor);
		// log.debug(config.pointColor.getRed() + ", " +
		// config.pointColor.getGreen() + ", "
		// + config.pointColor.getBlue() + ", " + config.pointColor.getAlpha());

		// draw points
		for (int ituple = 0; ituple < dataModel.getTupleCount(); ituple++) {
			if (isRunning == false) {
				return;
			}
			Tuple currentTuple = dataModel.getTuple(ituple);

			if ((currentTuple.getQueryFlag() && !showFocusPoints)
					|| (!currentTuple.getQueryFlag() && !showContextPoints)) {
				continue;
			}

			float xValue;
			try {
				xValue = currentTuple.getElement(xColumnIndex);
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}

			int x = toScreenX(xValue, xColumn.getSummaryStats().getMin(), xColumn.getSummaryStats().getMax(), left, size);
			float yValue = currentTuple.getElement(yColumnIndex);
			int y = toScreenY(yValue, yColumn.getSummaryStats().getMin(), yColumn.getSummaryStats().getMax(), top, size);
			int offsetX = x - (int) (config.pointShape.getBounds2D().getWidth() / 2.);
			int offsetY = y - (int) (config.pointShape.getBounds2D().getWidth() / 2.);

			g2.translate(offsetX, offsetY);
			g2.draw(config.pointShape);
			g2.translate(-offsetX, -offsetY);
		}

		// draw regression line (trendline)
		if (config.showRegressionLine && simpleRegression != null) {
			if (isRunning == false) {
				return;
			}

			double startX = xColumn.getSummaryStats().getMin();
			double startY = simpleRegression.predict(startX);
			double endX = xColumn.getSummaryStats().getMax();
			double endY = simpleRegression.predict(endX);

			int start_ix = toScreenX((float) startX, xColumn.getSummaryStats().getMin(), xColumn.getSummaryStats().getMax(), left, size);
			int start_iy = toScreenY((float) startY, yColumn.getSummaryStats().getMin(), yColumn.getSummaryStats().getMax(), top, size);
			int end_ix = toScreenX((float) endX, xColumn.getSummaryStats().getMin(), xColumn.getSummaryStats().getMax(), left, size);
			int end_iy = toScreenY((float) endY, yColumn.getSummaryStats().getMin(), yColumn.getSummaryStats().getMax(), top, size);

			g2.setColor(Color.black);
			g2.drawLine(start_ix, start_iy, end_ix, end_iy);
		}

		isRunning = false;
		fireRendererFinished();
	}

	private int toScreenY(float value, float minValue, float maxValue, int offset, int plotHeight) {
		float normVal = 1.f - ((value - minValue) / (maxValue - minValue));
		int y = offset + (int) (normVal * plotHeight);
		return y;
	}

	private int toScreenX(float value, float minValue, float maxValue,
			int offset, int plotWidth) {
		float normVal = (value - minValue) / (maxValue - minValue);
		int x = offset + (int) Math.round(normVal * plotWidth);
		return x;
	}
}
