package gov.ornl.table;

import java.util.ArrayList;
import java.util.List;

public class DoubleColumn extends Column {
    private ArrayList<Double> values = new ArrayList<>();
    private double minValue = Double.NaN;
    private double maxValue = Double.NaN;

    public DoubleColumn(String title) {
        super(title);
    }

    @Override
    public Object getValue(int rowIndex) {
        return values.get(rowIndex);
    }

    @Override
    public List<Double> getValues() {
        return values;
    }

    @Override
    public void clearValues() {
        values.clear();
        minValue = Double.NaN;
        maxValue = Double.NaN;
    }

    @Override
    public void addValue(int rowIndex, Object value) {
        double doubleValue = (double)value;

        values.add(rowIndex, doubleValue);
        if (values.size() == 1) {
            minValue = doubleValue;
            maxValue = doubleValue;
        } else {
            minValue = doubleValue < minValue ? doubleValue : minValue;
            maxValue = doubleValue > maxValue ? doubleValue : maxValue;
        }
    }

    @Override
    public void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    public int getRowCount() { return values.size(); }

    public double getMinValue() { return minValue; }

    public double getMaxValue() { return maxValue; }
}
