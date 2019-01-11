package gov.ornl.crossvis;

import gov.ornl.correlationview.CorrelationMatrixView;
import gov.ornl.datatable.*;
import gov.ornl.datatableview.DataTableView;
import gov.ornl.datatableview.DoubleAxis;
import gov.ornl.datatableview.NumberTextField;
import gov.ornl.imageview.ImageGridWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
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
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.controlsfx.control.StatusBar;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class CrossVis extends Application implements DataTableListener {
    private static final Logger log = Logger.getLogger(CrossVis.class.getName());

    private static final String VERSION_STRING = "v2.1.4";

    private String appTitleString = "C r o s s V i s (" + VERSION_STRING + ")";

    private CorrelationMatrixView correlationMatrixView;
    private DataTable dataTable;
    private DataTableView dataTableView;

    private Preferences preferences;

    private ScrollPane dataTableViewScrollPane;
    private TabPane tabPane;

    private TableView<DoubleColumn> doubleColumnTableView;
    private TableView<TemporalColumn> temporalColumnTableView;
    private TableView<CategoricalColumn> categoricalColumnTableView;
    private TableView<ImageColumn> imageColumnTableView;

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
    private MenuItem openImageGridViewMenuItem;

    private Stage crossVisStage;

    private NetCDFFilterWindow ncFilterWindow = null;
    private Stage ncFilterWindowStage = null;

    private ImageGridWindow imageGridWindow = null;
    private Stage imageGridWindowStage = null;

    private BooleanProperty dataTableUpdatesEnabled = new SimpleBooleanProperty(false);

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());

        decimalFormat = new DecimalFormat("##0.0%");

        dataTableUpdatesEnabled.addListener(observable -> setDataTableItems());
//        dataTableUpdatesEnabled.addListener(observable -> {
//            if (dataTableUpdatesEnabled.get()) {
//                setDataTableItems();
//            } else {
//                tupleTableView.getItems().clear();
//            }
//        });

        dataTable = new DataTable();
        dataTable.addDataTableListener(this);
    }

    private void createColumnTableViews() {
        // create table views for image columns
        imageColumnTableView = new TableView<>();
        imageColumnTableView.setEditable(true);
        imageColumnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataTable.setHighlightedColumn(newValue);
            }
        });

        TableColumn<ImageColumn, String> imageNameColumn = new TableColumn<>("Name");
        imageNameColumn.setMinWidth(180);
        imageNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        imageNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        imageNameColumn.setEditable(true);

        TableColumn<ImageColumn, Boolean> imageEnabledColumn = new TableColumn<>("Visible");
        imageEnabledColumn.setMinWidth(20);
        imageEnabledColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        imageEnabledColumn.setCellFactory(new Callback<TableColumn<ImageColumn, Boolean>, TableCell<ImageColumn, Boolean>>() {
            @Override
            public TableCell<ImageColumn, Boolean> call(TableColumn<ImageColumn, Boolean> param) {
                return new CheckBoxTableCell<ImageColumn, Boolean>() {
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
        imageEnabledColumn.setEditable(true);

        imageColumnTableView.getColumns().addAll(imageEnabledColumn, imageNameColumn);

        // create dataframe view for categorical columns
        categoricalColumnTableView = new TableView<>();
        categoricalColumnTableView.setEditable(true);
        categoricalColumnTableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataTable.setHighlightedColumn(newValue);
            }
        }));

        TableColumn<CategoricalColumn, String> categoricalNameColumn = new TableColumn<>("Name");
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

        TableColumn<TemporalColumn, String> temporalNameColumn = new TableColumn<>("Name");
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

        TableColumn<DoubleColumn, String> doubleNameColumn = new TableColumn<>("Name");
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

        ToggleButton showHistogramsButton = new ToggleButton("Histograms");
        showHistogramsButton.selectedProperty().bindBidirectional(dataTableView.showHistogramsProperty());
        showHistogramsButton.setTooltip(new Tooltip("Show Histograms"));

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
        toolBar.getItems().addAll(showUnselectedButton, showSelectedButton, new Separator(), showHistogramsButton, new Separator(), selectedItemsColorBox, unselectedItemsColorBox, new Separator(), opacityBox);

        return toolBar;
    }

    private void createStatusBar() {
        statusBar = new StatusBar();

        percentSelectedProgress = new ProgressBar();
        percentSelectedProgress.setProgress(0.0);
        percentSelectedProgress.setTooltip(new Tooltip("Percentage of Data Currently Selected"));
        percentSelectedProgress.setPrefWidth(300);

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
                if (imageGridWindowStage != null && imageGridWindowStage.isShowing()) {
                    imageGridWindowStage.close();
                }
            });

            dataTableView = new DataTableView();
            dataTableView.setDataTable(dataTable);
            dataTableView.setPrefHeight(400);
            dataTableView.setAxisSpacing(100);
            dataTableView.setPadding(new Insets(10));

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

            MenuBar menuBar = createMenuBar(mainStage);
