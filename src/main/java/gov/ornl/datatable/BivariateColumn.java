package gov.ornl.datatable;

public class BivariateColumn extends Column {

    private Column column1;
    private Column column2;

    BivariateColumnSummaryStats summaryStats;

    public BivariateColumn(String title, Column column1, Column column2) {
        super(title);
        this.column1 = column1;
        this.column2 = column2;
    }

    public boolean setFocusContext(Tuple tuple, int elementIdx) {
        getFocusTuples().add(tuple);
        return true;
    }

    public void swapColumns() {
        Column column = column1;
        column1 = column2;
        column2 = column;
        int columnIndex = dataModel.getColumnIndex(this);
        for (Tuple tuple : dataModel.getTuples()) {
            swapTupleElementValues((Object[])tuple.getElement(columnIndex));
        }
    }

    private void swapTupleElementValues(Object values[]) {
        Object tempValue = values[0];
        values[0] = values[1];
        values[1] = tempValue;
    }

    public Column getColumn1() { return column1; }

    public Column getColumn2() { return column2; }


    @Override
    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new BivariateColumnSummaryStats(this, getDataModel().getNumHistogramBins(), null);
        }
    }

    @Override
    public ColumnSummaryStats getStatistics() {
        return summaryStats;
    }
}
