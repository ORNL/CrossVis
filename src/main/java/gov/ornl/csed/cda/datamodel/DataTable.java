package gov.ornl.csed.cda.datamodel;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DataTable {
    private final static Logger log = Logger.getLogger(DataTable.class.getName());

    private ArrayList<Column> columns;
    private ArrayList<DataTableListener> listeners;

    public DataTable () {
        listeners = new ArrayList<>();
        columns = new ArrayList<>();
    }

    public void addDataTableListener(DataTableListener dataTableListener) {
        if (!listeners.contains(dataTableListener)) {
            listeners.add(dataTableListener);
        }
    }

    public boolean removeDataTableListener(DataTableListener dataTableListener) {
        return listeners.remove(dataTableListener);
    }

    private void fireDataTableChanged() {
        for (DataTableListener listener : listeners) {
            listener.dataTableChanged(this);
        }
    }

    private void fireDataTableColumnsChanged() {
        for (DataTableListener listener : listeners) {
            listener.dataTableColumnsChanged(this);
        }
    }

    private void fireDataTableCleared() {
        for (DataTableListener listener : listeners) {
            listener.dataTableCleared(this);
        }
    }

    private void fireDataTableColumnOrderChanged() {
        for (DataTableListener listener : listeners) {
            listener.dataTableColumnOrderChanged(this);
        }
    }

    private void fireDataTableRowsChanged() {
        for (DataTableListener listener : listeners) {
            listener.dataTableRowsChanged(this);
        }
    }

    public void clear() {
        clearDataModel();
        fireDataTableCleared();
    }

    private void clearDataModel() {
        columns.clear();
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public void setData(ArrayList<Column> newColumns, ArrayList<ArrayList<Object>> rows) {
        clearDataModel();
        columns.addAll(newColumns);
        addRows(rows);
        fireDataTableChanged();
    }

    public void addColumn(Column column) {
        columns.add(column);
        fireDataTableColumnsChanged();
    }

    public void changeColumnOrder(Column column, int newIndex){
        int originalIndex = columns.indexOf(column);
        if (originalIndex != newIndex && originalIndex != -1) {
            columns.remove(originalIndex);

            if (originalIndex < newIndex) {
                columns.set(newIndex-1, column);
            } else {
                columns.set(newIndex+1, column);
            }

            // TODO: Recalculate histograms and correlation and other stats related to order of columns

            fireDataTableColumnOrderChanged();
        }
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public void addRow(ArrayList<Object> rowValues) {
        addRowValues(rowValues);

        // update column statistics
        calculateColumnStatistics();

        fireDataTableRowsChanged();
    }

    private void addRowValues(ArrayList<Object> rowValues) {
        for (int icol = 0; icol < columns.size(); icol++) {
            Column column = columns.get(icol);
            if (column instanceof TemporalColumn) {
                ((TemporalColumn)column).addValue((Instant)rowValues.get(icol));
            } else if (column instanceof DoubleColumn) {
                ((DoubleColumn)column).addValue((Double)rowValues.get(icol));
            }
        }
    }

    public void addRows(ArrayList<ArrayList<Object>> rows) {
        for (ArrayList<Object> row : rows) {
            addRowValues(row);
        }

        // update column statistics
        calculateColumnStatistics();

        fireDataTableRowsChanged();
    }

    private void calculateColumnStatistics() {
        for (Column column : columns) {
            if (column instanceof DoubleColumn) {
                column.calculateStatistics();
            }
        }
    }

    public long getRowCount() {
        return columns.get(0).getSize();
    }
}
