package app;

import helpers.HelperSQL;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import sales.DynamicHomeSale;
import sales.SalesController;
import sales.SalesDAO;
import sales.SalesDAO;
import sales.SalesController;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

import static io.javalin.apibuilder.ApiBuilder.*;

public class REServer {

    public static void main(String[] args) {
        // exporting schema to JSON file
        HelperSQL.exportSchemaToFile(DynamicHomeSale.class);

        System.out.println(HelperSQL.insertBySQLBuilder(DynamicHomeSale.class, "property_sales"));

        // in memory test data store
        var sales = new SalesDAO();

        // API implementation
        SalesController salesHandler = new SalesController(sales);

        // start Javalin on port 707  0
        var app = Javalin.create()
                .get("/", ctx -> ctx.result("Real Estate server is running"))
                .start(7070);

        Javalin.create(config -> {
            // OpenAPI doc plugins
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> info.setTitle("Real Estate API"));
                });
            }));
            config.registerPlugin(new SwaggerPlugin());
            config.registerPlugin(new ReDocPlugin());

            // return a sale by sale ID
            app.get("/sales/{saleID}", ctx -> {
                salesHandler.getSaleByID(ctx, Integer.parseInt(ctx.pathParam("saleID")));
            });
            // get all sales records - could be big!
            app.get("/sales", ctx -> {
                String councilName = ctx.queryParam("councilname");
                String propertyType = ctx.queryParam("propertytype");
                String areaType = ctx.queryParam("areatype");
                int minPrice = ctx.queryParam("minprice") != null
                        ? Integer.parseInt(ctx.queryParam("minprice"))
                        : -1;
                int maxPrice = ctx.queryParam("maxprice") != null
                        ? Integer.parseInt(ctx.queryParam("maxprice"))
                        : -1;

                boolean hasFilter = councilName != null || propertyType != null
                        || areaType != null || minPrice < 0 || maxPrice < 0;
                if (hasFilter) {
                    salesHandler.filterSalesByCriteria(ctx, councilName,
                            propertyType, minPrice, maxPrice, areaType);
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
            app.get("sales/propertyId/{propertyID}", ctx -> {
                salesHandler.findPriceHistoryByPropertyId(ctx,
                        Integer.parseInt(ctx.pathParam("propertyID")));
            });
            // Get average sale price for a specific postcode
            app.get("/sales/average/{postcode}", ctx -> {
                salesHandler.averagePrice(ctx, Integer.parseInt(ctx.pathParam("postcode")));
            });
        });
      
            // Route builder 
            config.router.apiBuilder(() -> {
                // Health check
                get("/", ctx -> ctx.result("Real Estate server is running"));
                // Sales routes
                path("sales", () -> {
                    get(ctx -> {
                        String councilName = ctx.queryParam("councilname");
                        String propertyType = ctx.queryParam("propertytype");
                        String areaType = ctx.queryParam("areatype");
                        int minPrice = ctx.queryParam("minprice") != null ? Integer.parseInt(ctx.queryParam("minprice")) : -1;
                        int maxPrice = ctx.queryParam("maxprice") != null ? Integer.parseInt(ctx.queryParam("maxprice")) : -1;

                        boolean hasFilter = councilName != null || propertyType != null || areaType != null || minPrice >= 0 || maxPrice >= 0;

                        if (hasFilter) {
                            salesHandler.filterSalesByCriteria(ctx, councilName, propertyType, minPrice, maxPrice, areaType);
                        } else {
                            salesHandler.getAllSales(ctx);
                        }
                    });

                    post(salesHandler::createSale);

                    path("postcode/{postcode}", () ->
                        get(ctx -> salesHandler.findSaleByPostCode(ctx, Integer.parseInt(ctx.pathParam("postcode"))))
                    );

                    path("propertyId/{propertyID}", () ->
                        get(ctx -> salesHandler.findPriceHistoryByPropertyId(ctx, Integer.parseInt(ctx.pathParam("propertyID"))))
                    );

                    path("average/{postcode}", () ->
                        get(ctx -> salesHandler.averagePrice(ctx, Integer.parseInt(ctx.pathParam("postcode"))))
                    );

                    path("{saleID}", () ->
                        get(ctx -> salesHandler.getSaleByID(ctx, Integer.parseInt(ctx.pathParam("saleID"))))
                    );
                });
            });
        }).start(7070);

    // Console output
    System.out.println("✅ Javalin server started at http://localhost:7070");
    System.out.println("📘 Swagger UI: http://localhost:7070/swagger-ui");
    System.out.println("📕 ReDoc UI:   http://localhost:7070/redoc");
    System.out.println("📘 OpenAPI:   http://localhost:7070/openapi");
    }
}
