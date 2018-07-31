package gov.ornl.scout;

import com.apple.laf.AquaButtonBorder;
import gov.ornl.scout.dataframeview.DataFrameView;
import gov.ornl.experiments.scout.ScoutPreferenceKeys;
import gov.ornl.experiments.scout.TableColumnSpecification;
import gov.ornl.experiments.scout.TableColumnSpecificationDialog;
import gov.ornl.scout.dataframe.Column;
import gov.ornl.scout.dataframe.DataFrame;
import gov.ornl.scout.dataframe.TemporalColumn;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

public class ScoutApp extends Application {

    private Preferences preferences;
    private DataFrame dataFrame;
    private DataFrameView dataTableView;
    private ScrollPane dataTableViewScrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        dataTableView = new DataFrameView(Orientation.HORIZONTAL);
        dataTableView.setPrefHeight(400);
        dataTableView.setAxisSpacing(100);
        dataTableView.setPadding(new Insets(10));

        dataTableViewScrollPane = new ScrollPane(dataTableView);
        dataTableViewScrollPane.setFitToHeight(true);
        dataTableViewScrollPane.setFitToWidth(dataTableView.getFitToWidth());

        MenuBar menuBar = createMenuBar(primaryStage);

        ToolBar toolBar = createToolBar(primaryStage);

        VBox topBox = new VBox();
        topBox.getChildren().add(menuBar);
        topBox.getChildren().add(toolBar);

        BorderPane rootNode = new BorderPane();
        rootNode.setTop(topBox);
        rootNode.setCenter(dataTableViewScrollPane);

        Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(rootNode, screenVisualBounds.getWidth()*.7, 500, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("S C O U T");
        primaryStage.setScene(scene);
//        primaryStage.setX(20);
        primaryStage.show();

//        openCarsData();
//        openTitanPerformanceData();
        openCarsCategoricalData();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    @Override
    public void init() {
        preferences = Preferences.userNodeForPackage(this.getClass());
    }

    private ToolBar createToolBar(Stage stage) {
        javafx.scene.control.ToolBar toolbar = new ToolBar();

        ToggleButton showUnselectedButton = new ToggleButton("Unselected");
        showUnselectedButton.setTooltip(new Tooltip("Show Unselected Data"));
        showUnselectedButton.selectedProperty().bindBidirectional(dataTableView.showUnselectedItemsProperty());

        ToggleButton showSelectedButton = new ToggleButton("Selected");
        showSelectedButton.setTooltip(new Tooltip("Show Selected Data"));
        showSelectedButton.selectedProperty().bindBidirectional(dataTableView.showSelectedItemsProperty());

        // create selected items color modification UI components
        HBox selectedItemsColorBox = new HBox();
        selectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker selectedItemsColorPicker = new ColorPicker();
        selectedItemsColorPicker.valueProperty().bindBidirectional(dataTableView.selectedItemsColorProperty());
        selectedItemsColorPicker.getStyleClass().add("button");
//        selectedItemsColorPicker.setOnAction(event -> {
//            Color c = new Color(selectedItemsColorPicker.getValue().getRed(),
//                    selectedItemsColorPicker.getValue().getGreen(),
//                    selectedItemsColorPicker.getValue().getBlue(),
//                    pcpView.getOpacity());
//            selectedItemsColorPicker.setValue(c);
//        });

        selectedItemsColorBox.getChildren().addAll(new Label(" Selected Items: "), selectedItemsColorPicker);

        // create unselected items color modification UI components
        HBox unselectedItemsColorBox = new HBox();
        unselectedItemsColorBox.setAlignment(Pos.CENTER);

        ColorPicker unselectedItemsColorPicker = new ColorPicker();
        unselectedItemsColorPicker.getStyleClass().add("button");
        unselectedItemsColorPicker.valueProperty().bindBidirectional(dataTableView.unselectedItemsColorProperty());
        unselectedItemsColorBox.getChildren().addAll(new Label(" Unselected Items: "), unselectedItemsColorPicker);

        // create opacity slider
        HBox opacityBox = new HBox();
        opacityBox.setAlignment(Pos.CENTER);

        Slider opacitySlider = new Slider(0., 1., dataTableView.getPolylineOpacity());
        opacitySlider.valueProperty().bindBidirectional(dataTableView.polylineOpacityProperty());
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);

        opacityBox.getChildren().addAll(new Label(" Opacity: "), opacitySlider);

        toolbar.getItems().addAll(showSelectedButton, selectedItemsColorBox, new Separator(), showUnselectedButton,
                unselectedItemsColorBox, new Separator(), opacityBox);
        return toolbar;
    };

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        Menu scoutMenu = new Menu("Scout");
        Menu fileMenu = new Menu("File");
        Menu viewMenu = new Menu("View");
        menuBar.getMenus().addAll(scoutMenu, fileMenu, viewMenu);

