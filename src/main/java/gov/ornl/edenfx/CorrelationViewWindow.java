package gov.ornl.edenfx;

import gov.ornl.correlationview.CorrelationMatrixView;
import gov.ornl.datatable.DataTable;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CorrelationViewWindow extends Application {
    private CorrelationMatrixView correlationMatrixView;
    private DataTable dataTable;

    public CorrelationViewWindow(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        correlationMatrixView = new CorrelationMatrixView();
        correlationMatrixView.setPrefHeight(500);
        correlationMatrixView.setPadding(new Insets(10));

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(correlationMatrixView);

        Scene scene = new Scene(rootNode, 600, 600, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("M u s t a n g | Correlation Matrix View");
        primaryStage.setScene(scene);
        primaryStage.show();

        correlationMatrixView.setDataTable(dataTable);
    }
}
