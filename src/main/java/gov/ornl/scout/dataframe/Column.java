package gov.ornl.scout.dataframe;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public abstract class Column {
    private StringProperty title;

    public Column(String title) {
        this.title = new SimpleStringProperty(title);
    }

    public String getTitle() {
        return titleProperty().get();
    }

    public void setTitle(String title) {
        titleProperty().set(title);
    }

    public StringProperty titleProperty() { return title; }

    protected abstract void addValue(Object value);

    protected abstract void addValue(int rowIndex, Object value);

    protected abstract void clearValues();

    public abstract Object getValue(int rowIndex);

    public abstract List getValues();

    public abstract int getRowCount();
}
