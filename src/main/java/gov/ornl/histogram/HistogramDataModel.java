package gov.ornl.histogram;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class HistogramDataModel {
    protected int binCounts[];
    protected int maxBinCountIndex = -1;

    public HistogramDataModel() { }

    public int getBinCount(int binIndex) {
        return binCounts[binIndex];
    }

    public abstract int getBinIndex(Object value);

    public abstract void setNumBins(int binCount);

    public int getNumBins() {
        if (binCounts != null) {
            return binCounts.length;
        }
        return 0;
    }

    public int getMaxBinCount() {
        if (maxBinCountIndex != -1) {
            return binCounts[maxBinCountIndex];
        }
        return 0;
    }

    protected void clearBinCounts() {
        binCounts = null;
        maxBinCountIndex = -1;
    }

    protected void reallocateBinCountsArray(int numBins) {
        binCounts = new int[numBins];
        Arrays.fill(binCounts, 0);
        maxBinCountIndex = 0;
    }

    protected void setBinCount(int binIndex, int count) {
        binCounts[binIndex] = count;
        if ((maxBinCountIndex == -1) || count > getMaxBinCount()) {
            maxBinCountIndex = binIndex;
        }
    }

    public int getMaxBinCountIndex() { return maxBinCountIndex; }
}
