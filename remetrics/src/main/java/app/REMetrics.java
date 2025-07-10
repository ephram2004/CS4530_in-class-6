package app;

import helpers.HelperSQL;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sql.metric.Metric;
import sql.metric.MetricsController;
import sql.metric.MetricsDAO;

public class REMetrics {

    public static void main(String[] args) {
        // Export schema
        HelperSQL.exportSchemaToFile(Metric.class);

        // DAO and Controller setup
        var metricsDAO = new MetricsDAO();
        MetricsController metricsHandler = new MetricsController(metricsDAO);

        // Start server
        Javalin.create(config -> {
            // OpenAPI docs
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

            // Routes
            config.router.apiBuilder(() -> {
                // Health check
                get("/", ctx -> ctx.result("Real Estate Metrics server is running"));

                // Main GET + POST route
                path("metrics/{metric_name}/{metric_id}/{attribute}", () -> {
                    get(ctx -> {
                        metricsHandler.getMetricByID(
                                ctx,
                                ctx.pathParam("metric_name"),
                                ctx.pathParam("metric_id"),
                                ctx.pathParam("attribute")
                        );
                    });

                    post(ctx -> {
                        String metricName = ctx.pathParam("metric_name");
                        String metricId = ctx.pathParam("metric_id");
                        String attribute = ctx.pathParam("attribute");

                        System.out.printf("metricName=%s, metricId=%s, attribute=%s%n",
                                metricName, metricId, attribute);

                        if ("numaccessed".equals(attribute.toLowerCase())) {
                            metricsHandler.incrementNumAccessed(metricName, metricId);
                            ctx.status(201).result("Metric updated or created.");
                        } else {
                            ctx.status(400).result("Unsupported attribute: " + attribute);
                        }
                    });
                });
            });
        }).start(7072);

        // Correct console output
        System.out.println("✅ Javalin server started at http://localhost:7072");
        System.out.println("📘 Swagger UI: http://localhost:7072/swagger");
        System.out.println("📕 ReDoc UI:   http://localhost:7072/redoc");
        System.out.println("📘 OpenAPI:   http://localhost:7072/openapi");
    }
}
