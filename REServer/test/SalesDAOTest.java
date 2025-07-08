package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import sql.sales.DynamicHomeSale;
import sql.sales.SalesDAO;

import java.util.List;
import java.util.Optional;

public class SalesDAOTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Sample JSON for a new sale
        String json = """
        {
            "propertyId": 101,
            "purchasePrice": 850000,
            "postCode": 2000,
            "councilName": "City of Sydney",
            "address": "123 Redis Lane",
            "propertyType": "Apartment",
            "areaType": "sqm"
        }
        """;

        JsonNode node = mapper.readTree(json);
        DynamicHomeSale sale = new DynamicHomeSale(node);

        SalesDAO dao = new SalesDAO();

        // Save sale to Redis
        System.out.println("Saving new sale...");
        boolean inserted = dao.newSale(sale);
        System.out.println("Inserted: " + inserted);

        // Fetch by ID
        System.out.println("Fetching sale by ID...");
        Optional<DynamicHomeSale> fetched = dao.getSaleById(101);
        System.out.println("Fetched: " + fetched.orElse(null));

        // Fetch by postcode
        System.out.println("Fetching sales by postcode 2000...");
        List<DynamicHomeSale> byPostcode = dao.getSalesByPostCode(2000);
        byPostcode.forEach(s -> System.out.println("By postcode: " + s));

        // Fetch all
        System.out.println("Fetching all sales...");
        List<DynamicHomeSale> all = dao.getAllSales();
        all.forEach(s -> System.out.println("All: " + s));

        // Filtered test
        System.out.println("Filter test...");
        var filtered = dao.filterSalesByCriteria("City of Sydney", "Apartment", 800000, 900000, "sqm");
        filtered.forEach(f -> System.out.println("Filtered match: " + f));
    }
}
