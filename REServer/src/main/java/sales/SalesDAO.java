package sales;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesDAO {

    // TODO: Change this!!
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/PropertyData";
    private static final String JDBC_USER = "yk";
    private static final String JDBC_PASSWORD = "1234";

    public boolean newSale(HomeSale homeSale) throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO property_sales (property_id, download_date, council_name, " +
                                "purchase_price, address, post_code, property_type, " +
                                "strata_lot_number, property_name, area, area_type, " +
                                "contract_date, settlement_date, zoning, nature_of_property, " +
                                "primary_purpose, legal_description) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, homeSale.propertyId);
            stmt.setDate(2, homeSale.downloadDate);
            stmt.setString(3, homeSale.councilName);
            stmt.setInt(4, homeSale.purchasePrice);
            stmt.setString(5, homeSale.address);
            stmt.setInt(6, homeSale.postCode);
            stmt.setString(7, homeSale.propertyType);
            stmt.setInt(8, homeSale.strataLotNumber);
            stmt.setString(9, homeSale.propertyName);
            stmt.setDouble(10, homeSale.area);
            stmt.setString(11, homeSale.areaType);
            stmt.setDate(12, homeSale.contractDate);
            stmt.setDate(13, homeSale.settlementDate);
            stmt.setString(14, homeSale.zoning);
            stmt.setString(15, homeSale.natureOfProperty);
            stmt.setString(16, homeSale.primaryPurpose);
            stmt.setString(17, homeSale.legalDescription);
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
}
