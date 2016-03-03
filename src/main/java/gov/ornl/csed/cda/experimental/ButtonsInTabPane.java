package gov.ornl.csed.cda.experimental;

/**
 * Created by csg on 3/2/16.
 */
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/** https://forums.oracle.com/forums/thread.jspa?messageID=10573836 "Thread: Adding button next to tab " */
public class ButtonsInTabPane extends Application {
    public static void main(String[] args) { launch(args); }
    @Override public void start(Stage stage) {
        final VBox layout = new VBox(10);
        layout.getChildren().addAll(
                createTabPane(),
                createButtonInTabsTabPane(),
                createSideBySideButtonAndTabPane(),
                createFixedWidthTabPaneWithButtonOnTop(),
                createVariableWidthTabPaneWithButtonOnTop()
        );
        layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");

        stage.setScene(new Scene(layout));
        stage.show();
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(createTab("red"), createTab("green"), createTab("blue"));
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(Control.USE_PREF_SIZE);

        return tabPane;
    }

    private Tab createTab(final String color) {
        Tab tab = new Tab(color);
        tab.setContent(new Rectangle(500, 75, Color.valueOf(color)));
        return tab;
    }

    private TabPane createButtonInTabsTabPane() {
        final TabPane tabPane = createTabPane();
        int i = 1;
        for (final Tab tab: tabPane.getTabs()) {
            placeButtonInTab("ButtonInTab " + i, tab);
            i++;
        }
        return tabPane;
    }

    private void placeButtonInTab(final String buttonText, final Tab tab) {
        Button button = new Button(buttonText);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                tab.getTabPane().getSelectionModel().select(tab);
            }
        });
        tab.setGraphic(button);
    }

    private Pane createSideBySideButtonAndTabPane() {
        final HBox    hbox    = new HBox(5);
        final TabPane tabPane = createTabPane();
        final Button  button  = new Button("Button beside TabPane");
        hbox.getChildren().addAll(tabPane, button);

        return hbox;
    }

    private Group createFixedWidthTabPaneWithButtonOnTop() {
        final TabPane tabPane = createTabPane();
        final Button  button  = new Button("Button on top of fixed size TabPane");

        final Group   layout = new Group(tabPane, button);
        button.layoutXProperty().bind(
                tabPane.widthProperty().subtract(button.widthProperty().add(10.0))
        );
        button.setLayoutY(5);

        return layout;
    }

    private Pane createVariableWidthTabPaneWithButtonOnTop() {
        final TabPane tabPane = createTabPane();
        final Button  button  = new Button("Button on top of variable size TabPane");
        button.setMinWidth(Control.USE_PREF_SIZE);

        final Pane   layout = new AnchorPane();
        layout.getChildren().addAll(tabPane, button);
        AnchorPane.setTopAnchor(button, 5.0);
        AnchorPane.setRightAnchor(button, 10.0);
        layout.maxWidthProperty().bind(tabPane.widthProperty());

        return layout;
    }
}