//            menuBar.setUseSystemMenuBar(true);

            createColumnTableViews();

            tupleTableView = new TableView<>();
            CheckBox dataTableUpdatesCB = new CheckBox("Enable Data Table Updates");
            dataTableUpdatesCB.selectedProperty().bindBidirectional(dataTableUpdatesEnabled);
            VBox tupleTableViewNode = new VBox(dataTableUpdatesCB, tupleTableView);
            tupleTableViewNode.setSpacing(4.);

            doubleQueryTableView = QueryTableFactory.buildDoubleSelectionTable();
            doubleQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            temporalQueryTableView = QueryTableFactory.buildTemporalSelectionTable();
            temporalQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            categoricalQueryTableView = QueryTableFactory.buildCategoricalSelectionTable();
            categoricalQueryTableView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            TabPane queryTabPane = new TabPane();
            queryTabPane.getTabs().add(new Tab("Double Axis Selections", doubleQueryTableView));
            queryTabPane.getTabs().add(new Tab("Temporal Axis Selections", temporalQueryTableView));
            queryTabPane.getTabs().add(new Tab("Categorical Axis Selections", categoricalQueryTableView));
            for (Tab tab : queryTabPane.getTabs()) {
                tab.setClosable(false);
            }

            TabPane axisInfoTablesTabPane = new TabPane();
            axisInfoTablesTabPane.getTabs().add(new Tab("Double Axes", doubleColumnTableView));
            axisInfoTablesTabPane.getTabs().add(new Tab("Temporal Axes", temporalColumnTableView));
            axisInfoTablesTabPane.getTabs().add(new Tab("Categorical Axes", categoricalColumnTableView));
            axisInfoTablesTabPane.getTabs().add(new Tab("Image Axes", imageColumnTableView));
            for (Tab tab : axisInfoTablesTabPane.getTabs()) {
                tab.setClosable(false);
            }

            // create datamodel tab pane
            tabPane = new TabPane();
            Tab axisInfoTableTab = new Tab("Axis Details");
            axisInfoTableTab.setClosable(false);
            axisInfoTableTab.setContent(axisInfoTablesTabPane);
//            Tab quantitativeColumnTableTab = new Tab(" Quantitative Axes ");
//            quantitativeColumnTableTab.setClosable(false);
//            quantitativeColumnTableTab.setContent(doubleColumnTableView);
//
//            Tab temporalColumnTableTab = new Tab(" Temporal Axes ");
//            temporalColumnTableTab.setClosable(false);
//            temporalColumnTableTab.setContent(temporalColumnTableView);
//
//            Tab categoricalColumnTableTab = new Tab(" Categorical Axes ");
//            categoricalColumnTableTab.setClosable(false);
//            categoricalColumnTableTab.setContent(categoricalColumnTableView);

            Tab dataTableTab = new Tab(" Data Table ");
            dataTableTab.setClosable(false);
            dataTableTab.setContent(tupleTableViewNode);

            Tab queryTableTab = new Tab(" Axis Selections ", queryTabPane);
            queryTableTab.setClosable(false);
//        queryTableGrid.prefWidthProperty().bind(tabPane.widthProperty());
//            queryTableTab.setContent(queryTablePane);
//        queryTableTab.setContent(queryTableView);

            tabPane.getTabs().addAll(axisInfoTableTab, dataTableTab, queryTableTab);

