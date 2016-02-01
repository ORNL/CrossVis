package gov.ornl.csed.cda.Falcon;

import java.io.File;
import java.io.Serializable;

/**
 * Created by csg on 1/25/16.
 */
public class VariableClipboardData implements Serializable {
    FileMetadata fileMetadata;
    String variableName;

    public String toString() {
        return "[file: " + fileMetadata.file.getName() + " fileType: " + fileMetadata.fileType + " variableName: " + variableName + "]";
    }
}
