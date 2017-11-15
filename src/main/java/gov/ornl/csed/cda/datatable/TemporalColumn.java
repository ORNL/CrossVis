package gov.ornl.csed.cda.datatable;

import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

public class TemporalColumn extends Column {

    private SimpleObjectProperty<LocalDateTime> startLocalDateTime;
    private SimpleObjectProperty<LocalDateTime> endLocalDateTime;

    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;

    private SimpleObjectProperty<LocalDateTime> queryStartLocalDateTime;
    private SimpleObjectProperty<LocalDateTime> queryEndLocalDateTime;

//    private SimpleObjectProperty<Instant> queryStartInstant;
//    private SimpleObjectProperty<Instant> queryEndInstant;
    
    public TemporalColumn(String name) {
        super(name);
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

//    public void setQueryStartInstant(Instant instant) {
//        queryStartInstantProperty().set(instant);
//        queryStartLocalDateTimeProperty().set(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
//    }
//
//    public Instant getQueryStartInstant() {
//        return queryStartInstantProperty().get();
//    }
//
//    public SimpleObjectProperty<Instant> queryStartInstantProperty() {
//        if (queryStartInstant == null) {
//            queryStartInstant = new SimpleObjectProperty<>(this, "queryStartInstant");
//        }
//        return startInstant;
//    }
//
//    public SimpleObjectProperty<LocalDateTime> queryStartLocalDateTimeProperty() {
//        if (queryStartLocalDateTime == null) {
//            queryStartLocalDateTime = new SimpleObjectProperty<>(this, "queryStartLocalDateTime");
//        }
//        return queryStartLocalDateTime;
//    }
//
//    public void setQueryEndInstant(Instant instant) {
//        queryEndInstantProperty().set(instant);
//        queryEndLocalDateTimeProperty().set(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
//    }
//
//    public Instant getQueryEndInstant() {
//        return queryEndInstantProperty().get();
//    }
//
//    public SimpleObjectProperty<Instant> queryEndInstantProperty() {
//        if (queryEndInstant == null) {
//            queryEndInstant = new SimpleObjectProperty<>(this, "queryEndInstant");
//        }
//        return queryEndInstant;
//    }
//
//    public SimpleObjectProperty<LocalDateTime> queryEndLocalDateTimeProperty() {
//        if (queryEndLocalDateTime == null) {
//            queryEndLocalDateTime = new SimpleObjectProperty<>(this, "queryEndLocalDateTime");
//        }
//        return queryEndLocalDateTime;
//    }
}
