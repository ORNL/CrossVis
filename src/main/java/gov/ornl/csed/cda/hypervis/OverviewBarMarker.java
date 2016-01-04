package gov.ornl.csed.cda.hypervis;

import java.awt.*;

public class OverviewBarMarker {
	public float position;
    public Color fillColor;
    public Color outlineColor;
    public String label;
    private Shape markerShape;
    private boolean highlighted = false;
    private boolean selected = false;
    private boolean visible = false;

	public OverviewBarMarker(float position, Color fillColor, Color outlineColor, String label, boolean highlighted,
                             boolean selected, boolean visible) {
		this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.position = position;
        this.label = label;
        this.highlighted = highlighted;
        this.selected = selected;
        this.visible = visible;
	}

    public void setPosition(float position) {
        this.position = position;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setMarkerShape(Shape markerShape) {
        this.markerShape = markerShape;
    }

    public Shape getMarkerShape() {
        return markerShape;
    }
}
