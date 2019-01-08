package gov.ornl.imageview;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// java 8 code
public class DynamicTiles extends Application {
    private final static Logger log = Logger.getLogger(DynamicTiles.class.getName());

    //Class containing grid (see below)
    private GridDisplay gridDisplay;

    //Class responsible for displaying the grid containing the Rectangles
    public class GridDisplay {
        private static final double ELEMENT_SIZE = 100;
        private static final double GAP = ELEMENT_SIZE / 10;

        private TilePane tilePane = new TilePane();
        private Group display = new Group(tilePane);
        private int nRows;
        private int nCols;

        private int maxImageViewWidth = 500;
        private int minImageViewWidth = 20;
        private int maxImageViewHeight = 500;
        private int minImageViewHeight = 20;

        private DoubleProperty imageScale = new SimpleDoubleProperty(0.5);

        private ArrayList<ImageView> imageViewList = new ArrayList<>();

        public GridDisplay(int nCols) {
            tilePane.setStyle("-fx-background-color: rgba(255, 215, 0, 0.1);");
            tilePane.setHgap(GAP);
            tilePane.setVgap(GAP);

            imageScale.addListener(observable -> {
                for (ImageView imageView : imageViewList) {
                    imageView.setFitHeight(minImageViewHeight + (getImageScale() * (maxImageViewHeight - minImageViewHeight)));
                    imageView.setFitWidth(minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth)));
                }
            });
//            setColumns(nCols);
//            setRows(nRows);
//            createElements();
        }

        public void setColumns(int newColumns) {
            nCols = newColumns;
//            tilePane.setPrefColumns(nCols);
//            createElements();
        }

        public void setRows(int newRows) {
            nRows = newRows;
//            tilePane.setPrefRows(nRows);
//            createElements();
        }

        public Group getDisplay() {
            return display;
        }

        public void setImages(List<Image> imageList) {
            tilePane.getChildren().clear();
            imageViewList.clear();

            for (Image image : imageList) {
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(minImageViewHeight + (getImageScale() * (maxImageViewHeight - minImageViewHeight)));
                imageView.setFitWidth(minImageViewWidth + (getImageScale() * (maxImageViewWidth - minImageViewWidth)));
                imageView.setEffect(new DropShadow(10, Color.STEELBLUE));
//                ColorAdjust grayscale = new ColorAdjust();
//                grayscale.setSaturation(-1);
//                imageView.setEffect(grayscale);
                imageViewList.add(imageView);
                tilePane.getChildren().add(imageView);
            }
        }

        public double getImageScale() { return imageScale.get(); }

        public void setImageScale(double scale) { imageScale.set(scale); }

        public DoubleProperty imageScaleProperty() { return imageScale; }
//        public void setImageScale(double scale) {
//            for (ImageView imageView : imageViewList) {
//                imageView.setFitHeight(scale * imageView.getFitHeight());
//                imageView.setFitWidth(scale * imageView.getFitWidth());
//            }
//        }

        private void createElements() {
            tilePane.getChildren().clear();
            for (int i = 0; i < nCols; i++) {
                for (int j = 0; j < nRows; j++) {
                    tilePane.getChildren().add(createElement());
                }
            }
        }

        private Rectangle createElement() {
            Rectangle rectangle = new Rectangle(ELEMENT_SIZE, ELEMENT_SIZE);
            rectangle.setStroke(Color.ORANGE);
            rectangle.setFill(Color.STEELBLUE);

            return rectangle;
        }

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
            this.nCols = nCols;
            tilePane.setPrefColumns(nCols);
        }
    }

    @Override
    public void start(Stage primaryStage) {

        //Represents the grid with Rectangles
        gridDisplay = new GridDisplay(4);

        ScrollPane gridDisplayScrollPane = new ScrollPane(gridDisplay.getDisplay());
//        gridDisplayScrollPane.setFitToWidth(true);

        gridDisplayScrollPane.viewportBoundsProperty().addListener(observable -> {
            double viewPortWidth = gridDisplayScrollPane.getViewportBounds().getWidth();
            log.info("scrollpane viewpoint bounds width is now " + gridDisplayScrollPane.getViewportBounds().getWidth());
            int prefColumns = Math.min(gridDisplay.getImageCount(),
                    Math.max(1, (int) (viewPortWidth / gridDisplay.getImageWidth())));
            log.info("prefColumns is " + prefColumns);
            gridDisplay.setPrefColumns(prefColumns);
        });
//        gridDisplayScrollPane.setFitToWidth(true);
//        //Fields to specify number of rows/columns
//        TextField rowField = new TextField("2");
//        TextField columnField = new TextField("4");
//
//        //Function to set an action when text field loses focus
//        buildTextFieldActions(rowField, columnField);
//
//        HBox fields = new HBox(10);
//        fields.getChildren().add(rowField);
//        fields.getChildren().add(new Label("x"));
//        fields.getChildren().add(columnField);

        Button loadImagesButton = new Button("Load Images");
        loadImagesButton.setOnAction(event -> {
            ArrayList<Image> imageList = new ArrayList<>();
            File imageDirectory = new File("/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG");
            File imageFiles[] = imageDirectory.listFiles();
            for (File imageFile : imageFiles) {
                try {
                    Image image = new Image(new FileInputStream(imageFile));
                    imageList.add(image);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            gridDisplay.setImages(imageList);
        });

        Slider imageSizeSlider = new Slider(0, 1, 0.2);
        imageSizeSlider.valueProperty().bindBidirectional(gridDisplay.imageScaleProperty());
//        imageSizeSlider.valueProperty().addListener(observable -> {
//            gridDisplay.setImageScale(imageSizeSlider.getValue());
//        });

        HBox settingsBox = new HBox();
        settingsBox.setSpacing(2);
        settingsBox.setPadding(new Insets(4.));
        settingsBox.getChildren().addAll(loadImagesButton, imageSizeSlider);

        BorderPane mainPanel = new BorderPane();
        mainPanel.setCenter(gridDisplayScrollPane);
        mainPanel.setTop(settingsBox);
//        mainPanel.setTop(fields);

        Scene scene = new Scene(mainPanel, 1000, 800);
        primaryStage.setTitle("Test grid display");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void buildTextFieldActions(final TextField rowField, final TextField columnField) {
        rowField.focusedProperty().addListener((ov, t, t1) -> {
            if (!t1) {
                if (!rowField.getText().equals("")) {
                    try {
                        int nbRow = Integer.parseInt(rowField.getText());
                        gridDisplay.setRows(nbRow);
                    } catch (NumberFormatException nfe) {
                        System.out.println("Please enter a valid number.");
                    }
                }
            }
        });

        columnField.focusedProperty().addListener((ov, t, t1) -> {
            if (!t1) {
                if (!columnField.getText().equals("")) {
                    try {
                        int nbColumn = Integer.parseInt(columnField.getText());
                        gridDisplay.setColumns(nbColumn);
                    } catch (NumberFormatException nfe) {
                        System.out.println("Please enter a valid number.");
                    }
                }
            }
        });
    }
}
