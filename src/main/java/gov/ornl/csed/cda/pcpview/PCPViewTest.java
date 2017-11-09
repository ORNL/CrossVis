package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.IOUtilities;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

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

//        stage.setOnShown(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                try {
//                    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"),
//                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"), "Date", dataModel);
//                } catch (IOException e) {
//                    System.exit(0);
//                    e.printStackTrace();
//                }
//            }
//        });

        pcpView = new PCPView();
        pcpView.setPrefHeight(500);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_LINES);

        ScrollPane scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());
        scrollPane.setFitToHeight(true);

        Button dataButton = new Button("Load Data");
        dataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"),
                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"), "Date", dataModel);
                } catch (IOException e) {
                    System.exit(0);
                    e.printStackTrace();
                }
            }
        });

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);
        rootNode.setBottom(dataButton);

        Scene scene = new Scene(rootNode, 960, 500, true, SceneAntialiasing.BALANCED);

        stage.setTitle("PCPView Test");
        stage.setScene(scene);
        stage.show();

        dataModel = new DataModel();
        pcpView.setDataModel(dataModel);

//        try {
//            IOUtilities.readCSV(new File("data/csv/titan-performance.csv"),
//                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"), "Date", dataModel);
//        } catch (IOException e) {
//            System.exit(0);
//            e.printStackTrace();
//        }
    }



    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
