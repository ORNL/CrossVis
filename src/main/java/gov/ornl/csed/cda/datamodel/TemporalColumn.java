package gov.ornl.csed.cda.datamodel;

import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class TemporalColumn extends Column {
    private SimpleObjectProperty<LocalDateTime> startLocalDateTime;
    private SimpleObjectProperty<LocalDateTime> endLocalDateTime;

    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;

    private ArrayList<Instant> values;

    public TemporalColumn(String name) {
        super(name);
        values = new ArrayList<>();
    }

    @Override
    protected void calculateStatistics() {
        setStartInstant(null);
        setEndInstant(null);

        for (Instant instant : values) {
            if (getStartInstant() == null) {
                setStartInstant(instant);
                setEndInstant(instant);
            } else {
                if (instant.isBefore(getStartInstant())) {
                    setStartInstant(instant);
                } else if (instant.isAfter(getEndInstant())) {
                    setEndInstant(instant);
                }
            }
        }
    }

    public Instant getValueAt(int index) {
        return values.get(index);
    }

    public void addValue(Instant instant) {


        values.add(instant);
    }

    public void addValues(Instant instants[]) {
        for (Instant instant : instants) {
            addValue(instant);
        }
    }

//    public void setValueAt(int index, Instant instant) {
//        values.set(index, instant);
//    }

    public void clearValues() {
        values.clear();
        setStartInstant(null);
        setEndInstant(null);
    }

    public long getSize() {
        return values.size();
    }

    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
        startLocalDateTimeProperty().set(LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC));
    }

    public Instant getStartInstant() {
        return startInstantProperty().get();
    }

    public SimpleObjectProperty<Instant> startInstantProperty() {
        if (startInstant == null) {
            startInstant = new SimpleObjectProperty<>(this, "startInstant");
        }
        return startInstant;
    }

    public SimpleObjectProperty<LocalDateTime> startLocalDateTimeProperty() {
        if (startLocalDateTime == null) {
            startLocalDateTime = new SimpleObjectProperty<>(this, "startLocalDateTime");
        }
        return startLocalDateTime;
    }

    public void setEndInstant(Instant endInstant) {
        endInstantProperty().set(endInstant);
        endLocalDateTimeProperty().set(LocalDateTime.ofInstant(endInstant, ZoneOffset.UTC));
    }

    public Instant getEndInstant() {
        return endInstantProperty().get();
    }

    public SimpleObjectProperty<Instant> endInstantProperty() {
        if (endInstant == null) {
            endInstant = new SimpleObjectProperty<>(this, "endInstant");
        }
        return endInstant;
    }

    public SimpleObjectProperty<LocalDateTime> endLocalDateTimeProperty() {
        if (endLocalDateTime == null) {
            endLocalDateTime = new SimpleObjectProperty<>(this, "endLocalDateTime");
        }
        return endLocalDateTime;
    }
}
