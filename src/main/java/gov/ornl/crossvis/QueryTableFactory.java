package gov.ornl.crossvis;

import gov.ornl.datatable.*;
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
import java.util.ArrayList;
import java.util.HashSet;

public class QueryTableFactory {
    private static TableColumn<ColumnSelection, String> createMaxValueColumn(TableView tableView, boolean isTemporal) {
        TableColumn<ColumnSelection, String> maxColumn;
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
        maxColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelection, String> t) -> {
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

    private static TableColumn<ColumnSelection, String> createMinValueColumn(TableView tableView, boolean isTemporal) {
        TableColumn<ColumnSelection, String> minColumn = new TableColumn<>("Minimum Value");
        minColumn.setMinWidth(200);
        minColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnSelection, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnSelection, String> t) {
                if (t.getValue() instanceof TemporalColumnSelectionRange) {
                    return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getStartInstant().toString());
                } else {
                    return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMinValue()));
                }
            }
        });
        minColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelection, String> t) -> {
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

    private static TableColumn<ColumnSelection, String> createColorColumn(TableView tableView) {
        TableColumn<ColumnSelection, String> colorColumn = new TableColumn<>("Color");
        colorColumn.setMinWidth(100);
//        colorColumn.setCellValueFactory(cellData -> "Blue");
        colorColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Friends", "Family", "Work Contacts"));
//        colorColumn.setCellFactory(new ColorTableCell<ColumnSelection>(colorColumn));
        colorColumn.setEditable(true);
        return colorColumn;
    }

    private static TableColumn<ColumnSelection, String> createCategoriesColumn(TableView tableView) {
        TableColumn<ColumnSelection, String> categoriesColumn = new TableColumn<>("Selected Categories");
        categoriesColumn.setMinWidth(200);
        categoriesColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnSelection, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnSelection, String> t) {
//                if (t.getValue() instanceof CategoricalColumnSelection) {
                if (((CategoricalColumnSelection)t.getValue()).getSelectedCategories().isEmpty()) {
                    return new ReadOnlyObjectWrapper("");
                } else {
                    String categoriesString = "";
                    int counter = 0;
                    for (String selectedCategory : ((CategoricalColumnSelection)t.getValue()).getSelectedCategories()) {
                        categoriesString += selectedCategory;
                        if (++counter < ((CategoricalColumnSelection)t.getValue()).getSelectedCategories().size()) {
                            categoriesString += ", ";
                        }
                    }
                    return new ReadOnlyObjectWrapper<>(categoriesString);
                }
//                    return new ReadOnlyObjectWrapper(((CategoricalColumnSelection)t.getValue()).getSelectedCategories().toString());
//                    return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getStartInstant().toString());
//                } else {
//                    return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMinValue()));
//                }
            }
        });
        categoriesColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelection, String> t) -> {
            String categoriesString = t.getNewValue().trim();
            System.out.println("categoriesString in setOnEditCommit() is " + categoriesString);

            if (categoriesString.isEmpty()) {
                t.getRowValue().getColumn().getDataModel().removeColumnSelectionFromActiveQuery(t.getRowValue());
            } else {
                CategoricalColumn categoricalColumn = (CategoricalColumn)t.getRowValue().getColumn();
                // parse new selected categories string
                HashSet<String> newSelectedCategories = new HashSet<>();
                String userEnteredCategories[] = categoriesString.split(",");

                for (int i = 0; i < userEnteredCategories.length; i++) {
                    String userEnteredCategory = userEnteredCategories[i].trim();
                    if (categoricalColumn.getCategories().contains(userEnteredCategory)) {
                        newSelectedCategories.add(userEnteredCategory);
                    }
                }

                if (newSelectedCategories.isEmpty()) {
                    t.getRowValue().getColumn().getDataModel().removeColumnSelectionFromActiveQuery(t.getRowValue());
                } else {
                    CategoricalColumnSelection categoricalColumnSelection = (CategoricalColumnSelection)t.getRowValue();
                    categoricalColumnSelection.setSelectedCategories(newSelectedCategories);
                    t.getTableView().refresh();
                }
            }
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

    public static TableView<ColumnSelection> buildDoubleSelectionTable() {
        // create tableview for double column selections
        TableView<ColumnSelection> doubleSelectionTableView = new TableView<>();
        doubleSelectionTableView.setEditable(true);

        TableColumn<ColumnSelection, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelection, String>("column"));

        TableColumn<ColumnSelection, String> minValueColumn = createMinValueColumn(doubleSelectionTableView, false);

        TableColumn<ColumnSelection, String> maxValueColumn = createMaxValueColumn(doubleSelectionTableView, false);

        doubleSelectionTableView.getColumns().addAll(nameColumn, minValueColumn, maxValueColumn);

        return doubleSelectionTableView;
    }

    public static TableView<ColumnSelection> buildTemporalSelectionTable() {
        // create tableview for temporal column selections
        TableView<ColumnSelection> temporalSelectionTableView = new TableView<>();

        TableColumn<ColumnSelection, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelection, String>("column"));

        TableColumn<ColumnSelection, String> startValueColumn = createMinValueColumn(temporalSelectionTableView, true);

        TableColumn<ColumnSelection, String> endValueColumn = createMaxValueColumn(temporalSelectionTableView, true);

        temporalSelectionTableView.getColumns().addAll(nameColumn, startValueColumn, endValueColumn);

        return temporalSelectionTableView;
    }

    public static TableView<ColumnSelection> buildCategoricalSelectionTable() {
        // create tableview for categorical column selections
        TableView<ColumnSelection> categoricalSelectionTableView = new TableView<>();
        categoricalSelectionTableView.setEditable(true);

        TableColumn<ColumnSelection, String> nameColumn = new TableColumn<>("Column");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelection, String>("column"));

        TableColumn<ColumnSelection, String> categoriesColumn = createCategoriesColumn(categoricalSelectionTableView);

//        TableColumn<ColumnSelection, String> colorColumn = createColorColumn(categoricalSelectionTableView);

        categoricalSelectionTableView.getColumns().addAll(nameColumn, categoriesColumn);

        return categoricalSelectionTableView;
    }
}
