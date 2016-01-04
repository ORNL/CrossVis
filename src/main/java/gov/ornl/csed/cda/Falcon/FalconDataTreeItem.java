package gov.ornl.csed.cda.Falcon;

import java.io.File;
import java.io.Serializable;

/**
 * Created by csg on 12/30/15.
 */
public class FalconDataTreeItem implements Serializable{
    public File file;
    public String columnName;

    public FalconDataTreeItem() {}

    public FalconDataTreeItem(File file) {
        this.file = file;
    }

    public FalconDataTreeItem(File file, String columnName) {
        this(file);
        this.columnName = columnName;
    }

    public String toString() {
        if (columnName != null) {
            return columnName;
        } else if (file != null) {
            return file.getName();
        }

        return "";
    }
}
