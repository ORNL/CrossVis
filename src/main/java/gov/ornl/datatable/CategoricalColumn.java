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

    public boolean setFocusContext(Tuple tuple, int elementIdx) {
        getFocusTuples().add(tuple);
        return true;
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
            summaryStats = new CategoricalColumnSummaryStats(this, null);
        }
        summaryStats.setValues(getValues());
    }

    public String[] getValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        String values[] = new String[getDataTable().getTupleCount()];
        for (int i = 0; i < getDataTable().getTupleCount(); i++) {
            values[i] = (String) getDataTable().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public List<String> getValuesAsList() {
        int columnIndex = getDataTable().getColumnIndex(this);
        ArrayList<String> valuesList = new ArrayList<>();
        for (int i = 0; i < getDataTable().getTupleCount(); i++) {
            valuesList.add((String) getDataTable().getTuple(i).getElement(columnIndex));
        }
        return valuesList;
    }

    public String[] getQueriedValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataTable().getActiveQuery().getQueriedTuples();
        String values[] = new String[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (String)tuple.getElement(columnIndex);
        }

        return values;
    }

    public String[] getNonqueriedValues() {
        int columnIndex = getDataTable().getColumnIndex(this);

        Set<Tuple> nonqueriedTuples = getDataTable().getActiveQuery().getNonQueriedTuples();
        String values[] = new String[nonqueriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : nonqueriedTuples) {
            values[counter++] = (String)tuple.getElement(columnIndex);
        }

        return values;
    }

    @Override
    public CategoricalColumnSummaryStats getStatistics() {
        return summaryStats;
    }
}
