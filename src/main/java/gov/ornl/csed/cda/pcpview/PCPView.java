package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
public class PCPView extends Region implements DataModelListener {
    private final Logger log = LoggerFactory.getLogger(PCPView.class);

    private final static double DEFAULT_LINE_OPACITY = 0.5;
    private final static Color DEFAULT_SELECTED_ITEMS_COLOR = new Color(Color.STEELBLUE.getRed(),
            Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), DEFAULT_LINE_OPACITY);
    private final static Color DEFAULT_UNSELECTED_ITEMS_COLOR = new Color(Color.LIGHTGRAY.getRed(),
            Color.LIGHTGRAY.getGreen(), Color.LIGHTGRAY.getBlue(), DEFAULT_LINE_OPACITY);
    private final static DISPLAY_MODE DEFAULT_DISPLAY_MODE = DISPLAY_MODE.HISTOGRAM;

    private Canvas lineCanvas;
    private GraphicsContext lineGC;

    private Paint backgroundPaint;
    private Paint borderPaint;
    private double borderWidth;

    private ObjectProperty<Color> selectedItemsColor;
    private ObjectProperty<Color> unselectedItemsColor;
    private ObjectProperty<Color> backgroundColor;
    private ObjectProperty<Color> labelsColor;

    private BooleanProperty showSelectedItems;
    private BooleanProperty showUnselectedItems;

//    private Paint selectedLinePaint = DEFAULT_SELECTED_LINE_PAINT;
//    private Paint unselectedLinePaint = DEFAULT_UNSELECTED_LINE_PAINT;
//    private double lineOpacity = DEFAULT_LINE_OPACITY;

    private Pane pane;
    private double axisSpacing = 40d;

    private DataModel dataModel;

    private ArrayList<PCPAxis> axisList;
    private ArrayList<PCPTuple> tupleList;
    private HashSet<PCPTuple> unselectedTupleSet;
    private HashSet<PCPTuple> selectedTupleSet;
    private ArrayList<PCPBinSet> PCPBinSetList;

    private ObjectProperty<DISPLAY_MODE> displayMode;
//    private DISPLAY_MODE displayMode = DISPLAY_MODE.PCP_BINS;
//    private DISPLAY_MODE displayMode = DISPLAY_MODE.HISTOGRAM;
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

    public ObjectProperty<DISPLAY_MODE> displayModeProperty() { return displayMode; }

    public final DISPLAY_MODE getDisplayMode() { return displayMode.get(); }

    public final void setDisplayMode(DISPLAY_MODE newDisplayMode) { displayMode.set(newDisplayMode); }
//        if (getDisplayMode() != displayMode) {
//            if ((this.displayMode == DISPLAY_MODE.PCP_LINES) ||
//                    (this.displayMode == DISPLAY_MODE.PCP_BINS)){
//                lineGC.clearRect(0, 0, getWidth(), getHeight());
//            }
//            this.displayMode = displayMode;
//
//            if (displayMode == DISPLAY_MODE.PCP_LINES) {
//                fillTupleSets();
//            }
//
//            resize();
//        }
//    }

    public final boolean isShowingSelectedItems() { return showSelectedItems.get(); }

    public final void setShowSelectedItems(boolean enabled) { showSelectedItems.set(enabled); }

    public BooleanProperty showSelectedItems() { return showSelectedItems; }

    public final boolean isShowingUnselectedItems() { return showUnselectedItems.get(); }

    public final void setShowUnselectedItems(boolean enabled) { showUnselectedItems.set(enabled); }

    public BooleanProperty showUnselectedItems() { return showUnselectedItems; }

//    public DISPLAY_MODE getDisplayMode() { return displayMode; }

    public final Color getSelectedItemsColor() { return selectedItemsColor.get(); }

    public final void setSelectedItemsColor(Color color) {
        selectedItemsColor.set(color);
    }

    public ObjectProperty<Color> selectedItemsColor() { return selectedItemsColor; }

    public final Color getUnselectedItemsColor() { return unselectedItemsColor.get(); }

    public final void setUnselectedItemsColor(Color color) {
        unselectedItemsColor.set(color);
    }

    public ObjectProperty<Color> unselectedItemsColor() { return unselectedItemsColor; }

