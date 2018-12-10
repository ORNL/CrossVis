package gov.ornl.datatable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;

public class TemporalColumn extends Column {
    private TemporalColumnSummaryStats summaryStats;
    private ObjectProperty<Instant> startScaleValue = new SimpleObjectProperty<>(null);
    private ObjectProperty<Instant> endScaleValue = new SimpleObjectProperty<>(null);

    public TemporalColumn(String name) {
        super(name);
    }

    public Instant getStartScaleValue() { return startScaleValue.get(); }

    protected void setStartScaleValue(Instant instant) { startScaleValue.set(instant); }

    public ReadOnlyObjectProperty<Instant> startScaleValueProperty() { return startScaleValue; }

    public Instant getEndScaleValue() { return endScaleValue.get(); }

    protected void setEndScaleValue(Instant instant) { endScaleValue.set(instant); }

    public ReadOnlyObjectProperty<Instant> endScaleValueProperty() { return endScaleValue; }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new TemporalColumnSummaryStats(this, getDataModel().getNumHistogramBins(), null);
        }
        summaryStats.setValues(getValues(), getDataModel().getNumHistogramBins());

        if (getStartScaleValue() == null) {
            setStartScaleValue(summaryStats.getStartInstant());
        }

        if (getEndScaleValue() == null) {
            setEndScaleValue(summaryStats.getEndInstant());
        }
    }

    public Instant[] getValues() {
        Instant[] values = new Instant[getDataModel().getTupleCount()];
        int columnIndex = getDataModel().getColumnIndex(this);
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (Instant)getDataModel().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getQueriedValues() {
        if (getDataModel().getActiveQuery().getQueriedTuples().isEmpty()) {
            return null;
        }

        Instant[] values = new Instant[getDataModel().getActiveQuery().getQueriedTuples().size()];
        int columnIndex = getDataModel().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataModel().getActiveQuery().getQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getNonqueriedValues() {
        if (getDataModel().getActiveQuery().getNonQueriedTuples().isEmpty()) {
            return null;
        }

        Instant[] values = new Instant[getDataModel().getActiveQuery().getNonQueriedTuples().size()];
        int columnIndex = getDataModel().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataModel().getActiveQuery().getNonQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

    public TemporalColumnSummaryStats getStatistics () {
        return summaryStats;
    }
}
