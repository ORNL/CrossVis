package gov.ornl.datatable;

import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Map;

public class CategoricalColumnSummaryStats extends ColumnSummaryStats {
    private String[] values;

//    private MapProperty<String, Integer> categoryCountMap;
    private ObjectProperty<CategoricalHistogram> histogram;

    public CategoricalColumnSummaryStats(Column column) {
        super(column, 0);

        values = null;
//        categoryCountMap = new SimpleMapProperty<String, Integer>(FXCollections.observableHashMap());
    }

    public List<String> getColumnCategories() {
        return ((CategoricalColumn)getColumn()).getCategories();
    }

//    public MapProperty<String, Integer> categoryCountMapProperty() {
//        return categoryCountMap;
//    }
//
//    public Map<String, Integer> getCategoryCountMap() {
//        return categoryCountMapProperty().get();
//    }

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
        calculateHistogram();
//        for (String value : values) {
//            if (categoryCountMap.containsKey(value)) {
//                int valueCount = categoryCountMap.get(value);
//                categoryCountMap.put(value, valueCount + 1);
//            } else {
//                categoryCountMap.put(value, 1);
//            }
//        }
    }

    public CategoricalHistogram getHistogram() { return histogramProperty().get(); }

    public void setHistogram(CategoricalHistogram histogram) { histogramProperty().set(histogram); }

    public ObjectProperty<CategoricalHistogram> histogramProperty() {
        if (histogram == null) {
            histogram = new SimpleObjectProperty<>(this, "histogram");
        }
        return histogram;
    }

    @Override
    public void calculateHistogram() {
        setHistogram(new CategoricalHistogram(column.getName(), categoricalColumn().getCategories(), values));
    }
}
