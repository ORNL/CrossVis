package gov.ornl.scout.dataframe;

import java.util.*;

public class CategoricalColumn extends Column {
    private HashSet<String> categories = new HashSet<>();
    private HashMap<String, Integer> categoriesCounts = new HashMap<>();
    private ArrayList<String> values = new ArrayList<>();

    public CategoricalColumn(String title) {
        super(title);
    }

    @Override
    protected void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    protected void addValue(int rowIndex, Object value) {
        String category = (String)value;

        values.add(rowIndex, category);
        if (categories.contains(category)) {
            categoriesCounts.put(category, categoriesCounts.get(category) + 1);
        } else {
            categories.add(category);
            categoriesCounts.put(category, 0);
        }
    }

    @Override
    protected void clearValues() {
        values.clear();
        categoriesCounts.clear();
        categories.clear();
    }

    @Override
    public Object getValue(int rowIndex) {
        return values.get(rowIndex);
    }

    @Override
    public List getValues() {
        return values;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    public int getCategoryCount(String category) {
        return categoriesCounts.get(category);
    }

    public Set<String> getCategories() {
        return categories;
    }
}
