package gov.ornl.csed.cda.Falcon;/**
 * Created by csg on 12/30/15.
 */

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.MultiHistogramPanel;
import gov.ornl.csed.cda.timevis.MultiTimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.event.TableListener;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
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
//    private HashMap<String, TimeSeries> timeSeriesMap;

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

    // Multi View Panel Objects
    private MultiViewPanel multiViewPanel;

    // Multiple TimeSeries Panel Display Preference UIs
    private ChoiceBox<ChronoUnit> multiTimeSeriesChronoUnitChoice;
    private ColorPicker multipleTimeSeriesDataColorPicker;
    private Spinner multipleTimeSeriesPlotHeightSpinner;
    private Spinner multipleTimeSeriesPlotChronoUnitWidthSpinner;

    // UI components
    private TreeView<String> dataTreeView;
    private TreeItem<String> dataTreeRoot;
    private ChoiceBox<ChronoUnit> chronoChoiceBox;
    private TableView columnTableView;
    private TableView dataTableView;
    private Font fontAwesomeFont;

    // For keeping track of file metadata and linking to tree items
    private HashMap<File, FileMetadata> fileMetadataMap = new HashMap<>();
    private HashMap<TreeItem<String>, FileMetadata> fileTreeItemMetadataMap = new HashMap<>();

    // state variables
    private javafx.scene.paint.Color dataColor = Color.web("rgba(80, 80, 100, 0.4)");

    private ChronoUnit currentTimeSeriesChronoUnit;
    private String timeColumnName;
    private MultiHistogramPanel multiHistogramPanel;
    private Spinner multipleHistogramPlotHeightSpinner;
    private Spinner multipleHistogramBinSizeSpinner;
    private JLabel overviewDetailTimeSeriesNameLabel;
    private TabPane visTabPane;

    // multi view panel preferences
    private Spinner multiViewPlotHeightSpinner;
    private ChoiceBox<ChronoUnit> multiViewChronoUnitChoice;
    private ColorPicker multipleViewDataColorPicker;
    private Spinner multipleViewPlotChronoUnitWidthSpinner;
    private ToggleButton multiViewSyncScrollbarsToggleButton;
    private ToggleButton multiViewShowOverviewToggleButton;
    private ToggleButton multiViewShowButtonsToggleButton;
    private Spinner multipleViewHistogramBinSizeSpinner;

    // overview + detail panel
    private JPanel overviewDetailPanel;


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

        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(rootNode, screenBounds.getWidth()-20, 800);

        Node ODTimeSeriesNode = createOverviewDetailTimeSeriesPanel();
//        Node multiTimeSeriesNode = createMultiTimeSeriesPanel();
        Node multiHistogramNode = createMultiHistogramPanel();
        Node multiViewNode = createMultiViewPanel();

        createDataTreeView();
        createColumnTableView();
        createDataTableView();

        // TabPane setup for main visualization area
        visTabPane = new TabPane();
        visTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab multiViewTab = new Tab(" Multi View ");
        multiViewTab.setContent(multiViewNode);
        Tab ODTimeTab = new Tab(" Single Time Series ");
        ODTimeTab.setContent(ODTimeSeriesNode);
//        Tab multiTimeTab = new Tab(" Multiple Time Series ");
//        multiTimeTab.setContent(multiTimeSeriesNode);
        Tab multiHistoTab = new Tab(" Multiple Histograms ");
        multiHistoTab.setContent(multiHistogramNode);
        visTabPane.getTabs().addAll(multiViewTab, ODTimeTab, multiHistoTab);

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
//        StackPane topStackPane = new StackPane();
////        topStackPane.getChildren().addAll(timeSeriesNode);
//        topStackPane.getChildren().addAll(visTabPane);
//        StackPane bottomStackPane = new StackPane();
//        bottomStackPane.getChildren().addAll(columnTableView);
//        SplitPane rightSplitPane = new SplitPane();
//        rightSplitPane.getItems().addAll(topStackPane, bottomStackPane);
//        rightSplitPane.setDividerPositions(0.5);
//        rightSplitPane.setResizableWithParent(bottomStackPane, false);
//        rightSplitPane.setOrientation(Orientation.VERTICAL);

        // create left pane as a vertically split node (top data tree view and bottom is preferences)
