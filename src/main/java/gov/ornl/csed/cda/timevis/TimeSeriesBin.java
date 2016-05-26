package gov.ornl.csed.cda.timevis;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Created by csg on 12/6/15.
 */
public class TimeSeriesBin {
    private Instant instant;
    private ArrayList<TimeSeriesRecord> records = new ArrayList<>();
    private SummaryStatistics statistics = new SummaryStatistics();

    public TimeSeriesBin (Instant instant) {
        this.instant = Instant.from(instant);
    }

    public Instant getInstant() {
        return instant;
    }

    public ArrayList<TimeSeriesRecord> getRecords () {
        return records;
    }

    public SummaryStatistics getStatistics() {
        return statistics;
    }

    public void addRecord(TimeSeriesRecord record) {
        records.add(record);
        statistics.addValue(record.value);
    }
}
