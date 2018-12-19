package gov.ornl.correlationview;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ZoomCorrelationMatrixViewTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ZoomCorrelationMatrixView view = new ZoomCorrelationMatrixView();
        view.setPadding(new Insets(10));

        BorderPane rootPane = new BorderPane();
        rootPane.setCenter(view);

        Scene scene = new Scene(rootPane, 600, 600, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("ZoomCorrelationMatrixTest");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() { System.exit(0); }

}
