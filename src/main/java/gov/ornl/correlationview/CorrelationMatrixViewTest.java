package gov.ornl.correlationview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class CorrelationMatrixViewTest extends Application {
    public static final Logger log = Logger.getLogger(CorrelationMatrixViewTest.class.getName());

    private CorrelationMatrixView correlationMatrixView;
    private DataTable dataTable;

    @Override
    public void start(Stage primaryStage) throws Exception {
        correlationMatrixView = new CorrelationMatrixView();
        correlationMatrixView.setPrefHeight(500);
        correlationMatrixView.setPadding(new Insets(10));

        Button loadDataButton = new Button("Load Data");
        loadDataButton.setOnAction(event -> {
            ArrayList<String> categoricalColumnNames = new ArrayList<>();
            categoricalColumnNames.add("Origin");
            try {
                IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                        null, null, dataTable);
            } catch (IOException e) {
                System.exit(0);
                e.printStackTrace();
            }
        });

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(2.);
        buttonBox.getChildren().addAll(loadDataButton);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(correlationMatrixView);
        rootNode.setBottom(buttonBox);

        Scene scene = new Scene(rootNode, 600, 600, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("CorrelationMatrixView Text");
        primaryStage.setScene(scene);
        primaryStage.show();

        dataTable = new DataTable();
        correlationMatrixView.setDataTable(dataTable);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
