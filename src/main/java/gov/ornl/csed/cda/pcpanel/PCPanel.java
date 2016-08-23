package gov.ornl.csed.cda.pcpanel;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.event.TableListener;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by csg on 1/4/16.
 */
public class PCPanel extends JComponent implements ComponentListener, MouseMotionListener, MouseListener, TableListener {
    private final static Logger log = LoggerFactory.getLogger(PCPanel.class);

    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");
    public final static int RESIZE_TIMER_DELAY = 200;

    // Data objects
    private Table table;
    private ArrayList<PCAxis> axisList;
    private ArrayList<Point2D.Double[]> tuplePolylines;

    // drawing settings
    private boolean antialiasEnabled = true;
    private boolean showPolylines = true;
    private boolean showHistograms = false;
    private Font axisNameFont = new Font("Dialog", Font.BOLD, 12);
    private Font labelFont = new Font("Dialog", Font.PLAIN, 10);
    public Color axisLineColor = new Color(120, 120, 120);
    public Color polylineColor = new Color(20, 20, 60, 50);
    private Timer waitingTimer;

    public PCPanel () {
        waitingTimer = new Timer(RESIZE_TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waitingTimer.stop();
                resizePanel();
            }
        });
        waitingTimer.setRepeats(false);
        addComponentListener(this);
    }


    public void setTable(Table table) {
        this.table = table;
        this.table.addTableListener(this);
        layoutPanel();
//        calculateHistograms();
        calculatePolylines();
        repaint();
    }


    public Table getTable() {
        return table;
    }


    public void removeTable() {
        table = null;
        layoutPanel();
        tuplePolylines = null;
        repaint();
    }


    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (antialiasEnabled) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        if (table == null || table.getColumnCount() == 0) {
            String msgString = "No PCPanel data to draw";
            int strLength = g2.getFontMetrics().stringWidth(msgString);
            Rectangle clipBounds = g2.getClipBounds();

            double strX = clipBounds.getCenterX() - (strLength / 2.);
            double strY = clipBounds.getCenterY() - (g2.getFontMetrics().getHeight() / 2.);

            g2.setColor(getForeground());
            g2.drawString(msgString, (int)strX, (int)strY);
        } else {
            if (showPolylines && tuplePolylines != null) {
                g2.setColor(polylineColor);
                g2.setStroke(new BasicStroke(2.f));
                for (Point2D.Double[] points : tuplePolylines) {
                    Path2D.Double polyline = new Path2D.Double();
                    polyline.moveTo(points[0].x, points[0].y);
                    for (int i = 1; i < points.length; i++) {
                        polyline.lineTo(points[i].x, points[i].y);
                    }
                    g2.draw(polyline);
                }
            }

            if (axisList != null) {
                for (int iaxis = 0; iaxis < axisList.size(); iaxis++) {
                    PCAxis axis = axisList.get(iaxis);

                    String columnName = table.getColumnName(axis.dataModelIndex);
                    g2.setFont(axisNameFont);

                    if (axis.labelRectangle == null) {
                        int axisNameWidth = g2.getFontMetrics().stringWidth(columnName);
                        int axisNameHeight = g2.getFontMetrics().getHeight();
                        if (iaxis % 2 == 0) {
                            axis.labelRectangle = new Rectangle(axis.xPosition - (axisNameWidth / 2), getInsets().top, axisNameWidth, axisNameHeight);
                        } else {
                            axis.labelRectangle = new Rectangle(axis.xPosition - (axisNameWidth / 2), getInsets().top + (int)(axisNameHeight * .8), axisNameWidth, axisNameHeight);
                        }
                    }

                    g2.setColor(Color.DARK_GRAY);
                    g2.drawString(table.getColumnName(axis.dataModelIndex), axis.labelRectangle.x,
                            axis.labelRectangle.y + axis.labelRectangle.height - g2.getFontMetrics().getDescent());

                    g2.setFont(labelFont);
                    String minLabel = DECIMAL_FORMAT.format(axis.statistics.getMin());
                    String maxLabel = DECIMAL_FORMAT.format(axis.statistics.getMax());
                    int stringHeight = g2.getFontMetrics().getHeight();
                    int stringWidth = g2.getFontMetrics().stringWidth(minLabel);
                    g2.drawString(minLabel, axis.xPosition - (stringWidth/2), axis.bottomPosition + g2.getFontMetrics().getAscent() + 2);
                    stringWidth = g2.getFontMetrics().stringWidth(maxLabel);
                    g2.drawString(maxLabel, axis.xPosition - (stringWidth/2), axis.topPosition - g2.getFontMetrics().getDescent());

                    g2.setColor(axisLineColor);
                    g2.setStroke(new BasicStroke(2.f));
                    g2.drawLine(axis.xPosition, axis.topPosition, axis.xPosition, axis.bottomPosition);
                    g2.drawLine(axis.xPosition - 4, axis.topPosition, axis.xPosition + 4, axis.topPosition);
                    g2.drawLine(axis.xPosition - 4, axis.bottomPosition, axis.xPosition + 4, axis.bottomPosition);
                }
            }
        }
    }

    private void layoutPanel() {
        if (table == null || table.getColumnCount() == 0) {
            return;
        }

        if (axisList == null) {
            axisList = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                Column column = table.getColumn(i);
                if (column.canGetDouble()) {
                    PCAxis axis = new PCAxis(column, i);

                    double data[] = new double[table.getTupleCount()];
                    for (int ituple = 0; ituple < table.getTupleCount(); ituple++) {
                        data[ituple] = table.getDouble(ituple, i);
                    }
                    axis.statistics = new DescriptiveStatistics(data);
                    axisList.add(axis);
                } else if (column.canGetFloat()) {
                    PCAxis axis = new PCAxis(column, i);

                    double data[] = new double[table.getTupleCount()];
                    for (int ituple = 0; ituple < table.getTupleCount(); ituple++) {
                        data[ituple] = table.getFloat(ituple, i);
                    }
                    axis.statistics = new DescriptiveStatistics(data);
                    axisList.add(axis);
                } else {
                    log.debug("class is " + table.get(0, i).getClass());
                }
            }
        } else {
            for (int i = 0; i < table.getColumnCount(); i++) {
                Column column = table.getColumn(i);
                for (PCAxis axis : axisList) {
                    if (axis.column == column) {
                        axis.dataModelIndex = i;
                        break;
                    }
                }
            }
        }

        int screenWidth = getWidth() - 1;
        int screenHeight = getHeight() - 1;

        int axisSpacing = (screenWidth - (getInsets().left + getInsets().right)) / axisList.size();

        int axisTop = getInsets().top + axisNameFont.getSize() + labelFont.getSize() + 16;
        int axisBottom = screenHeight - (getInsets().bottom + labelFont.getSize() + 2);
        int axisHeight = axisBottom - axisTop;

        for (int i = 0; i < axisList.size(); i++) {
            PCAxis axis = axisList.get(i);

            axis.xPosition = getInsets().left + (axisSpacing / 2) + (i * axisSpacing);
            axis.topPosition = axisTop;
            axis.bottomPosition = axisBottom;
            axis.axisHeight = axisHeight;
            axis.rectangle = new Rectangle(axis.xPosition - (axisSpacing / 2), axis.topPosition, axis.axisWidth, axis.axisHeight);
            axis.labelRectangle = null;
        }
    }

    private void calculateHistograms() {
        if (table != null) {
            int binCount = (int) Math.floor(Math.sqrt(table.getTupleCount()));
            if (binCount < 1) {
                binCount = 1;
            }

            for (PCAxis axis : axisList) {
                double data[] = new double[table.getTupleCount()];
                for (int i = 0; i < table.getTupleCount(); i++) {
                    data[i] = table.getDouble(i, axis.dataModelIndex);

                }
                axis.histogram = new EmpiricalDistribution(binCount);
                axis.histogram.load(data);

                log.debug("Histogram for " + table.getColumnName(axis.dataModelIndex));

                for (SummaryStatistics binStats : axis.histogram.getBinStats()) {
                    log.debug("bin - " + binStats.getN() + " [" + binStats.getMin() + ", " + binStats.getMax() + "]");
                }
            }
        }
    }

    private void calculatePolylines() {
        tuplePolylines = new ArrayList<Point2D.Double[]>();
        if (table != null && table.getTupleCount() > 0) {
            for (int ituple = 0; ituple < table.getTupleCount(); ituple++) {
                Tuple tuple = table.getTuple(ituple);
                Point2D.Double[] tuplePoints = new Point2D.Double[table.getColumnCount()];
                for (PCAxis axis : axisList) {
                    int x = axis.xPosition;
                    double value = tuple.getDouble(axis.dataModelIndex);
                    if (Double.isNaN(value)) {
                        log.debug("Found a NaN value");
                    }
                    double normValue = (value - axis.statistics.getMin()) /
                            (axis.statistics.getMax() - axis.statistics.getMin());
                    double y = axis.bottomPosition - (normValue * axis.axisHeight);
                    tuplePoints[axis.dataModelIndex] = new Point2D.Double(x, y);
                }
                tuplePolylines.add(tuplePoints);
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (waitingTimer == null) {
            waitingTimer.start();
        } else {
            waitingTimer.restart();
        }
        layoutPanel();
        calculatePolylines();
        repaint();
    }

    public void resizePanel() {
        if (axisList != null && !axisList.isEmpty()) {
            layoutPanel();
            calculatePolylines();
            repaint();
        }
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

    @Override
    public void mouseClicked(MouseEvent e) {

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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    public static void main(String args[]) throws Exception {
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
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                PCPanel pcPanel1 = new PCPanel();
                pcPanel1.setBorder(BorderFactory.createTitledBorder("No Scrolling"));
                pcPanel1.setBackground(Color.white);
                pcPanel1.setTable(table);

//                pcPanel.setTable(table);

//                JScrollPane scroller = new JScrollPane(pcPanel1);
//                scroller.getVerticalScrollBar().setUnitIncrement(2);
//                scroller.getHorizontalScrollBar().setUnitIncrement(2);
//
//                PCPanel pcPanel2 = new PCPanel();
//
//                JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroller, pcPanel2);
//                splitPane.setOneTouchExpandable(true);
//                splitPane.setDividerLocation(400);

                ((JPanel)frame.getContentPane()).setLayout(new BorderLayout());
                ((JPanel)frame.getContentPane()).add(pcPanel1, BorderLayout.CENTER);

                frame.setSize(1400, 800);
                frame.setVisible(true);
            }
        });
    }

    @Override
    public void tableChanged(Table table, int i, int i1, int i2, int i3) {

    }
}
