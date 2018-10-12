package gov.ornl.datatableview;

import gov.ornl.util.GraphicsUtil;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.logging.Logger;

public class Polyline {
    public static Logger log = Logger.getLogger(Polyline.class.getName());

    private DataTableView dataTableView;
    private Color color;
    private double[] xPoints;
    private double[] yPoints;
    private Object[] values;

    public Polyline(DataTableView dataTableView, Object[] values) {
        this.dataTableView = dataTableView;
        this.values = values;
    }

    public Color getColor() { return color; }

    public void setColor(Color c) { this.color = c; }

    public void layout() {
        xPoints = new double[dataTableView.getAxisCount()];
        yPoints = new double[dataTableView.getAxisCount()];

        for (int iaxis = 0; iaxis < dataTableView.getAxisCount(); iaxis++) {
            Axis axis = dataTableView.getAxis(iaxis);
            if (axis instanceof DoubleAxis) {
                DoubleAxis doubleAxis = (DoubleAxis)axis;
                double value = (double)values[iaxis];
                double yPosition = GraphicsUtil.mapValue(value, doubleAxis.doubleColumn().getStatistics().getMinValue(),
                        doubleAxis.doubleColumn().getStatistics().getMaxValue(), doubleAxis.getFocusMinPosition(),
                        doubleAxis.getFocusMaxPosition());
                xPoints[iaxis] = doubleAxis.getCenterX();
                yPoints[iaxis] = yPosition;
            } else if (axis instanceof BivariateAxis) {
            }
        }
    }

    public double[] getXPoints() { return xPoints; }

    public double[] getYPoints() { return yPoints; }
}
