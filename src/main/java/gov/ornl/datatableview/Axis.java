package gov.ornl.datatableview;

import gov.ornl.datatable.DataTable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class Axis {
    public final static double DEFAULT_TITLE_TEXT_SIZE = 12d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;

    private StringProperty title;
    private DataTableView dataTableView;

    private Text titleText;
    private Rectangle titleTextRectangle;

    private Bounds bounds;
    private double centerX = 0d;
    private double centerY = 0d;

    private ObjectProperty<Color> textColor = new SimpleObjectProperty<>(DEFAULT_TEXT_COLOR);

    private Group graphicsGroup = new Group();

    public Axis(DataTableView dataTableView, String title) {
        this.dataTableView = dataTableView;
        this.title = new SimpleStringProperty(title);

        titleText = new Text(title);
        titleText.setFont(new Font(DEFAULT_TITLE_TEXT_SIZE));
        titleText.setSmooth(true);
        titleText.setFill(DEFAULT_TEXT_COLOR);
        titleText.setMouseTransparent(true);

        titleTextRectangle = new Rectangle();
        titleTextRectangle.setStrokeWidth(3.);
        titleTextRectangle.setStroke(Color.TRANSPARENT);
        titleTextRectangle.setFill(Color.TRANSPARENT);
        titleTextRectangle.setArcWidth(6.);
        titleTextRectangle.setArcHeight(6.);

        graphicsGroup.getChildren().addAll(titleTextRectangle, titleText);

        registerListeners();
    }

    public Color getTextColor() { return textColor.get(); }

    public void setTextColor(Color c) { textColor.set(c); }

    public ObjectProperty<Color> textColorProperty() { return textColor; }

    protected Bounds getBounds() { return bounds; }

    protected double getCenterX() { return centerX; }

    protected double getCenterY() { return centerY; }

    private void registerListeners() {
        titleText.textProperty().addListener((observable, oldValue, newValue) -> {
            titleText.setX(bounds.getMinX() + ((bounds.getWidth() - titleText.getLayoutBounds().getWidth()) / 2.));
            titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());
        });
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    protected DataTableView getDataTableView() { return dataTableView; }

    protected DataTable getDataTable() { return dataTableView.getDataTable(); }

    protected Text getTitleText() { return titleText; }

    public void resize (double left, double top, double width, double height) {
        bounds = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

        titleText.setText(title.getValue());
        if (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
            }
        }
        titleText.setX(bounds.getMinX() + ((width - titleText.getLayoutBounds().getWidth()) / 2.));
        titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());

        titleTextRectangle.setX(titleText.getX() - 4.);
        titleTextRectangle.setY(titleText.getY() - titleText.getLayoutBounds().getHeight());
        titleTextRectangle.setWidth(titleText.getLayoutBounds().getWidth() + 8.);
        titleTextRectangle.setHeight(titleText.getLayoutBounds().getHeight() + 4.);


    }
}
