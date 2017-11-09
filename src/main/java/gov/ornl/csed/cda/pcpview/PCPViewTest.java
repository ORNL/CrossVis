package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.IOUtilities;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Created by csg on 8/22/16.
 */
public class PCPViewTest extends Application {
    private PCPView pcpView;
    private DataModel dataModel;
    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) throws Exception {
//        StackPane pane = new StackPane(pcpView);
//        pane.setPadding(new Insets(20));

        pcpView = new PCPView();
        pcpView.setPrefHeight(500);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_LINES);

        ScrollPane scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());
        scrollPane.setFitToHeight(true);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);

        Scene scene = new Scene(rootNode, 960, 500, true, SceneAntialiasing.BALANCED);

        stage.setTitle("PCPView Test");
        stage.setScene(scene);
        stage.show();

        dataModel = new DataModel();
        pcpView.setDataModel(dataModel);

        try {
            IOUtilities.readCSV(new File("data/csv/cars.csv"), null, null, dataModel);
        } catch (IOException e) {
            System.exit(0);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
