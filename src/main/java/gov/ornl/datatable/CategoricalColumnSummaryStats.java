package gov.ornl.datatable;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

import java.util.Map;

public class CategoricalColumnSummaryStats extends ColumnSummaryStats {
    private String[] values;

    private MapProperty<String, Integer> categoryCountMap;

    public CategoricalColumnSummaryStats(Column column) {
        super(column, 0);

        values = null;
        categoryCountMap = new SimpleMapProperty<String, Integer>(FXCollections.observableHashMap());
    }

    public MapProperty<String, Integer> categoryCountMapProperty() {
        return categoryCountMap;
    }

    public Map<String, Integer> getCategoryCountMap() {
        return categoryCountMapProperty().get();
    }

    public void setValues(String[] values) {
        this.values = values;
        calculateStatistics();
    }

    public String[] getValues() {
        return values;
    }

    private CategoricalColumn categoricalColumn() {
        return (CategoricalColumn)getColumn();
    }

    @Override
    public void calculateStatistics() {
        for (String value : values) {
            if (categoryCountMap.containsKey(value)) {
                int valueCount = categoryCountMap.get(value);
                categoryCountMap.put(value, valueCount + 1);
            } else {
                categoryCountMap.put(value, 1);
            }
        }
    }

    @Override
    public void calculateHistogram() {
    }
}
