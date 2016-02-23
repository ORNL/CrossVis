package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by csg on 11/3/15.
 */
public class SegmentedTimeSeries extends JComponent {
    private final static Logger log = LoggerFactory.getLogger(SegmentedTimeSeries.class);

    private static double EPSILON = 0.0000001;

    private TreeMap<Double, TimeSeries> timeSeriesMap;
    private double maxTimeSeriesValue = Double.NaN;
    private double minTimeSeriesValue = Double.NaN;
    private int plotTimeUnitWidth = 2;
    private ChronoUnit chronoUnit;
    private int plotHeight = 60;
    private int plotSpacing = plotHeight + 10;
    private Insets margins = new Insets(4, 4, 4, 4);
    private int timeSeriesLabelWidth = 80;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public SegmentedTimeSeries(TreeMap<Double, TimeSeries> timeSeriesMap, ChronoUnit chronoUnit) {
        this.timeSeriesMap = timeSeriesMap;
        this.chronoUnit = chronoUnit;

        int largestPlotWidth = 0;

        if ((timeSeriesMap != null) && (!timeSeriesMap.isEmpty())) {
            for (Map.Entry<Double, TimeSeries> timeSeriesEntry : timeSeriesMap.entrySet()) {
                TimeSeries timeSeries = timeSeriesEntry.getValue();

                if (Double.isNaN(maxTimeSeriesValue)) {
                    maxTimeSeriesValue = timeSeries.getMaxValue();
                    minTimeSeriesValue = timeSeries.getMinValue();
                } else {
                    if (timeSeries.getMaxValue() > maxTimeSeriesValue) {
                        maxTimeSeriesValue = timeSeries.getMaxValue();
                    }
                    if (timeSeries.getMinValue() < minTimeSeriesValue) {
                        minTimeSeriesValue = timeSeries.getMinValue();
                    }
                }

                long totalTimeUnits = chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());
                int totalPlotWidth = ((int)totalTimeUnits + 1) * plotTimeUnitWidth;

                if (totalPlotWidth > largestPlotWidth) {
                    largestPlotWidth = totalPlotWidth;
                }
            }
        }

        setPreferredSize(new Dimension((largestPlotWidth + (margins.left + margins.right) + timeSeriesLabelWidth),
                (plotHeight + (timeSeriesMap.size() * plotSpacing) + (margins.top + margins.bottom))));
    }


    public void saveToImageFile (File imageFile) throws IOException {
        log.debug("writing image to file");

        int imageWidth = 600 * 4;
        int imageHeight = getHeight() * 4;

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setTransform(AffineTransform.getScaleInstance(4., 4.));

        paintComponent(g2);
        g2.dispose();

        ImageIO.write(image, "png", imageFile);

//        BufferedImage image = new BufferedImage(600, getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
////        ((Graphics2D)image.getGraphics()).setBackground(Color.white);
////        ((Graphics2D)image.getGraphics()).setForeground(Color.black);
//
//
//
//        paintComponent(image.getGraphics());
//        ImageIO.write(image, "png", imageFile);
        log.debug("finished writing image to file");
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        double minBuildHeight = timeSeriesMap.firstKey();
        double maxBuildHeight = timeSeriesMap.lastKey();

        g2.setColor(Color.darkGray);
        g2.translate(margins.left, margins.top);
        if ((timeSeriesMap != null) && (!timeSeriesMap.isEmpty())) {
            int timeseriesCounter = 0;

            ArrayList<Map.Entry<Double, TimeSeries>> entryList = new ArrayList<>(timeSeriesMap.entrySet());
            Collections.reverse(entryList);

//            for (Map.Entry<Double, TimeSeries> entry : timeSeriesMap.entrySet()) {
            for (Map.Entry<Double, TimeSeries> entry : entryList) {
                double buildHeight = entry.getKey();
//                double normHeight = (buildHeight - minBuildHeight) / (maxBuildHeight - minBuildHeight);
//                double yOffset = normHeight * getHeight();
//                double plotBaselineY = getHeight() - yOffset;

                double plotBaselineY = plotSpacing * timeseriesCounter;

                TimeSeries timeSeries = entry.getValue();

                String label = df.format(buildHeight);
                int stringWidth = g2.getFontMetrics().stringWidth(label) + 8;
                g2.drawString(label, timeSeriesLabelWidth - stringWidth, (int)(plotHeight + plotBaselineY - (plotSpacing / 2)));

                g2.translate(timeSeriesLabelWidth, plotBaselineY);
                drawTimeSeries(timeSeries, g2);
                g2.translate(-timeSeriesLabelWidth, -plotBaselineY);

                timeseriesCounter++;
            }
        }
        g2.translate(-margins.left, -margins.top);
    }

    private void drawTimeSeries (TimeSeries timeSeries, Graphics2D g2) {
        long totalTimeUnits = chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant());
        int plotWidth = ((int)totalTimeUnits + 1) * plotTimeUnitWidth;
        Rectangle plotRectangle = new Rectangle(0, 0, plotWidth, plotHeight);

        Point2D.Double lastPoint = null;
        for (TimeSeriesRecord record : timeSeries.getAllRecords()) {
            long deltaTime = chronoUnit.between(timeSeries.getStartInstant(), record.instant);
            double x = (double)(deltaTime * plotTimeUnitWidth) + (plotTimeUnitWidth / 2.);

            double norm = (record.value - minTimeSeriesValue) / (maxTimeSeriesValue - minTimeSeriesValue);
            double yOffset = norm * plotRectangle.height;
            double y = plotRectangle.height - yOffset;

            Point2D.Double point = new Point2D.Double(x, y);

            if (lastPoint != null) {
                Line2D.Double line = new Line2D.Double(lastPoint.x, lastPoint.y, point.x, lastPoint.y);
                g2.draw(line);
                line = new Line2D.Double(point.x, lastPoint.y, point.x, point.y);
                g2.draw(line);
//                Line2D.Double line = new Line2D.Double(lastPoint, point);
//                g2.draw(line);
            }

            Ellipse2D.Double circle = new Ellipse2D.Double(point.x - 1., point.y - 1., 2., 2.);
            g2.draw(circle);

            lastPoint = point;
        }