//            tabPane.getTabs().addAll(quantitativeColumnTableTab, categoricalColumnTableTab, temporalColumnTableTab, dataTableTab, queryTableTab);

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

            mainStage.setTitle(appTitleString);
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

        if (imageGridWindowStage != null && imageGridWindowStage.isShowing()) {
            imageGridWindowStage.close();
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

        MenuItem aboutMI = new MenuItem("About CrossVis");
        aboutMI.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.UTILITY);

//            alert.setTitle("About");
            alert.setTitle("About CrossVis (" + VERSION_STRING + ")");
            alert.setHeaderText(null);
            alert.setGraphic(null);

            String s = "CrossVis " + VERSION_STRING + "\n\n" +
                    "By Dr. Chad A. Steed\n\n" +
                    "Oak Ridge National Laboratory\n" +
                    "Oak Ridge, TN\n\n" +
                    "\u00a9 ";
            if (LocalDateTime.now().getYear() != 2018) {
                s += "2018 - " + LocalDateTime.now().getYear();
            } else {
                s += "2018";
            }
            alert.setContentText(s);
            alert.show();
        });

        // Application Menu
        MenuItem exitMI = new MenuItem("Quit CrossVis");
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.close();
            }
        });

        appMenu.getItems().addAll(aboutMI, exitMI);

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

        Menu polylineDisplayMenu = new Menu("Polyline Display Preferences");

        CheckMenuItem showPolylinesMI = new CheckMenuItem("Show Polylines");
        showPolylinesMI.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.META_DOWN));
        showPolylinesMI.selectedProperty().bindBidirectional(dataTableView.showPolylinesProperty());

        CheckMenuItem showSelectedPolylinesMI = new CheckMenuItem("Show Selected Polylines");
        showSelectedPolylinesMI.selectedProperty().bindBidirectional(dataTableView.showSelectedItemsProperty());

        CheckMenuItem showUnselectedPolylinesMI = new CheckMenuItem("Show Unselected Polylines");
        showUnselectedPolylinesMI.selectedProperty().bindBidirectional(dataTableView.showUnselectedItemsProperty());

        CheckMenuItem showContextSegmentsMI = new CheckMenuItem("Show Context Polyline Segments");
        showContextSegmentsMI.selectedProperty().bindBidirectional(dataTableView.getShowContextPolylineSegmentsProperty());

        polylineDisplayMenu.getItems().addAll(showPolylinesMI, showSelectedPolylinesMI, showUnselectedPolylinesMI,
                showContextSegmentsMI);

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

        MenuItem setNumericalAxisExtentsMenuItem = new MenuItem("Sync Ranges of Numerical Axes...");
        setNumericalAxisExtentsMenuItem.setOnAction(event -> {
            syncNumericalAxesRanges();
        });

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
        enableDataTableUpdatesCheckMenuItem.selectedProperty().bindBidirectional(dataTableUpdatesEnabled);
//        enableDataTableUpdatesCheckMenuItem.setSelected(false);
//        enableDataTableUpdatesCheckMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null && newValue) {
//                // refresh data datamodel based on current state of the data model
//                setDataTableItems();
//            } else {
//                tupleTableView.getItems().clear();
//            }
//        });

        CheckMenuItem showScattplotMarginValuesCheckMenuItem = new CheckMenuItem("Show Scatterplot Margin Values");
        showScattplotMarginValuesCheckMenuItem.selectedProperty().bindBidirectional(dataTableView.showScatterplotMarginValuesProperty());
