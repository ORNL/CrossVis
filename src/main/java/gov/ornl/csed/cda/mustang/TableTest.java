package gov.ornl.csed.cda.mustang;

import gov.ornl.csed.cda.datatable.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by csg on 8/15/16.
 */
public class TableTest {
    public static void main (String args[]) throws Exception {
        long startTime = System.currentTimeMillis();

        // Run test with Table class
        Table table = null;
        try {
            table = new CSVTableReader().readTable("data/csv/clm-dataset-2.csv");
        } catch (DataIOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // read all values in the table and calculate statistics
        double [][] data = new double[table.getColumnCount()][];

        for (int icol = 0; icol < table.getColumnCount(); icol++) {
            data[icol] = new double[table.getRowCount()];

            for (int irow = 0; irow < table.getRowCount(); irow++) {
                data[icol][irow] = table.getDouble(irow, icol);
            }

            DescriptiveStatistics stats = new DescriptiveStatistics(data[icol]);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Table load and process time: " + (endTime-startTime) + " ms");

        startTime = System.currentTimeMillis();
        DataModel dataModel = new DataModel();
        File csvFile = new File("data/csv/clm-dataset-2.csv");
        IOUtilities.readCSV(csvFile, dataModel);
        endTime = System.currentTimeMillis();
        System.out.println("DataModel load and process time: " + (endTime-startTime) + " ms");
    }
}
