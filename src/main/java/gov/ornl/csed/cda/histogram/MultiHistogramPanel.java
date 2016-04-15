package gov.ornl.csed.cda.histogram;

import gov.ornl.csed.cda.Falcon.FalconFX;
import javafx.geometry.Orientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by csg on 3/3/16.
 */
public class MultiHistogramPanel extends JPanel {
    private final static Logger log = LoggerFactory.getLogger(MultiHistogramPanel.class);

    private int plotHeight;
    private Box panelBox;

    private Font fontAwesomeFont = null;

    // display preferences
    private boolean showButtonPanels = true;

    // color settings
    private Color binFillColor = Color.lightGray;
    private Color binLineColor = Color.darkGray;
    private Color highlightBinFillColor = Color.darkGray;
    private Color highlightBinLineColor = Color.black;

    private int binCount = 20;

    // panels
    ArrayList<ViewInfo> viewInfoList = new ArrayList<>();

    public MultiHistogramPanel(int plotHeight) {
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

        panelBox = new Box(BoxLayout.PAGE_AXIS);
        JScrollPane scrollPane = new JScrollPane(panelBox);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void rebuildBoxPanel() {
        panelBox.removeAll();
        for (ViewInfo viewInfo : viewInfoList) {
            panelBox.add(viewInfo.viewPanel);
        }
        revalidate();
        panelBox.repaint();
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
//        buttonPanel.add(settingsButton);
        buttonPanel.add(moveDownButton);

        if (fontAwesomeFont != null) {
            buttonPanel.setPreferredSize(new Dimension(40, 100));
            moveDownButton.setFont(fontAwesomeFont);
            moveDownButton.setText("\uf078");
            moveUpButton.setFont(fontAwesomeFont);
            moveUpButton.setText("\uf077");
            removeButton.setFont(fontAwesomeFont);
            removeButton.setText("\uf1f8");
//            settingsButton.setFont(fontAwesomeFont);
//            settingsButton.setText("\uf085");
        } else {
            moveUpButton.setText("Move Up");
            moveDownButton.setText("Move Down");
            removeButton.setText("Remove");
//            settingsButton.setText("Settings");
        }

        return buttonPanel;
    }

    public void setPlotHeight (int plotHeight) {
        if (this.plotHeight != plotHeight) {
            this.plotHeight = plotHeight;
            for (ViewInfo viewInfo : viewInfoList) {
                viewInfo.viewPanel.setPreferredSize(new Dimension(viewInfo.viewPanel.getPreferredSize().width, plotHeight));
                viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
                viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
            }
            revalidate();
            panelBox.repaint();
        }
    }

    public int getPlotHeight () {
        return plotHeight;
    }

    public HistogramPanel addHistogram(Histogram histogram) {
        ViewInfo viewInfo = new ViewInfo();
        viewInfoList.add(viewInfo);

        viewInfo.histogramPanel = new HistogramPanel(HistogramPanel.ORIENTATION.HORIZONTAL, HistogramPanel.STATISTICS_MODE.MEAN_BASED);
        viewInfo.histogramPanel.setBackground(getBackground());
        viewInfo.histogramPanel.setHistogram(histogram);
        viewInfo.histogramPanel.setPreferredSize(new Dimension(100, plotHeight));
        viewInfo.histogramPanel.setMinimumSize(new Dimension(100, plotHeight));
        viewInfo.histogramPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        viewInfo.histogramPanel.setBorder(BorderFactory.createTitledBorder(histogram.getName()));

        viewInfo.buttonPanel = createButtonPanel(viewInfo);

        viewInfo.viewPanel = new JPanel();
        viewInfo.viewPanel.setBackground(getBackground());
        viewInfo.viewPanel.setPreferredSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMinimumSize(new Dimension(100, plotHeight));
        viewInfo.viewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, plotHeight));
        viewInfo.viewPanel.setLayout(new BorderLayout());

        viewInfo.viewPanel.add(viewInfo.buttonPanel, BorderLayout.WEST);
        viewInfo.viewPanel.add(viewInfo.histogramPanel, BorderLayout.CENTER);
        viewInfo.viewPanel.setBorder(BorderFactory.createTitledBorder(histogram.getName()));

        panelBox.add(viewInfo.viewPanel);
        revalidate();

        return viewInfo.histogramPanel;
    }

    public HistogramPanel getHistogramPanel(Histogram histogram) {
        for (ViewInfo viewInfo : viewInfoList) {
            if (viewInfo.histogramPanel.getHistogram() == histogram) {
                return viewInfo.histogramPanel;
            }
        }
        return null;
    }

    public void removeHistogramPanel(HistogramPanel histogramPanel) {
        ViewInfo viewToRemove = null;

        for (ViewInfo viewInfo : viewInfoList) {
            if (viewInfo.histogramPanel == histogramPanel) {
                viewToRemove = viewInfo;
            }
        }

        if (viewToRemove != null) {
            viewInfoList.remove(viewToRemove);
            // TODO: Need to remove from group here and reset time range for the group
            rebuildBoxPanel();
        }
    }

    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Table table = null;
                try {
                    table = new CSVTableReader().readTable("data/csv/cars.csv");
                } catch (DataIOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                JFrame frame = new JFrame();
                frame.setSize(300, 1000);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                int binCount = (int) Math.floor(Math.sqrt(table.getTupleCount()));
                if (binCount < 1) {
                    binCount = 1;
                }

                MultiHistogramPanel multiHistogramPanel = new MultiHistogramPanel(120);
                multiHistogramPanel.setBackground(Color.white);
                multiHistogramPanel.setBorder(BorderFactory.createTitledBorder("MultiHistogramPanel"));

                JScrollPane scroller = new JScrollPane(multiHistogramPanel);
                scroller.getVerticalScrollBar().setUnitIncrement(2);
                scroller.getHorizontalScrollBar().setUnitIncrement(2);
                scroller.setBackground(frame.getBackground());

                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.addTab("Test", scroller);

                JPanel mainPanel = (JPanel)frame.getContentPane();
                mainPanel.setLayout(new BorderLayout());
                mainPanel.add(tabbedPane, BorderLayout.CENTER);

                frame.setVisible(true);

                for (int icol = 0; icol < table.getColumnCount(); icol++) {
                    Column column = table.getColumn(icol);
                    double values[] = new double[column.getRowCount()];
                    for (int i = 0; i < column.getRowCount(); i++) {
                        double value = column.getDouble(i);
                        if (!Double.isNaN(value)) {
                            values[i] = column.getDouble(i);
                        }
                    }

                    Histogram histogram = new Histogram(table.getColumnName(icol), values, binCount);
                    multiHistogramPanel.addHistogram(histogram);
                }
            }
        });
    }

    private class ViewInfo {
        public JPanel viewPanel;
        public HistogramPanel histogramPanel;
        public JPanel buttonPanel;
    }

}
