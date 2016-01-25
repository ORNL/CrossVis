package gov.ornl.csed.cda.Falcon;/**
 * Created by csg on 12/30/15.
 */

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.MultiHistogramPanel;
import gov.ornl.csed.cda.timevis.MultiTimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.event.TableListener;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class FalconFX extends Application {
    private final static Logger log = LoggerFactory.getLogger(FalconFX.class);

    private final DataFormat objectDataFormat = new DataFormat("application/x-java-serialized-object");

    // Data table
    private Table dataTable;

    // List of TimeSeries objects
    private HashMap<String, TimeSeries> timeSeriesMap;

    // Earliest start and latest end instant of all time series

    // Overview + Detail TimeSeries Objects
    private TimeSeriesPanel overviewTimeSeriesPanel;
    private TimeSeriesPanel detailsTimeSeriesPanel;

    // Overview + Detail TimeSeries Display Preference UIs
    private ChoiceBox<ChronoUnit> ODTimeSeriesChronoUnitChoice;
    private ColorPicker ODTimeSeriesDataColorPicker;
    private Spinner ODTimeSeriesPlotHeightSpinner;
    private Spinner ODTimeSeriesPlotChronoUnitWidthSpinner;

    // Multiple TimeSeries Objects
    private MultiTimeSeriesPanel multiTimeSeriesPanel;
    private Instant multiTimeSeriesStartInstant;
    private Instant multiTimeSeriesEndInstant;

    // Multiple TimeSeries Panel Display Preference UIs
    private ChoiceBox<ChronoUnit> multiTimeSeriesChronoUnitChoice;
    private ColorPicker multipleTimeSeriesDataColorPicker;
    private Spinner multipleTimeSeriesPlotHeightSpinner;
    private Spinner multipleTimeSeriesPlotChronoUnitWidthSpinner;

    // UI components
    private TreeView<FalconDataTreeItem> dataTreeView;
    private TreeItem<FalconDataTreeItem> dataTreeRoot;
    private ChoiceBox<ChronoUnit> chronoChoiceBox;
    private TableView columnTableView;
    private TableView dataTableView;
    private Font fontAwesomeFont;

    // state variables
    private javafx.scene.paint.Color dataColor = Color.web("rgba(80, 80, 100, 0.4)");

    private ChronoUnit currentTimeSeriesChronoUnit;
    private String timeColumnName;
    private MultiHistogramPanel multiHistogramPanel;
    private Spinner multipleHistogramPlotHeightSpinner;
    private Spinner multipleHistogramBinSizeSpinner;


    public static void main(String[] args) {
        launch(args);
    }


    private java.awt.Color convertToAWTColor(Color color) {
        int r = (int)(color.getRed() * 255.0);
        int g = (int)(color.getGreen() * 255.0);
        int b = (int)(color.getBlue() * 255.0);
        int a = (int)(color.getOpacity() * 255.0);
        return new java.awt.Color(r, g, b, a);
    }

    @Override
    public void start(Stage primaryStage) {
//        primaryStage.setOnShown(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                FileChooser fileChooser = new FileChooser();
//                fileChooser.setTitle("Open CSV File");
//                File csvFile = fileChooser.showOpenDialog(primaryStage);
//                if (csvFile != null) {
//                    try {
//                        openCSVFile(csvFile);
//                    } catch (DataIOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

        InputStream is = FalconFX.class.getResourceAsStream("fontawesome-webfont.ttf");
        fontAwesomeFont = javafx.scene.text.Font.loadFont(is, 14);

        BorderPane rootNode = new BorderPane();

        Scene scene = new Scene(rootNode, 1200, 800);

        Node ODTimeSeriesNode = createOverviewDetailTimeSeriesPanel();
        Node multiTimeSeriesNode = createMultiTimeSeriesPanel();
        Node multiHistogramNode = createMultiHistogramPanel();

        createDataTreeView();
        createColumnTableView();
        createDataTableView();

        // TabPane setup for main visualization area
        TabPane tabPane = new TabPane();
        Tab ODTimeTab = new Tab("Overview + Detail Time Series");
        ODTimeTab.setContent(ODTimeSeriesNode);
        Tab multiTimeTab = new Tab("Multiple Time Series");
        multiTimeTab.setContent(multiTimeSeriesNode);
        Tab multiHistoTab = new Tab("Multiple Histograms");
        multiHistoTab.setContent(multiHistogramNode);
        tabPane.getTabs().addAll(ODTimeTab, multiTimeTab, multiHistoTab);

//        // Create left pane as a vertically split node
//        StackPane topStackPane = new StackPane();
//        topStackPane.getChildren().addAll(dataTreeView);
//        StackPane bottomStackPane = new StackPane();
//        bottomStackPane.getChildren().addAll(settingsBox);

//        SplitPane leftSplitPane = new SplitPane();
//        leftSplitPane.getItems().addAll(dataTreeView, bottomStackPane);
//        leftSplitPane.setDividerPositions(0.6);
//        leftSplitPane.setResizableWithParent(bottomStackPane, false);
//        leftSplitPane.setOrientation(Orientation.VERTICAL);

        // Create right pane as a vertically split node
        // (top - timeseries, middle - histogram, bottom - table views)
        StackPane topStackPane = new StackPane();
//        topStackPane.getChildren().addAll(timeSeriesNode);
        topStackPane.getChildren().addAll(tabPane);
        StackPane bottomStackPane = new StackPane();
        bottomStackPane.getChildren().addAll(columnTableView);
        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.getItems().addAll(topStackPane, bottomStackPane);
        rightSplitPane.setDividerPositions(0.5);
        rightSplitPane.setResizableWithParent(bottomStackPane, false);
        rightSplitPane.setOrientation(Orientation.VERTICAL);

        // create main split between left and right panes
        SplitPane mainSplitPane = new SplitPane();
        StackPane leftStackPane = new StackPane();
        leftStackPane.getChildren().add(dataTreeView);
        mainSplitPane.getItems().addAll(leftStackPane, rightSplitPane);
        mainSplitPane.setDividerPositions(0.25);
        mainSplitPane.setResizableWithParent(leftStackPane, false);

        MenuBar menubar = createMenuBar(primaryStage);

        rootNode.setTop(menubar);
        rootNode.setCenter(mainSplitPane);

        primaryStage.setTitle("Falcon");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openPLGFile(File plgFile) throws IOException {
        HashMap<String, PLGVariableSchema> variableSchemaMap = PLGFileReader.readVariableSchemas(plgFile);

        // populate data tree view
        Text itemIcon = new Text("\uf1c0");
        itemIcon.setFont(fontAwesomeFont);
        itemIcon.setFontSmoothingType(FontSmoothingType.LCD);
        TreeItem<FalconDataTreeItem> fileTreeItem = new TreeItem<>(new FalconDataTreeItem(plgFile, FalconDataTreeItem.FileType.PLG), itemIcon);
        for (PLGVariableSchema schema : variableSchemaMap.values()) {
            if (schema.typeString.equals("Int16") ||
                    schema.typeString.equals("Double") ||
                    schema.typeString.equals("Single") ||
                    schema.typeString.equals("Int32")) {
                if (schema.numValues > 1) {
                    TreeItem<FalconDataTreeItem> variableTreeItem = new TreeItem<>(new FalconDataTreeItem(plgFile,
                            FalconDataTreeItem.FileType.PLG, schema.variableName));
                    fileTreeItem.getChildren().add(variableTreeItem);
                }
            }
        }

        dataTreeRoot.getChildren().addAll(fileTreeItem);
    }

    private void openCSVFile(File csvFile) throws DataIOException {
        dataTable = new CSVTableReader().readTable(csvFile);
        dataTable.addTableListener(new TableListener() {
            @Override
            public void tableChanged(Table table, int i, int i1, int i2, int i3) {
                // TODO: Handle events
            }
        });

        // get the time column
        ArrayList<String> columnNames = new ArrayList<>();
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            columnNames.add(dataTable.getColumnName(i));
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(columnNames.get(0), columnNames);
        dialog.setTitle("Time Dimension");
        dialog.setHeaderText("Which Column Represents Time");
        dialog.setContentText("Time Column: ");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            log.debug("time dimension is " + result.get());
            timeColumnName = result.get();
        } else {
            dataTable = null;
            return;
        }

        // build time series for all variables in table
        timeSeriesMap = new HashMap<>();
        int timeColumnIdx = dataTable.getColumnNumber(timeColumnName);
        for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
            if (!dataTable.getColumnName(icolumn).equals(timeColumnName)) {
                TimeSeries timeSeries = new TimeSeries(dataTable.getColumnName(icolumn));
                for (int ituple = 0; ituple < dataTable.getTupleCount(); ituple++) {
                    Instant instant = Instant.ofEpochMilli(dataTable.getLong(ituple, timeColumnIdx));
                    double value = dataTable.getDouble(ituple, icolumn);
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                }

                timeSeriesMap.put(dataTable.getColumnName(icolumn), timeSeries);
            }
        }

        // populate data tree view
        Text itemIcon = new Text("\uf1c0");
        itemIcon.setFont(fontAwesomeFont);
        itemIcon.setFontSmoothingType(FontSmoothingType.LCD);
        TreeItem<FalconDataTreeItem> fileTreeItem = new TreeItem<>(new FalconDataTreeItem(csvFile, FalconDataTreeItem.FileType.CSV), itemIcon);
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            String columnName = dataTable.getColumnName(i);
            if (!columnName.equals(timeColumnName)) {
                TreeItem<FalconDataTreeItem> columnTreeItem = new TreeItem<>(new FalconDataTreeItem(csvFile,
                        FalconDataTreeItem.FileType.CSV, columnName));
                fileTreeItem.getChildren().addAll(columnTreeItem);
            }
        }
        dataTreeRoot.getChildren().addAll(fileTreeItem);

        // populate column table view

        // populate data table view
    }


    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open CSV File");
                File csvFile = fileChooser.showOpenDialog(primaryStage);
                if (csvFile != null) {
                    try {
                        openCSVFile(csvFile);
                    } catch (DataIOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MenuItem openPLGMI = new MenuItem("Open PLG...");
        openPLGMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open PLG File");
                File plgFile = fileChooser.showOpenDialog(primaryStage);
                if (plgFile != null) {
                    try {
                        openPLGFile(plgFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MenuItem exitMI = new MenuItem("Exit");
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                primaryStage.close();
            }
        });

        fileMenu.getItems().addAll(openCSVMI, openPLGMI, new SeparatorMenuItem(), exitMI);

        Menu editMenu = new Menu("Edit");
        Menu viewMenu = new Menu("View");

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);

        return menuBar;
    }

    private void createDataTreeView() {
        dataTreeView = new TreeView<>();
        dataTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTreeView.setCellFactory(new Callback<TreeView<FalconDataTreeItem>, TreeCell<FalconDataTreeItem>>() {
            @Override
            public TreeCell<FalconDataTreeItem> call(TreeView<FalconDataTreeItem> param) {
                final TreeCell<FalconDataTreeItem> treeCell = new TreeCell<FalconDataTreeItem>() {
                    public void updateItem(FalconDataTreeItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            setGraphic(getTreeItem().getGraphic());
                        }
                    }
                };

                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Dragboard db = treeCell.startDragAndDrop(TransferMode.COPY);
                        ClipboardContent content = new ClipboardContent();
                        content.put(objectDataFormat, treeCell.getItem());
                        db.setContent(content);
                        event.consume();
                        Label label = new Label(String.format("Visualize %s timeseries", treeCell.getItem().toString()));
                        new Scene(label);
                        db.setDragView(label.snapshot(null, null));
                    }
                });

                return treeCell;
            }
        });

        dataTreeRoot = new TreeItem<FalconDataTreeItem>();
        dataTreeView.setRoot(dataTreeRoot);
        dataTreeView.setShowRoot(false);
    }


    private void createDataTableView() {
        dataTableView = new TableView();
    }


    private void createColumnTableView() {
        columnTableView = new TableView();
    }


    private void loadColumnIntoODTimeSeries (FalconDataTreeItem falconDataTreeItem) {
        if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.CSV) {
            TimeSeries timeSeries = timeSeriesMap.get(falconDataTreeItem.variableName);
            overviewTimeSeriesPanel.setTimeSeries(timeSeries);
            detailsTimeSeriesPanel.setTimeSeries(timeSeries);
        } else if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.PLG) {
            // load time series for variable
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(falconDataTreeItem.variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(falconDataTreeItem.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
//                    timeSeriesMap.put(timeSeries.getName(), timeSeries);
                    overviewTimeSeriesPanel.setTimeSeries(timeSeries);
                    detailsTimeSeriesPanel.setTimeSeries(timeSeries);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadColumnIntoMultiHistogram (FalconDataTreeItem falconDataTreeItem) {
        if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.CSV) {
//            int binCount = (int) Math.floor(Math.sqrt(dataTable.getTupleCount()));
//            if (binCount < 1) {
//                binCount = 1;
//            }
            int binCount = multiHistogramPanel.getBinCount();

            for (int icol = 0; icol < dataTable.getColumnCount(); icol++) {
                if (dataTable.getColumnName(icol).equals(falconDataTreeItem.variableName)) {
                    Column column = dataTable.getColumn(icol);
                    double values[] = new double[column.getRowCount()];
                    for (int i = 0; i < column.getRowCount(); i++) {
                        values[i] = column.getDouble(i);
                    }

                    Histogram histogram = new Histogram(dataTable.getColumnName(icol), values, binCount);
                    multiHistogramPanel.addHistogram(histogram);
                    break;
                }
            }
        } else if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.PLG) {
            try {
                double variableData []= PLGFileReader.readPLGFileAsDoubleArray(falconDataTreeItem.file, falconDataTreeItem.variableName);
                Histogram histogram = new Histogram(falconDataTreeItem.variableName, variableData, multiHistogramPanel.getBinCount());
                multiHistogramPanel.addHistogram(histogram);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadColumnIntoMultiTimeSeries (FalconDataTreeItem falconDataTreeItem) {
        if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.CSV) {
            TimeSeries timeSeries = timeSeriesMap.get(falconDataTreeItem.variableName);
            if (multiTimeSeriesStartInstant == null) {
                multiTimeSeriesStartInstant = timeSeries.getStartInstant();
            } else if (multiTimeSeriesStartInstant.isBefore(timeSeries.getStartInstant())) {
                multiTimeSeriesStartInstant = timeSeries.getStartInstant();
            }

            if (multiTimeSeriesEndInstant == null) {
                multiTimeSeriesEndInstant = timeSeries.getEndInstant();
            } else if (multiTimeSeriesEndInstant.isAfter(timeSeries.getEndInstant())) {
                multiTimeSeriesEndInstant = timeSeries.getEndInstant();
            }
            multiTimeSeriesPanel.setDateTimeRange(multiTimeSeriesStartInstant, multiTimeSeriesEndInstant, multiTimeSeriesChronoUnitChoice.getSelectionModel().getSelectedItem());
            multiTimeSeriesPanel.addTimeSeries(timeSeries);
        } else if (falconDataTreeItem.fileType == FalconDataTreeItem.FileType.PLG) {
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(falconDataTreeItem.variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(falconDataTreeItem.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
//                    timeSeriesMap.put(timeSeries.getName(), timeSeries);

                    if (multiTimeSeriesStartInstant == null) {
                        multiTimeSeriesStartInstant = timeSeries.getStartInstant();
                    } else if (multiTimeSeriesStartInstant.isBefore(timeSeries.getStartInstant())) {
                        multiTimeSeriesStartInstant = timeSeries.getStartInstant();
                    }

                    if (multiTimeSeriesEndInstant == null) {
                        multiTimeSeriesEndInstant = timeSeries.getEndInstant();
                    } else if (multiTimeSeriesEndInstant.isAfter(timeSeries.getEndInstant())) {
                        multiTimeSeriesEndInstant = timeSeries.getEndInstant();
                    }
                    multiTimeSeriesPanel.setDateTimeRange(multiTimeSeriesStartInstant, multiTimeSeriesEndInstant, multiTimeSeriesChronoUnitChoice.getSelectionModel().getSelectedItem());
                    multiTimeSeriesPanel.addTimeSeries(timeSeries);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Node createMultiHistogramPanel() {
        multiHistogramPanel = new MultiHistogramPanel();
        multiHistogramPanel.setBackground(java.awt.Color.white);

        HBox settingsHBox = new HBox();
        settingsHBox.setAlignment(Pos.CENTER_LEFT);
        settingsHBox.setPadding(new javafx.geometry.Insets(4));
        settingsHBox.setSpacing(8.);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleHistogramPlotHeightSpinner = new Spinner(40, 400, multiHistogramPanel.getPlotHeight());
        multipleHistogramPlotHeightSpinner.setEditable(true);
        multipleHistogramPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiHistogramPanel.setPlotHeight((Integer)newValue));
        hBox.getChildren().addAll(new Label("Plot Height: "), multipleHistogramPlotHeightSpinner);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleHistogramBinSizeSpinner = new Spinner(10, 100, multiHistogramPanel.getBinCount());
        multipleHistogramBinSizeSpinner.setEditable(true);
        multipleHistogramBinSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiHistogramPanel.setBinCount((Integer)newValue));
        hBox.getChildren().addAll(new Label("Bin Count: "), multipleHistogramBinSizeSpinner);
        settingsHBox.getChildren().add(hBox);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JScrollPane scroller = new JScrollPane(multiHistogramPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(2);
        scroller.setBorder(border);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(scroller);
        tsSwingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        tsSwingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);

                if (dataTreeItem.variableName != null) {
                    loadColumnIntoMultiHistogram(dataTreeItem);
                }

                event.setDropCompleted(true);
            }
            event.consume();
        });

//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(tsSwingNode);
//        scrollPane.setFitToWidth(true);

//        scrollPane.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
//        scrollPane.setOnDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            if (db.hasContent(objectDataFormat)) {
//                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);
//
//                if (dataTreeItem.variableName != null) {
//                    loadColumnIntoMultiHistogram(dataTreeItem);
//                }
//
//                event.setDropCompleted(true);
//            }
//            event.consume();
//        });

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(settingsHBox);
        borderPane.setCenter(tsSwingNode);
        return borderPane;
    }

    private Node createMultiTimeSeriesPanel() {
        multiTimeSeriesPanel = new MultiTimeSeriesPanel();
        multiTimeSeriesPanel.setBackground(java.awt.Color.white);

        HBox settingsHBox = new HBox();
        settingsHBox.setAlignment(Pos.CENTER_LEFT);
        settingsHBox.setPadding(new javafx.geometry.Insets(4));
        settingsHBox.setSpacing(8.);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiTimeSeriesChronoUnitChoice = new ChoiceBox<ChronoUnit>();
        multiTimeSeriesChronoUnitChoice.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        multiTimeSeriesChronoUnitChoice.getSelectionModel().selectFirst();
        multiTimeSeriesChronoUnitChoice.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends ChronoUnit> ov,
                 ChronoUnit oldValue, ChronoUnit newValue) -> {
                    if (oldValue != newValue) {
                        multiTimeSeriesPanel.setChronoUnit(newValue);
                    }
                }
        );
        hBox.getChildren().addAll(new Label("Plot ChronoUnit: "), multiTimeSeriesChronoUnitChoice);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleTimeSeriesDataColorPicker = new ColorPicker(dataColor);
        multipleTimeSeriesDataColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color dataColor = multipleTimeSeriesDataColorPicker.getValue();
                multiTimeSeriesPanel.setDataColor(convertToAWTColor(dataColor));
            }
        });
        hBox.getChildren().addAll(new Label("Data Color: "), multipleTimeSeriesDataColorPicker);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleTimeSeriesPlotHeightSpinner = new Spinner(40, 400, multiTimeSeriesPanel.getPlotHeight());
        multipleTimeSeriesPlotHeightSpinner.setEditable(true);
        multipleTimeSeriesPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiTimeSeriesPanel.setPlotHeight((Integer)newValue));
        hBox.getChildren().addAll(new Label("Plot Height: "), multipleTimeSeriesPlotHeightSpinner);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleTimeSeriesPlotChronoUnitWidthSpinner = new Spinner(1, 10, multiTimeSeriesPanel.getChronoUnitWidth());
        multipleTimeSeriesPlotChronoUnitWidthSpinner.setEditable(true);
        multipleTimeSeriesPlotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiTimeSeriesPanel.setChronoUnitWidth((Integer)newValue));
        hBox.getChildren().addAll(new Label("Plot Unit Width: "), multipleTimeSeriesPlotChronoUnitWidthSpinner);
        settingsHBox.getChildren().add(hBox);

        JScrollPane scroller = new JScrollPane(multiTimeSeriesPanel);
        scroller.getHorizontalScrollBar().setUnitIncrement(2);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(scroller);
        tsSwingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        tsSwingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);

                if (dataTreeItem.variableName != null) {
                    loadColumnIntoMultiTimeSeries(dataTreeItem);
                }

                event.setDropCompleted(true);
            }
            event.consume();
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(settingsHBox);
        borderPane.setCenter(tsSwingNode);
        return borderPane;
    }

    private Node createOverviewDetailTimeSeriesPanel () {
        detailsTimeSeriesPanel = new TimeSeriesPanel(2, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        detailsTimeSeriesPanel.setBackground(java.awt.Color.white);

        overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1400, 100));
        overviewTimeSeriesPanel.setBackground(java.awt.Color.white);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        overviewTimeSeriesPanel.setBorder(border);

        JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
        scroller.getHorizontalScrollBar().setUnitIncrement(2);
        scroller.setBorder(border);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scroller, BorderLayout.CENTER);
        panel.add(overviewTimeSeriesPanel, BorderLayout.SOUTH);
        panel.setBackground(java.awt.Color.white);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(panel);
        tsSwingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        tsSwingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);

                if (dataTreeItem.variableName != null) {
                    loadColumnIntoODTimeSeries(dataTreeItem);
                }

                event.setDropCompleted(true);
            }

            event.consume();
        });

        scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                JScrollBar scrollBar = (JScrollBar)e.getSource();
                double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
                double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                TimeSeries timeSeries = overviewTimeSeriesPanel.getTimeSeries();
                if (timeSeries != null) {
                    double deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                    Instant startHighlightInstant = timeSeries.getStartInstant().plusMillis((long) deltaTime);
                    int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                    norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                    deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                    Instant endHighlightInstant = timeSeries.getEndInstant().minusMillis((long) deltaTime);
                    overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
                }
            }
        });

        // create display preferences UI components
        HBox settingsHBox = new HBox();
        settingsHBox.setAlignment(Pos.CENTER_LEFT);
        settingsHBox.setPadding(new javafx.geometry.Insets(4));
        settingsHBox.setSpacing(8.);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        ODTimeSeriesChronoUnitChoice = new ChoiceBox<ChronoUnit>();
        ODTimeSeriesChronoUnitChoice.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        ODTimeSeriesChronoUnitChoice.getSelectionModel().selectFirst();
        ODTimeSeriesChronoUnitChoice.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends ChronoUnit> ov,
                 ChronoUnit oldValue, ChronoUnit newValue) -> {
                    if (oldValue != newValue) {
                        detailsTimeSeriesPanel.setPlotChronoUnit(newValue);
                    }
                }
        );
        hBox.getChildren().addAll(new Label("Plot ChronoUnit: "), ODTimeSeriesChronoUnitChoice);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        ODTimeSeriesDataColorPicker = new ColorPicker(dataColor);
        ODTimeSeriesDataColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color dataColor = ODTimeSeriesDataColorPicker.getValue();
                detailsTimeSeriesPanel.setDataColor(convertToAWTColor(dataColor));
                overviewTimeSeriesPanel.setDataColor(convertToAWTColor(dataColor));
            }
        });
        hBox.getChildren().addAll(new Label("Data Color: "), ODTimeSeriesDataColorPicker);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        ODTimeSeriesPlotChronoUnitWidthSpinner = new Spinner(1, 10, detailsTimeSeriesPanel.getChronoUnitWidth());
        ODTimeSeriesPlotChronoUnitWidthSpinner.setEditable(true);
        ODTimeSeriesPlotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> detailsTimeSeriesPanel.setChronoUnitWidth((Integer)newValue));
        hBox.getChildren().addAll(new Label("Plot Unit Width: "), ODTimeSeriesPlotChronoUnitWidthSpinner);
        settingsHBox.getChildren().add(hBox);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(settingsHBox);
        borderPane.setCenter(tsSwingNode);

        return borderPane;
    }
}
