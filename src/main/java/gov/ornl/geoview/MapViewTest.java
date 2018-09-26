package gov.ornl.geoview;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MapViewTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        MapView mapView = new MapView();
        mapView.setPadding(new Insets(10));
        mapView.setPrefWidth(600);
        mapView.setPrefHeight(400);

        mapView.setSelectionMask(-60, 30, -60, 30);

        VBox labelBox = new VBox();
        labelBox.setSpacing(2.);
        labelBox.setPadding(new Insets(10));

        Label hoverLabel = new Label("Mouse Cursor Geographic Location");
        Label selectionLabel = new Label("Selection Geographic Location");
        labelBox.getChildren().addAll(hoverLabel, selectionLabel);

        mapView.mouseHoverGeoLocationProperty().addListener(observable -> {
            if (mapView.getMouseHoverGeoLocation() != null) {
                hoverLabel.setText("Mouse Cursor at [" + mapView.getMouseHoverGeoLocation().getX() + ", " +
                        mapView.getMouseHoverGeoLocation().getY() + "]");
            } else {
                hoverLabel.setText("Mouse Cursor Geographic Location");
            }
        });

        mapView.selectionGeoRectangleProperty().addListener(observable -> {
            if (mapView.getSelectionGeoRectangle() != null) {
                selectionLabel.setText("Selection L: " + mapView.getSelectionGeoRectangle().leftLongitude +
                        ", R: " + mapView.getSelectionGeoRectangle().rightLongitude +
                        ", T: " + mapView.getSelectionGeoRectangle().topLatitude +
                        ", B: " + mapView.getSelectionGeoRectangle().bottomLatitude);
            } else {
                selectionLabel.setText("Selection Geographic Location");
            }
        });

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(mapView);
        rootNode.setBottom(labelBox);

        Scene scene = new Scene(rootNode, 600, 400, true, SceneAntialiasing.BALANCED);

        primaryStage.setTitle("MapView Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
