package gov.ornl.histogram;

import gov.ornl.datatable.CategoricalHistogram;

import java.util.*;

public class CategoricalHistogramDataModel extends HistogramDataModel {
    private ArrayList<String> values = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();

    public CategoricalHistogramDataModel(Collection<String> values) {
        super();
        setValues(values);
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(Collection<String> values) {
        this.values.clear();
        this.categories.clear();

        HashMap<String, Integer> categoryMap = new HashMap<>();

        for (String value : values) {
            this.values.add(value);
            if (categoryMap.containsKey(value)) {
                categoryMap.put(value, categoryMap.get(value) + 1);
            } else {
                categoryMap.put(value, 1);
            }
        }

        reallocateBinCountsArray(categoryMap.size());
        categories.addAll(categoryMap.keySet());
        for (int i = 0; i < categories.size(); i++) {
            setBinCount(i, categoryMap.get(categories.get(i)));
        }
    }

    @Override
    public int getBinIndex(Object value) {
        if (value instanceof String) {
            return categories.indexOf((String)value);
        }

        return -1;
    }

    @Override
    public void setNumBins(int binCount) {
        // ignore, doesn't make sense for categorical histogram where number of bins equals number
        // of categories, only resetting values is valid way to reset the number of bins
    }

    public static void main (String args[]) {
        String values[] = new String[] {"American", "American", "Japan", "American", "Mexico", "Mexico",
            "American", "Japan", "Russia", "Mexico", "Mexico"};
        ArrayList<String> valueList = new ArrayList<>();
        for (String value : values) {
            valueList.add(value);
        }

        CategoricalHistogramDataModel histogram = new CategoricalHistogramDataModel(valueList);

        for (int i = 0; i < histogram.getNumBins(); i++) {
            System.out.println(i + ": " + histogram.getBinCount(i) + " [" + histogram.getCategories().get(i) + "]");
        }
    }
}
