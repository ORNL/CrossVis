package gov.ornl.csed.cda.coalesce;

import gov.ornl.csed.cda.datatable.*;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ColumnTableModel extends AbstractTableModel implements
        DataModelListener {

	private static final long serialVersionUID = 1L;
	private DataModel dataModel;
	private String[] columnHeaders = { "Name", "Show", "Highlighted", "Discrete", "Time", "Mean", "Median",
			"StDev", "Variance", "Q1", "Q3", "IQR", "Skewness", "Kustosis",
			"Min", "Max"};
	private boolean showQueryStatistics = false;

	public ColumnTableModel(DataModel dataModel) {
		this.dataModel = dataModel;
		dataModel.addDataModelListener(this);
	}

	public void setShowQueryStatistics(boolean showQueryStatistics) {
		if (this.showQueryStatistics != showQueryStatistics) {
			this.showQueryStatistics = showQueryStatistics;
			this.fireTableDataChanged();
		}
	}

	public boolean isShowingQueryStatistics() {
		return showQueryStatistics;
	}

	@Override
	public void dataModelChanged(DataModel dataModel) {
		this.fireTableDataChanged();
	}

	@Override
	public void highlightedColumnChanged(DataModel dataModel) {
        fireTableDataChanged();
	}

	public Class getColumnClass(int c) {
		if (c == 0) {
            return String.class;
        } else if (c == 1) {
            return Boolean.class;
        } else if (c == 2) {
            return Boolean.class;
        } else if (c == 3) {
            return Boolean.class;
        } else if (c == 4) {
            return Boolean.class;
		} else {
			return Double.class;
		}
	}

	@Override
	public int getColumnCount() {
		return columnHeaders.length;
	}

	@Override
	public int getRowCount() {
		if (dataModel != null) {
			return dataModel.getColumnCount() + dataModel.getDisabledColumnCount();
		}
		return 0;
	}

	public String getColumnName(int column) {
		return columnHeaders[column];
	}

	public boolean isCellEditable(int row, int col) {
		if (col >= 0 && col <= 4) {
			return true;
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
        Column column = null;
		if (row >= 0 && row < dataModel.getColumnCount()) {
            column = dataModel.getColumn(row);
        } else if (row >= dataModel.getColumnCount()) {
            int adjustedRow = row - dataModel.getColumnCount();
            column = dataModel.getDisabledColumns().get(adjustedRow);
        }

        if (column != null) {
            // modifying an enabled column
			if (col == 0) {
                String newName = (String) value;
                dataModel.setColumnName(column, newName);
            } else if (col == 1) {
                if ((Boolean) value == true) {
                    dataModel.enableColumn(column);
                } else {
                    dataModel.disableColumn(column);
                }
            } else if (col == 2) {
                if ((Boolean) value == true) {
                    dataModel.setHighlightedColumn(column);
                } else {
                    dataModel.clearHighlightedColumn();
                }
            } else if (col == 3) {
                if ((Boolean) value == true) {
                    dataModel.makeColumnDiscrete(column);
                } else {
                    dataModel.makeColumnContinuous(column);
                }
            } else if (col == 4) {
                if ((Boolean) value == true) {
                    dataModel.setTimeColumn(column);
                } else {
                    dataModel.clearTimeColumn();
                }
//			} else if (col == 13) {
//				// set the minimum query value
//				float floatValue = ((Float) value).floatValue();
//				if (floatValue <= column.getMaxValue()
//						&& floatValue >= column.getMinValue()
//						&& floatValue <= column.getMaxQueryValue()) {
//					column.setMinQueryValue(floatValue);
//					dataModel.setQueriedTuples();
//					// dataModel.fireQueryChanged();
//				}
//			} else if (col == 14) {
//				float floatValue = ((Float) value).floatValue();
//				if (floatValue <= column.getMaxValue()
//						&& floatValue >= column.getMinValue()
//						&& floatValue >= column.getMinQueryValue()) {
//					column.setMaxQueryValue(floatValue);
//					dataModel.setQueriedTuples();
//					// dataModel.fireQueryChanged();
//				}
			}
		}
	}

	@Override
	public Object getValueAt(int row, int col) {

		if (dataModel != null && (dataModel.getColumnCount() > 0 || dataModel.getDisabledColumnCount() > 0)) {

            Column column = null;
            if (row >= 0 && row < dataModel.getColumnCount()) {
                column = dataModel.getColumn(row);
            } else if (row >= dataModel.getColumnCount()) {
                int adjustedRow = row - dataModel.getColumnCount();
                column = dataModel.getDisabledColumns().get(adjustedRow);
            }

            if (column != null) {


//                Column column = null;
//			if (row >= 0 && row < dataModel.getColumnCount()) {
//				column = dataModel.getColumn(row);
//			} else {
//				return null;
//			}

                if (col == 0) {
                    return column.getName();
                } else if (col == 1) {
                    return new Boolean(column.isEnabled());
                } else if (col == 2) {
                    return new Boolean(column == dataModel.getHighlightedColumn());
                } else if (col == 3) {
                    return new Boolean(column.isDiscrete());
                } else if (col == 4) {
                    return new Boolean(column == dataModel.getTimeColumn());
                } else if (col == 5) {
                    if (showQueryStatistics) {
                        //					return column.getQueryMean();
                    } else {
                        return column.getSummaryStats().getMean();
                    }
                } else if (col == 6) {
                    if (showQueryStatistics) {
                        //					return column.getQueryMedian();
                    } else {
                        return column.getSummaryStats().getMedian();
                    }
                } else if (col == 7) {
                    if (showQueryStatistics) {
                        //					return column.getQueryStandardDeviation();
                    } else {
                        return column.getSummaryStats().getStandardDeviation();
                    }
                } else if (col == 8) {
                    if (showQueryStatistics) {
                        //					return column.getQueryVariance();
                    } else {
                        return column.getSummaryStats().getVariance();
                    }
                } else if (col == 9) {
                    if (showQueryStatistics) {
                        //					return column.getQueryQ1();
                    } else {
                        return column.getSummaryStats().getQuantile1();
                    }
                } else if (col == 10) {
                    if (showQueryStatistics) {
                        //					return column.getQueryQ3();
                    } else {
                        return column.getSummaryStats().getQuantile3();
                    }
                } else if (col == 11) {
                    if (showQueryStatistics) {
                        //					return column.getQueryIQR();
                    } else {
                        return column.getSummaryStats().getIQR();
                    }
                } else if (col == 12) {
                    if (showQueryStatistics) {
                        //					return column.getQuerySkewness();
                    } else {
                        return column.getSummaryStats().getSkewness();
                    }
                } else if (col == 13) {
                    if (showQueryStatistics) {
                        //					return column.getQueryKurtosis();
                    } else {
                        return column.getSummaryStats().getKurtosis();
                    }
                } else if (col == 14) {
                    if (showQueryStatistics) {
                        //					return column.getQueryMinValue();
                    } else {
                        return column.getSummaryStats().getMin();
                    }
                } else if (col == 15) {
                    if (showQueryStatistics) {
                        //					return column.getQueryMaxValue();
                    } else {
                        return column.getSummaryStats().getMax();
                    }
                }
            }
		}

		return null;
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		fireTableDataChanged();
	}

	@Override
	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
		this.fireTableDataChanged();
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

	@Override
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		fireTableDataChanged();
	}

	@Override
	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
		fireTableDataChanged();
	}
}