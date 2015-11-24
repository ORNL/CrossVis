package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatterplotAxesRenderer extends Renderer {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public final static double NINETY_DEGREES = 90. * (Math.PI / 180.);
	public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"##0.0###");

	protected Column xColumn;
	protected Column yColumn;
	protected int xColumnIndex;
	protected int yColumnIndex;
	protected int plotSize;
	protected DataModel dataModel;
	protected ScatterplotConfiguration config;
	protected Graphics2D g2;
	protected int plot_left, plot_right, plot_bottom, plot_top;
	protected int left, right, bottom, top;
	protected Rectangle correlationRect;
	protected Color correlationColor;
	protected int axisSize;
	protected int tickSize;
	protected boolean antialias;

	public ScatterplotAxesRenderer(DataModel dataModel, Column xColumn,
			Column yColumn, int plotSize, int axisSize,
			ScatterplotConfiguration config, boolean antialias) {
		// super(dataModel, xColumn, yColumn, size, config, null);
		this.config = config;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.plotSize = plotSize;
		this.axisSize = axisSize;
		this.dataModel = dataModel;
		this.antialias = antialias;

		xColumnIndex = dataModel.getColumnIndex(xColumn);
		yColumnIndex = dataModel.getColumnIndex(yColumn);

		// setup image
		image = new BufferedImage(plotSize + axisSize + 1, plotSize + axisSize
				+ 1, BufferedImage.TYPE_INT_ARGB);
		g2 = (Graphics2D) image.getGraphics();

		if (antialias) {
			// log.debug("antialias is enabled");
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		g2.setFont(config.labelFont);

		left = 0;
//		right = plotSize+axisSize-1;
		right = plotSize - 1;
		top = 0;
//		bottom = plotSize+axisSize-1;
		bottom = plotSize - 1;

		plot_left = axisSize - 1;
		plot_right = right;
		plot_top = top;
		plot_bottom = plotSize - axisSize + 1;

		tickSize = axisSize / 2;

		correlationRect = new Rectangle(left - 1, plot_bottom + 2, axisSize,
				axisSize);

		// TODO: Make code pull real correlation coefficient from Data Model
		float corrCoef = 0.5f;
//		if (config.useQueryCorrelationCoefficient
//				&& dataModel.isColumnQuerySet()) {
//			corrCoef = xColumn.getQueryCorrelationCoefficients().get(
//					dataModel.getColumnIndex(yColumn));
//		} else {
//			corrCoef = xColumn.getCorrelationCoefficients().get(
//					dataModel.getColumnIndex(yColumn));
//		}
		// float corrCoef =
		// xColumn.getCorrelationCoefficients().get(dataModel.getColumnIndex(yColumn));
		correlationColor = Utilities.getColorForCorrelationCoefficient(corrCoef, 1.);
	}

	public Column getXColumn() {
		return xColumn;
	}

	public Column getYColumn() {
		return yColumn;
	}

	public int getSize() {
		return plotSize;
	}

	public DataModel getDataModel() {
		return dataModel;
	}

	public ScatterplotConfiguration getConfig() {
		return config;
	}

	public void run() {
		isRunning = true;

		g2.setColor(config.axisLineColor);

		// Draw correlation coefficient color block
		if (config.showCorrelationIndicator) {
			g2.setColor(correlationColor);
			g2.fill(correlationRect);
		}

		g2.setColor(config.axisLineColor);

		g2.drawLine(plot_left, plot_bottom, plot_left, plot_top);
		g2.drawLine(plot_left, plot_bottom, plot_right + 1, plot_bottom);
		g2.drawLine(plot_right + 1, plot_bottom, plot_right + 1, plot_top);
		g2.drawLine(plot_left, plot_top, plot_right + 1, plot_top);

		if (config.showTickMarks) {
			g2.drawLine(plot_left, plot_top, plot_left - tickSize, plot_top);
			g2.drawLine(plot_left, plot_bottom, plot_left - tickSize,
					plot_bottom);
			g2.drawLine(plot_left, plot_bottom, plot_left, plot_bottom
					+ tickSize);
			g2.drawLine(plot_right + 1, plot_bottom, plot_right + 1,
					plot_bottom + tickSize);
		}

		// Axis Label Drawing
		if (config.showAxisNames) {
			g2.setColor(config.labelColor);
			String yAxisString = yColumn.getName();
			String xAxisString = xColumn.getName();
			int yAxisCenter = plot_top + (plotSize / 2);
			int xAxisCenter = plot_left + (plotSize / 2);

			int stringWidth = g2.getFontMetrics().stringWidth(yAxisString);
			g2.rotate(-NINETY_DEGREES, plot_left
					- (g2.getFontMetrics().getHeight() / 2), yAxisCenter
					+ (stringWidth / 2));
			g2.drawString(yAxisString, plot_left
					- (g2.getFontMetrics().getHeight() / 2), yAxisCenter
					+ (stringWidth / 2));
			g2.rotate(NINETY_DEGREES, plot_left
					- (g2.getFontMetrics().getHeight() / 2), yAxisCenter
					+ (stringWidth / 2));
			stringWidth = g2.getFontMetrics().stringWidth(xAxisString);
			g2.drawString(xAxisString, xAxisCenter - (stringWidth / 2),
					plot_bottom + g2.getFontMetrics().getHeight());
		}

		if (config.showAxisLabels) {
			// Y Axis min and max labeling
			String valueString = DECIMAL_FORMAT.format(yColumn.getSummaryStats().getMax());
			int stringWidth = g2.getFontMetrics().stringWidth(valueString);
			g2.drawString(valueString, plot_left - stringWidth
					- (g2.getFontMetrics().getHeight() / 2), plot_top
					+ g2.getFontMetrics().getHeight());
			valueString = DECIMAL_FORMAT.format(yColumn.getSummaryStats().getMin());
			stringWidth = g2.getFontMetrics().stringWidth(valueString);
			g2.drawString(valueString, plot_left - stringWidth
					- (g2.getFontMetrics().getHeight() / 2), plot_bottom - 3);

			// X Axis min and max labeling
			valueString = DECIMAL_FORMAT.format(xColumn.getSummaryStats().getMin());
			stringWidth = g2.getFontMetrics().stringWidth(valueString);
			g2.drawString(valueString, plot_left + 3, plot_bottom
					+ g2.getFontMetrics().getHeight());
			valueString = DECIMAL_FORMAT.format(xColumn.getSummaryStats().getMax());
			stringWidth = g2.getFontMetrics().stringWidth(valueString);
			g2.drawString(valueString, plot_right - stringWidth - 3,
					plot_bottom + g2.getFontMetrics().getHeight());
		}
		
		isRunning = false;
		fireRendererFinished();
	}
}
