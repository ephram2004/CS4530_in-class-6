package sql.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helpers.RedisJsonCommand;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class SalesDAO {

    private static final String KEY_PREFIX = "sale_id";
    private static final ObjectMapper mapper = new ObjectMapper();

    public boolean newSale(DynamicHomeSale homeSale) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            jedis.select(0);
            long saleId = jedis.dbSize() + 1;
            homeSale.saveToRedis(KEY_PREFIX, saleId);
            System.out.println("Inserted new Sale to Redis. " + KEY_PREFIX + ":" + saleId);
            return true;
        } catch (Exception e) {
            System.err.println("Could not insert new sale: " + e.getMessage());
            return false;
        }
    }

    public Optional<DynamicHomeSale> getSaleById(long propertyId) {
        try (UnifiedJedis jedis = new UnifiedJedis(new HostAndPort("localhost", 6379))) {
            // Step 1: Search by property_id
            String query = "@property_id:{" + propertyId + "}";
            SearchResult result = jedis.ftSearch("sale_idx", new Query(query).limit(0, 1000));

            if (result.getTotalResults() == 0) {
                return Optional.empty();
            }

            Document doc = result.getDocuments().get(0);
            String redisKey = doc.getId();

            byte[] raw = (byte[]) jedis.sendCommand(
                    RedisJsonCommand.JSON_GET,
                    SafeEncoder.encode(redisKey),
                    SafeEncoder.encode("."));

            String json = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
            JsonNode node = mapper.readTree(json);

            return Optional.of(new DynamicHomeSale(node));
        } catch (Exception e) {
            System.err.println("RedisSearch error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public List<DynamicHomeSale> getSalesByPostCode(int postCode) {
        List<DynamicHomeSale> sales = new ArrayList<>();
        try (UnifiedJedis jedis = new UnifiedJedis(new HostAndPort("localhost", 6379))) {
            // Wrap postCode as a string for TAG search (RedisSearch expects quoted strings
            // for TAG fields)
            String query = "@post_code:\"" + postCode + "\"";
            SearchResult result = jedis.ftSearch("sale_idx", new Query(query).limit(0, 1000));

            for (Document doc : result.getDocuments()) {
                String redisKey = doc.getId();

                byte[] raw = (byte[]) jedis.sendCommand(
                        RedisJsonCommand.JSON_GET,
                        SafeEncoder.encode(redisKey),
                        SafeEncoder.encode("."));

                String json = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                JsonNode node = mapper.readTree(json);
                sales.add(new DynamicHomeSale(node));
            }
        } catch (Exception e) {
            System.err.println("RedisSearch post_code lookup failed: " + e.getMessage());
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<Integer> getAllSalePrices() {
        List<Integer> prices = new ArrayList<>();
        for (DynamicHomeSale sale : getAllSales()) {
            Integer price = sale.getInt("purchasePrice");
            if (price != null) {
                prices.add(price);
            }
        }
        return prices;
    }

    public List<DynamicHomeSale> getAllSales() {
        List<DynamicHomeSale> sales = new ArrayList<>();
        int maxResults = 1000;

        try (UnifiedJedis jedis = new UnifiedJedis(new HostAndPort("localhost", 6379))) {
            ScanParams scanParams = new ScanParams().match("sale_id:*").count(1000);
            String cursor = "0";

            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = scanResult.getCursor();

                for (String redisKey : scanResult.getResult()) {
                    if (sales.size() >= maxResults) {
                        break;
                    }

                    try {
                        byte[] raw = (byte[]) jedis.sendCommand(
                                RedisJsonCommand.JSON_GET,
                                SafeEncoder.encode(redisKey),
                                SafeEncoder.encode("."));

                        if (raw != null) {
                            String json = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                            JsonNode node = mapper.readTree(json);
                            sales.add(new DynamicHomeSale(node));
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Skipped key due to error: " + redisKey);
                    }
                }
            } while (!cursor.equals("0") && sales.size() < maxResults);
        } catch (Exception e) {
            System.err.println("❌ Redis JSON getAllSales failed: " + e.getMessage());
        }

        return sales;
    }

    public double getAveragePrice(int postCode) {
        List<DynamicHomeSale> sales = getSalesByPostCode(postCode);
        if (sales.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (DynamicHomeSale s : sales) {
            Integer p = s.getInt("purchasePrice");
            if (p != null) {
                total += p;
            }
        }
        return Math.round(total / sales.size() * 100.0) / 100.0;
    }

    public List<DynamicHomeSale> filterSalesByCriteria(String councilName, String propertyType,
            int minPrice, int maxPrice, String areaType) {
        List<DynamicHomeSale> filtered = new ArrayList<>();
        for (DynamicHomeSale sale : this.getAllSales()) {
            boolean match = true;
            if (councilName != null && !councilName.equals(sale.getString("councilName"))) {
                match = false;
            }
            if (propertyType != null && !propertyType.equals(sale.getString("propertyType"))) {
                match = false;
            }
            Integer price = sale.getInt("purchasePrice");
            if (minPrice >= 0 && (price == null || price < minPrice)) {
                match = false;
            }
            if (maxPrice >= 0 && (price == null || price > maxPrice)) {
                match = false;
            }
            if (areaType != null && !areaType.equals(sale.getString("areaType"))) {
                match = false;
            }
            if (match) {
                filtered.add(sale);
            }
        }
        return filtered;
    }

    public int getPriceHistory(int propertyId) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        try (Jedis jedis = new Jedis("localhost", 6379)) {
            Set<String> keys = jedis.keys(KEY_PREFIX + ":*");
            for (String key : keys) {
                Map<String, String> data = jedis.hgetAll(key);
                if (data.isEmpty()) {
                    continue;
                }

                String propIdStr = data.get("propertyId");
                if (propIdStr != null && Integer.parseInt(propIdStr) == propertyId) {
                    String priceStr = data.get("purchasePrice");
                    if (priceStr != null) {
                        int price = Integer.parseInt(priceStr);
                        if (price < min) {
                            min = price;
                        }
                        if (price > max) {
                            max = price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getPriceHistory: " + e.getMessage());
            return 0;
        }

        return (min == Integer.MAX_VALUE || max == Integer.MIN_VALUE) ? 0 : (max - min);
    }

    private static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
