package gov.ornl.crossvis;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
//import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class.getName());
//
//    public static void openNetCDFFile(File f, DataTable dataTable) throws IOException {
//
//        dataTable.clear();
//
//        if (NetcdfFile.canOpen(f.getAbsolutePath())) {
//            NetcdfFile ncFile = NetcdfFile.open(f.getAbsolutePath());
//
//            NetCDFFilter netCDFFilter = NetCDFFilterDialog.getNetCDFFilter(ncFile);
//
////            List<Variable> ncVariableList = ncFile.getVariables();
////
////            for (Variable var : ncVariableList) {
////                log.info("Variable: " + var.getShortName() + " : " + var.getDataType().toString());
////            }
////
////            List<Dimension> ncDimensionList = ncFile.getDimensions();
////            for (Dimension dim : ncDimensionList) {
////                log.info("Dimension: " + dim.getShortName());
////            }
////
////            log.info("found " + ncDimensionList.size() + " dimensions and " + ncVariableList.size() + " variables.");
////
////            Array data = ncVariableList.get(0).read();
////
////            log.info("Finished reading variable " + ncVariableList.get(0).getShortName() + " with " + data.getShape()[0] + " size.");
//        }
//    }

    public static void openCSVFile(File f, DataTable dataTable) throws IOException {
        ArrayList<DataTableColumnSpecification> columnSpecifications = DataTableColumnSpecificationDialog.getColumnSpecifications(f);
        if (columnSpecifications == null) {
            return;
        }

        dataTable.clear();

        ArrayList<String> temporalColumnNames = new ArrayList<>();
        ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
        ArrayList<String> ignoreColumnNames = new ArrayList<>();
        ArrayList<String> categoricalColumnNames = new ArrayList<>();
        String imageFileColumnName = null;
        String imageFileDirectoryPath = null;

        for (DataTableColumnSpecification columnSpecification : columnSpecifications) {
            if (columnSpecification.getIgnore()) {
                ignoreColumnNames.add(columnSpecification.getName());
            } else if (columnSpecification.getType().equalsIgnoreCase("Temporal")) {
                temporalColumnNames.add(columnSpecification.getName());
                temporalColumnFormatters.add(columnSpecification.getDateTimeFormatter());
            } else if (columnSpecification.getType().equalsIgnoreCase("Categorical")) {
                categoricalColumnNames.add(columnSpecification.getName());
            } else if (columnSpecification.getType().equalsIgnoreCase("Image Filename")) {
                imageFileColumnName = columnSpecification.getName();
                imageFileDirectoryPath = columnSpecification.getImageFileDirectoryPath();
            }
        }

        IOUtilities.readCSV(f, ignoreColumnNames, categoricalColumnNames, temporalColumnNames, imageFileColumnName,
                imageFileDirectoryPath, temporalColumnFormatters, dataTable);
    }
}
