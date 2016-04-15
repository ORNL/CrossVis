package gov.ornl.csed.cda.timevis;

/**
 * Created by csg on 4/14/16.
 */
public interface TimeSeriesPanelSelectionListener {
    public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection);
    public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection);
    public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection);
}
