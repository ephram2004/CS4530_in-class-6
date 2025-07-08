package app;

import helpers.HelperSQL;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sql.sales.DynamicHomeSale;
import sql.sales.SalesController;
import sql.sales.SalesDAO;
import sql.metric.MetricsDAO;
import sql.metric.MetricsController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class REServer {

    public static void main(String[] args) {
        // exporting schema to JSON file
        HelperSQL.exportSchemaToFile(DynamicHomeSale.class);

        // in memory test data store
        var sales = new SalesDAO();
        var metrics = new MetricsDAO();

        // API implementation
        SalesController salesHandler = new SalesController(sales);

        MetricsController metricsHandler = new MetricsController(metrics);

        Javalin.create(config -> {
            // OpenAPI doc plugins
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Real Estate API");
                        info.setVersion("1.0.0");
                        info.setDescription("API for querying property sales");
                    });
                });
            }));
            config.registerPlugin(new SwaggerPlugin());
            config.registerPlugin(new ReDocPlugin());

            // Route builder
            config.router.apiBuilder(() -> {
                // Health check
                get("/", ctx -> ctx.result("Real Estate server is running"));
                
                path("sales", () -> {
                    get(ctx -> {
                        String councilName = ctx.queryParam("councilname");
                        String propertyType = ctx.queryParam("propertytype");
                        String areaType = ctx.queryParam("areatype");
                        int minPrice = ctx.queryParam("minprice") != null ? Integer.parseInt(ctx.queryParam("minprice"))
                                : -1;
                        int maxPrice = ctx.queryParam("maxprice") != null ? Integer.parseInt(ctx.queryParam("maxprice"))
                                : -1;

                        boolean hasFilter = councilName != null || propertyType != null || areaType != null
                                || minPrice >= 0 || maxPrice >= 0;

                        if (hasFilter) {
                            salesHandler.filterSalesByCriteria(ctx, councilName, propertyType, minPrice, maxPrice,
                                    areaType);
                        } else {
                            salesHandler.getAllSales(ctx);
                        }
                    });

                    post(salesHandler::createSale);

                    path("postcode/{postcode}", () -> { 
                        get(ctx -> {
                        // increment access count
                        metricsHandler.incrementNumAccessed("postcode", ctx.pathParam("postcode"));
                        salesHandler.findSaleByPostCode(ctx, Integer.parseInt(ctx.pathParam("postcode")));
                        });
                    });

                    path("price-history/propertyId/{propertyID}", ()
                            -> get(ctx -> salesHandler.findPriceHistoryByPropertyId(ctx, Integer.parseInt(ctx.pathParam("propertyID"))))
                    );
                    
                    path("average/{postcode}", ()
                            -> get(ctx -> salesHandler.averagePrice(ctx, Integer.parseInt(ctx.pathParam("postcode"))))
                    );

                    path("{saleID}", () -> { 
                        get(ctx -> {
                        // increment access count
                        metricsHandler.incrementNumAccessed("propertyid", ctx.pathParam("saleID"));
                        salesHandler.getSaleByID(ctx, Integer.parseInt(ctx.pathParam("saleID")));
                        });
                    });

                    path("{metric_name}/{metric_id}/{attribute}", () -> {
                        get(ctx -> metricsHandler.getMetricByID(
                            ctx,
                            ctx.pathParam("metric_name"),  
                            ctx.pathParam("metric_id"),   
                            ctx.pathParam("attribute")     
                        ));
                    });
                });
            });
        }).start(7070);

        // Console output
        System.out.println(
                "✅ Javalin server started at http://localhost:7070");
        System.out.println(
                "📘 Swagger UI: http://localhost:7070/swagger");
        System.out.println(
                "📕 ReDoc UI:   http://localhost:7070/redoc");
        System.out.println(
                "📘 OpenAPI:   http://localhost:7070/openapi");
    }
}
