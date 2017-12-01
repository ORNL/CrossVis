package gov.ornl.csed.cda.datatable;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.*;
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

	// Set of queried tuples
	private HashSet<Tuple> queriedTuples;

	// Set of nonQueried tuples (should be tuples - queried tuples)
	private HashSet<Tuple> nonQueriedTuples;

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
		queriedTuples = new HashSet<>();
		nonQueriedTuples = new HashSet<>();
        disabledColumnTuples = new ArrayList<>();
        disabledColumns = new ArrayList<>();

		activeQuery = new Query("Q1");
        listeners = new ArrayList<>();
	}

	public int getNumHistogramBins() {
		return numHistogramBins;
	}

	public void setNumHistogramBins(int numBins) {
		if (numBins != numHistogramBins) {
			numHistogramBins = numBins;
			calculateStatistics();
			if (activeQuery.hasColumnSelections()) {
				calculateQueryStatistics();
			}

			fireNumHistogramBinsChanged();
		}
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
		if (columns.isEmpty()) {
			return;
		}

        numHistogramBins = (int)Math.floor(Math.sqrt(tuples.size()));
        if (numHistogramBins > MAX_NUM_HISTOGRAM_BINS) {
            numHistogramBins = MAX_NUM_HISTOGRAM_BINS;
        }

		highlightedColumn = null;
		this.tuples.clear();
		this.tuples.addAll(tuples);
        disabledColumnTuples.clear();
		this.columns.clear();
		this.columns.addAll(columns);
		this.disabledColumns.clear();
		this.highlightedColumn = null;

		calculateStatistics();
		setQueriedTuples();

		fireDataModelReset();
	}

	public void addTuples(ArrayList<Tuple> newTuples) {
		this.tuples.addAll(newTuples);
		calculateStatistics();

		fireTuplesAdded(newTuples);
	}

	public void clear() {
		tuples.clear();
        disabledColumnTuples.clear();
		clearActiveQuery();
		this.columns.clear();
		this.disabledColumns.clear();
		this.highlightedColumn = null;

		fireDataModelReset();
	}

	public void setColumnName(QuantitativeColumn column, String name) {
		if (columns.contains(column)) {
			column.setName(name);
			fireColumnNameChanged(column);
        } else if (disabledColumns.contains(column)) {
		    column.setName(name);
		    fireColumnNameChanged(column);
		 }
	}

	public ArrayList<QuantitativeColumn> getColumns() {
		return columns;
	}

//	public void setColumns(ArrayList<QuantitativeColumn> columns) {
//		highlightedColumn = null;
//		this.columns.clear();
//		this.columns.addAll(columns);
//		this.tuples.clear();
//		fireDataModelChanged();
//	}

	public ArrayList<Tuple> getTuples() {
		return tuples;
	}

//    public void makeColumnDiscrete(QuantitativeColumn column) {
//        if (column.isContinuous()) {
//            column.makeDiscrete();
//            calculateStatistics();
//            if (getActiveQuery().hasColumnSelections()) {
//                calculateQueryStatistics();
//            }
//            fireDataModelChanged();
//        }
//    }
//
//    public void makeColumnContinuous(QuantitativeColumn column) {
//        if (column.isDiscrete()) {
//            column.makeContinuous();
//            calculateStatistics();
//            if (getActiveQuery().hasColumnSelections()) {
//                calculateQueryStatistics();
//            }
//            fireDataModelChanged();
//        }
//    }

