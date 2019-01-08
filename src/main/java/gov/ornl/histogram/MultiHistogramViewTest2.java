package gov.ornl.histogram;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MultiHistogramViewTest2 extends Application {
    DataTable table = new DataTable();
    VBox histogramViewBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        histogramViewBox = new VBox();

        Button loadDataButton = new Button("Load Data");
        loadDataButton.setOnAction(event -> {
            File f = new File("data/csv/cars-cat.csv");

            ArrayList<String> categoricalColumnNames = new ArrayList<>();
            categoricalColumnNames.add("Origin");
            try {
                IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                        null, null, null, null, table);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }

            for (int i = 0; i < table.getColumnCount(); i++) {
                Column column = table.getColumn(i);
                if (column instanceof DoubleColumn) {
                    HistogramView histogramView = new HistogramView(Orientation.HORIZONTAL);
                    histogramView.setHistogramDataModel(new DoubleHistogramDataModel(((DoubleColumn) column).getValuesAsList()));
                    histogramView.setPrefSize(400, 200);
                    histogramViewBox.getChildren().add(histogramView);
                }
            }
        });
        HBox buttonBox = new HBox();
        buttonBox.getChildren().add(loadDataButton);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(histogramViewBox);
        rootNode.setBottom(buttonBox);

        Scene scene = new Scene(rootNode, 400, 500, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("Multiple Histogram Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}
