/*
 *
 *  Class:  Talon
 *
 *      Author:     whw
 *
 *      Created:    26 Feb 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  N/A
 *
 *  Interfaces:     TalonDataListener
 *
 */

package gov.ornl.csed.cda.Talon;


import gov.ornl.csed.cda.experimental.SingleValueSummaryWindow;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.geometry.Orientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;


public class Talon implements TalonDataListener {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(Talon.class);





    // =-= INSTANCE FIELDS =-=
    private String talonVersionString = "Talon v0.2.2";
    private JFrame talonFrame = null;                                           // Frame for Talon instance

    // talonData class for the viz
    private TalonData talonData = null;

    // child panels
    private JPanel settingsPanel = null;                                        // Panel for combobox and radio buttons
    private SegmentedTimeSeriesPanel segmentedTimeSeriesPanel = null;           // Panel for segmented series plot
    private MultiImagePanel imagePanel = null;                                  // Panel for displaying build height images
    private DistanceIndicatorPanel distanceIndicatorPanel = null;               // Panel for distance indicator tick marks
    private TimeSeriesPanel timeSeriesOverviewPanel = null;

    // helper variables
    private JComboBox<String> segmentedVariableComboBox = null;                 // Combobox of variable names from plgFile in settingsPanel
    private int segmentedTimeSeriesPanel_imagePanel_sync = 0;                   // Flag that prevents circular references for scrollActionListeners
    private int segmentedTimeSeriesPanel_timeSeriesOverview_sync = 0;
    private JSpinner referenceValueSpinner;                                     // Spinner to choose build height values
    private boolean spinnerRespond = true;





    // =-= CONSTRUCTOR =-=
    //  -> initialize Talon
    //  -> add TalonDataListener
    //  -> make the frame visible
    public Talon() {

        //  -> initialize Talon
        initialize();

        //  -> add TalonDataListener
        talonData.addTalonDataListener(this);

        //  -> make the frame visible
        talonFrame.setVisible(true);

    }





    // =-= INSTANCE METHODS =-=

    //  -> create talonFrame
    //  -> call helper functions to initialize data, menu, and panels
    private void initialize () {

        //  -> create talonFrame
        talonFrame = new JFrame();
        talonFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        talonFrame.setBounds(50, 50, 1400, 700);
        talonFrame.setTitle(talonVersionString);

        //  -> call helper functions to initialize data, menu, and panels
        initializeData();
        initializeMenu();
        initializePanel();

    }



    //  -> create the TalonData instance
    private void initializeData() {
        talonData = new TalonData(ChronoUnit.SECONDS);
    }



    //  -> creates menubar and menuitems
    //  -> add menubar and menuitems to the frame
    //  -> add the actionListeners to the menuitmes
    private void initializeMenu() {

        //  -> creates menubar and menuitems
        JMenuBar talonMenuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem openPLG = new JMenuItem("Open PLG...", KeyEvent.VK_O); // This menu item will respond to the 'o' key being pressed
        JMenuItem openImages = new JMenuItem("Open Images...");
        JMenuItem showSummary = new JMenuItem("Show Summary");
        JMenuItem exit = new JMenuItem("Exit");


        //  -> add menubar and menuitems to the frame
        talonFrame.setJMenuBar(talonMenuBar);
        talonMenuBar.add(file);
        file.add(openPLG);
        file.add(openImages);
        file.addSeparator();
        file.add(showSummary);
        file.addSeparator();
        file.add(exit);


        //  -> add the actionListeners to the menuitmes

        //  --> creates file chooser
        //  --> sets plgFile in talonData
        openPLG.addActionListener(e -> {

            //  --> Creates file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select .plg to Open");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
            int retVal = fileChooser.showDialog(talonFrame, "Open File");

            //  --> sets plgFile in talonData
            if (retVal != JFileChooser.CANCEL_OPTION) {
//                log.debug("Changing PLG File");
                talonData.setPlgFile(fileChooser.getSelectedFile());
            }
        });


        //  --> creates file chooser
        //  --> sets imageDirectory in talonData
        openImages.addActionListener(e -> {

            //  --> creates file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image Directory to Open");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            int retVal = fileChooser.showDialog(talonFrame, "Open File");

            //  --> sets imageDirectory in talonData
            if (retVal != JFileChooser.CANCEL_OPTION) {
//                log.debug("Changing Image Directory");
                talonData.setImageDirectory(fileChooser.getSelectedFile());
            }
        });


        showSummary.addActionListener(e -> {
            new SingleValueSummaryWindow(talonData);
        });


        exit.addActionListener(e -> {
            talonFrame.dispatchEvent(new WindowEvent(talonFrame, Event.WINDOW_DESTROY));
        });

    }



