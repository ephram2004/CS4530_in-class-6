package sales;

import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SalesController {

    private SalesDAO homeSales;

    public SalesController(SalesDAO homeSales) {
        this.homeSales = homeSales;
    }

    // implements POST /sales
    public void createSale(Context ctx) {

        // Extract Home Sale from request body
        // TO DO override Validator exception method to report better error message
        HomeSale sale = ctx.bodyValidator(HomeSale.class)
                .get();

        // store new sale in data set
        try {
            homeSales.newSale(sale);
            ctx.result("Sale Created");
            ctx.status(201);
        } catch (SQLException e) {
            ctx.result("Failed to add sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    // implements Get /sales
    public void getAllSales(Context ctx) {
        try {
            List<HomeSale> allSales = homeSales.getAllSales();
            if (allSales.isEmpty()) {
                ctx.result("No Sales Found");
                ctx.status(404);
            } else {
                ctx.json(allSales);
                ctx.status(200);
            }
        } catch (SQLException e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    // implements GET /sales/{saleID}
    public void getSaleByID(Context ctx, int id) {
        try {
            Optional<HomeSale> sale = homeSales.getSaleById(id);
            sale.map(ctx::json).orElseGet(() -> {
                ctx.result("Sale not found");
                ctx.status(404);
                return ctx;
            });
        } catch (SQLException e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }

    // Implements GET /sales/postcode/{postcodeID}
    public void findSaleByPostCode(Context ctx, int postCode) {
        try {
            List<HomeSale> sales = homeSales.getSalesByPostCode(postCode);
            if (sales.isEmpty()) {
                ctx.result("No sales for postcode found");
                ctx.status(404);
            } else {
                ctx.json(sales);
                ctx.status(200);
            }
        } catch (SQLException e) {
            ctx.result("Error retrieving sale: " + e.getMessage());
            ctx.status(400);
        }
    }
}
