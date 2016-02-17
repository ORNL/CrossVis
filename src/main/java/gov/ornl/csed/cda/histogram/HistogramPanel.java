package gov.ornl.csed.cda.histogram;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by csg on 2/15/16.
 */
public class HistogramPanel extends JComponent implements ComponentListener {
    public static final int SUMMARY_STATS_DEFAULT_HEIGHT = 14;
    private int binCount = 20;
    private Histogram histogram;
    private Rectangle fullPlotRectangle;
    private Rectangle histogramPlotRectangle;
    private Rectangle summaryStatsRectangle;

    private ArrayList<Rectangle2D.Double> histogramBinRectangles;
    private ArrayList<Color> histogramBinColors;
    private int typicalValuePosition;
    private int dispersionRectangle;

    public HistogramPanel () {
        setMinimumSize(new Dimension(40,40));
        addComponentListener(this);
    }

    public void setBinCount(int binCount) {
        this.binCount = binCount;
        histogram.setBinCount(binCount);
        layoutPanel();
    }

    public int getBinCount() {
        return binCount;
    }

    public void setHistogram (Histogram histogram) {
        this.histogram = histogram;
        histogram.setBinCount(binCount);
        layoutPanel();
    }

    private void layoutPanel() {
        if (histogram != null) {
            int plotLeft = getInsets().left;
            int plotTop = getInsets().top;
            int plotBottom = getHeight() - (getInsets().bottom);
            int plotWidth = getWidth() - (getInsets().left + getInsets().right);
            fullPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, plotBottom - plotTop);

//            histogram.layoutAxis(plotRectangle.width, plotRectangle.height);

            int histogramPlotHeight = (int) (fullPlotRectangle.height - SUMMARY_STATS_DEFAULT_HEIGHT);
            histogramPlotRectangle = new Rectangle(plotLeft, plotTop, plotWidth, histogramPlotHeight);
            double binRectangleWidth = (double) histogramPlotRectangle.width / (double) binCount;

            summaryStatsRectangle = new Rectangle(plotLeft, (int) histogramPlotRectangle.getMaxY(), plotWidth, SUMMARY_STATS_DEFAULT_HEIGHT);

            EmpiricalDistribution empiricalDistribution = histogram.getDistributionStats();

            double maxN = 0.;
            for (SummaryStatistics summaryStatistics : empiricalDistribution.getBinStats()) {
                if (summaryStatistics.getN() > maxN) {
                    maxN = summaryStatistics.getN();
                }
            }

            histogramBinRectangles = new ArrayList<>();
            histogramBinColors = new ArrayList<>();
            int binNumber = 0;
            for (SummaryStatistics summaryStatistics : empiricalDistribution.getBinStats()) {
                double x = histogramPlotRectangle.x + (binNumber * binRectangleWidth);
                double normCount = summaryStatistics.getN() / maxN;
                double binHeight = normCount * histogramPlotRectangle.height;
                double y = (histogramPlotRectangle.y + histogramPlotRectangle.height) - binHeight;
                Rectangle2D.Double binRect = new Rectangle2D.Double(x, y, binRectangleWidth, binHeight);
                Color binColor = Color.gray;
                histogramBinRectangles.add(binRect);
                histogramBinColors.add(binColor);

                binNumber++;
            }
        }
        repaint();
    }



    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(getInsets().left, getInsets().top, getWidth()-(getInsets().left+getInsets().right), getHeight()-(getInsets().top+getInsets().bottom));

        if (histogram != null) {
//            g2.setColor(Color.blue);
//            g2.draw(fullPlotRectangle);
//
//            g2.setColor(Color.yellow);
//            g2.draw(histogramPlotRectangle);
//
//            g2.setColor(Color.cyan);
//            g2.draw(summaryStatsRectangle);
//            g2.translate(plotRectangle.x, plotRectangle.y);
//            histogram.draw(g2);
//            g2.translate(-plotRectangle.x, -plotRectangle.y);

            for (int i = 0; i < histogramBinRectangles.size(); i++) {
                g2.setColor(histogramBinColors.get(i));
                g2.fill(histogramBinRectangles.get(i));
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        layoutPanel();
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
                frame.setSize(500, 200);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                HistogramPanel histogramPanel = new HistogramPanel();
                histogramPanel.setBackground(Color.white);
                histogramPanel.setBorder(BorderFactory.createTitledBorder("Histogram Panel"));

                JPanel mainPanel = (JPanel)frame.getContentPane();
                mainPanel.setLayout(new BorderLayout());
                mainPanel.add(histogramPanel, BorderLayout.CENTER);

                frame.setVisible(true);

                int binCount = (int) Math.floor(Math.sqrt(table.getTupleCount()));
                if (binCount < 1) {
                    binCount = 1;
                }

                Column column = table.getColumn(0);
                double values[] = new double[column.getRowCount()];
                for (int i = 0; i < column.getRowCount(); i++) {
                    double value = column.getDouble(i);
                    if (!Double.isNaN(value)) {
                        values[i] = value;
                    }
                }
                Histogram histogram = new Histogram(table.getColumnName(0), values, binCount);
                histogramPanel.setHistogram(histogram);
            }
        });
    }
}
