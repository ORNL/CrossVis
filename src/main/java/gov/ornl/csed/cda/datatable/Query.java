package gov.ornl.csed.cda.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by csg on 11/25/14.
 */
public class Query {
    private String id;
    private ListProperty<ColumnSelectionRange> columnSelectionRangeList;
//    private ArrayList<Tuple> tuples;
    private HashMap<Column, SummaryStats> columnQuerySummaryStatsMap;
    private int maxHistogram2DBinCount;

    public Query(String id) {
        this.id = id;
        columnSelectionRangeList = new SimpleListProperty<>(FXCollections.observableArrayList());
//        tuples = new ArrayList<>();
        columnQuerySummaryStatsMap = new HashMap<>();
        maxHistogram2DBinCount = 0;
    }

    public final ObservableList<ColumnSelectionRange> getColumnsSelectionRangeList() { return columnSelectionRangeList.get(); }

    public ListProperty<ColumnSelectionRange> columnSelectionRangeList() { return columnSelectionRangeList; }

    public int getMaxHistogram2DBinCount() { return maxHistogram2DBinCount; }

    public void setMaxHistogram2DBinCount(int maxHistogram2DBinCount) {
        this.maxHistogram2DBinCount = maxHistogram2DBinCount;
    }

    public boolean hasColumnSelections() {
        if (columnSelectionRangeList.isEmpty()) {
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

//    public void addTuple(Tuple tuple) {
//        tuples.add(tuple);
//    }

//    public void clearTuples() {
//        tuples.clear();
//        columnQuerySummaryStatsMap.clear();
//    }

//    public ArrayList<Tuple> getTuples() {
//        return tuples;
//    }

    public void clear () {
        columnSelectionRangeList.clear();
        columnQuerySummaryStatsMap.clear();
    }

//    public void clearAllColumnSelections() {
//        columnSelectionRangeList.clear();
//        tuples.clear();
//        columnQuerySummaryStatsMap.clear();
//    }

//    public void clearColumnSelection(Column column) {
//        ArrayList<ColumnSelectionRange> selectionsToRemove = new ArrayList<>();
//
//        for (ColumnSelectionRange selection : columnSelectionRangeList) {
//            if (selection.getColumn() == column) {
//                selectionsToRemove.add(selection);
//            }
//        }
//
//        columnSelectionRangeList.removeAll(selectionsToRemove);
//    }

    public ArrayList<ColumnSelectionRange> getColumnSelectionRanges (Column column) {
        ArrayList<ColumnSelectionRange> rangeList = new ArrayList<>();

        for (ColumnSelectionRange columnSelectionRange : columnSelectionRangeList) {
            if (columnSelectionRange.getColumn() == column) {
                rangeList.add(columnSelectionRange);
            }
        }
        return rangeList;
    }

    public List<ColumnSelectionRange> getAllColumnSelectionRanges() {
        return columnSelectionRangeList;
    }

    public void addColumnSelectionRange (ColumnSelectionRange columnSelectionRange) {
        //TODO: See if an identical range selection exists or if this selection overlaps with another
        // ignore if identical exists and merge if overlapping selection exists
        columnSelectionRangeList.add(columnSelectionRange);
    }

    public boolean removeColumnSelectionRange(ColumnSelectionRange columnSelectionRange) {
        if (!columnSelectionRangeList.isEmpty()) {
            return columnSelectionRangeList.remove(columnSelectionRange);
        }

        return false;
    }

    public ArrayList<ColumnSelectionRange> removeColumnSelectionRanges(Column column) {
        if (!columnSelectionRangeList.isEmpty()) {
            ArrayList<ColumnSelectionRange> removedRanges = new ArrayList<>();

            for (ColumnSelectionRange rangeSelection : columnSelectionRangeList) {
                if (rangeSelection.getColumn() == column) {
                    removedRanges.add(rangeSelection);
                }
            }

            columnSelectionRangeList.removeAll(removedRanges);
            return removedRanges;
        }

        return null;
    }
}