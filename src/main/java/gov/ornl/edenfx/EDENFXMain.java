package gov.ornl.edenfx;

import gov.ornl.pcpview.ColumnSpecification;
import gov.ornl.pcpview.PCPView;
import gov.ornl.datatable.*;
import gov.ornl.pcpview.QueryTableFactory;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
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
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.prefs.Preferences;

/**
 * Created by csg on 8/29/16.
 */
public class EDENFXMain extends Application implements DataModelListener {
    private static final Logger log = LoggerFactory.getLogger(EDENFXMain.class);

    public static final String SPLASH_IMAGE = "EDENFX-SplashScreen.png";
    private static final int SPLASH_WIDTH = 650;
    private static final int SPLASH_HEIGHT = 350;

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

    private TableView<DoubleColumn> doubleColumnTableView;
    private TableView<TemporalColumn> temporalColumnTableView;
    private TableView<CategoricalColumn> categoricalColumnTableView;

    private TableView<ColumnSelectionRange> queryTableView = new TableView<>();
    private TableView<ColumnSelectionRange> doubleQueryTableView;
    private TableView<ColumnSelectionRange> temporalQueryTableView;
    private TableView<ColumnSelectionRange> categoricalQueryTableView;

    private TableView<Tuple> dataTableView;
    private MenuItem removeAllQueriesMI;
    private MenuItem exportSelectedDataMI;
    private MenuItem exportUnselectedDataMI;

    // toggle group for toolbar buttons to change display mode of PCPView
    private ToggleGroup displayModeMenuGroup;
    private RadioMenuItem summaryDisplayModeMenuItem;
    private RadioMenuItem histogramDisplayModeMenuItem;
    private RadioMenuItem binDisplayModeMenuItem;
    private RadioMenuItem lineDisplayModeMenuItem;

    private ToggleGroup displayModeButtonGroup;
    private ToggleButton summaryDisplayModeButton;
    private ToggleButton histogramDisplayModeButton;
    private ToggleButton binDisplayModeButton;
    private ToggleButton lineDisplayModeButton;

    private ProgressBar percentSelectedProgress;
    private DecimalFormat decimalFormat;
    private StatusBar statusBar;
    private MenuItem removeSelectedDataMenuItem;
    private MenuItem removeUnselectedDataMenuItem;

    private CheckMenuItem enableDataTableUpdatesCheckMenuItem;
    private MenuItem changeHistogramBinCountMenuItem;

    private ProgressBar loadProgress;
    private Label loadProgressText;
    private VBox splashLayout;

    private Stage mainStage;

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        String copyrightString = "© " + LocalDate.now().getYear() + " All Rights Reserved";
        Text copyrightText = new Text(splash.getLayoutBounds().getWidth()/2., 100., copyrightString);
        copyrightText.setFont(Font.font(16.));
        copyrightText.setFill(Color.WHITE);
        copyrightText.setTranslateY(80.);
        StackPane pane = new StackPane();
        pane.getChildren().add(splash);
        pane.getChildren().add(copyrightText);

        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        loadProgressText = new Label("Loading EDENFX . . .");
        loadProgressText.setTextFill(Color.WHITESMOKE);
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(pane, loadProgress, loadProgressText);
        splashLayout.setStyle(
                "-fx-padding: 5; " +
                        "-fx-background-color: black; " +
                        "-fx-border-width:5; " +
                        "-fx-border-color: black;"
        );
        splashLayout.setEffect(new DropShadow());

        preferences = Preferences.userNodeForPackage(this.getClass());

        decimalFormat = new DecimalFormat("##0.0%");
        displayModeMap = new HashMap<>();
        displayModeMap.put("Summary", PCPView.DISPLAY_MODE.SUMMARY);
        displayModeMap.put("Histograms", PCPView.DISPLAY_MODE.HISTOGRAM);
        displayModeMap.put("Parallel Coordinates Bins", PCPView.DISPLAY_MODE.PCP_BINS);
        displayModeMap.put("Parallel Coordinates Lines", PCPView.DISPLAY_MODE.PCP_LINES);

        dataModel = new DataModel();
        dataModel.addDataModelListener(this);
    }

