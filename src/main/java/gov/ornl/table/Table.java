package gov.ornl.table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Table {
    private final static Logger log = Logger.getLogger(Table.class.getName());

    private ArrayList<Column> columns = new ArrayList<>();

    public Table() { }

    public void clear() {
        columns.clear();
    }

    public Table selectData(TableFilter tableSelector) {
        // make new Table object for selected data
        Table newTable = new Table();
        newTable.copyTableColumns(this);

        // for each row test
        for (int i = 0; i < getRowCount(); i++) {
            if (tableSelector.accept(i)) {
                // add row to new table
                newTable.addRow(getRow(i));
            }
        }

        return newTable;
    }

    public List<String> getColumnTitles() {
        ArrayList<String> columnTitles = new ArrayList<>();
        for (Column column : columns) {
            columnTitles.add(column.getTitle());
        }
        return columnTitles;
    }

    public void copyTableColumns(Table srcTable) {
        for (Column column : srcTable.columns) {
            if (column instanceof DoubleColumn) {
                addDoubleColumn(column.getTitle());
            } else if (column instanceof TemporalColumn) {
                addTemporalColumn(column.getTitle());
            } else if (column instanceof CategoricalColumn) {
                addCategoricalColumn(column.getTitle());
            } else if (column instanceof ImageColumn) {
                addImageColumn(column.getTitle());
            } else if (column instanceof IntegerColumn) {
                addIntegerColumn(column.getTitle());
            } else if (column instanceof LongColumn) {
                addLongColumn(column.getTitle());
            } else if (column instanceof FloatColumn) {
                addFloatColumn(column.getTitle());
            }
        }
    }

    public DoubleColumn addDoubleColumn(String title) {
        DoubleColumn column = new DoubleColumn(title);
        columns.add(column);
        return column;
    }

    public ImageColumn addImageColumn (String title) {
        ImageColumn column = new ImageColumn(title);
        columns.add(column);
        return column;
    }

    public CategoricalColumn addCategoricalColumn(String title) {
        CategoricalColumn column = new CategoricalColumn(title);
        columns.add(column);
        return column;
    }

    public TemporalColumn addTemporalColumn(String title) {
        TemporalColumn column = new TemporalColumn(title);
        columns.add(column);
        return column;
    }

    public IntegerColumn addIntegerColumn(String title) {
        IntegerColumn column = new IntegerColumn(title);
        columns.add(column);
        return column;
    }

    public LongColumn addLongColumn(String title) {
        LongColumn column = new LongColumn(title);
        columns.add(column);
        return column;
    }

    public FloatColumn addFloatColumn(String title) {
        FloatColumn column = new FloatColumn(title);
        columns.add(column);
        return column;
    }

    public Column getColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    public Column getColumn(String title) {
        for (Column column : columns) {
            if (column.getTitle().equals(title)) {
                return column;
            }
        }

        return null;
    }

    public int getColumnCount() {
        return columns.size();
    }

    public void addRow(Object[] rowValues) {
        if (columns.size() == rowValues.length) {
            for (int i = 0; i < rowValues.length; i++) {
                columns.get(i).addValue(rowValues[i]);
            }
        }
    }

    public Object[] getRow(int rowIndex) {
        Object[] rowValues = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            rowValues[i] = columns.get(i).getValue(rowIndex);
        }
        return rowValues;
    }

    public int getRowCount() {
        return columns.get(0).getRowCount();
    }

    public void readRowsFromFile(File file, boolean header, Map<Column, DateTimeFormatter> timeFormatterMap) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        int rowCounter = 0;

        while ((line = reader.readLine()) != null) {
            if (rowCounter == 0 && header) {
                // skip it
                rowCounter++;
                continue;
            } else {
                String tokens[] = line.split(",");
                Object rowValues[] = new Object[columns.size()];
                boolean skipLine = false;

                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i].trim();
                    Column column = columns.get(i);

                    if (column instanceof DoubleColumn) {
                        double value = Double.NaN;
                        try {
                            value = Double.parseDouble(token);
                        } catch (NumberFormatException ex) {
                            log.warning("Error parsing double value '" + token + "'");
                            ex.printStackTrace();
                        }

                        if (Double.isNaN(value)) {
                            skipLine = true;
                            break;
                        } else {
                            rowValues[i] = value;
                        }
                    } else if (column instanceof IntegerColumn) {
                        int value;
                        try {
                            value = Integer.parseInt(token);
                        } catch (NumberFormatException ex) {
                            log.warning("Error parsing integer value '" + token + "'");
                            ex.printStackTrace();
                            skipLine = true;
                            break;
                        }

                        rowValues[i] = value;
                    } else if (column instanceof LongColumn) {
                        long value;
                        try {
                            value = Long.parseLong(token);
                        } catch (NumberFormatException ex) {
                            log.warning("Error parsing long value '" + token + "'");
                            ex.printStackTrace();
                            skipLine = true;
                            break;
                        }

                        rowValues[i] = value;
                    } else if (column instanceof FloatColumn) {
                        float value = Float.NaN;
                        try {
                            value = Float.parseFloat(token);
                        } catch (NumberFormatException ex) {
                            log.warning("Error parsing float value '" + token + "'");
                            ex.printStackTrace();
                        }

                        if (Float.isNaN(value)) {
                            skipLine = true;
                            break;
                        } else {
                            rowValues[i] = value;
                        }
                    } else if (column instanceof CategoricalColumn) {
                        if (token.isEmpty()) {
                            skipLine = true;
                            break;
                        } else {
                            rowValues[i] = token;
                        }
                    } else if (column instanceof TemporalColumn) {
                        LocalDateTime localDateTime = LocalDateTime.parse(token, timeFormatterMap.get(column));
                        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);

                        if (token.isEmpty()) {
                            skipLine = true;
                            break;
                        } else {
                            rowValues[i] = instant;
                        }
                    }
                }

                if (!skipLine) {
                    addRow(rowValues);
                }
            }
            rowCounter++;
        }
    }

    public static String[] getFileHeader(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        reader.close();

        String columnTitles[] = line.split(",");
        for (int i = 0; i < columnTitles.length; i++) {
            columnTitles[i] = columnTitles[i].trim();
        }
        return columnTitles;
    }

    public static void main (String args[]) throws IOException {
//        File f = new File("data/csv/cars-cat.csv");
        File f = new File("data/csv/titan-performance.csv");
        Table table = new Table();

        HashMap<Column, DateTimeFormatter> timeFormatterMap = new HashMap<>();

        String columnTitles[] = Table.getFileHeader(f);
        for (String columnTitle : columnTitles) {
            if (columnTitle.equals("Origin")) {
                table.addCategoricalColumn(columnTitle);
            } else if (columnTitle.equals("Date")) {
                TemporalColumn column = table.addTemporalColumn(columnTitle);
                timeFormatterMap.put(column, DateTimeFormatter.ofPattern("dd.MM.yyy HH:mm:ss"));
            } else {
                table.addDoubleColumn(columnTitle);
            }
        }

        table.readRowsFromFile(f, true, timeFormatterMap);

        log.info("Read " + table.getRowCount() + " rows.");
        for (int i = 0; i < table.getColumnCount(); i++) {
            log.info("Column " + i + "(" + table.getColumn(i).getClass().getName() + "): " + table.getColumn(i).getTitle() + " with " + table.getColumn(i).getRowCount() + " rows");
        }
    }
}
