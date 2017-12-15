package gov.ornl.csed.cda.datatable;

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


    public TemporalColumnSummaryStats(Column column, int numHistogramBins) {
        super(column, numHistogramBins);
    }

    public TemporalColumnSummaryStats(Column column, Instant startInstant, Instant endInstant, int numHistogramBins) {
        this(column, numHistogramBins);

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

        int columnIndex = getColumn().getDataModel().getColumnIndex(column);

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

//    @Override
//    public void calculateHistogram2D(ColumnSummaryStats columnSummaryStats) {
//        Histogram2DDimension xDimension = new Histogram2DDimension.Temporal(values, numHistogramBins, getStartInstant(), getEndInstant());
//        Histogram2DDimension yDimension = null;
//        if (columnSummaryStats.getColumn() instanceof TemporalColumn) {
//            TemporalColumnSummaryStats yColumnSummaryStats = (TemporalColumnSummaryStats)columnSummaryStats;
//            yDimension = new Histogram2DDimension.Temporal(yColumnSummaryStats.getValues(),
//                    numHistogramBins, yColumnSummaryStats.getStartInstant(), yColumnSummaryStats.getEndInstant());
//        } else if (columnSummaryStats.getColumn() instanceof DoubleColumn) {
//            DoubleColumnSummaryStats yColumnSummaryStats = (DoubleColumnSummaryStats)columnSummaryStats;
//            yDimension = new Histogram2DDimension.Double(yColumnSummaryStats.getValues(), numHistogramBins,
//                    yColumnSummaryStats.getMinValue(), yColumnSummaryStats.getMaxValue());
//        }
//        Histogram2D histogram2D = new Histogram2D(xDimension, yDimension);
//        columnHistogram2DMap.put(columnSummaryStats.getColumn(), histogram2D);
//    }

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

//    @Override
//    public void calculateQueryStatistics() {
//        int columnIndex = getColumn().getDataModel().getColumnIndex(column);
//
//        int counter = 0;
//        for (Tuple tuple : getColumn().getDataModel().getActiveQuery().getQueriedTuples()) {
//            Instant instant = (Instant)tuple.getElement(columnIndex);
//            if (counter == 0) {
//                setStartInstant(instant);
//                setEndInstant(instant);
//            } else {
//                if (instant.isBefore(getStartInstant())) {
//                    setStartInstant(instant);
//                } else if (instant.isAfter(getEndInstant())) {
//                    setEndInstant(instant);
//                }
//            }
//            counter++;
//        }
//    }


    public void setStartInstant(Instant startInstant) {
        startInstantProperty().set(startInstant);
        startLocalDateTimeProperty().set(LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC));
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
        endLocalDateTimeProperty().set(LocalDateTime.ofInstant(endInstant, ZoneOffset.UTC));
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
