package gov.ornl.eden;

import gov.ornl.datatable.DataModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCAxesRenderer extends Renderer {
	private final Logger log = LoggerFactory.getLogger(PCAxesRenderer.class);
	//
	// public static final Font RANGE_VALUE_FONT = new Font("Dialog",
	// Font.PLAIN, 8);
	// public static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 10);

	// public static final Font RANGE_VALUE_FONT = new Font("Dialog",
	// Font.PLAIN, 12);
	// public static final Font TITLE_FONT = new Font("Dialog", Font.BOLD, 14);
	public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"##0.0#######");
	public final static Color AXIS_BAR_FILL_COLOR = new Color(245, 245, 245);
	public final static Color AXIS_BAR_LINE_COLOR = new Color(120, 120, 120);
	public final static Color IQR_FILL_COLOR = new Color(220, 220, 220);
	public final static Color IQR_LINE_COLOR = new Color(170, 170, 221);
	// public final static Color IQR_LINE_COLOR = new Color(140, 140, 191);
	public final static Color QUERY_IQR_FILL_COLOR = new Color(220, 220, 220);
	public final static Color QUERY_IQR_LINE_COLOR = new Color(85, 85, 102);
	public final static Color HISTOGRAM_BIN_LINE_COLOR = new Color(120, 120,
			120, 180);
	public final static Color HISTOGRAM_BIN_FILL_COLOR = new Color(190, 190,
			241, 200);
	public final static int ARROW_WIDTH = 2;
	public final static int ARROW_HEIGHT = ARROW_WIDTH;
	public final static double NINETY_DEGREES = 90. * (Math.PI / 180.);

	private Font secondaryFont;
	private Font titleFont;

	private DataModel dataModel;
	private int screenWidth;
	private int screenHeight;
	private ArrayList<PCAxis> axisList;

	private int axisBarWidth;
	private int dispersionDisplayMode;
	private boolean showAxesAsBars;
	private boolean showFrequencyInfo;

	private int axisSpacing;
	private boolean showCorrelationIndicators;
	private boolean useQueryCorrelations;
	private boolean useQueryFrequency;
	private boolean antialias;
	private int correlationIndicatorHeight;

	public PCAxesRenderer(DataModel dataModel, int screenWidth,
			int screenHeight, int axisBarWidth, boolean showAxesAsBars,
			boolean showFrequencyInfo, int dispersionDisplayMode,
			ArrayList<PCAxis> axisList, int axisSpacing,
			boolean showCorrelationIndicators, boolean useQueryCorrelations,
			boolean useQueryFrequency, boolean antialias, Font titleFont,
			Font secondaryFont, int correlationIndicatorHeight) {
		this.correlationIndicatorHeight = correlationIndicatorHeight;
		this.titleFont = titleFont;
		this.secondaryFont = secondaryFont;
		this.showCorrelationIndicators = showCorrelationIndicators;
		this.useQueryCorrelations = useQueryCorrelations;
		this.axisSpacing = axisSpacing;
		this.dispersionDisplayMode = dispersionDisplayMode;
		this.dataModel = dataModel;
		this.showAxesAsBars = showAxesAsBars;
		this.screenHeight = screenHeight;
		this.screenWidth = screenWidth;
		this.axisList = axisList;
		this.axisBarWidth = axisBarWidth;
		this.showFrequencyInfo = showFrequencyInfo;
		this.useQueryFrequency = useQueryFrequency;
		this.antialias = antialias;
		// this.scatterplotSize = scatterplotSize;
		// this.scatterplotOffset = scatterplotOffset;

		// scatterplotSize = (int)(axisSpacing * .85);
		// scatterplotOffset = (axisSpacing - scatterplotSize) / 2;
	}

	public void run() {
		isRunning = true;

		image = new BufferedImage(screenWidth, screenHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();

		if (antialias) {
			// log.debug("antialias is enabled");
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		// for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
		for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
			if (!isRunning)
				return;

			PCAxis axis = (PCAxis) axisList.get(iaxis);

			// if (!dataModel.getColumn(iaxis).isEnabled()) {
			// if (!axis.column.isEnabled())
			// continue;
			// }

			g2.setStroke(new BasicStroke(2.f));

			if (this.showFrequencyInfo) {
				double freqData[];
				if (useQueryFrequency && (dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column) != null)) {
					freqData = dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column).getHistogram().getArray();
				} else {
					freqData = axis.column.getSummaryStats().getHistogram().getArray();
				}

				double maxFreq = 0.;
				for (int i = 0; i < freqData.length; i++) {
					if (freqData[i] > maxFreq) {
						maxFreq = freqData[i];
					}
				}
				double maxBinHeight = (double) axis.axisHeight / (double) freqData.length;
				int maxBinWidth = axis.axisBarRectangle.width;

				int half_axis_bar_width;
				if (showAxesAsBars) {
					half_axis_bar_width = axis.axisBarRectangle.width / 2;
				} else {
					half_axis_bar_width = 0;
				}
				int axis_bar_left = axis.xPosition - half_axis_bar_width;
				int axis_bar_right = axis.xPosition + half_axis_bar_width;

				for (int ibin = 0; ibin < freqData.length; ibin++) {
					double normVal = freqData[ibin] / maxFreq;

					int binWidth = (int) (maxBinWidth * normVal);
					int y = (axis.axisBarRectangle.y + axis.axisBarRectangle.height)
							- (int) ((maxBinHeight * ibin) + maxBinHeight);

					int binLeft = axis_bar_left - binWidth;
					int binRight = axis_bar_right + binWidth;
					Rectangle binRectangle = new Rectangle(binLeft, y,
							(binRight - binLeft), (int) maxBinHeight);

					g2.setColor(PCAxesRenderer.HISTOGRAM_BIN_FILL_COLOR);
					g2.fill(binRectangle);

					g2.setColor(PCAxesRenderer.HISTOGRAM_BIN_LINE_COLOR);
					g2.draw(binRectangle);

					// g2.drawRect(axis_bar_right, y, binWidth,
					// (int)maxBinHeight);

					/*
					 * Code below draws an oval for the histogram bin
					 * 
					 * int binWidth = (int)(maxBinWidth * normVal); int
					 * binHeight = (int)(maxBinHeight * normVal);
					 * 
					 * if (binWidth < 1) { binWidth = 1; } if (binHeight < 1) {
					 * binHeight = 1; }
					 * 
					 * int center_x = axis.xPosition; int center_y =
					 * (axis.axisBarRectangle.y + axis.axisBarRectangle.height)
					 * - (int)((maxBinHeight * ibin) + (maxBinHeight/2.));
					 * g2.setColor(HISTOGRAM_BIN_FILL_COLOR);
					 * g2.fillOval(center_x - (binWidth/2), center_y -
					 * (binHeight/2), binWidth, binHeight);
					 * g2.setColor(HISTOGRAM_BIN_LINE_COLOR);
					 * g2.drawOval(center_x - (binWidth/2), center_y -
					 * (binHeight/2), binWidth, binHeight);
					 */

					// code block below makes histogram blocks for wide
					// partitioned axes
					/*
					 * int currentBinWidth = (int)(maxBinWidth * normVal); int x
					 * = axis.xPosition + currentBinWidth; // int y = (binSize *
					 * ibin) + (binSize/2) + axis.axisBarRectangle.y; int y =
					 * (axis.axisBarRectangle.y + axis.axisBarRectangle.height)
					 * - (int)((maxBinHeight * ibin) + (maxBinHeight/2.)); //
					 * int y = (axis.axisBarRectangle.y +
					 * axis.axisBarRectangle.height) - (int)(binSize * ibin);
					 * g2.drawRect(axis.xPosition, y-(int)(maxBinHeight/2.),
					 * currentBinWidth, (int)maxBinHeight); // g2.drawOval(x-2,
					 * y-2, 4, 4);
					 */
				}
			}

			// draw axis as bar or line
			// if (showAxesAsBars || showFrequencyInfo) {
			if (showAxesAsBars) {
				boolean querySet = dataModel.getActiveQuery().hasColumnSelections();

				// draw the axis as a bar
				g2.setColor(AXIS_BAR_FILL_COLOR);
				g2.fill(axis.axisBarRectangle);

				g2.setColor(AXIS_BAR_LINE_COLOR);
				g2.draw(axis.axisBarRectangle);
				int x0 = axis.axisBarRectangle.x - 4;
				int x1 = axis.axisBarRectangle.x + axis.axisBarRectangle.width + 4;
				g2.drawLine(x0, axis.axisBarRectangle.y, x1, axis.axisBarRectangle.y);
				g2.drawLine(axis.xPosition - 2, axis.bottomPosition, axis.xPosition + 2, axis.bottomPosition);
				int y0 = axis.axisBarRectangle.y + axis.axisBarRectangle.height;
				g2.drawLine(x0, y0, x1, y0);
				g2.drawLine(x0, axis.focusTop, x1, axis.focusTop);
                g2.drawLine(x0, axis.focusBottom, x1, axis.focusBottom);

				if (dispersionDisplayMode == PCPanel.MEDIAN_DISPERSION_BOX_MODE) {
					// draw whiskers
					g2.setStroke(new BasicStroke(6.f));
					g2.setColor(IQR_LINE_COLOR);
					int dispersion_pane_center = axis.IQRWhiskerRectangle.x
							+ (axis.IQRWhiskerRectangle.width / 2);
					g2.drawLine(dispersion_pane_center,
							axis.IQRWhiskerRectangle.y, dispersion_pane_center,
							axis.IQRWhiskerRectangle.y
									+ axis.IQRWhiskerRectangle.height);
					// g2.drawLine(axis.xPosition, axis.IQRWhiskerRectangle.y,
					// axis.xPosition,
					// axis.IQRWhiskerRectangle.y+axis.IQRWhiskerRectangle.height);
					// g2.drawLine((int)axis.IQRBoxRectangle.getCenterX(),
					// axis.IQRWhiskerRectangle.y,
					// (int)axis.IQRBoxRectangle.getCenterX(),
					// axis.IQRWhiskerRectangle.y+axis.IQRWhiskerRectangle.height);

					// draw IQR range box
					g2.setStroke(new BasicStroke(2.f));
					g2.setColor(IQR_FILL_COLOR);
					g2.fill(axis.IQRBoxRectangle);
					g2.setColor(IQR_LINE_COLOR);
					g2.draw(axis.IQRBoxRectangle);

					// draw the median line
					g2.drawLine(
							axis.IQRBoxRectangle.x,
							axis.medianPosition,
							axis.IQRBoxRectangle.x + axis.IQRBoxRectangle.width,
							axis.medianPosition);

					if (axis.QueryIQRBoxRectangle != null && querySet) {
						// draw the query whiskers
						g2.setColor(QUERY_IQR_LINE_COLOR);
						dispersion_pane_center = axis.QueryIQRWhiskerRectangle.x
								+ (axis.QueryIQRWhiskerRectangle.width / 2);
						g2.drawLine(dispersion_pane_center,
								axis.QueryIQRWhiskerRectangle.y,
								dispersion_pane_center,
								axis.QueryIQRWhiskerRectangle.y
										+ axis.QueryIQRWhiskerRectangle.height);
						// g2.drawLine(axis.xPosition,
						// axis.QueryIQRWhiskerRectangle.y,
						// axis.xPosition,
						// axis.QueryIQRWhiskerRectangle.y+axis.QueryIQRWhiskerRectangle.height);

						// draw Query IQR range box
						g2.setColor(QUERY_IQR_FILL_COLOR);
						g2.fill(axis.QueryIQRBoxRectangle);
						g2.setColor(QUERY_IQR_LINE_COLOR);
						g2.draw(axis.QueryIQRBoxRectangle);

						// draw the Query median line
						g2.drawLine(axis.QueryIQRBoxRectangle.x,
								axis.queryMedianPosition,
								axis.QueryIQRBoxRectangle.x
										+ axis.QueryIQRBoxRectangle.width,
								axis.queryMedianPosition);
					}
				} else if (dispersionDisplayMode == PCPanel.MEAN_DISPERSION_BOX_MODE) {
					// draw mean centered standard deviation range rectangle
					g2.setStroke(new BasicStroke(2.f));
					g2.setColor(IQR_FILL_COLOR);
					g2.fill(axis.standardDeviationRangeRectangle);
					g2.setColor(IQR_LINE_COLOR);
					g2.draw(axis.standardDeviationRangeRectangle);

					// draw the mean line
					g2.drawLine(axis.standardDeviationRangeRectangle.x, axis.meanPosition,
							axis.standardDeviationRangeRectangle.x + axis.standardDeviationRangeRectangle.width,
							axis.meanPosition);

					if (axis.queryStandardDeviationRangeRectangle != null && querySet) {
						// draw mean centered standard deviation range range box
						// for queried lines
						g2.setColor(QUERY_IQR_FILL_COLOR);
						g2.fill(axis.queryStandardDeviationRangeRectangle);
						g2.setColor(QUERY_IQR_LINE_COLOR);
						g2.draw(axis.queryStandardDeviationRangeRectangle);

						// draw the query mean line
						g2.drawLine(
								axis.queryStandardDeviationRangeRectangle.x,
								axis.queryMeanPosition,
								axis.queryStandardDeviationRangeRectangle.x
										+ axis.queryStandardDeviationRangeRectangle.width,
								axis.queryMeanPosition);
					}
				}

			} else {
				// draw axis as a line
				g2.setColor(AXIS_BAR_LINE_COLOR);
				g2.drawLine(axis.xPosition, axis.topPosition, axis.xPosition, axis.bottomPosition);
				g2.drawLine(axis.xPosition - 2, axis.topPosition, axis.xPosition + 2, axis.topPosition);
				g2.drawLine(axis.xPosition - 2, axis.bottomPosition, axis.xPosition + 2, axis.bottomPosition);
                g2.drawLine(axis.xPosition - 2, axis.focusTop, axis.xPosition + 2, axis.focusTop);
                g2.drawLine(axis.xPosition - 2, axis.focusBottom, axis.xPosition + 2, axis.focusBottom);
			}

			// calculate rectangle for axis label/name
			g2.setFont(titleFont);
			int titleHeight = g2.getFontMetrics().getHeight();
			int titleWidth = g2.getFontMetrics().stringWidth(
					axis.column.getName());

			if (iaxis % 2 == 0) {
				axis.labelRectangle = new Rectangle(axis.xPosition - (titleWidth / 2), 0, titleWidth, titleHeight);
			} else {
				axis.labelRectangle = new Rectangle(axis.xPosition - (titleWidth / 2), (int) (titleHeight/1.2), titleWidth, titleHeight);
			}

			// g2.setFont(titleFont);
			// FontRenderContext frc = g2.getFontRenderContext();
			// int stringHeight =
			// (int)titleFont.getLineMetrics(axis.column.getName(),
			// frc).getHeight();
			// // int fontHeight = g2.getFontMetrics().getHeight();
			//
			// int stringWidth =
			// g2.getFontMetrics().stringWidth(axis.column.getName());
			// axis.labelRectangle = new
			// Rectangle(axis.xPosition-(stringWidth/2), 0, stringWidth,
			// stringHeight);
			//
			// draw range labels
			g2.setFont(secondaryFont);
			FontRenderContext frc = g2.getFontRenderContext();
			// fontHeight = g2.getFontMetrics().getHeight();

			g2.setColor(Color.gray);
			String minLabel = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMin());
			String maxLabel = DECIMAL_FORMAT.format(axis.column.getSummaryStats().getMax());
			// int stringHeight = (int)titleFont.getLineMetrics(minLabel,
			// frc).getHeight();
			int stringHeight = g2.getFontMetrics().getHeight();
			int stringWidth = g2.getFontMetrics().stringWidth(minLabel);
			g2.drawString(minLabel, axis.xPosition - (stringWidth / 2),
					axis.bottomPosition + g2.getFontMetrics().getAscent() + 2);
			stringWidth = g2.getFontMetrics().stringWidth(maxLabel);
			g2.drawString(maxLabel, axis.xPosition - (stringWidth / 2),
					axis.topPosition - g2.getFontMetrics().getDescent());

			// draw correlation indicator blocks
			if (showCorrelationIndicators) {
				g2.setStroke(new BasicStroke(1.f));
				ArrayList<Float> correlationCoefficients;
				if (useQueryCorrelations && (dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column) != null)) {
					correlationCoefficients = dataModel.getActiveQuery().getColumnQuerySummaryStats(axis.column).getCorrelationCoefficients();
//					correlationCoefficients = axis.column.getQueryCorrelationCoefficients();
				} else {
					correlationCoefficients = axis.column.getSummaryStats().getCorrelationCoefficients();
				}

				int bufferBetweenAxes = 6;
				// int blockWidth = (axisSpacing-12) / axisList.size();
				int blockWidth = (axisSpacing - (2 * bufferBetweenAxes))
						/ axisList.size();
				if (blockWidth > axisBarWidth) {
					blockWidth = axisBarWidth;
				}
				int blockXOffset = ((axisSpacing - (2 * bufferBetweenAxes)) - (axisList.size() * blockWidth)) / 2;

				for (int i = 0; i < axisList.size(); i++) {
					PCAxis currentAxis = axisList.get(i);
					double corrcoef = correlationCoefficients.get(currentAxis.dataModelIndex);

					// int x = (axis.rectangle.x + 6) + (i * blockWidth);
					int x = ((axis.rectangle.x + bufferBetweenAxes) + blockXOffset) + (i * blockWidth);

					// Rectangle r = new Rectangle(x,
					// axis.topPosition-(fontHeight+correlationIndicatorHeight)/*axis.labelRectangle.y+axis.labelRectangle.height*/,
					// blockWidth, correlationIndicatorHeight);
					Rectangle r = new Rectangle(x, axis.topPosition
							- (stringHeight + correlationIndicatorHeight),
							blockWidth, correlationIndicatorHeight);
					if (dataModel.getHighlightedColumn() == currentAxis.column) {
						r.y -= 2;
						r.height += 4;
					}

					if (currentAxis == axis) {
						g2.setColor(Color.white);
						g2.fill(r);
						g2.setColor(AXIS_BAR_LINE_COLOR);
						g2.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
						g2.drawLine(r.x, r.y + r.height, r.x + r.width, r.y);
						g2.draw(r);
					} else {
						Color c = Utilities.getColorForCorrelationCoefficient(
								corrcoef, 1.);
						g2.setColor(c);
						g2.fill(r);
						g2.setColor(AXIS_BAR_LINE_COLOR);
						g2.draw(r);
					}
				}
				g2.setStroke(new BasicStroke(2.f));
			}

			// draw regression coefficients
			if (dataModel.getOLSMultipleLinearRegression() != null) {
				if (dataModel.getOLSMultipleLinearRegressionDependentColumn() == axis.column) {
					// draw r2 and r2adj values
					String rSquaredString = "r2 = "
							+ DECIMAL_FORMAT.format(dataModel
									.getOLSMultipleLinearRegression()
									.calculateRSquared());
					String rSquaredAdjustedString = "r2Adj = "
							+ DECIMAL_FORMAT.format(dataModel
									.getOLSMultipleLinearRegression()
									.calculateAdjustedRSquared());
					stringWidth = g2.getFontMetrics().stringWidth(
							rSquaredString);
					int x = axis.xPosition - (stringWidth / 2);
					int y = axis.labelRectangle.height + stringHeight + 2;
					g2.drawString(rSquaredString, x, y);
					stringWidth = g2.getFontMetrics().stringWidth(
							rSquaredAdjustedString);
					x = axis.xPosition - (stringWidth / 2);
					y += stringHeight;
					g2.drawString(rSquaredAdjustedString, x, y);
				} else {
					// draw b values
					String bString = "b = "
							+ DECIMAL_FORMAT.format(dataModel
									.getOLSMultipleLinearRegression()
									.estimateRegressionParameters()[iaxis]);
					stringWidth = g2.getFontMetrics().stringWidth(bString);
					int x = axis.xPosition - (stringWidth / 2);
					int y = axis.labelRectangle.height + stringHeight + 2;
					g2.drawString(bString, x, y);
				}
			}

		}

		isRunning = false;
		fireRendererFinished();
	}
}
