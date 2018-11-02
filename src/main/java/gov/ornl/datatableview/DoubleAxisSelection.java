package gov.ornl.datatableview;

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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import javafx.util.converter.NumberStringConverter;

import java.util.Optional;

public class DoubleAxisSelection extends UnivariateAxisSelection {

    private Text minText;
    private Text maxText;
    private DoubleProperty draggingMinValue;
    private DoubleProperty draggingMaxValue;

    public DoubleAxisSelection(DoubleAxis doubleAxis, DoubleColumnSelectionRange selectionRange, double minValueY, double maxValueY) {
        super(doubleAxis, selectionRange, minValueY, maxValueY);

        minText = new Text(String.valueOf(selectionRange.getMinValue()));
//        minText = new Text());
//        minText.textProperty().bindBidirectional(getColumnSelectionRange().minValueProperty(), new NumberStringConverter());
        minText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minText.setX(doubleAxis.getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));
        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());

        minText.setFill(DEFAULT_TEXT_FILL);
        minText.setVisible(false);

        maxText = new Text(String.valueOf(selectionRange.getMaxValue()));
//        maxText = new Text();
//        maxText.textProperty().bindBidirectional(getColumnSelectionRange().maxValueProperty(), new NumberStringConverter());
        maxText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxText.setX(doubleAxis.getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
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
            draggingMinValue = new SimpleDoubleProperty(doubleColumnSelection().getMinValue());
            draggingMaxValue = new SimpleDoubleProperty(doubleColumnSelection().getMaxValue());
            minText.textProperty().bindBidirectional(draggingMinValue, new NumberStringConverter());
            maxText.textProperty().bindBidirectional(draggingMaxValue, new NumberStringConverter());
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
                doubleAxis().getMaxFocusValue(), doubleAxis().getMinFocusValue()));
        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                doubleAxis().getMaxFocusValue(), doubleAxis().getMinFocusValue()));
