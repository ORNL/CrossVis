package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.BinnedTimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeries;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by csg on 5/10/16.
 */
public class BinnedTimeSeriesTest {
    public static void main (String args[]) throws Exception {

        File csvFile = new File("/Users/csg/Desktop/D3/vast-samples/nf-test-5m.csv");
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
        System.out.println("Press Enter key to continue...");

        System.in.read();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Creating Binned TimeSeries with 10 minute bins");
                long start = System.currentTimeMillis();
                Duration binDuration = Duration.ofMinutes(10);
                BinnedTimeSeries binnedTimeSeries = new BinnedTimeSeries(timeSeries, binDuration);
                long end = System.currentTimeMillis();

                double timeElapsed = (end - start) / 1000.;
                System.out.println("Finished: " + binnedTimeSeries.getBinCount() + " bins created.");
                System.out.println("Process took " + timeElapsed + " seconds");

            }
        };
        runnable.run();
    }
}
