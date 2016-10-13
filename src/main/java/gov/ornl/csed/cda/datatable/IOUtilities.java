package gov.ornl.csed.cda.datatable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtilities {
	private static final Logger log = LoggerFactory.getLogger(IOUtilities.class);

	public static void readCSVSample(File f, DataModel dataModel,
			double sampleFactor) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		int totalLineCount = 0;
		String line = null;
		while ((line = reader.readLine()) != null) {
			totalLineCount++;
		}
		totalLineCount -= 1; // remove header line
		reader.close();

		log.debug("totalLineCount is " + totalLineCount);

		int sampleSize = (int) (sampleFactor * totalLineCount);
		log.debug("sample size is " + sampleSize);

		int sampleIndices[] = new int[sampleSize];
		boolean sampleSelected[] = new boolean[totalLineCount];
		Arrays.fill(sampleSelected, false);
		Random rand = new Random();
		for (int i = 0; i < sampleIndices.length; i++) {
			int index = rand.nextInt(totalLineCount);
			while (sampleSelected[index]) {
				log.debug("got a duplicate");
				index = rand.nextInt(totalLineCount);
			}
			sampleSelected[index] = true;
			sampleIndices[i] = index;
		}

		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		ArrayList<Column> columns = new ArrayList<Column>();
		reader = new BufferedReader(new FileReader(f));

		// Read the header line
		line = reader.readLine();
		int tokenCounter = 0;
		StringTokenizer st = new StringTokenizer(line);
		while (st.hasMoreTokens()) {
			String token = st.nextToken(",");
			Column column = new Column(token.trim());
//			column.setName(token.trim());
			columns.add(column);
			tokenCounter++;
		}

		// Read the data tuples
		int lineCounter = 0;
		boolean skipLine = false;
		while ((line = reader.readLine()) != null) {
			// is the current line selected to be read
			if (sampleSelected[lineCounter]) {
				// read the line as a tuple
				Tuple tuple = new Tuple();
				st = new StringTokenizer(line);
				tokenCounter = 0;

				skipLine = false;
				while (st.hasMoreTokens()) {
					String token = st.nextToken(",");
					try {
						double value = Double.parseDouble(token);

						// data attribute
						tuple.addElement(value);

						tokenCounter++;
					} catch (NumberFormatException ex) {
						log.debug("NumberFormatException caught so skipping record. "
								+ ex.fillInStackTrace());
						skipLine = true;
						break;
					}
				}

				if (!skipLine) {
					// log.debug("added tuple at index " + lineCounter);
					tuples.add(tuple);
				}

				// line = reader.readLine();
			}

			lineCounter++;
		}

		reader.close();
		dataModel.setData(tuples, columns);
	}

	public static void readCSV(File f, DataModel dataModel) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));

		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		ArrayList<Column> columns = new ArrayList<Column>();

		String line = reader.readLine();
		int line_counter = 0;
		int numLinesIgnored = 0;

		boolean skip_line = false;
		while (line != null) {
			if (line_counter == 0) {
				// The first line contains the column headers.

				int token_counter = 0;
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					String token = st.nextToken(",");
					Column column = new Column(token.trim());
					columns.add(column);
					token_counter++;
				}

				line_counter++;
				line = reader.readLine();
				continue;
			}

			Tuple tuple = new Tuple();
			StringTokenizer st = new StringTokenizer(line);
			int token_counter = 0;

			skip_line = false;
			while (st.hasMoreTokens()) {
				String token = st.nextToken(",");

				try {
					double value = Double.parseDouble(token);

//                    if (token_counter == 0) {
//                        log.debug("token=" + token + " value=" + ((long)value) + " dvalue=" + (long)dvalue);
//                    }

                    if (Double.isNaN(value)) {
                        skip_line = true;
                        break;
                    }
                    // data attribute
					tuple.addElement(value);
					token_counter++;
				} catch (NumberFormatException ex) {
					System.out.println("DataSet.readCSV(): NumberFormatException caught so skipping record. "
									+ ex.fillInStackTrace());
					skip_line = true;
					numLinesIgnored++;
					break;
				}
			}

			if (tuple.getElementCount() != columns.size()) {
				log.debug("Row ignored because it has "
						+ (columns.size() - tuple.getElementCount())
						+ " column values missing.");
                numLinesIgnored++;
                skip_line = true;
			}

			if (!skip_line) {
				tuples.add(tuple);
			}

			line_counter++;
			line = reader.readLine();
		}

		reader.close();

		dataModel.setData(tuples, columns);

		log.debug("Finished reading CSV file '" + f.getName() + "': Read " + tuples.size() + " rows with " + columns.size() + " columns; " + numLinesIgnored + " rows ignored.");
	}
}