//    private void createQueryTableView() {
//        queryTableView = new TableView();
//        queryTableView.setEditable(true);
//
//        TableColumn<ColumnSelectionRange, String> columnNameColumn = new TableColumn<>("Column");
//        columnNameColumn.setMinWidth(160);
//        columnNameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSelectionRange, String>("column"));
//
//        TableColumn<ColumnSelectionRange, String> minColumn = new TableColumn<>("Minimum Value");
//        minColumn.setMinWidth(200);
//        minColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ColumnSelectionRange, String>, ObservableValue<String>>() {
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<ColumnSelectionRange, String> t) {
//                if (t.getValue() instanceof TemporalColumnSelectionRange) {
//                    return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getStartInstant().toString());
//                } else {
//                    return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMinValue()));
//                }
//            }
//        });
//        minColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelectionRange, String> t) -> {
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
//                    queryTableView.refresh();
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
//                    queryTableView.refresh();
//                }
//            }
//        });
//        minColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        minColumn.setEditable(true);
//
//        TableColumn<ColumnSelectionRange, String> maxColumn = new TableColumn<>("Maximum Value");
//        maxColumn.setMinWidth(200);
//        maxColumn.setCellValueFactory(t -> {
//            if (t.getValue() instanceof TemporalColumnSelectionRange) {
//                return new ReadOnlyObjectWrapper(((TemporalColumnSelectionRange)t.getValue()).getEndInstant().toString());
//            } else {
//                return new ReadOnlyObjectWrapper(String.valueOf(((DoubleColumnSelectionRange)t.getValue()).getMaxValue()));
//            }
//        });
//        maxColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        maxColumn.setOnEditCommit((TableColumn.CellEditEvent<ColumnSelectionRange, String> t) -> {
//            if (t.getRowValue() instanceof TemporalColumnSelectionRange) {
//                try {
//                    Instant instant = Instant.parse(t.getNewValue());
//                    ((TemporalColumnSelectionRange) t.getRowValue()).setEndInstant(instant);
//                } catch (DateTimeParseException ex) {
//                    // show alert dialog
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Value Parsing Error");
//                    alert.setHeaderText("An Exception Occurred While Parsing New End Time");
//                    alert.setContentText(ex.getMessage());
//                    alert.showAndWait();
//                    queryTableView.refresh();
//                }
//            } else {
//                try {
//                    Double doubleValue = Double.parseDouble(t.getNewValue());
//                    ((DoubleColumnSelectionRange) t.getRowValue()).setMaxValue(doubleValue);
//                } catch (NumberFormatException ex) {
//                    // show alert dialog
//                    Alert alert = new Alert(Alert.AlertType.ERROR);
//                    alert.setTitle("Value Parsing Error");
//                    alert.setHeaderText("An Exception Occurred While Parsing New Maximum Value");
//                    alert.setContentText(ex.getMessage());
//                    alert.showAndWait();
//                    queryTableView.refresh();
//                }
//            }
//        });
//        maxColumn.setEditable(true);
//
//        queryTableView.getColumns().addAll(columnNameColumn, minColumn, maxColumn);
//    }

    private void createColumnTableViews() {
        // create table view for categorical columns
        categoricalColumnTableView = new TableView<>();
        categoricalColumnTableView.setEditable(true);
        categoricalColumnTableView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataModel.setHighlightedColumn(newValue);
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
                                    dataModel.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataModel.disableColumn(column);
                                }
                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });
        categoricalEnabledColumn.setEditable(true);

//        TableColumn <TemporalColumn, Instant> startColumn = new TableColumn<>("Start");
//        startColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TemporalColumn, Instant>, ObservableValue<Instant>>() {
//            public ObservableValue<Instant> call(TableColumn.CellDataFeatures<TemporalColumn, Instant> t) {
//                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getStartInstant());
//            }
//        });
//
//        TableColumn <TemporalColumn, Instant> endColumn = new TableColumn<>("End");
//        endColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TemporalColumn, Instant>, ObservableValue<Instant>>() {
//            public ObservableValue<Instant> call(TableColumn.CellDataFeatures<TemporalColumn, Instant> t) {
//                return new ReadOnlyObjectWrapper(t.getValue().getStatistics().getEndInstant());
//            }
//        });

        categoricalColumnTableView.getColumns().addAll(categoricalEnabledColumn, categoricalNameColumn);

        // create table view for temporal columns
        temporalColumnTableView = new TableView<>();
        temporalColumnTableView.setEditable(true);
        temporalColumnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataModel.setHighlightedColumn(newValue);
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
                                    dataModel.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = getTableView().getItems().get(rowNumber);
                                    dataModel.disableColumn(column);
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
        

        // create table view for double columns
        doubleColumnTableView = new TableView<>();
        doubleColumnTableView.setEditable(true);
        doubleColumnTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataModel.setHighlightedColumn(newValue);
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
                                    dataModel.enableColumn(column);
                                } else {
                                    // disable an enabled column
                                    // get the column name; disable column in data model
                                    Column column = doubleColumnTableView.getItems().get(rowNumber);
                                    dataModel.disableColumn(column);
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

        // Make toggle button group for display mode shortcut buttons
//        displayModeButtonMap = new HashMap<>();
        summaryDisplayModeButton = new ToggleButton("S");
        summaryDisplayModeButton.setTooltip(new Tooltip("Summary Display Mode"));
//        displayModeButtonMap.put(PCPView.DISPLAY_MODE.HISTOGRAM, summaryDisplayModeButton);
        histogramDisplayModeButton = new ToggleButton("H");
        histogramDisplayModeButton.setTooltip(new Tooltip("DoubleHistogram Display Mode"));
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
        } else if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.SUMMARY) {
            displayModeButtonGroup.selectToggle(summaryDisplayModeButton);
        }

        displayModeButtonGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue == summaryDisplayModeButton) {
                    pcpView.setDisplayMode(PCPView.DISPLAY_MODE.SUMMARY);
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
            }
        });

        // make toggle buttons for showing/hiding selected/unselected items
        ToggleButton showUnselectedButton = new ToggleButton("U");
        showUnselectedButton.setTooltip(new Tooltip("Show Unselected Items"));
        showUnselectedButton.selectedProperty().bindBidirectional(pcpView.showUnselectedItemsProperty());
        ToggleButton showSelectedButton = new ToggleButton("S");
        showSelectedButton.selectedProperty().bindBidirectional(pcpView.showSelectedItemsProperty());
        showSelectedButton.setTooltip(new Tooltip("Show Selected Items"));

        // create selected items color modification UI components
        HBox selectedItemsColorBox = new HBox();
        selectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker selectedItemsColorPicker = new ColorPicker();
        selectedItemsColorPicker.valueProperty().bindBidirectional(pcpView.selectedItemsColorProperty());
        selectedItemsColorPicker.getStyleClass().add("button");
