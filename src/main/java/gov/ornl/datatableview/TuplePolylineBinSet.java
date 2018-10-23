package gov.ornl.datatableview;

import gov.ornl.datatable.BivariateColumn;
import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.Histogram2D;
import gov.ornl.datatable.Histogram2DDimension;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

/**
 * Created by csg on 8/31/16.
 */
public class TuplePolylineBinSet {
    public static final Color DEFAULT_LINE_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.2);
    public static final Color DEFAULT_MAX_COUNT_FILL_COLOR = new Color(Color.DARKGRAY.getRed(), Color.DARKGRAY.getGreen(), Color.DARKGRAY.getBlue(), 0.8);
    public static final Color DEFAULT_MIN_COUNT_FILL_COLOR = new Color(Color.DARKGRAY.getRed(), Color.DARKGRAY.getGreen(), Color.DARKGRAY.getBlue(), 0.2);
    public static final Color DEFAULT_QUERY_MAX_COUNT_FILL_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.8);
    public static final Color DEFAULT_QUERY_MIN_COUNT_FILL_COLOR = new Color(Color.STEELBLUE.getRed(), Color.STEELBLUE.getGreen(), Color.STEELBLUE.getBlue(), 0.2);

    private Paint lineColor;
    private Color maxCountFillColor;
    private Color minCountFillColor;
    private Color maxQueryCountFillColor;
    private Color minQueryCountFillColor;

    private UnivariateAxis leftAxis;
    private UnivariateAxis rightAxis;
    private DataTable dataModel;

    private int binsWithQueries;

    private ArrayList<TuplePolylineBin> bins;

    public TuplePolylineBinSet(UnivariateAxis leftAxis, UnivariateAxis rightAxis, DataTable dataModel) {
        this.leftAxis = leftAxis;
        this.rightAxis = rightAxis;
        this.dataModel = dataModel;

        lineColor = DEFAULT_LINE_COLOR;
        maxCountFillColor = DEFAULT_MAX_COUNT_FILL_COLOR;
        minCountFillColor = DEFAULT_MIN_COUNT_FILL_COLOR;
        maxQueryCountFillColor = DEFAULT_QUERY_MAX_COUNT_FILL_COLOR;
        minQueryCountFillColor = DEFAULT_QUERY_MIN_COUNT_FILL_COLOR;

        binsWithQueries = 0;
    }

    public ArrayList<TuplePolylineBin> getBins () { return bins; }

    public Paint getLineColor() { return lineColor; }

    public void layoutBins() {
        bins = new ArrayList<>();
        binsWithQueries = 0;

        if (leftAxis instanceof UnivariateAxis && rightAxis instanceof UnivariateAxis) {
            Histogram2D histogram2D = leftAxis.getColumn().getStatistics().getColumnHistogram2D(rightAxis.getColumn());
            Histogram2D queryHistogram2D = null;
            if (dataModel.getActiveQuery().hasColumnSelections()) {
                queryHistogram2D = dataModel.getActiveQuery().getColumnQuerySummaryStats(leftAxis.getColumn()).getColumnHistogram2D(rightAxis.getColumn());
            }

            double leftX = leftAxis.getBarRightX();
            double rightX = rightAxis.getBarLeftX();
            double leftBinHeight = (leftAxis.getFocusMinPosition() - leftAxis.getFocusMaxPosition()) / histogram2D.getXDimension().getNumBins();
            double leftQueryBinHeight = 2d * (leftBinHeight / 3d);
            double queryHeightOffset = (leftBinHeight - leftQueryBinHeight) / 2d;

            for (int ix = 0; ix < histogram2D.getXDimension().getNumBins(); ix++) {
                double leftBottom = 0;
                double leftTop = 0;

                if (leftAxis instanceof CategoricalAxis) {
                    String category = ((Histogram2DDimension.Categorical)histogram2D.getXDimension()).getBinCategory(ix);
                    Rectangle categoryRectangle = ((CategoricalAxis)leftAxis).getCategoryRectangle(category);
                    leftBottom = categoryRectangle.getLayoutBounds().getMaxY();
                    leftTop = categoryRectangle.getLayoutBounds().getMinY();
                } else if (leftAxis instanceof DoubleAxis || leftAxis instanceof TemporalAxis) {
                    leftBottom = leftAxis.getFocusMinPosition() - (ix * leftBinHeight);
                    leftTop = leftAxis.getFocusMinPosition() - ((ix + 1) * leftBinHeight);
                }

                for (int iy = 0; iy < histogram2D.getYDimension().getNumBins(); iy++) {
                    int count = histogram2D.getBinCount(ix, iy);

                    if (count > 0) {
                        TuplePolylineBin bin = new TuplePolylineBin();
                        bin.count = count;

                        double rightBottom = 0.;
                        double rightTop = 0.;
                        if (rightAxis instanceof CategoricalAxis) {
                            String category = ((Histogram2DDimension.Categorical)histogram2D.getYDimension()).getBinCategory(iy);
                            Rectangle categoryRectangle = ((CategoricalAxis)rightAxis).getCategoryRectangle(category);
                            rightBottom = categoryRectangle.getLayoutBounds().getMaxY();
                            rightTop = categoryRectangle.getLayoutBounds().getMinY();
                        } else if (rightAxis instanceof DoubleAxis || rightAxis instanceof TemporalAxis) {
                            double rightBinHeight = (rightAxis.getFocusMinPosition() - rightAxis.getFocusMaxPosition()) / histogram2D.getYDimension().getNumBins();
                            rightBottom = rightAxis.getFocusMinPosition() - (iy * rightBinHeight);
                            rightTop = rightAxis.getFocusMinPosition() - ((iy + 1) * rightBinHeight);
                        }

                        // compute fill color
                        double normCount = GraphicsUtil.norm(count, 0, dataModel.getMaxHistogram2DBinCount());
                        Color fillColor = GraphicsUtil.lerpColorFX(minCountFillColor, maxCountFillColor, normCount);
                        bin.fillColor = fillColor;

                        // compute query fill color
                        if (queryHistogram2D != null) {
                            int queryCount = queryHistogram2D.getBinCount(ix, iy);

                            bin.queryCount = queryCount;
                            if (queryCount > 0) {
                                normCount = GraphicsUtil.norm(queryCount, 0, dataModel.getActiveQuery().getMaxHistogram2DBinCount());
                                Color queryFillColor = GraphicsUtil.lerpColorFX(minQueryCountFillColor, maxQueryCountFillColor, normCount);
                                bin.queryFillColor = queryFillColor;
                                binsWithQueries++;
                            }
                        }

                        bin.left = leftX;
                        bin.right = rightX;
                        bin.leftTop = leftTop;
                        bin.leftBottom = leftBottom;
                        bin.rightTop = rightTop;
                        bin.rightBottom = rightBottom;

                        bin.leftQueryBottom = bin.leftBottom - queryHeightOffset;
                        bin.leftQueryTop = bin.leftTop + queryHeightOffset;

                        bin.rightQueryBottom = bin.rightBottom - queryHeightOffset;
                        bin.rightQueryTop = bin.rightTop + queryHeightOffset;

                        bins.add(bin);
                    }
                }
            }
        }
    }
