package gov.ornl.csed.cda.Talon;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

        Double imageValue = null;

        SwingNode imageZoomPanel = new SwingNode();

        MenuItem openSingleImage = new MenuItem("Open Single Image");
        openSingleImage.setOnAction(e -> {
            BufferedImage image = null;

            FileChooser chooser = new FileChooser();
            File imageFile = chooser.showOpenDialog(primaryStage);

            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            imageZoomPanel.setContent(new ImageZoomPanel(image));
            imageZoomPanel.getContent().repaint();
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
