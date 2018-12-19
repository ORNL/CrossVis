package gov.ornl.datatable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashSet;
import java.util.Set;

public abstract class Column {
    protected DataTable dataModel;
    private StringProperty name;
    private BooleanProperty enabled;

    private HashSet<Tuple> focusTuples = new HashSet<>();
    private HashSet<Tuple> lowerContextTuples = new HashSet<>();
    private HashSet<Tuple> upperContextTuples = new HashSet<>();

    public Column(String name) {
        setName(name);
        setEnabled(true);
    }

    public abstract boolean setFocusContext(Tuple tuple, int elementIdx);

    public Set<Tuple> getFocusTuples() { return focusTuples; }

    public Set<Tuple> getLowerContextTuples() { return lowerContextTuples; }

    public Set<Tuple> getUpperContextTuples() { return upperContextTuples; }

    protected void setDataModel(DataTable dataModel) {
        this.dataModel = dataModel;
    }

    public DataTable getDataModel() {
        return dataModel;
    }

    public abstract void calculateStatistics();

    public abstract ColumnSummaryStats getStatistics();

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
