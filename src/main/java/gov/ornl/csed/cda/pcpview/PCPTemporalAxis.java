package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.ColumnSelectionRange;
import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.TemporalColumn;
import gov.ornl.csed.cda.datatable.TemporalColumnSelectionRange;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class PCPTemporalAxis extends PCPAxis {
    public final static Logger log = LoggerFactory.getLogger(PCPTemporalAxis.class);

    public final static Color DEFAULT_LABEL_COLOR = Color.BLACK;
    public final static double DEFAULT_NAME_LABEL_HEIGHT = 30d;
    public final static double DEFAULT_NAME_TEXT_SIZE = 12d;
    public final static double DEFAULT_CONTEXT_HEIGHT = 20d;
    public final static double DEFAULT_BAR_WIDTH = 10d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    
    public final static double DEFAULT_OVERVIEW_QUERY_BAR_SPACING = 30d;

    private double barTopY;
    private double barBottomY;
    private double focusTopY;
    private double focusBottomY;

    private double contextRegionHeight = DEFAULT_CONTEXT_HEIGHT;

    private Rectangle axisBar;
    private Line topCrossBarLine;
    private Line bottomCrossBarLine;
    private Line topFocusCrossBarLine;
    private Line bottomFocusCrossBarLine;

    private Text startInstantText;
    private Text endInstantText;

    private ArrayList<PCPTemporalAxisSelection> axisSelections = new ArrayList<>();

    // dragging variables
    private javafx.geometry.Point2D dragStartPoint;
    private javafx.geometry.Point2D dragEndPoint;
    private PCPTemporalAxisSelection draggingSelection;
    private boolean dragging = false;


    public PCPTemporalAxis(PCPView pcpView, TemporalColumn column, DataModel dataModel, Pane pane) {
        super(pcpView, column, dataModel, pane);

        this.pcpView = pcpView;
        this.column = column;
        this.dataModel = dataModel;
        this.pane = pane;

        barTopY = 0d;
        barBottomY = 0d;
        focusTopY = 0d;
        focusBottomY = 0d;

        centerX = 0d;

        startInstantText = new Text();
        startInstantText.textProperty().bindBidirectional(temporalColumn().startLocalDateTimeProperty(),
                new LocalDateTimeStringConverter(FormatStyle.SHORT, FormatStyle.MEDIUM));
        startInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        startInstantText.setSmooth(true);

        endInstantText = new Text();
        endInstantText.textProperty().bindBidirectional(column.endLocalDateTimeProperty(),
                new LocalDateTimeStringConverter(FormatStyle.SHORT, FormatStyle.MEDIUM));
        endInstantText.setFont(new Font(DEFAULT_TEXT_SIZE));
        endInstantText.setSmooth(true);

        axisBar = new Rectangle();
        axisBar.setStroke(Color.DARKGRAY);
        axisBar.setFill(Color.WHITESMOKE);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        topCrossBarLine = makeLine();
        bottomCrossBarLine = makeLine();
        topFocusCrossBarLine = makeLine();
        bottomFocusCrossBarLine = makeLine();

        graphicsGroup.getChildren().addAll(axisBar, topCrossBarLine, bottomCrossBarLine, topFocusCrossBarLine,
                bottomFocusCrossBarLine, startInstantText, endInstantText);

        registerListeners();
    }

    public ArrayList<PCPTemporalAxisSelection> getAxisSelections() {
        return axisSelections;
    }

    public double getFocusTopY() { return focusTopY; }
    public double getFocusBottomY() { return focusBottomY; }
    public double getUpperContextTopY() { return barTopY; }
    public double getUpperContextBottomY() { return focusTopY; }
    public double getLowerContextTopY() { return focusBottomY; }
    public double getLowerContextBottomY() { return barBottomY; }

    public double getVerticalBarTop() { return barTopY; }
    public double getVerticalBarBottom() { return barBottomY; }
//    public double getBarTopY () {
//        return barTopY;
//    }
//
//    public double getBarBottomY() {
//        return barBottomY;
//    }

    public double getBarLeftX() { return axisBar.getX(); }

    public double getBarRightX() { return axisBar.getX() + axisBar.getWidth(); }

    private TemporalColumn temporalColumn() {
        return (TemporalColumn)column;
    }

    protected Rectangle getAxisBar() {
        return axisBar;
    }

    private void registerListeners() {
        PCPTemporalAxis thisPCPAxis = this;

        nameText.textProperty().addListener((observable -> {
            nameText.setX(bounds.getX() + ((bounds.getWidth() - nameText.getLayoutBounds().getWidth()) / 2.));
            nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
        }));

        axisBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dragStartPoint = new Point2D(event.getX(), event.getY());
                dragEndPoint = new Point2D(event.getX(), event.getY());
            }
        });

        axisBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!dragging) {
                    dragging = true;
                }

                dragEndPoint = new Point2D(event.getX(), event.getY());

                double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
                double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

                selectionMaxY = selectionMaxY < getFocusTopY() ? getFocusTopY() : selectionMaxY;
                selectionMinY = selectionMinY > getFocusBottomY() ? getFocusBottomY() : selectionMinY;

                if (selectionMaxY == getFocusTopY()) {
                    log.debug("selectionMaxY = " + selectionMaxY + " getBarTopY() = " + getFocusTopY());
                }

