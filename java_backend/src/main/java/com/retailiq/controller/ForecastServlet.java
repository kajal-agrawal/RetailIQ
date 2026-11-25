package com.retailiq.controller;

import com.retailiq.dao.ForecastDAO;
import com.retailiq.dao.ProductDAO;
import com.retailiq.model.ForecastResult;
import com.retailiq.model.HistoricalSalePoint;
import com.retailiq.model.Product;
import com.retailiq.service.AIRuleEngineService;
import com.retailiq.service.MLClientService;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ForecastServlet - Controller orchestrating ML Forecasting, AI Rules, and Reporting.
 * 
 * Endpoints:
 * - GET /forecast?action=view&id={productId}&horizon={7|14} -> Generates unified forecast view
 */
@WebServlet("/forecast")
public class ForecastServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ProductDAO productDAO;
    private ForecastDAO forecastDAO;
    private MLClientService mlClientService;
    private AIRuleEngineService aiRuleEngineService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productDAO = new ProductDAO();
        this.forecastDAO = new ForecastDAO();
        this.mlClientService = new MLClientService();
        this.aiRuleEngineService = new AIRuleEngineService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        showForecastView(request, response);
    }

    private void showForecastView(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Product> products = productDAO.findAll();
        request.setAttribute("products", products);

        if (products.isEmpty()) {
            request.getRequestDispatcher("/WEB-INF/views/forecast/forecast_view.jsp").forward(request, response);
            return;
        }

        // Determine selected product
        String idParam = request.getParameter("id");
        int productId = (idParam != null && !idParam.isEmpty()) ? 
                Integer.parseInt(idParam) : products.get(0).getProductId();

        Product selectedProduct = productDAO.findById(productId);
        if (selectedProduct == null) {
            selectedProduct = products.get(0);
            productId = selectedProduct.getProductId();
        }

        // Determine forecast horizon (7 or 14 days)
        String horizonParam = request.getParameter("horizon");
        int horizonDays = (horizonParam != null && !horizonParam.isEmpty()) ? 
                Integer.parseInt(horizonParam) : 7;

        // 1. Fetch historical sales points from MySQL
        List<HistoricalSalePoint> history = forecastDAO.getHistoricalDailySales(productId);

        // 2. Request ML Forecast from Flask API (with local fallback if offline)
        ForecastResult forecast = mlClientService.requestForecast(
                productId, selectedProduct.getLeadTimeDays(), horizonDays, 
                selectedProduct.getCurrentStock(), history);

        // 3. Apply AI IF-THEN Rule Expert System
        aiRuleEngineService.evaluateInventory(selectedProduct, forecast);

        // 4. Save audit record to forecast_logs in MySQL
        forecastDAO.saveForecastLog(
                productId,
                forecast.getRecommendedModel(),
                horizonDays,
                forecast.getChosenTotalDemand(),
                forecast.getSafetyStock(),
                forecast.getRecommendedOrderQuantity(),
                forecast.getAlertStatus()
        );

        // 5. Pass data to JSP view
        request.setAttribute("selectedProduct", selectedProduct);
        request.setAttribute("forecast", forecast);
        request.setAttribute("history", history);
        request.setAttribute("horizonDays", horizonDays);

        request.getRequestDispatcher("/WEB-INF/views/forecast/forecast_view.jsp").forward(request, response);
    }

    private boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login&error=session_expired");
            return false;
        }
        return true;
    }
}