        MenuItem openCSVMenuItem = new MenuItem("Open CSV...");
        openCSVMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        openCSVMenuItem.setOnAction(event -> openCarsCategoricalData());
//        openCSVMenuItem.setOnAction(event -> openCSVFile(stage));
//        openCSVMenuItem.setOnAction(event -> openCarsData());

        MenuItem exitMenuItem = new MenuItem("Quit Scout");
        exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
        exitMenuItem.setOnAction(event -> stage.close());

        Menu orientationMenu = new Menu("Orientation");
        ToggleGroup orientationMenuGroup = new ToggleGroup();

        RadioMenuItem horizontalOrientationMenuItem = new RadioMenuItem("Horizontal");
        horizontalOrientationMenuItem.setToggleGroup(orientationMenuGroup);
        if (dataTableView.getOrientation() == Orientation.HORIZONTAL) {
            horizontalOrientationMenuItem.setSelected(true);
        }

        RadioMenuItem verticalOrientationMenuItem = new RadioMenuItem("Vertical");
        verticalOrientationMenuItem.setToggleGroup(orientationMenuGroup);
        if (dataTableView.getOrientation() == Orientation.VERTICAL) {
            verticalOrientationMenuItem.setSelected(true);
        }

        orientationMenu.getItems().addAll(horizontalOrientationMenuItem, verticalOrientationMenuItem);

        orientationMenuGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioMenuItem toggleItem = (RadioMenuItem)newValue;
                if (toggleItem.getText().equals("Horizontal")) {
                    dataTableView.setOrientation(Orientation.HORIZONTAL);
                } else {
                    dataTableView.setOrientation(Orientation.VERTICAL);
                }
            }
        });

        scoutMenu.getItems().addAll(exitMenuItem);
        fileMenu.getItems().addAll(openCSVMenuItem);
        viewMenu.getItems().add(orientationMenu);
        return menuBar;
    }

    private void openCarsData() {
        try {
            DataFrame newDataFrame = new DataFrame();
            String columnNames[] = DataFrame.getFileHeader(new File("data/csv/cars.csv"));
            for (String columnName : columnNames) {
                newDataFrame.addDoubleColumn(columnName);
            }
            newDataFrame.readRowsFromFile(new File("data/csv/cars.csv"), true, null);
            setDataFrame(newDataFrame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openCarsCategoricalData() {
        try {
            DataFrame newDataFrame = new DataFrame();
            String columnNames[] = DataFrame.getFileHeader(new File("data/csv/cars-cat.csv"));
            for (String columnName : columnNames) {
                if (columnName.equals("Origin")) {
                    newDataFrame.addCategoricalColumn(columnName);
                } else {
                    newDataFrame.addDoubleColumn(columnName);
                }
            }
            newDataFrame.readRowsFromFile(new File("data/csv/cars-cat.csv"), true, null);
            setDataFrame(newDataFrame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openTitanPerformanceData() {
        try {
            HashMap<Column, DateTimeFormatter> timeFormatterMap = new HashMap<>();
            DataFrame newDataFrame = new DataFrame();
            String columnNames[] = DataFrame.getFileHeader(new File("data/csv/titan-performance.csv"));
            for (String columnName : columnNames) {
                if (columnName.equals("Date")) {
                    TemporalColumn column = newDataFrame.addTemporalColumn(columnName);
                    timeFormatterMap.put(column, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                } else {
                    newDataFrame.addDoubleColumn(columnName);
                }
            }

            newDataFrame.readRowsFromFile(new File("data/csv/titan-performance.csv"), true, timeFormatterMap);
            setDataFrame(newDataFrame);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                    DataFrame newDataFrame = new DataFrame();

                    HashMap<Column, DateTimeFormatter> timeColumnFormatterMap = new HashMap<>();
                    for (TableColumnSpecification columnSpecification : columnSpecifications) {
                        if (columnSpecification.getType().equals("Temporal")) {
                            TemporalColumn column = newDataFrame.addTemporalColumn(columnSpecification.getName());
                            timeColumnFormatterMap.put(column, DateTimeFormatter.ofPattern(columnSpecification.getParsePattern()));
                        } else if (columnSpecification.getType().equals("Double")) {
                            newDataFrame.addDoubleColumn(columnSpecification.getName());
                        } else if (columnSpecification.getType().equals("Categorical")) {
                            newDataFrame.addCategoricalColumn(columnSpecification.getName());
                        }
                    }

                    newDataFrame.readRowsFromFile(csvFile, true, timeColumnFormatterMap);
                    setDataFrame(newDataFrame);
                    preferences.put(ScoutPreferenceKeys.LAST_CSV_READ_DIRECTORY, csvFile.getParentFile().getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDataFrame(DataFrame df) {
        this.dataFrame = df;
        dataTableView.setDataFrame(df);
    }
}
