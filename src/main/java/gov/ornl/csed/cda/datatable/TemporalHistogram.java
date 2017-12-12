package gov.ornl.csed.cda.datatable;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

public class TemporalHistogram extends Histogram {
    public Instant values[];
    private int numBins;
    private Duration binDuration;

    private int binCounts[];
    private int maxBinCount;

    private Instant startInstant;
    private Instant endInstant;
    private Duration histogramDuration;

    public TemporalHistogram(String name, Instant values[], int numBins) {
        super(name);

        this.values = values;
        this.numBins = numBins;

        calculate();
    }

    public TemporalHistogram(String name, Instant values[], int numBins, Instant startInstant, Instant endInstant) {
        super(name);

        this.values = values;
        this.numBins = numBins;
        this.startInstant = startInstant;
        this.endInstant = endInstant;

        calculate();
    }

    public int getBinCount(int i) {
        return binCounts[i];
    }

    public Instant getBinLowerBound(int i) {
        return startInstant.plus(binDuration.multipliedBy(i));
    }

    public Instant getBinUpperBound(int i) {
        return startInstant.plus(binDuration.multipliedBy(i + 1));
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    public int getNumBins() {
        return numBins;
    }

    public void setNumBins(int numBins) {
        this.numBins = numBins;
        calculate();
    }

    public void setValues (Instant values[]) {
        this.values = values;
        calculate();
    }

    public void setStartInstant(Instant startInstant) {
        this.startInstant = startInstant;
        calculate();
    }

    public void setEndInstant(Instant endInstant) {
        this.endInstant = endInstant;
        calculate();
    }

    public int getMaxBinCount() {
        return maxBinCount;
    }

    private void calculateRange() {
        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                startInstant = values[i];
                endInstant = values[i];
            } else {
                if (values[i].isBefore(startInstant)) {
                    startInstant = values[i];
                } else if (values[i].isAfter(endInstant)) {
                    endInstant = values[i];
                }
            }
        }
    }

    
    public void calculate() {
        if (startInstant == null || endInstant == null) {
            calculateRange();
        }

        histogramDuration = Duration.between(startInstant, endInstant);
        binDuration = histogramDuration.dividedBy(numBins);

        binCounts = new int[numBins];
        Arrays.fill(binCounts, 0);
        maxBinCount = 0;

        for (Instant value : values) {
            Duration valueOffsetDuration = Duration.between(startInstant, value);
            int binIndex = (int) (valueOffsetDuration.toMillis() / binDuration.toMillis());

            if (binIndex < 0) {
                // the value is smaller than the minValue
            } else if (binIndex >= numBins) {
                // if the value is equal to the max value increment the last bin
                if (value.equals(endInstant)) {
                    binCounts[numBins - 1]++;
                    if (binCounts[numBins - 1] > maxBinCount) {
                        maxBinCount = binCounts[numBins - 1];
                    }
                }
            } else {
                binCounts[binIndex]++;
                if (binCounts[binIndex] > maxBinCount) {
                    maxBinCount = binCounts[binIndex];
                }
            }
        }
    }
}
