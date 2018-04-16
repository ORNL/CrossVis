package gov.ornl.datatable;

import java.time.Duration;
import java.time.Instant;

public abstract class Histogram2DDimension {
    private final static double EPSILON = 0.000001;

    public abstract void setNumBins(int numBins);
    public abstract int getNumBins();
    public abstract int size();
    public abstract Object getValue(int index);
    public abstract int getBinIndex(Object value);

    public static class Temporal extends Histogram2DDimension {
        private Instant data[];
        private int numBins;
        private Duration totalDuration;
        private Duration binDuration;
        private Instant endInstant;
        private Instant startInstant;

        public Temporal(Instant data[], int numBins) {
            this.data = data;
            this.numBins = numBins;

            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    startInstant = Instant.from(data[i]);
                    endInstant = Instant.from(data[i]);
                } else {
                    if (data[i].isBefore(startInstant)) {
                        startInstant = Instant.from(data[i]);
                    } else if (data[i].isAfter(endInstant)) {
                        endInstant = Instant.from(data[i]);
                    }
                }
            }

            totalDuration = Duration.between(startInstant, endInstant);
            binDuration = totalDuration.dividedBy(numBins);
        }

        public Temporal(Instant data[], int numBins, Instant startInstant, Instant endInstant) {
            this.data = data;
            this.numBins = numBins;
            this.startInstant = Instant.from(startInstant);
            this.endInstant = Instant.from(endInstant);

            totalDuration = Duration.between(startInstant, endInstant);
            binDuration = totalDuration.dividedBy(numBins);
        }

        public int getBinIndex(Object value) {
            Instant instantValue = (Instant)value;

            Duration valueOffsetDuration = Duration.between(startInstant, instantValue);
            int binIndex = (int) (valueOffsetDuration.toMillis() / binDuration.toMillis());

            return binIndex;
        }

        public int size() {
            return data.length;
        }

        public int getNumBins() {
            return numBins;
        }

        public void setNumBins(int numBins) {
            this.numBins = numBins;
            binDuration = totalDuration.dividedBy(numBins);
        }

        public Object getValue(int index) {
            return data[index];
        }

        public Duration getBinDuration() {
            return binDuration;
        }

        public Duration getTotalDuration() {
            return totalDuration;
        }

        public Instant getBinLowerBound(int index) {
            return startInstant.plus(binDuration.multipliedBy(index));
        }

        public Instant getBinUpperBound(int index) {
            return startInstant.plus(binDuration.multipliedBy(index + 1));
        }

        public Instant getStartInstant() {
            return startInstant;
        }

        public void setStartInstant(Instant startInstant) {
            this.startInstant = Instant.from(startInstant);
            totalDuration = Duration.between(startInstant, endInstant);
            binDuration = totalDuration.dividedBy(numBins);
        }

        public Instant getEndInstant() {
            return endInstant;
        }

        public void setEndInstant(Instant endInstant) {
            this.endInstant = Instant.from(endInstant);
            totalDuration = Duration.between(startInstant, endInstant);
            binDuration = totalDuration.dividedBy(numBins);
        }
    }

    public static class Double extends Histogram2DDimension {
        private double data[];
        private int numBins;
        private double binSize;
        private double maxValue;
        private double minValue;

        public Double(double data[], int numBins) {
            this.data = data;
            this.numBins = numBins;

            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    minValue = data[i];
                    maxValue = data[i];
                } else {
                    if (data[i] < minValue) {
                        minValue = data[i];
                    }
                    if (data[i] > maxValue) {
                        maxValue = data[i];
                    }
                }
            }

            binSize = (maxValue - minValue) / numBins;
        }

        public Double(double data[], int numBins, double minValue, double maxValue) {
            this.data = data;
            this.numBins = numBins;
            this.minValue = minValue;
            this.maxValue = maxValue;

            binSize = (maxValue - minValue) / numBins;
        }

        public int getBinIndex(Object value) {
            double doubleValue = (double)value;
            int binIndex = (int)((doubleValue - minValue) / binSize);

            if (binIndex >= numBins) {
                if ((Math.abs(doubleValue - maxValue)) <= EPSILON) {
                    binIndex = numBins - 1;
                }
            }

            return binIndex;
        }

        public int size() {
            return data.length;
        }

        @Override
        public void setNumBins(int numBins) {
            this.numBins = numBins;
            binSize = (maxValue - minValue) / numBins;
        }

        public int getNumBins() {
            return numBins;
        }

        public Object getValue(int index) {
            return data[index];
        }

        public double getBinLowerBound(int index) {
            return minValue + (index * binSize);
        }

        public double getBinUpperBound(int index) {
            return minValue + ((index + 1) * binSize);
        }

        public double getMinValue() {
            return minValue;
        }

        public void setMinValue(double minValue) {
            this.minValue = minValue;
            binSize = (maxValue - minValue) / numBins;
        }

        public double getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(double maxValue) {
            this.maxValue = maxValue;
            binSize = (maxValue - minValue) / numBins;
        }
    }
}
