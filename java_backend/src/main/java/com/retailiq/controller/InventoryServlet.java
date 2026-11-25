package com.retailiq.controller;

import com.retailiq.dao.InventoryLogDAO;
import com.retailiq.dao.ProductDAO;
import com.retailiq.model.InventoryLog;
import com.retailiq.model.Product;
import com.retailiq.model.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * InventoryServlet - Controller for stock health status, restocking, and audit logging.
 * 
 * Endpoints:
 * - GET  /inventory?action=status  -> Stock levels, low-stock warnings, and restock modal
 * - POST /inventory?action=restock -> Restocks inventory and writes audit logs
 * - GET  /inventory?action=logs    -> Chronological inventory audit trail
 */
@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ProductDAO productDAO;
    private InventoryLogDAO inventoryLogDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productDAO = new ProductDAO();
        this.inventoryLogDAO = new InventoryLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "status";
        }

        switch (action) {
            case "logs":
                showAuditLogs(request, response);
                break;
            case "status":
            default:
                showStockStatus(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if ("restock".equalsIgnoreCase(action)) {
            processRestock(request, response);
        } else {
            showStockStatus(request, response);
        }
    }

    private void showStockStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Product> products = productDAO.findAll();
        List<Product> lowStock = productDAO.findLowStock();

        request.setAttribute("products", products);
        request.setAttribute("lowStockProducts", lowStock);
        request.getRequestDispatcher("/WEB-INF/views/inventory/stock_status.jsp").forward(request, response);
    }

    private void processRestock(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String reason = request.getParameter("reason");

            boolean success = inventoryLogDAO.restockProduct(productId, currentUser.getUserId(), quantity, reason);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/inventory?action=status&msg=restocked");
            } else {
                response.sendRedirect(request.getContextPath() + "/inventory?action=status&error=restock_failed");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/inventory?action=status&error=" + e.getMessage());
        }
    }

    private void showAuditLogs(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<InventoryLog> logs = inventoryLogDAO.findRecent(100);
        request.setAttribute("logs", logs);
        request.getRequestDispatcher("/WEB-INF/views/inventory/audit_logs.jsp").forward(request, response);
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
