package gov.ornl.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FixTitanTime {
    public static void main (String args[]) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("data/csv/titan-performance.csv"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("data/csv/titan-performance-fixed.csv"));

        String line = reader.readLine();
        writer.write(line + "\n");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm:ss");

        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split(",");
            LocalDateTime localDateTime = LocalDateTime.parse(tokens[0], dateTimeFormatter);

//            String dateString = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//            LocalDateTime test = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String outputLine = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ",";
            for (int i = 1; i < tokens.length; i++) {
                outputLine += tokens[i];
                if ((i + 1) < tokens.length) {
                    outputLine += ",";
                }
            }
            writer.write(outputLine + "\n");
        }

        writer.close();
        reader.close();
    }
}
