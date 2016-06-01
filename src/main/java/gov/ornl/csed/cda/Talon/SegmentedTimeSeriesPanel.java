/*
 *
 *  Class:  SegmentedTimeSeriesPanel
 *
 *      Author:     whw
 *
 *      Created:    29 Feb 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  JComponent
 *
 *  Interfaces:     MouseListener,
 *                  TalonDataListener
 *
 */

package gov.ornl.csed.cda.Talon;


import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class SegmentedTimeSeriesPanel extends JComponent implements MouseListener, TalonDataListener {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(SegmentedTimeSeriesPanel.class);





    // =-= INSTANCE FIELDS =-=
    private TalonData data = null;
    private int plotTimeUnitWidth = 2;                                          // A second is represented by two pixels (now has a spinner to adjust this)
    private Insets margins = new Insets(4, 4, 4, 4);                            // Visual buffer surrounding the vis
    private int timeSeriesLabelWidth = 80;                                      // Pixel width of the plot labels (now has a spinner to adjust this)
    private int plotHeight = 60;                                                // The total height of a plotted series segment
    private int plotSpacing = plotHeight + 10;                                  // Adds buffer to plot height for in between plotted series segments
    private DecimalFormat df = new DecimalFormat("#,##0.00");                   // Decimal formatter for build height
    private ArrayList<Rectangle2D.Double> buildHeightLabelRectangles = new ArrayList<>();
    private Rectangle2D.Double labelsRectangle = new Rectangle.Double();
    private HashMap<Rectangle2D.Double, Double> labelsMap = new HashMap<>();





    // =-= CONSTRUCTOR =-=
    SegmentedTimeSeriesPanel (TalonData data) {

        this.data = data;
        data.addTalonDataListener(this);

        addMouseListener(this);
    }





    // =-= METHODS =-=

    // getters/setters
    public ArrayList<Rectangle2D.Double> getBuildHeightLabelRectangles() {
        return buildHeightLabelRectangles;
    }


    public Rectangle2D.Double getLabelsRectangle() {
        return labelsRectangle;
    }


    public HashMap<Rectangle2D.Double, Double> getLabelsMap() {
        return labelsMap;
    }


    public int getPlotTimeUnitWidth() {
        return plotTimeUnitWidth;
    }


    public int getPlotHeight() {
        return plotHeight;
    }


    //  -> set plotTimeUnitWidth
    //  -> update preferred size
    //  -> redraw and revalidate scrollbars
    public void setPlotTimeUnitWidth(int plotTimeUnitWidth) {

        if (this.plotTimeUnitWidth != plotTimeUnitWidth) {


            //  -> set plotTimeUnitWidth
            this.plotTimeUnitWidth = plotTimeUnitWidth;
            int largestPlotWidth = data.getLongestPlotDuration();
            int timeSeriesEntries = data.getSegmentedTimeSeriesMap().size();


            //  -> update preferred size
            int width = (largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth;
            int height = plotHeight + (timeSeriesEntries * plotSpacing) + (margins.top + margins.bottom);

            setPreferredSize(new Dimension(width, height));


            //  -> redraw and revalidate scrollbars
            repaint();
            revalidate();
        }
    }


    //  -> set plotHeight
    //  -> update preferred size
    //  -> redraw and revalidate scrollbars
    public void setPlotHeight(int plotHeight) {

        if (this.plotHeight != plotHeight) {


            //  -> set plotHeight
            this.plotHeight = plotHeight;
            this.plotSpacing = plotHeight + 10;
            int largestPlotWidth = data.getLongestPlotDuration();
            int timeSeriesEntries = data.getSegmentedTimeSeriesMap().size();


            //  -> update preferred size
            int width = (largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth;
            int height = plotHeight + (timeSeriesEntries * plotSpacing) + (margins.top + margins.bottom);

            setPreferredSize(new Dimension(width, height));


            //  -> redraw and revalidate scrollbars
            repaint();
            revalidate();
        }
    }


    // other methods

    //  -> Draws everything in the segment panel
    //  -> Triggered by calling repaint();
    public void paintComponent(Graphics g) {

        // TODO: 6/1/16 - clip the drawing to only the viewable area
        Rectangle2D viewableArea = this.getVisibleRect();

        // You will pretty much always do this for a vis
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        TreeMap<Double, TimeSeries> timeSeriesMap = data.getSegmentedTimeSeriesMap();
        TimeSeries referenceTimeSeries = data.getReferenceTimeSeries();
        ArrayList<Map.Entry<Double, TimeSeries>> entryList = new ArrayList<>(timeSeriesMap.entrySet());
        Collections.reverse(entryList);
        double referenceHeight = data.getReferenceValue();

        // Draw if segmented time series exists
        if (timeSeriesMap != null && !timeSeriesMap.isEmpty()) {
            double minBuildHeight = timeSeriesMap.firstKey();
            double maxBuildHeight = timeSeriesMap.lastKey();

            // Set color and translate pen away from the corner
            g2.setColor(Color.darkGray);
            g2.translate(margins.left, margins.top);
            int timeseriesCounter = 0;

            // set the location of labelsRectangle to the upper left-side corner
            labelsRectangle.x = margins.left;
            labelsRectangle.y = 0;

            buildHeightLabelRectangles.clear();
            labelsMap.clear();

            // for every time series in the (reverse order) entry list
            for (Map.Entry<Double, TimeSeries> entry : entryList) {
                // get current build height
                double buildHeight = entry.getKey();

                // Set the base line for the current build height time series
                double plotBaselineY = plotSpacing * timeseriesCounter;

                if (viewableArea.intersects(0, plotBaselineY + margins.top, data.getLongestPlotDuration() * plotTimeUnitWidth, plotSpacing)) {

                    // Get time series for current build height
                    TimeSeries timeSeries = entry.getValue();

                    // Create label for the current build height; Set width for label and draw
                    String label = df.format(buildHeight);
                    int stringWidth = g2.getFontMetrics().stringWidth(label) + 20;

                    if (!Double.isNaN(referenceHeight) && referenceHeight == buildHeight) {
                        g2.setFont(g2.getFont().deriveFont(Font.BOLD, g2.getFontMetrics().getFont().getSize() + 3));
                        g2.drawString(label, timeSeriesLabelWidth - stringWidth, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)));
                        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, g2.getFontMetrics().getFont().getSize() - 3));

                    } else {
                        g2.drawString(label, timeSeriesLabelWidth - stringWidth, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)));

                    }

                    Double distance = Double.MAX_VALUE;

                    if (data.getTimeSeriesDistances().containsKey(entry.getKey())) {
                        distance = data.getTimeSeriesDistances().get(entry.getKey());
                    }

                    if (distance > 1 && !distance.equals(Double.MAX_VALUE)) {
                        g2.setColor(Color.RED);
                    }
                    g2.draw3DRect(0, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)) + 3, timeSeriesLabelWidth - margins.right, 7, false);
                    g2.setColor(Color.BLACK);

                    double tmp = (1 - distance < 0) ? 0 : (1 - distance);
                    g2.setColor(Color.GRAY);
                    g2.fill3DRect(0, (int) (plotHeight + plotBaselineY - (plotSpacing / 2)) + 3, (int) (tmp * (timeSeriesLabelWidth - margins.right)), 7, false);
                    g2.setColor(Color.BLACK);

                    // put g2 past the label and at the bottom of the time series baseline and draw time series
                    g2.translate(timeSeriesLabelWidth, plotBaselineY);

                    if (referenceTimeSeries != null) {
                        g2.setColor(Color.LIGHT_GRAY);
                        drawTimeSeries(referenceTimeSeries, g2);
                        g2.setColor(Color.BLACK);
                    }

                    drawTimeSeries(timeSeries, g2);
                    g2.translate(-timeSeriesLabelWidth, -plotBaselineY);
                }

                // create rectangle for current label
                Rectangle2D.Double labelRect = new Rectangle2D.Double();
                labelRect.width = timeSeriesLabelWidth;
                labelRect.height = plotSpacing;
                labelRect.x = margins.left;
                labelRect.y = plotSpacing * timeseriesCounter;

                // add the current label to the array list
                buildHeightLabelRectangles.add(labelRect);
                labelsMap.put(labelRect, buildHeight);

                timeseriesCounter++;
            }

            // set height of the labelsRectagle
            labelsRectangle.width = timeSeriesLabelWidth;
            labelsRectangle.height = plotSpacing * timeseriesCounter;

            g2.translate(-margins.left, -margins.top);

        }
    }


    //  -> Actually draws the point and lines of the time series for the current build height
    private void drawTimeSeries (TimeSeries timeSeries, Graphics2D g2) {

        ChronoUnit chronoUnit = data.getChronoUnit();
        double minTimeSeriesValue = data.getMinTimeSeriesValue().getMinValue();
        double maxTimeSeriesValue = data.getMaxTimeSeriesValue().getMaxValue();

        // find the "time length" of the current time series
        long totalTimeUnits = 0;
        if(!timeSeries.getAllRecords().isEmpty()) {
            totalTimeUnits = chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

        // translate the "time length" to physical length of the current timeseries
        int plotWidth = ((int) totalTimeUnits + 1) * plotTimeUnitWidth;

        // Create a rectangle around the draw area
        Rectangle plotRectangle = new Rectangle(0, 0, plotWidth, plotHeight);

        Point2D.Double lastPoint = null;

        long totalPlotDeltaTime = 0;
        if(!timeSeries.getAllRecords().isEmpty()) {
            totalPlotDeltaTime = ChronoUnit.MILLIS.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

        // for each record in the current time series
        for (TimeSeriesRecord record : timeSeries.getAllRecords()) {

            // find amount of time since beginning of time series for current build height and set 'x' coordinate
            long deltaTime = ChronoUnit.MILLIS.between(timeSeries.getStartInstant(), record.instant);
            double normTime = (double) deltaTime / totalPlotDeltaTime;
            double x = normTime * plotWidth;

            // normalize the start point and set drawing start point and set 'y' coordinate
            double norm = (record.value - minTimeSeriesValue) / (maxTimeSeriesValue - minTimeSeriesValue);
            double yOffset = norm * plotRectangle.height;
            double y = plotRectangle.height - yOffset;

            // Create a new point at the (x, y) coordinate
            Point2D.Double point = new Point2D.Double(x, y);

            // Draw lines connecting the old and new point
            if (lastPoint != null) {

                // Draws step-wise line
                Line2D.Double line = new Line2D.Double(lastPoint.x, lastPoint.y, point.x, lastPoint.y);
                g2.draw(line);
                line = new Line2D.Double(point.x, lastPoint.y, point.x, point.y);
                g2.draw(line);
            }

            // Draw circle at new point
            Ellipse2D.Double circle = new Ellipse2D.Double(point.x - 1., point.y - 1., 2., 2.);
            g2.draw(circle);

            lastPoint = point;

        }
    }


    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {

        if (labelsRectangle.contains(e.getPoint())) {

            int spacing = (int) labelsRectangle.height / buildHeightLabelRectangles.size();

            int bin = e.getY() / spacing;

            Rectangle2D.Double temp = buildHeightLabelRectangles.get(bin);

            double height = labelsMap.get(temp);
            data.setReferenceValue(height);
        }
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


    // TalonDataListener methods
    @Override
    public void TalonDataPlgFileChange() {
//        log.debug("PLG File Change");
        repaint();
    }

    @Override
    public void TalonDataSegmentingVariableChange() {
//        log.debug("Segmenting Variable Change");
    }

    @Override
    public void TalonDataSegmentedVariableChange() {
//        log.debug("Segmented Variable Change");

        int largestPlotWidth = 0;
        int timeSeriesEntries = data.getSegmentedTimeSeriesMap().size();

        int width = 0;
        int height = 0;

        if(timeSeriesEntries != 0) {
            largestPlotWidth = data.getLongestPlotDuration();
            width = (largestPlotWidth * plotTimeUnitWidth) + (margins.left + margins.right) + timeSeriesLabelWidth;
            height = plotHeight + (timeSeriesEntries * plotSpacing) + (margins.top + margins.bottom);
        }

        setPreferredSize(new Dimension(width, height));
        repaint();
        revalidate();
    }

    @Override
    public void TalonDataReferenceValueChange() {
//        log.debug("Reference Value Change");
        repaint();
    }

    @Override
    public void TalonDataImageDirectoryChange() {
//        log.debug("Image Directory Change");
    }





    // =-= MAIN =-=
    public static void main(String[] args) {

        TalonData data = new TalonData(ChronoUnit.SECONDS);
        EventQueue.invokeLater(new Runnable() {
            public void run() {

                // create the panel
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // create the menu bar and items
                JMenuBar menuBar = new JMenuBar();
                JMenu file = new JMenu("File");
                JMenuItem open = new JMenuItem("Open");

                // add them to the panel
                file.add(open);
                menuBar.add(file);
                frame.setJMenuBar(menuBar);

                open.addActionListener(e -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select .plg to Open");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setMultiSelectionEnabled(false);
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PLG File", "plg"));
                    int retVal = fileChooser.showDialog(frame, "Open File");

                    // Test to see if a file was chosen and opened
                    if (retVal != JFileChooser.CANCEL_OPTION) {
                        data.setPlgFile(fileChooser.getSelectedFile());
                        data.setSegmentedVariableName("OPC.PowerSupply.Beam.BeamCurrent");
                    }
                });

                SegmentedTimeSeriesPanel segmentedTimeSeriesPanel = new SegmentedTimeSeriesPanel(data);

                segmentedTimeSeriesPanel.TalonDataPlgFileChange();

                JScrollPane scrollPane = new JScrollPane(segmentedTimeSeriesPanel);


                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(scrollPane, BorderLayout.CENTER);

                frame.setSize(new Dimension(200, 600));
                frame.setVisible(true);
            }
        });



    }
}
