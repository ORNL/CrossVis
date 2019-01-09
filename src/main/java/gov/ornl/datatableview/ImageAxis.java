package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.ImageColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.image.Image;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;

public class ImageAxis extends UnivariateAxis {
    HashMap<Pair<File,Image>, Double> imageAxisPositions = new HashMap<>();

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
        return imageAxisPositions.get(imagePair);
//        return getCenterY();
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        imageAxisPositions.clear();

        Pair<File, Image> imagePairs[] = imageColumn().getValues();
        for (int i = 0; i < imagePairs.length; i++) {
            double axisPosition = GraphicsUtil.mapValue(i, 0, imagePairs.length,
                    getAxisBar().getLayoutBounds().getMinY(), getAxisBar().getLayoutBounds().getMaxY());
            imageAxisPositions.put(imagePairs[i], axisPosition);
        }
    }
}
