package gov.ornl.util;

import java.io.*;

public class FixYahooDataDates {
    public static void main (String args[]) throws IOException {
        File directory = new File("/Users/csg/Desktop/financial-data");
        File files[] = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".csv")) {
                    return true;
                }
                return false;
            }
        });

        File newFile = new File("/Users/csg/Desktop/financial-data/fixed/all.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));

        boolean writeHeader = true;

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String symbol = file.getName().substring(0, file.getName().indexOf(".csv"));
            BufferedReader reader = new BufferedReader(new FileReader(file));

//            File fixedFileDirectory = new File(file.getParent(), "fixed");
//            if (!fixedFileDirectory.exists()) {
//                fixedFileDirectory.mkdir();
//            }
//            File newFile = new File(fixedFileDirectory, file.getName());
//            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));

            String line = reader.readLine();
            if (writeHeader) {
                writer.write("Symbol," + line + "\n");
                writeHeader = false;
            }

            while((line = reader.readLine()) != null) {
                String tokens[] = line.split(",");
                String newDateToken = tokens[0] + "T00:00:00";
                writer.write(symbol + "," + newDateToken + line.substring(line.indexOf(",")) + "\n");
            }

            reader.close();

        }
        writer.close();
    }
}
