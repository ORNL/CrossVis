package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.*;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Created by csg on 5/10/16.
 */
public class OverviewDetailTimeSeriesTest {
    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    int plotUnitWidth = 10;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


                    NumericTimeSeriesPanel detailsNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                    detailsNumericTimeSeriesPanel.setBackground(Color.white);

                    NumericTimeSeriesPanel overviewNumericTimeSeriesPanel = new NumericTimeSeriesPanel(plotUnitWidth, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                    overviewNumericTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                    overviewNumericTimeSeriesPanel.setBackground(Color.white);
                    Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                    overviewNumericTimeSeriesPanel.setBorder(border);

                    JScrollPane scroller = new JScrollPane(detailsNumericTimeSeriesPanel);
                    scroller.getVerticalScrollBar().setUnitIncrement(10);
                    scroller.getHorizontalScrollBar().setUnitIncrement(10);
                    scroller.setBackground(frame.getBackground());
                    scroller.setBorder(border);

                    JToggleButton button = new JToggleButton("Show Moving Range Plots");
                    button.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                detailsNumericTimeSeriesPanel.setMovingRangeDisplayOption(NumericTimeSeriesPanel.MovingRangeDisplayOption.PLOT_VALUE);
                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                                detailsNumericTimeSeriesPanel.setMovingRangeDisplayOption(NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN);
                            }
                        }
                    });

                    ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                    ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                    ((JPanel)frame.getContentPane()).add(overviewNumericTimeSeriesPanel, BorderLayout.SOUTH);

                    frame.setSize(1000, 400);
                    frame.setVisible(true);

                    detailsNumericTimeSeriesPanel.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                        @Override
                        public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            overviewNumericTimeSeriesPanel.addTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
                        }

                        @Override
                        public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection, Instant previousStartInstant, Instant previousEndInstant) {
                            overviewNumericTimeSeriesPanel.updateTimeSeriesSelection(previousStartInstant, previousEndInstant, timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
                        }

                        @Override
                        public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            overviewNumericTimeSeriesPanel.removeTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
                        }
                    });
//                    TimeSeries timeSeries = new TimeSeries("Test");
//
//                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords+120, ChronoUnit.SECONDS);
//
//                    double value = 0.;
//
//                    for (int i = 120; i < numTimeRecords; i++) {
//                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
//                        instant = instant.plusMillis(random.nextInt(1000));
//                        value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
//                        double range = Math.abs(value) * .25;
//                        double upperRange = value + range;
//                        double lowerRange = value - range;
//                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
//                    }

//                    File csvFile = new File("/Users/csg/Desktop/D3/vast-samples/nf-test-1m.csv");
//                    Table dataTable =  new CSVTableReader().readTable(csvFile);
//
//                    String timeColumnName = "TimeSeconds<t>";
//                    int timeColumnIndex = dataTable.getColumnNumber(timeColumnName);
//
//                    String dataColumnName = "durationSeconds<n>";
//                    int dataColumnIndex = dataTable.getColumnNumber(dataColumnName);
//
//                    TimeSeries timeSeries = new TimeSeries("Test");
//
//                    System.out.println("Reading file " + csvFile.getName() + "...");
//                    for (int ituple = 0; ituple < dataTable.getTupleCount(); ituple++) {
//                        double timeSeconds = dataTable.getDouble(ituple, timeColumnIndex);
//                        long timeMillis = (long)(timeSeconds * 1000.);
//                        Instant instant = Instant.ofEpochMilli(timeMillis);
//
//                        double value = dataTable.getDouble(ituple, dataColumnIndex);
//                        if (!Double.isNaN(value)) {
//                            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//                        }
//                    }
//
//                    System.out.println("Read data for " + timeSeries.getRecordCount() + " records");
//

                    TimeSeries timeSeries = getRandomTimeSeries();
//                    TimeSeries timeSeries = getCSVTimeSeries();

//                    System.out.println("Press Enter key to continue...");
//                    System.in.read();

                    overviewNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                    detailsNumericTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());

                    scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            JScrollBar scrollBar = (JScrollBar)e.getSource();
                            double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();

                            double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                            double deltaTime = norm * Duration.between(overviewNumericTimeSeriesPanel.getStartInstant(), overviewNumericTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant startHighlightInstant = overviewNumericTimeSeriesPanel.getStartInstant().plusMillis((long)deltaTime);

                            int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                            norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                            deltaTime = norm * Duration.between(overviewNumericTimeSeriesPanel.getStartInstant(), overviewNumericTimeSeriesPanel.getEndInstant()).toMillis();
                            Instant endHighlightInstant = overviewNumericTimeSeriesPanel.getEndInstant().minusMillis((long) deltaTime);

                            overviewNumericTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                        }
                    });

                    overviewNumericTimeSeriesPanel.setPinningEnabled(false);
                    overviewNumericTimeSeriesPanel.setInteractiveSelectionEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static TimeSeries getRandomTimeSeries() {
        Random random = new Random(System.currentTimeMillis());
//        int numTimeRecords = 864000;
        int numTimeRecords = 5400;

        TimeSeries timeSeries = new TimeSeries("Test");

        Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant endInstant = Instant.from(startInstant).plus(numTimeRecords+120, ChronoUnit.SECONDS);
        Duration duration = Duration.of(1, ChronoUnit.SECONDS);
        startInstant.plus(duration);
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
