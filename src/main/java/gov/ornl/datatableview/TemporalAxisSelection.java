package gov.ornl.datatableview;

import gov.ornl.datatable.TemporalColumn;
import gov.ornl.datatable.TemporalColumnSelectionRange;
import gov.ornl.util.GraphicsUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.time.Instant;

public class TemporalAxisSelection extends UnivariateAxisSelection {

    private Text minText;
    private Text maxText;
    private ObjectProperty<Instant> draggingMinValue;
    private ObjectProperty<Instant> draggingMaxValue;

    public TemporalAxisSelection(TemporalAxis temporalAxis, TemporalColumnSelectionRange selectionRange, double minValueY, double maxValueY) {
        super(temporalAxis, selectionRange, minValueY, maxValueY);

        minText = new Text(String.valueOf(selectionRange.getStartInstant()));
        minText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minText.setX(temporalAxis.getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));
        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());

        minText.setFill(DEFAULT_TEXT_FILL);
        minText.setVisible(false);

        maxText = new Text(String.valueOf(selectionRange.getEndInstant()));
        maxText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxText.setX(temporalAxis.getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
        maxText.setFill(DEFAULT_TEXT_FILL);
        maxText.setVisible(false);

        getGraphicsGroup().getChildren().addAll(minText, maxText);

        registerListeners();
    }

    private TemporalColumnSelectionRange temporalColumnSelection() {
        return (TemporalColumnSelectionRange)getColumnSelection();
    }

    private TemporalAxis temporalAxis() {
        return (TemporalAxis)univariateAxis();
    }

    protected void layoutGraphics(double bottomY, double topY) {
        super.layoutGraphics(bottomY, topY);

        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());
        minText.setX(univariateAxis().getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));

        maxText.setX(univariateAxis().getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
    }


    @Override
    protected void handleRectangleMouseEntered() {
        minText.setVisible(true);
        maxText.setVisible(true);
    }

    @Override
    protected void handleRectangleMouseExited() {
        minText.setVisible(false);
        maxText.setVisible(false);
    }

    @Override
    protected void handleRectangleMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;

            // unbind range selection min/max labels from selection range min/max properties
//                    minText.textProperty().unbindBidirectional(getColumnSelectionRange().minValueProperty());
//                    maxText.textProperty().unbindBidirectional(getColumnSelectionRange().maxValueProperty());

            // bind range selection min/max labels to local values during drag operation
            draggingMinValue = new SimpleObjectProperty(temporalColumnSelection().getStartInstant());
            draggingMaxValue = new SimpleObjectProperty(temporalColumnSelection().getEndInstant());
            draggingMinValue.addListener((observable, oldValue, newValue) -> {
                minText.setText(draggingMinValue.get().toString());
            });
            draggingMaxValue.addListener((observable, oldValue, newValue) -> {
                maxText.setText(draggingMaxValue.get().toString());
            });
