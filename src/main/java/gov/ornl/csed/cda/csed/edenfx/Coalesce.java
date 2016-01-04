package gov.ornl.csed.cda.csed.edenfx;

import gov.ornl.csed.cda.pcvis.PCPanel;
import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;


/**
 * Created by csg on 10/23/15.
 */
public class Coalesce extends Application implements DataModelListener {
    private final static Logger log = LoggerFactory.getLogger(Coalesce.class);

    private PCPanel pcPanel;
    private DataModel pcDataModel;
    private TableView columnTable = new TableView();
    private TimeSeriesPanel overviewTimeSeriesPanel;
    private TimeSeriesPanel detailsTimeSeriesPanel;

    private ArrayList<PCPAxis> pcpAxisList = new ArrayList<PCPAxis>();

    public static void main(String args[]) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane rootNode = new BorderPane();

        Scene scene = new Scene(rootNode, 1400, 800);

        pcDataModel = new DataModel();
        pcDataModel.addDataModelListener(this);
        pcPanel = new PCPanel(pcDataModel);
        pcPanel.setBackground(Color.white);

        SwingNode pcPanelSwingNode = new SwingNode();
        pcPanelSwingNode.setContent(pcPanel);

        detailsTimeSeriesPanel = new TimeSeriesPanel(2, ChronoUnit.MINUTES, TimeSeriesPanel.PlotDisplayOption.LINE);
        detailsTimeSeriesPanel.setBackground(Color.white);
        detailsTimeSeriesPanel.setPreferredSize(new Dimension(1400, 120));
        SwingNode detailsTimeSeriesPanelSwingNode = new SwingNode();
        detailsTimeSeriesPanelSwingNode.setContent(detailsTimeSeriesPanel);

        overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.LINE);
        overviewTimeSeriesPanel.setBackground(Color.white);
        overviewTimeSeriesPanel.setPreferredSize(new Dimension(1400, 80));
        SwingNode overviewTimeSeriesPanelSwingNode = new SwingNode();
        overviewTimeSeriesPanelSwingNode.setContent(overviewTimeSeriesPanel);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(detailsTimeSeriesPanelSwingNode, overviewTimeSeriesPanelSwingNode);

        columnTable = createColumnTableView();
        Tab columnTab = new Tab();
        columnTab.setText("Axis Information");
        columnTab.setContent(columnTable);

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(columnTab);

        StackPane pcStackPane = new StackPane();
        pcStackPane.getChildren().addAll(pcPanelSwingNode);
        StackPane tableStackPane = new StackPane();
        tableStackPane.getChildren().add(tabPane);

        SplitPane topSplit = new SplitPane();
        topSplit.setOrientation(Orientation.VERTICAL);
        topSplit.getItems().addAll(pcStackPane, tableStackPane);
        topSplit.setDividerPositions(0.7);
        topSplit.setResizableWithParent(pcStackPane, false);

        MenuBar menuBar = createMenuBar(primaryStage);

        rootNode.setTop(menuBar);
        rootNode.setCenter(topSplit);

        primaryStage.setTitle("EDEN FX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TableView createColumnTableView() {
        TableView table = new TableView();
        table.setEditable(true);
        table.setItems(FXCollections.observableArrayList(pcpAxisList));

        TableColumn nameCol = new TableColumn("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory("axisName"));

        TableColumn visibleCol = new TableColumn("Visible");
        TableColumn typeCol = new TableColumn("Type");
        TableColumn meanCol = new TableColumn("Mean");
        meanCol.setCellValueFactory(new PropertyValueFactory("meanValue"));
        TableColumn medianCol = new TableColumn("Median");
        medianCol.setCellValueFactory(new PropertyValueFactory("medianValue"));
        TableColumn stdevCol = new TableColumn("Standard Dev.");
        stdevCol.setCellValueFactory(new PropertyValueFactory("stdevValue"));
        TableColumn iqrCol = new TableColumn("IQR");
        iqrCol.setCellValueFactory(new PropertyValueFactory("iqrValue"));
        TableColumn skewnessCol = new TableColumn("Skewness");
        skewnessCol.setCellValueFactory(new PropertyValueFactory("skewnessValue"));
        TableColumn kurtosisCol = new TableColumn("Kurtosis");
        kurtosisCol.setCellValueFactory(new PropertyValueFactory("kurtosisValue"));
        TableColumn minCol = new TableColumn("Min");
        minCol.setCellValueFactory(new PropertyValueFactory("minValue"));
        TableColumn maxCol = new TableColumn("Max");
        maxCol.setCellValueFactory(new PropertyValueFactory("maxValue"));
        TableColumn focusMinCol = new TableColumn("Focus Min");
        TableColumn focusMaxCol = new TableColumn("Focus Max");

        table.getColumns().addAll(visibleCol, nameCol, typeCol, focusMinCol, focusMaxCol, minCol, maxCol,
                meanCol, stdevCol, medianCol, iqrCol, skewnessCol, kurtosisCol);

        return table;
    }

    private MenuBar createMenuBar(final Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        javafx.scene.control.Menu fileMenu = new javafx.scene.control.Menu("File");

        MenuItem openCSVFile = new MenuItem("Open CSV...");
        openCSVFile.setOnAction ( new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser csvFileChooser = new FileChooser();
                csvFileChooser.setTitle("Select CSV File");
                File csvFile = csvFileChooser.showOpenDialog(primaryStage);
                if (csvFile != null) {
                    try {
                        readCSVFile(csvFile, Double.NaN);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        MenuItem exportCSVFile = new MenuItem("Export to CSV...");
        exportCSVFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                log.debug("Export to CSV file");
            }
        });

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.close();
            }
        });

        fileMenu.getItems().addAll(openCSVFile, exportCSVFile, new SeparatorMenuItem(), exitMenuItem);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    private void readCSVFile (File csvFile, double samplingFactor) throws IOException {
        pcDataModel.clear();
        if (!Double.isNaN(samplingFactor)) {
            IOUtilities.readCSVSample(csvFile, pcDataModel, samplingFactor);
        } else {
            IOUtilities.readCSV(csvFile, pcDataModel);
        }
    }

    @Override
    public void dataModelChanged(DataModel dataModel) {
        pcpAxisList.clear();
        for (int i = 0; i < dataModel.getColumnCount(); i++) {
            Column column = dataModel.getColumn(i);
            PCPAxis axis = new PCPAxis();
            axis.setAxisName(column.getName());
            axis.setMinValue(column.getSummaryStats().getMin());
            axis.setMaxValue(column.getSummaryStats().getMax());
            axis.setIQRValue(column.getSummaryStats().getIQR());
            axis.setStdevValue(column.getSummaryStats().getStandardDeviation());
            axis.setKurtosisValue(column.getSummaryStats().getKurtosis());
            axis.setSkewnessValue(column.getSummaryStats().getSkewness());
            axis.setMeanValue(column.getSummaryStats().getMean());
            axis.setMedianValue(column.getSummaryStats().getMedian());

            pcpAxisList.add(axis);
        }
        columnTable.getItems().addAll(FXCollections.observableArrayList(pcpAxisList));
//        columnTable.setItems();
    }

    @Override
    public void queryChanged(DataModel dataModel) {

    }

    @Override
    public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {

    }

    @Override
    public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {

    }

    @Override
    public void highlightedColumnChanged(DataModel dataModel) {

    }

    @Override
    public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {

    }

    @Override
    public void columnDisabled(DataModel dataModel, Column disabledColumn) {

    }

    @Override
    public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void columnEnabled(DataModel dataModel, Column enabledColumn) {

    }
}
