package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.NumericTimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Created by csg on 5/12/16.
 */
public class MovingRangeTest {
    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    int plotUnitWidth = 10;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setSize(1000, 400);

                    Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

                    NumericTimeSeriesPanel normalNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                    normalNumericTimeSeriesPanel.setBackground(Color.white);

                    JScrollPane normalTimeSeriesScroller = new JScrollPane(normalNumericTimeSeriesPanel);
                    normalTimeSeriesScroller.setBackground(frame.getBackground());
                    normalTimeSeriesScroller.setBorder(BorderFactory.createTitledBorder("Normal Data Value Mode"));

                    NumericTimeSeriesPanel movingRangeNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                    movingRangeNumericTimeSeriesPanel.setBackground(Color.white);
                    movingRangeNumericTimeSeriesPanel.setMovingRangeDisplayOption(NumericTimeSeriesPanel.MovingRangeDisplayOption.PLOT_VALUE);

                    JScrollPane movingRangeTimeSeriesScroller = new JScrollPane(movingRangeNumericTimeSeriesPanel);
                    movingRangeTimeSeriesScroller.setBackground(frame.getBackground());
                    movingRangeTimeSeriesScroller.getHorizontalScrollBar().setEnabled(false);
                    movingRangeTimeSeriesScroller.setBorder(BorderFactory.createTitledBorder("Moving Range Mode"));

                    normalTimeSeriesScroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            movingRangeTimeSeriesScroller.getHorizontalScrollBar().getModel().setValue(normalTimeSeriesScroller.getHorizontalScrollBar().getModel().getValue());
                        }
                    });

//                    JToggleButton button = new JToggleButton("Show Moving Range Plots");
//                    button.addItemListener(new ItemListener() {
//                        @Override
//                        public void itemStateChanged(ItemEvent e) {
//                            if (e.getStateChange() == ItemEvent.SELECTED) {
//                                detailsNumericTimeSeriesPanel.setMovingRangeModeEnabled(true);
//                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
//                                detailsNumericTimeSeriesPanel.setMovingRangeModeEnabled(false);
//                            }
//                        }
//                    });

                    JPanel mainPanel = (JPanel)frame.getContentPane();
                    mainPanel.setLayout(new GridLayout(2, 1));
                    mainPanel.add(normalTimeSeriesScroller);
                    mainPanel.add(movingRangeTimeSeriesScroller);

                    frame.setVisible(true);

                    TimeSeries timeSeries = getRandomTimeSeries();
//                    TimeSeries timeSeries = getCSVTimeSeries();

//                    System.out.println("Press Enter key to continue...");
//                    System.in.read();

                    normalNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                    movingRangeNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static TimeSeries getRandomTimeSeries() {
        Random random = new Random(System.currentTimeMillis());
        int numTimeRecords = 86400;

        TimeSeries timeSeries = new TimeSeries("Test");

        Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant endInstant = Instant.from(startInstant).plus(numTimeRecords+120, ChronoUnit.SECONDS);

        double value = 0.;

        for (int i = 120; i < numTimeRecords; i++) {
            Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
            instant = instant.plusMillis(random.nextInt(1000));
            value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
            double range = Math.abs(value) * .25;
            double upperRange = value + range;
            double lowerRange = value - range;
            timeSeries.addRecord(instant, value, upperRange, lowerRange);
        }

        return timeSeries;
    }

    public static TimeSeries getCSVTimeSeries() throws Exception {
        File csvFile = new File("/Users/csg/Desktop/D3/vast-samples/nf-test-1m.csv");
        Table dataTable =  new CSVTableReader().readTable(csvFile);

        String timeColumnName = "TimeSeconds<t>";
        int timeColumnIndex = dataTable.getColumnNumber(timeColumnName);

        String dataColumnName = "durationSeconds<n>";
        int dataColumnIndex = dataTable.getColumnNumber(dataColumnName);

        TimeSeries timeSeries = new TimeSeries("Test");

        System.out.println("Reading file " + csvFile.getName() + "...");
        for (int ituple = 0; ituple < dataTable.getTupleCount(); ituple++) {
            double timeSeconds = dataTable.getDouble(ituple, timeColumnIndex);
            long timeMillis = (long)(timeSeconds * 1000.);
            Instant instant = Instant.ofEpochMilli(timeMillis);

            double value = dataTable.getDouble(ituple, dataColumnIndex);
            if (!Double.isNaN(value)) {
                timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
            }
        }

        System.out.println("Read data for " + timeSeries.getRecordCount() + " records");

        return timeSeries;
    }
}
