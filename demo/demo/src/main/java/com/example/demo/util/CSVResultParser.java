package com.example.demo.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class CSVResultParser {

    /**
     * Parses a CSV file of comparison results and returns a JSON-formatted string.
     * @param csvFilePath the path to the CSV file.
     * @return a JSON string representing the results.
     */
    public static String parseResultsFromCSV(String csvFilePath) {
        StringBuilder jsonBuilder = new StringBuilder("{\"results\":[");
        Set<String> seenPairs = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            String[] files = br.readLine().split(",");

            int numberOfFiles = files.length - 1;

            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                for (int colIndex = 1; colIndex <= numberOfFiles; colIndex++) {
                    // Check if it's a self-comparison or a previously seen pair
                    String pairKey1 = files[rowIndex + 1] + " vs " + files[colIndex];
                    String pairKey2 = files[colIndex] + " vs " + files[rowIndex + 1];

                    if (!seenPairs.contains(pairKey1) && !seenPairs.contains(pairKey2) && rowIndex != colIndex - 1) {
                        try {
                            double similarity = Double.parseDouble(values[colIndex]);
                            if (similarity != 1.0) {
                                jsonBuilder.append("{\"file1\":\"")
                                        .append(files[rowIndex + 1].replace("\\", "\\\\"))
                                        .append("\",")
                                        .append("\"file2\":\"")
                                        .append(files[colIndex].replace("\\", "\\\\"))
                                        .append("\",")
                                        .append("\"similarity\":")
                                        .append(new DecimalFormat("#.##").format(similarity * 100))
                                        .append("},");
                                seenPairs.add(pairKey1);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid similarity value between " + files[rowIndex + 1] + " and " + files[colIndex] + ": " + values[colIndex]);
                        }
                    }
                }
                rowIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"results\":[]}";
        }

        if (jsonBuilder.length() > 11) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]}");

        return jsonBuilder.toString();
    }
}