package gov.ornl.csed.cda.timevis;

/**
 * Created by csg on 4/27/16.
 */
public interface TimeSeriesListener {
    public void timeSeriesRecordAdded(TimeSeries timeSeries, TimeSeriesRecord record);
}
