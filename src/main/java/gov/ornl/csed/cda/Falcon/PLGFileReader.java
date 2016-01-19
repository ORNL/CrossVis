package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.timevis.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableTuple;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by csg on 1/15/16.
 */
public class PLGFileReader {
    private static final Logger log = LoggerFactory.getLogger(PLGFileReader.class);
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static HashMap<String, TimeSeries> readPLGFile(File plgFile, Collection<String> variables) throws IOException {
        HashMap<String, TimeSeries> variableTimeSeriesMap = new HashMap<>();

        BufferedReader plgFileReader = new BufferedReader(new FileReader(plgFile));

        String line = plgFileReader.readLine();
        while ((line != null)) {
            // skip the first set of variable metadata lines
            if (line.startsWith("#")) {
                line = plgFileReader.readLine();
                continue;
            }

            String tokens[] = line.split("[|]");
            if (tokens.length == 5) {
                String variableName = tokens[1];
                if (variables.contains(variableName)) {
                    try {
                        // store it
                        TimeSeries timeSeries = variableTimeSeriesMap.get(variableName);
                        if (timeSeries == null) {
                            timeSeries = new TimeSeries(variableName);
                            variableTimeSeriesMap.put(variableName, timeSeries);
                        }

                        LocalDateTime localDateTime = dateTimeFormatter.parse(tokens[0], LocalDateTime::from);
                        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                        double value = Double.parseDouble(tokens[4]);
                        timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                    } catch (NumberFormatException ex) {
                        log.debug("Exception caught processing value for variable " + variableName + " (value string = " + tokens[4] + ")");
                    }
                }
            }

            line = plgFileReader.readLine();
        }
        plgFileReader.close();

        log.debug("Read time series for " + variableTimeSeriesMap.size() + " variables");
        log.debug("Time Series: ");
        for (TimeSeries timeSeries : variableTimeSeriesMap.values()) {
            if (timeSeries.getRecordCount() > 1) {
                log.debug(timeSeries.getName() + " - " + timeSeries.getRecordCount() + " records");
            }
        }

        return variableTimeSeriesMap;
    }

    public static HashMap<String, PLGVariableSchema> readVariableSchemas(File plgFile) throws IOException {
        HashMap<String, PLGVariableSchema> variableSchemaMap = new HashMap<>();
//        HashSet<String> typeSet = new HashSet<String>();

        BufferedReader plgFileReader = new BufferedReader(new FileReader(plgFile));

        String line = plgFileReader.readLine();
        while ((line != null)) {
            if (line.startsWith("#-IOItem:")) {
                // parse variable schema
                String tokens[] = line.substring("#IOItem: ".length()).split("[|]");
                PLGVariableSchema schema = new PLGVariableSchema();
                schema.variableName = tokens[0].substring(tokens[0].indexOf("=") + 1);
//                log.debug("variableName: " + variableName);
                schema.typeString = tokens[2].substring(tokens[2].indexOf("=") + 1);
                schema.numValues = 0;
//                typeSet.add(typeString);

//                log.debug(variableName + " " + typeString + " " + axisString);

                variableSchemaMap.put(schema.variableName, schema);
            } else {
                // read variable name and increment num values counter
                String tokens[] = line.split("[|]");
                if (tokens.length >= 2) {
                    String variableName = tokens[1];
                    PLGVariableSchema schema = variableSchemaMap.get(tokens[1]);
                    if (schema != null) {
                        schema.numValues = schema.numValues + 1;
                    }
                }
            }

            line = plgFileReader.readLine();
        }
        plgFileReader.close();

        log.debug("PLG file " + plgFile.getName() + " has " + variableSchemaMap.size() + " variables");
//        log.debug("Found " + typeSet.size() + " different data types");
//        log.debug("Types: " + typeSet.toString());

        return variableSchemaMap;
    }

    public static void main (String args[]) throws IOException {
        File plgFile = new File("/Users/csg/Desktop/AM_data/R1140_2015-01-30_15.06/R1140_2015-01-30_15.06/R1140_2015-01-30_15.06.plg");
        HashMap<String, PLGVariableSchema> variableSchemaMap = readVariableSchemas(plgFile);

        ArrayList<String> variablesToRead = new ArrayList<>();
        for (String variableName : variableSchemaMap.keySet()) {
            PLGVariableSchema schema = variableSchemaMap.get(variableName);
            if (schema.typeString.equals("Int16") ||
                    schema.typeString.equals("Double") ||
                    schema.typeString.equals("Single") ||
                    schema.typeString.equals("Int32")) {
//                    variableType.equals("UInt16") ||
//                    variableType.equals("UInt32")) {
                variablesToRead.add(variableName);
            }
        }

        log.debug("Will attempt to read times series for the following " + variablesToRead.size() + " variables: " + variablesToRead.toString());

        HashMap<String, TimeSeries> variableTimeSeriesList = readPLGFile(plgFile, variablesToRead);


    }
}
