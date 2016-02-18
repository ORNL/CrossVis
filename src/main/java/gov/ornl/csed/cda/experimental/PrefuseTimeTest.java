package gov.ornl.csed.cda.experimental;

import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by csg on 12/29/15.
 */
public class PrefuseTimeTest {
    private final static Logger log = LoggerFactory.getLogger(PrefuseTimeTest.class);

    public static void main (String args[]) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String data = "data/csv/R1140_2015-01-30_15.06.small.csv";
                Table table = null;
                try {
                    table = new CSVTableReader().readTable(data);
                } catch (DataIOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }

                log.debug("Read " + table.getTupleCount() + " tuples with " + table.getColumnCount() + " columns");
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                TimeSeriesPanel detailsTimeSeriesPanel = new TimeSeriesPanel(2, ChronoUnit.SECONDS, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
                detailsTimeSeriesPanel.setBackground(Color.white);

                TimeSeriesPanel overviewTimeSeriesPanel = new TimeSeriesPanel(2, TimeSeriesPanel.PlotDisplayOption.STEPPED_LINE);
                overviewTimeSeriesPanel.setPreferredSize(new Dimension(1000, 100));
                overviewTimeSeriesPanel.setBackground(Color.white);
                Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                overviewTimeSeriesPanel.setBorder(border);

                JScrollPane scroller = new JScrollPane(detailsTimeSeriesPanel);
                scroller.getVerticalScrollBar().setUnitIncrement(10);
                scroller.getHorizontalScrollBar().setUnitIncrement(10);
                scroller.setBackground(frame.getBackground());
                scroller.setBorder(border);

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(scroller, BorderLayout.CENTER);
                ((JPanel)frame.getContentPane()).add(overviewTimeSeriesPanel, BorderLayout.SOUTH);

                frame.setSize(1000, 300);
                frame.setVisible(true);

                TimeSeries timeSeries = new TimeSeries("TS");

                for (int i = 0; i < table.getTupleCount(); i++) {
                    Instant instant = Instant.ofEpochMilli(table.getLong(i, 0));
                    double beamCurrentValue = table.getDouble(i, 10);

                    timeSeries.addRecord(instant, beamCurrentValue, Double.NaN, Double.NaN);
                }

                overviewTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());
                detailsTimeSeriesPanel.setTimeSeries(timeSeries, timeSeries.getStartInstant(), timeSeries.getEndInstant());

                scroller.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
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
//                            log.debug("scrollBarRight = " + scrollBarRight + " norm=" + norm);
//                            log.debug("scrollbar value=" + scrollBar.getModel().getValue() + " extent=" + scrollBar.getModel().getExtent() + " min=" + scrollBar.getModel().getMinimum() + " max=" + scrollBar.getModel().getMaximum());
//                            log.debug("highlight end instant = " + endHighlightInstant);

                    }
                });
            }
        });
    }
}
