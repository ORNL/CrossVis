package gov.ornl.scout;

import gov.ornl.edenfx.EDENFXPreferenceKeys;
import gov.ornl.table.Table;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class TableColumnSpecificationDialog {
    private static final Logger log = Logger.getLogger(TableColumnSpecificationDialog.class.getName());

    public static ArrayList<TableColumnSpecification> getColumnSpecifications (File csvFile) throws IOException {
        String columnNames[] = Table.getFileHeader(csvFile);

        ObservableList<TableColumnSpecification> tableColumnSpecs = FXCollections.observableArrayList();
        for (String columnName : columnNames) {
            TableColumnSpecification columnSpec = new TableColumnSpecification(columnName, "Double",
                    "", false);
            tableColumnSpecs.add(columnSpec);
        }

        Dialog<ObservableList<TableColumnSpecification>> dialog = new Dialog<>();
        dialog.setTitle("CSV Column Specifications");
        dialog.setHeaderText("Specify Details for each Column");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TableView<TableColumnSpecification> columnSpecificationTableView = new TableView<>();
        columnSpecificationTableView.setEditable(true);

        TableColumn<TableColumnSpecification, String> nameColumn = new TableColumn<>("Column Name");
        nameColumn.setMinWidth(180);
        nameColumn.setCellValueFactory(new PropertyValueFactory<TableColumnSpecification, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(false);

        TableColumn<TableColumnSpecification, Boolean> ignoreColumn = new TableColumn<>("Ignore");
        ignoreColumn.setMinWidth(20);
        ignoreColumn.setCellValueFactory(new PropertyValueFactory<>("ignore"));
        ignoreColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        ignoreColumn.setEditable(true);

        TableColumn<TableColumnSpecification, String> typeColumn = new TableColumn<>("Column Type");
        typeColumn.setMinWidth(180);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setEditable(true);
        typeColumn.setCellFactory(column -> new ComboBoxTableCell<>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")));
        /*typeColumn.setCellFactory(new Callback<TableColumn<TableColumnSpecification, String>, TableCell<TableColumnSpecification, String>>() {
            @Override
            public TableCell<TableColumnSpecification, String> call(TableColumn<TableColumnSpecification, String> param) {
                return new ComboBoxTableCell<TableColumnSpecification, String>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (!empty) {
                            TableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
                            if (columnSpecification.getType().equals("Temporal")) {

                            } else {
                                if (columnSpecification.getParsePattern().isEmpty()) {
                                    columnSpecification.setParsePattern("");
                                }
                            }
//                                log.info("item is " + item + "  spec is " + tableColumnSpecs.get(rowNumber).getType());
////                                if (!item.equals(tableColumnSpecs.get(rowNumber).getType())) {
////                                    if (item.equalsIgnoreCase("Temporal")) {
////                                        tableColumnSpecs.get(rowNumber).setParsePattern("Temporal");
////                                    } else if (item.equalsIgnoreCase("Double")) {
////                                        tableColumnSpecs.get(rowNumber).setParsePattern("Not Temporal");
////                                    } else if (item.equalsIgnoreCase("Categorical")) {
////                                        tableColumnSpecs.get(rowNumber).setParsePattern("Not Temporal");
////                                    }
////                                }
//                                log.info("type set for row " + row.getIndex() + ": " + tableColumnSpecs.get(row.getIndex()).getParsePattern());
//                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });*/

        ArrayList<String> parsePatterns = new ArrayList<>();
        parsePatterns.add("dd.MM.yyy HH:mm:ss");
        parsePatterns.add("yyyy-MM-ddTHH:mm:ssz");

        TableColumn<TableColumnSpecification, String> parseColumn = new TableColumn<>("DateTime Parse Pattern");
        parseColumn.setMinWidth(180);
        parseColumn.setCellValueFactory(new PropertyValueFactory<TableColumnSpecification, String>("parsePattern"));
        parseColumn.setCellFactory(new Callback<TableColumn<TableColumnSpecification, String>, TableCell<TableColumnSpecification, String>>() {
            @Override
            public TableCell<TableColumnSpecification, String> call(TableColumn<TableColumnSpecification, String> param) {
                return new ComboBoxTableCell<TableColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
                    {
                        setComboBoxEditable(true);
                    }
                };
            }
        });
        parseColumn.setEditable(true);
//        parseColumn.setCellFactory(new Callback<TableColumn<TableColumnSpecification, String>, TableCell<TableColumnSpecification, String>>() {
//            @Override
//            public TableCell<TableColumnSpecification, String> call(TableColumn<TableColumnSpecification, String> param) {
//                ComboBoxTableCell<TableColumnSpecification, String> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList(parsePatterns));
//                cell.setComboBoxEditable(true);
//                cell.tableRowProperty().addListener((observable, oldValue, newValue) -> {
//                    log.info("row listener");
//                    if (newValue != null) {
//                        TableColumnSpecification columnSpecification = ((TableColumnSpecification)newValue.getItem());
//                        if (columnSpecification.getType().equals("Temporal")) {
//                            cell.setEditable(true);
//                            cell.setStyle("");
//                            log.info("row changed and ENABLING parse pattern cell");
//                        } else {
//                            cell.setEditable(false);
//                            cell.setStyle("-fx-text-fill: grey;-fx-border-color: red");
//                            log.info("row changed and DISABLING parse pattern cell");
//                        }
//                    }
//                });
//                return cell;
//                return new ComboBoxTableCell<TableColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
//                    {
//                        setComboBoxEditable(true);
//                    }
//
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        if (!empty) {
//                            TableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
//                            if (columnSpecification.getType().equals("Temporal")) {
//                                setDisable(false);
//                                setEditable(true);
//                                this.setStyle("");
//                                log.info("updating parse pattern for temporal column");
//                            } else {
//                                setDisable(true);
//                                setEditable(false);
//                                this.setStyle("-fx-text-fill: grey;-fx-border-color: red");
//                                log.info("updating parse pattern for non-temporal column");
//                            }
//                        }
//                        super.updateItem(item, empty);
//                    }
//                };
//            }
//        });
//        parseColumn.setEditable(true);

        columnSpecificationTableView.getColumns().addAll(ignoreColumn, nameColumn, typeColumn, parseColumn);
        columnSpecificationTableView.setItems(tableColumnSpecs);

        columnSpecificationTableView.setPrefHeight(200.);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(columnSpecificationTableView);

        dialog.getDialogPane().setContent(borderPane);

        Platform.runLater(() -> columnSpecificationTableView.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return tableColumnSpecs;
            }
            return null;
        });

        Optional<ObservableList<TableColumnSpecification>> result = dialog.showAndWait();

        if (result.isPresent()) {
            return new ArrayList<TableColumnSpecification>(result.get());
        }

        return null;
    }
}
