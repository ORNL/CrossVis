package gov.ornl.crossvis;

import gov.ornl.correlationview.CorrelationMatrixView;
import gov.ornl.datatable.*;
import gov.ornl.datatableview.DataTableView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.StatusBar;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CrossVis extends Application implements DataTableListener {
    private static final Logger log = Logger.getLogger(CrossVis.class.getName());

    private static final String VERSION_STRING = "v2.1.2";

//    private PCPView pcpView;
    private CorrelationMatrixView correlationMatrixView;
    private DataTable dataTable;
    private DataTableView dataTableView;

    private Preferences preferences;

    private ScrollPane dataTableViewScrollPane;
    private TabPane tabPane;

    private TableView<DoubleColumn> doubleColumnTableView;
    private TableView<TemporalColumn> temporalColumnTableView;
    private TableView<CategoricalColumn> categoricalColumnTableView;

    private TableView<ColumnSelection> queryTableView = new TableView<>();
    private TableView<ColumnSelection> doubleQueryTableView;
    private TableView<ColumnSelection> temporalQueryTableView;
    private TableView<ColumnSelection> categoricalQueryTableView;

    private TableView<Tuple> tupleTableView;

    private ProgressBar percentSelectedProgress;
    private DecimalFormat decimalFormat;
    private StatusBar statusBar;

    private MenuItem removeSelectedDataMenuItem;
    private MenuItem removeUnselectedDataMenuItem;
    private MenuItem exportSelectedDataMenuItem;
    private MenuItem exportUnselectedDataMenuItem;
    private CheckMenuItem enableDataTableUpdatesCheckMenuItem;
    private MenuItem removeAllQueriesMI;

    private Stage crossVisStage;

    private NetCDFFilterWindow ncFilterWindow = null;
    private Stage ncFilterWindowStage = null;

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());

        decimalFormat = new DecimalFormat("##0.0%");

        dataTable = new DataTable();
        dataTable.addDataModelListener(this);
    }

    private void createColumnTableViews() {
        // create dataframe view for categorical columns
        categoricalColumnTableView = new TableView<>();
        categoricalColumnTableView.setEditable(true);
        categoricalColumnTableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataTable.setHighlightedColumn(newValue);
            }
        }));

        TableColumn<CategoricalColumn, String> categoricalNameColumn = new TableColumn<>("Variable Name");
        categoricalNameColumn.setMinWidth(180);
        categoricalNameColumn.setCellValueFactory(new PropertyValueFactory<CategoricalColumn, String>("name"));
        categoricalNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        categoricalNameColumn.setEditable(true);

        TableColumn<CategoricalColumn, Boolean> categoricalEnabledColumn = new TableColumn<>("Visible");
        categoricalEnabledColumn.setMinWidth(20);
        categoricalEnabledColumn.setCellValueFactory(new PropertyValueFactory<CategoricalColumn, Boolean>("enabled"));
        categoricalEnabledColumn.setCellFactory(new Callback<TableColumn<CategoricalColumn, Boolean>, TableCell<CategoricalColumn, Boolean>>() {
            @Override
            public TableCell<CategoricalColumn, Boolean> call(TableColumn<CategoricalColumn, Boolean> param) {
                return new CheckBoxTableCell<CategoricalColumn, Boolean>() {
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
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataTable.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataTable.disableColumn(column);
                                }
                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });
        categoricalEnabledColumn.setEditable(true);

        categoricalColumnTableView.getColumns().addAll(categoricalEnabledColumn, categoricalNameColumn);

        // create dataframe view for temporal columns
        temporalColumnTableView = new TableView<>();
        temporalColumnTableView.setEditable(true);
        temporalColumnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataTable.setHighlightedColumn(newValue);
            }
        });

        TableColumn<TemporalColumn, String> temporalNameColumn = new TableColumn<>("Variable Name");
        temporalNameColumn.setMinWidth(180);
        temporalNameColumn.setCellValueFactory(new PropertyValueFactory<TemporalColumn, String>("name"));
        temporalNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        temporalNameColumn.setEditable(true);

        TableColumn<TemporalColumn, Boolean> temporalEnabledColumn = new TableColumn<>("Visible");
        temporalEnabledColumn.setMinWidth(20);
        temporalEnabledColumn.setCellValueFactory(new PropertyValueFactory<TemporalColumn, Boolean>("enabled"));
        temporalEnabledColumn.setCellFactory(new Callback<TableColumn<TemporalColumn, Boolean>, TableCell<TemporalColumn, Boolean>>() {
            @Override
            public TableCell<TemporalColumn, Boolean> call(TableColumn<TemporalColumn, Boolean> param) {
                return new CheckBoxTableCell<TemporalColumn, Boolean>() {
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
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataTable.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataTable.disableColumn(column);
                                }
                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });
        temporalEnabledColumn.setEditable(true);

        TableColumn <TemporalColumn, Instant> startColumn = new TableColumn<>("Start");
        startColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TemporalColumn, Instant>, ObservableValue<Instant>>() {
            public ObservableValue<Instant> call(TableColumn.CellDataFeatures<TemporalColumn, Instant> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getStartInstant());
            }
        });

        TableColumn <TemporalColumn, Instant> endColumn = new TableColumn<>("End");
        endColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TemporalColumn, Instant>, ObservableValue<Instant>>() {
            public ObservableValue<Instant> call(TableColumn.CellDataFeatures<TemporalColumn, Instant> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getEndInstant());
            }
        });

        temporalColumnTableView.getColumns().addAll(temporalEnabledColumn, temporalNameColumn, startColumn, endColumn);
        

        // create dataframe view for double columns
        doubleColumnTableView = new TableView<>();
        doubleColumnTableView.setEditable(true);
        doubleColumnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataTable.setHighlightedColumn(newValue);
            }
        });

        TableColumn<DoubleColumn, String> doubleNameColumn = new TableColumn<>("Variable Name");
        doubleNameColumn.setMinWidth(180);
        doubleNameColumn.setCellValueFactory(new PropertyValueFactory<DoubleColumn, String>("name"));
        doubleNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doubleNameColumn.setEditable(true);

        TableColumn<DoubleColumn, Boolean> doubleEnabledColumn = new TableColumn<>("Visible");
        doubleEnabledColumn.setMinWidth(20);
        doubleEnabledColumn.setCellValueFactory(new PropertyValueFactory<DoubleColumn, Boolean>("enabled"));
        doubleEnabledColumn.setCellFactory(new Callback<TableColumn<DoubleColumn, Boolean>, TableCell<DoubleColumn, Boolean>>() {
            @Override
            public TableCell<DoubleColumn, Boolean> call(TableColumn<DoubleColumn, Boolean> param) {
                return new CheckBoxTableCell<DoubleColumn, Boolean>() {
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
                                    Column column = doubleColumnTableView.getItems().get(rowNumber);
                                    dataTable.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = doubleColumnTableView.getItems().get(rowNumber);
                                    dataTable.disableColumn(column);
                                }
                            }
                        }

                        super.updateItem(item, empty);
                    }
                };
            }
        });
        doubleEnabledColumn.setEditable(true);

        TableColumn <DoubleColumn, Double > minColumn = new TableColumn<>("Min");
        minColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getMinValue());
            }
        });

        TableColumn<DoubleColumn, Double> maxColumn = new TableColumn<>("Max");
        maxColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getMaxValue());
            }
        });

        TableColumn<DoubleColumn, Double> meanColumn = new TableColumn<>("Mean");
        meanColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getMeanValue());
            }
        });

        TableColumn<DoubleColumn, Double> stdevColumn = new TableColumn<>("St. Dev.");
        stdevColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getStandardDeviationValue());
            }
        });

        TableColumn<DoubleColumn, Double> varianceColumn = new TableColumn<>("Variance");
        varianceColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getVarianceValue());
            }
        });

        TableColumn<DoubleColumn, Double> medianColumn = new TableColumn<>("Median");
        medianColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getMedianValue());
            }
        });

        TableColumn<DoubleColumn, Double> percentile25Column = new TableColumn<>("25th Percentile");
        percentile25Column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getPercentile25Value());
            }
        });

        TableColumn<DoubleColumn, Double> percentile75Column = new TableColumn<>("75th Percentile");
        percentile75Column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getPercentile75Value());
            }
        });

        TableColumn<DoubleColumn, Double> IQRColumn = new TableColumn<>("Interquartile Range");
        IQRColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<DoubleColumn, Double>, ObservableValue<Double>>() {
            public ObservableValue<Double> call(TableColumn.CellDataFeatures<DoubleColumn, Double> t) {
                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getIQR());
            }
        });

        doubleColumnTableView.getColumns().addAll(doubleEnabledColumn, doubleNameColumn, minColumn, maxColumn, meanColumn,
                medianColumn, varianceColumn, stdevColumn, percentile25Column, percentile75Column, IQRColumn);
    }

    private ToolBar createToolBar(Stage stage) {
        ToolBar toolBar = new ToolBar();

        // make toggle buttons for showing/hiding selected/unselected items
        ToggleButton showUnselectedButton = new ToggleButton("Unselected");
        showUnselectedButton.setTooltip(new Tooltip("Show Unselected Items"));
        showUnselectedButton.selectedProperty().bindBidirectional(dataTableView.showUnselectedItemsProperty());
        ToggleButton showSelectedButton = new ToggleButton("Selected");
        showSelectedButton.selectedProperty().bindBidirectional(dataTableView.showSelectedItemsProperty());
        showSelectedButton.setTooltip(new Tooltip("Show Selected Items"));

        // create selected items color modification UI components
        HBox selectedItemsColorBox = new HBox();
        selectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker selectedItemsColorPicker = new ColorPicker();
        selectedItemsColorPicker.valueProperty().bindBidirectional(dataTableView.selectedItemsColorProperty());
        selectedItemsColorPicker.getStyleClass().add("button");

        selectedItemsColorBox.getChildren().addAll(new Label(" Selected Items: "), selectedItemsColorPicker);

        // create unselected items color modification UI components
        HBox unselectedItemsColorBox = new HBox();
        unselectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker unselectedItemsColorPicker = new ColorPicker();
        unselectedItemsColorPicker.getStyleClass().add("button");
        unselectedItemsColorPicker.valueProperty().bindBidirectional(dataTableView.unselectedItemsColorProperty());
        unselectedItemsColorBox.getChildren().addAll(new Label(" Unselected Items: "), unselectedItemsColorPicker);

        // create opacity slider
        HBox opacityBox = new HBox();
        opacityBox.setAlignment(Pos.CENTER);

        Slider opacitySlider = new Slider(0.01, 1., dataTableView.getDataItemsOpacity());
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);

        opacitySlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            log.info("opacity slide value changing is " + newValue);
            if (!newValue) {
                dataTableView.setDataItemsOpacity(opacitySlider.getValue());
            }
        });

        opacityBox.getChildren().addAll(new Label(" Opacity: "), opacitySlider);

        // add all items to resize
        toolBar.getItems().addAll(showUnselectedButton, showSelectedButton, new Separator(), selectedItemsColorBox, unselectedItemsColorBox, new Separator(), opacityBox);

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
    public void start(Stage mainStage) throws Exception {
//        try {
            crossVisStage = mainStage;

            crossVisStage.setOnCloseRequest(event -> {
                if (ncFilterWindowStage != null && ncFilterWindowStage.isShowing()) {
                    ncFilterWindowStage.close();
                }
            });

            dataTableView = new DataTableView();
            dataTableView.setDataTable(dataTable);
            dataTableView.setPrefHeight(400);
            dataTableView.setAxisSpacing(100);
            dataTableView.setPadding(new Insets(10));
//            pcpView = new PCPView();
//            pcpView.setDataTable(dataTable);
//            pcpView.setPrefHeight(400);
//            pcpView.setAxisSpacing(100);
//            pcpView.setPadding(new Insets(10));

            dataTableViewScrollPane = new ScrollPane(dataTableView);
            dataTableViewScrollPane.setFitToHeight(true);
            dataTableViewScrollPane.setFitToWidth(dataTableView.getFitToWidth());

            correlationMatrixView = new CorrelationMatrixView();
            correlationMatrixView.setDataTable(dataTable);
            correlationMatrixView.setPadding(new Insets(6));
            correlationMatrixView.setShowQueryCorrelations(true);
            correlationMatrixView.setColorScaleOrientation(Orientation.VERTICAL);

            ToolBar toolBar = createToolBar(mainStage);

            createStatusBar();
//        statusBar.progressProperty().bindBidirectional(pcpView.drawingProgressProperty());

            MenuBar menuBar = createMenuBar(mainStage);
            menuBar.setUseSystemMenuBar(true);

            createColumnTableViews();

            tupleTableView = new TableView<>();

            doubleQueryTableView = QueryTableFactory.buildDoubleSelectionTable();
            doubleQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            temporalQueryTableView = QueryTableFactory.buildTemporalSelectionTable();
            temporalQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            categoricalQueryTableView = QueryTableFactory.buildCategoricalSelectionTable();
            categoricalQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            GridPane queryTablePane = new GridPane();
            ColumnConstraints col1Constraints = new ColumnConstraints();
            col1Constraints.setFillWidth(true);
            col1Constraints.setHgrow(Priority.ALWAYS);
            ColumnConstraints col2Constraints = new ColumnConstraints();
            col2Constraints.setFillWidth(true);
            col2Constraints.setHgrow(Priority.ALWAYS);
            ColumnConstraints col3Constraints = new ColumnConstraints();
            col3Constraints.setFillWidth(true);
            col3Constraints.setHgrow(Priority.ALWAYS);
            queryTablePane.getColumnConstraints().addAll(col1Constraints, col2Constraints, col3Constraints);
            TitledPane doubleQueryTitledPane = new TitledPane("Double Column Selections", doubleQueryTableView);
            doubleQueryTitledPane.setCollapsible(false);
            queryTablePane.add(doubleQueryTitledPane, 0, 0);
            TitledPane temporalQueryTitledPane = new TitledPane("Temporal Column Selections", temporalQueryTableView);
            temporalQueryTitledPane.setCollapsible(false);
            queryTablePane.add(temporalQueryTitledPane, 1, 0);
            TitledPane categoricalQueryTitledPane = new TitledPane("Categorical Column Selections", categoricalQueryTableView);
            categoricalQueryTitledPane.setCollapsible(false);
            queryTablePane.add(categoricalQueryTitledPane, 2, 0);

            // create datamodel tab pane
            tabPane = new TabPane();
            Tab quantitativeColumnTableTab = new Tab(" Quantitative Columns ");
            quantitativeColumnTableTab.setClosable(false);
            quantitativeColumnTableTab.setContent(doubleColumnTableView);

            Tab temporalColumnTableTab = new Tab(" Temporal Columns ");
            temporalColumnTableTab.setClosable(false);
            temporalColumnTableTab.setContent(temporalColumnTableView);

            Tab categoricalColumnTableTab = new Tab(" Categorical Columns ");
            categoricalColumnTableTab.setClosable(false);
            categoricalColumnTableTab.setContent(categoricalColumnTableView);

            Tab dataTableTab = new Tab(" Data Table ");
            dataTableTab.setClosable(false);
            dataTableTab.setContent(tupleTableView);

            Tab queryTableTab = new Tab(" Queries ");
            queryTableTab.setClosable(false);
//        queryTableGrid.prefWidthProperty().bind(tabPane.widthProperty());
            queryTableTab.setContent(queryTablePane);
//        queryTableTab.setContent(queryTableView);

            tabPane.getTabs().addAll(temporalColumnTableTab, quantitativeColumnTableTab, categoricalColumnTableTab, dataTableTab, queryTableTab);

            CheckBox showCorrelationsForQueriedDataCB = new CheckBox("Show Queried Data Correlations");
            showCorrelationsForQueriedDataCB.selectedProperty().bindBidirectional(correlationMatrixView.showQueryCorrelationsProperty());
            correlationMatrixView.setBackground(showCorrelationsForQueriedDataCB.getBackground());
            VBox correlationMatrixVBox = new VBox();
            correlationMatrixVBox.setSpacing(2.);
            correlationMatrixVBox.getChildren().addAll(correlationMatrixView, showCorrelationsForQueriedDataCB);

            SplitPane bottomSplit = new SplitPane();
            bottomSplit.setOrientation(Orientation.HORIZONTAL);
            bottomSplit.getItems().addAll(tabPane, correlationMatrixVBox);
            bottomSplit.setResizableWithParent(correlationMatrixVBox, false);
            bottomSplit.setDividerPositions(0.85);

            SplitPane mainSplit = new SplitPane();
            mainSplit.setOrientation(Orientation.VERTICAL);
            mainSplit.getItems().addAll(dataTableViewScrollPane, bottomSplit);
            mainSplit.setResizableWithParent(dataTableViewScrollPane, false);
            mainSplit.setDividerPositions(0.7);

            VBox topContainer = new VBox();
            topContainer.getChildren().add(menuBar);
            topContainer.getChildren().add(toolBar);

            BorderPane rootNode = new BorderPane();
            rootNode.setCenter(mainSplit);
            rootNode.setTop(topContainer);
            rootNode.setBottom(statusBar);

            Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
            double sceneWidth = screenVisualBounds.getWidth() - 40;
            sceneWidth = sceneWidth > 2000 ? 2000 : sceneWidth;
            Scene scene = new Scene(rootNode, sceneWidth, 800, true, SceneAntialiasing.BALANCED);

            mainStage.setTitle("C r o s s V i s | (" + VERSION_STRING + ")");
            mainStage.setScene(scene);
            mainStage.show();
//        } catch(Exception ex) {
//            ex.printStackTrace();
//            System.exit(0);
//        }
    }

    @Override
    public void stop() {
        if (ncFilterWindowStage != null && ncFilterWindowStage.isShowing()) {
            ncFilterWindowStage.close();
        }

        System.exit(0);
    }

    public static void main (String args[]) {
        launch(args);
    }

    private void changeNumHistogramBins() {
        Dialog<Integer> numHistogramBinsDialog = new Dialog<>();
        numHistogramBinsDialog.setTitle("Number of Histogram Bins");
        numHistogramBinsDialog.setHeaderText("Set the number of histogram bins.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, (int)dataTableView.getPCPVerticalBarHeight(), dataTable.getNumHistogramBins()));
        spinner.setEditable(true);

        grid.add(new Label("Number of DoubleHistogram Bins: "), 0, 0);
        grid.add(spinner, 1, 0);

        numHistogramBinsDialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> spinner.requestFocus());

        numHistogramBinsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.OK, ButtonType.CANCEL);

        final Button buttonApply = (Button)numHistogramBinsDialog.getDialogPane().lookupButton(ButtonType.APPLY);
        buttonApply.setDisable(true);
        buttonApply.addEventFilter(ActionEvent.ACTION, event -> {
            int numHistogramBins = spinner.getValue();
            if (numHistogramBins != dataTable.getNumHistogramBins()) {
                dataTable.setNumHistogramBins(numHistogramBins);
            }
            event.consume();
        });

        final Button buttonOK = (Button)numHistogramBinsDialog.getDialogPane().lookupButton(ButtonType.OK);
        buttonOK.setDisable(true);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getValue() != dataTable.getNumHistogramBins()) {
                buttonApply.setDisable(false);
                buttonOK.setDisable(false);
            }
        });

        numHistogramBinsDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return spinner.getValue();
            }
            return null;
        });

        Optional<Integer> result = numHistogramBinsDialog.showAndWait();

        result.ifPresent(numHistogramBins -> {
            System.out.println("new numHistogramBins is " + numHistogramBins);
            dataTable.setNumHistogramBins(numHistogramBins);
        });
    }

    private void changeAxisSpacing() {
        Dialog<Integer> axisSpacingDialog = new Dialog<>();
        axisSpacingDialog.setTitle("Axis Spacing");
        axisSpacingDialog.setHeaderText("Set the pixel spacing between crossvis");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 800, dataTableView.getAxisSpacing(), 1));
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
            dataTableView.setAxisSpacing(axisSpacing);
            event.consume();
        });

        final Button buttonOK = (Button)axisSpacingDialog.getDialogPane().lookupButton(ButtonType.OK);
        buttonOK.setDisable(true);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getValue() != dataTableView.getAxisSpacing()) {
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
            dataTableView.setAxisSpacing(axisSpacing);
        });
    }

    private MenuBar createMenuBar (Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu appMenu = new Menu("CrossVis");
        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");
        Menu dataMenu = new Menu("Data");
        Menu queryMenu = new Menu("Query");

        menuBar.getMenus().addAll(appMenu, fileMenu, viewMenu, dataMenu, queryMenu);

        // Application Menu
        MenuItem exitMI = new MenuItem("Quit CrossVis");
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.close();
            }
        });

        appMenu.getItems().addAll(exitMI);

        // File Menu
        MenuItem openNetCDFMI = new MenuItem("Open NetCDF...");
        openNetCDFMI.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN));
        openNetCDFMI.setOnAction(event -> { openNetCDFFile(); });

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCSVMI.setOnAction(event -> { openCSVFile(); });

        exportSelectedDataMenuItem = new MenuItem("Export Selected Data...");
        exportSelectedDataMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        exportSelectedDataMenuItem.setOnAction(event -> { exportSelectedData(); });

        exportUnselectedDataMenuItem = new MenuItem("Export Unselected Data...");
        exportUnselectedDataMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.META_DOWN));
        exportUnselectedDataMenuItem.setOnAction(event -> { exportUnselectedData(); });

        MenuItem saveScreenShotMI = new MenuItem("Save Screenshot...");
        saveScreenShotMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        saveScreenShotMI.setOnAction(event -> { saveScreenShot(); });

        fileMenu.getItems().addAll(openNetCDFMI, openCSVMI, new SeparatorMenuItem(), exportSelectedDataMenuItem,
                exportUnselectedDataMenuItem, new SeparatorMenuItem(), saveScreenShotMI);

        // View Menu
        CheckMenuItem showScatterplotsMI = new CheckMenuItem("Show Scatterplots");
        showScatterplotsMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        showScatterplotsMI.selectedProperty().bindBidirectional(dataTableView.showScatterplotsProperty());

        CheckMenuItem showHistogramsMI = new CheckMenuItem("Show Histograms");
        showHistogramsMI.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN));
        showHistogramsMI.selectedProperty().bindBidirectional(dataTableView.showHistogramsProperty());

        CheckMenuItem showPolylinesMI = new CheckMenuItem("Show Parallel Coordinate Lines");
        showPolylinesMI.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.META_DOWN));
        showPolylinesMI.selectedProperty().bindBidirectional(dataTableView.showPolylinesProperty());

        CheckMenuItem showSummaryStatsMI = new CheckMenuItem("Show Summary Statitics");
        showSummaryStatsMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN, KeyCombination.SHIFT_DOWN));
        showSummaryStatsMI.selectedProperty().bindBidirectional(dataTableView.showSummaryStatisticsProperty());

        CheckMenuItem showCorrelationsMI = new CheckMenuItem("Show Parallel Coordinate Axis Correlations");
        showCorrelationsMI.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        showCorrelationsMI.selectedProperty().bindBidirectional(dataTableView.showCorrelationsProperty());

        Menu summaryStatsDisplayModeMenu = new Menu("Axis Statistics Display Mode");
        ToggleGroup summaryStatsDisplayModeMenuGroup = new ToggleGroup();

        RadioMenuItem meanStatsModeMI = new RadioMenuItem("Mean / Standard Deviation Mode");
        meanStatsModeMI.setToggleGroup(summaryStatsDisplayModeMenuGroup);
        meanStatsModeMI.setSelected(dataTableView.getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT);

        RadioMenuItem medianStatsModeMI = new RadioMenuItem("Median / Interquartile Range Mode");
        medianStatsModeMI.setToggleGroup(summaryStatsDisplayModeMenuGroup);
        medianStatsModeMI.setSelected(dataTableView.getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT);

        summaryStatsDisplayModeMenu.getItems().addAll(meanStatsModeMI, medianStatsModeMI);

        summaryStatsDisplayModeMenuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                RadioMenuItem radioMenuItem = (RadioMenuItem)newValue;
                if (radioMenuItem.getText().equals(meanStatsModeMI.getText())) {
                    dataTableView.setSummaryStatisticsDisplayMode(DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT);
                } else if (radioMenuItem.getText().equals(medianStatsModeMI.getText())) {
                    dataTableView.setSummaryStatisticsDisplayMode(DataTableView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT);
                }
            }
        });

        dataTableView.summaryStatisticsDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                    meanStatsModeMI.setSelected(true);
                } else if (newValue == DataTableView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT) {
                    medianStatsModeMI.setSelected(true);
                }
            }
        });

        Menu polylineDisplayModeMenu = new Menu("TuplePolyline Display Mode");
        ToggleGroup polylineDisplayModeMenuGroup = new ToggleGroup();

        RadioMenuItem lineDisplayModeMI = new RadioMenuItem("TuplePolyline Display Mode");
        lineDisplayModeMI.setToggleGroup(polylineDisplayModeMenuGroup);
        if (dataTableView.getPolylineDisplayMode() == DataTableView.POLYLINE_DISPLAY_MODE.POLYLINES) {
            lineDisplayModeMI.setSelected(true);
        }

        RadioMenuItem binDisplayModeMI = new RadioMenuItem("Binned TuplePolyline Display Mode");
        binDisplayModeMI.setToggleGroup(polylineDisplayModeMenuGroup);
        if (dataTableView.getPolylineDisplayMode() == DataTableView.POLYLINE_DISPLAY_MODE.BINNED_POLYLINES) {
            lineDisplayModeMI.setSelected(true);
        }

        polylineDisplayModeMenu.getItems().addAll(lineDisplayModeMI, binDisplayModeMI);

        polylineDisplayModeMenuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                if (toggleItem.getText().equals(lineDisplayModeMI.getText())) {
                    dataTableView.setPolylineDisplayMode(DataTableView.POLYLINE_DISPLAY_MODE.POLYLINES);
                } else if (toggleItem.getText().equals(binDisplayModeMI.getText())) {
                    dataTableView.setPolylineDisplayMode(DataTableView.POLYLINE_DISPLAY_MODE.BINNED_POLYLINES);
                }
            }
        });

        dataTableView.polylineDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue == DataTableView.POLYLINE_DISPLAY_MODE.POLYLINES) {
                    lineDisplayModeMI.setSelected(true);
                } else {
                    binDisplayModeMI.setSelected(true);
                }
            }
        });

        Menu axisLayoutMenu = new Menu("Axis Layout");

        MenuItem changeAxisSpacingMI = new MenuItem("Change Axis Spacing...");
        changeAxisSpacingMI.setOnAction(event -> {
            changeAxisSpacing();
        });

        CheckMenuItem fitPCPAxesToWidthCheckMI = new CheckMenuItem("Fit Axis Spacing to Window Width");
        fitPCPAxesToWidthCheckMI.setSelected(dataTableView.getFitToWidth());
