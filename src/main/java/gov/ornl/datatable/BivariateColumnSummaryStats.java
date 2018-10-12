package gov.ornl.datatable;

import java.util.logging.Logger;

public class BivariateColumnSummaryStats extends ColumnSummaryStats {
    private final static Logger log = Logger.getLogger(BivariateColumnSummaryStats.class.getName());

    public BivariateColumnSummaryStats(BivariateColumn bivariateColumn, int numHistogramBins, Query query) {
        super(bivariateColumn, numHistogramBins, query);
    }

    @Override
    public void calculateStatistics() {
        // nothing to do because this is a merge of two existing columns
    }

    @Override
    public void calculateHistogram() {
        // nothing to do because this is linked to other columns
    }

    public BivariateColumn bivariateColumn() { return (BivariateColumn)column; }
}
