package gov.ornl.crossvis;

import gov.ornl.datatable.*;
import gov.ornl.pcpview.PCPView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.prefs.Preferences;

public class PCPViewWindow extends Application implements DataTableListener {
    private PCPView pcpView;
    private DataTable dataTable;

    private ScrollPane scrollPane;
    private StatusBar statusBar;
    private DecimalFormat percentageFormat = new DecimalFormat("##0.0%");

    private MenuItem exportSelectedDataMenuItem = new MenuItem("Export Selected Data...");
    private MenuItem exportUnselectedDataMenuItem = new MenuItem("Export Unselected Data...");

    private Menu queryStatisticsMenu = new Menu("Statistics Display Options");
    private CheckMenuItem showSelectedDataStatisticsCheckMI = new CheckMenuItem("Show Summary Statistics for Selected Data");
    private CheckMenuItem showUnselectedDataStatisticsCheckMI = new CheckMenuItem("Show Summary Statistics for Unselected Data");

    private Menu polylineDisplayModeMenu = new Menu("TuplePolyline Display Mode");
    private ToggleGroup polylineDisplayModeMenuGroup = new ToggleGroup();
    private RadioMenuItem noPolylineModeMenuItem = new RadioMenuItem("Show Polylines");
    private RadioMenuItem binnedModeMenuItem = new RadioMenuItem("Show Binned Polylines");
    private RadioMenuItem polylineModeMenuItem = new RadioMenuItem("Hide Polylines");

    private CheckMenuItem showHistogramsCheckMenuItem = new CheckMenuItem("Show Histograms");

    private Menu axisLayoutMenu = new Menu("Axis Layout");
    private CheckMenuItem fitAxesToWidthCheckMenuItem = new CheckMenuItem("Fit Axis Spacing to Window Width");
    private MenuItem changeAxisSpacingMenuItem = new MenuItem("Change Axis Spacing...");

    private MenuItem removeSelectedDataMenuItem = new MenuItem("Remove Selected Data");
    private MenuItem removeUnselectedDataMenuItem = new MenuItem("Remove Unselected Data");

    private MenuItem removeAllQueriesMenuItem = new MenuItem("Remove All Axis Selections");
    private ProgressBar percentSelectedProgress;

    private Preferences preferences = Preferences.userNodeForPackage(this.getClass());

