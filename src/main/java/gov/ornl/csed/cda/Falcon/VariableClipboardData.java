package gov.ornl.csed.cda.Falcon;

import java.io.File;
import java.io.Serializable;

/**
 * Created by csg on 1/25/16.
 */
public class VariableClipboardData implements Serializable {
    File file;
    FalconDataTreeItem.FileType fileType;
    String variableName;

    public String toString() {
        return "[file: " + file.getName() + " fileType: " + fileType + " variableName: " + variableName + "]";
    }
}
