package gov.ornl.csed.cda.datatable;

import java.time.Instant;

public class TemporalColumn extends Column {
    private TemporalColumnSummaryStats summaryStats;
//    private TemporalColumnSummaryStats querySummaryStats;

//    private SimpleObjectProperty<LocalDateTime> startLocalDateTime;
//    private SimpleObjectProperty<LocalDateTime> endLocalDateTime;
//
//    private SimpleObjectProperty<Instant> startInstant;
//    private SimpleObjectProperty<Instant> endInstant;

//    private SimpleObjectProperty<LocalDateTime> queryStartLocalDateTime;
//    private SimpleObjectProperty<LocalDateTime> queryEndLocalDateTime;

//    private SimpleObjectProperty<Instant> queryStartInstant;
//    private SimpleObjectProperty<Instant> queryEndInstant;
    
    public TemporalColumn(String name) {
        super(name);
//        summaryStats = new TemporalColumnSummaryStats(this);
    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new TemporalColumnSummaryStats(this, getDataModel().getNumHistogramBins());
        }
        summaryStats.setValues(getValues());
//        summaryStats.calculateStatistics();
//        int columnIndex = getDataModel().getColumnIndex(this);
//        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
//            Instant instant = (Instant)getDataModel().getTuple(0).getElement(columnIndex);
//            if (i == 0) {
//                summaryStats.setStartInstant(instant);
//                summaryStats.setEndInstant(instant);
//            } else {
//                if (instant.isBefore(summaryStats.getStartInstant())) {
//                    summaryStats.setStartInstant(instant);
//                } else if (instant.isAfter(summaryStats.getEndInstant())) {
//                    summaryStats.setEndInstant(instant);
//                }
//            }
//        }
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
        Instant[] values = new Instant[getDataModel().getActiveQuery().getQueriedTuples().size()];
        int columnIndex = getDataModel().getColumnIndex(this);
        int counter = 0;
        for (Tuple tuple : getDataModel().getActiveQuery().getQueriedTuples()) {
            values[counter++] = (Instant)tuple.getElement(columnIndex);
        }

        return values;
    }

//    @Override
//    public void calculateQueryStatistics() {
//        int columnIndex = getDataModel().getColumnIndex(this);
//
//        int counter = 0;
//        for (Tuple tuple : getDataModel().getQueriedTupleSet()) {
//            Instant instant = (Instant)tuple.getElement(columnIndex);
//            if (counter == 0) {
//                querySummaryStats.setStartInstant(instant);
//                querySummaryStats.setEndInstant(instant);
//            } else {
//                if (instant.isBefore(querySummaryStats.getStartInstant())) {
//                    querySummaryStats.setStartInstant(instant);
//                } else if (instant.isAfter(querySummaryStats.getEndInstant())) {
//                    querySummaryStats.setEndInstant(instant);
//                }
//            }
//            counter++;
//        }
//    }

    public TemporalColumnSummaryStats getStatistics () {
        return summaryStats;
    }

//    public TemporalColumnSummaryStats getQueryStatistics() {
//        return querySummaryStats;
//    }

//    public void setStartInstant(Instant startInstant) {
//        startInstantProperty().set(startInstant);
//        startLocalDateTimeProperty().set(LocalDateTime.ofInstant(startInstant, ZoneOffset.UTC));
//    }
//
//    public Instant getStartInstant() {
//        return startInstantProperty().get();
//    }
//
//    public SimpleObjectProperty<Instant> startInstantProperty() {
//        if (startInstant == null) {
//            startInstant = new SimpleObjectProperty<>(this, "startInstant");
//        }
//        return startInstant;
//    }
//
//    public SimpleObjectProperty<LocalDateTime> startLocalDateTimeProperty() {
//        if (startLocalDateTime == null) {
//            startLocalDateTime = new SimpleObjectProperty<>(this, "startLocalDateTime");
//        }
//        return startLocalDateTime;
//    }
//
//    public void setEndInstant(Instant endInstant) {
//        endInstantProperty().set(endInstant);
//        endLocalDateTimeProperty().set(LocalDateTime.ofInstant(endInstant, ZoneOffset.UTC));
//    }
//
//    public Instant getEndInstant() {
//        return endInstantProperty().get();
//    }
//
//    public SimpleObjectProperty<Instant> endInstantProperty() {
//        if (endInstant == null) {
//            endInstant = new SimpleObjectProperty<>(this, "endInstant");
//        }
//        return endInstant;
//    }
//
//    public SimpleObjectProperty<LocalDateTime> endLocalDateTimeProperty() {
//        if (endLocalDateTime == null) {
//            endLocalDateTime = new SimpleObjectProperty<>(this, "endLocalDateTime");
//        }
//        return endLocalDateTime;
//    }

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
