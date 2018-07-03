package gov.ornl.scout.dataframeview;

import javafx.scene.paint.Color;

public class DataFrameViewDefaultSettings {
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static double DEFAULT_LINE_OPACITY = 0.5;
    public final static Color DEFAULT_SELECTED_ITEMS_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_LINE_OPACITY);
    public final static Color DEFAULT_UNSELECTED_ITEMS_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_LINE_OPACITY);
    public final static double DEFAULT_COLUMN_TITLE_TEXT_SIZE = 12d;
    public static final Color DEFAULT_ROW_POLYLINE_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.2);

}
