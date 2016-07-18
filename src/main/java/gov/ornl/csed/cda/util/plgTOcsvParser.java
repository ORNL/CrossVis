package gov.ornl.csed.cda.util;

/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    12 Jul 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */


/*  PROBLEM DESCRIPTION
making a consistent set of time series records for multiple variables sampled at different instants

- get all time series for all variables from file. no sampling, just using data as recorded in the file
- make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
- iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
- write all data to csv (time written as epoch time in millis)
 */


/*  WHAT I'LL NEED

FOR SAMPLED FILE
- time resolution; sample rate; sample period
- struct to hold the sampled time series
- the start instant
- the end instant

FOR PER LAYER FILE
- time series for the segmenting variable

FOR BOTH
- list of names of variables to pull out of the plg file
- struct to hold the raw time series of all of the desired time series
- a .plg filename
- a .csv filename
-

 */


import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import prefuse.data.Tree;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.Time;
import java.time.Instant;
import java.util.*;

public class PlgToCsvParser {

    private String plgFilename = "";
    private String csvFilename = "";

    private File plgFile;
    private File csvFile;

    private ArrayList<String> plgDesiredVarNames = new ArrayList<>();
    private String plgSegmentingVarName = "";

    private HashMap<String, TimeSeries> rawTimeSeries;
    private TreeMap<String, TreeMap<Instant, Double>> seriesTreeSet = new TreeMap<>();
    private TreeMap<String, TimeSeries> newTimeSeries = new TreeMap();

    private Instant startInstant;
    private Instant endInstant;

    private Long sampleDuration = 10L;


