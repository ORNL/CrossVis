package gov.ornl.csed.cda.timevis;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by csg on 1/13/16.
 */
public class TimeSeriesPlotInfo {
    public TimeSeries timeSeries;
    public Rectangle plotRectangle;
    public TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap;
    public ArrayList<TimeSeriesSummaryInfo> summaryInfoList;
}
