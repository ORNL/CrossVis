package gov.ornl.csed.cda.experimental;

import com.fastdtw.timeseries.TimeSeries;

/**
 * Created by csg on 2/5/16.
 */
public class FastDTWTest {
    TimeSeries test;

    public static void main (String args[]) {
        TimeSeries test = new TimeSeries() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public int numOfDimensions() {
                return 0;
            }

            @Override
            public double getTimeAtNthPoint(int i) {
                return 0;
            }

            @Override
            public double getMeasurement(int i, int i1) {
                return 0;
            }

            @Override
            public double[] getMeasurementVector(int i) {
                return new double[0];
            }
        };
        System.out.println();
    }
}
