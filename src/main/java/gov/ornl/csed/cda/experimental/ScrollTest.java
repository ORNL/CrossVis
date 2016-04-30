package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Created by csg on 4/21/16.
 */
public class ScrollTest {
    public static void main (String args[]) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                int numTimeSeriesRecords = 60*12;
                double minValue = -10.;
                double maxValue = 10.;
                double valueIncrement = (maxValue - minValue) / numTimeSeriesRecords;

                Instant startInstant = Instant.now().truncatedTo(ChronoUnit.HOURS);

                TimeSeries timeSeries = new TimeSeries("V");
                for (int itime = 0; itime <= numTimeSeriesRecords; itime++) {
                    Instant instant = Instant.from(startInstant).plus(itime, ChronoUnit.MINUTES);
//                value = Math.max(0., Math.min(10., value + .8 * Math.random() - .4 + .2 * Math.cos(itime + .2)));
                    double value = minValue + (itime * valueIncrement);
                    timeSeries.addRecord(instant, value, Double.NaN, Double.NaN);
                }

                TimeSeriesPanel timeSeriesPanel = new TimeSeriesPanel(10, ChronoUnit.MINUTES, TimeSeriesPanel.PlotDisplayOption.LINE);
                timeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                timeSeriesPanel.setBackground(Color.white);
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                timeSeriesPanel.setBorder(border);

                timeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
//
//                JScrollPane scrollPane = new JScrollPane(timeSeriesPanel) {
//                    public void paintComponent(Graphics g) {
//                        Graphics2D g2 = (Graphics2D)g;
//
//                        System.out.println("in scrollPane paintComponent");
//
//                        Rectangle bounds = getHorizontalScrollBar().getBounds();
//                        int top = (bounds.width / 2) - 2;
//                        int bottom = top + 4;
//
//                        for (int i = bounds.x; i < bounds.width; i+=4) {
//                            g2.setColor(Color.blue);
//                            g2.fillRect(i, top, 4, 4);
//                        }
//                    }
//                };
//                scrollPane.getHorizontalScrollBar().setOpaque(false);
////                JScrollPane scrollPane = new JScrollPane(timeSeriesPanel);

//                scrollPane.getViewport()
//                scrollPane.getHorizontalScrollBar().setUI(new MyScrollBarUI());

//                JScrollBar scrollBar = new JScrollBar() {
//                    public void paintComponent(Graphics g) {
//                        Graphics2D g2 = (Graphics2D)g;
//
//                        System.out.println("in scrollPane paintComponent");
//                        int top = (getHeight() / 2) - 1;
//                        int bottom = top + 2;
//
//                        for (int i = 0; i < getWidth(); i+=4) {
//                            g2.setColor(Color.blue);
//                            g2.fillRect(i, top, 2, 2);
//                        }
//                    }
//                };

//                scrollPane.setHorizontalScrollBar(scrollBar);

                LightScrollPane scrollPane = new LightScrollPane(timeSeriesPanel);


//                ScrollBarLayerUI scrollBarLayerUI = new ScrollBarLayerUI();
//                JLayer<JComponent> jLayer = new JLayer<JComponent>(scrollPane.getHorizontalScrollBar(), scrollBarLayerUI);

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                ((JPanel)frame.getContentPane()).add(scrollPane, BorderLayout.CENTER);
                frame.setSize(1000, 300);
                frame.setVisible(true);

            }
        });
    }
}
