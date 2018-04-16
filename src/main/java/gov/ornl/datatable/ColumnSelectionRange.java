package gov.ornl.datatable;

public abstract class ColumnSelectionRange {
    protected Column column;

    public ColumnSelectionRange (Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }
}
