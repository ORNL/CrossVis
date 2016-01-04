package gov.ornl.csed.cda.datatable;

import java.io.Serializable;

public class Column implements Serializable {

    private String name;
    private SummaryStats summaryStats = new SummaryStats();
    private SummaryStats focusSummaryStats = null;
    private boolean enabled = true;
    private boolean isDiscrete = false;

    public Column(String name) {
        this.name = name;
    }

    public SummaryStats getFocusSummaryStats () {
        return focusSummaryStats;
    }

    public void makeDiscrete() {
        isDiscrete = true;
    }

    public void makeContinuous() {
        isDiscrete = false;
    }

    public boolean isContinuous () {
        return !isDiscrete;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public void setFocusSummaryStats (SummaryStats focusSummaryStats) {
        this.focusSummaryStats = focusSummaryStats;
    }

    public SummaryStats getSummaryStats() {
        return summaryStats;
    }

    public void setSummaryStats(SummaryStats summaryStats) {
        this.summaryStats = summaryStats;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
