/*
 *
 *  Class:  TalonData
 *
 *      Author:     whw
 *
 *      Created:    11 May 2016
 *
 *      Purpose:    [A description of why this class exists.  For what
 *                  reason was it written?  Which jobs does it perform?]
 *
 *
 *  Inherits From:  N/A
 *
 *  Interfaces:     N/A
 *
 */

package gov.ornl.csed.cda.Talon;



import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;
import gov.ornl.csed.cda.Falcon.PLGFileReader;
import gov.ornl.csed.cda.Falcon.PLGVariableSchema;
import gov.ornl.csed.cda.timevis.TimeSeries;
import gov.ornl.csed.cda.timevis.TimeSeriesRecord;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class TalonData {





    // =-= CLASS FIELDS =-=
    private final static Logger log = LoggerFactory.getLogger(TalonData.class);

    // list of instances of TalonData.. not sure what this would be used for BUT we have it now
    private static ArrayList<TalonData> instances = new ArrayList<>();





    // =-= INSTANCE FIELDS =-=
    private ArrayList<TalonDataListener> talonDataListenerList = new ArrayList<>();

    private File plgFile = null;
    private ArrayList<String> listOfVariables = new ArrayList<>();
    private HashMap<String, PLGVariableSchema> variableSchemaMap = new HashMap<>();

    private ArrayList<File> imageFiles = new ArrayList<>();
    private ArrayList<Double> heightValues = new ArrayList<>();
    private ArrayList<Dimension> imageDimensions = new ArrayList<>();

    private String segmentedVariableName = "";
    private TimeSeries segmentedVariableTimeSeries = null;
    private TreeMap<Double, TimeSeries> segmentedTimeSeriesMap = new TreeMap<>();
    private TimeSeries maxTimeSeriesValue = null;
    private TimeSeries minTimeSeriesValue = null;
    private TimeSeries longestTimeSeries = null;
    private TimeSeries shortestTimeSeries = null;

    private String[] segmentingVariableNames = {"Builds.State.CurrentBuild.CurrentHeight",
            "Analyse.CurrentZLevel",
            "OPC.Table.CurrentPosition",
            "Process.TableControl.Position",
            "Process.TableControl.CalibratedPosition",
            "Process.TableControl.TargetMotorPosition",
            "Builds.State.CurrentBuild.LastLayer",
            "Builds.State.CurrentBuild.CurrentZLevel",
            "OPC.Table.TargetPosition",
            "Process.ServicePageControl.TotalMachineTime"};
    private String segmentingVariableName = "Builds.State.CurrentBuild.CurrentHeight";
    private TimeSeries segmentingVariableTimeSeries = null;

    private Double referenceValue = Double.NaN;
    private TimeSeries referenceTimeSeries = null;

    private TreeMap<Double, Double> timeSeriesDistances = new TreeMap<>();
    private double maxDistance = Double.NaN;
    private double minDistance = Double.NaN;
    private double medianDistance = Double.NaN;
    private double _25thQuartile = Double.NaN;
    private double _75thQuartile = Double.NaN;
    private double innerQuartileRange = Double.NaN;
    private double upperThreshold = Double.NaN;
    private double lowerThreshold = Double.NaN;

    private ChronoUnit chronoUnit = null;

    private TreeMap<String, Double> singleValueVariableValues = new TreeMap<>();





    // =-= CONSTRUCTOR =-=
    //  -> set the chrono unit
    //  -> add new instances to list of instances
    public TalonData(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;

        if (!instances.contains(this)) {
            instances.add(this);
        }
    }





    // =-= INSTANCE METHODS =-=

    // getters/setters

    public ArrayList<String> getListOfVariables() {
        return listOfVariables;
    }


    public File getPlgFile() {
        return plgFile;
    }


    public String getSegmentedVariableName() {
        return segmentedVariableName;
    }


    public Double getReferenceValue() {
        return referenceValue;
    }


    public TimeSeries getSegmentedVariableTimeSeries() {
        return segmentedVariableTimeSeries;
    }


    public TreeMap<Double, TimeSeries> getSegmentedTimeSeriesMap() {
        return segmentedTimeSeriesMap;
    }


    public TimeSeries getReferenceTimeSeries() {
        return referenceTimeSeries;
    }


    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }


    public TreeMap<Double, Double> getTimeSeriesDistances() {
        return timeSeriesDistances;
    }


    public TimeSeries getMinTimeSeriesValue() {
        return minTimeSeriesValue;
    }


    public TimeSeries getMaxTimeSeriesValue() {
        return maxTimeSeriesValue;
    }


    public double getMedianDistance() {
        return medianDistance;
    }


    public double get_25thQuartile() {
        return _25thQuartile;
    }


    public double get_75thQuartile() {
        return _75thQuartile;
    }


    public double getLowerThreshold() {
        return lowerThreshold;
    }


    public double getUpperThreshold() {
        return upperThreshold;
    }


    public double getMaxDistance() {
        return maxDistance;
    }


    public ArrayList<File> getImageFiles() {
        return imageFiles;
    }


    public ArrayList<Double> getHeightValues() {
        return heightValues;
    }


    public ArrayList<Dimension> getImageDimensions() {
        return imageDimensions;
    }


    public ArrayList<String> getSegmentingVariableNames() {
        ArrayList<String> tmp = new ArrayList<>();
        for (String str : segmentingVariableNames) {
            tmp.add(str);
        }

        Collections.sort(tmp);
        return tmp;
    }


    public TreeMap<String, Double> getSingleValueVariableValues() {
        return singleValueVariableValues;
    }

    //  -> set plgFile and clear all other fields
    //  -> read in variable schemas from the new plg file and populate related fields
    //  -> fire listener methods
    public void setPlgFile(File plgFile) {

        //  -> set plgFile and clear all other fields
        this.plgFile = plgFile;

        listOfVariables.clear();
        variableSchemaMap.clear();

        segmentedVariableName = "";
        segmentedVariableTimeSeries = null;
        segmentedTimeSeriesMap.clear();
        maxTimeSeriesValue = null;
        minTimeSeriesValue = null;
        longestTimeSeries = null;
        shortestTimeSeries = null;

        segmentingVariableTimeSeries = null;

        referenceValue = Double.NaN;
        referenceTimeSeries = null;

        timeSeriesDistances.clear();
        maxDistance = Double.NaN;
        minDistance = Double.NaN;
        medianDistance = Double.NaN;
        _25thQuartile = Double.NaN;
        _75thQuartile = Double.NaN;
        innerQuartileRange = Double.NaN;
        upperThreshold = Double.NaN;
        lowerThreshold = Double.NaN;


        //  -> read in variable schemas from the new plg file and populate related fields

        //  --> gather the schemas for all variables
        //  --> compile list of variable names of interest
        //  --> populate listOfVariables
        //  --> sort the listOfVariables
        //  --> load the time series of the segmenting variable
        try {

            //  --> gather the schemas for all variables
            variableSchemaMap = PLGFileReader.readVariableSchemas(plgFile);


            ArrayList<String> singleValueVariables = new ArrayList<>();
            HashMap<String, TimeSeries> singleValueVariablesSeries = new HashMap<>();


            //  --> compile list of variable names of interest
            for (PLGVariableSchema variableSchema : variableSchemaMap.values()) {
                if (variableSchema.typeString.equals("Int16") ||
                        variableSchema.typeString.equals("Double") ||
                        variableSchema.typeString.equals("Single") ||
                        variableSchema.typeString.equals("Int32")) {


                    //  --> populate listOfVariables
                    if (variableSchema.numValues > 10) { // todo - still need to decide what value this should really be/how & when to set it
                        listOfVariables.add(variableSchema.variableName);
                    }

                    String[] temp = variableSchema.variableName.split("[.]");

                    if (variableSchema.numValues == 1 &&
                            !temp[0].equals("Themes") &&
                            !temp[0].equals("Analyse") &&
                            !temp[0].equals("Process") &&
                            !temp[0].equals("Measurements")) {
                        singleValueVariables.add(variableSchema.variableName);
                    }
                }
            }


            //  --> sort the listOfVariables
            listOfVariables.add("");
            Collections.sort(listOfVariables);


            //  --> load the time series of the segmenting variable
            ArrayList<String> segmentingVariableName = new ArrayList<>();
            segmentingVariableName.add(this.segmentingVariableName);
            HashMap<String, TimeSeries> segmentingVariableTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, segmentingVariableName);

            if (!segmentingVariableName.isEmpty()) {
                this.segmentingVariableTimeSeries = segmentingVariableTimeSeries.get(this.segmentingVariableName);
            }


            singleValueVariablesSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, singleValueVariables);

            for (TimeSeries series : singleValueVariablesSeries.values()) {
                singleValueVariableValues.putIfAbsent(series.getName(), series.getMaxValue());
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }


        //  -> fire listener methods
        fireTalonDataPlgFileChange();
    }



    //  -> set segmentedVariableName
    //  -> read in the time series corresponding to segmentedVariableName
    //  -> set reference value to default and segment the time series
    //  -> calculate stats from segmented time series
    //  -> calculate distance for time series segments
    //  -> fire listener method
    public void setSegmentedVariableName(String segmentedVariableName) {

        //  -> set segmentedVariableName
        this.segmentedVariableName = segmentedVariableName;


        if(this.segmentedVariableName != null && !this.segmentedVariableName.isEmpty()) {

            //  -> read in the time series corresponding to segmentedVariableName
            ArrayList<String> segmentedVariableNameTmp = new ArrayList<>();
            segmentedVariableNameTmp.add(this.segmentedVariableName);
            HashMap<String, TimeSeries> segmentedVariableTimeSeries = null;

            try {
                segmentedVariableTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, segmentedVariableNameTmp);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (segmentedVariableTimeSeries != null && !segmentedVariableTimeSeries.isEmpty()) {
                this.segmentedVariableTimeSeries = segmentedVariableTimeSeries.get(this.segmentedVariableName);


                //  -> set reference value to default and segment the time series
                referenceValue = 0.1;
                segmentTimeSeries();


                //  -> calculate stats from segmented time series
                calculateTimeSeriesStats();


                referenceValue = findFirstDistance();


                //  -> clear distances and calculate distance for time series segments
                this.timeSeriesDistances.clear();
                calculateDistances();


            }

        } else {
            segmentedVariableTimeSeries = null;
            referenceValue = Double.NaN;
            referenceTimeSeries = null;
            segmentedTimeSeriesMap.clear();
            timeSeriesDistances.clear();
            maxDistance = Double.NaN;
            minDistance = Double.NaN;
            medianDistance = Double.NaN;
            _25thQuartile = Double.NaN;
            _75thQuartile = Double.NaN;
            innerQuartileRange = Double.NaN;
            upperThreshold = Double.NaN;
            lowerThreshold = Double.NaN;
            maxTimeSeriesValue = null;
            minTimeSeriesValue = null;
            longestTimeSeries = null;
            shortestTimeSeries = null;
        }


        //  -> fire listener method
        fireTalonDataSegmentedVariableChange();

        if (!segmentedVariableName.equals("")) {
            fireTalonDataReferenceValueChange();
        }
    }



    //  ->
    public void setSegmentingVariableName(String segmentingVariableName) {

        this.segmentingVariableName = segmentingVariableName;

        try {
            //  --> load the time series of the segmenting variable
            ArrayList<String> segmentingVariableNames = new ArrayList<>();
            segmentingVariableNames.add(this.segmentingVariableName);
            HashMap<String, TimeSeries> segmentingVariableTimeSeries = PLGFileReader.readPLGFileAsTimeSeries(plgFile, segmentingVariableNames);

            if (!segmentingVariableNames.isEmpty()) {
                this.segmentingVariableTimeSeries = segmentingVariableTimeSeries.get(this.segmentingVariableName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (segmentedVariableTimeSeries != null && segmentingVariableTimeSeries != null) {
            segmentTimeSeries();


            //  -> calculate stats from segmented time series
            calculateTimeSeriesStats();


            referenceValue = findFirstDistance();


            //  -> clear distances and calculate distance for time series segments
            this.timeSeriesDistances.clear();
            calculateDistances();

            fireTalonDataSegmentingVariableChange();
        }

    }



    //  -> set the reference value
    //  -> calculate distances
    //  -> clear distance map
    //  -> set the segmentedTimeSeriesMap
    //  -> fire listener method
    public void setReferenceValue(Double referenceValue) {

        //  -> set the reference value
        if (referenceValue.equals(this.referenceValue)) {
            this.referenceValue = Double.NaN;
        } else {
            this.referenceValue = referenceValue;
        }


        //  -> clear distance map
        this.timeSeriesDistances.clear();
        referenceTimeSeries = new TimeSeries("null");


        if (!this.referenceValue.isNaN()) {
            //  -> calculate distances
            calculateDistances();


            //  -> set the segmentedTimeSeriesMap
            referenceTimeSeries = segmentedTimeSeriesMap.get(referenceValue);
        }


        //  -> fire listener method
        fireTalonDataReferenceValueChange();
    }



    //  -> pull all the image files from the image directory
    //  -> get image dimensions and add
    //  -> derive segment value from the image file name and add
    //  -> fire listener method
    public void setImageDirectory(File imageDirectory) {

        //  -> pull all the image files from the image directory
        File[] files = imageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".png")) {
                    return true;
                }
                return false;
            }
        });


        String prefix = "Layer";
        String postfix = "Image";

        for (File imageFile : files) {
            try {

                //  -> get image dimensions and add
                BufferedImage image = ImageIO.read(imageFile);
                imageFiles.add(imageFile);
                Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
                imageDimensions.add(dimension);


                //  -> derive segment value from the image file name and add
                String heightString = imageFile.getName().substring(prefix.length(), imageFile.getName().indexOf(postfix));
                heightValues.add(Double.valueOf(heightString));
            } catch (IOException err) {
                err.printStackTrace();
            }
        }


        //  -> fire listener method
        fireTalonDataImageDirectoryChange();
    }



    // - Auxiliary Methods -

    //  -> get previous and current segment record of segmenting time series
    //  -> get all records of segmented time series between the two
    //  -> build time series from records
    //  -> add to the segmentedTimeSeriesMap
    private void segmentTimeSeries() {
        TimeSeriesRecord lastSegmentingRecord = null;
        TimeSeriesRecord lastSegmentedRecord = null;                            //<------------------------------------------


        //  -> get previous and current segment record of segmenting time series
        for (TimeSeriesRecord currentSegmentRecord : segmentingVariableTimeSeries.getAllRecords()) {


            //  -> get all records of segmented time series between the two
            if (lastSegmentingRecord != null) {
                ArrayList<TimeSeriesRecord> segmentRecordList = segmentedVariableTimeSeries.getRecordsBetween(lastSegmentingRecord.instant, currentSegmentRecord.instant);


                //  -> build time series from records
                if (segmentRecordList != null && !segmentRecordList.isEmpty()) {
                    TimeSeries timeSeries = new TimeSeries(String.valueOf(lastSegmentingRecord.value));

                    if (lastSegmentedRecord != null) {
                        timeSeries.addRecord(lastSegmentingRecord.instant, lastSegmentedRecord.value, Double.NaN, Double.NaN);
                    }

                    for (TimeSeriesRecord valueRecord : segmentRecordList) {
                        timeSeries.addRecord(valueRecord.instant, valueRecord.value, Double.NaN, Double.NaN);
                    }

                    timeSeries.addRecord(currentSegmentRecord.instant, segmentRecordList.get(segmentRecordList.size()-1).value, Double.NaN, Double.NaN);

                    lastSegmentedRecord = segmentRecordList.get(segmentRecordList.size()-1);

                    //  -> add to the segmentedTimeSeriesMap
                    segmentedTimeSeriesMap.put(lastSegmentingRecord.value, timeSeries);
                } else {
                    segmentedTimeSeriesMap.put(lastSegmentingRecord.value, new TimeSeries(String.valueOf(lastSegmentingRecord.value)));
                }
            }

            lastSegmentingRecord = currentSegmentRecord;
        }
    }



    //  -> build time series for the reference value
    //  -> find the distance form the reference value time series to all
    //  -> add distances to timeSeriesDistances
    //  -> calculate distance statistics
    private void calculateDistances() {

        com.fastdtw.timeseries.TimeSeries referenceDTWtimeSeries;
        TimeSeriesBase.Builder buildTemp1 = TimeSeriesBase.builder();

        if (segmentedTimeSeriesMap.get(referenceValue).getStartInstant() != null) {

            referenceTimeSeries = segmentedTimeSeriesMap.get(referenceValue);


            //  -> build time series for the reference value
            for (TimeSeriesRecord record : referenceTimeSeries.getAllRecords()) {
                buildTemp1.add((double) record.instant.toEpochMilli(), record.value);
            }

            referenceDTWtimeSeries = buildTemp1.build();


            //  -> find the distance form the reference value time series to all others
            for (Map.Entry<Double, TimeSeries> segment : segmentedTimeSeriesMap.entrySet()) {

                if (segment.getValue().getAllRecords().isEmpty()) {
                    timeSeriesDistances.put(segment.getKey(), Double.NaN);      // <---------------------------------------------------------------
                    continue;
                }

                com.fastdtw.timeseries.TimeSeries ts2;
                TimeSeriesBase.Builder buildTemp2 = TimeSeriesBase.builder();

                for (TimeSeriesRecord record : segment.getValue().getAllRecords()) {
                    buildTemp2.add((double) record.instant.toEpochMilli(), record.value);
                }

                ts2 = buildTemp2.build();


                //  -> add distances to timeSeriesDistances
                if (!(referenceDTWtimeSeries.size() == 1 && ts2.size() == 1)) {
                    double distance = FastDTW.compare(referenceDTWtimeSeries, ts2, 10, Distances.EUCLIDEAN_DISTANCE).getDistance();
                    timeSeriesDistances.put(segment.getKey(), distance);
                }
            }
        }


        //  -> calculate distance statistics
        calculateDistanceStatistics();
    }



    //  -> add distances to stats variable
    //  -> call the stats functions
    //  -> ensure that upperThreshold will not equal 0
    //  -> scale all of the statistics by dividing by upperthreshold
    private void calculateDistanceStatistics() {
        DescriptiveStatistics stats = new DescriptiveStatistics();


        //  -> add distances to stats variable
        for (Map.Entry<Double, Double> entry : timeSeriesDistances.entrySet()) {

            if(!Double.isNaN(entry.getValue())) {
                stats.addValue(entry.getValue());
            }

        }


        //  -> call the stats functions
        _75thQuartile = stats.getPercentile(75);
        _25thQuartile = stats.getPercentile(25);
        medianDistance = stats.getPercentile(50);
        maxDistance = stats.getMax();
        minDistance = stats.getMin();
        innerQuartileRange = _75thQuartile - _25thQuartile;

        double tmp = _75thQuartile + 1.5 * (innerQuartileRange);
        upperThreshold = (tmp > maxDistance) ? maxDistance : tmp;

        tmp = _25thQuartile - 1.5 * (innerQuartileRange);
        lowerThreshold = (tmp < 0) ? 0 : tmp;


        //  -> ensure that upperThreshold will not equal 0
        if (upperThreshold == 0) {
            upperThreshold = maxDistance;
        }

        if (maxDistance == 0) {
            return;
        }


        //  -> scale all of the statistics by dividing by upperthreshold
        for (Map.Entry<Double, Double> entry : timeSeriesDistances.entrySet()) {

            timeSeriesDistances.replace(entry.getKey(), entry.getValue(), entry.getValue() / upperThreshold);

        }

        _75thQuartile = _75thQuartile / upperThreshold;
        _25thQuartile = _25thQuartile / upperThreshold;
        medianDistance = medianDistance / upperThreshold;
        maxDistance = maxDistance / upperThreshold;
        minDistance = minDistance / upperThreshold;
        innerQuartileRange = innerQuartileRange / upperThreshold;
        lowerThreshold = lowerThreshold / upperThreshold;
        upperThreshold = 1;
    }



    //  -> clear old stats
    //  -> initialize new stats
    //  -> calculate new stats
    private void calculateTimeSeriesStats() {


        //  -> clear old stats
        maxTimeSeriesValue = null;
        minTimeSeriesValue = null;
        longestTimeSeries = null;
        shortestTimeSeries = null;
        int totalTimeUnitsLong = Integer.MIN_VALUE;
        int totalTimeUnitsShort = Integer.MAX_VALUE;
        int totalTimeUnitsTmp = 0;

        if ((segmentedTimeSeriesMap != null) && (!segmentedTimeSeriesMap.isEmpty())) {
            for (Map.Entry<Double, TimeSeries> timeSeriesEntry : segmentedTimeSeriesMap.entrySet()) {
                TimeSeries timeSeries = timeSeriesEntry.getValue();


                //  -> initialize new stats
                if (maxTimeSeriesValue == null) {
                    maxTimeSeriesValue = timeSeries;
                    minTimeSeriesValue = timeSeries;
                    longestTimeSeries = timeSeries;
                    shortestTimeSeries = timeSeries;
                    totalTimeUnitsLong = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;
                    totalTimeUnitsShort = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;

                } else {


                    //  -> calculate new stats
                    maxTimeSeriesValue = (timeSeries.getMaxValue() > maxTimeSeriesValue.getMaxValue()) ? timeSeries : maxTimeSeriesValue;
                    minTimeSeriesValue = (timeSeries.getMinValue() < minTimeSeriesValue.getMinValue()) ? timeSeries : minTimeSeriesValue;

                    if(timeSeries.getStartInstant() == null) {
                        continue;
                    }
                    totalTimeUnitsTmp = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;

                    longestTimeSeries = (totalTimeUnitsTmp > totalTimeUnitsLong) ? timeSeries : longestTimeSeries;
                    shortestTimeSeries = (totalTimeUnitsTmp < totalTimeUnitsShort) ? timeSeries : shortestTimeSeries;

                    totalTimeUnitsLong = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;
                    totalTimeUnitsShort = (int) chronoUnit.between(timeSeries.getStartInstant(), timeSeries.getEndInstant()) + 1;
                }
            }
        }
    }



    //  -> returns the number of chrono units in the shortest segmented time series
    public int getShortestPlotDuration() {
        return (int) chronoUnit.between(shortestTimeSeries.getStartInstant(), shortestTimeSeries.getEndInstant()) + 1;
    }



    //  -> returns the number of chrono units in the longest segmented time series
    public int getLongestPlotDuration() {
        return (int) chronoUnit.between(longestTimeSeries.getStartInstant(), longestTimeSeries.getEndInstant()) + 1;
    }



    private double findFirstDistance() {
        for (Map.Entry<Double, TimeSeries> entry : segmentedTimeSeriesMap.entrySet()) {
            if(entry.getValue().getStartInstant() != null && !entry.getKey().equals(segmentedTimeSeriesMap.firstKey())) {
                double tmp = entry.getKey();
                return entry.getKey();
            }
        }

        return segmentedTimeSeriesMap.firstKey();
    }



    // - TalonDataListener Helper Methods -

    //  -> add new TalonDataListener to list
    public boolean addTalonDataListener(TalonDataListener l) {

        if (!talonDataListenerList.contains(l)) {
            return talonDataListenerList.add(l);
        } else {
            return false;
        }
    }


    //  -> remove TalonDataListener from list
    public boolean removeTalonDataListener(TalonDataListener l) {
        return talonDataListenerList.remove(l);
    }



    //  -> alerts all listeners to change
    private void fireTalonDataPlgFileChange() {

        if (talonDataListenerList != null && !talonDataListenerList.isEmpty()) {

            for (TalonDataListener listener : talonDataListenerList) {
                listener.TalonDataPlgFileChange();
            }
        }
    }


    //  -> alerts all listeners to change
    private void fireTalonDataSegmentingVariableChange() {

        if (talonDataListenerList != null && !talonDataListenerList.isEmpty()) {

            for (TalonDataListener listener : talonDataListenerList) {
                listener.TalonDataSegmentingVariableChange();
            }
        }
    }


    //  -> alerts all listeners to change
    private void fireTalonDataSegmentedVariableChange() {

        if (talonDataListenerList != null && !talonDataListenerList.isEmpty()) {

            for (TalonDataListener listener : talonDataListenerList) {
                listener.TalonDataSegmentedVariableChange();
            }
        }
    }


    //  -> alerts all listeners to change
    private void fireTalonDataReferenceValueChange() {

        if (talonDataListenerList != null && !talonDataListenerList.isEmpty()) {

            for (TalonDataListener listener : talonDataListenerList) {
                listener.TalonDataReferenceValueChange();
            }
        }
    }


    //  -> alerts all listeners to change
    private void fireTalonDataImageDirectoryChange() {

        if (talonDataListenerList != null && !talonDataListenerList.isEmpty()) {

            for (TalonDataListener listener : talonDataListenerList) {
                listener.TalonDataImageDirectoryChange();
            }
        }
    }

}
