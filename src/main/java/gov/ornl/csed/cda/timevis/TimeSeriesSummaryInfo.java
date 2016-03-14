package gov.ornl.csed.cda.timevis;

import java.awt.geom.Point2D;
import java.time.Instant;

/**
 * Created by csg on 12/7/15.
 */
public class TimeSeriesSummaryInfo {
    Instant instant;
    Point2D meanPoint;
    Point2D maxPoint;
    Point2D minPoint;
    Point2D upperStandardDeviationRangePoint;
    Point2D lowerStandardDeviationRangePoint;
    double meanValue;
    double maxValue;
    double minValue;
    double standardDeviationValue;
    int numSamples;
}
