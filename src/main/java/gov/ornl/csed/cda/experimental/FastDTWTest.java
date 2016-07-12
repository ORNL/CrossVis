package gov.ornl.csed.cda.experimental;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;
import gov.ornl.csed.cda.Falcon.FileMetadata;
import gov.ornl.csed.cda.Falcon.MultiViewPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by csg on 2/5/16.
 */
public class FastDTWTest {

    public static void main (String args[]) throws IOException {
        double mean = 0;
        double min;
        double max;
        double mid;

        ArrayList<String> varNames = new ArrayList<>();
        varNames.add("time");
        varNames.add("test");

        TimeSeries testTimeSeries = new TimeSeries("testTimeSeries");
        TimeSeries dummyTimeSeries = new TimeSeries("");
        TreeMap<Double, TimeSeries> testSegments = new TreeMap<>();

        // READ IN THE TEST TIME SERIES
        File rawCSVfile = new File("/Users/whw/Desktop/DTW_testTimeSeriesRaw.csv");
        FileMetadata fileMetadata = new FileMetadata(rawCSVfile);
        fileMetadata.fileType = FileMetadata.FileType.CSV;
        fileMetadata.timeChronoUnit = ChronoUnit.MILLIS;
        fileMetadata.timeColumnIndex = 0;
        fileMetadata.variableList = varNames;

        try {
            testTimeSeries = readCSVVariableTimeSeries(fileMetadata, "test");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // === check to make sure that the instants and the values are properly added to the time series ===
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            System.out.println(testTimeSeries.getAllRecords().get(i).instant + ", " + testTimeSeries.getAllRecords().get(i).value);

            mean += testTimeSeries.getAllRecords().get(i).value;
        }

        mean /= testTimeSeries.getAllRecords().size();
        min = testTimeSeries.getMinValue();
        max = testTimeSeries.getMaxValue();
        mid = (min + max) / 2.;


        System.out.println("the mean is : " + mean);
        System.out.println("the min is : " + min);
        System.out.println("the max is : " + max);

        // CREATE ALL OF THE TEST SEGMENTS

        // test 0 - the original time series
        testSegments.put(0., testTimeSeries);

        // test 1 - the original X2
        dummyTimeSeries = new TimeSeries("Test 1: Original X2");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant, record.value * 2, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(1., dummyTimeSeries);
        }

