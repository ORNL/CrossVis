package gov.ornl.csed.cda.datatable;

import java.util.ArrayList;

public class ColumnSelection {
    Query query;
    private ArrayList<ColumnSelectionRange> selectionRangeArrayList = new ArrayList<ColumnSelectionRange>();
    private Column column;

    public ColumnSelection(Query query, Column column) {
        this.query = query;
        this.column = column;
    }

    public Query getQuery() {
        return query;
    }

    public Column getColumn() {
        return column;
    }

    public ColumnSelectionRange addColumnSelectionRange(double minValue, double maxValue) {
        // TODO: first see if this range overlaps other existing selection range objects
        // if it does then merge the two into a new column selection range
        ColumnSelectionRange selectionRange = new ColumnSelectionRange(this, minValue, maxValue);
        selectionRangeArrayList.add(selectionRange);
        return selectionRange;
    }

    public int getColumnSelectionRangeCount() {
        return selectionRangeArrayList.size();
    }

    public void removeColumnSelectionRange(ColumnSelectionRange selectionRange) {
        selectionRangeArrayList.remove(selectionRange);
    }

    public ArrayList<ColumnSelectionRange> getColumnSelectionRanges() {
        return selectionRangeArrayList;
    }

    public void merge (ColumnSelection otherColumnSelection) {
        for (ColumnSelectionRange range : otherColumnSelection.getColumnSelectionRanges()) {
            addColumnSelectionRange(range.getMinValue(), range.getMaxValue());
        }
    }
}
