package gov.ornl.csed.cda.Falcon;

import java.io.File;
import java.io.Serializable;

/**
 * Created by csg on 1/25/16.
 */
public class VariableClipboardData implements Serializable {
    private File file;
    private FileMetadata.FileType fileType;
    private String variableName;

    public VariableClipboardData (File file, FileMetadata.FileType fileType, String variableName) {
        this.file = file;
        this.fileType = fileType;
        this.variableName = variableName;
    }

    public FileMetadata.FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileMetadata.FileType fileType) {
        this.fileType = fileType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String toString() {
        return "[VariableClipboardData: file=" + file.getName() + ", fileType=" + fileType + ", variableName=" + variableName + "]";
    }
}
