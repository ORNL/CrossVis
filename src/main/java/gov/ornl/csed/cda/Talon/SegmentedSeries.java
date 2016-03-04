package gov.ornl.csed.cda.Talon;

import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.Falcon.PLGVariableSchema;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * Created by whw on 2/26/16.
 * halseywh@ornl.gov
 *
 */


public class SegmentedSeries  {
    // ========== CLASS FIELDS ==========
    private String version = "Talon v1.0";
    private JFrame frame;           // Frame for SegmentedSeries instance
    private JPanel topPanel;        // Panel for combobox
    private SegmentedTimeSeriesPanel segmentPanel;                  // Panel for segmented series plot
    private File plgFile;           // Captures .plg filename on open
    private HashMap<String, PLGVariableSchema> variableSchemaMap;   // Holds variableSchema of variables in plgFile
    private JComboBox<String> variableCB;                           // Combobox of variable names from plgFile in topPanel
    private String buildHeightVarName = "Builds.State.CurrentBuild.CurrentHeight";  // Name of variable to segment over
    private TimeSeries buildHeightTimeSeries;                       // Time series of variable to segment over
    private TimeSeries segmentTimeSeries;                           // Time series of variable to segment
    private TreeMap<Double, TimeSeries> segmentTimeSeriesMap;       // Tree map that holds final segmented time series

    // ========== CONSTRUCTOR ==========
    public SegmentedSeries () {
        // Call helper init functions and make visible
        initialize();
        frame.setVisible(true);
    }

    // ============ METHODS ============
    private void initialize () {
        // Create and set instance's frame
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 1400, 700);
        frame.setTitle(version);

