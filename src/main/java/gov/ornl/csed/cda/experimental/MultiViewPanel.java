package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.sql.Time;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * Created by csg on 2/3/16.
 */
public class MultiViewPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(MultiViewPanel.class);
    private Box box;

    public MultiViewPanel () {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBorder(null);
        setBackground(Color.white);
        box = new Box(BoxLayout.PAGE_AXIS);
        add(box, BorderLayout.NORTH);
    }

    public void addTimeSeries(TimeSeries timeSeries) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        TimeSeriesPanel detailTimeSeriesPanel = new TimeSeriesPanel(1, ChronoUnit.MINUTES, TimeSeriesPanel.PlotDisplayOption.LINE);
        detailTimeSeriesPanel.setTimeSeries(timeSeries);
//        timeSeriesPanel.setPreferredSize(new Dimension(100, 80));
        JScrollPane scrollPane = new JScrollPane(detailTimeSeriesPanel);
        scrollPane.setPreferredSize(new Dimension(100, 100));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.LINE);
        overviewTimeSeriesPanel.setTimeSeries(timeSeries);
        overviewTimeSeriesPanel.setBackground(Color.yellow);
//        overviewTimeSeriesPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        gbc.gridx = 2;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        panel.add(overviewTimeSeriesPanel, gbc);

        JPanel placeHolderPanel = new JPanel();
        placeHolderPanel.setBackground(Color.blue);
        gbc.gridy = 1;
        panel.add(placeHolderPanel, gbc);

        box.add(panel);
        revalidate();
    }

    public static void main (String args[]) {
        int numTimeSeries = 3;
        int numTimeSeriesRecords = 50400;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Instant startInstant = Instant.now();
        Instant endInstant = Instant.from(startInstant).plus(numTimeSeriesRecords + 50, ChronoUnit.MINUTES);

        ArrayList<TimeSeries> timeSeriesList = new ArrayList<>();
        for (int i = 0; i < numTimeSeries; i++) {
            double value = 0.;
            double uncertaintyValue = 0.;
            TimeSeries timeSeries = new TimeSeries("V"+i);
            for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
                timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
            }
            timeSeriesList.add(timeSeries);
        }

        MultiViewPanel multiViewPanel = new MultiViewPanel();
        JScrollPane scroller = new JScrollPane(multiViewPanel);



        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);

        for (TimeSeries timeSeries : timeSeriesList) {
            multiViewPanel.addTimeSeries(timeSeries);
        }
    }
}
