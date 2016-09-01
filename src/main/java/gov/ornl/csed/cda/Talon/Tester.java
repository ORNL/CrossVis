package gov.ornl.csed.cda.Talon;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by whw on 9/1/16.
 */
public class Tester extends Application {

    public Tester() {

    }

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /**
         * CREATE THE APPROPRIATE PRIMITIVES FOR YOU NEED
         */



        VBox root = new VBox();

        /**
         * This will stay the same regardless of future panels that may be tested
         * May edit prior lines to create the desired setup of the window
         */
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
