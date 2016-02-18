package gov.ornl.csed.cda.coalesce;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.ColumnSelection;
import gov.ornl.csed.cda.datatable.ColumnSelectionRange;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.DataModelListener;
import gov.ornl.csed.cda.datatable.IOUtilities;
import gov.ornl.csed.cda.datatable.Tuple;
import gov.ornl.csed.cda.hypervis.*;
import gov.ornl.csed.cda.pcvis.CorrelationMatrixPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by csg on 1/17/15.
 */
public class EDENx implements OverviewBarListener, ActionListener, DataModelListener, WindowListener, HyperVariatePanelListener, ListSelectionListener {
    private final static Logger log = LoggerFactory.getLogger(EDENx.class);

    private final static int INITIAL_WINDOW_WIDTH = 800;
    private final static String TITLE_STRING = "EDENx";
    private static File file;

    private JFrame frame;
    private DataModel dataModel;
    private HyperVariatePanel hyperVariatePanel;
    private OverviewBar overviewBar;
    private JScrollPane hyperPCPanelScroller;
    private HashMap<OverviewBarMarker, Column> markerColumnMap = new HashMap<OverviewBarMarker, Column>();
    private HashMap<Column, OverviewBarMarker> columnMarkerMap = new HashMap<Column, OverviewBarMarker>();
    private ArrayList<OverviewBarMarker> selectedMarkers = new ArrayList<OverviewBarMarker>();
    private ColumnTableModel columnTableModel;
    private JTable columnTable;
    private CorrelationMatrixPanel correlationMatrix;
    private NumberFormat percentDF = new DecimalFormat("###.##%");

    private TimeSeries timeSeries;
    private TimeSeriesPanel overviewTimeSeriesPanel;
    private TimeSeriesPanel detailsTimeSeriesPanel;

    private JTextArea queryTextArea;
    private JMenuItem removeSelectedDataMenuItem;
    private JMenuItem removeUnselectedDataMenuItem;
    private JMenuItem removeHighlightedAxisMenuItem;

    private JToolBar toolBar;
    private JLabel statusLabel;

    public EDENx() {
        initialize();
        frame.setVisible(true);
    }

