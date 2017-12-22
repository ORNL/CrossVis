package gov.ornl.csed.cda.datatable;

import java.util.Set;

public class DoubleColumn extends Column {

    private DoubleColumnSummaryStats summaryStats;

    public DoubleColumn(String name) {
        super(name);
    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new DoubleColumnSummaryStats(this, getDataModel().getNumHistogramBins());
        }
        summaryStats.setValues(getValues());
    }

    public double[] getValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        double values[] = new double[getDataModel().getTupleCount()];
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (Double) getDataModel().getTuple(i).getElement(columnIndex);
        }
        
        return values;
    }

    public double[] getQueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataModel().getActiveQuery().getQueriedTuples();
        double values[] = new double[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (Double) tuple.getElement(columnIndex);
        }

        return values;
    }

    public DoubleColumnSummaryStats getStatistics() { return summaryStats; }
}