        // test 2 - the original inverted
        dummyTimeSeries = new TimeSeries("Test 2: Original reflected over the mid point value");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant, mid - (record.value - mid), Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(2., dummyTimeSeries);
        }

        // test 3 - the original reflected
        dummyTimeSeries = new TimeSeries("Test 3: Original reflected over the x-axis");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant, -record.value, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(3., dummyTimeSeries);
        }

        // test 4 - the original with small duration perturbations
        ArrayList<Long> durationPerturbations = new ArrayList<>();
        ArrayList<Long> valuePerturbations = new ArrayList<>();

        dummyTimeSeries = new TimeSeries("Test 4: Original with small time duration variations");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);
            long perturbation;
            Random random = new Random(500);

            perturbation = (long)random.nextInt(10) * ((random.nextInt(1) == 0) ? -1 : 1);
            durationPerturbations.add(perturbation);

            dummyTimeSeries.addRecord(record.instant.plusMillis(perturbation), record.value, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(4., dummyTimeSeries);
        }

        // test 5 - the original with small value perturbations
        dummyTimeSeries = new TimeSeries("Test 5: Original with small value variations");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);
            long perturbation;
            Random random = new Random(500);

            perturbation = (long)random.nextInt((int) ((max - min) * .1)) * ((random.nextInt(1) == 0) ? -1 : 1);
            valuePerturbations.add(perturbation);

            dummyTimeSeries.addRecord(record.instant, record.value + perturbation, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(5., dummyTimeSeries);
        }

        // test 6 - the original with small duration and value perturbations (a using the SAME perturbations from the previous two tests but combined)
        dummyTimeSeries = new TimeSeries("Test 6: Original with [same] time duration and value variations");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant.plusMillis(durationPerturbations.get(i)), record.value + valuePerturbations.get(i), Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(6., dummyTimeSeries);
        }

        // test 7 - a point (the mean value)
        dummyTimeSeries = new TimeSeries("Test 7: Single point equal to the mean value of the time series (mean taken of the raw data)");
        dummyTimeSeries.addRecord(testTimeSeries.getStartInstant(), mean, Double.NaN, Double.NaN);
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(7., dummyTimeSeries);
        }

        // test 8 - a point (the max value)
        dummyTimeSeries = new TimeSeries("Test 8: Single point equal to the max value fo the time series");
        dummyTimeSeries.addRecord(testTimeSeries.getStartInstant(), testTimeSeries.getMaxValue(), Double.NaN, Double.NaN);
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(8., dummyTimeSeries);
        }

        // test 9 - a line (the mean value and with time durations equal to the original)
        dummyTimeSeries = new TimeSeries("Test 9: Line equal to the mean of the time series (time durations are the same as the original test segment)");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant, mean, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(9., dummyTimeSeries);
        }

        // test 10 - a line (the min value " " " " " " " ")
        dummyTimeSeries = new TimeSeries("Test 10: Line equal to the min value of the time series");
        for (int i = 0; i < testTimeSeries.getAllRecords().size(); i++) {
            TimeSeriesRecord record = testTimeSeries.getAllRecords().get(i);

            dummyTimeSeries.addRecord(record.instant, min, Double.NaN, Double.NaN);
        }
        if (!dummyTimeSeries.isEmpty()) {
            testSegments.put(10., dummyTimeSeries);
        }

        // PREVIEW ALL OF THE TIME SERIES
        System.out.print("\n\n");
        for (TimeSeries series : testSegments.values()) {
            for (int i = 0; i < series.getAllRecords().size(); i++) {
                System.out.print("(" + series.getAllRecords().get(i).instant + "," + series.getAllRecords().get(i).value + ")\t");
            }
            System.out.print("\n");
        }
        System.out.print("\n\n");

        // CALCULATE THE DISTANCES USING FASTDTW
        TreeMap<Double, Double> distances = calculateDistances(testTimeSeries, testSegments);

        // PRINT OUT THE RESULTS
        for (Map.Entry<Double, Double> distance : distances.entrySet()) {
            System.out.println(distance.getKey() + " : " + distance.getValue());
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ArrayList<TimeSeries> timeSeriesList = new ArrayList<>();

        MultiViewPanel multiViewPanel = new MultiViewPanel(200);
        JScrollPane scroller = new JScrollPane(multiViewPanel);

        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        frame.setSize(1800, 600);
        frame.setVisible(true);

        for (TimeSeries timeSeries : testSegments.values()) {
            multiViewPanel.addTimeSeries(timeSeries, "Group 1");
        }
    }

    private static TimeSeries readCSVVariableTimeSeries(FileMetadata fileMetadata, String variableName) throws IOException {
//        int valuesRead = 0;
//        int valuesStored = 0;
        if (fileMetadata.timeSeriesMap.containsKey(variableName)) {
            return fileMetadata.timeSeriesMap.get(variableName);
        } else {
            int variableIndex = fileMetadata.variableList.indexOf(variableName);

            TimeSeries timeSeries = new TimeSeries(fileMetadata.file.getName() + ":" + variableName);
            BufferedReader csvFileReader = new BufferedReader(new FileReader(fileMetadata.file));

            // skip first line
            csvFileReader.readLine();
            String line = null;
            while ((line = csvFileReader.readLine()) != null) {
                String tokens[] = line.split(",");

                // parse time value
                Instant instant = null;
                if (fileMetadata.timeChronoUnit == ChronoUnit.MILLIS) {
                    long timeMillis = Long.parseLong(tokens[fileMetadata.timeColumnIndex]);
                    instant = Instant.ofEpochMilli(timeMillis);
                } else {
                    double seconds = Double.parseDouble(tokens[fileMetadata.timeColumnIndex]);
                    long timeMillis = (long) (seconds * 1000.);
                    instant = Instant.ofEpochMilli(timeMillis);
                }

                // parse data value
                double value = Double.parseDouble(tokens[variableIndex]);
//                valuesRead++;
                if (!Double.isNaN(value)) {
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//                    valuesStored++;
                } else {
//                    log.debug("Ignored value: time instant: " + instant.toString() + " value: " + value);
                }
            }

            fileMetadata.timeSeriesMap.put(variableName, timeSeries);
            csvFileReader.close();
//            log.debug("valuesRead: " + valuesRead);
//            log.debug("valuesStored: " + valuesStored);
            return timeSeries;
        }
    }

    private static TreeMap<Double, Double> calculateDistances(TimeSeries referenceTimeSeries, TreeMap<Double, TimeSeries> segmentedTimeSeriesMap) {

        com.fastdtw.timeseries.TimeSeries referenceDTWtimeSeries;
        TimeSeriesBase.Builder buildTemp1 = TimeSeriesBase.builder();
        TreeMap<Double, Double> timeSeriesDistances = new TreeMap<>();

        //  -> build time series for the reference value
        for (TimeSeriesRecord record : referenceTimeSeries.getAllRecords()) {
            buildTemp1.add((double) record.instant.toEpochMilli(), record.value);
        }

        referenceDTWtimeSeries = buildTemp1.build();


        //  -> find the distance form the reference value time series to all others
        for (Map.Entry<Double, TimeSeries> segment : segmentedTimeSeriesMap.entrySet()) {

            if (segment.getValue().getAllRecords().isEmpty()) {
                timeSeriesDistances.put(segment.getKey(), Double.NaN);
                continue;
            }

            com.fastdtw.timeseries.TimeSeries ts2;
            TimeSeriesBase.Builder buildTemp2 = TimeSeriesBase.builder();

            for (TimeSeriesRecord record : segment.getValue().getAllRecords()) {
                buildTemp2.add((double) record.instant.toEpochMilli(), record.value);
            }

            ts2 = buildTemp2.build();


            //  -> add distances to timeSeriesDistances
            if (!(referenceDTWtimeSeries.size() == 1 && ts2.size() == 1)) {
                double distance = FastDTW.compare(referenceDTWtimeSeries, ts2, FastDTW.DEFAULT_SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getDistance();
                WarpPath path = FastDTW.compare(referenceDTWtimeSeries, ts2, FastDTW.DEFAULT_SEARCH_RADIUS, Distances.EUCLIDEAN_DISTANCE).getPath();

                timeSeriesDistances.put(segment.getKey(), distance);
            }
        }

        return timeSeriesDistances;
    }

}