    //  -> initialize all the child panels
    //  -> add scrollbars/other panel parameters
    //  -> initialize elements for the settings panel
    //  -> populate the settings panel
    //  -> add actionListeners to settings panel elements
    //  -> synchronize scroll bars between segmentedTimeSeriesPanel and imagePanel
    //  -> populate application frame with child panels
    private void initializePanel() {

        //  -> initialize all the child panels
        settingsPanel = new JPanel();
        segmentedTimeSeriesPanel = new SegmentedTimeSeriesPanel(talonData);
        distanceIndicatorPanel = new DistanceIndicatorPanel(talonData);
        imagePanel = new MultiImagePanel(Orientation.VERTICAL, talonData);
        timeSeriesOverviewPanel = new TimeSeriesPanel(2, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);


        //  -> add scrollbars/other panel settings
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.LINE_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane segmentmentedTimeSeriesPanelScroller = new JScrollPane(segmentedTimeSeriesPanel);
        segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().setUnitIncrement(10);
        segmentmentedTimeSeriesPanelScroller.getHorizontalScrollBar().setUnitIncrement(10);

        imagePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JScrollPane imagePanelScroller = new JScrollPane(imagePanel);
        imagePanelScroller.getVerticalScrollBar().setUnitIncrement(10);

        JScrollPane timeSeriesOverview = new JScrollPane(timeSeriesOverviewPanel);
        timeSeriesOverview.getHorizontalScrollBar().setUnitIncrement(10);

        distanceIndicatorPanel.setPreferredSize(new Dimension(30, segmentmentedTimeSeriesPanelScroller.getHeight()));


        //  -> initialize elements for the settings panel
        JLabel segmentedVariableComboBoxLabel = new JLabel("Choose Variable: ");
        segmentedVariableComboBox = new JComboBox<>();

        JLabel plotWidthLabel = new JLabel("Plot Unit Width");
        SpinnerNumberModel plotWidthModel = new SpinnerNumberModel(segmentedTimeSeriesPanel.getPlotTimeUnitWidth(), 1, 20, 1);
        JSpinner plotWidthSpinner = new JSpinner(plotWidthModel);

        JLabel heightLabel = new JLabel("Plot Unit Height");
        SpinnerNumberModel heightModel = new SpinnerNumberModel(segmentedTimeSeriesPanel.getPlotHeight(), 20, 200, 5);
        JSpinner plotHeightSpinner = new JSpinner(heightModel);

        JLabel referenceLabel = new JLabel("Reference Height");
        SpinnerNumberModel referenceModel = new SpinnerNumberModel(0.1, 0.01, 1000, 0.01);
        referenceValueSpinner = new JSpinner(referenceModel);


        //  -> populate the settings panel
        settingsPanel.add(segmentedVariableComboBoxLabel);
        settingsPanel.add(segmentedVariableComboBox);

        settingsPanel.add(plotWidthLabel);
        settingsPanel.add(plotWidthSpinner);

        settingsPanel.add(heightLabel);
        settingsPanel.add(plotHeightSpinner);

        settingsPanel.add(referenceLabel);
        settingsPanel.add(referenceValueSpinner);


        //  -> add actionListeners to settings panel elements

        //  --> set segmented variable name in talonData
        segmentedVariableComboBox.addActionListener(e -> {
//            log.debug("Changing Segmented Variable");
            if (segmentedVariableComboBox.getSelectedItem() != null) {
                talonData.setSegmentedVariableName((String) segmentedVariableComboBox.getSelectedItem());
            }
        });


        //  --> set plot time unit width in segmentedTimeSeriesPanel
        plotWidthSpinner.addChangeListener(e -> {
            segmentedTimeSeriesPanel.setPlotTimeUnitWidth(plotWidthModel.getNumber().intValue());
            timeSeriesOverviewPanel.setPlotUnitWidth(plotWidthModel.getNumber().intValue());
        });


        //  --> set plot time unit height in segmentedTimeSeriesPanel
        plotHeightSpinner.addChangeListener(e -> {
            segmentedTimeSeriesPanel.setPlotHeight(heightModel.getNumber().intValue());
            timeSeriesOverviewPanel.setTimeBarHeight(heightModel.getNumber().intValue());
        });


        //  --> set reference value in talonData
        referenceValueSpinner.addChangeListener(e -> {
//            log.debug("Changing Reference Value");
            if (spinnerRespond) {
                talonData.setReferenceValue((double) referenceValueSpinner.getValue());
            }
        });


        //  -> synchronize scroll bars between segmentedTimeSeriesPanel and imagePanel
        imagePanelScroller.getVerticalScrollBar().addAdjustmentListener(e -> {

            if(segmentedTimeSeriesPanel_imagePanel_sync == 0) {
                segmentedTimeSeriesPanel_imagePanel_sync = 1;


                int imageScrollBarMin = imagePanelScroller.getVerticalScrollBar().getModel().getMinimum();
                int imageScrollBarMax = imagePanelScroller.getVerticalScrollBar().getModel().getMaximum();
                int imageScrollBarHeight = imagePanelScroller.getVerticalScrollBar().getModel().getExtent();
                int imageScrollBarRange = imageScrollBarMax - imageScrollBarHeight;

                int segmentScrollBarMin = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMinimum();
                int segmentScrollBarMax = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMaximum();
                int segmentScrollBarHeight = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getExtent();
                int segmentScrollBarRange = segmentScrollBarMax - segmentScrollBarHeight;

                int currentValue = imagePanelScroller.getVerticalScrollBar().getModel().getValue();

                segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().setValue((int) GraphicsUtil.mapValue(currentValue, imageScrollBarMin, imageScrollBarRange, segmentScrollBarMin, segmentScrollBarRange));
            }
            segmentedTimeSeriesPanel_imagePanel_sync = 0;
        });

        segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().addAdjustmentListener(e -> {

            if (segmentedTimeSeriesPanel_imagePanel_sync == 0) {
                segmentedTimeSeriesPanel_imagePanel_sync = 1;

                int imageScrollBarMin = imagePanelScroller.getVerticalScrollBar().getModel().getMinimum();
                int imageScrollBarMax = imagePanelScroller.getVerticalScrollBar().getModel().getMaximum();
                int imageScrollBarHeight = imagePanelScroller.getVerticalScrollBar().getModel().getExtent();
                int imageScrollBarRange = imageScrollBarMax - imageScrollBarHeight;

                int segmentScrollBarMin = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMinimum();
                int segmentScrollBarMax = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMaximum();
                int segmentScrollBarHeight = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getExtent();
                int segmentScrollBarRange = segmentScrollBarMax - segmentScrollBarHeight;

                int currentValue = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getValue();

                imagePanelScroller.getVerticalScrollBar().getModel().setValue((int) GraphicsUtil.mapValue(currentValue, segmentScrollBarMin, segmentScrollBarRange, imageScrollBarMin, imageScrollBarRange));
            }

            segmentedTimeSeriesPanel_imagePanel_sync = 0;


            int overviewScrollBarMin = timeSeriesOverview.getHorizontalScrollBar().getModel().getMinimum();
            int overviewScrollBarMax = timeSeriesOverview.getHorizontalScrollBar().getModel().getMaximum();
            int overviewScrollBarHeight = timeSeriesOverview.getHorizontalScrollBar().getModel().getExtent();
            int overviewScrollBarRange = overviewScrollBarMax - overviewScrollBarHeight;

            int segmentScrollBarMin = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMinimum();
            int segmentScrollBarMax = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getMaximum();
            int segmentScrollBarHeight = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getExtent();
            int segmentScrollBarRange = segmentScrollBarMax - segmentScrollBarHeight;

            int currentValue = segmentmentedTimeSeriesPanelScroller.getVerticalScrollBar().getModel().getValue();

            timeSeriesOverview.getHorizontalScrollBar().getModel().setValue((int) GraphicsUtil.mapValue(segmentScrollBarMax - currentValue, segmentScrollBarMin, segmentScrollBarRange, overviewScrollBarMin, overviewScrollBarRange));
        });


        //  -> populate application frame with child panels
        JPanel mainPanel = (JPanel) talonFrame.getContentPane();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(settingsPanel, BorderLayout.NORTH);
        mainPanel.add(timeSeriesOverview, BorderLayout.SOUTH);

        JPanel leftPanel = new JPanel(new BorderLayout());  // combine segmentedTimeSeriesPanel and distanceIndicatorPanel into one (leftPanel)
        leftPanel.add(segmentmentedTimeSeriesPanelScroller, BorderLayout.CENTER);
        leftPanel.add(distanceIndicatorPanel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, imagePanelScroller);  // add leftPanel and imagePanelScroller (right panel) to a split pane
        splitPane.setDividerLocation(600);
        splitPane.setOneTouchExpandable(true);

        mainPanel.add(splitPane, BorderLayout.CENTER);

    }



