package gov.ornl.datatableview;

import gov.ornl.datatable.CategoricalColumn;
import gov.ornl.datatable.Column;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CategoricalAxis extends Axis {
    private final static double DEFAULT_CATEGORY_STROKE_WIDTH = 1.f;
    private final static Color DEFAULT_CATEGORY_STROKE_COLOR = new Color(0.1, 0.1, 0.1, 1.0);
    private final static Color DEFAULT_CATEGORY_FILL_COLOR = Color.LIGHTGRAY;
    private final static Color DEFAULT_QUERY_STROKE_COLOR = new Color(0.5, 0.5, 0.5, 1.0);
    private final static Color DEFAULT_SELECTED_CATEGORY_STROKE_COLOR = Color.YELLOW;
    private final static Color DEFAULT_SELECTED_CATEGORY_FILL_COLOR = Color.YELLOW;

    private static final DecimalFormat percentageFormat = new DecimalFormat("0.0#%");

    private Group categoryRectanglesGroup = new Group();
    private HashMap<String, Rectangle> categoryRectangleMap = new HashMap<>();


    public CategoricalAxis(DataTableView dataFrameView, Column column, Orientation orientation) {
        super(dataFrameView, column, orientation);

        graphicsGroup.getChildren().add(categoryRectanglesGroup);
    }

    public CategoricalColumn categoricalColumn() { return (CategoricalColumn)column; }

    public String getCategoryForAxisPosition(double axisPosition) {
        for (String category : categoryRectangleMap.keySet()) {
            Rectangle rectangle = categoryRectangleMap.get(category);
            if (axisPosition >= rectangle.getLayoutBounds().getMinY() &&
                    axisPosition < rectangle.getLayoutBounds().getMaxY()) {
                return category;
            }
        }

        return null;
    }

    public List<String> getCategories() { return categoricalColumn().getCategories(); }

    public Rectangle getCategoryRectangle(String category) { return categoryRectangleMap.get(category); }


    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        return getCategoryForAxisPosition(axisPosition);
    }

    @Override
    protected void handleAxisBarMousePressed(MouseEvent event) {

    }

    @Override
    protected void handleAxisBarMouseDragged(MouseEvent event) {

    }

    @Override
    protected void handleAxisBarMouseReleased(MouseEvent event) {

    }

    protected void layout() {
        super.layout();

        categoryRectangleMap.clear();
        categoryRectanglesGroup.getChildren().clear();

        Rectangle lastCategoryRectangle = null;

        for (String category : categoricalColumn().getCategories()) {
            int categoryCount = categoricalColumn().getStatistics().getHistogram().getCategoryCount(category);

            Rectangle rectangle = null;
            if (orientation == Orientation.HORIZONTAL) {
                double categoryHeight = GraphicsUtil.mapValue(categoryCount, 0, categoricalColumn().getStatistics().getHistogram().getTotalCount(),
                        0, getAxisBarHeight());
                double rectangleTop = lastCategoryRectangle == null ? getAxisBarTop() : lastCategoryRectangle.getY() + lastCategoryRectangle.getHeight();

                rectangle = new Rectangle(getAxisBarLeft(), rectangleTop, getAxisBarSize(), categoryHeight);

            } else {
                double categoryWidth = GraphicsUtil.mapValue(categoryCount, 0, categoricalColumn().getStatistics().getHistogram().getTotalCount(),
                        0, getAxisBarWidth());
                double rectangleLeft = lastCategoryRectangle == null ? getAxisBarLeft() : lastCategoryRectangle.getX() + lastCategoryRectangle.getWidth();

                rectangle = new Rectangle(rectangleLeft, getAxisBarTop(), categoryWidth, getAxisBarHeight());
            }

            rectangle.setStroke(DEFAULT_CATEGORY_STROKE_COLOR);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setFill(DEFAULT_CATEGORY_FILL_COLOR);
            rectangle.setStrokeWidth(DEFAULT_CATEGORY_STROKE_WIDTH);
            rectangle.setArcHeight(6);
            rectangle.setArcWidth(6);

//            rectangle.setOnMouseClicked(event -> { handleCategoryRectangleClicked(rectangle, category); });
            Tooltip.install(rectangle, new Tooltip(category + " : " + categoryCount + " of " +
                    categoricalColumn().getStatistics().getHistogram().getTotalCount() +
                    " (" + percentageFormat.format((double)categoryCount / categoricalColumn().getStatistics().getHistogram().getTotalCount()) +
                    " of total)"));

            categoryRectangleMap.put(category, rectangle);
            categoryRectanglesGroup.getChildren().add(rectangle);

            lastCategoryRectangle = rectangle;
        }
    }

    private void handleCategoryRectangleClicked(Rectangle rectangle, String category) {
        log.info("Rectangle for category '" + category + "' clicked.");
    }
}
