package app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import helpers.HelperSQL;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sql.sales.DynamicHomeSale;

public class REServer {

    public static void main(String[] args) {
        // exporting schema to JSON file
        HelperSQL.exportSchemaToFile(DynamicHomeSale.class);
        String salesUrl = "http://localhost:7071/sales/";
        String metricsUrl = "http://localhost:7072/metrics/";

        // client for sending reqs
        HttpClient client = HttpClient.newHttpClient();

        Javalin app = Javalin.create(config -> {
            // OpenAPI Plugin
            config.registerPlugin(new OpenApiPlugin(pluginConfig -> {
                pluginConfig.withDefinitionConfiguration((version, definition) -> {
                    definition.withInfo(info -> {
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
                // Sales routes
                path("sales", () -> {
                    get(ctx -> {
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
                                || areaType != null || minPrice >= 0 || maxPrice >= 0;

                        String url = salesUrl;
                        if (hasFilter) {
                            // Build query parameters
                            StringBuilder queryParams = new StringBuilder();
                            if (councilName != null) {
                                queryParams.append("councilname=").append(councilName).append("&");
                            }
                            if (propertyType != null) {
                                queryParams.append("propertytype=").append(propertyType)
                                        .append("&");
                            }
                            if (areaType != null) {
                                queryParams.append("areatype=").append(areaType).append("&");
                            }
                            if (minPrice >= 0) {
                                queryParams.append("minprice=").append(minPrice).append("&");
                            }
                            if (maxPrice >= 0) {
                                queryParams.append("maxprice=").append(maxPrice).append("&");
                            }

                            if (queryParams.length() > 0) {
                                url += "?" + queryParams.substring(0, queryParams.length() - 1);
                            }
                        }

                        try {
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build();
                            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                            ctx.result(res.body());
                        } catch (IllegalArgumentException e) {
                            ctx.status(400).result("Invalid URL format: " + e.getMessage());
                        } catch (IOException e) {
                            ctx.status(503);
                            ctx.result("Error connecting to sales service: " + e.getMessage());
                        } catch (InterruptedException e) {
                            ctx.result("Request interrupted: " + e.getMessage());
                            ctx.status(503);
                        }
                    });

                    post(ctx -> {
                        try {
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(salesUrl))
                                    .POST(HttpRequest.BodyPublishers.ofString(ctx.body()))
                                    .header("Content-Type", "application/json")
                                    .build();
                            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                            ctx.result(res.body());
                        } catch (IllegalArgumentException e) {
                            ctx.status(400).result("Invalid URL format: " + e.getMessage());
                        } catch (IOException e) {
                            ctx.status(503);
                            ctx.result("Error connecting to sales service: " + e.getMessage());
                        } catch (InterruptedException e) {
                            ctx.result("Request interrupted: " + e.getMessage());
                            ctx.status(503);
                        }
                    });

                    path("postcode/{postcode}", () -> {
                        get(ctx -> {
                            String postcode = ctx.pathParam("postcode");
                            String url = salesUrl + "postcode/" + postcode;
                            try {
                                HttpRequest salesReq = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET()
                                        .build();
                                HttpResponse<String> res = client.send(salesReq,
                                        HttpResponse.BodyHandlers.ofString());
                                ctx.result(res.body());
                            } catch (IllegalArgumentException e) {
                                ctx.status(400).result("Invalid URL format: " + e.getMessage());
                            } catch (IOException e) {
                                ctx.status(503);
                                ctx.result("Error connecting to sales service: " + e.getMessage());
                            } catch (InterruptedException e) {
                                ctx.result("Request interrupted: " + e.getMessage());
                                ctx.status(503);
                            }
                        });
                    });

                    path("average/{postcode}", () -> get(ctx -> {
                        String postcode = ctx.pathParam("postcode");
                        String url = salesUrl + "average/" + postcode;
                        try {
                            HttpRequest salesReq = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build();
                            HttpResponse<String> res = client.send(salesReq, HttpResponse.BodyHandlers.ofString());

                            ctx.result(res.body());
                        } catch (IllegalArgumentException e) {
                            ctx.status(400).result("Invalid URL format: " + e.getMessage());
                        } catch (IOException e) {
                            ctx.status(503);
                            ctx.result("Error connecting to sales service: " + e.getMessage());
                        } catch (InterruptedException e) {
                            ctx.result("Request interrupted: " + e.getMessage());
                            ctx.status(503);
                        }
                    }));

                    path("price-history/propertyId/{propertyID}", () -> get(ctx -> {
                        String propertyID = ctx.pathParam("propertyID");
                        String url = salesUrl + "price-history/propertyId/" + propertyID;
                        try {
                            HttpRequest salesReq = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build();
                            HttpResponse<String> res = client.send(salesReq, HttpResponse.BodyHandlers.ofString());

                            ctx.result(res.body());
                        } catch (IllegalArgumentException e) {
                            ctx.status(400).result("Invalid URL format: " + e.getMessage());
                        } catch (IOException e) {
                            ctx.status(503);
                            ctx.result("Error connecting to sales service: " + e.getMessage());
                        } catch (InterruptedException e) {
                            ctx.result("Request interrupted: " + e.getMessage());
                            ctx.status(503);
                        }
                    }));

                    path("{saleID}", () -> get(ctx -> {
                        String saleID = ctx.pathParam("saleID");
                        String url = salesUrl + saleID;
                        try {
                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build();
                            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

                            ctx.result(res.body());
                        } catch (IllegalArgumentException e) {
                            ctx.status(400).result("Invalid URL format: " + e.getMessage());
                        } catch (IOException e) {
                            ctx.status(503);
                            ctx.result("Error connecting to sales service: " + e.getMessage());
                        } catch (InterruptedException e) {
                            ctx.result("Request interrupted: " + e.getMessage());
                            ctx.status(503);
                        }
                    }));

                    path("{metric_name}/{metric_id}/{attribute}", () -> {
                        get(ctx -> {
                            String metricName = ctx.pathParam("metric_name");
                            String metricId = ctx.pathParam("metric_id");
                            String attribute = ctx.pathParam("attribute");
                            String url = metricsUrl + metricName + "/" + metricId + "/" + attribute;
                            try {
                                HttpRequest req = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .GET()
                                        .build();
                                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                                ctx.result(res.body());
                            } catch (IllegalArgumentException e) {
                                ctx.status(400).result("Invalid URL format: " + e.getMessage());
                            } catch (IOException e) {
                                ctx.status(503);
                                ctx.result("Error connecting to sales service: " + e.getMessage());
                            } catch (InterruptedException e) {
                                ctx.result("Request interrupted: " + e.getMessage());
                                ctx.status(503);
                            }
                        });
                    });
                });
            });
        });

        app.start(7070);

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
