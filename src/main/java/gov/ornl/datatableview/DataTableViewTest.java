package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.collections.FXCollections;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

public class DataTableViewTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		DataTableView dataTableView = new DataTableView();
		dataTableView.setPrefHeight(500);
		dataTableView.setPadding(new Insets(10, 10, 10, 10));

		ScrollPane scrollPane = new ScrollPane(dataTableView);
		scrollPane.setFitToWidth(dataTableView.getFitToWidth());
		scrollPane.setFitToHeight(true);

		Button loadDataButton = new Button("Load Data");
		loadDataButton.setOnAction(event -> {
			try {
				DataTable dataTable = new DataTable();
				dataTable.setCalculateNonQueryStatistics(true);

				// reads titan data with date field
//                ArrayList<String> temporalColumnNames = new ArrayList<>();
//                temporalColumnNames.add("Date");
//                ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//                temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//                IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                        temporalColumnNames, temporalColumnFormatters, dataTable);

				// Reads cars data set
//                IOUtilities.readCSV(new File("data/csv/cars.csv"), null, null, null, null, dataTable);

//				ArrayList<String> categoricalColumnNames = new ArrayList<>();
//				categoricalColumnNames.add("Origin");
//				IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames, null, null,
//						dataTable);

				ArrayList<String> categoricalColumnNames = new ArrayList<>();
				categoricalColumnNames.add("TYPE");
				IOUtilities.readCSV(new File("/Users/csg/Dropbox (ORNL)/papers/diatom-paper/Data vis_Parameters of diatoms.csv"), null, categoricalColumnNames, null, null,
						dataTable);

				dataTableView.setDataTable(dataTable);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		});

		Button addBivariateAxisButton = new Button("Add Bivariate Axis");
		addBivariateAxisButton.setOnAction(event -> {
			dataTableView.getDataTable().addBivariateColumn(dataTableView.getDataTable().getColumn(2),
					dataTableView.getDataTable().getColumn(3), 0);
		});

		Slider opacitySlider = new Slider(0.01, 1., dataTableView.getDataItemsOpacity());
//        opacitySlider.valueProperty().bindBidirectional(pcpView.dataItemsOpacityProperty());
		opacitySlider.setShowTickLabels(false);
		opacitySlider.setShowTickMarks(false);
		opacitySlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				dataTableView.setDataItemsOpacity(opacitySlider.getValue());
			}
		});

		CheckBox showScatterplotsCB = new CheckBox("Show Scatterplots");
		showScatterplotsCB.selectedProperty().bindBidirectional(dataTableView.showScatterplotsProperty());

		CheckBox showPolylinesCB = new CheckBox("Show Polylines");
		showPolylinesCB.selectedProperty().bindBidirectional(dataTableView.showPolylinesProperty());

		CheckBox showSummaryStatsCB = new CheckBox("Show Summary Statistics");
		showSummaryStatsCB.selectedProperty().bindBidirectional(dataTableView.showSummaryStatisticsProperty());

		CheckBox showHistogramCB = new CheckBox("Show Histograms");
		showHistogramCB.selectedProperty().bindBidirectional(dataTableView.showHistogramsProperty());

		CheckBox showSelectedPolylinesCB = new CheckBox("Show Selected Polylines");
		showSelectedPolylinesCB.selectedProperty().bindBidirectional(dataTableView.showSelectedItemsProperty());

		CheckBox showUnselectedPolylinesCB = new CheckBox("Show Unselected Polylines");
		showUnselectedPolylinesCB.selectedProperty().bindBidirectional(dataTableView.showUnselectedItemsProperty());

		CheckBox showCorrelationIndicatorsCB = new CheckBox("Show Correlations");
		showCorrelationIndicatorsCB.selectedProperty().bindBidirectional(dataTableView.showCorrelationsProperty());

		ChoiceBox<DataTableView.POLYLINE_DISPLAY_MODE> polylineDisplayModeChoiceBox =
				new ChoiceBox<>(FXCollections.observableArrayList(DataTableView.POLYLINE_DISPLAY_MODE.POLYLINES,
						DataTableView.POLYLINE_DISPLAY_MODE.BINNED_POLYLINES));
		if (dataTableView.getPolylineDisplayMode() == DataTableView.POLYLINE_DISPLAY_MODE.POLYLINES) {
			polylineDisplayModeChoiceBox.getSelectionModel().select(0);
		} else {
			polylineDisplayModeChoiceBox.getSelectionModel().select(1);
		}
		polylineDisplayModeChoiceBox.setOnAction(event -> {
			dataTableView.setPolylineDisplayMode(polylineDisplayModeChoiceBox.getValue());
		});

		ChoiceBox<DataTableView.STATISTICS_DISPLAY_MODE> statisticsDisplayModeChoiceBox =
				new ChoiceBox<>(FXCollections.observableArrayList(DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT,
						DataTableView.STATISTICS_DISPLAY_MODE.MEDIAN_BOXPLOT));
		if (dataTableView.getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
			statisticsDisplayModeChoiceBox.getSelectionModel().select(0);
		} else {
			statisticsDisplayModeChoiceBox.getSelectionModel().select(1);
		}
		statisticsDisplayModeChoiceBox.setOnAction(event -> {
			dataTableView.setSummaryStatisticsDisplayMode(statisticsDisplayModeChoiceBox.getValue());
		});

		Button addColumnButton = new Button("Add Disabled Column");
		addColumnButton.setOnAction(event -> {
			ArrayList<Column> disabledColumns = dataTableView.getDataTable().getDisabledColumns();
			if (disabledColumns != null && !(disabledColumns.isEmpty())) {
				ChoiceDialog<Column> choiceDialog = new ChoiceDialog<>(disabledColumns.get(0), disabledColumns);
				Optional<Column> result = choiceDialog.showAndWait();
				if (result.isPresent()) {
					dataTableView.getDataTable().enableColumn(result.get());
				}
			}
		});

		HBox settingsPane = new HBox();
		settingsPane.setSpacing(2);
		settingsPane.setPadding(new Insets(4));

		settingsPane.getChildren().addAll(loadDataButton, addBivariateAxisButton, showPolylinesCB,
				showSelectedPolylinesCB, showUnselectedPolylinesCB, showHistogramCB, showSummaryStatsCB, showCorrelationIndicatorsCB,
				showScatterplotsCB, statisticsDisplayModeChoiceBox, polylineDisplayModeChoiceBox, opacitySlider, addColumnButton);

		BorderPane rootNode = new BorderPane();
		rootNode.setCenter(scrollPane);
		rootNode.setBottom(settingsPane);

		Rectangle2D screenVisualBounds = Screen.getPrimary().getVisualBounds();
		double sceneWidth = screenVisualBounds.getWidth() - 40;
		sceneWidth = sceneWidth > 2000 ? 2000 : sceneWidth;

		Scene scene = new Scene(rootNode, sceneWidth, 600, true, SceneAntialiasing.BALANCED);

		primaryStage.setTitle("DataTableView Test");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public void stop() {
		System.exit(0);
	}
}
