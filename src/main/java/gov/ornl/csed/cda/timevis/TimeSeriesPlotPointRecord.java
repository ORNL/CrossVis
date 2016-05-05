package gov.ornl.csed.cda.timevis;

import java.awt.geom.Point2D;
import java.time.Instant;

/**
 * Created by csg on 5/1/16.
 */
public class TimeSeriesPlotPointRecord {
    public Instant instant;
    public double x;
    public double meanY;
    public double maxY;
    public double minY;
    public double upperStdevRangeY;
    public double lowerStdevRangeY;

    public double valueY;

    public TimeSeriesBin bin;
    public TimeSeriesRecord record;
}
