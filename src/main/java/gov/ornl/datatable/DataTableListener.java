package gov.ornl.datatable;

import java.util.ArrayList;
import java.util.List;

public interface DataTableListener {
    // Methods affecting the full scope of the data table
    // Called when the tuples and columns have been cleared and possibly replaced with new values
    public void dataTableReset(DataTable dataTable);

    public void dataTableStatisticsChanged(DataTable dataTable);

    public void dataTableColumnExtentsChanged(DataTable dataTable);

    public void dataTableColumnFocusExtentsChanged(DataTable dataTable);

    // Called when the number of histogram bins is changed
    public void dataTableNumHistogramBinsChanged(DataTable dataTable);

    // Methods affecting the query state of the data table
    // Called when the active query is reset with no selections (all selections are removed)
    public void dataTableAllColumnSelectionsRemoved(DataTable dataTable);

    // Called when selections for a particular column are removed from the active query
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataTable, Column column);

    // Called when a selection is added to the active query
    public void dataTableColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelectionRange);

    // Called when a selection is removed from the active query
    public void dataTableColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelectionRange);

    public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections);

    // Called when a selection from the active query is modified (min/max range)
    public void dataTableColumnSelectionChanged(DataTable dataTable, ColumnSelection columnSelectionRange);

    // Methods affecting the highlighted column
    // Called when the highlighted column changes
    public void dataTableHighlightedColumnChanged(DataTable dataTable, Column oldHighlightedColumn, Column newHighlightedColumn);

    // Methods affecting the tuples in the data table
    // Called when new tuples are added to the data table
    public void dataTableTuplesAdded(DataTable dataTable, ArrayList<Tuple> newTuples);

    // Called when tuples are removed from the data table
    public void dataTableTuplesRemoved(DataTable dataTable, int numTuplesRemoved);

    // Methods affecting the columns in the data table
    // Called when a column is disabled
    public void dataTableColumnDisabled(DataTable dataTable, Column disabledColumn);

    // Called when a set of columns are disabled
    public void dataTableColumnsDisabled(DataTable dataTable, ArrayList<Column> disabledColumns);

    // Called when a column is enabled (previously disabled)
    public void dataTableColumnEnabled(DataTable dataTable, Column enabledColumn);

    // Called when a new bivariate column is added (combination of two enabled columns)
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index);

    // Called when the column order is changed
    public void dataTableColumnOrderChanged(DataTable dataTable);

    // Called when a column name changes
    public void dataTableColumnNameChanged(DataTable dataTable, Column column);
}
