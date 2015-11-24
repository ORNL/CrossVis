package gov.ornl.eden;

import gov.ornl.datatable.Column;

import java.awt.Rectangle;
import java.util.ArrayList;

public class PCAxis {
	public Column column;
	public int dataModelIndex;

	public int xPosition = 0;
	public int topPosition = 0;
	public int bottomPosition = 0;

    public int focusTop = 0;
    public int focusBottom = 0;
	public int focusHeight = 0;

	public int axisHeight = 0;
	public int axisWidth = 0;

	public Rectangle rectangle;
	public Rectangle labelRectangle;

	public Rectangle axisBarRectangle;
    public Rectangle focusRectangle;
    public Rectangle upperContextRectangle;
    public Rectangle lowerContextRectangle;

	public Rectangle IQRBoxRectangle;
	public Rectangle IQRWhiskerRectangle;
	public Rectangle QueryIQRBoxRectangle;
	public Rectangle QueryIQRWhiskerRectangle;

	public int medianPosition;
	public int queryMedianPosition;
	public int meanPosition;
	public int queryMeanPosition;

	public Rectangle standardDeviationRangeRectangle;
	public Rectangle queryStandardDeviationRangeRectangle;

	public Rectangle frequencyDisplayRectangle;

	public ArrayList<PCAxisSelection> axisSelectionList = new ArrayList<PCAxisSelection>();
	
	public int scatterplot_x0;
	public int scatterplot_y0;

	public PCAxis(Column column, int dataModelIndex) {
		this.column = column;
		this.dataModelIndex = dataModelIndex;
	}
}
