package gov.ornl.imageview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ImageGridDisplayTest extends Application {
    private final static Logger log = Logger.getLogger(ImageGridDisplayTest.class.getName());

    private ImageGridDisplay imageGridDisplay = new ImageGridDisplay();

    @Override
    public void start(Stage primaryStage) throws Exception {
        ScrollPane imageGridDisplayScrollPane = new ScrollPane(imageGridDisplay.getDisplay());

        imageGridDisplayScrollPane.viewportBoundsProperty().addListener(observable -> {
            double viewPortWidth = imageGridDisplayScrollPane.getViewportBounds().getWidth();
            log.info("scrollpane viewpoint bounds width is now " + imageGridDisplayScrollPane.getViewportBounds().getWidth());
            int prefColumns = Math.min(imageGridDisplay.getImageCount(),
                    Math.max(1, (int) (viewPortWidth / imageGridDisplay.getImageWidth())));
            log.info("prefColumns is " + prefColumns);
            imageGridDisplay.setPrefColumns(prefColumns);
        });

        Button loadImagesButton = new Button("Load Images");
        loadImagesButton.setOnAction(event -> {
            DataTable dataTable = new DataTable();
            ArrayList<String> categoricalColumnNames = new ArrayList<>();
            categoricalColumnNames.add("Type");
            String imageColumnName = "Image Filename";
            String imageDirectoryPath = "/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG";
            String csvFilePath = "/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/DiatomsParameters.csv";
            try {
                IOUtilities.readCSV(new File(csvFilePath), null, categoricalColumnNames,
                        null, imageColumnName, imageDirectoryPath, null,
                        dataTable);
                imageGridDisplay.setDataTable(dataTable);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            ArrayList<Image> imageList = new ArrayList<>();
//            File imageDirectory = new File("/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG");
//            File imageFiles[] = imageDirectory.listFiles();
//            for (File imageFile : imageFiles) {
//                try {
//                    Image image = new Image(new FileInputStream(imageFile));
//                    imageList.add(image);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//            imageGridDisplay.setImages(imageList);
        });

        Slider imageSizeSlider = new Slider(0, 1, 0.2);
        imageSizeSlider.valueProperty().bindBidirectional(imageGridDisplay.imageScaleProperty());

        HBox settingsBox = new HBox();
        settingsBox.setSpacing(2);
        settingsBox.setPadding(new Insets(4.));
        settingsBox.getChildren().addAll(loadImagesButton, imageSizeSlider);

        BorderPane mainPanel = new BorderPane();
        mainPanel.setCenter(imageGridDisplayScrollPane);
        mainPanel.setTop(settingsBox);

        Scene scene = new Scene(mainPanel, 1000, 800);
        primaryStage.setTitle("ImageGridDisplayTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
