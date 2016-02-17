package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.Falcon.MultiViewPanel;
import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.timevis.TimeSeries;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csg on 2/17/16.
 */
public class MultiViewPLGTest {
    public static void main (String args[]) throws IOException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        File plgFile = new File(args[0]);
        ArrayList<String> variableNames = new ArrayList<>();
        variableNames.add("OPC.PowerSupply.Beam.BeamCurrent");
        variableNames.add("OPC.PowerSupply.SmokeDetector.Counts");
        HashMap<String, TimeSeries> varTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, variableNames);


        MultiViewPanel multiViewPanel = new MultiViewPanel(100);
        JScrollPane scroller = new JScrollPane(multiViewPanel);

//        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        ((JPanel)frame.getContentPane()).add(multiViewPanel, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);

        for (TimeSeries timeSeries : varTimeSeriesMap.values()) {
            multiViewPanel.addTimeSeries(timeSeries);
        }
    }
}
