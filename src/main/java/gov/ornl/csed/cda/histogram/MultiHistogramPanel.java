package gov.ornl.csed.cda.histogram;

import com.sun.xml.internal.ws.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by csg on 1/5/16.
 */
public class MultiHistogramPanel extends JComponent implements ComponentListener {
    private final static Logger log = LoggerFactory.getLogger(MultiHistogramPanel.class);

    public final static int DEFAULT_AXIS_LABEL_HEIGHT = 16;

    private Table table;
    private ArrayList<Histogram> histogramList;
    private int histogramPlotHeight = 60;
    private int plotSpacing = 10;
    private Rectangle panelPlotRectangle;

    public MultiHistogramPanel() {
        addComponentListener(this);
    }

    public void setTable(Table table) {
        this.table = table;
        layoutPanel();
        repaint();
    }

    public Table getTable() {
        return table;
    }

    public void removeTable() {
        table = null;
        repaint();
    }

    public void setHistogramPlotHeight (int height) {
        histogramPlotHeight = height;
        layoutPanel();
        repaint();
    }

    public int getHistogramPlotHeight () {
        return histogramPlotHeight;
    }

    private void calculateHistograms() {
        histogramList = new ArrayList<>();

        if (table != null) {
            int binCount = (int) Math.floor(Math.sqrt(table.getTupleCount()));
            if (binCount < 1) {
                binCount = 1;
            }

            for (int icol = 0; icol < table.getColumnCount(); icol++) {
                Column column = table.getColumn(icol);
                Histogram histogram = new Histogram(table, column, binCount);
                histogramList.add(histogram);
            }
        }
    }

    public void layoutPanel() {
        if (table != null) {
            if (histogramList == null) {
                calculateHistograms();
            }

            int panelWidth = getWidth() - (getInsets().left + getInsets().right);
            int panelHeight = (histogramList.size() * (histogramPlotHeight + plotSpacing)) + (getInsets().top + getInsets().bottom);

            if (panelWidth <= 0 || panelHeight <= 0) {
                return;
            }

            setPreferredSize(new Dimension(panelWidth, panelHeight));
            revalidate();

            panelPlotRectangle = new Rectangle(getInsets().left, getInsets().top, panelWidth,
                    (histogramList.size() * (histogramPlotHeight + plotSpacing)) - plotSpacing);

            for (int i = 0; i < histogramList.size(); i++) {
                Histogram histogram = histogramList.get(i);
                histogram.layoutAxis(panelPlotRectangle.width, histogramPlotHeight);
            }
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.green);
//        g2.drawRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left + getInsets().right), getHeight() - (getInsets().top + getInsets().bottom));
        g2.draw(panelPlotRectangle);

        if ((getWidth() <= 0) || (getHeight() <= 0)) {
            return;
        }

        Font normalFont = g2.getFont().deriveFont(Font.PLAIN, 10.f);
        g2.setFont(normalFont);

        if (histogramList != null) {
            for (int i = 0; i < histogramList.size(); i++) {
                Histogram histogram = histogramList.get(i);
                int plotYOffset = getInsets().top + (i * (histogramPlotHeight + plotSpacing));
                g2.translate(getInsets().left, plotYOffset);
                histogram.draw(g2);

                g2.translate(-getInsets().left, -plotYOffset);
            }
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
                frame.setSize(500, 1000);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                MultiHistogramPanel multiHistogramPanel = new MultiHistogramPanel();
                multiHistogramPanel.setBackground(Color.white);
                multiHistogramPanel.setBorder(BorderFactory.createTitledBorder("MultiHistogramPanel"));

                JScrollPane scroller = new JScrollPane(multiHistogramPanel);
                scroller.getVerticalScrollBar().setUnitIncrement(2);
                scroller.getHorizontalScrollBar().setUnitIncrement(2);
                scroller.setBackground(frame.getBackground());

                JPanel mainPanel = (JPanel)frame.getContentPane();
                mainPanel.setLayout(new BorderLayout());
                mainPanel.add(scroller, BorderLayout.CENTER);

                frame.setVisible(true);

                multiHistogramPanel.setTable(table);

            }
        });
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) { }

    @Override
    public void componentShown(ComponentEvent e) { }

    @Override
    public void componentHidden(ComponentEvent e) { }
}
