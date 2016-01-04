package gov.ornl.csed.cda.datatable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by csg on 4/27/15.
 */
public class NetFlowIPMapTest {
    public static void main (String args[]) throws Exception {
        File f = new File("/Users/csg/Desktop/equinix-chicago-netflow/dirA/20130529-130000.UTC/equinix-chicago.dirA.20130529-130800.UTC.anon.pcap.csv");
        BufferedReader reader = new BufferedReader(new FileReader(f));

        TreeMap<String, ArrayList<ArrayList<String>>> ipMap = new TreeMap<String, ArrayList<ArrayList<String>>>();

        ArrayList<String> headerLine = new ArrayList<String>();

        String line = reader.readLine();
        int line_counter = 0;

        boolean skip_line = false;
        while (line != null) {
            if (line_counter == 0) {
                // The first line contains the column headers.
                int token_counter = 0;
                StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken(",");
                    headerLine.add(token.trim());
//                    Column column = new Column(token.trim());
//                    columns.add(column);
                    token_counter++;
                }

                line_counter++;
                line = reader.readLine();
                continue;
            }

            StringTokenizer st = new StringTokenizer(line);
            int token_counter = 0;
            String key = "";
            ArrayList<String> values = new ArrayList<String>();

            skip_line = false;
            while (st.hasMoreTokens()) {
                String token = st.nextToken(",");

                if (token_counter == 0) {
                    // this is the key value for the map
                    key = token.trim();
                } else {
                    values.add(token.trim());
                }

                token_counter++;
            }

            if (key != null) {
                ArrayList<ArrayList<String>> valueList;

                if (ipMap.containsKey(key)) {
                    valueList = ipMap.get(key);
                } else {
                    valueList = new ArrayList<ArrayList<String>>();
                    ipMap.put(key, valueList);
                }

                valueList.add(values);
            }

            line_counter++;
            line = reader.readLine();
        }

        reader.close();

        int singleValueEntries = 0;
        for (String ip : ipMap.keySet()) {
            if (ipMap.get(ip).size() > 1) {
                System.out.println(ip + ": " + ipMap.get(ip).size());
            } else {
                singleValueEntries++;
            }
        }

        System.out.println("There are " + singleValueEntries + " single value entries and " + (ipMap.size() - singleValueEntries) + " multiple value entries");
    }
}
