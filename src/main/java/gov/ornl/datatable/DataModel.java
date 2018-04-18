package gov.ornl.datatable;

import javafx.collections.SetChangeListener;

import java.util.ArrayList;
import java.util.logging.Logger;

public class DataModel {
	private static final int DEFAULT_NUM_HISTOGRAM_BINS = 50;
    private static final int MAX_NUM_HISTOGRAM_BINS = 100;

    private final static Logger log = Logger.getLogger(DataModel.class.getName());

    // List of enabled tuples
	protected ArrayList<Tuple> tuples;

    // List of enabled columns
	protected ArrayList<Column> columns;

    // List of disabled columns
	protected ArrayList<Column> disabledColumns;

    // List of tuple elements for disabled columns
    protected ArrayList<Tuple> disabledColumnTuples;

    // List of active listeners
	private ArrayList<DataModelListener> listeners;

    // Special columns
	private Column highlightedColumn = null;

    // List of saved queries
	private ArrayList<Query> savedQueryList;

    // Current active query
	private Query activeQuery;

    // Sequential number for future query IDs
	private int nextQueryNumber = 2;

    // The number of histogram bins to use
	private int numHistogramBins = DEFAULT_NUM_HISTOGRAM_BINS;

    // Current maximum 2D histogram bin count (the max count of all 2D histograms)
    private int maxHistogram2DBinCount = 0;


	public DataModel() {
        tuples = new ArrayList<>();
        columns = new ArrayList<>();
        disabledColumnTuples = new ArrayList<>();
        disabledColumns = new ArrayList<>();

		activeQuery = new Query("Q1", this);
        listeners = new ArrayList<>();
	}

	public int getNumHistogramBins() {
		return numHistogramBins;
	}

	public void setNumHistogramBins(int numBins) {
		if (numBins != numHistogramBins) {
			numHistogramBins = numBins;
			for (Column column : columns) {
			    column.getStatistics().setNumHistogramBins(numHistogramBins);
            }
			calculateStatistics();
			if (activeQuery.hasColumnSelections()) {
				activeQuery.calculateStatistics();
			}

			fireNumHistogramBinsChanged();
		}
	}

	public ArrayList<DoubleColumn> getDoubleColumns() {
		ArrayList<DoubleColumn> doubleColumns = new ArrayList<>();
		for (Column column : columns) {
			if (column instanceof DoubleColumn) {
				doubleColumns.add((DoubleColumn)column);
			}
		}
		for (Column column : disabledColumns) {
			if (column instanceof DoubleColumn) {
				doubleColumns.add((DoubleColumn)column);
			}
		}

		if (doubleColumns.isEmpty()) {
			return null;
		}
		return doubleColumns;
	}

	public ArrayList<TemporalColumn> getTemporalColumns() {
		ArrayList<TemporalColumn> temporalColumns = new ArrayList<>();
		for (Column column : columns) {
			if (column instanceof TemporalColumn) {
				temporalColumns.add((TemporalColumn)column);
			}
		}
		for (Column column : disabledColumns) {
			if (column instanceof TemporalColumn) {
				temporalColumns.add((TemporalColumn)column);
			}
		}

		if (temporalColumns.isEmpty()) {
			return null;
		}
		return temporalColumns;
	}

	public ArrayList<CategoricalColumn> getCategoricalColumns() {
		ArrayList<CategoricalColumn> categoricalColumns = new ArrayList<>();
		for (Column column : columns) {
			if (column instanceof CategoricalColumn) {
				categoricalColumns.add((CategoricalColumn)column);
			}
		}
		for (Column column : disabledColumns) {
			if (column instanceof CategoricalColumn) {
				categoricalColumns.add((CategoricalColumn)column);
			}
		}

		if (categoricalColumns.isEmpty()) {
			return null;
		}
		return categoricalColumns;
	}

	public final Query getActiveQuery() { return activeQuery; }

	public int getMaxHistogram2DBinCount() { return maxHistogram2DBinCount; }

	public boolean isEmpty() {
		return tuples.isEmpty();
	}