        // Call helper functions to create panels and menu
        initializeMenu();
        initializePanel();
    }

    // Initializes the menubar, menus, and all menu items
    //  -> Sets actionListeners for menu items
    private void initializeMenu() {
        // Create instance's menu bar and add to frame
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // Populate the menu bar with menus and items
        JMenu file = new JMenu("File");
        menuBar.add(file);

        JMenuItem file_open = new JMenuItem("Open", KeyEvent.VK_O); // This menu item will respond to the 'o' key being pressed
        file.add(file_open);

        // Add action listeners to specific menu items
        //  -> Creates file chooser
        //  -> Opens file
        //  -> Grabs time series for segmenting variable
        //  -> Grabs schemas for all other variables of valid types
        //  -> Populates combobox with variable names
        file_open.addActionListener(new ActionListener() {

            // Process performed if open menu item is selected
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create file chooser and initialize
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select .plg to Open");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
                int retVal = fileChooser.showDialog(frame, "Open File");

                // Test to see if a file was chosen and opened
                if (retVal != JFileChooser.CANCEL_OPTION) {
                    segmentPanel.clearTimeSeries();
                    variableSchemaMap = null;
                    try {
                        // Get file from chooser
                        plgFile = fileChooser.getSelectedFile();

                        // Read schemas from selected file into schema map
                        variableSchemaMap = PLGFileReader.readVariableSchemas(plgFile);

                        // Reset topPanel combobox for new set of variables from selected file
                        variableCB.removeAllItems();

                        // Copy variable names with correct types
                        ArrayList<String> copyVarNames = new ArrayList<>();
                        for (PLGVariableSchema variableSchema : variableSchemaMap.values()) {
                            if (variableSchema.typeString.equals("Int16") ||
                                    variableSchema.typeString.equals("Double") ||
                                    variableSchema.typeString.equals("Single") ||
                                    variableSchema.typeString.equals("Int32")) {

                                /* TODO
                                    -> we need to ask them if they want to filter variable based on the number of values or not
                                 */
                                if (variableSchema.numValues > 10) {
                                    copyVarNames.add(variableSchema.variableName);
                                }
                            }
                        }

                        // Sort variable names
                        Collections.sort(copyVarNames);

                        // Add empty string and variable names to combobox
                        variableCB.addItem("");
                        for (String str : copyVarNames) {
                            variableCB.addItem(str);
                        }

                        // Add buildHeightVarName to ArrayList because PLGFileReader.readPLGFileAsTimeSeries expects Collection<String>
                        ArrayList<String> variables = new ArrayList<String>();
                        variables.add(buildHeightVarName);

                        // Grab output of PLGFileReader.readPLGFileAsTimeSeries and pull out TimeSeries for segmenting variable
                        HashMap<String, TimeSeries> readVarsMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, variables);
                        if (readVarsMap != null && !readVarsMap.isEmpty()) {
                            buildHeightTimeSeries = readVarsMap.get(buildHeightVarName);

                            // Debug: print number of points in time series for segmenting variable
                            System.out.println("buildHeightTimeSeries: " + buildHeightTimeSeries.getAllRecords().size());

                            // Add the filename to the frame title
                            frame.setTitle(version + " - " + plgFile.getName());
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return;
                    }
                }
            }
        });

    }

    // Initializes the panels for the tool
    //  -> Creates panel for combobox and sets actionListener
    //  -> Creates new SegmentedTimeSeries panel for the vis
    private void initializePanel() {
        /* TODO
            main segmented view panel
                custom
                i will/make/paint this myself
        */
        // Initialize draw panel and add scroll bar
        segmentPanel = new SegmentedTimeSeriesPanel(ChronoUnit.SECONDS);
        JScrollPane segmentPanelScroller = new JScrollPane(segmentPanel);
        segmentPanelScroller.getVerticalScrollBar().setUnitIncrement(10);
        segmentPanelScroller.getHorizontalScrollBar().setUnitIncrement(10);

        // Create new panel for combobox and initialize
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        // Create label for combobox in topPanel
        JLabel topPanelLabel = new JLabel("Choose Variable: ");

        // Create new combobox
        variableCB = new JComboBox<>();

        // Add action listener to combobox
        //  -> Grabs name of variable selected
        //  -> Grabs time series for segmenting variable
        //  -> Segment the segmentTimeSeries by calling segment()
        //  -> Trigger redraw in draw panel
        variableCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Grab name of variable selected
                String varName = (String) variableCB.getSelectedItem();

                // Test of empty string
                if((varName != null) && !varName.isEmpty()) {
                    // Add varName to ArrayList because PLGFileReader.readPLGFileAsTimeSeries expects Collection<String>
                    ArrayList<String> variables = new ArrayList<String>();
                    variables.add(varName);

                    // Grab output of PLGFileReader.readPLGFileAsTimeSeries and pull out TimeSeries for segmenting variable
                    HashMap<String, TimeSeries> readVarsMap = null;
                    try {
                        readVarsMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, variables);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    if (readVarsMap != null && !readVarsMap.isEmpty()) {
                        segmentTimeSeries = readVarsMap.get(varName);

                        // Debug: print number of points in time series for segmenting variable
                        System.out.println("segmentTimeSeries: " + segmentTimeSeries.getAllRecords().size());
                    }

                    // Debug: print out variable name selected
                    System.out.println(varName);

                    // Segment the time series and set it in segmentPanel
                    segmentTimeSeriesMap = segment();
                    segmentPanel.setTimeSeries(segmentTimeSeriesMap);

                }
            }
        });

        // Add components to topPanel and border around it
        topPanel.add(topPanelLabel);
        topPanel.add(variableCB);

        // Add
        SpinnerNumberModel widthModel = new SpinnerNumberModel(segmentPanel.getPlotTimeUnitWidth(), 1, 20, 1);
        JSpinner plotWidthSpinner = new JSpinner(widthModel);

        plotWidthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                segmentPanel.setPlotTimeUnitWidth(widthModel.getNumber().intValue());
            }
        });

        JLabel widthLabel = new JLabel("Plot Unit Width");
        topPanel.add(widthLabel);
        topPanel.add(plotWidthSpinner);


        SpinnerNumberModel heightModel = new SpinnerNumberModel(segmentPanel.getPlotHeight(), 20, 200, 5);
        JSpinner plotHeightSpinner = new JSpinner(heightModel);

        plotHeightSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                segmentPanel.setPlotHeight(heightModel.getNumber().intValue());
            }
        });

        JLabel heightLabel = new JLabel("Plot Unit Height");
        topPanel.add(heightLabel);
        topPanel.add(plotHeightSpinner);


        // mainPanel is a "dummy" panel of parent frame
        // use it to set layout and add top and draw panels
        JPanel mainPanel = (JPanel)frame.getContentPane();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(segmentPanelScroller, BorderLayout.CENTER);
    }

    // Segments segmentTimeSeries by buildHeightTimeSeries
    private TreeMap<Double, TimeSeries> segment() {
        TreeMap<Double, TimeSeries> temp = new TreeMap<>();
        TimeSeriesRecord lastSegmentRecord = null;

        // Get previous height record and current height record
        for (TimeSeriesRecord currentSegmentRecord : buildHeightTimeSeries.getAllRecords()) {

            // Get all of the for the segment time series between previous and current height record
            if (lastSegmentRecord != null) {
                ArrayList<TimeSeriesRecord> segmentRecordList = segmentTimeSeries.getRecordsBetween(lastSegmentRecord.instant, currentSegmentRecord.instant);

                //
                if (segmentRecordList != null && !segmentRecordList.isEmpty()) {
                    TimeSeries timeSeries = new TimeSeries(String.valueOf(lastSegmentRecord.value));
                    for (TimeSeriesRecord valueRecord : segmentRecordList) {
                        timeSeries.addRecord(valueRecord.instant, valueRecord.value, Double.NaN, Double.NaN);
                    }
                    temp.put(lastSegmentRecord.value, timeSeries);
                }
            }

            lastSegmentRecord = currentSegmentRecord;
        }

        // TODO
        // CAN TEST THE DYNAMIC TIME WARPING HERE
        // START BY COMPARING ALL TO FIRST TIME SERIES

        return temp;
    }

    public static void main (String args[]) {

        // Create instance of SegmentedSeries class
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SegmentedSeries app = new SegmentedSeries();

            }
        });
    }
}
