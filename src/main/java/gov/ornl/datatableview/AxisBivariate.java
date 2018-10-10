package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.scatterplot.Scatterplot;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class AxisBivariate extends Axis {

    private Column xColumn;
    private Column yColumn;

    private Scatterplot scatterplot;
    private Rectangle scatterplotRectangle;

    public AxisBivariate(DataTableView dataTableView, Column xColumn, Column yColumn) {
        super(dataTableView, yColumn.getName() + " vs. " + xColumn.getName());

        this.xColumn = xColumn;
        this.yColumn = yColumn;

        scatterplot = new Scatterplot(xColumn, yColumn);

        scatterplotRectangle = new Rectangle();
        scatterplotRectangle.setStroke(Color.BLUE);
        scatterplotRectangle.setMouseTransparent(true);
        scatterplotRectangle.setFill(Color.TRANSPARENT);

        getGraphicsGroup().getChildren().addAll(scatterplot.getGraphicsGroup(), scatterplotRectangle);
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        double axisTop = getTitleText().getLayoutBounds().getMaxY() + 4;

        double scatterplotSize = width < (height - axisTop) ? width : (height - axisTop);
        double scatterplotLeft = left + ((width - scatterplotSize) / 2.);
        double scatterplotTop = axisTop + ((height - scatterplotSize) / 2.);

        scatterplotRectangle.setX(scatterplotLeft);
        scatterplotRectangle.setY(scatterplotTop);
        scatterplotRectangle.setWidth(scatterplotSize);
        scatterplotRectangle.setHeight(scatterplotSize);

        scatterplot.resize(scatterplotLeft, scatterplotTop, scatterplotSize, scatterplotSize);
    }
}
