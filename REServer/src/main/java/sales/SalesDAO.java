package sales;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import credentials.Credentials;

public class SalesDAO {

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/" +
            Credentials.get("POSTGRES_DB");
    private static final String JDBC_USER = Credentials.get("POSTGRES_USER");
    private static final String JDBC_PASSWORD = Credentials.get("POSTGRES_PASSWORD");

    public boolean newSale(HomeSale homeSale) throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO property_sales (property_id, download_date, council_name, " +
                                "purchase_price, address, post_code, property_type, " +
                                "strata_lot_number, property_name, area, area_type, " +
                                "contract_date, settlement_date, zoning, nature_of_property, " +
                                "primary_purpose, legal_description) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, homeSale.getPropertyId());
            stmt.setDate(2, homeSale.getDownloadDate());
            stmt.setString(3, homeSale.getCouncilName());
            stmt.setInt(4, homeSale.getPurchasePrice());
            stmt.setString(5, homeSale.getAddress());
            stmt.setInt(6, homeSale.getPostCode());
            stmt.setString(7, homeSale.getPropertyType());
            stmt.setInt(8, homeSale.getStrataLotNumber());
            stmt.setString(9, homeSale.getPropertyName());
            stmt.setDouble(10, homeSale.getArea());
            stmt.setString(11, homeSale.getAreaType());
            stmt.setDate(12, homeSale.getContractDate());
            stmt.setDate(13, homeSale.getSettlementDate());
            stmt.setString(14, homeSale.getZoning());
            stmt.setString(15, homeSale.getNatureOfProperty());
            stmt.setString(16, homeSale.getPrimaryPurpose());
            stmt.setString(17, homeSale.getLegalDescription());
            stmt.executeUpdate();
        }
        return true;
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public Optional<HomeSale> getSaleById(int saleID) throws SQLException {

        HomeSale sale = null;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn
                        .prepareStatement(
                                "SELECT * FROM property_sales WHERE property_id = ?")) {
            stmt.setInt(1, saleID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sale = new HomeSale(
                        rs.getInt("property_id"),
                        rs.getDate("download_date"),
                        rs.getString("council_name"),
                        rs.getInt("purchase_price"),
                        rs.getString("address"),
                        rs.getInt("post_code"),
                        rs.getString("property_type"),
                        rs.getInt("strata_lot_number"),
                        rs.getString("property_name"),
                        rs.getDouble("area"),
                        rs.getString("area_type"),
                        rs.getDate("contract_date"),
                        rs.getDate("settlement_date"),
                        rs.getString("zoning"),
                        rs.getString("nature_of_property"),
                        rs.getString("primary_purpose"),
                        rs.getString("legal_description"));
            }
            return Optional.of(sale);
        }
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public List<HomeSale> getSalesByPostCode(int postCode) throws SQLException {
        List<HomeSale> sales = new ArrayList<HomeSale>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn
                        .prepareStatement(
                                "SELECT * FROM property_sales WHERE post_code = ?")) {
            stmt.setInt(1, postCode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HomeSale sale = new HomeSale(
                        rs.getInt("property_id"),
                        rs.getDate("download_date"),
                        rs.getString("council_name"),
                        rs.getInt("purchase_price"),
                        rs.getString("address"),
                        rs.getInt("post_code"),
                        rs.getString("property_type"),
                        rs.getInt("strata_lot_number"),
                        rs.getString("property_name"),
                        rs.getDouble("area"),
                        rs.getString("area_type"),
                        rs.getDate("contract_date"),
                        rs.getDate("settlement_date"),
                        rs.getString("zoning"),
                        rs.getString("nature_of_property"),
                        rs.getString("primary_purpose"),
                        rs.getString("legal_description"));
                sales.add(sale);
            }
        }
        return sales;
    }

    // returns the individual prices for all sales. Potentially large
    public List<Integer> getAllSalePrices() throws SQLException {
        List<Integer> prices = new ArrayList<Integer>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn
                        .prepareStatement(
                                "SELECT purchase_price FROM property_sales")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prices.add(rs.getInt("purchase_price"));
            }
        }
        return prices;
    }

    public List<HomeSale> getAllSales() throws SQLException {
        List<HomeSale> sales = new ArrayList<HomeSale>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn
                        .prepareStatement(
                                "SELECT * FROM property_sales")) {
            ResultSet rs = stmt.executeQuery();
            int counter = 0;
            while (rs.next()) {
                HomeSale sale = new HomeSale(
                        rs.getInt("property_id"),
                        rs.getDate("download_date"),
                        rs.getString("council_name"),
                        rs.getInt("purchase_price"),
                        rs.getString("address"),
                        rs.getInt("post_code"),
                        rs.getString("property_type"),
                        rs.getInt("strata_lot_number"),
                        rs.getString("property_name"),
                        rs.getDouble("area"),
                        rs.getString("area_type"),
                        rs.getDate("contract_date"),
                        rs.getDate("settlement_date"),
                        rs.getString("zoning"),
                        rs.getString("nature_of_property"),
                        rs.getString("primary_purpose"),
                        rs.getString("legal_description"));
                sales.add(sale);

                if (counter > 50) {
                    break;
                }
                counter++;
            }
        }
        return sales;
    }

    public List<HomeSale> filterSalesByCriteria(String councilName, String propertyType,
            int minPrice, int maxPrice, String areaType) throws SQLException {
        List<HomeSale> sales = new ArrayList<HomeSale>();
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

        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HomeSale sale = new HomeSale(
                        rs.getInt("property_id"),
                        rs.getDate("download_date"),
                        rs.getString("council_name"),
                        rs.getInt("purchase_price"),
                        rs.getString("address"),
                        rs.getInt("post_code"),
                        rs.getString("property_type"),
                        rs.getInt("strata_lot_number"),
                        rs.getString("property_name"),
                        rs.getDouble("area"),
                        rs.getString("area_type"),
                        rs.getDate("contract_date"),
                        rs.getDate("settlement_date"),
                        rs.getString("zoning"),
                        rs.getString("nature_of_property"),
                        rs.getString("primary_purpose"),
                        rs.getString("legal_description"));
                sales.add(sale);
            }
        }
        return sales;
    }
}
