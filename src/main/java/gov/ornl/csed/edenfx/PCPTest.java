package gov.ornl.csed.edenfx;

import gov.ornl.datatable.DataModel;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.File;


/**
 * Created by csg on 10/23/15.
 */
public class PCPTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root, 800, 400, Color.ALICEBLUE);

        Group pcVis = new Group();
        ScrollPane scroller = new ScrollPane(pcVis);

        DataModel dataModel = new DataModel();
        IOUtilities.readCSV(new File("data/csv/cars.csv"), dataModel);

        render(dataModel, pcVis, 1000, 400);

        root.getChildren().add(scroller);

        primaryStage.setTitle("PCP Test");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main (String args[]) {
        launch(args);
    }

    private void render(DataModel dataModel, Group visGroup, int width, int height) {
        double axisSpacing = width / dataModel.getColumnCount();
        double startX = axisSpacing / 2.;
        double axisTop = 10.;
        double axisBottom = height - 10.;

        for (int i = 0; i < dataModel.getColumnCount(); i++) {
            double centerX = startX + (i * axisSpacing);
            Line axisLine = new Line(centerX, axisTop, centerX, axisBottom);
            visGroup.getChildren().add(axisLine);
        }
    }
}
