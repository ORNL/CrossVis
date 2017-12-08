package gov.ornl.csed.cda.datatable2;

public abstract class ColumnSelectionRange {
    protected Column column;

    public ColumnSelectionRange (Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }
}