/*
    public void layoutBinsOld() {
        bins = new ArrayList<>();
        binsWithQueries = 0;

        Histogram2D histogram2D = leftAxis.getColumn().getStatistics().getColumnHistogram2D(rightAxis.getColumn());
//        if (histogram2D == null ) {
//            System.out.println("Problem here.");
//        }
        Histogram2D queryHistogram2D = null;
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            queryHistogram2D = dataModel.getActiveQuery().getColumnQuerySummaryStats(leftAxis.getColumn()).getColumnHistogram2D(rightAxis.getColumn());
//            queryHistogram2D = dataModel.getActiveQuery().getColumnQuerySummaryStats((QuantitativeColumn)leftAxis.getColumn()).getHistogram2DList().get(rightAxis.getColumnDataModelIndex());
        }

        double leftX = leftAxis.getCenterX();
        double rightX = rightAxis.getCenterX();
        double leftBinHeight = 0;
        if (leftAxis instanceof UnivariateAxis) {
            leftX = ((UnivariateAxis)leftAxis).getBarRightX();
            leftBinHeight = (((UnivariateAxis)leftAxis).getFocusMinPosition() - ((UnivariateAxis)leftAxis).getFocusMaxPosition()) / histogram2D.getXDimension().getNumBins();
        } else if (leftAxis instanceof BivariateAxis) {
            leftX = ((BivariateAxis)leftAxis).getScatterplot().getPlotBounds().getMaxX();
            leftBinHeight = ((BivariateAxis)leftAxis).getScatterplot().getDataBounds().getHeight() / histogram2D.getXDimension().getNumBins();
        }

        if (rightAxis instanceof UnivariateAxis) {
            rightX = ((UnivariateAxis)rightAxis).getBarLeftX();
        } else if (rightAxis instanceof BivariateAxis) {
            rightX = ((BivariateAxis)rightAxis).getScatterplot().getPlotBounds().getMinX();
        }
//        double leftX = leftAxis.getAxisBar().getLayoutBounds().getMaxX();
//        double rightX = rightAxis.getAxisBar().getLayoutBounds().getMinX();
//        double leftX = leftAxis.getCenterX();
//        double rightX = rightAxis.getCenterX();

//        double leftBinHeight = (leftAxis.getFocusBottomY() - leftAxis.getFocusTopY()) / histogram2D.getXDimension().getNumBins();
        double leftQueryBinHeight = 2d * (leftBinHeight / 3d);
        double queryHeightOffset = (leftBinHeight - leftQueryBinHeight) / 2d;

        for (int ix = 0; ix < histogram2D.getXDimension().getNumBins(); ix++) {
            double leftBottom = 0;
            double leftTop = 0;

            if (leftAxis instanceof CategoricalAxis) {
                String category = ((Histogram2DDimension.Categorical)histogram2D.getXDimension()).getBinCategory(ix);
                Rectangle categoryRectangle = ((CategoricalAxis)leftAxis).getCategoryRectangle(category);
                leftBottom = categoryRectangle.getLayoutBounds().getMaxY();
                leftTop = categoryRectangle.getLayoutBounds().getMinY();
            } else if (leftAxis instanceof DoubleAxis || leftAxis instanceof TemporalAxis) {
                leftBottom = ((UnivariateAxis)leftAxis).getFocusMinPosition() - (ix * leftBinHeight);
                leftTop = ((UnivariateAxis)leftAxis).getFocusMinPosition() - ((ix + 1) * leftBinHeight);
            } else if (leftAxis instanceof BivariateAxis) {
                leftBottom = ((BivariateAxis)leftAxis).getScatterplot().getDataBounds().getMinY() - (ix * leftBinHeight);
                leftTop = ((BivariateAxis)leftAxis).getScatterplot().getDataBounds().getMinY() - ((ix + 1) * leftBinHeight);
            }

            for (int iy = 0; iy < histogram2D.getYDimension().getNumBins(); iy++) {
                int count = histogram2D.getBinCount(ix, iy);

                if (count > 0) {
                    TuplePolylineBin bin = new TuplePolylineBin();
                    bin.count = count;

                    double rightBottom = 0.;
                    double rightTop = 0.;
                    if (rightAxis instanceof CategoricalAxis) {
                        String category = ((Histogram2DDimension.Categorical)histogram2D.getYDimension()).getBinCategory(iy);
                        Rectangle categoryRectangle = ((CategoricalAxis)rightAxis).getCategoryRectangle(category);
                        rightBottom = categoryRectangle.getLayoutBounds().getMaxY();
                        rightTop = categoryRectangle.getLayoutBounds().getMinY();
                    } else if (rightAxis instanceof DoubleAxis || rightAxis instanceof TemporalAxis) {
                        double rightBinHeight = (((UnivariateAxis)rightAxis).getFocusMinPosition() - ((UnivariateAxis)rightAxis).getFocusMaxPosition()) / histogram2D.getYDimension().getNumBins();
                        rightBottom = ((UnivariateAxis)rightAxis).getFocusMinPosition() - (iy * rightBinHeight);
                        rightTop = ((UnivariateAxis)rightAxis).getFocusMinPosition() - ((iy + 1) * rightBinHeight);
                    } else if (rightAxis instanceof BivariateAxis) {
                        double rightBinHeight = ((BivariateAxis)rightAxis).getScatterplot().getDataBounds().getHeight() / histogram2D.getYDimension().getNumBins();
                        rightBottom = ((BivariateAxis)rightAxis).getScatterplot().getDataBounds().getMinY() - (iy * rightBinHeight);
                        rightTop = ((BivariateAxis)rightAxis).getScatterplot().getDataBounds().getMinY() - ((iy + 1) * rightBinHeight);
                    }

                    // compute fill color
                    double normCount = GraphicsUtil.norm(count, 0, dataModel.getMaxHistogram2DBinCount());
                    Color fillColor = GraphicsUtil.lerpColorFX(minCountFillColor, maxCountFillColor, normCount);
                    bin.fillColor = fillColor;

                    // compute query fill color
                    if (queryHistogram2D != null) {
                        int queryCount = queryHistogram2D.getBinCount(ix, iy);

                        bin.queryCount = queryCount;
                        if (queryCount > 0) {
                            normCount = GraphicsUtil.norm(queryCount, 0, dataModel.getActiveQuery().getMaxHistogram2DBinCount());
                            Color queryFillColor = GraphicsUtil.lerpColorFX(minQueryCountFillColor, maxQueryCountFillColor, normCount);
                            bin.queryFillColor = queryFillColor;
                            binsWithQueries++;
                        }
                    }

                    bin.left = leftX;
                    bin.right = rightX;
                    bin.leftTop = leftTop;
                    bin.leftBottom = leftBottom;
                    bin.rightTop = rightTop;
                    bin.rightBottom = rightBottom;
//                    bin.rightBottom = rightAxis.getFocusBottomY() - (iy * rightBinHeight);
//                    bin.rightTop = rightAxis.getFocusBottomY() - ((iy + 1) * rightBinHeight);

                    bin.leftQueryBottom = bin.leftBottom - queryHeightOffset;
                    bin.leftQueryTop = bin.leftTop + queryHeightOffset;

                    bin.rightQueryBottom = bin.rightBottom - queryHeightOffset;
                    bin.rightQueryTop = bin.rightTop + queryHeightOffset;

                    bins.add(bin);
                }
            }
        }
    }
    */
}
