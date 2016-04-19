package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.timevis.TimeSeriesSelection;

/**
 * Created by csg on 4/18/16.
 */
public interface SelectionDetailsPanelListener {
    public void selectionDetailsPanelMouseHover(SelectionDetailsPanel selectionDetailsPanel, TimeSeriesSelection timeSeriesSelection);
    public void selectionDetailsPanelMouseClicked(SelectionDetailsPanel selectionDetailsPanel, TimeSeriesSelection timeSeriesSelection);
}
