package gov.ornl.datatable;

public abstract class ColumnSummaryStats {
    private static final int DEFAULT_NUM_HISTOGRAM_BINS = 50;
    private static final int MAX_NUM_HISTOGRAM_BINS = 100;

    protected Column column;
    protected Query query = null;
    protected int numHistogramBins = DEFAULT_NUM_HISTOGRAM_BINS;

    public ColumnSummaryStats(Column column, int numHistogramBins, Query query) {
        this.query = query;
        this.column = column;
        this.numHistogramBins = numHistogramBins;
    }

    public Column getColumn() {
        return column;
    }

    public abstract void calculateStatistics();

    public abstract void calculateHistogram();

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
