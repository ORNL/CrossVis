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
    private final static double DEFAULT_LINE_OPACITY = 0.5;

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
    private ArrayList<PCPBinSet> PCPBinSetList;

//    private DISPLAY_MODE displayMode = DISPLAY_MODE.PCP_BINS;
    private DISPLAY_MODE displayMode = DISPLAY_MODE.HISTOGRAM;
//    private DISPLAY_MODE displayMode = DISPLAY_MODE.PCP_LINES;

    private boolean fitAxisSpacingToWidthEnabled = true;

    public enum DISPLAY_MODE {HISTOGRAM, PCP_LINES, PCP_BINS};


    public PCPView() {
        backgroundPaint = Color.TRANSPARENT;
        borderPaint = Color.TRANSPARENT;
        borderWidth = 0d;

        init();
        initGraphics();
        registerListeners();
    }

    public void setDisplayMode(DISPLAY_MODE displayMode) {
        if (this.displayMode != displayMode) {
            if ((this.displayMode == DISPLAY_MODE.PCP_LINES) ||
                    (this.displayMode == DISPLAY_MODE.PCP_BINS)){
                lineGC.clearRect(0, 0, getWidth(), getHeight());
            }
            this.displayMode = displayMode;
            resize();
        }
    }

    public DISPLAY_MODE getDisplayMode() { return displayMode; }

    public void setSelectedLinePaint(Paint selectedLinePaint) {
        this.selectedLinePaint = selectedLinePaint;
        redraw();
    }

    public Paint getSelectedLinePaint() { return selectedLinePaint; }
    public Paint getUnselectedLinePaint() { return unselectedLinePaint; }

    public void setUnselectedLinePaint(Paint unselectedLinePaint) {
        this.unselectedLinePaint = unselectedLinePaint;
        redraw();
    }

    public void handleQueryChange() {
        if (displayMode == DISPLAY_MODE.PCP_LINES) {
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
        } else if (displayMode == DISPLAY_MODE.HISTOGRAM) {
            double left = getInsets().getLeft() + (axisSpacing / 2.);
            double top = getInsets().getTop();
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

            for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                PCPAxis pcpAxis = axisList.get(iaxis);
                pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);

                pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
            }
        } else if (displayMode == DISPLAY_MODE.PCP_BINS) {
            double left = getInsets().getLeft() + (axisSpacing / 2.);
            double top = getInsets().getTop();
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

            for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                PCPAxis pcpAxis = axisList.get(iaxis);
                pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);
            }

            for (PCPBinSet PCPBinSet : PCPBinSetList) {
                PCPBinSet.layoutBins();
            }
            redraw();
        }
    }

    public void setAxisSpacing(double axisSpacing) {
        this.axisSpacing = axisSpacing;
        resize();
    }

    private void initializeLayout() {
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

        // create PCPBinSets for axis configuration
        PCPBinSetList = new ArrayList<>();
        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
            PCPBinSetList.add(binSet);
        }

        resize();
    }

    public void setDataModel (DataModel dataModel) {
        this.dataModel = dataModel;

//        // add axis lines to the pane
//        axisList.clear();
//        for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
//            PCPAxis pcpAxis = new PCPAxis(dataModel.getColumn(iaxis), iaxis, dataModel, pane);
//            pane.getChildren().add(pcpAxis.getGraphicsGroup());
//            axisList.add(pcpAxis);
//        }
//
//        // add tuples polylines from data model
//        tupleList = new ArrayList<>();
//        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//            Tuple tuple = dataModel.getTuple(iTuple);
//            PCPTuple pcpTuple = new PCPTuple(tuple);
//            tupleList.add(pcpTuple);
//        }
//
//        // create selected and unselected tuple sets
//        unselectedTupleSet = new HashSet<>();
//        selectedTupleSet = new HashSet<>(tupleList);

        resize();

        this.dataModel.addDataModelListener(new DataModelListener() {
            @Override
            public void dataModelChanged(DataModel dataModel) {
                initializeLayout();
            }

            @Override
            public void queryChanged(DataModel dataModel) {
                log.debug("Query Changed");
                handleQueryChange();
            }

            @Override
            public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                log.debug("ColumnSelection added");
//                handleQueryChange();
            }

            @Override
            public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
                log.debug("columnSelectionRange removed");
                handleQueryChange();
            }

            @Override
            public void highlightedColumnChanged(DataModel dataModel) {
                log.debug("highlighted column changed");
            }

            @Override
            public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
                log.debug("data model tuples added");
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

        getChildren().add(pane);
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

    public void setFitAxisSpacingToWidthEnabled (boolean enabled) {
        fitAxisSpacingToWidthEnabled = enabled;
    }

    public int getAxisSpacing () {return (int)axisSpacing;}

    public boolean getFitAxisSpacingToWidthEnabled() { return fitAxisSpacingToWidthEnabled; }

    private void resize() {
        if (dataModel != null && !dataModel.isEmpty()) {
            double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());
            double pcpWidth;
            double width;

            if (fitAxisSpacingToWidthEnabled) {
                width = getWidth();
                pcpWidth = width - (getInsets().getLeft() + getInsets().getRight());
                axisSpacing = pcpWidth / dataModel.getColumnCount();
            } else {
                pcpWidth = axisSpacing * dataModel.getColumnCount();
                width = (getInsets().getLeft() + getInsets().getRight()) + pcpWidth;
            }

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

                        if (displayMode == DISPLAY_MODE.HISTOGRAM) {
                            pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                            pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
                        }
                    }

                    // add tuples polylines from data model
                    if (displayMode == DISPLAY_MODE.PCP_LINES) {
                        if (tupleList != null) {
                            for (PCPTuple pcpTuple : tupleList) {
                                pcpTuple.layout(axisList);
                            }
                        }
                    } else if (displayMode == DISPLAY_MODE.PCP_BINS) {
                        // layout PCPBins
                        for (PCPBinSet PCPBinSet : PCPBinSetList) {
                            PCPBinSet.layoutBins();
                        }
                    }
                }
                redraw();
            }
        }
    }

    private void redraw() {
        pane.setBackground(new Background(new BackgroundFill(backgroundPaint, new CornerRadii(1024), Insets.EMPTY)));
        pane.setBorder(new Border(new BorderStroke(borderPaint, BorderStrokeStyle.SOLID, new CornerRadii(1024), new BorderWidths(borderWidth))));

        if (displayMode == DISPLAY_MODE.PCP_LINES) {
            drawTuplePolylines();
        } else if (displayMode == DISPLAY_MODE.PCP_BINS) {
            drawPCPBins();
        }
    }

    private void drawPCPBins() {
        lineCanvas.setCache(false);
        lineGC.setLineCap(StrokeLineCap.BUTT);
        lineGC.clearRect(0, 0, getWidth(), getHeight());
        lineGC.setGlobalAlpha(lineOpacity);
        lineGC.setLineWidth(2);
        lineGC.setLineWidth(2d);

        if ((PCPBinSetList != null) && (!PCPBinSetList.isEmpty())) {
            for (PCPBinSet binSet : PCPBinSetList) {
                for (PCPBin bin : binSet.getBins()) {
                    lineGC.setFill(bin.fillColor);
                    double xValues[] = new double[] {bin.left, bin.right, bin.right, bin.left};
                    double yValues[] = new double[] {bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};
                    lineGC.fillPolygon(xValues, yValues, xValues.length);
                }
            }

            for (PCPBinSet binSet : PCPBinSetList) {
                for (PCPBin bin : binSet.getBins()) {
                    if (bin.queryCount > 0) {
                        lineGC.setFill(bin.queryFillColor);
                        double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                        double yValues[] = new double[]{bin.leftQueryTop, bin.rightQueryTop, bin.rightQueryBottom, bin.leftQueryBottom};
                        lineGC.fillPolygon(xValues, yValues, xValues.length);
                    }
                }
            }
        }

        lineCanvas.setCache(true);
        lineCanvas.setCacheHint(CacheHint.QUALITY);
    }

    private void drawTuplePolylines() {
        lineCanvas.setCache(false);
        lineGC.setLineCap(StrokeLineCap.BUTT);
        lineGC.clearRect(0, 0, getWidth(), getHeight());
        lineGC.setGlobalAlpha(lineOpacity);
        lineGC.setLineWidth(2);
        lineGC.setLineWidth(2d);

        if ((unselectedTupleSet != null) && (!unselectedTupleSet.isEmpty())) {
            lineGC.setStroke(unselectedLinePaint);
            for (PCPTuple pcpTuple : unselectedTupleSet) {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }

        if ((selectedTupleSet != null) && (!selectedTupleSet.isEmpty())) {
            lineGC.setStroke(selectedLinePaint);
            for (PCPTuple pcpTuple : selectedTupleSet) {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }

        lineCanvas.setCache(true);
        lineCanvas.setCacheHint(CacheHint.QUALITY);
    }
}
