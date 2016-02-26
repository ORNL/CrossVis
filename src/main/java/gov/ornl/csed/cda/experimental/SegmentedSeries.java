package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.Falcon.PLGVariableSchema;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Created by whw on 2/26/16.
 * halseywh@ornl.gov
 */


public class SegmentedSeries extends JComponent {
    // ========== CLASS FIELDS ==========


    // ========== CONSTRUCTOR ==========
    public SegmentedSeries () {

    }

    // ========== METHODS ==========
    public static void main () {

        // Create frame and panel
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create the application frame and set behaviors
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Create menu bar and add it to the frame
                JMenuBar menuBar = new JMenuBar();
                frame.setJMenuBar(menuBar);

                // Create menu option "File" and add it to the menu bar
                JMenu menu_file = new JMenu("File");
                menuBar.add(menu_file);

                // Create menu item - "File > Open PLG" and add it to File
                JMenuItem menu_file_openPLG = new JMenuItem("Open PLG");
                menu_file.add(menu_file_openPLG);

                // Add action listener to menu_file_openPLG
                menu_file_openPLG.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Create a file chooser for user navigation
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Select .plg to Open");
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("plg"));
                        int retVal = fileChooser.showDialog(frame, "Open");
                        if (retVal != JFileChooser.CANCEL_OPTION) {
                            try {

                            } catch () {

                            }

                        }
                    }
                });
            }
        });
    }
}