	public Column getHighlightedColumn() {
		return highlightedColumn;
	}

	public void setHighlightedColumn(Column column) {
        if (column != highlightedColumn) {
           	Column oldHighlightedColumn = highlightedColumn;

            if (column == null) {
                highlightedColumn = null;
                fireHighlightedColumnChanged(oldHighlightedColumn);
            } else if (columns.contains(column)) {
                highlightedColumn = column;
                fireHighlightedColumnChanged(oldHighlightedColumn);
            }
        }
	}

	public void setData(ArrayList<Tuple> tuples, ArrayList<Column> columns) {
		clearDataModel();

		if (columns.isEmpty()) {
			return;
		}

        if (tuples != null && !tuples.isEmpty()) {
			this.tuples.addAll(tuples);
			numHistogramBins = (int)Math.floor(Math.sqrt(tuples.size()));
			if (numHistogramBins > MAX_NUM_HISTOGRAM_BINS) {
				numHistogramBins = MAX_NUM_HISTOGRAM_BINS;
			}
		}

		this.columns.addAll(columns);
		for (Column column : this.columns) {
			column.setDataModel(this);
		}

		calculateStatistics();
		getActiveQuery().setQueriedTuples();
		fireDataModelReset();
	}

	public void addTuples(ArrayList<Tuple> newTuples) {
		this.tuples.addAll(newTuples);
		calculateStatistics();

		fireTuplesAdded(newTuples);
	}

	public void clear() {
		clearDataModel();
		fireDataModelReset();
	}

	private void clearDataModel() {
		tuples.clear();
		disabledColumnTuples.clear();
		clearActiveQuery();
		this.columns.clear();
		this.disabledColumns.clear();
		this.highlightedColumn = null;
	}

