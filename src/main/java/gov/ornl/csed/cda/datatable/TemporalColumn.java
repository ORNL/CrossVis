package gov.ornl.csed.cda.datatable;

import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;

public class TemporalColumn extends Column {

    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;
    
    public TemporalColumn(String name) {
        super(name);
    }

    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
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

    public void setEndInstant(Instant endInstant) {
        endInstantProperty().set(endInstant);
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
}
