package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.histogram.Histogram;
import gov.ornl.csed.cda.histogram.HistogramPanel;
import gov.ornl.csed.cda.timevis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by csg on 4/18/16.
 */
public class SelectionDetailsPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(SelectionDetailsPanel.class);

    private int plotHeight;
    private int binCount;
    private Box panelBox;
    private Font fontAwesomeFont = null;

    private ArrayList<ViewInfo> viewInfoList = new ArrayList<>();
    private ArrayList<SelectionDetailsPanelListener> listeners = new ArrayList<>();

    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public SelectionDetailsPanel(int plotHeight, int binCount) {
        this.plotHeight = plotHeight;
        this.binCount = binCount;

        InputStream is = FalconMain.class.getResourceAsStream("fontawesome-webfont.ttf");
        try {
            fontAwesomeFont = Font.createFont(Font.TRUETYPE_FONT, is);
            fontAwesomeFont = fontAwesomeFont.deriveFont(Font.PLAIN, 12F);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initialize();

        panelBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int index = e.getPoint().y / plotHeight;
                if (viewInfoList.size() > index) {
                    TimeSeriesSelection selection = viewInfoList.get(index).timeSeriesSelection;
                    fireMouseClickedEvent(selection);
                    // find the position in the time series panel for the mid point of the selection
                    long midDeltaTime = (ChronoUnit.MILLIS.between(selection.getStartInstant(), selection.getEndInstant()) / 2);
                    Instant middleInstant = selection.getStartInstant().plusMillis(midDeltaTime);
                    int middleX = viewInfoList.get(index).detailsTimeSeriesPanel.getXForInstant(middleInstant);
                    int halfViewPortWidth = viewInfoList.get(index).timeSeriesPanelScrollPane.getViewport().getWidth() / 2;
                    viewInfoList.get(index).timeSeriesPanelScrollPane.getHorizontalScrollBar().setValue(middleX - halfViewPortWidth);
                    // change scroll position so that mid point is in center of view point
                }
            }
        });

        panelBox.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                int index = e.getPoint().y / plotHeight;
                if (viewInfoList.size() > index) {
                    fireMouseHoverEvent(viewInfoList.get(index).timeSeriesSelection);
                }
            }
        });
    }

    public void addSelectionDetailsPanelListener(SelectionDetailsPanelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean removeSelectionDetailsPanelListener(SelectionDetailsPanelListener listener) {
        return listeners.remove(listener);
    }

    private void fireMouseHoverEvent(TimeSeriesSelection timeSeriesSelection) {
        for (SelectionDetailsPanelListener listener : listeners) {
            listener.selectionDetailsPanelMouseHover(this, timeSeriesSelection);
        }
    }

    private void fireMouseClickedEvent(TimeSeriesSelection timeSeriesSelection) {
        for (SelectionDetailsPanelListener listener : listeners) {
            listener.selectionDetailsPanelMouseClicked(this, timeSeriesSelection);
        }
    }

    public int getPlotHeight() {
        return plotHeight;
    }

    public void setPlotHeight(int plotHeight) {

        if (this.plotHeight != plotHeight) {
            this.plotHeight = plotHeight;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.viewPanel.setPreferredSize(new Dimension(viewInfo.viewPanel.getPreferredSize().width, plotHeight));
                viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
                viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
                log.debug("setting viewPanel height to " + plotHeight);
            }
            panelBox.revalidate();
        }
    }

    public void setBinCount(int binCount) {
        if (this.binCount != binCount) {
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.histogramPanel.setBinCount(binCount);
            }
        }
    }

    public int getBinCount() {
        return binCount;
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(Color.white);

        panelBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane scroller = new JScrollPane(panelBox);
        add(scroller, BorderLayout.CENTER);
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
                    // TODO: Need to remove from group here and reset time range for the group
                    rebuildBoxPanel();
                    viewInfo.detailsTimeSeriesPanel.removeTimeSeriesSelection(viewInfo.timeSeriesSelection);
                    viewInfo.overviewTimeSeriesPanel.removeTimeSeriesSelection(viewInfo.timeSeriesSelection.getStartInstant(),
                            viewInfo.timeSeriesSelection.getEndInstant());
                }
            }
        });

        JButton saveButton = new JButton();
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get file to save to
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Save Selected Data to CSV File");
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(saveButton));
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File csvFile = chooser.getSelectedFile();

                    if (csvFile != null) {
                        // save data to file
                        try {
                            TimeSeriesRecord.writeRecordsToFile(csvFile, viewInfo.records);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

//        JButton settingsButton = new JButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(getBackground());
//        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        buttonPanel.setLayout(new GridLayout(4, 0, 0, 0));
        buttonPanel.add(moveUpButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(moveDownButton);

        if (fontAwesomeFont != null) {
            buttonPanel.setPreferredSize(new Dimension(50, 100));
            moveDownButton.setFont(fontAwesomeFont);
            moveDownButton.setText("\uf078");
            moveUpButton.setFont(fontAwesomeFont);
            moveUpButton.setText("\uf077");
            removeButton.setFont(fontAwesomeFont);
            removeButton.setText("\uf1f8");
            saveButton.setFont(fontAwesomeFont);
            saveButton.setText("\uf0c7");
//            settingsButton.setFont(fontAwesomeFont);
//            settingsButton.setText("\uf085");
        } else {
            moveUpButton.setText("Move Up");
            moveDownButton.setText("Move Down");
            removeButton.setText("Remove");
            saveButton.setText("Save");
//            settingsButton.setText("Settings");
        }

        return buttonPanel;
    }

    public void removeAllSelections() {
        viewInfoList.clear();
        rebuildBoxPanel();
    }

    public void addSelection(TimeSeriesPanel detailsTimeSeriesPanel, TimeSeriesPanel overviewTimeSeriesPanel, JScrollPane timeSeriesScrollPane, TimeSeriesSelection timeSeriesSelection) {
        // get the data in the selection range
        ArrayList<TimeSeriesRecord> records = detailsTimeSeriesPanel.getTimeSeries().getRecordsBetween(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());

        ViewInfo viewInfo = new ViewInfo();
        viewInfo.records = records;
        viewInfo.detailsTimeSeriesPanel = detailsTimeSeriesPanel;
        viewInfo.overviewTimeSeriesPanel = overviewTimeSeriesPanel;
        viewInfo.timeSeriesPanelScrollPane = timeSeriesScrollPane;
        viewInfo.timeSeriesSelection = timeSeriesSelection;

        // add to selection view
        int binCount = 20;
        double values[] = new double[records.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = records.get(i).value;
        }
        Histogram histogram = new Histogram(detailsTimeSeriesPanel.getTimeSeries().getName(), values, binCount);

        viewInfo.histogramPanel = new HistogramPanel(HistogramPanel.ORIENTATION.HORIZONTAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);
        viewInfo.histogramPanel.setBinCount(binCount);
        viewInfo.histogramPanel.setHistogram(histogram);
        viewInfo.histogramPanel.setBackground(getBackground());

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        viewInfo.startLabel = new JLabel("Start Instant: " + dtFormatter.format(timeSeriesSelection.getStartInstant()));
        viewInfo.startLabel.setFont(viewInfo.startLabel.getFont().deriveFont(10.f));
        viewInfo.startLabel.setOpaque(true);
        labelPanel.add(viewInfo.startLabel);
        viewInfo.endLabel = new JLabel("End Instant: " + dtFormatter.format(timeSeriesSelection.getEndInstant()));
        viewInfo.endLabel.setFont(viewInfo.endLabel.getFont().deriveFont(10.f));
        viewInfo.endLabel.setBackground(getBackground());
        labelPanel.add(viewInfo.endLabel);

        JPanel buttonPanel = createButtonPanel(viewInfo);

        viewInfo.viewPanel = new JPanel();
        viewInfo.viewPanel.setLayout(new BorderLayout());
        viewInfo.viewPanel.setBorder(BorderFactory.createTitledBorder(histogram.getName()));
        viewInfo.viewPanel.setPreferredSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        viewInfo.viewPanel.add(viewInfo.histogramPanel, BorderLayout.CENTER);
        viewInfo.viewPanel.add(labelPanel, BorderLayout.NORTH);
        viewInfo.viewPanel.add(buttonPanel, BorderLayout.WEST);
        viewInfo.viewPanel.setBackground(getBackground());

        viewInfoList.add(viewInfo);
        panelBox.add(viewInfo.viewPanel);
        revalidate();
    }

    public void updateSelection(TimeSeriesSelection timeSeriesSelection) {
        for (ViewInfo viewInfo : viewInfoList) {
            if (viewInfo.timeSeriesSelection == timeSeriesSelection) {
                viewInfo.startLabel.setText("Start Instant: " + dtFormatter.format(timeSeriesSelection.getStartInstant()));
                viewInfo.endLabel.setText("End Instant: " + dtFormatter.format(timeSeriesSelection.getEndInstant()));

                ArrayList<TimeSeriesRecord> records = viewInfo.detailsTimeSeriesPanel.getTimeSeries().getRecordsBetween(timeSeriesSelection.getStartInstant(), timeSeriesSelection.getEndInstant());
                int binCount = 20;
                double values[] = new double[records.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = records.get(i).value;
                }
                Histogram histogram = new Histogram(viewInfo.detailsTimeSeriesPanel.getTimeSeries().getName(), values, binCount);
                viewInfo.histogramPanel.setHistogram(histogram);

            }
        }
    }

    public void rebuildBoxPanel() {
        panelBox.removeAll();
        for (ViewInfo viewInfo : viewInfoList) {
            panelBox.add(viewInfo.viewPanel);
        }
        revalidate();
        panelBox.repaint();
    }

    public void deleteSelection(TimeSeriesSelection timeSeriesSelection) {
        ViewInfo viewToRemove = null;

        for (ViewInfo viewInfo : viewInfoList) {
            if (viewInfo.timeSeriesSelection == timeSeriesSelection) {
                viewToRemove = viewInfo;
            }
        }

        if (viewToRemove != null) {
            viewInfoList.remove(viewToRemove);
            viewToRemove.detailsTimeSeriesPanel.removeTimeSeriesSelection(viewToRemove.timeSeriesSelection);
            rebuildBoxPanel();
        }
    }

    public static void main (String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Random random = new Random(System.currentTimeMillis());

                int numTimeSeriesRecord = 60*48;
                double minValue = -10.;
                double maxValue = 10.;

                Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);
                Instant endInstant = Instant.from(startInstant).plus(numTimeSeriesRecord+120, ChronoUnit.SECONDS);

                TimeSeries timeSeries = new TimeSeries("Test");

                double value = 0.;

                for (int i = 120; i < numTimeSeriesRecord; i++) {
                    Instant instant = Instant.from(startInstant).plus(i, ChronoUnit.SECONDS);
                    instant = instant.plusMillis(random.nextInt(1000));
                    value = Math.max(-20., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(i + .2)));
                    double range = Math.abs(value) * .25;
                    double upperRange = value + range;
                    double lowerRange = value - range;
                    timeSeries.addRecord(instant, value, upperRange, lowerRange);
                }


                SelectionDetailsPanel selectionDetailsPanel = new SelectionDetailsPanel(120, 20);
                selectionDetailsPanel.setPreferredSize(new Dimension(200, 140));
                selectionDetailsPanel.setBackground(Color.white);

                NumericTimeSeriesPanel numericTimeSeriesPanel = new NumericTimeSeriesPanel(2, ChronoUnit.SECONDS, NumericTimeSeriesPanel.PlotDisplayOption.LINE);
                numericTimeSeriesPanel.setBackground(Color.white);
                numericTimeSeriesPanel.setTimeSeries(timeSeries, startInstant, endInstant);
                JScrollPane scroller = new JScrollPane(numericTimeSeriesPanel);
                numericTimeSeriesPanel.addTimeSeriesPanelSelectionListener(new TimeSeriesPanelSelectionListener() {
                    @Override
                    public void selectionCreated(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                        selectionDetailsPanel.addSelection(timeSeriesPanel, numericTimeSeriesPanel, scroller, timeSeriesSelection);
                    }

                    @Override
                    public void selectionMoved(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection,
                                               Instant previousStartInstant, Instant previousEndInstant) {
                        selectionDetailsPanel.updateSelection(timeSeriesSelection);
                    }

                    @Override
                    public void selectionDeleted(TimeSeriesPanel timeSeriesPanel, TimeSeriesSelection timeSeriesSelection) {
                        selectionDetailsPanel.deleteSelection(timeSeriesSelection);
                    }
                });



                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 300);

                JPanel mainPanel = (JPanel)frame.getContentPane();
                mainPanel.add(scroller, BorderLayout.CENTER);
                mainPanel.add(selectionDetailsPanel, BorderLayout.EAST);

                frame.setVisible(true);
            }
        });
    }

    private class ViewInfo {
        public ArrayList<TimeSeriesRecord> records;
        public TimeSeriesSelection timeSeriesSelection;
        public TimeSeriesPanel detailsTimeSeriesPanel;
        public TimeSeriesPanel overviewTimeSeriesPanel;
        public JScrollPane timeSeriesPanelScrollPane;
        public JPanel viewPanel;
        public JPanel buttonPanel;
        public JLabel startLabel;
        public JLabel endLabel;
        public HistogramPanel histogramPanel;
    }
}
