package gov.ornl.datatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DoubleColumn extends Column {

    private DoubleColumnSummaryStats summaryStats;

    public DoubleColumn(String name) {
        super(name);
    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new DoubleColumnSummaryStats(this, getDataModel().getNumHistogramBins(), null);
        }
        summaryStats.setValues(getValues());
    }

    public double[] getValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        double values[] = new double[getDataModel().getTupleCount()];
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (double)getDataModel().getTuple(i).getElement(columnIndex);
        }
        
        return values;
    }

    public List<Double> getValuesAsList() {
        int columnIndex = getDataModel().getColumnIndex(this);

        ArrayList<Double> valuesList = new ArrayList<>();
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            valuesList.add((double)getDataModel().getTuple(i).getElement(columnIndex));
        }

        return valuesList;
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

    public double[] getNonqueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> nonqueriedTuples = getDataModel().getActiveQuery().getNonQueriedTuples();
        double values[] = new double[nonqueriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : nonqueriedTuples) {
            values[counter++] = (Double) tuple.getElement(columnIndex);
        }

        return values;
    }

    public DoubleColumnSummaryStats getStatistics() { return summaryStats; }
}