//        selectedItemsColorPicker.setOnAction(event -> {
//            Color c = new Color(selectedItemsColorPicker.getValue().getRed(),
//                    selectedItemsColorPicker.getValue().getGreen(),
//                    selectedItemsColorPicker.getValue().getBlue(),
//                    pcpView.getOpacity());
//            selectedItemsColorPicker.setValue(c);
//        });

        selectedItemsColorBox.getChildren().addAll(new Label(" Selected Items: "), selectedItemsColorPicker);

        // create unselected items color modification UI components
        HBox unselectedItemsColorBox = new HBox();
        unselectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker unselectedItemsColorPicker = new ColorPicker();
        unselectedItemsColorPicker.getStyleClass().add("button");
        unselectedItemsColorPicker.valueProperty().bindBidirectional(pcpView.unselectedItemsColorProperty());
        unselectedItemsColorBox.getChildren().addAll(new Label(" Unselected Items: "), unselectedItemsColorPicker);

        // create opacity slider
        HBox opacityBox = new HBox();
        opacityBox.setAlignment(Pos.CENTER);

        Slider opacitySlider = new Slider(0., 1., pcpView.getDataItemsOpacity());
        opacitySlider.valueProperty().bindBidirectional(pcpView.dataItemsOpacityProperty());
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);

        opacityBox.getChildren().addAll(new Label(" Opacity: "), opacitySlider);

//        // create unselected items color modification UI components
//        HBox backgroundColorBox = new HBox();
//        backgroundColorBox.setAlignment(Pos.CENTER);
//
//        ColorPicker backgroundColorPicker = new ColorPicker();
//        backgroundColorPicker.getStyleClass().add("button");
//        backgroundColorPicker.valueProperty().bindBidirectional(pcpView.backgroundColorProperty());
//        backgroundColorBox.getChildren().addAll(new Label(" Background: "), backgroundColorPicker);
//
//        // create label color UI components
//        HBox labelColorBox = new HBox();
//        labelColorBox.setAlignment(Pos.CENTER);
//
//        ColorPicker labelColorPicker = new ColorPicker();
//        labelColorPicker.getStyleClass().add("button");
//        labelColorPicker.valueProperty().bindBidirectional(pcpView.labelsColorProperty());
//        labelColorBox.getChildren().addAll(new Label(" Labels: "), labelColorPicker);
//
//        // create name text rotation spinner
//        HBox nameTextRotationBox = new HBox();
//        nameTextRotationBox.setAlignment(Pos.CENTER);
//
//        Spinner<Double> nameTextRotationSpinner = new Spinner<Double>(-30., 30., pcpView.getNameTextRotation());
//        pcpView.nameTextRotationProperty().bind(nameTextRotationSpinner.valueProperty());
//        nameTextRotationSpinner.setEditable(true);
//        nameTextRotationBox.getChildren().addAll(new Label(" Name Label Rotation: "), nameTextRotationSpinner);

        // add all items to layout
        toolBar.getItems().addAll(summaryDisplayModeButton, histogramDisplayModeButton, binDisplayModeButton, lineDisplayModeButton, new Separator(),
                showUnselectedButton, showSelectedButton, new Separator(), selectedItemsColorBox, unselectedItemsColorBox, new Separator(), opacityBox);
//        , backgroundColorBox, labelColorBox, nameTextRotationBox);

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
    public void start(final Stage initStage) throws Exception {
        final Task<Integer> startUpTask = new Task<Integer>() {
            @Override
            public Integer call() throws InterruptedException {
                updateMessage("Initializing . . . ");
                for (int i = 0; i < 20; i++) {
                    Thread.sleep(150);
                    updateProgress(i+1, 10);
                }
                Thread.sleep(100);
                updateMessage("Finished initializing.");

                return 1;
            }
        };

        showSplash(initStage, startUpTask, () -> showMainStage());
        new Thread(startUpTask).start();
    }

    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        loadProgressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.0), splashLayout);
                fadeSplash.setFromValue(1d);
                fadeSplash.setToValue(0d);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            }
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
    }

    public interface InitCompletionHandler {
        void complete();
    }

    private void showMainStage () {
        mainStage = new Stage(StageStyle.DECORATED);

        pcpView = new PCPView();
        pcpView.setDataModel(dataModel);
        pcpView.setPrefHeight(400);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpScrollPane = new ScrollPane(pcpView);
        pcpScrollPane.setFitToHeight(true);
        pcpScrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());

        ToolBar toolBar = createToolBar(mainStage);

        createStatusBar();
