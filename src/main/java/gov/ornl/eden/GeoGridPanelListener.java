package gov.ornl.eden;

public interface GeoGridPanelListener {
	public void geoGridPanelMouseLocationChanged(int x, int y);

	public void geoGridPanelAreaSelected(int left, int right, int bottom,
			int top);
}
