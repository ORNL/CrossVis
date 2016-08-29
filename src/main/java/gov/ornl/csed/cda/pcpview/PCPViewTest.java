package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.IOUtilities;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by csg on 8/22/16.
 */
public class PCPViewTest extends Application {
    private PCPView pcpView;

    @Override
    public void init() {
        DataModel dataModel = new DataModel();
        try {
            IOUtilities.readCSV(new File("data/csv/am-test.csv"), dataModel);
        } catch (IOException e) {
            System.exit(0);
            e.printStackTrace();
        }

        pcpView = new PCPView();
        pcpView.setDataModel(dataModel);
    }

    @Override
    public void start(Stage stage) throws Exception {
//        StackPane pane = new StackPane(pcpView);
//        pane.setPadding(new Insets(20));

        pcpView.setPrefHeight(400);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 800, 400, true, SceneAntialiasing.BALANCED);

        stage.setTitle("PCPView Test");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
