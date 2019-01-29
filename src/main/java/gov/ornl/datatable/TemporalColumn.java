package gov.ornl.datatable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;

public class TemporalColumn extends Column {
    private TemporalColumnSummaryStats summaryStats;
    private ObjectProperty<Instant> startScaleValue = new SimpleObjectProperty<>(null);
    private ObjectProperty<Instant> endScaleValue = new SimpleObjectProperty<>(null);
    private ObjectProperty<Instant> startFocusValue = new SimpleObjectProperty<>(null);
    private ObjectProperty<Instant> endFocusValue = new SimpleObjectProperty<>(null);

    public TemporalColumn(String name) {
        super(name);
    }

    public boolean setFocusContext(Tuple tuple, int elementIdx) {
        if (((Instant)tuple.getElement(elementIdx)).isAfter(getEndFocusValue())) {
            getUpperContextTuples().add(tuple);
            return false;
        } else if (((Instant)tuple.getElement(elementIdx)).isBefore(getStartFocusValue())) {
            getLowerContextTuples().add(tuple);
            return false;
        }
        getFocusTuples().add(tuple);
        return true;
    }

    public Instant getStartFocusValue() { return startFocusValue.get(); }

    protected void setStartFocusValue(Instant instant) { startFocusValue.set(instant); }

    public ReadOnlyObjectProperty<Instant> startFocusValueProperty() { return startFocusValue; }

    public Instant getEndFocusValue() { return endFocusValue.get(); }

    protected void setEndFocusValue(Instant instant) { endFocusValue.set(instant); }

    public ReadOnlyObjectProperty<Instant> endFocusValueProperty() { return endFocusValue; }

    public Instant getStartScaleValue() { return startScaleValue.get(); }

    protected void setStartScaleValue(Instant instant) {
        startScaleValue.set(instant);

        if (getStartFocusValue() == null || getStartFocusValue().isBefore(getStartScaleValue())) {
            setStartFocusValue(instant);
        }
    }

    public ReadOnlyObjectProperty<Instant> startScaleValueProperty() { return startScaleValue; }

    public Instant getEndScaleValue() { return endScaleValue.get(); }

    protected void setEndScaleValue(Instant instant) {
        endScaleValue.set(instant);

        if (getEndFocusValue() == null || getEndFocusValue().isAfter(getEndScaleValue())) {
            setEndFocusValue(instant);
        }
    }

    public ReadOnlyObjectProperty<Instant> endScaleValueProperty() { return endScaleValue; }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new TemporalColumnSummaryStats(this, getDataTable().getNumHistogramBins(), null);
        }
        summaryStats.setValues(getValues(), getDataTable().getNumHistogramBins());

        if (getStartScaleValue() == null) {
            setStartScaleValue(summaryStats.getStartInstant());
        }

        if (getEndScaleValue() == null) {
            setEndScaleValue(summaryStats.getEndInstant());
        }

        if (getStartFocusValue() == null) {
            setStartFocusValue(summaryStats.getStartInstant());
        }

        if (getEndFocusValue() == null) {
            setEndFocusValue(summaryStats.getEndInstant());
        }
    }

    public Instant[] getFocusValues() {
        int columnIndex = getDataTable().getColumnIndex(this);
        if (columnIndex == -1) {
            columnIndex = getDataTable().getDisabledColumns().indexOf(this);
        }

        Instant[] values = new Instant[getFocusTuples().size()];
        int counter = 0;
        for (Tuple focusTuple : getFocusTuples()) {
            values[counter++] = (Instant)focusTuple.getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getValues() {
        Instant[] values = new Instant[getDataTable().getTupleCount()];
        int columnIndex = getDataTable().getColumnIndex(this);
        for (int i = 0; i < getDataTable().getTupleCount(); i++) {
            values[i] = (Instant) getDataTable().getTuple(i).getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getQueriedValues() {
        if (getDataTable().getActiveQuery().getQueriedTuples().isEmpty()) {
            return null;
        }

        Instant[] values = new Instant[getDataTable().getActiveQuery().getQueriedTuples().size()];
        int columnIndex = getDataTable().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataTable().getActiveQuery().getQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

    public Instant[] getNonqueriedValues() {
        if (getDataTable().getActiveQuery().getNonQueriedTuples().isEmpty()) {
            return null;
        }

        Instant[] values = new Instant[getDataTable().getActiveQuery().getNonQueriedTuples().size()];
        int columnIndex = getDataTable().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataTable().getActiveQuery().getNonQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

    public TemporalColumnSummaryStats getStatistics () {
        return summaryStats;
    }
}
