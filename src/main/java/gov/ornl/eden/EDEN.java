package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DataModelListener;
import gov.ornl.datatable.IOUtilities;
import gov.ornl.datatable.Tuple;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EDEN implements DataModelListener, ActionListener, WindowListener,
		ListSelectionListener, ItemListener, DisplaySettingsPanelListener {
	private final static Logger log = LoggerFactory.getLogger(EDEN.class);

	private final static String VERSION_STRING = "v0.11.3";
	private final static String TITLE_STRING = "E D E N";

	private JFrame edenFrame;
	private DataModel dataModel;
	private PCPanel pcPanel;
	private CorrelationMatrixPanel corrMatrixPanel;
	private ColumnTableModel colTableModel;
    private DataTableModel dataTableModel;
    private JTable dataTable;
	private JTable statsTable;
	private JCheckBoxMenuItem showFilteredDataMenuItem;
	private JCheckBoxMenuItem showAxesAsBarsMenuItem;
	private JMenuItem removeSelectedDataMenuItem;
	private JMenuItem removeHighlightedAxisMenuItem;
	private JMenuItem runMLRMenuItem;
	private JCheckBoxMenuItem showQueryLimitsMenuItem;
	private JMenuItem runMultiCollinearityFilterMenuItem;
	private JRadioButtonMenuItem renderTuplesAsLines;
	private JRadioButtonMenuItem renderTuplesAsDots;
	private JCheckBoxMenuItem showFrequencyDataMenuItem;
	private DisplaySettingsPanel settingsPanel;
	private JCheckBoxMenuItem showPCLinesMenuItem;
	private JRadioButtonMenuItem medianDisplayMenuItem;
	private JRadioButtonMenuItem meanDisplayMenuItem;
	private JMenuItem removeUnselectedDataMenuItem;
	private JCheckBox queryStatsCheckBox;
	private JCheckBoxMenuItem antialiasMenuItem;
	private JCheckBoxMenuItem showCorrelationIndicatorsMenuItem;
	private JCheckBoxMenuItem useQueryCorrelationsMenuItem;
	private JMenuItem arrangeByDispersion;
	private JMenuItem arrangeByCorrelation;
	private JCheckBoxMenuItem useSelectedDataArrangeMenuItem;
	private JCheckBoxMenuItem useQueryFrequencyDataMenuItem;
	private JMenuItem arrangeByDispersionDifference;
	private JMenuItem arrangeByTypical;
	private JMenuItem arrangeByTypicalDifference;
    private JMenu colorScaleMenu;

    public EDEN() {
		initialize();
		edenFrame.addWindowListener(this);
		edenFrame.setVisible(true);
	}

	public EDEN(File dataFile) {
		this();
		try {
			IOUtilities.readCSV(dataFile, dataModel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public EDEN(File dataFile, double sampleFactor) {
		this();
		try {
			IOUtilities.readCSVSample(dataFile, dataModel, sampleFactor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public EDEN(ArrayList<Tuple> tuples, ArrayList<Column> columns) {
		this();
//		initialize();
//		edenFrame.setVisible(true);
		dataModel.setData(tuples, columns);
	}

	private void initialize() {
		dataModel = new DataModel();
		dataModel.addDataModelListener(this);

		edenFrame = new JFrame();
		edenFrame.addWindowListener(this);
		edenFrame.setTitle(TITLE_STRING + " - " + VERSION_STRING);
		edenFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		edenFrame.setBounds(100, 100, 1400, 700);

		initializePanel();
		initializeMenu();
	}

	private void initializeMenu() {
		JMenuBar menuBar = new JMenuBar();
		edenFrame.setJMenuBar(menuBar);

		// the file menu
		JMenu menu = new JMenu("File");
		JMenuItem mi = new JMenuItem("Open CSV...", KeyEvent.VK_O);
		mi.addActionListener(this);
		mi.setActionCommand("open file csv");
		menu.add(mi);

		mi = new JMenuItem("Open Sampled CSV...", KeyEvent.VK_S);
		mi.addActionListener(this);
		mi.setActionCommand("open file sampled csv");
		menu.add(mi);

		menu.addSeparator();

		mi = new JMenuItem("Export Selected Lines...", KeyEvent.VK_E);
		mi.addActionListener(this);
		mi.setActionCommand("save selected lines");
		menu.add(mi);

		mi = new JMenuItem("Screen Capture...", KeyEvent.VK_S);
		mi.addActionListener(this);
		mi.setActionCommand("screen capture");
		menu.add(mi);
		
		menu.addSeparator();

		mi = new JMenuItem("Exit", KeyEvent.VK_X);
		mi.addActionListener(this);
		mi.setActionCommand("exit");
		menu.add(mi);

		menuBar.add(menu);

		// The view menu
		menu = new JMenu("View");
		menuBar.add(menu);

		antialiasMenuItem = new JCheckBoxMenuItem("Antialias", true);
		antialiasMenuItem.setSelected(pcPanel.getAntialiasEnabled());
		antialiasMenuItem.setMnemonic(KeyEvent.VK_A);
		antialiasMenuItem.addActionListener(this);
		menu.add(antialiasMenuItem);

		showPCLinesMenuItem = new JCheckBoxMenuItem(
				"Show Parallel Coordinate Lines", true);
		showPCLinesMenuItem.setSelected(pcPanel.isShowingPolylines());
		showPCLinesMenuItem.setMnemonic(KeyEvent.VK_P);
		showPCLinesMenuItem.addItemListener(this);
		menu.add(showPCLinesMenuItem);

		showFilteredDataMenuItem = new JCheckBoxMenuItem("Show Filtered Data",
				true);
		showFilteredDataMenuItem.setMnemonic(KeyEvent.VK_F);
		showFilteredDataMenuItem.setSelected(pcPanel.isShowingFilteredData());
		showFilteredDataMenuItem.addItemListener(this);
		menu.add(showFilteredDataMenuItem);

		showAxesAsBarsMenuItem = new JCheckBoxMenuItem(
				"Show Axes Dispersion Information", true);
		showAxesAsBarsMenuItem.setMnemonic(KeyEvent.VK_B);
		showAxesAsBarsMenuItem.setSelected(pcPanel.isShowingAxesAsBars());
		showAxesAsBarsMenuItem.addItemListener(this);
		menu.add(showAxesAsBarsMenuItem);

		showQueryLimitsMenuItem = new JCheckBoxMenuItem("Show Query Limits",
				true);
		showQueryLimitsMenuItem.setSelected(pcPanel.isShowingQueryLimits());
		showQueryLimitsMenuItem.setMnemonic(KeyEvent.VK_Q);
		showQueryLimitsMenuItem.addItemListener(this);
		menu.add(showQueryLimitsMenuItem);

		JMenu frequencyMenu = new JMenu("Frequency Information");
		menu.add(frequencyMenu);
		showFrequencyDataMenuItem = new JCheckBoxMenuItem("Show Frequency Information", true);
		showFrequencyDataMenuItem.setMnemonic(KeyEvent.VK_F);
		showFrequencyDataMenuItem.setSelected(pcPanel.isShowingFrequencyInfo());
		showFrequencyDataMenuItem.addItemListener(this);
		frequencyMenu.add(showFrequencyDataMenuItem);
		useQueryFrequencyDataMenuItem = new JCheckBoxMenuItem("Use Only Selected Data", true);
		useQueryFrequencyDataMenuItem.addItemListener(this);
		useQueryFrequencyDataMenuItem.setMnemonic(KeyEvent.VK_S);
		useQueryFrequencyDataMenuItem.setSelected(true);
		frequencyMenu.add(useQueryFrequencyDataMenuItem);

		JMenu dispersionDisplayMenu = new JMenu("Dispersion Display");
		menu.add(dispersionDisplayMenu);
		ButtonGroup group = new ButtonGroup();
		medianDisplayMenuItem = new JRadioButtonMenuItem("Median / IQR", false);
		medianDisplayMenuItem.setMnemonic(KeyEvent.VK_M);
		group.add(medianDisplayMenuItem);
		dispersionDisplayMenu.add(medianDisplayMenuItem);
		meanDisplayMenuItem = new JRadioButtonMenuItem("Mean / Standard Deviation", false);
		meanDisplayMenuItem.setMnemonic(KeyEvent.VK_E);
		group.add(meanDisplayMenuItem);
		dispersionDisplayMenu.add(meanDisplayMenuItem);
		if (pcPanel.getDispersionDisplayMode() == PCPanel.MEAN_DISPERSION_BOX_MODE) {
			meanDisplayMenuItem.setSelected(true);
		} else {
			medianDisplayMenuItem.setSelected(true);
		}
		medianDisplayMenuItem.addItemListener(this);
		meanDisplayMenuItem.addItemListener(this);

		JMenu correlationDisplayMenu = new JMenu("Correlation Indicators");
		menu.add(correlationDisplayMenu);
		showCorrelationIndicatorsMenuItem = new JCheckBoxMenuItem(
				"Show Correlation Indicators", true);
		showCorrelationIndicatorsMenuItem.setMnemonic(KeyEvent.VK_S);
		showCorrelationIndicatorsMenuItem.setSelected(pcPanel
				.getShowCorrelationIndicators());
		showCorrelationIndicatorsMenuItem.addItemListener(this);
		correlationDisplayMenu.add(showCorrelationIndicatorsMenuItem);

		useQueryCorrelationsMenuItem = new JCheckBoxMenuItem(
				"Use Only Selected Data", true);
		useQueryCorrelationsMenuItem.setMnemonic(KeyEvent.VK_C);
		useQueryCorrelationsMenuItem.setSelected(pcPanel
				.getUseQueryCorrelations());
		useQueryCorrelationsMenuItem.addItemListener(this);
		correlationDisplayMenu.add(useQueryCorrelationsMenuItem);

		JMenu tupleRenderMenu = new JMenu("Tuple Rendering");
		menu.add(tupleRenderMenu);
		tupleRenderMenu.setEnabled(false);

		group = new ButtonGroup();
		renderTuplesAsLines = new JRadioButtonMenuItem("Polylines", true);
		renderTuplesAsLines.setMnemonic(KeyEvent.VK_P);
		group.add(renderTuplesAsLines);
		tupleRenderMenu.add(renderTuplesAsLines);
		renderTuplesAsDots = new JRadioButtonMenuItem("Dots", false);
		renderTuplesAsDots.setMnemonic(KeyEvent.VK_D);
		group.add(renderTuplesAsDots);
		tupleRenderMenu.add(renderTuplesAsDots);
		if (pcPanel.getTupleDisplayMode() == PCPanel.RENDER_TUPLES_AS_DOTS) {
			renderTuplesAsDots.setSelected(true);
			renderTuplesAsLines.setSelected(false);
		} else if (pcPanel.getTupleDisplayMode() == PCPanel.RENDER_TUPLES_AS_LINES) {
			renderTuplesAsDots.setSelected(false);
			renderTuplesAsLines.setSelected(true);
		}
		renderTuplesAsDots.addItemListener(this);
		renderTuplesAsLines.addItemListener(this);

		JMenu arrangeMenu = new JMenu("Arrange Axes");
		menu.add(arrangeMenu);

		useSelectedDataArrangeMenuItem = new JCheckBoxMenuItem(
				"Use Only Selected Data", true);
		useSelectedDataArrangeMenuItem.setMnemonic(KeyEvent.VK_S);
		useSelectedDataArrangeMenuItem.addItemListener(this);
		arrangeMenu.add(useSelectedDataArrangeMenuItem);

		arrangeByCorrelation = new JMenuItem(
				"Arrange by Correlation with Highlighted Axis", KeyEvent.VK_C);
		arrangeByCorrelation.addActionListener(this);
		arrangeMenu.add(arrangeByCorrelation);

		arrangeByDispersion = new JMenuItem("Arrange by Axis Dispersion",
				KeyEvent.VK_D);
		arrangeByDispersion.addActionListener(this);
		arrangeMenu.add(arrangeByDispersion);

		arrangeByDispersionDifference = new JMenuItem(
				"Arrange by Axis Dispersion Difference from Selected Data",
				KeyEvent.VK_I);
		arrangeByDispersionDifference.addActionListener(this);
		arrangeMenu.add(arrangeByDispersionDifference);

		arrangeByTypical = new JMenuItem("Arrange by Axis Typical Value",
				KeyEvent.VK_T);
		arrangeByTypical.addActionListener(this);
		arrangeMenu.add(arrangeByTypical);

		arrangeByTypicalDifference = new JMenuItem(
				"Arrange by Axis Typical Value Difference from Selected Data",
				KeyEvent.VK_Y);
		arrangeByTypicalDifference.addActionListener(this);
		arrangeMenu.add(arrangeByTypicalDifference);

//		colorScaleMenu = new JMenu("Apply Color Scale to Axis");
//        menu.add(colorScaleMenu);

		// The data menu
		menu = new JMenu("Data");
		menuBar.add(menu);
		removeSelectedDataMenuItem = new JMenuItem("Remove Selected Lines",
				KeyEvent.VK_S);
		removeSelectedDataMenuItem.addActionListener(this);
		removeSelectedDataMenuItem.setActionCommand("remove selected lines");
		menu.add(removeSelectedDataMenuItem);

		removeUnselectedDataMenuItem = new JMenuItem("Keep Only Selected Data",
				KeyEvent.VK_U);
		removeUnselectedDataMenuItem.setEnabled(true);
		removeUnselectedDataMenuItem.addActionListener(this);
		menu.add(removeUnselectedDataMenuItem);

		removeHighlightedAxisMenuItem = new JMenuItem(
				"Remove Highlighted Axis", KeyEvent.VK_A);
		removeHighlightedAxisMenuItem.addActionListener(this);
		removeHighlightedAxisMenuItem
				.setActionCommand("remove highlighted axis");
		menu.add(removeHighlightedAxisMenuItem);

		// The Analytics menu
		menu = new JMenu("Analytics");
		menuBar.add(menu);

		// Run multicollinearity filter
		runMultiCollinearityFilterMenuItem = new JMenuItem(
				"Run Multicollinearity Filter", KeyEvent.VK_M);
		runMultiCollinearityFilterMenuItem.addActionListener(this);
		runMultiCollinearityFilterMenuItem.setEnabled(true);
		menu.add(runMultiCollinearityFilterMenuItem);
		//
		// // Run Multiple Regression
		// runMLRMenuItem = new JMenuItem("Run Multiple Regression",
		// KeyEvent.VK_R);
		// runMLRMenuItem.addActionListener(this);
		// runMLRMenuItem.setEnabled(true);
		// // runMLRMenuItem.setActionCommand("run mlr");
		// menu.add(runMLRMenuItem);
	}

	private void initializePanel() {
		pcPanel = new PCPanel(dataModel);
		pcPanel.setBackground(Color.white);

		// int initialAlphaValue = (int) ((double)
		// (pcPanel.getAlphaValue()/255.) * 100);
		settingsPanel = new DisplaySettingsPanel(pcPanel.getFocusLineColor(),
				pcPanel.getContextLineColor());
		settingsPanel.addPCDisplaySettingsPanelListener(this);
		settingsPanel.setBorder(BorderFactory
				.createTitledBorder("Display Settings"));
		JScrollPane settingsPanelScroller = new JScrollPane(settingsPanel);
		// JScrollPane pcPanelScroller = new JScrollPane(pcPanel);

		// scatterplotPanel = new ScatterplotRowPanel(dataModel);
		// scatterplotPanel.setBackground(Color.white);

		// JSplitPane plotSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// pcPanel, scatterplotPanel);
		// plotSplit.setDividerLocation(300);
		// plotSplit.setOneTouchExpandable(true);
		//
		corrMatrixPanel = new CorrelationMatrixPanel(dataModel);
		corrMatrixPanel.setBackground(Color.white);

		// varSummaryPanel = new VariableSummaryPanel(dataModel);
		// varSummaryPanel.setBackground(Color.white);

		// varListPanel = new VariableListPanel(dataModel);
		// varListPanel.setBackground(Color.white);
		// JScrollPane varListPanelScroller = new JScrollPane(varListPanel);

		// JSplitPane sideSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// corrMatrixPanel, varListPanelScroller);
		// JSplitPane sideSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// corrMatrixPanel, settingsPanel);
		// sideSplit.setDividerLocation(200);
		// sideSplit.setOneTouchExpandable(true);

		// JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// sideSplit, pcPanelScroller);
		// mainSplit.setDividerLocation(200);
		// mainSplit.setOneTouchExpandable(true);

        dataTableModel = new DataTableModel(dataModel);
        dataTable = new JTable(dataTableModel);
        dataTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        dataTable.getSelectionModel().addListSelectionListener(this); //TODO: handle selections of tuples in data table
        JScrollPane dataTableScroller = new JScrollPane(dataTable);
        JPanel dataTablePanel = new JPanel();
        dataTablePanel.setLayout(new BorderLayout());
        dataTablePanel.add(dataTableScroller, BorderLayout.CENTER);

		colTableModel = new ColumnTableModel(dataModel);
		statsTable = new JTable(colTableModel);
		statsTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		statsTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane tableScroller = new JScrollPane(statsTable);

		JPanel statsTablePanel = new JPanel();
		statsTablePanel.setLayout(new BorderLayout());
		// statsTablePanel.setLayout(new BoxLayout(statsTablePanel,
		// BoxLayout.PAGE_AXIS));

		queryStatsCheckBox = new JCheckBox("Show Statistics for Selected Lines");
		queryStatsCheckBox.addItemListener(this);
		statsTablePanel.add(queryStatsCheckBox, BorderLayout.NORTH);
		statsTablePanel.add(tableScroller, BorderLayout.CENTER);

		// scatterplotPanel = new ScatterPlotPanel(dataModel);
		// scatterplotPanel.setBackground(Color.white);

		// JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		// pcPanel, tableScroller);
		// centerSplit.setDividerLocation(500);
		// centerSplit.setOneTouchExpandable(true);

		// JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		// sideSplit, centerSplit);
		// mainSplit.setDividerLocation(320);
		// mainSplit.setOneTouchExpandable(true);

		JTabbedPane tabPane = new JTabbedPane();
        tabPane.add(dataTablePanel, "Data Table");
		tabPane.add(statsTablePanel, "Column Statistics Table");
		tabPane.add(settingsPanelScroller, "Display Settings");

		JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				corrMatrixPanel, tabPane);
		bottomSplit.setDividerLocation(200);
		bottomSplit.setOneTouchExpandable(true);

		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				pcPanel, bottomSplit);
		mainSplit.setDividerLocation(450);
		mainSplit.setOneTouchExpandable(true);

		JPanel mainPanel = (JPanel) edenFrame.getContentPane();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(mainSplit, BorderLayout.CENTER);
	}

	static File file = null;
	static double sampleFactor = Double.NaN;

	public static void main(String args[]) throws Exception {
//		if (args.length == 0)  {
//			System.err.println("I need a file!");
//			System.err.println("Usage: java EDEN <input_filename> [optional_parameter]");
//			System.exit(0);
//		}
		
		if (args.length > 0) {
			file = new File(args[0]);

			if (args.length == 2) {
				try {
					sampleFactor = Double.parseDouble(args[1]);
					if (sampleFactor < 0. || sampleFactor > 1.) {
						sampleFactor = Double.NaN;
						System.err.println("Second argument ignored from command line.  This argument is reserved for the sample factor but it is outside the valid range (0.0 to 1.0).");
					}
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
					System.err.print("Second argument ignored from command line.  This argument is reserved for the sample factor but an exception occured in parsing it.");
				}
			}
		}

		String edenHome = System.getProperty("EDEN_HOME");
		if (edenHome == null) {
			edenHome = System.getProperty("user.dir");
			log.warn("EDEN_HOME environment variable is not set. Using user home directory as default.");
		}
		log.debug("EDEN_HOME is '" + edenHome + "'");

		GUIProperties.loadResources(edenHome);
		GUIProperties properties = new GUIProperties();
		try {
			properties.load();
		} catch (IOException ioe) {
			log.error("Exception caught while loading the EDEN properties file:\n"
					+ ioe.fillInStackTrace());
		}

		GUIContext.getInstance().registerComponent(GUIContext.PROPERTIES,
				properties);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (file != null) {
						if (!Double.isNaN(sampleFactor)) {
							EDEN window = new EDEN(file, sampleFactor);
						} else {
							EDEN window = new EDEN(file);
						}
					} else {
						EDEN window = new EDEN();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void windowActivated(WindowEvent event) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
	}

	@Override
	public void windowClosing(WindowEvent event) {
		if (GUIContext.getInstance().getProperties() != null) {
			GUIContext.getInstance().getProperties().save();
		}
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
	}

	@Override
	public void windowDeiconified(WindowEvent event) {
	}

	@Override
	public void windowIconified(WindowEvent event) {
	}

	@Override
	public void windowOpened(WindowEvent event) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("open file sampled csv")) {
			String lastDirectoryPath = (String) GUIContext.getInstance()
					.getProperties().get("LAST_CSV_DIRECTORY_PATH");
			JFileChooser chooser = new JFileChooser(lastDirectoryPath);
			chooser.setDialogTitle("Open CSV File");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = chooser.showOpenDialog(this.edenFrame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				int retValue = JOptionPane
						.showConfirmDialog(
								edenFrame,
								"Do you want to select a random sample of row tuples from this file?",
								"Sample Option?", JOptionPane.YES_NO_OPTION);
				double sampleFactor = Double.NaN;
				if (retValue == JOptionPane.YES_OPTION) {
					String sampleSizeString = JOptionPane.showInputDialog(
							edenFrame,
							"Enter sample size factor (between 0.0 and 1.0)",
							"Sample Size Factor", JOptionPane.QUESTION_MESSAGE);
					if (sampleSizeString != null) {
						try {
							sampleFactor = Double.parseDouble(sampleSizeString);
						} catch (NumberFormatException ex) {
							ex.printStackTrace();
							System.err
									.println("Error parsing the sample size string");
							return;
						}
					}
				}

				try {
					dataModel.clear();
					if (!Double.isNaN(sampleFactor)) {
						IOUtilities.readCSVSample(selectedFile, dataModel, sampleFactor);
					} else {
						IOUtilities.readCSV(selectedFile, dataModel);
					}
					String path = selectedFile.getParentFile().getCanonicalPath();
					GUIContext.getInstance().getProperties().put("LAST_CSV_DIRECTORY_PATH", path);
					edenFrame.setTitle(VERSION_STRING + " | " + selectedFile.getName());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if (e.getActionCommand().equals("open file csv")) {
			String lastDirectoryPath = (String) GUIContext.getInstance().getProperties().get("LAST_CSV_DIRECTORY_PATH");
			JFileChooser chooser = new JFileChooser(lastDirectoryPath);
			chooser.setDialogTitle("Open CSV File");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = chooser.showOpenDialog(this.edenFrame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();

				try {
					dataModel.clear();
					IOUtilities.readCSV(selectedFile, dataModel);
					String path = selectedFile.getParentFile().getCanonicalPath();
					GUIContext.getInstance().getProperties().put("LAST_CSV_DIRECTORY_PATH", path);
					edenFrame.setTitle(VERSION_STRING + " | " + selectedFile.getName());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if (e.getActionCommand().equals("exit")) {
			System.exit(0);
		} else if (e.getSource() == this.removeHighlightedAxisMenuItem) {
			// remove highlighted axis from data model
			if (dataModel.getHighlightedColumn() != null) {
				dataModel.disableColumn(dataModel.getHighlightedColumn());
			}
		} else if (e.getSource() == this.removeSelectedDataMenuItem) {
			// remove selected lines from data model
			int linesRemoved = dataModel.removeSelectedTuples();
			JOptionPane.showMessageDialog(edenFrame, linesRemoved
					+ " tuples removed.", "Tuples Removed",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getSource() == removeUnselectedDataMenuItem) {
			int linesRemoved = dataModel.removeUnselectedTuples();
//			int linesRemoved = dataModel.removeSelectedTuples();
			JOptionPane.showMessageDialog(edenFrame, linesRemoved
					+ " tuples removed.", "Tuples Removed",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getSource() == runMLRMenuItem) {
			// run multiple linear regression for highlighted axis
			runMultipleLinearRegression();
		} else if (e.getSource() == runMultiCollinearityFilterMenuItem) {
			runMulticollinearityFilter();
		} else if (e.getActionCommand().equals("save selected lines")) {
			try {
				saveSelectedLines();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else if (e.getSource() == this.arrangeByCorrelation) {
			if (dataModel.getHighlightedColumn() == null) {
				JOptionPane.showMessageDialog(
								this.edenFrame,
								"An axis must be highlighted. Please select an axis and try again.",
								"No Highlighted Axis",
								JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (useSelectedDataArrangeMenuItem.isSelected() && dataModel.getActiveQuery().hasColumnSelections()) {
				dataModel.orderColumnsByCorrelation(dataModel.getHighlightedColumn(), true);
			} else {
				dataModel.orderColumnsByCorrelation(dataModel.getHighlightedColumn(), false);
			}
//			pcPanel.arrangeColumnsByCorrelation(
//					dataModel.getHighlightedColumn(),
//					useSelectedDataArrangeMenuItem.isSelected());
		} else if (e.getSource() == this.arrangeByDispersion) {
			pcPanel.arrangeColumnsByDispersion(useSelectedDataArrangeMenuItem
					.isSelected());
		} else if (e.getSource() == arrangeByDispersionDifference) {
			pcPanel.arrangeColumnsByDispersionDifference();
		} else if (e.getSource() == arrangeByTypical) {
			pcPanel.arrangeColumnsByTypical(useSelectedDataArrangeMenuItem
					.isSelected());
		} else if (e.getSource() == arrangeByTypicalDifference) {
			pcPanel.arrangeColumnsByTypicalDifference();
		} else if (e.getSource() == this.antialiasMenuItem) {
			pcPanel.setAntialiasEnabled(antialiasMenuItem.isSelected());
		} else if (e.getActionCommand().equals("screen capture")) {
			String lastDirectoryPath = (String) GUIContext.getInstance().getProperties().get("LAST_SCREEN_CAPTURE_DIRECTORY_PATH");
			JFileChooser chooser = new JFileChooser(lastDirectoryPath);
			chooser.setDialogTitle("Save Selected Lines");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int ret = chooser.showSaveDialog(this.edenFrame);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				
				BufferedImage pcPanelImage = new BufferedImage(pcPanel.getWidth()*4, pcPanel.getHeight()*4, BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2 = pcPanelImage.createGraphics();

                g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setTransform(AffineTransform.getScaleInstance(4., 4.));

				pcPanel.paintComponent(g2);
				
				try {
					ImageIO.write(pcPanelImage, "png", selectedFile);
					String path = selectedFile.getParentFile().getCanonicalPath();
					GUIContext.getInstance().getProperties().put("LAST_SCREEN_CAPTURE_DIRECTORY_PATH", path);
				} catch (IOException ex) {
					ex.printStackTrace();
					log.debug(ex.getMessage());
				}
			}
		}
	}

	private void saveSelectedLines() throws IOException {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save Selected Lines");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = chooser.showSaveDialog(this.edenFrame);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					selectedFile));

			// Get header line string
			StringBuffer headerLineBuffer = new StringBuffer();
			ArrayList<Integer> enabledColumnIndices = new ArrayList<Integer>();
			for (int i = 0; i < dataModel.getColumnCount(); i++) {
				Column column = dataModel.getColumn(i);
				if (column.isEnabled()) {
					if (headerLineBuffer.length() == 0) {
						headerLineBuffer.append(column.getName());
					} else {
						headerLineBuffer.append(", " + column.getName());
					}
					enabledColumnIndices.add(i);
				}
			}

			String headerLine = headerLineBuffer.toString().trim();
			writer.write(headerLine + "\n");

			// Get tuple lines and write to file
			ArrayList<Tuple> queriedTuples = dataModel.getQueriedTuples();
			for (int iTuple = 0; iTuple < queriedTuples.size(); iTuple++) {
				Tuple tuple = queriedTuples.get(iTuple);
				StringBuffer lineBuffer = new StringBuffer();
				for (int columnIdx : enabledColumnIndices) {
					if (lineBuffer.length() == 0) {
						lineBuffer.append(tuple.getElement(columnIdx));
					} else {
						lineBuffer.append(", " + tuple.getElement(columnIdx));
					}
				}
				String line = lineBuffer.toString().trim();

				if (iTuple == 0) {
					writer.write(line);
				} else {
					writer.write("\n" + line);
				}
			}

			writer.close();
		}
	}

	private void runMulticollinearityFilter() {
		if (dataModel.getHighlightedColumn() == null) {
			JOptionPane
					.showMessageDialog(
							this.edenFrame,
							"An axis must be highlighted. Please select an axis and try again.",
							"No Highlighted Axis", JOptionPane.ERROR_MESSAGE);
			return;
		}
		dataModel.runMulticollinearityFilter(dataModel.getHighlightedColumn(),
				true, 0.5f);
		// dataModel.runMulticollienarityFilter(dataModel.getHighlightedColumn(),
		// true, 0.5f);
	}

	private void runMultipleLinearRegression() {
		if (dataModel.getHighlightedColumn() == null) {
			JOptionPane.showMessageDialog(this.edenFrame,
					"An axis must be highlighted", "No Highlighted Axis",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		OLSMultipleLinearRegression regression = dataModel
				.calculateOLSMultipleLinearRegression(dataModel
						.getHighlightedColumn());
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		// log.debug("valueChanged source class is " +
		// event.getSource().getClass().toString());
		if (event.getSource() == statsTable.getSelectionModel()) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			String columnName = (String) statsTable.getValueAt(
					statsTable.getSelectedRow(), 1);
			Column column = dataModel.getColumn(columnName);
			dataModel.setHighlightedColumn(column);
		}
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		// if (dataModel.getColumnCount() >= 2) {
		// scatterplotPanel.setAxes(dataModel.getColumn(0),
		// dataModel.getColumn(1));
		// }
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
		int colIndex = dataModel.getColumnIndex(dataModel
				.getHighlightedColumn());
		if (colIndex != -1) {
			statsTable.setRowSelectionInterval(colIndex, colIndex);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getItem() == showFilteredDataMenuItem) {
			pcPanel.setShowingFilteredData(showFilteredDataMenuItem.isSelected());
		} else if (event.getItem() == showAxesAsBarsMenuItem) {
			pcPanel.setShowingAxesAsBars(showAxesAsBarsMenuItem.isSelected());
		} else if (event.getItem() == showQueryLimitsMenuItem) {
			pcPanel.setShowingQueryLimits(showQueryLimitsMenuItem.isSelected());
		} else if (event.getItem() == this.renderTuplesAsLines) {
			pcPanel.setTupleDisplayMode(PCPanel.RENDER_TUPLES_AS_LINES);
		} else if (event.getItem() == this.renderTuplesAsDots) {
			pcPanel.setTupleDisplayMode(PCPanel.RENDER_TUPLES_AS_DOTS);
		} else if (event.getItem() == this.showFrequencyDataMenuItem) {
			pcPanel.setShowFrequencyInfo(showFrequencyDataMenuItem.isSelected());
		} else if (event.getItem() == this.useQueryFrequencyDataMenuItem) {
			pcPanel.setUseQueryFrequencyData(useQueryFrequencyDataMenuItem.isSelected());
		} else if (event.getSource() == meanDisplayMenuItem) {
			pcPanel.setDispersionDisplayMode(PCPanel.MEAN_DISPERSION_BOX_MODE);
		} else if (event.getSource() == medianDisplayMenuItem) {
			pcPanel.setDispersionDisplayMode(PCPanel.MEDIAN_DISPERSION_BOX_MODE);
		} else if (event.getItem() == showPCLinesMenuItem) {
			pcPanel.setShowingPolylines(showPCLinesMenuItem.isSelected());
		} else if (event.getItem() == this.queryStatsCheckBox) {
			colTableModel.setShowQueryStatistics(queryStatsCheckBox.isSelected());
		} else if (event.getItem() == useQueryCorrelationsMenuItem) {
			pcPanel.setUseQueryCorrelationCoefficients(useQueryCorrelationsMenuItem.isSelected());
			corrMatrixPanel.setUseQueryCorrelationCoefficients(useQueryCorrelationsMenuItem.isSelected());
		} else if (event.getItem() == showCorrelationIndicatorsMenuItem) {
			pcPanel.setShowCorrelationIndicators(showCorrelationIndicatorsMenuItem.isSelected());
		}
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		boolean querySet = dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections();
		arrangeByDispersionDifference.setEnabled(querySet);
		arrangeByTypicalDifference.setEnabled(querySet);
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
	}

	// @Override
	// public void pcAlphaSettingChanged(int newAlpha) {
	// // TODO Auto-generated method stub
	// pcPanel.setAlphaValue(newAlpha);
	// }

	@Override
	public void columnDisabled(DataModel dataModel, Column disabledColumn) {
	}

	@Override
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
	}

	@Override
	public void columnEnabled(DataModel dataModel, Column enabledColumn) {
	}

	@Override
	public void selectedDataColorChanged(Color color) {
		pcPanel.setFocusLineColor(color);
		corrMatrixPanel.setScatterplotFocusPointColor(color);
	}

	@Override
	public void unselectedDataColorChanged(Color color) {
		pcPanel.setContextLineColor(color);
		corrMatrixPanel.setScaterplotContextPointColor(color);
	}

	@Override
	public void secondaryFontSizeChanged(int fontSize) {
		pcPanel.setSecondaryFontSize(fontSize);
	}

	@Override
	public void titleFontSizeChanged(int fontSize) {
		pcPanel.setTitleFontSize(fontSize);
	}

	@Override
	public void axisWidthChanged(int width) {
		pcPanel.setAxisBarWidth(width);
	}

	@Override
	public void scatterplotSizeChanged(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void correlationBoxSizeChanged(int size) {
		pcPanel.setCorrelationBoxSize(size);
	}

	@Override
	public void pcLineSizeChanged(int size) {
		pcPanel.setLineSize(size);
	}

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		boolean querySet = dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections();
		arrangeByDispersionDifference.setEnabled(querySet);
		arrangeByTypicalDifference.setEnabled(querySet);
	}
	
	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		// TODO Auto-generated method stub
		boolean querySet = dataModel.getActiveQuery() != null && dataModel.getActiveQuery().hasColumnSelections();
		arrangeByDispersionDifference.setEnabled(querySet);
		arrangeByTypicalDifference.setEnabled(querySet);
	}
}
