package gov.ornl.datatableview;

import gov.ornl.datatable.ColumnSelection;
import gov.ornl.datatable.ImageColumn;
import gov.ornl.datatable.ImageColumnSelection;
import gov.ornl.util.GraphicsUtil;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

public class ImageAxis extends UnivariateAxis {
//    HashMap<Pair<File,Image>, Double> imageAxisPositions = new HashMap<>();
    private ArrayList<Pair<File,Image>> imagePairs = new ArrayList<>();

    private ImageAxisSelection draggingSelection;

    private Group imageTickLineGroup = new Group();
    private HashMap<Pair<File,Image>, Line> imagePairToTickLineMap = new HashMap<>();

    private ImageView hoverImageView;

    private boolean draggingToRemove = false;

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
            hoverImageView.setVisible(true);
        });

        getAxisBar().setOnMouseExited(event -> {
            hoverImageView.setVisible(false);
        });

        getAxisBar().setOnMouseMoved(event -> {
            Pair<File,Image> imagePair = (Pair<File,Image>)getValueForAxisPosition(event.getY());
            if (imagePair != null) {
                if (!hoverImageView.isVisible()) {
                    hoverImageView.setVisible(true);
                }
                hoverImageView.setImage(imagePair.getValue());
                hoverImageView.setX(getBounds().getMinX() - 2.);
                double hoverImageY = event.getY() - (hoverImageView.getLayoutBounds().getHeight() / 2.);
                if (hoverImageY < getMaxFocusPosition()) {
                    hoverImageY = hoverImageY + (getMaxFocusPosition() - hoverImageY);
                } else if ((hoverImageY + hoverImageView.getLayoutBounds().getHeight()) > getMinFocusPosition()) {
                    hoverImageY = hoverImageY - ((hoverImageY + hoverImageView.getLayoutBounds().getHeight()) - getMinFocusPosition());
                }
                hoverImageView.setY(hoverImageY);
            } else {
                hoverImageView.setVisible(false);
            }
        });

        getAxisBar().setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
            if (event.isMetaDown() || event.isControlDown()) {
                draggingToRemove = true;
            } else {
                draggingToRemove = false;
            }
        });

        getAxisBar().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
            double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

            selectionMaxY = selectionMaxY < getMaxFocusPosition() ? getMaxFocusPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getMinFocusPosition() ? getMinFocusPosition() : selectionMinY;

            int selectionMaxImagePairIndex = getImagePairIndexForAxisPosition(selectionMaxY);
            int selectionMinImagePairIndex = getImagePairIndexForAxisPosition(selectionMinY);
            HashSet<Pair<File, Image>> selectedImagePairs = new HashSet<>(imagePairs.subList(selectionMinImagePairIndex,
                    selectionMaxImagePairIndex));

            if (draggingSelection == null) {
                ImageColumnSelection columnSelection = new ImageColumnSelection(imageColumn(), selectedImagePairs);
                draggingSelection = new ImageAxisSelection(this, columnSelection, selectionMinY, selectionMaxY);
                axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
                axisSelectionGraphicsGroup.toFront();
            } else {
                draggingSelection.update(selectedImagePairs, selectionMinY, selectionMaxY);
            }
        });

        getAxisBar().setOnMouseReleased(event -> {
            if (draggingSelection != null) {
                axisSelectionGraphicsGroup.getChildren().remove(draggingSelection.getGraphicsGroup());

                if (draggingToRemove && getDataTable().getActiveQuery().hasColumnSelections()) {
                    List<ColumnSelection> columnSelections = getDataTable().getActiveQuery().getColumnSelections(imageColumn());
                    if (columnSelections != null && !columnSelections.isEmpty()) {
                        Set<Pair<File,Image>> imagePairsToRemove = ((ImageColumnSelection)draggingSelection.getColumnSelection()).getSelectedImagePairs();

                        for (ColumnSelection columnSelection : columnSelections) {
                            ImageColumnSelection imageColumnSelection = (ImageColumnSelection)columnSelection;
                            imageColumnSelection.removeImagePairs(imagePairsToRemove);
                        }
                    }
                    updateImageTickLines();
                } else {
                    getDataTable().addColumnSelectionToActiveQuery(draggingSelection.getColumnSelection());
                }

                dragging = false;
                draggingSelection = null;
            }
        });
    }

    private ImageColumn imageColumn() { return (ImageColumn)getColumn(); }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        int index = getImagePairIndexForAxisPosition(axisPosition);
        if (index != -1) {
            return imagePairs.get(index);
        }

        return null;
//        int index = (int)GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getMaxFocusPosition(),
//                0, imagePairs.size());
//        if (index >= 0 && index < imagePairs.size()) {
//            return imagePairs.get(index);
//        } else {
//            return null;
//        }
    }

    protected int getImagePairIndexForAxisPosition(double axisPosition) {
        int index = (int)GraphicsUtil.mapValue(axisPosition, getMinFocusPosition(), getMaxFocusPosition(),
                0, imagePairs.size());
        if (index >= 0 && index < imagePairs.size()) {
            return index;
        }

        return -1;
    }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        // see if an axis selection already exists for the given column selection
        if (getAxisSelectionList().contains(columnSelection)) {
            return null;
        }

        ImageColumnSelection imageColumnSelection = (ImageColumnSelection)columnSelection;

        ImageAxisSelection imageAxisSelection = new ImageAxisSelection(this, imageColumnSelection,
                getMinFocusPosition(), getMaxFocusPosition());

        getAxisSelectionList().add(imageAxisSelection);

        return imageAxisSelection;
    }

    protected double getAxisPositionForValue(Pair<File,Image> imagePair) {
        int pairIndex = imagePairs.indexOf(imagePair);
        if (pairIndex >= 0 && pairIndex < imagePairs.size()) {
            return GraphicsUtil.mapValue(pairIndex, 0, imagePairs.size(), getMinFocusPosition(), getMaxFocusPosition());
        }
        return Double.NaN;
    }

    @Override
    public void resize(double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        hoverImageView.setFitWidth((getBounds().getWidth() - getAxisBar().getWidth()) / 2.);

        imageTickLineGroup.getChildren().clear();
        imagePairToTickLineMap.clear();
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
            imagePairToTickLineMap.put(imagePairs.get(i), line);
//            imageTickLineToImagePairMap.put(line, imagePairs.get(i));
        }
    }

    private void updateImageTickLines() {
        if (getDataTable().getActiveQuery().hasColumnSelections()) {
            for (int i = 0; i < imagePairs.size(); i++) {
                if (getDataTable().getTuple(i).getQueryFlag()) {
                    imagePairToTickLineMap.get(imagePairs.get(i)).strokeProperty().bind(getDataTableView().selectedItemsColorProperty());
                } else {
                    imagePairToTickLineMap.get(imagePairs.get(i)).strokeProperty().bind(getDataTableView().unselectedItemsColorProperty());
                }
            }
        } else {
            for (Line line : imagePairToTickLineMap.values()) {
                line.strokeProperty().bind(getDataTableView().selectedItemsColorProperty());
            }
        }
//        Set<Pair<File,Image>> selectedImagePairs = imageColumn()
//        for (Map.Entry<Line, Pair<File,Image>> mapEntry : imageTickLineToImagePairMap.entrySet()) {
//            if (selectedImagePairs.contains(mapEntry.getValue())) {
//                mapEntry.getKey().strokeProperty().bind(getDataTableView().selectedItemsColorProperty());
//            } else {
//                mapEntry.getKey().strokeProperty().bind(getDataTableView().unselectedItemsColorProperty());
//            }
//        }
    }
}
