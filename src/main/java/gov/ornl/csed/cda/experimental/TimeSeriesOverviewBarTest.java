package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.overviewbar.ScrollBarOverviewPanel;
import gov.ornl.csed.cda.timevis.NumericTimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by csg on 4/22/16.
 */
public class TimeSeriesOverviewBarTest {
    private final static Logger log = LoggerFactory.getLogger(TimeSeriesOverviewBarTest.class);

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                int numTimeSeriesRecords = 60*2;
                double minValue = -10.;
                double maxValue = 10.;
                double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;

                Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);

                TimeSeries timeSeries = new TimeSeries("V");
                for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                    Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                    double value = minValue + (itime * valueIncrement);
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                    log.debug("instant: " + instant.toString());
                }
                log.debug("timeseries.getEndInstant(): " + timeSeries.getEndInstant().toString());

                NumericTimeSeriesPanel numericTimeSeriesPanel = new NumericTimeSeriesPanel(10, ChronoUnit.MINUTES, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                numericTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                numericTimeSeriesPanel.setBackground(Color.white);
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                numericTimeSeriesPanel.setBorder(border);

                numericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant().minus(1, ChronoUnit.MINUTES), timeSeries.getEndInstant().plus(1, ChronoUnit.MINUTES));

                JScrollPane scrollPane = new JScrollPane(numericTimeSeriesPanel);

                ScrollBarOverviewPanel overviewBar = new ScrollBarOverviewPanel(scrollPane.getHorizontalScrollBar());
                overviewBar.setBackground(Color.white);
//                overviewBar.addOverviewBarListener(new OverviewBarListener() {
//                    @Override
//                    public void overviewBarMarkerClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
//
//                    }
//
//                    @Override
//                    public void overviewBarMarkerDoubleClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
//
//                    }
//
//                    @Override
//                    public void overviewBarMarkerControlClicked(OverviewBar overviewBar, OverviewBarMarker marker) {
//
//                    }
//                });

                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(scrollPane, BorderLayout.CENTER);
                panel.add(overviewBar, BorderLayout.SOUTH);

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                ((JPanel)frame.getContentPane()).add(panel, BorderLayout.CENTER);
                frame.setSize(1000, 300);
                frame.setVisible(true);

            }
        });
    }
}
