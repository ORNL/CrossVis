package gov.ornl.histogram;

import gov.ornl.table.Column;
import gov.ornl.table.DoubleColumn;
import gov.ornl.table.Table;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MultiHistogramViewTest extends Application {
    Table table;
    ListView<Column> histogramListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        histogramListView = new ListView<>();
//        listView.getItems().addAll("One", "Two", "Three", "Four");
        histogramListView.setCellFactory(param -> new HistogramCell());
        histogramListView.setPrefWidth(400);

        Button loadDataButton = new Button("Load Data");
        loadDataButton.setOnAction(event -> {
            File f = new File("data/csv/cars-cat.csv");

            table = new Table();
            String columnTitles[] = new String[0];
            try {
                columnTitles = Table.getFileHeader(f);
                for (String columnTitle : columnTitles) {
                    if (columnTitle.equalsIgnoreCase("Origin")) {
                        table.addCategoricalColumn(columnTitle);
                    } else {
                        table.addDoubleColumn(columnTitle);
                    }
                }
                table.readRowsFromFile(f, true, null);

                for (int i = 0; i < table.getColumnCount(); i++) {
                    Column column = table.getColumn(i);
                    histogramListView.getItems().add(column);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        });
        HBox buttonBox = new HBox();
        buttonBox.getChildren().add(loadDataButton);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(histogramListView);
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

    private class HistogramCell extends ListCell<Column> {
        private final HistogramView histogramView = new HistogramView(Orientation.HORIZONTAL);

        public HistogramCell() {
            setPrefWidth(0);
//            prefWidthProperty().bind(histogramListView.widthProperty().subtract(2));
            ListCell thisCell = this;
//            histogramView.prefWidthProperty().bind(getListView().widthProperty());
            histogramView.setPrefHeight(80);
            histogramView.setPadding(new Insets(10));
//            histogramView.setPrefSize(list, 80);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(Column column, boolean empty) {
            super.updateItem(column, empty);

            if (empty || column == null) {
                setGraphic(null);
            } else {
                if (column instanceof DoubleColumn) {
                    DoubleHistogramDataModel histogramDataModel = new DoubleHistogramDataModel(column.getValues());
                    histogramView.setHistogramDataModel(histogramDataModel);
                    histogramView.setTitle(column.getTitle());
                    setGraphic(histogramView);
                }
            }
        }
    }
}
