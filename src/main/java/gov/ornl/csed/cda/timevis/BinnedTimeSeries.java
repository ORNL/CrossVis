package gov.ornl.csed.cda.timevis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by csg on 4/30/16.
 */
public class BinnedTimeSeries implements TimeSeriesListener {
    private final static Logger log = LoggerFactory.getLogger(BinnedTimeSeries.class);

    private TimeSeries timeSeries;
    private TreeMap<Instant, TimeSeriesBin> binMap = new TreeMap<>();
    private Duration binDuration;

    private double maxRangeValue = Double.NaN;
    private double minRangeValue = Double.NaN;

    private Instant startInstant;
    private Instant endInstant;

    public BinnedTimeSeries (TimeSeries timeSeries, Duration binDuration) {
        this.timeSeries = timeSeries;
        this.binDuration = binDuration;

        if (timeSeries.getStartInstant() != null) {
            this.startInstant = Instant.from(timeSeries.getStartInstant());
        }
        if (timeSeries.getEndInstant() != null) {
            this.endInstant = Instant.from(timeSeries.getEndInstant());
        }

        this.timeSeries.addTimeSeriesListener(this);
        recalculateBins();
    }

    public double getMaxRangeValue() {
        return maxRangeValue;
    }

    public double getMinRangeValue() {
        return minRangeValue;
    }

    public TimeSeriesBin getBin (Instant instant) {
        Instant binInstant = binMap.floorKey(instant);
        return binMap.get(binInstant);
    }

    public Collection<TimeSeriesBin> getBinsBetween (Instant startInstant, Instant endInstant) {
        NavigableMap<Instant, TimeSeriesBin> subMap = binMap.subMap(startInstant, true, endInstant, true);
        if (!subMap.isEmpty()) {
            return subMap.values();
        }
        return null;
    }

    public Collection<TimeSeriesBin> getAllBins() {
        return binMap.values();
    }

    public void setTimeSeries (TimeSeries timeSeries, Duration binDuration) {
        this.timeSeries.removeTimeSeriesListener(this);
        this.timeSeries = timeSeries;
        this.startInstant = Instant.from(timeSeries.getStartInstant());
        this.endInstant = Instant.from(timeSeries.getEndInstant());
        this.timeSeries.addTimeSeriesListener(this);
        this.binDuration = binDuration;
        recalculateBins();
    }

    public void setBinDuration (Duration binDuration) {
        this.binDuration = binDuration;
        recalculateBins();
    }

    public int getBinCount() {
        return binMap.size();
    }

    @Override
    public void timeSeriesRecordAdded(TimeSeries timeSeries, TimeSeriesRecord record) {
        if (startInstant == null || (!startInstant.equals(timeSeries.getStartInstant())) ) {
            this.startInstant = Instant.from(timeSeries.getStartInstant());
        }

        if (endInstant == null || (!endInstant.equals(timeSeries.getEndInstant())) ) {
            this.endInstant = Instant.from(timeSeries.getEndInstant());
        }

        Instant binStartInstant = toBinInstant(record.instant);
        TimeSeriesBin bin = binMap.get(binStartInstant);
        if (bin == null) {
            bin = new TimeSeriesBin(binStartInstant);
            binMap.put(binStartInstant, bin);
        }

        bin.addRecord(record);
    }

    private void recalculateBins() {
        binMap = new TreeMap<>();
        maxRangeValue = Double.NaN;
        minRangeValue = Double.NaN;

        if (!timeSeries.isEmpty()) {
            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
            if (records != null && !records.isEmpty()) {
                for (TimeSeriesRecord record : records) {
                    //                long deltaMillis = Duration.between(timeSeries.getStartInstant(), record.instant).toMillis();
                    //                int binNumber = (int) (deltaMillis / binDuration.toMillis());
                    //                long binDeltaMillis = binDuration.toMillis() * binNumber;
                    //
                    //                Instant binStartInstant = timeSeries.getStartInstant().plusMillis(binDeltaMillis);

                    Instant binStartInstant = toBinInstant(record.instant);
                    TimeSeriesBin bin = binMap.get(binStartInstant);
                    if (bin == null) {
                        bin = new TimeSeriesBin(binStartInstant);
                        binMap.put(binStartInstant, bin);
                    }
                    bin.addRecord(record);

                    // keep record of overall max and min values for ranges (for full extents)
                    double currentMinRangeValue = Math.min(bin.getStatistics().getMin(),
                            bin.getStatistics().getMean() - bin.getStatistics().getStandardDeviation());
                    double currentMaxRangeValue = Math.max(bin.getStatistics().getMax(),
                            bin.getStatistics().getMean() + bin.getStatistics().getStandardDeviation());

                    if (Double.isNaN(minRangeValue)) {
                        minRangeValue = currentMinRangeValue;
                        maxRangeValue = currentMaxRangeValue;
                    } else {
                        minRangeValue = Math.min(minRangeValue, currentMinRangeValue);
                        maxRangeValue = Math.max(maxRangeValue, currentMaxRangeValue);
                    }
                }

                // sort bin records
                for (TimeSeriesBin bin : binMap.values()) {
                    Collections.sort(bin.getRecords());
                }
            }
        }
    }

    private Instant toBinInstant (Instant instant) {
        long deltaMillis = Duration.between(startInstant, instant).toMillis();
        long binNumber = deltaMillis / binDuration.toMillis();
        return startInstant.plusMillis(binNumber * binDuration.toMillis());
//        long binNumber = instant.toEpochMilli() / binDuration.toMillis();
//        long binEpochMilli = binNumber * binDuration.toMillis();
//        return Instant.ofEpochMilli(binEpochMilli);

//        long deltaMillis = Duration.between(timeSeries.getStartInstant(), instant).toMillis();
//        int binNumber = (int) (deltaMillis / binDuration.toMillis());
//        long binDeltaMillis = binDuration.toMillis() * binNumber;
//
//        return timeSeries.getStartInstant().plusMillis(binDeltaMillis);
    }

    public static void main (String args[]) {
        int numTimeSeriesRecords = 60*24*20;
        double minValue = -10.;
        double maxValue = 10.;
        double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;

        Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);

        TimeSeries timeSeries = new TimeSeries("V");
        Duration binDuration = Duration.ofMinutes(10);
        BinnedTimeSeries binnedTimeSeries = new BinnedTimeSeries(timeSeries, binDuration);

        for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
            Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
//                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
            double value = minValue + (itime * valueIncrement);
            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
        }

        System.out.println("there are " + binnedTimeSeries.getBinCount() + " bins");

        System.out.println("Sleeping");
        try {
            Thread.sleep(1000*30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Making panel");

        TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(10, TimeSeriesPanel.PlotDisplayOption.LINE);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
        overviewTimeSeriesPanel.setBackground(Color.white);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        overviewTimeSeriesPanel.setBorder(border);

        overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ((JPanel)frame.getContentPane()).add(overviewTimeSeriesPanel, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);
    }
}
