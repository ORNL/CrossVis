package gov.ornl.csed.cda.datatable;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;

public class TemporalColumnSelectionRange extends ColumnSelectionRange {
    private ListProperty<Instant> rangeInstants;

    public TemporalColumnSelectionRange(TemporalColumn column, Instant startInstant, Instant endInstant) {
        super(column);
        rangeInstants = new SimpleListProperty<>();
        ObservableList<Instant> observableList = FXCollections.observableArrayList(startInstant, endInstant);
        rangeInstants.set(observableList);
    }

    public final Instant getStartInstant() {
        return rangeInstants.get(0);
    }

    public final void setStartInstant(Instant startInstant) {
        rangeInstants.set(0, startInstant);
    }

    public final Instant getEndInstant() {
        return rangeInstants.get(1);
    }

    public final void setEndInstant(Instant endInstant) {
        rangeInstants.set(1, endInstant);
    }

    public final void setRangeInstants(Instant startInstant, Instant endInstant) {
        rangeInstants.setAll(startInstant, endInstant);
    }

    public ListProperty<Instant> rangeInstantsProperty() {
        return rangeInstants;
    }
}
