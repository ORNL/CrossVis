package gov.ornl.csed.cda.datamodel;

public interface DataTableListener {
    // Called when the multiple elements of the data model have changed
    public void dataTableChanged(DataTable dataTable);

    // Called when the data table has been cleared
    public void dataTableCleared(DataTable dataTable);

    // Called when columns have been added, removed, or modified
    public void dataTableColumnsChanged(DataTable dataTable);

    // Called when the ordering of the columns have changed
    public void dataTableColumnOrderChanged(DataTable dataTable);

    // Called when rows as added, removed, or modified
    public void dataTableRowsChanged(DataTable dataTable);
}
