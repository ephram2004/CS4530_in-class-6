package app;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import sales.SalesDAO;
import sales.SalesController;

public class REServer {
    public static void main(String[] args) {

        // in memory test data store
        var sales = new SalesDAO();

        // API implementation
        SalesController salesHandler = new SalesController(sales);

        // start Javalin on port 7070
        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Real Estate server is running"))
                .start(7070);

        // configure endpoint handlers to process HTTP requests
        JavalinConfig config = new JavalinConfig();
        config.router.apiBuilder(() -> {
            // Sales records are immutable hence no PUT and DELETE

            // return a sale by sale ID
            app.get("/sales/{saleID}", ctx -> {
                salesHandler.getSaleByID(ctx, Integer.parseInt(ctx.pathParam("saleID")));
            });
            // get all sales records - could be big!
            app.get("/sales", ctx -> {
                // Extract all possible filter params
                String councilName = ctx.queryParam("councilname");
                String propertyType = ctx.queryParam("propertytype");
                String areaType = ctx.queryParam("areatype");
                int minPrice = ctx.queryParam("minprice") != null ? Integer.parseInt(ctx.queryParam("minprice"))
                        : -1;
                int maxPrice = ctx.queryParam("maxprice") != null ? Integer.parseInt(ctx.queryParam("maxprice"))
                        : -1;

                // If no filters, return all sales. Otherwise, filter.
                boolean hasFilter = councilName != null || propertyType != null || areaType != null || minPrice < 0
                        || maxPrice < 0;
                if (hasFilter) {
                    salesHandler.filterSalesByCriteria(ctx, councilName, propertyType, minPrice, maxPrice, areaType);
                } else {
                    salesHandler.getAllSales(ctx);
                }
            });
            // create a new sales record
            app.post("/sales", ctx -> {

                salesHandler.createSale(ctx);
            });
            // Get all sales for a specified postcode
            app.get("/sales/postcode/{postcode}", ctx -> {
                salesHandler.findSaleByPostCode(ctx, Integer.parseInt(ctx.pathParam("postcode")));
            });
        });

    }
}
