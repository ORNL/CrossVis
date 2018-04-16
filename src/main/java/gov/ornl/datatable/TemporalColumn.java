package gov.ornl.datatable;

import java.time.Instant;

public class TemporalColumn extends Column {
    private TemporalColumnSummaryStats summaryStats;

    public TemporalColumn(String name) {
        super(name);
    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new TemporalColumnSummaryStats(this, getDataModel().getNumHistogramBins());
        }
        summaryStats.setValues(getValues());
    }

    public Instant[] getValues() {
        Instant[] values = new Instant[getDataModel().getTupleCount()];
        int columnIndex = getDataModel().getColumnIndex(this);
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (Instant)getDataModel().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getQueriedValues() {
        if (getDataModel().getActiveQuery().getQueriedTuples().isEmpty()) {
            return null;
        }

        Instant[] values = new Instant[getDataModel().getActiveQuery().getQueriedTuples().size()];
        int columnIndex = getDataModel().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataModel().getActiveQuery().getQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

    public TemporalColumnSummaryStats getStatistics () {
        return summaryStats;
    }
}