//        Path2D.Double valuePath = new Path2D.Double();
//        ArrayList<Point.Double> points = new ArrayList<>();
//
//        for (TimeSeriesRecord record : timeSeries.getAllRecords()) {
//            long deltaStartInstant = chronoUnit.between(timeSeries.getStartInstant(), record.instant);
//            int x = (int) deltaStartInstant * plotTimeUnitWidth;
//
//            double normValue = (record.value - minTimeSeriesValue) / (maxTimeSeriesValue - minTimeSeriesValue);
//            double yOffset = normValue * plotRectangle.height;
//            double valueY = plotRectangle.height - yOffset;
//
//            if (valuePath.getCurrentPoint() == null) {
//                valuePath.moveTo(x, valueY);
//            } else {
//                valuePath.lineTo(x, valueY);
//            }
//
//            points.add(new Point.Double(x, valueY));
//        }
//
//        g2.draw(valuePath);
//
//        for (int i = 0; i < points.size(); i++) {
//            Point.Double point = points.get(i);
//            Ellipse2D.Double ellipse = new Ellipse2D.Double(point.x-1, point.y-1, 2, 2);
////            g2.fill(ellipse);
//            g2.draw(ellipse);
//        }
    }

    public static void main (String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: java SegmentedTimeSeries <plg_file> <segment_variable> <value_variable>");
            System.exit(0);
        }
        File plgFile = new File(args[0]);

        ArrayList<String> varNames = new ArrayList<>();
        varNames.add(args[1]);
        varNames.add(args[2]);

        HashMap<String, TimeSeries> timeSeriesMap = PLGFileReader.readPLGFileAsTimeSeries(plgFile, varNames);

        TreeMap<Double, TimeSeries> segmentTimeSeriesMap = new TreeMap<>();

        TimeSeries segmentTimeSeries = timeSeriesMap.get(args[1]);
        TimeSeries valueTimeSeries = timeSeriesMap.get(args[2]);

        TimeSeriesRecord lastSegmentRecord = null;
        for (TimeSeriesRecord currentSegmentRecord : segmentTimeSeries.getAllRecords()) {
            if (lastSegmentRecord != null) {
                ArrayList<TimeSeriesRecord> segmentRecordList = valueTimeSeries.getRecordsBetween(lastSegmentRecord.instant, currentSegmentRecord.instant);
                TimeSeries timeSeries = new TimeSeries(String.valueOf(lastSegmentRecord.value));
                for (TimeSeriesRecord valueRecord: segmentRecordList) {
                    timeSeries.addRecord(valueRecord.instant, valueRecord.value, Double.NaN, Double.NaN);
                }
                segmentTimeSeriesMap.put(lastSegmentRecord.value, timeSeries);
            }

            lastSegmentRecord = currentSegmentRecord;
        }


        // create frame and panel
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                SegmentedTimeSeries segmentedTimeSeries = new SegmentedTimeSeries(segmentTimeSeriesMap, ChronoUnit.SECONDS);

                JMenuBar menuBar = new JMenuBar();
                frame.setJMenuBar(menuBar);
                JMenu menu = new JMenu("File");
                menuBar.add(menu);
                JMenuItem saveImageMenuItem = new JMenuItem("Save Image...");
                menu.add(saveImageMenuItem);

                saveImageMenuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Save to Image File");
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.setMultiSelectionEnabled(false);
                        int retVal = fileChooser.showDialog(frame, "Save");
                        if (retVal != JFileChooser.CANCEL_OPTION) {
                            try {
                                segmentedTimeSeries.saveToImageFile(fileChooser.getSelectedFile());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });

                JScrollPane scroller = new JScrollPane(segmentedTimeSeries);
                scroller.getVerticalScrollBar().setUnitIncrement(10);
                scroller.getHorizontalScrollBar().setUnitIncrement(10);
                ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                frame.setSize(1000, 300);
                frame.setVisible(true);
            }
        });
    }
}