//        StackPane leftStackPane = new StackPane();
//        leftStackPane.getChildren().add(dataTreeView);
//        StackPane topStackPane = new StackPane();
//        topStackPane.getChildren().add(dataTreeView);
//        StackPane bottomStackPane = new StackPane();
//        bottomStackPane.getChildren().add(settingsPane);
//        SplitPane leftSplitPane = new SplitPane();
//        leftSplitPane.getItems().addAll(topStackPane, bottomStackPane);
//        leftSplitPane.setDividerPositions(0.3);
//        leftSplitPane.setResizableWithParent(bottomStackPane, false);
//        leftSplitPane.setOrientation(Orientation.VERTICAL);
//

        // left panel
        StackPane leftStackPane = new StackPane();
        leftStackPane.getChildren().add(dataTreeView);
        leftStackPane.setMaxWidth(400.);

        // create main split between left and right panes
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(leftStackPane, visTabPane);
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
        TreeItem<String> fileTreeItem = new TreeItem<>(plgFile.getName(), itemIcon);
        FileMetadata fileMetadata = new FileMetadata(plgFile);
        fileMetadata.fileType = FileMetadata.FileType.PLG;
        fileTreeItemMetadataMap.put(fileTreeItem, fileMetadata);
        fileMetadataMap.put(plgFile, fileMetadata);
//        fileTreeItemMap.put(fileTreeItem, plgFile);
//        fileTypeMap.put(plgFile, FalconDataTreeItem.FileType.PLG);

        for (PLGVariableSchema schema : variableSchemaMap.values()) {
//            if (schema.variableName.contains("ArcTrip")) {
//                log.debug(schema.variableName + " " + schema.typeString + " " + schema.numValues);
//            }

            if (schema.typeString.equals("Int16") ||
                    schema.typeString.equals("Double") ||
                    schema.typeString.equals("Single") ||
                    schema.typeString.equals("Int32")) {
                if (schema.numValues > 0) {
                    fileMetadata.variableList.add(schema.variableName);
                    fileMetadata.variableValueCountList.add(schema.numValues);

                    String tokens[] = schema.variableName.split("[.]");

                    TreeItem<String> parentTreeItem = fileTreeItem;
                    String compoundItemName = "";
                    for (int i = 0; i < tokens.length; i++) {
                        TreeItem<String> treeItem = null;

                        // if an item already exists for this token, use it
                        for (TreeItem<String> item : parentTreeItem.getChildren()) {
                            if (item.getValue().equals(tokens[i])) {
                                treeItem = item;
                                break;
                            }
                        }

                        // item doesn't exist for this token so create it
                        if (treeItem == null) {
                            treeItem = new TreeItem<>(tokens[i]);
                            parentTreeItem.getChildren().add(treeItem);
                        }

                        // update parent item
                        parentTreeItem = treeItem;
                    }
                }
            }
        }

        dataTreeRoot.getChildren().addAll(fileTreeItem);
    }

    private void captureVisualizationImage(File imageFile) throws IOException {
        JPanel activePanel = null;

        // get active visualization tab
        if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Single Time Series ")) {
            activePanel = overviewDetailPanel;
        } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multiple Time Series ")) {

        } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multiple Histograms ")) {

        } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multi View ")) {
            activePanel = multiViewPanel;
        } else {
            return;
        }

        int imageWidth = activePanel.getWidth() * 4;
        int imageHeight = activePanel.getHeight() * 4;


        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setTransform(AffineTransform.getScaleInstance(4., 4.));

        activePanel.paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", imageFile);
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

        ChoiceBox<ChronoUnit> timeChronoUnitChoiceBox = new ChoiceBox<>();
        timeChronoUnitChoiceBox.getSelectionModel().select(0);

        ChronoUnit timeChronoUnit = null;
        ChoiceDialog<ChronoUnit> timeChronoUnitChoiceDialog = new ChoiceDialog<>(ChronoUnit.MILLIS, FXCollections.observableArrayList(ChronoUnit.MILLIS, ChronoUnit.SECONDS));
        timeChronoUnitChoiceDialog.setTitle("Time Chronological Unit");
        timeChronoUnitChoiceDialog.setHeaderText("Select the Chronological Unit of Time");
        timeChronoUnitChoiceDialog.setContentText("Time Chronological Unit: ");
        Optional<ChronoUnit> timeChronoUnitResult = timeChronoUnitChoiceDialog.showAndWait();
        if (timeChronoUnitResult.isPresent()) {
            log.debug("time chronological unit is " + timeChronoUnitResult.get());
            timeChronoUnit = timeChronoUnitResult.get();
        } else {
            dataTable = null;
            return;
        }

        // populate data tree view
        Text itemIcon = new Text("\uf1c0");
        itemIcon.setFont(fontAwesomeFont);
        itemIcon.setFontSmoothingType(FontSmoothingType.LCD);
