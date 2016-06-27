package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.NumericTimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Created by csg on 6/17/16.
 */
public class BooleanTimeSeriesTest {
    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                int plotUnitWidth = 4;
                NumericTimeSeriesPanel detailsNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                detailsNumericTimeSeriesPanel.setBackground(Color.white);
                detailsNumericTimeSeriesPanel.setMovingRangeDisplayOption(NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN);

                JScrollPane scroller = new JScrollPane(detailsNumericTimeSeriesPanel);
                scroller.getVerticalScrollBar().setUnitIncrement(10);
                scroller.getHorizontalScrollBar().setUnitIncrement(10);
                scroller.setBackground(frame.getBackground());
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                scroller.setBorder(border);

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);

                frame.setSize(1000, 400);
                frame.setVisible(true);

                TimeSeries timeSeries = getRandomBooleanTimeSeries();
                detailsNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
            }
        });
    }

    public static TimeSeries getRandomBooleanTimeSeries() {
        Random random = new Random(System.currentTimeMillis());

        int numTimeRecords = 5400;
        TimeSeries timeSeries = new TimeSeries("Boolean Test");

        Instant start = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant end = Instant.from(start).plus(numTimeRecords+120, ChronoUnit.SECONDS);

        for (int i = 120; i < numTimeRecords; i++) {
            Instant instant = Instant.from(start).plus(i, ChronoUnit.SECONDS);
//            instant = instant.plusMillis(random.nextInt(1000));
            boolean value = random.nextBoolean();
            TimeSeriesRecord record = new TimeSeriesRecord();
            record.instant = instant;
            record.boolValue = value;
            timeSeries.addRecord(record);
        }

        return timeSeries;
    }
}
