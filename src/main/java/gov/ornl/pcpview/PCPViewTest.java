package gov.ornl.pcpview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by csg on 8/22/16.
 */
public class PCPViewTest extends Application {
    public static final Logger log = Logger.getLogger(PCPViewTest.class.getName());

    private PCPView pcpView;
    private DataTable dataTable;

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) throws Exception {
        pcpView = new PCPView();
        pcpView.setPrefHeight(500);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpView.setPolylineDisplayMode(PCPView.POLYLINE_DISPLAY_MODE.POLYLINES);

        ScrollPane scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToWidth(pcpView.getFitToWidth());
        scrollPane.setFitToHeight(true);

        Button dataButton = new Button("Load Data");
        dataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    long start = System.currentTimeMillis();
//                    ArrayList<String> ignoreColumnNames = new ArrayList<>();
//                    ignoreColumnNames.add("CMEDV");
//                    ignoreColumnNames.add("INDUS");
//                    ignoreColumnNames.add("CHAS");
//                    ignoreColumnNames.add("NOX");
//                    ignoreColumnNames.add("RM");
//                    ignoreColumnNames.add("DIS");
//                    ignoreColumnNames.add("RAD");
//                    ignoreColumnNames.add("PTRATIO");
//                    ignoreColumnNames.add("B");
//                    ignoreColumnNames.add("LSTAT");
//                    IOUtilities.readCSV(new File("data/csv/boston_corrected_cleaned.csv"), ignoreColumnNames,
//                            null, null, null, dataTable);

//                    IOUtilities.readCSV(new File("data/csv/cars.csv"), null, null,
//                            null, null, dataTable);
//                    IOUtilities.readCSV(new File("/Users/csg/Dropbox (ORNL)/projects/SciDAC/data/2018-01-RiccuitoEnsemble/QMCdaily_US_combined.csv"),
//                            null, null, null, null, dataTable);

                    ArrayList<String> categoricalColumnNames = new ArrayList<>();
                    categoricalColumnNames.add("Origin");
                    IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                            null, null, dataTable);

//                    ArrayList<String> temporalColumnNames = new ArrayList<>();
//                    temporalColumnNames.add("Date");
//                    ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//                    temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//////                    ArrayList<String> ignoreColumnNames = new ArrayList<>();
////////                    ignoreColumnNames.add("StageoutPilots");
//////
//                    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                            temporalColumnNames, temporalColumnFormatters, dataTable);
                    Column latColumn = dataTable.getColumn("LAT");
                    Column lonColumn = dataTable.getColumn("LON");
                    pcpView.setGeographicAxes(latColumn, lonColumn);

                    long elapsed = System.currentTimeMillis() - start;
                    log.info("Reading data and populating data model took " + elapsed + " ms");
                } catch (IOException e) {
                    System.exit(0);
                    e.printStackTrace();
                }
            }
        });

        ChoiceBox<PCPView.POLYLINE_DISPLAY_MODE> polylineDisplayModeChoiceBox =
                new ChoiceBox<>(FXCollections.observableArrayList(PCPView.POLYLINE_DISPLAY_MODE.POLYLINES,
                PCPView.POLYLINE_DISPLAY_MODE.BINNED_POLYLINES));
        if (pcpView.getPolylineDisplayMode() == PCPView.POLYLINE_DISPLAY_MODE.POLYLINES) {
            polylineDisplayModeChoiceBox.getSelectionModel().select(0);
        } else {
            polylineDisplayModeChoiceBox.getSelectionModel().select(1);
        }
        polylineDisplayModeChoiceBox.setOnAction(event -> {
            pcpView.setPolylineDisplayMode(polylineDisplayModeChoiceBox.getValue());
        });

        ChoiceBox<PCPView.STATISTICS_DISPLAY_MODE> statisticsDisplayModeChoiceBox =
                new ChoiceBox<>(FXCollections.observableArrayList(PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT,
                        PCPView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT));
        if (pcpView.getSummaryStatisticsDisplayMode() == PCPView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
            statisticsDisplayModeChoiceBox.getSelectionModel().select(0);
        } else {
            statisticsDisplayModeChoiceBox.getSelectionModel().select(1);
        }
        statisticsDisplayModeChoiceBox.setOnAction(event -> {
            pcpView.setSummaryStatisticsDisplayMode(statisticsDisplayModeChoiceBox.getValue());
        });

        Slider opacitySlider = new Slider(0.01, 1., pcpView.getDataItemsOpacity());
