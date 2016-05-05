package gov.ornl.csed.cda.timevis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import sun.security.krb5.internal.crypto.Des;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by csg on 12/6/15.
 */
public class TimeSeriesBin {
    private Instant instant;
    private ArrayList<TimeSeriesRecord> records = new ArrayList<>();
    private DescriptiveStatistics statistics = new DescriptiveStatistics();

    public TimeSeriesBin (Instant instant) {
        this.instant = Instant.from(instant);
    }

    public Instant getInstant() {
        return instant;
    }

    public ArrayList<TimeSeriesRecord> getRecords () {
        return records;
    }

    public DescriptiveStatistics getStatistics() {
        return statistics;
    }

    public void addRecord(TimeSeriesRecord record) {
        records.add(record);
        Collections.sort(records);
        statistics.addValue(record.value);
    }
}
