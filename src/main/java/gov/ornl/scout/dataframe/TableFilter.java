package gov.ornl.scout.dataframe;

public interface TableFilter {
    public boolean accept(int rowIndex);
}
