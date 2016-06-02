/*
 *
 *  Class:  DistanceIndicatorPanel
 *
 *      Author:     whw
 *
 *      Created:    9 Mar 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  JComponent
 *
 *  Interfaces:     TalonDataListener
 *
 */

package gov.ornl.csed.cda.Talon;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.abs;
import static java.lang.Math.floor;


public class DistanceIndicatorPanel extends JComponent implements TalonDataListener {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(DistanceIndicatorPanel.class);





    // =-= INSTANCE FIELDS =-=
    private TalonData data = null;
    private int tickMarksize = 7;
    private int tickMarkSpacing;
    private int displayMode = 1;
    private DistancePanel minPanel = new DistancePanel(2);
    private DistancePanel maxPanel = new DistancePanel(1);





    // =-= CONSTRUCTOR =-=
    public DistanceIndicatorPanel(TalonData data) {

        this.data = data;
        data.addTalonDataListener(this);

        this.setLayout(new GridLayout(1, 2));

        this.add(minPanel, 0);
        this.add(maxPanel, 1);
    }





    // =-= INSTANCE METHODS =-=

    // other methods
    public void resetDisplay() {
        minPanel.setPreferredSize(new Dimension(this.getWidth()/2, this.getHeight()));
        maxPanel.setPreferredSize(new Dimension(this.getWidth()/2, this.getHeight()));
        minPanel.repaint();
        maxPanel.repaint();
    }


    // TalonDataListener methods
    @Override
    public void TalonDataPlgFileChange() {
//        log.debug("PLG File Change");
    }

    @Override
    public void TalonDataSegmentingVariableChange() {
//        log.debug("Segmenting Variable Change");
        resetDisplay();
    }

    @Override
    public void TalonDataSegmentedVariableChange() {
//        log.debug("Segmented Variable Change");
        resetDisplay();
    }

    @Override
    public void TalonDataReferenceValueChange() {
//        log.debug("Reference Value Change");
        resetDisplay();
    }

    @Override
    public void TalonDataImageDirectoryChange() {
//        log.debug("Image Directory Change");
    }





    // =-= NESTED CLASS =-=
    private class DistancePanel extends JComponent implements MouseMotionListener {





        // =-= INSTANCE FIELDS =-=
        private int option;
        private TreeMap<Integer, Map.Entry<Double, Double>> displayedDistances = new TreeMap<>();





        // =-= CONSTRUCTORS =-=
        public DistancePanel() {
            this.option = 0;
            addMouseMotionListener(this);
        }


        public DistancePanel(int option) {
            this.option = option;
            addMouseMotionListener(this);
        }





