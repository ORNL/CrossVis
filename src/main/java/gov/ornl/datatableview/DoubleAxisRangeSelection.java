package gov.ornl.datatableview;

import gov.ornl.datatable.DoubleColumnSelectionRange;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.logging.Logger;


public class DoubleAxisRangeSelection extends AxisSelection {
    private final static Logger log = Logger.getLogger(DoubleAxisRangeSelection.class.getName());

    private Rectangle rangeRectangle;
    private Polyline minPositionLine;
    private Polyline maxPositionLine;

    private Point2D dragStartPoint;
    private Point2D dragEndPoint;
    private boolean dragging;

    private DoubleColumnSelectionRange columnSelectionRange;
    private double minValuePosition;
    private double maxValuePosition;

    private Text minValueText;
    private Text maxValueText;
    private Rectangle selectionRectangle;

    public DoubleAxisRangeSelection(Axis axis, DoubleColumnSelectionRange columnSelectionRange,
                                    double minValuePosition, double maxValuePosition) {
        this.axis = axis;
        this.columnSelectionRange = columnSelectionRange;

        minValueText = new Text(String.valueOf(columnSelectionRange.getMinValue()));
        minValueText.setFont(new Font(DataTableViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_SIZE));
        minValueText.setVisible(false);
        minValueText.setFill(DataTableViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_FILL);

        maxValueText = new Text(String.valueOf(columnSelectionRange.getMaxValue()));
        maxValueText.setFont(new Font(DataTableViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_SIZE));
        maxValueText.setVisible(false);
        maxValueText.setFill(DataTableViewDefaultSettings.DEFAULT_AXIS_SELECTION_TEXT_FILL);

        selectionRectangle = new Rectangle();
        selectionRectangle.setFill(DataTableViewDefaultSettings.DEFAULT_AXIS_SELECTION_RECTANGLE_FILL_COLOR);

        getGraphicsGroup().getChildren().addAll(selectionRectangle, minValueText, maxValueText);
    }

    public DoubleColumnSelectionRange getColumnSelectionRange() {
        return columnSelectionRange;
    }

    private DoubleAxis doubleAxis() { return (DoubleAxis)getAxis(); }

    private Orientation getAxisOrientation() { return getAxis().getOrientation(); }

    @Override
    public void layout() {
        if (getAxisOrientation() == Orientation.HORIZONTAL) {
            selectionRectangle.setY(maxValuePosition);
            selectionRectangle.setHeight(minValuePosition - maxValuePosition);
            selectionRectangle.setX(getAxis().getAxisBarLeft());
            selectionRectangle.setWidth(getAxis().getAxisBarWidth());

            minValueText.setY(minValuePosition + minValueText.getLayoutBounds().getHeight());
            minValueText.setX(getAxis().getCenterX() - (minValueText.getLayoutBounds().getWidth() / 2.));

            maxValueText.setY(maxValuePosition - 2.);
            maxValueText.setX(getAxis().getCenterX() - (maxValueText.getLayoutBounds().getWidth() / 2.));
        } else {
            selectionRectangle.setX(minValuePosition);
            selectionRectangle.setWidth(maxValuePosition - minValuePosition);
            selectionRectangle.setY(getAxis().getAxisBarTop());
            selectionRectangle.setHeight(getAxis().getAxisBarHeight());

            minValueText.setY(getAxis().getCenterY() + minValueText.getLayoutBounds().getHeight() / 2.);
            minValueText.setX(minValuePosition - minValueText.getLayoutBounds().getWidth());

            maxValueText.setY(getAxis().getCenterY() + maxValueText.getLayoutBounds().getHeight() / 2.);
            maxValueText.setX(maxValuePosition);
        }
    }

    public void update(double minValue, double maxValue, double minValuePosition, double maxValuePosition) {
        columnSelectionRange.setMaxValue(maxValue);
        columnSelectionRange.setMinValue(minValue);
        this.minValuePosition = minValuePosition;
        this.maxValuePosition = maxValuePosition;
        layout();
    }
}
