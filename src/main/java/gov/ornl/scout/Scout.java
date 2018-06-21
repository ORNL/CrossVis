package gov.ornl.scout;

import gov.ornl.table.Column;
import gov.ornl.table.Table;
import gov.ornl.table.TemporalColumn;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

public class Scout extends Application {

    private Preferences preferences;
    private Table table;
    private ListView<Column> histogramListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        MenuBar menuBar = createMenuBar(primaryStage);

        VBox topBox = new VBox();
        topBox.getChildren().add(menuBar);

        histogramListView = new ListView<>();
        histogramListView.setCellFactory(param -> new HistogramCell());
        histogramListView.setPrefWidth(400);
        histogramListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        histogramListView.setStyle("-fx-selection-bar:#e2ecfe;");

        BorderPane rootNode = new BorderPane();
        rootNode.setTop(topBox);
        rootNode.setCenter(histogramListView);

        Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(rootNode, 400, screenVisualBounds.getHeight() * .9, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("S C O U T");
        primaryStage.setScene(scene);
        primaryStage.setX(20);
//        primaryStage.setY(/);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu scoutMenu = new Menu("Scout");
        Menu fileMenu = new Menu("File");
        menuBar.getMenus().addAll(scoutMenu, fileMenu);

        MenuItem openCSVMenuItem = new MenuItem("Open CSV...");
        openCSVMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCSVMenuItem.setOnAction(event -> openCSVFile(stage));

        MenuItem exitMenuItem = new MenuItem("Quit Scout");
        exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMenuItem.setOnAction(event -> stage.close());

        scoutMenu.getItems().addAll(exitMenuItem);
        fileMenu.getItems().addAll(openCSVMenuItem);

        return menuBar;
    }

    private void openCSVFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        String lastCSVDirectoryPath = preferences.get(ScoutPreferenceKeys.LAST_CSV_READ_DIRECTORY, "");
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

        File csvFile = fileChooser.showOpenDialog(stage);
        if (csvFile != null) {
            try {
                ArrayList<TableColumnSpecification> columnSpecifications = TableColumnSpecificationDialog.getColumnSpecifications(csvFile);

                if (columnSpecifications != null) {
                    Table newTable = new Table();

                    HashMap<Column, DateTimeFormatter> timeColumnFormatterMap = new HashMap<>();
                    for (TableColumnSpecification columnSpecification : columnSpecifications) {
                        if (columnSpecification.getType().equals("Temporal")) {
                            TemporalColumn column = newTable.addTemporalColumn(columnSpecification.getName());
                            timeColumnFormatterMap.put(column, DateTimeFormatter.ofPattern(columnSpecification.getParsePattern()));
                        } else if (columnSpecification.getType().equals("Double")) {
                            newTable.addDoubleColumn(columnSpecification.getName());
                        } else if (columnSpecification.getType().equals("Categorical")) {
                            newTable.addCategoricalColumn(columnSpecification.getName());
                        }
                    }

                    newTable.readRowsFromFile(csvFile, true, timeColumnFormatterMap);
                    setTable(newTable);
                    preferences.put(ScoutPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTable(Table table) {
        this.table = table;
        for (int i = 0; i < table.getColumnCount(); i++) {
            Column column = table.getColumn(i);
            histogramListView.getItems().add(column);
        }
    }
}