    // TalonDataListener methods

    //  -> clear the segmentedVariableComboBox
    //  -> add new list of variables
    //  -> reset the frame title
    @Override
    public void TalonDataPlgFileChange() {
//        log.debug("PLG File Change");

        //  -> clear the segmentedVariableComboBox
        segmentedVariableComboBox.removeAllItems();

        //  -> add new list of variables
        for (String str : talonData.getListOfVariables()) {
            segmentedVariableComboBox.addItem(str);
        }

        //  -> reset the frame title
        talonFrame.setTitle(talonVersionString + " - " + talonData.getPlgFile().getName());
    }

    //  -> n/a
    @Override
    public void TalonDataSegmentingVariableChange() {
//        log.debug("Segmenting Variable Change");
    }

    //  -> remove current time series from the timeSeriesOverviewPanel
    //  -> add the new time series
    @Override
    public void TalonDataSegmentedVariableChange() {
//        log.debug("Segmented Variable Change");

        //  -> remove current time series from the timeSeriesOverviewPanel
        timeSeriesOverviewPanel.removeTimeSeries();

        //  -> add the new time series
        if(talonData.getSegmentedVariableTimeSeries() != null) {
            Instant startInstant = talonData.getSegmentedVariableTimeSeries().getStartInstant();
            Instant endInstant = talonData.getSegmentedVariableTimeSeries().getEndInstant();

            TimeSeries timeSeries = talonData.getSegmentedVariableTimeSeries();
            timeSeriesOverviewPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
            timeSeriesOverviewPanel.setPreferredSize(new Dimension(talonFrame.getWidth(), 100));

        }

    }

    //  -> change the reference value of the referenceValueSpinner to reflect changes
    @Override
    public void TalonDataReferenceValueChange() {

//        log.debug("Reference Value Change");

        if(((Double) referenceValueSpinner.getValue()).compareTo(talonData.getReferenceValue()) != 0) {
            spinnerRespond = false;
            referenceValueSpinner.setValue(talonData.getReferenceValue());
            spinnerRespond = true;
        }

    }

    //  -> n/a
    @Override
    public void TalonDataImageDirectoryChange() {
//        log.debug("Image Directory Change");
    }





    // =-= MAIN =-=
    public static void main (String args[]) {

        // Create instance of Talon class
        EventQueue.invokeLater(() -> {
            Talon app = new Talon();
        });

    }
}
