package gov.ornl.csed.cda.datamodel;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private String id;

    private ListProperty<ColumnSelectionRange> columnSelectionRanges;

    public Query(String id) {
        this.id = id;
        columnSelectionRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public final ObservableList<ColumnSelectionRange> getColumnSelectionRanges() { return columnSelectionRanges.get(); }

    public ListProperty<ColumnSelectionRange> columnSelectionRangesProperty() { return columnSelectionRanges; }

    public boolean hasColumnSelections() {
        if (columnSelectionRanges.isEmpty()) {
            return false;
        }
        return true;
    }

    public void clear() {
        columnSelectionRanges.clear();
    }

    public ArrayList<ColumnSelectionRange> getColumnSelectionRanges (Column column) {
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

    public boolean removeColumnSelectionRange(ColumnSelectionRange columnSelectionRange) {
        if (!columnSelectionRanges.isEmpty()) {
            return columnSelectionRanges.remove(columnSelectionRange);
        }
        return false;
    }

    public ArrayList<ColumnSelectionRange> removeColumnSelectionRanges(Column column) {
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
}