//	public OLSMultipleLinearRegression calculateOLSMultipleLinearRegression(
//			QuantitativeColumn yColumn) {
//		regression = new OLSMultipleLinearRegression();
//		regressionYColumn = yColumn;
//
//		int yItemIndex = getColumnIndex(highlightedColumn);
//
//		double[] y = new double[getTupleCount()];
//		double[][] x = new double[getTupleCount()][getColumnCount() - 1];
//
//		for (int i = 0; i < tuples.size(); i++) {
//			Tuple tuple = tuples.get(i);
//			y[i] = tuple.getElement(yItemIndex);
//
//			for (int j = 0, k = 0; j < getColumnCount(); j++) {
//				if (j == yItemIndex) {
//					continue;
//				}
//				x[i][k++] = tuple.getElement(j);
//			}
//		}
//
//		regression.newSampleData(y, x);
//
//		log.info("Regression results:");
//		log.info("rSquared: " + regression.calculateRSquared()
//				+ " rSquaredAdj: " + regression.calculateAdjustedRSquared());
//		double[] beta = regression.estimateRegressionParameters();
//		for (int i = 0; i < beta.length; i++) {
//			log.info("b[" + i + "]: " + beta[i]);
//		}
//
////		fireDataModelChanged();
//		return regression;
//	}

	public double[] getColumnValues(int columnIndex) {
		QuantitativeColumn column = columns.get(columnIndex);

		double[] values = new double[tuples.size()];

		for (int ituple = 0; ituple < tuples.size(); ituple++) {
			Tuple tuple = tuples.get(ituple);
			values[ituple] = tuple.getElement(columnIndex);
		}

		return values;
	}

	public double[] getColumnQueriedValues(int columnIndex) {
		QuantitativeColumn column = columns.get(columnIndex);

		double[] values = new double[queriedTuples.size()];
//		double[] values = new double[getActiveQuery().getTuples().size()];

//		for (int ituple = 0; ituple < getActiveQuery().getTuples().size(); ituple++) {
//			Tuple tuple = getActiveQuery().getTuples().get(ituple);
//			values[ituple] = tuple.getElement(columnIndex);
//		}

		int tupleCounter = 0;
		for (Tuple tuple : queriedTuples) {
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

	public TemporalColumn getTemporalColumn() {
	    return temporalColumn;
    }

    public boolean hasTemporalColumn() {
		return temporalColumn != null;
	}

	public QuantitativeColumn getColumn(int idx) {
		return columns.get(idx);
	}

	public QuantitativeColumn getColumn(String columnName) {
		for (QuantitativeColumn column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

	public int getColumnIndex(QuantitativeColumn column) {
		return columns.indexOf(column);
	}

	public int getTupleCount() {
		return tuples.size();
	}

	public int getQuantitativeColumnCount() {
		return columns.size();
	}

	public int getTotalColumnCount() {
	    if (temporalColumn == null) {
	        return columns.size();
        }

        return 1 + columns.size();
    }

	public void disableColumn(QuantitativeColumn disabledColumn) {
		if (!disabledColumns.contains(disabledColumn)) {
            int disabledColumnIndex = columns.indexOf(disabledColumn);
            removeTupleElementsForColumn(disabledColumn);
			disabledColumn.setEnabled(false);

			disabledColumn.setQueryMeanValue(Double.NaN);
			disabledColumn.setQueryStandardDeviationValue(Double.NaN);

			if (disabledColumn == this.highlightedColumn) {
				highlightedColumn = null;
//                fireHighlightedColumnChanged();
			}

			disabledColumns.add(disabledColumn);
            columns.remove(disabledColumn);

            getActiveQuery().removeColumnSelectionRanges(disabledColumn);
            setQueriedTuples();
//            clearActiveQueryColumnSelections(disabledColumn);

            for (QuantitativeColumn column : columns) {
                column.getSummaryStats().getCorrelationCoefficients().remove(disabledColumnIndex);
                column.getSummaryStats().getHistogram2DList().remove(disabledColumnIndex);
            }

			fireColumnDisabled(disabledColumn);
		}
	}

    public void disableColumns(ArrayList<QuantitativeColumn> columns) {
		for (QuantitativeColumn column : columns) {
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

	public void enableColumn(QuantitativeColumn column) {
		if (disabledColumns.contains(column)) {
            // move elements from disable column tuples to active tuples list
            addTupleElementsForDisabledColumn(column);
			disabledColumns.remove(column);
			column.setEnabled(true);
            columns.add(column);
            calculateStatistics();
            calculateQueryStatistics();
			// fireDataModelChanged();
			fireColumnEnabled(column);
		}
	}

	public int getDisabledColumnCount() {
		return disabledColumns.size();
	}

	public ArrayList<QuantitativeColumn> getDisabledColumns() {
		return disabledColumns;
	}

	public void clearColumnSelectionRange (ColumnSelectionRange selectionRange) {
		getActiveQuery().removeColumnSelectionRange(selectionRange);
        if (!getActiveQuery().hasColumnSelections()) {
            for (QuantitativeColumn column : columns) {
                column.setQueryMeanValue(Double.NaN);
                column.setQueryStandardDeviationValue(Double.NaN);
            }
        }
		setQueriedTuples();
		fireColumnSelectionRemoved(selectionRange);
	}

	public void clearTemporalColumnSelectionRange (TemporalColumnSelectionRange selectionRange) {
		getActiveQuery().removeTemporalColumnSelectionRange(selectionRange);
		if (!getActiveQuery().hasColumnSelections()) {
//			temporalColumn.setQueryEndInstant(null);
//			temporalColumn.setQueryStartInstant(null);
		}
		setQueriedTuples();
		fireTemporalColumnSelectionRemoved(selectionRange);
	}

    public int removeUnselectedTuples() {
        int tuplesRemoved = 0;

        if (getActiveQuery().hasColumnSelections()) {
//            tuplesRemoved = tuples.size() - getActiveQuery().getTuples().size();
//            tuples.clear();
//            tuples.addAll(getActiveQuery().getTuples());
//            getActiveQuery().clearAllColumnSelections();
			tuplesRemoved = nonQueriedTuples.size();
			tuples.clear();
			tuples.addAll(queriedTuples);
			getActiveQuery().clear();
			calculateStatistics();
			setQueriedTuples();
//            calculateStatistics();
            fireTuplesRemoved(tuplesRemoved);
        }

        return tuplesRemoved;
    }

	public int removeSelectedTuples() {
        int tuplesRemoved = 0;

		if (getActiveQuery().hasColumnSelections()) {
//            tuplesRemoved = getActiveQuery().getTuples().size();
//            tuples.removeAll(getActiveQuery().getTuples());
//            getActiveQuery().clearAllColumnSelections();
//            calculateStatistics();
			tuplesRemoved = queriedTuples.size();
			tuples.clear();
			tuples.addAll(nonQueriedTuples);
			getActiveQuery().clear();
			calculateStatistics();
			setQueriedTuples();
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
        activeQuery = new Query("Q" + (nextQueryNumber++));
        for (QuantitativeColumn column : columns) {
            column.setQueryStandardDeviationValue(Double.NaN);
            column.setQueryMeanValue(Double.NaN);
        }
        fireQueryCleared();
	}

	public void clearActiveQueryColumnSelections(QuantitativeColumn column) {
		if (activeQuery != null) {
            getActiveQuery().removeColumnSelectionRanges(column);
            setQueriedTuples();
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

        setQueriedTuples();

		fireColumnSelectionAdded(newColumnSelectionRange);

		newColumnSelectionRange.rangeValuesProperty().addListener((observable, oldValue, newValue) -> {
			setQueriedTuples();
			fireColumnSelectionChanged(newColumnSelectionRange);
		});
	}

	public void addColumnSelectionRangeToActiveQuery(TemporalColumnSelectionRange temporalColumnSelectionRange) {
		getActiveQuery().addTemporalColumnSelectionRange(temporalColumnSelectionRange);
		setQueriedTuples();
		fireTemporalColumnSelectionAdded(temporalColumnSelectionRange);
		temporalColumnSelectionRange.rangeInstantsProperty().addListener((observable, oldValue, newValue) -> {
			setQueriedTuples();
			fireTemporalColumnSelectionChanged(temporalColumnSelectionRange);
		});
	}

    public void orderColumnsByCorrelation (QuantitativeColumn compareColumn, boolean useQueryCorrelations) {
        int compareColumnIndex = getColumnIndex(compareColumn);

        ArrayList<QuantitativeColumn> newColumnList = new ArrayList<QuantitativeColumn>();
        ArrayList<ColumnSortRecord> positiveColumnList = new ArrayList<ColumnSortRecord>();
        ArrayList<ColumnSortRecord> negativeColumnList = new ArrayList<ColumnSortRecord>();
        ArrayList<ColumnSortRecord> nanColumnList = new ArrayList<ColumnSortRecord>();

        for (int i = 0; i < columns.size(); i++) {
            QuantitativeColumn column = columns.get(i);
            if (column == compareColumn) {
                continue;
            }

			double corrCoef;
            if (useQueryCorrelations) {
                corrCoef = getActiveQuery().getColumnQuerySummaryStats(column).getCorrelationCoefficients().get(compareColumnIndex);
            } else {
                corrCoef = column.getSummaryStats().getCorrelationCoefficients().get(compareColumnIndex);
            }

            ColumnSortRecord columnSortRecord = new ColumnSortRecord(column, corrCoef);
            if (Double.isNaN(corrCoef)) {
                nanColumnList.add(columnSortRecord);
            } else if (corrCoef < 0.) {
                negativeColumnList.add(columnSortRecord);
            } else {
                positiveColumnList.add(columnSortRecord);
            }
        }

        // add negatively correlated axes
        if (!negativeColumnList.isEmpty()) {
            Object sortedRecords[] = negativeColumnList.toArray();
            Arrays.sort(sortedRecords);

            for (Object recordObject : sortedRecords) {
                ColumnSortRecord sortRecord = (ColumnSortRecord)recordObject;
                newColumnList.add(sortRecord.column);
            }
        }

        // compare axis goes between negative and positive correlated axes
        newColumnList.add(compareColumn);

        // add positively correlated axes
        if (!positiveColumnList.isEmpty()) {
            Object sortedRecords[] = positiveColumnList.toArray();
            Arrays.sort(sortedRecords);

            for (Object recordObject : sortedRecords) {
                ColumnSortRecord sortRecord = (ColumnSortRecord)recordObject;
                newColumnList.add(sortRecord.column);
            }
        }

        // add nan axes at bottom of the list
        if (!nanColumnList.isEmpty()) {
            for (ColumnSortRecord sortRecord : nanColumnList) {
                newColumnList.add(sortRecord.column);
            }
        }

        changeColumnOrder(newColumnList);
    }

    public void changeColumnOrder(QuantitativeColumn column, int newColumnIndex) {
		ArrayList<QuantitativeColumn> newColumnOrder = new ArrayList<>(columns);
		newColumnOrder.remove(newColumnOrder.indexOf(column));
		newColumnOrder.add(newColumnIndex, column);

		changeColumnOrder(newColumnOrder);
	}

    public void changeColumnOrder(ArrayList<QuantitativeColumn> newColumnOrder) {
        // determine destination indices for new column order
        int dstColumnIndices[] = new int[newColumnOrder.size()];
        for (int i = 0; i < newColumnOrder.size(); i++) {
            // find index of column in new column order
            QuantitativeColumn column = newColumnOrder.get(i);
            dstColumnIndices[i] = columns.indexOf(column);
        }

        // reset columns array
        columns = newColumnOrder;

        // rearrange column correlation coefficients
        for (int iColumn = 0; iColumn < columns.size(); iColumn++) {
            QuantitativeColumn column = columns.get(iColumn);
            ArrayList<Double> corrCoef = column.getSummaryStats().getCorrelationCoefficients();
            ArrayList<Double> newCorrCoef = new ArrayList<Double>();
            for (int iCorrCoef = 0; iCorrCoef < corrCoef.size(); iCorrCoef++) {
                newCorrCoef.add(corrCoef.get(dstColumnIndices[iCorrCoef]));
            }
            column.getSummaryStats().setCorrelationCoefficients(newCorrCoef);

			ArrayList<Histogram2D> histogram2DList = column.getSummaryStats().getHistogram2DList();
			ArrayList<Histogram2D> newHistogram2DList = new ArrayList<>();
			for (int i = 0; i < histogram2DList.size(); i++) {
				newHistogram2DList.add(histogram2DList.get(dstColumnIndices[i]));
			}
			column.getSummaryStats().setHistogram2DList(newHistogram2DList);
        }

        // move tuple elements to reflect new column order
        for (int iTuple = 0; iTuple < tuples.size(); iTuple++) {
            Tuple tuple = tuples.get(iTuple);
            Double elements[] = tuple.getElementsAsArray();
            tuple.removeAllElements();

            for (int iElement = 0; iElement < elements.length; iElement++) {
                tuple.addElement(elements[dstColumnIndices[iElement]]);
            }
        }

        // move query statistics to reflect new column order
        if (getActiveQuery().hasColumnSelections()) {
            for (int iColumn = 0; iColumn < columns.size(); iColumn++) {
                QuantitativeColumn column = columns.get(iColumn);
                SummaryStats summaryStats = getActiveQuery().getColumnQuerySummaryStats(column);
                ArrayList<Double> corrCoef = summaryStats.getCorrelationCoefficients();
                ArrayList<Double> newCorrCoef = new ArrayList<Double>();
                for (int iCorrCoef = 0; iCorrCoef < corrCoef.size(); iCorrCoef++) {
                    newCorrCoef.add(corrCoef.get(dstColumnIndices[iCorrCoef]));
                }
                summaryStats.setCorrelationCoefficients(newCorrCoef);

				ArrayList<Histogram2D> histogram2DList = summaryStats.getHistogram2DList();
				ArrayList<Histogram2D> newHistogram2DList = new ArrayList<>();
				for (int i = 0; i < histogram2DList.size(); i++) {
					newHistogram2DList.add(histogram2DList.get(dstColumnIndices[i]));
				}
				summaryStats.setHistogram2DList(newHistogram2DList);
            }
        }

        fireColumnOrderChanged();
    }

    public int getQueriedTupleCount() {
//		return getActiveQuery().getTuples().size();
		return queriedTuples.size();
    }

    public Set<Tuple> getQueriedTuples() {
//        return getActiveQuery().getTuples();
		return queriedTuples;
    }

    public Set<Tuple> getNonQueriedTuples() {
		return nonQueriedTuples;
	}

    private void setQueriedTuples() {
		queriedTuples.clear();
		nonQueriedTuples.clear();
//        getActiveQuery().clearTuples();

        if (getTupleCount() == 0) {
            return;
        }

        if (!getActiveQuery().getAllColumnSelectionRanges().isEmpty() ||
				!getActiveQuery().getTemporalColumnSelectionRangeList().isEmpty()) {
            for (int ituple = 0; ituple < getTupleCount(); ituple++) {
                Tuple currentTuple = getTuple(ituple);
                currentTuple.setQueryFlag(true);

                // get temporal queries
				if (hasTemporalColumn()) {
					List<TemporalColumnSelectionRange> temporalColumnSelectionRanges = getActiveQuery().getTemporalColumnSelectionRangeList();
					if (temporalColumnSelectionRanges != null && !temporalColumnSelectionRanges.isEmpty()) {
						boolean insideSelection = false;

						for (TemporalColumnSelectionRange selectionRange : temporalColumnSelectionRanges) {
							if (!((currentTuple.getInstant().isBefore(selectionRange.getStartInstant())) ||
									currentTuple.getInstant().isAfter(selectionRange.getEndInstant()))) {
								insideSelection = true;
								break;
							}
						}

						if (!insideSelection) {
							currentTuple.setQueryFlag(false);
//							break;
						}
					}
				}

                for (int icolumn = 0; icolumn < columns.size(); icolumn++) {
                    QuantitativeColumn column = columns.get(icolumn);
                    ArrayList<ColumnSelectionRange> columnSelectionRanges = getActiveQuery().getColumnSelectionRanges(column);
                    if ((columnSelectionRanges != null) && (!columnSelectionRanges.isEmpty())) {
                        boolean insideSelection = false;

                        for (ColumnSelectionRange selectionRange : columnSelectionRanges) {
                            if ((currentTuple.getElement(icolumn) <= selectionRange.getMaxValue()) &&
                                    (currentTuple.getElement(icolumn) >= selectionRange.getMinValue())) {
                                insideSelection = true;
                                break;
                            }
                        }

                        if (!insideSelection) {
                            currentTuple.setQueryFlag(false);
                            break;
                        }
                    }
                }

                if (currentTuple.getQueryFlag()) {
					queriedTuples.add(currentTuple);
//                    getActiveQuery().addTuple(currentTuple);
                } else {
					nonQueriedTuples.add(currentTuple);
				}
            }

            calculateQueryStatistics();
//			fireQueryChanged();
        } else {
            for (Tuple tuple : tuples) {
                tuple.setQueryFlag(false);
				nonQueriedTuples.add(tuple);
            }
            for (QuantitativeColumn column : columns) {
				column.setQueryMeanValue(Double.NaN);
				column.setQueryStandardDeviationValue(Double.NaN);
			}
        }

        log.info("After setQueriedTuples() with " + tuples.size() + " total tuples: Queried tuples set size is " + queriedTuples.size() + ", nonQueried tuples set size is " + nonQueriedTuples.size());
//        log.info("after setQueriedTuples() number of queried tuples is " + getActiveQuery().getTuples().size() + " queryTuplesSet size " + queriedTuples.size() + " nonQueriedTuplesSet size " + nonQueriedTuples.size());
    }

    // get index of column and remove all tuple elements at this index
    // add the tuple elements to a list of disabledColumnTuples for later enabling
    private void removeTupleElementsForColumn(QuantitativeColumn column) {
        int columnIndex = columns.indexOf(column);

        for (int iTuple = 0; iTuple < tuples.size(); iTuple++) {
            Tuple tuple = tuples.get(iTuple);
            double elementValue = tuple.getElement(columnIndex);
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

    private void addTupleElementsForDisabledColumn(QuantitativeColumn column) {
        int columnIndex = disabledColumns.indexOf(column);
        if (columnIndex != -1) {
            for (int iTuple = 0; iTuple < disabledColumnTuples.size(); iTuple++) {
                Tuple disabledTuple = disabledColumnTuples.get(iTuple);
                double elementValue = disabledTuple.getElement(columnIndex);
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

    private void calculateQueryStatistics() {
        // find start and end of temporal column
//        if (hasTemporalColumn()) {
//            boolean firstIteration = true;
//            for (Tuple tuple : queriedTuples) {
//                if (firstIteration) {
//                    temporalColumn.setQueryStartInstant(tuple.getInstant());
//                    temporalColumn.setQueryEndInstant(tuple.getInstant());
//                    firstIteration = false;
//                } else {
//                    if (tuple.getInstant().isBefore(temporalColumn.getQueryStartInstant())) {
//                        temporalColumn.setQueryStartInstant(tuple.getInstant());
//                    } else if (tuple.getInstant().isAfter(temporalColumn.getQueryEndInstant())) {
//                        temporalColumn.setQueryEndInstant(tuple.getInstant());
//                    }
//                }
//            }
//        }

        double[][] data = new double[columns.size()][];

        for (int icolumn = 0; icolumn < columns.size(); icolumn++) {
            QuantitativeColumn column = columns.get(icolumn);
            data[icolumn] = getColumnQueriedValues(icolumn);
            DescriptiveStatistics stats = new DescriptiveStatistics(data[icolumn]);

            SummaryStats columnSummaryStats = new SummaryStats();
            getActiveQuery().setColumnQuerySummaryStats(column, columnSummaryStats);

			column.setQueryMeanValue(stats.getMean());
			column.setQueryStandardDeviationValue(stats.getStandardDeviation());

            columnSummaryStats.setMean(stats.getMean());
            columnSummaryStats.setMedian(stats.getPercentile(50));
            columnSummaryStats.setVariance(stats.getVariance());
            columnSummaryStats.setStandardDeviation(stats.getStandardDeviation());
            columnSummaryStats.setQuantile1(stats.getPercentile(25));
            columnSummaryStats.setQuantile3(stats.getPercentile(75));
            columnSummaryStats.setSkewness(stats.getSkewness());
            columnSummaryStats.setKurtosis(stats.getKurtosis());
            columnSummaryStats.setMax(stats.getMax());
            columnSummaryStats.setMin(stats.getMin());

            // calculate whiskers for box plot 1.5 of IQR
            double iqr_range = 1.5f * columnSummaryStats.getIQR();
            double lowerFence = columnSummaryStats.getQuantile1() - iqr_range;
            double upperFence = columnSummaryStats.getQuantile3() + iqr_range;
            double sorted_data[] = stats.getSortedValues();

            // find upper datum that is not greater than upper fence
            if (upperFence >= columnSummaryStats.getMax()) {
                columnSummaryStats.setUpperWhisker(columnSummaryStats.getMax());
            } else {
                // find largest datum not larger than upper fence value
                for (int i = sorted_data.length - 1; i >= 0; i--) {
                    if (sorted_data[i] <= upperFence) {
                        columnSummaryStats.setUpperWhisker(sorted_data[i]);
                        break;
                    }
                }
            }

            if (lowerFence <= columnSummaryStats.getMin()) {
                columnSummaryStats.setLowerWhisker(columnSummaryStats.getMin());
            } else {
                // find smallest datum not less than lower fence value
                for (int i = 0; i < sorted_data.length; i++) {
                    if (sorted_data[i] >= lowerFence) {
                        columnSummaryStats.setLowerWhisker(sorted_data[i]);
                        break;
                    }
                }
            }

            // calculate frequency information for column
            Histogram histogram;
            if (column.isContinuous()) {
                histogram = new Histogram(column.getName(), data[icolumn],
						numHistogramBins, column.getSummaryStats().getMin(),
                        column.getSummaryStats().getMax());
            } else {
                int numBins = column.getSummaryStats().getHistogram().getNumBins();
                histogram = new Histogram(column.getName(), data[icolumn], numBins, column.getSummaryStats().getMin(),
                        column.getSummaryStats().getMax());
            }
            columnSummaryStats.setHistogram(histogram);
        }

        PearsonsCorrelation pCorr = new PearsonsCorrelation();

        getActiveQuery().setMaxHistogram2DBinCount(0);
        for (int ix = 0; ix < columns.size(); ix++) {
            QuantitativeColumn column = columns.get(ix);
            SummaryStats columnSummaryStats = getActiveQuery().getColumnQuerySummaryStats(column);

            ArrayList<Double> coefList = new ArrayList<>();
            ArrayList<Histogram2D> histogram2DArrayList = new ArrayList<Histogram2D>();

            for (int iy = 0; iy < columns.size(); iy++) {
                try {
                    double coef = pCorr.correlation(data[ix], data[iy]);
                    coefList.add(coef);
                } catch (Exception ex) {
                    coefList.add(0.);
                }

                // calculate 2D histograms
                // TODO: This could be optimized to reduce some computational complexity
                // TODO: The code current calculates a redundant 2D histogram for each pair of variables
                Histogram2D histogram2D = column.getSummaryStats().getHistogram2DList().get(iy);
                Histogram2D queryHistogram2D = new Histogram2D("", data[ix], data[iy], numHistogramBins,
                        histogram2D.getXMinValue(), histogram2D.getXMaxValue(), histogram2D.getYMinValue(), histogram2D.getYMaxValue());
                histogram2DArrayList.add(queryHistogram2D);

                if (histogram2D.getMaxBinCount() > columnSummaryStats.getMaxHistogram2DCount()) {
                    columnSummaryStats.setMaxHistogram2DCount(histogram2D.getMaxBinCount());
                }
            }

            columnSummaryStats.setCorrelationCoefficients(coefList);
            columnSummaryStats.setHistogram2DList(histogram2DArrayList);

            if (columnSummaryStats.getMaxHistogram2DCount() > getActiveQuery().getMaxHistogram2DBinCount()) {
                getActiveQuery().setMaxHistogram2DBinCount(column.getSummaryStats().getMaxHistogram2DCount());
            }
        }
    }

    private void calculateStatistics() {
		// find start and end of temporal column
		if (hasTemporalColumn()) {
			for (int ituple = 0; ituple < tuples.size(); ituple++) {
				Tuple tuple = tuples.get(ituple);

				if (ituple == 0) {
					temporalColumn.setStartInstant(tuple.getInstant());
					temporalColumn.setEndInstant(tuple.getInstant());
				} else {
					if (tuple.getInstant().isBefore(temporalColumn.getStartInstant())) {
						temporalColumn.setStartInstant(tuple.getInstant());
					} else if (tuple.getInstant().isAfter(temporalColumn.getEndInstant())) {
						temporalColumn.setEndInstant(tuple.getInstant());
					}
				}
			}
		}

        double[][] data = new double[columns.size()][];

        for (int icolumn = 0; icolumn < columns.size(); icolumn++) {
            QuantitativeColumn column = columns.get(icolumn);

            data[icolumn] = getColumnValues(icolumn);

//			int nanCounter = 0;
//			for (double value : data[icolumn]) {
//				if (Double.isNaN(value)) {
//					nanCounter++;
//				}
//			}
//
//			log.info("Number of NaN values for column " + column.getName() + " is " + nanCounter);

            // calculate descriptive statistics
            DescriptiveStatistics stats = new DescriptiveStatistics(data[icolumn]);

            column.getSummaryStats().setMean(stats.getMean());
            column.getSummaryStats().setMedian(stats.getPercentile(50));
            column.getSummaryStats().setVariance(stats.getVariance());
            column.getSummaryStats().setStandardDeviation(stats.getStandardDeviation());
            column.getSummaryStats().setMax(stats.getMax());
            column.getSummaryStats().setMin(stats.getMin());
            column.getSummaryStats().setQuantile1(stats.getPercentile(25));
            column.getSummaryStats().setQuantile3(stats.getPercentile(75));
            column.getSummaryStats().setSkewness(stats.getSkewness());
            column.getSummaryStats().setKurtosis(stats.getKurtosis());

            column.setMeanValue(stats.getMean());
            column.setMinValue(stats.getMin());
            column.setMaxValue(stats.getMax());
            column.setStandardDeviationValue(stats.getStandardDeviation());

            // calculate whiskers for box plot 1.5 of IQR
            double iqr_range = 1.5 * column.getSummaryStats().getIQR();
            double lowerFence = column.getSummaryStats().getQuantile1() - iqr_range;
            double upperFence = column.getSummaryStats().getQuantile3() + iqr_range;
            double sorted_data[] = stats.getSortedValues();

            // find upper datum that is not greater than upper fence
            if (upperFence >= column.getSummaryStats().getMax()) {
                column.getSummaryStats().setUpperWhisker(column.getSummaryStats().getMax());
            } else {
                // find largest datum not larger than upper fence value
                for (int i = sorted_data.length - 1; i >= 0; i--) {
                    if (sorted_data[i] <= upperFence) {
                        column.getSummaryStats().setUpperWhisker(sorted_data[i]);
                        break;
                    }
                }
            }

            if (lowerFence <= column.getSummaryStats().getMin()) {
                column.getSummaryStats().setLowerWhisker(column.getSummaryStats().getMin());
            } else {
                // find smallest datum not less than lower fence value
                for (int i = 0; i < sorted_data.length; i++) {
                    if (sorted_data[i] >= lowerFence) {
                        column.getSummaryStats().setLowerWhisker(sorted_data[i]);
                        break;
                    }
                }
            }

            // calculate frequency information for column
            Histogram histogram;
            if (column.isContinuous()) {
                histogram = new Histogram(column.getName(), data[icolumn],
						numHistogramBins, column.getSummaryStats().getMin(),
                        column.getSummaryStats().getMax());
            } else {
                int numBins = ((int)column.getSummaryStats().getMax() - (int)column.getSummaryStats().getMin()) + 1;
                histogram = new Histogram(column.getName(), data[icolumn], numBins, column.getSummaryStats().getMin(),
                        column.getSummaryStats().getMax());
            }
            column.getSummaryStats().setHistogram(histogram);
        }

        PearsonsCorrelation pCorr = new PearsonsCorrelation();

        for (int ix = 0; ix < columns.size(); ix++) {
            QuantitativeColumn column = columns.get(ix);
            ArrayList<Histogram2D> histogram2DArrayList = new ArrayList<Histogram2D>();
            ArrayList<Double> coefList = new ArrayList<>();

            for (int iy = 0; iy < columns.size(); iy++) {
                double coef = pCorr.correlation(data[ix], data[iy]);
                coefList.add((double) coef);

                // calculate 2D histograms
                // TODO: This could be optimized to reduce some computational complexity
                // TODO: The code current calculates a redundant 2D histogram for each pair of variables
                Histogram2D histogram2D = new Histogram2D("", data[ix], data[iy], numHistogramBins);
                histogram2DArrayList.add(histogram2D);

                if (histogram2D.getMaxBinCount() > column.getSummaryStats().getMaxHistogram2DCount()) {
                    column.getSummaryStats().setMaxHistogram2DCount(histogram2D.getMaxBinCount());
                }
            }

            column.getSummaryStats().setCorrelationCoefficients(coefList);
            column.getSummaryStats().setHistogram2DList(histogram2DArrayList);

            if (column.getSummaryStats().getMaxHistogram2DCount() > maxHistogram2DBinCount) {
                maxHistogram2DBinCount = column.getSummaryStats().getMaxHistogram2DCount();
            }
        }
    }

    public void fireNumHistogramBinsChanged() {
		for (DataModelListener listener : listeners) {
			listener.dataModelNumHistogramBinsChanged(this);
		}
	}

	private void fireColumnDisabled(QuantitativeColumn column) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnDisabled(this, column);
		}
	}

	private void fireColumnsDisabled(ArrayList<QuantitativeColumn> disabledColumns) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnsDisabled(this, disabledColumns);
		}
	}

	private void fireColumnEnabled(QuantitativeColumn column) {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnEnabled(this, column);
		}
	}

	private void fireColumnOrderChanged() {
		for (DataModelListener listener : listeners) {
			listener.dataModelColumnOrderChanged(this);
		}
	}

	private void fireColumnNameChanged(QuantitativeColumn column) {
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

	public void fireHighlightedColumnChanged(QuantitativeColumn oldHighlightedColumn) {
		for (DataModelListener listener : listeners) {
			listener.dataModelHighlightedColumnChanged(this, oldHighlightedColumn, highlightedColumn);
		}
	}

	public void fireTemporalColumnSelectionAdded(TemporalColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelTemporalColumnSelectionAdded(this, columnSelectionRange);
		}
	}

	public void fireTemporalColumnSelectionRemoved(TemporalColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelTemporalColumnSelectionRemoved(this, columnSelectionRange);
		}
	}

	public void fireTemporalColumnSelectionChanged(TemporalColumnSelectionRange columnSelectionRange) {
		for (DataModelListener listener : listeners) {
			listener.dataModelTemporalColumnSelectionChanged(this, columnSelectionRange);
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

	public void fireQueryColumnCleared(QuantitativeColumn column) {
        for (DataModelListener listener : listeners) {
            listener.dataModelQueryColumnCleared(this, column);
        }
    }
}
