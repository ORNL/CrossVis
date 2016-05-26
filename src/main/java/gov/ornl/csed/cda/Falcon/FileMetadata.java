package gov.ornl.csed.cda.Falcon;

import gov.ornl.csed.cda.timevis.TimeSeries;
import prefuse.data.Table;

import java.io.File;
import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class FileMetadata implements Serializable {
    public File file;
    public FileType fileType;
    public int timeColumnIndex;
    public ChronoUnit timeChronoUnit;
    public ArrayList<String> variableList = new ArrayList<>();
    public ArrayList<Integer> variableValueCountList = new ArrayList<>();
    public HashMap<String, TimeSeries> timeSeriesMap = new HashMap<>();
    public enum FileType {CSV, PLG}

    public FileMetadata(File file) {
        this.file = file;
    }
}