//                Instant start = Instant.now().truncatedTo(ChronoUnit.MINUTES);
//                Instant end = start.plusSeconds(60);
//                Instant testInstant = GraphicsUtil.mapValue(getBarBottomY(), getBarTopY(), getBarBottomY(),
//                        temporalColumn().getEndInstant(), temporalColumn().getStartInstant());

                Instant selectionEndInstant = GraphicsUtil.mapValue(selectionMaxY, getFocusTopY(), getFocusBottomY(),
                        temporalColumn().getEndInstant(), temporalColumn().getStartInstant());
                Instant selectionStartInstant = GraphicsUtil.mapValue(selectionMinY, getFocusTopY(), getFocusBottomY(),
                        temporalColumn().getEndInstant(), temporalColumn().getStartInstant());

                log.debug("selectionMaxY: " + selectionMaxY + "  selectionEndInstant: " + selectionEndInstant);
                log.debug("selectionMinY: " + selectionMinY + "  selectionStartInstant: " + selectionStartInstant);


                if (draggingSelection == null) {
//                    ColumnSelectionRange selectionRange = dataModel.addColumnSelectionRangeToActiveQuery(column, minSelectionValue, maxSelectionValue);
                    TemporalColumnSelectionRange selectionRange = new TemporalColumnSelectionRange(temporalColumn(), selectionStartInstant, selectionEndInstant);
                    draggingSelection = new PCPTemporalAxisSelection(thisPCPAxis, selectionRange, selectionMinY, selectionMaxY, pane, dataModel);
                } else {
                    draggingSelection.update(selectionStartInstant, selectionEndInstant, selectionMinY, selectionMaxY);
                }

            }
        });

        axisBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (draggingSelection != null) {
                    axisSelections.add(draggingSelection);
                    dataModel.addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelectionRange());
                    dragging = false;
                    draggingSelection = null;
//                    dataModel.setQueriedTuples();
                }
            }
        });
    }

    @Override
    public void layout(double centerX, double topY, double width, double height) {
        this.centerX = centerX;
        double left = centerX - (width / 2d);
        bounds = new Rectangle(left, topY, width, height);
        barTopY = topY + DEFAULT_NAME_LABEL_HEIGHT;
        barBottomY = bounds.getY() + bounds.getHeight() - endInstantText.getLayoutBounds().getHeight();
        focusTopY = topY + DEFAULT_NAME_LABEL_HEIGHT + contextRegionHeight;
        focusBottomY = barBottomY - contextRegionHeight;

        axisBar.setX(centerX - (DEFAULT_BAR_WIDTH / 2d));
        axisBar.setY(barTopY);
        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setHeight(barBottomY - barTopY);

        topCrossBarLine.setStartY(barTopY);
        topCrossBarLine.setEndY(barTopY);
        topCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        topCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        bottomCrossBarLine.setStartY(barBottomY);
        bottomCrossBarLine.setEndY(barBottomY);
        bottomCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        bottomCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        topFocusCrossBarLine.setStartY(focusTopY);
        topFocusCrossBarLine.setEndY(focusTopY);
        topFocusCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        topFocusCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        bottomFocusCrossBarLine.setStartY(focusBottomY);
        bottomFocusCrossBarLine.setEndY(focusBottomY);
        bottomFocusCrossBarLine.setStartX(centerX - (DEFAULT_BAR_WIDTH / 2.));
        bottomFocusCrossBarLine.setEndX(centerX + (DEFAULT_BAR_WIDTH / 2.));

        nameText.setText(column.getName());
        if (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (nameText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                nameText.setText(nameText.getText().substring(0, nameText.getText().length() - 1));
            }
        }

//        nameText.setFont(new Font(DEFAULT_TEXT_SIZE));
//        adjustTextSize(nameText, width, DEFAULT_TEXT_SIZE);

        nameText.setX(bounds.getX() + ((width - nameText.getLayoutBounds().getWidth()) / 2.));
        nameText.setY(bounds.getY() + nameText.getLayoutBounds().getHeight());
        nameText.setRotate(getNameTextRotation());
//        nameText.setY(barTopY - (DEFAULT_NAME_LABEL_HEIGHT / 2.));
//        nameText.setRotate(-10.);

//        overviewStartInstantText.setX(bounds.getX() + ((width - overviewStartInstantText.getLayoutBounds().getWidth()) / 2.));
        startInstantText.setX(getCenterX() - (startInstantText.getLayoutBounds().getWidth() / 2.));
//        startInstantText.setX(axisBar.getLayoutBounds().getMaxX() - startInstantText.getLayoutBounds().getWidth());
        startInstantText.setY(barBottomY + startInstantText.getLayoutBounds().getHeight());

//        overviewEndInstantText.setX(bounds.getX() + ((width - overviewEndInstantText.getLayoutBounds().getWidth()) / 2.));
        endInstantText.setX(axisBar.getLayoutBounds().getMaxX() - endInstantText.getLayoutBounds().getWidth());
        endInstantText.setX(getCenterX() - (endInstantText.getLayoutBounds().getWidth() / 2.));
        endInstantText.setY(barTopY - 4d);

        if (!axisSelections.isEmpty()) {
            for (PCPTemporalAxisSelection axisSelection : axisSelections) {
                axisSelection.relayout();
            }
        }


    }
}
