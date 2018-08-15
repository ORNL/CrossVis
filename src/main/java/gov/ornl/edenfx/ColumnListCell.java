package gov.ornl.edenfx;

import gov.ornl.datatable.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ColumnListCell extends ListCell<Column> {
    HBox cellBox = new HBox(20);
//    GridPane cellPane = new GridPane();
//    Pane cellPane = new Pane();
    Label columnTitleLabel = new Label();
    Label columnTypeLabel = new Label();
//    CheckBox enabledCheckBox = new CheckBox();
//    TextField columnTitleTextField = new TextField();

    Column column;

    public ColumnListCell() {
        columnTypeLabel.setTextAlignment(TextAlignment.RIGHT);
//        cellPane.add(enabledCheckBox, 0, 0, 1, 1);
//        cellPane.add(columnTitleText, 1, 0, 2, 1);
//        cellPane.add(columnTypeText, 3, 0, 1, 1);
        columnTitleLabel.setPrefWidth(50);
        columnTitleLabel.setMinWidth(50);
        columnTitleLabel.setMaxWidth(Double.MAX_VALUE);
        cellBox.getChildren().addAll(columnTitleLabel, columnTypeLabel);
        HBox.setHgrow(columnTitleLabel, Priority.ALWAYS);
//        HBox.setHgrow(columnTypeText, Priority.NEVER);
//        columnTitleTextField.setMinWidth(50);
//        columnTitleTextField.setPrefWidth(50);
//        columnTitleTextField.setMaxWidth(Double.MAX_VALUE);
//        columnTitleTextField.setEditable(false);

//        cellBox.getChildren().addAll(columnTitleTextField);
//        HBox.setHgrow(columnTitleTextField, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(Column column, boolean empty) {
        super.updateItem(column, empty);
        setText(null);
        if (empty) {
            setGraphic(null);
        } else {
            columnTitleLabel.textProperty().bindBidirectional(column.nameProperty());
//            enabledCheckBox.selectedProperty().bindBidirectional(column.enabledProperty());
            if (column instanceof DoubleColumn) {
                columnTypeLabel.setText("(Double)");
            } else if (column instanceof TemporalColumn) {
                columnTypeLabel.setText("(Temporal)");
            } else if (column instanceof CategoricalColumn) {
                columnTypeLabel.setText("(Categorical)");
            }
//            columnTitleTextField.setText(column.getName());
            setGraphic(cellBox);
        }
    }
}
