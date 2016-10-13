package gov.ornl.csed.cda.edenfx;

import gov.ornl.csed.cda.Falcon.FalconPreferenceKeys;
import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.pcpview.PCPAxis;
import gov.ornl.csed.cda.pcpview.PCPAxisSelection;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Created by csg on 8/29/16.
 */
public class EDENFXMain extends Application implements DataModelListener {
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
    private TableView<ColumnSelectionRange> queryTableView;
    private TableView<Tuple> dataTableView;
    private MenuItem removeAllQueriesMI;

    // toggle group for toolbar buttons to change display mode of PCPView
    private ToggleGroup displayModeMenuGroup;
    private RadioMenuItem summaryDisplayModeMenuItem;
    private RadioMenuItem histogramDisplayModeMenuItem;
    private RadioMenuItem binDisplayModeMenuItem;
    private RadioMenuItem lineDisplayModeMenuItem;
//    private HashMap<PCPView.DISPLAY_MODE, ToggleButton> displayModeButtonMap;

    private ToggleGroup displayModeButtonGroup;
//    private HashMap<PCPView.DISPLAY_MODE, RadioMenuItem> displayModeMenuItemMap;
    private ToggleButton summaryDisplayModeButton;
    private ToggleButton histogramDisplayModeButton;
    private ToggleButton binDisplayModeButton;
    private ToggleButton lineDisplayModeButton;

    private ProgressBar percentSelectedProgress;
    private DecimalFormat decimalFormat;
    private StatusBar statusBar;

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());

        decimalFormat = new DecimalFormat("###.0%");
        displayModeMap = new HashMap<>();
        displayModeMap.put("Histograms", PCPView.DISPLAY_MODE.HISTOGRAM);
        displayModeMap.put("Parallel Coordinates Bins", PCPView.DISPLAY_MODE.PCP_BINS);
        displayModeMap.put("Parallel Coordinates Lines", PCPView.DISPLAY_MODE.PCP_LINES);

        dataModel = new DataModel();
        dataModel.addDataModelListener(this);
    }

    private void createQueryTableView() {
        queryTableView = new TableView();
        queryTableView.setEditable(true);

        TableColumn<ColumnSelectionRange, String> columnNameColumn = new TableColumn<>("Column");
        columnNameColumn.setMinWidth(160);
        columnNameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));

        TableColumn<ColumnSelectionRange, Number> minColumn = new TableColumn<>("Minimum Value");
        minColumn.setMinWidth(200);
        minColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, Number>("minValue"));
        minColumn.setCellFactory(TextFieldTableCell.<ColumnSelectionRange, Number>forTableColumn(new NumberStringConverter()));
        minColumn.setEditable(true);

        TableColumn<ColumnSelectionRange, Number> maxColumn = new TableColumn<>("Maximum Value");
        maxColumn.setMinWidth(200);
        maxColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, Number>("maxValue"));
        maxColumn.setCellFactory(TextFieldTableCell.<ColumnSelectionRange, Number>forTableColumn(new NumberStringConverter()));
        maxColumn.setEditable(true);

        queryTableView.getColumns().addAll(columnNameColumn, minColumn, maxColumn);
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

