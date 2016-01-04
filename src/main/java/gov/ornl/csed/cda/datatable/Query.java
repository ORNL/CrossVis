package gov.ornl.csed.cda.datatable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csg on 11/25/14.
 */
public class Query {
    private String id;
    private ArrayList<ColumnSelection> columnSelectionList = new ArrayList<ColumnSelection>();
    private ArrayList<Tuple> tuples = new ArrayList<Tuple>();
    private HashMap<Column, SummaryStats> columnQuerySummaryStatsMap = new HashMap<Column, SummaryStats>();

    public Query(String id) {
        this.id = id;
    }

    public boolean hasColumnSelections() {
        if (columnSelectionList.isEmpty()) {
            return false;
        }
        return true;
    }

    public SummaryStats getColumnQuerySummaryStats(Column column) {
        return columnQuerySummaryStatsMap.get(column);
    }

    public void setColumnQuerySummaryStats(Column column, SummaryStats querySummaryStats) {
        columnQuerySummaryStatsMap.put(column, querySummaryStats);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void addTuple(Tuple tuple) {
        tuples.add(tuple);
    }

    public void clearTuples() {
        tuples.clear();
        columnQuerySummaryStatsMap.clear();
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }

    public void clearAllColumnSelections() {
        columnSelectionList.clear();
        tuples.clear();
        columnQuerySummaryStatsMap.clear();
    }

    public void clearColumnSelection(Column column) {
        ArrayList<ColumnSelection> selectionsToRemove = new ArrayList<ColumnSelection>();

        for (ColumnSelection selection : columnSelectionList) {
            if (selection.getColumn() == column) {
                selectionsToRemove.add(selection);
            }
        }

        columnSelectionList.removeAll(selectionsToRemove);
    }

    public ColumnSelection getColumnSelection (Column column) {
        for (ColumnSelection columnSelection : columnSelectionList) {
            if (columnSelection.getColumn() == column) {
                return columnSelection;
            }
        }
        return null;
    }

    public ArrayList<ColumnSelection> getColumnSelections() {
        return columnSelectionList;
    }

    public void addColumnSelection (ColumnSelection columnSelection) {
        // first see if a column selection already exists for the given column selection column
        ColumnSelection existingColumnSelection = getColumnSelection(columnSelection.getColumn());

        if (existingColumnSelection != null) {
            // if it exists, then merge this with the existing one
            existingColumnSelection.merge(columnSelection);
        } else {
            // if it does not exist, then add this columnSelection
            columnSelectionList.add(columnSelection);
        }
    }
}