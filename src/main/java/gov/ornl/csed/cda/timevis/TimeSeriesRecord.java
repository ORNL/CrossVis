package gov.ornl.csed.cda.timevis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

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

	public static void writeRecordsToFile (File file, ArrayList<TimeSeriesRecord> records) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		for (int i = 0; i < records.size(); i++) {
			TimeSeriesRecord record = records.get(i);
			StringBuffer buffer = new StringBuffer();
			String line = record.instant.toEpochMilli() + ", " + record.value;
			if ((i + 1) < records.size()) {
				line += "\n";
			}
			writer.write(line);
		}

		writer.close();
	}
}
