package com.retailiq.controller;

import com.retailiq.dao.AnalyticsDAO;
import com.retailiq.model.CategoryRevenue;
import com.retailiq.model.ForecastLogEntry;
import com.retailiq.model.TopProductMetric;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * DashboardServlet - Controller for the Executive Business Intelligence Dashboard.
 * 
 * Aggregates all KPI statistics, sales velocity metrics, category distributions,
 * inventory turnover, and AI forecast logs.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private AnalyticsDAO analyticsDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.analyticsDAO = new AnalyticsDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        // 1. KPI Summary Metrics
        BigDecimal totalRevenue = analyticsDAO.getTotalRevenue();
        BigDecimal monthlyRevenue = analyticsDAO.getMonthlyRevenue();
        int totalOrders = analyticsDAO.getTotalOrdersCount();
        int totalProducts = analyticsDAO.getTotalCatalogCount();
        int lowStockCount = analyticsDAO.getLowStockAlertCount();
        double inventoryTurnover = analyticsDAO.getInventoryTurnoverRatio();

        // 2. Visual Analytics Distributions
        List<CategoryRevenue> categoryBreakdown = analyticsDAO.getCategoryRevenueBreakdown();
        List<TopProductMetric> topProducts = analyticsDAO.getTopSellingProducts(5);
        Map<String, Double> salesTimeline = analyticsDAO.getRecentDailySalesTimeline(14);
        List<ForecastLogEntry> recentForecastLogs = analyticsDAO.getRecentForecastLogs(5);

        // 3. Set Request Attributes
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("monthlyRevenue", monthlyRevenue);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("totalProducts", totalProducts);
        request.setAttribute("lowStockCount", lowStockCount);
        request.setAttribute("inventoryTurnover", inventoryTurnover);

        request.setAttribute("categoryBreakdown", categoryBreakdown);
        request.setAttribute("topProducts", topProducts);
        request.setAttribute("salesTimeline", salesTimeline);
        request.setAttribute("recentForecastLogs", recentForecastLogs);

        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
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
