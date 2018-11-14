package gov.ornl.datatable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TemporalColumnSummaryStats extends ColumnSummaryStats {
    private SimpleObjectProperty<Instant> startInstant;
    private SimpleObjectProperty<Instant> endInstant;
    private SimpleObjectProperty<LocalDateTime> startLocalDateTime;
    private SimpleObjectProperty<LocalDateTime> endLocalDateTime;
    private SimpleObjectProperty<TemporalHistogram> histogram;
    private Instant[] values;


    public TemporalColumnSummaryStats(Column column, int numHistogramBins, Query query) {
        super(column, numHistogramBins, query);
    }

    public TemporalColumnSummaryStats(Column column, Instant startInstant, Instant endInstant, int numHistogramBins, Query query) {
        this(column, numHistogramBins, query);

        setStartInstant(startInstant);
        setEndInstant(endInstant);
    }

    public void setValues(Instant[] values, int numHistogramBins) {
        this.values = values;
        this.numHistogramBins = numHistogramBins;
        calculateStatistics();
    }

    public void setValues(Instant[] values) {
        this.values = values;

//        numHistogramBins = (int)Math.floor(Math.sqrt(values.length));
//        if (numHistogramBins > MAX_NUM_HISTOGRAM_BINS) {
//            numHistogramBins = MAX_NUM_HISTOGRAM_BINS;
//        }

        calculateStatistics();
    }

    public Instant[] getValues() {
        return values;
    }

    @Override
    public void calculateStatistics() {
        Instant start = null;
        Instant end = null;

//        int columnIndex = getColumn().getDataModel().getColumnIndex(column);

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Instant instant = values[i];
                if (i == 0) {
                    start = end = instant;
                } else {
                    if (instant.isBefore(start)) {
                        start = instant;
                    } else if (instant.isAfter(end)) {
                        end = instant;
                    }
                }
            }
        }

        setStartInstant(start);
        setEndInstant(end);

        calculateHistogram();
    }

    private TemporalColumn temporalColumn() {
        return (TemporalColumn)getColumn();
    }

    @Override
    public void calculateHistogram() {
        setHistogram(new TemporalHistogram(column.getName(), values, numHistogramBins,
                temporalColumn().getStatistics().getStartInstant(),
                temporalColumn().getStatistics().getEndInstant()));
    }

    public TemporalHistogram getHistogram() {
        return histogramProperty().get();
    }

    public void setHistogram(TemporalHistogram histogram) {
        histogramProperty().set(histogram);
    }

    public ObjectProperty<TemporalHistogram> histogramProperty() {
        if (histogram == null) {
            histogram = new SimpleObjectProperty<>(this, "histogram");
        }
        return histogram;
    }

    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
        if (startInstant == null) {
            startLocalDateTimeProperty().set(null);
        } else {
            startLocalDateTimeProperty().set(LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC));
        }
    }

    public Instant getStartInstant() {
        return startInstantProperty().get();
    }

    public ObjectProperty<Instant> startInstantProperty() {
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
        if (endInstant != null) {
            endLocalDateTimeProperty().set(LocalDateTime.ofInstant(endInstant, ZoneOffset.UTC));
        } else {
            endLocalDateTimeProperty().set(null);
        }
    }

    public Instant getEndInstant() {
        return endInstantProperty().get();
    }

    public ObjectProperty<Instant> endInstantProperty() {
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
