package gov.ornl.csed.cda.timevis;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by csg on 12/6/15.
 */
public class TimeSeriesBin {
    public Instant instant;
    public ArrayList<TimeSeriesRecord> records;

    public TimeSeriesBin (Instant instant) {
        this.instant = Instant.from(instant);
        records = new ArrayList<>();
    }
}
