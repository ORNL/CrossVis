package gov.ornl.csed.cda.Falcon;/**
 * Created by csg on 12/29/15.
 */

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.event.TableListener;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.TableReadListener;
import prefuse.data.parser.DataParseException;

import javax.swing.*;
import javax.swing.JMenuBar;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Falcon {
    private final static Logger log = LoggerFactory.getLogger(Falcon.class);

    private static File file = null;

    private JFrame frame;
    private Table dataTable;
    private TimeSeriesPanel overviewTimeSeriesPanel;
    private TimeSeriesPanel detailsTimeSeriesPanel;
    private TimeSeries timeSeries;

    private JComboBox<String> timeColumnComboBox;
    private JComboBox<String> selectedColumnComboBox;
    private JComboBox<ChronoUnit> detailChronoUnitComboBox;

    public Falcon () {
        initialize();
        frame.setVisible(true);
    }


    public Falcon (File dataFile) {
        this();
        readCSVTable(dataFile);
    }


    private void readCSVTable (File csvFile) {
        try {
            dataTable = new CSVTableReader().readTable(csvFile);
            dataTable.addTableListener(new TableListener() {
                @Override
                public void tableChanged(Table table, int i, int i1, int i2, int i3) {
                    // TODO: Handle events
                }
            });
        } catch (DataIOException e) {
            e.printStackTrace();
            return;
        }

        // TODO: populate timeseries panels
//        timeSeries.clear();
//        for (int i = 0; i < dataTable.getTupleCount(); i++) {
//            Instant instant = Instant.ofEpochMilli(dataTable.getLong(i, 0));
//            double value = dataTable.getDouble(i, 10);
//            timeSeries
//        }
        // TODO: populate data and column tables
        timeColumnComboBox.removeAllItems();
        timeColumnComboBox.addItem("");
        selectedColumnComboBox.removeAllItems();
        selectedColumnComboBox.addItem("");

        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            timeColumnComboBox.addItem(dataTable.getColumnName(i));
            selectedColumnComboBox.addItem(dataTable.getColumnName(i));
        }
    }


    private void initialize() {
        dataTable = new Table();

        frame = new JFrame();
        frame.setTitle("F a l c o n");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, 1400, 600);

        initializePanel();
        initializeMenu();
    }


    private JPanel createTimeSeriesPanel () {
        detailsTimeSeriesPanel = new TimeSeriesPanel(2, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        detailsTimeSeriesPanel.setBackground(Color.white);

        overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1400, 100));
        overviewTimeSeriesPanel.setBackground(Color.white);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        overviewTimeSeriesPanel.setBorder(border);

        JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        scroller.setBackground(frame.getBackground());
        scroller.setBorder(border);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scroller, BorderLayout.CENTER);
        panel.add(overviewTimeSeriesPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Display Settings"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Time Column: "), gbc);

        timeColumnComboBox = new JComboBox<>();
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panel.add(timeColumnComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Time ChronoUnit: "), gbc);

        detailChronoUnitComboBox = new JComboBox<>();
        detailChronoUnitComboBox.addItem(ChronoUnit.SECONDS);
        detailChronoUnitComboBox.addItem(ChronoUnit.MINUTES);
        detailChronoUnitComboBox.addItem(ChronoUnit.HOURS);
        detailChronoUnitComboBox.addItem(ChronoUnit.HALF_DAYS);
        detailChronoUnitComboBox.addItem(ChronoUnit.DAYS);
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(detailChronoUnitComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Value Column: "), gbc);

        selectedColumnComboBox = new JComboBox<>();
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        panel.add(selectedColumnComboBox, gbc);

        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applySettings();
            }
        });
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(applyButton, gbc);

        return panel;
    }

    private void applySettings() {
        timeSeries = new TimeSeries("TS");

        String timeColumn = (String)timeColumnComboBox.getSelectedItem();
        String valueColumn = (String)selectedColumnComboBox.getSelectedItem();

        detailsTimeSeriesPanel.removeTimeSeries();

        if (timeColumn.equals("") || valueColumn.equals("")) {
            return;
        }

        ChronoUnit chronoUnit = (ChronoUnit)detailChronoUnitComboBox.getSelectedItem();
        detailsTimeSeriesPanel.setPlotChronoUnit(chronoUnit);

        int timeColumnIndex = dataTable.getColumnNumber(timeColumn);
        int valueColumnIndex = dataTable.getColumnNumber(valueColumn);

        for (int i = 0; i < dataTable.getTupleCount(); i++) {
            Instant instant = Instant.ofEpochMilli(dataTable.getLong(i, timeColumnIndex));
            double value = dataTable.getDouble(i, valueColumnIndex);

            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
        }

        overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
    }

    private JTabbedPane createTablePanel() {
        JTable dataTable = new JTable();
        JTable columnTable = new JTable();

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Column Table", columnTable);
        tabbedPane.addTab("Data Table", dataTable);

        return tabbedPane;
    }


    private void initializePanel() {
        JPanel timeSeriesPanel = createTimeSeriesPanel();
        JPanel settingsPanel = createSettingsPanel();
        JTabbedPane tableTabbedPane = createTablePanel();

        JSplitPane viewSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, timeSeriesPanel, tableTabbedPane);
        viewSplit.setDividerLocation(300);
        viewSplit.setOneTouchExpandable(true);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsPanel, viewSplit);
        mainSplit.setDividerLocation(350);
        mainSplit.setOneTouchExpandable(true);

        ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
        ((JPanel)frame.getContentPane()).add(mainSplit, BorderLayout.CENTER);
    }


    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        JMenuItem mi = new JMenuItem("Open CSV...", KeyEvent.VK_O);
//        mi.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        }
        menu.add(mi);

        mi = new JMenuItem("Exit", KeyEvent.VK_X);
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.addSeparator();
        menu.add(mi);
    }


    public static void main (String args[]) {
        if (args.length > 0) {
            file = new File(args[0]);
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (file != null) {
                        Falcon window = new Falcon(file);
                    } else {
                        Falcon window = new Falcon();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