//        draggingMaxValue.set(GraphicsUtil.mapValue(topY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue()));
//        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue()));

        layoutGraphics(bottomY, topY);
    }

    @Override
    protected void handleRectangleMousePressed(MouseEvent event) {
        if (event.isPopupTrigger()) {
            Dialog dialog = createSelectionRangeInputDialog(doubleColumnSelection().getMinValue(),
                    doubleColumnSelection().getMaxValue());
            Optional<Pair<Double, Double>> result = dialog.showAndWait();

            result.ifPresent(newMinValue -> {
                double minValue = result.get().getKey();
                double maxValue = result.get().getValue();

                // ensure min is the min and max is the max
                minValue = Math.min(minValue, maxValue);
                maxValue = Math.max(minValue, maxValue);

                // clamp within the bounds of the focus range
                minValue = minValue < doubleAxis().getMinFocusValue() ? doubleAxis().getMinFocusValue() : minValue;
                maxValue = maxValue > doubleAxis().getMaxFocusValue() ? doubleAxis().getMaxFocusValue() : maxValue;
//                minValue = minValue < ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue() ? ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue() : minValue;
//                maxValue = maxValue > ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue() ? ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue() : maxValue;

                // find the y positions for the min and max
                double topY = GraphicsUtil.mapValue(maxValue, doubleAxis().getMinFocusValue(), doubleAxis().getMaxFocusValue(),
                        univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
                double bottomY = GraphicsUtil.mapValue(minValue, doubleAxis().getMinFocusValue(), doubleAxis().getMaxFocusValue(),
                        univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
//                double topY = GraphicsUtil.mapValue(maxValue,
//                        ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue(),
//                        ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                        univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
//                double bottomY = GraphicsUtil.mapValue(minValue,
//                        ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue(),
//                        ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                        univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());

                // update display and data model values
                update(minValue, maxValue, bottomY, topY);
            });
        }
    }

    @Override
    protected void handleRectangleMouseReleased() {
        if (dragging) {
            dragging = false;

            doubleColumnSelection().setRangeValues(draggingMinValue.get(), draggingMaxValue.get());

            // unbind selection range min/max labels from dragging min/max range values
            minText.textProperty().unbindBidirectional(draggingMinValue);
            maxText.textProperty().unbindBidirectional(draggingMaxValue);

        } else {
//            getPane().getChildren().remove(getGraphicsGroup());
//            getPCPAxis().getAxisSelectionList().remove(this);
            getAxis().getDataTable().removeColumnSelectionFromActiveQuery(doubleColumnSelection());
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
    protected void handleBottomCrossbarMousePressed() {

    }

    @Override
    protected void handleBottomCrossbarMouseReleased() {
        if (dragging) {
            dragging = false;

            // update column selection range min properties
            doubleColumnSelection().setMinValue(draggingMinValue.get());

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
    protected void handleBottomCrossbarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;

            // unbind range selection min labels from selection range min properties
//                    minText.textProperty().unbindBidirectional(getColumnSelectionRange().minValueProperty());

            // bind range selection min/max labels to local values during drag operation
            draggingMinValue = new SimpleDoubleProperty(doubleColumnSelection().getMinValue());
            minText.textProperty().bindBidirectional(draggingMinValue, new NumberStringConverter());
        }

        double deltaY = event.getY() - dragEndPoint.getY();
        dragEndPoint = new Point2D(event.getX(), event.getY());

        double bottomY = getBottomY() + deltaY;

        if (bottomY > univariateAxis().getFocusMinPosition()) {
            bottomY = univariateAxis().getFocusMaxPosition();
        }

        if (bottomY < getTopY()) {
            bottomY = getTopY();
        }

        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
                doubleAxis().getMaxFocusValue(), doubleAxis().getMinFocusValue()));
//        draggingMinValue.set(GraphicsUtil.mapValue(bottomY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue()));
        layoutGraphics(bottomY, getTopY());
    }

    @Override
    protected void handleTopCrossbarMouseDragged(MouseEvent event) {
        if (!dragging) {
            dragging = true;

            // unbind range selection max label from selection range max property
//                    maxText.textProperty().unbindBidirectional(getColumnSelectionRange().maxValueProperty());

            // bind range selection max labels to local value during drag operation
            draggingMaxValue = new SimpleDoubleProperty(doubleColumnSelection().getMaxValue());
            maxText.textProperty().bindBidirectional(draggingMaxValue, new NumberStringConverter());
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
                doubleAxis().getMaxFocusValue(), doubleAxis().getMinFocusValue()));
//        draggingMaxValue.set(GraphicsUtil.mapValue(topY, univariateAxis().getFocusMaxPosition(), univariateAxis().getFocusMinPosition(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue()));

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
            doubleColumnSelection().setMaxValue(draggingMaxValue.get());

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
        doubleColumnSelection().rangeValuesProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                minText.setText(newValue.get(0).toString());
                maxText.setText(newValue.get(1).toString());
                resize();
            }
        });
    }

    private DoubleColumnSelectionRange doubleColumnSelection() {
        return (DoubleColumnSelectionRange)getColumnSelection();
    }

    private DoubleAxis doubleAxis() {
        return (DoubleAxis)univariateAxis();
    }

    protected void layoutGraphics(double bottomY, double topY) {
        super.layoutGraphics(bottomY, topY);

        minText.setY(getBottomY() + minText.getLayoutBounds().getHeight());
        minText.setX(univariateAxis().getCenterX() - (minText.getLayoutBounds().getWidth() / 2d));

        maxText.setX(univariateAxis().getCenterX() - (maxText.getLayoutBounds().getWidth() / 2d));
        maxText.setY(getTopY() - 2d);
    }

    @Override
    public void resize() {
        double topY = GraphicsUtil.mapValue(doubleColumnSelection().getMaxValue(),
                doubleAxis().getMinFocusValue(), doubleAxis().getMaxFocusValue(),
                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
        double bottomY = GraphicsUtil.mapValue(doubleColumnSelection().getMinValue(),
                doubleAxis().getMinFocusValue(), doubleAxis().getMaxFocusValue(),
                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
//        double topY = GraphicsUtil.mapValue(doubleColumnSelection().getMaxValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
//        double bottomY = GraphicsUtil.mapValue(doubleColumnSelection().getMinValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMinValue(),
//                ((DoubleColumn)univariateAxis().getColumn()).getStatistics().getMaxValue(),
//                univariateAxis().getFocusMinPosition(), univariateAxis().getFocusMaxPosition());
        layoutGraphics(bottomY, topY);
    }

    protected void update(double minValue, double maxValue, double minValueY, double maxValueY) {
        doubleColumnSelection().setMaxValue(maxValue);
        doubleColumnSelection().setMinValue(minValue);
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
