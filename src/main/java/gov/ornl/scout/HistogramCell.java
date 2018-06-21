package gov.ornl.scout;

import gov.ornl.histogram.CategoricalHistogramDataModel;
import gov.ornl.histogram.DoubleHistogramDataModel;
import gov.ornl.histogram.HistogramView;
import gov.ornl.table.CategoricalColumn;
import gov.ornl.table.Column;
import gov.ornl.table.DoubleColumn;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class HistogramCell extends ListCell<Column> {
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
            content.putString(getItem().getTitle());

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
                    if (items.get(i).getTitle().equals(db.getString())) {
                        draggedIndex = i;
                    }
                    if (items.get(i).getTitle().equals(getItem().getTitle())) {
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
                DoubleHistogramDataModel histogramDataModel = new DoubleHistogramDataModel(column.getValues());
                histogramView.setHistogramDataModel(histogramDataModel);
                histogramView.setTitle(column.getTitle());
                setGraphic(histogramView);
            } else if (column instanceof CategoricalColumn) {
                CategoricalHistogramDataModel categoricalHistogramDataModel = new CategoricalHistogramDataModel(column.getValues());
                histogramView.setHistogramDataModel(categoricalHistogramDataModel);
                histogramView.setTitle(column.getTitle());
                setGraphic(histogramView);
            }
        }
    }
}
