package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.Talon.Talon;
import gov.ornl.csed.cda.coalesce.Utilities;
import gov.ornl.csed.cda.timevis.*;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;


public class FalconMain extends Application {
    private final static Logger log = LoggerFactory.getLogger(FalconMain.class);
    private final static String version = "v0.2.3";

    // used for drag and drop interface
    private final DataFormat objectDataFormat = new DataFormat("application/x-java-serialized-object");

    // user preferences class
    private Preferences preferences;

    // Multi View Panel Objects
    private MultiViewPanel multiViewPanel;

    // UI components
    private TreeView<String> dataTreeView;
    private TreeItem<String> dataTreeRoot;
    private TableView columnTableView;
    private TableView dataTableView;
    private Font fontAwesomeFont;

    // For keeping track of file metadata and linking to tree items
    private HashMap<File, FileMetadata> fileMetadataMap = new HashMap<>();
    private HashMap<TreeItem<String>, FileMetadata> fileTreeItemMetadataMap = new HashMap<>();

    private HashMap<TimeSeriesSelection, SelectionViewInfo> selectionViewInfoMap = new HashMap<>();

    private SelectionDetailsPanel selectionDetailPanel;
    private Spinner multipleViewHistogramBinSizeSpinner;
    private Spinner multiViewPlotHeightSpinner;
    private CheckBox multiViewShowButtonsCheckBox;
    private Spinner multiViewPlotChronoUnitWidthSpinner;
    private ChoiceBox<NumericTimeSeriesPanel.PlotDisplayOption>  multiViewPlotDisplayOptionChoiceBox;
    private ChoiceBox<ChronoUnit> multiViewChronoUnitChoice;
    private ChoiceBox<NumericTimeSeriesPanel.MovingRangeDisplayOption> multiViewMovingRangeDisplayOptionChoiceBox;
    private ColorPicker multiViewPointColorPicker;
    private ColorPicker multiViewLineColorPicker;
    private ColorPicker multiViewStdevRangeLineColorPicker;
    private ColorPicker multiViewMinMaxRangeLineColorPicker;
    private ColorPicker multiViewSpectrumPositiveColorPicker;
    private ColorPicker multiViewSpectrumNegativeColorPicker;
    private CheckBox multiViewSyncScrollbarsCheckBox;

    public static void main(String[] args) {
        launch(args);
    }

    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                FileChooser fileChooser = new FileChooser();

                String lastPLGReadDirectory = preferences.get(FalconPreferenceKeys.LAST_PLG_READ_DIRECTORY, "");
                if (!lastPLGReadDirectory.isEmpty()) {
                    log.debug("LAST_PLG_READ_DIRECTORY is " + lastPLGReadDirectory);
                    if(new File(lastPLGReadDirectory).exists()) {
                        fileChooser.setInitialDirectory(new File(lastPLGReadDirectory));
                    }
                }
                fileChooser.setTitle("Open PLG File");
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    try {
                        openPLGFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        InputStream is = FalconMain.class.getResourceAsStream("fontawesome-webfont.ttf");
        fontAwesomeFont = javafx.scene.text.Font.loadFont(is, 14);

        BorderPane rootNode = new BorderPane();

        javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(rootNode, screenBounds.getWidth()-20, screenBounds.getHeight()*.7);

        Node multiViewNode = createMultiViewPanel();
        Node selectionViewNode = createSelectionDetailPanel(primaryStage);
        Node sideSettingsNode = createSettingsPane();

        createDataTreeView();
        createColumnTableView();
        createDataTableView();

        // left panel
        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(Orientation.VERTICAL);
        leftSplit.getItems().addAll(dataTreeView, sideSettingsNode);
        leftSplit.setResizableWithParent(sideSettingsNode, false);
        leftSplit.setMaxWidth(400.);

        // right panel (selection view)
        StackPane rightStackPane = new StackPane();
        rightStackPane.getChildren().add(selectionViewNode);
        rightStackPane.setMinWidth(200);

        // create main split between left and right panes
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(leftSplit, multiViewNode, rightStackPane);
        mainSplitPane.setDividerPositions(0.2, .9);
        mainSplitPane.setResizableWithParent(leftSplit, false);
        mainSplitPane.setResizableWithParent(rightStackPane, false);

        MenuBar menubar = createMenuBar(primaryStage);

        rootNode.setTop(menubar);
        rootNode.setCenter(mainSplitPane);

        primaryStage.setTitle("Falcon");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void captureVisualizationImage(File imageFile) throws IOException {
        int imageWidth = multiViewPanel.getWidth() * 4;
        int imageHeight = multiViewPanel.getHeight() * 4;


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

        multiViewPanel.paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", imageFile);
    }

    private void openPLGFile(File plgFile) throws IOException {
        HashMap<String, PLGVariableSchema> tmp = PLGFileReader.readVariableSchemas(plgFile);

        TreeMap<String, PLGVariableSchema> variableSchemaMap = new TreeMap<>();

        for (Map.Entry<String, PLGVariableSchema> entry : tmp.entrySet()) {
            variableSchemaMap.put(entry.getKey(), entry.getValue());
        }

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

        preferences.put(FalconPreferenceKeys.LAST_PLG_READ_DIRECTORY, plgFile.getParentFile().getAbsolutePath());
    }

    private void openCSVFile(File csvFile) throws IOException {
        // read first line of CSV File
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        String headerLine = reader.readLine();
        if (headerLine == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("CSV File Reader Error");
            alert.setContentText("Aborting the file read operation because the file appears to be empty.");
            alert.showAndWait();
            return;
        }
        reader.close();

        LineNumberReader lineNumberReader = new LineNumberReader(
                new FileReader(csvFile));
        lineNumberReader.skip(Long.MAX_VALUE);
        int numLines = lineNumberReader.getLineNumber() - 1;
        lineNumberReader.close();

        log.debug("Number of lines is " + numLines);

        // Parse column names
        String columnNames[] = headerLine.split(",");

        // prompt the user for the time column name
        ChoiceDialog<String> dialog = new ChoiceDialog<>(columnNames[0], columnNames);
        dialog.setTitle("Time Dimension");
        dialog.setHeaderText("Select the Time Column");
        dialog.setContentText("Time Column: ");

        int timeColumnIndex = -1;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
//            log.debug("time dimension is " + result.get());
            String timeColumnName = result.get();
            for (int i = 0; i < columnNames.length; i++) {
                String columnName = columnNames[i];
                if (columnName.equals(timeColumnName)) {
                    timeColumnIndex = i;
                    break;
                }
            }
        } else {
//            dataTable = null;
            return;
        }

        // prompt the user for the time units (milliseconds or seconds)
        ChoiceBox<ChronoUnit> timeChronoUnitChoiceBox = new ChoiceBox<>();
        timeChronoUnitChoiceBox.getSelectionModel().select(0);

        ChronoUnit timeChronoUnit = null;
        ChoiceDialog<ChronoUnit> timeChronoUnitChoiceDialog = new ChoiceDialog<>(ChronoUnit.MILLIS, FXCollections.observableArrayList(ChronoUnit.MILLIS, ChronoUnit.SECONDS));
        timeChronoUnitChoiceDialog.setTitle("Time Chronological Unit");
        timeChronoUnitChoiceDialog.setHeaderText("Select the Chronological Unit of Time");
        timeChronoUnitChoiceDialog.setContentText("Time Chronological Unit: ");
        Optional<ChronoUnit> timeChronoUnitResult = timeChronoUnitChoiceDialog.showAndWait();
        if (timeChronoUnitResult.isPresent()) {
            timeChronoUnit = timeChronoUnitResult.get();
        } else {
//            dataTable = null;
            return;
        }

        // populate data tree and create file metadata record
        Text itemIcon = new Text("\uf1c0");
        itemIcon.setFont(fontAwesomeFont);
        itemIcon.setFontSmoothingType(FontSmoothingType.LCD);
        TreeItem<String>fileTreeItem = new TreeItem<>(csvFile.getName());
        FileMetadata fileMetadata = new FileMetadata(csvFile);
        fileMetadata.fileType = FileMetadata.FileType.CSV;
        fileMetadata.timeColumnIndex = timeColumnIndex;
        fileMetadata.timeChronoUnit = timeChronoUnit;
        fileTreeItemMetadataMap.put(fileTreeItem, fileMetadata);
        fileMetadataMap.put(csvFile, fileMetadata);

        // build time series for all variables in table
        fileMetadata.timeSeriesMap = new HashMap<>();
//        int timeColumnIdx = dataTable.getColumnNumber(timeColumnName);
//        for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
//            if (!dataTable.getColumnName(icolumn).equals(timeColumnName)) {
//                TimeSeries timeSeries = new TimeSeries(dataTable.getColumnName(icolumn));
//                for (int ituple = 0; ituple < dataTable.getTupleCount(); ituple++) {
//                    Instant instant = null;
//                    if (timeChronoUnit == ChronoUnit.MILLIS) {
//                        instant = Instant.ofEpochMilli(dataTable.getLong(ituple, timeColumnIdx));
//                    } else {
//                        double seconds = dataTable.getDouble(ituple, timeColumnIdx);
//                        long timeMillis = (long)(seconds * 1000.);
//                        instant = Instant.ofEpochMilli(timeMillis);
//                    }
//                    if (dataTable.canGetDouble(dataTable.getColumnName(icolumn))) {
//                        double value = dataTable.getDouble(ituple, icolumn);
//                        if (!Double.isNaN(value)) {
//                            timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//                        }
//                    }
//                }
//
//                fileMetadata.timeSeriesMap.put(dataTable.getColumnName(icolumn), timeSeries);
//            }
//        }

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            fileMetadata.variableList.add(columnName);
            fileMetadata.variableValueCountList.add(numLines);

            if (i != timeColumnIndex) {
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
            }
        }

