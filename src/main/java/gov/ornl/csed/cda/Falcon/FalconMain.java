package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.Talon.Talon;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeriesPanelSelectionListener;
import gov.ornl.csed.cda.timevis.TimeSeriesSelection;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

public class FalconMain extends Application {
    private final static Logger log = LoggerFactory.getLogger(FalconMain.class);

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

                String lastPLGReadDirectory = preferences.get("LAST_PLG_READ_DIRECTORY", "");
                if (!lastPLGReadDirectory.isEmpty()) {
                    log.debug("LAST_PLG_READ_DIRECTORY is " + lastPLGReadDirectory);
                    fileChooser.setInitialDirectory(new File(lastPLGReadDirectory));
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
        leftSplit.setMaxWidth(350.);

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

        preferences.put("LAST_PLG_READ_DIRECTORY", plgFile.getParentFile().getAbsolutePath());
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
        preferences.put("LAST_CSV_READ_DIRECTORY", csvFile.getParentFile().getAbsolutePath());
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

        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                String lastCSVDirectoryPath = preferences.get("LAST_CSV_READ_DIRECTORY", "");
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
                String lastPLGDirectoryPath = preferences.get("LAST_PLG_READ_DIRECTORY", "");
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

        MenuItem talonWindow = new MenuItem("Talon Window");
        talonWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!fileMetadataMap.isEmpty()) {

                    File temp = null;

                    for (File file : fileMetadataMap.keySet()) {
                        temp = file;
                        break;
                    }

                    new Talon(temp);
                } else {
                    new Talon();
                }
            }
        });

        fileMenu.getItems().addAll(openCSVMI, openPLGMI, new SeparatorMenuItem(), captureScreenMI, new SeparatorMenuItem(), exitMI);
        viewMenu.getItems().add(talonWindow);

        menuBar.getMenus().addAll(fileMenu, viewMenu);

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

//                            log.debug("clipboard data is " + variableClipboardData.toString());
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

    private void addSelectionView(TimeSeriesPanel detailTimeSeriesPanel, TimeSeriesPanel overviewTimeSeriesPanel,
                                  JScrollPane detailTimeSeriesPanelScrollPane, TimeSeriesSelection timeSeriesSelection) {
        overviewTimeSeriesPanel.addTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
        selectionDetailPanel.addSelection(detailTimeSeriesPanel, overviewTimeSeriesPanel, detailTimeSeriesPanelScrollPane, timeSeriesSelection);
    }

    private void updateSelectionView(TimeSeriesPanel detailTimeSeriesPanel, TimeSeriesPanel overviewTimeSeriesPanel,
                                     TimeSeriesSelection timeSeriesSelection, Instant previousStartInstant, Instant previousEndInstant) {
        overviewTimeSeriesPanel.updateTimeSeriesSelection(previousStartInstant, previousEndInstant, timeSeriesSelection.getStartInstant(),
                timeSeriesSelection.getEndInstant());
        selectionDetailPanel.updateSelection(timeSeriesSelection);
    }

    private void deleteSelectionView(TimeSeriesPanel detailTimeSeriesPanel, TimeSeriesPanel overviewTimeSeriesPanel,
                                     TimeSeriesSelection timeSeriesSelection) {
//        log.debug("TimeSeries selection deleted");
        overviewTimeSeriesPanel.removeTimeSeriesSelection(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
        selectionDetailPanel.deleteSelection(timeSeriesSelection);
    }

    private TimeSeries readCSVVariableTimeSeries(FileMetadata fileMetadata, String variableName) throws IOException {
//        int valuesRead = 0;
//        int valuesStored = 0;
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
                } else {
                    double seconds = Double.parseDouble(tokens[fileMetadata.timeColumnIndex]);
                    long timeMillis = (long) (seconds * 1000.);
                    instant = Instant.ofEpochMilli(timeMillis);
                }

                // parse data value
                double value = Double.parseDouble(tokens[variableIndex]);
//                valuesRead++;
                if (!Double.isNaN(value)) {
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
//                    valuesStored++;
                } else {
                    log.debug("Ignored value: time instant: " + instant.toString() + " value: " + value);
                }
            }

            fileMetadata.timeSeriesMap.put(variableName, timeSeries);
            csvFileReader.close();
