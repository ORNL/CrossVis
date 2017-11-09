package gov.ornl.csed.cda.datatable;

public class ColumnSortRecord implements Comparable<ColumnSortRecord> {
	public QuantitativeColumn column;
	public double sortValue;

	public ColumnSortRecord(QuantitativeColumn column, double sortValue) {
		this.column = column;
		this.sortValue = sortValue;
	}

	public int compareTo(ColumnSortRecord that) {
		if (this.sortValue == that.sortValue) {
			return 0;
		} else if (this.sortValue < that.sortValue) {
			return 1;
		}
		return -1;
	}
}