//                    minText.textProperty().bindBidirectional(draggingMinValue, new NumberStringConverter());
//                    maxText.textProperty().bindBidirectional(draggingMaxValue, new NumberStringConverter());
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double topY = getTopY() + deltaY;
        double bottomY = getBottomY() + deltaY;

        if (topY < univariateAxis().getFocusMaxPosition()) {
            deltaY = univariateAxis().getFocusMaxPosition() - topY;
            topY = univariateAxis().getFocusMaxPosition();
            bottomY = bottomY + deltaY;
        }

        if (bottomY > univariateAxis().getFocusMinPosition()) {
            deltaY = bottomY - univariateAxis().getFocusMinPosition();
            topY = topY - deltaY;
            bottomY = univariateAxis().getFocusMinPosition();
        }

        draggingMaxValue.set(GraphicsUtil.mapValue(topY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant()));

        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant()));

        layoutGraphics(bottomY, topY);
    }

    @Override
    protected void handleRectangleMousePressed(MouseEvent event) {
        //TODO: Make popup dialog to change start and end instants for selection
    }

    @Override
    protected void handleRectangleMouseReleased() {
        if (dragging) {
            dragging = false;

            // update column selection range min/max properties
            temporalColumnSelection().setRangeInstants((Instant)draggingMinValue.get(), (Instant)draggingMaxValue.get());

        } else {
//            getPane().getChildren().remove(getGraphicsGroup());
//            getPCPAxis().getAxisSelectionList().remove(this);
            getAxis().getDataTable().removeColumnSelectionFromActiveQuery(temporalColumnSelection());
//                    dataModel.setQueriedTuples();
        }
    }

    @Override
    protected void handleBottomCrossbarMouseEntered() {
        maxText.setVisible(false);
        minText.setVisible(true);
    }

    @Override
    protected void handleBottomCrossbarMouseExited() {
        maxText.setVisible(false);
        minText.setVisible(false);
    }

    @Override
    protected void handleBottomCrossbarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;

            // bind range selection min/max labels to local values during drag operation
            draggingMinValue = new SimpleObjectProperty(temporalColumnSelection().getStartInstant());
            draggingMinValue.addListener((observable, oldValue, newValue) -> {
                minText.setText(draggingMinValue.get().toString());
            });
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double bottomY = getBottomY() + deltaY;

        if (bottomY > univariateAxis().getFocusMinPosition()) {
            bottomY = univariateAxis().getFocusMinPosition();
        }

        if (bottomY < getTopY()) {
            bottomY = getTopY();
        }

        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant()));
        layoutGraphics(bottomY, getTopY());
    }

    @Override
    protected void handleBottomCrossbarMousePressed() {

    }

    @Override
    protected void handleBottomCrossbarMouseReleased() {
        if (dragging) {
            dragging = false;

            // update column selection range min properties
            temporalColumnSelection().setStartInstant((Instant)draggingMinValue.get());

            // unbind selection range min labels from dragging min range value
            minText.textProperty().unbindBidirectional(draggingMinValue);
        }
    }

    @Override
    protected void handleTopCrossbarMouseEntered() {
        minText.setVisible(false);
        maxText.setVisible(true);
    }

    @Override
    protected void handleTopCrossbarMouseExited() {
        maxText.setVisible(false);
        minText.setVisible(false);
    }

    @Override
    protected void handleTopCrossbarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;

            // bind range selection max labels to local value during drag operation
            draggingMaxValue = new SimpleObjectProperty(temporalColumnSelection().getEndInstant());
            draggingMaxValue.addListener((observable, oldValue, newValue) -> {
                maxText.setText(draggingMaxValue.get().toString());
            });
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double topY = getTopY() + deltaY;

        if (topY < univariateAxis().getFocusMaxPosition()) {
            topY = univariateAxis().getFocusMaxPosition();
        }

        if (topY > getBottomY()) {
            topY = getBottomY();
        }

        draggingMaxValue.set(GraphicsUtil.mapValue(topY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant()));

        layoutGraphics(getBottomY(), topY);
    }

    @Override
    protected void handleTopCrossbarMousePressed() {

    }

    @Override
    protected void handleTopCrossbarMouseReleased() {
        if (dragging) {
            dragging = false;

            // update column selection range max property
            temporalColumnSelection().setEndInstant((Instant)draggingMaxValue.get());

            // unbind selection range max label from dragging max range value
            maxText.textProperty().unbindBidirectional(draggingMaxValue);
       }
    }

    private void registerListeners() {
        temporalColumnSelection().rangeInstantsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                minText.setText(newValue.get(0).toString());
                maxText.setText(newValue.get(1).toString());
                resize();
            }
        });
    }

    @Override
    public void resize() {
        double topY = GraphicsUtil.mapValue(temporalColumnSelection().getEndInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
        double bottomY = GraphicsUtil.mapValue(temporalColumnSelection().getStartInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getStartInstant(),
                ((TemporalColumn)univariateAxis().getColumn()).getStatistics().getEndInstant(),
                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
        layoutGraphics(bottomY, topY);
    }

    public void update(Instant minValue, Instant maxValue, double minValueY, double maxValueY) {
        temporalColumnSelection().setEndInstant(maxValue);
        temporalColumnSelection().setStartInstant(minValue);
        layoutGraphics(minValueY, maxValueY);
    }

    private Dialog<Pair<Double, Double>> createSelectionRangeInputDialog (Instant minValue, Instant maxValue) {
        Dialog<Pair<Double, Double>> dialog = new Dialog<>();
        dialog.setTitle("Change Selection Value Range");
        dialog.setHeaderText("Enter New Minimum and Maximum Range Values");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 150, 10, 10));

        NumberTextField minValueField = new NumberTextField();
        minValueField.setText(String.valueOf(minValue));
        NumberTextField maxValueField = new NumberTextField();
        maxValueField.setText(String.valueOf(maxValue));

        grid.add(new Label(" Maximum Value: "), 0, 0);
        grid.add(maxValueField, 1, 0);
        grid.add(new Label(" Minimum Value: "), 0, 1);
        grid.add(minValueField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> minValueField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<Double, Double> (new Double(minValueField.getText()), new Double(maxValueField.getText()));
            }
            return null;
        });

        return dialog;
    }
}
