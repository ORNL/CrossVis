package gov.ornl.histogram;

import gov.ornl.datatable.*;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MultiHistogramViewTest extends Application {
    private DataTable table = new DataTable();
//    private static final ObservableList<Column> columns = FXCollections.observableArrayList();
//    ListView<Column> histogramListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ListView<Column> histogramListView = new ListView<>();
        histogramListView.setCellFactory(param -> new HistogramCell());
        histogramListView.setPrefWidth(400);
        histogramListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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

            for (int i = 0; i < table.getColumnCount(); i++) {
                Column column = table.getColumn(i);
                histogramListView.getItems().add(column);
//                    columns.add(column);
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
            ListCell thisCell = this;
            histogramView.setShowAxes(false);
            histogramView.setPrefHeight(60);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            setOnDragDetected(event -> {
                if (getItem() == null) {
                    return;
                }

                ObservableList<Column> items = getListView().getItems();

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getName());

                dragboard.setDragView(histogramView.snapshot(null, null));

                dragboard.setContent(content);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setOpacity(1.);
                }
            });

            setOnDragDropped(event -> {
                if (getItem() == null) {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ObservableList<Column> items = getListView().getItems();
                    int draggedIndex = -1;
                    int thisIndex = -1;
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getName().equals(db.getString())) {
                            draggedIndex = i;
                        }
                        if (items.get(i).getName().equals(getItem().getName())) {
                            thisIndex = i;
                        }
                    }

//                    Column temp = items.get(draggedIndex);
//                    columns.set(draggedIndex, columns.get(thisIndex));
//                    columns.set(thisIndex, temp);
//
                    Column draggingItem = items.remove(draggedIndex);
//                    items.set(draggedIndex, getItem());
                    items.add(thisIndex, draggingItem);
                    getListView().getSelectionModel().clearAndSelect(thisIndex);

//                    List<Column> itemsCopy = new ArrayList<>(getListView().getItems());
//                    getListView().getItems().setAll(itemsCopy);

                    success = true;
                }

                event.setDropCompleted(success);
                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(Column column, boolean empty) {
            super.updateItem(column, empty);

            if (empty || column == null) {
                setGraphic(null);
            } else {
                if (column instanceof DoubleColumn) {
                    DoubleHistogramDataModel histogramDataModel = new DoubleHistogramDataModel(((DoubleColumn)column).getValuesAsList());
                    histogramView.setHistogramDataModel(histogramDataModel);
                    histogramView.setTitle(column.getName());
                    setGraphic(histogramView);
                } else if (column instanceof CategoricalColumn) {
                    CategoricalHistogramDataModel categoricalHistogramDataModel = new CategoricalHistogramDataModel(((CategoricalColumn)column).getValuesAsList());
                    histogramView.setHistogramDataModel(categoricalHistogramDataModel);
                    histogramView.setTitle(column.getName());
                    setGraphic(histogramView);
                }
            }
        }
    }
}
