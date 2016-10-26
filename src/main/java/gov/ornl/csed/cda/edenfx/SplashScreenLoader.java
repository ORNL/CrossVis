package gov.ornl.csed.cda.edenfx;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by csg on 10/25/16.
 */
public class SplashScreenLoader extends Preloader {
    public static interface SharedScene {
        Parent getParentNode();
    }

    private Stage splashScreen;
    private ProgressBar bar;
    private boolean isEmbedded = false;

//    @Override
//    public void start(Stage stage) throws Exception {
//        splashScreen = stage;
//        splashScreen.setScene(createScene());
//        splashScreen.show();
//    }

    private Scene createScene() {
        bar = new ProgressBar();
        BorderPane pane = new BorderPane();
        pane.setCenter(bar);
        return new Scene(pane, 400, 300);
    }

    public void start(Stage stage) throws Exception {
        isEmbedded = (stage.getWidth() > 0);
        splashScreen = stage;
        splashScreen.setScene(createScene());
    }

    @Override
    public void handleProgressNotification(ProgressNotification notification) {
        if (notification.getProgress() != 1 && !splashScreen.isShowing()) {
            splashScreen.show();
        }
        bar.setProgress(notification.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification notification) {
        if (notification.getType() == StateChangeNotification.Type.BEFORE_START) {
            if (isEmbedded && splashScreen.isShowing()) {
                // fade out, hide stage at the end of animation
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(5000),
                        splashScreen.getScene().getRoot());
                fadeTransition.setFromValue(1.0);
                fadeTransition.setToValue(0.0);
                final Stage stage = splashScreen;
                EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        splashScreen.hide();
                    }
                };
                fadeTransition.setOnFinished(eventHandler);
                fadeTransition.play();
            } else {
                splashScreen.hide();
            }
        }
    }
//    @Override
//    public void handleApplicationNotification(PreloaderNotification notification) {
//        if (notification instanceof StateChangeNotification) {
//            splashScreen.hide();
//        }
//    }
}
