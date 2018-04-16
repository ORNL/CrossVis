package gov.ornl.pcpview;

import gov.ornl.datatable.DataModel;
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
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.time.Instant;

public class PCPTemporalAxisSelection extends PCPAxisSelection {

    private Text minText;
    private Text maxText;
    private ObjectProperty<Instant> draggingMinValue;
    private ObjectProperty<Instant> draggingMaxValue;

    public PCPTemporalAxisSelection(PCPAxis pcpAxis, TemporalColumnSelectionRange selectionRange, double minValueY, double maxValueY, Pane pane, DataModel dataModel) {
        super(pcpAxis, selectionRange, minValueY, maxValueY, pane, dataModel);

        minText = new Text(String.valueOf(selectionRange.getStartInstant()));
        minText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minText.setX(pcpAxis.getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));
        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());

        minText.setFill(DEFAULT_TEXT_FILL);
        minText.setVisible(false);

        maxText = new Text(String.valueOf(selectionRange.getEndInstant()));
        maxText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxText.setX(pcpAxis.getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
        maxText.setFill(DEFAULT_TEXT_FILL);
        maxText.setVisible(false);

        getGraphicsGroup().getChildren().addAll(minText, maxText);

        registerListeners();
    }

    private TemporalColumnSelectionRange temporalColumnSelectionRange() {
        return (TemporalColumnSelectionRange)getColumnSelectionRange();
    }

    private PCPTemporalAxis temporalAxis() {
        return (PCPTemporalAxis)getPCPAxis();
    }

    protected void layoutGraphics(double bottomY, double topY) {
        super.layoutGraphics(bottomY, topY);

        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());
        minText.setX(getPCPAxis().getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));

        maxText.setX(getPCPAxis().getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
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
            draggingMinValue = new SimpleObjectProperty(temporalColumnSelectionRange().getStartInstant());
            draggingMaxValue = new SimpleObjectProperty(temporalColumnSelectionRange().getEndInstant());
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

        if (topY < getPCPAxis().getFocusTopY()) {
            deltaY = getPCPAxis().getFocusTopY() - topY;
            topY = getPCPAxis().getFocusTopY();
            bottomY = bottomY + deltaY;
        }

        if (bottomY > getPCPAxis().getFocusBottomY()) {
            deltaY = bottomY - getPCPAxis().getFocusBottomY();
            topY = topY - deltaY;
            bottomY = getPCPAxis().getFocusBottomY();
        }

        draggingMaxValue.set(GraphicsUtil.mapValue(topY, getPCPAxis().getFocusTopY(), getPCPAxis().getFocusBottomY(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant()));

        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, getPCPAxis().getFocusTopY(), getPCPAxis().getFocusBottomY(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant()));

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
            temporalColumnSelectionRange().setRangeInstants((Instant)draggingMinValue.get(), (Instant)draggingMaxValue.get());

        } else {
            getPane().getChildren().remove(getGraphicsGroup());
            getPCPAxis().getAxisSelectionList().remove(this);
            getDataModel().clearColumnSelectionRange(temporalColumnSelectionRange());
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
            draggingMinValue = new SimpleObjectProperty(temporalColumnSelectionRange().getStartInstant());
            draggingMinValue.addListener((observable, oldValue, newValue) -> {
                minText.setText(draggingMinValue.get().toString());
            });
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double bottomY = getBottomY() + deltaY;

        if (bottomY > getPCPAxis().getFocusBottomY()) {
            bottomY = getPCPAxis().getFocusBottomY();
        }

        if (bottomY < getTopY()) {
            bottomY = getTopY();
        }

        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, getPCPAxis().getFocusTopY(), getPCPAxis().getFocusBottomY(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant()));
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
            temporalColumnSelectionRange().setStartInstant((Instant)draggingMinValue.get());

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
            draggingMaxValue = new SimpleObjectProperty(temporalColumnSelectionRange().getEndInstant());
            draggingMaxValue.addListener((observable, oldValue, newValue) -> {
                maxText.setText(draggingMaxValue.get().toString());
            });
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double topY = getTopY() + deltaY;

        if (topY < getPCPAxis().getFocusTopY()) {
            topY = getPCPAxis().getFocusTopY();
        }

        if (topY > getBottomY()) {
            topY = getBottomY();
        }

        draggingMaxValue.set(GraphicsUtil.mapValue(topY, getPCPAxis().getFocusTopY(), getPCPAxis().getFocusBottomY(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant()));

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
            temporalColumnSelectionRange().setEndInstant((Instant)draggingMaxValue.get());

            // unbind selection range max label from dragging max range value
            maxText.textProperty().unbindBidirectional(draggingMaxValue);
       }
    }

    private void registerListeners() {
        temporalColumnSelectionRange().rangeInstantsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                minText.setText(newValue.get(0).toString());
                maxText.setText(newValue.get(1).toString());
                relayout();
            }
        });
    }

    @Override
    public void relayout() {
        double topY = GraphicsUtil.mapValue(temporalColumnSelectionRange().getEndInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());
        double bottomY = GraphicsUtil.mapValue(temporalColumnSelectionRange().getStartInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getStartInstant(),
                ((TemporalColumn)getPCPAxis().getColumn()).getStatistics().getEndInstant(),
                getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());
        layoutGraphics(bottomY, topY);
    }

    public void update(Instant minValue, Instant maxValue, double minValueY, double maxValueY) {
        temporalColumnSelectionRange().setEndInstant(maxValue);
        temporalColumnSelectionRange().setStartInstant(minValue);
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
