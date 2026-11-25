<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Product Catalog & Inventory" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container-fluid px-4 py-4">
    <!-- Breadcrumb & Header -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">Product Inventory Catalog</h2>
            <p class="text-muted small mb-0">Manage SKU specifications, safety stock levels, and supplier lead times</p>
        </div>
        <a href="${pageContext.request.contextPath}/products?action=new" class="btn btn-primary">
            <i class="bi bi-plus-circle me-1"></i> Add New Product
        </a>
    </div>

    <!-- Notification Messages -->
    <c:if test="${param.msg == 'created'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> Product created successfully!
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.msg == 'updated'}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <i class="bi bi-info-circle-fill me-1"></i> Product updated successfully!
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${param.msg == 'deleted'}">
        <div class="alert alert-warning alert-dismissible fade show" role="alert">
            <i class="bi bi-trash-fill me-1"></i> Product deleted successfully.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Product Table Card -->
    <div class="card shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="text-uppercase small text-muted">
                            <th class="ps-3">ID</th>
                            <th>SKU</th>
                            <th>Product Name</th>
                            <th>Category</th>
                            <th class="text-end">Cost ($)</th>
                            <th class="text-end">Unit Price ($)</th>
                            <th class="text-center">Current Stock</th>
                            <th class="text-center">Safety Stock</th>
                            <th class="text-center">Lead Time</th>
                            <th class="text-center">Status</th>
                            <th class="text-end pe-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty products}">
                                <c:forEach var="p" items="${products}">
                                    <tr>
                                        <td class="ps-3 text-muted small">${p.productId}</td>
                                        <td><code>${p.sku}</code></td>
                                        <td class="fw-semibold text-dark">${p.productName}</td>
                                        <td>
                                            <span class="badge bg-light text-dark border">
                                                ${p.categoryName}
                                            </span>
                                        </td>
                                        <td class="text-end"><fmt:formatNumber value="${p.costPrice}" type="currency" currencySymbol="$" /></td>
                                        <td class="text-end fw-semibold"><fmt:formatNumber value="${p.unitPrice}" type="currency" currencySymbol="$" /></td>
                                        
                                        <!-- Current Stock with color cue -->
                                        <td class="text-center">
                                            <span class="fw-bold ${p.currentStock <= p.safetyStock ? 'text-danger' : 'text-success'}">
                                                ${p.currentStock}
                                            </span>
                                        </td>
                                        <td class="text-center text-muted">${p.safetyStock}</td>
                                        <td class="text-center text-muted">${p.leadTimeDays} days</td>
                                        
                                        <!-- Stock Status Badge -->
                                        <td class="text-center">
                                            <c:choose>
                                                <c:when test="${p.stockStatus == 'OUT_OF_STOCK'}">
                                                    <span class="badge bg-danger badge-status">Out of Stock</span>
                                                </c:when>
                                                <c:when test="${p.stockStatus == 'LOW_STOCK'}">
                                                    <span class="badge bg-warning text-dark badge-status">Low Stock</span>
                                                </c:when>
                                                <c:when test="${p.stockStatus == 'OVERSTOCK'}">
                                                    <span class="badge bg-info text-dark badge-status">Overstock</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success badge-status">Optimal</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <!-- Action Buttons -->
                                        <td class="text-end pe-3">
                                            <a href="${pageContext.request.contextPath}/products?action=edit&id=${p.productId}" 
                                               class="btn btn-outline-secondary btn-sm me-1" title="Edit Product">
                                                <i class="bi bi-pencil-square"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/products?action=delete&id=${p.productId}" 
                                               class="btn btn-outline-danger btn-sm" 
                                               onclick="return confirm('Are you sure you want to delete ${p.productName}?');" 
                                               title="Delete Product">
                                                <i class="bi bi-trash"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="11" class="text-center py-4 text-muted">
                                        <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                                        No products found in the catalog.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
