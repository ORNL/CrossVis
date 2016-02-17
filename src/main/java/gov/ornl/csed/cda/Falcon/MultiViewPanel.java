package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.HistogramPanel;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
    private Box panelBox;
    private Font fontAwesomeFont = null;

    ArrayList<JPanel> panelList = new ArrayList<JPanel>();
    HashMap<TimeSeries, ArrayList<JPanel>> timeSeriesPanelMap = new HashMap<>();

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

    public int getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(int plotHeight) {
        this.plotHeight = plotHeight;
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBorder(null);
        setBackground(Color.white);

        panelBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane scroller = new JScrollPane(panelBox);
        add(scroller, BorderLayout.CENTER);
//        detailsPanel = new JPanel();
//        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
//        JScrollPane detailsScroller = new JScrollPane(detailsPanel);
////        add(detailsScroller, BorderLayout.CENTER);
//
//        overviewPanel = new JPanel();
//        overviewPanel.setLayout(new BoxLayout(overviewPanel, BoxLayout.PAGE_AXIS));
//        JScrollPane overviewScroller = new JScrollPane(overviewPanel);
////        add(overviewScroller, BorderLayout.EAST);
//
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailsScroller, overviewScroller);
//        splitPane.setDividerLocation(700);
//        splitPane.setOneTouchExpandable(true);
//        add(splitPane, BorderLayout.CENTER);


//        overviewBox = new Box(BoxLayout.PAGE_AXIS);
//        ScrollPane overviewScroller = new ScrollPane(overviewBox);

//        detailsBox = new Box(BoxLayout.PAGE_AXIS);
//        add(overviewBox, BorderLayout.NORTH);
    }

    private void rebuildBoxPanel() {
        panelBox.removeAll();
        for (JPanel panel : panelList) {
            panelBox.add(panel);
        }
        revalidate();
    }

    private JPanel createButtonPanel (JPanel viewPanel) {
        JButton moveUpButton = new JButton();
        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = panelList.indexOf(viewPanel);
                if (index != 0) {
                    panelList.set(index, panelList.get(index - 1));
                    panelList.set(index - 1, viewPanel);
                    rebuildBoxPanel();
                }
            }
        });

        JButton moveDownButton = new JButton();
        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = panelList.indexOf(viewPanel);
                if (index != (panelList.size() - 1)) {
                    panelList.set(index, panelList.get(index + 1));
                    panelList.set(index+1, viewPanel);
                    rebuildBoxPanel();
                }
            }
        });

        JButton removeButton = new JButton();
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (panelList.remove(viewPanel)) {
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

    public void addTimeSeries(TimeSeries timeSeries) {
        JPanel viewPanel = new JPanel();
        viewPanel.setBackground(Color.white);
        viewPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = createButtonPanel(viewPanel);

        TimeSeriesPanel detailTimeSeriesPanel = new TimeSeriesPanel(1, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
        detailTimeSeriesPanel.setTimeSeries(timeSeries);
//        timeSeriesPanel.setPreferredSize(new Dimension(100, 80));
        JScrollPane detailsTimeSeriesScroller = new JScrollPane(detailTimeSeriesPanel);
        detailsTimeSeriesScroller.setPreferredSize(new Dimension(100, 100));
        detailsTimeSeriesScroller.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.LINE);
        overviewTimeSeriesPanel.setTimeSeries(timeSeries);
        overviewTimeSeriesPanel.setShowTimeRangeLabels(false);
        overviewTimeSeriesPanel.setBackground(Color.white);
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
                overviewTimeSeriesPanel.setHighlightRange(startHighlightInstant, endHighlightInstant);
            }
        });


        HistogramPanel histogramPanel = new HistogramPanel();

        ArrayList<TimeSeriesRecord> records = timeSeries.getAllRecords();
        double values[] = new double[records.size()];
        for (int i = 0; i < values.length; i++) {
            double value = records.get(i).value;
            if (!Double.isNaN(value)) {
                values[i] = value;
            }
        }
        Histogram histogram = new Histogram(timeSeries.getName(), values, histogramPanel.getBinCount());
        histogramPanel.setBackground(Color.white);
        histogramPanel.setHistogram(histogram);



        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setLayout(new GridLayout(2, 1));
//        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
        sidePanel.add(histogramPanel);
        sidePanel.add(overviewTimeSeriesPanel);
//        sidePanel.add(placeHolderPanel);
        sidePanel.setPreferredSize(new Dimension(200, 100));
        sidePanel.setBorder(BorderFactory.createTitledBorder("Overview"));


//        viewPanel
        viewPanel.add(detailsTimeSeriesScroller, BorderLayout.CENTER);
        viewPanel.add(sidePanel, BorderLayout.EAST);
        viewPanel.add(buttonPanel, BorderLayout.WEST);
        viewPanel.setBorder(BorderFactory.createTitledBorder(timeSeries.getName()));

        panelList.add(viewPanel);

        panelBox.add(viewPanel);
        revalidate();
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, sidePanel);
//        splitPane.setDividerLocation(700);

//        detailsTimeSeriesScroller.setBorder(BorderFactory.createMatteBorder(2,2,2,2, Color.BLUE));
//        sidePanel.setBorder(BorderFactory.createMatteBorder(2,2,2,2, Color.BLUE));
//        detailsPanel.add(detailsTimeSeriesScroller);
//        overviewPanel.add(sidePanel);

//        JPanel panel = new JPanel();
//        panel.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.gridheight = 2;
//        gbc.gridwidth = 2;
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.;
//        gbc.fill = GridBagConstraints.BOTH;
//        panel.add(scrollPane, gbc);
//
//        gbc.gridx = 2;
//        gbc.gridheight = 1;
//        gbc.gridwidth = 1;
//        gbc.weightx = 0.5;
//        panel.add(overviewTimeSeriesPanel, gbc);
//
//        gbc.gridy = 1;
//        panel.add(placeHolderPanel, gbc);

//        box.add(panel);
//        box.add(overviewTimeSeriesPanel);
//        box.add(splitPane);
//        revalidate();

//        log.debug("Num children panels: " + panelBox.getAccessibleContext().getAccessibleChildrenCount());
    }

    public static void main (String args[]) {
        int numTimeSeries = 3;
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
        }

//        TimeSeriesPanel overviewPanel = new TimeSeriesPanel(1, TimeSeriesPanel.PlotDisplayOption.LINE);
//        overviewPanel.setTimeSeries(timeSeriesList.get(0));
        MultiViewPanel multiViewPanel = new MultiViewPanel(100);
        JScrollPane scroller = new JScrollPane(multiViewPanel);


//        ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
        ((JPanel)frame.getContentPane()).add(multiViewPanel, BorderLayout.CENTER);
        frame.setSize(1000, 300);
        frame.setVisible(true);

        for (TimeSeries timeSeries : timeSeriesList) {
            multiViewPanel.addTimeSeries(timeSeries);
        }
    }
}
