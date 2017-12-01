package gov.ornl.csed.cda.datamodel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DataTableCSVReader {
    private final static Logger log = Logger.getLogger(DataTableCSVReader.class.getName());

    public static void read(File f, String temporalColumnName, DateTimeFormatter dateTimeFormatter, DataTable dataTable) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        int temporalColumnIndex = -1;
        String line;
        int lineCounter = 0;
        int numLinesIgnored = 0;
        boolean skipLine = false;

        ArrayList<ArrayList<Object>> rowList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (lineCounter == 0) {
                int tokenCounter = 0;
                String tokens[] = line.split(",");
                for (String token : tokens) {
                    if (temporalColumnName != null && token.equals(temporalColumnName)) {
                        temporalColumnIndex = tokenCounter;
                        dataTable.addColumn(new TemporalColumn(token.trim()));
                    } else {
                        dataTable.addColumn(new DoubleColumn(token.trim()));
                    }
                    tokenCounter++;
                }
                lineCounter++;
                continue;
            }

            ArrayList<Object> rowObjects = new ArrayList<>();
            String tokens[] = line.split(",");
            int tokenCounter = 0;
            skipLine = false;
            for (String token : tokens) {
                Column column = dataTable.getColumn(tokenCounter);
                if (column instanceof TemporalColumn) {
                    LocalDateTime localDateTime = LocalDateTime.parse(token, dateTimeFormatter);
                    Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
                    rowObjects.add(instant);
//                    ((TemporalColumn)column).addValue(instant);
                    tokenCounter++;
                } else if (column instanceof DoubleColumn){
                    try {
                        double value = Double.parseDouble(token);

                        if (Double.isNaN(value)) {
                            skipLine = true;
                            break;
                        }

                        rowObjects.add(value);
                        tokenCounter++;
                    } catch (NumberFormatException ex) {
                        log.warning("DataTableCSVReader.read(): NumberFormatException caught so skipping line: " + ex.fillInStackTrace());
                        skipLine = true;
                        numLinesIgnored++;
                    }
//                    ((DoubleColumn)column).addValue(value);
//                    tokenCounter++;
                }
            }

            if (rowObjects.size() != dataTable.getColumnCount()) {
                log.warning("Row ignored because it has " + (dataTable.getColumnCount() - rowObjects.size()) +
                " columns values missing.");
                numLinesIgnored++;
                skipLine = true;
            }

            log.info("Adding row for line " + lineCounter);
            if (!skipLine) {
                rowList.add(rowObjects);
//                dataTable.addRow(rowObjects);
//                for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
//                    Column column = dataTable.getColumn(icolumn);
//                    if (column instanceof TemporalColumn) {
//                        ((TemporalColumn)column).addValue((Instant)rowObjects.get(icolumn));
//                    } else if (column instanceof DoubleColumn) {
//                        ((DoubleColumn)column).addValue((Double)rowObjects.get(icolumn));
//                    }
//                }
            }

            lineCounter++;
        }

        reader.close();
        log.info("Finished reading CSV file '" + f.getName() + "': Read " + dataTable.getRowCount() + " rows with " + dataTable.getColumnCount() + " columns; " + numLinesIgnored + " rows ignored.");

        log.info("Adding data to datatable");
        long startMillis = System.currentTimeMillis();
        dataTable.addRows(rowList);
        long elaspedMillis = System.currentTimeMillis() - startMillis;
        log.info("Loading table took " + (elaspedMillis/1000.) + " seconds");
    }

    public static void main (String args[]) throws IOException {
        DataTable dataTable = new DataTable();
        DataTableCSVReader.read(new File("data/csv/titan-performance.csv"), "Date",
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"), dataTable);

        // print out data table to screen
//        for (int i = 0; i < dataTable.getRowCount(); i++) {
//            for (int j = 0; j < dataTable.getColumnCount(); j++) {
//                if (dataTable.getColumn(j) instanceof TemporalColumn) {
//                    System.out.print(((TemporalColumn)dataTable.getColumn(j)).getValueAt(i));
//                } else if (dataTable.getColumn(j) instanceof DoubleColumn) {
//                    System.out.print(((DoubleColumn)dataTable.getColumn(j)).getValueAt(i));
//                }
//
//                if (j+1 == dataTable.getColumnCount()) {
//                    System.out.print("\n");
//                } else {
//                    System.out.print(", ");
//                }
//            }
//        }
    }
}