//        opacitySlider.valueProperty().bindBidirectional(pcpView.dataItemsOpacityProperty());
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);
        opacitySlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            log.info("opacity slide value changing is " + newValue);
            if (!newValue) {
                pcpView.setDataItemsOpacity(opacitySlider.getValue());
            }
        });

        CheckBox showScatterplotsCB = new CheckBox("Show Scatterplots");
        showScatterplotsCB.selectedProperty().bindBidirectional(pcpView.showScatterplotsProperty());

        CheckBox showPolylinesCB = new CheckBox("Show Polylines");
        showPolylinesCB.selectedProperty().bindBidirectional(pcpView.showPolylinesProperty());

        CheckBox showSummaryStatsCB = new CheckBox("Show Summary Statistics");
        showSummaryStatsCB.selectedProperty().bindBidirectional(pcpView.showSummaryStatisticsProperty());

        CheckBox showHistogramCB = new CheckBox("Show Histograms");
        showHistogramCB.selectedProperty().bindBidirectional(pcpView.showHistogramsProperty());

        CheckBox showSelectedPolylinesCB = new CheckBox("Show Selected Polylines");
        showSelectedPolylinesCB.selectedProperty().bindBidirectional(pcpView.showSelectedItemsProperty());

        CheckBox showUnselectedPolylinesCB = new CheckBox("Show Unselected Polylines");
        showUnselectedPolylinesCB.selectedProperty().bindBidirectional(pcpView.showUnselectedItemsProperty());

        CheckBox showCorrelationIndicatorsCB = new CheckBox("Show Correlations");
        showCorrelationIndicatorsCB.selectedProperty().bindBidirectional(pcpView.showCorrelationsProperty());

        HBox settingsPane = new HBox();
        settingsPane.setSpacing(2.);
        settingsPane.setPadding(new Insets(4));

        settingsPane.getChildren().add(dataButton);
        settingsPane.getChildren().add(showPolylinesCB);
        settingsPane.getChildren().add(showSelectedPolylinesCB);
        settingsPane.getChildren().add(showUnselectedPolylinesCB);
        settingsPane.getChildren().add(showHistogramCB);
        settingsPane.getChildren().add(showSummaryStatsCB);
        settingsPane.getChildren().add(showScatterplotsCB);
        settingsPane.getChildren().add(statisticsDisplayModeChoiceBox);
        settingsPane.getChildren().add(polylineDisplayModeChoiceBox);
        settingsPane.getChildren().add(showCorrelationIndicatorsCB);
        settingsPane.getChildren().add(opacitySlider);

//        SplitPane mainSplit = new SplitPane(settingsPane, scrollPane);
//        mainSplit.setOrientation(Orientation.HORIZONTAL);
//        mainSplit.setDividerPositions(0.2);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);
        rootNode.setBottom(settingsPane);

        Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
        double sceneWidth = screenVisualBounds.getWidth() - 40;
        sceneWidth = sceneWidth > 2000 ? 2000 : sceneWidth;

        Scene scene = new Scene(rootNode, sceneWidth, 600, true, SceneAntialiasing.BALANCED);

        stage.setTitle("PCPView Test");
        stage.setScene(scene);
        stage.show();

        dataTable = new DataTable();
        pcpView.setDataTable(dataTable);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
