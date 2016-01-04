package gov.ornl.csed.cda.hypervis;

/**
 * Created by csg on 1/30/15.
 */
public interface OverviewBarListener {
    public void overviewBarMarkerClicked(OverviewBar overviewBar, OverviewBarMarker marker);
    public void overviewBarMarkerDoubleClicked(OverviewBar overviewBar, OverviewBarMarker marker);
    public void overviewBarMarkerControlClicked(OverviewBar overviewBar, OverviewBarMarker marker);
}
