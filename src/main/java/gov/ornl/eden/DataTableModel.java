package gov.ornl.eden;

import gov.ornl.datatable.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by csg on 10/8/15.
 */
public class DataTableModel extends AbstractTableModel implements DataModelListener {
    private final static Logger log = LoggerFactory.getLogger(DataTableModel.class);
    private DataModel dataModel;

    public DataTableModel(DataModel dataModel) {
        this.dataModel = dataModel;
        dataModel.addDataModelListener(this);
    }

    public Class getColumnClass(int c) {
        return Float.class;
    }

    @Override
    public int getRowCount() {
//        log.debug("getRowCount() called");
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            // a query is set so return the number of queried tuples
//            log.debug("row count is " + dataModel.getQueriedTupleCount());
            return dataModel.getQueriedTupleCount();
        } else {
            // no query is set so return the totoal number of tuples
//            log.debug("row count is " + dataModel.getTupleCount());
            return dataModel.getTupleCount();
        }
    }

    @Override
    public int getColumnCount() {
//        log.debug("column count is " + dataModel.getColumnCount());
        return dataModel.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
//        log.debug("entered getValueAt()");
        if (columnIndex < dataModel.getColumnCount() && columnIndex >= 0) {
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                if (rowIndex >= 0 && rowIndex < dataModel.getQueriedTupleCount()) {
                    return dataModel.getActiveQuery().getTuples().get(rowIndex).getElement(columnIndex);
                }
            } else {
                if (rowIndex >= 0 && rowIndex < dataModel.getTupleCount()) {
                    return dataModel.getTuples().get(rowIndex).getElement(columnIndex);
                }
            }
        }
        return null;
    }

    public String getColumnName(int column) {
//        log.debug("getColumnName()");
        if (column < dataModel.getColumnCount() && column >= 0) {
            return dataModel.getColumn(column).getName();
        }
        return null;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public void dataModelChanged(DataModel dataModel) {
        fireTableStructureChanged();
//        log.debug("dataModelChanged() in DataTableModel");
    }

    @Override
    public void highlightedColumnChanged(DataModel dataModel) {
        // do nothing
    }

    @Override
    public void queryChanged(DataModel dataModel) {
//        log.debug("queryChanged called");
        fireTableDataChanged();
    }

    @Override
    public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        fireTableDataChanged();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        fireTableDataChanged();
    }

    @Override
    public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
        fireTableDataChanged();
    }

    @Override
    public void columnDisabled(DataModel dataModel, Column disabledColumn) {
        fireTableDataChanged();
    }

    @Override
    public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {
        fireTableDataChanged();
    }

    @Override
    public void columnEnabled(DataModel dataModel, Column enabledColumn) {
        fireTableDataChanged();
    }
}
