package gov.ornl.imageview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ImageGridWindow extends Application {
    private final static Logger log = Logger.getLogger(ImageGridWindow.class.getName());

    private DataTable dataTable;
    private ImageGridDisplay imageGridDisplay;

    public ImageGridWindow() { }

    public ImageGridWindow(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public ObjectProperty<Color> selectedImagesColorProperty() {
        return imageGridDisplay.selectedImagesColorProperty();
    }

    public ObjectProperty<Color> unselectedImagesColorProperty() {
        return imageGridDisplay.unselectedImagesColorProperty();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void stop() {
        imageGridDisplay.closeChildrenImageWindows();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnHiding(event -> {
            imageGridDisplay.closeChildrenImageWindows();
        });

        primaryStage.setOnCloseRequest(event -> {
            imageGridDisplay.closeChildrenImageWindows();
        });

        imageGridDisplay = new ImageGridDisplay();
        ScrollPane scrollPane = new ScrollPane(imageGridDisplay.getDisplay());
        scrollPane.viewportBoundsProperty().addListener(observable -> {
            double viewPortWidth = scrollPane.getViewportBounds().getWidth();
            int preferredColumns = Math.min(imageGridDisplay.getImageCount(),
                    Math.max(1, (int)(viewPortWidth / imageGridDisplay.getImageWidth())));
            imageGridDisplay.setPrefColumns(preferredColumns);
        });

        Slider imageScaleSlider = new Slider(0, 1, 0.5);
        imageScaleSlider.setPrefWidth(150);
        imageScaleSlider.setShowTickMarks(true);
        imageScaleSlider.valueProperty().bindBidirectional(imageGridDisplay.imageScaleProperty());

        Label sliderLabel = new Label("Image Scale:");

        CheckBox showSelectedImagesCheckBox = new CheckBox("Show Selected");
        showSelectedImagesCheckBox.selectedProperty().bindBidirectional(imageGridDisplay.showSelectedImagesProperty());

        CheckBox showUnselectedImagesCheckBox = new CheckBox("Show Unselected");
        showUnselectedImagesCheckBox.selectedProperty().bindBidirectional(imageGridDisplay.showUnselectedImagesProperty());

        ToolBar settingsToolBar = new ToolBar();
        settingsToolBar.setOrientation(Orientation.HORIZONTAL);
        settingsToolBar.setPadding(new Insets(4.));
        settingsToolBar.getItems().addAll(showSelectedImagesCheckBox, showUnselectedImagesCheckBox, new Separator(),
                sliderLabel, imageScaleSlider);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(scrollPane);
        mainPane.setTop(settingsToolBar);

        Scene scene = new Scene(mainPane, 800, 1200);
        primaryStage.setTitle("Image Grid View Window");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (dataTable == null) {
            dataTable = new DataTable();
            ArrayList<String> categoricalColumnNames = new ArrayList<>();
            categoricalColumnNames.add("Type");
            String imageColumnName = "Image Filename";
            String imageDirectoryPath = "/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG";
            String csvFilePath = "/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/DiatomsParameters.csv";
            try {
                IOUtilities.readCSV(new File(csvFilePath), null, categoricalColumnNames,
                        null, imageColumnName, imageDirectoryPath, null,
                        dataTable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (dataTable != null) {
            imageGridDisplay.setDataTable(dataTable);
        }
    }
}
