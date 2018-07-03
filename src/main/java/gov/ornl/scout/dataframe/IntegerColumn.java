package gov.ornl.scout.dataframe;

import java.util.ArrayList;
import java.util.List;

public class IntegerColumn extends Column {
    ArrayList<Integer> values = new ArrayList<>();
    int minValue = Integer.MAX_VALUE;
    int maxValue = Integer.MIN_VALUE;

    public IntegerColumn(String title) {
        super(title);
    }

    @Override
    protected void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    protected void addValue(int rowIndex, Object value) {
        int intValue = (int)value;

        values.add(rowIndex, intValue);
        if (values.size() == 1) {
            minValue = intValue;
            maxValue = intValue;
        } else {
            minValue = intValue < minValue ? intValue : minValue;
            maxValue = intValue > maxValue ? intValue : maxValue;
        }
    }

    @Override
    protected void clearValues() {
        values.clear();
        minValue = 0;
        maxValue = 0;
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

    public long getMinValue() { return minValue; }

    public long getMaxValue() { return maxValue; }
}
