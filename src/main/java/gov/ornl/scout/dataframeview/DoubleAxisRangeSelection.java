package gov.ornl.scout.dataframeview;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class DoubleAxisRangeSelection extends AxisSelection {

    private Rectangle rangeRectangle;
    private Polyline minPositionLine;
    private Polyline maxPositionLine;

    private Point2D dragStartPoint;
    private Point2D dragEndPoint;
    private boolean dragging;

    private double minValue;
    private double maxValue;
    private double minValuePosition;
    private double maxValuePosition;

    private Text minValueText;
    private Text maxValueText;
    private Rectangle selectionRectangle;

    public DoubleAxisRangeSelection(Axis axis, double minValue, double maxValue, double minValuePosition,
                                    double maxValuePosition) {
        this.axis = axis;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minValuePosition = minValuePosition;
        this.maxValuePosition = maxValuePosition;

        minValueText = new Text(String.valueOf(minValue));
        minValueText.setFont(new Font(DataFrameViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_SIZE));
        minValueText.setVisible(false);
        minValueText.setFill(DataFrameViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_FILL);

        maxValueText = new Text(String.valueOf(maxValue));
        maxValueText.setFont(new Font(DataFrameViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_SIZE));
        maxValueText.setVisible(false);
        maxValueText.setFill(DataFrameViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_FILL);

        selectionRectangle = new Rectangle();
        selectionRectangle.setFill(DataFrameViewDefaultSettings.DEFAULT_AXIS_SELECTION_RECTANGLE_FILL_COLOR);

        getGraphicsGroup().getChildren().addAll(minValueText, maxValueText);
    }

    @Override
    public void layout() {

    }
}
