package gov.ornl.csed.cda.util;

import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by csg on 6/3/16.
 */
public class PLGFileSegmenter {
    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public static void main (String args[]) throws IOException {
//        File plgFile = new File("/Users/csg/Desktop/AM_data/LongBuild/R1119_2016-04-15_12.43_20160415_Q10_BOEING_TENSILE_V2.plg");
//        File plgFile = new File("/Users/csg/Desktop/AM_data/2016-Boeing-GoodBad-Builds/Bad/R1119_2016-03-28_11.58_20160328_Q10_BOEING_TENSILE_BUILD_V1/Bad.plg");
        File plgFile = new File("/Users/csg/Desktop/AM_data/2016-Boeing-GoodBad-Builds/Good/R1119_2016-04-07_13.28_20160406_Q10_BOEING_TENSILE_V1/Good.plg");
        ArrayList<String> varNames = new ArrayList<>();
        varNames.add("OPC.PowerSupply.Beam.BeamCurrent");
        varNames.add("OPC.Table.CurrentPosition");

        HashMap<String, TimeSeries> timeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, varNames);
        TimeSeries segmentTimeSeries = timeSeriesMap.get("OPC.Table.CurrentPosition");
        TimeSeries valueTimeSeries = timeSeriesMap.get("OPC.PowerSupply.Beam.BeamCurrent");

        TreeMap<Double, LayerInfo> timeSeriesLayerMap = new TreeMap<>();

        ArrayList<TimeSeriesRecord> segmentTimeSeriesRecords = segmentTimeSeries.getAllRecords();
        TimeSeriesRecord lastSegmentRecord = null;
        for (TimeSeriesRecord segmentRecord : segmentTimeSeriesRecords) {
            if (lastSegmentRecord != null) {
                // get all the value records and add to new time series object
                LayerInfo layerInfo = new LayerInfo();
                layerInfo.tablePosition = lastSegmentRecord.value;
                layerInfo.start = Instant.from(lastSegmentRecord.instant);
                layerInfo.end = Instant.from(segmentRecord.instant);

                layerInfo.valueRecords = new ArrayList<TimeSeriesRecord>();

                ArrayList<TimeSeriesRecord> valueRecords = valueTimeSeries.getRecordsBetween(layerInfo.start, layerInfo.end);
                if (valueRecords != null) {
                    for (TimeSeriesRecord valueRecord : valueRecords) {
                        layerInfo.valueRecords.add(valueRecord);
                    }
                    timeSeriesLayerMap.put(layerInfo.tablePosition, layerInfo);
                } else {
                    System.err.println("no value records for layer (" + dtFormatter.format(layerInfo.start) + " - " + dtFormatter.format(layerInfo.end) + ")");
                }
            }
            lastSegmentRecord = segmentRecord;
        }

        // write to text file
        File outputFile = new File("BOEING_GOOD.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        double prevLayerLastValue = Double.NaN;
        for (LayerInfo layerInfo : timeSeriesLayerMap.values()) {
            StringBuffer lineBuffer = new StringBuffer();
            lineBuffer.append(layerInfo.tablePosition + "," + layerInfo.start.toEpochMilli() + "," + layerInfo.end.toEpochMilli());

            TimeSeriesRecord layerRecords[] = layerInfo.valueRecords.toArray(new TimeSeriesRecord[layerInfo.valueRecords.size()]);
            Arrays.sort(layerRecords);

            if (!Double.isNaN(prevLayerLastValue)) {
                // write first record using layer start instant and the last value from the previous layer
                lineBuffer.append("," + layerInfo.start.toEpochMilli() + "," + prevLayerLastValue);
            }

            for (TimeSeriesRecord record : layerRecords) {
                lineBuffer.append("," + record.instant.toEpochMilli() + "," + record.value);
            }

            // write last record using last record value in this layer and layer end instant
            if (!layerRecords[layerRecords.length - 1].instant.equals(layerInfo.end)) {
                lineBuffer.append("," + layerInfo.end.toEpochMilli() + "," + layerRecords[layerRecords.length-1].value);
            }
            writer.write(lineBuffer.toString() + "\n");

            prevLayerLastValue = layerRecords[layerRecords.length - 1].value;
        }
        writer.close();
    }

    static class LayerInfo {
        double tablePosition;
        ArrayList<TimeSeriesRecord> valueRecords;
        Instant start;
        Instant end;
    }
}
