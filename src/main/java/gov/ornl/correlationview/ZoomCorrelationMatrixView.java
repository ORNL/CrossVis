package gov.ornl.correlationview;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.logging.Logger;

public class ZoomCorrelationMatrixView extends Region {
    private final static Logger log = Logger.getLogger(ZoomCorrelationMatrixView.class.getName());

    private Pane pane;

    private Bounds viewBounds;

    private Group cellGroup = new Group();

    private int numRows = 4;
    private int numCols = 4;

    private boolean dragging = false;
    private Point2D dragStartPoint;
    private Point2D dragEndPoint;

    public ZoomCorrelationMatrixView () {
        initialize();
        registerListeners();
    }

    private void initialize() {
        pane = new Pane();
        this.setBackground(new Background(new BackgroundFill(Color.GHOSTWHITE, null, null)));
        pane.getChildren().add(cellGroup);

        this.getChildren().add(pane);
//        initView();
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resizeView());
        heightProperty().addListener(o -> resizeView());

        pane.setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            pane.setTranslateX(dragEndPoint.getX() - dragStartPoint.getX());
            pane.setTranslateY(dragEndPoint.getY() - dragStartPoint.getY());
        });
    }

    public void resizeView() {
        double cellWidth = getWidth() / numCols;
        double cellHeight = getHeight() / numRows;

        cellGroup.getChildren().clear();
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                Rectangle rect = new Rectangle(i*cellWidth, j*cellHeight, cellWidth, cellHeight);
                rect.setStroke(Color.SLATEGRAY);
                rect.setFill(Color.CORAL);
                cellGroup.getChildren().add(rect);
            }
        }
    }

//    public void resizeView() {
//
//    }
}
