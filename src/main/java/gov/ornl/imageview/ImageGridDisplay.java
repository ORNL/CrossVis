package gov.ornl.imageview;

import gov.ornl.datatable.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageGridDisplay implements DataTableListener {
    private static final double ELEMENT_SIZE = 100;
    private static final double GAP = ELEMENT_SIZE / 10;

    private TilePane tilePane = new TilePane();
    private Group display = new Group(tilePane);
    private DataTable dataTable;

    private double maxImageViewWidth = 500;
    private double minImageViewWidth = 20;
    private double maxImageViewHeight = 500;
    private double minImageViewHeight = 20;

    private DoubleProperty imageScale = new SimpleDoubleProperty(0.5);

    private ArrayList<ImageView> imageViewList = new ArrayList<>();
    private HashMap<Pair<File,Image>,ImageView> imageViewMap = new HashMap<>();

    public ImageGridDisplay() {
//        tilePane.setStyle("-fx-background-color: rgba(255, 215, 0, 0.1);");
        tilePane.setHgap(GAP);
        tilePane.setVgap(GAP);

        imageScale.addListener(observable -> {
            for (ImageView imageView : imageViewList) {
                imageView.setFitHeight(minImageViewHeight + (getImageScale() * (maxImageViewHeight - minImageViewHeight)));
                imageView.setFitWidth(minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth)));
            }
        });
    }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        this.dataTable.addDataTableListener(this);
        initView();
//        ImageColumn imageColumn = dataTable.getImageColumn();
//        if (imageColumn != null) {
//            setImages(imageColumn.getValuesAsList());
//        } else {
//            tilePane.getChildren().clear();
//            imageViewList.clear();
//        }
    }

    private void initView() {
        clearView();
        if (dataTable != null && !dataTable.isEmpty()) {
            ImageColumn imageColumn = dataTable.getImageColumn();
            if (imageColumn != null) {
                setImages(imageColumn.getValuesAsList());
            } else {
                tilePane.getChildren().clear();
                imageViewList.clear();
            }
        }
    }

    private void clearView() {
        tilePane.getChildren().clear();
        imageViewList.clear();
        imageViewMap.clear();
    }

    public Group getDisplay() {
        return display;
    }

    private void setImages(List<Pair<File,Image>> imagePairList) {
        clearView();

        boolean firstImage = true;
        for (Pair<File,Image> imagePair : imagePairList) {
            if (firstImage) {
                maxImageViewWidth = imagePair.getValue().getWidth();
                maxImageViewHeight = imagePair.getValue().getHeight();
            }
        }

        for (Pair<File,Image> imagePair : imagePairList) {
            ImageView imageView = new ImageView(imagePair.getValue());
            Tooltip tooltip = new Tooltip(imagePair.getKey().getName());
            Tooltip.install(imageView, tooltip);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(minImageViewHeight + (getImageScale() * (maxImageViewHeight - minImageViewHeight)));
            imageView.setFitWidth(minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth)));
//            imageView.setEffect(new DropShadow(10, Color.STEELBLUE));
//                ColorAdjust grayscale = new ColorAdjust();
//                grayscale.setSaturation(-1);
//                imageView.setEffect(grayscale);
            imageViewList.add(imageView);
            imageViewMap.put(imagePair, imageView);
            tilePane.getChildren().add(imageView);
        }
    }

    public double getImageScale() { return imageScale.get(); }

    public void setImageScale(double scale) { imageScale.set(scale); }

    public DoubleProperty imageScaleProperty() { return imageScale; }

    public int getImageCount() {
        return imageViewList.size();
    }

    public double getPrefTileWidth() {
        return tilePane.getPrefTileWidth();
    }

    public double getImageWidth() {
        return minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth));
    }

    public void setPrefColumns(int nCols) {
        tilePane.setPrefColumns(nCols);
    }

    private void handleQueryChanged() {
        Pair<File,Image> queriedImagePairs [] = dataTable.getImageColumn().getQueriedValues();
        Pair<File,Image> nonqueriedImagePairs [] = dataTable.getImageColumn().getNonqueriedValues();

        ArrayList<ImageView> queryImageViewList = new ArrayList<>();
        ArrayList<ImageView> nonqueryImageViewList = new ArrayList<>();

        tilePane.getChildren().clear();

        if (queriedImagePairs != null) {
            for (Pair<File,Image> queryImagePair : queriedImagePairs) {
//                ImageView imageView = getImageViewForImage(queryImage.);
                ImageView imageView = imageViewMap.get(queryImagePair);
                imageView.setEffect(new DropShadow(10, Color.STEELBLUE));
                queryImageViewList.add(imageView);
            }
        }

        if (nonqueriedImagePairs != null) {
            for (Pair<File,Image> nonqueryImagePair : nonqueriedImagePairs) {
//                ImageView imageView = getImageViewForImage(nonqueryImagePair);
                ImageView imageView = imageViewMap.get(nonqueryImagePair);
                imageView.setEffect(null);
                nonqueryImageViewList.add(imageView);
            }
        }

        tilePane.getChildren().addAll(queryImageViewList);
        tilePane.getChildren().addAll(nonqueryImageViewList);
    }

//    private ImageView getImageViewForImage(Image image) {
//        for (ImageView imageView : imageViewList) {
//            if (imageView.getImage() == image) {
//                return imageView;
//            }
//        }
//        return null;
//    }

    @Override
    public void dataTableReset(DataTable dataTable) {
        initView();
    }

    @Override
    public void dataTableStatisticsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableColumnExtentsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableColumnFocusExtentsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableNumHistogramBinsChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataTable) {
        handleQueryChanged();
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataTable, Column column) {
        handleQueryChanged();
    }

    @Override
    public void dataTableColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelectionRange) {
        handleQueryChanged();
    }

    @Override
    public void dataTableColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelectionRange) {
        handleQueryChanged();
    }

    @Override
    public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections) {
        handleQueryChanged();
    }

    @Override
    public void dataTableColumnSelectionChanged(DataTable dataTable, ColumnSelection columnSelectionRange) {
        handleQueryChanged();
    }

    @Override
    public void dataTableHighlightedColumnChanged(DataTable dataTable, Column oldHighlightedColumn, Column newHighlightedColumn) {

    }

    @Override
    public void dataTableTuplesAdded(DataTable dataTable, ArrayList<Tuple> newTuples) {
        initView();
    }

    @Override
    public void dataTableTuplesRemoved(DataTable dataTable, int numTuplesRemoved) {
        initView();
    }

    @Override
    public void dataTableColumnDisabled(DataTable dataTable, Column disabledColumn) {

    }

    @Override
    public void dataTableColumnsDisabled(DataTable dataTable, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataTableColumnEnabled(DataTable dataTable, Column enabledColumn) {

    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index) {

    }

    @Override
    public void dataTableColumnOrderChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableColumnNameChanged(DataTable dataTable, Column column) {

    }
}
