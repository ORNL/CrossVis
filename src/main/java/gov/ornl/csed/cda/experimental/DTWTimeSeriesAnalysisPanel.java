package gov.ornl.csed.cda.experimental;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;
import gov.ornl.csed.cda.timevis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by csg on 5/3/16.
 */
public class DTWTimeSeriesAnalysisPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(DTWTimeSeriesAnalysisPanel.class);

    private TimeSeries timeSeries;
    private TimeSeriesSelection referenceTimeSeriesSelection;

    private NumericTimeSeriesPanel referenceNumericTimeSeriesPanel;
    private Box segmentTimeSeriesBox;

//    private ArrayList<SegmentRecord> segmentRecords = new ArrayList<>();
    private TreeMap<Double, SegmentRecord> segmentDistantMap = new TreeMap<>();

    public DTWTimeSeriesAnalysisPanel() {
        initialize();
    }

    private void initialize() {
        referenceNumericTimeSeriesPanel = new NumericTimeSeriesPanel(10, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);

//        JScrollPane selectionScroller = new JScrollPane(selectionTimeSeriesPanel);

        segmentTimeSeriesBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane segmentBoxScrollPane = new JScrollPane(segmentTimeSeriesBox);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, referenceNumericTimeSeriesPanel, segmentBoxScrollPane);
        splitPane.setDividerLocation(100);
        splitPane.setOneTouchExpandable(true);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    public void setTimeSeriesSelection (TimeSeries timeSeries, TimeSeriesSelection timeSeriesSelection) {
        this.timeSeries = timeSeries;
        this.referenceTimeSeriesSelection = timeSeriesSelection;

        processSegments();
    }

    private boolean instantIsBetween (Instant instant, Instant startInstant, Instant endInstant) {
        if (instant.isBefore(startInstant) || instant.isAfter(endInstant)) {
            return false;
        }

        return true;
    }

    private void processSegments() {
        segmentTimeSeriesBox.removeAll();

        // extract the example selection from the full time series and add to selectionTimeSeriesPanel
        ArrayList<TimeSeriesRecord> selectionRecords = timeSeries.getRecordsBetween(referenceTimeSeriesSelection.getStartInstant(),
                referenceTimeSeriesSelection.getEndInstant());
        TimeSeries referenceTimeSeries = new TimeSeries("Reference");
        for (TimeSeriesRecord record : selectionRecords) {
            referenceTimeSeries.addRecord(record.instant, record.value, Double.NaN, Double.NaN);
        }
        referenceNumericTimeSeriesPanel.setTimeSeries(referenceTimeSeries, referenceTimeSeries.getStartInstant(), referenceTimeSeries.getEndInstant());
        referenceNumericTimeSeriesPanel.setValueAxisMax(timeSeries.getMaxValue());
        referenceNumericTimeSeriesPanel.setValueAxisMin(timeSeries.getMinValue());
        referenceNumericTimeSeriesPanel.setShowTimeRangeLabels(false);
        referenceNumericTimeSeriesPanel.setBackground(Color.white);

        // slide the selection range from the start to the end of the full time series
        // calculate the DTW distance metric at each step and store segment and results
        // make a new TimeSeries and NumericTimeSeriesPanel for each segment and add to a scrollable panel
        Duration selectionDuration = Duration.between(referenceTimeSeriesSelection.getStartInstant(),
                referenceTimeSeriesSelection.getEndInstant());

        Instant currentSegmentStartInstant = timeSeries.getStartInstant();
        Instant currentSegmentEndInstant = currentSegmentStartInstant.plus(selectionDuration);
        while (!currentSegmentEndInstant.isAfter(timeSeries.getEndInstant())) {
            if ((instantIsBetween(currentSegmentEndInstant, referenceTimeSeriesSelection.getStartInstant(),
                    referenceTimeSeriesSelection.getEndInstant())) ||
                    (instantIsBetween(currentSegmentStartInstant, referenceTimeSeriesSelection.getStartInstant(),
                    referenceTimeSeriesSelection.getEndInstant()))) {
                // don't compare reference segment with itself

                currentSegmentStartInstant = currentSegmentStartInstant.plus(1, ChronoUnit.SECONDS);
                currentSegmentEndInstant = currentSegmentStartInstant.plus(selectionDuration);
                continue;
            }

            ArrayList<TimeSeriesRecord> segmentRecords = timeSeries.getRecordsBetween(currentSegmentStartInstant,
                    currentSegmentEndInstant);
            TimeSeries segmentTimeSeries = new TimeSeries("Segment");
            for (TimeSeriesRecord record : segmentRecords) {
                segmentTimeSeries.addRecord(record.instant, record.value, Double.NaN, Double.NaN);
            }

            double dtwDistance = computeDTWDistance(referenceTimeSeries, segmentTimeSeries);
            log.debug("distance: " + dtwDistance);

            SegmentRecord segmentRecord = new SegmentRecord();
            segmentRecord.distance = dtwDistance;
            segmentRecord.segmentStartInstant = currentSegmentStartInstant;
            segmentRecord.segmentEndInstant = currentSegmentEndInstant;
            segmentRecord.segmentTimeSeries = segmentTimeSeries;

            JPanel segmentPanel = new JPanel();
            segmentPanel.setLayout(new BorderLayout());

            JLabel distantLabel = new JLabel(String.valueOf(dtwDistance));
            segmentPanel.add(distantLabel, BorderLayout.NORTH);

            segmentRecord.segmentNumericTimeSeriesPanel = new NumericTimeSeriesPanel(10, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
            segmentRecord.segmentNumericTimeSeriesPanel.setPreferredSize(new Dimension(200, 100));
            segmentPanel.add(segmentRecord.segmentNumericTimeSeriesPanel, BorderLayout.CENTER);
            segmentRecord.segmentPanel = segmentPanel;
//            segmentTimeSeriesBox.add(segmentPanel);

            segmentRecord.segmentNumericTimeSeriesPanel.setTimeSeries(segmentTimeSeries, segmentTimeSeries.getStartInstant(), segmentTimeSeries.getEndInstant());
            segmentRecord.segmentNumericTimeSeriesPanel.setValueAxisMax(timeSeries.getMaxValue());
            segmentRecord.segmentNumericTimeSeriesPanel.setValueAxisMin(timeSeries.getMinValue());
            segmentRecord.segmentNumericTimeSeriesPanel.setShowTimeRangeLabels(false);
            segmentRecord.segmentNumericTimeSeriesPanel.setBackground(Color.white);
//            revalidate();

            segmentDistantMap.put(segmentRecord.distance, segmentRecord);

            currentSegmentStartInstant = currentSegmentStartInstant.plus(1, ChronoUnit.SECONDS);
            currentSegmentEndInstant = currentSegmentStartInstant.plus(selectionDuration);
        }

        for (SegmentRecord record : segmentDistantMap.values()) {
            segmentTimeSeriesBox.add(record.segmentPanel);
        }

        revalidate();
    }

    private double computeDTWDistance (TimeSeries ts1, TimeSeries ts2) {
        // prep first time series
        TimeSeriesBase.Builder ts1ReferenceBuilder = TimeSeriesBase.builder();
        ArrayList<TimeSeriesRecord> ts1Records = ts1.getAllRecords();
        for (TimeSeriesRecord record : ts1Records) {
            ts1ReferenceBuilder.add((double)record.instant.toEpochMilli(), record.value);
        }
        com.fastdtw.timeseries.TimeSeries dtwTS1 = ts1ReferenceBuilder.build();

        // prep second time series
        TimeSeriesBase.Builder ts2ReferenceBuilder = TimeSeriesBase.builder();
        ArrayList<TimeSeriesRecord> ts2Records = ts2.getAllRecords();
        for (TimeSeriesRecord record : ts2Records) {
            ts2ReferenceBuilder.add((double)record.instant.toEpochMilli(), record.value);
        }
        com.fastdtw.timeseries.TimeSeries dtwTS2 = ts2ReferenceBuilder.build();

        if ( !((dtwTS1.size() == 1) && (dtwTS2.size() == 1)) ) {
            double distance = FastDTW.compare(dtwTS1, dtwTS2, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
            return distance;
        }

        return Double.NaN;
    }

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Random random = new Random(System.currentTimeMillis());

//                    int numTimeRecords = 50400;
                    int numTimeRecords = 3600 * 4;
//                    int numTimeRecords = 86400/8;
//                    int numTimeRecords = 1200;
                    int plotUnitWidth = 10;
                    JFrame frame = new JFrame();
                    frame.setTitle("Time Series Segment Comparison Tool");
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

                    ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                    ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                    ((JPanel)frame.getContentPane()).add(overviewNumericTimeSeriesPanel, BorderLayout.SOUTH);

                    frame.setSize(1000, 400);
                    frame.setVisible(true);

                    TimeSeries timeSeries = new TimeSeries("Test");

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords, ChronoUnit.SECONDS);

                    double value = 0.;

                    for (int i = 0; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
                        instant = instant.plusMillis(random.nextInt(1000));
                        value = Math.max(-20., Math.min(20., value + .8 * Math.random() - .4 + .5 * Math.cos(i + .2)));
                        double range = Math.abs(value) * .25;
                        double upperRange = value + range;
                        double lowerRange = value - range;
                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
                    }

                    overviewNumericTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
                    detailsNumericTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);

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

                    detailsNumericTimeSeriesPanel.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                        @Override
                        public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            JFrame DTWAnalysisFrame = new JFrame();
                            DTWAnalysisFrame.setTitle("DTW Analysis Frame");
                            DTWAnalysisFrame.setBounds(100, 100, 400, 1000);

                            DTWTimeSeriesAnalysisPanel dtwPanel = new DTWTimeSeriesAnalysisPanel();

                            ((JPanel)DTWAnalysisFrame.getContentPane()).setLayout(new BorderLayout());
                            ((JPanel)DTWAnalysisFrame.getContentPane()).add(dtwPanel, BorderLayout.CENTER);
                            DTWAnalysisFrame.setVisible(true);

                            dtwPanel.setTimeSeriesSelection(timeSeriesPanel.getTimeSeries(), timeSeriesSelection);
                        }

                        @Override
                        public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                                   Instant previousStartInstant, Instant previousEndInstant) {

                        }

                        @Override
                        public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {

                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    class SegmentRecord {
        TimeSeries segmentTimeSeries;
        NumericTimeSeriesPanel segmentNumericTimeSeriesPanel;
        JPanel segmentPanel;
        Instant segmentStartInstant;
        Instant segmentEndInstant;
        double distance;
    }
}
