package gov.ornl.csed.cda.edenfx;

import gov.ornl.csed.cda.Falcon.FalconPreferenceKeys;
import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.pcpview.PCPView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Created by csg on 8/29/16.
 */
public class EDENFXMain extends Application {
    private static final Logger log = LoggerFactory.getLogger(EDENFXMain.class);

    private PCPView pcpView;
    private DataModel dataModel;

    private HashMap<String, PCPView.DISPLAY_MODE> displayModeMap;
    private Preferences preferences;
    private Menu displayModeMenu;

    private ScrollPane pcpScrollPane;
    private TabPane tabPane;
    private Menu axisLayoutMenu;
    private CheckMenuItem fitPCPAxesToWidthCheckMI;
    private MenuItem changeAxisSpacingMI;

    private TableView<Column> columnTableView;
    private TableView<Tuple> dataTableView;

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());

        displayModeMap = new HashMap<>();
        displayModeMap.put("Histograms", PCPView.DISPLAY_MODE.HISTOGRAM);
        displayModeMap.put("Parallel Coordinates Bins", PCPView.DISPLAY_MODE.PCP_BINS);
        displayModeMap.put("Parallel Coordinates Lines", PCPView.DISPLAY_MODE.PCP_LINES);

        dataModel = new DataModel();
    }

    private void createColumnTableView() {
        columnTableView = new TableView<>();
        columnTableView.setEditable(true);
        columnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                log.debug("Column '" + newValue.getName() + "' selected in column table");
                dataModel.setHighlightedColumn(newValue);
            } else {
                log.debug("SelectedItemProperty changed and new value is null");
            }
        });

        TableColumn<Column, String> nameColumn = new TableColumn<>("Variable Name");
        nameColumn.setMinWidth(140);
        nameColumn.setCellValueFactory(new PropertyValueFactory<Column, String>("name"));

        TableColumn<Column, Boolean> enabledColumn = new TableColumn<>("Visible");
        enabledColumn.setMinWidth(20);
        enabledColumn.setCellValueFactory(new PropertyValueFactory<Column, Boolean>("enabled"));
//        enabledColumn.setCellFactory(column -> new CheckBoxTableCell());
        enabledColumn.setCellFactory(new Callback<TableColumn<Column, Boolean>, TableCell<Column, Boolean>>() {
            @Override
            public TableCell<Column, Boolean> call(TableColumn<Column, Boolean> param) {
                return new CheckBoxTableCell<Column, Boolean>() {
                    {
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    public void updateItem(Boolean item, boolean empty) {
                        if (!empty) {
                            TableRow row = getTableRow();

                            if (row != null) {
                                int rowNumber = row.getIndex();
                                TableView.TableViewSelectionModel sm = getTableView().getSelectionModel();

                                if (item) {
                                    // enable a disabled column
                                    // get the column name; lookup column in data model; enable the column
//                                    sm.select(rowNumber);
//                                    log.debug("Would set column " + dataModel.getColumn(rowNumber) + " to enabled");

//                                    dataModel.enableColumn(dataModel.getColumn(rowNumber));
                                    Column column = columnTableView.getItems().get(rowNumber);
                                    dataModel.enableColumn(column);
                                    log.debug("Set column '" + column.getName() + "' to enabled");
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = columnTableView.getItems().get(rowNumber);
                                    dataModel.disableColumn(column);
                                    log.debug("Set column '" + column.getName() + "' to disabled");
//                                    sm.clearSelection(rowNumber);
//                                    log.debug("Would set column " + dataModel.getColumn(rowNumber) + " to disabled");
//                                    dataModel.disableColumn(dataModel.getColumn(rowNumber));
                                }
                            }
                        }

                        super.updateItem(item, empty);
                    }
                };
            }
        });
        enabledColumn.setEditable(true);

        TableColumn < Column, Double > minColumn = new TableColumn<>("Min");
        minColumn.setCellValueFactory(new PropertyValueFactory<Column, Double>("minValue"));

        TableColumn<Column, Double> maxColumn = new TableColumn<>("Max");
        maxColumn.setCellValueFactory(new PropertyValueFactory<Column, Double>("maxValue"));

        TableColumn<Column, Double> stdevColumn = new TableColumn<>("St. Dev.");
        stdevColumn.setCellValueFactory(new PropertyValueFactory<Column, Double>("standardDeviationValue"));

        TableColumn<Column, Double> meanColumn = new TableColumn<>("Mean");
        meanColumn.setCellValueFactory(new PropertyValueFactory<Column, Double>("meanValue"));


        columnTableView.getColumns().addAll(enabledColumn, nameColumn, minColumn, maxColumn, meanColumn, stdevColumn);

    }

    @Override
    public void start(Stage stage) throws Exception {
        pcpView = new PCPView();
        pcpView.setDataModel(dataModel);
        pcpView.setPrefHeight(400);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpScrollPane = new ScrollPane(pcpView);
        pcpScrollPane.setFitToHeight(true);
        pcpScrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());

        MenuBar menuBar = createMenuBar(stage);
