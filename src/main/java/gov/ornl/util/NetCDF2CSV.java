package gov.ornl.util;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NetCDF2CSV {
    private static final Logger log = Logger.getLogger(NetCDF2CSV.class.getName());

    private static final String USAGE = "Usage: java NetCDF2CSV <NetCDF File Path> <CSV File Path>";

    private static void convert2CSV(String ncFilePath, String csvFilePath) throws Exception {
        File csvFile = new File(csvFilePath);
        BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile));

        NetcdfFile ncFile = NetcdfFile.open(ncFilePath);

        Variable timeVariable = ncFile.findVariable("time");
        ArrayDouble.D1 timeArray = (ArrayDouble.D1)timeVariable.read();
        Variable latVariable = ncFile.findVariable("lat");
        ArrayDouble.D1 latArray = (ArrayDouble.D1)latVariable.read();
        Variable lonVariable = ncFile.findVariable("lon");
        ArrayDouble.D1 lonArray = (ArrayDouble.D1)lonVariable.read();

        Dimension pftDimension = ncFile.findDimension("pft");
        Dimension ensembleDimension = ncFile.findDimension("ensemble");

        List<Variable> variables = ncFile.getVariables();
        ArrayList<Variable> parameters = new ArrayList<>();
        for (Variable variable : variables) {
            if (variable.getRank() == 1) {
                if (variable.getDimension(0).equals(ensembleDimension)) {
                    parameters.add(variable);
                }
            }
        }

        for (Variable parameter : parameters) {
            System.out.println(parameter.getShortName());
        }

        ArrayDouble.D1 parameterArrays[] = new ArrayDouble.D1[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            parameterArrays[i] = (ArrayDouble.D1) parameters.get(i).read();
        }

//        for (int i = 0; i < ensembleDimension.getLength(); i++) {
//            String line = i + ", ";
//            for (ArrayDouble.D1 parameterArray : parameterArrays) {
//                line += parameterArray.get(i) + ", ";
//            }
//            System.out.println(line);
//        }

//        for (int iEnsemble = 0; iEnsemble < shape[0]; iEnsemble++) {
//            double value = br_mrArray.get(iEnsemble);
//            System.out.print(value);
//            if (iEnsemble + 1 < 200) {
//                System.out.print("\n");
//            }
//        }



        Variable gppVariable = ncFile.findVariable("gpp");
        int gppShape[] = gppVariable.getShape();

        ArrayFloat.D5 gppArray = (ArrayFloat.D5) gppVariable.read();

        csvWriter.write("Ensemble,Day,Lat,Lon,GPP\n");
        int rowCounter = 0;
        for (int i = 0; i < gppShape[0]; i++) {
            int ensemble = i;
            for (int j = 0; j < gppShape[1]; j++) {
                int pft = 0;
                for (int k = 0; k < 4; k++) {
                    double day = timeArray.get(k);
                    for (int l = 0; l < gppShape[3]; l++) {
                        double lat = latArray.get(l);
                        for (int m = 0; m < gppShape[4]; m++) {
                            double lon = lonArray.get(m);
                            float gppValue = gppArray.get(i, j, k, l, m);
                            csvWriter.write(ensemble + "," + day + "," + lat + "," + lon + "," + gppValue + "\n");
                            rowCounter++;
                        }
                    }
                }
            }
        }
        csvWriter.close();

        log.info("Wrote " + rowCounter + " rows.");
    }

    public static void main (String args[]) throws Exception {
        if (args.length != 2) {
            System.out.println(USAGE);
            System.exit(0);
        } else {
            convert2CSV(args[0], args[1]);
        }
    }
}
