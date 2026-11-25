package com.retailiq.controller;

import com.retailiq.dao.CategoryDAO;
import com.retailiq.dao.ProductDAO;
import com.retailiq.model.Category;
import com.retailiq.model.Product;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ProductServlet - Controller for Product Catalog CRUD operations.
 * 
 * Endpoints:
 * - GET  /products?action=list   -> Displays the catalog
 * - GET  /products?action=new    -> Shows Add Product form
 * - GET  /products?action=edit   -> Shows Edit Product form
 * - POST /products?action=save   -> Handles Insert and Update
 * - GET  /products?action=delete -> Deletes a product
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "new":
                showNewForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteProduct(request, response);
                break;
            case "list":
            default:
                listProducts(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (!isAuthenticated(request, response)) return;

        String action = request.getParameter("action");
        if ("save".equalsIgnoreCase(action)) {
            saveProduct(request, response);
        } else {
            listProducts(request, response);
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Product> products = productDAO.findAll();
        request.setAttribute("products", products);
        request.getRequestDispatcher("/WEB-INF/views/products/list.jsp").forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Category> categories = categoryDAO.findAll();
        request.setAttribute("categories", categories);
        request.setAttribute("product", new Product());
        request.getRequestDispatcher("/WEB-INF/views/products/form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Product existingProduct = productDAO.findById(id);
        List<Category> categories = categoryDAO.findAll();

        request.setAttribute("product", existingProduct);
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("/WEB-INF/views/products/form.jsp").forward(request, response);
    }

    private void saveProduct(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String idStr = request.getParameter("productId");
            int categoryId = Integer.parseInt(request.getParameter("categoryId"));
            String sku = request.getParameter("sku");
            String productName = request.getParameter("productName");
            BigDecimal costPrice = new BigDecimal(request.getParameter("costPrice"));
            BigDecimal unitPrice = new BigDecimal(request.getParameter("unitPrice"));
            int currentStock = Integer.parseInt(request.getParameter("currentStock"));
            int safetyStock = Integer.parseInt(request.getParameter("safetyStock"));
            int leadTimeDays = Integer.parseInt(request.getParameter("leadTimeDays"));

            Product p = new Product();
            p.setCategoryId(categoryId);
            p.setSku(sku);
            p.setProductName(productName);
            p.setCostPrice(costPrice);
            p.setUnitPrice(unitPrice);
            p.setCurrentStock(currentStock);
            p.setSafetyStock(safetyStock);
            p.setLeadTimeDays(leadTimeDays);

            if (idStr == null || idStr.trim().isEmpty() || "0".equals(idStr.trim())) {
                // Insert new product
                productDAO.insert(p);
                response.sendRedirect(request.getContextPath() + "/products?action=list&msg=created");
            } else {
                // Update existing product
                p.setProductId(Integer.parseInt(idStr));
                productDAO.update(p);
                response.sendRedirect(request.getContextPath() + "/products?action=list&msg=updated");
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Error saving product: " + e.getMessage());
            showNewForm(request, response);
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            productDAO.delete(id);
            response.sendRedirect(request.getContextPath() + "/products?action=list&msg=deleted");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/products?action=list&error=delete_failed");
        }
    }

    // Security check helper
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
