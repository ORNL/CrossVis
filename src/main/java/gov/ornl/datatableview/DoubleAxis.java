package gov.ornl.datatableview;

import gov.ornl.datatable.DoubleColumn;

public class DoubleAxis extends UnivariateAxis {

    public DoubleAxis(DataTableView dataTableView, DoubleColumn column) {
        super(dataTableView, column);
    }

    public DoubleColumn doubleColumn () {
        return (DoubleColumn)getColumn();
    }
}
