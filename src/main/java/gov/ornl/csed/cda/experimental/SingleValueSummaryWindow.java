/*
 *
 *  Class:  SingleValueSummaryWindow
 *
 *      Author:     whw
 *
 *      Created:    23 May 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */


package gov.ornl.csed.cda.experimental;


import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.Falcon.PLGVariableSchema;
import gov.ornl.csed.cda.Talon.TalonData;
import gov.ornl.csed.cda.timevis.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class SingleValueSummaryWindow extends JComponent {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(SingleValueSummaryWindow.class);





    // =-= INSTANCE FIELDS =-=
    private JFrame singleValueSummaryFrame = null;
    private JTextArea textArea = null;
//    private JPanel singleValueSummaryPanel = null;
    private TalonData talonData = null;
    private TreeMap<String, Double> singleValueVariableValues= null;
    private int variableNameLabelWidth = 0;
    private int valueLabelWidth = 0;
    private Font font = new Font("Menlo", Font.PLAIN, 13);





    // =-= CONSTRUCTOR =-=
    public SingleValueSummaryWindow(TreeMap<String, Double> singleValueVariableValues) {
        this.singleValueVariableValues = singleValueVariableValues;

        initialize();

        singleValueSummaryFrame.setVisible(true);
    }



    public SingleValueSummaryWindow(TalonData talonData) {
        this.talonData = talonData;
        singleValueVariableValues = talonData.getSingleValueVariableValues();

        initialize();

        singleValueSummaryFrame.setVisible(true);
    }





    // =-= CLASS METHODS =-=





    // =-= INSTANCE METHODS =-=
    private void initialize() {

        //  -> create talonFrame
        singleValueSummaryFrame = new JFrame();
        singleValueSummaryFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        singleValueSummaryFrame.setBounds(100, 75, 800, 500);

        initializeMenu();
        initializePanel();
    }



    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save...");

        file.add(save);
        menuBar.add(file);
        singleValueSummaryFrame.setJMenuBar(menuBar);

        save.addActionListener(e -> {
            writeOutSingleValueVariables();
        });
    }



    private void initializePanel() {

        layoutComponent();
        textArea.setEditable(false);

//        singleValueSummaryPanel = new JPanel();
        JScrollPane singleValueSummaryScroller = new JScrollPane(textArea);
        JPanel mainPanel = (JPanel) singleValueSummaryFrame.getContentPane();

        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(singleValueSummaryScroller, BorderLayout.CENTER);

    }



    //  -> find the max variable name string length
    //  -> find the max value string length
    //  -> make the rectangles for the variable names and values
    //  -> Determine the total drawing space and setPreferredSize
    private void layoutComponent() {
        int skip = 0;
        for (Map.Entry<String, Double> entry : singleValueVariableValues.entrySet()) {
            variableNameLabelWidth = (entry.getKey().length() > valueLabelWidth) ? entry.getKey().length() : variableNameLabelWidth;
            valueLabelWidth = (entry.getValue().toString().length() > valueLabelWidth) ? entry.getValue().toString().length() : valueLabelWidth;

//            if (entry.getValue().equals(0D)) {
//                skip++;
//            }
        }


        textArea = new JTextArea(singleValueVariableValues.size() - skip, variableNameLabelWidth + valueLabelWidth);

        for (Map.Entry<String, Double> entry : singleValueVariableValues.entrySet()) {
//            if (entry.getValue().equals(0D)) {
//                continue;
//            }

            textArea.append(entry.getKey() + ": " + entry.getValue().toString() + "\n");
        }

        textArea.setFont(font);

    }



    private void writeOutSingleValueVariables() {

        FileWriter writer = null;
//        System.
        int length = talonData.getPlgFile().getName().length();
        String filename = talonData.getPlgFile().getName().substring(0, length - 4) + ".txt";

//        System.out.println(filename);

        try {
            writer = new FileWriter(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (writer != null) {
            PrintWriter printer = new PrintWriter(writer);

            for (Map.Entry<String, Double> entry : singleValueVariableValues.entrySet()) {
                printer.printf("%s%n", entry.getKey() + ":  " + entry.getValue().toString());
            }
        }

        System.out.println("DONE");
    }






    // =-= MAIN =-=

    //  -> read in the variable schemas
    //  -> find all variable with one value and store the names in an array list
    //  -> read in the "time series" from the list of 1-value variables
    //  -> pop up new panel with all of the information
    public static void main(String args[]) {
        String plgFileName = "/Users/whw/ORNL Internship/Printer Log Files/R1140_2015-01-30_15.06.plg";
        File plgFile = new File(plgFileName);
        HashMap<String, PLGVariableSchema> variableSchemaMap = new HashMap<>();
        ArrayList<String> singleValueVariables = new ArrayList<>();
        HashMap<String, TimeSeries> singleValueVariablesSeries = new HashMap<>();
        TreeMap<String, Double> singleValueVariableValues = new TreeMap<>();


        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select .plg to Open");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
        int retVal = fileChooser.showDialog(new JFrame(), "Open File");

        //  --> sets plgFile in talonData
        if (retVal != JFileChooser.CANCEL_OPTION) {
//                log.debug("Changing PLG File");
            plgFile = fileChooser.getSelectedFile();
        }

        //  -> read in the variable schemas
        try {
            variableSchemaMap = PLGFileReader.readVariableSchemas(plgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //  -> find all variable with one value and store the names in an array list
        for (PLGVariableSchema schema : variableSchemaMap.values()) {
            if(schema.typeString.equals("Int16") ||
                schema.typeString.equals("Double") ||
                schema.typeString.equals("Single") ||
                schema.typeString.equals("Int32")) {

                String[] temp = schema.variableName.split("[.]");

                if (schema.numValues == 1 &&
                        !temp[0].equals("Themes") &&
                        !temp[0].equals("Analyse") &&
                        !temp[0].equals("Process") &&
                        !temp[0].equals("Measurements")) {
                    singleValueVariables.add(schema.variableName);
                }
            }

        }


        //  -> read in the "time series" from the list of 1-value variables
        try {
            long time = System.currentTimeMillis();

            singleValueVariablesSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, singleValueVariables);

            time = System.currentTimeMillis() - time;
            log.debug("took " + time + " ms");


        } catch (IOException e1) {
            e1.printStackTrace();
        }


        for (TimeSeries series : singleValueVariablesSeries.values()) {
            singleValueVariableValues.putIfAbsent(series.getName(), series.getMaxValue());
        }


        EventQueue.invokeLater(() -> {
            SingleValueSummaryWindow window = new SingleValueSummaryWindow(singleValueVariableValues);
        });

    }

}
