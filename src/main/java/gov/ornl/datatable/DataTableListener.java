package gov.ornl.datatable;

import java.util.ArrayList;

public interface DataTableListener {
	// Methods affecting the full scope of the data model
	// Called when the tuples and columns have been cleared and possibly replaced with new values
	public void dataModelReset(DataTable dataModel);

	public void dataModelStatisticsChanged(DataTable dataModel);

	// Called when the number of histogram bins is changed
	public void dataModelNumHistogramBinsChanged(DataTable dataModel);

	// Methods affecting the query state of the data model
	// Called when the active query is reset with no selections (all selections are removed)
	public void dataTableAllColumnSelectionsRemoved(DataTable dataModel);

    // Called when selections for a particular column are removed from the active query
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataModel, Column column);

	// Called when a selection is added to the active query
	public void dataModelColumnSelectionAdded(DataTable dataModel, ColumnSelection columnSelectionRange);

	// Called when a selection is removed from the active query
	public void dataModelColumnSelectionRemoved(DataTable dataModel, ColumnSelection columnSelectionRange);

	// Called when a selection from the active query is modified (min/max range)
	public void dataModelColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange);

	// Methods affecting the highlighted column
	// Called when the highlighted column changes
	public void dataModelHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn);

	// Methods affecting the tuples in the data model
	// Called when new tuples are added to the data model
	public void dataModelTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples);

	// Called when tuples are removed from the data model
	public void dataModelTuplesRemoved(DataTable dataModel, int numTuplesRemoved);

	// Methods affecting the columns in the data model
	// Called when a column is disabled
	public void dataModelColumnDisabled(DataTable dataModel, Column disabledColumn);

    // Called when a set of columns are disabled
	public void dataModelColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns);

	// Called when a column is enabled (previously disabled)
	public void dataModelColumnEnabled(DataTable dataModel, Column enabledColumn);

	// Called when the column order is changed
	public void dataModelColumnOrderChanged(DataTable dataModel);

	// Called when a column name changes
	public void dataModelColumnNameChanged(DataTable dataModel, Column column);
}
