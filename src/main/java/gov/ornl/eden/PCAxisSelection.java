package gov.ornl.eden;

import gov.ornl.datatable.ColumnSelectionRange;


/**
 * Created by csg on 11/26/14.
 */
public class PCAxisSelection {
    private ColumnSelectionRange selectionRange;
    private int maxPosition;
    private int minPosition;

    public PCAxisSelection() {
    	
    }
    
    public PCAxisSelection(ColumnSelectionRange selectionRange) {
        this.selectionRange = selectionRange;
    }

    public void setColumnSelectionRange(ColumnSelectionRange selectionRange) {
    	this.selectionRange = selectionRange;
    }
    
    public int getMinPosition() {
        return minPosition;
    }

    public void setMinPosition(int minPosition) {
        this.minPosition = minPosition;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public void setMaxPosition(int maxPosition) {
        this.maxPosition = maxPosition;
    }

    public ColumnSelectionRange getColumnSelectionRange() {
        return selectionRange;
    }
}
