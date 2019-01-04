package gov.ornl.imageview;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.TilePaneBuilder;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


public class LoadImage extends Application {

    // a TilePane for image thumbnails
    final TilePane tilePane = TilePaneBuilder.create()
            .padding(new Insets(20, 10, 10, 10))
            .hgap(4)
            .vgap(4)
            .build();

    @Override
    public void start(final Stage primaryStage) {

        primaryStage.setTitle("TilePane with ImageView");

        //create a border pane  - center with TilePane for thubmnails and bottom for Button
        BorderPane borderPane = BorderPaneBuilder.create()
                .prefHeight(400)
                .prefWidth(600)
                .build();

        //create Button for load location with images
        Button loadButton = new Button("Load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //create a task to load images
                Thread loadImageThread = new Thread(task);
                loadImageThread.setDaemon(true);
                loadImageThread.start();
            }
        });

        borderPane.setCenter(tilePane);
        borderPane.setBottom(loadButton);
        Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    Task<Void> task = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File selectedDirectory = directoryChooser.showDialog(null);
            //there is an ImageFileFilter class below
            File[] imageFiles = selectedDirectory.listFiles(new ImageFileFilter());

            tilePane.getChildren().clear();

            for (File file : imageFiles) {
                try {
                    ImageView imageView = ImageViewBuilder.create()
                            .preserveRatio(true)
                            .fitHeight(128)
                            .fitWidth(128)
                            .image(new Image(new FileInputStream(file)))
                            .build();

                    //add imageView to the TilePane
                    tilePane.getChildren().add(imageView);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    };

    public static void main(String[] args) {
        launch(args);
    }


    private class ImageFileFilter implements FileFilter {

        private final String[] validFileExtension = new String[] {"jpg", "jpeg", "png", "gif"};
        @Override
        public boolean accept(File pathname) {
            for (String extension : validFileExtension) {
                if (pathname.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
