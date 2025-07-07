package sql.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
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

import sql.ADAO;

public class SalesDAO extends ADAO {

    public boolean newSale(DynamicHomeSale homeSale) throws SQLException {
        try (Connection conn = getConnection()) {
            System.out.println("Inserting new Sale: " + homeSale);
            homeSale.postgressInsert(conn, "property_sales");
            System.out.println("Inserted new Sale.");
        } catch (Exception e) {
            System.err.println("Could not insert new sale! " + e);
            // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
        }
        return true;
    }

    private static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private DynamicHomeSale mapResultSetToDynamicHomeSale(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = meta.getColumnLabel(i); // or getColumnName(i)
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            rows.add(row);
        }

        ObjectMapper mapper = new ObjectMapper();
        return new DynamicHomeSale(mapper.valueToTree(rows));
    }

    public Optional<DynamicHomeSale> getSaleById(int saleID) throws SQLException {
        String sql = "SELECT * FROM property_sales WHERE property_id = ?";
        DynamicHomeSale sale = null;
        try (
                Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sale = mapResultSetToDynamicHomeSale(rs);
                }
            }
        }
        return Optional.ofNullable(sale);
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public List<DynamicHomeSale> getSalesByPostCode(int postCode) throws SQLException {
        List<DynamicHomeSale> sales = new ArrayList<>();
        String sql = "SELECT * FROM property_sales WHERE post_code = ?";
        try (
                Connection conn = getConnection(); PreparedStatement stmt = conn.
                        prepareStatement(sql)) {
            stmt.setInt(1, postCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DynamicHomeSale sale = mapResultSetToDynamicHomeSale(rs);
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<Integer> getAllSalePrices() throws SQLException {
        List<Integer> prices = new ArrayList<>();
        String sql = "SELECT purchase_price FROM property_sales";
        try (
                Connection conn = getConnection(); PreparedStatement stmt = conn.
                        prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                prices.add(rs.getInt("purchase_price"));
            }
        }
        return prices;
    }

    public List<DynamicHomeSale> getAllSales() throws SQLException {
        List<DynamicHomeSale> sales = new ArrayList<>();
        String sql = "SELECT * FROM property_sales";
        try (
                Connection conn = getConnection(); PreparedStatement stmt = conn.
                        prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sales.add(mapResultSetToDynamicHomeSale(rs));
            }
        }
        return sales;
    }

    public int getPriceHistory(int propertyId) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn
                .prepareStatement("""
                                  SELECT
                                  MAX(purchase_price) - MIN(purchase_price) AS price_change
                                  FROM property_sales
                                  WHERE property_id = ?""")) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && !rs.wasNull()) {
                int priceChange = rs.getInt("price_change");
                return priceChange;
            } else {
                return 0;
            }
        }
    }

    public double getAveragePrice(int postCode) throws SQLException {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn
                .prepareStatement(
                        "SELECT AVG(purchase_price) AS average FROM property_sales WHERE post_code = ?")) {
            stmt.setInt(1, postCode);
            ResultSet rs = stmt.executeQuery();
            System.out.println(rs);
            if (rs.next() && !rs.wasNull()) {
                double avg = rs.getDouble("average");
                return round(avg, 2);
            } else {
                return 0;
            }
        }
    }

    public List<DynamicHomeSale> filterSalesByCriteria(String councilName, String propertyType,
            int minPrice, int maxPrice, String areaType) throws SQLException {
        List<DynamicHomeSale> sales = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM property_sales WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (councilName != null && !councilName.isEmpty()) {
            query.append(" AND council_name = ?");
            params.add(councilName);
        }
        if (propertyType != null && !propertyType.isEmpty()) {
            query.append(" AND property_type = ?");
            params.add(propertyType);
        }
        if (minPrice >= 0) {
            query.append(" AND purchase_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice >= 0) {
            query.append(" AND purchase_price <= ?");
            params.add(maxPrice);
        }
        if (areaType != null && !areaType.isEmpty()) {
            query.append(" AND area_type = ?");
            params.add(areaType);
        }

        try (Connection conn = getConnection(); PreparedStatement stmt = conn
                .prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sales.add(mapResultSetToDynamicHomeSale(rs));
            }
        }
        return sales;
    }
}
