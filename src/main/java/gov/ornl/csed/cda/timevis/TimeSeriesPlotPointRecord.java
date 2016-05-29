package gov.ornl.csed.cda.timevis;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Instant;

/**
 * Created by csg on 5/1/16.
 */
public class TimeSeriesPlotPointRecord {
    public Instant instant;

    public double x;

    /* overview y values */
    public double meanY;
    public double maxY;
    public double minY;
    public double upperStdevRangeY;
    public double lowerStdevRangeY;

    /* line, point, bar y values */
    public double valueY;

    /* spectrum y values */
    public double spectrumTopY;
    public double spectrumBottomY;

    /* color */
    public Color color;

    public TimeSeriesBin bin;
    public TimeSeriesRecord record;
}
