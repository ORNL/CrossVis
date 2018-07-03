package gov.ornl.histogram;

import gov.ornl.scout.dataframe.CategoricalColumn;
import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.DataFrame;
import gov.ornl.scout.dataframe.DoubleColumn;
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

public class HistogramViewTest extends Application {
    DataFrame table;
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

            table = new DataFrame();
            String columnTitles[] = new String[0];
            try {
                columnTitles = DataFrame.getFileHeader(f);
                for (String columnTitle : columnTitles) {
                    if (columnTitle.equalsIgnoreCase("Origin")) {
                        table.addCategoricalColumn(columnTitle);
                    } else {
                        table.addDoubleColumn(columnTitle);
                    }
                }
                table.readRowsFromFile(f, true, null);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }

            showAxesCheckBox.setDisable(false);
            showTitleCheckBox.setDisable(false);
            variableChoiceBox.setDisable(false);
            variableChoiceBox.getItems().add("NONE");
            variableChoiceBox.getItems().addAll(FXCollections.observableArrayList(table.getColumnTitles()));
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
                    histogramDataModel = new DoubleHistogramDataModel(column.getValues());
                } else if (column instanceof CategoricalColumn) {
                    histogramDataModel = new CategoricalHistogramDataModel(column.getValues());
                }

                horizontalHistogramView.setHistogramDataModel(histogramDataModel);
                horizontalHistogramView.setTitle(column.getTitle());
                verticalHistogramView.setHistogramDataModel(histogramDataModel);
                verticalHistogramView.setTitle(column.getTitle());
            }
        });

        showAxesCheckBox = new CheckBox(" Show Axes ");
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
