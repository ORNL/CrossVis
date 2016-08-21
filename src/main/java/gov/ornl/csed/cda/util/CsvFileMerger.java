package gov.ornl.csed.cda.util;

/*
 *
 *  Class:  [CLASS NAME]
 *
 *      Author:     whw
 *
 *      Created:    10 Aug 2016
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



/*
WHAT I'M GOING TO DO

    √ check to see if the appropriate number of CLA
    - read in CLA
    - read in the two csv files
    ? input about which columns (one in each file) to key off of
    - for each line in the appendee file
        - check the value of the key column
        - pull the corresponding row from the appender file
        √ make sure that multiple feature vectors do NOT exist (then we would have to choose and which one?) tree map should handle this
        - concatenate the appender features to the appendee vector
     - write out the new table to a new csv file

 */

import javafx.application.Application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class CsvFileMerger extends Application {

    // the necessaries
    private static String usage = "read the documentation";
    private final static Logger log = LoggerFactory.getLogger(CsvFileMerger.class);

    // the other
    private static JFrame frame = null;

    public static void merge(File appendeeFile, File appenderFile, File outputFile, Integer appendeeKeyCol, Integer appenderKeyCol) throws IOException {
        ArrayList<CSVRecord> appendeeEntries = new ArrayList<>();
        TreeMap<Double, CSVRecord> appenderEntries = new TreeMap<>();


        // - read in the two csv files
        CSVParser file1Parser = new CSVParser(new FileReader(appendeeFile), CSVFormat.DEFAULT);
        CSVParser file2Parser = new CSVParser(new FileReader(appenderFile), CSVFormat.DEFAULT);

        CSVRecord file1HeaderRecord = file1Parser.iterator().next();
        CSVRecord file2HeaderRecord = file2Parser.iterator().next();


        for (CSVRecord record : file1Parser) {
            appendeeEntries.add(record);
        }

        for (CSVRecord record : file2Parser) {

            appenderEntries.put(Double.valueOf(record.get(appenderKeyCol)), record);
        }

        BufferedWriter csvWriter = null;
        csvWriter = new BufferedWriter(new FileWriter(outputFile));

        StringBuffer buffer = new StringBuffer();

        // - write out the headers
        for (int j = 0; j < file1HeaderRecord.size(); j++) {
            buffer.append(file1HeaderRecord.get(j) + ",");

        }

        for (int j = 0; j < file2HeaderRecord.size(); j++) {
            if (j == appenderKeyCol) {
                continue;
            }

            buffer.append(file2HeaderRecord.get(j) + ",");

        }
        buffer.deleteCharAt(buffer.length() - 1);

        csvWriter.write(buffer.toString().trim() + "\n");

        // - for each line in the appendee file
        for (int i = 0; i < appendeeEntries.size(); i++) {
            CSVRecord temp = appendeeEntries.get(i);
            buffer = new StringBuffer();

            // - check the value of the key column
            Double appendeeKeyValue = Double.valueOf(temp.get(appendeeKeyCol));

            // - pull the corresponding row from the appender file
            CSVRecord appender = appenderEntries.get(appendeeKeyValue);

            // - concatenate the appender features to the appendee vector
            for (int j = 0; j < temp.size(); j++) {
                buffer.append(temp.get(j) + ",");

            }

            for (int j = 0; j < appender.size(); j++) {
                if (j == appenderKeyCol) {
                    continue;
                }
                buffer.append(appender.get(j) + ",");

            }
            buffer.deleteCharAt(buffer.length() - 1);

            // - write out the new table to a new csv file
            csvWriter.write(buffer.toString().trim() + "\n");

        }

        file1Parser.close();
        file2Parser.close();
        csvWriter.flush();
        csvWriter.close();

        return;
    }

    public static void gui() {

        File file1;
        File file2;
        File outputFile;

        initialize();

        return;
    }

    private static void initialize() {

    }

    public static void main(String[] args) throws IOException {

        File file1 = null;
        File file2 = null;
        File outputFile = null;

        Integer appenderKeyCol;
        Integer appendeeKeyCol;

        // √ check to see if the appropriate number of CLA for comman line or not
        if (args.length != 5) {
            // launch the GUI
//            System.out.println(args.length);
            CsvFileMerger.gui();
            launch(args);

        } else {
            // run from commandLine

            // - read in CLA

            // appendee
            file1 = new File(args[0]);

            // appender
            file2 = new File(args[1]);

            outputFile = new File(args[2]);

            appenderKeyCol = Integer.parseInt(args[4]) - 1;
            appendeeKeyCol = Integer.parseInt(args[3]) - 1;

            CsvFileMerger.merge(file1, file2, outputFile, appendeeKeyCol, appenderKeyCol);
        }

        return;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setOnShown(e -> {
            FileChooser filechooser1 = new FileChooser();
            filechooser1.setTitle("Choose the Appendee CSV File");
            File file1 = filechooser1.showOpenDialog(primaryStage);
            log.debug(String.valueOf(file1));

            FileChooser fileChooser2 = new FileChooser();
            fileChooser2.setTitle("Choose the Appender CSV File");
            File file2 = fileChooser2.showOpenDialog(primaryStage);
            log.debug(String.valueOf(file2));
        });

        Parent root = new StackPane();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);

        primaryStage.show();

    }
}