        dataTreeRoot.getChildren().addAll(fileTreeItem);
        preferences.put(FalconPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
    }
    /*
    private void openCSVFile(File csvFile) throws IOException {
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
            }
        }

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
            }
        }
        dataTreeRoot.getChildren().addAll(fileTreeItem);

        // populate column table view

        // populate data table view
    }
*/

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        Menu falcon = new Menu("Falcon");
        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                String lastCSVDirectoryPath = preferences.get(FalconPreferenceKeys.LAST_CSV_READ_DIRECTORY, "");
                if (!lastCSVDirectoryPath.isEmpty()) {
                    fileChooser.setInitialDirectory(new File(lastCSVDirectoryPath));
                }
                fileChooser.setTitle("Open CSV File");
                File csvFile = fileChooser.showOpenDialog(primaryStage);
                if (csvFile != null) {
                    try {
                        openCSVFile(csvFile);
                    } catch (IOException e) {
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
                String lastPLGDirectoryPath = preferences.get(FalconPreferenceKeys.LAST_PLG_READ_DIRECTORY, "");
                if (!lastPLGDirectoryPath.isEmpty()) {
                    fileChooser.setInitialDirectory(new File(lastPLGDirectoryPath));
                }

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

        MenuItem saveTemplateMI = new MenuItem("Save View Template...");
        saveTemplateMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setTitle("Select View Template File");
                File templateFile = fileChooser.showSaveDialog(primaryStage);
                if (!templateFile.getName().endsWith(".vtf")) {
                    templateFile = new File(templateFile.getAbsolutePath() + ".vtf");
                }
                if (templateFile != null) {
                    try {
                        saveViewTemplate(templateFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        MenuItem loadTemplateMI = new MenuItem("Load View Template...");
        loadTemplateMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // if timeseries are being shown, ask user if they want to clear the display
                if (!multiViewPanel.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Reset Display");
                    alert.setHeaderText("Clear existing visualization panels?");
                    alert.setContentText("Choose your preference");

                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.YES) {
                        multiViewPanel.removeAllTimeSeries();
                        selectionDetailPanel.removeAllSelections();
                    }
                }

                // get the template file
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setTitle("Load View Template File");
                File templateFile = fileChooser.showOpenDialog(primaryStage);
                if (templateFile != null) {
                    try {
                        loadViewTemplate(templateFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
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
                if(!imageFile.getName().endsWith(".png")) {
                    imageFile = new File(imageFile.getAbsolutePath() + ".png");
                }
                if (imageFile != null) {
                    try{
                        captureVisualizationImage(imageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MenuItem aboutFalcon = new MenuItem("About Falcon");
        aboutFalcon.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("About");
                alert.setHeaderText("About Falcon " + version);
                String s = "Falcon is developed and maintained by the Computational Data Analytics Group \nat Oak Ridge National Laboratory.\n\nThe lead developer is Dr. Chad Steed\nOther developers:\tWilliam Halsey\n\n\u00a9 2015 - 2016";
                alert.setContentText(s);
                alert.show();

            }
        });

        MenuItem exitMI = new MenuItem("Exit");
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                primaryStage.close();
            }
        });

        MenuItem clearVisualizationsMI = new MenuItem("Remove All Time Series Visualzations");
        clearVisualizationsMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                multiViewPanel.removeAllTimeSeries();
                selectionDetailPanel.removeAllSelections();
            }
        });

        MenuItem resetDisplayPreferencesMI = new MenuItem("Reset Display Preferences to Default Values");
        resetDisplayPreferencesMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // for each display preference get default value and set UI component (this will
                // also cause the preferences to be updated
            }
        });

        MenuItem talonWindow = new MenuItem("Talon Window");
        talonWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!fileMetadataMap.isEmpty()) {

                    File temp = null;
                    TreeMap<String, File> files = new TreeMap<>();

                    for (File file : fileMetadataMap.keySet()) {
                        files.put(file.getName(), file);
                    }

//                    log.debug(files.toString());

                    ListView<String> listView = new ListView<String>();
                    ObservableList<String> observableList = FXCollections.observableArrayList(files.keySet());
                    listView.setItems(observableList);

                    VBox box = new VBox();
                    box.setAlignment(Pos.CENTER);
                    box.setPadding(new Insets(1, 1, 1, 1));
                    box.setSpacing(3);
                    box.getChildren().add(listView);

                    Button select = new Button("Select");
                    box.getChildren().add(select);

                    Stage stage = new Stage();
                    Scene scene = new Scene(box);

                    select.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            String selection = listView.getSelectionModel().getSelectedItem();

                            if (selection != null && !selection.isEmpty()) {
                                new Talon(files.get(selection));
                            } else {
                                new Talon();
                            }

                            stage.close();
                        }
                    });

                    stage.setScene(scene);

                    stage.show();

                } else {
                    new Talon();
                }
            }
        });

        falcon.getItems().addAll(aboutFalcon);
        fileMenu.getItems().addAll(openCSVMI, openPLGMI, new SeparatorMenuItem(), saveTemplateMI, loadTemplateMI, new SeparatorMenuItem(), captureScreenMI, new SeparatorMenuItem(), exitMI);
        viewMenu.getItems().addAll(talonWindow, clearVisualizationsMI);

        menuBar.getMenus().addAll(falcon, fileMenu, viewMenu);

        return menuBar;
    }

    private void saveViewTemplate(File f) throws IOException {
        Properties properties = new Properties();

        // get variables in the view now
        ArrayList<String> variableNames = multiViewPanel.getTimeSeriesNames();
        StringBuffer variableNamesBuffer = new StringBuffer();
        for (int i = 0; i < variableNames.size(); i++) {
            String variableName = variableNames.get(i).substring(variableNames.get(i).indexOf(":") + 1);
            if ((i + 1) < variableNames.size()) {
                variableNamesBuffer.append(variableName + ",");
            } else {
                variableNamesBuffer.append(variableName);
            }
        }

        properties.setProperty(FalconViewTemplateKeys.VARIABLES, variableNamesBuffer.toString().trim());
        properties.setProperty(FalconViewTemplateKeys.GENERAL_HISTOGRAM_BIN_COUNT, String.valueOf(multiViewPanel.getBinCount()));
        properties.setProperty(FalconViewTemplateKeys.GENERAL_VARIABLE_PANEL_HEIGHT, String.valueOf(multiViewPanel.getPlotHeight()));
        properties.setProperty(FalconViewTemplateKeys.GENERAL_SHOW_BUTTONS_CHECKBOX, String.valueOf(multiViewPanel.getShowButtonPanelsEnabled()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_PLOT_UNIT_WIDTH, String.valueOf(multiViewPanel.getChronoUnitWidth()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_DETAIL_PLOT_DISPLAY_OPTION, String.valueOf(multiViewPanel.getDetailTimeSeriesPlotDisplayOption()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_PLOT_CHRONO_UNIT, String.valueOf(multiViewPanel.getDetailChronoUnit()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_MOVING_RANGE_DISPLAY, String.valueOf(multiViewPanel.getMovingRangeDisplayOption()));

        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_POINT_COLOR, convertColorToString(multiViewPanel.getTimeSeriesPointColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_LINE_COLOR, convertColorToString(multiViewPanel.getTimeSeriesLineColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_STDDEV_RANGE_COLOR, convertColorToString(multiViewPanel.getTimeSeriesStandardDeviationRangeColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_MINMAX_RANGE_COLOR, convertColorToString(multiViewPanel.getTimeSeriesMinMaxRangeColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_SPECTRUM_POSITIVE_COLOR, convertColorToString(multiViewPanel.getTimeSeriesSpectrumPositiveColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_SPECTRUM_NEGATIVE_COLOR, convertColorToString(multiViewPanel.getTimeSeriesSpectrumNegativeColor()));
        properties.setProperty(FalconViewTemplateKeys.TIME_SERIES_SYNC_TIME_SERIES_SCROLL_BARS, String.valueOf(multiViewPanel.getSyncGroupScrollbarsEnabled()));
//        properties.setProperty(FalconViewTemplateKeys.SELECTION_DETAILS_PLOT_HEIGHT, String.valueOf(selectionDetailPanel.getPlotHeight()));
//        properties.setProperty(FalconViewTemplateKeys.SELECTION_DETAILS_BIN_COUNT, String.valueOf(selectionDetailPanel.getBinCount()));

        OutputStream outputStream = new FileOutputStream(f);
        properties.store(outputStream, null);
    }

    private String convertColorToString(java.awt.Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }

    private java.awt.Color convertStringToColor(String string) {
        String items[] = string.split(",");
        int red = Integer.valueOf(items[0].trim());
        int green = Integer.valueOf(items[1].trim());
        int blue = Integer.valueOf(items[2].trim());
        int alpha = Integer.valueOf(items[3].trim());
        return new java.awt.Color(red, green, blue, alpha);
    }

    private void loadViewTemplate(File f) throws IOException {
        ArrayList<FileMetadata> targetFiles = new ArrayList<>();
        if (fileMetadataMap.size() > 1) {
            // prompt user to specify which of the opened files to apply the template settings to
            Dialog<ObservableList<File>> filesDialog = new Dialog<>();
            filesDialog.setHeaderText("Template Target Selection");
            filesDialog.setContentText("Choose Open File(s) to Apply Template");

            // Set the button types
            filesDialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

            BorderPane borderPane = new BorderPane();

            ObservableList<File> filenames = FXCollections.observableArrayList();
            for (FileMetadata fileMetadata : fileMetadataMap.values()) {
                filenames.add(fileMetadata.file);
            }

            ListView<File> fileListView = new ListView<>(filenames);
            fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            borderPane.setCenter(fileListView);

            filesDialog.getDialogPane().setContent(borderPane);

            // Request focus on the username field by default.
            Platform.runLater(() -> fileListView.requestFocus());

            // Convert the result to a username-password-pair when the login button is clicked.
            filesDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    return fileListView.getSelectionModel().getSelectedItems();
                }
                return null;
            });

            Optional<ObservableList<File>> result = filesDialog.showAndWait();

            if (result.isPresent()) {
                for (File file : result.get()) {
                    targetFiles.add(fileMetadataMap.get(file));
                }
            }
        } else if (fileMetadataMap.size() == 1) {
            targetFiles.addAll(fileMetadataMap.values());
        } else if (fileMetadataMap.isEmpty()) {
            // TODO: Maybe show a error message here?
            return;
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(f));

        // get display settings and setup display
        String strValue = properties.getProperty(FalconViewTemplateKeys.GENERAL_HISTOGRAM_BIN_COUNT);
        if (strValue != null) {
            int generalHistogramBinCount = Integer.valueOf(strValue);
            multiViewPanel.setBinCount(generalHistogramBinCount);
            multipleViewHistogramBinSizeSpinner.getValueFactory().setValue(generalHistogramBinCount);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.GENERAL_VARIABLE_PANEL_HEIGHT);
        if (strValue != null) {
            int generalPlotHeight = Integer.valueOf(strValue);
            multiViewPanel.setPlotHeight(generalPlotHeight);
            multiViewPlotHeightSpinner.getValueFactory().setValue(generalPlotHeight);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.GENERAL_SHOW_BUTTONS_CHECKBOX);
        if (strValue != null) {
            boolean showButtons = Boolean.valueOf(strValue);
            multiViewPanel.setShowButtonPanelsEnabled(showButtons);
            multiViewShowButtonsCheckBox.setSelected(showButtons);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_PLOT_UNIT_WIDTH);
        if (strValue != null) {
            int timeSeriesPlotUnitWidth = Integer.valueOf(strValue);
            multiViewPanel.setChronoUnitWidth(timeSeriesPlotUnitWidth);
            multiViewPlotChronoUnitWidthSpinner.getValueFactory().setValue(timeSeriesPlotUnitWidth);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_DETAIL_PLOT_DISPLAY_OPTION);
        if (strValue != null) {
            NumericTimeSeriesPanel.PlotDisplayOption timeSeriesDisplayOption = NumericTimeSeriesPanel.PlotDisplayOption.valueOf(strValue);
            multiViewPanel.setDetailTimeSeriesPlotDisplayOption(timeSeriesDisplayOption);
            multiViewPlotDisplayOptionChoiceBox.setValue(timeSeriesDisplayOption);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_PLOT_CHRONO_UNIT);
        if (strValue != null) {
            ChronoUnit timeSeriesChronoUnit = ChronoUnit.valueOf(strValue.toUpperCase());
            multiViewPanel.setDetailChronoUnit(timeSeriesChronoUnit);
            multiViewChronoUnitChoice.setValue(timeSeriesChronoUnit);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_MOVING_RANGE_DISPLAY);
        if (strValue != null) {
            NumericTimeSeriesPanel.MovingRangeDisplayOption timeSeriesMovingRangeDisplayOption = NumericTimeSeriesPanel.MovingRangeDisplayOption.valueOf(strValue);
            multiViewPanel.setMovingRangeDisplayOption(timeSeriesMovingRangeDisplayOption);
            multiViewMovingRangeDisplayOptionChoiceBox.setValue(timeSeriesMovingRangeDisplayOption);
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_POINT_COLOR);
        if (strValue != null) {
            java.awt.Color pointColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesPointColor(pointColor);
            multiViewPointColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(pointColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_LINE_COLOR);
        if (strValue != null) {
            java.awt.Color lineColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesLineColor(lineColor);
            multiViewLineColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(lineColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_STDDEV_RANGE_COLOR);
        if (strValue != null) {
            java.awt.Color stDevRangeColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesStandardDeviationRangeColor(stDevRangeColor);
            multiViewStdevRangeLineColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(stDevRangeColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_MINMAX_RANGE_COLOR);
        if (strValue != null) {
            java.awt.Color minMaxRangeColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesMinMaxRangeColor(minMaxRangeColor);
            multiViewMinMaxRangeLineColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(minMaxRangeColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_SPECTRUM_POSITIVE_COLOR);
        if (strValue != null) {
            java.awt.Color spectrumPositiveColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesSpectrumPositiveColor(spectrumPositiveColor);
            multiViewSpectrumPositiveColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(spectrumPositiveColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_SPECTRUM_NEGATIVE_COLOR);
        if (strValue != null) {
            java.awt.Color spectrumNegativeColor = convertStringToColor(strValue);
            multiViewPanel.setTimeSeriesSpectrumNegativeColor(spectrumNegativeColor);
            multiViewSpectrumNegativeColorPicker.setValue(GraphicsUtil.convertToJavaFXColor(spectrumNegativeColor));
        }

        strValue = properties.getProperty(FalconViewTemplateKeys.TIME_SERIES_SYNC_TIME_SERIES_SCROLL_BARS);
        if (strValue != null) {
            Boolean syncScrollBars = Boolean.valueOf(strValue);
            multiViewPanel.setSyncGroupScollbarsEnabled(syncScrollBars);
            multiViewSyncScrollbarsCheckBox.setSelected(syncScrollBars);
        }
//
//        strValue = properties.getProperty(FalconViewTemplateKeys.SELECTION_DETAILS_PLOT_HEIGHT);
//        if (strValue != null) {
//            int selectionPlotHeight = Integer.valueOf(strValue);
//            selectionDetailPanel.setPlotHeight(selectionPlotHeight);
//        }
//
//        strValue = properties.getProperty(FalconViewTemplateKeys.SELECTION_DETAILS_BIN_COUNT);
//        if (strValue != null) {
//            int binCount = Integer.valueOf(strValue);
//            selectionDetailPanel.setBinCount(binCount);
//        }

        // get variables and load each one into the view
        String variablesString = properties.getProperty(FalconViewTemplateKeys.VARIABLES);
        if (variablesString != null && !(variablesString.trim().isEmpty())) {
            String variableNames[] = variablesString.split(",");
            for (FileMetadata fileMetadata : targetFiles) {
                for (String variableName : variableNames) {
                    // read variable time series from file
                    loadColumnIntoMultiView(fileMetadata, variableName);
                }
            }
        }
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
                                loadColumnIntoMultiView(fileMetadata, variableName);
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
                            VariableClipboardData variableClipboardData = treeItemToVariableClipboardData(treeItem);

                            log.debug("clipboard data is " + variableClipboardData.getFile().getAbsolutePath()); // <--------------------

                            Dragboard db = treeCell.startDragAndDrop(TransferMode.COPY);
                            ClipboardContent content = new ClipboardContent();
                            content.put(objectDataFormat, variableClipboardData);
                            db.setContent(content);
                            event.consume();
                            Label label = new Label(String.format("Visualize %s", variableClipboardData.getVariableName()));
                            new Scene(label);
                            db.setDragView(label.snapshot(null, null));
                        }
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

    private void addSelectionView(NumericTimeSeriesPanel detailNumericTimeSeriesPanel, NumericTimeSeriesPanel overviewNumericTimeSeriesPanel,
                                  JScrollPane detailTimeSeriesPanelScrollPane, TimeSeriesSelection timeSeriesSelection) {
        overviewNumericTimeSeriesPanel.addTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
        selectionDetailPanel.addSelection(detailNumericTimeSeriesPanel, overviewNumericTimeSeriesPanel, detailTimeSeriesPanelScrollPane, timeSeriesSelection);
    }

    private void updateSelectionView(NumericTimeSeriesPanel detailNumericTimeSeriesPanel, NumericTimeSeriesPanel overviewNumericTimeSeriesPanel,
                                     TimeSeriesSelection timeSeriesSelection, Instant previousStartInstant, Instant previousEndInstant) {
        overviewNumericTimeSeriesPanel.updateTimeSeriesSelection(previousStartInstant, previousEndInstant, timeSeriesSelection.getStartInstant(),
                timeSeriesSelection.getEndInstant());
        selectionDetailPanel.updateSelection(timeSeriesSelection);
    }

    private void deleteSelectionView(NumericTimeSeriesPanel detailNumericTimeSeriesPanel, NumericTimeSeriesPanel overviewNumericTimeSeriesPanel,
                                     TimeSeriesSelection timeSeriesSelection) {
        overviewNumericTimeSeriesPanel.removeTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
        selectionDetailPanel.deleteSelection(timeSeriesSelection);
    }

    private TimeSeries readCSVVariableTimeSeries(FileMetadata fileMetadata, String variableName) throws IOException {
        if (fileMetadata.timeSeriesMap.containsKey(variableName)) {
            return fileMetadata.timeSeriesMap.get(variableName);
        } else {
            int variableIndex = fileMetadata.variableList.indexOf(variableName);

            TimeSeries timeSeries = new TimeSeries(fileMetadata.file.getName() + ":" + variableName);
            BufferedReader csvFileReader = new BufferedReader(new FileReader(fileMetadata.file));

            // skip first line
            csvFileReader.readLine();
            String line = null;
            while ((line = csvFileReader.readLine()) != null) {
                String tokens[] = line.split(",");

                // parse time value
                Instant instant = null;
                if (fileMetadata.timeChronoUnit == ChronoUnit.MILLIS) {
                    long timeMillis = Long.parseLong(tokens[fileMetadata.timeColumnIndex]);
                    instant = Instant.ofEpochMilli(timeMillis);

                    System.out.println(instant);
                } else {
                    double seconds = Double.parseDouble(tokens[fileMetadata.timeColumnIndex]);
                    long timeMillis = (long) (seconds * 1000.);
                    instant = Instant.ofEpochMilli(timeMillis);
                }

                // parse data value
                double value = Double.parseDouble(tokens[variableIndex]);
                if (!Double.isNaN(value)) {
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                } else {
                    log.debug("Ignored value: time instant: " + instant.toString() + " value: " + value);
                }
            }

            fileMetadata.timeSeriesMap.put(variableName, timeSeries);
            csvFileReader.close();

            return timeSeries;
        }
    }

    private void loadColumnIntoMultiView (FileMetadata fileMetadata, String variableName) {
        if (fileMetadata.fileType == FileMetadata.FileType.CSV) {

            // read variable time series from csv file
            TimeSeries timeSeries = null;
            try {
                timeSeries = readCSVVariableTimeSeries(fileMetadata, variableName);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("CSV File Read Error");
                alert.setContentText("An exception occurred while reading the file: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            multiViewPanel.addTimeSeries(timeSeries, fileMetadata.file.getName());

            NumericTimeSeriesPanel overviewTSPanel = multiViewPanel.getOverviewTimeSeriesPanel(timeSeries);
            NumericTimeSeriesPanel detailsTSPanel = multiViewPanel.getDetailTimeSeriesPanel(timeSeries);
            JScrollPane detailsTimeSeriesPanelScrollPanel = multiViewPanel.getDetailsTimeSeriesScrollPane(timeSeries);

            detailsTSPanel.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                @Override
                public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                    addSelectionView(detailsTSPanel, overviewTSPanel, detailsTimeSeriesPanelScrollPanel, timeSeriesSelection);
                }

                @Override
                public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                           Instant previousStartInstant, Instant previousEndInstant) {
                    updateSelectionView(detailsTSPanel, overviewTSPanel, timeSeriesSelection, previousStartInstant, previousEndInstant);
                }

                @Override
                public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                    deleteSelectionView(detailsTSPanel, overviewTSPanel, timeSeriesSelection);
                }
            });
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
                    timeSeries.setName(fileMetadata.file.getName() + ":" + timeSeries.getName());
                    multiViewPanel.addTimeSeries(timeSeries, fileMetadata.file.getAbsolutePath());

                    NumericTimeSeriesPanel overviewTSPanel = multiViewPanel.getOverviewTimeSeriesPanel(timeSeries);
                    NumericTimeSeriesPanel detailsTimeSeries = multiViewPanel.getDetailTimeSeriesPanel(timeSeries);
                    JScrollPane detailsTimeSeriesPanelScrollPanel = multiViewPanel.getDetailsTimeSeriesScrollPane(timeSeries);

                    detailsTimeSeries.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                        @Override
                        public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            addSelectionView(detailsTimeSeries, overviewTSPanel, detailsTimeSeriesPanelScrollPanel, timeSeriesSelection);
                        }

                        @Override
                        public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                                   Instant previousStartInstant, Instant previousEndInstant) {
                            updateSelectionView(detailsTimeSeries, overviewTSPanel, timeSeriesSelection, previousStartInstant,
                                    previousEndInstant);
                        }

                        @Override
                        public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            deleteSelectionView(detailsTimeSeries, overviewTSPanel, timeSeriesSelection);
                        }
                    });
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Node createSettingsPane() {
        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(4, 4, 4, 4));

        // Spinner for the histogram bin count in multi view panel
        int initialBinCount = preferences.getInt(FalconPreferenceKeys.MULTI_VIEW_HISTOGRAM_BIN_SIZE, multiViewPanel.getBinCount());
        multiViewPanel.setBinCount(initialBinCount);
        multipleViewHistogramBinSizeSpinner = new Spinner(2, 400, initialBinCount);
        multipleViewHistogramBinSizeSpinner.setEditable(true);
        multipleViewHistogramBinSizeSpinner.setTooltip(new Tooltip("Change Bin Count for Overview Histogram"));
        multipleViewHistogramBinSizeSpinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                multiViewPanel.setBinCount((Integer)newValue);
                preferences.putInt(FalconPreferenceKeys.MULTI_VIEW_HISTOGRAM_BIN_SIZE, (Integer)newValue);
            }
        });
        multipleViewHistogramBinSizeSpinner.setPrefWidth(100.);
        grid.add(new Label("Histogram Bin Count: "), 0, 0);
        grid.add(multipleViewHistogramBinSizeSpinner, 1, 0);

        // Spinner for the plot height in the multi view panel
        int initialPlotHeight = preferences.getInt(FalconPreferenceKeys.LAST_VARIABLE_PANEL_HEIGHT, multiViewPanel.getPlotHeight());
        multiViewPanel.setPlotHeight(initialPlotHeight);
        multiViewPlotHeightSpinner = new Spinner(40, 400, initialPlotHeight);
        multiViewPlotHeightSpinner.setEditable(true);
        multiViewPlotHeightSpinner.setTooltip(new Tooltip("Change Height of Variable Panels"));
        multiViewPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            multiViewPanel.setPlotHeight((Integer)newValue);
            preferences.putInt(FalconPreferenceKeys.LAST_VARIABLE_PANEL_HEIGHT, (Integer) newValue);
        });
        multiViewPlotHeightSpinner.setPrefWidth(100.);
        grid.add(new Label("Variable Panel Height: "), 0, 1);
        grid.add(multiViewPlotHeightSpinner, 1, 1);

        // Checkbox for showing or hiding button panel in the multi view panel
        multiViewShowButtonsCheckBox = new CheckBox("Show Button Panel");
        multiViewShowButtonsCheckBox.setTooltip(new Tooltip("Enable or Disable Side Button Panel"));
        multiViewShowButtonsCheckBox.setSelected(multiViewPanel.getShowButtonPanelsEnabled());
        multiViewShowButtonsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            multiViewPanel.setShowButtonPanelsEnabled((Boolean)newValue);
            preferences.putBoolean(FalconPreferenceKeys.LAST_SHOW_BUTTONS_CHECKBOX, (Boolean) newValue);
        });
        grid.add(multiViewShowButtonsCheckBox, 0, 2, 2, 1);

        ScrollPane scrollPane = new ScrollPane(grid);
        TitledPane generalTitledPane = new TitledPane("General Display Settings", scrollPane);

        // Create Time Series Plot Settings Pane
        grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(4, 4, 4, 4));

        // spinner for the plot unit width in the detail time series visualization of the multiview panel
        int lastChronoUnitWidth = preferences.getInt(FalconPreferenceKeys.LAST_CHRONO_UNIT_WIDTH, multiViewPanel.getChronoUnitWidth());
        multiViewPanel.setChronoUnitWidth(lastChronoUnitWidth);
        multiViewPlotChronoUnitWidthSpinner = new Spinner(1, 400, lastChronoUnitWidth);
        multiViewPlotChronoUnitWidthSpinner.setEditable(true);
        multiViewPlotChronoUnitWidthSpinner.setTooltip(new Tooltip("Change ChronoUnit Width in Detail Time Series Plot"));
        multiViewPlotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            multiViewPanel.setChronoUnitWidth((Integer)newValue);
            preferences.putInt(FalconPreferenceKeys.LAST_CHRONO_UNIT_WIDTH, (Integer) newValue);
        });
        multiViewPlotChronoUnitWidthSpinner.setPrefWidth(100.);
        grid.add(new Label("Plot Unit Width: "), 0, 0);
        grid.add(multiViewPlotChronoUnitWidthSpinner, 1, 0);

        // Choicebox for the detail time series visualization plot display mode
        multiViewPlotDisplayOptionChoiceBox = new ChoiceBox<>();
        multiViewPlotDisplayOptionChoiceBox.setTooltip(new Tooltip("Change Display Mode for Detail Time Series Plot"));
        multiViewPlotDisplayOptionChoiceBox.getItems().addAll(NumericTimeSeriesPanel.PlotDisplayOption.POINT, NumericTimeSeriesPanel.PlotDisplayOption.LINE,
                NumericTimeSeriesPanel.PlotDisplayOption.STEPPED_LINE, NumericTimeSeriesPanel.PlotDisplayOption.SPECTRUM);
        NumericTimeSeriesPanel.PlotDisplayOption lastPlotDisplayOption = NumericTimeSeriesPanel.PlotDisplayOption.valueOf( preferences.get(FalconPreferenceKeys.LAST_PLOT_DISPLAY_OPTION, multiViewPanel.getDetailTimeSeriesPlotDisplayOption().toString()) );
        multiViewPanel.setDetailTimeSeriesPlotDisplayOption(lastPlotDisplayOption);
        multiViewPlotDisplayOptionChoiceBox.getSelectionModel().select(lastPlotDisplayOption);
        multiViewPlotDisplayOptionChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends NumericTimeSeriesPanel.PlotDisplayOption> ov,
                 NumericTimeSeriesPanel.PlotDisplayOption oldValue, NumericTimeSeriesPanel.PlotDisplayOption newValue) -> {
                    if (oldValue != newValue) {
                        multiViewPanel.setDetailTimeSeriesPlotDisplayOption(newValue);
                        preferences.put(FalconPreferenceKeys.LAST_PLOT_DISPLAY_OPTION, newValue.toString());
                    }
                }
        );
        grid.add(new Label("Detail Plot Display Option: "), 0, 1);
        grid.add(multiViewPlotDisplayOptionChoiceBox, 1, 1);

        // Choicebox for selecting the detail time series visualizaton chronological unit
        multiViewChronoUnitChoice = new ChoiceBox<ChronoUnit>();
        multiViewChronoUnitChoice.setTooltip(new Tooltip("Change ChronoUnit for Detail Time Series Plot"));
        multiViewChronoUnitChoice.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        ChronoUnit lastChronoUnit = ChronoUnit.valueOf( preferences.get(FalconPreferenceKeys.LAST_CHRONO_UNIT, multiViewPanel.getDetailChronoUnit().toString()).toUpperCase() );
        multiViewPanel.setDetailChronoUnit(lastChronoUnit);
        multiViewChronoUnitChoice.getSelectionModel().select(lastChronoUnit);
        multiViewChronoUnitChoice.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends ChronoUnit> ov,
             ChronoUnit oldValue, ChronoUnit newValue) -> {
                if (oldValue != newValue) {
                    multiViewPanel.setDetailChronoUnit(newValue);
                    preferences.put(FalconPreferenceKeys.LAST_CHRONO_UNIT, newValue.toString().toUpperCase());
                }
            }
        );
        grid.add(new Label("Plot Chrono Unit: "), 0, 2);
        grid.add(multiViewChronoUnitChoice, 1, 2);

        // Choicebox for selecting the moving range display option of the detail time series visualization
        multiViewMovingRangeDisplayOptionChoiceBox = new ChoiceBox<>();
        multiViewMovingRangeDisplayOptionChoiceBox.setTooltip(new Tooltip("Choose Moving Range Display Option"));
        multiViewMovingRangeDisplayOptionChoiceBox.getItems().addAll(NumericTimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN, NumericTimeSeriesPanel.MovingRangeDisplayOption.PLOT_VALUE, NumericTimeSeriesPanel.MovingRangeDisplayOption.OPACITY);
        NumericTimeSeriesPanel.MovingRangeDisplayOption lastMovingRangeDisplayOption = NumericTimeSeriesPanel.MovingRangeDisplayOption.valueOf( preferences.get(FalconPreferenceKeys.LAST_MOVING_RANGE_DISPLAY_OPTION, multiViewPanel.getMovingRangeDisplayOption().toString()) );
        multiViewPanel.setMovingRangeDisplayOption(lastMovingRangeDisplayOption);
        multiViewMovingRangeDisplayOptionChoiceBox.getSelectionModel().select(lastMovingRangeDisplayOption);
        multiViewMovingRangeDisplayOptionChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends NumericTimeSeriesPanel.MovingRangeDisplayOption> ov,
                 NumericTimeSeriesPanel.MovingRangeDisplayOption oldValue, NumericTimeSeriesPanel.MovingRangeDisplayOption newValue) -> {
                    if (oldValue != newValue) {
                        multiViewPanel.setMovingRangeDisplayOption(newValue);
                        preferences.put(FalconPreferenceKeys.LAST_MOVING_RANGE_DISPLAY_OPTION, newValue.toString());
                    }
                }
        );
        grid.add(new Label("Moving Range Display: "), 0, 3);
        grid.add(multiViewMovingRangeDisplayOptionChoiceBox, 1, 3);

        // Colorpicker for selecting the multiview panel point color
        java.awt.Color lastPointColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_POINT_COLOR, multiViewPanel.getTimeSeriesPointColor().getRGB() ) );
        multiViewPanel.setTimeSeriesPointColor(lastPointColor);
        multiViewPointColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastPointColor));
        multiViewPointColorPicker.setTooltip(new Tooltip("Change Time Series Plot Point Color"));
        multiViewPointColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color dataColor = multiViewPointColorPicker.getValue();
                multiViewPanel.setTimeSeriesPointColor(GraphicsUtil.convertToAWTColor(dataColor));
                preferences.putInt(FalconPreferenceKeys.LAST_POINT_COLOR, GraphicsUtil.convertToAWTColor(dataColor).getRGB());
            }
        });
        grid.add(new Label("Point Color: "), 0, 4);
        grid.add(multiViewPointColorPicker, 1, 4);

        // Colorpicker for selecting the multi view line color
        java.awt.Color lastLineColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_LINE_COLOR, multiViewPanel.getTimeSeriesLineColor().getRGB()) );
        multiViewPanel.setTimeSeriesLineColor(lastLineColor);
        multiViewLineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastLineColor));
        multiViewLineColorPicker.setTooltip(new Tooltip("Change Time Series Plot Line Color"));
        multiViewLineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = multiViewLineColorPicker.getValue();
                multiViewPanel.setTimeSeriesLineColor(GraphicsUtil.convertToAWTColor(color));
                preferences.putInt(FalconPreferenceKeys.LAST_LINE_COLOR, GraphicsUtil.convertToAWTColor(color).getRGB());
            }
        });
        grid.add(new Label("Line Color: "), 0, 5);
        grid.add(multiViewLineColorPicker, 1, 5);

        // Colorpicker for selecting the multi view standard deviation range line color
        java.awt.Color lastStdDevColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_STDDEV_COLOR, multiViewPanel.getTimeSeriesStandardDeviationRangeColor().getRGB()) );
        multiViewPanel.setTimeSeriesStandardDeviationRangeColor(lastStdDevColor);
        multiViewStdevRangeLineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastStdDevColor));
        multiViewStdevRangeLineColorPicker.setTooltip(new Tooltip("Change Time Series Standard Deviation Range Line Color"));
        multiViewStdevRangeLineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = multiViewStdevRangeLineColorPicker.getValue();
                multiViewPanel.setTimeSeriesStandardDeviationRangeColor(GraphicsUtil.convertToAWTColor(color));
                preferences.putInt(FalconPreferenceKeys.LAST_STDDEV_COLOR, GraphicsUtil.convertToAWTColor(color).getRGB());
            }
        });
        grid.add(new Label("Standard Deviation Range Color: "), 0, 6);
        grid.add(multiViewStdevRangeLineColorPicker, 1, 6);

        // ColorPicker for selecting the multiView min/max range line color
        java.awt.Color lastMinMaxColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_MINMAX_COLOR, multiViewPanel.getTimeSeriesMinMaxRangeColor().getRGB()) );
        multiViewPanel.setTimeSeriesMinMaxRangeColor(lastMinMaxColor);
        multiViewMinMaxRangeLineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastMinMaxColor));
        multiViewMinMaxRangeLineColorPicker.setTooltip(new Tooltip("Change Time Series Min/Max Range Line Color"));
        multiViewMinMaxRangeLineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = multiViewMinMaxRangeLineColorPicker.getValue();
                multiViewPanel.setTimeSeriesMinMaxRangeColor(GraphicsUtil.convertToAWTColor(color));
                preferences.putInt(FalconPreferenceKeys.LAST_MINMAX_COLOR, GraphicsUtil.convertToAWTColor(color).getRGB());
            }
        });
        grid.add(new Label("Min/Max Range Color: "), 0, 7);
        grid.add(multiViewMinMaxRangeLineColorPicker, 1, 7);

        // ColorPicker for selecting the multiView spectrum positive values line color
        java.awt.Color lastSpectrumPositiveColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_SPECTRUM_POSITIVE_COLOR, multiViewPanel.getTimeSeriesSpectrumPositiveColor().getRGB()) );
        multiViewPanel.setTimeSeriesSpectrumPositiveColor(lastSpectrumPositiveColor);
        multiViewSpectrumPositiveColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastSpectrumPositiveColor));
        multiViewSpectrumPositiveColorPicker.setTooltip(new Tooltip("Change Time Series Spectrum Positive Value Color"));
        multiViewSpectrumPositiveColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = multiViewSpectrumPositiveColorPicker.getValue();
                multiViewPanel.setTimeSeriesSpectrumPositiveColor(GraphicsUtil.convertToAWTColor(color));
                preferences.putInt(FalconPreferenceKeys.LAST_SPECTRUM_POSITIVE_COLOR, GraphicsUtil.convertToAWTColor(color).getRGB());
            }
        });
        grid.add(new Label("Spectrum Positive Color: "), 0, 8);
        grid.add(multiViewSpectrumPositiveColorPicker, 1, 8);

        // ColorPicker for selecting the multiView spectrum negative value line color
        java.awt.Color lastSpectrumNegativeColor = new java.awt.Color( preferences.getInt(FalconPreferenceKeys.LAST_SPECTRUM_NEGATIVE_COLOR, multiViewPanel.getTimeSeriesSpectrumNegativeColor().getRGB()) );
        multiViewPanel.setTimeSeriesSpectrumNegativeColor(lastSpectrumNegativeColor);
        multiViewSpectrumNegativeColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(lastSpectrumNegativeColor));
        multiViewSpectrumNegativeColorPicker.setTooltip(new Tooltip("Change Time Series Spectrum Negative Value Color"));
        multiViewSpectrumNegativeColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = multiViewSpectrumNegativeColorPicker.getValue();
                multiViewPanel.setTimeSeriesSpectrumNegativeColor(GraphicsUtil.convertToAWTColor(color));
                preferences.putInt(FalconPreferenceKeys.LAST_SPECTRUM_NEGATIVE_COLOR, GraphicsUtil.convertToAWTColor(color).getRGB());
            }
        });
        grid.add(new Label("Spectrum Negative Color: "), 0, 9);
        grid.add(multiViewSpectrumNegativeColorPicker, 1, 9);

        // CheckBox for enabling/disabling synchronized scrollbars for variables of the same file in the multiView panel
        boolean lastSyncScrollBars = preferences.getBoolean(FalconPreferenceKeys.LAST_SYNC_SCROLL_BARS, multiViewPanel.getSyncGroupScrollbarsEnabled());
        multiViewPanel.setSyncGroupScollbarsEnabled(lastSyncScrollBars);
        multiViewSyncScrollbarsCheckBox = new CheckBox("Sync File TimeSeries Scrollbars");
        multiViewSyncScrollbarsCheckBox.setTooltip(new Tooltip("Sync Scrollbars for all TimeSeries from the Same File"));
        multiViewSyncScrollbarsCheckBox.setSelected(lastSyncScrollBars);
        multiViewSyncScrollbarsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            multiViewPanel.setSyncGroupScollbarsEnabled((Boolean)newValue);
            preferences.putBoolean(FalconPreferenceKeys.LAST_SYNC_SCROLL_BARS, (Boolean) newValue);
        });
        grid.add(multiViewSyncScrollbarsCheckBox, 0, 10, 2, 1);

        scrollPane = new ScrollPane(grid);
        TitledPane timeSeriesTitledPane = new TitledPane("TimeSeries Display Settings", scrollPane);

        // Create Histogram Settings Pane
        grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(4, 4, 4, 4));


        grid.add(new Label("Plot Height: "), 0, 0);
        int lastPlotHeight = preferences.getInt(FalconPreferenceKeys.LAST_PLOT_HEIGHT, selectionDetailPanel.getPlotHeight());
        selectionDetailPanel.setPlotHeight(lastPlotHeight);
        Spinner selectionPlotHeightSpinner = new Spinner(40, 400, lastPlotHeight);
        selectionPlotHeightSpinner.setTooltip(new Tooltip("Change Selection Details Panel Height"));
        selectionPlotHeightSpinner.setEditable(true);
        selectionPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            selectionDetailPanel.setPlotHeight((Integer)newValue);
            preferences.putInt(FalconPreferenceKeys.LAST_PLOT_HEIGHT, (Integer) newValue);
        });
        selectionPlotHeightSpinner.setPrefWidth(100.);
        grid.add(selectionPlotHeightSpinner, 1, 0);

        grid.add(new Label("Bin Count: "), 0, 1);
        int lastBinCount = preferences.getInt(FalconPreferenceKeys.LAST_BIN_COUNT, selectionDetailPanel.getBinCount());
        selectionDetailPanel.setBinCount(lastBinCount);
        Spinner selectionBinSizeSpinner = new Spinner(2, 400, lastBinCount);
        selectionBinSizeSpinner.setEditable(true);
        selectionBinSizeSpinner.setTooltip(new Tooltip("Change Selection Details Bin Count"));
        selectionBinSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            selectionDetailPanel.setBinCount((Integer)newValue);
            preferences.putInt(FalconPreferenceKeys.LAST_BIN_COUNT, (Integer) newValue);
        });
        selectionBinSizeSpinner.setPrefWidth(100.);
        grid.add(selectionBinSizeSpinner, 1, 1);

        scrollPane = new ScrollPane(grid);
        TitledPane selectionSettingsTitledPane = new TitledPane("Selection Details Display Settings", scrollPane);

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(generalTitledPane, timeSeriesTitledPane, selectionSettingsTitledPane);
        accordion.setExpandedPane(generalTitledPane);

        return accordion;
    }

    private Node createMultiViewPanel() {
        multiViewPanel = new MultiViewPanel(160);
        multiViewPanel.setBackground(java.awt.Color.WHITE);

        HBox settingsHBox = new HBox();
        settingsHBox.setAlignment(Pos.CENTER_LEFT);
        settingsHBox.setPadding(new javafx.geometry.Insets(4));
        settingsHBox.setSpacing(8.);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JScrollPane scroller = new JScrollPane(multiViewPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(1);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(scroller);
        swingNode.setOnDragOver(event -> event.acceptTransferModes(TransferMode.COPY));
        swingNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(objectDataFormat)) {
                VariableClipboardData variableClipboardData = (VariableClipboardData)db.getContent(objectDataFormat);
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

    private Node createSelectionDetailPanel (Stage primaryStage) {
        selectionDetailPanel = new SelectionDetailsPanel(120, 20);
        selectionDetailPanel.setBackground(java.awt.Color.WHITE);

        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        JScrollPane scroller = new JScrollPane(selectionDetailPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(2);
        scroller.setBorder(border);

        SwingNode tsSwingNode = new SwingNode();
        tsSwingNode.setContent(scroller);

        return tsSwingNode;
    }

    private class SelectionViewInfo {
        public NumericTimeSeriesPanel detailNumericTimeSeriesPanel;
        public JScrollPane detailsTimeSeriesPanelScrollPane;
        public TimeSeriesSelection selection;
    }
}
