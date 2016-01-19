package gov.ornl.csed.cda.timevis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by csg on 1/8/16.
 */
public class MultiScaleTimeSeriesPanel extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
    private final static Logger log = LoggerFactory.getLogger(MultiScaleTimeSeriesPanel.class);

    private TimeSeries timeSeries;
    private Instant startInstant;
    private Instant endInstant;
    private Instant plotLeftInstant;
    private Instant plotRightInstant;
    private Duration totalDuration;
    private int plotNameBarHeight = 14;
    private int timeInfoBarHeight = 14;
    private int plotValueBarHeight = 14;
    private int timeInfoBarTop;
    private int timeInfoBarBottom;
    private int valueInfoBarTop;
    private int valueInfoBarBottom;
    private int plotUnitWidth = 1;
    private int numPlotUnits = 0;
    private int plotUnitDurationMillis = 0;

    private Rectangle plotRectangle;
    private Rectangle2D.Double detailPlotRectangle;
    private Rectangle2D.Double leftOverviewPlotRectangle;
    private Rectangle2D.Double rightOverviewPlotRectangle;

    private Instant detailStartInstant;
    private Instant detailEndInstant;
    private Instant detailMiddleInstant;
    private Duration detailDuration;
    private ChronoUnit detailChronoUnit = ChronoUnit.MINUTES;

    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    Color dataColor = new Color(80, 80, 130, 180);
    Color rangeColor = new Color(140, 140, 160, 100);
    private Color selectedRegionFillColor = Color.white;

    TreeMap<Instant, ArrayList<Point2D.Double>> plotPointMap;
    ArrayList<TimeSeriesSummaryInfo> leftSummaryInfoList;
    ArrayList<TimeSeriesSummaryInfo> rightSummaryInfoList;

    public MultiScaleTimeSeriesPanel(int plotUnitWidth) {
        this.plotUnitWidth = plotUnitWidth;
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setTimeSeries(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
        startInstant = timeSeries.getStartInstant();
        endInstant = timeSeries.getEndInstant();

        totalDuration = Duration.between(startInstant, endInstant);

        long halfDurationMS = (long)(totalDuration.toMillis() * .5);
        Instant middleInstant = startInstant.plusMillis(halfDurationMS);

        long detailHalfDurationMS = Duration.of(15, ChronoUnit.MINUTES).toMillis();
        detailStartInstant = middleInstant.minusMillis(detailHalfDurationMS);
        detailEndInstant = middleInstant.plusMillis(detailHalfDurationMS);
        detailMiddleInstant = middleInstant;
        detailDuration = Duration.between(detailStartInstant, detailEndInstant);

        layoutPanel();
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getBackground());
        g2.drawRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.black);
        g2.draw(plotRectangle);

        g2.setColor(Color.blue);
        g2.draw(detailPlotRectangle);
        Point2D.Double lastDrawnPoint = null;
        for (ArrayList<Point2D.Double> instantPoints : plotPointMap.values()) {
            for (Point2D.Double point : instantPoints) {
                if (lastDrawnPoint != null) {
                    Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x - 1,
                            point.y - 1, 2., 2.);
                    Line2D.Double line = new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, point.x, point.y);
                    g2.setColor(dataColor);
                    g2.draw(line);
                    g2.setColor(dataColor);
                    g2.draw(ellipse);
                }
                lastDrawnPoint = point;
            }
        }

        g2.setColor(Color.orange);
        g2.draw(leftOverviewPlotRectangle);

        g2.setColor(Color.yellow);
        g2.draw(rightOverviewPlotRectangle);
    }

    public void layoutPanel() {
        if (timeSeries != null) {
            int plotLeft = getInsets().left;
            int plotTop = getInsets().top + timeInfoBarHeight;
            int plotBottom = getHeight() - (getInsets().bottom + plotValueBarHeight);
            timeInfoBarTop = getInsets().top;
            timeInfoBarBottom = timeInfoBarTop + timeInfoBarHeight;
            valueInfoBarTop = plotBottom;
            valueInfoBarBottom = valueInfoBarTop + plotValueBarHeight;

            int plotWidth = getWidth() - (getInsets().left + getInsets().right);
            plotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);

            // find location of detail middle instant (assuming position in overall time range)
            double normPosition = (double)Duration.between(startInstant, detailMiddleInstant).toMillis() /
                    totalDuration.toMillis();
            double detailMiddleXPosition = plotRectangle.x + (plotWidth * normPosition);

            // get time duration between start and end detail instants using detail region's choronunit
            long detailDeltaTime = detailChronoUnit.between(detailStartInstant, detailEndInstant);

            // find width of detail rectangle
            long detailRectangleWidth = detailDeltaTime * plotUnitWidth;
            log.debug("detailRectangleWidth is " + detailRectangleWidth + " " + (double)detailRectangleWidth/plotWidth + "%");

            // compute location of detail start instant
            double detailStartXPosition = detailMiddleXPosition - detailRectangleWidth/2.;

            // compute location of detail end instant
            double detailEndXPosition = detailMiddleXPosition + detailRectangleWidth/2.;

            // compute detail plot rectangle
            detailPlotRectangle = new Rectangle2D.Double(detailStartXPosition, plotRectangle.y, detailRectangleWidth,
                    plotRectangle.height);

            // compute left overview rectangle
            double leftOverviewWidth = detailStartXPosition - plotLeft;
            leftOverviewPlotRectangle = new Rectangle2D.Double(plotLeft, plotTop, leftOverviewWidth,
                    plotRectangle.height);
            int leftNumPlotUnits = (int) (leftOverviewWidth / plotUnitWidth);
            Duration leftOverviewDuration = Duration.between(startInstant, detailStartInstant);
            double leftPlotUnitDurationReal = (double)leftOverviewDuration.toMillis() / leftNumPlotUnits;
            long leftPlotUnitDurationMillis = (int)Math.ceil(leftPlotUnitDurationReal);
            Instant leftOverviewRightInstant = startInstant.plusMillis(leftPlotUnitDurationMillis * leftNumPlotUnits);
            log.debug("leftOverview right instant is " + leftOverviewRightInstant + " detailStartInstant is " + detailStartInstant);

            // compute right overview rectangle
            double rightOverviewWidth = plotRectangle.getMaxX() - detailEndXPosition;
            rightOverviewPlotRectangle = new Rectangle2D.Double(detailEndXPosition, plotTop, rightOverviewWidth, plotRectangle.height);

            calculatePoints();
        }
    }

    private void calculatePoints() {
        // calculate detail region points
        plotPointMap = new TreeMap<>();

        int numPointsCalculated = 0;
        ArrayList<TimeSeriesRecord> records = timeSeries.getRecordsBetween(detailStartInstant, detailEndInstant);

        if (records != null) {
            for (TimeSeriesRecord record : records) {
                long deltaTime = detailChronoUnit.between(detailStartInstant, record.instant);
                double x = detailPlotRectangle.x + (deltaTime * plotUnitWidth);

                double norm = (record.value - timeSeries.getMinValue()) / (timeSeries.getMaxValue() - timeSeries.getMinValue());
                double yOffset = norm * (plotRectangle.height);
                double y = plotRectangle.height - yOffset;

                Point2D.Double point = new Point2D.Double(x, y);

                ArrayList<Point2D.Double> instantPoints = plotPointMap.get(record.instant);
                if (instantPoints == null) {
                    instantPoints = new ArrayList<>();
                    plotPointMap.put(record.instant, instantPoints);
                }
                instantPoints.add(point);
                numPointsCalculated++;
            }
        }

        // calculate left overview summaries
        leftSummaryInfoList = new ArrayList<>();
       
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
//                    int numTimeRecords = 50400;
                    int numTimeRecords = 60*10;
//                    int numTimeRecords = 1200;
                    int plotUnitWidth = 4;
                    JFrame frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    MultiScaleTimeSeriesPanel msTimeSeriesPanel = new MultiScaleTimeSeriesPanel(plotUnitWidth);
                    msTimeSeriesPanel.setBackground(Color.white);
                    msTimeSeriesPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

                    ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                    ((JPanel)frame.getContentPane()).add(msTimeSeriesPanel, BorderLayout.CENTER);

                    frame.setSize(1000, 200);
                    frame.setVisible(true);

                    TimeSeries timeSeries = new TimeSeries("Test");

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords, ChronoUnit.MINUTES);

                    log.debug("startInstant = " + startInstant + " endInstant = " + endInstant);

                    double value = 0.;

                    for (int i = 0; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.MINUTES);
                        value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
                        double range = Math.abs(value) * .25;
                        double upperRange = value + range;
                        double lowerRange = value - range;
                        timeSeries.addRecord(instant, value, upperRange, lowerRange);
                    }

                    msTimeSeriesPanel.setTimeSeries(timeSeries);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
