package gov.ornl.imageview;

import gov.ornl.datatable.*;
import gov.ornl.datatableview.DataTableView;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class ImageGridDisplay implements DataTableListener {
    private static final Logger log = Logger.getLogger(ImageGridDisplay.class.getName());

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
    private ObjectProperty<Color> selectedImagesColor = new SimpleObjectProperty<>(DataTableView.DEFAULT_SELECTED_ITEMS_COLOR);
    private ObjectProperty<Color> unselectedImagesColor = new SimpleObjectProperty<>(DataTableView.DEFAULT_UNSELECTED_ITEMS_COLOR);

    private BooleanProperty showSelectedImages = new SimpleBooleanProperty(true);
    private BooleanProperty showUnselectedImages = new SimpleBooleanProperty(true);

    private ArrayList<ImageView> imageViewList = new ArrayList<>();
    private HashMap<Pair<File,Image>,ImageView> pairToImageViewMap = new HashMap<>();
    private HashMap<ImageView, Pair<File,Image>> imageViewToPairMap = new HashMap<>();

    private DropShadow selectedDropShadow;
    private DropShadow unselectedDropShadow;

    private HashSet<Stage> imageViewWindowStageSet = new HashSet<>();

    public ImageGridDisplay() {
        selectedDropShadow = new DropShadow(10, getSelectedImagesColor());
        selectedDropShadow.colorProperty().bind(selectedImagesColorProperty());
        unselectedDropShadow = new DropShadow(10, getUnselectedImagesColor());
        unselectedDropShadow.colorProperty().bind(unselectedImagesColorProperty());

//        tilePane.setStyle("-fx-background-color: rgba(255, 215, 0, 0.1);");
        tilePane.setHgap(GAP);
        tilePane.setVgap(GAP);

        imageScale.addListener(observable -> {
            for (ImageView imageView : imageViewList) {
                imageView.setFitHeight(minImageViewHeight + (getImageScale() * (maxImageViewHeight - minImageViewHeight)));
                imageView.setFitWidth(minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth)));
            }
        });

        registerListeners();
    }

    public void closeChildrenImageWindows() {
        if (!imageViewWindowStageSet.isEmpty()) {
            for (Stage imageViewWindowStage : imageViewWindowStageSet) {
                imageViewWindowStage.close();
            }
        }
    }

    public void setShowSelectedImages(boolean show) {showSelectedImages.set(show);}

    public boolean isShowingSelectedImages() { return showSelectedImages.get(); }

    public BooleanProperty showSelectedImagesProperty() { return showSelectedImages; }

    public void setShowUnselectedImages(boolean show) {
        showUnselectedImages.set(show); }

    public boolean isShowingUnselectedImages() { return showUnselectedImages.get(); }

    public BooleanProperty showUnselectedImagesProperty() { return showUnselectedImages; }

    public void setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        this.dataTable.addDataTableListener(this);
        initView();
    }

    private void registerListeners() {
        showSelectedImages.addListener(observable -> {
            initView();
        });

        showUnselectedImages.addListener(observable -> {
            initView();
        });
    }

    public Color getSelectedImagesColor() { return selectedImagesColor.get(); }

    public void setSelectedImagesColor(Color color) { selectedImagesColor.set(color); }

    public ObjectProperty<Color> selectedImagesColorProperty() { return selectedImagesColor; }

    public Color getUnselectedImagesColor() { return unselectedImagesColor.get(); }

    public void setUnselectedImagesColor(Color color) { unselectedImagesColor.set(color); }

    public ObjectProperty<Color> unselectedImagesColorProperty() { return unselectedImagesColor; }

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
        pairToImageViewMap.clear();
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
            imageView.setOnMouseClicked(event -> {
                try {
                    ImageViewWindow imageViewWindow = new ImageViewWindow(imageViewToPairMap.get(imageView).getKey());
                    Stage imageViewWindowStage = new Stage();
                    imageViewWindow.start(imageViewWindowStage);
                    imageViewWindowStageSet.add(imageViewWindowStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            imageViewList.add(imageView);
            pairToImageViewMap.put(imagePair, imageView);
            imageViewToPairMap.put(imageView, imagePair);
        }

        setQueriedImageViews();
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

    private void setQueriedImageViews() {
        Pair<File,Image> queriedImagePairs [] = dataTable.getImageColumn().getQueriedValues();
        Pair<File,Image> nonqueriedImagePairs [] = dataTable.getImageColumn().getNonqueriedValues();

        ArrayList<ImageView> queryImageViewList = new ArrayList<>();
        ArrayList<ImageView> nonqueryImageViewList = new ArrayList<>();

        tilePane.getChildren().clear();

        if (isShowingSelectedImages() && queriedImagePairs != null) {
            for (Pair<File,Image> queryImagePair : queriedImagePairs) {
//                ImageView imageView = getImageViewForImage(queryImage.);
                ImageView imageView = pairToImageViewMap.get(queryImagePair);
                imageView.setEffect(selectedDropShadow);
//                dropShadow.colorProperty().bind(selectedImagesColor);
//                imageView.setEffect(dropShadow);
                queryImageViewList.add(imageView);
            }
            tilePane.getChildren().addAll(queryImageViewList);
        }

        if (isShowingUnselectedImages() && nonqueriedImagePairs != null) {
            for (Pair<File,Image> nonqueryImagePair : nonqueriedImagePairs) {
//                ImageView imageView = getImageViewForImage(nonqueryImagePair);
                ImageView imageView = pairToImageViewMap.get(nonqueryImagePair);
                imageView.setEffect(unselectedDropShadow);
//                imageView.setEffect(null);
                nonqueryImageViewList.add(imageView);
            }
            tilePane.getChildren().addAll(nonqueryImageViewList);
        }
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
        setQueriedImageViews();
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataTable, Column column) {
        setQueriedImageViews();
    }

    @Override
    public void dataTableColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelectionRange) {
        setQueriedImageViews();
    }

    @Override
    public void dataTableColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelectionRange) {
        setQueriedImageViews();
    }

    @Override
    public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections) {
        setQueriedImageViews();
    }

    @Override
    public void dataTableColumnSelectionChanged(DataTable dataTable, ColumnSelection columnSelectionRange) {
        setQueriedImageViews();
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