        // =-= INSTANCE METHODS =-=
        public void paintComponent(Graphics g) {

            // You will pretty much always do this for a vis
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (displayedDistances != null && !displayedDistances.isEmpty()) {
                displayedDistances.clear();
            }

            tickMarksize = 5;

            TreeMap<Double, Double> segmentDistanceMap = data.getTimeSeriesDistances();

            if (segmentDistanceMap != null && !segmentDistanceMap.isEmpty()) {
                int count = 1;

                tickMarkSpacing = this.getHeight() / segmentDistanceMap.size();

                segmentDistanceMap.size();

                // In order to print a tick mark per level tickMarkSpacing ≥ tickMarkSize
                if (tickMarkSpacing < tickMarksize) {

                    tickMarkSpacing = tickMarksize;
                    Map.Entry<Double, Double> me = segmentDistanceMap.firstEntry();
                    double max = me.getValue();
                    int combine = 1;
                    int numberOfBins = (int) floor(this.getHeight() / (float) tickMarksize);
                    int segmentsPerBin = segmentDistanceMap.size() / numberOfBins;
                    int segmentRemainder = segmentDistanceMap.size() % numberOfBins;

                    for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {

                        switch (option) {
                            case 1:
                                if ( (abs(max) < abs(entry.getValue()) && !Double.isNaN(entry.getValue())) || Double.isNaN(max)) {
                                    max = entry.getValue();
                                    me = entry;
                                }

                                if (Double.isNaN(max)) {
                                    g2.setColor(Color.LIGHT_GRAY);
                                } else {
                                    g2.setColor(getColor(max));
                                }

                                break;

                            case 2:
                                if ( (abs(max) > abs(entry.getValue()) && !Double.isNaN(entry.getValue())) || Double.isNaN(max)) {
                                    max = entry.getValue();
                                    me = entry;
                                }

                                if (Double.isNaN(max)) {
                                    g2.setColor(Color.LIGHT_GRAY);
                                } else {
                                    g2.setColor(getColor(max));
                                }

                                break;

                            default:
                                break;
                        }

                        if (combine % ((count <= segmentRemainder) ? segmentsPerBin + 1 : segmentsPerBin) != 0 && combine != segmentDistanceMap.size()) {
                            combine++;
                            continue;
                        }

                        displayedDistances.put(this.getHeight() - tickMarkSpacing * count, me);

                        if (displayMode == 0) {
                            g2.fillRect(0, this.getHeight() - tickMarkSpacing * count - this.getHeight() % numberOfBins, 15, tickMarksize);
                        } else {
                            Double lowerbound = (option == 1) ? data.getMedianDistance() : data.getLowerThreshold();
                            Double upperbound = (option == 1) ? data.getUpperThreshold() : data.getMedianDistance();

                            int offset = (int) (15 * (me.getValue() - lowerbound) / (upperbound - lowerbound));

                            offset = (offset < 0) ? 0 : offset;
                            offset = (offset > 14) ? 14 : offset;

                            if (option == 1) {
                                if (g2.getColor().equals(Color.LIGHT_GRAY)) {
                                    g2.fillRect(0, this.getHeight() - tickMarkSpacing * count - this.getHeight() % numberOfBins + 1, 15, tickMarksize);
                                } else {
                                    g2.fillRect(0, this.getHeight() - tickMarkSpacing * count - this.getHeight() % numberOfBins + 1, offset, tickMarksize);
                                }
                            } else {
                                g2.fillRect(offset, this.getHeight() - tickMarkSpacing * count - this.getHeight() % numberOfBins + 1, 15 - offset, tickMarksize);
                            }

                        }

                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawRect(0, this.getHeight() - tickMarkSpacing * count - this.getHeight() % numberOfBins, 15, tickMarksize);

                        me = segmentDistanceMap.higherEntry(entry.getKey());

                        if (me != null) {
                            max = me.getValue();
                        }

                        combine = 1;
                        count++;
                    }
                    
                } else {
                    int remainder = (int)(this.getHeight() % data.getSegmentedTimeSeriesMap().size());

                    for (Map.Entry<Double, Double> entry : segmentDistanceMap.entrySet()) {
                        tickMarksize = (count < remainder) ? (int)(this.getHeight() / (double)data.getSegmentedTimeSeriesMap().size())+1 : (int)(this.getHeight() / (double)data.getSegmentedTimeSeriesMap().size());
                        tickMarkSpacing = tickMarksize;

                        switch (option) {
                            case 1:
                                if (Double.isNaN(entry.getValue())) {
                                    g2.setColor(Color.LIGHT_GRAY);
                                } else {
                                    g2.setColor(getColor(entry.getValue()));
                                }
                                break;

                            case 2:
                                if (Double.isNaN(entry.getValue())) {
                                    g2.setColor(Color.LIGHT_GRAY);
                                } else {
                                    g2.setColor(getColor(entry.getValue()));
                                }
                                break;

                            default:
                                break;
                        }

                        int y = this.getHeight() - ((count < remainder) ? (count * ((int)(this.getHeight() / (double)data.getSegmentedTimeSeriesMap().size())+1)) : (remainder * ((int)(this.getHeight() / (double)data.getSegmentedTimeSeriesMap().size())+1) + (count - remainder) * ((int)(this.getHeight() / (double)data.getSegmentedTimeSeriesMap().size()))));
                        displayedDistances.put(y, entry);
//                        displayedDistances.put(this.getHeight() - tickMarkSpacing * count, entry);

                        if (displayMode == 0) {
//                            g2.fillRect(0, this.getHeight() - tickMarkSpacing * count, 15, tickMarksize);
                            g2.fillRect(0, y, 15, tickMarksize);
                        } else {
                            Double lowerbound = (option == 1) ? data.getMedianDistance() : data.getLowerThreshold();
                            Double upperbound = (option == 1) ? data.getUpperThreshold() : data.getMedianDistance();

                            int offset = (int) (15 * (entry.getValue() - lowerbound) / (upperbound - lowerbound));

                            offset = (offset < 0) ? 0 : offset;
                            offset = (offset > 14) ? 14 : offset;

                            if (option == 1) {
                                if (g2.getColor().equals(Color.LIGHT_GRAY)) {
//                                    g2.fillRect(0, this.getHeight() - tickMarkSpacing * count, 15, tickMarksize);
                                    g2.fillRect(0, y, 15, tickMarksize);
                                } else {
//                                    g2.fillRect(0, this.getHeight() - tickMarkSpacing * count, offset, tickMarksize);
                                    g2.fillRect(0, y, offset, tickMarksize);
                                }
                            } else {
//                                g2.fillRect(offset, this.getHeight() - tickMarkSpacing * count, 15 - offset, tickMarksize);
                                g2.fillRect(offset, y, 15 - offset, tickMarksize);
                            }

                        }

                        g2.setColor(Color.LIGHT_GRAY);
//                        g2.drawRect(0, this.getHeight() - tickMarkSpacing * count, 15, tickMarksize);
                        g2.drawRect(0, y, 15, tickMarksize);

                        count++;
                    }
                }

            }
        }


