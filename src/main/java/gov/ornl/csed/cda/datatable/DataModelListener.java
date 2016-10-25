package gov.ornl.csed.cda.datatable;

import java.util.ArrayList;

public interface DataModelListener {
	// Methods affecting the full scope of the data model
	// Called when the tuples and columns have been cleared and possibly replaced with new values
	public void dataModelReset (DataModel dataModel);

	// Called when the number of histogram bins is changed
	public void dataModelNumHistogramBinsChanged (DataModel dataModel);

	// Methods affecting the query state of the data model
	// Called when the active query is reset with no selections (all selections are removed)
	public void dataModelQueryCleared (DataModel dataModel);

    // Called when selections for a particular column are removed from the active query
    public void dataModelQueryColumnCleared (DataModel dataModel, Column column);

	// Called when a selection is added to the active query
	public void dataModelColumnSelectionAdded (DataModel dataModel, ColumnSelectionRange columnSelectionRange);

	// Called when a selection is removed from the active query
	public void dataModelColumnSelectionRemoved (DataModel dataModel, ColumnSelectionRange columnSelectionRange);

	// Called when a selection from the active query is modified (min/max range)
	public void dataModelColumnSelectionChanged (DataModel dataModel, ColumnSelectionRange columnSelectionRange);

	// Methods affecting the highlighted column
	// Called when the highlighted column changes
	public void dataModelHighlightedColumnChanged (DataModel dataModel, Column oldHighlightedColumn, Column newHighlightedColumn);

	// Methods affecting the tuples in the data model
	// Called when new tuples are added to the data model
	public void dataModelTuplesAdded (DataModel dataModel, ArrayList<Tuple> newTuples);

	// Called when tuples are removed from the data model
	public void dataModelTuplesRemoved (DataModel dataModel, int numTuplesRemoved);

	// Methods affecting the columns in the data model
	// Called when a column is disabled
	public void dataModelColumnDisabled (DataModel dataModel, Column disabledColumn);

    // Called when a set of columns are disabled
	public void dataModelColumnsDisabled (DataModel dataModel, ArrayList<Column> disabledColumns);

	// Called when a column is enabled (previously disabled)
	public void dataModelColumnEnabled (DataModel dataModel, Column enabledColumn);

	// Called when the column order is changed
	public void dataModelColumnOrderChanged (DataModel dataModel);

	// Called when a column name changes
	public void dataModelColumnNameChanged (DataModel dataModel, Column column);
}