    public EDENx(File dataFile) {
        this();
        try {
            IOUtilities.readCSV(dataFile, dataModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EDENx(ArrayList<Tuple> tuples, ArrayList<Column> columns) {
        this();
        dataModel.setData(tuples, columns);
    }

    private void initialize() {
        dataModel = new DataModel();
        dataModel.addDataModelListener(this);

        frame = new JFrame();
        frame.addWindowListener(this);
        frame.setTitle(TITLE_STRING);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = INITIAL_WINDOW_WIDTH;
        double height = screenSize.getHeight();
        frame.setBounds(10, 10, (int)width, (int)height);

        initializePanel();
        initializeMenu();
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // the file menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem mi = new JMenuItem("Open CSV...", KeyEvent.VK_O);
        mi.addActionListener(this);
        mi.setActionCommand("open file csv");
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Export Current Selection...", KeyEvent.VK_E);
        mi.addActionListener(this);
        mi.setActionCommand("export selected data");
        menu.add(mi);

        menu.addSeparator();

        mi = new JMenuItem("Exit", KeyEvent.VK_X);
        mi.addActionListener(this);
        mi.setActionCommand("exit");
        menu.add(mi);

        // the view menu
        menu = new JMenu("View");
        menuBar.add(menu);
        JMenu pcMenu = new JMenu("View in Parallel Coordinates");
        menu.add(pcMenu);
        mi = new JMenuItem("Selected Data");
        mi.addActionListener(this);
        mi.setActionCommand("pcp selected data");
        pcMenu.add(mi);
        mi = new JMenuItem("All Data");
        mi.addActionListener(this);
        mi.setActionCommand("pcp all data");
        pcMenu.add(mi);

        JMenu arrangeMenu = new JMenu("Rearrange Variables");
        menu.add(arrangeMenu);
        mi = new JMenuItem("By Correlation with Highlighted Axis");
        mi.addActionListener(this);
        mi.setActionCommand("arrange correlation");
        arrangeMenu.add(mi);
        mi = new JMenuItem("By Dispersion");
        mi.setEnabled(false);
        mi.addActionListener(this);
        mi.setActionCommand("arrange dispersion");
        arrangeMenu.add(mi);
        mi = new JMenuItem("By Typical Value");
        mi.setEnabled(false);
        mi.addActionListener(this);
        mi.setActionCommand("arrange typical");
        arrangeMenu.add(mi);

//        JMenu hideVariablesMenu = new JMenu("Filter Variables");
//        menu.add(hideVariablesMenu);
//        mi = new JMenuItem("Filter by Correlation Coefficient...");
//        mi.setEnabled(true);
//        mi.addActionListener(this);
//        mi.setActionCommand("filter variables correlation");
//        hideVariablesMenu.add(mi);

//        JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem("Show Lines for Queried Data", hyperVariatePanel.getDrawQueriedDataLines());
//        checkBoxMenuItem.setEnabled(true);
//        checkBoxMenuItem.addActionListener(this);
//        checkBoxMenuItem.setActionCommand("show queried lines");
//        menu.add(checkBoxMenuItem);

        // The data menu
        menu = new JMenu("Data");
        menuBar.add(menu);
        removeSelectedDataMenuItem = new JMenuItem("Remove Selected Data",
                KeyEvent.VK_S);
        removeSelectedDataMenuItem.addActionListener(this);
        removeSelectedDataMenuItem.setActionCommand("remove selected data");
        menu.add(removeSelectedDataMenuItem);

        removeUnselectedDataMenuItem = new JMenuItem("Remove Unselected Data",
                KeyEvent.VK_U);
        removeUnselectedDataMenuItem.setEnabled(true);
        removeUnselectedDataMenuItem.addActionListener(this);
        menu.add(removeUnselectedDataMenuItem);

        removeHighlightedAxisMenuItem = new JMenuItem(
                "Remove Highlighted Variable", KeyEvent.VK_H);
        removeHighlightedAxisMenuItem.addActionListener(this);
        removeHighlightedAxisMenuItem
                .setActionCommand("remove highlighted variable");
        menu.add(removeHighlightedAxisMenuItem);
    }

    private JPanel createSettingsPanel () {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

        columnTableModel = new ColumnTableModel(dataModel);
        columnTable = new JTable(columnTableModel);
        columnTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnTable.getSelectionModel().addListSelectionListener(this);
        JScrollPane tableScroller = new JScrollPane(columnTable);

        queryTextArea = new JTextArea();
        queryTextArea.setLineWrap(true);
        queryTextArea.setWrapStyleWord(true);
        queryTextArea.setEditable(false);

        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add("Variables", tableScroller);
        tabPane.add("Query Text", queryTextArea);
        settingsPanel.add(tabPane, BorderLayout.CENTER);
        return settingsPanel;
    }

    private void initializePanel() {

        JPanel settingsPanel = createSettingsPanel();
        settingsPanel.setPreferredSize(new Dimension(1000, 250));

        hyperVariatePanel = new HyperVariatePanel(dataModel);
        hyperVariatePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hyperVariatePanel.addHyperPCPanelListener(this);
        hyperVariatePanel.setBackground(Color.WHITE);

        hyperPCPanelScroller = new JScrollPane(hyperVariatePanel);
//        hyperPCPanelScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        hyperPCPanelScroller.getVerticalScrollBar().setUnitIncrement(10);
        hyperPCPanelScroller.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
//                log.debug("pcScrollPane resized: " + hyperPCPanelScroller.getSize().toString());
//                log.debug("hyperPCPanel height: " + hyperPCPanel.getHeight());
                int height = hyperVariatePanel.getHeight();
                int width = hyperPCPanelScroller.getWidth();
                hyperVariatePanel.setPreferredSize(new Dimension(width - 20, height));
                hyperVariatePanel.revalidate();
                hyperPCPanelScroller.revalidate();
            }
        });

        overviewBar = new OverviewBar(hyperPCPanelScroller);
        overviewBar.setBackground(Color.white);
        overviewBar.addOverviewBarListener(this);

        JPanel summaryVisPanel = new JPanel();
        summaryVisPanel.setBorder(BorderFactory.createTitledBorder("HyperVariate Histogram View"));
        summaryVisPanel.setLayout(new BorderLayout());
        summaryVisPanel.add(hyperPCPanelScroller, BorderLayout.CENTER);
        summaryVisPanel.add(overviewBar, BorderLayout.EAST);

        overviewTimeSeriesPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        overviewTimeSeriesPanel.setBackground(Color.white);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1000, 80));
        Border overviewTimeSeriesBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2), BorderFactory.createEtchedBorder());
        overviewTimeSeriesPanel.setBorder(overviewTimeSeriesBorder);
        detailsTimeSeriesPanel = new TimeSeriesPanel(1, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        detailsTimeSeriesPanel.setBackground(Color.white);
        detailsTimeSeriesPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        JScrollPane detailsTimeSeriesScroller = new JScrollPane(detailsTimeSeriesPanel);
        detailsTimeSeriesScroller.getVerticalScrollBar().setUnitIncrement(4);

        detailsTimeSeriesScroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting() && timeSeries != null) {
                    JScrollBar scrollBar = (JScrollBar) e.getSource();
                    double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
                    double norm = (double) scrollBar.getModel().getValue() / scrollBarModelWidth;
                    double deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                    Instant startHighlightInstant = timeSeries.getStartInstant().plusMillis((long) deltaTime);
                    int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                    norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                    deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                    Instant endHighlightInstant = timeSeries.getEndInstant().minusMillis((long) deltaTime);
                    overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                }
            }
        });

        JPanel timeSeriesPanel = new JPanel();
        timeSeriesPanel.setBorder(BorderFactory.createTitledBorder("Overview + Detail Time View"));

        timeSeriesPanel.setLayout(new BorderLayout());
        timeSeriesPanel.add(detailsTimeSeriesScroller, BorderLayout.CENTER);
        timeSeriesPanel.add(overviewTimeSeriesPanel, BorderLayout.SOUTH);

        JSplitPane visSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, timeSeriesPanel, summaryVisPanel);
        visSplit.setDividerLocation(250);
        visSplit.setOneTouchExpandable(true);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visSplit, settingsPanel);
