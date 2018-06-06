package gov.ornl.table;

import java.util.List;

public abstract class Column {
    private String title;

    public Column(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected abstract void addValue(Object value);
    protected abstract void addValue(int rowIndex, Object value);
    protected abstract void clearValues();

    public abstract Object getValue(int rowIndex);
    public abstract List getValues();
    public abstract int getRowCount();
}
