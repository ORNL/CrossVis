package gov.ornl.pcpview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class PCPCategoricalAxis extends PCPAxis {
    private final static Logger log = Logger.getLogger(DataModel.class.getName());

    private static final DecimalFormat percentageFormat = new DecimalFormat("0.0#%");

    // category rectangles
    private Group categoriesRectangleGroup;
    private HashMap<String, Rectangle> categoriesRectangleMap = new HashMap<>();

    // query category rectangles
    private Group queryCategoriesRectangleGroup;
    private HashMap<String, Rectangle> queryCategoriesRectangleMap = new HashMap<>();

    // dragging selection
    Rectangle draggingSelectionRectangle;

    public PCPCategoricalAxis(PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        categoriesRectangleGroup = new Group();
        queryCategoriesRectangleGroup = new Group();

        graphicsGroup.getChildren().add(categoriesRectangleGroup);

        registerListeners();
    }

    public Rectangle getCategoryRectangle(String category) {
        return categoriesRectangleMap.get(category);
    }

    public Rectangle getQueryCategoryRectangle(String category) {
        return queryCategoriesRectangleMap.get(category);
    }

    private CategoricalColumn categoricalColumn() {
        return (CategoricalColumn)getColumn();
    }

    private void registerListeners() {

    }

    public void layout(double center, double top, double width, double height) {
        super.layout(center, top, width, height);

        if (!dataModel.isEmpty()) {
            CategoricalHistogram histogram = categoricalColumn().getStatistics().getHistogram();

            // remove previously shown category shapes
            categoriesRectangleGroup.getChildren().clear();
            categoriesRectangleMap.clear();

            double lastRectangleBottomY = getFocusTopY();

            for (String category : histogram.getCategories()) {
                int categoryCount = histogram.getCategoryCount(category);
                double y = lastRectangleBottomY;
                double categoryHeight = GraphicsUtil.mapValue(categoryCount, 0, histogram.getTotalCount(), 0, getFocusBottomY()-getFocusTopY());
                Rectangle rectangle = new Rectangle(getAxisBar().getX()+2, y, getAxisBar().getWidth()-4, categoryHeight);
                rectangle.setStroke(histogramFill.darker());
                rectangle.setFill(histogramFill);
                rectangle.setArcHeight(6);
                rectangle.setArcWidth(6);

                rectangle.setOnMouseClicked(event -> {
                    handleCategoryRectangleClicked(rectangle, category);
                });

//                rectangle.setMouseTransparent(true);
                Tooltip.install(rectangle, new Tooltip(category + " : " + categoryCount + " (" + percentageFormat.format((double)categoryCount/histogram.getTotalCount()) + " of total)"));
//                categoriesRectangleList.add(rectangle);
                categoriesRectangleMap.put(category, rectangle);
                categoriesRectangleGroup.getChildren().add(rectangle);

                lastRectangleBottomY = rectangle.getY() + rectangle.getHeight();
            }

            if (graphicsGroup.getChildren().contains(queryCategoriesRectangleGroup)) {
                graphicsGroup.getChildren().remove(queryCategoriesRectangleGroup);
            }


            queryCategoriesRectangleGroup.getChildren().clear();
            queryCategoriesRectangleMap.clear();

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                CategoricalColumnSummaryStats queryColumnSummaryStats = (CategoricalColumnSummaryStats)dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());
                CategoricalHistogram queryHistogram = queryColumnSummaryStats.getHistogram();

                for (String category : queryHistogram.getCategories()) {
                    int queryCategoryCount = queryHistogram.getCategoryCount(category);
                    if (queryCategoryCount > 0) {
                        Rectangle overallCategoryRectangle = categoriesRectangleMap.get(category);
                        int overallCategoryCount = histogram.getCategoryCount(category);
                        double queryCategoryHeight = GraphicsUtil.mapValue(queryCategoryCount, 0, overallCategoryCount, 0, overallCategoryRectangle.getHeight());
                        double y = overallCategoryRectangle.getY() + ((overallCategoryRectangle.getHeight() - queryCategoryHeight) / 2.);
                        Rectangle queryRectangle = new Rectangle(getAxisBar().getX() + 2, y, getAxisBar().getWidth() - 4, queryCategoryHeight);
                        queryRectangle.setArcHeight(6);
                        queryRectangle.setArcWidth(6);
                        queryRectangle.setStroke(queryHistogramFill.darker());
                        queryRectangle.setFill(queryHistogramFill);
//                        queryRectangle.setMouseTransparent(true);

                        queryRectangle.setOnMouseClicked(event -> {
                            handleCategoryRectangleClicked(queryRectangle, category);
                        });

                        Tooltip.install(queryRectangle, new Tooltip(category + " : " + queryCategoryCount + " (" + percentageFormat.format((double) queryCategoryCount / overallCategoryCount) + " of category)"));

                        queryCategoriesRectangleMap.put(category, queryRectangle);
                        queryCategoriesRectangleGroup.getChildren().add(queryRectangle);
                    }
                }

                graphicsGroup.getChildren().add(queryCategoriesRectangleGroup);
            }
        }
    }

    private void handleCategoryRectangleClicked(Rectangle rectangle, String category) {
        log.info("got a click on a category rectangle (" + category + ")");

        // if there are no current axis selections, make a new selection and add this category
        if (getAxisSelectionList().isEmpty()) {
            HashSet<String> categories = new HashSet<>();
            categories.add(category);
            CategoricalColumnSelection columnSelection = new CategoricalColumnSelection(categoricalColumn(), categories);
            PCPCategoricalAxisSelection axisSelection = new PCPCategoricalAxisSelection(this, columnSelection, pane, dataModel);
            getAxisSelectionList().add(axisSelection);
            dataModel.addColumnSelectionRangeToActiveQuery(columnSelection);
        } else {
            for (PCPAxisSelection selection : getAxisSelectionList()) {
                PCPCategoricalAxisSelection categoricalSelection = (PCPCategoricalAxisSelection)selection;
                CategoricalColumnSelection categoricalColumnSelection = (CategoricalColumnSelection)categoricalSelection.getColumnSelectionRange();

                if (categoricalColumnSelection.getSelectedCategories().contains(category)) {
                    // remove the category from the selection
                    categoricalColumnSelection.removeCategory(category);
                } else {
                    categoricalColumnSelection.addCategory(category);
                }
            }
        }
    }

    @Override
    protected void handleAxisBarMousePressed() {

    }

    @Override
    protected void handleAxisBarMouseDragged(MouseEvent event) {
//        if (!dragging) {
//            dragging = true;
//        }
//
//        dragEndPoint = new Point2D(event.getX(), event.getY());
//
//        double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
//        double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());
//
//        selectionMaxY = selectionMaxY < getFocusTopY() ? getFocusTopY() : selectionMaxY;
//        selectionMinY = selectionMinY > getFocusBottomY() ? getFocusBottomY() : selectionMinY;
//
//        if (draggingSelectionRectangle == null) {
//            draggingSelectionRectangle = new Rectangle(getAxisBar().getX()-2, selectionMaxY, getAxisBar().getWidth()+4, (selectionMinY - selectionMaxY) );
//            draggingSelectionRectangle.setFill(Color.YELLOW);
//            draggingSelectionRectangle.setFill(Color.BLUE);
//            graphicsGroup.getChildren().add(draggingSelectionRectangle);
//        } else {
//            draggingSelectionRectangle.setY(selectionMaxY);
//            draggingSelectionRectangle.setHeight(selectionMinY - selectionMaxY);
//        }
    }

    @Override
    protected void handleAxisBarMouseReleased() {
//        if (draggingSelectionRectangle != null) {
//            graphicsGroup.getChildren().remove(draggingSelectionRectangle);
//            draggingSelectionRectangle = null;
//        }
    }

    @Override
    public Group getHistogramBinRectangleGroup() {
        return null;
    }

    @Override
    public Group getQueryHistogramBinRectangleGroup() {
        return null;
    }
}
