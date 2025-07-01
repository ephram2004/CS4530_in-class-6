package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    private static final String PATH_TO_FILE = "/Users/ejacquin/Desktop/Northeastern/School_Work/"
            + "Summer_II_2025/CS4530/In-Class/CS4530_in-class-6/nsw_property_data.csv";

    // TODO: Change this!!
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/PropertyData";
    private static final String JDBC_USER = "team-6";
    private static final String JDBC_PASSWORD = "1234";

    public static void main(String[] args) {

        // TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the
        // highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
            System.out.println("File opened");
            String headers = parser.getHeaderNames().toString();
            System.out.println("headers: " + headers);

            try (Connection conn = DriverManager
                    .getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

                conn.setAutoCommit(false); // group many inserts into one commit

                String sql = "INSERT INTO property_sales (property_id, download_date, " +
                        "council_name, purchase_price, address, post_code, property_type, " +
                        "strata_lot_number, property_name, area, area_type, " +
                        "contract_date, settlement_date, zoning, nature_of_property, " +
                        "primary_purpose, legal_description) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    List<CSVRecord> skippedRows = new ArrayList<>();
                    // Iterate over input CSV records
                    int count = 0;
                    for (final CSVRecord record : parser) {
                        Integer propId = parseIntSafe(record.get("property_id"));
                        if (propId == null) {
                            skippedRows.add(record);
                            continue;
                        }

                        stmt.setObject(1, propId);
                        stmt.setDate(2,
                                parseDateSafe(record.get("download_date"))); // yyyy-mm-dd
                        stmt.setString(3,
                                parseStringSafe(record.get("council_name")));
                        stmt.setObject(4,
                                parseIntSafe(record.get("purchase_price")));
                        stmt.setString(5,
                                parseStringSafe(record.get("address")));
                        stmt.setObject(6,
                                parseIntSafe(record.get("post_code")));
                        stmt.setString(7,
                                parseStringSafe(record.get("property_type")));
                        stmt.setObject(8,
                                parseIntSafe(record.get("strata_lot_number")));
                        stmt.setString(9,
                                parseStringSafe(record.get("property_name")));
                        stmt.setObject(10,
                                parseDoubleSafe(record.get("area")));
                        stmt.setString(11,
                                parseStringSafe(record.get("area_type")));
                        stmt.setDate(12,
                                parseDateSafe(record.get("contract_date")));
                        stmt.setDate(13,
                                parseDateSafe(record.get("settlement_date")));
                        stmt.setString(14,
                                parseStringSafe(record.get("zoning")));
                        stmt.setString(15,
                                parseStringSafe(record.get("nature_of_property")));
                        stmt.setString(16,
                                parseStringSafe(record.get("primary_purpose")));
                        stmt.setString(17,
                                parseStringSafe(record.get("legal_description")));

                        stmt.addBatch();

                        System.out.println("Added new row. PropertyID = " + propId);

                        // execute batch every 1000 rows
                        if (++count % 1000 == 0) {
                            stmt.executeBatch();
                        }
                    }

                    stmt.executeBatch(); // execute remaining
                    conn.commit(); // one commit for all rows
                    System.out.println("Total records: " + count);
                    System.out.println("Skipped " + skippedRows.size() + " bad rows.");
                } catch (SQLException e) {
                    System.err.println("Error during insert: " + e.getMessage());
                    try {
                        conn.rollback();
                        System.err.println("Transaction rolled back.");
                    } catch (SQLException anotherone) {
                        System.err.println("Rollback failed: " + anotherone.getMessage());
                    }
                }
            }
        } catch (IOException | SQLException e) {
            System.out.println("File open failed " + e.getMessage());
        }
    }

    private static Integer parseIntSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDoubleSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Date parseDateSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Date.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String parseStringSafe(String value) {
        try {
            return (value == null || value.isBlank()) ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}