package gov.ornl.csed.cda.datamodel;

public abstract class ColumnSelectionRange {
    protected Column column;

    public ColumnSelectionRange(Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }
}
