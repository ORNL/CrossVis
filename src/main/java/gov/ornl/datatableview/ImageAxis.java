package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.ImageColumn;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageAxis extends UnivariateAxis {
//    HashMap<Pair<File,Image>, Double> imageAxisPositions = new HashMap<>();
    private ArrayList<Pair<File,Image>> imagePairs = new ArrayList<>();

    private Group imageTickLineGroup = new Group();

    private ImageView hoverImageView;

    public ImageAxis(DataTableView dataTableView, ImageColumn column) {
        super(dataTableView, column);

        imagePairs.addAll(imageColumn().getValuesAsList());

        hoverImageView = new ImageView();
        hoverImageView.setPreserveRatio(true);
        hoverImageView.setSmooth(true);
        hoverImageView.setVisible(false);
        hoverImageView.setCache(true);
        hoverImageView.setMouseTransparent(true);

        getUpperContextBar().setVisible(false);
        getLowerContextBar().setVisible(false);
        getUpperContextBarHandle().setVisible(false);
        getLowerContextBarHandle().setVisible(false);

        getGraphicsGroup().getChildren().addAll(imageTickLineGroup, hoverImageView);

        registerListeners();
    }

    private void registerListeners() {
        getAxisBar().setOnMouseEntered(event -> {
//            log.info("Mouse entered");
            hoverImageView.setVisible(true);
//            hoverImageView.setImage(imagePairs.get(0).getValue());
//            hoverValueText.setVisible(true);
//            hoverValueText.toFront();
        });

        getAxisBar().setOnMouseExited(event -> {
//            log.info("mouse exited");
            hoverImageView.setVisible(false);
//            hoverValueText.setVisible(false);
        });

        getAxisBar().setOnMouseMoved(event -> {
//            log.info("mouse moved");
//            hoverImageView.setX(getBounds().getMinX());
//            hoverImageView.setY(event.getY());

            Pair<File,Image> imagePair = (Pair<File,Image>)getValueForAxisPosition(event.getY());
            if (imagePair != null) {
                if (!hoverImageView.isVisible()) {
                    hoverImageView.setVisible(true);
                }
                hoverImageView.setImage(imagePair.getValue());
//                hoverImageView.setPreserveRatio(true);
//                hoverImageView.setFitWidth(getBounds().getWidth());
//                hoverImageView.setX(getCenterX() - (hoverImageView.getLayoutBounds().getWidth() / 2.));
                hoverImageView.setX(getBounds().getMinX() - 2.);
//                hoverImageView.setY(event.getY());
                double hoverImageY = event.getY() - (hoverImageView.getLayoutBounds().getHeight() / 2.);
                if (hoverImageY < getMaxFocusPosition()) {
                    hoverImageY = hoverImageY + (getMaxFocusPosition() - hoverImageY);
                } else if ((hoverImageY + hoverImageView.getLayoutBounds().getHeight()) > getMinFocusPosition()) {
                    hoverImageY = hoverImageY - ((hoverImageY + hoverImageView.getLayoutBounds().getHeight()) - getMinFocusPosition());
                }
                hoverImageView.setY(hoverImageY);
            } else {
                log.info("got null image pair");
                hoverImageView.setVisible(false);
            }

//            Object value = getValueForAxisPosition(event.getY());
//            if (value != null) {
//                hoverValueText.setText(getValueForAxisPosition(event.getY()).toString());
//                hoverValueText.setY(event.getY());
//                hoverValueText.setX(getCenterX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
//            } else {
//                hoverValueText.setText("");
//            }
//            hoverValueText.toFront();
        });
    }

    private ImageColumn imageColumn() { return (ImageColumn)getColumn(); }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        int index = (int)GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getMaxFocusPosition(),
                0, imagePairs.size());
        if (index >= 0 && index < imagePairs.size()) {
            return imagePairs.get(index);
        } else {
            return null;
        }
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        return null;
    }

//    protected double getAxisPositionForValue(Image image) {
//        return getCenterY();
//    }

    protected double getAxisPositionForValue(Pair<File,Image> imagePair) {
        int pairIndex = imagePairs.indexOf(imagePair);
        if (pairIndex >= 0 && pairIndex < imagePairs.size()) {
            return GraphicsUtil.mapValue(pairIndex, 0, imagePairs.size(), getMinFocusPosition(), getMaxFocusPosition());
        }
        return Double.NaN;
//        return GraphicsUtil.mapValue()
//        return imageAxisPositions.get(imagePair);
//        return getCenterY();
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        log.info("resize");
        super.resize(left, top, width, height);

        hoverImageView.setFitWidth((getBounds().getWidth() - getAxisBar().getWidth()) / 2.);

        imageTickLineGroup.getChildren().clear();
        for (int i = 0; i < imagePairs.size(); i++) {
            double y = getAxisPositionForValue(imagePairs.get(i));
            Line line = new Line(getAxisBar().getLayoutBounds().getMinX()+ 2., y, getAxisBar().getLayoutBounds().getMaxX() - 2., y);
            if (getDataTable().getActiveQuery().hasColumnSelections()) {
                if (getDataTable().getTuple(i).getQueryFlag()) {
                    line.strokeProperty().bind(getDataTableView().selectedItemsColorProperty());
                } else {
                    line.strokeProperty().bind(getDataTableView().unselectedItemsColorProperty());
                }
            } else {
                line.strokeProperty().bind(getDataTableView().selectedItemsColorProperty());
            }
            line.setStrokeWidth(1.);
            line.setMouseTransparent(true);
            imageTickLineGroup.getChildren().add(line);
        }
//        imageAxisPositions.clear();
//
//        Pair<File, Image> imagePairs[] = imageColumn().getValues();
//        for (int i = 0; i < imagePairs.length; i++) {
//            double axisPosition = GraphicsUtil.mapValue(i, 0, imagePairs.length,
//                    getAxisBar().getLayoutBounds().getMinY(), getAxisBar().getLayoutBounds().getMaxY());
//            imageAxisPositions.put(imagePairs[i], axisPosition);
//        }
    }
}
