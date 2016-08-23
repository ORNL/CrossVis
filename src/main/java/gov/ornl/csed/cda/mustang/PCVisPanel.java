package gov.ornl.csed.cda.mustang;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.IOUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.io.File;

/**
 * Created by csg on 8/12/16.
 */
public class PCVisPanel extends JComponent {
    final Logger logger = LogManager.getLogManager().getLogger(PCVisPanel.class.getName());

    private DataModel dataModel;

    public PCVisPanel() {
    }

    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private void layoutPanel() {

    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    public static void main(String args[]) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DataModel dataModel = new DataModel();
                File file = new File("data/csv/cars.csv");
                try {
                    IOUtilities.readCSV(file, dataModel);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
//                DataModel dataModel = new DataModel();
//                File file = new File("data/csv/cars.csv");
//                IOUtilities.readCSV(file, dataModel);
//
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                PCVisPanel pcVisPanel = new PCVisPanel();
                pcVisPanel.setBackground(Color.white);
                pcVisPanel.setDataModel(dataModel);

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(pcVisPanel, BorderLayout.CENTER);

                frame.setSize(1400, 800);
                frame.setVisible(true);
            }
        });
    }

}
