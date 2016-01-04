package gov.ornl.csed.cda.datatable;

public class ColumnSortRecord implements Comparable<ColumnSortRecord> {
	public Column column;
	public double sortValue;

	public ColumnSortRecord(Column column, double sortValue) {
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
