<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Invoice Receipt" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container py-4" style="max-width: 720px;">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <a href="${pageContext.request.contextPath}/sales?action=history" class="btn btn-outline-secondary btn-sm">
            <i class="bi bi-arrow-left me-1"></i> Back to History
        </a>
        <div>
            <button onclick="window.print()" class="btn btn-outline-primary btn-sm me-2">
                <i class="bi bi-printer me-1"></i> Print Receipt
            </button>
            <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-success btn-sm">
                <i class="bi bi-cart-plus me-1"></i> New Transaction
            </a>
        </div>
    </div>

    <c:if test="${param.msg == 'success'}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> Transaction completed successfully! Stock updated.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card shadow p-4 border" id="printableReceipt">
        <!-- Receipt Header -->
        <div class="text-center border-bottom pb-3 mb-3">
            <h3 class="fw-bold mb-1">Retail<span class="text-primary">IQ</span> Store</h3>
            <p class="text-muted small mb-0">Official Sales Invoice & Customer Receipt</p>
        </div>

        <div class="row mb-3 small">
            <div class="col-6">
                <strong>Invoice:</strong> <code>${transaction.invoiceNumber}</code><br>
                <strong>Date:</strong> <fmt:formatDate value="${transaction.transactionDate}" pattern="yyyy-MM-dd HH:mm:ss" />
            </div>
            <div class="col-6 text-end">
                <strong>Cashier:</strong> ${transaction.cashierName}<br>
                <strong>Payment Mode:</strong> <span class="badge bg-secondary">${transaction.paymentMethod}</span>
            </div>
        </div>

        <!-- Line Items Table -->
        <div class="table-responsive mb-3">
            <table class="table table-bordered table-sm align-middle mb-0">
                <thead class="table-light">
                    <tr class="small text-muted">
                        <th>SKU</th>
                        <th>Product Description</th>
                        <th class="text-center">Qty</th>
                        <th class="text-end">Unit Price</th>
                        <th class="text-end">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${transaction.items}">
                        <tr>
                            <td><code>${item.sku}</code></td>
                            <td class="fw-semibold">${item.productName}</td>
                            <td class="text-center">${item.quantity}</td>
                            <td class="text-end">$<fmt:formatNumber value="${item.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-end fw-semibold">$<fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
                <tfoot>
                    <tr class="table-light">
                        <th colspan="4" class="text-end fs-5">Grand Total Paid:</th>
                        <th class="text-end fs-5 text-primary fw-bold">
                            $<fmt:formatNumber value="${transaction.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/>
                        </th>
                    </tr>
                </tfoot>
            </table>
        </div>

        <div class="text-center text-muted small mt-2">
            <p class="mb-0">Thank you for shopping with RetailIQ!</p>
            <p class="mb-0">Inventory stock and audit logs updated atomically via ACID transaction.</p>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
