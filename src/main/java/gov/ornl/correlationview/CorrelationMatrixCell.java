package gov.ornl.correlationview;

import gov.ornl.datatable.Column;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CorrelationMatrixCell {
    private Column xColumn;
    private Column yColumn;
    private double correlation;
    Rectangle cellRectangle = new Rectangle();

    public CorrelationMatrixCell(Column xColumn, Column yColumn, double correlation) {
        this.xColumn = xColumn;
        this.yColumn = yColumn;
        this.correlation = correlation;

        cellRectangle.setStroke(Color.gray(0.4));
        cellRectangle.setOnMouseEntered(event -> {
            Tooltip.install(cellRectangle, new Tooltip("r (" + yColumn.getName() + ", " + xColumn.getName() + ") = " + String.valueOf(correlation)));
        });
        cellRectangle.setOnMouseExited(event -> {
            Tooltip.uninstall(cellRectangle, null);
        });
    }
    public Column getXColumn() { return xColumn; }

    public Column getYColumn() { return yColumn; }

    public double getCorrelation() { return correlation; }

    public void setCorrelation(double correlation) {
        this.correlation = correlation;
    }

    private void setRectangleFillColor() {
        cellRectangle.setFill(Color.STEELBLUE);
    }

    public Rectangle getCellRectangle() { return cellRectangle; }
}
