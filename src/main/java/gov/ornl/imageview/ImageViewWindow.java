package gov.ornl.imageview;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

public class ImageViewWindow extends Application {
    private Logger log = Logger.getLogger(ImageViewWindow.class.getName());

    private Image image;
    private File file;
    private ImageView imageView;

    private double minImageViewHeight = 50;
    private double minImageViewWidth = 50;

    private Slider imageScaleSlider = new Slider(0, 1, 0.5);

    public ImageViewWindow() { }

    public ImageViewWindow(File file) throws FileNotFoundException {
        this.image = new Image(new FileInputStream(file));
        this.file = file;
    }

    public void setImage(File file) throws FileNotFoundException {
        this.image = new Image(new FileInputStream(file));
        this.file = file;
        imageView.setImage(image);
        scaleImageView();
    }

    private void scaleImageView() {
        double fitHeight = minImageViewHeight + (getImageScale() * (image.getHeight() - minImageViewHeight));
        double fitWidth = minImageViewWidth + (getImageScale() * (image.getWidth() - minImageViewWidth));

        fitHeight = fitHeight < minImageViewHeight ? minImageViewHeight : fitHeight;
        fitWidth = fitWidth < minImageViewWidth ? minImageViewWidth : fitWidth;

        log.info("scale: " + getImageScale() + " fitHeight = " + fitHeight + "fitWidth = " + fitWidth);

        imageView.setFitWidth(fitWidth);
        imageView.setFitHeight(fitHeight);
    }

    public double getImageScale() {
        return imageScaleSlider.getValue();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            ScrollPane scrollPane = new ScrollPane(imageView);

            imageScaleSlider = new Slider(0, 1, 0.5);
            imageScaleSlider.setPrefWidth(150);
            imageScaleSlider.setShowTickMarks(true);
            imageScaleSlider.valueProperty().addListener(observable -> {
                log.info("Slider scale: " + imageScaleSlider.getValue());
                scaleImageView();
            });

            Label sliderLabel = new Label("Image Scale:");

            ToolBar toolBar = new ToolBar();
            toolBar.setOrientation(Orientation.HORIZONTAL);
            toolBar.setPadding(new Insets(4.));
            toolBar.getItems().addAll(sliderLabel, imageScaleSlider);

            BorderPane mainPane = new BorderPane();
            mainPane.setCenter(scrollPane);
            mainPane.setTop(toolBar);

            Scene scene = new Scene(mainPane, 800, 800);
            primaryStage.setTitle("Image View Window");
            primaryStage.setScene(scene);
            primaryStage.show();

            if (file == null) {
                setImage(new File("/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG/3597_03A_CNN2 processed_pores.png"));
            } else {
                setImage(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
