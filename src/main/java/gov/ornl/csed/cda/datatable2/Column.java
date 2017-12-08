package gov.ornl.csed.cda.datatable2;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class Column {
    protected DataModel dataModel;
    private StringProperty name;
    private BooleanProperty enabled;

    public Column(String name) {
        setName(name);
        setEnabled(true);
    }

    protected void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public abstract void calculateStatistics();

    public abstract ColumnSummaryStats getStatistics();

//    public abstract void calculateQueryStatistics();
//
//    public abstract ColumnSummaryStats getQueryStatistics();

    public void setEnabled(boolean enabled) {
        enabledProperty().set(enabled);
    }

    public boolean getEnabled() {
        return enabledProperty().get();
    }

    public BooleanProperty enabledProperty() {
        if (enabled == null) {
            enabled = new SimpleBooleanProperty(true);
        }
        return enabled;
    }

    public void setName(String name) {
        nameProperty().set(name);
    }

    public String getName() {
        return nameProperty().get();
    }

    public StringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty("");
        }

        return name;
    }

    public String toString() {
        return getName();
    }
}