    public PlgToCsvParser(String plgFilename, String csvFilename, String variablesFileName, Long sampleDuration) throws IOException {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(variablesFileName)));

        String line = bufferedReader.readLine();

        while (line != null) {

            if (!plgDesiredVarNames.contains(line)) {
                plgDesiredVarNames.add(line);
            }

            line = bufferedReader.readLine();
        }
    }

    public PlgToCsvParser(String plgFilename, String csvFilename, Long sampleDuration) {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        plgDesiredVarNames.add("OPC.PowerSupply.Beam.BeamCurrent");
        plgDesiredVarNames.add("OPC.PowerSupply.HighVoltage.Grid");
    }


    public void parsePerSampleData() {

        // - get all time series for all variables from file. no sampling, just using data as recorded in the file
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newTimeSeries = convertRawToSampledTimeSeries(rawTimeSeries, sampleDuration);

        // test to see if output is correct
        // print out the raw time series
//        System.out.println();
//        for (TimeSeriesRecord record : rawTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }

        // print out the sampled time series
//        System.out.println();
//        for (TimeSeriesRecord record : newTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }


        // - write all data to csv (time written as epoch time in millis)
        try {
            writeSampledTimeSeriesToCsv(newTimeSeries, csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void parseLosslessData() {

        // - get all time series for all variables from file. no sampling, just using data as recorded in the file
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newTimeSeries = convertRawToLosslessTimeSeries(rawTimeSeries);

        // test to see if output is correct
        // print out the raw time series
//        System.out.println();
//        for (TimeSeriesRecord record : rawTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }

        // print out the sampled time series
//        System.out.println();
//        for (TimeSeriesRecord record : newTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }


        // - write all data to csv (time written as epoch time in millis)
        try {
            writeSampledTimeSeriesToCsv(newTimeSeries, csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parsePerLayerData() {
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TreeMap<String, TimeSeries> convertRawToSampledTimeSeries(HashMap<String, TimeSeries> rawTimeSeries, Long sampleDuration) {
        TreeMap <String, TimeSeries> timeSeries = new TreeMap<>();

        // initialize the start and end instants
        TimeSeries[] timeSeriesArr = new TimeSeries[rawTimeSeries.entrySet().size()];
        timeSeriesArr = rawTimeSeries.values().toArray(timeSeriesArr);

        startInstant = timeSeriesArr[0].getStartInstant();
        endInstant = timeSeriesArr[0].getEndInstant();

        // - make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
        for (Map.Entry<String, TimeSeries> entry : rawTimeSeries.entrySet()) {
            TreeMap<Instant, Double> temp = new TreeMap<>();

            // update the overall start and end instants if necessary
            if (entry.getValue().getStartInstant().isBefore(startInstant)) {
                startInstant = entry.getValue().getStartInstant();
            }

            if (entry.getValue().getEndInstant().isAfter(endInstant)) {
                endInstant = entry.getValue().getEndInstant();
            }

            for (TimeSeriesRecord record : entry.getValue().getAllRecords()) {
                temp.put(record.instant, record.value);
            }

            seriesTreeSet.put(entry.getKey(), temp);
        }

        Long comparison = endInstant.toEpochMilli() - startInstant.toEpochMilli();
        System.out.println(startInstant);
        System.out.println(endInstant);
        System.out.println(comparison);

        System.out.println(Instant.ofEpochMilli(comparison));
        // - iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
        for (Map.Entry<String, TreeMap<Instant, Double>> entry : seriesTreeSet.entrySet()) {
            String key = entry.getKey();
            TreeMap<Instant, Double> value = entry.getValue();
            TimeSeries temp = new TimeSeries(key + "_sampled");

            Instant sampleInstant = startInstant;

            while (sampleInstant.isBefore(endInstant)) {

                temp.addRecord(sampleInstant, value.floorEntry(sampleInstant).getValue(), Double.NaN, Double.NaN);

                sampleInstant = sampleInstant.plusMillis(sampleDuration);
            }

            timeSeries.put(key, temp);
        }

        return timeSeries;
    }


    public TreeMap<String, TimeSeries> convertRawToLosslessTimeSeries(HashMap<String, TimeSeries> rawTimeSeries) {
        TreeMap <String, TimeSeries> timeSeries = new TreeMap<>();
        TreeMap <String, TreeMap <Instant, Double>> varValues = new TreeMap<>();

        ArrayList <Instant> instants = new ArrayList<>();

        for (Map.Entry <String, TimeSeries> series : rawTimeSeries.entrySet()) {

            TreeMap <Instant, Double> temp = new TreeMap<>();

            for (TimeSeriesRecord record : series.getValue().getAllRecords()) {

                temp.put(record.instant, record.value);

                if (!instants.contains(record.instant)) {
                    instants.add(record.instant);
                }
            }

            varValues.put(series.getKey(), temp);
        }

        Collections.sort(instants);

        System.out.println(instants.get(0).toEpochMilli());
        System.out.println(instants.get(instants.size()-1).toEpochMilli());

        for (Map.Entry <String, TimeSeries> entry : rawTimeSeries.entrySet()) {

            TimeSeries temp = new TimeSeries(entry.getKey() + "_lossless");

            for (int i = 0; i < instants.size(); i++) {

                Instant instant = instants.get(i);

                temp.addRecord(instant, varValues.get(entry.getKey()).floorEntry(instant).getValue(), Double.NaN, Double.NaN);
                System.out.println(instant.toEpochMilli() + " : " + varValues.get("OPC.PowerSupply.Beam.BeamCurrent").floorEntry(instant).getValue());
            }

            timeSeries.put(entry.getKey(), temp);

        }
//        // initialize the start and end instants
//        TimeSeries[] timeSeriesArr = new TimeSeries[rawTimeSeries.entrySet().size()];
//        timeSeriesArr = rawTimeSeries.values().toArray(timeSeriesArr);
//
//        startInstant = timeSeriesArr[0].getStartInstant();
//        endInstant = timeSeriesArr[0].getEndInstant();
//
//        // - make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
//        for (Map.Entry<String, TimeSeries> entry : rawTimeSeries.entrySet()) {
//            TreeMap<Instant, Double> temp = new TreeMap<>();
//
//            // update the overall start and end instants if necessary
//            if (entry.getValue().getStartInstant().isBefore(startInstant)) {
//                startInstant = entry.getValue().getStartInstant();
//            }
//
//            if (entry.getValue().getEndInstant().isAfter(endInstant)) {
//                endInstant = entry.getValue().getEndInstant();
//            }
//
//            for (TimeSeriesRecord record : entry.getValue().getAllRecords()) {
//                temp.put(record.instant, record.value);
//            }
//
//            seriesTreeSet.put(entry.getKey(), temp);
//        }
//
//        Long comparison = endInstant.toEpochMilli() - startInstant.toEpochMilli();
//        System.out.println(startInstant);
//        System.out.println(endInstant);
//        System.out.println(comparison);
//
//        System.out.println(Instant.ofEpochMilli(comparison));
//        // - iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
//        for (Map.Entry<String, TreeMap<Instant, Double>> entry : seriesTreeSet.entrySet()) {
//            String key = entry.getKey();
//            TreeMap<Instant, Double> value = entry.getValue();
//            TimeSeries temp = new TimeSeries(key + "_sampled");
//
//            Instant sampleInstant = startInstant;
//
//            while (sampleInstant.isBefore(endInstant)) {
//
//                temp.addRecord(sampleInstant, value.floorEntry(sampleInstant).getValue(), Double.NaN, Double.NaN);
//
//                sampleInstant = sampleInstant.plusMillis(sampleDuration);
//            }
//
//            timeSeries.put(key, temp);
//        }

        return timeSeries;
    }

    // TODO: 7/13/16 - do this
    public void writeSampledTimeSeriesToCsv(TreeMap<String, TimeSeries> sampledTimeSeries, File csvFile) throws IOException {

        String rowBuffer = "Time";

        // open the csv file for writing
        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile));

        // write out the headings
        for (Map.Entry<String, TimeSeries> entry : sampledTimeSeries.entrySet()) {
            rowBuffer += "," + entry.getKey();
        }

        // TODO: 7/13/16 - IO
//        System.out.println(rowBuffer);
        csvWriter.write(rowBuffer.trim() + "\n");

        System.out.println(sampledTimeSeries.firstEntry().getValue().getEndInstant().toEpochMilli());

        // build the row
        Integer dummy = sampledTimeSeries.firstEntry().getValue().getAllRecords().size();
        for (TimeSeriesRecord record : sampledTimeSeries.firstEntry().getValue().getAllRecords()) {

            rowBuffer = String.valueOf( record.instant.toEpochMilli() );
//            System.out.println(rowBuffer);
//            System.out.println(Instant.ofEpochMilli( Long.parseLong(rowBuffer) ));
//            System.out.println(record.instant);

            for (TimeSeries series : sampledTimeSeries.values()) {
                rowBuffer += "," + series.getRecordsAt(record.instant).get(0).value;
            }

            // TODO: 7/13/16 - IO
//            System.out.println(rowBuffer);
            csvWriter.write(rowBuffer.trim() + "\n");
        }
        csvWriter.close();

    }

    public static void main(String[] args) {

        // default values if
        String plgFileName = "/Users/whw/ORNL Internship/New Build Data/29-6-2016/For William/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1 LogFiles/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1.plg";
        String csvFileName = "/Users/whw/ORNL Internship/test2.csv";
        String variablesFileName = "";
        Long sampleDuration = 1000L;

        PlgToCsvParser parser = null;

        if (args.length == 5) {
            plgFileName = args[1];
            csvFileName = args[2];
            variablesFileName = args[3];
            sampleDuration = Long.valueOf(args[4]);

            try {
                parser = new PlgToCsvParser(plgFileName, csvFileName, variablesFileName, sampleDuration);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args.length > 1) {
            // TODO: 7/14/16 - fix this message
            System.out.println("Error");
            return;

        } else {

            parser = new PlgToCsvParser(plgFileName, csvFileName, sampleDuration);
        }

        if (parser != null) {
            parser.parsePerSampleData();
//            parser.parseLosslessData();
        }
    }
}
