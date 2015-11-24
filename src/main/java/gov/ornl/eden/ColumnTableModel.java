package gov.ornl.eden;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.DataModelListener;
import gov.ornl.datatable.Tuple;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ColumnTableModel extends AbstractTableModel implements DataModelListener {

	private static final long serialVersionUID = 1L;
	private DataModel dataModel;
	private String[] columnHeaders = { "Display", "Variable", "Mean", "Median",
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
		// dataModel.getColumnIndex(dataModel.getHighlightedColumn());
	}

	public Class getColumnClass(int c) {
		if (c == 0) {
			return Boolean.class;
		} else if (c == 1) {
			return String.class;
		} else {
			return Float.class;
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
		if (col == 0 || col == 1) {
			return true;
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
        if (dataModel != null && row >= 0) {
            Column column = null;
            if (row < dataModel.getColumnCount()) {
                column = dataModel.getColumn(row);
            } else if ((row >= dataModel.getColumnCount()) &&
                    (row < dataModel.getColumnCount() + dataModel.getDisabledColumnCount())) {
                int disabledColumnIndex = row - dataModel.getColumnCount();
                column = dataModel.getDisabledColumns().get(disabledColumnIndex);
            }

            if (col == 0) {
				if ((Boolean) value == true) {
					dataModel.enableColumn(column);
				} else {
					dataModel.disableColumn(column);
				}
			} else if (col == 1) {
                String newName = (String) value;
                dataModel.setColumnName(column, newName);
            }
        }
//		if (row >= 0 && row < dataModel.getColumnCount()) {
//			// modifying an enabled column
//			Column column = dataModel.getColumn(row);
//
//			if (col == 0) {
//				if ((Boolean) value == true) {
//					dataModel.enableColumn(column);
//				} else {
//					dataModel.disableColumn(column);
//				}
//				// disabling a column
//				// dataModel.disableColumn(column);
//			} else if (col == 1) {
//				String newName = (String) value;
//				dataModel.setColumnName(column, newName);
////			} else if (col == 13) {
////				// set the minimum query value
////				float floatValue = ((Float) value).floatValue();
////				if (floatValue <= column.getMaxValue()
////						&& floatValue >= column.getMinValue()
////						&& floatValue <= column.getMaxQueryValue()) {
////					column.setMinQueryValue(floatValue);
////					dataModel.setQueriedTuples();
////					// dataModel.fireQueryChanged();
////				}
////			} else if (col == 14) {
////				float floatValue = ((Float) value).floatValue();
////				if (floatValue <= column.getMaxValue()
////						&& floatValue >= column.getMinValue()
////						&& floatValue >= column.getMinQueryValue()) {
////					column.setMaxQueryValue(floatValue);
////					dataModel.setQueriedTuples();
////					// dataModel.fireQueryChanged();
////				}
//			}
//		}
	}

	private Object getColumnStatValue(Column column, int statIndex) {
        if (statIndex == 0) {
            return new Boolean(column.isEnabled());
        } else if (statIndex == 1) {
            return column.getName();
        } else if (statIndex == 2) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMean();
            } else {
                return column.getSummaryStats().getMean();
            }
        } else if (statIndex == 3) {
            if (showQueryStatistics  && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMedian();
            } else {
                return column.getSummaryStats().getMedian();
            }
        } else if (statIndex == 4) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getStandardDeviation();
            } else {
                return column.getSummaryStats().getStandardDeviation();
            }
        } else if (statIndex == 5) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getVariance();
            } else {
                return column.getSummaryStats().getVariance();
            }
        } else if (statIndex == 6) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getQuantile1();
            } else {
                return column.getSummaryStats().getQuantile1();
            }
        } else if (statIndex == 7) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getQuantile3();
            } else {
                return column.getSummaryStats().getQuantile3();
            }
        } else if (statIndex == 8) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getIQR();
            } else {
                return column.getSummaryStats().getIQR();
            }
        } else if (statIndex == 9) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getSkewness();
            } else {
                return column.getSummaryStats().getSkewness();
            }
        } else if (statIndex == 10) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getKurtosis();
            } else {
                return column.getSummaryStats().getKurtosis();
            }
        } else if (statIndex == 11) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMin();
            } else {
                return column.getSummaryStats().getMin();
            }
        } else if (statIndex == 12) {
            if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
                return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMax();
            } else {
                return column.getSummaryStats().getMax();
            }
        }

        return null;
    }

	@Override
	public Object getValueAt(int row, int col) {
        if (dataModel != null && row >= 0) {
            if (row < dataModel.getColumnCount()) {
                return getColumnStatValue(dataModel.getColumn(row), col);
            } else if ((row >= dataModel.getColumnCount()) &&
                    (row < dataModel.getColumnCount() + dataModel.getDisabledColumnCount())) {
                int disabledColumnIndex = row - dataModel.getColumnCount();
                return getColumnStatValue(dataModel.getDisabledColumns().get(disabledColumnIndex), col);
            }
        }

        return null;

//        System.out.println("in getValueAt() row = " + row + " disabled count = " + dataModel.getDisabledColumnCount());
//		if (dataModel != null && dataModel.getColumnCount() > 0) {
//			Column column = null;
//			if (row >= 0 && row < dataModel.getColumnCount()) {
//				column = dataModel.getColumn(row);
//			} else {
//				return null;
//			}


//			if (col == 0) {
//				return new Boolean(column.isEnabled());
//			} else if (col == 1) {
//				return column.getName();
//			} else if (col == 2) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMean();
//				} else {
//					return column.getSummaryStats().getMean();
//				}
//			} else if (col == 3) {
//				if (showQueryStatistics  && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMedian();
//				} else {
//					return column.getSummaryStats().getMedian();
//				}
//			} else if (col == 4) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getStandardDeviation();
//				} else {
//					return column.getSummaryStats().getStandardDeviation();
//				}
//			} else if (col == 5) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getVariance();
//				} else {
//					return column.getSummaryStats().getVariance();
//				}
//			} else if (col == 6) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getQuantile1();
//				} else {
//					return column.getSummaryStats().getQuantile1();
//				}
//			} else if (col == 7) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getQuantile3();
//				} else {
//					return column.getSummaryStats().getQuantile3();
//				}
//			} else if (col == 8) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getIQR();
//				} else {
//					return column.getSummaryStats().getIQR();
//				}
//			} else if (col == 9) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getSkewness();
//				} else {
//					return column.getSummaryStats().getSkewness();
//				}
//			} else if (col == 10) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getKurtosis();
//				} else {
//					return column.getSummaryStats().getKurtosis();
//				}
//			} else if (col == 11) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMin();
//				} else {
//					return column.getSummaryStats().getMin();
//				}
//			} else if (col == 12) {
//				if (showQueryStatistics && dataModel.getActiveQuery().hasColumnSelections()) {
//					return dataModel.getActiveQuery().getColumnQuerySummaryStats(column).getMax();
//				} else {
//					return column.getSummaryStats().getMax();
//				}
//			}
//		}
//
//        if (dataModel != null && dataModel.getDisabledColumnCount() > 0) {
//            Column column = null;
//            if (row >= 0 && row < dataModel.getDisabledColumnCount()) {
//                column = dataModel.getDisabledColumns().get(row - dataModel.getColumnCount());
//            } else {
//                return null;
//            }
//
//            return getColumnStatValue(column, col);
//        }
//
//		return null;
	}

	@Override
	public void queryChanged(DataModel dataModel) {
		this.fireTableDataChanged();
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
	public void columnsDisabled(DataModel dataModel,
			ArrayList<Column> disabledColumns) {
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
