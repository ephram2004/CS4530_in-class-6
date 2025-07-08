package main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;

import helper.Helper;
import helper.RedisJsonCommand;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.util.SafeEncoder;

// TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
            .setHeader()
            .setSkipHeaderRecord(true)
            .build();

    private static final String PATH_TO_FILE = "/Users/ejacquin/Desktop/Northeastern/School_Work/"
            + "Summer_II_2025/CS4530/In-Class/CS4530_in-class-6/nsw_property_data.csv";

    private static final int BATCH_SIZE = 50000;
    private static final ObjectMapper objMap = new ObjectMapper();

    public static void main(String[] args) {

        // TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the
        // highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        HostAndPort hostAndPort = new HostAndPort("localhost", 6379);

        try (UnifiedJedis jedis = new UnifiedJedis(hostAndPort)) {
            System.out.println("Connected to Redis");

            IndexDefinition indexDef = new IndexDefinition(IndexDefinition.Type.JSON)
                    .setPrefixes("sale_id:");

            Schema schema = new Schema()
                    .addTextField("$.sale_id", 1.0).as("sale_id")
                    .addTagField("$.property_id").as("property_id")
                    .addTagField("$.download_date").as("download_date")
                    .addTextField("$.council_name", 1.0).as("council_name")
                    .addTextField("$.address", 1.0).as("address")
                    .addTextField("$.post_code", 1.0).as("post_code")
                    .addTagField("$.property_type").as("property_type")
                    .addTagField("$.strata_lot_number").as("strata_lot_number")
                    .addTextField("$.property_name", 1.0).as("property_name")
                    .addTagField("$.area").as("area")
                    .addTagField("$.area_type").as("area_type")
                    .addTagField("$.contract_date").as("contract_date")
                    .addTagField("$.settlement_date").as("settlement_date")
                    .addTagField("$.zoning").as("zoning")
                    .addTagField("$.nature_of_property").as("nature_of_property")
                    .addTagField("$primary_purpose").as("primary_purpose")
                    .addTextField("$.legal_description", 1.0).as("legal_description");

            jedis.ftCreate("sale_idx", IndexOptions.defaultOptions().setDefinition(indexDef), schema);
            System.out.println("Index created.");
        }

        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT); Jedis jedis = new Jedis(hostAndPort)) {
            System.out.println("Connected to Redis");
            System.out.println("File opened");

            Pipeline pipeline = jedis.pipelined();

            int count = 0;
            long saleId = 0;

            List<CSVRecord> skippedRows = new ArrayList<>();

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

                String jsonString = objMap.writeValueAsString(propertyData);

                pipeline.sendCommand(
                        RedisJsonCommand.JSON_SET,
                        SafeEncoder.encode(redisKey),
                        SafeEncoder.encode("."),
                        SafeEncoder.encode(jsonString)
                );

                if (count % BATCH_SIZE == 0) {
                    System.out.println("Flushing batch of " + BATCH_SIZE + " records...");
                    pipeline.sync();
                }

                System.out.println("Loaded " + redisKey);
                count++;
                saleId++;
            }

            if (count % BATCH_SIZE != 0) {
                pipeline.sync();
            }

            System.out.println("Imported " + saleId + " rows into Redis.");
            System.out.println("Skipped " + skippedRows.size() + " bad rows.");

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
