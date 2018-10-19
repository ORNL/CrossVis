package gov.ornl.datatableview;

import gov.ornl.datatable.BivariateColumn;
import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelection;
import gov.ornl.scatterplot.Scatterplot;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BivariateAxis extends Axis {

    private BivariateColumn bivariateColumn;

    private Scatterplot scatterplot;
//    private Rectangle scatterplotRectangle;

    public BivariateAxis(DataTableView dataTableView, BivariateColumn bivariateColumn) {
        super(dataTableView, bivariateColumn);

        this.bivariateColumn = bivariateColumn;

        scatterplot = new Scatterplot(bivariateColumn.getColumn1(), bivariateColumn.getColumn2());

//        scatterplotRectangle = new Rectangle();
//        scatterplotRectangle.setStroke(Color.BLUE);
//        scatterplotRectangle.setMouseTransparent(true);
//        scatterplotRectangle.setFill(Color.TRANSPARENT);

        getGraphicsGroup().getChildren().addAll(scatterplot.getGraphicsGroup());
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        return null;
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        double axisTop = getTitleText().getLayoutBounds().getMaxY() + 4;

        double scatterplotSize = width < (height - axisTop) ? width : (height - axisTop);
        double scatterplotLeft = left + ((width - scatterplotSize) / 2.);
        double scatterplotTop = axisTop + ((height - scatterplotSize) / 2.);

//        scatterplotRectangle.setX(scatterplotLeft);
//        scatterplotRectangle.setY(scatterplotTop);
//        scatterplotRectangle.setWidth(scatterplotSize);
//        scatterplotRectangle.setHeight(scatterplotSize);

        scatterplot.resize(scatterplotLeft, scatterplotTop, scatterplotSize, scatterplotSize);
    }

    public Rectangle getScatterplotRectangle() {
        return scatterplot.getPlotRectangle();
    }
}
