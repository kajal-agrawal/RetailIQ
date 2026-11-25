<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Inventory Health & Restocking" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container-fluid px-4 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">Inventory Management & Restock</h2>
            <p class="text-muted small mb-0">Monitor stock levels, safety stock buffers, and perform supplier restocks</p>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?action=logs" class="btn btn-outline-secondary">
            <i class="bi bi-journal-text me-1"></i> View Audit Trail
        </a>
    </div>

    <!-- Alert Banners -->
    <c:if test="${param.msg == 'restocked'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> Product restocked successfully! Audit log created.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Low Stock Warning Banner -->
    <c:if test="${not empty lowStockProducts}">
        <div class="alert alert-warning shadow-sm mb-4" role="alert">
            <h5 class="alert-heading fw-bold">
                <i class="bi bi-exclamation-triangle-fill me-1 text-danger"></i> Low Stock Alerts Detected!
            </h5>
            <p class="mb-0 small">The following products have breached their safety stock thresholds and require restocking:</p>
            <ul class="mb-0 mt-2 small">
                <c:forEach var="lp" items="${lowStockProducts}">
                    <li><strong>${lp.productName}</strong> (Current Stock: <span class="text-danger fw-bold">${lp.currentStock}</span>, Safety Threshold: ${lp.safetyStock}, Lead Time: ${lp.leadTimeDays} days)</li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <!-- Inventory Table -->
    <div class="card shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="small text-uppercase text-muted">
                            <th class="ps-3">SKU</th>
                            <th>Product Name</th>
                            <th>Category</th>
                            <th class="text-center">Current Stock</th>
                            <th class="text-center">Safety Stock</th>
                            <th class="text-center">Lead Time</th>
                            <th class="text-center">Stock Status</th>
                            <th class="text-end pe-3">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${products}">
                            <tr>
                                <td class="ps-3"><code>${p.sku}</code></td>
                                <td class="fw-semibold">${p.productName}</td>
                                <td><span class="badge bg-light text-dark border">${p.categoryName}</span></td>
                                <td class="text-center">
                                    <span class="fs-6 fw-bold ${p.currentStock <= p.safetyStock ? 'text-danger' : 'text-success'}">
                                        ${p.currentStock}
                                    </span>
                                </td>
                                <td class="text-center text-muted">${p.safetyStock} units</td>
                                <td class="text-center text-muted">${p.leadTimeDays} days</td>
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
                                <td class="text-end pe-3">
                                    <button type="button" class="btn btn-sm btn-primary" 
                                            onclick="openRestockModal(${p.productId}, '${p.productName}', ${p.currentStock})">
                                        <i class="bi bi-plus-slash-minus me-1"></i> Restock
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Restock Modal -->
<div class="modal fade" id="restockModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/inventory?action=restock" method="POST">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Restock Product Inventory</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" id="modalProductId" name="productId">
                    <div class="mb-3">
                        <label class="form-label small text-muted">Product</label>
                        <input type="text" id="modalProductName" class="form-control" readonly>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small text-muted">Current Stock On Hand</label>
                        <input type="text" id="modalCurrentStock" class="form-control" readonly>
                    </div>
                    <div class="mb-3">
                        <label for="quantity" class="form-label small fw-semibold">Quantity to Add (Units) *</label>
                        <input type="number" min="1" class="form-control" id="quantity" name="quantity" required placeholder="e.g. 50">
                    </div>
                    <div class="mb-3">
                        <label for="reason" class="form-label small fw-semibold">Audit Reason / PO Number</label>
                        <input type="text" class="form-control" id="reason" name="reason" placeholder="e.g. Purchase Order PO-2026-101">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-success">
                        <i class="bi bi-check-circle me-1"></i> Confirm Restock
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function openRestockModal(productId, productName, currentStock) {
    document.getElementById("modalProductId").value = productId;
    document.getElementById("modalProductName").value = productName;
    document.getElementById("modalCurrentStock").value = currentStock + " units";
    new bootstrap.Modal(document.getElementById('restockModal')).show();
}
</script>

<jsp:include page="../common/footer.jsp" />
