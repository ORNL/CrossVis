package gov.ornl.csed.cda.timevis;

import java.time.Instant;

public class TimeSeriesRecord implements Comparable<TimeSeriesRecord> {
	public Instant instant;
	public double x;
	public double y;
	public double value;
    public double uncertainty = Double.NaN;
	public double upperRange = Double.NaN;
	public double lowerRange = Double.NaN;

	@Override
	public int compareTo(TimeSeriesRecord otherRecord) {
		if (instant.isBefore(otherRecord.instant)) {
            return -1;
        } else if (instant.isAfter(otherRecord.instant)) {
            return 1;
        }
		return 0;
	}
}
