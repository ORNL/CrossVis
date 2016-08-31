package gov.ornl.csed.cda.util;

/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    12 Jul 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  [PARENT CLASS]
 *
 *  Interfaces:     [INTERFACES USED]
 *
 */


/*  PROBLEM DESCRIPTION
making a consistent set of time series records for multiple variables sampled at different instants

- get all time series for all variables from file. no sampling, just using data as recorded in the file
- make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
- iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
- write all data to csv (time written as epoch time in millis)
 */


/*  WHAT I'LL NEED

FOR SAMPLED FILE
- time resolution; sample rate; sample period
- struct to hold the sampled time series
- the start instant
- the end instant

FOR PER LAYER FILE
- time series for the segmenting variable

FOR BOTH
- list of names of variables to pull out of the plg file
- struct to hold the raw time series of all of the desired time series
- a .plg filename
- a .csv filename
-

 */


import gov.ornl.csed.cda.Falcon.*;
import gov.ornl.csed.cda.timevis.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.security.provider.ConfigFile;

import javax.swing.*;
import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlgToCsvParser extends Application {

    private String plgFilename = "";
    private String csvFilename = "";

    private File plgFile = null;
    private File csvFile = null;

    private ArrayList<String> plgDesiredVarNames = new ArrayList<>();
    private String plgSegmentingVarName = "";

    private HashMap<String, TimeSeries> rawTimeSeries;
    private TreeMap<String, TreeMap<Instant, Double>> seriesTreeSet = new TreeMap<>();
    private TreeMap<String, TimeSeries> newTimeSeries = new TreeMap();

    private Instant startInstant;
    private Instant endInstant;

    private static Integer parserOption = null;
    private ParserTypes parserOption_e = null;

    private Long sampleDuration = 1000L;

    /*
    ========================================================================================================================================================================
     */

    private Font fontAwesomeFont;
    private HashMap<TreeItem<String>, FileMetadata> fileTreeItemMetadataMap = new HashMap<>();
    private HashMap<File, FileMetadata> fileMetadataMap = new HashMap<>();
    private TreeItem<String> dataTreeRoot;
    private TreeView<String> dataTreeView;
    private final DataFormat objectDataFormat = new DataFormat("application/x-java-serialized-object");
    private ListView<String> variableListView;

    /*
    ========================================================================================================================================================================
     */

    private enum ParserTypes {
        SAMPLED, LOSSLESS
    }

    public PlgToCsvParser() {

    }


    public PlgToCsvParser(String plgFilename, String csvFilename, String variablesFileName, Long sampleDuration, String plgSegmentingVarName) throws IOException {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;
        this.plgSegmentingVarName = plgSegmentingVarName;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(variablesFileName)));

        String line = bufferedReader.readLine();

        while (line != null) {

            if (!plgDesiredVarNames.contains(line)) {
                plgDesiredVarNames.add(line);
            }

            line = bufferedReader.readLine();
            plgDesiredVarNames.add(line.trim());
        }
    }


    public PlgToCsvParser(String plgFilename, String csvFilename, String variablesFileName, Long sampleDuration) throws IOException {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(variablesFileName)));

        String line = bufferedReader.readLine();

        while (line != null) {

            if (!plgDesiredVarNames.contains(line)) {
                plgDesiredVarNames.add(line);
            }

            line = bufferedReader.readLine();
            if (line != null) {
                plgDesiredVarNames.add(line.trim());

            }
        }
    }

    public PlgToCsvParser(String plgFilename, String csvFilename, Long sampleDuration) {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        plgDesiredVarNames.add("OPC.PowerSupply.Beam.BeamCurrent");
        plgDesiredVarNames.add("OPC.PowerSupply.HighVoltage.Grid");
    }

    public PlgToCsvParser(String plgFilename, String csvFilename, String plgSegmentingVarName) {
        this.plgFilename = plgFilename;
        this.csvFilename = csvFilename;
        this.plgSegmentingVarName = plgSegmentingVarName;

        plgFile = new File(plgFilename);
        csvFile = new File(csvFilename);

        this.sampleDuration = sampleDuration;

        plgDesiredVarNames.add("OPC.PowerSupply.Beam.BeamCurrent");
        plgDesiredVarNames.add("OPC.PowerSupply.HighVoltage.Grid");
    }

    public PlgToCsvParser(File plgFile, File csvFile, ArrayList<String> plgDesiredVarNames, Long sampleDuration) {
        this.plgFile = plgFile;
        this.csvFile = csvFile;

        this.sampleDuration = sampleDuration;

        this.plgDesiredVarNames = plgDesiredVarNames;

    }


    public void parsePerSampleData() {

        // - get all time series for all variables from file. no sampling, just using data as recorded in the file
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newTimeSeries = convertRawToSampledTimeSeries(rawTimeSeries, sampleDuration);

        // test to see if output is correct
        // print out the raw time series
//        System.out.println();
//        for (TimeSeriesRecord record : rawTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }

        // print out the sampled time series
//        System.out.println();
//        for (TimeSeriesRecord record : newTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }


        // - write all data to csv (time written as epoch time in millis)
        try {
            writeSampledTimeSeriesToCsv(newTimeSeries, csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void parseLosslessData() {

        // - get all time series for all variables from file. no sampling, just using data as recorded in the file
        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newTimeSeries = convertRawToLosslessTimeSeries(rawTimeSeries);

        // test to see if output is correct
        // print out the raw time series
//        System.out.println();
//        for (TimeSeriesRecord record : rawTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }

        // print out the sampled time series
//        System.out.println();
//        for (TimeSeriesRecord record : newTimeSeries.get("OPC.PowerSupply.Beam.BeamCurrent").getAllRecords()) {
//            System.out.println(record.instant + " : " + record.value);
//        }


        // - write all data to csv (time written as epoch time in millis)
        try {
            writeSampledTimeSeriesToCsv(newTimeSeries, csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parsePerLayerData() {

        try {
            rawTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, plgDesiredVarNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> dummy = new ArrayList<>();
        dummy.add(plgSegmentingVarName);
        HashMap<String, TimeSeries> segmentingTimeSeries = null;
        try {
            segmentingTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, dummy);
        } catch (IOException e) {
            e.printStackTrace();
        }

        newTimeSeries = convertRawToPerLayerData(rawTimeSeries, segmentingTimeSeries);

        try {
            writeSampledTimeSeriesToCsv(newTimeSeries, csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public TreeMap<String, TimeSeries> convertRawToSampledTimeSeries(HashMap<String, TimeSeries> rawTimeSeries, Long sampleDuration) {
        TreeMap <String, TimeSeries> timeSeries = new TreeMap<>();

        // initialize the start and end instants
        TimeSeries[] timeSeriesArr = new TimeSeries[rawTimeSeries.entrySet().size()];
        timeSeriesArr = rawTimeSeries.values().toArray(timeSeriesArr);

        startInstant = timeSeriesArr[0].getStartInstant();
        endInstant = timeSeriesArr[0].getEndInstant();

        // - make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
        for (Map.Entry<String, TimeSeries> entry : rawTimeSeries.entrySet()) {
            TreeMap<Instant, Double> temp = new TreeMap<>();

            // update the overall start and end instants if necessary
            if (entry.getValue().getStartInstant().isBefore(startInstant)) {
                startInstant = entry.getValue().getStartInstant();
            }

            if (entry.getValue().getEndInstant().isAfter(endInstant)) {
                endInstant = entry.getValue().getEndInstant();
            }

            for (TimeSeriesRecord record : entry.getValue().getAllRecords()) {
                temp.put(record.instant, record.value);
            }

            seriesTreeSet.put(entry.getKey(), temp);
        }

        Long comparison = endInstant.toEpochMilli() - startInstant.toEpochMilli();
//        System.out.println(startInstant);
//        System.out.println(endInstant);
//        System.out.println(comparison);
//
//        System.out.println(Instant.ofEpochMilli(comparison));

        // - iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
        for (Map.Entry<String, TreeMap<Instant, Double>> entry : seriesTreeSet.entrySet()) {
            String key = entry.getKey();
            TreeMap<Instant, Double> value = entry.getValue();
            TimeSeries temp = new TimeSeries(key + "_sampled");

            Instant sampleInstant = startInstant;

            while (sampleInstant.isBefore(endInstant)) {

                temp.addRecord(sampleInstant, value.floorEntry(sampleInstant).getValue(), Double.NaN, Double.NaN);

                sampleInstant = sampleInstant.plusMillis(sampleDuration);
            }

            timeSeries.put(key, temp);
        }

        return timeSeries;
    }


    public TreeMap<String, TimeSeries> convertRawToLosslessTimeSeries(HashMap<String, TimeSeries> rawTimeSeries) {
        TreeMap <String, TimeSeries> timeSeries = new TreeMap<>();
        TreeMap <String, TreeMap <Instant, Double>> varValues = new TreeMap<>();

        ArrayList <Instant> instants = new ArrayList<>();

        for (Map.Entry <String, TimeSeries> series : rawTimeSeries.entrySet()) {

            TreeMap <Instant, Double> temp = new TreeMap<>();

            for (TimeSeriesRecord record : series.getValue().getAllRecords()) {

                temp.put(record.instant, record.value);

                if (!instants.contains(record.instant)) {
                    instants.add(record.instant);
                }
            }

            varValues.put(series.getKey(), temp);
        }

        Collections.sort(instants);

        System.out.println(instants.get(0).toEpochMilli());
        System.out.println(instants.get(instants.size()-1).toEpochMilli());

        for (Map.Entry <String, TimeSeries> entry : rawTimeSeries.entrySet()) {

            TimeSeries temp = new TimeSeries(entry.getKey() + "_lossless");

            for (int i = 0; i < instants.size(); i++) {

                Instant instant = instants.get(i);

                temp.addRecord(instant, varValues.get(entry.getKey()).floorEntry(instant).getValue(), Double.NaN, Double.NaN);
//                System.out.println(instant.toEpochMilli() + " : " + varValues.get("OPC.PowerSupply.Beam.BeamCurrent").floorEntry(instant).getValue());
            }

            timeSeries.put(entry.getKey(), temp);

        }
//        // initialize the start and end instants
//        TimeSeries[] timeSeriesArr = new TimeSeries[rawTimeSeries.entrySet().size()];
//        timeSeriesArr = rawTimeSeries.values().toArray(timeSeriesArr);
//
//        startInstant = timeSeriesArr[0].getStartInstant();
//        endInstant = timeSeriesArr[0].getEndInstant();
//
//        // - make a treeset and put all instants for all timeseries data into the set. duplicates should not be an issue
//        for (Map.Entry<String, TimeSeries> entry : rawTimeSeries.entrySet()) {
//            TreeMap<Instant, Double> temp = new TreeMap<>();
//
//            // update the overall start and end instants if necessary
//            if (entry.getValue().getStartInstant().isBefore(startInstant)) {
//                startInstant = entry.getValue().getStartInstant();
//            }
//
//            if (entry.getValue().getEndInstant().isAfter(endInstant)) {
//                endInstant = entry.getValue().getEndInstant();
//            }
//
//            for (TimeSeriesRecord record : entry.getValue().getAllRecords()) {
//                temp.put(record.instant, record.value);
//            }
//
//            seriesTreeSet.put(entry.getKey(), temp);
//        }
//
//        Long comparison = endInstant.toEpochMilli() - startInstant.toEpochMilli();
//        System.out.println(startInstant);
//        System.out.println(endInstant);
//        System.out.println(comparison);
//
//        System.out.println(Instant.ofEpochMilli(comparison));
//        // - iterate over all elements in the set. for each instant get a value for each variable. if variable has recorded value(s) use it/them; if variable doesn't have a value at current instant get value with greatest instant that is less than current value (floor record). store values.
//        for (Map.Entry<String, TreeMap<Instant, Double>> entry : seriesTreeSet.entrySet()) {
//            String key = entry.getKey();
//            TreeMap<Instant, Double> value = entry.getValue();
//            TimeSeries temp = new TimeSeries(key + "_sampled");
//
//            Instant sampleInstant = startInstant;
//
//            while (sampleInstant.isBefore(endInstant)) {
//
//                temp.addRecord(sampleInstant, value.floorEntry(sampleInstant).getValue(), Double.NaN, Double.NaN);
//
//                sampleInstant = sampleInstant.plusMillis(sampleDuration);
//            }
//
//            timeSeries.put(key, temp);
//        }

        return timeSeries;
    }


    public TreeMap<String,TimeSeries> convertRawToPerLayerData(HashMap<String, TimeSeries> rawTimeSeries, HashMap<String, TimeSeries> segmentingTimeSeriesHashMap) {
        TreeMap<String, TimeSeries> timeSeries = new TreeMap<>();

        TimeSeries segmentingTimeSeries = segmentingTimeSeriesHashMap.get(plgSegmentingVarName);

        Instant nextInstant = null;
        Instant currentInstant = null;

        // iterate through all of the records of the segmenting time series
        for (TimeSeriesRecord segmentingRecord : segmentingTimeSeries.getAllRecords()) {

            nextInstant = segmentingRecord.instant;
            Long buildHeightTime;

            // Once we have a current and next instant
            if (currentInstant != null) {

                // cycle through all of the desired variables
                ArrayList<TimeSeriesRecord> temp;

                buildHeightTime = nextInstant.toEpochMilli() - currentInstant.toEpochMilli();

                for (Map.Entry<String, TimeSeries> entry : rawTimeSeries.entrySet()) {
                    temp = entry.getValue().getRecordsBetween(currentInstant, nextInstant);

                    if (temp == null) {
                        // TODO: 7/26/16 - add an empty entry?
                        // or have to find the last available value and set that
                        continue;
                    }

                    Long recordDuration;
                    Double average = 0.;
                    Instant lastRecord = currentInstant;

                    // iterate through all of the records and calculate weighted average
                    for (int i = 0; i < temp.size(); i++) {
                        TimeSeriesRecord record = temp.get(i);
                        recordDuration = lastRecord.toEpochMilli() - record.instant.toEpochMilli();

                        average += record.value * recordDuration / buildHeightTime;

                        lastRecord = record.instant;
                    }

                    // add the average to the correct time series
                    if (!timeSeries.containsKey(entry.getKey())) {
                        timeSeries.put(entry.getKey(), new TimeSeries(entry.getKey()));
                    }

                    timeSeries.get(entry.getKey()).addRecord(currentInstant, average, Double.NaN, Double.NaN);
                }
            }

            currentInstant = nextInstant;
        }

        // catch the trailing end of the series if any

        return timeSeries;
    }


    public void writeSampledTimeSeriesToCsv(TreeMap<String, TimeSeries> sampledTimeSeries, File csvFile) throws IOException {

        String rowBuffer = "Time";

        // open the csv file for writing
        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile));

        // write out the headings
        for (Map.Entry<String, TimeSeries> entry : sampledTimeSeries.entrySet()) {
            rowBuffer += "," + entry.getKey();
        }

//        System.out.println(rowBuffer);
        csvWriter.write(rowBuffer.trim() + "\n");

        System.out.println(sampledTimeSeries.firstEntry().getValue().getEndInstant().toEpochMilli());

        // build the row
        Integer dummy = sampledTimeSeries.firstEntry().getValue().getAllRecords().size();
        for (TimeSeriesRecord record : sampledTimeSeries.firstEntry().getValue().getAllRecords()) {

            rowBuffer = String.valueOf( record.instant.toEpochMilli() );
//            System.out.println(rowBuffer);
//            System.out.println(Instant.ofEpochMilli( Long.parseLong(rowBuffer) ));
//            System.out.println(record.instant);

            for (TimeSeries series : sampledTimeSeries.values()) {
                rowBuffer += "," + series.getRecordsAt(record.instant).get(0).value;
            }

//            System.out.println(rowBuffer);
            csvWriter.write(rowBuffer.trim() + "\n");
        }
        csvWriter.close();

    }

    public static void main(String[] args) {

        // default values if
        // TODO: 7/14/16 - fix this message
        String usage =  "PlgToCsvParser Usage\n" +
                        "====================\n" +
                        "\n" +
                        "PlgToCsvParser  [Parser Type]  {PLG file path}.plg  {CSV file path}.csv  {Variables names file path}.txt  [Sample Duration in ms]\n\n" +
                        "Parser Type - 1: Features constructed by regularly sampling PLG values, 2: Features constructed every time a value is updated\n" +
                        "PLG file path - Full path to the desired PLG input file\n" +
                        "CSV file path - Full path to the desired CSV output file\n" +
                        "Variables name file path - Full path to text file containing desired variable names; one variable name per line\n" +
                        "Sample Duration in ms - Duration in between regular sampling. Must be a whole number. This value is disregarded for Parser Type 2\n";

        String plgFileName = "/Users/whw/ORNL Internship/New Build Data/29-6-2016/For William/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1 LogFiles/R1119_2016-06-14_19.09_20160614_Q10_DEHOFF_ORNL TEST ARTICACT 1.plg";
//        String csvFileName = "/Users/whw/ORNL Internship/test_perSample.csv";
//        String csvFileName = "/Users/whw/ORNL Internship/test_lossless.csv";
        String csvFileName = "/Users/whw/ORNL Internship/test_perLayer.csv";
        String variablesFileName = "";
        Long sampleDuration = 1000L;

        PlgToCsvParser parser = null;

        if (args.length == 5) {
            parserOption = Integer.parseInt(args[0]);
            plgFileName = args[1];
            csvFileName = args[2];
            variablesFileName = args[3];
            sampleDuration = Long.valueOf(args[4]);

            try {
                parser = new PlgToCsvParser(plgFileName, csvFileName, variablesFileName, sampleDuration);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            launch(args);
            System.exit(0);

        }

        if (parser != null) {
//            parser.parsePerSampleData();
            if (parserOption == 1) {
                parser.parsePerSampleData();
            } else if (parserOption == 2) {
                parser.parseLosslessData();
            } else {
                parser.parsePerLayerData();
            }

        }

        System.exit(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        InputStream is = FalconMain.class.getResourceAsStream("fontawesome-webfont.ttf");
        fontAwesomeFont = javafx.scene.text.Font.loadFont(is, 14);
        createDataTreeView();

        // create the menu bar
        Menu plgToCsvParserMenu = new Menu("P2C Parser");
        MenuItem aboutPlgToCsvParserMenuItem = new MenuItem("About");
        aboutPlgToCsvParserMenuItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("About P2C Parser");
            String s = "P2C Parser is developed and maintained by the Computational Data Analytics Group \nat Oak Ridge National Laboratory.\n\nThe lead developer is William Halsey\n\n\u00a9 2015 - 2016";
            alert.setContentText(s);
            alert.show();
        });
        
        plgToCsvParserMenu.getItems().addAll(aboutPlgToCsvParserMenuItem);

        Menu fileMenu = new Menu("File");
        MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose a PLG File to Open");
            plgFile = fileChooser.showOpenDialog(primaryStage);

            try {
                openPLGFile(plgFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        MenuItem saveTemplateMenuItem = new MenuItem("Save Template");
        saveTemplateMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setTitle("Select View Template File");
            File templateFile = fileChooser.showSaveDialog(primaryStage);
            if (!templateFile.getName().endsWith(".vtf")) {
                templateFile = new File(templateFile.getAbsolutePath() + ".vtf");
            }
            if (templateFile != null) {
                try {
                    saveViewTemplate(templateFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        MenuItem loadTemplateMenuItem = new MenuItem("Load Template");
        loadTemplateMenuItem.setOnAction(e -> {
            // if timeseries are being shown, ask user if they want to clear the display
            if (variableListView.getItems().size() != 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Reset Display");
                alert.setHeaderText("Clear existing visualization panels?");
                alert.setContentText("Choose your preference");

                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.YES) {
                    variableListView.getItems().removeAll(variableListView.getItems());
                }
            }

            // get the template file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setTitle("Load View Template File");
            File templateFile = fileChooser.showOpenDialog(primaryStage);
            if (templateFile != null) {
                try {
                    loadViewTemplate(templateFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> {
            primaryStage.close();
        });
        
        fileMenu.getItems().addAll(openMenuItem, new SeparatorMenuItem(), saveTemplateMenuItem, loadTemplateMenuItem, new SeparatorMenuItem(), exitMenuItem);

        MenuBar menuBar = new MenuBar(plgToCsvParserMenu, fileMenu);

        // create the primitive components
//        TreeView<String> variableListViewer = new TreeView<String>();

        Button rmButton = new Button("–");
        rmButton.setOnAction(e -> {
            variableListView.getItems().remove(variableListView.getSelectionModel().getSelectedIndex());
        });

        Spinner<Integer> sampleDurationSpinner = new Spinner<>(1000, 1000000, 1000, 1);
        sampleDurationSpinner.setEditable(true);

        sampleDurationSpinner.setVisible(false);

        ChoiceBox<String> parserChooser = new ChoiceBox<>();
        for (int i = 0; i < ParserTypes.values().length; i++) {
            parserChooser.getItems().add(String.valueOf(ParserTypes.values()[i]));
        }
        parserChooser.setValue(String.valueOf(ParserTypes.LOSSLESS));
        parserChooser.setOnAction(e -> {
            parserOption_e = ParserTypes.valueOf(parserChooser.getValue());

            if (parserOption_e == ParserTypes.SAMPLED) {
                sampleDurationSpinner.setVisible(true);
            } else {
                sampleDurationSpinner.setVisible(false);
            }
        });

        Button parserButton = new Button("Parse");
        parserButton.setDisable(true);
        parserButton.setOnAction(e -> {
            if (plgFile != null && csvFile != null) {
                // do the conversion
                this.sampleDuration = sampleDurationSpinner.getValue().longValue();
                for (String item : variableListView.getItems()) {
                    this.plgDesiredVarNames.add(item);
                }

                if (parserChooser.getValue().equals(ParserTypes.LOSSLESS.toString())) {
                    System.out.println("parsing lossless data");

                    this.parseLosslessData();

                } else if (parserChooser.getValue().equals(ParserTypes.SAMPLED.toString())) {
                    System.out.println("parsing sampled data");

                    this.parsePerSampleData();

                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Status");
                alert.setHeaderText("DONE");
                alert.show();
            }

        });

        Button saveAsButton = new Button("Save As...");
        saveAsButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File to Save to");
            csvFile = fileChooser.showSaveDialog(primaryStage);

            if (!csvFile.getName().endsWith(".csv")) {
                csvFile = new File(csvFile.getAbsolutePath() + ".csv");
            }

            parserButton.setDisable(false);
        });

        variableListView = new ListView<>();
//        variableListView.setOnDragDropped(e -> {
//            // TODO: 8/26/16
//        });

        // group the primitives into the scene
        HBox buttonGroup = new HBox(rmButton, saveAsButton, parserButton);
        buttonGroup.setSpacing(3D);

        VBox rootRightPanel = new VBox(variableListView, buttonGroup, parserChooser, sampleDurationSpinner);
        rootRightPanel.setSpacing(3D);

        HBox components = new HBox(dataTreeView, rootRightPanel);
        components.setSpacing(3D);

        VBox root = new VBox(menuBar, components);
        root.setSpacing(3D);

        // create the scene and add the root
        Scene scene = new Scene(root);

        // add the scene to the stage and show
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /* Tree View Code
    ========================================================================================================================================================================
     */

    private void openPLGFile(File plgFile) throws IOException {
        HashMap<String, PLGVariableSchema> tmp = PLGFileReader.readVariableSchemas(plgFile);

        TreeMap<String, PLGVariableSchema> variableSchemaMap = new TreeMap<>();

        for (Map.Entry<String, PLGVariableSchema> entry : tmp.entrySet()) {
            variableSchemaMap.put(entry.getKey(), entry.getValue());
        }

        // populate data tree view
        Text itemIcon = new Text("\uf1c0");
        itemIcon.setFont(fontAwesomeFont);
        itemIcon.setFontSmoothingType(FontSmoothingType.LCD);
        TreeItem<String> fileTreeItem = new TreeItem<>(plgFile.getName(), itemIcon);
        FileMetadata fileMetadata = new FileMetadata(plgFile);
        fileMetadata.fileType = FileMetadata.FileType.PLG;
        fileTreeItemMetadataMap.put(fileTreeItem, fileMetadata);
        fileMetadataMap.put(plgFile, fileMetadata);

        for (PLGVariableSchema schema : variableSchemaMap.values()) {

            if (schema.typeString.equals("Int16") ||
                    schema.typeString.equals("Double") ||
                    schema.typeString.equals("Single") ||
                    schema.typeString.equals("Int32")) {
                if (schema.numValues > 0) {
                    fileMetadata.variableList.add(schema.variableName);
                    fileMetadata.variableValueCountList.add(schema.numValues);

                    String tokens[] = schema.variableName.split("[.]");

                    TreeItem<String> parentTreeItem = fileTreeItem;
                    String compoundItemName = "";
                    for (int i = 0; i < tokens.length; i++) {
                        TreeItem<String> treeItem = null;

                        // if an item already exists for this token, use it
                        for (TreeItem<String> item : parentTreeItem.getChildren()) {
                            if (item.getValue().equals(tokens[i])) {
                                treeItem = item;
                                break;
                            }
                        }

                        // item doesn't exist for this token so create it
                        if (treeItem == null) {
                            treeItem = new TreeItem<>(tokens[i]);
                            parentTreeItem.getChildren().add(treeItem);
                        }

                        // update parent item
                        parentTreeItem = treeItem;
                    }
                }
            }
        }

        dataTreeRoot.getChildren().addAll(fileTreeItem);
    }

    private void createDataTreeView() {
        dataTreeView = new TreeView<>();
        dataTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                final TreeCell<String> treeCell = new TreeCell<String>() {
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            setGraphic(getTreeItem().getGraphic());
                            Tooltip tooltip = getTooltipForCell(this);
                            setTooltip(tooltip);
                        }
                    }
                };

                treeCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            TreeItem<String> treeItem = treeCell.getTreeItem();
                            if (treeItem.isLeaf()) {
                                FileMetadata fileMetadata = getFileMetadataForTreeItem(treeItem);
                                String variableName = getFullTreeItemName(treeItem);

                                variableListView.getItems().add(variableName);
                            }
                        }
                    }
                });

//                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        TreeItem<String> treeItem = treeCell.getTreeItem();
//                        // we can only drag and drop leaf nodes in the tree
//                        // the leaves are the full variable names
//                        // nonleaf nodes are file nodes or partial variable names
//                        // TODO: when a nonleaf is dragged add all child variables
//                        if (treeItem.isLeaf()) {
//                            VariableClipboardData variableClipboardData = treeItemToVariableClipboardData(treeItem);
//
//                            Dragboard db = treeCell.startDragAndDrop(TransferMode.COPY);
//                            ClipboardContent content = new ClipboardContent();
//                            content.put(objectDataFormat, variableClipboardData);
//                            db.setContent(content);
//                            event.consume();
//                            Label label = new Label(String.format("Visualize %s", variableClipboardData.getVariableName()));
//                            new Scene(label);
//                            db.setDragView(label.snapshot(null, null));
//                        }
//                    }
//                });

                return treeCell;
            }
        });

        dataTreeRoot = new TreeItem<String>();
        dataTreeView.setRoot(dataTreeRoot);
        dataTreeView.setShowRoot(false);
    }

    private FileMetadata getFileMetadataForTreeItem(TreeItem<String> treeItem) {
        TreeItem<String> parentItem = treeItem;
        while (parentItem != null) {
            FileMetadata fileMetadata = fileTreeItemMetadataMap.get(parentItem);
            if (fileMetadata != null) {
                return fileMetadata;
            }

            parentItem = parentItem.getParent();
        }

        return null;
    }

    private Tooltip getTooltipForCell(TreeCell<String> treeCell) {
        Tooltip tooltip = new Tooltip();

        TreeItem<String> treeItem = treeCell.getTreeItem();

        // build full variable name
        String fullVarName = treeItem.getValue();
        TreeItem<String> parent = treeItem.getParent();
        FileMetadata fileMetadata = null;
        while(parent != null) {
            if (fileTreeItemMetadataMap.containsKey(parent)) {
                fileMetadata = fileTreeItemMetadataMap.get(parent);
                break;
            }
            fullVarName = parent.getValue() + "." + fullVarName;
            parent = parent.getParent();
        }

        String tooltipText = fullVarName;
        if (treeItem.isLeaf()) {
            // the item represents a variable
            // show full name and number of values in tooltip
            int idx = fileMetadata.variableList.indexOf(fullVarName);
            if (idx != -1) {
                tooltipText += " (" + fileMetadata.variableValueCountList.get(idx) + " values)";
            }
        }

        tooltip.setText(tooltipText);
        return tooltip;
    }

    private String getFullTreeItemName(TreeItem<String> treeItem) {
        String variableName = treeItem.getValue();

        TreeItem<String> parentItem = treeItem.getParent();
        while (parentItem != null && !fileTreeItemMetadataMap.containsKey(parentItem)) {
            variableName = parentItem.getValue() + "." + variableName;
            parentItem = parentItem.getParent();
        }

        return variableName;
    }

    private VariableClipboardData treeItemToVariableClipboardData(TreeItem<String> treeItem) {
        String variableName = null;

        TreeItem<String> currentTreeItem = treeItem;
        while (currentTreeItem.getParent() != null) {
            if (variableName == null) {
                variableName = currentTreeItem.getValue();
            } else {
                variableName = currentTreeItem.getValue() + "." + variableName;
            }

            currentTreeItem = currentTreeItem.getParent();
            if (fileTreeItemMetadataMap.containsKey(currentTreeItem)) {
                FileMetadata fileMetadata = fileTreeItemMetadataMap.get(currentTreeItem);

                VariableClipboardData variableClipboardData = new VariableClipboardData(fileMetadata.file, fileMetadata.fileType,
                        variableName);
                return variableClipboardData;
            }
        }

        return null;
    }

    private void saveViewTemplate(File f) throws IOException {
        Properties properties = new Properties();

        // get variables in the view now
        ArrayList<String> variableNames = new ArrayList<>();

        for (String item : variableListView.getItems()) {
            variableNames.add(item);
        }

        StringBuffer variableNamesBuffer = new StringBuffer();
        for (int i = 0; i < variableNames.size(); i++) {
            String variableName = variableNames.get(i).substring(variableNames.get(i).indexOf(":") + 1);
            if ((i + 1) < variableNames.size()) {
                variableNamesBuffer.append(variableName + ",");
            } else {
                variableNamesBuffer.append(variableName);
            }
        }

        properties.setProperty(FalconViewTemplateKeys.VARIABLES, variableNamesBuffer.toString().trim());

        OutputStream outputStream = new FileOutputStream(f);
        properties.store(outputStream, null);

    }

    private void loadViewTemplate(File f) throws IOException {
        ArrayList<FileMetadata> targetFiles = new ArrayList<>();
        if (fileMetadataMap.size() > 1) {
//            // prompt user to specify which of the opened files to apply the template settings to
//            Dialog<ObservableList<File>> filesDialog = new Dialog<>();
//            filesDialog.setHeaderText("Template Target Selection");
////            filesDialog.setContentText("Choose Open File(s) to Apply Template");
//
//            // Set the button types
//            filesDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
//
//            BorderPane borderPane = new BorderPane();
//
//            ObservableList<File> filenames = FXCollections.observableArrayList();
//            for (FileMetadata fileMetadata : fileMetadataMap.values()) {
//                filenames.add(fileMetadata.file);
//            }
//
//            ListView<File> fileListView = new ListView<>(filenames);
//            fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            borderPane.setCenter(fileListView);
//            borderPane.setMaxHeight(200);
//            borderPane.setMinWidth(400);
//
//            filesDialog.getDialogPane().setContentText("Choose Open File(s) to Apply Template");
////            filesDialog.getDialogPane().setExpandableContent(borderPane);
//            filesDialog.getDialogPane().setContent(borderPane);
////            filesDialog.getDialogPane().getScene().getWindow().sizeToScene();
////            filesDialog.getDialogPane().setMinWidth(500);
////            filesDialog.getDialogPane().setMaxHeight(200);
//
//            // Request focus on the username field by default.
//            Platform.runLater(() -> fileListView.requestFocus());
//
//            // Convert the result to a username-password-pair when the login button is clicked.
//            filesDialog.setResultConverter(dialogButton -> {
//                if (dialogButton == ButtonType.APPLY) {
//                    return fileListView.getSelectionModel().getSelectedItems();
//                }
//                return null;
//            });
//
//            filesDialog.setResizable(true);
//            Optional<ObservableList<File>> result = filesDialog.showAndWait();
//
//            if (result.isPresent()) {
//                for (File file : result.get()) {
//                    targetFiles.add(fileMetadataMap.get(file));
//                }
//            }
        } else if (fileMetadataMap.size() == 1) {
            targetFiles.addAll(fileMetadataMap.values());
        } else if (fileMetadataMap.isEmpty()) {
            // TODO: Maybe show a error message here?
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(f));

        // get variables and load each one into the view
        String variablesString = properties.getProperty(FalconViewTemplateKeys.VARIABLES);
        if (variablesString != null && !(variablesString.trim().isEmpty())) {
            String variableNames[] = variablesString.split(",");
            for (FileMetadata fileMetadata : targetFiles) {
                for (String variableName : variableNames) {
                    // read variable time series from file
                    loadColumnIntoMultiView(fileMetadata, variableName);
                }
            }
        }
    }

    private void loadColumnIntoMultiView (FileMetadata fileMetadata, String variableName) {
        try {
            ArrayList<String> variableList = new ArrayList<>();
            variableList.add(variableName);
//            Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);

            Map<String, PLGVariableSchema> schemaMap = PLGFileReader.readVariableSchemas(fileMetadata.file);

            if (!schemaMap.containsKey(variableName)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Variable Read Error");
                alert.setHeaderText("Variable not found in file");
                alert.setContentText("The variable '" + variableName + "' was not found in the file '" + fileMetadata.file.getName() + "'");
                alert.showAndWait();
                variableList.remove(variableName);
//                return;
            }
            for (int i = 0; i < variableList.size(); i++) {
                variableListView.getItems().add(variableList.get(i));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
