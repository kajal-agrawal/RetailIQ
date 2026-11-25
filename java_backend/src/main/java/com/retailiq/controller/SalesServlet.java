package com.retailiq.controller;

import com.retailiq.dao.ProductDAO;
import com.retailiq.dao.SalesDAO;
import com.retailiq.model.Product;
import com.retailiq.model.SaleItem;
import com.retailiq.model.SaleTransaction;
import com.retailiq.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SalesServlet - Controller for Point of Sale (POS) and Sales Order History.
 * 
 * Endpoints:
 * - GET  /sales?action=pos     -> Interactive Point of Sale checkout counter
 * - POST /sales?action=checkout-> Executes atomic checkout
 * - GET  /sales?action=history -> Transaction ledger and invoice history
 * - GET  /sales?action=invoice -> Detailed view of a single invoice
 */
@WebServlet("/sales")
public class SalesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private SalesDAO salesDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.salesDAO = new SalesDAO();
        this.productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "pos";
        }

        switch (action) {
            case "history":
                listHistory(request, response);
                break;
            case "invoice":
                viewInvoice(request, response);
                break;
            case "pos":
            default:
                showPOS(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if ("checkout".equalsIgnoreCase(action)) {
            processCheckout(request, response);
        } else {
            showPOS(request, response);
        }
    }

    private void showPOS(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Product> products = productDAO.findAll();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/WEB-INF/views/sales/pos.jsp").forward(request, response);
    }

    private void processCheckout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String paymentMethod = request.getParameter("paymentMethod");
        String[] productIds = request.getParameterValues("productId");
        String[] quantities = request.getParameterValues("quantity");

        if (productIds == null || quantities == null || productIds.length == 0) {
            request.setAttribute("errorMessage", "Cart is empty. Please add at least one item.");
            showPOS(request, response);
            return;
        }

        try {
            List<SaleItem> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (int i = 0; i < productIds.length; i++) {
                int pid = Integer.parseInt(productIds[i]);
                int qty = Integer.parseInt(quantities[i]);
                if (qty <= 0) continue;

                Product p = productDAO.findById(pid);
                if (p == null) {
                    throw new IllegalArgumentException("Product ID " + pid + " not found.");
                }

                SaleItem item = new SaleItem(pid, p.getProductName(), qty, p.getUnitPrice());
                items.add(item);
                totalAmount = totalAmount.add(item.getSubtotal());
            }

            SaleTransaction tx = new SaleTransaction();
            tx.setUserId(currentUser.getUserId());
            tx.setTotalAmount(totalAmount);
            tx.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");

            String invoiceNumber = salesDAO.processSale(tx, items);
            response.sendRedirect(request.getContextPath() + "/sales?action=invoice&inv=" + invoiceNumber + "&msg=success");

        } catch (Exception e) {
            request.setAttribute("errorMessage", "Checkout failed: " + e.getMessage());
            showPOS(request, response);
        }
    }

    private void listHistory(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<SaleTransaction> history = salesDAO.findRecent(50);
        request.setAttribute("transactions", history);
        request.getRequestDispatcher("/WEB-INF/views/sales/history.jsp").forward(request, response);
    }

    private void viewInvoice(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String inv = request.getParameter("inv");
        if (inv != null && !inv.trim().isEmpty()) {
            SaleTransaction tx = salesDAO.findByInvoice(inv.trim());
            request.setAttribute("transaction", tx);
        }
        request.getRequestDispatcher("/WEB-INF/views/sales/invoice.jsp").forward(request, response);
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