//        TreeItem<FalconDataTreeItem> fileTreeItem = new TreeItem<>(new FalconDataTreeItem(csvFile, FalconDataTreeItem.FileType.CSV), itemIcon);
        TreeItem<String>fileTreeItem = new TreeItem<>(csvFile.getName());
        FileMetadata fileMetadata = new FileMetadata(csvFile);
        fileMetadata.fileType = FileMetadata.FileType.CSV;
        fileTreeItemMetadataMap.put(fileTreeItem, fileMetadata);
        fileMetadataMap.put(csvFile, fileMetadata);

        // build time series for all variables in table
        fileMetadata.timeSeriesMap = new HashMap<>();
        int timeColumnIdx = dataTable.getColumnNumber(timeColumnName);
        for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
            if (!dataTable.getColumnName(icolumn).equals(timeColumnName)) {
                TimeSeries timeSeries = new TimeSeries(dataTable.getColumnName(icolumn));
                for (int ituple = 0; ituple < dataTable.getTupleCount(); ituple++) {
                    Instant instant = null;
                    if (timeChronoUnit == ChronoUnit.MILLIS) {
                        instant = Instant.ofEpochMilli(dataTable.getLong(ituple, timeColumnIdx));
                    } else {
                        double seconds = dataTable.getDouble(ituple, timeColumnIdx);
                        long timeMillis = (long)(seconds * 1000.);
                        instant = Instant.ofEpochMilli(timeMillis);
                    }
                    if (dataTable.canGetDouble(dataTable.getColumnName(icolumn))) {
                        double value = dataTable.getDouble(ituple, icolumn);
                        if (!Double.isNaN(value)) {
                            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                        }
                    }
                }

                fileMetadata.timeSeriesMap.put(dataTable.getColumnName(icolumn), timeSeries);
                log.debug("added timeseries for " + timeSeries.getName() + " with " + timeSeries.getRecordCount() + " records");
            }
        }
