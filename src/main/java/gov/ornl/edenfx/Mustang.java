package gov.ornl.edenfx;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import gov.ornl.pcpview.PCPViewTest;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Mustang extends Application {
    private static final Logger log = Logger.getLogger(Mustang.class.getName());

    private Preferences preferences;
    private DataTable dataTable;

    ListView<Column> columnListView;

    MenuItem openPCPViewMenuItem;
    MenuItem openCorrelationViewMenuItem;
    MenuItem openScatterplotViewMenuItem;
    MenuItem openSPLOMViewMenuItem;
    MenuItem openDataTableViewMenuItem;

    ArrayList<Application> openViewWindows = new ArrayList<>();

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
        dataTable = new DataTable();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MenuBar menuBar = createMenuBar(primaryStage);

        columnListView = new ListView<>();
        columnListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        columnListView.setCellFactory(new Callback<ListView<Column>, ListCell<Column>>() {
            @Override
            public ListCell<Column> call(ListView<Column> param) {
                ColumnListCell columnListCell = new ColumnListCell();
                return columnListCell;
            }
        });

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(columnListView);
        rootNode.setTop(menuBar);

        Scene scene = new Scene(rootNode, 400, 600, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("M u s t a n g");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() { System.exit(0); }

    public static void main (String args[]) { launch(args); }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu mustangMenu = new Menu("Mustang");
        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");
        menuBar.getMenus().addAll(mustangMenu, fileMenu, viewMenu);

        MenuItem openCSVMI = new MenuItem("Open CSV...");
        openCSVMI.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCSVMI.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            String lastCSVDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_READ_DIRECTORY, "");
            if (!lastCSVDirectoryPath.isEmpty()) {
                File lastCSVDirectory = new File(lastCSVDirectoryPath);
                if (lastCSVDirectory != null && lastCSVDirectory.exists() && lastCSVDirectory.canRead()) {
                    fileChooser.setInitialDirectory(lastCSVDirectory);
                }
            }

            fileChooser.setTitle("Open CSV File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*"),
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            File csvFile = fileChooser.showOpenDialog(stage);
            if (csvFile != null) {
                try {
                    openCSVFile(csvFile);
                    preferences.put(EDENFXPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        fileMenu.getItems().add(openCSVMI);

        openPCPViewMenuItem = new MenuItem("Open Parallel Coordinate View");
        openPCPViewMenuItem.setDisable(true);
        openPCPViewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.META_DOWN));
        openPCPViewMenuItem.setOnAction(event -> {
            try {
                openPCPView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        viewMenu.getItems().add(openPCPViewMenuItem);
        
        openCorrelationViewMenuItem = new MenuItem("Open Correlation Matrix View");
        openCorrelationViewMenuItem.setDisable(true);
        openCorrelationViewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCorrelationViewMenuItem.setOnAction(event -> {
            try {
                openCorrelationView();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        viewMenu.getItems().add(openCorrelationViewMenuItem);

        openScatterplotViewMenuItem = new MenuItem("Open Scatterplot View");
        openScatterplotViewMenuItem.setDisable(true);
        openScatterplotViewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        openScatterplotViewMenuItem.setOnAction(event -> {
            try {
                openScatterplotView();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        viewMenu.getItems().add(openScatterplotViewMenuItem);

        openSPLOMViewMenuItem = new MenuItem("Open SPLOM View");
        openSPLOMViewMenuItem.setDisable(true);
        openSPLOMViewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.META_DOWN));
        openSPLOMViewMenuItem.setOnAction(event -> {
            try {
                openSPLOMView();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        viewMenu.getItems().add(openSPLOMViewMenuItem);

        openDataTableViewMenuItem = new MenuItem("Open Data Table View");
        openDataTableViewMenuItem.setDisable(true);
        openDataTableViewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.META_DOWN));
        openDataTableViewMenuItem.setOnAction(event -> {
            try {
                openDataTableView();
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
        viewMenu.getItems().add(openDataTableViewMenuItem);

        MenuItem quitMI = new MenuItem("Quit Mustang");
        quitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        quitMI.setOnAction(event -> { stage.close(); });
        mustangMenu.getItems().add(quitMI);

        return menuBar;
    }

    private void openPCPView() throws Exception {
        PCPViewWindow pcpViewWindow = new PCPViewWindow(dataTable.getDuplicate());
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> {
            openViewWindows.remove(pcpViewWindow);
        });
        pcpViewWindow.start(stage);
        openViewWindows.add(pcpViewWindow);
        log.info("There are " + openViewWindows.size() + " open view windows");
    }

    private void openCorrelationView() throws Exception {
        CorrelationViewWindow correlationViewWindow = new CorrelationViewWindow(dataTable.getDuplicate());
        Stage stage = new Stage();
        stage.setOnCloseRequest(event -> {
            openViewWindows.remove(correlationViewWindow);
        });
        correlationViewWindow.start(stage);
        openViewWindows.add(correlationViewWindow);
    }

    private void openScatterplotView() throws Exception {

    }

    private void openSPLOMView() throws Exception {

    }

    private void openDataTableView() throws Exception {

    }

    private void openCSVFile(File f) throws IOException {
        ArrayList<DataTableColumnSpecification> columnSpecifications = DataTableColumnSpecificationDialog.getColumnSpecifications(f);

        if (columnSpecifications == null) {
            return;
        }

        dataTable.clear();

        ArrayList<String> temporalColumnNames = new ArrayList<>();
        ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
        ArrayList<String> ignoreColumnNames = new ArrayList<>();
        ArrayList<String> categoricalColumnNames = new ArrayList<>();

        String lastDateTimeParsePattern = null;
        for (DataTableColumnSpecification columnSpecification : columnSpecifications) {
            if (columnSpecification.getIgnore()) {
                ignoreColumnNames.add(columnSpecification.getName());
            } else if (columnSpecification.getType().equalsIgnoreCase("Temporal")) {
                temporalColumnNames.add(columnSpecification.getName());
                temporalColumnFormatters.add(columnSpecification.getDateTimeFormatter());
//                temporalColumnFormatters.add(DateTimeFormatter.ofPattern(columnSpecification.getParsePattern()));
//                lastDateTimeParsePattern = columnSpecification.getParsePattern();
            } else if (columnSpecification.getType().equalsIgnoreCase("Categorical")) {
                categoricalColumnNames.add(columnSpecification.getName());
            }
        }

        IOUtilities.readCSV(f, ignoreColumnNames, categoricalColumnNames, temporalColumnNames, temporalColumnFormatters, dataTable);

        columnListView.getItems().clear();
        for (int icolumn = 0; icolumn < dataTable.getColumnCount(); icolumn++) {
            Column column = dataTable.getColumn(icolumn);
            columnListView.getItems().add(column);
        }

        openPCPViewMenuItem.setDisable(false);
        openCorrelationViewMenuItem.setDisable(false);
        openScatterplotViewMenuItem.setDisable(false);
        openSPLOMViewMenuItem.setDisable(false);
        openDataTableViewMenuItem.setDisable(false);
    }
}
