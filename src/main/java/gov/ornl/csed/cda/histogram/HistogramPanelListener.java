package gov.ornl.csed.cda.histogram;

/**
 * Created by csg on 4/20/16.
 */
public interface HistogramPanelListener {
    public void histogramPanelLowerLimitChanged(HistogramPanel histogramPanel, double lowerLimitValue);
    public void histogramPanelUpperLimitChanged(HistogramPanel histogramPanel, double upperLimitValue);
}
