package gov.ornl.csed.cda.flight;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by csg on 4/26/16.
 */
public class Flight {
    // TODO: Make TimeSeriesListener for listening to changes in the underlying data
    // TODO: Make TimeSeriesPanel listen for changes and repaint/regenerate as necessary
    // TODO: Make main loop use a timer to incrementally add new values to the time series
    // TODO: Make artistic / expressive representations
    // TODO: Implement fade algorithm in the TimeSeriesPanel
    // TODO: Output individual images for each rendering step for creating a high resolution movie
    // TODO: Instead of overview, create a detail view that scrolls, add capability to set TimeSeriesPanel
    //       to use an overview / summary rendering using a specified chronounit.  This would work like
    //       the overview but at a fixed time interval size instead of fitting the time series to the
    //       current width of the panel.

    public static final int numTimeSeriesRecords = 60*60*6;
    public static final double minValue = -10.;
    public static final double maxValue = 10.;
    public static TimeSeries timeSeries = new TimeSeries("V");
    public static final Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
    public static final Instant endInstant = startInstant.plus(numTimeSeriesRecords+1, ChronoUnit.SECONDS);

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {

                TimeSeriesPanel timeSeriesPanel = new TimeSeriesPanel(10, ChronoUnit.MINUTES, TimeSeriesPanel.PlotDisplayOption.LINE);
//                detailsTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                timeSeriesPanel.setBackground(Color.white);
//                detailsTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
                timeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
                timeSeriesPanel.setValueAxisMax(11.0);
                timeSeriesPanel.setValueAxisMin(-11.0);
                timeSeriesPanel.setDataColor(new Color(80, 80, 130, 100));

                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//                detailsTimeSeriesPanel.setBorder(border);

//                detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());

                JScrollPane scrollPane = new JScrollPane(timeSeriesPanel);

                JButton makeDataButton = new JButton("Generate Data");
                makeDataButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                generatePoints();
                            }
                        });
                        thread.start();
                    }
                });

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                ((JPanel)frame.getContentPane()).add(scrollPane, BorderLayout.CENTER);
                ((JPanel)frame.getContentPane()).add(makeDataButton, BorderLayout.NORTH);
                frame.setSize(1000, 300);
                frame.setVisible(true);


//                TimeSeries timeSeries = new TimeSeries("V");


//                double value = 0.;
//                for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
//                    Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.SECONDS);
//                    value = Math.max(minValue, Math.min(maxValue, value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
//                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
    }

    public static void generatePoints() {
        double value = 0.;
        for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
            Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.SECONDS);
            value = Math.max(minValue, Math.min(maxValue, value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
