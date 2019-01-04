package gov.ornl.imageview;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;


public class ImageSetViewTest extends Application {
    private ImageSetView imageSetView;
    private FlowPane imagePane;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        imageSetView = new ImageSetView();
//        imageSetView.setPrefHeight(500);
//        imageSetView.setPadding(new Insets(10));
//        imageSetView.setOrientation(Orientation.HORIZONTAL);

//        ScrollPane scrollPane = new ScrollPane(imageSetView);
//        scrollPane.setFitToHeight(true);

        imagePane = new FlowPane();
        imagePane.widthProperty().addListener(observable -> {
            imagePane.setPrefWrapLength(imagePane.getWidth());
        });
        ScrollPane scrollPane = new ScrollPane(imagePane);

        Button loadDataButton = new Button("Load Data");
        loadDataButton.setOnAction(event -> {
            ArrayList<Image> imageList = new ArrayList<>();
            File imageDirectory = new File("/Users/csg/Dropbox (ORNL)/data/CNMS_SEM_images/AllDiatomImagesPNG");
            File imageFiles[] = imageDirectory.listFiles();
            for (File imageFile : imageFiles) {
                try {
                    Image image = new Image(new FileInputStream(imageFile));
                    imageList.add(image);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    imagePane.getChildren().add(imageView);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(2);
        buttonBox.setPadding(new Insets(4));
        buttonBox.getChildren().addAll(loadDataButton);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);
//        rootNode.setCenter(imageSetView);
        rootNode.setBottom(buttonBox);

        Scene scene = new Scene(rootNode, 800, 800, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("ImageSetView Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