    public PCPViewWindow(DataTable dataTable) {
        this.dataTable = dataTable;
        dataTable.addDataModelListener(this);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        exportSelectedDataMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.META_DOWN));
        exportSelectedDataMenuItem.setOnAction(event -> {exportSelectedData();});

        exportUnselectedDataMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.U, KeyCombination.META_DOWN));
        exportUnselectedDataMenuItem.setOnAction(event -> { exportUnselectedData(); });

        removeSelectedDataMenuItem.setOnAction(event -> { removeSelectedData(); });
        removeUnselectedDataMenuItem.setOnAction(event -> { removeUnselectedData(); });

        MenuItem closeMenuItem = new MenuItem("Close PCPView Windows");
        closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        closeMenuItem.setOnAction(event -> { ((Stage)menuBar.getScene().getWindow()).close(); });
        
        showSelectedDataStatisticsCheckMI.setSelected(dataTable.getCalculateQueryStatistics());
        showSelectedDataStatisticsCheckMI.setDisable(dataTable == null || dataTable.isEmpty());
        showSelectedDataStatisticsCheckMI.selectedProperty().addListener(observable -> {
            dataTable.setCalculateQueryStatistics(showSelectedDataStatisticsCheckMI.isSelected());
        });

        showUnselectedDataStatisticsCheckMI.setSelected(dataTable.getCalculateQueryStatistics());
        showUnselectedDataStatisticsCheckMI.setDisable(dataTable == null || dataTable.isEmpty());
        showUnselectedDataStatisticsCheckMI.selectedProperty().addListener(observable -> {
            dataTable.setCalculateQueryStatistics(showUnselectedDataStatisticsCheckMI.isSelected());
        });

        queryStatisticsMenu.getItems().addAll(showSelectedDataStatisticsCheckMI, showUnselectedDataStatisticsCheckMI);

        noPolylineModeMenuItem.setToggleGroup(polylineDisplayModeMenuGroup);

        binnedModeMenuItem.setToggleGroup(polylineDisplayModeMenuGroup);

        polylineModeMenuItem.setToggleGroup(polylineDisplayModeMenuGroup);

        polylineDisplayModeMenuGroup.selectedToggleProperty().addListener(observable -> {
            // update polyline display mode in pcpview
        });

        polylineDisplayModeMenu.setDisable(dataTable == null || dataTable.isEmpty());
        polylineDisplayModeMenu.getItems().addAll(polylineModeMenuItem, binnedModeMenuItem, noPolylineModeMenuItem);

        showHistogramsCheckMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN));
        showHistogramsCheckMenuItem.setDisable(dataTable == null || dataTable.isEmpty());
        showHistogramsCheckMenuItem.setOnAction(event -> {
            // turn on histograms
        });

        fitAxesToWidthCheckMenuItem.setSelected(pcpView.getFitToWidth());
        fitAxesToWidthCheckMenuItem.setDisable(dataTable == null || dataTable.isEmpty());
        fitAxesToWidthCheckMenuItem.selectedProperty().addListener(observable -> {
            pcpView.setFitToWidth(fitAxesToWidthCheckMenuItem.isSelected());
            if (pcpView.getFitToWidth()) {
                changeAxisSpacingMenuItem.setDisable(true);
                scrollPane.setFitToWidth(true);
            } else {
                changeAxisSpacingMenuItem.setDisable(false);
                scrollPane.setFitToWidth(false);
            }
        });

        changeAxisSpacingMenuItem.setDisable(dataTable == null || dataTable.isEmpty());
        changeAxisSpacingMenuItem.setOnAction(event -> {
            changeAxisSpacing();
        });

        axisLayoutMenu.getItems().addAll(fitAxesToWidthCheckMenuItem, changeAxisSpacingMenuItem);

        removeAllQueriesMenuItem.setDisable(dataTable == null || dataTable.isEmpty() || !dataTable.getActiveQuery().hasColumnSelections());
        removeAllQueriesMenuItem.setOnAction(event -> {
            pcpView.clearQuery();
        });

        Menu PCPViewMenu = new Menu ("PCPView");
        PCPViewMenu.getItems().add(closeMenuItem);

        Menu dataMenu = new Menu("Data");
        dataMenu.getItems().addAll(exportSelectedDataMenuItem, exportUnselectedDataMenuItem, new SeparatorMenuItem(),
                removeSelectedDataMenuItem, removeUnselectedDataMenuItem);
        Menu viewMenu = new Menu("View");
        viewMenu.getItems().addAll(polylineDisplayModeMenu, queryStatisticsMenu, axisLayoutMenu,
                showHistogramsCheckMenuItem, removeAllQueriesMenuItem);

        menuBar.getMenus().addAll(PCPViewMenu, dataMenu, viewMenu);

        return menuBar;
    }

    private void changeAxisSpacing() {
        Dialog<Integer> axisSpacingDialog = new Dialog<>();
        axisSpacingDialog.setTitle("Change Axis Spacing");
        axisSpacingDialog.setHeaderText("Change the spacing between parallel coordinate crossvis");

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

        result.ifPresent(axisSpacing -> { pcpView.setAxisSpacing(axisSpacing); });
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
                IOUtilities.exportSelectedFromDataTableQueryToCSV(csvFile, dataTable);
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
                IOUtilities.exportUnselectedFromDataTableToCSV(csvFile, dataTable);
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        pcpView = new PCPView();
        pcpView.setPrefHeight(500);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));
        pcpView.setPolylineDisplayMode(PCPView.POLYLINE_DISPLAY_MODE.POLYLINES);

        scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToWidth(pcpView.getFitToWidth());
        scrollPane.setFitToHeight(true);

        MenuBar menuBar = createMenuBar();
        statusBar = createStatusBar();
        updatePercentSelectedProgress();

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);
        rootNode.setTop(menuBar);
        rootNode.setBottom(statusBar);

        Scene scene = new Scene(rootNode, 960, 500, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("M u s t a n g | Parallel Coordinates View");
        primaryStage.setScene(scene);
        primaryStage.show();

        pcpView.setDataTable(dataTable);
    }

    private StatusBar createStatusBar() {
        StatusBar statusBar = new StatusBar();

        percentSelectedProgress = new ProgressBar();
        percentSelectedProgress.setProgress(0.0);
        percentSelectedProgress.setTooltip(new Tooltip("Percent of Lines Selected"));

        statusBar.getLeftItems().add(percentSelectedProgress);

        return statusBar;
    }

    private void updatePercentSelectedProgress() {
        if (dataTable != null && !dataTable.isEmpty()) {
            double percentSelected = (double)dataTable.getActiveQuery().getQueriedTupleCount() / dataTable.getTupleCount();
            percentSelectedProgress.setProgress(percentSelected);
            statusBar.setText(" " + dataTable.getActiveQuery().getQueriedTupleCount() + " of " +
                    dataTable.getTupleCount() + " tuples selected (" + percentageFormat.format(percentSelected) + ")");
        }
    }

    @Override
    public void dataModelReset(DataTable dataModel) {
        removeAllQueriesMenuItem.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
    }

    @Override
    public void dataModelStatisticsChanged(DataTable dataModel) {

    }

    @Override
    public void dataModelNumHistogramBinsChanged(DataTable dataModel) {

    }

    @Override
    public void dataTableAllColumnSelectionsRemoved(DataTable dataModel) {
        removeAllQueriesMenuItem.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
    }

    @Override
    public void dataTableAllColumnSelectionsForColumnRemoved(DataTable dataModel, Column column) {

    }

    @Override
    public void dataModelColumnSelectionAdded(DataTable dataModel, ColumnSelection columnSelectionRange) {
        removeAllQueriesMenuItem.setDisable(false);
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataTable dataModel, ColumnSelection columnSelectionRange) {
        removeAllQueriesMenuItem.setDisable(!dataTable.getActiveQuery().hasColumnSelections());
    }

    @Override
    public void dataModelColumnSelectionChanged(DataTable dataModel, ColumnSelection columnSelectionRange) {

    }

    @Override
    public void dataModelHighlightedColumnChanged(DataTable dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {

    }

    @Override
    public void dataModelTuplesAdded(DataTable dataModel, ArrayList<Tuple> newTuples) {

    }

    @Override
    public void dataModelTuplesRemoved(DataTable dataModel, int numTuplesRemoved) {

    }

    @Override
    public void dataModelColumnDisabled(DataTable dataModel, Column disabledColumn) {

    }

    @Override
    public void dataModelColumnsDisabled(DataTable dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataTable dataModel, Column enabledColumn) {

    }

    @Override
    public void dataTableBivariateColumnAdded(DataTable dataTable, BivariateColumn bivariateColumn) {

    }

    @Override
    public void dataModelColumnOrderChanged(DataTable dataModel) {

    }

    @Override
    public void dataModelColumnNameChanged(DataTable dataModel, Column column) {

    }
}
