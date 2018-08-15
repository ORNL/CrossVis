package gov.ornl.edenfx;

import gov.ornl.datatable.IOUtilities;
import javafx.application.Platform;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Logger;

public class DataTableColumnSpecificationDialog {
    private static final Logger log = Logger.getLogger(DataTableColumnSpecificationDialog.class.getName());

    public static ArrayList<DataTableColumnSpecification> getColumnSpecifications (File csvFile) throws IOException {
        String columnNames[] = IOUtilities.readCSVHeader(csvFile);

        ObservableList<DataTableColumnSpecification> tableColumnSpecs = FXCollections.observableArrayList();
        for (String columnName : columnNames) {
            DataTableColumnSpecification columnSpec = new DataTableColumnSpecification(columnName, "Double",
                    "", false);
            tableColumnSpecs.add(columnSpec);
        }

        Dialog<ObservableList<DataTableColumnSpecification>> dialog = new Dialog<>();
        dialog.setTitle("CSV Column Specifications");
        dialog.setHeaderText("Specify Details for each Column");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TableView<DataTableColumnSpecification> columnSpecificationTableView = new TableView<>();
        columnSpecificationTableView.setEditable(true);

        TableColumn<DataTableColumnSpecification, String> nameColumn = new TableColumn<>("Column Name");
        nameColumn.setMinWidth(180);
        nameColumn.setCellValueFactory(new PropertyValueFactory<DataTableColumnSpecification, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(false);

        TableColumn<DataTableColumnSpecification, Boolean> ignoreColumn = new TableColumn<>("Ignore");
        ignoreColumn.setMinWidth(20);
        ignoreColumn.setCellValueFactory(new PropertyValueFactory<>("ignore"));
        ignoreColumn.setCellFactory(column -> new CheckBoxTableCell<>());
        ignoreColumn.setEditable(true);

        TableColumn<DataTableColumnSpecification, String> typeColumn = new TableColumn<>("Column Type");
        typeColumn.setMinWidth(180);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setEditable(true);
        typeColumn.setCellFactory(column -> new ComboBoxTableCell<>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")));
        /*typeColumn.setCellFactory(new Callback<TableColumn<DataTableColumnSpecification, String>, TableCell<DataTableColumnSpecification, String>>() {
            @Override
            public TableCell<DataTableColumnSpecification, String> call(TableColumn<DataTableColumnSpecification, String> param) {
                return new ComboBoxTableCell<DataTableColumnSpecification, String>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (!empty) {
                            DataTableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
                            if (columnSpecification.getType().equals("Temporal")) {

                            } else {
                                if (columnSpecification.getParsePattern().isEmpty()) {
                                    columnSpecification.setDateTimeFormatterID("");
                                }
                            }
//                                log.info("item is " + item + "  spec is " + tableColumnSpecs.get(rowNumber).getType());
////                                if (!item.equals(tableColumnSpecs.get(rowNumber).getType())) {
////                                    if (item.equalsIgnoreCase("Temporal")) {
////                                        tableColumnSpecs.get(rowNumber).setDateTimeFormatterID("Temporal");
////                                    } else if (item.equalsIgnoreCase("Double")) {
////                                        tableColumnSpecs.get(rowNumber).setDateTimeFormatterID("Not Temporal");
////                                    } else if (item.equalsIgnoreCase("Categorical")) {
////                                        tableColumnSpecs.get(rowNumber).setDateTimeFormatterID("Not Temporal");
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
        parsePatterns.add("BASIC_ISO_DATE");
        parsePatterns.add("ISO_DATE");
        parsePatterns.add("ISO_DATE_TIME");
        parsePatterns.add("ISO_INSTANT");
        parsePatterns.add("ISO_LOCAL_DATE");
        parsePatterns.add("ISO_LOCAL_DATE_TIME");
        parsePatterns.add("ISO_LOCAL_TIME");
        parsePatterns.add("ISO_OFFSET_DATE");
        parsePatterns.add("ISO_OFFSET_DATE_TIME");
        parsePatterns.add("ISO_OFFSET_TIME");
        parsePatterns.add("ISO_ORDINAL_DATE");
        parsePatterns.add("ISO_TIME");
        parsePatterns.add("ISO_WEEK_DATE");
        parsePatterns.add("ISO_ZONED_DATE_TIME");
        parsePatterns.add("RFC_1123_DATE_TIME");

        TableColumn<DataTableColumnSpecification, String> parseColumn = new TableColumn<>("Date Time Formatter");
        parseColumn.setMinWidth(180);
        parseColumn.setCellValueFactory(new PropertyValueFactory<DataTableColumnSpecification, String>("parsePattern"));
        parseColumn.setCellFactory(new Callback<TableColumn<DataTableColumnSpecification, String>, TableCell<DataTableColumnSpecification, String>>() {
            @Override
            public TableCell<DataTableColumnSpecification, String> call(TableColumn<DataTableColumnSpecification, String> param) {
                return new ComboBoxTableCell<DataTableColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
                    {
                        setComboBoxEditable(true);
                    }
                };
            }
        });
        parseColumn.setEditable(true);
//        parseColumn.setCellFactory(new Callback<TableColumn<DataTableColumnSpecification, String>, TableCell<DataTableColumnSpecification, String>>() {
//            @Override
//            public TableCell<DataTableColumnSpecification, String> call(TableColumn<DataTableColumnSpecification, String> param) {
//                ComboBoxTableCell<DataTableColumnSpecification, String> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList(parsePatterns));
//                cell.setComboBoxEditable(true);
//                cell.tableRowProperty().addListener((observable, oldValue, newValue) -> {
//                    log.info("row listener");
//                    if (newValue != null) {
//                        DataTableColumnSpecification columnSpecification = ((DataTableColumnSpecification)newValue.getItem());
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
//                return new ComboBoxTableCell<DataTableColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
//                    {
//                        setComboBoxEditable(true);
//                    }
//
//                    @Override
//                    public void updateItem(String item, boolean empty) {
//                        if (!empty) {
//                            DataTableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
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

        Optional<ObservableList<DataTableColumnSpecification>> result = dialog.showAndWait();

        if (result.isPresent()) {
            return new ArrayList<DataTableColumnSpecification>(result.get());
        }

        return null;
    }
}
