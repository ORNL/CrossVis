package gov.ornl.csed.cda.Falcon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by csg on 2/22/16.
 */
public class PLGReader {
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static TreeMap<Instant, ArrayList<Double>> readTimeSeriesAsMap(File f, String variableName) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(f));

        TreeMap<Instant, ArrayList<Double>> timeDataMap = new TreeMap<>();

        int lineCounter = 0;
        String line = fileReader.readLine();
        while (line != null) {
            // skip the lines that start with '#'
            // these are the variable metadata / schema lines
            if (line.startsWith("#")) {
                line = fileReader.readLine();
                lineCounter++;
                continue;
            }

            // split the line string using the '|' token delimiter
            String tokens[] = line.split("[|]");
            if (tokens.length == 5) {
                // check if this is the variable we are looking for
                if (variableName.equals(tokens[1])) {
                    try {
                        // parse the date-time field and convert to a Java Instant
                        LocalDateTime localDateTime = dateTimeFormatter.parse(tokens[0], LocalDateTime::from);
                        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

                        // parse the value for the variable
                        double value = Double.parseDouble(tokens[4]);

                        // see if we already have an entry for the instant
                        ArrayList<Double> instantValues = timeDataMap.get(instant);
                        if (instantValues == null) {
                            // an arraylist does not exist for this instant so create a new one
                            instantValues = new ArrayList<>();
                            timeDataMap.put(instant, instantValues);
                        } else {
                            System.out.println(tokens[0] + " - " + value + ", " + instantValues.get(0));
                        }

                        // store the new value
                        instantValues.add(value);
                    } catch (NumberFormatException ex) {
                        System.err.println("Exception caught while processing value for variable " + variableName + " (value string = " + tokens[4] + ")");
                    }
                }
            }

            // read the next line
            line = fileReader.readLine();
            lineCounter++;
        }

        // clean up and return results
        fileReader.close();
        return timeDataMap;
    }

    public static void main (String args[]) throws Exception {
        File plgFile = new File("/Users/csg/Desktop/AM_data/R1140_2015-01-30_15.06/R1140_2015-01-30_15.06/R1140_2015-01-30_15.06.plg");
//        File plgFile = new File("/Users/csg/Desktop/AM_data/R1057_2014-09-16_9.19_20140916_M1_AIR FORCE _BUILD G/R1057_2014-09-16_9.19_20140916_M1_AIR FORCE _BUILD G.plg");
//        File plgFile = new File("/Users/csg/Desktop/AM_data/R1057_2014-11-07_15.14_20141107_A2_AIRFORCE_BUILD H.Folder1/R1057_2014-11-07_15.14_20141107_A2_AIRFORCE_BUILD H.plg");

        String beamCurrentVariableName = "OPC.PowerSupply.Beam.BeamCurrent";
        String buildHeightVariableName = "Builds.State.CurrentBuild.CurrentHeight";

        System.out.println("Reading '" + beamCurrentVariableName + "' from file '" + plgFile.getName());
        TreeMap<Instant, ArrayList<Double>> instantBeamCurrentDataMap = readTimeSeriesAsMap(plgFile, beamCurrentVariableName);
        TreeMap<Instant, ArrayList<Double>> instantBuildHeightDataMap = readTimeSeriesAsMap(plgFile, buildHeightVariableName);

        // Tree Map to organize values for each build height (segmented time series)
        TreeMap<Double, Map<Instant, ArrayList<Double>>> heightValueMap = new TreeMap<>();

        double currentBuildHeightValue = Double.NaN;
        Instant currentBuildHeightStartInstant = null;
        for (Map.Entry<Instant, ArrayList<Double>> buildHeightMapEntry : instantBuildHeightDataMap.entrySet()) {
            if (currentBuildHeightStartInstant != null) {
                Instant currentBuildHeightEndInstant = buildHeightMapEntry.getKey();
                // get beam current values for time range of current build height
                NavigableMap<Instant, ArrayList<Double>> heightSubMap = instantBeamCurrentDataMap.subMap(currentBuildHeightStartInstant,
                        true, currentBuildHeightEndInstant, false);
                heightValueMap.put(currentBuildHeightValue, heightSubMap);
            }

            // set current values and continue
            currentBuildHeightStartInstant = buildHeightMapEntry.getKey();
            ArrayList<Double> heightValueList = buildHeightMapEntry.getValue();
            if (heightValueList.size() == 1) {
                currentBuildHeightValue = heightValueList.get(0);
            } else {
                System.err.println("I found 2 heights at the first entry?");
                System.exit(0);
            }
        }

        for (double height : heightValueMap.keySet()) {
            System.out.println("Height: " + height);
            Map<Instant, ArrayList<Double>> valueMap = heightValueMap.get(height);
            for (Instant instant : valueMap.keySet()) {
                ArrayList<Double> values = valueMap.get(instant);
                for (double value : values) {
                    System.out.println(instant.toString() + " - " + value);
                }
            }
        }
    }
}
