package gov.ornl.csed.cda.datatable;

import java.util.ArrayList;

public interface DataModelListener {
	public void dataModelChanged(DataModel dataModel);

	public void queryChanged(DataModel dataModel);
	
	public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange);

	public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange);
	
	public void highlightedColumnChanged(DataModel dataModel);

	public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples);

	public void columnDisabled(DataModel dataModel, Column disabledColumn);

	public void columnsDisabled(DataModel dataModel,
								ArrayList<Column> disabledColumns);

	public void columnEnabled(DataModel dataModel, Column enabledColumn);
}
