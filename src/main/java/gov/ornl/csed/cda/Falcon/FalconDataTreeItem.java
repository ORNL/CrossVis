package gov.ornl.csed.cda.Falcon;

import java.io.File;
import java.io.Serializable;

/**
 * Created by csg on 12/30/15.
 */
public class FalconDataTreeItem implements Serializable{
    public File file;
    public FileType fileType;
    public String variableName;

    public enum FileType {CSV, PLG}

    public FalconDataTreeItem() {}

    public FalconDataTreeItem(File file, FileType fileType) {
        this.file = file;
        this.fileType = fileType;
    }

    public FalconDataTreeItem(File file, FileType fileType, String columnName) {
        this(file, fileType);
        this.variableName = columnName;
    }

    public String toString() {
        if (variableName != null) {
            return variableName;
        } else if (file != null) {
            return file.getName() + " (" + fileType + ")";
        }

        return "";
    }
}
