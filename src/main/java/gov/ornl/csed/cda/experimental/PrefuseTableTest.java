package gov.ornl.csed.cda.experimental;

import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

/**
 * Created by csg on 1/7/16.
 */
public class PrefuseTableTest {
    public static void main (String args[]) throws DataIOException {
        Table table = new CSVTableReader().readTable("data/csv/test_table.csv");
        for (int i = 0; i < table.getColumnCount(); i++) {
            System.out.println("Column: " + table.getColumnName(i));
            System.out.println(" canGetDouble() " + table.canGetDouble(table.getColumnName(i)));
            System.out.println(" canGetInteger() " + table.canGetInt(table.getColumnName(i)));
            System.out.println(" canGetString() " + table.canGetString(table.getColumnName(i)));
            System.out.println(" canGetBoolean() " + table.canGetBoolean(table.getColumnName(i)));
            System.out.println(" canGetDate() " + table.canGetDate(table.getColumnName(i)));
        }

    }
}
