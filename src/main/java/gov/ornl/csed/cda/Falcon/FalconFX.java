package gov.ornl.csed.cda.Falcon;/**
 * Created by csg on 12/30/15.
 */

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.*;
import javafx.geometry.*;
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
import java.io.File;
import java.io.InputStream;
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

    // Timeseries Objects
    private TimeSeriesPanel overviewTimeSeriesPanel;
    private TimeSeriesPanel detailsTimeSeriesPanel;
    private HashMap<String, TimeSeries> timeSeriesMap;

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


        SwingNode timeSeriesNode = createTimeSeriesPanel();

        VBox settingsBox = createSettingsVBox();

        createDataTreeView();
        createColumnTableView();
        createDataTableView();

        // Create left pane as a vertically split node
        StackPane topStackPane = new StackPane();
        topStackPane.getChildren().addAll(dataTreeView);
        StackPane bottomStackPane = new StackPane();
        bottomStackPane.getChildren().addAll(settingsBox);

        SplitPane leftSplitPane = new SplitPane();
        leftSplitPane.getItems().addAll(topStackPane, bottomStackPane);
        leftSplitPane.setDividerPositions(0.6);
        leftSplitPane.setResizableWithParent(bottomStackPane, false);
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        // Create right pane as a vertically split node
        // (top - timeseries, middle - histogram, bottom - table views)
        topStackPane = new StackPane();
        topStackPane.getChildren().addAll(timeSeriesNode);
        bottomStackPane = new StackPane();
        bottomStackPane.getChildren().addAll(columnTableView);
        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.getItems().addAll(topStackPane, bottomStackPane);
        rightSplitPane.setDividerPositions(0.5);
        rightSplitPane.setResizableWithParent(bottomStackPane, false);
        rightSplitPane.setOrientation(Orientation.VERTICAL);

        // create main split between left and right panes
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(leftSplitPane, rightSplitPane);
        mainSplitPane.setDividerPositions(0.25);
        mainSplitPane.setResizableWithParent(leftSplitPane, false);

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


    private void loadColumnTimeSeries (String columnName) {
        TimeSeries timeSeries = timeSeriesMap.get(columnName);
        overviewTimeSeriesPanel.setTimeSeries(timeSeries);
        detailsTimeSeriesPanel.setTimeSeries(timeSeries);

//        timeSeries = new TimeSeries("TS", ChronoUnit.MINUTES);
//
//        detailsTimeSeriesPanel.removeTimeSeries();
//        overviewTimeSeriesPanel.removeTimeSeries();
//
//        ChronoUnit chronoUnit = chronoChoiceBox.getSelectionModel().getSelectedItem();
//        detailsTimeSeriesPanel.setPlotChronoUnit(chronoUnit);
//
//        int timeColumnIdx = dataTable.getColumnNumber(timeColumnName);
//        int valueColumnIdx = dataTable.getColumnNumber(columnName);
//
//        for(int i = 0; i < dataTable.getTupleCount(); i++) {
//            Instant instant = Instant.ofEpochMilli(dataTable.getLong(i, timeColumnIdx));
//            double value = dataTable.getDouble(i, valueColumnIdx);
//            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//        }
//
//        overviewTimeSeriesPanel.setTimeSeries(timeSeries);
//        detailsTimeSeriesPanel.setTimeSeries(timeSeries);
    }


    private VBox createSettingsVBox() {
        VBox settingsVBox = new VBox();
        settingsVBox.setPadding(new javafx.geometry.Insets(10));
        settingsVBox.setSpacing(8);

        chronoChoiceBox = new ChoiceBox<>();
        chronoChoiceBox.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS,
                ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        chronoChoiceBox.getSelectionModel().selectFirst();
        chronoChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends ChronoUnit> ov,
                 ChronoUnit oldValue, ChronoUnit newValue) -> {
                    if (oldValue != newValue) {
                        currentTimeSeriesChronoUnit = newValue;
                        detailsTimeSeriesPanel.setPlotChronoUnit(currentTimeSeriesChronoUnit);
                    }
                    log.debug("New current chrono level is " + currentTimeSeriesChronoUnit);
                }
        );
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BOTTOM_LEFT);
        hBox.getChildren().addAll(new javafx.scene.control.Label("Current ChronoUnit: "), chronoChoiceBox);
        settingsVBox.getChildren().addAll(hBox);


        ColorPicker dataColorPicker = new ColorPicker(dataColor);
        dataColorPicker.setOnAction(new EventHandler() {
            public void handle(javafx.event.Event event) {
                dataColor = dataColorPicker.getValue();
                // TODO: redisplay timeseries with new color
            }
        });
        hBox = new HBox();
        hBox.setAlignment(Pos.BOTTOM_LEFT);
        hBox.getChildren().addAll(new javafx.scene.control.Label("Data Color: "), dataColorPicker);
        settingsVBox.getChildren().addAll(hBox);
//
//        Spinner plotChronoUnitWidthSpinner = new Spinner(1, 10, timeSeriesPanel.getChronoUnitWidth());
//        plotChronoUnitWidthSpinner.setEditable(true);
//        plotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) ->
//                timeSeriesPanel.setChronoUnitWidth((Integer) newValue));
//
//        hBox = new HBox();
//        hBox.setAlignment(Pos.BOTTOM_LEFT);
//        hBox.getChildren().addAll(new javafx.scene.control.Label("Plot Unit Width: "), plotChronoUnitWidthSpinner);
//        settingsVBox.getChildren().addAll(hBox);

        return settingsVBox;
    }


    private SwingNode createTimeSeriesPanel () {
        detailsTimeSeriesPanel = new TimeSeriesPanel(2, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        detailsTimeSeriesPanel.setBackground(java.awt.Color.white);

        overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1400, 100));
        overviewTimeSeriesPanel.setBackground(java.awt.Color.white);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        overviewTimeSeriesPanel.setBorder(border);

        JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        scroller.setBorder(border);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scroller, BorderLayout.CENTER);
        panel.add(overviewTimeSeriesPanel, BorderLayout.SOUTH);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(panel);
        tsSwingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        tsSwingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);

                if (dataTreeItem.columnName != null) {
                    loadColumnTimeSeries(dataTreeItem.columnName);
                }

                event.setDropCompleted(true);
            }

            event.consume();
        });

        return tsSwingNode;
    }
}