//        statusBar.progressProperty().bindBidirectional(pcpView.drawingProgressProperty());

        MenuBar menuBar = createMenuBar(mainStage);
//        menuBar.setUseSystemMenuBar(true);
        createColumnTableViews();
//        createQueryTableView();
        dataTableView = new TableView<>();

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
        Tab quantitativeColumnTableTab = new Tab(" Quantitative Column Table ");
        quantitativeColumnTableTab.setClosable(false);
        quantitativeColumnTableTab.setContent(doubleColumnTableView);

        Tab temporalColumnTableTab = new Tab(" Temporal Column Table ");
        temporalColumnTableTab.setClosable(false);
        temporalColumnTableTab.setContent(temporalColumnTableView);

        Tab categoricalColumnTableTab = new Tab(" Categorical Column Table ");
        categoricalColumnTableTab.setClosable(false);
        categoricalColumnTableTab.setContent(categoricalColumnTableView);

        Tab dataTableTab = new Tab(" Data Table ");
        dataTableTab.setClosable(false);
        dataTableTab.setContent(dataTableView);

        Tab queryTableTab = new Tab(" Query Table ");
        queryTableTab.setClosable(false);
//        queryTableGrid.prefWidthProperty().bind(tabPane.widthProperty());
        queryTableTab.setContent(queryTablePane);
