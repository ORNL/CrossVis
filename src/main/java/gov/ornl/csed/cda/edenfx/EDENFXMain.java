package gov.ornl.csed.cda.edenfx;

import gov.ornl.csed.cda.Falcon.FalconPreferenceKeys;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.IOUtilities;
import gov.ornl.csed.cda.pcpview.PCPView;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by csg on 8/29/16.
 */
public class EDENFXMain extends Application {
    private static final Logger log = LoggerFactory.getLogger(EDENFXMain.class);

    private PCPView pcpView;
    private DataModel dataModel;

    private HashMap<String, PCPView.DISPLAY_MODE> displayModeMap;
    private Preferences preferences;
    private Menu displayModeMenu;

    private ChoiceBox<String> displayModeChoiceBox;
    private Spinner axisSpacingSpinner;
    private CheckBox fitAxesToWidthCheckBox;
    private ScrollPane pcpScrollPane;
    private TabPane tabPane;
    private Menu axisLayoutMenu;
    private CheckMenuItem fitPCPAxesToWidthCheckMI;
    private MenuItem changeAxisSpacingMI;

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());

        displayModeMap = new HashMap<>();
        displayModeMap.put("Histograms", PCPView.DISPLAY_MODE.HISTOGRAM);
        displayModeMap.put("Parallel Coordinates Bins", PCPView.DISPLAY_MODE.PCP_BINS);
        displayModeMap.put("Parallel Coordinates Lines", PCPView.DISPLAY_MODE.PCP_LINES);


        dataModel = new DataModel();
    }

    @Override
    public void start(Stage stage) throws Exception {
        pcpView = new PCPView();
        pcpView.setDataModel(dataModel);
        pcpView.setPrefHeight(400);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpScrollPane = new ScrollPane(pcpView);
        pcpScrollPane.setFitToHeight(true);
        pcpScrollPane.setFitToWidth(pcpView.getFitAxisSpacingToWidthEnabled());

        MenuBar menuBar = createMenuBar(stage);
        menuBar.setUseSystemMenuBar(true);

        tabPane = new TabPane();
        Tab columnTableTab = new Tab(" Column Table ");
        columnTableTab.setClosable(false);
        Tab dataTableTab = new Tab(" Data Table ");
        dataTableTab.setClosable(false);
        tabPane.getTabs().addAll(columnTableTab, dataTableTab);

        SplitPane middleSplit = new SplitPane();
        middleSplit.setOrientation(Orientation.VERTICAL);
        middleSplit.getItems().addAll(pcpScrollPane, tabPane);
        middleSplit.setResizableWithParent(tabPane, false);
        middleSplit.setDividerPositions(0.7);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(middleSplit);
        rootNode.setTop(menuBar);
//        rootNode.setLeft(settingsPane);

        Scene scene = new Scene(rootNode, 1000, 500, true, SceneAntialiasing.BALANCED);

        stage.setTitle("EDEN.FX Alpha Version");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String args[]) {
        launch(args);
    }

    private Node createSideSettingsPane() {
        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setPadding(new Insets(4));

        // Display mode Choice
        displayModeChoiceBox = new ChoiceBox<>();
        displayModeChoiceBox.setTooltip(new Tooltip("Change the display mode for the parallel coordinates plot"));
        displayModeChoiceBox.getItems().addAll("Histogram", "Parallel Coordinates Bins", "Parallel Coordinates Lines");
        PCPView.DISPLAY_MODE displayMode = pcpView.getDisplayMode();
        if (displayMode == PCPView.DISPLAY_MODE.HISTOGRAM) {
            displayModeChoiceBox.getSelectionModel().select("Histogram");
        } else if (displayMode == PCPView.DISPLAY_MODE.PCP_BINS) {
            displayModeChoiceBox.getSelectionModel().select("Parallel Coordinates Bins");
        } else {
            displayModeChoiceBox.getSelectionModel().select("Parallel Coordinates Lines");
        }
        displayModeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (oldValue != newValue) {
                PCPView.DISPLAY_MODE newDisplayMode = displayModeMap.get((String)newValue);
                pcpView.setDisplayMode(newDisplayMode);
            }
        });
        grid.add(new Label("Display Mode: "), 0, 0);
        grid.add(displayModeChoiceBox, 1, 0);

        // Fix Axes to Width Check
        fitAxesToWidthCheckBox = new CheckBox("Fit Axes to Width");
        fitAxesToWidthCheckBox.setTooltip(new Tooltip("Determine the axis spacing using the current width of the display"));
        fitAxesToWidthCheckBox.setSelected(pcpView.getFitAxisSpacingToWidthEnabled());
        fitAxesToWidthCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (fitAxesToWidthCheckBox.isSelected()) {
                axisSpacingSpinner.setDisable(true);
                pcpView.setFitAxisSpacingToWidthEnabled(true);
                pcpScrollPane.setFitToWidth(true);
            } else {
                axisSpacingSpinner.setDisable(false);
                axisSpacingSpinner.getValueFactory().setValue(pcpView.getAxisSpacing());
                pcpView.setFitAxisSpacingToWidthEnabled(false);
                pcpScrollPane.setFitToWidth(false);
            }
        });
        grid.add(fitAxesToWidthCheckBox, 0, 1, 2, 1);

        // Axis spacing Spinner
        axisSpacingSpinner = new Spinner(10, 300, pcpView.getAxisSpacing());
        axisSpacingSpinner.setEditable(true);
        axisSpacingSpinner.setTooltip(new Tooltip("Change the spacing between axes in parallel coordinates plot"));
        axisSpacingSpinner.setDisable(pcpView.getFitAxisSpacingToWidthEnabled());
        axisSpacingSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
           pcpView.setAxisSpacing((Integer)newValue);
        });
        grid.add(new Label("Axis Spacing: "), 0, 2);
        grid.add(axisSpacingSpinner, 1, 2);

        TitledPane generalSettingsTitledPane = new TitledPane("General Display Settings", new ScrollPane(grid));

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(generalSettingsTitledPane);
        accordion.setExpandedPane(generalSettingsTitledPane);

        return accordion;
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
                String lastCSVDirectoryPath = preferences.get(EDENFXPreferenceKeys.LAST_CSV_EXPORT_DIRECTORY, "");
                if (!lastCSVDirectoryPath.isEmpty()) {
                    fileChooser.setInitialDirectory(new File(lastCSVDirectoryPath));
                }
                fileChooser.setTitle("Open CSV File");
                File csvFile = fileChooser.showOpenDialog(stage);
                if (csvFile != null) {
                    try {
                        openCSVFile(csvFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        displayModeMenu = new Menu("Display Mode");
        displayModeMenu.setDisable(true);
        final ToggleGroup displayModeGroup = new ToggleGroup();
        RadioMenuItem item = new RadioMenuItem("Histograms");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.HISTOGRAM) {
            item.setSelected(true);
        }
        item = new RadioMenuItem("Parallel Coordinates Bins");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_BINS) {
            item.setSelected(true);
        }
        item = new RadioMenuItem("Parallel Coordinates Lines");
        item.setToggleGroup(displayModeGroup);
        displayModeMenu.getItems().add(item);
        if (pcpView.getDisplayMode() == PCPView.DISPLAY_MODE.PCP_LINES) {
            item.setSelected(true);
        }
        viewMenu.getItems().add(displayModeMenu);

        axisLayoutMenu = new Menu("Axis Layout");
        fitPCPAxesToWidthCheckMI = new CheckMenuItem("Fit Axis Spacing to Width");
        fitPCPAxesToWidthCheckMI.setSelected(pcpView.getFitAxisSpacingToWidthEnabled());
        fitPCPAxesToWidthCheckMI.setDisable(true);
        fitPCPAxesToWidthCheckMI.selectedProperty().addListener((observable, oldValue, newValue) -> {
           pcpView.setFitAxisSpacingToWidthEnabled(fitPCPAxesToWidthCheckMI.isSelected());
        });

        changeAxisSpacingMI = new MenuItem("Change Axis Spacing...");
        changeAxisSpacingMI.setDisable(true);
        changeAxisSpacingMI.setOnAction(event -> {
            // TODO: Show dialog with spinner and slider for changing the axis spacing
        });

        axisLayoutMenu.getItems().addAll(fitPCPAxesToWidthCheckMI);
        viewMenu.getItems().add(axisLayoutMenu);

        displayModeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue != null) {
                    RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                    PCPView.DISPLAY_MODE newDisplayMode = displayModeMap.get(toggleItem.getText());
                    pcpView.setDisplayMode(newDisplayMode);
                }
            }
        });


        MenuItem exitMI = new MenuItem("Quit Falcon");
        exitMI.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMI.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                stage.close();
            }
        });

        edenfxMenu.getItems().addAll(exitMI);
        fileMenu.getItems().addAll(openCSVMI);

        return menuBar;
    }

    private void openCSVFile(File f) throws IOException {
        if (dataModel != null) {
            // TODO: Clear the data model from the PCPView
            dataModel.clear();
        }

        IOUtilities.readCSV(f, dataModel);
        displayModeMenu.setDisable(false);
    }

}
