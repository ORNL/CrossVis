package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.Column;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.QuantitativeColumn;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class PCPAxis {
    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;

    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;

    protected DataModel dataModel;
    protected Column column;

    protected double centerX;
    protected Rectangle bounds;

    protected Group graphicsGroup;

    protected Text nameText;
    protected DoubleProperty nameTextRotation;

    protected Color labelColor = DEFAULT_LABEL_COLOR;

    protected Pane pane;

    protected PCPView pcpView;

    public PCPAxis (PCPView pcpView, Column column, DataModel dataModel, Pane pane) {
        this.pcpView = pcpView;
        this.column = column;
        this.dataModel = dataModel;
        this.pane = pane;

        centerX = 0d;
        bounds = new Rectangle();

        nameTextRotation = new SimpleDoubleProperty(0.0);

        nameText = new Text(column.getName());
//        nameText.textProperty().bindBidirectional(column.nameProperty());
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bindBidirectional(column.nameProperty());
        Tooltip.install(nameText, tooltip);
        nameText.setFont(new Font(DEFAULT_NAME_TEXT_SIZE));
        nameText.setSmooth(true);
        nameText.setFill(labelColor);
        nameText.rotateProperty().bindBidirectional(nameTextRotation);

        graphicsGroup = new Group(nameText);

    }

    public void setLabelColor(Color labelColor) {
        if (this.labelColor != labelColor) {
            this.labelColor = labelColor;
            nameText.setFill(labelColor);
//            minValueText.setFill(labelColor);
//            maxValueText.setFill(labelColor);
        }
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public Column getColumn() { return column; }

    public Group getGraphicsGroup() { return graphicsGroup; }

    public Text getNameText() { return nameText; }

    public final double getNameTextRotation() { return nameTextRotation.get(); }

    public final void setNameTextRotation(double value) { nameTextRotation.set(value); }

    public DoubleProperty nameTextRotationProperty() { return nameTextRotation; }

    public abstract void layout(double centerX, double topY, double width, double height);

    protected Line makeLine() {
        Line line = new Line();
        line.setStroke(Color.DARKGRAY);
        line.setSmooth(true);
        line.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        return line;
    }
}
