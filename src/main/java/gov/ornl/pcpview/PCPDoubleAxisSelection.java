package gov.ornl.pcpview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.DoubleColumn;
import gov.ornl.datatable.DoubleColumnSelectionRange;
import gov.ornl.util.GraphicsUtil;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.util.converter.NumberStringConverter;

import java.util.Optional;

public class PCPDoubleAxisSelection extends PCPAxisSelection {

    private Text minText;
    private Text maxText;
    private DoubleProperty draggingMinValue;
    private DoubleProperty draggingMaxValue;

    public PCPDoubleAxisSelection(PCPAxis pcpAxis, DoubleColumnSelectionRange selectionRange, double minValueY, double maxValueY, Pane pane, DataTable dataModel) {
        super(pcpAxis, selectionRange, minValueY, maxValueY, pane, dataModel);

        minText = new Text(String.valueOf(selectionRange.getMinValue()));
//        minText = new Text());
//        minText.textProperty().bindBidirectional(getColumnSelectionRange().minValueProperty(), new NumberStringConverter());
        minText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minText.setX(pcpAxis.getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));
        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());

        minText.setFill(DEFAULT_TEXT_FILL);
        minText.setVisible(false);

        maxText = new Text(String.valueOf(selectionRange.getMaxValue()));
//        maxText = new Text();
//        maxText.textProperty().bindBidirectional(getColumnSelectionRange().maxValueProperty(), new NumberStringConverter());
        maxText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxText.setX(pcpAxis.getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
        maxText.setFill(DEFAULT_TEXT_FILL);
        maxText.setVisible(false);

        getGraphicsGroup().getChildren().addAll(minText, maxText);

        registerListeners();
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
            draggingMinValue = new SimpleDoubleProperty(doubleColumnSelectionRange().getMinValue());
            draggingMaxValue = new SimpleDoubleProperty(doubleColumnSelectionRange().getMaxValue());
            minText.textProperty().bindBidirectional(draggingMinValue, new NumberStringConverter());
            maxText.textProperty().bindBidirectional(draggingMaxValue, new NumberStringConverter());
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
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue()));
        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, getPCPAxis().getFocusTopY(), getPCPAxis().getFocusBottomY(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue()));

        layoutGraphics(bottomY, topY);
    }

    @Override
    protected void handleRectangleMousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            Dialog dialog = createSelectionRangeInputDialog(doubleColumnSelectionRange().getMinValue(),
                    doubleColumnSelectionRange().getMaxValue());
            Optional<Pair<Double, Double>> result = dialog.showAndWait();

            result.ifPresent(newMinValue -> {
                double minValue = result.get().getKey();
                double maxValue = result.get().getValue();

                // ensure min is the min and max is the max
                minValue = Math.min(minValue, maxValue);
                maxValue = Math.max(minValue, maxValue);

                // clamp within the bounds of the focus range
                minValue = minValue < ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue() ? ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue() : minValue;
                maxValue = maxValue > ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue() ? ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue() : maxValue;

                // find the y positions for the min and max
                double topY = GraphicsUtil.mapValue(maxValue,
                        ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue(),
                        ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                        getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());
                double bottomY = GraphicsUtil.mapValue(minValue,
                        ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue(),
                        ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                        getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());

                // update display and data model values
                update(minValue, maxValue, bottomY, topY);
            });
        }
    }

    @Override
    protected void handleRectangleMouseReleased() {
        if (dragging) {
            dragging = false;

            doubleColumnSelectionRange().setRangeValues(draggingMinValue.get(), draggingMaxValue.get());

            // unbind selection range min/max labels from dragging min/max range values
            minText.textProperty().unbindBidirectional(draggingMinValue);
            maxText.textProperty().unbindBidirectional(draggingMaxValue);

        } else {
            getPane().getChildren().remove(getGraphicsGroup());
            getPCPAxis().getAxisSelectionList().remove(this);
            getDataModel().clearColumnSelectionRange(doubleColumnSelectionRange());
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

            // unbind range selection min labels from selection range min properties
//                    minText.textProperty().unbindBidirectional(getColumnSelectionRange().minValueProperty());

            // bind range selection min/max labels to local values during drag operation
            draggingMinValue = new SimpleDoubleProperty(doubleColumnSelectionRange().getMinValue());
            minText.textProperty().bindBidirectional(draggingMinValue, new NumberStringConverter());
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
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue()));
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
            doubleColumnSelectionRange().setMinValue(draggingMinValue.get());

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

            // unbind range selection max label from selection range max property
//                    maxText.textProperty().unbindBidirectional(getColumnSelectionRange().maxValueProperty());

            // bind range selection max labels to local value during drag operation
            draggingMaxValue = new SimpleDoubleProperty(doubleColumnSelectionRange().getMaxValue());
            maxText.textProperty().bindBidirectional(draggingMaxValue, new NumberStringConverter());
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
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue()));

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
            doubleColumnSelectionRange().setMaxValue(draggingMaxValue.get());

            // unbind selection range max label from dragging max range value
            maxText.textProperty().unbindBidirectional(draggingMaxValue);
        }
    }

    @Override
    protected void handleRectangleMouseEntered() {
        minText.setVisible(true);
        maxText.setVisible(true);
    }

    private void registerListeners() {
        doubleColumnSelectionRange().rangeValuesProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                minText.setText(newValue.get(0).toString());
                maxText.setText(newValue.get(1).toString());
                relayout();
            }
        });
    }

    private DoubleColumnSelectionRange doubleColumnSelectionRange() {
        return (DoubleColumnSelectionRange)getColumnSelectionRange();
    }

    private PCPDoubleAxis doubleAxis() {
        return (PCPDoubleAxis)getPCPAxis();
    }

    protected void layoutGraphics(double bottomY, double topY) {
        super.layoutGraphics(bottomY, topY);

        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());
        minText.setX(getPCPAxis().getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));

        maxText.setX(getPCPAxis().getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
    }

    @Override
    public void relayout() {
        double topY = GraphicsUtil.mapValue(doubleColumnSelectionRange().getMaxValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());
        double bottomY = GraphicsUtil.mapValue(doubleColumnSelectionRange().getMinValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMinValue(),
                ((DoubleColumn)getPCPAxis().getColumn()).getStatistics().getMaxValue(),
                getPCPAxis().getFocusBottomY(), getPCPAxis().getFocusTopY());
        layoutGraphics(bottomY, topY);
    }

    protected void update(double minValue, double maxValue, double minValueY, double maxValueY) {
        doubleColumnSelectionRange().setMaxValue(maxValue);
        doubleColumnSelectionRange().setMinValue(minValue);
        layoutGraphics(minValueY, maxValueY);
    }

    private Dialog<Pair<Double, Double>> createSelectionRangeInputDialog (double minValue, double maxValue) {
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