//        queryTableTab.setContent(queryTableView);

        tabPane.getTabs().addAll(temporalColumnTableTab, quantitativeColumnTableTab, categoricalColumnTableTab, dataTableTab, queryTableTab);

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

        Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(rootNode, screenVisualBounds.getWidth() - 20, 800, true, SceneAntialiasing.BALANCED);

        mainStage.setTitle("EDEN.FX");
        mainStage.setScene(scene);
        mainStage.show();
    }

    @Override
    public void stop() {
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
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, (int)pcpView.getPCPVerticalBarHeight(), dataModel.getNumHistogramBins()));
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
            if (numHistogramBins != dataModel.getNumHistogramBins()) {
                dataModel.setNumHistogramBins(numHistogramBins);
            }
            event.consume();
        });

        final Button buttonOK = (Button)numHistogramBinsDialog.getDialogPane().lookupButton(ButtonType.OK);
        buttonOK.setDisable(true);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getValue() != dataModel.getNumHistogramBins()) {
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
            dataModel.setNumHistogramBins(numHistogramBins);
        });
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
                String lastCSVDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_READ_DIRECTORY, "");
                if (!lastCSVDirectoryPath.isEmpty()) {
                    File lastCSVDirectory = new File(lastCSVDirectoryPath);
                    if (lastCSVDirectory != null && lastCSVDirectory.exists() && lastCSVDirectory.canRead()) {
                        fileChooser.setInitialDirectory(new File(lastCSVDirectoryPath));
                    }
                }
                fileChooser.setTitle("Open CSV File");
                File csvFile = fileChooser.showOpenDialog(stage);
                if (csvFile != null) {
                    try {
                        openCSVFile(csvFile);
                        displayModeMenu.setDisable(false);
                        fitPCPAxesToWidthCheckMI.setDisable(false);
                        changeAxisSpacingMI.setDisable(pcpView.getFitAxisSpacingToWidthEnabled());
                        preferences.put(EDENFXPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        exportSelectedDataMI = new MenuItem("Export Selected Data...");
        exportSelectedDataMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        exportSelectedDataMI.setOnAction(event -> {
            if (dataModel.getActiveQuery().getQueriedTuples().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText(null);
                alert.setContentText("No tuples are currently selected.  Export operation canceled.");
                alert.showAndWait();
                return;
            }

            FileChooser fileChooser = new FileChooser();
            String lastExportDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
            if (!lastExportDirectoryPath.isEmpty()) {
                File lastExportDirectory = new File(lastExportDirectoryPath);
                if (lastExportDirectory != null && lastExportDirectory.exists() && lastExportDirectory.canRead()) {
                    fileChooser.setInitialDirectory(new File(lastExportDirectoryPath));
                }
            }
            fileChooser.setTitle("Export Selected Data to CSV File");
            File csvFile = fileChooser.showSaveDialog(stage);
            if (csvFile != null) {
                try {
                    exportDataToCSV(csvFile, true);
                    preferences.put(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        exportUnselectedDataMI = new MenuItem("Export Unselected Data...");
        exportUnselectedDataMI.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.META_DOWN));
        exportUnselectedDataMI.setOnAction(event -> {
            if (dataModel.getActiveQuery().getQueriedTuples().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("No Tuples Selected");
                alert.setHeaderText(null);
                alert.setContentText("No selections are set so all tuples will be exported.  Export all data?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.CANCEL) {
                    return;
                }
            }

            FileChooser fileChooser = new FileChooser();
            String lastExportDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
            if (!lastExportDirectoryPath.isEmpty()) {
                File lastExportDirectory = new File(lastExportDirectoryPath);
                if (lastExportDirectory != null && lastExportDirectory.exists() && lastExportDirectory.canRead()) {
                    fileChooser.setInitialDirectory(new File(lastExportDirectoryPath));
                }
            }
            fileChooser.setTitle("Export Unselected Data to CSV File");
            File csvFile = fileChooser.showSaveDialog(stage);
            if (csvFile != null) {
                try {
                    exportDataToCSV(csvFile, false);
                    preferences.put(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        displayModeMenu = new Menu("Display Mode");
        displayModeMenu.setDisable(true);
        displayModeMenuGroup = new ToggleGroup();

        summaryDisplayModeMenuItem = new RadioMenuItem("Summary");
        summaryDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.SUMMARY) {
            summaryDisplayModeMenuItem.setSelected(true);
        }
        histogramDisplayModeMenuItem = new RadioMenuItem("Histograms");
        histogramDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.HISTOGRAM) {
            histogramDisplayModeMenuItem.setSelected(true);
        }
        binDisplayModeMenuItem = new RadioMenuItem("Parallel Coordinates Bins");
        binDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_BINS) {
            binDisplayModeMenuItem.setSelected(true);
        }
        lineDisplayModeMenuItem = new RadioMenuItem("Parallel Coordinates Lines");
        lineDisplayModeMenuItem.setToggleGroup(displayModeMenuGroup);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_LINES) {
            lineDisplayModeMenuItem.setSelected(true);
        }
        displayModeMenu.getItems().addAll(summaryDisplayModeMenuItem, histogramDisplayModeMenuItem, binDisplayModeMenuItem, lineDisplayModeMenuItem);
        viewMenu.getItems().add(displayModeMenu);

        // create listener for display mode menu group
        displayModeMenuGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                    PCPView.DISPLAY_MODE newDisplayMode = displayModeMap.get(toggleItem.getText());
                    if (newDisplayMode == PCPView.DISPLAY_MODE.HISTOGRAM) {
                        displayModeButtonGroup.selectToggle(histogramDisplayModeButton);
                    } else if (newDisplayMode == PCPView.DISPLAY_MODE.PCP_BINS) {
                        displayModeButtonGroup.selectToggle(binDisplayModeButton);
                    } else if (newDisplayMode == PCPView.DISPLAY_MODE.PCP_LINES) {
                        displayModeButtonGroup.selectToggle(lineDisplayModeButton);
                    } else if (newDisplayMode == PCPView.DISPLAY_MODE.SUMMARY) {
                        displayModeButtonGroup.selectToggle(summaryDisplayModeButton);
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

        Menu removeDataMenu = new Menu("Remove Data");
        // create menu item to remove selected data
        removeSelectedDataMenuItem = new MenuItem("Remove Selected Data");
        removeSelectedDataMenuItem.setOnAction(event -> {
            int removedTuples = dataModel.removeSelectedTuples();
            log.debug("Removed " + removedTuples + " tuples");
        });

        // create menu item to keep selected data and remove unselected data
        removeUnselectedDataMenuItem = new MenuItem("Remove Unselected Data");
        removeUnselectedDataMenuItem.setOnAction(event -> {
            int removedTuples = dataModel.removeUnselectedTuples();
            log.debug("Removed " + removedTuples + " tuples");
        });
        removeDataMenu.getItems().addAll(removeSelectedDataMenuItem, removeUnselectedDataMenuItem);
        viewMenu.getItems().add(removeDataMenu);

        // create menu item to remove all active queries
        removeAllQueriesMI = new MenuItem("Remove All Range Queries");
        removeAllQueriesMI.setDisable(true);
        removeAllQueriesMI.setOnAction(event -> {
            pcpView.clearQuery();
        });
        viewMenu.getItems().add(removeAllQueriesMI);

        // create menu item to enabled/disable data datamodel updates
        enableDataTableUpdatesCheckMenuItem = new CheckMenuItem("Enable Data Table Updates");
        enableDataTableUpdatesCheckMenuItem.setSelected(false);
        enableDataTableUpdatesCheckMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue) {
                // refresh data datamodel based on current state of the data model
                setDataTableItems();
            } else {
                dataTableView.getItems().clear();
            }
        });
        viewMenu.getItems().add(enableDataTableUpdatesCheckMenuItem);

        // change histogram bin count menu item
        changeHistogramBinCountMenuItem = new MenuItem("Change Number of Histogram Bins...");
        changeHistogramBinCountMenuItem.setOnAction(event -> {
            changeNumHistogramBins();
        });
        viewMenu.getItems().add(changeHistogramBinCountMenuItem);


        MenuItem exitMI = new MenuItem("Quit EDEN.fx");
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.close();
            }
        });

        edenfxMenu.getItems().addAll(exitMI);
        fileMenu.getItems().addAll(openCSVMI, exportSelectedDataMI, exportUnselectedDataMI);

        return menuBar;
    }

    private void exportDataToCSV (File csvFile, boolean exportQueriedData) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        // write header line with column names
        StringBuffer headerLine = new StringBuffer();
        for (int icol = 0; icol < dataModel.getColumnCount(); icol++) {
            Column column = dataModel.getColumn(icol);
            if (headerLine.length() == 0) {
                headerLine.append(column.getName());
            } else {
                headerLine.append(", " + column.getName());
            }
        }

        writer.write(headerLine.toString().trim() + "\n");

        // get data
        Set<Tuple> tuples = null;
        if (exportQueriedData) {
            tuples = dataModel.getActiveQuery().getQueriedTuples();
        } else {
            tuples = dataModel.getActiveQuery().getNonQueriedTuples();
        }

        // write to csv file
        int tupleCounter = 0;
        for (Tuple tuple : tuples) {
            StringBuffer lineBuffer = new StringBuffer();
            for (int i = 0; i < tuple.getElementCount(); i++) {
                if (lineBuffer.length() == 0) {
                    lineBuffer.append(tuple.getElement(i));
                } else {
                    lineBuffer.append(", " + tuple.getElement(i));
                }
            }

            if (tupleCounter == 0) {
                writer.write(lineBuffer.toString().trim());
            } else {
                writer.write("\n" + lineBuffer.toString().trim());
            }

            tupleCounter++;
        }

        writer.close();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (exportQueriedData) {
            alert.setTitle("Selected Data Exported");
        } else {
            alert.setTitle("Unselected Data Exported");
        }
        alert.setHeaderText(null);
        alert.setContentText("Successfully exported " + tuples.size() + " tuples to '" + csvFile.getName() + "'");
        alert.showAndWait();
    }

    private void updatePercentSelected() {
        if (dataModel != null && !dataModel.isEmpty()) {
            double percentSelected = (double) dataModel.getActiveQuery().getQueriedTupleCount() / dataModel.getTupleCount();
            percentSelectedProgress.setProgress(percentSelected);
            statusBar.setText(" " + dataModel.getActiveQuery().getQueriedTupleCount() + " of " + dataModel.getTupleCount() + " tuples selected (" + decimalFormat.format(percentSelected) + ")");
        } else {
            statusBar.setText(" Ready ");
        }
    }

    private void openCSVFile(File f) throws IOException {
        String columnNames[] = IOUtilities.readCSVHeader(f);
        int lineCount = IOUtilities.getCSVLineCount(f);

        ArrayList<ColumnSpecification> columnSpecifications = getColumnSpecifications(columnNames);
        if (columnSpecifications == null) {
            return;
        }

        if (dataModel != null) {
            // TODO: Clear the data model from the PCPView
            dataModel.clear();
        }

        ArrayList<String> temporalColumnNames = new ArrayList<>();
        ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
        ArrayList<String> ignoreColumnNames = new ArrayList<>();
        ArrayList<String> categoricalColumnNames = new ArrayList<>();

        String lastDateTimeParsePattern = null;
        for (ColumnSpecification columnSpecification : columnSpecifications) {
            if (columnSpecification.getIgnore()) {
                ignoreColumnNames.add(columnSpecification.getName());
            } else if (columnSpecification.getType().equalsIgnoreCase("Temporal")) {
                temporalColumnNames.add(columnSpecification.getName());
                temporalColumnFormatters.add(DateTimeFormatter.ofPattern(columnSpecification.getParsePattern()));
                lastDateTimeParsePattern = columnSpecification.getParsePattern();
            } else if (columnSpecification.getType().equalsIgnoreCase("Categorical")) {
                categoricalColumnNames.add(columnSpecification.getName());
            }
        }

        long start = System.currentTimeMillis();
        IOUtilities.readCSV(f, ignoreColumnNames, categoricalColumnNames, temporalColumnNames, temporalColumnFormatters, dataModel);
        long elasped = System.currentTimeMillis() - start;
        log.debug("Reading file data took " + elasped + "ms");

        if (lastDateTimeParsePattern != null) {
            preferences.put(EDENFXPreferenceKeys.LAST_CSV_IMPORT_DATETIME_PARSE_PATTERN, lastDateTimeParsePattern);
        }

        temporalColumnTableView.getItems().clear();
        ArrayList<TemporalColumn> temporalColumns = dataModel.getTemporalColumns();
        if (temporalColumns != null && !temporalColumns.isEmpty()) {
            temporalColumnTableView.setItems(FXCollections.observableArrayList(temporalColumns));
        }

        doubleColumnTableView.getItems().clear();
        ArrayList<DoubleColumn> doubleColumns = dataModel.getDoubleColumns();
        if (doubleColumns != null && !doubleColumns.isEmpty()) {
            doubleColumnTableView.setItems(FXCollections.observableArrayList(doubleColumns));
        }

        categoricalColumnTableView.getItems().clear();
        ArrayList<CategoricalColumn> categoricalColumns = dataModel.getCategoricalColumns();
        if (categoricalColumns != null && !categoricalColumns.isEmpty()) {
            categoricalColumnTableView.setItems(FXCollections.observableArrayList(categoricalColumns));
        }

//        doubleQueryTableView.getItems().clear();
//        temporalQueryTableView.getItems().clear();
//        categoricalQueryTableView.getItems().clear();
        doubleQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));
