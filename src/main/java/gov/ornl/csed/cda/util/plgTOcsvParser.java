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


import com.sun.javafx.collections.MappingChange;
import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PlgToCsvParser {

    private String plgFilename = "";
    private String csvFilename = "";

    private File plgFile;
    private File csvFile;

    private ArrayList<String> plgDesiredVarNames = new ArrayList<>();
    private String plgSegmentingVarName = "";

    private HashMap<String, TimeSeries> rawTimeSeries;
    private TreeMap<String, TreeMap<Instant, Double>> seriesTreeSet = new TreeMap<>();
    private TreeMap<String, TimeSeries> sampledTimeSeries = new TreeMap();

    private Instant startInstant;
    private Instant endInstant;

    private Long sampleDuration = 10L;


    public PlgToCsvParser(String plgFilename, String csvFilename, Long sampleDuration) {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        plgDesiredVarNames.add("OPC.PowerSupply.Beam.BeamCurrent");
    }


    public void parsePerSampledData() {

        // - get all time series for all variables from file. no sampling, just using data as recorded in the file
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize the start and end instants
        TimeSeries[] timeSeries = new TimeSeries[rawTimeSeries.entrySet().size()];
        timeSeries = rawTimeSeries.values().toArray(timeSeries);

        startInstant = timeSeries[0].getStartInstant();
        endInstant = timeSeries[0].getEndInstant();

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

            sampledTimeSeries.put(key, temp);
        }

        // test to see if output is correct
        // print out the raw time series
//        System.out.println();
//        for (TimeSeriesRecord record : rawTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }

        // print out the sampled time series
//        System.out.println();
//        for (TimeSeriesRecord record : sampledTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }


        // - write all data to csv (time written as epoch time in millis)
        // TODO: 7/13/16 - do this

    }

    public void parsePerLayerData() {
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PlgToCsvParser parser = new PlgToCsvParser("/Users/whw/ORNL Internship/New Build Data/29-6-2016/For William/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1 LogFiles/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1.plg", "/Users/whw/ORNL Internship/test.csv", 1000000L);
        parser.parsePerSampledData();
    }
}