//        fileTreeItemMap.put(fileTreeItem, csvFile);
//        fileTypeMap.put(csvFile, FalconDataTreeItem.FileType.CSV);

        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            String columnName = dataTable.getColumnName(i);
            if (!columnName.equals(timeColumnName)) {
                fileMetadata.variableList.add(columnName);
                fileMetadata.variableValueCountList.add(dataTable.getTupleCount());

                String tokens[] = columnName.split("[.]");

                TreeItem<String> parentTreeItem = fileTreeItem;
                for (int itoken = 0; itoken < tokens.length; itoken++) {
                    TreeItem<String> treeItem = null;
                    for (TreeItem<String> item : parentTreeItem.getChildren()) {
                        if (item.getValue().equals(tokens[itoken])) {
                            treeItem = item;
                            break;
                        }
                    }

                    if (treeItem == null) {
                        treeItem = new TreeItem<>(tokens[itoken]);
                        parentTreeItem.getChildren().add(treeItem);
                    }
                    parentTreeItem = treeItem;
                }

//                TreeItem<String> columnTreeItem = new TreeItem<>(columnName);
//                fileTreeItem.getChildren().addAll(columnTreeItem);
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

        MenuItem captureScreenMI = new MenuItem("Screen Capture...");
        captureScreenMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select File for Screen Capture");
                File imageFile = fileChooser.showSaveDialog(primaryStage);
                if (imageFile != null) {
                    try{
                        captureVisualizationImage(imageFile);
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

        fileMenu.getItems().addAll(openCSVMI, openPLGMI, new SeparatorMenuItem(), captureScreenMI, new SeparatorMenuItem(), exitMI);

        Menu editMenu = new Menu("Edit");
        Menu viewMenu = new Menu("View");

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);

        return menuBar;
    }


    private Tooltip getTooltipForCell(TreeCell<String> treeCell) {
        Tooltip tooltip = new Tooltip();

        TreeItem<String> treeItem = treeCell.getTreeItem();

        // build full variable name
        String fullVarName = treeItem.getValue();
        TreeItem<String> parent = treeItem.getParent();
        FileMetadata fileMetadata = null;
        while(parent != null) {
            if (fileTreeItemMetadataMap.containsKey(parent)) {
                fileMetadata = fileTreeItemMetadataMap.get(parent);
                break;
            }
            fullVarName = parent.getValue() + "." + fullVarName;
            parent = parent.getParent();
        }

        String tooltipText = fullVarName;
        if (treeItem.isLeaf()) {
            // the item represents a variable
            // show full name and number of values in tooltip
            int idx = fileMetadata.variableList.indexOf(fullVarName);
            if (idx != -1) {
                tooltipText += " (" + fileMetadata.variableValueCountList.get(idx) + " values)";
            }
        }

        tooltip.setText(tooltipText);
//        Tooltip.install(treeCell, tooltip);
        return tooltip;
    }

    private String getFullTreeItemName(TreeItem<String> treeItem) {
        String variableName = treeItem.getValue();

        TreeItem<String> parentItem = treeItem.getParent();
        while (parentItem != null && !fileTreeItemMetadataMap.containsKey(parentItem)) {
            variableName = parentItem.getValue() + "." + variableName;
            parentItem = parentItem.getParent();
        }

        return variableName;
    }

    private FileMetadata getFileMetadataForTreeItem(TreeItem<String> treeItem) {
        TreeItem<String> parentItem = treeItem;
        while (parentItem != null) {
            FileMetadata fileMetadata = fileTreeItemMetadataMap.get(parentItem);
            if (fileMetadata != null) {
                return fileMetadata;
            }

            parentItem = parentItem.getParent();
        }

        return null;
    }

    private VariableClipboardData treeItemToVariableClipboardData(TreeItem<String> treeItem) {
        String variableName = null;

        TreeItem<String> currentTreeItem = treeItem;
        while (currentTreeItem.getParent() != null) {
            if (variableName == null) {
                variableName = currentTreeItem.getValue();
            } else {
                variableName = currentTreeItem.getValue() + "." + variableName;
            }

            currentTreeItem = currentTreeItem.getParent();
            if (fileTreeItemMetadataMap.containsKey(currentTreeItem)) {
                FileMetadata fileMetadata = fileTreeItemMetadataMap.get(currentTreeItem);
                VariableClipboardData variableClipboardData = new VariableClipboardData(fileMetadata.file, fileMetadata.fileType,
                        variableName);
                return variableClipboardData;
            }
        }

//        String variableName = treeItem.getValue();
//        treeItem = treeItem.getParent();
//        while (treeItem.getParent() != null) {
//            variableName = treeItem.getValue() + "." + variableName;
//            treeItem = treeItem.getParent();
//
//            // if parent tree item is a file node, get file details and stop
//            if (fileTreeItemMetadataMap.containsKey(treeItem)) {
//                FileMetadata fileMetadata = fileTreeItemMetadataMap.get(treeItem);
//                VariableClipboardData variableClipboardData = new VariableClipboardData(fileMetadata.file,
//                        fileMetadata.fileType, variableName);
//                return variableClipboardData;
//            }
//        }

        return null;
    }

    private void createDataTreeView() {
        dataTreeView = new TreeView<>();
        dataTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                final TreeCell<String> treeCell = new TreeCell<String>() {
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            setGraphic(getTreeItem().getGraphic());
                            Tooltip tooltip = getTooltipForCell(this);
                            setTooltip(tooltip);
                        }
                    }
                };

                treeCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2) {
                            TreeItem<String> treeItem = treeCell.getTreeItem();
                            if (treeItem.isLeaf()) {
                                FileMetadata fileMetadata = getFileMetadataForTreeItem(treeItem);
                                String variableName = getFullTreeItemName(treeItem);
//                                VariableClipboardData variableClipboardData = new VariableClipboardData(fileMetadata.file, fileMetadata.fileType, variableName);
//                                VariableClipboardData variableClipboardData = treeItemToVariableClipboardData(treeItem);
//                                // based on the visible tab pane, show data in visualization
                                if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Single Time Series ")) {
                                    loadColumnIntoODTimeSeries(fileMetadata, variableName);
                                } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multiple Time Series ")) {
                                    loadColumnIntoMultiTimeSeries(fileMetadata, variableName);
                                } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multiple Histograms ")) {
                                    loadColumnIntoMultiHistogram(fileMetadata, variableName);
                                } else if (visTabPane.getSelectionModel().getSelectedItem().getText().equals(" Multi View ")) {
                                    loadColumnIntoMultiView(fileMetadata, variableName);
                                }
                            }
                        }
                    }
                });

                treeCell.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        TreeItem<String> treeItem = treeCell.getTreeItem();
                        // we can only drag and drop leaf nodes in the tree
                        // the leaves are the full variable names
                        // nonleaf nodes are file nodes or partial variable names
                        // TODO: when a nonleaf is dragged add all child variables
                        if (treeItem.isLeaf()) {
//                            VariableClipboardData variableClipboardData = new VariableClipboardData();
//
//                            // build the variable name
//                            variableClipboardData.variableName = treeItem.getValue();
//                            treeItem = treeItem.getParent();
//                            while (treeItem.getParent() != null) {
//                                variableClipboardData.variableName = treeItem.getValue() + "." + variableClipboardData.variableName;
//                                treeItem = treeItem.getParent();
//
//                                // if parent tree item is a file node, get file details and stop
//                                if (fileTreeItemMetadataMap.containsKey(treeItem)) {
//                                    variableClipboardData.fileMetadata = fileTreeItemMetadataMap.get(treeItem);
//                                    break;
//                                }
//                            }

                            VariableClipboardData variableClipboardData = treeItemToVariableClipboardData(treeItem);

                            log.debug("clipboard data is " + variableClipboardData.toString());
                            Dragboard db = treeCell.startDragAndDrop(TransferMode.COPY);
                            ClipboardContent content = new ClipboardContent();
                            content.put(objectDataFormat, variableClipboardData);
                            db.setContent(content);
                            event.consume();
                            Label label = new Label(String.format("Visualize %s", variableClipboardData.getVariableName()));
                            new Scene(label);
                            db.setDragView(label.snapshot(null, null));
                        }
