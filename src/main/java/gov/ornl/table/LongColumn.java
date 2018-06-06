package gov.ornl.table;

import java.util.ArrayList;
import java.util.List;

public class LongColumn extends Column {
    ArrayList<Long> values = new ArrayList<>();
    long minValue = Long.MAX_VALUE;
    long maxValue = Long.MIN_VALUE;

    public LongColumn(String title) {
        super(title);
    }

    @Override
    protected void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    protected void addValue(int rowIndex, Object value) {
        long longValue = (long)value;

        values.add(rowIndex, longValue);
        if (values.size() == 1) {
            minValue = longValue;
            maxValue = longValue;
        } else {
            minValue = longValue < minValue ? longValue : minValue;
            maxValue = longValue > maxValue ? longValue : maxValue;
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
