package gov.ornl.scout;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Scout extends Application {

    private Preferences preferences;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
    }
}
