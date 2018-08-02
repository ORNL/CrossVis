package gov.ornl.datatable;

public abstract class ColumnSelection {
    protected Column column;

    public ColumnSelection(Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }
}

