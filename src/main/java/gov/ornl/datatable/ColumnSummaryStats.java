package gov.ornl.datatable;

import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

public abstract class ColumnSummaryStats {
    private static final int DEFAULT_NUM_HISTOGRAM_BINS = 50;
    private static final int MAX_NUM_HISTOGRAM_BINS = 100;

    protected Column column;
    protected Query query = null;
    protected SimpleMapProperty<Column, Histogram2D> columnHistogram2DMap;
    protected int numHistogramBins = DEFAULT_NUM_HISTOGRAM_BINS;

    public ColumnSummaryStats(Column column, int numHistogramBins, Query query) {
        this.query = query;
        this.column = column;
        this.numHistogramBins = numHistogramBins;
        columnHistogram2DMap = new SimpleMapProperty<>(FXCollections.observableHashMap());
    }

    public Column getColumn() {
        return column;
    }

    public abstract void calculateStatistics();

    public abstract void calculateHistogram();

    public void calculateHistogram2D(ColumnSummaryStats columnSummaryStats) {
        Histogram2DDimension xDimension = null;
        if (this instanceof TemporalColumnSummaryStats) {
            TemporalColumnSummaryStats xColumnSummaryStats = (TemporalColumnSummaryStats)this;
            xDimension = new Histogram2DDimension.Temporal(xColumnSummaryStats.getValues(), numHistogramBins,
                    ((TemporalColumnSummaryStats)xColumnSummaryStats.getColumn().getStatistics()).getStartInstant(),
                    ((TemporalColumnSummaryStats)xColumnSummaryStats.getColumn().getStatistics()).getEndInstant());
        } else if (this instanceof  DoubleColumnSummaryStats) {
            DoubleColumnSummaryStats xColumnSummaryStats = (DoubleColumnSummaryStats)this;
            xDimension = new Histogram2DDimension.Double(xColumnSummaryStats.getValues(), numHistogramBins,
                    ((DoubleColumnSummaryStats)xColumnSummaryStats.getColumn().getStatistics()).getMinValue(),
                    ((DoubleColumnSummaryStats)xColumnSummaryStats.getColumn().getStatistics()).getMaxValue());
        } else if (this instanceof CategoricalColumnSummaryStats) {
            CategoricalColumnSummaryStats xColumnSummaryStats = (CategoricalColumnSummaryStats)this;
            xDimension = new Histogram2DDimension.Categorical(xColumnSummaryStats.getValues(), xColumnSummaryStats.getColumnCategories());
        }

        Histogram2DDimension yDimension = null;
        if (columnSummaryStats.getColumn() instanceof TemporalColumn) {
            TemporalColumnSummaryStats yColumnSummaryStats = (TemporalColumnSummaryStats)columnSummaryStats;
            yDimension = new Histogram2DDimension.Temporal(yColumnSummaryStats.getValues(),
                    columnSummaryStats.getNumHistogramBins(),
                    ((TemporalColumnSummaryStats)yColumnSummaryStats.getColumn().getStatistics()).getStartInstant(),
                    ((TemporalColumnSummaryStats)yColumnSummaryStats.getColumn().getStatistics()).getEndInstant());
        } else if (columnSummaryStats.getColumn() instanceof DoubleColumn) {
            DoubleColumnSummaryStats yColumnSummaryStats = (DoubleColumnSummaryStats)columnSummaryStats;
            yDimension = new Histogram2DDimension.Double(yColumnSummaryStats.getValues(), columnSummaryStats.getNumHistogramBins(),
                    ((DoubleColumnSummaryStats)yColumnSummaryStats.getColumn().getStatistics()).getMinValue(),
                    ((DoubleColumnSummaryStats)yColumnSummaryStats.getColumn().getStatistics()).getMaxValue());
        } else if (columnSummaryStats.getColumn() instanceof CategoricalColumn) {
            CategoricalColumnSummaryStats yColumnSummaryStats = (CategoricalColumnSummaryStats)columnSummaryStats;
            yDimension = new Histogram2DDimension.Categorical(yColumnSummaryStats.getValues(), yColumnSummaryStats.getColumnCategories());
        }

//        if (xDimension instanceof Histogram2DDimension.Categorical || yDimension instanceof Histogram2DDimension.Categorical) {
//            System.out.println("categorical dimensions");
//        }

        Histogram2D histogram2D = new Histogram2D(xDimension, yDimension);
        columnHistogram2DMap.put(columnSummaryStats.getColumn(), histogram2D);
    }

    public int getMaxHistogram2DBinCount() {
        int maxBinCount = 0;
        for (Histogram2D histogram2D : columnHistogram2DMap.values()) {
            if (histogram2D.getMaxBinCount() > maxBinCount) {
                maxBinCount = histogram2D.getMaxBinCount();
            }
        }
        return maxBinCount;
    }

    public Histogram2D getColumnHistogram2D(Column column) {
        return columnHistogram2DMap.get(column);
    }

    public void setNumHistogramBins(int numBins) {
        if (numBins != numHistogramBins) {
            numHistogramBins = numBins;
            calculateHistogram();
        }
    }

    public int getNumHistogramBins() {
        return numHistogramBins;
    }
}
