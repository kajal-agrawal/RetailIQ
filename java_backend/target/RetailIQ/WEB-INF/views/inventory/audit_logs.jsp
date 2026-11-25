<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Inventory Audit Trail" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container-fluid px-4 py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h2 class="fw-bold mb-1">Inventory Audit Trail Log</h2>
            <p class="text-muted small mb-0">Immutable chronological record of all stock deductions and purchase restocks</p>
        </div>
        <a href="${pageContext.request.contextPath}/inventory?action=status" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i> Back to Inventory
        </a>
    </div>

    <div class="card shadow-sm">
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr class="small text-uppercase text-muted">
                            <th class="ps-3">Log ID</th>
                            <th>Timestamp</th>
                            <th>Product</th>
                            <th>Change Type</th>
                            <th class="text-center">Qty Changed</th>
                            <th class="text-center">Stock Result</th>
                            <th>Staff</th>
                            <th>Reason / Reference</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty logs}">
                                <c:forEach var="log" items="${logs}">
                                    <tr>
                                        <td class="ps-3 text-muted small">${log.logId}</td>
                                        <td class="text-muted small">
                                            <fmt:formatDate value="${log.logTimestamp}" pattern="yyyy-MM-dd HH:mm:ss" />
                                        </td>
                                        <td>
                                            <div class="fw-semibold">${log.productName}</div>
                                            <small class="text-muted"><code>${log.sku}</code></small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${log.changeType == 'PURCHASE_RESTOCK'}">
                                                    <span class="badge bg-success">RESTOCK</span>
                                                </c:when>
                                                <c:when test="${log.changeType == 'SALE_DEDUCTION'}">
                                                    <span class="badge bg-primary">SALE</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${log.changeType}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center fw-bold ${log.quantityChanged > 0 ? 'text-success' : 'text-danger'}">
                                            ${log.quantityChanged > 0 ? '+' : ''}${log.quantityChanged}
                                        </td>
                                        <td class="text-center fw-bold text-dark">
                                            ${log.stockAfterChange}
                                        </td>
                                        <td>
                                            <i class="bi bi-person me-1 text-secondary"></i> ${log.username}
                                        </td>
                                        <td class="text-muted small">
                                            ${log.reason}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center py-5 text-muted">
                                        <i class="bi bi-journal-x fs-2 d-block mb-2"></i>
                                        No inventory audit logs found.
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
