package gov.ornl.datatable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CategoricalColumn extends Column {

    private HashSet<String> categories = new HashSet<>();

    private CategoricalColumnSummaryStats summaryStats;

    public CategoricalColumn(String name) {
        super(name);
    }

    @Override
    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new CategoricalColumnSummaryStats(this);
        }
        summaryStats.setValues(getValues());
    }

    public String[] getValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        String values[] = new String[getDataModel().getTupleCount()];
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (String)getDataModel().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public String[] getQueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataModel().getActiveQuery().getQueriedTuples();
        String values[] = new String[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (String)tuple.getElement(columnIndex);
        }

        return values;
    }
    @Override
    public CategoricalColumnSummaryStats getStatistics() {
        return summaryStats;
    }
}
