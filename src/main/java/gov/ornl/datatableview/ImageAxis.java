package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.ImageColumn;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.File;

public class ImageAxis extends UnivariateAxis {

    public ImageAxis(DataTableView dataTableView, ImageColumn column) {
        super(dataTableView, column);

        getUpperContextBar().setVisible(false);
        getLowerContextBar().setVisible(false);
        getUpperContextBarHandle().setVisible(false);
        getLowerContextBarHandle().setVisible(false);

//        registerListeners();
    }

    private ImageColumn imageColumn() { return (ImageColumn)getColumn(); }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        return null;
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        return null;
    }

    protected double getAxisPositionForValue(Image image) {
        return getCenterY();
    }

    protected double getAxisPositionForValue(Pair<File,Image> imagePair) {
        return getCenterY();
    }
}
