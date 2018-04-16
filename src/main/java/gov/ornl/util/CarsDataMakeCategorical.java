package gov.ornl.util;

import java.io.*;

public class CarsDataMakeCategorical {

    public static void main (String args[]) throws Exception {
        File inputFile = new File("data/csv/cars.csv");
        File outputFile = new File("data/csv/cars-cat.csv");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        String line = reader.readLine();
        writer.write(line + "\n");

        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split(",");
            for (int i = 0; i < tokens.length; i++) {
                String token = tokens[i].trim();
                if (i == (tokens.length - 1)) {
                    int originID = (int)Float.parseFloat(token);

                    if (originID == 1) {
                        writer.write("American");
                    } else if (originID == 2) {
                        writer.write("European");
                    } else {
                        writer.write("Japanese");
                    }
                } else {
                    double value = Double.parseDouble(token);
                    writer.write(String.valueOf(value));
                }

                if ((i+1) < tokens.length) {
                    writer.write(",");
                }
            }
            writer.write("\n");
        }

        reader.close();
        writer.close();
    }
}