//        showScattplotMarginValuesCheckMenuItem.setSelected(dataTableView.isShowingScatterplotMarginValues());
//        showScattplotMarginValuesCheckMenuItem.selectedProperty().addListener((observable) -> {
//            dataTableView.setShowScatterplotMarginValues(showScattplotMarginValuesCheckMenuItem.isSelected());
//        });

        openImageGridViewMenuItem = new MenuItem("Open Image Grid View");
        openImageGridViewMenuItem.setDisable(true);
        openImageGridViewMenuItem.setOnAction(event -> {
            // if data model has images open image grid view
            openImageGridWindow();
        });

        viewMenu.getItems().addAll(showScatterplotsMI, showScattplotMarginValuesCheckMenuItem, showHistogramsMI, showSummaryStatsMI, showCorrelationsMI,
                polylineDisplayMenu, summaryStatsDisplayModeMenu, axisLayoutMenu, setNumericalAxisExtentsMenuItem,
                changeHistogramBinCountMenuItem, enableDataTableUpdatesCheckMenuItem, openImageGridViewMenuItem);


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

    private void openImageGridWindow() {
        if (imageGridWindow == null) {
            imageGridWindow = new ImageGridWindow(dataTable);
            try {
                imageGridWindowStage = new Stage();
                imageGridWindow.start(imageGridWindowStage);
                imageGridWindow.selectedImagesColorProperty().bind(dataTableView.selectedItemsColorProperty());
                imageGridWindow.unselectedImagesColorProperty().bind(dataTableView.unselectedItemsColorProperty());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            imageGridWindowStage.show();
        }
//        if (ncFilterWindow == null) {
//            ncFilterWindow = new NetCDFFilterWindow(dataTable);
//            try {
//                ncFilterWindowStage = new Stage();
//                ncFilterWindow.start(ncFilterWindowStage);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            ncFilterWindowStage.show();
//        }
    }

    private void syncNumericalAxesRanges() {
        Dialog<double[]> dialog = new Dialog();
        dialog.setTitle("Synchronize Numerical Axis Ranges");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        NumberTextField minValueTextField = new NumberTextField();
        NumberTextField maxValueTextField = new NumberTextField();

        ObservableList<DoubleAxis> doubleAxes = FXCollections.observableArrayList();
        for (int i = 0; i < dataTableView.getAxisCount(); i++) {
            if (dataTableView.getAxis(i) instanceof DoubleAxis) {
                doubleAxes.add((DoubleAxis)dataTableView.getAxis(i));
            }
        }
        ListView<DoubleAxis> axisListView = new ListView<>(doubleAxes);
        axisListView.setPrefHeight(200);
        axisListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        axisListView.getSelectionModel().selectedItemProperty().addListener(observable -> {
            if (minValueTextField.getText().isEmpty()) {
                minValueTextField.setText(String.valueOf(axisListView.getSelectionModel().getSelectedItem().doubleColumn().getStatistics().getMinValue()));
            }
            if (maxValueTextField.getText().isEmpty()) {
                maxValueTextField.setText(String.valueOf(axisListView.getSelectionModel().getSelectedItem().doubleColumn().getStatistics().getMaxValue()));
            }
        });

        Button useSelectedAxisExtentsButton = new Button("Use Range Extents of Selected Axes");
        useSelectedAxisExtentsButton.setAlignment(Pos.CENTER);
        useSelectedAxisExtentsButton.setOnAction(event -> {
            double minValue = Double.NaN;
            double maxValue = Double.NaN;
            for (DoubleAxis axis : axisListView.getSelectionModel().getSelectedItems()) {
                if (Double.isNaN(minValue) || axis.doubleColumn().getStatistics().getMinValue() < minValue) {
                    minValue = axis.doubleColumn().getStatistics().getMinValue();
                }
                if (Double.isNaN(maxValue) || axis.doubleColumn().getStatistics().getMaxValue() > maxValue) {
                    maxValue = axis.doubleColumn().getStatistics().getMaxValue();
                }
            }
            minValueTextField.setText(String.valueOf(minValue));
            maxValueTextField.setText(String.valueOf(maxValue));
        });

        CheckBox setFocusExtentsCB = new CheckBox("Set Focus Extents");
        setFocusExtentsCB.setSelected(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Numerical Axes"), 0, 0);
        grid.add(axisListView, 0, 1, 1, 4);
        grid.add(new Label("Maximum Value:"), 1, 1);
        grid.add(maxValueTextField, 2, 1);
        grid.add(new Label("Minimum Value:"), 1, 2);
        grid.add(minValueTextField, 2, 2);
        grid.add(useSelectedAxisExtentsButton, 1, 3, 2, 1);
        grid.add(setFocusExtentsCB, 1, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> axisListView.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && !axisListView.getSelectionModel().getSelectedItems().isEmpty()) {
                return new double[] {Double.parseDouble(minValueTextField.getText()), Double.parseDouble(maxValueTextField.getText())};
            }
            return null;
        });

        Optional<double[]> result = dialog.showAndWait();
        result.ifPresent(extents -> {
//            System.out.println("min = " + extents[0] + "max = " + extents[1]);
            ArrayList<DoubleColumn> columns = new ArrayList<>();
            for (DoubleAxis axis : axisListView.getSelectionModel().getSelectedItems()) {
                columns.add(axis.doubleColumn());
//                axis.setMaxFocusValue(extents[1]);
//                axis.setMinFocusValue(extents[0]);
            }
            dataTableView.getDataTable().setDoubleColumnScaleExtents(columns, extents[0], extents[1],
                    setFocusExtentsCB.isSelected());
        });
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
            double percentSelected = (double)dataTable.getActiveQuery().getQueriedTupleCount() / dataTable.getTupleCount();
            percentSelectedProgress.setProgress(percentSelected);
            log.info("percentSelected is " + percentSelected);
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

                doubleQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
                temporalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
                categoricalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));

                setDataTableColumns();
                setDataTableItems();

                crossVisStage.setTitle(appTitleString + " -- " + csvFile.getAbsolutePath());
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
        if (dataTableUpdatesEnabled.get()) {
            ObservableList<Tuple> tableTuples;
            tupleTableView.getItems().clear();

            if (dataTable.getActiveQuery().hasColumnSelections()) {
                tableTuples = FXCollections.observableArrayList(dataTable.getActiveQuery().getQueriedTuples());
            } else {
                tableTuples = FXCollections.observableArrayList(dataTable.getTuples());
            }

            tupleTableView.setItems(tableTuples);
        } else {
            tupleTableView.getItems().clear();
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
    public void dataTableReset(DataTable dataTable) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());

        if (dataTable.getImageColumn() != null) {
            openImageGridViewMenuItem.setDisable(false);
        } else {
            openImageGridViewMenuItem.setDisable(true);
        }

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

        imageColumnTableView.getItems().clear();
        ImageColumn imageColumn = dataTable.getImageColumn();
        if (imageColumn != null) {
            imageColumnTableView.setItems(FXCollections.observableArrayList(imageColumn));
        }

        doubleQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));