        public Color getColor(double value) {
            Color c;
            double norm;
            double median = data.getMedianDistance();
            double lowerThreshold = data.getLowerThreshold();
            double upperThreshold = data.getUpperThreshold();

            if (value == Double.NaN) {
                return null;
            }

            if (this.option == 1) {
                Color c1 = new Color(211, 37, 37); // high pos. corr.
                Color c0 = new Color(240, 240, 240); // low pos. corr.

                value = (value > upperThreshold) ? upperThreshold : value;
                value = (value < median) ? median : value;

                norm = (value - median) / (upperThreshold - median);

                int r = c0.getRed()
                        + (int) (norm * (c1.getRed() - c0.getRed()));
                int green = c0.getGreen()
                        + (int) (norm * (c1.getGreen() - c0.getGreen()));
                int b = c0.getBlue()
                        + (int) (norm * (c1.getBlue() - c0.getBlue()));
                c = new Color(r, green, b);

            } else {
                Color c1 = new Color(44, 110, 211/* 177 */); // high neg. corr.
                Color c0 = new Color(240, 240, 240);// low neg. corr.

                value = (value < lowerThreshold) ? lowerThreshold : value;
                value = (value > median) ? median : value;

                norm = (value - lowerThreshold) / (median - lowerThreshold);

                int r = c0.getRed()
                        + (int) ((1 - norm) * (c1.getRed() - c0.getRed()));
                int green = c0.getGreen()
                        + (int) ((1 - norm) * (c1.getGreen() - c0.getGreen()));
                int b = c0.getBlue()
                        + (int) ((1 - norm) * (c1.getBlue() - c0.getBlue()));
                c = new Color(r, green, b);

            }

            return c;
        }


        // MouseMotionListener methods
        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            setToolTipText("");

            int location = e.getY();

            if (displayedDistances.floorEntry(location) != null) {
                setToolTipText("Build Height: " + displayedDistances.floorEntry(location).getValue().getKey() + "\nDistance: " + displayedDistances.floorEntry(location).getValue().getValue());
            }
        }
    }





    // =-= MAIN =-=
}