//    public void setSelectedLinePaint(Paint selectedLinePaint) {
//        this.selectedLinePaint = selectedLinePaint;
//        redraw();
//    }
//
//    public Paint getSelectedLinePaint() { return selectedLinePaint; }
//    public Paint getUnselectedLinePaint() { return unselectedLinePaint; }
//
//    public void setUnselectedLinePaint(Paint unselectedLinePaint) {
//        this.unselectedLinePaint = unselectedLinePaint;
//        redraw();
//    }

    private void fillTupleSets() {
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
    }

    public void handleQueryChange() {
        // TODO: Improve efficiency by only laying out the objects that change (e.g., don't layout the axes, just bins, lines, or histograms)
        double left = getInsets().getLeft() + (axisSpacing / 2.);
        double top = getInsets().getTop();
        double pcpHeight = getHeight() - (getInsets().getTop() + getInsets().getBottom());

        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            PCPAxis pcpAxis = axisList.get(iaxis);
            pcpAxis.layout(left + (iaxis * axisSpacing), top, axisSpacing, pcpHeight);

            if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
                pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
            }
        }

        if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
            fillTupleSets();
            redraw();
        } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
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

    private void reinitializeLayout() {
        if (axisList.isEmpty()) {
            for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
                PCPAxis pcpAxis = new PCPAxis(this, dataModel.getColumn(iaxis), dataModel, pane);
                pane.getChildren().add(pcpAxis.getGraphicsGroup());
                axisList.add(pcpAxis);
            }
        } else {
            ArrayList<PCPAxis> newAxisList = new ArrayList<PCPAxis>();
            for (int iDstCol = 0; iDstCol < dataModel.getColumnCount(); iDstCol++) {
                Column column = dataModel.getColumn(iDstCol);
                for (int iSrcCol = 0; iSrcCol < axisList.size(); iSrcCol++) {
                    PCPAxis pcpAxis = axisList.get(iSrcCol);
                    if (pcpAxis.getColumn() == column) {
                        newAxisList.add(pcpAxis);

                        log.debug("Axis added to list: " + pcpAxis.getColumn().getName());
                        break;
                    }
                }
            }

            axisList = newAxisList;
        }

        // add tuples polylines from data model
        tupleList = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
            Tuple tuple = dataModel.getTuple(iTuple);
            PCPTuple pcpTuple = new PCPTuple(tuple);
            tupleList.add(pcpTuple);
        }

        fillTupleSets();

        // create PCPBinSets for axis configuration
        PCPBinSetList = new ArrayList<>();
        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
            PCPBinSetList.add(binSet);
        }

        resize();
    }

    private void initializeLayout() {
        pane.getChildren().clear();
        pane.getChildren().add(lineCanvas);

        // add axis lines to the pane
        axisList.clear();
        for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
            PCPAxis pcpAxis = new PCPAxis(this, dataModel.getColumn(iaxis), dataModel, pane);
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

    private void addAxis(Column column) {
        PCPAxis pcpAxis = new PCPAxis(this, column, dataModel, pane);
        pane.getChildren().add(pcpAxis.getGraphicsGroup());
        axisList.add(pcpAxis);
    }

    @Override
    public void dataModelReset(DataModel dataModel) {
        reinitializeLayout();
//        if (axisList.isEmpty()) {
//            for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
//                PCPAxis pcpAxis = new PCPAxis(this, dataModel.getColumn(iaxis), dataModel, pane);
//                pane.getChildren().add(pcpAxis.getGraphicsGroup());
//                axisList.add(pcpAxis);
//            }
//        } else {
//            ArrayList<PCPAxis> newAxisList = new ArrayList<PCPAxis>();
//            for (int iDstCol = 0; iDstCol < dataModel.getColumnCount(); iDstCol++) {
//                Column column = dataModel.getColumn(iDstCol);
//                for (int iSrcCol = 0; iSrcCol < axisList.size(); iSrcCol++) {
//                    PCPAxis pcpAxis = axisList.get(iSrcCol);
//                    if (pcpAxis.getColumn() == column) {
//                        newAxisList.add(pcpAxis);
//
//                        log.debug("Axis added to list: " + pcpAxis.getColumn().getName());
//                        break;
//                    }
//                }
//            }
//
//            axisList = newAxisList;
//        }
//
//
//        // add tuples polylines from data model
//        tupleList = new ArrayList<>();
//        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//            Tuple tuple = dataModel.getTuple(iTuple);
//            PCPTuple pcpTuple = new PCPTuple(tuple);
//            tupleList.add(pcpTuple);
//        }
//
//        fillTupleSets();
//
//        // create PCPBinSets for axis configuration
//        PCPBinSetList = new ArrayList<>();
//        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
//            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
//            PCPBinSetList.add(binSet);
//        }
//
//        resize();
    }

    @Override
    public void dataModelQueryCleared(DataModel dataModel) {
        handleQueryChange();
    }

    @Override
    public void dataModelQueryColumnCleared(DataModel dataModel, Column column) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionAdded(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionRemoved(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelColumnSelectionChanged(DataModel dataModel, ColumnSelectionRange columnSelectionRange) {
        handleQueryChange();
    }

    @Override
    public void dataModelHighlightedColumnChanged(DataModel dataModel, Column oldHighlightedColumn, Column newHighlightedColumn) {
        if (dataModel.getHighlightedColumn() == null) {
            for (PCPAxis pcpAxis : axisList) {
                pcpAxis.setHighlighted(false);
            }
        } else {
            for (PCPAxis pcpAxis : axisList) {
                if (pcpAxis.getColumn() == newHighlightedColumn) {
                    pcpAxis.setHighlighted(true);
                } else if (pcpAxis.getColumn() == oldHighlightedColumn) {
                    pcpAxis.setHighlighted(false);
                }
            }
        }
    }

    @Override
    public void dataModelTuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {

    }

    @Override
    public void dataModelTuplesRemoved(DataModel dataModel, int numTuplesRemoved) {

    }

    @Override
    public void dataModelColumnDisabled(DataModel dataModel, Column disabledColumn) {
        for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
            PCPAxis pcpAxis = axisList.get(iaxis);
            if (pcpAxis.getColumn() == disabledColumn) {
                axisList.remove(pcpAxis);

                // remove axis graphics from pane
                pane.getChildren().remove(pcpAxis.getGraphicsGroup());
                pane.getChildren().remove(pcpAxis.getHistogramBinRectangleGroup());
                pane.getChildren().remove(pcpAxis.getQueryHistogramBinRectangleGroup());

                // remove axis selection graphics
                if (!pcpAxis.getAxisSelectionList().isEmpty()) {
                    for (PCPAxisSelection axisSelection : pcpAxis.getAxisSelectionList()) {
                        pane.getChildren().remove(axisSelection.getGraphicsGroup());
                    }
                }

                // create PCPBinSets for axis configuration
                PCPBinSetList = new ArrayList<>();
                for (int i = 0; i < axisList.size()-1; i++) {
                    PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
                    binSet.layoutBins();
                    PCPBinSetList.add(binSet);
                }

                // add tuples polylines from data model
                tupleList = new ArrayList<>();
                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
                    Tuple tuple = dataModel.getTuple(iTuple);
                    PCPTuple pcpTuple = new PCPTuple(tuple);
                    tupleList.add(pcpTuple);
                }
                fillTupleSets();

                resize();
                break;
            }
        }
    }

    @Override
    public void dataModelColumnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

    }

    @Override
    public void dataModelColumnEnabled(DataModel dataModel, Column enabledColumn) {
        // add axis lines to the pane
        addAxis(enabledColumn);

        // add tuples polylines from data model
        tupleList = new ArrayList<>();
        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
            Tuple tuple = dataModel.getTuple(iTuple);
            PCPTuple pcpTuple = new PCPTuple(tuple);
            tupleList.add(pcpTuple);
        }

        fillTupleSets();

        // create PCPBinSets for axis configuration
        PCPBinSetList = new ArrayList<>();
        for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
            PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
            PCPBinSetList.add(binSet);
        }

        resize();
    }

    @Override
    public void dataModelColumnOrderChanged(DataModel dataModel) {
        reinitializeLayout();
    }

    @Override
    public void dataModelColumnNameChanged(DataModel dataModel, Column column) { }

    public void setDataModel (DataModel dataModel) {
        this.dataModel = dataModel;
        dataModel.addDataModelListener(this);
        resize();

/*
        this.dataModel = dataModel;
        final PCPView pcpView = this;
        resize();

        this.dataModel.addDataModelListener(new DataModelListener() {
            @Override
            public void dataModelChanged(DataModel dataModel) {
//                initializeLayout();
//                pane.getChildren().clear();
//                pane.getChildren().add(lineCanvas);

                // add axis lines to the pane
//                axisList.clear();


//                pane.getChildren().clear();
//                pane.getChildren().add(lineCanvas);
//
//                // add axis lines to the pane
//                axisList.clear();
//                for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
//                    PCPAxis pcpAxis = new PCPAxis(pcpView, dataModel.getColumn(iaxis), iaxis, dataModel, pane);
//                    pane.getChildren().add(pcpAxis.getGraphicsGroup());
//                    axisList.add(pcpAxis);
//                }



                if (axisList.isEmpty()) {
                    for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
                        PCPAxis pcpAxis = new PCPAxis(pcpView, dataModel.getColumn(iaxis), dataModel, pane);
                        pane.getChildren().add(pcpAxis.getGraphicsGroup());
                        axisList.add(pcpAxis);
                    }
                } else {
                    ArrayList<PCPAxis> newAxisList = new ArrayList<PCPAxis>();
                    for (int iDstCol = 0; iDstCol < dataModel.getColumnCount(); iDstCol++) {
                        Column column = dataModel.getColumn(iDstCol);
                        for (int iSrcCol = 0; iSrcCol < axisList.size(); iSrcCol++) {
                            PCPAxis pcpAxis = axisList.get(iSrcCol);
                            if (pcpAxis.getColumn() == column) {
                                newAxisList.add(pcpAxis);

                                log.debug("Axis added to list: " + pcpAxis.getColumn().getName());
                                break;
                            }
                        }
                    }

                    axisList = newAxisList;
                }


                // add tuples polylines from data model
                tupleList = new ArrayList<>();
                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
                    Tuple tuple = dataModel.getTuple(iTuple);
                    PCPTuple pcpTuple = new PCPTuple(tuple);
                    tupleList.add(pcpTuple);
                }

                fillTupleSets();

                // create PCPBinSets for axis configuration
                PCPBinSetList = new ArrayList<>();
                for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
                    PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
                    PCPBinSetList.add(binSet);
                }

                resize();

//                // create PCPBinSets for axis configuration
//                PCPBinSetList = new ArrayList<>();
//                for (int i = 0; i < axisList.size()-1; i++) {
//                    PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
//                    binSet.layoutBins();
//                    PCPBinSetList.add(binSet);
//                }
//
//                // add tuples polylines from data model
//                tupleList = new ArrayList<>();
//                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//                    Tuple tuple = dataModel.getTuple(iTuple);
//                    PCPTuple pcpTuple = new PCPTuple(tuple);
//                    tupleList.add(pcpTuple);
//                }
//                fillTupleSets();
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
                if (dataModel.getHighlightedColumn() == null) {
                    for (PCPAxis pcpAxis : axisList) {
                        pcpAxis.setHighlighted(false);
                    }
                } else {
                    for (PCPAxis pcpAxis : axisList) {
                        if (pcpAxis.getColumn() == dataModel.getHighlightedColumn()) {
                            pcpAxis.setHighlighted(true);
                        } else {
                            pcpAxis.setHighlighted(false);
                        }
                    }
                }
                log.debug("highlighted column changed");
            }

            @Override
            public void tuplesAdded(DataModel dataModel, ArrayList<Tuple> newTuples) {
                log.debug("data model tuples added");
            }

            @Override
            public void columnDisabled(DataModel dataModel, Column disabledColumn) {
                for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                    PCPAxis pcpAxis = axisList.get(iaxis);
                    if (pcpAxis.getColumn() == disabledColumn) {
                        axisList.remove(pcpAxis);

                        // remove axis graphics from pane
                        pane.getChildren().remove(pcpAxis.getGraphicsGroup());
                        pane.getChildren().remove(pcpAxis.getHistogramBinRectangleGroup());
                        pane.getChildren().remove(pcpAxis.getQueryHistogramBinRectangleGroup());

                        // remove axis selection graphics
                        if (!pcpAxis.getAxisSelectionList().isEmpty()) {
                            for (PCPAxisSelection axisSelection : pcpAxis.getAxisSelectionList()) {
                                pane.getChildren().remove(axisSelection.getGraphicsGroup());
                            }
                        }

                        // create PCPBinSets for axis configuration
                        PCPBinSetList = new ArrayList<>();
                        for (int i = 0; i < axisList.size()-1; i++) {
                            PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
                            binSet.layoutBins();
                            PCPBinSetList.add(binSet);
                        }

                        // add tuples polylines from data model
                        tupleList = new ArrayList<>();
                        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
                            Tuple tuple = dataModel.getTuple(iTuple);
                            PCPTuple pcpTuple = new PCPTuple(tuple);
                            tupleList.add(pcpTuple);
                        }
                        fillTupleSets();

                        resize();
                        break;
                    }
                }
//                pane.getChildren().clear();
//                initializeLayout();
//                for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
//                    PCPAxis pcpAxis = axisList.get(iaxis);
//                    if (pcpAxis.getColumn() == disabledColumn) {
//                        axisList.remove(pcpAxis);
//                        pane.getChildren().remove(pcpAxis.getGraphicsGroup());
//                        pane.getChildren().remove(pcpAxis.getHistogramBinRectangleGroup());
//                        pane.getChildren().remove(pcpAxis.getQueryHistogramBinRectangleGroup());
//
//                        // remove axis selection graphics
//                        if (!pcpAxis.getAxisSelectionList().isEmpty()) {
//                            for (PCPAxisSelection axisSelection : pcpAxis.getAxisSelectionList()) {
//                                pane.getChildren().remove(axisSelection.getGraphicsGroup());
//                            }
//                        }
//
//                        // create PCPBinSets for axis configuration
//                        PCPBinSetList = new ArrayList<>();
//                        for (int i = 0; i < axisList.size()-1; i++) {
//                            PCPBinSet binSet = new PCPBinSet(axisList.get(i), axisList.get(i+1), dataModel);
//                            binSet.layoutBins();
//                            PCPBinSetList.add(binSet);
//                        }
//
//                        // add tuples polylines from data model
//                        tupleList = new ArrayList<>();
//                        for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
//                            Tuple tuple = dataModel.getTuple(iTuple);
//                            PCPTuple pcpTuple = new PCPTuple(tuple);
//                            tupleList.add(pcpTuple);
//                        }
//                        fillTupleSets();
//
//                        break;
//                    }
//                }
//                resize();
            }

            @Override
            public void columnsDisabled(DataModel dataModel, ArrayList<Column> disabledColumns) {

            }

            @Override
            public void columnEnabled(DataModel dataModel, Column enabledColumn) {
                // add axis lines to the pane
                addAxis(enabledColumn);
//                PCPAxis pcpAxis = new PCPAxis(this, enabledColumn, dataModel.getColumnIndex(enabledColumn), dataModel, pane);
//                pane.getChildren().add(pcpAxis.getGraphicsGroup());
//                axisList.add(pcpAxis);
//                axisList.clear();
//                for (int iaxis = 0; iaxis < dataModel.getColumnCount(); iaxis++) {
//                    PCPAxis pcpAxis = new PCPAxis(dataModel.getColumn(iaxis), iaxis, dataModel, pane);
//                    pane.getChildren().add(pcpAxis.getGraphicsGroup());
//                    axisList.add(pcpAxis);
//                }

                // add tuples polylines from data model
                tupleList = new ArrayList<>();
                for (int iTuple = 0; iTuple < dataModel.getTupleCount(); iTuple++) {
                    Tuple tuple = dataModel.getTuple(iTuple);
                    PCPTuple pcpTuple = new PCPTuple(tuple);
                    tupleList.add(pcpTuple);
                }

                fillTupleSets();

                // create PCPBinSets for axis configuration
                PCPBinSetList = new ArrayList<>();
                for (int iaxis = 0; iaxis < axisList.size()-1; iaxis++) {
                    PCPBinSet binSet = new PCPBinSet(axisList.get(iaxis), axisList.get(iaxis+1), dataModel);
                    PCPBinSetList.add(binSet);
                }

                resize();
            }
        });*/
    }

    private void init() {
        axisList = new ArrayList<>();
        unselectedTupleSet = new HashSet<>();
        selectedTupleSet = new HashSet<>();
        selectedItemsColor = new SimpleObjectProperty<>(DEFAULT_SELECTED_ITEMS_COLOR);
        unselectedItemsColor = new SimpleObjectProperty<>(DEFAULT_UNSELECTED_ITEMS_COLOR);
        showSelectedItems = new SimpleBooleanProperty(true);
        showUnselectedItems = new SimpleBooleanProperty(true);
        displayMode = new SimpleObjectProperty<>(DEFAULT_DISPLAY_MODE);
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

        selectedItemsColor.addListener((observable, oldValue, newValue) -> {
            redraw();
        });

        unselectedItemsColor.addListener((observable, oldValue, newValue) -> {
            redraw();
        });

        showSelectedItems.addListener(((observable, oldValue, newValue) -> {
            redraw();
        }));

        showUnselectedItems.addListener(((observable, oldValue, newValue) -> {
            redraw();
        }));

        displayMode.addListener(((observable, oldValue, newValue) -> {
            if ((oldValue == DISPLAY_MODE.PCP_LINES) || (oldValue == DISPLAY_MODE.PCP_BINS)) {
                lineGC.clearRect(0, 0, getWidth(), getHeight());
            }

            if (newValue == DISPLAY_MODE.PCP_LINES) {
                fillTupleSets();
            }

            resize();
            //        if (getDisplayMode() != displayMode) {
//            if ((this.displayMode == DISPLAY_MODE.PCP_LINES) ||
//                    (this.displayMode == DISPLAY_MODE.PCP_BINS)){
//                lineGC.clearRect(0, 0, getWidth(), getHeight());
//            }
//            this.displayMode = displayMode;
//
//            if (displayMode == DISPLAY_MODE.PCP_LINES) {
//                fillTupleSets();
//            }
//
//            resize();
//        }
        }));
    }

    public void setFitAxisSpacingToWidthEnabled (boolean enabled) {
        fitAxisSpacingToWidthEnabled = enabled;
    }

    public int getAxisSpacing () {return (int)axisSpacing;}

    public final PCPAxis getAxis(int index) {
        return axisList.get(index);
    }

    public final int getAxisCount() { return axisList.size(); }

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

                        if (getDisplayMode() == DISPLAY_MODE.HISTOGRAM) {
                            pane.getChildren().add(0, pcpAxis.getHistogramBinRectangleGroup());
                            pane.getChildren().add(1, pcpAxis.getQueryHistogramBinRectangleGroup());
                        }
                    }

                    // add tuples polylines from data model
                    if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
                        if (tupleList != null) {
                            for (PCPTuple pcpTuple : tupleList) {
                                pcpTuple.layout(axisList);
                            }
                        }
                    } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
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

        if (getDisplayMode() == DISPLAY_MODE.PCP_LINES) {
            drawTuplePolylines();
        } else if (getDisplayMode() == DISPLAY_MODE.PCP_BINS) {
            drawPCPBins();
        }
    }

    public void clearQuery() {
        dataModel.clearActiveQuery();
//        dataModel.setQueriedTuples();

        // clear all axis selection graphics
        for (PCPAxis pcpAxis : axisList) {
            if (!pcpAxis.getAxisSelectionList().isEmpty()) {
                for (PCPAxisSelection pcpAxisSelection : pcpAxis.getAxisSelectionList()) {
                    pane.getChildren().remove(pcpAxisSelection.getGraphicsGroup());
                }
                pcpAxis.getAxisSelectionList().clear();
            }
        }

        handleQueryChange();
    }

    private void drawPCPBins() {
        lineCanvas.setCache(false);
        lineGC.setLineCap(StrokeLineCap.BUTT);
        lineGC.clearRect(0, 0, getWidth(), getHeight());
//        lineGC.setGlobalAlpha(lineOpacity);
        lineGC.setLineWidth(2);
        lineGC.setLineWidth(2d);

        if ((PCPBinSetList != null) && (!PCPBinSetList.isEmpty())) {

            if (dataModel.getActiveQuery().hasColumnSelections()) {
                if (isShowingUnselectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            Color binColor = new Color(getUnselectedItemsColor().getRed(), getUnselectedItemsColor().getGreen(),
                                    getUnselectedItemsColor().getBlue(), bin.fillColor.getOpacity());
                            lineGC.setFill(binColor);
                            double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                            double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};
                            lineGC.fillPolygon(xValues, yValues, xValues.length);
                        }
                    }
                }

                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            if (bin.queryCount > 0) {
                                //                        lineGC.setFill(bin.queryFillColor);
                                Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                        getSelectedItemsColor().getBlue(), bin.queryFillColor.getOpacity());
                                lineGC.setFill(binColor);
                                double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                                double yValues[] = new double[]{bin.leftQueryTop, bin.rightQueryTop, bin.rightQueryBottom, bin.leftQueryBottom};
                                lineGC.fillPolygon(xValues, yValues, xValues.length);
                            }
                        }
                    }
                }
            } else {
                if (isShowingSelectedItems()) {
                    for (PCPBinSet binSet : PCPBinSetList) {
                        for (PCPBin bin : binSet.getBins()) {
                            Color binColor = new Color(getSelectedItemsColor().getRed(), getSelectedItemsColor().getGreen(),
                                    getSelectedItemsColor().getBlue(), bin.fillColor.getOpacity());
                            lineGC.setFill(binColor);
                            double xValues[] = new double[]{bin.left, bin.right, bin.right, bin.left};
                            double yValues[] = new double[]{bin.leftTop, bin.rightTop, bin.rightBottom, bin.leftBottom};
                            lineGC.fillPolygon(xValues, yValues, xValues.length);
                        }
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
//        lineGC.setGlobalAlpha(lineOpacity);
        lineGC.setLineWidth(2);
        lineGC.setLineWidth(2d);

        if ((isShowingUnselectedItems()) && (unselectedTupleSet != null) && (!unselectedTupleSet.isEmpty())) {
            lineGC.setStroke(getUnselectedItemsColor());
            for (PCPTuple pcpTuple : unselectedTupleSet) {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    lineGC.strokeLine(axisList.get(i-1).getBarRightX(), pcpTuple.getYPoints()[i-1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }

        if ((isShowingSelectedItems()) && (selectedTupleSet != null) && (!selectedTupleSet.isEmpty())) {
            lineGC.setStroke(getSelectedItemsColor());
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