//        imageQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof ImageColumnSelection));

        setDataTableColumns();
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataTable) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
        doubleQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataTable.getActiveQuery().columnSelectionsProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataTable, Column column) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableColumnSelectionAdded(DataTable dataTable, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableColumnSelectionRemoved(DataTable dataTable, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableColumnSelectionsRemoved(DataTable dataTable, List<ColumnSelection> removedColumnSelections) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataTableColumnSelectionChanged(DataTable dataTable, ColumnSelection columnSelectionRange) {
        removeAllQueriesMI.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
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
    public void dataTableHighlightedColumnChanged(DataTable dataTable, Column oldHighlightedColumn, Column newHighlightedColumn) {
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
    public void dataTableTuplesAdded(DataTable dataTable, ArrayList<Tuple> newTuples) {
        updatePercentSelected();
    }

    @Override
    public void dataTableTuplesRemoved(DataTable dataTable, int numTuplesRemoved) {
        updatePercentSelected();
    }

    @Override
    public void dataTableNumHistogramBinsChanged(DataTable dataTable) {}

    @Override
    public void dataTableStatisticsChanged(DataTable dataTable) { }

    @Override
    public void dataTableColumnExtentsChanged(DataTable dataTable) { }

    @Override
    public void dataTableColumnFocusExtentsChanged(DataTable dataTable) { }

    @Override
    public void dataTableColumnDisabled(DataTable dataTable, Column disabledColumn) {
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
    public void dataTableColumnsDisabled(DataTable dataTable, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataTableColumnEnabled(DataTable dataTable, Column enabledColumn) {

    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn, int index) {

    }

    @Override
    public void dataTableColumnOrderChanged(DataTable dataTable) {

    }

    @Override
    public void dataTableColumnNameChanged(DataTable dataTable, Column column) {

    }
}
