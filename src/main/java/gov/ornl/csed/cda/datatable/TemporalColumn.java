package gov.ornl.csed.cda.datatable;

import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.time.LocalDateTime;

public class TemporalColumn extends Column {

    private SimpleObjectProperty<LocalDateTime> startLocalDateTime;
    private SimpleObjectProperty<LocalDateTime> endLocalDateTime;

    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;
    
    public TemporalColumn(String name) {
        super(name);
    }

    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
        startLocalDateTimeProperty().set(LocalDateTime.from(startInstant));
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
        endLocalDateTimeProperty().set(LocalDateTime.from(endInstant));
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
