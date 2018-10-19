package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CategoricalAxis extends UnivariateAxis {
    private static final DecimalFormat percentageFormat = new DecimalFormat("0.0#%");

    private final static double DEFAULT_CATEGORY_STROKE_WIDTH = 1.5f;
    private final static Color DEFAULT_CATEGORY_STROKE_COLOR = new Color(0.1, 0.1, 0.1, 1.0);
    private final static Color DEFAULT_CATEGORY_FILL_COLOR = Color.LIGHTGRAY;
    private final static Color DEFAULT_QUERY_STROKE_COLOR = new Color(0.5, 0.5, 0.5, 1.0);
    private final static Color DEFAULT_SELECTED_CATEGORY_STROKE_COLOR = Color.YELLOW;
    private final static Color DEFAULT_SELECTED_CATEGORY_FILL_COLOR = Color.YELLOW;

    // category rectangles
    private Group categoriesRectangleGroup;
    private HashMap<String, Rectangle> categoriesRectangleMap = new HashMap<>();

    // query category rectangles
    private Group queryCategoriesRectangleGroup;
    private HashMap<String, Rectangle> queryCategoriesRectangleMap = new HashMap<>();

    private Group nonQueryCategoriesRectangleGroup;
    private HashMap<String, Rectangle> nonQueryCategoriesRectangleMap = new HashMap<>();

    public CategoricalAxis(DataTableView dataTableView, Column column) {
        super(dataTableView, column);

        categoriesRectangleGroup = new Group();
        queryCategoriesRectangleGroup = new Group();
        nonQueryCategoriesRectangleGroup = new Group();

        getGraphicsGroup().getChildren().addAll(categoriesRectangleGroup);

        registerListeners();
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        // see if an axis selection already exists for the column selection
        for (AxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelection() == columnSelection) {
                // an axis selection already exists for the given column selection so abort
                return null;
            }
        }

        CategoricalColumnSelection categoricalColumnSelection = (CategoricalColumnSelection)columnSelection;

        CategoricalAxisSelection newAxisSelection = new CategoricalAxisSelection(this, categoricalColumnSelection);
        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        for (String category : categoriesRectangleMap.keySet()) {
            Rectangle rectangle = categoriesRectangleMap.get(category);
            if (axisPosition >= rectangle.getLayoutBounds().getMinY() &&
                    axisPosition < rectangle.getLayoutBounds().getMaxY()) {
                return category;
            }
        }
        return null;
    }

    public List<String> getCategories() {
        return categoricalColumn().getCategories();
    }

    public Rectangle getCategoryRectangle(String category) {
        return categoriesRectangleMap.get(category);
    }

    public Rectangle getQueryCategoryRectangle(String category) {
        return queryCategoriesRectangleMap.get(category);
    }

    public Rectangle getNonQueryCategoryRectangle(String category) {
        return nonQueryCategoriesRectangleMap.get(category);
    }

    private CategoricalColumn categoricalColumn() {
        return (CategoricalColumn)getColumn();
    }

    private void registerListeners() {
    }

    private void handleCategoryRectangleClicked(Rectangle rectangle, String category) {
        // if there are no current axis selections, make a new selection and add this category
        if (getAxisSelectionList().isEmpty()) {
            HashSet<String> categories = new HashSet<>();
            categories.add(category);
            CategoricalColumnSelection columnSelection = new CategoricalColumnSelection(categoricalColumn(), categories);
            CategoricalAxisSelection axisSelection = new CategoricalAxisSelection(this, columnSelection);
            getAxisSelectionList().add(axisSelection);
            getDataTable().addColumnSelectionRangeToActiveQuery(columnSelection);
        } else {
            ArrayList<AxisSelection> selectionsToRemove = new ArrayList<>();
            for (AxisSelection selection : getAxisSelectionList()) {
                CategoricalAxisSelection categoricalSelection = (CategoricalAxisSelection)selection;
                CategoricalColumnSelection categoricalColumnSelection = (CategoricalColumnSelection)categoricalSelection.getColumnSelection();

                if (categoricalColumnSelection.getSelectedCategories().contains(category)) {
                    // remove the category from the selection
                    categoricalColumnSelection.removeCategory(category);
                    if (categoricalColumnSelection.getSelectedCategories().isEmpty()) {
                        selectionsToRemove.add(selection);
                    }
                } else {
                    categoricalColumnSelection.addCategory(category);
                }
            }
            if (!selectionsToRemove.isEmpty()) {
                getAxisSelectionList().removeAll(selectionsToRemove);
            }
        }
    }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        if (!getDataTable().isEmpty()) {
            CategoricalHistogram histogram = categoricalColumn().getStatistics().getHistogram();

            HashSet<String> selectedCategories = new HashSet<>();
            for (AxisSelection axisSelection : getAxisSelectionList()) {
                selectedCategories.addAll(((CategoricalColumnSelection)axisSelection.getColumnSelection()).getSelectedCategories());
            }

            // remove previously shown category shapes
            categoriesRectangleGroup.getChildren().clear();
            categoriesRectangleMap.clear();

            double lastRectangleBottomY = getFocusMaxPosition();

            for (String category : histogram.getCategories()) {
                int categoryCount = histogram.getCategoryCount(category);
                double y = lastRectangleBottomY;
                double categoryHeight = GraphicsUtil.mapValue(categoryCount, 0, histogram.getTotalCount(), 0, getFocusMinPosition()-getFocusMaxPosition());

                Rectangle rectangle = new Rectangle(getAxisBar().getX(), y, getAxisBar().getWidth(), categoryHeight);
                rectangle.setStroke(DEFAULT_CATEGORY_STROKE_COLOR);
                rectangle.setFill(DEFAULT_CATEGORY_FILL_COLOR);
                rectangle.setStrokeWidth(DEFAULT_CATEGORY_STROKE_WIDTH);
                rectangle.setArcHeight(6);
                rectangle.setArcWidth(6);

                rectangle.setOnMouseClicked(event -> {
                    handleCategoryRectangleClicked(rectangle, category);
                });

                Tooltip.install(rectangle, new Tooltip(category + " : " + categoryCount + " of " +
                        histogram.getTotalCount() + " (" +
                        percentageFormat.format((double)categoryCount/histogram.getTotalCount()) + " of total)"));

                categoriesRectangleMap.put(category, rectangle);
                categoriesRectangleGroup.getChildren().add(rectangle);

                if (selectedCategories.contains(category)) {
                    Rectangle innerRectangle = new Rectangle(rectangle.getX()+1, rectangle.getY()+1,
                            rectangle.getWidth()-2, rectangle.getHeight()-2);
                    innerRectangle.setStroke(DEFAULT_SELECTED_CATEGORY_STROKE_COLOR);
                    innerRectangle.setFill(null);
                    innerRectangle.setArcWidth(6);
                    innerRectangle.setArcHeight(6);
                    innerRectangle.setMouseTransparent(true);
                    categoriesRectangleGroup.getChildren().add(innerRectangle);
                }

                lastRectangleBottomY = rectangle.getY() + rectangle.getHeight();
            }

            if (getGraphicsGroup().getChildren().contains(queryCategoriesRectangleGroup)) {
                getGraphicsGroup().getChildren().remove(queryCategoriesRectangleGroup);
            }
            if (getGraphicsGroup().getChildren().contains(nonQueryCategoriesRectangleGroup)) {
                getGraphicsGroup().getChildren().remove(nonQueryCategoriesRectangleGroup);
            }
            queryCategoriesRectangleGroup.getChildren().clear();
            queryCategoriesRectangleMap.clear();
            nonQueryCategoriesRectangleGroup.getChildren().clear();
            nonQueryCategoriesRectangleMap.clear();

            if (getDataTable().getActiveQuery().hasColumnSelections()) {
                CategoricalColumnSummaryStats queryColumnSummaryStats = (CategoricalColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());
                CategoricalHistogram queryHistogram = queryColumnSummaryStats.getHistogram();

                for (String category : queryHistogram.getCategories()) {
                    int queryCategoryCount = queryHistogram.getCategoryCount(category);
                    Rectangle overallCategoryRectangle = categoriesRectangleMap.get(category);
                    int overallCategoryCount = histogram.getCategoryCount(category);
                    int nonQueryCategoryCount = overallCategoryCount - queryCategoryCount;

                    Rectangle queryRectangle = new Rectangle (overallCategoryRectangle.getLayoutBounds().getMinX() + 4,
                            0, overallCategoryRectangle.getLayoutBounds().getWidth() - 8d, 0);
                    Rectangle nonQueryRectangle = new Rectangle (overallCategoryRectangle.getLayoutBounds().getMinX() + 4,
                            0, overallCategoryRectangle.getLayoutBounds().getWidth() - 8d, 0);

                    if (queryCategoryCount > 0) {
                        queryRectangle.setY(overallCategoryRectangle.getY() + 2d);

                        if (nonQueryCategoryCount > 0) {
                            double queryRectangleHeight = GraphicsUtil.mapValue(queryCategoryCount, 0, overallCategoryCount,
                                    0, overallCategoryRectangle.getHeight() - 4);
                            queryRectangle.setHeight(queryRectangleHeight);
                        } else {
                            queryRectangle.setHeight(overallCategoryRectangle.getHeight() - 4);
                        }

                        queryRectangle.setArcHeight(6);
                        queryRectangle.setArcWidth(6);
                        queryRectangle.setStroke(DEFAULT_QUERY_STROKE_COLOR);
                        queryRectangle.setFill(getDataTableView().getSelectedItemsColor());
                        queryRectangle.setMouseTransparent(true);

                        queryCategoriesRectangleMap.put(category, queryRectangle);
                        queryCategoriesRectangleGroup.getChildren().add(queryRectangle);
                    }

                    if (nonQueryCategoryCount > 0) {
                        if (queryCategoryCount > 0) {
                            double nonQueryRectangleHeight = GraphicsUtil.mapValue(nonQueryCategoryCount, 0,
                                    overallCategoryCount, 0, overallCategoryRectangle.getHeight() - 4);
                            nonQueryRectangle.setHeight(nonQueryRectangleHeight);
                            nonQueryRectangle.setY(queryRectangle.getY() + queryRectangle.getHeight());
                        } else {
                            nonQueryRectangle.setHeight(overallCategoryRectangle.getHeight() - 4);
                            nonQueryRectangle.setY(overallCategoryRectangle.getY() + 2d);
                        }

                        nonQueryRectangle.setArcHeight(6);
                        nonQueryRectangle.setArcWidth(6);
                        nonQueryRectangle.setStroke(DEFAULT_QUERY_STROKE_COLOR);
                        nonQueryRectangle.setFill(getDataTableView().getUnselectedItemsColor());
                        nonQueryRectangle.setMouseTransparent(true);

                        nonQueryCategoriesRectangleMap.put(category, nonQueryRectangle);
                        nonQueryCategoriesRectangleGroup.getChildren().add(nonQueryRectangle);
                    }

                    Tooltip.install(overallCategoryRectangle,
                            new Tooltip (category + " : " + overallCategoryCount + " of " +
                                    histogram.getTotalCount() + " (" +
                                    percentageFormat.format((double)overallCategoryCount/histogram.getTotalCount()) + " of total)\n" +
                                    queryCategoryCount + " of " + overallCategoryCount + " selected (" +
                                    percentageFormat.format((double)queryCategoryCount / overallCategoryCount) + ")"));
//                            ))
//                            new Tooltip(category + ": " + queryCategoryCount + " of " + overallCategoryCount + " selected (" +
//                                    percentageFormat.format((double)queryCategoryCount / overallCategoryCount) + ")"));
                }

                getGraphicsGroup().getChildren().addAll(nonQueryCategoriesRectangleGroup, queryCategoriesRectangleGroup);
            }
        }
    }

}
