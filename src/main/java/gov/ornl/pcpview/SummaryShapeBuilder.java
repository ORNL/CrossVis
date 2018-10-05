package gov.ornl.pcpview;

import gov.ornl.datatable.CategoricalColumn;
import gov.ornl.datatable.CategoricalHistogram;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class SummaryShapeBuilder {
    public static void buildShapes(ArrayList<PCPUnivariateAxis> axisList, PCPView pcpView, Group shapeGroup) {
        for (int iaxis = 1; iaxis < axisList.size(); iaxis++) {
            PCPUnivariateAxis rightAxis = axisList.get(iaxis);
            PCPUnivariateAxis leftAxis = axisList.get(iaxis-1);

            Group segmentGroup = null;
            if (leftAxis instanceof PCPDoubleAxis && rightAxis instanceof PCPDoubleAxis) {
                segmentGroup = buildSegmentShapes((PCPDoubleAxis)leftAxis, (PCPDoubleAxis)rightAxis, pcpView);
            } else if (leftAxis instanceof PCPCategoricalAxis && rightAxis instanceof PCPDoubleAxis) {
                segmentGroup = buildSegmentShapes((PCPCategoricalAxis)leftAxis, (PCPDoubleAxis)rightAxis, pcpView);
            } else if (leftAxis instanceof PCPDoubleAxis && rightAxis instanceof PCPCategoricalAxis) {
                segmentGroup = buildSegmentShapes((PCPDoubleAxis)leftAxis, (PCPCategoricalAxis)rightAxis, pcpView);
            }

            if (segmentGroup != null) {
                shapeGroup.getChildren().add(segmentGroup);
            }
        }
    }

    private static Group buildSegmentShapes(PCPDoubleAxis leftAxis, PCPCategoricalAxis rightAxis, PCPView pcpView) {
        Group segmentGroup = new Group();

        CategoricalHistogram catHistogram = ((CategoricalColumn)rightAxis.getColumn()).getStatistics().getHistogram();

        double leftAxisRangeTop = leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY();
        double leftAxisRangeBottom = leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY();
        double leftAxisX = leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX();
        double leftAxisRangeHeight = leftAxisRangeBottom - leftAxisRangeTop;
        double leftAxisLastBottomY = leftAxisRangeTop;

        for (String category : rightAxis.getCategories()) {
            Rectangle categoryRectangle = rightAxis.getCategoryRectangle(category);
            double leftAxisCategoryHeight = leftAxisRangeHeight * ((double)catHistogram.getCategoryCount(category) / catHistogram.getTotalCount());

            Polygon catPolygon = new Polygon(new double[]{
                    categoryRectangle.getLayoutBounds().getMinX(), categoryRectangle.getLayoutBounds().getMaxY(),
                    leftAxisX, leftAxisLastBottomY + leftAxisCategoryHeight,
                    leftAxisX, leftAxisLastBottomY,
                    categoryRectangle.getLayoutBounds().getMinX(), categoryRectangle.getLayoutBounds().getMinY()
            });
            catPolygon.setFill(pcpView.getOverallSummaryFillColor());

            leftAxisLastBottomY += leftAxisCategoryHeight;

            segmentGroup.getChildren().add(catPolygon);
        }

        return segmentGroup;
    }

    private static Group buildSegmentShapes(PCPCategoricalAxis leftAxis, PCPDoubleAxis rightAxis, PCPView pcpView) {
        Group segmentGroup = new Group();

        CategoricalHistogram catHistogram = ((CategoricalColumn)leftAxis.getColumn()).getStatistics().getHistogram();

        double rightAxisRangeTop = rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY();
        double rightAxisRangeBottom = rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY();
        double rightAxisX = rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX();
        double rightAxisRangeHeight = rightAxisRangeBottom - rightAxisRangeTop;
        double rightAxisLastBottomY = rightAxisRangeTop;

        for (String category : leftAxis.getCategories()) {
            Rectangle categoryRectangle = leftAxis.getCategoryRectangle(category);
            double rightAxisCategoryHeight = rightAxisRangeHeight * ((double)catHistogram.getCategoryCount(category) / catHistogram.getTotalCount());

            Polygon catPolygon = new Polygon(new double[]{
                    categoryRectangle.getLayoutBounds().getMaxX(), categoryRectangle.getLayoutBounds().getMaxY(),
                    rightAxisX, rightAxisLastBottomY + rightAxisCategoryHeight,
                    rightAxisX, rightAxisLastBottomY,
                    categoryRectangle.getLayoutBounds().getMaxX(), categoryRectangle.getLayoutBounds().getMinY()
            });
            catPolygon.setFill(pcpView.getOverallSummaryFillColor());

            rightAxisLastBottomY += rightAxisCategoryHeight;

            segmentGroup.getChildren().add(catPolygon);
        }

        return segmentGroup;
    }

    private static Group buildSegmentShapes(PCPDoubleAxis leftAxis, PCPDoubleAxis rightAxis, PCPView pcpView) {
        Group segmentGroup = new Group();

        if (leftAxis.getDataTable().getActiveQuery().hasColumnSelections()) {
            Polygon queryDispersionPolygon = new Polygon(new double[] {
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getQueryDispersionRectangle().getLayoutBounds().getMinY(),
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getQueryDispersionRectangle().getLayoutBounds().getMinY()
            });
            queryDispersionPolygon.setFill(pcpView.getSelectedItemsColor());

            Line queryTypicalLine = new Line(leftAxis.getBarRightX(), leftAxis.getQueryTypicalLine().getEndY(),
                    rightAxis.getBarLeftX(), rightAxis.getQueryTypicalLine().getStartY());
            queryTypicalLine.setStroke(pcpView.getSelectedItemsColor().darker());
            queryTypicalLine.setStrokeWidth(2d);

            Polygon nonQueryDispersionPolygon = new Polygon(new double[] {
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getNonQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getNonQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getNonQueryDispersionRectangle().getLayoutBounds().getMinY(),
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getNonQueryDispersionRectangle().getLayoutBounds().getMinY()
            });
            nonQueryDispersionPolygon.setFill(pcpView.getUnselectedItemsColor());

            Line nonQueryTypicalLine = new Line(leftAxis.getBarRightX(), leftAxis.getNonQueryTypicalLine().getEndY(),
                    rightAxis.getBarLeftX(), rightAxis.getNonQueryTypicalLine().getStartY());
            nonQueryTypicalLine.setStroke(pcpView.getUnselectedItemsColor().darker());
            nonQueryTypicalLine.setStrokeWidth(2d);

            segmentGroup.getChildren().addAll(nonQueryDispersionPolygon, nonQueryTypicalLine, queryDispersionPolygon,
                    queryTypicalLine);
        } else {
            Polygon overallDispersionPolygon = new Polygon(new double[] {
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                    rightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY(),
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                    leftAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY()
            });
            overallDispersionPolygon.setFill(pcpView.getOverallSummaryFillColor());

            Line overallTypicalLine = new Line(leftAxis.getBarRightX(), leftAxis.getOverallTypicalLine().getEndY(),
                    rightAxis.getBarLeftX(), rightAxis.getOverallTypicalLine().getStartY());
            overallTypicalLine.setStroke(pcpView.getOverallSummaryStrokeColor());
            overallTypicalLine.setStrokeWidth(2d);

            segmentGroup.getChildren().addAll(overallDispersionPolygon, overallTypicalLine);
        }

        return segmentGroup;
    }
}
