package gov.ornl.csed.cda.mustang;

import gov.ornl.csed.cda.Falcon.FalconPreferenceKeys;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Created by csg on 8/12/16.
 */
public class MustangMain extends Application {

    private Preferences preferences;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane rootNode = new BorderPane();

        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(rootNode, screenBounds.getWidth()*.8, screenBounds.getHeight()*.5);

        MenuBar menubar = createMenuBar(primaryStage);

        rootNode.setTop(menubar);
//        rootNode.setCenter(mainSplitPane);

        primaryStage.setTitle("Mustang | Prototype");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem openPLGMenuItem = new MenuItem("Open PLG...");
        openPLGMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                String lastPLGDirectoryPath = preferences.get(FalconPreferenceKeys.LAST_PLG_READ_DIRECTORY, "");
                if (!lastPLGDirectoryPath.isEmpty()) {
                    fileChooser.setInitialDirectory(new File(lastPLGDirectoryPath));
                }

                fileChooser.setTitle("Open PLG File");
                File plgFile = fileChooser.showOpenDialog(primaryStage);
                if (plgFile != null) {
//                    try {
////                        openPLGFile(plgFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });

        MenuItem exitMI = new MenuItem("Exit");
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                primaryStage.close();
            }
        });

        fileMenu.getItems().addAll(openPLGMenuItem, exitMI);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }
}
