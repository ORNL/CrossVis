package gov.ornl.csed.cda.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.*;

public class Query {
    private String id;

    private ListProperty<ColumnSelectionRange> columnSelectionRanges;
    private HashMap<Column, ColumnSummaryStats> columnQuerySummaryStatsMap;
    private int maxHistogram2DBinCount;
    private DataModel dataModel;

    private HashSet<Tuple> queriedTuples;
    private HashSet<Tuple> nonQueriedTuples;

    public Query(String id, DataModel dataModel) {
        this.id = id;
        this.dataModel = dataModel;
        columnSelectionRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
        columnQuerySummaryStatsMap = new HashMap<>();
        maxHistogram2DBinCount = 0;
        queriedTuples = new HashSet<>();
        nonQueriedTuples = new HashSet<>();
    }

    public Set<Tuple> getQueriedTuples() {
        return queriedTuples;
    }

    public int getQueriedTupleCount() {
        return queriedTuples.size();
    }

    public int getNonQueriedTupleCount() {
        return nonQueriedTuples.size();
    }

    public Set<Tuple> getNonQueriedTuples() {
        return nonQueriedTuples;
    }

    public void setQueriedTuples() {
        queriedTuples.clear();
        nonQueriedTuples.clear();

        if (dataModel.getTupleCount() == 0) {
            return;
        }

        if (hasColumnSelections()) {
            for (Tuple tuple : dataModel.getTuples()) {
                tuple.setQueryFlag(true);

                for (int icol = 0; icol < dataModel.getColumnCount(); icol++) {
                    Column column = dataModel.getColumn(icol);
                    ArrayList<ColumnSelectionRange> selectionRanges = getColumnSelectionRanges(column);
                    if (selectionRanges != null && (!selectionRanges.isEmpty())) {
                        boolean insideSelectionRange = false;

                        if (column instanceof DoubleColumn) {
                            for (ColumnSelectionRange selectionRange : selectionRanges) {
                                if ((((Double)tuple.getElement(icol)) <= ((DoubleColumnSelectionRange)selectionRange).getMaxValue()) &&
                                        (((Double)tuple.getElement(icol)) >= ((DoubleColumnSelectionRange)selectionRange).getMinValue())) {
                                    insideSelectionRange = true;
                                    break;
                                }
                            }
                        } else if (column instanceof TemporalColumn) {
                            for (ColumnSelectionRange selectionRange : selectionRanges) {
                                if (!((((Instant)tuple.getElement(icol)).isBefore(((TemporalColumnSelectionRange)selectionRange).getStartInstant())) ||
                                        ((Instant)tuple.getElement(icol)).isAfter(((TemporalColumnSelectionRange)selectionRange).getEndInstant()))) {
                                    insideSelectionRange = true;
                                    break;
							    }
                            }
                        }

                        if (!insideSelectionRange) {
                            tuple.setQueryFlag(false);
                            break;
                        }
                    }
                }

                if (tuple.getQueryFlag()) {
                    queriedTuples.add(tuple);
                } else {
                    nonQueriedTuples.add(tuple);
                }
            }

            calculateStatistics();
        } else {
            for (Tuple tuple : dataModel.getTuples()) {
                tuple.setQueryFlag(false);
                nonQueriedTuples.add(tuple);
            }
        }
    }

    public void setNumHistogramBins(int numBins) {
        for (ColumnSummaryStats summaryStats : columnQuerySummaryStatsMap.values()) {
            summaryStats.setNumHistogramBins(numBins);
        }
        
        calculateColumn2DHistograms();
    }

    public void calculateStatistics() {
        if (!queriedTuples.isEmpty()) {
            for (int icolumn = 0; icolumn < dataModel.getColumnCount(); icolumn++) {
                Column column = dataModel.getColumn(icolumn);
                ColumnSummaryStats columnSummaryStats = columnQuerySummaryStatsMap.get(column);
                if (column instanceof TemporalColumn) {
                    Instant values[] = ((TemporalColumn) column).getQueriedValues();
                    if (columnSummaryStats == null) {
                        columnSummaryStats = new TemporalColumnSummaryStats(column, dataModel.getNumHistogramBins());
                        columnQuerySummaryStatsMap.put(column, columnSummaryStats);
                    }
                    ((TemporalColumnSummaryStats) columnSummaryStats).setValues(values);
                } else if (column instanceof DoubleColumn) {
                    double values[] = ((DoubleColumn) column).getQueriedValues();
                    if (columnSummaryStats == null) {
                        columnSummaryStats = new DoubleColumnSummaryStats(column, dataModel.getNumHistogramBins());
                        columnQuerySummaryStatsMap.put(column, columnSummaryStats);
                    }
                    ((DoubleColumnSummaryStats) columnSummaryStats).setValues(values);
                }
            }

            calculateColumn2DHistograms();
        }
    }

    private void calculateColumn2DHistograms() {
        maxHistogram2DBinCount = 0;
        for (ColumnSummaryStats columnSummaryStats : columnQuerySummaryStatsMap.values()) {
            for (ColumnSummaryStats compareColumnSummaryStats : columnQuerySummaryStatsMap.values()) {
                if (columnSummaryStats != compareColumnSummaryStats) {
                    columnSummaryStats.calculateHistogram2D(compareColumnSummaryStats);
                    if (columnSummaryStats.getMaxHistogram2DBinCount() > maxHistogram2DBinCount) {
                        maxHistogram2DBinCount = columnSummaryStats.getMaxHistogram2DBinCount();
                    }
                }
            }
        }
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

    public ColumnSummaryStats getColumnQuerySummaryStats(Column column) {
        return columnQuerySummaryStatsMap.get(column);
    }

    public void setColumnQuerySummaryStats(Column column, ColumnSummaryStats querySummaryStats) {
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
        columnQuerySummaryStatsMap.clear();
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