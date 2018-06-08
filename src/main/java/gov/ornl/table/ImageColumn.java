package gov.ornl.table;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageColumn extends Column {
    ArrayList<BufferedImage> values = new ArrayList<>();

    public ImageColumn(String title) {
        super(title);
    }

    @Override
    protected void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    protected void addValue(int rowIndex, Object value) {
        BufferedImage imageValue = (BufferedImage)value;

        values.add(rowIndex, imageValue);
    }

    @Override
    protected void clearValues() {
        values.clear();
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
}
