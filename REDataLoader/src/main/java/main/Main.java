package main;

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

import helper.Helper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    private static final String PATH_TO_FILE = "/Users/ejacquin/Desktop/Northeastern/School_Work/"
            + "Summer_II_2025/CS4530/In-Class/CS4530_in-class-6/nsw_property_data.csv";
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/" +
            Credentials.get("POSTGRES_DB");
    private static final String JDBC_USER = Credentials.get("POSTGRES_USER");
    private static final String JDBC_PASSWORD = Credentials.get("POSTGRES_PASSWORD");

    public static void main(String[] args) {

        // TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the
        // highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

                try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT);
                                Jedis jedis = new Jedis("localhost", 6379)) {

                        System.out.println("Connected to Redis");
                        System.out.println("File opened");

                        List<CSVRecord> skippedRows = new ArrayList<>();
                        int count = 0;
                        long saleId = 0;

                        int batchSize = 50000;
                        Pipeline pipeline = jedis.pipelined();

                        for (final CSVRecord record : parser) {
                                String redisKey = "sale_id:" + saleId;
                                Map<String, String> propertyData = new HashMap<>();
                                putSafe(propertyData, "sale_id", Helper.parseStringSafe(String.valueOf(saleId)));
                                if (!putSafe(propertyData, "property_id",
                                                Helper.parseStringSafe(record.get("property_id")))) {
                                        skippedRows.add(record);
                                        count++;
                                        continue;
                                }
                                putSafe(propertyData, "download_date",
                                                Helper.parseStringSafe(record.get("download_date")));
                                putSafe(propertyData, "council_name",
                                                Helper.parseStringSafe(record.get("council_name")));
                                putSafe(propertyData, "purchase_price",
                                                String.valueOf(Helper.parseIntSafe(record.get("purchase_price"))));
                                putSafe(propertyData, "address", Helper.parseStringSafe(record.get("address")));
                                putSafe(propertyData, "post_code",
                                                String.valueOf(Helper.parseIntSafe(record.get("post_code"))));
                                putSafe(propertyData, "property_type",
                                                Helper.parseStringSafe(record.get("property_type")));
                                putSafe(propertyData, "strata_lot_number",
                                                String.valueOf(Helper.parseIntSafe(record.get("strata_lot_number"))));
                                putSafe(propertyData, "property_name",
                                                Helper.parseStringSafe(record.get("property_name")));
                                putSafe(propertyData, "area",
                                                String.valueOf(Helper.parseDoubleSafe(record.get("area"))));
                                putSafe(propertyData, "area_type", Helper.parseStringSafe(record.get("area_type")));
                                putSafe(propertyData, "contract_date",
                                                Helper.parseStringSafe(record.get("contract_date")));
                                putSafe(propertyData, "settlement_date",
                                                Helper.parseStringSafe(record.get("settlement_date")));
                                putSafe(propertyData, "zoning", Helper.parseStringSafe(record.get("zoning")));
                                putSafe(propertyData, "nature_of_property",
                                                Helper.parseStringSafe(record.get("nature_of_property")));
                                putSafe(propertyData, "primary_purpose",
                                                Helper.parseStringSafe(record.get("primary_purpose")));
                                putSafe(propertyData, "legal_description",
                                                Helper.parseStringSafe(record.get("legal_description")));

                                pipeline.hset(redisKey, propertyData);

                                if (count % batchSize == 0) {
                                        System.out.println("Flushing batch of " + batchSize + " records...");
                                        pipeline.sync();
                                }

                                System.out.println("Loaded " + redisKey);
                                count++;
                                saleId++;
                        }

                        pipeline.sync();
                        System.out.println("Imported " + saleId + " rows into Redis.");
                        System.out.println("Skipped " + skippedRows.size() + " bad rows.");
                        jedis.set("metrics", "{}");

                } catch (IOException e) {
                        System.out.println("Error reading CSV: " + e.getMessage());
                }
        }

        private static boolean putSafe(Map<String, String> map, String key, Object value) {
                if (key != null && value != null) {
                        map.put(key, value.toString());
                        return true;
                }
                return false;
        }

}