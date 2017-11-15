package gov.ornl.csed.cda.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Query {
    private String id;

    private ListProperty<TemporalColumnSelectionRange> temporalColumnSelectionRanges;

    private ListProperty<ColumnSelectionRange> columnSelectionRanges;
    private HashMap<QuantitativeColumn, SummaryStats> columnQuerySummaryStatsMap;
    private int maxHistogram2DBinCount;

    public Query(String id) {
        this.id = id;
        temporalColumnSelectionRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
        columnSelectionRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
        columnQuerySummaryStatsMap = new HashMap<>();
        maxHistogram2DBinCount = 0;
    }

    public final ObservableList<TemporalColumnSelectionRange> getTemporalColumnSelectionRangeList() {
        return temporalColumnSelectionRanges.get();
    }

    public ListProperty<TemporalColumnSelectionRange> temporalColumnSelectionRangesProperty() {
        return temporalColumnSelectionRanges;
    }

    public final ObservableList<ColumnSelectionRange> getColumnSelectionRanges() { return columnSelectionRanges.get(); }

    public ListProperty<ColumnSelectionRange> columnSelectionRangesProperty() { return columnSelectionRanges; }

    public int getMaxHistogram2DBinCount() { return maxHistogram2DBinCount; }

    public void setMaxHistogram2DBinCount(int maxHistogram2DBinCount) {
        this.maxHistogram2DBinCount = maxHistogram2DBinCount;
    }

    public boolean hasColumnSelections() {
        if (columnSelectionRanges.isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean hasTemporalColumnSelections() {
        if (temporalColumnSelectionRanges.isEmpty()) {
            return false;
        }
        return true;
    }

    public SummaryStats getColumnQuerySummaryStats(QuantitativeColumn column) {
        return columnQuerySummaryStatsMap.get(column);
    }

    public void setColumnQuerySummaryStats(QuantitativeColumn column, SummaryStats querySummaryStats) {
        columnQuerySummaryStatsMap.put(column, querySummaryStats);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void clear () {
        columnSelectionRanges.clear();
        temporalColumnSelectionRanges.clear();
        columnQuerySummaryStatsMap.clear();
    }

    public ArrayList<ColumnSelectionRange> getColumnSelectionRanges (QuantitativeColumn column) {
        ArrayList<ColumnSelectionRange> rangeList = new ArrayList<>();

        for (ColumnSelectionRange columnSelectionRange : columnSelectionRanges) {
            if (columnSelectionRange.getColumn() == column) {
                rangeList.add(columnSelectionRange);
            }
        }
        return rangeList;
    }

    public List<ColumnSelectionRange> getAllColumnSelectionRanges() {
        return columnSelectionRanges;
    }

    public void addColumnSelectionRange (ColumnSelectionRange columnSelectionRange) {
        //TODO: See if an identical range selection exists or if this selection overlaps with another
        // ignore if identical exists and merge if overlapping selection exists
        columnSelectionRanges.add(columnSelectionRange);
    }

    public void addTemporalColumnSelectionRange (TemporalColumnSelectionRange temporalColumnSelectionRange) {
        // TODO: See if an identical range selection exists or if the selection overlaps with another
        // ignore if identical range selection exists and merge if overlapping with another
        temporalColumnSelectionRanges.add(temporalColumnSelectionRange);
    }

    public boolean removeColumnSelectionRange(ColumnSelectionRange columnSelectionRange) {
        if (!columnSelectionRanges.isEmpty()) {
            return columnSelectionRanges.remove(columnSelectionRange);
        }
        return false;
    }

    public boolean removeTemporalColumnSelectionRange(TemporalColumnSelectionRange temporalColumnSelectionRange) {
        if (!temporalColumnSelectionRanges.isEmpty()) {
            return temporalColumnSelectionRanges.remove(temporalColumnSelectionRange);
        }
        return false;
    }

    public ArrayList<ColumnSelectionRange> removeColumnSelectionRanges(QuantitativeColumn column) {
        if (!columnSelectionRanges.isEmpty()) {
            ArrayList<ColumnSelectionRange> removedRanges = new ArrayList<>();

            for (ColumnSelectionRange rangeSelection : columnSelectionRanges) {
                if (rangeSelection.getColumn() == column) {
                    removedRanges.add(rangeSelection);
                }
            }

            columnSelectionRanges.removeAll(removedRanges);
            return removedRanges;
        }

        return null;
    }

    public ArrayList<TemporalColumnSelectionRange> removeTemporalColumnSelectionRanges() {
        if (!temporalColumnSelectionRanges.isEmpty()) {
            ArrayList<TemporalColumnSelectionRange> removedRanges = new ArrayList<>();

            for (TemporalColumnSelectionRange temporalColumnSelectionRange : temporalColumnSelectionRanges) {
                removedRanges.add(temporalColumnSelectionRange);
            }

            temporalColumnSelectionRanges.clear();
            return removedRanges;
        }
        return null;
    }
}