//        TableColumn<ColumnSelectionRange, Number> minColumn = new TableColumn<>("Minimum Value");
//        minColumn.setMinWidth(200);
//        minColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, Number>("minValue"));
//        minColumn.setCellFactory(TextFieldTableCell.<ColumnSelectionRange, Number>forTableColumn(new NumberStringConverter()));
//        minColumn.setEditable(true);

        TableColumn<Column, String> nameColumn = new TableColumn<>("Variable Name");
        nameColumn.setMinWidth(180);
        nameColumn.setCellValueFactory(new PropertyValueFactory<Column, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(true);

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

    private ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();

        // Make toggle button group for display mode shortcut buttons
//        displayModeButtonMap = new HashMap<>();
        summaryDisplayModeButton = new ToggleButton("S");
        summaryDisplayModeButton.setTooltip(new Tooltip("Summary Display Mode"));
//        displayModeButtonMap.put(PCPView.DISPLAY_MODE.HISTOGRAM, summaryDisplayModeButton);
        histogramDisplayModeButton = new ToggleButton("H");
        histogramDisplayModeButton.setTooltip(new Tooltip("Histogram Display Mode"));
//        displayModeButtonMap.put(PCPView.DISPLAY_MODE.HISTOGRAM, histogramDisplayModeButton);
        binDisplayModeButton = new ToggleButton("B");
        binDisplayModeButton.setTooltip(new Tooltip("Binned Parallel Coordinates Display Mode"));
//        displayModeButtonMap.put(PCPView.DISPLAY_MODE.PCP_BINS, binDisplayModeButton);
        lineDisplayModeButton = new ToggleButton("L");
        lineDisplayModeButton.setTooltip(new Tooltip("Parallel Coordinates Line Display Mode"));
//        displayModeButtonMap.put(PCPView.DISPLAY_MODE.PCP_LINES, lineDisplayModeButton);
        displayModeButtonGroup = new ToggleGroup();
        displayModeButtonGroup.getToggles().addAll(summaryDisplayModeButton, histogramDisplayModeButton, binDisplayModeButton, lineDisplayModeButton);

        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.HISTOGRAM) {
            displayModeButtonGroup.selectToggle(histogramDisplayModeButton);
        } else if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_BINS) {
            displayModeButtonGroup.selectToggle(binDisplayModeButton);
        } else if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_LINES) {
            displayModeButtonGroup.selectToggle(lineDisplayModeButton);
        }

        displayModeButtonGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("Button DisplayMode Group changed");
            if (newValue != null) {
                if (newValue == summaryDisplayModeButton) {
                    pcpView.setDisplayMode(PCPView.DISPLAY_MODE.HISTOGRAM);
                    displayModeMenuGroup.selectToggle(summaryDisplayModeMenuItem);
                } else if (newValue == histogramDisplayModeButton) {
                    pcpView.setDisplayMode(PCPView.DISPLAY_MODE.HISTOGRAM);
                    displayModeMenuGroup.selectToggle(histogramDisplayModeMenuItem);
                } else if (newValue == binDisplayModeButton) {
                    pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_BINS);
                    displayModeMenuGroup.selectToggle(binDisplayModeMenuItem);
                } else if (newValue == lineDisplayModeButton) {
                    pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_LINES);
                    displayModeMenuGroup.selectToggle(lineDisplayModeMenuItem);
                }

//                pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_LINES);

