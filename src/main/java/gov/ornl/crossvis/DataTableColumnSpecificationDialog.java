package gov.ornl.crossvis;

import gov.ornl.datatable.IOUtilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;

public class DataTableColumnSpecificationDialog {
    private static final Logger log = Logger.getLogger(DataTableColumnSpecificationDialog.class.getName());

    private static String dateTimeFormatExampleString =
//        "BASIC_ISO_DATE: '20111203'\n" +
//        "ISO_DATE: '2011-12-03' or '2011-12-03+01:00'\n" +
        "ISO_DATE_TIME: '2011-12-03T10:15:30', '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/Paris]'\n" +
        "ISO_INSTANT: '2011-12-03T10:15:30Z'\n" +
//        "ISO_LOCAL_DATE: '2011-12-03'\n" +
        "ISO_LOCAL_DATE_TIME: '2011-12-03T10:15:30'\n" +
//        "ISO_LOCAL_TIME: '10:15' or '10:15:30'\n" +
//        "ISO_OFFSET_DATE: '2011-12-03+01:00'\n" +
        "ISO_OFFSET_DATE_TIME: '2011-12-03T10:15:30+01:00'\n" +
//        "ISO_OFFSET_TIME: '10:15+01:00' or '10:15:30+01:00'\n" +
//        "ISO_ORDINAL_DATE: '2012-337'\n" +
//        "ISO_TIME: '10:15', '10:15:30' or '10:15:30+01:00'\n" +
//        "ISO_WEEK_DATE: '2012-W48-6'\n" +
        "ISO_ZONED_DATE_TIME: '2011-12-03T10:15:30+01:00[Europe/Paris]'\n" +
        "RFC_1123_DATE_TIME: 'Tue, 3 Jun 2008 11:05:30 GMT'\n";

