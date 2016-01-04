package gov.ornl.csed.cda.datatable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyntheticDataGenerator {
	private final static Logger log = LoggerFactory.getLogger(SyntheticDataGenerator.class);
			
	public static int numRows = 1000;
	public static int numCols = 10;
	public static double maxValue = 10.;
	public static double minValue = -10.;
	
	public static void main(String[] args) throws Exception {
		ArrayList<ArrayList<Double>> rows = new ArrayList<ArrayList<Double>>();
		
		for (int irow = 0; irow < numRows; irow++) {
			ArrayList<Double> row = new ArrayList<Double>();
			
			for (int icol = 0; icol < numCols; icol++) {
				double value = 0.f;
				if (icol == 0) {
					value = (((double)irow/numRows) * (maxValue - minValue)) + minValue;
				} else if (icol == 1) {
					value = maxValue - (((double)irow/numRows) * (maxValue - minValue));
				} else if (icol == 2) {
					value = maxValue - (((double)irow/numRows) * (maxValue - minValue));
				} else if (icol == 3) {
					value = (((double)irow/numRows) * (maxValue - minValue)) + minValue;
				} else if (icol == 4) {
					value = (double) Math.exp(row.get(1));
				} else if (icol == 5) {
					value = (double) Math.pow(row.get(1), 2);
				} else if (icol == 6) {
					value = (double) Math.cos(row.get(1));
				} else if (icol == 7) {
					value = (double) Math.sin(row.get(1));
				} else if (icol == 8) {
					value = (double) Math.cosh(row.get(1));
				} else if (icol == 9) {
					value = (double) Math.pow(row.get(1), 4);
				}
				row.add(value);
			}
			rows.add(row);
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("test.csv"));
		
		StringBuffer header = new StringBuffer();
		for (int icol = 0; icol < numCols; icol++) {
			header.append(icol+1);
			if (icol + 1 < numCols) {
				header.append(", ");
			}
		}
		writer.write(header.toString() + "\n");
		
		for (int irow = 0; irow < numRows; irow++) {
			ArrayList<Double> row = rows.get(irow);
			for (int icol = 0; icol < numCols; icol++) {
				writer.write(String.valueOf(row.get(icol)));
				if (icol + 1 < numCols) {
					writer.write(", ");
				} else {
					writer.write("\n");
				}
			}
		}
		writer.close();
	}
}