//                        Dragboard db = treeCell.startDragAndDrop(TransferMode.COPY);
//                        ClipboardContent content = new ClipboardContent();
//
//                        content.put(objectDataFormat, treeCell.getItem());
//                        db.setContent(content);
//                        event.consume();
//                        Label label = new Label(String.format("Visualize %s timeseries", treeCell.getItem().toString()));
//                        new Scene(label);
//                        db.setDragView(label.snapshot(null, null));
                    }
                });

                return treeCell;
            }
        });

        dataTreeRoot = new TreeItem<String>();
        dataTreeView.setRoot(dataTreeRoot);
        dataTreeView.setShowRoot(false);
    }


    private void createDataTableView() {
        dataTableView = new TableView();
    }


    private void createColumnTableView() {
        columnTableView = new TableView();
    }


    private void loadColumnIntoODTimeSeries (FileMetadata fileMetadata, String variableName) {
        if (fileMetadata.fileType == FileMetadata.FileType.CSV) {
            TimeSeries timeSeries = fileMetadata.timeSeriesMap.get(variableName);
//            TimeSeries timeSeries = timeSeriesMap.get(variableClipboardData.variableName);
            overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
            detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
            overviewDetailTimeSeriesNameLabel.setText(timeSeries.getName());
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            // load time series for variable
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
//                    timeSeriesMap.put(timeSeries.getName(), timeSeries);
                    overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                    detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                    overviewDetailTimeSeriesNameLabel.setText(timeSeries.getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadColumnIntoMultiView (FileMetadata fileMetadata, String variableName) {
        if (fileMetadata.fileType == FileMetadata.FileType.CSV) {
            TimeSeries timeSeries = fileMetadata.timeSeriesMap.get(variableName);
            multiViewPanel.addTimeSeries(timeSeries, fileMetadata.file.getName());
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
                    timeSeries.setName(fileMetadata.file.getName() + ":" + timeSeries.getName());
                    multiViewPanel.addTimeSeries(timeSeries, fileMetadata.file.getName());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadColumnIntoMultiHistogram (FileMetadata fileMetadata, String variableName) {
        if (fileMetadata.fileType == FileMetadata.FileType.CSV) {
//            int binCount = (int) Math.floor(Math.sqrt(dataTable.getTupleCount()));
//            if (binCount < 1) {
//                binCount = 1;
//            }
//            int binCount = multiHistogramPanel.getBinCount();
            int binCount = 20;
            TimeSeries timeSeries = fileMetadata.timeSeriesMap.get(variableName);
            ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
            double values[] = new double[records.size()];
            for (int i = 0; i < values.length; i++) {
                values[i] = records.get(i).value;
            }
            Histogram histogram = new Histogram(timeSeries.getName(), values, binCount);
            multiHistogramPanel.addHistogram(histogram);

//            for (int icol = 0; icol < dataTable.getColumnCount(); icol++) {
//                if (dataTable.getColumnName(icol).equals(variableClipboardData.variableName)) {
//                    Column column = dataTable.getColumn(icol);
//                    double values[] = new double[column.getRowCount()];
//                    for (int i = 0; i < column.getRowCount(); i++) {
//                        values[i] = column.getDouble(i);
//                    }
//
//                    Histogram histogram = new Histogram(dataTable.getColumnName(icol), values, binCount);
//                    multiHistogramPanel.addHistogram(histogram);
//                    break;
//                }
//            }
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            try {
                double variableData []= PLGFileReader.readPLGFileAsDoubleArray(fileMetadata.file, variableName);
                Histogram histogram = new Histogram(variableName, variableData, /*multiHistogramPanel.getBinCount()*/20);
                multiHistogramPanel.addHistogram(histogram);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadColumnIntoMultiTimeSeries (FileMetadata fileMetadata, String variableName) {
        if (fileMetadata.fileType == FileMetadata.FileType.CSV) {
            TimeSeries timeSeries = fileMetadata.timeSeriesMap.get(variableName);
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
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);
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

    private Node createMultiViewPanel() {
        multiViewPanel = new MultiViewPanel(160);
        multiViewPanel.setBackground(java.awt.Color.WHITE);

        HBox settingsHBox = new HBox();
        settingsHBox.setAlignment(Pos.CENTER_LEFT);
        settingsHBox.setPadding(new javafx.geometry.Insets(4));
        settingsHBox.setSpacing(8.);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiViewPlotHeightSpinner = new Spinner(40, 400, multiViewPanel.getPlotHeight());
        multiViewPlotHeightSpinner.setEditable(true);
        multiViewPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setPlotHeight((Integer)newValue));
        multiViewPlotHeightSpinner.setPrefWidth(80.);
        hBox.getChildren().addAll(new Label("Plot Height: "), multiViewPlotHeightSpinner);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleViewPlotChronoUnitWidthSpinner = new Spinner(1, 40, multiViewPanel.getChronoUnitWidth());
        multipleViewPlotChronoUnitWidthSpinner.setEditable(true);
        multipleViewPlotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setChronoUnitWidth((Integer)newValue));
        multipleViewPlotChronoUnitWidthSpinner.setPrefWidth(80.);
        hBox.getChildren().addAll(new Label("Plot Unit Width: "), multipleViewPlotChronoUnitWidthSpinner);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleViewHistogramBinSizeSpinner = new Spinner(2, 200, multiViewPanel.getBinCount());
        multipleViewHistogramBinSizeSpinner.setEditable(true);
        multipleViewHistogramBinSizeSpinner.setPrefWidth(80.);
        multipleViewHistogramBinSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setBinCount((Integer)newValue));
        hBox.getChildren().addAll(new Label("Histogram Bin Count: "), multipleViewHistogramBinSizeSpinner);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multipleViewDataColorPicker = new ColorPicker(dataColor);
        multipleViewDataColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color dataColor = multipleViewDataColorPicker.getValue();
                multiViewPanel.setDataColor(convertToAWTColor(dataColor));
            }
        });
        hBox.getChildren().addAll(new Label("Data Color: "), multipleViewDataColorPicker);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiViewChronoUnitChoice = new ChoiceBox<ChronoUnit>();
        multiViewChronoUnitChoice.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        multiViewChronoUnitChoice.getSelectionModel().select(multiViewPanel.getDetailChronoUnit());
        multiViewChronoUnitChoice.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends ChronoUnit> ov,
                 ChronoUnit oldValue, ChronoUnit newValue) -> {
                    if (oldValue != newValue) {
                        multiViewPanel.setDetailChronoUnit(newValue);
                    }
                }
        );
        hBox.getChildren().addAll(new Label("Plot Chrono Unit: "), multiViewChronoUnitChoice);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiViewSyncScrollbarsToggleButton = new ToggleButton("Sync Scrollbars");
        multiViewSyncScrollbarsToggleButton.setSelected(multiViewPanel.getSyncGroupScrollbarsEnabled());
        multiViewSyncScrollbarsToggleButton.selectedProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setSyncGroupScollbarsEnabled((Boolean)newValue));
        hBox.getChildren().add(multiViewSyncScrollbarsToggleButton);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiViewShowOverviewToggleButton = new ToggleButton("Show Overview");
        multiViewShowOverviewToggleButton.setSelected(multiViewPanel.getShowOverviewEnabled());
        multiViewShowOverviewToggleButton.selectedProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setShowOverviewEnabled((Boolean)newValue));
        hBox.getChildren().add(multiViewShowOverviewToggleButton);
        settingsHBox.getChildren().add(hBox);

        hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        multiViewShowButtonsToggleButton = new ToggleButton("Show Buttons");
        multiViewShowButtonsToggleButton.setTooltip(new Tooltip("Show or hide the buttons for each variable view panel"));
        multiViewShowButtonsToggleButton.setSelected(multiViewPanel.getShowButtonPanelsEnabled());
        multiViewShowButtonsToggleButton.selectedProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setShowButtonPanelsEnabled((Boolean)newValue));
        hBox.getChildren().add(multiViewShowButtonsToggleButton);
        settingsHBox.getChildren().add(hBox);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JScrollPane scroller = new JScrollPane(multiViewPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(1);
//        scroller.setBorder(border);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(scroller);
        swingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        swingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                VariableClipboardData variableClipboardData = (VariableClipboardData)db.getContent(objectDataFormat);

//                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);
                FileMetadata fileMetadata = fileMetadataMap.get(variableClipboardData.getFile());
                loadColumnIntoMultiView(fileMetadata, variableClipboardData.getVariableName());

                event.setDropCompleted(true);
            }
            event.consume();
        });

        final ContextMenu cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("Something");
        cm.getItems().add(cmItem1);

        swingNode.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getButton() == MouseButton.SECONDARY) {
                            cm.show(swingNode, event.getScreenX(), event.getScreenY());
                        }
                    }
                });

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(settingsHBox);
        borderPane.setCenter(swingNode);
        return borderPane;
    }

    private Node createMultiHistogramPanel() {
        multiHistogramPanel = new MultiHistogramPanel(120);
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
        multipleHistogramBinSizeSpinner = new Spinner(2, 200, multiHistogramPanel.getBinCount());
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
                VariableClipboardData variableClipboardData = (VariableClipboardData)db.getContent(objectDataFormat);

//                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);
                FileMetadata fileMetadata = fileMetadataMap.get(variableClipboardData.getFile());
                loadColumnIntoMultiHistogram(fileMetadata, variableClipboardData.getVariableName());

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
//                FalconDataTreeItem dataTreeItem = (FalconDataTreeItem)db.getContent(objectDataFormat);
                VariableClipboardData variableClipboardData = (VariableClipboardData)db.getContent(objectDataFormat);
                FileMetadata fileMetadata = fileMetadataMap.get(variableClipboardData.getFile());
                loadColumnIntoMultiTimeSeries(fileMetadata, variableClipboardData.getVariableName());

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

        overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.LINE);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1400, 100));
        overviewTimeSeriesPanel.setBackground(java.awt.Color.white);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        overviewTimeSeriesPanel.setBorder(BorderFactory.createTitledBorder(border, "Overview"));

        JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
        scroller.getHorizontalScrollBar().setUnitIncrement(2);
        scroller.setBorder(BorderFactory.createTitledBorder(border, "Detail"));

        overviewDetailTimeSeriesNameLabel = new JLabel(" ");
        overviewDetailTimeSeriesNameLabel.setFont(overviewDetailTimeSeriesNameLabel.getFont().deriveFont(java.awt.Font.BOLD));
        overviewDetailTimeSeriesNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        overviewDetailPanel = new JPanel();
        overviewDetailPanel.setLayout(new BorderLayout());
        overviewDetailPanel.add(overviewDetailTimeSeriesNameLabel, BorderLayout.NORTH);
        overviewDetailPanel.add(scroller, BorderLayout.CENTER);
        overviewDetailPanel.add(overviewTimeSeriesPanel, BorderLayout.SOUTH);
        overviewDetailPanel.setBackground(java.awt.Color.white);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(overviewDetailPanel);
        tsSwingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        tsSwingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                VariableClipboardData variableClipboardData = (VariableClipboardData)db.getContent(objectDataFormat);
                FileMetadata fileMetadata = fileMetadataMap.get(variableClipboardData.getFile());
                loadColumnIntoODTimeSeries(fileMetadata, variableClipboardData.getVariableName());
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
