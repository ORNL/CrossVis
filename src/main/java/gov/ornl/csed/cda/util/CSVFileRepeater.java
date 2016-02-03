package gov.ornl.csed.cda.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;

/**
 * This only makes sense for files with null values.  If null values are encountered, the
 * program replaces it with the last known value for the particular variable.
 */
public class CSVFileRepeater {
    public static void main (String args[]) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java CSVFileReader <Source CSV File> <Destination CSV File>");
            System.exit(0);
        }

        File srcCSVFile = new File(args[0]);
        File dstCSVFile = new File(args[1]);

        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(dstCSVFile));

        CSVParser csvParser = new CSVParser(new FileReader(srcCSVFile), CSVFormat.DEFAULT);
        CSVRecord csvHeaderRecord = csvParser.iterator().next();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < csvHeaderRecord.size(); i++) {
            buffer.append(csvHeaderRecord.get(i));
            if (i+1 < csvHeaderRecord.size()) {
                buffer.append(",");
            }
        }
        csvWriter.write(buffer.toString().trim() + "\n");


        ArrayList<String> lastSeenRecords = new ArrayList<>();
        for (int i = 0; i < csvHeaderRecord.size(); i++) {
            lastSeenRecords.add("NaN");
        }

        for (CSVRecord csvRecord : csvParser) {
            buffer = new StringBuffer();
            for (int i = 0; i < csvRecord.size(); i++) {
                String element = csvRecord.get(i).trim();
                if (element.equals("NaN")) {
                    element = lastSeenRecords.get(i);
                } else {
                    lastSeenRecords.set(i, element);
                }
                buffer.append(element);
                if (i+1 < csvRecord.size()) {
                    buffer.append(",");
                }
            }
            csvWriter.write(buffer.toString().trim() + "\n");
        }
        csvParser.close();
        csvWriter.close();
    }
}
