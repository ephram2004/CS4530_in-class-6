package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.JsonNode;

import credentials.Credentials;
import helper.CSVToJson;
import helper.DataLoaderHelper;
import sql.sales.DynamicHomeSale;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final String SQL_TABLE_NAME = "property_sales";
    private static final String PATH_TO_FILE = "./nsw_property_data.csv";
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/"
            + Credentials.get("POSTGRES_DB");
    private static final String JDBC_USER = Credentials.get("POSTGRES_USER");
    private static final String JDBC_PASSWORD = Credentials.get("POSTGRES_PASSWORD");
    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    public static void main(String[] args) {
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
            System.out.println("File opened");
            List<String> headers = parser.getHeaderNames();
            System.out.println("headers: " + headers);

            String sql = DynamicHomeSale
                    .insertBySQLBuilder(DynamicHomeSale.class, SQL_TABLE_NAME);

            System.out.println("SQL INSERT cmd: " + sql);

            try (Connection conn = DriverManager
                    .getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

                conn.setAutoCommit(false); // group many inserts into one commit

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int count = 0;
                    long saleId = 0;
                    List<CSVRecord> skippedRows = new ArrayList<>();

                    for (final CSVRecord record : parser) {
                        String key = "sale_id:" + saleId;

                        Integer propId = DataLoaderHelper
                                .parseIntSafe(record.get("property_id"));
                        if (propId == null) {
                            skippedRows.add(record);
                            continue;
                        }

                        JsonNode json = CSVToJson.csvRecordToJson(record);
                        DynamicHomeSale hs = new DynamicHomeSale(json);

                        try {
                            hs.postgressBatchInsert(stmt);
                        } catch (IllegalAccessException e) {
                            System.out.println(
                                    "Error accessing non-existing field! Skipping..." + e);
                        } catch (SQLException e) {
                            System.err.println("Error accessing Database! Closing..." + e);
                            return;
                        }

                        stmt.addBatch();

                        // execute batch every 1000 rows
                        if (++count % 1000 == 0) {
                            stmt.executeBatch();
                        }

                        if (count % 100 == 0) {
                            System.out.println("Loaded up to: " + key);
                        }

                        saleId++;
                    }

                    stmt.executeBatch(); // execute remaining
                    conn.commit(); // one commit for all rows

                    System.out.println("✅ Imported " + count + " rows into Redis.");
                    System.out.println("⚠️ Skipped " + skippedRows.size() + " malformed rows.");

                } catch (SQLException e) {
                    System.err.println("Error accessing Database! Closing..." + e);
                    try {
                        conn.rollback();
                        System.err.println("Transaction rolled back. Bye...");
                    } catch (SQLException anotherone) {
                        System.err.println("Rollback failed: " + anotherone.getMessage());
                    }
                }
            }
        } catch (IOException | SQLException e) {
            System.out.println("File open failed " + e.getMessage());
        }
    }
}
