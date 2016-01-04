package gov.ornl.csed.cda.timevis;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Created by csg on 10/20/15.
 */
public class BorderedTitledPane extends StackPane {
    BorderedTitledPane(String titleString, Node content) {
        Label titleLabel = new Label("  " + titleString + "  ");
        titleLabel.getStyleClass().add("bordered-titled-title");
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);
        StackPane contentPane = new StackPane();
        content.getStyleClass().add("bordered-titled-content");
        contentPane.getChildren().add(content);
        getStyleClass().add("bordered-titled-border");
        getChildren().addAll(titleLabel, contentPane);
    }
}
