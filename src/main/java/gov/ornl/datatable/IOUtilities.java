package gov.ornl.datatable;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
//		ArrayList<DoubleColumn> columns = new ArrayList<DoubleColumn>();
//		reader = new BufferedReader(new FileReader(f));
//
//		// Read the header line
//		line = reader.readLine();
//		int tokenCounter = 0;
//		StringTokenizer st = new StringTokenizer(line);
//		while (st.hasMoreTokens()) {
//			String token = st.nextToken(",");
//			DoubleColumn column = new DoubleColumn(token.trim());
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

	public static String[] readCSVHeader(File f) throws  IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String headerLine = reader.readLine();
		reader.close();

		String columnNames[] = headerLine.trim().split(",");
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = columnNames[i].trim();
		}
		return columnNames;
	}

	public static int getCSVLineCount(File f) throws IOException {
		LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(f));
		lineNumberReader.skip(Long.MAX_VALUE);
		int numLines = lineNumberReader.getLineNumber();
		lineNumberReader.close();
		return numLines;
	}

	public static void readCSV(File f, ArrayList<String> ignoreColumnNames, ArrayList<String> categoricalColumnNames, ArrayList<String> temporalColumnNames,
							   ArrayList<DateTimeFormatter> temporalColumnFormatters, DataModel dataModel) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));

		ArrayList<Tuple> tuples = new ArrayList<>();
		ArrayList<Column> columns = new ArrayList<>();

		int temporalColumnIndices[] = null;
		if (temporalColumnNames != null && !temporalColumnNames.isEmpty()) {
		    temporalColumnIndices = new int[temporalColumnNames.size()];
		    Arrays.fill(temporalColumnIndices, -1);
        }

        int categoricalColumnIndices[] = null;
		if (categoricalColumnNames != null && !categoricalColumnNames.isEmpty()) {
			categoricalColumnIndices = new int[categoricalColumnNames.size()];
			Arrays.fill(categoricalColumnIndices, -1);
		}

        int ignoreColumnIndices[] = null;
		if (ignoreColumnNames != null && !ignoreColumnNames.isEmpty()) {
			ignoreColumnIndices = new int[ignoreColumnNames.size()];
			Arrays.fill(ignoreColumnIndices, -1);
		}

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

					Column column = null;

					if (ignoreColumnNames != null) {
						boolean isIgnoreColumn = false;
						for (int i = 0; i < ignoreColumnNames.size(); i++) {
							if (token.trim().equals(ignoreColumnNames.get(i))) {
								ignoreColumnIndices[i] = tokenCounter;
								tokenCounter++;
								isIgnoreColumn = true;
								break;
							}
						}
						if (isIgnoreColumn) {
							continue;
						}
					}

					if (temporalColumnNames != null) {
					    for (int i = 0; i < temporalColumnNames.size(); i++) {
					        if (token.trim().equals(temporalColumnNames.get(i))) {
					            temporalColumnIndices[i] = tokenCounter;
					            column = new TemporalColumn(token.trim());
                            }
                        }
                    }

                    if (categoricalColumnNames != null) {
						for (int i = 0; i < categoricalColumnNames.size(); i++) {
							if (token.trim().equals(categoricalColumnNames.get(i))) {
								categoricalColumnIndices[i] = tokenCounter;
								column = new CategoricalColumn(token.trim(), null);
							}
						}
					}

                    if (column == null) {
					    column = new DoubleColumn(token.trim());
                    }

                    columns.add(column);

//					if (temporalColumnName != null && token.equals(temporalColumnName)) {
//						temporalColumnIndex = tokenCounter;
//						temporalColumn = new TemporalColumn(token.trim());
//					} else {
//						DoubleColumn column = new DoubleColumn(token.trim());
//						columns.add(column);
//					}

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

				if (ignoreColumnIndices != null) {
					boolean ignoreColumn = false;
					// is this a column to ignore
					for (int i = 0; i < ignoreColumnIndices.length; i++) {
						if (tokenCounter == ignoreColumnIndices[i]) {
							tokenCounter++;
							ignoreColumn = true;
							break;
						}
					}
					if (ignoreColumn) {
						continue;
					}
				}

				if (temporalColumnIndices != null) {
				    Instant instant = null;

				    // is this a temporal column
				    for (int i = 0; i < temporalColumnIndices.length; i++) {
				        if (tokenCounter == temporalColumnIndices[i]) {
                            LocalDateTime localDateTime = LocalDateTime.parse(token, temporalColumnFormatters.get(i));
                            instant = localDateTime.toInstant(ZoneOffset.UTC);
//                            tuple.addElement(instant);
//                            tokenCounter++;
                            break;
//                            tuple.setInstant(instant);
                        }
                    }

                    if (instant != null) {
				        tuple.addElement(instant);
				        tokenCounter++;
				        continue;
                    }
                }

                if (categoricalColumnIndices != null) {
					String category = null;

					// is this a categorical column
					for (int i = 0; i < categoricalColumnIndices.length; i++) {
						if (tokenCounter == categoricalColumnIndices[i]) {
							category = token.trim();
							((CategoricalColumn)columns.get(tokenCounter)).addCategory(category);
							break;
						}
					}

					if (category != null) {
						tuple.addElement(category);
						tokenCounter++;
						continue;
					}
				}

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

		long start = System.currentTimeMillis();
		dataModel.setData(tuples, columns);
		long elapsed = System.currentTimeMillis() - start;

		log.info("Finished setting data in datamodel (it took " + elapsed + " ms");
	}

	public static void main (String args[]) throws IOException {
	    DataModel dataModel = new DataModel();

	    ArrayList<String> categoricalColumnNames = new ArrayList<>();
	    categoricalColumnNames.add("Origin");

	    IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
				null, null, dataModel);

	    log.info("Finished");
//	    ArrayList<String> temporalColumnNames = new ArrayList<>();
//	    temporalColumnNames.add("Date");
//	    ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//	    temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
//
//	    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                temporalColumnNames, temporalColumnFormatters, dataModel);
    }
}
