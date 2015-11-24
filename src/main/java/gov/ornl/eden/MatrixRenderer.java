package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MatrixRenderer extends Renderer {
	public static final Font COLUMN_NAME_VALUE_FONT = new Font("Dialog",
			Font.PLAIN, 8);

	// private DataModel dataModel;
	private int cellSize;
	private Rectangle matrixRect;
	private boolean useQueryCorrelations;
	private ArrayList<Column> columns;
	private DataModel dataModel;

	public MatrixRenderer(DataModel dataModel, int cellSize, Rectangle matrixRect,
			boolean useQueryCorrelations, ArrayList<Column> columns) {
		this.dataModel = dataModel;
		this.cellSize = cellSize;
		this.matrixRect = matrixRect;
		this.useQueryCorrelations = useQueryCorrelations;
		this.columns = columns;
	}

	public void run() {
		isRunning = true;

		image = new BufferedImage(matrixRect.width, matrixRect.height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(MatrixRenderer.COLUMN_NAME_VALUE_FONT);

		// for (int iColumn = 0; iColumn < dataModel.getColumnCount();
		// iColumn++) {
		for (int iColumn = 0; iColumn < columns.size(); iColumn++) {
			if (!isRunning) {
				return;
			}

			Column column = columns.get(iColumn);
			// Column column = dataModel.getColumn(iColumn);

			// ArrayList<Float> correlationList =
			// column.getCorrelationCoefficients();
			ArrayList<Float> correlationList;

			if (useQueryCorrelations && (dataModel.getActiveQuery().getColumnQuerySummaryStats(column) != null)) {
				correlationList = dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getCorrelationCoefficients();
//				correlationList = column.getQueryCorrelationCoefficients();
			} else {
				correlationList = column.getSummaryStats().getCorrelationCoefficients();
			}
			int ypos = iColumn * cellSize;

			for (int i = 0; i < correlationList.size(); i++) {
				if (!isRunning) {
					return;
				}

				int xpos = i * cellSize;
				if (i == iColumn) {
					g2.setColor(Color.DARK_GRAY);
					ypos = ypos + (cellSize / 2)
							+ (g2.getFontMetrics().getHeight() / 2);
					g2.drawString(column.getName(), xpos, ypos);
					break;
				}
				float corrCoef = correlationList.get(i);
				Color c = Utilities.getColorForCorrelationCoefficient(corrCoef,
						1.0f);
				if (c == null) {
					continue;
				}

				g2.setColor(c);
				g2.fillRect(xpos + 2, ypos + 2, cellSize - 3, cellSize - 3);
			}
		}

		isRunning = false;
		fireRendererFinished();
	}
}
