package gov.ornl.crossvis;

import gov.ornl.correlationview.CorrelationMatrixView;
import gov.ornl.datatable.DataTable;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class CorrelationViewWindow extends Application {
    private CorrelationMatrixView correlationMatrixView;
    private DataTable dataTable;

    public CorrelationViewWindow(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        correlationMatrixView = new CorrelationMatrixView();
        correlationMatrixView.setPrefHeight(500);
        correlationMatrixView.setPadding(new Insets(10));

        MenuBar menuBar = createMenuBar(primaryStage);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(correlationMatrixView);
        rootNode.setTop(menuBar);

        Scene scene = new Scene(rootNode, 600, 600, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("M u s t a n g | Correlation Matrix View");
        primaryStage.setScene(scene);
        primaryStage.show();

        correlationMatrixView.setDataTable(dataTable);
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        MenuItem closeMenuItem = new MenuItem("Close Correlation View Window");
        closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN));
        closeMenuItem.setOnAction(event -> { stage.close(); });

        CheckMenuItem showQueryCorrelationsMenuItem = new CheckMenuItem("Show Query Correlations");
        showQueryCorrelationsMenuItem.selectedProperty().bindBidirectional(correlationMatrixView.showQueryCorrelationsProperty());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(closeMenuItem);

        Menu viewMenu = new Menu("View");
        viewMenu.getItems().add(showQueryCorrelationsMenuItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu);
        return menuBar;
    }
}