	public void setColumnName(Column column, String name) {
		if (columns.contains(column)) {
			column.setName(name);
			fireColumnNameChanged(column);
        } else if (disabledColumns.contains(column)) {
		    column.setName(name);
		    fireColumnNameChanged(column);
		 }
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public ArrayList<Tuple> getTuples() {
		return tuples;
	}

	public Object[] getColumnValues(Column column) {
	    int index = getColumnIndex(column);
	    return getColumnValues(index);
    }

	public Object[] getColumnValues(int columnIndex) {
		Column column = columns.get(columnIndex);

		Object[] values = new Object[tuples.size()];

		for (int ituple = 0; ituple < tuples.size(); ituple++) {
			Tuple tuple = tuples.get(ituple);
			values[ituple] = tuple.getElement(columnIndex);
		}

		return values;
	}

	public Object[] getColumnQueriedValues(int columnIndex) {
		Column column = columns.get(columnIndex);

		Object[] values = new Object[getActiveQuery().getQueriedTuples().size()];

		int tupleCounter = 0;
		for (Tuple tuple : getActiveQuery().getQueriedTuples()) {
			values[tupleCounter++] = tuple.getElement(columnIndex);
		}

		return values;
	}

	public void addDataModelListener(DataModelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public boolean removeDataModelListener(DataModelListener listener) {
        return listeners.remove(listener);
    }

	public Tuple getTuple(int idx) {
		return tuples.get(idx);
	}

	public Column getColumn(int idx) {
		return columns.get(idx);
	}

	public Column getColumn(String columnName) {
		for (Column column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	public int getColumnIndex(Column column) {
		return columns.indexOf(column);
	}

	public int getTupleCount() {
		return tuples.size();
	}

	public int getColumnCount() {
	    return columns.size();
    }

	public void disableColumn(Column disabledColumn) {
		if (!disabledColumns.contains(disabledColumn)) {
            removeTupleElementsForColumn(disabledColumn);
			disabledColumn.setEnabled(false);

			if (disabledColumn == this.highlightedColumn) {
				highlightedColumn = null;
//                fireHighlightedColumnChanged();
			}

			disabledColumns.add(disabledColumn);
            columns.remove(disabledColumn);

            getActiveQuery().removeColumnSelectionRanges(disabledColumn);
            getActiveQuery().setQueriedTuples();

			fireColumnDisabled(disabledColumn);
		}
	}

    public void disableColumns(ArrayList<Column> columns) {
		for (Column column : columns) {
			if (!disabledColumns.contains(column)) {
                removeTupleElementsForColumn(column);
				column.setEnabled(false);

                if (column == this.highlightedColumn) {
					highlightedColumn = null;
//                    fireHighlightedColumnChanged();
				}

				disabledColumns.add(column);
                columns.remove(column);
                clearActiveQueryColumnSelections(column);
			}
		}

		fireColumnsDisabled(columns);
	}

	public void enableColumn(Column column) {
		if (disabledColumns.contains(column)) {
            // move elements from disable column tuples to active tuples list
            addTupleElementsForDisabledColumn(column);
			disabledColumns.remove(column);
			column.setEnabled(true);
            columns.add(column);
            calculateStatistics();
            getActiveQuery().calculateStatistics();
			// fireDataModelChanged();
			fireColumnEnabled(column);
		}
	}

	public int getDisabledColumnCount() {
		return disabledColumns.size();
	}

	public ArrayList<Column> getDisabledColumns() {
		return disabledColumns;
	}

	public void clearColumnSelectionRange (ColumnSelectionRange selectionRange) {
		getActiveQuery().removeColumnSelectionRange(selectionRange);
		getActiveQuery().setQueriedTuples();
		fireColumnSelectionRemoved(selectionRange);
	}

    public int removeUnselectedTuples() {
        int tuplesRemoved = 0;

        if (getActiveQuery().hasColumnSelections()) {
			tuplesRemoved = getActiveQuery().getNonQueriedTuples().size();
			tuples.clear();
			tuples.addAll(getActiveQuery().getQueriedTuples());
			getActiveQuery().clear();
			calculateStatistics();
			getActiveQuery().setQueriedTuples();
            fireTuplesRemoved(tuplesRemoved);
        }

        return tuplesRemoved;
    }

	public int removeSelectedTuples() {
        int tuplesRemoved = 0;

		if (getActiveQuery().hasColumnSelections()) {
			tuplesRemoved = getActiveQuery().getQueriedTuples().size();
			tuples.clear();
			tuples.addAll(getActiveQuery().getNonQueriedTuples());
			getActiveQuery().clear();
			calculateStatistics();
			getActiveQuery().setQueriedTuples();
            fireTuplesRemoved(tuplesRemoved);
        }

        return tuplesRemoved;
	}

//	public void saveActiveQuery() {
//		savedQueryList.add(activeQuery);
//		activeQuery = new Query("Q"+(nextQueryNumber++));
//	}

//	public Query getActiveQuery() {
//		return activeQuery;
//	}

//	public void setActiveQuery(String queryID) {
//		for (Query query : savedQueryList) {
//			if (query.getID().equals(queryID)) {
//				activeQuery = query;
//				savedQueryList.remove(query);
//				return;
//			}
//		}
//	}

	public void clearActiveQuery() {
        activeQuery = new Query("Q" + (nextQueryNumber++), this);
        fireQueryCleared();
	}

	public void clearActiveQueryColumnSelections(Column column) {
		if (activeQuery != null) {
            getActiveQuery().removeColumnSelectionRanges(column);
            getActiveQuery().setQueriedTuples();
			fireQueryColumnCleared(column);
		}
	}

//	public ArrayList<Query> getSavedQueryList() {
//		return savedQueryList;
//	}

//	public Query getQueryByID(String ID) {
//		if (activeQuery.getID().equals(ID)) {
//			return activeQuery;
//		} else {
//			for (Query query : savedQueryList) {
//				if (query.getID().equals(ID)) {
//					return query;
//				}
//			}
//		}
//
//		return null;
//	}

	public void addColumnSelectionRangeToActiveQuery(ColumnSelectionRange newColumnSelectionRange) {
		getActiveQuery().addColumnSelectionRange(newColumnSelectionRange);

        getActiveQuery().setQueriedTuples();

		fireColumnSelectionAdded(newColumnSelectionRange);

		if (newColumnSelectionRange instanceof DoubleColumnSelectionRange) {
			((DoubleColumnSelectionRange)newColumnSelectionRange).rangeValuesProperty().addListener((observable, oldValue, newValue) -> {
				getActiveQuery().setQueriedTuples();
				fireColumnSelectionChanged(newColumnSelectionRange);
			});
		} else if (newColumnSelectionRange instanceof TemporalColumnSelectionRange) {
			((TemporalColumnSelectionRange)newColumnSelectionRange).rangeInstantsProperty().addListener((observable, oldValue, newValue) -> {
				getActiveQuery().setQueriedTuples();
				fireColumnSelectionChanged(newColumnSelectionRange);
			});
		} else if (newColumnSelectionRange instanceof CategoricalColumnSelection) {
			((CategoricalColumnSelection)newColumnSelectionRange).selectedCategoriesProperty().addListener(new SetChangeListener<String>() {
			    @Override
                public void onChanged(Change<? extends String> change) {
			        log.info("categorical column selection changed");
			        if (((CategoricalColumnSelection) newColumnSelectionRange).getSelectedCategories().isEmpty()) {
			        	getActiveQuery().removeColumnSelectionRange(newColumnSelectionRange);
					}
			        getActiveQuery().setQueriedTuples();
			        fireColumnSelectionChanged(newColumnSelectionRange);
                }
			});
//            ((CategoricalColumnSelection)newColumnSelectionRange).selectedCategoriesProperty().addListener((observable, oldValue, newValue) -> {
//                getActiveQuery().setQueriedTuples();
//                fireColumnSelectionChanged(newColumnSelectionRange);
//            });
        }
	}

//    public void orderColumnsByCorrelation (DoubleColumn compareColumn, boolean useQueryCorrelations) {
//        int compareColumnIndex = getColumnIndex(compareColumn);
//
//        ArrayList<DoubleColumn> newColumnList = new ArrayList<DoubleColumn>();
//        ArrayList<ColumnSortRecord> positiveColumnList = new ArrayList<ColumnSortRecord>();
//        ArrayList<ColumnSortRecord> negativeColumnList = new ArrayList<ColumnSortRecord>();
//        ArrayList<ColumnSortRecord> nanColumnList = new ArrayList<ColumnSortRecord>();
//
//        for (int i = 0; i < columns.size(); i++) {
//            DoubleColumn column = columns.get(i);
//            if (column == compareColumn) {
//                continue;
//            }
//
//			double corrCoef;
//            if (useQueryCorrelations) {
//                corrCoef = getActiveQuery().getColumnQuerySummaryStats(column).getCorrelationCoefficients().get(compareColumnIndex);
//            } else {
//                corrCoef = column.getSummaryStats().getCorrelationCoefficients().get(compareColumnIndex);
//            }
//
//            ColumnSortRecord columnSortRecord = new ColumnSortRecord(column, corrCoef);
//            if (Double.isNaN(corrCoef)) {
//                nanColumnList.add(columnSortRecord);
//            } else if (corrCoef < 0.) {
//                negativeColumnList.add(columnSortRecord);
//            } else {
//                positiveColumnList.add(columnSortRecord);
//            }
//        }
//
//        // add negatively correlated axes
//        if (!negativeColumnList.isEmpty()) {
//            Object sortedRecords[] = negativeColumnList.toArray();
//            Arrays.sort(sortedRecords);
//
//            for (Object recordObject : sortedRecords) {
//                ColumnSortRecord sortRecord = (ColumnSortRecord)recordObject;
//                newColumnList.add(sortRecord.column);
//            }
//        }
//
//        // compare axis goes between negative and positive correlated axes
//        newColumnList.add(compareColumn);
//
//        // add positively correlated axes
//        if (!positiveColumnList.isEmpty()) {
//            Object sortedRecords[] = positiveColumnList.toArray();
//            Arrays.sort(sortedRecords);
//
//            for (Object recordObject : sortedRecords) {
//                ColumnSortRecord sortRecord = (ColumnSortRecord)recordObject;
//                newColumnList.add(sortRecord.column);
//            }
//        }
//
//        // add nan axes at bottom of the list
//        if (!nanColumnList.isEmpty()) {
//            for (ColumnSortRecord sortRecord : nanColumnList) {
//                newColumnList.add(sortRecord.column);
//            }
//        }
//
//        changeColumnOrder(newColumnList);
//    }

    public void changeColumnOrder(Column column, int newColumnIndex) {
		ArrayList<Column> newColumnOrder = new ArrayList<>(columns);
		newColumnOrder.remove(newColumnOrder.indexOf(column));
		newColumnOrder.add(newColumnIndex, column);

		changeColumnOrder(newColumnOrder);
	}

    public void changeColumnOrder(ArrayList<Column> newColumnOrder) {
        // determine destination indices for new column order
        int dstColumnIndices[] = new int[newColumnOrder.size()];
        for (int i = 0; i < newColumnOrder.size(); i++) {
            // find index of column in new column order
            Column column = newColumnOrder.get(i);
            dstColumnIndices[i] = columns.indexOf(column);
        }

        // reset columns array
        columns = newColumnOrder;

        /*
        // rearrange column correlation coefficients
        for (int iColumn = 0; iColumn < columns.size(); iColumn++) {
            Column column = columns.get(iColumn);
            ArrayList<Double> corrCoef = column.getSummaryStats().getCorrelationCoefficients();
            ArrayList<Double> newCorrCoef = new ArrayList<Double>();
            for (int iCorrCoef = 0; iCorrCoef < corrCoef.size(); iCorrCoef++) {
                newCorrCoef.add(corrCoef.get(dstColumnIndices[iCorrCoef]));
            }
            column.getSummaryStats().setCorrelationCoefficients(newCorrCoef);

			ArrayList<Histogram2DOLD> histogram2DList = column.getSummaryStats().getHistogram2DList();
			ArrayList<Histogram2DOLD> newHistogram2DList = new ArrayList<>();
			for (int i = 0; i < histogram2DList.size(); i++) {
				newHistogram2DList.add(histogram2DList.get(dstColumnIndices[i]));
			}
			column.getSummaryStats().setHistogram2DList(newHistogram2DList);
        }
        */

        // move tuple elements to reflect new column order
        for (int iTuple = 0; iTuple < tuples.size(); iTuple++) {
            Tuple tuple = tuples.get(iTuple);
            Object elements[] = tuple.getElementsAsArray();
            tuple.removeAllElements();

            for (int iElement = 0; iElement < elements.length; iElement++) {
                tuple.addElement(elements[dstColumnIndices[iElement]]);
            }
        }

        // move query statistics to reflect new column order
        /*
		if (getActiveQuery().hasColumnSelections()) {
            for (int iColumn = 0; iColumn < columns.size(); iColumn++) {
                Column column = columns.get(iColumn);
                ColumnSummaryStats summaryStats = getActiveQuery().getColumnQuerySummaryStats(column);
                ArrayList<Double> corrCoef = summaryStats.getCorrelationCoefficients();
                ArrayList<Double> newCorrCoef = new ArrayList<Double>();
                for (int iCorrCoef = 0; iCorrCoef < corrCoef.size(); iCorrCoef++) {
                    newCorrCoef.add(corrCoef.get(dstColumnIndices[iCorrCoef]));
                }
                summaryStats.setCorrelationCoefficients(newCorrCoef);

				ArrayList<Histogram2DOLD> histogram2DList = summaryStats.getHistogram2DList();
				ArrayList<Histogram2DOLD> newHistogram2DList = new ArrayList<>();
				for (int i = 0; i < histogram2DList.size(); i++) {
					newHistogram2DList.add(histogram2DList.get(dstColumnIndices[i]));
				}
				summaryStats.setHistogram2DList(newHistogram2DList);
            }
        }
        */

        fireColumnOrderChanged();
    }

    // get index of column and remove all tuple elements at this index
    // add the tuple elements to a list of disabledColumnTuples for later enabling
    private void removeTupleElementsForColumn(Column column) {
        int columnIndex = columns.indexOf(column);

        for (int iTuple = 0; iTuple < tuples.size(); iTuple++) {
            Tuple tuple = tuples.get(iTuple);
            Object elementValue = tuple.getElement(columnIndex);
            tuple.removeElement(columnIndex);

            if (disabledColumnTuples.size() != tuples.size()) {
                Tuple disabledTuple = new Tuple();
                disabledTuple.addElement(elementValue);
                disabledColumnTuples.add(disabledTuple);
            } else {
                Tuple disabledTuple = disabledColumnTuples.get(iTuple);
                disabledTuple.addElement(elementValue);
            }
        }
    }

    private void addTupleElementsForDisabledColumn(Column column) {
        int columnIndex = disabledColumns.indexOf(column);
        if (columnIndex != -1) {
            for (int iTuple = 0; iTuple < disabledColumnTuples.size(); iTuple++) {
                Tuple disabledTuple = disabledColumnTuples.get(iTuple);
                Object elementValue = disabledTuple.getElement(columnIndex);
                disabledTuple.removeElement(columnIndex);

                if (disabledColumnTuples.size() != tuples.size()) {
                    Tuple tuple = new Tuple();
                    tuple.addElement(elementValue);
                    tuples.add(tuple);
                } else {
                    Tuple tuple = tuples.get(iTuple);
                    tuple.addElement(elementValue);
                }
            }
        }
    }

    private void calculateStatistics() {
        for (Column column : columns) {
            column.calculateStatistics();
        }

        calculateColumn2DHistograms();
    }

    private void calculateColumn2DHistograms() {
        maxHistogram2DBinCount = 0;
        for (Column column : columns) {
            for (Column compareColumn : columns) {
                if (column != compareColumn) {
                    column.getStatistics().calculateHistogram2D(compareColumn.getStatistics());
                    if (column.getStatistics().getMaxHistogram2DBinCount() > maxHistogram2DBinCount) {
                        maxHistogram2DBinCount = column.getStatistics().getMaxHistogram2DBinCount();
                    }
                }
            }
        }
    }

    public void fireNumHistogramBinsChanged() {
		for (DataModelListener listener : listeners) {
			listener.dataModelNumHistogramBinsChanged(this);
		}
	}

	private void fireColumnDisabled(Column column) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnDisabled(this, column);
		}
	}

	private void fireColumnsDisabled(ArrayList<Column> disabledColumns) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnsDisabled(this, disabledColumns);
		}
	}

