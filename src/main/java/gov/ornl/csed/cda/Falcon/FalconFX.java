package gov.ornl.csed.cda.Falcon;/**
 * Created by csg on 12/30/15.
 */

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
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import jdk.nashorn.internal.runtime.options.Option;
import org.apache.commons.math3.geometry.spherical.oned.ArcsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.event.TableListener;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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
        primaryStage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
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

        InputStream is = FalconFX.class.getResourceAsStream("fontawesome-webfont.ttf");
        fontAwesomeFont = javafx.scene.text.Font.loadFont(is, 14);

        BorderPane rootNode = new BorderPane();

        Scene scene = new Scene(rootNode, 1200, 800);

        Node ODTimeSeriesNode = createOverviewDetailTimeSeriesPanel();
        Node multiTimeSeriesNode = createMultiTimeSeriesPanel();

        createDataTreeView();
        createColumnTableView();
        createDataTableView();

        // TabPane setup for main visualization area
        TabPane tabPane = new TabPane();
        Tab ODTimeTab = new Tab("Overview + Detail Time Series");
        ODTimeTab.setContent(ODTimeSeriesNode);
        Tab multiTimeTab = new Tab("Multiple Time Series");
        multiTimeTab.setContent(multiTimeSeriesNode);
        tabPane.getTabs().addAll(ODTimeTab, multiTimeTab);

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
        TreeItem<FalconDataTreeItem> fileTreeItem = new TreeItem<>(new FalconDataTreeItem(csvFile), itemIcon);
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            String columnName = dataTable.getColumnName(i);
            if (!columnName.equals(timeColumnName)) {
                TreeItem<FalconDataTreeItem> columnTreeItem = new TreeItem<>(new FalconDataTreeItem(csvFile, columnName));
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

        MenuItem exitMI = new MenuItem("Exit");
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                primaryStage.close();
            }
        });

        fileMenu.getItems().addAll(openCSVMI, new SeparatorMenuItem(), exitMI);

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


    private void loadColumnIntoODTimeSeries (String columnName) {
        TimeSeries timeSeries = timeSeriesMap.get(columnName);
        overviewTimeSeriesPanel.setTimeSeries(timeSeries);
        detailsTimeSeriesPanel.setTimeSeries(timeSeries);
    }

    private void loadColumnIntoMultiTimeSeries (String columnName) {
        TimeSeries timeSeries = timeSeriesMap.get(columnName);
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
        multiTimeSeriesPanel.setDateTimeRange(multiTimeSeriesStartInstant, multiTimeSeriesEndInstant, chronoChoiceBox.getSelectionModel().getSelectedItem());
        multiTimeSeriesPanel.addTimeSeries(timeSeries);
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

                if (dataTreeItem.columnName != null) {
                    loadColumnIntoMultiTimeSeries(dataTreeItem.columnName);
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

    private Node createMultiHistogramPanel() {

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

                if (dataTreeItem.columnName != null) {
                    loadColumnIntoODTimeSeries(dataTreeItem.columnName);
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
