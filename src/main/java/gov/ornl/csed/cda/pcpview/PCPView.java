package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.*;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by csg on 8/22/16.
 */
public class PCPView extends Region {
    private final Logger log = LoggerFactory.getLogger(PCPView.class);

    private final static Paint DEFAULT_SELECTED_LINE_PAINT = Color.STEELBLUE;
    private final static Paint DEFAULT_UNSELECTED_LINE_PAINT = Color.LIGHTGRAY;
    private final static double DEFAULT_LINE_OPACITY = 0.1;

    private Canvas lineCanvas;
    private GraphicsContext lineGC;

    private Paint backgroundPaint;
    private Paint borderPaint;
    private double borderWidth;

    private Paint selectedLinePaint = DEFAULT_SELECTED_LINE_PAINT;
    private Paint unselectedLinePaint = DEFAULT_UNSELECTED_LINE_PAINT;
    private double lineOpacity = DEFAULT_LINE_OPACITY;

    private Pane pane;
    private double axisSpacing = 40d;

    private DataModel dataModel;

    private ArrayList<PCPAxis> axisList;
    private ArrayList<PCPTuple> tupleList;
    private HashSet<PCPTuple> unselectedTupleSet;
    private HashSet<PCPTuple> selectedTupleSet;

    public PCPView() {
        backgroundPaint = Color.TRANSPARENT;
        borderPaint = Color.TRANSPARENT;
        borderWidth = 0d;

        init();
        initGraphics();
        registerListeners();
    }

    public void setSelectedLinePaint(Paint selectedLinePaint) {
        this.selectedLinePaint = selectedLinePaint;
//        recolorLines();
        redraw();
    }

    public Paint getSelectedLinePaint() { return selectedLinePaint; }
    public Paint getUnselectedLinePaint() { return unselectedLinePaint; }

    public void setUnselectedLinePaint(Paint unselectedLinePaint) {
        this.unselectedLinePaint = unselectedLinePaint;
//        recolorLines();
        redraw();
    }

    public void handleQueryChange() {
        unselectedTupleSet.clear();
        selectedTupleSet.clear();
        if ((tupleList != null) && (!tupleList.isEmpty())) {
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                for (PCPTuple pcpTuple : tupleList) {
                    if (pcpTuple.getTuple().getQueryFlag()) {
                        selectedTupleSet.add(pcpTuple);
                    } else {
                        unselectedTupleSet.add(pcpTuple);
                    }
                }
            } else {
                selectedTupleSet.addAll(tupleList);
            }
        }
        redraw();
    }

