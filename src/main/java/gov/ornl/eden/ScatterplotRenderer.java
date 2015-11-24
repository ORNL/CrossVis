package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public abstract class ScatterplotRenderer extends Renderer {
	protected Column xColumn;
	protected Column yColumn;
	protected int xColumnIndex;
	protected int yColumnIndex;
	protected int size;
	protected DataModel dataModel;
	protected ScatterplotConfiguration config;
	protected Graphics2D g2;
	protected int x0, x1, y0, y1;
	protected int plotAreaSize;
	protected Rectangle correlationRect;
	protected Color correlationColor;
	protected SimpleRegression simpleRegression;

	public ScatterplotRenderer(DataModel dataModel, Column xColumn,
			Column yColumn, int size, ScatterplotConfiguration config,
			SimpleRegression simpleRegression) {
		this.config = config;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.size = size;
		this.dataModel = dataModel;
		this.simpleRegression = simpleRegression;

		xColumnIndex = dataModel.getColumnIndex(xColumn);
		yColumnIndex = dataModel.getColumnIndex(yColumn);

		// setup image
		image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		g2 = (Graphics2D) image.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setFont(config.labelFont);

		// calculate lower left point location
		x0 = config.borderSize + g2.getFontMetrics().getHeight();
		y0 = size - config.borderSize - g2.getFontMetrics().getHeight();
		// x0 = config.borderSize + g2.getFontMetrics().getHeight() +
		// (int)(config.pointShape.getBounds2D().getWidth()/2);
		// y0 = size - config.borderSize - g2.getFontMetrics().getHeight() -
		// (int)(config.pointShape.getBounds2D().getHeight()/2);

		// calculate plot area size
		int maxShapeDimension = (int) Math.max(config.pointShape.getBounds2D()
				.getWidth(), config.pointShape.getBounds2D().getHeight());
		plotAreaSize = size
				- ((config.borderSize * 2) + g2.getFontMetrics().getHeight() + maxShapeDimension);

		// calculate the upper right point
		x1 = x0 + plotAreaSize;
		y1 = y0 - plotAreaSize;

		// calculate correlation rectangle
		correlationRect = new Rectangle(config.borderSize, size
				- config.borderSize, x0 - config.borderSize, y0
				- config.borderSize);
		// correlationRect = new Rectangle(config.borderSize,
		// size-config.borderSize-g2.getFontMetrics().getHeight(),
		// g2.getFontMetrics().getHeight(),
		// g2.getFontMetrics().getHeight());

		float corrCoef = xColumn.getSummaryStats().getCorrelationCoefficients().get(dataModel.getColumnIndex(yColumn));
		correlationColor = Utilities.getColorForCorrelationCoefficient(corrCoef, 1.);

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
}