//        doubleQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty());
//        temporalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty());
//        categoricalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty());
//        queryTableView.getItems().clear();
//        queryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty());
        
        setDataTableColumns();

        start = System.currentTimeMillis();
        setDataTableItems();
        elasped = System.currentTimeMillis() - start;
        log.debug("Setting data datamodel items took " + elasped + "ms");

        displayModeMenu.setDisable(false);
    }

    private ArrayList<ColumnSpecification> getColumnSpecifications (String columnNames[]) {
        ObservableList<ColumnSpecification> tableColumnSpecs = FXCollections.observableArrayList();
        for (String columnName : columnNames) {
            ColumnSpecification columnSpec = new ColumnSpecification(columnName, "Double",
                    "", false);
            tableColumnSpecs.add(columnSpec);
        }
        final ObservableList<ColumnSpecification> temporalColumnSpecs = FXCollections.observableArrayList();
        final ObservableList<ColumnSpecification> categoricalColumnSpecs = FXCollections.observableArrayList();

        Dialog<ObservableList<ColumnSpecification>> dialog = new Dialog<>();
        dialog.setTitle("CSV Column Specifications");
        dialog.setHeaderText("Specify Details for each Column");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create tableview for temporal column details
        TableView<ColumnSpecification> temporalSpecsTableView = new TableView<>();
        temporalSpecsTableView.setEditable(true);

        TableColumn<ColumnSpecification, String> nameColumn = new TableColumn<>("Column Name");
        nameColumn.setMinWidth(180);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSpecification, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(false);

        ArrayList<String> parsePatterns = new ArrayList<>();
        if (!preferences.get(EDENFXPreferenceKeys.LAST_CSV_IMPORT_DATETIME_PARSE_PATTERN, "").isEmpty()) {
            parsePatterns.add(preferences.get(EDENFXPreferenceKeys.LAST_CSV_IMPORT_DATETIME_PARSE_PATTERN, ""));
        }
        parsePatterns.add(preferences.get(EDENFXPreferenceKeys.DEFAULT_CSV_IMPORT_DATETIME_PARSE_PATTERN,
                "yyyy-MM-ddTHH:mm:ssz"));
        parsePatterns.add("dd.MM.yyy HH:mm:ss");

        TableColumn<ColumnSpecification, String> parseColumn = new TableColumn<>("DateTime Parse Pattern");
        parseColumn.setMinWidth(180);
        parseColumn.setCellValueFactory(new PropertyValueFactory<ColumnSpecification, String>("parsePattern"));
        parseColumn.setCellFactory(new Callback<TableColumn<ColumnSpecification, String>, TableCell<ColumnSpecification, String>>() {
            @Override
            public TableCell<ColumnSpecification, String> call(TableColumn<ColumnSpecification, String> param) {
                return new ComboBoxTableCell<ColumnSpecification, String>(FXCollections.observableArrayList(parsePatterns)) {
                    {
                        setComboBoxEditable(true);
                    }
                };
            }
        });
        parseColumn.setEditable(true);
        
        temporalSpecsTableView.getColumns().addAll(nameColumn, parseColumn);
        temporalColumnSpecs.addAll(temporalColumnSpecs);
        
        // create tableview for other column details
        TableView<ColumnSpecification> columnSpecsTableView = new TableView<>();
        columnSpecsTableView.setEditable(true);

        nameColumn = new TableColumn<>("Column Name");
        nameColumn.setMinWidth(180);
        nameColumn.setCellValueFactory(new PropertyValueFactory<ColumnSpecification, String>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(false);

        TableColumn<ColumnSpecification, Boolean> ignoreColumn = new TableColumn<>("Ignore");
        ignoreColumn.setMinWidth(20);
        ignoreColumn.setCellValueFactory(new PropertyValueFactory<ColumnSpecification, Boolean>("ignore"));
        ignoreColumn.setCellFactory(column -> new CheckBoxTableCell());
        ignoreColumn.setEditable(true);

        TableColumn<ColumnSpecification, String> typeColumn = new TableColumn<>("Column Type");
        typeColumn.setMinWidth(180);
        typeColumn.setCellValueFactory(new PropertyValueFactory<ColumnSpecification, String>("type"));
        typeColumn.setEditable(true);
        typeColumn.setCellFactory(new Callback<TableColumn<ColumnSpecification, String>, TableCell<ColumnSpecification, String>>() {
            @Override
            public TableCell<ColumnSpecification, String> call(TableColumn<ColumnSpecification, String> param) {
                return new ComboBoxTableCell<ColumnSpecification, String>(FXCollections.observableArrayList("Temporal", "Double", "Categorical")) {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        if (!empty) {
                            TableRow row = getTableRow();
                            if (row != null) {
                                int rowNumber = row.getIndex();
//                                TableView.TableViewSelectionModel sm = getTableView().getSelectionModel();
                                if (item.equalsIgnoreCase("Temporal")) {
                                    // add row to temporal column specs table
                                    if (!temporalSpecsTableView.getItems().contains(tableColumnSpecs.get(rowNumber))) {
                                        temporalSpecsTableView.getItems().add(tableColumnSpecs.get(rowNumber));
                                    }
                                } else if (item.equalsIgnoreCase("Double")) {
                                    // check temporal column specs table for column and remove if found
                                    temporalSpecsTableView.getItems().remove(tableColumnSpecs.get(rowNumber));
                                } else if (item.equalsIgnoreCase("Categorical")) {
                                    temporalSpecsTableView.getItems().remove(tableColumnSpecs.get(rowNumber));
                                }
                            }
                        }
                        super.updateItem(item, empty);
                    }
                };
            }
        });

        columnSpecsTableView.getColumns().addAll(ignoreColumn, nameColumn, typeColumn);
        columnSpecsTableView.setItems(tableColumnSpecs);

        columnSpecsTableView.setPrefHeight(150.);
        temporalSpecsTableView.setPrefHeight(100.);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("General Column Parameters:"), 0, 0);
        grid.add(columnSpecsTableView, 0, 1);
        grid.add(new Label("Temporal Column Parameters: "), 0, 2);
        grid.add(temporalSpecsTableView, 0, 3);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> columnSpecsTableView.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return tableColumnSpecs;
            }
            return null;
        });

        Optional<ObservableList<ColumnSpecification>> result = dialog.showAndWait();

        if (result.isPresent()) {
            return new ArrayList<ColumnSpecification>(result.get());
        }

        return null;
    }

    private void setDataTableItems() {
        if (enableDataTableUpdatesCheckMenuItem.isSelected()) {
            ObservableList<Tuple> tableTuples;
            dataTableView.getItems().clear();

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                tableTuples = FXCollections.observableArrayList(dataModel.getActiveQuery().getQueriedTuples());
            } else {
                tableTuples = FXCollections.observableArrayList(dataModel.getTuples());
            }

            dataTableView.setItems(tableTuples);
        }
    }

    private void setDataTableColumns() {
        dataTableView.getColumns().clear();

        // make a column for each enabled data datamodel column
        if (!dataModel.isEmpty()) {
            for (int icol = 0; icol < dataModel.getColumnCount(); icol++) {
                Column column = dataModel.getColumn(icol);
                if (column instanceof TemporalColumn) {
                    TemporalColumn temporalColumn = (TemporalColumn)column;
                    TableColumn<Tuple, Instant> tableColumn = new TableColumn<Tuple, Instant>(temporalColumn.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Instant>, ObservableValue<Instant>>() {
                        public ObservableValue<Instant> call(TableColumn.CellDataFeatures<Tuple, Instant> t) {
                            int columnIndex = dataModel.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.debug("Weird!");
                            }
                            return new ReadOnlyObjectWrapper((Instant)t.getValue().getElement(columnIndex));

                        }
                    });
                    dataTableView.getColumns().add(tableColumn);
                } else if (column instanceof DoubleColumn) {
                    TableColumn<Tuple, Double> tableColumn = new TableColumn<>(column.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, Double>, ObservableValue<Double>>() {
                        public ObservableValue<Double> call(TableColumn.CellDataFeatures<Tuple, Double> t) {
                            int columnIndex = dataModel.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.debug("Weird!");
                            }
                            return new ReadOnlyObjectWrapper((Double)t.getValue().getElement(columnIndex));

                        }
                    });
                    dataTableView.getColumns().add(tableColumn);
                } else if (column instanceof CategoricalColumn) {
                    TableColumn<Tuple, String> tableColumn = new TableColumn<>(column.getName());
                    tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Tuple, String>, ObservableValue<String>>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<Tuple, String> param) {
                            int columnIndex = dataModel.getColumnIndex(column);
                            if (columnIndex == -1) {
                                log.debug("Weird!");
                            }
                            return new ReadOnlyObjectWrapper<>((String)param.getValue().getElement(columnIndex));
                        }
                    });
                    dataTableView.getColumns().add(tableColumn);
                }
            }
        }
    }

    @Override
    public void dataModelReset(DataModel dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
//        doubleQueryTableView.getItems().clear();
//        temporalQueryTableView.getItems().clear();
//        categoricalQueryTableView.getItems().clear();
        setDataTableItems();
        updatePercentSelected();
    }

    @Override
    public void dataModelQueryCleared(DataModel dataModel) {
        removeAllQueriesMI.setDisable(!dataModel.getActiveQuery().hasColumnSelections());
        setDataTableItems();
        updatePercentSelected();

//        doubleQueryTableView.getItems().clear();
//        temporalQueryTableView.getItems().clear();
//        categoricalQueryTableView.getItems().clear();
//        Predicate<ColumnSelectionRange> isDoubleSelection = (selection -> selection instanceof DoubleColumnSelectionRange);
        doubleQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof DoubleColumnSelectionRange));
        temporalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof TemporalColumnSelectionRange));
        categoricalQueryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty().filtered(selection -> selection instanceof CategoricalColumnSelection));
//        queryTableView.getItems().clear();
//        queryTableView.setItems(dataModel.getActiveQuery().columnSelectionRangesProperty());
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
    public void dataModelHighlightedColumnChanged(DataModel dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
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
    public void dataModelTuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
        updatePercentSelected();
    }

    @Override
    public void dataModelTuplesRemoved(DataModel dataModel, int numTuplesRemoved) {
        updatePercentSelected();
    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataModel dataModel) {}

    @Override
    public void dataModelColumnDisabled(DataModel dataModel, Column disabledColumn) {
        // reset the data datamodel columns
        dataTableView.getItems().clear();
        dataTableView.getColumns().clear();
        setDataTableColumns();
        setDataTableItems();

        updatePercentSelected();
        doubleColumnTableView.refresh();
        temporalColumnTableView.refresh();
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
