package gov.ornl.util;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetCDFUtils {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public static Variable getCoverageVariable(NetcdfFile ncFile) throws Exception {
        List<Variable> vars = new ArrayList<>();
        for (Variable v : ncFile.getVariables()) {
            boolean hasLatDim = false;
            boolean hasLngDim = false;
            for (Dimension d : v.getDimensions()) {
                if (d.getFullName().toLowerCase().contains(LATITUDE)) {
                    hasLatDim = true;
                } else if (d.getFullName().toLowerCase().contains(LONGITUDE)) {
                    hasLngDim = true;
                }
                if (hasLatDim && hasLngDim) {
                    vars.add(v);
                }
            }
        }
        if (vars.size() == 1) {
            return vars.get(0);
        } else if (vars.isEmpty()) {
            throw new Exception("Could not detect coverage variable in NetCDF file.");
        } else {
            throw new Exception("The NetCDF file contained multiple coverages and thus cannot be converted in this way.");
        }
    }

    public static double[][] extractDimensionValues(NetcdfFile ncFile, Variable variable) throws Exception {
        List<Dimension> dimensions = variable.getDimensions();
        double[][] dimensionValues = new double[dimensions.size()][];

        for (int i = 0; i < dimensions.size(); i++) {
            Dimension dimension = dimensions.get(i);
            Variable dimensionVariable = getDimensionAsVariable(ncFile, dimension);
            dimensionValues[i] = getVariableValuesAs1DArray(dimensionVariable);
        }
        return dimensionValues;

    }

    public static List<Variable> getDimensionsAsVariables(NetcdfFile ncFile, List<Dimension> dimensions) {
        List<Variable> variables = new ArrayList<>();
        for (Dimension dim : dimensions) {
            variables.add(getDimensionAsVariable(ncFile, dim));
        }
        return variables;
    }

    public static Variable getDimensionAsVariable(NetcdfFile ncFile, Dimension dim) {
        Variable var = ncFile.findVariable(dim.getFullName());
//        if (var == null) {
//            throw new Exception("Could not find variable matching dimension {}", dim.getFullName());
//        }
        return var;
    }

    public static double[] getVariableValuesAs1DArray(Variable variable) throws Exception {
        if (variable.getDimensions().size() != 1) {
            throw new Exception("Variable {} had multiple dimensions [{}] thus could not be returned as a 1D Array");
        }
        try {
            Array array = variable.read(variable.getRanges());
            return (double[]) array.get1DJavaArray(double.class);
        } catch (Exception e) {
            throw new Exception("Failed to get 1D data array from variable {}");
        }
    }

    public static double getScalarVariablesValue(Variable variable) throws Exception {
        if(variable.isScalar()) {
            try {
                return variable.readScalarDouble();
            } catch (IOException e) {
                throw new Exception("Problem getting {} scalar value");
            }
        }
        throw new Exception("Variable {} is not a scalar");
    }

    public static void printVariables(NetcdfFile ncFile) {
        for (Variable var : ncFile.getVariables()) {
            System.out.println(var);
        }
    }
}
