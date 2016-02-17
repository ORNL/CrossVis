package gov.ornl.csed.cda.timevis;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by csg on 2/3/16.
 */
public class OverviewTimeSeriesTest {

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                int numTimeSeriesRecords = 60*12;
                double minValue = -10.;
                double maxValue = 10.;
                double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;

                Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);

                TimeSeries timeSeries = new TimeSeries("V");
                for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                    Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
//                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
                    double value = minValue + (itime * valueIncrement);
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                }

                TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(10, TimeSeriesPanel.PlotDisplayOption.LINE);
                overviewTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                overviewTimeSeriesPanel.setBackground(Color.white);
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                overviewTimeSeriesPanel.setBorder(border);

                overviewTimeSeriesPanel.setTimeSeries(timeSeries);

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                ((JPanel)frame.getContentPane()).add(overviewTimeSeriesPanel, BorderLayout.CENTER);
                frame.setSize(1000, 300);
                frame.setVisible(true);
            }
        });
    }
}
