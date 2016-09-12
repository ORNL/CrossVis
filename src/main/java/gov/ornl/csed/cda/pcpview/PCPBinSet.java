package gov.ornl.csed.cda.pcpview;

import gov.ornl.csed.cda.datatable.DataModel;
import gov.ornl.csed.cda.datatable.Histogram2D;
import gov.ornl.csed.cda.util.GraphicsUtil;
import javafx.scene.paint.*;

import java.util.ArrayList;

/**
 * Created by csg on 8/31/16.
 */
public class PCPBinSet {
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

    private PCPAxis leftAxis;
    private PCPAxis rightAxis;
    private DataModel dataModel;

    private ArrayList<PCPBin> bins;

    public PCPBinSet(PCPAxis leftAxis, PCPAxis rightAxis, DataModel dataModel) {
        this.leftAxis = leftAxis;
        this.rightAxis = rightAxis;
        this.dataModel = dataModel;

        lineColor = DEFAULT_LINE_COLOR;
        maxCountFillColor = DEFAULT_MAX_COUNT_FILL_COLOR;
        minCountFillColor = DEFAULT_MIN_COUNT_FILL_COLOR;
        maxQueryCountFillColor = DEFAULT_QUERY_MAX_COUNT_FILL_COLOR;
        minQueryCountFillColor = DEFAULT_QUERY_MIN_COUNT_FILL_COLOR;
    }

    public ArrayList<PCPBin> getBins () { return bins; }

    public Paint getLineColor() { return lineColor; }

    public void layoutBins() {
        bins = new ArrayList<>();

        Histogram2D histogram2D = leftAxis.getColumn().getSummaryStats().getHistogram2DList().get(rightAxis.getColumnDataModelIndex());
        Histogram2D queryHistogram2D = null;
        if (dataModel.getActiveQuery().hasColumnSelections()) {
            queryHistogram2D = dataModel.getActiveQuery().getColumnQuerySummaryStats(leftAxis.getColumn()).getHistogram2DList().get(rightAxis.getColumnDataModelIndex());
        }

        double leftX = leftAxis.getCenterX();
        double rightX = rightAxis.getCenterX();

        double binHeight = (leftAxis.getFocusBottomY() - leftAxis.getFocusTopY()) / histogram2D.getNumBins();
        double binQueryHeight = 2d * (binHeight / 3d);
        double queryHeightOffset = (binHeight - binQueryHeight) / 2d;

        for (int ix = 0; ix < histogram2D.getNumBins(); ix++) {
            double leftBottom = leftAxis.getFocusBottomY() - (ix * binHeight);
            double leftTop = leftAxis.getFocusBottomY() - ((ix + 1) * binHeight);

            for (int iy = 0; iy < histogram2D.getNumBins(); iy++) {
                int count = histogram2D.getBinCount(ix, iy);

                if (count > 0) {
                    PCPBin bin = new PCPBin();
                    bin.count = count;

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
                        }
                    }

                    bin.left = leftX;
                    bin.right = rightX;
                    bin.leftTop = leftTop;
                    bin.leftBottom = leftBottom;
                    bin.rightBottom = rightAxis.getFocusBottomY() - (iy * binHeight);
                    bin.rightTop = rightAxis.getFocusBottomY() - ((iy + 1) * binHeight);

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
