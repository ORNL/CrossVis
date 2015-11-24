package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DataModelListener;
import gov.ornl.datatable.Tuple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GeoGridPanel extends JPanel implements ComponentListener,
		MouseListener, MouseMotionListener, DataModelListener {
	private final static Logger log = LoggerFactory
			.getLogger(GeoGridPanel.class);

	private DataModel dataModel = new DataModel();
	private int BORDER = 5;

	private Color selectedAreaColor = new Color(0, 103, 253);
	private Color graticuleColor = new Color(220, 220, 220);
	private Color gridFillColor = new Color(250, 250, 250);
	private Color gridBorderColor = new Color(150, 150, 150);

	private int left;
	private int right;
	private int bottom;
	private int top;
	private int numRows;
	private int numCols;
	private int gridSize;

	private BufferedImage basemapImage;

	private boolean showGraticule = false;

	private double gridSpacing;
	private int gridWidth;
	private int gridHeight;
	private int x0;
	private int y0;

	private ArrayList<GeoGridPanelListener> listeners = new ArrayList<GeoGridPanelListener>();

	private Tuple tupleData[][];
	private boolean tupleBitMask[][];
	// private LandMask tupleQueryMask = null;
	private BufferedImage tupleGridImage = null;

	public GeoGridPanel(int left, int right, int bottom, int top, int gridSize,
			DataModel dataModel) {
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.gridSize = gridSize;
		this.dataModel = dataModel;

		dataModel.addDataModelListener(this);
		layoutGrid();
		addComponentListener(this);
	}

	public void addGeoGridPanelListener(GeoGridPanelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public boolean removeGeoGridPanelListener(GeoGridPanelListener listener) {
		return listeners.remove(listener);
	}

	private void layoutGrid() {
		int panelWidth = getWidth() - 1 - BORDER;
		int panelHeight = getHeight() - 1 - BORDER;

		numCols = (right - left) / gridSize;
		numRows = (top - bottom) / gridSize;

		double gridXSpacing = (double) panelWidth / numCols;
		double gridYSpacing = (double) panelHeight / numRows;

		gridSpacing = Math.min(gridXSpacing, gridYSpacing);

		gridWidth = (int) (gridSpacing * numCols);
		gridHeight = (int) (gridSpacing * numRows);

		x0 = BORDER + (panelWidth - gridWidth) / 2;
		y0 = (panelHeight - gridHeight) / 2;
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (basemapImage != null) {
			g2.drawImage(basemapImage, x0, y0, gridWidth, gridHeight, null);
		} else {
			g2.setColor(gridFillColor);
			g2.fillRect(x0, y0, gridWidth, gridHeight);
		}

		if (tupleData != null) {
			WritableRaster wr = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
					numCols, numRows, 4, null);
			int colorData[] = new int[4];
			for (int irow = 0; irow < numRows; irow++) {
				for (int icol = 0; icol < numCols; icol++) {
					// if (tupleData[irow][icol] != null) {
					// if (tupleData[irow][icol].getQueryFlag()) {
					if (tupleBitMask[irow][icol] == true) {
						colorData[0] = PCPanel.DEFAULT_FOCUS_LINE_COLOR
								.getRed();
						colorData[1] = PCPanel.DEFAULT_FOCUS_LINE_COLOR
								.getGreen();
						colorData[2] = PCPanel.DEFAULT_FOCUS_LINE_COLOR
								.getBlue();
						colorData[3] = 255;

						wr.setPixel(icol, irow, colorData);
					}
					// }
				}
			}
			tupleGridImage = new BufferedImage(numCols, numRows,
					BufferedImage.TYPE_INT_ARGB);
			tupleGridImage.setData(wr);

			g2.drawImage(tupleGridImage, x0, y0, gridWidth, gridHeight, null);
		}

		g2.translate(x0, y0);

		if (showGraticule) {
			g2.setColor(graticuleColor);
			for (int i = 1; i < numCols; i++) {
				int ix = (int) (0.5 + (i * gridSpacing));
				g2.drawLine(ix, 0, ix, gridHeight);
			}
			for (int i = 1; i < numRows; i++) {
				int iy = (int) (0.5 + (i * gridSpacing));
				g2.drawLine(0, iy, gridWidth, iy);
			}
		}

		g2.translate(-x0, -y0);

		g2.setColor(gridBorderColor);
		g2.drawRect(x0, y0, gridWidth, gridHeight);

	}

	public void setBasemapImage(BufferedImage basemapImage) {
		this.basemapImage = basemapImage;
	}

	@Override
	public void componentHidden(ComponentEvent event) {
	}

	@Override
	public void componentMoved(ComponentEvent event) {
	}

	@Override
	public void componentResized(ComponentEvent event) {
		layoutGrid();
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent event) {
		layoutGrid();
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent event) {
	}

	@Override
	public void mouseMoved(MouseEvent event) {

	}

	@Override
	public void mouseClicked(MouseEvent event) {
	}

	@Override
	public void mouseEntered(MouseEvent event) {
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
	}

	public static void main(String args[]) throws Exception {
		BufferedImage backgroundImage = ImageIO.read(new File(
				"resources/rasters/HYP_50M_SR_W/HYP_50M_SR_W.png"));

		DataModel dataModel = new DataModel();

		GeoGridPanel panel = new GeoGridPanel(-180 * 3600, 180 * 3600,
				-90 * 3600, 90 * 3600, 1 * 3600, dataModel);
		// GeoGridPanel panel = new GeoGridPanel(-80, 10, -40, 20, 1);
		panel.setBasemapImage(backgroundImage);
		JFrame frame = new JFrame();
		frame.setBounds(10, 10, 800, 600);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private int snapCoordinateToGrid(double latitude) {
		int iLat = (int) ((latitude / gridSize));
		return iLat * gridSize;
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		Column latColumn = dataModel.getColumn("lat");
		Column lonColumn = dataModel.getColumn("lon");

		if (latColumn != null && lonColumn != null) {
			double maxLat = latColumn.getSummaryStats().getMax() + 90.;
			double minLat = latColumn.getSummaryStats().getMin() + 90.;
			double maxLon = lonColumn.getSummaryStats().getMax();
			double minLon = lonColumn.getSummaryStats().getMin();

			log.debug("maxLat=" + maxLat + " minLat=" + minLat + " maxLon="
					+ maxLon + " minLon=" + minLon);

			int iMaxLat = snapCoordinateToGrid(maxLat * 3600.);
			int iMinLat = snapCoordinateToGrid(minLat * 3600.);
			int iMaxLon = snapCoordinateToGrid(maxLon * 3600.);
			int iMinLon = snapCoordinateToGrid(minLon * 3600.);

			top = iMaxLat + gridSize;
			bottom = iMinLat;
			left = iMinLon;
			right = iMaxLon + gridSize;

			log.debug("top=" + top / 3600. + " bottom=" + bottom / 3600.
					+ " left=" + left / 3600. + " right=" + right / 3600.);

			layoutGrid();
			generateTupleGrid();
			repaint();
		}
	}

	private void generateTupleGrid() {
		if (dataModel.getTupleCount() > 0) {
			Column latColumn = dataModel.getColumn("lat");
			Column lonColumn = dataModel.getColumn("lon");

			if (latColumn != null && lonColumn != null) {
				tupleData = new Tuple[numRows][numCols];
				tupleBitMask = new boolean[numRows][numCols];
				for (int i = 0; i < numRows; i++) {
					Arrays.fill(tupleBitMask[i], false);
				}

				int latItemIndex = dataModel.getColumnIndex(latColumn);
				int lonItemIndex = dataModel.getColumnIndex(lonColumn);

				for (int i = 0; i < dataModel.getTupleCount(); i++) {
					Tuple tuple = dataModel.getTuple(i);

					double lat = tuple.getElement(latItemIndex) + 90.;
					lat = lat * 3600.;
					double lon = tuple.getElement(lonItemIndex);
					lon = lon * 3600.;

					double column = (lon - left) / gridSize;
					double row = (lat - bottom) / gridSize;

					int iColumn = (int) ((lon - left) / gridSize);
					int iRow = (int) ((lat - bottom) / gridSize);

					try {
						tupleData[(numRows - 1) - iRow][iColumn] = tuple;
						if (tuple.getQueryFlag() == true) {
							tupleBitMask[(numRows - 1) - iRow][iColumn] = true;
						}
					} catch (ArrayIndexOutOfBoundsException ex) {
						log.debug("Bad index");
						// ex.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		generateTupleGrid();
		repaint();
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
	}

	@Override
	public void columnDisabled(DataModel dataModel, Column disabledColumn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
		// TODO Auto-generated method stub

	}

	@Override
	public void columnEnabled(DataModel dataModel, Column enabledColumn) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel,
			ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel,
			ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		
	}
}