//                displayModeMenuGroup.selectToggle(displayModeMenuItemMap.get(newDisplayMode));
            }
        });

        // make toggle buttons for showing/hiding selected/unselected items
        ToggleButton showUnselectedButton = new ToggleButton("U");
        showUnselectedButton.setTooltip(new Tooltip("Show Unselected Items"));
        showUnselectedButton.selectedProperty().bindBidirectional(pcpView.showUnselectedItems());
        ToggleButton showSelectedButton = new ToggleButton("S");
        showSelectedButton.selectedProperty().bindBidirectional(pcpView.showSelectedItems());
        showSelectedButton.setTooltip(new Tooltip("Show Selected Items"));

        // create selected items color modification UI components
        HBox selectedItemsColorBox = new HBox();
        selectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker selectedItemsColorPicker = new ColorPicker();
        selectedItemsColorPicker.valueProperty().bindBidirectional(pcpView.selectedItemsColor());
        selectedItemsColorPicker.getStyleClass().add("button");
        selectedItemsColorBox.getChildren().addAll(new Label(" Selected Color: "), selectedItemsColorPicker);

        // create unselected items color modification UI components
        HBox unselectedItemsColorBox = new HBox();
        unselectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker unselectedItemsColorPicker = new ColorPicker();
        unselectedItemsColorPicker.getStyleClass().add("button");
        unselectedItemsColorPicker.valueProperty().bindBidirectional(pcpView.unselectedItemsColor());
        unselectedItemsColorBox.getChildren().addAll(new Label(" Unselected Color: "), unselectedItemsColorPicker);

        // add all items to layout
        toolBar.getItems().addAll(histogramDisplayModeButton, binDisplayModeButton, lineDisplayModeButton, new Separator(),
                showUnselectedButton, showSelectedButton, new Separator(), selectedItemsColorBox, unselectedItemsColorBox);

        return toolBar;
    }

    private void createStatusBar() {
        statusBar = new StatusBar();

        percentSelectedProgress = new ProgressBar();
        percentSelectedProgress.setProgress(0.0);
        percentSelectedProgress.setTooltip(new Tooltip("Selected Tuples Percentage"));

        statusBar.getLeftItems().add(percentSelectedProgress);

        updatePercentSelected();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setOnShown(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open CSV File");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    openCSVFile(file);
                    displayModeMenu.setDisable(false);
                    fitPCPAxesToWidthCheckMI.setDisable(false);
                    changeAxisSpacingMI.setDisable(pcpView.getFitAxisSpacingToWidthEnabled());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        pcpView = new PCPView();
        pcpView.setDataModel(dataModel);
        pcpView.setPrefHeight(400);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpScrollPane = new ScrollPane(pcpView);
        pcpScrollPane.setFitToHeight(true);
        pcpScrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());

        ToolBar toolBar = createToolBar(stage);

        createStatusBar();

        MenuBar menuBar = createMenuBar(stage);
//        menuBar.setUseSystemMenuBar(true);
        createColumnTableView();
        createQueryTableView();
        dataTableView = new TableView<>();

        // create table tab pane
        tabPane = new TabPane();
        Tab columnTableTab = new Tab(" Column Table ");
        columnTableTab.setClosable(false);
        columnTableTab.setContent(columnTableView);

        Tab dataTableTab = new Tab(" Data Table ");
        dataTableTab.setClosable(false);
        dataTableTab.setContent(dataTableView);

        Tab queryTableTab = new Tab(" Query Table ");
        queryTableTab.setClosable(false);
        queryTableTab.setContent(queryTableView);

        tabPane.getTabs().addAll(columnTableTab, dataTableTab, queryTableTab);

        SplitPane middleSplit = new SplitPane();
        middleSplit.setOrientation(Orientation.VERTICAL);
        middleSplit.getItems().addAll(pcpScrollPane, tabPane);
        middleSplit.setResizableWithParent(pcpScrollPane, false);
        middleSplit.setDividerPositions(0.7);

        VBox topContainer = new VBox();
        topContainer.getChildren().add(menuBar);
        topContainer.getChildren().add(toolBar);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(middleSplit);
        rootNode.setTop(topContainer);
        rootNode.setBottom(statusBar);
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
        displayModeMenuGroup = new ToggleGroup();
//        displayModeMenuItemMap = new HashMap<>();

        histogramDisplayModeMenuItem = new RadioMenuItem("Histograms");
        histogramDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
//        displayModeMenu.getItems().add(item);
//        displayModeMenuItemMap.put(PCPView.DISPLAY_MODE.HISTOGRAM, item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.HISTOGRAM) {
            histogramDisplayModeMenuItem.setSelected(true);
        }
        binDisplayModeMenuItem = new RadioMenuItem("Parallel Coordinates Bins");
        binDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
//        displayModeMenu.getItems().add(item);
//        displayModeMenuItemMap.put(PCPView.DISPLAY_MODE.PCP_BINS, item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_BINS) {
            binDisplayModeMenuItem.setSelected(true);
        }
        lineDisplayModeMenuItem = new RadioMenuItem("Parallel Coordinates Lines");
        lineDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
