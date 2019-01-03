package gov.ornl.datatableview;

import gov.ornl.datatable.BivariateColumn;
import gov.ornl.datatable.Column;
import gov.ornl.datatable.ColumnSelection;
import gov.ornl.scatterplot.Scatterplot;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BivariateAxis extends Axis {

//    private BivariateColumn bivariateColumn;

    private Scatterplot scatterplot;
//    private Rectangle scatterplotRectangle;
//    private Bounds scatterplotBounds;

    public BivariateAxis(DataTableView dataTableView, BivariateColumn bivariateColumn) {
        super(dataTableView, bivariateColumn);

//        this.bivariateColumn = bivariateColumn;

        scatterplot = new Scatterplot(bivariateColumn.getColumn1(), bivariateColumn.getColumn2(), dataTableView.getSelectedItemsColor(),
                dataTableView.getUnselectedItemsColor(), dataTableView.getDataItemsOpacity());
        scatterplot.setShowXAxisMarginValues(true);
        scatterplot.setShowYAxisMarginValues(false);

//        scatterplotRectangle = new Rectangle();
//        scatterplotRectangle.setStroke(Color.BLUE);
//        scatterplotRectangle.setMouseTransparent(true);
//        scatterplotRectangle.setFill(Color.TRANSPARENT);

        getGraphicsGroup().getChildren().addAll(scatterplot.getGraphicsGroup());
    }

    public void swapColumnAxes() {
        bivariateColumn().swapColumns();
        scatterplot.swapColumnAxes();
        getDataTableView().resizeView();
    }

    public BivariateColumn bivariateColumn() { return (BivariateColumn)getColumn(); }

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

    public Scatterplot getScatterplot() { return scatterplot; }
}