//        menuBar.setUseSystemMenuBar(true);
        createColumnTableView();
        dataTableView = new TableView<>();

        tabPane = new TabPane();
        Tab columnTableTab = new Tab(" Column Table ");
        columnTableTab.setClosable(false);
        columnTableTab.setContent(columnTableView);

        Tab dataTableTab = new Tab(" Data Table ");
        dataTableTab.setClosable(false);
        dataTableTab.setContent(dataTableView);
        tabPane.getTabs().addAll(columnTableTab, dataTableTab);

        SplitPane middleSplit = new SplitPane();
        middleSplit.setOrientation(Orientation.VERTICAL);
        middleSplit.getItems().addAll(pcpScrollPane, tabPane);
        middleSplit.setResizableWithParent(tabPane, false);
        middleSplit.setDividerPositions(0.7);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(middleSplit);
        rootNode.setTop(menuBar);
//        rootNode.setLeft(settingsPane);

        Scene scene = new Scene(rootNode, 1000, 500, true, SceneAntialiasing.BALANCED);

        stage.setTitle("EDEN.FX Alpha Version");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String args[]) {
        launch(args);
    }

    private void changeAxisSpacing() {
        Dialog<Integer> axisSpacingDialog = new Dialog<>();
        axisSpacingDialog.setTitle("Axis Spacing");
        axisSpacingDialog.setHeaderText("Set the pixel spacing between axes");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 800, pcpView.getAxisSpacing(), 1));
        spinner.setEditable(true);

        grid.add(new Label("Axis Spacing: "), 0, 0);
        grid.add(spinner, 1, 0);

        axisSpacingDialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> spinner.requestFocus());

        axisSpacingDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL);

        final Button buttonApply = (Button)axisSpacingDialog.getDialogPane().lookupButton(ButtonType.APPLY);
        buttonApply.setDisable(true);
        buttonApply.addEventFilter(ActionEvent.ACTION, event -> {
            int axisSpacing = spinner.getValue();
            pcpView.setAxisSpacing(axisSpacing);
            event.consume();
        });

        final Button buttonOK = (Button)axisSpacingDialog.getDialogPane().lookupButton(ButtonType.OK);
        buttonOK.setDisable(true);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getValue() != pcpView.getAxisSpacing()) {
                buttonApply.setDisable(false);
                buttonOK.setDisable(false);
            }
        });

        axisSpacingDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return spinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = axisSpacingDialog.showAndWait();

        result.ifPresent(axisSpacing -> {
            System.out.println("new axis spacing is " + axisSpacing);
            pcpView.setAxisSpacing(axisSpacing);
        });
    }

    private MenuBar createMenuBar (Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu edenfxMenu = new Menu("EDEN.FX");
        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");
        menuBar.getMenus().addAll(edenfxMenu, fileMenu, viewMenu);

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCSVMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                String lastCSVDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
                if (!lastCSVDirectoryPath.isEmpty()) {
                    fileChooser.setInitialDirectory(new File(lastCSVDirectoryPath));
                }
                fileChooser.setTitle("Open CSV File");
                File csvFile = fileChooser.showOpenDialog(stage);
                if (csvFile != null) {
                    try {
                        openCSVFile(csvFile);
                        displayModeMenu.setDisable(false);
                        fitPCPAxesToWidthCheckMI.setDisable(false);
                        changeAxisSpacingMI.setDisable(pcpView.getFitAxisSpacingToWidthEnabled());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        displayModeMenu = new Menu("Display Mode");
        displayModeMenu.setDisable(true);
        final ToggleGroup displayModeGroup = new ToggleGroup();
        RadioMenuItem item = new RadioMenuItem("Histograms");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.HISTOGRAM) {
            item.setSelected(true);
        }
        item = new RadioMenuItem("Parallel Coordinates Bins");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_BINS) {
            item.setSelected(true);
        }
        item = new RadioMenuItem("Parallel Coordinates Lines");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_LINES) {
            item.setSelected(true);
        }
        viewMenu.getItems().add(displayModeMenu);

        axisLayoutMenu = new Menu("Axis Layout");
        fitPCPAxesToWidthCheckMI = new CheckMenuItem("Fit Axis Spacing to Width");
        fitPCPAxesToWidthCheckMI.setSelected(pcpView.getFitAxisSpacingToWidthEnabled());
        fitPCPAxesToWidthCheckMI.setDisable(true);
        fitPCPAxesToWidthCheckMI.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pcpView.setFitAxisSpacingToWidthEnabled(fitPCPAxesToWidthCheckMI.isSelected());
            if (pcpView.getFitAxisSpacingToWidthEnabled()) {
                changeAxisSpacingMI.setDisable(true);
                pcpScrollPane.setFitToWidth(true);
            } else {
                changeAxisSpacingMI.setDisable(false);
                pcpScrollPane.setFitToWidth(false);
            }
        });

        changeAxisSpacingMI = new MenuItem("Change Axis Spacing...");
        changeAxisSpacingMI.setDisable(true);
        changeAxisSpacingMI.setOnAction(event -> {
            changeAxisSpacing();
        });

        axisLayoutMenu.getItems().addAll(fitPCPAxesToWidthCheckMI, changeAxisSpacingMI);
        viewMenu.getItems().add(axisLayoutMenu);

        displayModeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                    PCPView.DISPLAY_MODE newDisplayMode = displayModeMap.get(toggleItem.getText());
                    pcpView.setDisplayMode(newDisplayMode);
                }
            }
        });


        MenuItem exitMI = new MenuItem("Quit Falcon");
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.close();
            }
        });

        edenfxMenu.getItems().addAll(exitMI);
        fileMenu.getItems().addAll(openCSVMI);

        return menuBar;
    }

    private void openCSVFile(File f) throws IOException {
        if (dataModel != null) {
            // TODO: Clear the data model from the PCPView
            dataModel.clear();
        }

        IOUtilities.readCSV(f, dataModel);

        columnTableView.getItems().clear();
        columnTableView.setItems(FXCollections.observableArrayList(dataModel.getColumns()));

//        dataTableView.getItems().clear();
        setDataTableColumns();
        setDataTableItems();

        dataModel.addDataModelListener(new DataModelListener() {
            @Override
            public void dataModelChanged(DataModel dataModel) {

            }

            @Override
            public void queryChanged(DataModel dataModel) {
                setDataTableItems();
            }

            @Override
            public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {

            }

            @Override
            public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {

            }

            @Override
            public void highlightedColumnChanged(DataModel dataModel) {

            }

            @Override
            public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {

            }

            @Override
            public void columnDisabled(DataModel dataModel, Column disabledColumn) {
                for (int i = 0; i < dataTableView.getColumns().size(); i++) {
                    if (dataTableView.getColumns().get(i).getText().equals(disabledColumn.getName())) {
                        dataTableView.getItems().clear();
                        dataTableView.getColumns().remove(i);


                        setDataTableItems();
                        break;
                    }
                }
            }

            @Override
            public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

            }

            @Override
            public void columnEnabled(DataModel dataModel, Column enabledColumn) {
                int columnIndex = dataModel.getColumnIndex(enabledColumn);
                TableColumn<Tuple, Double> tableColumn = new TableColumn<>(enabledColumn.getName());
                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Double>, ObservableValue<Double>>() {
                    public ObservableValue<Double> call(TableColumn.CellDataFeatures<Tuple, Double> t) {
                        return new ReadOnlyObjectWrapper(t.getValue().getElement(columnIndex));
                    }
                });
                log.debug("Adding column to column table at index " + columnIndex);
                dataTableView.getColumns().add(columnIndex, tableColumn);

                setDataTableItems();
            }
        });

        displayModeMenu.setDisable(false);
    }

    private void setDataTableItems() {
        ObservableList<Tuple> tableTuples;
        dataTableView.getItems().clear();

        if (dataModel.getActiveQuery().hasColumnSelections()) {
            tableTuples = FXCollections.observableArrayList(dataModel.getActiveQuery().getTuples());
        } else {
            tableTuples = FXCollections.observableArrayList(dataModel.getTuples());
        }

        dataTableView.setItems(tableTuples);
    }

    private void setDataTableColumns() {
        dataTableView.getColumns().clear();

        // make a column for each enabled data table column
        if (!dataModel.isEmpty()) {
            for (int icol = 0; icol < dataModel.getColumnCount(); icol++) {
                Column column = dataModel.getColumn(icol);
//                int columnIndex = icol;
                TableColumn<Tuple, Double> tableColumn = new TableColumn<>(column.getName());
                tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Double>, ObservableValue<Double>>() {
                    public ObservableValue<Double> call(TableColumn.CellDataFeatures<Tuple, Double> t) {
                        int columnIndex = dataModel.getColumnIndex(column);
                        return new ReadOnlyObjectWrapper(t.getValue().getElement(columnIndex));
                    }
                });
                dataTableView.getColumns().add(tableColumn);
            }
        }
    }
}
