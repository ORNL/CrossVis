package gov.ornl.datatable;

import java.util.HashMap;
import java.util.List;

public class CategoricalHistogram extends Histogram {
    private String values[];
    private List<String> categories;
    private HashMap<String, Integer> categoryCounts = new HashMap<>();

    public CategoricalHistogram(String name, List<String> categories, String values[]) {
        super(name);
        this.categories = categories;
        this.values = values;
        calculateStatistics();
    }

    public List<String> getCategories() {
        return categories;
    }

    public int getCategoryCount(String category) {
        return categoryCounts.get(category);
    }

    public int getNumCategories() {
        return categories.size();
    }

    public int getTotalCount() {
        return values.length;
    }

    public void setValues (String values[]) {
        this.values = values;
        calculateStatistics();
    }

    public void calculateStatistics() {
        categoryCounts.clear();
        for (String category : categories) {
            categoryCounts.put(category, 0);
        }

        for (String value : values) {
            int count = categoryCounts.get(value);
            categoryCounts.put(value, count+1);
        }
    }
}
