package gov.ornl.csed.cda.datatable2;

public abstract class ColumnSummaryStats {
    protected Column column;

    public ColumnSummaryStats(Column column) {
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }

    public abstract void calculateStatistics();
    public abstract void calculateHistogram();
}
