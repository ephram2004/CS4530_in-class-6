package sql.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import redis.clients.jedis.Jedis;
import java.util.Set;

import credentials.Credentials;

public class SalesDAO {

    private static final String KEY_PREFIX = "property_sales";
    private static final ObjectMapper mapper = new ObjectMapper();

    public boolean newSale(DynamicHomeSale homeSale) {
        try (Jedis jedis = new Jedis("10.0.100.74", 8001)) {
            homeSale.saveToRedis(KEY_PREFIX, "propertyId");
            String postcode = String.valueOf(homeSale.getInt("postCode"));
            if (postcode != null && !postcode.equals("null")) {
                jedis.sadd("postcode:" + postcode, String.valueOf(homeSale.getInt("propertyId")));
            }
            System.out.println("✅ Inserted new Sale to Redis.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Could not insert new sale: " + e.getMessage());
            return false;
        }
    }

    public Optional<DynamicHomeSale> getSaleById(int propertyId) {
        try (Jedis jedis = new Jedis("10.0.100.74", 8001)) {
            String redisKey = KEY_PREFIX + ":" + propertyId;
            Map<String, String> data = jedis.hgetAll(redisKey);
            if (data == null || data.isEmpty())
                return Optional.empty();
            JsonNode node = mapper.valueToTree(data);
            return Optional.of(new DynamicHomeSale(node));
        } catch (Exception e) {
            System.err.println("❌ Redis read error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public List<DynamicHomeSale> getSalesByPostCode(int postCode) {
        List<DynamicHomeSale> sales = new ArrayList<>();
        try (Jedis jedis = new Jedis("10.0.100.74", 8001)) {
            Set<String> ids = jedis.smembers("postcode:" + postCode);
            for (String id : ids) {
                Map<String, String> data = jedis.hgetAll(KEY_PREFIX + ":" + id);
                if (!data.isEmpty()) {
                    JsonNode node = mapper.valueToTree(data);
                    sales.add(new DynamicHomeSale(node));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Redis postcode lookup failed: " + e.getMessage());
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<Integer> getAllSalePrices() {
        List<Integer> prices = new ArrayList<>();
        for (DynamicHomeSale sale : getAllSales()) {
            Integer price = sale.getInt("purchasePrice");
            if (price != null)
                prices.add(price);
        }
        return prices;
    }

    public List<DynamicHomeSale> getAllSales() {
        List<DynamicHomeSale> sales = new ArrayList<>();
        try (Jedis jedis = new Jedis("10.0.100.74", 8001)) {
            Set<String> keys = jedis.keys(KEY_PREFIX + ":*");
            for (String key : keys) {
                Map<String, String> data = jedis.hgetAll(key);
                if (!data.isEmpty()) {
                    JsonNode node = mapper.valueToTree(data);
                    sales.add(new DynamicHomeSale(node));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Redis keys failed: " + e.getMessage());
        }
        return sales;
    }

    public double getAveragePrice(int postCode) {
        List<DynamicHomeSale> sales = getSalesByPostCode(postCode);
        if (sales.isEmpty())
            return 0;
        double total = 0;
        for (DynamicHomeSale s : sales) {
            Integer p = s.getInt("purchasePrice");
            if (p != null)
                total += p;
        }
        return Math.round(total / sales.size() * 100.0) / 100.0;
    }

    public List<DynamicHomeSale> filterSalesByCriteria(String councilName, String propertyType,
            int minPrice, int maxPrice, String areaType) {
        List<DynamicHomeSale> filtered = new ArrayList<>();
        for (DynamicHomeSale sale : getAllSales()) {
            boolean match = true;
            if (councilName != null && !councilName.equals(sale.getString("councilName")))
                match = false;
            if (propertyType != null && !propertyType.equals(sale.getString("propertyType")))
                match = false;
            Integer price = sale.getInt("purchasePrice");
            if (minPrice >= 0 && (price == null || price < minPrice))
                match = false;
            if (maxPrice >= 0 && (price == null || price > maxPrice))
                match = false;
            if (areaType != null && !areaType.equals(sale.getString("areaType")))
                match = false;
            if (match)
                filtered.add(sale);
        }
        return filtered;
    }

    public int getPriceHistory(int propertyId) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
    
        try (Jedis jedis = new Jedis("10.0.100.74", 8001)) {
            Set<String> keys = jedis.keys(KEY_PREFIX + ":*");
            for (String key : keys) {
                Map<String, String> data = jedis.hgetAll(key);
                if (data.isEmpty()) continue;
    
                String propIdStr = data.get("propertyId");
                if (propIdStr != null && Integer.parseInt(propIdStr) == propertyId) {
                    String priceStr = data.get("purchasePrice");
                    if (priceStr != null) {
                        int price = Integer.parseInt(priceStr);
                        if (price < min) min = price;
                        if (price > max) max = price;
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
        if (places < 0)
            throw new IllegalArgumentException();
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