    public static ArrayList<DataTableColumnSpecification> getColumnSpecifications (File csvFile) throws IOException {
        String columnNames[] = IOUtilities.readCSVHeader(csvFile);

        ObservableList<DataTableColumnSpecification> tableColumnSpecs = FXCollections.observableArrayList();
        for (String columnName : columnNames) {
            DataTableColumnSpecification columnSpec = new DataTableColumnSpecification(columnName, "Double",
                    "ISO_LOCAL_DATE_TIME", false);
            tableColumnSpecs.add(columnSpec);
        }

        Dialog<ObservableList<DataTableColumnSpecification>> dialog = new Dialog<>();
        dialog.setTitle("CSV Column Specifications");
        dialog.setHeaderText("Specify Details for each Column");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField imageDirectoryTextField = new TextField();
        imageDirectoryTextField.setEditable(true);
        imageDirectoryTextField.setDisable(true);
        imageDirectoryTextField.textProperty().addListener(observable -> {
            for (DataTableColumnSpecification specification : tableColumnSpecs) {
                if (specification.getType().equalsIgnoreCase("Image Filename")) {
                    specification.setImageFileDirectoryPath(imageDirectoryTextField.getText());
                }
            }
        });

        Button chooseImageDirectoryButton = new Button("...");
        chooseImageDirectoryButton.setDisable(true);
        chooseImageDirectoryButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Image Directory");
            File directory = directoryChooser.showDialog(null);
            imageDirectoryTextField.setText(directory.getAbsolutePath());
        });

        Label imageDirectoryLabel = new Label("Image File Directory:");
        imageDirectoryLabel.setAlignment(Pos.BOTTOM_RIGHT);

        HBox imageDirectoryPane = new HBox();
        imageDirectoryPane.setSpacing(2.);
        imageDirectoryPane.getChildren().addAll(imageDirectoryLabel, imageDirectoryTextField,
                chooseImageDirectoryButton);
        HBox.setHgrow(imageDirectoryTextField, Priority.ALWAYS);
        imageDirectoryPane.setFillHeight(true);
        imageDirectoryPane.setAlignment(Pos.CENTER);

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
//        typeColumn.setCellFactory(column -> new ComboBoxTableCell<>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")));
        typeColumn.setCellFactory(new Callback<TableColumn<DataTableColumnSpecification, String>, TableCell<DataTableColumnSpecification, String>>() {
            @Override
            public TableCell<DataTableColumnSpecification, String> call(TableColumn<DataTableColumnSpecification, String> param) {
                return new ComboBoxTableCell<DataTableColumnSpecification, String>(FXCollections.observableArrayList("Double", "Temporal", "Categorical", "Image Filename")) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (!empty) {
                            DataTableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
                            if (columnSpecification.getType().equals("Temporal")) {
                                columnSpecification.setDateTimeFormatterID(columnSpecification.getDateTimeFormatterID());
                            } else {
                                columnSpecification.setDateTimeFormatterID("");
                            }

                            DataTableColumnSpecification currentImageFilenameColumn = null;
                            for (DataTableColumnSpecification specification : tableColumnSpecs) {
                                if (columnSpecification != specification &&
                                        specification.getType().equals("Image Filename")) {
                                    currentImageFilenameColumn = specification;
                                }
                            }

                            if (columnSpecification.getType().equals("Image Filename")) {
                                imageDirectoryTextField.setDisable(false);
                                chooseImageDirectoryButton.setDisable(false);

                                if (currentImageFilenameColumn != null) {
                                    currentImageFilenameColumn.setType("Double");
                                }
                            } else if (currentImageFilenameColumn == null) {
                                imageDirectoryTextField.setText("");
                                imageDirectoryTextField.setDisable(true);
                                chooseImageDirectoryButton.setDisable(true);
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
        });

        ArrayList<String> parsePatterns = new ArrayList<>();
        parsePatterns.add("");
//        parsePatterns.add("BASIC_ISO_DATE");
//        parsePatterns.add("ISO_DATE");
        parsePatterns.add("ISO_DATE_TIME");
        parsePatterns.add("ISO_INSTANT");
//        parsePatterns.add("ISO_LOCAL_DATE");
        parsePatterns.add("ISO_LOCAL_DATE_TIME");
//        parsePatterns.add("ISO_LOCAL_TIME");
//        parsePatterns.add("ISO_OFFSET_DATE");
        parsePatterns.add("ISO_OFFSET_DATE_TIME");
//        parsePatterns.add("ISO_OFFSET_TIME");
//        parsePatterns.add("ISO_ORDINAL_DATE");
//        parsePatterns.add("ISO_TIME");
//        parsePatterns.add("ISO_WEEK_DATE");
        parsePatterns.add("ISO_ZONED_DATE_TIME");
        parsePatterns.add("RFC_1123_DATE_TIME");

        TableColumn<DataTableColumnSpecification, String> parseColumn = new TableColumn<>("Date Time Formatter");
        parseColumn.setMinWidth(180);
        parseColumn.setCellValueFactory(new PropertyValueFactory<DataTableColumnSpecification, String>("dateTimeFormatterID"));
        parseColumn.setCellFactory(new Callback<TableColumn<DataTableColumnSpecification, String>, TableCell<DataTableColumnSpecification, String>>() {
            @Override
            public TableCell<DataTableColumnSpecification, String> call(TableColumn<DataTableColumnSpecification, String> param) {
                return new ComboBoxTableCell<DataTableColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
                    {
                        setComboBoxEditable(true);
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (!empty) {
                            DataTableColumnSpecification columnSpecification = getTableView().getItems().get(getIndex());
                            if (columnSpecification.getType().equals("Temporal")) {
                                setDisable(false);
                                setEditable(true);
                                this.setStyle("");
                                log.info("updating parse pattern for temporal column");
                            } else {
                                setDisable(true);
                                setEditable(false);
//                                this.getItems().add("N/A");
//                                columnSpecification.setDateTimeFormatterID("N/A");
                                this.setStyle("-fx-text-fill: grey;-fx-border-color: red");
                                this.setStyle("-fx-text-fill: grey;");
                                log.info("updating parse pattern for non-temporal column");
                            }
                        }
                        super.updateItem(item, empty);
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

        List<String> fileLines = IOUtilities.readCSVLines(csvFile, 0, 11);
        String fileLinesString = "";
        for (String line : fileLines) {
            fileLinesString += line + "\n";
        }

        guessColumnDataTypes(fileLines.subList(1, fileLines.size()), tableColumnSpecs);

        TextArea fileLinesTextArea = new TextArea();
        fileLinesTextArea.setText(fileLinesString);

        VBox columnSpecificationsTableBox = new VBox();
        columnSpecificationsTableBox.setSpacing(2.);
//        HBox columnSpecificationsHeaderBox = new HBox();
        Button dateTimeFormattingHelpButton = new Button("Show Date Time Format Examples...");
        dateTimeFormattingHelpButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Date Time Formatting Examples");
            alert.setHeaderText("Listed below are examples for date time formatting options:");
            alert.setContentText(dateTimeFormatExampleString);
            alert.initModality(Modality.NONE);
            dialog.setOnCloseRequest(closeEvent -> {
                if (alert.isShowing()) {
                    alert.close();
                }
            });
            alert.show();
        });
//        dateTimeFormattingHelpButton.setAlignment(Pos.BASELINE_RIGHT);
        Label columnSpecificationsLabel = new Label("Column Specifications: ");
        columnSpecificationsLabel.setAlignment(Pos.CENTER_LEFT);
        BorderPane columnSpecificationsHeaderPane = new BorderPane();
        columnSpecificationsHeaderPane.setCenter(columnSpecificationsLabel);
        columnSpecificationsHeaderPane.setRight(dateTimeFormattingHelpButton);
        BorderPane.setAlignment(columnSpecificationsLabel, Pos.CENTER_LEFT);
//        columnSpecificationsHeaderBox.getChildren().add(columnSpecificationsLabel);
//        columnSpecificationsHeaderBox.getChildren().add(dateTimeFormattingHelpButton);
//        HBox.setHgrow(columnSpecificationsLabel, Priority.ALWAYS);
//        HBox.setHgrow(dateTimeFormattingHelpButton, Priority.NEVER);
        columnSpecificationsTableBox.getChildren().addAll(columnSpecificationsHeaderPane, columnSpecificationTableView);

        VBox fileLinesBox = new VBox();
        fileLinesBox.setSpacing(2.);
//        fileLinesBox.setPadding(new Insets(6., 0., 0., 0.));
        fileLinesBox.getChildren().addAll(new Label("Listing of First 10 Lines:"), fileLinesTextArea);

        VBox bottomBox = new VBox();
        bottomBox.setSpacing(2.);
        bottomBox.setPadding(new Insets(6., 0., 0., 0.));
        bottomBox.getChildren().addAll(imageDirectoryPane, fileLinesBox);
        bottomBox.setFillWidth(true);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(columnSpecificationsTableBox);
        borderPane.setBottom(bottomBox);

        dialog.getDialogPane().setContent(borderPane);

        Platform.runLater(() -> columnSpecificationTableView.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return tableColumnSpecs;
            }
            return null;
        });

        dialog.setOnCloseRequest(event -> {
            for (DataTableColumnSpecification spec : tableColumnSpecs) {
                if (spec.getType().equals("Image Filename")) {
                    if (spec.getImageFileDirectoryPath().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setHeaderText("Image File Directory");
                        alert.setHeaderText(null);
                        alert.setContentText("An image file directory was not set.  If you proceed, the image" +
                                " filenames must include the full system path to the image file.  " +
                                "Would you like to set the directory now?");
                        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == ButtonType.YES){
                            DirectoryChooser directoryChooser = new DirectoryChooser();
                            directoryChooser.setTitle("Select Image Directory");
                            File directory = directoryChooser.showDialog(null);
                            spec.setImageFileDirectoryPath(directory.getAbsolutePath());
                        }
                    }
                }
            }
        });

        Optional<ObservableList<DataTableColumnSpecification>> result = dialog.showAndWait();

        if (result.isPresent()) {
            return new ArrayList<>(result.get());
        }

        return null;
    }

    private static void guessColumnDataTypes(List<String> fileLines, List<DataTableColumnSpecification> tableColumnSpecs ) {
        for (int i = 0; i < tableColumnSpecs.size(); i++) {
            boolean isNumber = true;
            DataTableColumnSpecification columnSpecification = tableColumnSpecs.get(i);
            for (String line : fileLines) {
                String tokens[] = line.split(",");
                if (!stringIsNumber(tokens[i])) {
                    isNumber = false;
                    break;
                }
            }

            if (isNumber) {
                columnSpecification.setType("Double");
            } else {
                if ((columnSpecification.getName().contains("image") ||
                    columnSpecification.getName().contains("Image")) &&
                        (columnSpecification.getName().contains("file") ||
                                columnSpecification.getName().contains("File"))) {
                    columnSpecification.setType("Image Filename");
                } else if (columnSpecification.getName().contains("date") ||
                        columnSpecification.getName().contains("Date") ||
                        columnSpecification.getName().contains("time") ||
                        columnSpecification.getName().contains("Time")) {
                    String columnExample = fileLines.get(0).split(",")[tableColumnSpecs.indexOf(columnSpecification)].trim();
                    boolean foundDateTimeFormatter = false;
                    for (Map.Entry<String, DateTimeFormatter> mapEntry : columnSpecification.getDateTimeFormatterMap().entrySet()) {
                        try {
                            mapEntry.getValue().parse(columnExample);
                            columnSpecification.setType("Temporal");
                            columnSpecification.setDateTimeFormatterID(mapEntry.getKey());
                            foundDateTimeFormatter = true;
                            break;
                        } catch (DateTimeParseException ex) {

                        }
                    }
                    if (!foundDateTimeFormatter) {
                        columnSpecification.setType("Categorical");
                    }
                } else {
                    columnSpecification.setType("Categorical");
                }
            }
        }
    }

    private static boolean stringIsNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
