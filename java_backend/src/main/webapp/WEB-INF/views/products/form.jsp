<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Product Form" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container py-4" style="max-width: 800px;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">${product.productId > 0 ? 'Edit Product' : 'Add New Product'}</h2>
            <p class="text-muted small mb-0">Fill in the product specifications, pricing, and safety stock thresholds</p>
        </div>
        <a href="${pageContext.request.contextPath}/products?action=list" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> Back to Catalog
        </a>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-octagon-fill me-1"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card shadow-sm p-4">
        <form action="${pageContext.request.contextPath}/products?action=save" method="POST">
            <!-- Hidden input for existing product ID -->
            <input type="hidden" name="productId" value="${product.productId}" />

            <div class="row g-3">
                <!-- SKU -->
                <div class="col-md-6">
                    <label for="sku" class="form-label small fw-semibold">SKU (Stock Keeping Unit) *</label>
                    <input type="text" class="form-control" id="sku" name="sku" 
                           value="${product.sku}" placeholder="e.g. ELEC-MOU-01" required />
                </div>

                <!-- Category -->
                <div class="col-md-6">
                    <label for="categoryId" class="form-label small fw-semibold">Category *</label>
                    <select class="form-select" id="categoryId" name="categoryId" required>
                        <option value="" disabled ${product.categoryId == 0 ? 'selected' : ''}>-- Select Category --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.categoryId}" ${product.categoryId == cat.categoryId ? 'selected' : ''}>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Product Name -->
                <div class="col-12">
                    <label for="productName" class="form-label small fw-semibold">Product Name *</label>
                    <input type="text" class="form-control" id="productName" name="productName" 
                           value="${product.productName}" placeholder="e.g. Ergonomic Wireless Optical Mouse" required />
                </div>

                <!-- Cost Price -->
                <div class="col-md-6">
                    <label for="costPrice" class="form-label small fw-semibold">Cost Price ($) *</label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" step="0.01" min="0" class="form-control" id="costPrice" name="costPrice" 
                               value="${product.costPrice != null ? product.costPrice : '0.00'}" required />
                    </div>
                </div>

                <!-- Unit Price -->
                <div class="col-md-6">
                    <label for="unitPrice" class="form-label small fw-semibold">Selling Unit Price ($) *</label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" step="0.01" min="0" class="form-control" id="unitPrice" name="unitPrice" 
                               value="${product.unitPrice != null ? product.unitPrice : '0.00'}" required />
                    </div>
                    <div class="form-text small">Must be greater than or equal to cost price.</div>
                </div>

                <!-- Current Stock -->
                <div class="col-md-4">
                    <label for="currentStock" class="form-label small fw-semibold">Current Stock Units *</label>
                    <input type="number" min="0" class="form-control" id="currentStock" name="currentStock" 
                           value="${product.currentStock}" required />
                </div>

                <!-- Safety Stock -->
                <div class="col-md-4">
                    <label for="safetyStock" class="form-label small fw-semibold">Safety Stock Threshold *</label>
                    <input type="number" min="0" class="form-control" id="safetyStock" name="safetyStock" 
                           value="${product.safetyStock > 0 ? product.safetyStock : 10}" required />
                    <div class="form-text small">Triggers low-stock alerts.</div>
                </div>

                <!-- Lead Time Days -->
                <div class="col-md-4">
                    <label for="leadTimeDays" class="form-label small fw-semibold">Supplier Lead Time (Days) *</label>
                    <input type="number" min="1" class="form-control" id="leadTimeDays" name="leadTimeDays" 
                           value="${product.leadTimeDays > 0 ? product.leadTimeDays : 3}" required />
                    <div class="form-text small">Used in ROP calculations.</div>
                </div>
            </div>

            <div class="mt-4 pt-3 border-top d-flex justify-content-end gap-2">
                <a href="${pageContext.request.contextPath}/products?action=list" class="btn btn-outline-secondary">Cancel</a>
                <button type="submit" class="btn btn-primary px-4">
                    <i class="bi bi-save me-1"></i> ${product.productId > 0 ? 'Update Product' : 'Save Product'}
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
