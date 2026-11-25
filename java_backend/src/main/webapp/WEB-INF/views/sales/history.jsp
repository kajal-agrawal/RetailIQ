<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Sales Order History" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container-fluid px-4 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">Sales Transaction History</h2>
            <p class="text-muted small mb-0">Chronological archive of customer checkout orders and receipts</p>
        </div>
        <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary">
            <i class="bi bi-cart-plus me-1"></i> New Sale (POS)
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="small text-uppercase text-muted">
                            <th class="ps-3">Invoice Number</th>
                            <th>Date & Time</th>
                            <th>Cashier / Staff</th>
                            <th>Payment Method</th>
                            <th class="text-end">Total Amount</th>
                            <th class="text-center pe-3">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty transactions}">
                                <c:forEach var="t" items="${transactions}">
                                    <tr>
                                        <td class="ps-3">
                                            <span class="fw-bold text-primary"><code>${t.invoiceNumber}</code></span>
                                        </td>
                                        <td class="text-muted">
                                            <fmt:formatDate value="${t.transactionDate}" pattern="yyyy-MM-dd HH:mm:ss" />
                                        </td>
                                        <td>
                                            <i class="bi bi-person me-1 text-secondary"></i> ${t.cashierName}
                                        </td>
                                        <td>
                                            <span class="badge ${t.paymentMethod == 'CARD' ? 'bg-primary' : (t.paymentMethod == 'UPI' ? 'bg-info text-dark' : 'bg-secondary')}">
                                                ${t.paymentMethod}
                                            </span>
                                        </td>
                                        <td class="text-end fw-bold">
                                            $<fmt:formatNumber value="${t.totalAmount}" minFractionDigits="2" maxFractionDigits="2" />
                                        </td>
                                        <td class="text-center pe-3">
                                            <a href="${pageContext.request.contextPath}/sales?action=invoice&inv=${t.invoiceNumber}" 
                                               class="btn btn-sm btn-outline-secondary">
                                                <i class="bi bi-file-earmark-text me-1"></i> View Receipt
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="text-center py-5 text-muted">
                                        <i class="bi bi-receipt fs-2 d-block mb-2"></i>
                                        No sales transactions recorded yet.
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
