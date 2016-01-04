package gov.ornl.csed.cda.hypervis;

/**
 * Created by csg on 8/19/14.
 */
public interface HyperVariatePanelListener {
    public void hyperVariatePanelAxisClicked(HyperVariatePanel hyperVariatePanel, int axisIndex);
    public void hyperVariatePanelPolylineClicked(HyperVariatePanel hyperVariatePanel, int x, int y);
}
