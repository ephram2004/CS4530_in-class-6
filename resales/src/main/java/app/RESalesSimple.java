package app;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class RESalesSimple {

    public static void main(String[] args) {
        Javalin.create(config -> {
            config.router.apiBuilder(() -> {
                // Health check
                get("/", ctx -> ctx.result("Sales service is running"));
                
                // Sales routes
                path("sales", () -> {
                    get(ctx -> {
                        String councilName = ctx.queryParam("councilname");
                        String propertyType = ctx.queryParam("propertytype");
                        String areaType = ctx.queryParam("areatype");
                        int minPrice = ctx.queryParam("minprice") != null ? Integer.parseInt(ctx.queryParam("minprice"))
                                : -1;
                        int maxPrice = ctx.queryParam("maxprice") != null ? Integer.parseInt(ctx.queryParam("maxprice"))
                                : -1;

                        // Mock response for testing
                        ctx.json("[{\"id\": 1, \"price\": 500000, \"postcode\": 12345, \"councilName\": \"Test Council\", \"propertyType\": \"House\", \"areaType\": \"Residential\"}]");
                    });

                    post(ctx -> {
                        ctx.result("Sale created successfully");
                        ctx.status(201);
                    });

                    path("postcode/{postcode}", () -> {
                        get(ctx -> {
                            String postcode = ctx.pathParam("postcode");
                            ctx.json("[{\"id\": 1, \"price\": 500000, \"postcode\": " + postcode + "}]");
                        });
                    });

                    path("average/{postcode}", () -> {
                        get(ctx -> {
                            String postcode = ctx.pathParam("postcode");
                            ctx.result("450000.00");
                        });
                    });

                    path("price-history/propertyId/{propertyID}", () -> {
                        get(ctx -> {
                            String propertyID = ctx.pathParam("propertyID");
                            ctx.result("50000");
                        });
                    });

                    path("{saleID}", () -> {
                        get(ctx -> {
                            String saleID = ctx.pathParam("saleID");
                            ctx.json("{\"id\": " + saleID + ", \"price\": 500000, \"postcode\": 12345}");
                        });
                    });
                });
            });
        }).start(7071);

        System.out.println("✅ Sales service started at http://localhost:7071");
    }
} 