package gov.ornl.scout.dataframe;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TemporalColumn extends Column {
    ArrayList<Instant> values = new ArrayList<>();
    Instant startInstant;
    Instant endInstant;

    public TemporalColumn(String title) {
        super(title);
    }

    @Override
    protected void addValue(Object value) {
        addValue(values.size(), value);
    }

    @Override
    protected void addValue(int rowIndex, Object value) {
        Instant instantValue = (Instant)value;
        values.add(rowIndex, instantValue);
        if (startInstant == null) {
            startInstant = Instant.from(instantValue);
            endInstant = Instant.from(instantValue);
        } else {
            if (instantValue.isBefore(startInstant)) {
                startInstant = Instant.from(instantValue);
            } else if (instantValue.isAfter(endInstant)) {
                endInstant = Instant.from(instantValue);
            }
        }
    }

    @Override
    protected void clearValues() {
        values.clear();
        startInstant = null;
        endInstant = null;
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

    public Instant getStartInstant() {
        return startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }
}