//        mainSplit.setDividerLocation(.2);
        mainSplit.setResizeWeight(.95);
        mainSplit.setOneTouchExpandable(true);
        mainSplit.setContinuousLayout(true);

        toolBar = createToolBar();

        JPanel mainPanel = (JPanel)frame.getContentPane();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainSplit, BorderLayout.CENTER);
        mainPanel.add(toolBar, BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        statusLabel = new JLabel("Ready");

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setLayout(new BorderLayout());
        toolBar.add(statusLabel, BorderLayout.WEST);

        return toolBar;
    }

    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            file = new File(args[0]);
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
            log.error("Exception caught while loading the EDEN properties file:\n" + ioe.fillInStackTrace());
        }

        GUIContext.getInstance().registerComponent(GUIContext.PROPERTIES, properties);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (file != null) {
                        EDENx window = new EDENx(file);
                    } else {
                        EDENx window = new EDENx();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("open file csv")) {
            String lastDirectoryPath = (String) GUIContext.getInstance().getProperties().get("LAST_CSV_DIRECTORY_PATH");
            JFileChooser chooser = new JFileChooser(lastDirectoryPath);
            chooser.setDialogTitle("Open CSV File");
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int ret = chooser.showOpenDialog(this.frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();

                try {
                    dataModel.clear();
                    IOUtilities.readCSV(selectedFile, dataModel);
                    String path = selectedFile.getParentFile().getCanonicalPath();
                    GUIContext.getInstance().getProperties().put("LAST_CSV_DIRECTORY_PATH", path);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (e.getActionCommand().equals("pcp selected data")) {
//        else if (e.getSource() == this.openPCPanelButton) {
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                ArrayList<Tuple> queriedTuples = dataModel.getQueriedTuples();
                ArrayList<Tuple> copyTuples = new ArrayList<Tuple>();
                for (Tuple queryTuple : queriedTuples) {
                    Tuple copyTuple = new Tuple(queryTuple);
                    copyTuples.add(copyTuple);
                }

                ArrayList<Column> activeColumns = dataModel.getColumns();
                ArrayList<Column> copyColumns = new ArrayList<Column>();
                for (Column column : activeColumns) {
                    Column copyColumn = new Column(column.getName());
                    copyColumns.add(copyColumn);
                }
                EDEN eden = new EDEN(copyTuples, copyColumns);
            } else {
                log.debug("no data is selected currently");
            }
        } else if (e.getActionCommand().equals("pcp all data")) {
            ArrayList<Tuple> tuples = new ArrayList<Tuple>();
            for (int i = 0; i < dataModel.getTupleCount(); i++) {
                Tuple tuple = new Tuple(dataModel.getTuple(i));
                tuples.add(tuple);
            }

            ArrayList<Column> columns = dataModel.getColumns();
            ArrayList<Column> copyColumns = new ArrayList<Column>();
            for (Column column : columns) {
                Column copyColumn = new Column(column.getName());
                copyColumns.add(copyColumn);
            }
            EDEN eden = new EDEN(tuples, copyColumns);
//            if (queriedTuples != null && !queriedTuples.isEmpty()) {
//
//                // make a copy of the Columns in the data model (note indices of disabled columns)
//                ArrayList<Integer> disableColumnIndices = new ArrayList<Integer>();
//                ArrayList<Column> enabledColumns = new ArrayList<Column>();
//                for (int i = 0; i < dataModel.getColumns().size(); i++) {
//                    Column column = dataModel.getColumn(i);
//                    if (column.isEnabled()) {
//                        Column newColumn = new Column(column.getName());
//                        enabledColumns.add(newColumn);
//                    } else {
//                        disableColumnIndices.add(i);
//                    }
//                }
//
//                // make a new list of tuples removing any columns that are currently disabled in the data model
//                ArrayList<Tuple> newTuples = new ArrayList<Tuple>();
//                for (Tuple tuple : queriedTuples) {
//                    Tuple newTuple = new Tuple();
//                    for (int i = 0; i < tuple.getElementCount(); i++) {
//                        if (!disableColumnIndices.contains(i)) {
//                            newTuple.addElement(tuple.getElement(i));
//                        }
//                    }
//                    newTuples.add(newTuple);
//                }
//                EDEN eden = new EDEN(newTuples, enabledColumns);
//            } else {
//                EDEN eden = new EDEN(dataModel.getTuples(), dataModel.getColumns());
//            }
        } else if (e.getActionCommand().equals("arrange correlation")) {
            if (dataModel.getHighlightedColumn() == null) {
                JOptionPane.showMessageDialog(frame,
                        "An axis must be highlighted. Please select an axis and try again.", "No Highlighted Axis",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                dataModel.orderColumnsByCorrelation(dataModel.getHighlightedColumn(), true);
//                hyperPCPanel.arrangeColumnsByCorrelation(dataModel.getHighlightedColumn(), true);
            } else {
                dataModel.orderColumnsByCorrelation(dataModel.getHighlightedColumn(), false);
//                hyperPCPanel.arrangeColumnsByCorrelation(dataModel.getHighlightedColumn(), false);
            }

            addColumnOverviewBarMarkers();
        } else if (e.getSource() == removeHighlightedAxisMenuItem) {
            if (dataModel.getHighlightedColumn() != null) {
                dataModel.disableColumn(dataModel.getHighlightedColumn());
            }
        } else if (e.getSource() == removeSelectedDataMenuItem) {
            int linesRemoved = dataModel.removeSelectedTuples();
            JOptionPane.showMessageDialog(frame, linesRemoved + " tuples removed.", "Tuples Removed",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == removeUnselectedDataMenuItem) {
            int linesRemoved = dataModel.removeUnselectedTuples();
            JOptionPane.showMessageDialog(frame, linesRemoved + " tuples removed.", "Tuples Removed",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("export selected data")) {
            try {
                saveSelectedData();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else if (e.getActionCommand().equals("exit")) {
            System.exit(0);
        }
    }

    private void saveSelectedData() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Selected Data");
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int ret = chooser.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));

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

    private void recolorOverviewBarMarkers() {
        int highlightedColumnIndex = -1;
        if (dataModel.getHighlightedColumn() != null) {
            highlightedColumnIndex = dataModel.getColumnIndex(dataModel.getHighlightedColumn());
        }

        for (int iColumn = 0; iColumn < dataModel.getColumnCount(); iColumn++) {
            Column column = dataModel.getColumn(iColumn);
            OverviewBarMarker marker = columnMarkerMap.get(column);

            if (iColumn == highlightedColumnIndex) {
                marker.fillColor = Color.black;
            } else if (highlightedColumnIndex == -1) {
                marker.fillColor = Color.white;
            } else {
                double correlationCoefficient = dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(iColumn);
                marker.fillColor = Utilities.getColorForCorrelationCoefficient(correlationCoefficient, 1.);
            }
        }
    }

    private void resetOverviewBarMarkerPositions() {
        for (int iColumn = 0; iColumn < dataModel.getColumnCount(); iColumn++) {
            Column column = dataModel.getColumn(iColumn);
            OverviewBarMarker marker = columnMarkerMap.get(column);
            float position = (float) iColumn / (float)(dataModel.getColumnCount()-1);
            marker.setPosition(position);
        }
    }

    private void addColumnOverviewBarMarkers() {
        overviewBar.removeAllMarkers();

        // TODO: if a query is set use the query correlation coefficients
        int highlightedColumnIndex = -1;
        if (dataModel.getHighlightedColumn() != null) {
            highlightedColumnIndex = dataModel.getColumnIndex(dataModel.getHighlightedColumn());
        }

        for (int iColumn = 0; iColumn < dataModel.getColumnCount(); iColumn++) {
            Column column = dataModel.getColumn(iColumn);
            boolean isHighlighted = (iColumn == highlightedColumnIndex);
            boolean isVisible = column.isEnabled();
            boolean isSelected = true;

            Color fillColor;
            if (iColumn == highlightedColumnIndex) {
                fillColor = Color.black;
            } else if (highlightedColumnIndex == -1) {
                fillColor = Color.white;
            } else {
                double correlationCoefficient = dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(iColumn);
                fillColor = Utilities.getColorForCorrelationCoefficient(correlationCoefficient, 1.);
            }

            float position = (float) iColumn / (float)(dataModel.getColumnCount()-1);
            OverviewBarMarker marker = new OverviewBarMarker(position, fillColor, Color.DARK_GRAY,
                    column.getName(), isHighlighted, true, isVisible);
            overviewBar.addMarker(marker);
            markerColumnMap.put(marker, column);
            columnMarkerMap.put(column, marker);
        }
    }


    @Override
    public void dataModelChanged(DataModel dataModel) {
        addColumnOverviewBarMarkers();
        overviewBar.layoutBar();
        overviewBar.repaint();

        if (dataModel.getActiveQuery().hasColumnSelections()) {
            int numSelectedTuples = dataModel.getActiveQuery().getTuples().size();
            int numTuples = dataModel.getTupleCount();
            double percentSelected = (double) numSelectedTuples / (double) numTuples;
            statusLabel.setText("Selected " + percentDF.format(percentSelected) + " (" + numSelectedTuples + "/" + numTuples + ") tuples");
        } else {
            statusLabel.setText("Showing " + dataModel.getTupleCount() + " tuples");
        }

        populateTimeSeries();
    }

    private void populateTimeSeries() {
        if ((dataModel.getTimeColumn() != null) && (dataModel.getHighlightedColumn() != null)) {
            int timeColumnIndex = dataModel.getColumnIndex(dataModel.getTimeColumn());
            int highlightedColumnIndex = dataModel.getColumnIndex(dataModel.getHighlightedColumn());

            timeSeries = new TimeSeries(dataModel.getHighlightedColumn().getName());
            for (Tuple tuple : dataModel.getTuples()) {
                Instant instant = Instant.ofEpochMilli((long)tuple.getElement(timeColumnIndex));
                double value = tuple.getElement(highlightedColumnIndex);
                if (!Double.isNaN(value)) {
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                }
            }

            overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
            detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }
    }

    @Override
    public void queryChanged(DataModel dataModel) {
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            int numSelectedTuples = dataModel.getActiveQuery().getTuples().size();
            int numTuples = dataModel.getTupleCount();
            double percentSelected = (double) numSelectedTuples / (double) numTuples;

            StringBuffer buffer = new StringBuffer();
            buffer.append("The current selection includes " + percentDF.format(percentSelected) + " of the total dataset " +
                    "(" + numSelectedTuples + " of " + numTuples + " total items).\n");
            buffer.append("The current selection includes ");
            for (ColumnSelection columnSelection : dataModel.getActiveQuery().getColumnSelections()) {
                buffer.append(columnSelection.getColumn().getName() + " values between ");
                if (columnSelection.getColumnSelectionRangeCount() == 1) {
                    ColumnSelectionRange columnSelectionRange = columnSelection.getColumnSelectionRanges().get(0);
                    buffer.append(columnSelectionRange.getMinValue() + " and " + columnSelectionRange.getMaxValue());
                }
            }
            buffer.append(".\n");

            statusLabel.setText("Selected " + percentDF.format(percentSelected) + " (" + numSelectedTuples + "/" + numTuples + ") tuples");
            queryTextArea.setText(buffer.toString());
        } else {
            statusLabel.setText("Showing " + dataModel.getTupleCount() + " tuples");
            queryTextArea.setText("");
        }
    }

    @Override
    public void highlightedColumnChanged(DataModel dataModel) {
        // TODO: reset the highlighted overview bar marker to the new highlighted column marker
        recolorOverviewBarMarkers();
        if (dataModel.getHighlightedColumn() != null) {
            // recolor all markers to white fill color
            overviewBar.setHighlightedMarker(null);
        } else {
            // recolor all markers based on correlation with highlighted column
            overviewBar.setHighlightedMarker(columnMarkerMap.get(dataModel.getHighlightedColumn()));
        }
    }

    @Override
    public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
        // TODO: recolor all overview bar markers and repaint it (should be check for new columns?)
    }

    @Override
    public void columnDisabled(DataModel dataModel, Column disabledColumn) {
        // TODO: find column overview bar marker and set visible flag to false
        OverviewBarMarker marker = columnMarkerMap.get(disabledColumn);
        if (marker != null) {
            overviewBar.removeMarker(marker);
            columnMarkerMap.remove(disabledColumn);
            markerColumnMap.remove(marker);
            resetOverviewBarMarkerPositions();
//            marker.setVisible(false);
            overviewBar.layoutBar();
            overviewBar.repaint();
        }
    }

    @Override
    public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {
        // TODO: final column overview bar markers and set visible flags to false
    }

    @Override
    public void columnEnabled(DataModel dataModel, Column enabledColumn) {
        int columnIndex = dataModel.getColumnIndex(enabledColumn);

        Color fillColor;
        if (dataModel.getHighlightedColumn() == null) {
            fillColor = Color.white;
        } else {
            double correlationCoefficient = dataModel.getHighlightedColumn().getSummaryStats().getCorrelationCoefficients().get(columnIndex);
            fillColor = Utilities.getColorForCorrelationCoefficient(correlationCoefficient, 1.);
        }

        float position = (float) columnIndex / (float)(dataModel.getColumnCount()-1);
        OverviewBarMarker marker = new OverviewBarMarker(position, fillColor, Color.DARK_GRAY,
                enabledColumn.getName(), false, true, true);
        overviewBar.addMarker(marker);
        markerColumnMap.put(marker, enabledColumn);
        columnMarkerMap.put(enabledColumn, marker);
        resetOverviewBarMarkerPositions();
        overviewBar.layoutBar();
        overviewBar.repaint();

//        OverviewBarMarker marker = columnMarkerMap.get(enabledColumn);
//        if (marker != null) {
////            marker.setVisible(true);
//            resetOverviewBarMarkerPositions();
//            overviewBar.layoutBar();
//            overviewBar.repaint();
//        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void hyperVariatePanelAxisClicked(HyperVariatePanel hyperVariatePanel, int axisIndex) {

    }

    @Override
    public void hyperVariatePanelPolylineClicked(HyperVariatePanel hyperVariatePanel, int x, int y) {

    }

    @Override
    public void overviewBarMarkerClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
        int scrollViewHeight = hyperPCPanelScroller.getVerticalScrollBar().getMaximum();
        int scrollY =  (int)(marker.position * scrollViewHeight) - (hyperPCPanelScroller.getVerticalScrollBar().getVisibleAmount()/2);
        hyperPCPanelScroller.getVerticalScrollBar().setValue(scrollY);
    }

    @Override
    public void overviewBarMarkerDoubleClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
        Column newHighlightedColumn = markerColumnMap.get(marker);
        dataModel.setHighlightedColumn(newHighlightedColumn);
    }

    @Override
    public void overviewBarMarkerControlClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
        log.debug("marker control clicked " + marker.position);
        Column column = markerColumnMap.get(marker);
        if (column.isEnabled()) {
            dataModel.disableColumn(column);
//            marker.setVisible(false);
        } else {
            dataModel.enableColumn(column);
//            marker.setVisible(true);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

    }

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		if (dataModel.getActiveQuery().hasColumnSelections()) {
            int numSelectedTuples = dataModel.getActiveQuery().getTuples().size();
            int numTuples = dataModel.getTupleCount();
            double percentSelected = (double) numSelectedTuples / (double) numTuples;

            StringBuffer buffer = new StringBuffer();
            buffer.append("The current selection includes " + percentDF.format(percentSelected) + " of the total dataset " +
                    "(" + numSelectedTuples + " of " + numTuples + " total items).\n");
            buffer.append("The current selection includes ");
            for (ColumnSelection columnSelection : dataModel.getActiveQuery().getColumnSelections()) {
                buffer.append(columnSelection.getColumn().getName() + " values between ");
                if (columnSelection.getColumnSelectionRangeCount() == 1) {
                    ColumnSelectionRange columnSelectionRange1 = columnSelection.getColumnSelectionRanges().get(0);
                    buffer.append(columnSelectionRange1.getMinValue() + " and " + columnSelectionRange1.getMaxValue());
                }
            }
            buffer.append(".\n");

            statusLabel.setText("Selected " + percentDF.format(percentSelected) + " (" + numSelectedTuples + "/" + numTuples + ") tuples");
            queryTextArea.setText(buffer.toString());
        } else {
            statusLabel.setText("Showing " + dataModel.getTupleCount() + " tuples");
            queryTextArea.setText("");
        }
	}

	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel,
			ColumnSelectionRange columnSelectionRange) {
		if (dataModel.getActiveQuery().hasColumnSelections()) {
            int numSelectedTuples = dataModel.getActiveQuery().getTuples().size();
            int numTuples = dataModel.getTupleCount();
            double percentSelected = (double) numSelectedTuples / (double) numTuples;

            StringBuffer buffer = new StringBuffer();
            buffer.append("The current selection includes " + percentDF.format(percentSelected) + " of the total dataset " +
                    "(" + numSelectedTuples + " of " + numTuples + " total items).\n");
            buffer.append("The current selection includes ");
            for (ColumnSelection columnSelection : dataModel.getActiveQuery().getColumnSelections()) {
                buffer.append(columnSelection.getColumn().getName() + " values between ");
                if (columnSelection.getColumnSelectionRangeCount() == 1) {
                    ColumnSelectionRange columnSelectionRange1 = columnSelection.getColumnSelectionRanges().get(0);
                    buffer.append(columnSelectionRange1.getMinValue() + " and " + columnSelectionRange1.getMaxValue());
                }
            }
            buffer.append(".\n");

            statusLabel.setText("Selected " + percentDF.format(percentSelected) + " (" + numSelectedTuples + "/" + numTuples + ") tuples");
            queryTextArea.setText(buffer.toString());
        } else {
            statusLabel.setText("Showing " + dataModel.getTupleCount() + " tuples");
            queryTextArea.setText("");
        }
	}
}
