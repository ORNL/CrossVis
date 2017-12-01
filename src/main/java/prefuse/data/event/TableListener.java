package prefuse.data.event;

import java.util.EventListener;

import prefuse.data.Table;

/**
 * Listener interface for monitoring changes to a datamodel.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface TableListener extends EventListener {

    /**
     * Notification that a datamodel has changed.
     * @param t the datamodel that has changed
     * @param start the starting row index of the changed datamodel region
     * @param end the ending row index of the changed datamodel region
     * @param col the column that has changed, or
     * {@link EventConstants#ALL_COLUMNS} if the operation affects all
     * columns
     * @param type the type of modification, one of
     * {@link EventConstants#INSERT}, {@link EventConstants#DELETE}, or
     * {@link EventConstants#UPDATE}.
     */
    public void tableChanged(Table t, int start, int end, int col, int type);
    
} // end of interface TableListener