//    private void recolorLines() {
//        if ((tupleList != null) && (!tupleList.isEmpty())) {
//            for (PCPTuple pcpTuple : tupleList) {
//                if (pcpTuple.getTuple().getQueryFlag()) {
//                    pcpTuple.setStroke(selectedLinePaint);
//                } else {
//                    pcpTuple.setStroke(unselectedLinePaint);
//                }
//            }
//        }
//    }

    public void setAxisSpacing(double axisSpacing) {
        this.axisSpacing = axisSpacing;
        resize();
    }

    public void setDataModel (DataModel dataModel) {
        this.dataModel = dataModel;

        // add axis lines to the pane
        axisList.clear();
        for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
            PCPAxis pcpAxis = new PCPAxis(dataModel.getColumn(iaxis), iaxis, dataModel, pane);
            pane.getChildren().add(pcpAxis.getGraphicsGroup());
            axisList.add(pcpAxis);
        }

        // add tuples polylines from data model
        tupleList = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
            Tuple tuple = dataModel.getTuple(iTuple);
            PCPTuple pcpTuple = new PCPTuple(tuple);
            tupleList.add(pcpTuple);
        }

        // create selected and unselected tuple sets
        unselectedTupleSet = new HashSet<>();
        selectedTupleSet = new HashSet<>(tupleList);

        resize();

        this.dataModel.addDataModelListener(new DataModelListener() {
            @Override
            public void dataModelChanged(DataModel dataModel) {

            }

            @Override
            public void queryChanged(DataModel dataModel) {
                log.debug("Query Changed");
                handleQueryChange();
            }

            @Override
            public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                log.debug("ColumnSelection added");
                handleQueryChange();
            }

            @Override
            public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                log.debug("columnSelectionRange removed");
                handleQueryChange();
            }

            @Override
            public void highlightedColumnChanged(DataModel dataModel) {

            }

            @Override
            public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {

            }

            @Override
            public void columnDisabled(DataModel dataModel, Column disabledColumn) {

            }

            @Override
            public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

            }

            @Override
            public void columnEnabled(DataModel dataModel, Column enabledColumn) {

            }
        });
    }

    private void init() {
        axisList = new ArrayList<>();
    }

    private void initGraphics() {
        lineCanvas = new Canvas(getWidth(), getHeight());
        lineGC = lineCanvas.getGraphicsContext2D();

        pane = new Pane(lineCanvas);
//        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
//        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        getChildren().setAll(pane);
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());

        lineCanvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Clicked in the polyline canvas");
            }
        });
    }

    private void resize() {
        if (dataModel != null && !dataModel.isEmpty()) {
            double pcpWidth = axisSpacing * dataModel.getColumnCount();
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());
            double width = (getInsets().getLeft() + getInsets().getRight()) + pcpWidth;

            if (pcpWidth > 0 && pcpHeight > 0) {
                pane.setPrefSize(width, getHeight());
                pane.setMinWidth(width);

                lineCanvas.setWidth(width);
                lineCanvas.setHeight(getHeight());

                if (axisList != null) {
                    double left = getInsets().getLeft() + (axisSpacing / 2.);
                    double top = getInsets().getTop();

                    for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                        PCPAxis pcpAxis = axisList.get(iaxis);
                        pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);
                    }

                    // add tuples polylines from data model
                    for (PCPTuple pcpTuple : tupleList) {
                        pcpTuple.layout(axisList);
                    }
                }
                redraw();
            }
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        drawTuplePolylines();
//        drawPolylines();
//        drawAxes();
    }

    private void drawTuplePolylines() {
        lineCanvas.setCache(false);
        lineGC.setLineCap(StrokeLineCap.BUTT);
        lineGC.clearRect(0, 0, getWidth(), getHeight());
        lineGC.setGlobalAlpha(lineOpacity);
//        lineGC.setStroke(new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.2));

        if (!unselectedTupleSet.isEmpty()) {
            lineGC.setStroke(unselectedLinePaint);
            for (PCPTuple pcpTuple : unselectedTupleSet) {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }

        if (!selectedTupleSet.isEmpty()) {
            lineGC.setStroke(selectedLinePaint);
            for (PCPTuple pcpTuple : selectedTupleSet) {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }
//        if (tupleList != null && !tupleList.isEmpty()) {
//            for (PCPTuple pcpTuple : tupleList) {
//                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
//                    lineGC.setStroke(pcpTuple.getColor());
//                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
//                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
////                    lineGC.strokeLine(pcpTuple.getXPoints()[i-1], pcpTuple.getYPoints()[i-1], pcpTuple.getXPoints()[i], pcpTuple.getYPoints()[i]);
//                }
//            }
//        }

        lineCanvas.setCache(true);
        lineCanvas.setCacheHint(CacheHint.QUALITY);
    }

/*
    private void drawPolylines() {
        double availableHeight = getHeight() * .8;
        double axisSpacing = getWidth() / dataModel.getColumnCount();
        double left = axisSpacing / 2.;
        double right = getWidth() - left;
        double top = (getHeight() - availableHeight) / 2.;
        double bottom = top + availableHeight;

        lineCanvas.setCache(false);
        lineGC.setLineCap(StrokeLineCap.BUTT);
        lineGC.clearRect(0, 0, getWidth(), getHeight());
        lineGC.setGlobalAlpha(0.5);

        if (dataModel != null && !dataModel.isEmpty()) {

            lineGC.setStroke(Color.STEELBLUE);
            for (int ituple = 0; ituple < dataModel.getTupleCount(); ituple++) {
                Tuple tuple = dataModel.getTuple(ituple);
                double lastX = 0.;
                double lastY = 0.;
                for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
                    double value = tuple.getElement(iaxis);
                    Column column = dataModel.getColumn(iaxis);
                    double x = left + (iaxis * axisSpacing);
                    double y = GraphicsUtil.mapValue(value, column.getSummaryStats().getMin(), column.getSummaryStats().getMax(),
                            bottom, top);

                    if (iaxis > 0) {
                        lineGC.strokeLine(lastX, lastY, x, y);
                    }
                    lastX = x;
                    lastY = y;
                }
            }
        }

        lineCanvas.setCache(true);
        lineCanvas.setCacheHint(CacheHint.QUALITY);
    }
    */

}