//        fitPCPAxesToWidthCheckMI.setDisable(true);
        fitPCPAxesToWidthCheckMI.selectedProperty().addListener((observable, oldValue, newValue) -> {
            dataTableView.setFitToWidth(fitPCPAxesToWidthCheckMI.isSelected());
            if (dataTableView.getFitToWidth()) {
                changeAxisSpacingMI.setDisable(true);
                dataTableViewScrollPane.setFitToWidth(true);
            } else {
                changeAxisSpacingMI.setDisable(false);
                dataTableViewScrollPane.setFitToWidth(false);
            }
        });

        if (dataTableView.getFitToWidth()) {
            changeAxisSpacingMI.setDisable(true);
        } else {
            changeAxisSpacingMI.setDisable(true);
        }

        axisLayoutMenu.getItems().addAll(fitPCPAxesToWidthCheckMI, changeAxisSpacingMI);

        MenuItem changeHistogramBinCountMenuItem = new MenuItem("Change Number of Histogram Bins...");
        changeHistogramBinCountMenuItem.setOnAction(event -> {
            changeNumHistogramBins();
        });

//        CheckMenuItem showSelectedCorrelationsMI = new CheckMenuItem("Show Correlations with Selected Data");
//        showSelectedCorrelationsMI.setSelected(correlationMatrixView.isShowingQueryCorrelations());
//        showSelectedCorrelationsMI.selectedProperty().addListener(observable -> {
//            correlationMatrixView.setShowQueryCorrelations(showSelectedCorrelationsMI.isSelected());
//        });

        // create menu item to enabled/disable data datamodel updates
        enableDataTableUpdatesCheckMenuItem = new CheckMenuItem("Enable Data Table View Updates");
        enableDataTableUpdatesCheckMenuItem.setSelected(false);
        enableDataTableUpdatesCheckMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                // refresh data datamodel based on current state of the data model
                setDataTableItems();
            } else {
                tupleTableView.getItems().clear();
            }
        });

        viewMenu.getItems().addAll(showScatterplotsMI, showHistogramsMI, showPolylinesMI, showSummaryStatsMI, showCorrelationsMI,
                summaryStatsDisplayModeMenu, polylineDisplayModeMenu, axisLayoutMenu, changeHistogramBinCountMenuItem,
                enableDataTableUpdatesCheckMenuItem);


        // Data Menu
        Menu removeDataMenu = new Menu("Remove Data");
        // create menu item to remove selected data
        removeSelectedDataMenuItem = new MenuItem("Remove Selected Data");
        removeSelectedDataMenuItem.setOnAction(event -> {
            removeSelectedData();
        });

        // create menu item to keep selected data and remove unselected data
        removeUnselectedDataMenuItem = new MenuItem("Remove Unselected Data");
        removeUnselectedDataMenuItem.setOnAction(event -> {
            removeUnselectedData();
        });
        removeDataMenu.getItems().addAll(removeSelectedDataMenuItem, removeUnselectedDataMenuItem);

        dataMenu.getItems().addAll(removeDataMenu);

        // Query Menu
        CheckMenuItem showQueryStatisticsCheckMI = new CheckMenuItem("Show Selected Data Statistics");
        showQueryStatisticsCheckMI.setSelected(dataTable.getCalculateQueryStatistics());
        showQueryStatisticsCheckMI.setDisable(false);
        showQueryStatisticsCheckMI.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            dataTable.setCalculateQueryStatistics(newValue);
        }));

        CheckMenuItem showNonQueryStatisticsCheckMI = new CheckMenuItem("Show Unselected Data Statistics");
        showNonQueryStatisticsCheckMI.setSelected(dataTable.getCalculateNonQueryStatistics());
        showNonQueryStatisticsCheckMI.setDisable(false);
        showNonQueryStatisticsCheckMI.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            dataTable.setCalculateNonQueryStatistics(newValue);
        }));

        removeAllQueriesMI = new MenuItem("Remove All Axis Queries");
        removeAllQueriesMI.setDisable(true);
        removeAllQueriesMI.setOnAction(event -> {
            dataTableView.clearQuery();
        });

        queryMenu.getItems().addAll(showQueryStatisticsCheckMI, showNonQueryStatisticsCheckMI, removeAllQueriesMI);

        return menuBar;
    }

    private void saveScreenShot() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Snapshot Image File");

        String lastSnapshotDirectory = preferences.get(CrossVisPreferenceKeys.LAST_SNAPSHOT_DIRECTORY, "");
        if (!lastSnapshotDirectory.isEmpty()) {
            File dir = new File(lastSnapshotDirectory);
            if (dir.exists() && dir.canWrite()) {
                fileChooser.setInitialDirectory(dir);
            }
        }

        File imageFile = fileChooser.showSaveDialog(null);
        if (imageFile != null) {
            ArrayList<Integer> scaleChoices = new ArrayList<>();
            scaleChoices.add(1);
            scaleChoices.add(2);
            scaleChoices.add(4);
            scaleChoices.add(8);
            int scaleFactor = 1;

            ChoiceDialog<Integer> dialog = new ChoiceDialog<>(scaleFactor, scaleChoices);
            dialog.setTitle("Snapshot Image Scale Factor");
            dialog.setHeaderText("Higher Scale Factors Result in Larger, Higher Resolution Images");
            dialog.setContentText("Snapshot Image Scale Factor: ");

            Optional<Integer> result = dialog.showAndWait();
            if (result.isPresent()) {
                scaleFactor = result.get();
            } else {
                return;
            }

            WritableImage pcpSnapshotImage = dataTableView.getSnapshot(scaleFactor);

            try {
                if (!imageFile.getName().endsWith(".png")) {
                    imageFile = new File(imageFile.getParent(), imageFile.getName() + ".png");
                }
                ImageIO.write(SwingFXUtils.fromFXImage(pcpSnapshotImage, null), "png", imageFile);
                preferences.put(CrossVisPreferenceKeys.LAST_SNAPSHOT_DIRECTORY, imageFile.getParentFile().getAbsolutePath());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Snapshot Saved");
                alert.setHeaderText("Snapshot Image Saved Successfully");
                alert.setContentText(imageFile.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Snapshot Error");
                alert.setContentText("Error occurred while saving snapshot: \n" +
                        e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void exportSelectedData() {
        if (dataTable.getActiveQuery().getQueriedTuples().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Export Error");
            alert.setHeaderText(null);
            alert.setContentText("No tuples are currently selected.  Export operation canceled.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        String lastExportDirectoryPath = preferences.get(CrossVisPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
        if (!lastExportDirectoryPath.isEmpty()) {
            File lastExportDirectory = new File(lastExportDirectoryPath);
            if (lastExportDirectory != null && lastExportDirectory.exists() && lastExportDirectory.canRead()) {
                fileChooser.setInitialDirectory(new File(lastExportDirectoryPath));
            }
        }
        fileChooser.setTitle("Export Selected Data to CSV File");
        File csvFile = fileChooser.showSaveDialog(null);
        if (csvFile != null) {
            try {
                int numTuplesExported = IOUtilities.exportSelectedFromDataTableQueryToCSV(csvFile, dataTable);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Selected Data Exported");
                alert.setHeaderText(null);
                alert.setContentText("Successfully exported " + numTuplesExported + " tuples to '" + csvFile.getName() + "'");
                alert.showAndWait();

                preferences.put(CrossVisPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void exportUnselectedData() {
        if (dataTable.getActiveQuery().getQueriedTuples().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Export Error");
            alert.setHeaderText(null);
            alert.setContentText("No selections are set so all tuples will be exported.  Export all data?.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.CANCEL) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
        String lastExportDirectoryPath = preferences.get(CrossVisPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
        if (!lastExportDirectoryPath.isEmpty()) {
            File lastExportDirectory = new File(lastExportDirectoryPath);
            if (lastExportDirectory != null && lastExportDirectory.exists() && lastExportDirectory.canRead()) {
                fileChooser.setInitialDirectory(new File(lastExportDirectoryPath));
            }
        }
        fileChooser.setTitle("Export Unselected Data to CSV File");
        File csvFile = fileChooser.showSaveDialog(null);
        if (csvFile != null) {
            try {
                int numTuplesExported = IOUtilities.exportUnselectedFromDataTableToCSV(csvFile, dataTable);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Unselected Data Exported");
                alert.setHeaderText(null);
                alert.setContentText("Successfully exported " + numTuplesExported + " tuples to '" + csvFile.getName() + "'");
                alert.showAndWait();

                preferences.put(CrossVisPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void removeSelectedData() {
        int removedTuples = dataTable.removeSelectedTuples();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Selected Data Removed");
        alert.setHeaderText(null);
        alert.setContentText(removedTuples + " polylines were removed.");
        alert.showAndWait();
    }

    private void removeUnselectedData() {
        int removedTuples = dataTable.removeUnselectedTuples();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Unselected Data Removed");
        alert.setHeaderText(null);
        alert.setContentText(removedTuples + " polylines were removed.");
        alert.showAndWait();
    }

    private void updatePercentSelected() {
        if (dataTable != null && !dataTable.isEmpty()) {
            double percentSelected = (double) dataTable.getActiveQuery().getQueriedTupleCount() / dataTable.getTupleCount();
            percentSelectedProgress.setProgress(percentSelected);
            statusBar.setText(" " + dataTable.getActiveQuery().getQueriedTupleCount() + " of " + dataTable.getTupleCount() + " tuples selected (" + decimalFormat.format(percentSelected) + ")");
        } else {
            statusBar.setText(" Ready ");
        }
    }

    private void openNetCDFFile() {
        if (ncFilterWindow == null) {
            ncFilterWindow = new NetCDFFilterWindow(dataTable);
            try {
                ncFilterWindowStage = new Stage();
                ncFilterWindow.start(ncFilterWindowStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ncFilterWindowStage.show();
        }
    }

    private void openCSVFile() {
        FileChooser fileChooser = new FileChooser();
        String lastCSVDirectoryPath = preferences.get(CrossVisPreferenceKeys.LAST_CSV_READ_DIRECTORY, "");
        if (!lastCSVDirectoryPath.isEmpty()) {
            File lastCSVDirectory = new File(lastCSVDirectoryPath);
            if (lastCSVDirectory != null && lastCSVDirectory.exists() && lastCSVDirectory.canRead()) {
                fileChooser.setInitialDirectory(new File(lastCSVDirectoryPath));
            }
        }

        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        File csvFile = fileChooser.showOpenDialog(crossVisStage);
        if (csvFile != null) {
            try {
                FileUtils.openCSVFile(csvFile, dataTable);
                preferences.put(CrossVisPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());

                temporalColumnTableView.getItems().clear();
                ArrayList<TemporalColumn> temporalColumns = dataTable.getTemporalColumns();
                if (temporalColumns != null && !temporalColumns.isEmpty()) {
                    temporalColumnTableView.setItems(FXCollections.observableArrayList(temporalColumns));
                }

                doubleColumnTableView.getItems().clear();
                ArrayList<DoubleColumn> doubleColumns = dataTable.getDoubleColumns();
                if (doubleColumns != null && !doubleColumns.isEmpty()) {
                    doubleColumnTableView.setItems(FXCollections.observableArrayList(doubleColumns));
                }

                categoricalColumnTableView.getItems().clear();
                ArrayList<CategoricalColumn> categoricalColumns = dataTable.getCategoricalColumns();
                if (categoricalColumns != null && !categoricalColumns.isEmpty()) {
                    categoricalColumnTableView.setItems(FXCollections.observableArrayList(categoricalColumns));
                }

                doubleQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
                temporalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
                categoricalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));

                setDataTableColumns();
                setDataTableItems();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("CSV File Read Error");
                alert.setHeaderText(null);
                alert.setContentText("An IOException was caught while reading the csv file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void setDataTableItems() {
        if (enableDataTableUpdatesCheckMenuItem.isSelected()) {
            ObservableList<Tuple> tableTuples;
            tupleTableView.getItems().clear();

            if (dataTable.getActiveQuery().hasColumnSelections()) {
                tableTuples = FXCollections.observableArrayList(dataTable.getActiveQuery().getQueriedTuples());
            } else {
                tableTuples = FXCollections.observableArrayList(dataTable.getTuples());
            }

            tupleTableView.setItems(tableTuples);
        }
    }

    private void setDataTableColumns() {
        tupleTableView.getColumns().clear();

        // make a column for each enabled data datamodel column
        if (!dataTable.isEmpty()) {
            for (int icol = 0; icol < dataTable.getColumnCount(); icol++) {
                Column column = dataTable.getColumn(icol);
                if (column instanceof TemporalColumn) {
                    TemporalColumn temporalColumn = (TemporalColumn)column;
                    TableColumn<Tuple, Instant> tableColumn = new TableColumn<Tuple, Instant>(temporalColumn.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Instant>, ObservableValue<Instant>>() {
                        public ObservableValue<Instant> call(TableColumn.CellDataFeatures<Tuple, Instant> t) {
                            int columnIndex = dataTable.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.info("Weird!");
                            }
                            return new ReadOnlyObjectWrapper((Instant)t.getValue().getElement(columnIndex));

                        }
                    });
                    tupleTableView.getColumns().add(tableColumn);
                } else if (column instanceof DoubleColumn) {
                    TableColumn<Tuple, Double> tableColumn = new TableColumn<>(column.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Double>, ObservableValue<Double>>() {
                        public ObservableValue<Double> call(TableColumn.CellDataFeatures<Tuple, Double> t) {
                            int columnIndex = dataTable.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.info("Weird!");
                            }
                            return new ReadOnlyObjectWrapper((Double)t.getValue().getElement(columnIndex));

                        }
                    });
                    tupleTableView.getColumns().add(tableColumn);
                } else if (column instanceof CategoricalColumn) {
                    TableColumn<Tuple, String> tableColumn = new TableColumn<>(column.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<Tuple, String> param) {
                            int columnIndex = dataTable.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.info("Weird!");
                            }
                            return new ReadOnlyObjectWrapper<>((String)param.getValue().getElement(columnIndex));
                        }
                    });
                    tupleTableView.getColumns().add(tableColumn);
                }
            }
        }
    }

    @Override
    public void dataModelReset(DataTable dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());

        temporalColumnTableView.getItems().clear();
        ArrayList<TemporalColumn> temporalColumns = dataTable.getTemporalColumns();
        if (temporalColumns != null && !temporalColumns.isEmpty()) {
            temporalColumnTableView.setItems(FXCollections.observableArrayList(temporalColumns));
        }

        doubleColumnTableView.getItems().clear();
        ArrayList<DoubleColumn> doubleColumns = dataTable.getDoubleColumns();
        if (doubleColumns != null && !doubleColumns.isEmpty()) {
            doubleColumnTableView.setItems(FXCollections.observableArrayList(doubleColumns));
        }

        categoricalColumnTableView.getItems().clear();
        ArrayList<CategoricalColumn> categoricalColumns = dataTable.getCategoricalColumns();
        if (categoricalColumns != null && !categoricalColumns.isEmpty()) {
            categoricalColumnTableView.setItems(FXCollections.observableArrayList(categoricalColumns));
        }

        doubleQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));

        setDataTableColumns();
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
        doubleQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataModel, Column column) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionAdded(DataTable dataModel, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataTable dataModel, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        queryTableView.refresh();
        doubleQueryTableView.refresh();
        temporalQueryTableView.refresh();
        categoricalQueryTableView.refresh();
        doubleColumnTableView.refresh();
        temporalColumnTableView.refresh();
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        if (newHighlightedColumn != null) {
            if (newHighlightedColumn instanceof TemporalColumn) {
                temporalColumnTableView.getSelectionModel().select((TemporalColumn)newHighlightedColumn);
            } else if (newHighlightedColumn instanceof DoubleColumn) {
                doubleColumnTableView.getSelectionModel().select((DoubleColumn)newHighlightedColumn);
            }
        } else {
            temporalColumnTableView.getSelectionModel().clearSelection();
            doubleColumnTableView.getSelectionModel().clearSelection();
        }
    }

    @Override
    public void dataModelTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples) {
        updatePercentSelected();
    }

    @Override
    public void dataModelTuplesRemoved(DataTable dataModel, int numTuplesRemoved) {
        updatePercentSelected();
    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataTable dataModel) {}

    @Override
    public void dataModelStatisticsChanged(DataTable dataModel) { }

    @Override
    public void dataModelColumnDisabled(DataTable dataModel, Column disabledColumn) {
        // reset the data datamodel columns
        tupleTableView.getItems().clear();
        tupleTableView.getColumns().clear();
        setDataTableColumns();
        setDataTableItems();

        updatePercentSelected();
        doubleColumnTableView.refresh();
        temporalColumnTableView.refresh();
    }

    @Override
    public void dataModelColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataTable dataModel, Column enabledColumn) {

    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index) {

    }

    @Override
    public void dataModelColumnOrderChanged(DataTable dataModel) {

    }

    @Override
    public void dataModelColumnNameChanged(DataTable dataModel, Column column) {

    }
}
