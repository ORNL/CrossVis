package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.Falcon.PLGVariableSchema;
import gov.ornl.csed.cda.timevis.TimeSeries;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * Created by whw on 2/26/16.
 * halseywh@ornl.gov
 *
 * TODO
 *  look at SegmentedTimeSeries line 235 for how to segment the time series
 */


public class SegmentedSeries  {
    // ========== CLASS FIELDS ==========
    private JFrame frame;
    private JPanel panel;
    private File plgFile;
    private HashMap<String, PLGVariableSchema> variableSchemaMap;
    private JComboBox<String> variableCB;
    private String buildHeightVarName = "Builds.State.CurrentBuild.CurrentHeight";
    private TimeSeries buildHeightTimeSeries;

    // ========== CONSTRUCTOR ==========
    public SegmentedSeries () {
        initialize();
        frame.setVisible(true);
    }

    // ========== METHODS ==========
    private void initialize () {
        frame = new JFrame();
        frame.setTitle("title");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(50, 50, 1400, 700);

        initializePanel();
        initializeMenu();
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu file = new JMenu("File");
        menuBar.add(file);

        JMenuItem file_open = new JMenuItem("Open", KeyEvent.VK_O);
        file.add(file_open);

        file_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select .plg to Open");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
                int retVal = fileChooser.showDialog(frame, "Open");
                if (retVal != JFileChooser.CANCEL_OPTION) {
                    HashMap<String, PLGVariableSchema> variableSchemaMap = null;
                    try {

                        variableSchemaMap = PLGFileReader.readVariableSchemas(fileChooser.getSelectedFile());
                        variableCB.removeAllItems();

                        ArrayList<String> copyVarNames = new ArrayList<>();
                        for (PLGVariableSchema variableSchema : variableSchemaMap.values()) {
                            if (variableSchema.typeString.equals("Int16") ||
                                    variableSchema.typeString.equals("Double") ||
                                    variableSchema.typeString.equals("Single") ||
                                    variableSchema.typeString.equals("Int32")) {

                                if (variableSchema.numValues > 1) {
                                    copyVarNames.add(variableSchema.variableName);
                                }
                            }
                        }

                        plgFile = fileChooser.getSelectedFile();

                        Collections.sort(copyVarNames);
                        variableCB.addItem("");
                        for (String str : copyVarNames) {
                            variableCB.addItem(str);
                        }

                        ArrayList<String> variables = new ArrayList<String>();
                        variables.add(buildHeightVarName);

                        HashMap<String, TimeSeries> readVarsMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, variables);
                        if (readVarsMap != null && !readVarsMap.isEmpty()) {
                            buildHeightTimeSeries = readVarsMap.get(buildHeightVarName);
                            System.out.println("buildHeightTimeSeries: " + buildHeightTimeSeries.getAllRecords().size());
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return;
                    }
                }
            }
        });
    }

    private void initializePanel() {
        /* TODO
            main segmented view panel
                custom
                i will/make/paint this myself
        */
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        JLabel topPanelLabel = new JLabel("Choose Variable: ");
        variableCB = new JComboBox<>();
        variableCB.addItem("");

        variableCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String varName = (String) variableCB.getSelectedItem();
                System.out.println(varName);
            }
        });

        topPanel.add(topPanelLabel);
        topPanel.add(variableCB);
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel visPanel = new JPanel();
        visPanel.setBackground(Color.ORANGE);

        JPanel mainPanel = (JPanel)frame.getContentPane();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(visPanel, BorderLayout.CENTER);
    }

    public static void main (String args[]) {

        // Create frame and panel
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SegmentedSeries app = new SegmentedSeries();

//                // Create the application frame and set behaviors
//                JFrame frame = new JFrame();
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//                // Create menu bar and add it to the frame
//                JMenuBar menuBar = new JMenuBar();
//                frame.setJMenuBar(menuBar);
//
//                // Create menu option "File" and add it to the menu bar
//                JMenu menu_file = new JMenu("File");
//                menuBar.add(menu_file);
//
//                // Create menu item - "File > Open PLG" and add it to File
//                JMenuItem menu_file_openPLG = new JMenuItem("Open PLG");
//                menu_file.add(menu_file_openPLG);
//
//                // Add action listener to menu_file_openPLG
//                menu_file_openPLG.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        // Create a file chooser for user navigation
//                        JFileChooser fileChooser = new JFileChooser();
//                        fileChooser.setDialogTitle("Select .plg to Open");
//                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                        fileChooser.setMultiSelectionEnabled(false);
//                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
//                        int retVal = fileChooser.showDialog(frame, "Open");
//                        if (retVal != JFileChooser.CANCEL_OPTION) {
//                            HashMap<String, PLGVariableSchema> variableSchemaMap = null;
//                            try {
//                                variableSchemaMap = PLGFileReader.readVariableSchemas(fileChooser.getSelectedFile());
//                            } catch (IOException e1) {
//                                e1.printStackTrace();
//                                return;
//                            }
//
//
//                        }
//                    }
//                });
//
//                frame.setSize(new Dimension(1000,300));
//                frame.setVisible(true);
            }
        });
    }
}
