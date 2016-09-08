package gov.ornl.csed.cda.Talon;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

/**
 * Created by whw on 9/1/16.
 */
public class ImageZoomPanelTester extends Application {

    public ImageZoomPanelTester() {

    }

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /**
         * CREATE THE APPROPRIATE PRIMITIVES FOR YOU NEED
         */

        BufferedImage image = new BufferedImage();
        Double imageValue = null;

        SwingNode imageZoomPanel = new SwingNode();

        MenuItem openSingleImage = new MenuItem("Open Single Image");
        openSingleImage.setOnAction(e -> {
            imageZoomPanel.getContent().add(new ImageZoomPanel());
        });

        MenuItem openMultiImages = new MenuItem("Open Multi Images");
        openMultiImages.setOnAction(e -> {

        });

        Menu file = new Menu("File");
        file.getItems().addAll(openSingleImage, openMultiImages);

        MenuBar menuBar = new MenuBar(file);

        VBox root = new VBox();
        root.getChildren().addAll(menuBar, imageZoomPanel);

        /**
         * This will stay the same regardless of future panels that may be tested
         * May edit prior lines to create the desired setup of the window
         */
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