//            log.debug("valuesRead: " + valuesRead);
//            log.debug("valuesStored: " + valuesStored);
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

            TimeSeriesPanel overviewTSPanel = multiViewPanel.getOverviewTimeSeriesPanel(timeSeries);
            TimeSeriesPanel detailsTSPanel = multiViewPanel.getDetailTimeSeriesPanel(timeSeries);
            JScrollPane detailsTimeSeriesPanelScrollPanel = multiViewPanel.getDetailsTimeSeriesScrollPane(timeSeries);

            detailsTSPanel.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                @Override
                public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                    addSelectionView(timeSeriesPanel, overviewTSPanel, detailsTimeSeriesPanelScrollPanel, timeSeriesSelection);
                }

                @Override
                public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                           Instant previousStartInstant, Instant previousEndInstant) {
                    updateSelectionView(timeSeriesPanel, overviewTSPanel, timeSeriesSelection, previousStartInstant, previousEndInstant);
                }

                @Override
                public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                    deleteSelectionView(timeSeriesPanel, overviewTSPanel, timeSeriesSelection);
                }
            });
        } else if (fileMetadata.fileType == FileMetadata.FileType.PLG) {
            try {
                ArrayList<String> variableList = new ArrayList<>();
                variableList.add(variableName);
                Map<String, TimeSeries> PLGTimeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(fileMetadata.file, variableList);
                for (TimeSeries timeSeries : PLGTimeSeriesMap.values()) {
                    timeSeries.setName(fileMetadata.file.getName() + ":" + timeSeries.getName());
                    multiViewPanel.addTimeSeries(timeSeries, fileMetadata.file.getName());

                    TimeSeriesPanel overviewTSPanel = multiViewPanel.getOverviewTimeSeriesPanel(timeSeries);
                    TimeSeriesPanel detailsTimeSeries = multiViewPanel.getDetailTimeSeriesPanel(timeSeries);
                    JScrollPane detailsTimeSeriesPanelScrollPanel = multiViewPanel.getDetailsTimeSeriesScrollPane(timeSeries);

                    detailsTimeSeries.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                        @Override
                        public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            addSelectionView(timeSeriesPanel, overviewTSPanel, detailsTimeSeriesPanelScrollPanel, timeSeriesSelection);
                        }

                        @Override
                        public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                                   Instant previousStartInstant, Instant previousEndInstant) {
                            updateSelectionView(timeSeriesPanel, overviewTSPanel, timeSeriesSelection, previousStartInstant,
                                    previousEndInstant);
                        }

                        @Override
                        public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                            deleteSelectionView(timeSeriesPanel, overviewTSPanel, timeSeriesSelection);
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

        int initialBinCount = preferences.getInt("MULTI_VIEW_HISTOGRAM_BIN_SIZE", multiViewPanel.getBinCount());
        Spinner multipleViewHistogramBinSizeSpinner = new Spinner(2, 400, initialBinCount);
        multipleViewHistogramBinSizeSpinner.setEditable(true);
        multipleViewHistogramBinSizeSpinner.setTooltip(new Tooltip("Change Bin Count for Overview Histogram"));
        multipleViewHistogramBinSizeSpinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                multiViewPanel.setBinCount((Integer)newValue);
                preferences.putInt("MULTI_VIEW_HISTOGRAM_BIN_SIZE", (Integer)newValue);
            }
        });
        grid.add(new Label("Histogram Bin Count: "), 0, 0);
        grid.add(multipleViewHistogramBinSizeSpinner, 1, 0);

        Spinner plotHeightSpinner = new Spinner(40, 400, multiViewPanel.getPlotHeight());
        plotHeightSpinner.setEditable(true);
        plotHeightSpinner.setTooltip(new Tooltip("Change Height of Variable Panels"));
        plotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setPlotHeight((Integer)newValue));
        plotHeightSpinner.setPrefWidth(80.);
        grid.add(new Label("Variable Panel Height: "), 0, 1);
        grid.add(plotHeightSpinner, 1, 1);

        CheckBox showButtonsCheckBox = new CheckBox("Show Button Panel");
        showButtonsCheckBox.setTooltip(new Tooltip("Enable or Disable Side Button Panel"));
        showButtonsCheckBox.setSelected(multiViewPanel.getShowButtonPanelsEnabled());
        showButtonsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setShowButtonPanelsEnabled((Boolean)newValue));
        grid.add(showButtonsCheckBox, 0, 2, 2, 1);

        ScrollPane scrollPane = new ScrollPane(grid);
        TitledPane generalTitledPane = new TitledPane("General Display Settings", scrollPane);

        // Create Time Series Plot Settings Pane
        grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(4, 4, 4, 4));

        Spinner plotChronoUnitWidthSpinner = new Spinner(1, 400, multiViewPanel.getChronoUnitWidth());
        plotChronoUnitWidthSpinner.setEditable(true);
        plotChronoUnitWidthSpinner.setTooltip(new Tooltip("Change ChronoUnit Width in Detail Time Series Plot"));
        plotChronoUnitWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setChronoUnitWidth((Integer)newValue));
        grid.add(new Label("Plot Unit Width: "), 0, 0);
        grid.add(plotChronoUnitWidthSpinner, 1, 0);

        TimeSeriesPanel.PlotDisplayOption displayOption = TimeSeriesPanel.PlotDisplayOption.valueOf(preferences.get("DISPLAY_OPTION", multiViewPanel.getDetailTimeSeriesPlotDisplayOption().toString()));
        ChoiceBox<TimeSeriesPanel.PlotDisplayOption> plotDisplayOptionChoiceBox = new ChoiceBox<>();
        plotDisplayOptionChoiceBox.setTooltip(new Tooltip("Change Display Mode for Detail Time Series Plot"));
        plotDisplayOptionChoiceBox.getItems().addAll(TimeSeriesPanel.PlotDisplayOption.POINT, TimeSeriesPanel.PlotDisplayOption.LINE,
                TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE, TimeSeriesPanel.PlotDisplayOption.SPECTRUM);
        plotDisplayOptionChoiceBox.getSelectionModel().select(displayOption);
        plotDisplayOptionChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TimeSeriesPanel.PlotDisplayOption> ov,
                 TimeSeriesPanel.PlotDisplayOption oldValue, TimeSeriesPanel.PlotDisplayOption newValue) -> {
                    if (oldValue != newValue) {
                        multiViewPanel.setDetailTimeSeriesPlotDisplayOption(newValue);
                        preferences.put("DISPLAY_OPTION", newValue.toString());
                    }
                }
        );
        grid.add(new Label("Detail Plot Display Option: "), 0, 1);
        grid.add(plotDisplayOptionChoiceBox, 1, 1);

        ChoiceBox<ChronoUnit> chronoUnitChoice = new ChoiceBox<ChronoUnit>();
        chronoUnitChoice.setTooltip(new Tooltip("Change ChronoUnit for Detail Time Series Plot"));
        chronoUnitChoice.getItems().addAll(ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS, ChronoUnit.DAYS);
        chronoUnitChoice.getSelectionModel().select(multiViewPanel.getDetailChronoUnit());
        chronoUnitChoice.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends ChronoUnit> ov,
             ChronoUnit oldValue, ChronoUnit newValue) -> {
                if (oldValue != newValue) {
                    multiViewPanel.setDetailChronoUnit(newValue);
                }
            }
        );
        grid.add(new Label("Plot Chrono Unit: "), 0, 2);
        grid.add(chronoUnitChoice, 1, 2);

        ChoiceBox<TimeSeriesPanel.MovingRangeDisplayOption> movingRangeDisplayOptionChoiceBox = new ChoiceBox<>();
        movingRangeDisplayOptionChoiceBox.setTooltip(new Tooltip("Choose Moving Range Display Option"));
        movingRangeDisplayOptionChoiceBox.getItems().addAll(TimeSeriesPanel.MovingRangeDisplayOption.NOT_SHOWN, TimeSeriesPanel.MovingRangeDisplayOption.PLOT_VALUE, TimeSeriesPanel.MovingRangeDisplayOption.OPACITY);
        movingRangeDisplayOptionChoiceBox.getSelectionModel().select(multiViewPanel.getMovingRangeDisplayOption());
        movingRangeDisplayOptionChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends TimeSeriesPanel.MovingRangeDisplayOption> ov,
                 TimeSeriesPanel.MovingRangeDisplayOption oldValue, TimeSeriesPanel.MovingRangeDisplayOption newValue) -> {
                    if (oldValue != newValue) {
                        multiViewPanel.setMovingRangeDisplayOption(newValue);
                    }
                }
        );
        grid.add(new Label("Moving Range Display: "), 0, 3);
        grid.add(movingRangeDisplayOptionChoiceBox, 1, 3);

        ColorPicker pointColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesPointColor()));
        pointColorPicker.setTooltip(new Tooltip("Change Time Series Plot Point Color"));
        pointColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color dataColor = pointColorPicker.getValue();
                multiViewPanel.setTimeSeriesPointColor(GraphicsUtil.convertToAWTColor(dataColor));
            }
        });
        grid.add(new Label("Point Color: "), 0, 4);
        grid.add(pointColorPicker, 1, 4);

        ColorPicker lineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesLineColor()));
        lineColorPicker.setTooltip(new Tooltip("Change Time Series Plot Line Color"));
        lineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = lineColorPicker.getValue();
                multiViewPanel.setTimeSeriesLineColor(GraphicsUtil.convertToAWTColor(color));
            }
        });
        grid.add(new Label("Line Color: "), 0, 5);
        grid.add(lineColorPicker, 1, 5);

        ColorPicker stdevRangeLineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesStandardDeviationRangeColor()));
        stdevRangeLineColorPicker.setTooltip(new Tooltip("Change Time Series Standard Deviation Range Line Color"));
        stdevRangeLineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = stdevRangeLineColorPicker.getValue();
                multiViewPanel.setTimeSeriesStandardDeviationRangeColor(GraphicsUtil.convertToAWTColor(color));
            }
        });
        grid.add(new Label("Standard Deviation Range Color: "), 0, 6);
        grid.add(stdevRangeLineColorPicker, 1, 6);

        ColorPicker minmaxRangeLineColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesMinMaxRangeColor()));
        minmaxRangeLineColorPicker.setTooltip(new Tooltip("Change Time Series Min/Max Range Line Color"));
        minmaxRangeLineColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = minmaxRangeLineColorPicker.getValue();
                multiViewPanel.setTimeSeriesMinMaxRangeColor(GraphicsUtil.convertToAWTColor(color));
            }
        });
        grid.add(new Label("Min/Max Range Color: "), 0, 7);
        grid.add(minmaxRangeLineColorPicker, 1, 7);

        ColorPicker spectrumPositiveColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesSpectrumPositiveColor()));
        spectrumPositiveColorPicker.setTooltip(new Tooltip("Change Time Series Spectrum Positive Value Color"));
        spectrumPositiveColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = spectrumPositiveColorPicker.getValue();
                multiViewPanel.setTimeSeriesSpectrumPositiveColor(GraphicsUtil.convertToAWTColor(color));
            }
        });
        grid.add(new Label("Spectrum Positive Color: "), 0, 8);
        grid.add(spectrumPositiveColorPicker, 1, 8);

        ColorPicker spectrumNegativeColorPicker = new ColorPicker(GraphicsUtil.convertToJavaFXColor(multiViewPanel.getTimeSeriesSpectrumNegativeColor()));
        spectrumNegativeColorPicker.setTooltip(new Tooltip("Change Time Series Spectrum Negative Value Color"));
        spectrumNegativeColorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Color color = spectrumNegativeColorPicker.getValue();
                multiViewPanel.setTimeSeriesSpectrumNegativeColor(GraphicsUtil.convertToAWTColor(color));
            }
        });
        grid.add(new Label("Spectrum Negative Color: "), 0, 9);
        grid.add(spectrumNegativeColorPicker, 1, 9);

        CheckBox syncScrollbarsCheckBox = new CheckBox("Sync File TimeSeries Scrollbars");
        syncScrollbarsCheckBox.setTooltip(new Tooltip("Sync Scrollbars for all TimeSeries from the Same File"));
        syncScrollbarsCheckBox.setSelected(multiViewPanel.getSyncGroupScrollbarsEnabled());
        syncScrollbarsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> multiViewPanel.setSyncGroupScollbarsEnabled((Boolean)newValue));
        grid.add(syncScrollbarsCheckBox, 0, 10, 2, 1);

        scrollPane = new ScrollPane(grid);
        TitledPane timeSeriesTitledPane = new TitledPane("TimeSeries Display Settings", scrollPane);

        // Create Histogram Settings Pane
        grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new javafx.geometry.Insets(4, 4, 4, 4));

        grid.add(new Label("Plot Height: "), 0, 0);
        Spinner selectionPlotHeightSpinner = new Spinner(40, 400, selectionDetailPanel.getPlotHeight());
        selectionPlotHeightSpinner.setTooltip(new Tooltip("Change Selection Details Panel Height"));
        selectionPlotHeightSpinner.setEditable(true);
        selectionPlotHeightSpinner.valueProperty().addListener((obs, oldValue, newValue) -> selectionDetailPanel.setPlotHeight((Integer)newValue));
        grid.add(selectionPlotHeightSpinner, 1, 0);

        grid.add(new Label("Bin Count: "), 0, 1);
        Spinner selectionBinSizeSpinner = new Spinner(2, 400, selectionDetailPanel.getBinCount());
        selectionBinSizeSpinner.setEditable(true);
        selectionBinSizeSpinner.setTooltip(new Tooltip("Change Selection Details Bin Count"));
        selectionBinSizeSpinner.valueProperty().addListener((obs, oldValue, newValue) -> selectionDetailPanel.setBinCount((Integer)newValue));
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
        public TimeSeriesPanel detailTimeSeriesPanel;
        public JScrollPane detailsTimeSeriesPanelScrollPane;
        public TimeSeriesSelection selection;
    }
}
