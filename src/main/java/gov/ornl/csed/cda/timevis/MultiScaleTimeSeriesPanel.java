package gov.ornl.csed.cda.timevis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
    private Rectangle detailPlotRectangle;
    private Rectangle leftOverviewPlotRectangle;
    private Rectangle rightOverviewPlotRectangle;

    private Instant detailStartInstant;
    private Instant detailEndInstant;
    private Instant detailMiddleInstant;
    private Duration detailDuration;
    private ChronoUnit detailChronoUnit = ChronoUnit.SECONDS;

    private Color gridLineColor = new Color(230, 230, 230);
    private Color hoverLineColor = new Color(50, 50, 50, 100);
    private Color unselectedRegionFillColor = new Color(240, 240, 240);
    Color dataColor = new Color(80, 80, 130, 180);
    Color rangeColor = new Color(140, 140, 160, 100);
    private Color selectedRegionFillColor = Color.white;

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

        long detailHalfDurationMS = (long)(totalDuration.toMillis() * .01);
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

        g2.setColor(Color.orange);
        g2.draw(leftOverviewPlotRectangle);
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

            // compute start and end detail instants
            detailStartInstant = detailMiddleInstant.minus(detailDeltaTime/2, detailChronoUnit);
            detailEndInstant = detailMiddleInstant.plus(detailDeltaTime/2, detailChronoUnit);

            // find width of detail rectangle
            long detailRectangleWidth = detailDeltaTime * plotUnitWidth;

            // compute location of detail start instant
            double detailStartXPosition = detailMiddleXPosition - detailRectangleWidth/2.;

            // compute location of detail end instant
            double detailEndXPosition = detailMiddleXPosition + detailRectangleWidth/2.;

            // compute detail plot rectangle
            detailPlotRectangle = new Rectangle((int)detailStartXPosition, plotRectangle.y, (int)detailRectangleWidth, plotRectangle.height);

            // compute left overview rectangle
            double leftOverviewWidth = detailStartXPosition - plotLeft;
            leftOverviewPlotRectangle = new Rectangle(plotLeft, plotTop, (int)leftOverviewWidth, plotRectangle.height);
            int leftNumPlotUnits = (int) (leftOverviewWidth / plotUnitWidth);
            Duration leftOverviewDuration = Duration.between(startInstant, detailStartInstant);
            double leftPlotUnitDurationReal = (double)leftOverviewDuration.toMillis() / leftNumPlotUnits;
            long leftPlotUnitDurationMillis = (int)Math.ceil(leftPlotUnitDurationReal);
            Instant leftOverviewRightInstant = startInstant.plusMillis(leftPlotUnitDurationMillis * leftNumPlotUnits);
            log.debug("leftOverview right instant is " + leftOverviewRightInstant + " detailStartInstant is " + detailStartInstant);

            // compute right overview rectangle
            double rightOverviewWidth = plotRectangle.getMaxX() - detailEndXPosition;
            rightOverviewPlotRectangle = new Rectangle((int)detailEndXPosition, plotTop, (int)rightOverviewWidth, plotRectangle.height);

//            int plotWidth = getWidth() - (getInsets().left + getInsets().right);
//            numPlotUnits = plotWidth / plotUnitWidth;
//            double plotUnitDurationReal = (double)totalDuration.toMillis() / numPlotUnits;
//            plotUnitDurationMillis = (int)Math.ceil(plotUnitDurationReal);
//            plotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);
//            plotLeftInstant = startInstant;
//            plotRightInstant = startInstant.plusMillis(plotUnitDurationMillis * numPlotUnits);
        }
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
                    int numTimeRecords = 12000;
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

                    Instant startInstant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
//                    Instant startInstant = Instant.now();
                    Instant endInstant = Instant.from(startInstant).plus(numTimeRecords, ChronoUnit.SECONDS);

                    log.debug("startInstant = " + startInstant + " endInstant = " + endInstant);

                    double value = 0.;

                    for (int i = 0; i < numTimeRecords; i++) {
                        Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
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
