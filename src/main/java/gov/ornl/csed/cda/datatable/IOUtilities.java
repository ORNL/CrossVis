package gov.ornl.csed.cda.datatable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class IOUtilities {
	private static final Logger log = Logger.getLogger(IOUtilities.class.getName());

//	public static void readCSVSample(File f, DataModel dataModel,
//			double sampleFactor) throws IOException {
//		BufferedReader reader = new BufferedReader(new FileReader(f));
//		int totalLineCount = 0;
//		String line = null;
//		while ((line = reader.readLine()) != null) {
//			totalLineCount++;
//		}
//		totalLineCount -= 1; // remove header line
//		reader.close();
//
//		log.info("totalLineCount is " + totalLineCount);
//
//		int sampleSize = (int) (sampleFactor * totalLineCount);
//		log.info("sample size is " + sampleSize);
//
//		int sampleIndices[] = new int[sampleSize];
//		boolean sampleSelected[] = new boolean[totalLineCount];
//		Arrays.fill(sampleSelected, false);
//		Random rand = new Random();
//		for (int i = 0; i < sampleIndices.length; i++) {
//			int index = rand.nextInt(totalLineCount);
//			while (sampleSelected[index]) {
//				log.info("got a duplicate");
//				index = rand.nextInt(totalLineCount);
//			}
//			sampleSelected[index] = true;
//			sampleIndices[i] = index;
//		}
//
//		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
//		ArrayList<QuantitativeColumn> columns = new ArrayList<QuantitativeColumn>();
//		reader = new BufferedReader(new FileReader(f));
//
//		// Read the header line
//		line = reader.readLine();
//		int tokenCounter = 0;
//		StringTokenizer st = new StringTokenizer(line);
//		while (st.hasMoreTokens()) {
//			String token = st.nextToken(",");
//			QuantitativeColumn column = new QuantitativeColumn(token.trim());
////			column.setName(token.trim());
//			columns.add(column);
//			tokenCounter++;
//		}
//
//		// Read the data tuples
//		int lineCounter = 0;
//		boolean skipLine = false;
//		while ((line = reader.readLine()) != null) {
//			// is the current line selected to be read
//			if (sampleSelected[lineCounter]) {
//				// read the line as a tuple
//				Tuple tuple = new Tuple();
//				st = new StringTokenizer(line);
//				tokenCounter = 0;
//
//				skipLine = false;
//				while (st.hasMoreTokens()) {
//					String token = st.nextToken(",");
//					try {
//						double value = Double.parseDouble(token);
//
//						// data attribute
//						tuple.addElement(value);
//
//						tokenCounter++;
//					} catch (NumberFormatException ex) {
//						log.info("NumberFormatException caught so skipping record. "
//								+ ex.fillInStackTrace());
//						skipLine = true;
//						break;
//					}
//				}
//
//				if (!skipLine) {
//					// log.info("added tuple at index " + lineCounter);
//					tuples.add(tuple);
//				}
//
//				// line = reader.readLine();
//			}
//
//			lineCounter++;
//		}
//
//		reader.close();
//		dataModel.setData(tuples, columns);
//	}

	public static void readCSV(File f, DateTimeFormatter dateTimeFormatter, String temporalColumnName, DataModel dataModel) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));

		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		ArrayList<QuantitativeColumn> columns = new ArrayList<QuantitativeColumn>();
		TemporalColumn temporalColumn = null;
		int temporalColumnIndex = -1;

		String line = reader.readLine();
		int lineCounter = 0;
		int numLinesIgnored = 0;

		boolean skip_line = false;
		while (line != null) {
			if (lineCounter == 0) {
				// The first line contains the column headers.
				int tokenCounter = 0;
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					String token = st.nextToken(",");

					if (temporalColumnName != null && token.equals(temporalColumnName)) {
						temporalColumnIndex = tokenCounter;
						temporalColumn = new TemporalColumn(token.trim());
					} else {
						QuantitativeColumn column = new QuantitativeColumn(token.trim());
						columns.add(column);
					}

					tokenCounter++;
				}

				lineCounter++;
				line = reader.readLine();
				continue;
			}

			Tuple tuple = new Tuple();
			StringTokenizer st = new StringTokenizer(line);
			int tokenCounter = 0;

			skip_line = false;
			while (st.hasMoreTokens()) {
				String token = st.nextToken(",");

				if (tokenCounter == temporalColumnIndex) {
					LocalDateTime localDateTime = LocalDateTime.parse(token, dateTimeFormatter);
					Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
					tuple.setInstant(instant);
					tokenCounter++;
				} else {
					try {
						double value = Double.parseDouble(token);

//                    if (token_counter == 0) {
//                        log.info("token=" + token + " value=" + ((long)value) + " dvalue=" + (long)dvalue);
//                    }

						if (Double.isNaN(value)) {
							skip_line = true;
							break;
						}
						// data attribute
						tuple.addElement(value);
						tokenCounter++;
					} catch (NumberFormatException ex) {
						System.out.println("DataSet.readCSV(): NumberFormatException caught so skipping record. "
								+ ex.fillInStackTrace());
						skip_line = true;
						numLinesIgnored++;
						break;
					}
				}
			}

			if (tuple.getElementCount() != columns.size()) {
				log.info("Row ignored because it has "
						+ (columns.size() - tuple.getElementCount())
						+ " column values missing.");
                numLinesIgnored++;
                skip_line = true;
			}

			if (!skip_line) {
				tuples.add(tuple);
			}

			lineCounter++;
			line = reader.readLine();
		}

		reader.close();

		log.info("Finished reading CSV file '" + f.getName() + "': Read " + tuples.size() + " rows with " + columns.size() + " columns; " + numLinesIgnored + " rows ignored.");

		dataModel.setData(tuples, columns, temporalColumn);

		log.info("Finished setting data in datamodel");
	}
}
