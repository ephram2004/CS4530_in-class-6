package sql.sales;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;

public class SalesController {

    private final SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    @OpenApi(summary = "Create a new home sale", operationId = "createSale", path = "/sales", methods = HttpMethod.POST, tags = {
        "Sales"}, requestBody = @OpenApiRequestBody(content = {
        @OpenApiContent(from = DynamicHomeSale.class)}), responses = {
        @OpenApiResponse(status = "201"),
        @OpenApiResponse(status = "400")
    })
    // implements POST /sales
    public void createSale(Context ctx) {

        ObjectMapper mapper = new ObjectMapper();

        /*try {
            JsonNode jsonSalesArray = mapper.readTree(ctx.body());
            List<DynamicHomeSale> salesList = new ArrayList<>();
            for (JsonNode sale : jsonSalesArray) {
                DynamicHomeSale homeSale = new DynamicHomeSale(sale);
                salesList.add(homeSale);
            }
        } catch (Exception e) {
            ctx.status(400);
            return;
        }
         */
        // Extract Home Sale from request body
        // TO DO override Validator exception method to report better error message
        try {
            JsonNode jsonSale = mapper.readTree(ctx.body()); // store new sale in data set
            DynamicHomeSale sale = new DynamicHomeSale(jsonSale);
            homeSales.newSale(sale);
            ctx.result("Sale Created");
            ctx.status(201);
        } catch (Exception e) {
            ctx.result("Failed to add sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    @OpenApi(summary = "Get all sales", operationId = "getAllSales", path = "/sales", methods = HttpMethod.GET, tags = {
        "Sales"}, responses = {
        @OpenApiResponse(status = "200", content = {
            @OpenApiContent(from = DynamicHomeSale[].class)}),
        @OpenApiResponse(status = "404")
    })
    // implements Get /sales
    public void getAllSales(Context ctx) {
        try {
            List<DynamicHomeSale> allSales = homeSales.getAllSales();
            if (allSales.isEmpty()) {
                ctx.result("No Sales Found");
                ctx.status(404);
            } else {
                ctx.json(allSales);
                ctx.status(200);
            }
        } catch (Exception e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    @OpenApi(summary = "Get sale by ID", operationId = "getSaleById", path = "/sales/{saleID}", methods = HttpMethod.GET, tags = {
        "Sales"}, pathParams = {
        @OpenApiParam(name = "saleID")
    }, responses = {
        @OpenApiResponse(status = "200", content = {
            @OpenApiContent(from = DynamicHomeSale.class)}),
        @OpenApiResponse(status = "404")
    })
    // implements GET /sales/{saleID}
    public void getSaleByID(Context ctx, int id) {
        try {
            Optional<DynamicHomeSale> sale = homeSales.getSaleById(id);
            sale.map(ctx::json).orElseGet(() -> {
                ctx.result("Sale not found");
                ctx.status(404);
                return ctx;
            });
        } catch (Exception e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    @OpenApi(summary = "Get sales by postcode", operationId = "getSalesByPostcode", path = "/sales/postcode/{postcode}", methods = HttpMethod.GET, tags = {
        "Sales"}, pathParams = {
        @OpenApiParam(name = "postcode")
    }, responses = {
        @OpenApiResponse(status = "200", content = {
            @OpenApiContent(from = DynamicHomeSale[].class)}),
        @OpenApiResponse(status = "404")
    })
    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(Context ctx, int postCode) {
        try {
            List<DynamicHomeSale> sales = homeSales.getSalesByPostCode(postCode);
            if (sales.isEmpty()) {
                ctx.result("No sales for postcode found");
                ctx.status(404);
            } else {
                ctx.json(sales);
                ctx.status(200);
            }
        } catch (Exception e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    @OpenApi(summary = "Get price history (diff) by property ID", operationId = "getPriceHistory", path = "/sales/propertyId/{propertyID}", methods = HttpMethod.GET, tags = {
        "Sales"}, pathParams = {
        @OpenApiParam(name = "propertyID")
    }, responses = {
        @OpenApiResponse(status = "200"),
        @OpenApiResponse(status = "400")
    })
    public void findPriceHistoryByPropertyId(Context ctx, int propertyId) {
        try {
            int priceDiff = homeSales.getPriceHistory(propertyId);
            ctx.result(String.valueOf(priceDiff));
            ctx.status(200);
        } catch (Exception e) {
            ctx.result("Error retrieving price difference: " + e.getMessage());
        }
    }

    @OpenApi(summary = "Filter sales by criteria", operationId = "filterSales", path = "/sales", // If you're keeping
            // filters as query
            // params on /sales
            methods = HttpMethod.GET, tags = {"Sales"}, queryParams = {
                @OpenApiParam(name = "councilname", required = false),
                @OpenApiParam(name = "propertytype", required = false),
                @OpenApiParam(name = "areatype", required = false),
                @OpenApiParam(name = "minprice", required = false),
                @OpenApiParam(name = "maxprice", required = false)
            }, responses = {
                @OpenApiResponse(status = "200", content = {
            @OpenApiContent(from = DynamicHomeSale[].class)}),
                @OpenApiResponse(status = "404")
            })
    public void filterSalesByCriteria(Context ctx, String councilName, String propertyType, int minPrice, int maxPrice,
            String areaType) {
        try {
            List<DynamicHomeSale> sales = homeSales.filterSalesByCriteria(councilName, propertyType, minPrice, maxPrice,
                    areaType);

            if (sales.isEmpty()) {
                ctx.result("No sales found");
                ctx.status(404);
            } else {
                ctx.json(sales);
                ctx.status(200);
            }
        } catch (Exception e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    @OpenApi(summary = "Get average sale price by postcode", operationId = "getAveragePrice", path = "/sales/average/{postcode}", methods = HttpMethod.GET, tags = {
        "Sales"}, pathParams = {
        @OpenApiParam(name = "postcode")
    }, responses = {
        @OpenApiResponse(status = "200"),
        @OpenApiResponse(status = "400")
    })
    public void averagePrice(Context ctx, int postCode) {
        try {
            double averagePrice = homeSales.getAveragePrice(postCode);
            ctx.result(Double.toString(averagePrice));
        } catch (Exception e) {
            ctx.result("Error getting average: " + e.getMessage());
            ctx.status(400);
        }
    }
}
