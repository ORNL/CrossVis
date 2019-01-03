package gov.ornl.scatterplot;

import gov.ornl.datatable.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScatterplotTest extends Application {
    private static Scatterplot scatterplot;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DataTable dataTable = new DataTable();
        dataTable.addDataModelListener(new DataTableListener() {
            @Override
            public void dataTableReset(DataTable dataTable) {

            }

            @Override
            public void dataTableStatisticsChanged(DataTable dataTable) {

            }

            @Override
            public void dataTableColumnExtentsChanged(DataTable dataTable) {

            }

            @Override
            public void dataTableColumnFocusExtentsChanged(DataTable dataTable) {

            }

            @Override
            public void dataTableNumHistogramBinsChanged(DataTable dataTable) {

            }

            @Override
            public void dataTableAllColumnSelectionsRemoved(DataTable dataTable) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataTable, Column column) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelectionRange) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelectionRange) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableColumnSelectionChanged(DataTable dataTable, ColumnSelection columnSelectionRange) {
                scatterplot.fillSelectionPointSets();
                scatterplot.drawPoints();
            }

            @Override
            public void dataTableHighlightedColumnChanged(DataTable dataTable, Column oldHighlightedColumn, Column newHighlightedColumn) {

            }

            @Override
            public void dataTableTuplesAdded(DataTable dataTable, ArrayList<Tuple> newTuples) {

            }

            @Override
            public void dataTableTuplesRemoved(DataTable dataTable, int numTuplesRemoved) {

            }

            @Override
            public void dataTableColumnDisabled(DataTable dataTable, Column disabledColumn) {

            }

            @Override
            public void dataTableColumnsDisabled(DataTable dataTable, ArrayList<Column> disabledColumns) {

            }

            @Override
            public void dataTableColumnEnabled(DataTable dataTable, Column enabledColumn) {

            }

            @Override
            public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index) {

            }

            @Override
            public void dataTableColumnOrderChanged(DataTable dataTable) {

            }

            @Override
            public void dataTableColumnNameChanged(DataTable dataTable, Column column) {

            }
        });

        ArrayList<String> categoricalColumnNames = new ArrayList<>();
        categoricalColumnNames.add("Origin");
        try {
            IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                    null, null, dataTable);
        } catch (IOException e) {
            System.exit(0);
            e.printStackTrace();
        }

        scatterplot = new Scatterplot(dataTable.getColumn(4), dataTable.getColumn(0), Color.STEELBLUE, Color.SILVER, 0.5);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scatterplot.getGraphicsGroup());

        rootNode.setOnMouseClicked(event -> {
            if (event.isSecondaryButtonDown()) {
                final ContextMenu contextMenu = new ContextMenu();
                CheckMenuItem showXAxisMarginValuesCheck = new CheckMenuItem("Show X Axis Margin Values");
                showXAxisMarginValuesCheck.selectedProperty().bindBidirectional(scatterplot.showXAxisMarginValuesProperty());
                CheckMenuItem showYAxisMarginValuesCheck = new CheckMenuItem("Show Y Axis Margin Values");
                showYAxisMarginValuesCheck.selectedProperty().bindBidirectional(scatterplot.showYAxisMarginValuesProperty());
                MenuItem swapAxesMenuItem = new MenuItem("Swap X and Y Axes");
                swapAxesMenuItem.setOnAction(event1 -> {
                    scatterplot.swapColumnAxes();
                });
                MenuItem closeMenuItem = new MenuItem("Close Popup Menu");
                closeMenuItem.setOnAction(event1 -> {
                    contextMenu.hide();
                });
                contextMenu.show(rootNode, event.getScreenX(), event.getScreenY());
            }
        });

        rootNode.widthProperty().addListener(observable -> {
            scatterplot.resize(0, 0, rootNode.getWidth(), rootNode.getHeight());
        });

        rootNode.heightProperty().addListener(observable -> {
            scatterplot.resize(0, 0, rootNode.getWidth(), rootNode.getHeight());
        });

        Scene scene = new Scene(rootNode, 600, 600, true, SceneAntialiasing.BALANCED);
        primaryStage.setTitle("Scatterplot Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
