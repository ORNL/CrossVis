package gov.ornl.csed.cda.datatable;

import java.util.Set;

public class DoubleColumn extends Column {

    private DoubleColumnSummaryStats summaryStats;
//    private DoubleColumnSummaryStats querySummaryStats;

    public DoubleColumn(String name) {
        super(name);
//        summaryStats = new DoubleColumnSummaryStats(this, getDataModel().getNumHistogramBins());
//        querySummaryStats = new DoubleColumnSummaryStats();
    }

    /*
    public void calculateQueryStatistics() {
        double values[] = getQueryValues();

        DescriptiveStatistics stats = new DescriptiveStatistics(values);

        querySummaryStats.setMinValue(stats.getMin());
        querySummaryStats.setMaxValue(stats.getMax());
        querySummaryStats.setMeanValue(stats.getMean());
        querySummaryStats.setMedianValue(stats.getPercentile(50));
        querySummaryStats.setVarianceValue(stats.getVariance());
        querySummaryStats.setStandardDeviationValue(stats.getStandardDeviation());
        querySummaryStats.setPercentile25Value(stats.getPercentile(25));
        querySummaryStats.setPercentile75Value(stats.getPercentile(75));
        querySummaryStats.setSkewnessValue(stats.getSkewness());
        querySummaryStats.setKurtosisValue(stats.getKurtosis());

        // calculate whiskers for box plot 1.5 of IQR
        double iqr_range = 1.5 * querySummaryStats.getIQR();
        double lowerFence = querySummaryStats.getPercentile25Value() - iqr_range;
        double upperFence = querySummaryStats.getPercentile75Value() + iqr_range;
        double sorted_data[] = stats.getSortedValues();

        // find upper datum that is not greater than upper fence
        if (upperFence >= querySummaryStats.getMaxValue()) {
            querySummaryStats.setUpperWhiskerValue(querySummaryStats.getMaxValue());
        } else {
            // find largest datum not larger than upper fence value
            for (int i = sorted_data.length - 1; i >= 0; i--) {
                if (sorted_data[i] <= upperFence) {
                    querySummaryStats.setUpperWhiskerValue(sorted_data[i]);
                    break;
                }
            }
        }

        if (lowerFence <= querySummaryStats.getMinValue()) {
            querySummaryStats.setLowerWhiskerValue(querySummaryStats.getMinValue());
        } else {
            // find smallest datum not less than lower fence value
            for (int i = 0; i < sorted_data.length; i++) {
                if (sorted_data[i] >= lowerFence) {
                    querySummaryStats.setLowerWhiskerValue(sorted_data[i]);
                    break;
                }
            }
        }
    }
    */

//    public double[] getValues() {
//        return dataModel.getColumnValues(this);
//    }

    public void calculateStatistics() {
        if (summaryStats == null) {
            summaryStats = new DoubleColumnSummaryStats(this, getDataModel().getNumHistogramBins());
        }
        summaryStats.setValues(getValues());
//        DescriptiveStatistics stats = new DescriptiveStatistics(values);
//
//        summaryStats.setMinValue(stats.getMin());
//        summaryStats.setMaxValue(stats.getMax());
//        summaryStats.setMeanValue(stats.getMean());
//        summaryStats.setMedianValue(stats.getPercentile(50));
//        summaryStats.setVarianceValue(stats.getVariance());
//        summaryStats.setStandardDeviationValue(stats.getStandardDeviation());
//        summaryStats.setPercentile25Value(stats.getPercentile(25));
//        summaryStats.setPercentile75Value(stats.getPercentile(75));
//        summaryStats.setSkewnessValue(stats.getSkewness());
//        summaryStats.setKurtosisValue(stats.getKurtosis());
//
//        // calculate whiskers for box plot 1.5 of IQR
//        double iqr_range = 1.5 * summaryStats.getIQR();
//        double lowerFence = summaryStats.getPercentile25Value() - iqr_range;
//        double upperFence = summaryStats.getPercentile75Value() + iqr_range;
//        double sorted_data[] = stats.getSortedValues();
//
//        // find upper datum that is not greater than upper fence
//        if (upperFence >= summaryStats.getMaxValue()) {
//            summaryStats.setUpperWhiskerValue(summaryStats.getMaxValue());
//        } else {
//            // find largest datum not larger than upper fence value
//            for (int i = sorted_data.length - 1; i >= 0; i--) {
//                if (sorted_data[i] <= upperFence) {
//                    summaryStats.setUpperWhiskerValue(sorted_data[i]);
//                    break;
//                }
//            }
//        }
//
//        if (lowerFence <= summaryStats.getMinValue()) {
//            summaryStats.setLowerWhiskerValue(summaryStats.getMinValue());
//        } else {
//            // find smallest datum not less than lower fence value
//            for (int i = 0; i < sorted_data.length; i++) {
//                if (sorted_data[i] >= lowerFence) {
//                    summaryStats.setLowerWhiskerValue(sorted_data[i]);
//                    break;
//                }
//            }
//        }
    }

    public double[] getValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        double values[] = new double[getDataModel().getTupleCount()];
        for (int i = 0; i < getDataModel().getTupleCount(); i++) {
            values[i] = (Double) getDataModel().getTuple(i).getElement(columnIndex);
        }
        
        return values;
    }

    public double[] getQueriedValues() {
        int columnIndex = getDataModel().getColumnIndex(this);

        Set<Tuple> queriedTuples = getDataModel().getActiveQuery().getQueriedTuples();
        double values[] = new double[queriedTuples.size()];

        int counter = 0;
        for (Tuple tuple : queriedTuples) {
            values[counter++] = (Double) tuple.getElement(columnIndex);
        }

        return values;
    }

    public DoubleColumnSummaryStats getStatistics() { return summaryStats; }

//    public DoubleColumnSummaryStats getQuerySummaryStats() {
//        return querySummaryStats;
//    }
}