	private void fireColumnEnabled(Column column) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnEnabled(this, column);
		}
	}

	private void fireColumnOrderChanged() {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnOrderChanged(this);
		}
	}

	private void fireColumnNameChanged(Column column) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnNameChanged(this, column);
		}
	}

	private void fireDataModelReset() {
		for (DataModelListener listener : listeners) {
			listener.dataModelReset(this);
		}
	}

	private void fireTuplesAdded(ArrayList<Tuple> newTuples) {
		for (DataModelListener listener : listeners) {
			listener.dataModelTuplesAdded(this, newTuples);
		}
	}

	private void fireTuplesRemoved(int numTuplesRemoved) {
		for (DataModelListener listener : listeners) {
			listener.dataModelTuplesRemoved(this, numTuplesRemoved);
		}
	}

	public void fireHighlightedColumnChanged(Column oldHighlightedColumn) {
		for (DataModelListener listener : listeners) {
			listener.dataModelHighlightedColumnChanged(this, oldHighlightedColumn, highlightedColumn);
		}
	}

	public void fireColumnSelectionAdded(ColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnSelectionAdded(this, columnSelectionRange);
		}
	}

	public void fireColumnSelectionRemoved(ColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnSelectionRemoved(this, columnSelectionRange);
		}
	}

	public void fireColumnSelectionChanged(ColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnSelectionChanged(this, columnSelectionRange);
		}
	}

	public void fireQueryCleared() {
		for (DataModelListener listener : listeners) {
			listener.dataModelQueryCleared(this);
		}
	}

	public void fireQueryColumnCleared(Column column) {
        for (DataModelListener listener : listeners) {
            listener.dataModelQueryColumnCleared(this, column);
        }
    }
}