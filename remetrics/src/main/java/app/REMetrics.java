package app;

import helpers.HelperSQL;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import sql.metric.Metric;
import sql.metric.MetricsController;
import sql.metric.MetricsDAO;

public class REMetrics {

    public static void main(String[] args) {
        // exporting schema to JSON file
        HelperSQL.exportSchemaToFile(Metric.class);

        // in memory test data store
        var metricsDAO = new MetricsDAO();

        // API implementation
        MetricsController metricsHandler = new MetricsController(metricsDAO);

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
                // Sales routes
                path("metrics", () -> {
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
        }).start(7072);

        // Console output
        System.out.println(
                "✅ Javalin server started at http://localhost:7071");
        System.out.println(
                "📘 Swagger UI: http://localhost:7071/swagger");
        System.out.println(
                "📕 ReDoc UI:   http://localhost:7071/redoc");
        System.out.println(
                "📘 OpenAPI:   http://localhost:7071/openapi");
    }
}
