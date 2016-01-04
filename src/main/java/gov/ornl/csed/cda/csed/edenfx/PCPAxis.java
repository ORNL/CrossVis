package gov.ornl.csed.cda.csed.edenfx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by csg on 10/26/15.
 */
public class PCPAxis {

    private StringProperty axisName;
    public void setAxisName(String value) {axisNameProperty().set(value);}
    public String getAxisName() {return axisNameProperty().get();}
    public StringProperty axisNameProperty() {
        if (axisName == null) {
            axisName = new SimpleStringProperty(this, "axisName");
        }
        return axisName;
    }

    private DoubleProperty minValue;
    public DoubleProperty minValueProperty() {
        if (minValue == null) {
            minValue = new SimpleDoubleProperty(this, "minValue");
        }
        return minValue;
    }
    public void setMinValue(double value) {minValueProperty().set(value);}
    public double getMinValue() {return minValueProperty().get();}

    private DoubleProperty maxValue;
    public DoubleProperty maxValueProperty() {
        if (maxValue == null) {
            maxValue = new SimpleDoubleProperty(this, "maxValue");
        }
        return maxValue;
    }
    public void setMaxValue(double value) {maxValueProperty().set(value);}
    public double getMaxValue() {return maxValueProperty().get();}

    private DoubleProperty meanValue;
    public DoubleProperty meanValueProperty() {
        if (meanValue == null) {
            meanValue = new SimpleDoubleProperty(this, "meanValue");
        }
        return meanValue;
    }
    public void setMeanValue(double value) {meanValueProperty().set(value);}
    public double getMeanValue() {return meanValueProperty().get();}

    private DoubleProperty medianValue;
    public DoubleProperty medianValueProperty() {
        if (medianValue == null) {
            medianValue = new SimpleDoubleProperty(this, "medianValue");
        }
        return medianValue;
    }
    public void setMedianValue(double value) {medianValueProperty().set(value);}
    public double getMedianValue() {return medianValueProperty().get();}

    private DoubleProperty iqrValue;
    public DoubleProperty iqrValueProperty() {
        if (iqrValue == null) {
            iqrValue = new SimpleDoubleProperty(this, "iqrValue");
        }
        return iqrValue;
    }
    public void setIQRValue(double value) {iqrValueProperty().set(value);}
    public double getIQRValue() {return iqrValueProperty().get();}

    private DoubleProperty skewnessValue;
    public DoubleProperty skewnessValueProperty() {
        if (skewnessValue == null) {
            skewnessValue = new SimpleDoubleProperty(this, "skewnessValue");
        }
        return skewnessValue;
    }
    public void setSkewnessValue(double value) {skewnessValueProperty().set(value);}
    public double getSkewnessValue() {return skewnessValueProperty().get();}

    private DoubleProperty kurtosisValue;
    public DoubleProperty kurtosisValueProperty() {
        if (kurtosisValue == null) {
            kurtosisValue = new SimpleDoubleProperty(this, "kurtosisValue");
        }
        return kurtosisValue;
    }
    public void setKurtosisValue(double value) {kurtosisValueProperty().set(value);}
    public double getKurtosisValue() {return kurtosisValueProperty().get();}

    private DoubleProperty stdevValue;
    public DoubleProperty stdevValueProperty() {
        if (stdevValue == null) {
            stdevValue = new SimpleDoubleProperty(this, "stdevValue");
        }
        return stdevValue;
    }
    public void setStdevValue(double value) {stdevValueProperty().set(value);}
    public double getStdevValue() {return stdevValueProperty().get();}
}
