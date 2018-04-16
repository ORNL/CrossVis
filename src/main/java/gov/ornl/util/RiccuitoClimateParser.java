package gov.ornl.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class RiccuitoClimateParser {
    public static void main (String args[]) throws IOException {
        String modelInputsFilename = "/Users/csg/Dropbox (ORNL)/projects/SciDAC/data/2018-01-RiccuitoEnsemble/QMCsample.dat";
        String dailyGPPFilename = "/Users/csg/Dropbox (ORNL)/projects/SciDAC/data/2018-01-RiccuitoEnsemble/171211_QMCdaily_US-MOz_ICB20TRCNPRDCTCBC_postprocessed.txt";

        String csvOutputFilename = "/Users/csg/Dropbox (ORNL)/projects/SciDAC/data/2018-01-RiccuitoEnsemble/QMCdaily_US_combined.csv";


        ArrayList<String> csvHeaderList = new ArrayList<>();
        csvHeaderList.add("ensemble");
        csvHeaderList.add("year");
        csvHeaderList.add("day of year");
        csvHeaderList.add("GPP");

        ArrayList<ArrayList<Double>> inputs = new ArrayList<>();

        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvOutputFilename));

        Stream<String> inputsStream = Files.lines(Paths.get(modelInputsFilename));

        inputsStream.forEach((String inputLine) -> {
            if (csvHeaderList.size() == 4) {
                // this is the inputs file header line; parse the variable names and add to header list
                String varNames[] = inputLine.trim().split("\\s+");
                csvHeaderList.addAll(Arrays.asList(varNames));
            } else {
                String valueStrings[] = inputLine.trim().split("\\s+");
                ArrayList<Double> inputValues = new ArrayList<>();
                Arrays.asList(valueStrings).forEach(valueString -> {
                    double value = Double.parseDouble(valueString);
                    inputValues.add(value);
                });
                inputs.add(inputValues);
            }
        });
        inputsStream.close();

        // write output csv file header
        for (int i = 0; i < csvHeaderList.size(); i++) {
            csvWriter.write(csvHeaderList.get(i));
            if ((i + 1) < csvHeaderList.size()) {
                csvWriter.write(",");
            }
        }
        csvWriter.write("\n");

        Stream<String> gppDataStream = Files.lines(Paths.get(dailyGPPFilename));

        int inputIndex = 0;
        Iterator<String> gppDataIterator = gppDataStream.iterator();
        while (gppDataIterator.hasNext()) {
            String lineString = gppDataIterator.next();
            String valueStrings[] = lineString.trim().split(" ");
            int currentDay = 1;
            int currentYear = 1;
            for (String valueString : valueStrings) {
                double value = Double.parseDouble(valueString);
                StringBuffer csvBuffer = new StringBuffer(inputIndex + "," + currentYear + "," + currentDay + "," + value);
                inputs.get(inputIndex).forEach(inputValue -> {
                    csvBuffer.append("," + inputValue);
                });

                csvWriter.write(csvBuffer.toString().trim() + "\n");

                currentDay++;
                if (currentDay > 365) {
                    currentYear++;
                    currentDay = 1;
                }
            }
            inputIndex++;

            System.out.println("Wrote ensemble run " + inputIndex);
        }
        gppDataStream.close();
        csvWriter.close();

        System.out.println("Finished!");
    }
}
