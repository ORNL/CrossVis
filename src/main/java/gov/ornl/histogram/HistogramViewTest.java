package gov.ornl.histogram;

import gov.ornl.datatable.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class HistogramViewTest extends Application {
    DataTable table = new DataTable();
    ChoiceBox<String> variableChoiceBox;
    CheckBox showAxesCheckBox;
    CheckBox showTitleCheckBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() { System.exit(0); }

    @Override
    public void start(Stage primaryStage) {
        HistogramView horizontalHistogramView = new HistogramView(Orientation.HORIZONTAL, true, true);
        horizontalHistogramView.setPrefHeight(500);
        horizontalHistogramView.setPadding(new Insets(20));

        HistogramView verticalHistogramView = new HistogramView(Orientation.VERTICAL, true, true);
        verticalHistogramView.setPrefHeight(500);
        verticalHistogramView.setPadding(new Insets(20));

        Button loadDataButton = new Button("Load Data");
        loadDataButton.setOnAction(event -> {
            File f = new File("data/csv/cars-cat.csv");

            ArrayList<String> categoricalColumnNames = new ArrayList<>();
            categoricalColumnNames.add("Origin");
            try {
                IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                        null, null, table);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }

            ArrayList<String> columnTitles = new ArrayList<>();
            for (Column column : table.getColumns()) {
                columnTitles.add(column.getName());
            }
            showAxesCheckBox.setDisable(false);
            showTitleCheckBox.setDisable(false);
            variableChoiceBox.setDisable(false);
            variableChoiceBox.getItems().add("NONE");
            variableChoiceBox.getItems().addAll(FXCollections.observableArrayList(columnTitles));
            variableChoiceBox.getSelectionModel().select(0);
        });

        variableChoiceBox = new ChoiceBox<>();
        variableChoiceBox.setDisable(true);
        variableChoiceBox.setOnAction(event -> {
            if (variableChoiceBox.getValue().equals("NONE")) {
                horizontalHistogramView.clear();
                horizontalHistogramView.setTitle("None");
                verticalHistogramView.clear();
                verticalHistogramView.setTitle("None");
            } else {
                Column column = table.getColumn(variableChoiceBox.getValue());
                HistogramDataModel histogramDataModel = null;
                if (column instanceof DoubleColumn) {
                    histogramDataModel = new DoubleHistogramDataModel(((DoubleColumn)column).getValuesAsList());
                } else if (column instanceof CategoricalColumn) {
                    histogramDataModel = new CategoricalHistogramDataModel(((CategoricalColumn)column).getValuesAsList());
                }

                horizontalHistogramView.setHistogramDataModel(histogramDataModel);
                horizontalHistogramView.setTitle(column.getName());
                verticalHistogramView.setHistogramDataModel(histogramDataModel);
                verticalHistogramView.setTitle(column.getName());
            }
        });

        showAxesCheckBox = new CheckBox(" Show CrossVis ");
        showAxesCheckBox.setDisable(true);
        showAxesCheckBox.setSelected(horizontalHistogramView.getShowAxes());
        showAxesCheckBox.setOnAction(event -> {
            horizontalHistogramView.setShowAxes(showAxesCheckBox.isSelected());
            verticalHistogramView.setShowAxes(showAxesCheckBox.isSelected());
        });

        showTitleCheckBox = new CheckBox(" Show Title ");
        showTitleCheckBox.setDisable(true);
        showTitleCheckBox.setSelected(horizontalHistogramView.getShowTitle());
        showTitleCheckBox.setOnAction(event -> {
            horizontalHistogramView.setShowTitle(showTitleCheckBox.isSelected());
            verticalHistogramView.setShowTitle(showTitleCheckBox.isSelected());
        });


        BorderPane rootNode = new BorderPane();

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(loadDataButton, variableChoiceBox, showTitleCheckBox, showAxesCheckBox);

        GridPane histogramViewPane = new GridPane();
        histogramViewPane.setGridLinesVisible(true);
        ColumnConstraints leftColumnConstraints = new ColumnConstraints(100, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
        leftColumnConstraints.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumnConstraints = new ColumnConstraints(100, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
        rightColumnConstraints.setHgrow(Priority.ALWAYS);
        histogramViewPane.getColumnConstraints().addAll(leftColumnConstraints, rightColumnConstraints);

        histogramViewPane.add(horizontalHistogramView, 0, 0);
        histogramViewPane.add(verticalHistogramView, 1, 0);

        rootNode.setCenter(histogramViewPane);
        rootNode.setBottom(buttonBox);

        Scene scene = new Scene(rootNode, 960, 500, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("HistogramView Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
