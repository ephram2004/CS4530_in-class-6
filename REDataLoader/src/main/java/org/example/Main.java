package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setAllowDuplicateHeaderNames(false)
            .build();

    static final private String PATH_TO_FILE = "C:\\tmp\\redata\\nsw_property_data.csv";

    public static void main(String[] args) {

        // TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the
        // highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        // Path of the temporary file to write work in progress CSV results to
        Path tempFile = null;
        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
            System.out.println("File opened");
            String headers = parser.getHeaderNames().toString();
            System.out.println("headers: " + headers);
            // Iterate over input CSV records
            int count = 0;
            for (final CSVRecord record : parser) {
                // Get all of the header names and associated values from the record
                final Map<String, String> recordValues = record.toMap();

                // Write the updated values to the output CSV
                System.out.println(recordValues.toString());
                count++;
            }
            System.out.println("Total records: " + count);
        } catch (IOException e) {
            System.out.println("File open failed ");
        }

    }
}