//        displayModeMenu.getItems().add(item);
//        displayModeMenuItemMap.put(PCPView.DISPLAY_MODE.PCP_LINES, item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_LINES) {
            lineDisplayModeMenuItem.setSelected(true);
        }
        displayModeMenu.getItems().addAll(histogramDisplayModeMenuItem, binDisplayModeMenuItem, lineDisplayModeMenuItem);
        viewMenu.getItems().add(displayModeMenu);

        // create listener for display mode menu group
        displayModeMenuGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                log.debug("Menu Display Mode group changed");
                if (newValue != null) {
                    RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                    PCPView.DISPLAY_MODE newDisplayMode = displayModeMap.get(toggleItem.getText());
                    if (newDisplayMode == PCPView.DISPLAY_MODE.HISTOGRAM) {
                        displayModeButtonGroup.selectToggle(histogramDisplayModeButton);
                    } else if (newDisplayMode == PCPView.DISPLAY_MODE.PCP_BINS) {
                        displayModeButtonGroup.selectToggle(binDisplayModeButton);
                    } else if (newDisplayMode == PCPView.DISPLAY_MODE.PCP_LINES) {
                        displayModeButtonGroup.selectToggle(lineDisplayModeButton);
                    }

                    pcpView.setDisplayMode(newDisplayMode);
                }
            }
        });

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

        // create menu item to remove all active queries
        removeAllQueriesMI = new MenuItem("Remove All Range Queries");
        removeAllQueriesMI.setDisable(true);
        removeAllQueriesMI.setOnAction(event -> {
            pcpView.clearQuery();
        });
        viewMenu.getItems().add(removeAllQueriesMI);


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

    private void updatePercentSelected() {
        if (dataModel != null && !dataModel.isEmpty()) {
            double percentSelected = (double) dataModel.getQueriedTupleCount() / dataModel.getTupleCount();
            percentSelectedProgress.setProgress(percentSelected);
            statusBar.setText(" " + dataModel.getQueriedTupleCount() + " of " + dataModel.getTupleCount() + " tuples selected (" + decimalFormat.format(percentSelected) + ")");
        } else {
            statusBar.setText(" Ready ");
        }
    }

    private void openCSVFile(File f) throws IOException {
        if (dataModel != null) {
            // TODO: Clear the data model from the PCPView
            dataModel.clear();
        }

        IOUtilities.readCSV(f, dataModel);

        columnTableView.getItems().clear();
        columnTableView.setItems(FXCollections.observableArrayList(dataModel.getColumns()));

        queryTableView.getItems().clear();
        queryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangeList());
        
//        dataTableView.getItems().clear();
        setDataTableColumns();
        setDataTableItems();

     /*   dataModel.addDataModelListener(new DataModelListener() {
            @Override
            public void dataModelChanged(DataModel dataModel) {
                updatePercentSelected();
            }

            @Override
            public void queryChanged(DataModel dataModel) {
                log.debug("EDENFXMain queryChanged: " + dataModel.getActiveQuery().hasColumnSelections());
                removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
                setDataTableItems();

                updatePercentSelected();
//                double percentSelected = (double)dataModel.getQueriedTupleCount() / dataModel.getTupleCount();
//                percentSelectedProgress.setProgress(percentSelected);
//                statusBar.setText(" " + dataModel.getQueriedTupleCount() + " of " + dataModel.getTupleCount() + " tuples selected (" + decimalFormat.format(percentSelected) + ")");
//                if (dataModel.getActiveQuery().hasColumnSelections()) {
//                    queryTableView.setItems(FXCollections.observableArrayList(dataModel.getActiveQuery().getAllColumnSelectionRanges()));
//                } else {
//                    queryTableView.getItems().clear();
//                }
            }

            @Override
            public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
                setDataTableItems();
                updatePercentSelected();
//                if (dataModel.getActiveQuery().hasColumnSelections()) {
//                    queryTableView.setItems(FXCollections.observableArrayList(dataModel.getActiveQuery().getAllColumnSelectionRanges()));
//                } else {
//                    queryTableView.getItems().clear();
//                }
            }

            @Override
            public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
                setDataTableItems();
                updatePercentSelected();
//                if (dataModel.getActiveQuery().hasColumnSelections()) {
//                    queryTableView.setItems(FXCollections.observableArrayList(dataModel.getActiveQuery().getAllColumnSelectionRanges()));
//                } else {
//                    queryTableView.getItems().clear();
//                }
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
*/
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

    @Override
    public void dataModelReset(DataModel dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelQueryCleared(DataModel dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelQueryColumnCleared(DataModel dataModel, Column column) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
//        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataModel dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        if (newHighlightedColumn != null) {
            columnTableView.getSelectionModel().select(newHighlightedColumn);
        } else {
            columnTableView.getSelectionModel().clearSelection();
        }
    }

    @Override
    public void dataModelTuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
        updatePercentSelected();
    }

    @Override
    public void dataModelTuplesRemoved(DataModel dataModel, int numTuplesRemoved) {
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnDisabled(DataModel dataModel, Column disabledColumn) {

    }

    @Override
    public void dataModelColumnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataModel dataModel, Column enabledColumn) {

    }

    @Override
    public void dataModelColumnOrderChanged(DataModel dataModel) {

    }

    @Override
    public void dataModelColumnNameChanged(DataModel dataModel, Column column) {

    }
}
