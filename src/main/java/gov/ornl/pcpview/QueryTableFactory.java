package gov.ornl.pcpview;

import gov.ornl.datatable.CategoricalColumnSelection;
import gov.ornl.datatable.ColumnSelectionRange;
import gov.ornl.datatable.DoubleColumnSelectionRange;
import gov.ornl.datatable.TemporalColumnSelectionRange;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class QueryTableFactory {
    private static TableColumn<ColumnSelectionRange, String> createMaxValueColumn(TableView tableView, boolean isTemporal) {
        TableColumn<ColumnSelectionRange, String> maxColumn;
        if (isTemporal) {
            maxColumn = new TableColumn<>("End");
        } else {
            maxColumn = new TableColumn<>("Maximum Value");
        }

        maxColumn.setMinWidth(200);
        maxColumn.setCellValueFactory(t -> {
            if (t.getValue() instanceof TemporalColumnSelectionRange) {
                return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getEndInstant().toString());
            } else {
                return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMaxValue()));
            }
        });
        maxColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        maxColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelectionRange, String> t) -> {
            if (t.getRowValue() instanceof TemporalColumnSelectionRange) {
                try {
                    Instant instant = Instant.parse(t.getNewValue());
                    ((TemporalColumnSelectionRange) t.getRowValue()).setEndInstant(instant);
                } catch (DateTimeParseException ex) {
                    // show alert dialog
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Value Parsing Error");
                    alert.setHeaderText("An Exception Occurred While Parsing New End Time");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    tableView.refresh();
                }
            } else {
                try {
                    Double doubleValue = Double.parseDouble(t.getNewValue());
                    ((DoubleColumnSelectionRange) t.getRowValue()).setMaxValue(doubleValue);
                } catch (NumberFormatException ex) {
                    // show alert dialog
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Value Parsing Error");
                    alert.setHeaderText("An Exception Occurred While Parsing New Maximum Value");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    tableView.refresh();
                }
            }
        });
        maxColumn.setEditable(true);

        return maxColumn;
    }

    private static TableColumn<ColumnSelectionRange, String> createMinValueColumn(TableView tableView, boolean isTemporal) {
        TableColumn<ColumnSelectionRange, String> minColumn = new TableColumn<>("Minimum Value");
        minColumn.setMinWidth(200);
        minColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnSelectionRange, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnSelectionRange, String> t) {
                if (t.getValue() instanceof TemporalColumnSelectionRange) {
                    return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getStartInstant().toString());
                } else {
                    return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMinValue()));
                }
            }
        });
        minColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelectionRange, String> t) -> {
            if (t.getRowValue() instanceof TemporalColumnSelectionRange) {
                try {
                    Instant instant = Instant.parse(t.getNewValue());
                    ((TemporalColumnSelectionRange) t.getRowValue()).setStartInstant(instant);
                } catch (DateTimeParseException ex) {
                    // show alert dialog
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Value Parsing Error");
                    alert.setHeaderText("An Exception Occurred While Parsing New Start Time");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    tableView.refresh();
                }
            } else {
                try {
                    Double doubleValue = Double.parseDouble(t.getNewValue());
                    ((DoubleColumnSelectionRange) t.getRowValue()).setMinValue(doubleValue);
                } catch (NumberFormatException ex) {
                    // show alert dialog
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Value Parsing Error");
                    alert.setHeaderText("An Exception Occurred While Parsing New Minimum Value");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                    tableView.refresh();
                }
            }
        });
        minColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        minColumn.setEditable(true);

        return minColumn;
    }

    private static TableColumn<ColumnSelectionRange, String> createColorColumn(TableView tableView) {
        TableColumn<ColumnSelectionRange, String> colorColumn = new TableColumn<>("Color");
        colorColumn.setMinWidth(100);
//        colorColumn.setCellValueFactory(cellData -> "Blue");
        colorColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Friends", "Family", "Work Contacts"));
//        colorColumn.setCellFactory(new ColorTableCell<ColumnSelectionRange>(colorColumn));
        colorColumn.setEditable(true);
        return colorColumn;
    }

    private static TableColumn<ColumnSelectionRange, String> createCategoriesColumn(TableView tableView) {
        TableColumn<ColumnSelectionRange, String> categoriesColumn = new TableColumn<>("Selected Categories");
        categoriesColumn.setMinWidth(200);
        categoriesColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnSelectionRange, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnSelectionRange, String> t) {
//                if (t.getValue() instanceof CategoricalColumnSelection) {
                    return new ReadOnlyObjectWrapper(((CategoricalColumnSelection)t.getValue()).getSelectedCategories().toString());
//                    return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getStartInstant().toString());
//                } else {
//                    return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMinValue()));
//                }
            }
        });
        categoriesColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelectionRange, String> t) -> {
            String categoriesString = t.getNewValue();
            System.out.println("categoriesString in setOnEditCommit() is " + categoriesString);
//            if (t.getRowValue() instanceof TemporalColumnSelectionRange) {
//                try {
//                    Instant instant = Instant.parse(t.getNewValue());
//                    ((TemporalColumnSelectionRange) t.getRowValue()).setStartInstant(instant);
//                } catch (DateTimeParseException ex) {
//                    // show alert dialog
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Value Parsing Error");
//                    alert.setHeaderText("An Exception Occurred While Parsing New Start Time");
//                    alert.setContentText(ex.getMessage());
//                    alert.showAndWait();
//                    tableView.refresh();
//                }
//            } else {
//                try {
//                    Double doubleValue = Double.parseDouble(t.getNewValue());
//                    ((DoubleColumnSelectionRange) t.getRowValue()).setMinValue(doubleValue);
//                } catch (NumberFormatException ex) {
//                    // show alert dialog
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Value Parsing Error");
//                    alert.setHeaderText("An Exception Occurred While Parsing New Minimum Value");
//                    alert.setContentText(ex.getMessage());
//                    alert.showAndWait();
//                    tableView.refresh();
//                }
//            }
        });
        categoriesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoriesColumn.setEditable(true);

        return categoriesColumn;
    }

    public static TableView<ColumnSelectionRange> buildDoubleSelectionTable() {
        // create tableview for double column selections
        TableView<ColumnSelectionRange> doubleSelectionTableView = new TableView<>();
        doubleSelectionTableView.setEditable(true);

        TableColumn<ColumnSelectionRange, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));

        TableColumn<ColumnSelectionRange, String> minValueColumn = createMinValueColumn(doubleSelectionTableView, false);

        TableColumn<ColumnSelectionRange, String> maxValueColumn = createMaxValueColumn(doubleSelectionTableView, false);

        doubleSelectionTableView.getColumns().addAll(nameColumn, minValueColumn, maxValueColumn);

        return doubleSelectionTableView;
    }

    public static TableView<ColumnSelectionRange> buildTemporalSelectionTable() {
        // create tableview for temporal column selections
        TableView<ColumnSelectionRange> temporalSelectionTableView = new TableView<>();

        TableColumn<ColumnSelectionRange, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));

        TableColumn<ColumnSelectionRange, String> startValueColumn = createMinValueColumn(temporalSelectionTableView, true);

        TableColumn<ColumnSelectionRange, String> endValueColumn = createMaxValueColumn(temporalSelectionTableView, true);

        temporalSelectionTableView.getColumns().addAll(nameColumn, startValueColumn, endValueColumn);

        return temporalSelectionTableView;
    }

    public static TableView<ColumnSelectionRange> buildCategoricalSelectionTable() {
        // create tableview for categorical column selections
        TableView<ColumnSelectionRange> categoricalSelectionTableView = new TableView<>();

        TableColumn<ColumnSelectionRange, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));

        TableColumn<ColumnSelectionRange, String> categoriesColumn = createCategoriesColumn(categoricalSelectionTableView);

//        TableColumn<ColumnSelectionRange, String> colorColumn = createColorColumn(categoricalSelectionTableView);

        categoricalSelectionTableView.getColumns().addAll(nameColumn, categoriesColumn);

        return categoricalSelectionTableView;
    }
}
