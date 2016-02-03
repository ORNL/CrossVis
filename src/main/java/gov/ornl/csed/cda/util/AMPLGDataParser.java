package gov.ornl.csed.cda.temporaldb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by csg on 9/28/15.
 */
public class AMPLGDataParser {
    private static final Logger log = LoggerFactory.getLogger(AMPLGDataParser.class);
    private static DateTimeFormatter dataTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static HashSet<String> variablesOfInterest = new HashSet<String>(Arrays.asList("OPC.Vacuum.ChamberVacuumGuageFB",
            "OPC.Temperature.ColumnTemperature", "OPC.PowerSupply.HighVoltage.ArcTrip", "Process.RightRegulator.ZeroPulseCounter",
            "Process.LeftRegulator.ZeroPulseCounter", "OPC.Temperature.BottomTemperature", "Analyse.Calculations.Melt[1].ContourLength",
            "Process.RightRegulator.PulseLength", "OPC.PowerSupply.Filament.VoltageFB", "Builds.State.CurrentBuild.CurrentHeight",
            "OPC.Vacuum.TurboPump[0].Current", "OPC.Vacuum.ChamberVacuumGaugeFB", "OPC.Rake.CurrentFeedback",
            "Builds.State.CurrentBuild.LastLayerTime", "OPC.Vacuum.ColumnVaccumGaugeFB", "Builds.State.CurrentBuild.CurrentZLevel",
            "Process.LeftRegulator.PulseLength", "OPC.PowerSupply.Beam.BeamCurrent", "OPC.Table.CurrentFeedback"));

    public static void main (String args[]) throws Exception {
        TreeMap<Instant, ArrayList<AMPLGRecord>> recordMap = new TreeMap<Instant, ArrayList<AMPLGRecord>>();
        HashMap<String, Integer> variableRecordCounts = new HashMap<String, Integer>();

        File plgFile = new File(args[0]);
        File csvFile = new File(args[1]);

        BufferedReader plgReader = new BufferedReader(new FileReader(plgFile));

        String line = plgReader.readLine();
        while (line != null) {
            if (line.startsWith("#")) {
                line = plgReader.readLine();
                continue;
            }

            String tokens[] = line.split("[|]");
            if (tokens.length == 5) {
                // we only keep the variables of interest
                if (variablesOfInterest.contains(tokens[1])) {
                    AMPLGRecord record = new AMPLGRecord();
                    LocalDateTime localDateTime = dataTimeFormatter.parse(tokens[0], LocalDateTime::from);
                    record.instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    record.variableName = tokens[1];
                    record.clock = Long.parseLong(tokens[3]);
                    try {
                        record.value = Double.parseDouble(tokens[4]);
                        ArrayList<AMPLGRecord> instantRecords = recordMap.get(record.instant);
                        if (instantRecords == null) {
                            instantRecords = new ArrayList<AMPLGRecord>();
                            recordMap.put(record.instant, instantRecords);
                        }
                        instantRecords.add(record);

                        if (variableRecordCounts.containsKey(record.variableName)) {
                            int count = variableRecordCounts.get(record.variableName);
                            variableRecordCounts.put(record.variableName, count+1);
                        } else {
                            variableRecordCounts.put(record.variableName, 1);
                        }
                    } catch (NumberFormatException ex) {
                        //                    log.debug("skipped non number " + tokens[4]);
                    }
                }
            }

            line = plgReader.readLine();
        }
        plgReader.close();

        log.debug("Read " + recordMap.size() + " unique instants with numerical values found and " + variableRecordCounts.size() + " variables.");

        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile));

        log.debug("Variable Record Counts:");
        ArrayList<String>variablesList = new ArrayList<String>();
        StringBuffer headerLineBuffer = new StringBuffer();
        headerLineBuffer.append("Time");
        for (String varName : variableRecordCounts.keySet()) {
            variablesList.add(varName);
            int count = variableRecordCounts.get(varName);
            headerLineBuffer.append(", " + varName);
            log.debug(varName + " = " + count);
        }
        String headerLine = headerLineBuffer.toString().trim();
//        headerLine = headerLine.substring(0, headerLine.length()-1);
        csvWriter.write(headerLine + "\n");

        log.debug("Writing data to csv file at " + csvFile);

        int counter = 0;
        for (Instant instant : recordMap.keySet()) {
//        for (ArrayList<AMPLGRecord> records : recordMap.values()) {
            ArrayList<AMPLGRecord> records = recordMap.get(instant);
            if (!records.isEmpty()) {
                double data[] = new double[variableRecordCounts.size()];
                Arrays.fill(data, Double.NaN);
                for (AMPLGRecord plgRecord : records) {
                    int index = variablesList.indexOf(plgRecord.variableName);
                    data[index] = plgRecord.value;
                }

                StringBuffer buff = new StringBuffer();
                buff.append(instant.toEpochMilli());

                for (int i = 0; i < data.length; i++) {
                    buff.append(", " + data[i]);
                }
                csvWriter.write(buff.toString().trim() + "\n");
                counter++;
                if (counter % 1000 == 0) {
                    log.debug(counter + " lines written");
                }
            }
        }
        csvWriter.close();
    }
}
