package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TemporalAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(TemporalAxis.class.getName());

    // histogram bin rectangles
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup= new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Color histogramFill = DEFAULT_HISTOGRAM_FILL;
    private Color histogramStroke = DEFAULT_HISTOGRAM_STROKE;
    private Color queryHistogramFill = DEFAULT_QUERY_HISTOGRAM_FILL;

    private Text minValueText;
    private Text maxValueText;

    public TemporalAxis(DataTableView dataTableView, Column column) {
        super(dataTableView, column);

        minValueText = new Text();
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);

        minValueText.setText(((TemporalColumn)column).getStatistics().getStartInstant().toString());
        maxValueText.setText(((TemporalColumn)column).getStatistics().getEndInstant().toString());

        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        if (getDataTableView().isShowingHistograms()) {
            getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
            getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        }

        registerListeners();
    }

    protected TemporalColumn temporalColumn() { return (TemporalColumn)getColumn(); }

    public double getAxisPositionForValue(Instant instant) {
        double position = GraphicsUtil.mapValue(instant, temporalColumn().getStatistics().getStartInstant(),
                temporalColumn().getStatistics().getEndInstant(), getFocusMinPosition(), getFocusMaxPosition());
        return position;
    }

    private void registerListeners() {
        ((TemporalColumn)getColumn()).getStatistics().startInstantProperty().addListener((observable, oldValue, newValue) -> {
            minValueText.setText(newValue.toString());
        });

        ((TemporalColumn)getColumn()).getStatistics().endInstantProperty().addListener((observable, oldValue, newValue) -> {
            maxValueText.setText(newValue.toString());
        });

        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                if (newValue) {
                    getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
                    getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
//                    graphicsGroup.getChildren().add(2, nonqueryHistogramGroup);
                } else {
                    getGraphicsGroup().getChildren().remove(overallHistogramGroup);
                    getGraphicsGroup().getChildren().remove(queryHistogramGroup);
//                    graphicsGroup.getChildren().remove(nonqueryHistogramGroup);
                }

                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });
    }

    public void resize(double center, double top, double width, double height) {
        super.resize(center, top, width, height);

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                // resize histogram bin information
                TemporalHistogram histogram = temporalColumn().getStatistics().getHistogram();
                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

                overallHistogramGroup.getChildren().clear();
                overallHistogramRectangles.clear();

                for (int i = 0; i < histogram.getNumBins(); i++) {
                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                    double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i), 0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2, DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                    double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                    rectangle.setStroke(histogramFill.darker());
                    rectangle.setFill(histogramFill);
                    overallHistogramRectangles.add(rectangle);
                    overallHistogramGroup.getChildren().add(rectangle);
                }

                queryHistogramGroup.getChildren().clear();
                queryHistogramRectangles.clear();

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
//                    TemporalColumnSummaryStats queryColumnSummaryStats = (TemporalColumnSummaryStats) dataModel.getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    // resize query histogram bins
                    TemporalHistogram queryHistogram = ((TemporalColumnSummaryStats) getDataTable().getActiveQuery().getColumnQuerySummaryStats(temporalColumn())).getHistogram();

                    if (queryHistogram != null) {
                        if (getDataTable().getCalculateQueryStatistics()) {
                            if (histogram.getNumBins() != queryHistogram.getNumBins()) {
                                log.info("query histogram and overall histogram have different bin sizes");
                            }

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                if (queryHistogram.getBinCount(i) > 0) {
                                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                                    double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                            0, histogram.getMaxBinCount(), DEFAULT_BAR_WIDTH + 2,
                                            DEFAULT_BAR_WIDTH + 2 + maxHistogramBinWidth);
                                    double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
                                    rectangle.setStroke(queryHistogramFill.darker());
                                    rectangle.setFill(queryHistogramFill);

                                    queryHistogramRectangles.add(rectangle);
                                    queryHistogramGroup.getChildren().add(rectangle);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
