package gov.ornl.eden;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Line2D;

public class ScatterplotConfiguration {
	private static final Font DEFAULT_AXIS_LABEL_FONT = new Font("Dialog",
			Font.PLAIN, 9);
	private static final int DEFAULT_BORDER_SIZE = 4;
	private static final Shape DEFAULT_POINT_SHAPE = new Line2D.Float(0, 0, 0, 0);
	public final static Color DEFAULT_POINT_COLOR = new Color(20, 20, 80, 100);

	public boolean showTickMarks = false;
	public boolean showGridLines = false;
	public boolean showAxisLabels = true;
	public boolean showAxisNames = true;
	public boolean showRegressionLine = true;
	public Font labelFont = DEFAULT_AXIS_LABEL_FONT;
	public int borderSize = DEFAULT_BORDER_SIZE;
	public Shape pointShape = DEFAULT_POINT_SHAPE;
	public Color pointColor = DEFAULT_POINT_COLOR;
	public Color labelColor = Color.DARK_GRAY;
	public Color axisLineColor = Color.DARK_GRAY;
	public boolean showCorrelationIndicator = true;
	public boolean useQueryRegressionLine = true;
	public boolean useQueryCorrelationCoefficient = true;
}
