package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.HistogramPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import javafx.scene.paint.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by csg on 2/3/16.
 */
public class MultiViewPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(MultiViewPanel.class);

    private int plotHeight;
    private int plotUnitWidth = 1;
    private Box panelBox;
    private Font fontAwesomeFont = null;
    private boolean alignTimeSeries = false;
    private boolean linkPanelScrollBars = false;
    private Color dataColor = new Color(80, 80, 130, 180);
    private ArrayList<ViewInfo> viewInfoList = new ArrayList<ViewInfo>();

    private boolean showOverview = true;
    private Instant startInstant;
    private Instant endInstant;
    private ChronoUnit detailChronoUnit = ChronoUnit.SECONDS;
    private TimeSeriesPanel.PlotDisplayOption timeSeriesDisplayOption = TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE;

    public MultiViewPanel (int plotHeight) {
        this.plotHeight = plotHeight;

        InputStream is = FalconFX.class.getResourceAsStream("fontawesome-webfont.ttf");
        try {
            fontAwesomeFont = Font.createFont(Font.TRUETYPE_FONT, is);
            fontAwesomeFont = fontAwesomeFont.deriveFont(Font.PLAIN, 12F);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize();
    }

    public boolean getShowOverviewEnabled() {
        return showOverview;
    }

    public boolean getAlignTimeSeriesEnabled () {
        return alignTimeSeries;
    }

    public ChronoUnit getDetailChronoUnit() {
        return detailChronoUnit;
    }

    public void setDetailChronoUnit(ChronoUnit chronoUnit) {
        if (detailChronoUnit != chronoUnit) {
            detailChronoUnit = chronoUnit;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setPlotChronoUnit(detailChronoUnit);
            }
        }
    }

    public void setDataColor(Color color) {
        if (dataColor != color) {
            dataColor = color;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setDataColor(dataColor);
                viewInfo.overviewTimeSeriesPanel.setDataColor(dataColor);
            }
        }
    }

    public int getChronoUnitWidth() {
        return plotUnitWidth;
    }

    public void setChronoUnitWidth (int plotUnitWidth) {
        if (this.plotUnitWidth != plotUnitWidth) {
            this.plotUnitWidth = plotUnitWidth;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.detailTimeSeriesPanel.setPlotUnitWidth(plotUnitWidth);
            }
        }
    }

    public Color getDataColor() {
        return dataColor;
    }


    public void setAlignTimeSeriesEnabled (boolean enabled) {
        if (alignTimeSeries != enabled) {
            if (enabled) {
                // enabling aligned timeseries
                for (ViewInfo viewInfo : viewInfoList) {
                    viewInfo.detailTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
                    viewInfo.overviewTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
                }
            } else {
                // disabling aligned timeseries
                for (ViewInfo viewInfo : viewInfoList) {
                    viewInfo.detailTimeSeriesPanel.setDisplayTimeRange(viewInfo.timeSeries.getStartInstant(), viewInfo.timeSeries.getEndInstant());
                    viewInfo.overviewTimeSeriesPanel.setDisplayTimeRange(viewInfo.timeSeries.getStartInstant(), viewInfo.timeSeries.getEndInstant());
                }
            }

            alignTimeSeries = enabled;
        }
    }

    public int getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(int plotHeight) {
        this.plotHeight = plotHeight;
        for (ViewInfo viewInfo : viewInfoList) {
            viewInfo.viewPanel.setPreferredSize(new Dimension(viewInfo.viewPanel.getPreferredSize().width, plotHeight));
            viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
            viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        }
        revalidate();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(Color.white);

        panelBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane scroller = new JScrollPane(panelBox);
        add(scroller, BorderLayout.CENTER);
    }

    private void rebuildBoxPanel() {
        panelBox.removeAll();
        for (ViewInfo viewInfo : viewInfoList) {
            panelBox.add(viewInfo.viewPanel);
        }
        revalidate();
    }

    private JPanel createButtonPanel (ViewInfo viewInfo) {
        JButton moveUpButton = new JButton();
        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = viewInfoList.indexOf(viewInfo);
                if (index != 0) {
                    viewInfoList.set(index, viewInfoList.get(index - 1));
                    viewInfoList.set(index - 1, viewInfo);
                    rebuildBoxPanel();
                }
            }
        });

        JButton moveDownButton = new JButton();
        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = viewInfoList.indexOf(viewInfo);
                if (index != (viewInfoList.size() - 1)) {
                    viewInfoList.set(index, viewInfoList.get(index + 1));
                    viewInfoList.set(index+1, viewInfo);
                    rebuildBoxPanel();
                }
            }
        });

        JButton removeButton = new JButton();
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (viewInfoList.remove(viewInfo)) {
                    rebuildBoxPanel();
                }
            }
        });

        JButton settingsButton = new JButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setLayout(new GridLayout(4, 0, 0, 0));
        buttonPanel.add(moveUpButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(moveDownButton);

        if (fontAwesomeFont != null) {
            buttonPanel.setPreferredSize(new Dimension(40, 100));
            moveDownButton.setFont(fontAwesomeFont);
            moveDownButton.setText("\uf078");
            moveUpButton.setFont(fontAwesomeFont);
            moveUpButton.setText("\uf077");
            removeButton.setFont(fontAwesomeFont);
            removeButton.setText("\uf1f8");
            settingsButton.setFont(fontAwesomeFont);
            settingsButton.setText("\uf085");
        } else {
            moveUpButton.setText("Move Up");
            moveDownButton.setText("Move Down");
            removeButton.setText("Remove");
            settingsButton.setText("Settings");
        }

        return buttonPanel;
    }

    public void setShowOverviewEnabled(boolean enabled) {
        if (enabled != showOverview) {
            showOverview = enabled;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.sidePanel.setVisible(showOverview);
            }
        }
    }

    public void addTimeSeries(TimeSeries timeSeries) {
        if (viewInfoList.isEmpty()) {
            startInstant = Instant.from(timeSeries.getStartInstant());
            endInstant = Instant.from(timeSeries.getEndInstant());
        } else {
            boolean resetAllTimeSeriesRanges = false;
            if (timeSeries.getStartInstant().isBefore(startInstant)) {
                startInstant = Instant.from(timeSeries.getStartInstant());
                resetAllTimeSeriesRanges = true;
            }
            if (timeSeries.getEndInstant().isAfter(endInstant)) {
                endInstant = Instant.from(timeSeries.getEndInstant());
                resetAllTimeSeriesRanges = true;
            }

            if (resetAllTimeSeriesRanges && alignTimeSeries) {
                for (ViewInfo viewInfo : viewInfoList) {
                    viewInfo.detailTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
                    viewInfo.overviewTimeSeriesPanel.setDisplayTimeRange(startInstant, endInstant);
                }
            }
        }

        ViewInfo viewInfo = new ViewInfo();
        viewInfo.timeSeries = timeSeries;
        viewInfoList.add(viewInfo);

        viewInfo.viewPanel = new JPanel();
        viewInfo.viewPanel.setPreferredSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        viewInfo.viewPanel.setBackground(Color.white);
        viewInfo.viewPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = createButtonPanel(viewInfo);

        viewInfo.detailTimeSeriesPanel = new TimeSeriesPanel(1, detailChronoUnit, timeSeriesDisplayOption);
        if (alignTimeSeries) {
            viewInfo.detailTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
        } else {
            viewInfo.detailTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

//        timeSeriesPanel.setPreferredSize(new Dimension(100, 80));
        JScrollPane detailsTimeSeriesScroller = new JScrollPane(viewInfo.detailTimeSeriesPanel);
        detailsTimeSeriesScroller.setPreferredSize(new Dimension(100, 100));
        detailsTimeSeriesScroller.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        viewInfo.overviewTimeSeriesPanel = new TimeSeriesPanel(1, timeSeriesDisplayOption);
        viewInfo.overviewTimeSeriesPanel.setShowTimeRangeLabels(false);
        viewInfo.overviewTimeSeriesPanel.setBackground(Color.white);
        if (alignTimeSeries) {
            viewInfo.overviewTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
        } else {
            viewInfo.overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
        }

//        overviewTimeSeriesPanel.setPreferredSize(new Dimension(200, 100));
//        overviewTimeSeriesPanel.setBorder(BorderFactory.createTitledBorder("Overview"));
//        overviewTimeSeriesPanel.setPreferredSize(new Dimension(100, 50));
//        overviewTimeSeriesPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        detailsTimeSeriesScroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                JScrollBar scrollBar = (JScrollBar)e.getSource();
                double scrollBarModelWidth = scrollBar.getModel().getMaximum() - scrollBar.getModel().getMinimum();
                double norm = (double)scrollBar.getModel().getValue() / scrollBarModelWidth;
                double deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                Instant startHighlightInstant = timeSeries.getStartInstant().plusMillis((long)deltaTime);
                int scrollBarRight = scrollBar.getModel().getValue() + scrollBar.getModel().getExtent();
                norm = 1. - (double) scrollBarRight / (double) scrollBarModelWidth;
                deltaTime = norm * Duration.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()).toMillis();
                Instant endHighlightInstant = timeSeries.getEndInstant().minusMillis((long) deltaTime);
                viewInfo.overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
            }
        });


        viewInfo.histogramPanel = new HistogramPanel(HistogramPanel.ORIENTATION.HORIZONTAL);

        ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
        double values[] = new double[records.size()];
        for (int i = 0; i < values.length; i++) {
            double value = records.get(i).value;
            if (!Double.isNaN(value)) {
                values[i] = value;
            }
        }
        Histogram histogram = new Histogram(timeSeries.getName(), values, viewInfo.histogramPanel.getBinCount());
        viewInfo.histogramPanel.setBackground(Color.white);
        viewInfo.histogramPanel.setHistogram(histogram);

        viewInfo.sidePanel = new JPanel();
        viewInfo.sidePanel.setBackground(Color.WHITE);
        viewInfo.sidePanel.setLayout(new GridLayout(2, 1));
        viewInfo.sidePanel.add(viewInfo.histogramPanel);
        viewInfo.sidePanel.add(viewInfo.overviewTimeSeriesPanel);
        viewInfo.sidePanel.setPreferredSize(new Dimension(200, plotHeight));
        viewInfo.sidePanel.setBorder(BorderFactory.createTitledBorder("Overview"));
        viewInfo.sidePanel.setVisible(showOverview);

        viewInfo.viewPanel.add(detailsTimeSeriesScroller, BorderLayout.CENTER);
        viewInfo.viewPanel.add(viewInfo.sidePanel, BorderLayout.EAST);
        viewInfo.viewPanel.add(buttonPanel, BorderLayout.WEST);
        viewInfo.viewPanel.setBorder(BorderFactory.createTitledBorder(timeSeries.getName()));

        panelBox.add(viewInfo.viewPanel);
        revalidate();
    }

    public void drawToImage(File imageFile) throws IOException {
        int imageWidth = getWidth() * 4;
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

        paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", imageFile);
    }

    public static void main (String args[]) throws IOException {
        int numTimeSeries = 6;
        int numTimeSeriesRecords = 60*48;
        double minValue = -10.;
        double maxValue = 10.;
        double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
//        Instant endInstant = Instant.from(startInstant).plus(numTimeSeriesRecords + 50, ChronoUnit.MINUTES);

        ArrayList<TimeSeries> timeSeriesList = new ArrayList<>();
        for (int i = 0; i < numTimeSeries; i++) {
            double value = 0.;
            double uncertaintyValue = 0.;
            TimeSeries timeSeries = new TimeSeries("V"+i);
            for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
//                value = minValue + (itime * valueIncrement);
                timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
            }
            timeSeriesList.add(timeSeries);

            startInstant = startInstant.plus(60, ChronoUnit.MINUTES);
        }

//        TimeSeriesPanel overviewPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.LINE);
//        overviewPanel.setTimeSeries(timeSeriesList.get(0));
        MultiViewPanel multiViewPanel = new MultiViewPanel(150);
        multiViewPanel.setAlignTimeSeriesEnabled(true);
        JScrollPane scroller = new JScrollPane(multiViewPanel);


//        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);

        for (TimeSeries timeSeries : timeSeriesList) {
            multiViewPanel.addTimeSeries(timeSeries);
        }

//        multiViewPanel.setShowOverviewEnabled(false);
//        multiViewPanel.setShowOverviewEnabled(true);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.debug("Saving screen capture");
        File imageFile = new File("test.png");
        multiViewPanel.drawToImage(imageFile);
    }

    private class ViewInfo {
        public TimeSeries timeSeries;
        public JPanel viewPanel;
        public JPanel sidePanel;
        public TimeSeriesPanel detailTimeSeriesPanel;
        public TimeSeriesPanel overviewTimeSeriesPanel;
        public HistogramPanel histogramPanel;
    }
}
