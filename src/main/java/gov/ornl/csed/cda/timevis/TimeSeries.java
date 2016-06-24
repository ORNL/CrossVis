package gov.ornl.csed.cda.timevis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TimeSeries {
	private final static Logger log = LoggerFactory.getLogger(TimeSeries.class);

	private String name;
	private double maxValue = Double.NaN;
	private double minValue = Double.NaN;
    private Instant startInstant;
    private Instant endInstant;
    private TreeMap<Instant, ArrayList<TimeSeriesRecord>> recordMap = new TreeMap<>();
    private ArrayList<TimeSeriesListener> listeners = new ArrayList<>();


	public TimeSeries(String name) {
		this.name = name;
	}


    public TreeMap<Instant, ArrayList<TimeSeriesRecord>> getRecordMap() {
        return recordMap;
    }


	public void clear() {
        recordMap.clear();
        startInstant = null;
        endInstant = null;
        maxValue = Double.NaN;
        minValue = Double.NaN;
        Point2D point = new Point2D.Double();
    }


    public void addTimeSeriesListener(TimeSeriesListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }


    public boolean removeTimeSeriesListener (TimeSeriesListener listener) {
        return listeners.remove(listener);
    }


    public void fireDataRecordAdded (TimeSeriesRecord record) {
        for (TimeSeriesListener listener : listeners) {
            listener.timeSeriesRecordAdded(this, record);
        }
    }


	public Instant getStartInstant() {
		return startInstant;
	}


	public Instant getEndInstant() {
		return endInstant;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}


	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}


    public double getMaxValue() {
        return maxValue;
    }


    public double getMinValue() {
        return minValue;
    }


    public void addRecord(TimeSeriesRecord record) {
        if (recordMap.isEmpty()) {
            if (!Double.isNaN(record.value)) {
                maxValue = record.value;
                minValue = record.value;
            }

            startInstant = record.instant;
            endInstant = record.instant;
        } else {
            if (!Double.isNaN(record.value)) {
                if (record.value > maxValue) {
                    maxValue = record.value;
                }
                if (record.value < minValue) {
                    minValue = record.value;
                }
            }

            if (record.instant.isBefore(startInstant)) {
                startInstant = record.instant;
            }
            if (record.instant.isAfter(endInstant)) {
                endInstant = record.instant;
            }
        }

        ArrayList<TimeSeriesRecord> instantRecordList = recordMap.get(record.instant);
        if (instantRecordList == null) {
            instantRecordList = new ArrayList<>();
            recordMap.put(record.instant, instantRecordList);
        }

        // TODO: (CAS 6/17/2016) This needs to be removed, it will improve efficiency to not sort records on each addition. Maybe only sort after all data is added or insert into sorted location.
        instantRecordList.add(record);
        Collections.sort(instantRecordList);

        fireDataRecordAdded(record);
    }

    // TODO: (CAS 6/17/2016) Either use the upperRange and lowerRange or remove them from the function (removal is probably way to go)
	public void addRecord(Instant instant, double value, double upperRange, double lowerRange) {
		TimeSeriesRecord record = new TimeSeriesRecord();
		record.instant = Instant.from(instant);
		record.value = value;

        if (recordMap.isEmpty()) {
            maxValue = value;
            minValue = value;
            startInstant = instant;
            endInstant = instant;
        } else {
            if (value > maxValue) {
                maxValue = value;
            }
            if (value < minValue) {
                minValue = value;
            }
            if (instant.isBefore(startInstant)) {
                startInstant = instant;
            }
            if (instant.isAfter(endInstant)) {
                endInstant = instant;
            }
        }

        ArrayList<TimeSeriesRecord> instantRecordList = recordMap.get(instant);
        if (instantRecordList == null) {
//            bin = new TimeSeriesBin(instant);
            instantRecordList = new ArrayList<>();
//            recordMap.put(instant, bin);
            recordMap.put(instant, instantRecordList);
        }

        //TODO: Remove this sort.  It is terribly inefficient to sort after each new record addition
        instantRecordList.add(record);
        Collections.sort(instantRecordList);
//        bin.records.add(record);
//        Collections.sort(bin.records);

        fireDataRecordAdded(record);
    }


    public ArrayList<TimeSeriesRecord> getNearestRecordsFor(Instant instant, Duration searchRangeDuration) {
        Instant searchRangeStart = instant.minus(searchRangeDuration.abs());
        Instant searchRangeEnd = instant.plus(searchRangeDuration.abs());

        ArrayList<TimeSeriesRecord> records = getRecordsBetween(searchRangeStart, searchRangeEnd);

        if (records != null && !records.isEmpty()) {
            ArrayList<TimeSeriesRecord> nearestRecords = new ArrayList<>();
            Duration nearestRecordDuration = null;
            for (TimeSeriesRecord record : records) {
                Duration duration = Duration.between(instant, record.instant);
                if (nearestRecordDuration == null) {
                    nearestRecords.add(record);
                    nearestRecordDuration = duration;
                } else if (duration.abs().toMillis() < nearestRecordDuration.abs().toMillis()) {
                    nearestRecords.clear();
                    nearestRecords.add(record);
                    nearestRecordDuration = duration;
                } else if (duration.abs().toMillis() == nearestRecordDuration.abs().toMillis()) {
                    nearestRecords.add(record);
                    nearestRecordDuration = duration;
                }
            }

            if (!nearestRecords.isEmpty()) {
                return nearestRecords;
            }
        }

        return null;
    }


	public ArrayList<TimeSeriesRecord> getRecordsAt(Instant instant) {
		if (recordMap.isEmpty()) {
			return null;
		}
		
		if (instant.isBefore(recordMap.firstKey()) || instant.isAfter(recordMap.lastKey())) {
			return null;
		}

        return recordMap.get(instant);
	}


	public ArrayList<TimeSeriesRecord> getRecordsBetween(Instant start, Instant end) {
		ArrayList<TimeSeriesRecord> records = null;

		NavigableMap<Instant, ArrayList<TimeSeriesRecord>> subRecordMap = recordMap.subMap(start, true, end, true);
		if (!subRecordMap.isEmpty()) {
			records = new ArrayList<>();
			for (ArrayList<TimeSeriesRecord> instantRecordList : subRecordMap.values()) {
				for (TimeSeriesRecord record : instantRecordList) {
                    if (record.instant.isBefore(start)) {
                        continue;
                    } else if (record.instant.equals(end) || record.instant.isAfter(end)) {
                        break;
                    } else {
                        records.add(record);
                    }
				}
			}
		}
		return records;
	}


	public ArrayList<TimeSeriesRecord> getAllRecords() {
		ArrayList<TimeSeriesRecord> records = new ArrayList<>();
        for (ArrayList<TimeSeriesRecord> instantRecordList : recordMap.values()) {
            for (TimeSeriesRecord record : instantRecordList) {
                records.add(record);
            }
        }
        return records;
	}


    public boolean isEmpty() {
        return recordMap.isEmpty();
    }


	public int getRecordCount() {
        int recordCount = 0;
        for (ArrayList<TimeSeriesRecord> instantRecordList : recordMap.values()) {
            recordCount += instantRecordList.size();
        }

        return recordCount;
	}
}
