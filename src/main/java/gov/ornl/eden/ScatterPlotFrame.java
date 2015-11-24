package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.IOUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ScatterPlotFrame extends JFrame implements DisplaySettingsPanelListener, ActionListener, ItemListener {
	private ScatterPlotPanel spPanel;
	ScatterplotConfiguration config;
	ScatterplotConfiguration nonqueryConfig;
	Column xColumn;
	Column yColumn;
	DataModel dataModel;
	DisplaySettingsPanel displaySettingsPanel;

	private JCheckBoxMenuItem showCorrelationIndicatorMenuItem;
	private JCheckBoxMenuItem useQueryCorrelationMenuItem;
	private JCheckBoxMenuItem showRegressionLineMenuItem;
	private JCheckBoxMenuItem useQueryRegressionLineMenuItem;

	public ScatterPlotFrame(DataModel dataModel, Column xColumn,
			Column yColumn, ScatterplotConfiguration config,
			ScatterplotConfiguration nonqueryConfig) {
		this.dataModel = dataModel;
		this.xColumn = xColumn;
		this.yColumn = yColumn;
		this.config = config;
		this.nonqueryConfig = nonqueryConfig;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initialize();
	}

	public void setPointAlphaValue(int alphaValue) {
		spPanel.setPointAlphaValue(alphaValue);
	}

	private void initialize() {
		setTitle("Scatterplot " + xColumn.getName() + " vs " + yColumn.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 550, 660);
		initializePanel();
		initializeMenu();
	}

	public void setColumns(Column xColumn, Column yColumn) {
		spPanel.setAxes(xColumn, yColumn);
	}

	private void initializeMenu() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem("Close", KeyEvent.VK_C);
		mi.setActionCommand("close");
		mi.addActionListener(this);
		menu.add(mi);

		menuBar.add(menu);

		// The View Menu
		menu = new JMenu("View");
		menuBar.add(menu);

		JMenu correlationDisplayMenu = new JMenu("Correlation Indicator");
		menu.add(correlationDisplayMenu);
		showCorrelationIndicatorMenuItem = new JCheckBoxMenuItem(
				"Show Correlation Indicator", true);
		showCorrelationIndicatorMenuItem.setMnemonic(KeyEvent.VK_S);
		showCorrelationIndicatorMenuItem.setSelected(spPanel
				.getShowCorrelationIndicator());
		showCorrelationIndicatorMenuItem.addItemListener(this);
		correlationDisplayMenu.add(showCorrelationIndicatorMenuItem);

		useQueryCorrelationMenuItem = new JCheckBoxMenuItem(
				"Compute Correlation for Selected Data", true);
		useQueryCorrelationMenuItem.setMnemonic(KeyEvent.VK_C);
		useQueryCorrelationMenuItem.setSelected(spPanel
				.getUseQueryCorrelation());
		useQueryCorrelationMenuItem.addItemListener(this);
		correlationDisplayMenu.add(useQueryCorrelationMenuItem);

		JMenu regressionDisplayMenu = new JMenu("Regression Line");
		menu.add(regressionDisplayMenu);
		showRegressionLineMenuItem = new JCheckBoxMenuItem(
				"Show Regression Line");
		showRegressionLineMenuItem.setMnemonic(KeyEvent.VK_S);
		showRegressionLineMenuItem.setSelected(spPanel.getShowRegressionLine());
		showRegressionLineMenuItem.addItemListener(this);
		regressionDisplayMenu.add(showRegressionLineMenuItem);

		useQueryRegressionLineMenuItem = new JCheckBoxMenuItem(
				"Compute Regression Line for Selected Data", true);
		useQueryRegressionLineMenuItem.setMnemonic(KeyEvent.VK_C);
		useQueryRegressionLineMenuItem.setSelected(spPanel
				.getUseQueryRegressionLine());
		useQueryRegressionLineMenuItem.addItemListener(this);
		regressionDisplayMenu.add(useQueryRegressionLineMenuItem);

	}

	private void initializePanel() {
		displaySettingsPanel = new DisplaySettingsPanel(config.pointColor,
				nonqueryConfig.pointColor);
		displaySettingsPanel.addPCDisplaySettingsPanelListener(this);
		displaySettingsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Settings"));
		JScrollPane displaySettingsScroller = new JScrollPane(
				displaySettingsPanel);

		spPanel = new ScatterPlotPanel(dataModel, config, nonqueryConfig);
		spPanel.setBackground(Color.white);

		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				spPanel, displaySettingsScroller);
		mainSplit.setDividerLocation(500);
		mainSplit.setOneTouchExpandable(true);

		JPanel mainPanel = (JPanel) getContentPane();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainSplit);
	}

	public static void main(String args[]) throws Exception {
		final String filename = args[0];
		final String xColumnName = args[1];
		final String yColumnName = args[2];

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					File f = new File(filename);
					DataModel dm = new DataModel();
					dm.clear();

					ScatterplotConfiguration config = new ScatterplotConfiguration();
					config.pointColor = new Color(50, 50, 120, 100);
					config.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
					config.showTickMarks = true;
					config.labelFont = new Font("Dialog", Font.PLAIN, 10);
					config.showAxisLabels = true;
					config.showAxisNames = true;
					config.showRegressionLine = false;

					ScatterplotConfiguration nonqueryConfig = new ScatterplotConfiguration();
					nonqueryConfig.pointColor = new Color(100, 100, 100, 100);
					nonqueryConfig.pointShape = new Ellipse2D.Float(0, 0, 6, 6);
					nonqueryConfig.showTickMarks = true;
					nonqueryConfig.showRegressionLine = false;
					nonqueryConfig.labelFont = new Font("Dialog", Font.PLAIN,
							10);

					/*
					 * Column xCol = new Column("xCol"); Column yCol = new
					 * Column("yCol"); ArrayList<Column> columns = new
					 * ArrayList<Column>(); columns.add(xCol);
					 * columns.add(yCol);
					 * 
					 * ArrayList<Tuple> tuples = new ArrayList<Tuple>(); Tuple
					 * tuple = new Tuple(); tuple.addElement(10.f);
					 * tuple.addElement(10.f); tuples.add(tuple); tuple = new
					 * Tuple(); tuple.addElement(10.f); tuple.addElement(20.f);
					 * tuples.add(tuple); tuple = new Tuple();
					 * tuple.addElement(20.f); tuple.addElement(20.f);
					 * tuples.add(tuple); tuple = new Tuple();
					 * tuple.addElement(20.f); tuple.addElement(10.f);
					 * tuples.add(tuple); tuple = new Tuple();
					 * tuple.addElement(15.f); tuple.addElement(15.f);
					 * tuples.add(tuple);
					 * 
					 * dm.setData(tuples, columns);
					 */

					IOUtilities.readCSV(f, dm);
					Column xCol = dm.getColumn(xColumnName);
					Column yCol = dm.getColumn(yColumnName);
					// Column xCol = dm.getColumn(0);
					// Column yCol = dm.getColumn(2);
					dm.setQueriedTuples();
					ScatterPlotFrame window = new ScatterPlotFrame(dm, xCol,
							yCol, config, nonqueryConfig);
					window.setVisible(true);
					window.setColumns(xCol, yCol);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void selectedDataColorChanged(Color color) {
		spPanel.setFocusShapeColor(color);
	}

	@Override
	public void unselectedDataColorChanged(Color color) {
		spPanel.setContextShapeColor(color);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("close")) {
			dispose();
		}
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getSource() == this.showCorrelationIndicatorMenuItem) {
			spPanel.setShowCorrelationIndicator(showCorrelationIndicatorMenuItem
					.isSelected());
		} else if (event.getSource() == this.useQueryCorrelationMenuItem) {
			spPanel.setUseQueryCorrelation(useQueryCorrelationMenuItem
					.isSelected());
		} else if (event.getSource() == this.showRegressionLineMenuItem) {
			spPanel.setShowRegressionLine(showRegressionLineMenuItem
					.isSelected());
		} else if (event.getSource() == this.useQueryRegressionLineMenuItem) {
			spPanel.setUseQueryRegressionLine(useQueryRegressionLineMenuItem
					.isSelected());
		}
	}

	@Override
	public void secondaryFontSizeChanged(int fontSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void titleFontSizeChanged(int fontSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void axisWidthChanged(int width) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scatterplotSizeChanged(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void correlationBoxSizeChanged(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pcLineSizeChanged(int size) {
		// TODO Auto-generated method stub

	}
}
