package gov.ornl.pcpview;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;

public class SummaryShapeRenderer {
    public static void render(Canvas selectedCanvas, Canvas unselectedCanvas, ArrayList<PCPAxis> axisList,
                              Color overallFillColor, Color overallStrokeColor,
                              Color queryFillColor, Color queryStrokeColor,
                              Color nonqueryFillColor, Color nonqueryStrokeColor) {
        selectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        selectedCanvas.getGraphicsContext2D().clearRect(0, 0, selectedCanvas.getWidth(), selectedCanvas.getHeight());
        selectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        selectedCanvas.getGraphicsContext2D().setLineDashes(2d, 2d);

        unselectedCanvas.getGraphicsContext2D().setLineCap(StrokeLineCap.BUTT);
        unselectedCanvas.getGraphicsContext2D().clearRect(0, 0, unselectedCanvas.getWidth(), unselectedCanvas.getHeight());
        unselectedCanvas.getGraphicsContext2D().setLineWidth(2d);
        unselectedCanvas.getGraphicsContext2D().setLineDashes(2d, 2d);
        
        for (int iaxis = 1; iaxis < axisList.size(); iaxis++) {
            PCPAxis rightAxis = axisList.get(iaxis);
            PCPAxis leftAxis = axisList.get(iaxis-1);
            
            if (leftAxis instanceof PCPDoubleAxis && rightAxis instanceof PCPDoubleAxis) {
                PCPDoubleAxis dLeftAxis = (PCPDoubleAxis)leftAxis;
                PCPDoubleAxis dRightAxis = (PCPDoubleAxis)rightAxis;

                if (!(Double.isNaN(dLeftAxis.getOverallTypicalLine().getEndY())) &&
                        !(Double.isNaN(dRightAxis.getOverallTypicalLine().getStartY()))) {
                    double xValues[] = new double[]{dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                            dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                            dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                            dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX()};
                    double yValues[] = new double[]{dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                            dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxY(),
                            dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY(),
                            dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMinY()};

                    unselectedCanvas.getGraphicsContext2D().setFill(overallFillColor);

                    unselectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);

                    unselectedCanvas.getGraphicsContext2D().setStroke(overallStrokeColor);
                    unselectedCanvas.getGraphicsContext2D().strokeLine(dLeftAxis.getBarRightX(), dLeftAxis.getOverallTypicalLine().getEndY(),
                            dRightAxis.getBarLeftX(), dRightAxis.getOverallTypicalLine().getStartY());
                }

                if (leftAxis.dataModel.getActiveQuery().hasColumnSelections()) {
                    if (!(Double.isNaN(dLeftAxis.getQueryTypicalLine().getEndY())) &&
                            !(Double.isNaN(dRightAxis.getQueryTypicalLine().getStartY()))) {

                        double xValues[] = new double[]{dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX(),
                                dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                                dRightAxis.getOverallDispersionRectangle().getLayoutBounds().getMinX(),
                                dLeftAxis.getOverallDispersionRectangle().getLayoutBounds().getMaxX()};
                        double yValues[] = new double[]{dLeftAxis.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                                dRightAxis.getQueryDispersionRectangle().getLayoutBounds().getMaxY(),
                                dRightAxis.getQueryDispersionRectangle().getLayoutBounds().getMinY(),
                                dLeftAxis.getQueryDispersionRectangle().getLayoutBounds().getMinY()};

                        selectedCanvas.getGraphicsContext2D().setFill(queryFillColor);
                        selectedCanvas.getGraphicsContext2D().fillPolygon(xValues, yValues, xValues.length);

                        selectedCanvas.getGraphicsContext2D().setStroke(queryStrokeColor);
                        selectedCanvas.getGraphicsContext2D().strokeLine(dLeftAxis.getBarRightX(), dLeftAxis.getQueryTypicalLine().getEndY(),
                                dRightAxis.getBarLeftX(), dRightAxis.getQueryTypicalLine().getStartY());
                    }
                }
            } else if (leftAxis instanceof PCPCategoricalAxis && rightAxis instanceof PCPDoubleAxis) {

            } else if (leftAxis instanceof PCPDoubleAxis && rightAxis instanceof PCPCategoricalAxis) {

            } else if (leftAxis instanceof PCPCategoricalAxis && rightAxis instanceof PCPCategoricalAxis) {
                
            }
        }
    }
}
