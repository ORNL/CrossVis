package gov.ornl.csed.cda.timevis;

import java.time.Instant;

/**
 * Created by csg on 4/14/16.
 */
public interface TimeSeriesPanelSelectionListener {
    public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection);
    public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection, Instant previousStartInstant, Instant previousEndInstant);
    public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection);
}
