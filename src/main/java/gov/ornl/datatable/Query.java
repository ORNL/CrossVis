package gov.ornl.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

public class Query {
    private final static Logger log = Logger.getLogger(Query.class.getName());

    private String id;

    private ListProperty<ColumnSelectionRange> columnSelectionRanges;
    private HashMap<Column, ColumnSummaryStats> columnQuerySummaryStatsMap;
    private HashMap<Column, ColumnSummaryStats> columnNonquerySummaryStatsMap;
    private int maxHistogram2DBinCount;
    private DataModel dataModel;

    private HashSet<Tuple> queriedTuples;
    private HashSet<Tuple> nonQueriedTuples;

    public Query(String id, DataModel dataModel) {
        this.id = id;
        this.dataModel = dataModel;
        columnSelectionRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
        columnQuerySummaryStatsMap = new HashMap<>();
        columnNonquerySummaryStatsMap = new HashMap<>();
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
                        } else if (column instanceof CategoricalColumn) {
                            for (ColumnSelectionRange selectionRange : selectionRanges) {
                                if (((CategoricalColumnSelection)selectionRange).getSelectedCategories().contains((String)tuple.getElement(icol))) {
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
        long start = System.currentTimeMillis();

        if (!queriedTuples.isEmpty()) {
            for (int icolumn = 0; icolumn < dataModel.getColumnCount(); icolumn++) {
                Column column = dataModel.getColumn(icolumn);
                ColumnSummaryStats queryColumnSummaryStats = columnQuerySummaryStatsMap.get(column);
                ColumnSummaryStats nonqueryColumnSummaryStats = columnNonquerySummaryStatsMap.get(column);

                if (column instanceof TemporalColumn) {
                    Instant queriedValues[] = ((TemporalColumn)column).getQueriedValues();
                    if (queryColumnSummaryStats == null) {
                        queryColumnSummaryStats = new TemporalColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                        columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                    }
                    ((TemporalColumnSummaryStats)queryColumnSummaryStats).setValues(queriedValues);

                    Instant nonqueriedValues[] = ((TemporalColumn)column).getNonqueriedValues();
                    if (nonqueryColumnSummaryStats == null) {
                        nonqueryColumnSummaryStats = new TemporalColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                        columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                    }
                    ((TemporalColumnSummaryStats)nonqueryColumnSummaryStats).setValues(nonqueriedValues);
                } else if (column instanceof DoubleColumn) {
                    if (dataModel.getCalculateQueryStatistics()) {
                        double queriedValues[] = ((DoubleColumn) column).getQueriedValues();
                        if (queryColumnSummaryStats == null) {
                            queryColumnSummaryStats = new DoubleColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                            columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                        }
                        ((DoubleColumnSummaryStats) queryColumnSummaryStats).setValues(queriedValues);
                    } else {
                        columnQuerySummaryStatsMap.remove(column);
                    }

                    if (dataModel.getCalculateNonQueryStatistics()) {
                        double nonqueriedValues[] = ((DoubleColumn) column).getNonqueriedValues();
                        if (nonqueryColumnSummaryStats == null) {
                            nonqueryColumnSummaryStats = new DoubleColumnSummaryStats(column, dataModel.getNumHistogramBins(), this);
                            columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                        }
                        ((DoubleColumnSummaryStats) nonqueryColumnSummaryStats).setValues(nonqueriedValues);
                    } else {
                        columnNonquerySummaryStatsMap.remove(column);
                    }
                } else if (column instanceof CategoricalColumn) {
                    String queriedValues[] = ((CategoricalColumn)column).getQueriedValues();
                    if (queryColumnSummaryStats == null) {
                        queryColumnSummaryStats = new CategoricalColumnSummaryStats(column, this);
                        columnQuerySummaryStatsMap.put(column, queryColumnSummaryStats);
                    }
                    ((CategoricalColumnSummaryStats)queryColumnSummaryStats).setValues(queriedValues);

                    String nonqueriedValues[] = ((CategoricalColumn)column).getNonqueriedValues();
                    if (nonqueryColumnSummaryStats == null) {
                        nonqueryColumnSummaryStats = new CategoricalColumnSummaryStats(column, this);
                        columnNonquerySummaryStatsMap.put(column, nonqueryColumnSummaryStats);
                    }
                    ((CategoricalColumnSummaryStats)nonqueryColumnSummaryStats).setValues(nonqueriedValues);
                }
            }
            calculateColumn2DHistograms();
        }
        long elapsed = System.currentTimeMillis() - start;
        log.info("calculateStatistics() took " + elapsed + "ms");
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

    public ColumnSummaryStats getColumnNonquerySummaryStats(Column column) {
        return columnNonquerySummaryStatsMap.get(column);
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
        columnNonquerySummaryStatsMap.clear();
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