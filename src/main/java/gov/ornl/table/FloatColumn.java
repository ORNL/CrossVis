package gov.ornl.table;

import java.util.ArrayList;
import java.util.List;

public class FloatColumn extends Column {
    ArrayList<Float> values = new ArrayList<>();
    float minValue = Float.NaN;
    float maxValue = Float.NaN;

    public FloatColumn(String title) {
        super(title);
    }

    @Override
    public Object getValue(int rowIndex) {
        return values.get(rowIndex);
    }

    @Override
    public List<Float> getValues() {
        return values;
    }

    @Override
    public void clearValues() {
        values.clear();
        minValue = Float.NaN;
        maxValue = Float.NaN;
    }

    @Override
    public void addValue(int rowIndex, Object value) {
        float floatValue = (float)value;

        values.add(rowIndex, floatValue);
        if (values.size() == 1) {
            minValue = floatValue;
            maxValue = floatValue;
        } else {
            minValue = floatValue < minValue ? floatValue : minValue;
            maxValue = floatValue > maxValue ? floatValue : maxValue;
        }
    }

    @Override
    public void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    public int getRowCount() { return values.size(); }

    public float getMinValue() { return minValue; }

    public float getMaxValue() { return maxValue; }
}
