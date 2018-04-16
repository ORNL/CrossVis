package gov.ornl.datatable;

import java.util.*;

public class CategoricalColumn extends Column {

    private ArrayList<String> categories = new ArrayList<>();
    private CategoricalColumnSummaryStats summaryStats;

    public CategoricalColumn(String name, List<String> categories) {
        super(name);
        if (categories != null && (categories.size() > 0)) {
            this.categories.addAll(categories);
        }
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public int getCategoryID(String category) {
        return categories.indexOf(category);
    }

    public String getCategory(int id) {
        if (id > 0 && id < categories.size()) {
            return categories.get(id);
        }
        return null;
    }

    public void addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
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
