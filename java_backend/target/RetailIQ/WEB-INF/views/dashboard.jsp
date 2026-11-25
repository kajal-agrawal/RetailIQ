<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Executive Business Analytics Dashboard" />
</jsp:include>
<jsp:include page="common/navbar.jsp" />

<!-- Load Chart.js CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>

<div class="container-fluid px-4 py-4">
    <!-- Header & Quick Actions -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
        <div>
            <h2 class="fw-bold mb-1">
                <i class="bi bi-speedometer2 text-primary me-2"></i>Executive Analytics Dashboard
            </h2>
            <p class="text-muted small mb-0">Real-time store performance, revenue velocity, and automated inventory health</p>
        </div>
        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/sales?action=pos" class="btn btn-primary btn-sm">
                <i class="bi bi-cart-plus me-1"></i> New POS Sale
            </a>
            <a href="${pageContext.request.contextPath}/inventory?action=status" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-boxes me-1"></i> Restock Inventory
            </a>
            <a href="${pageContext.request.contextPath}/forecast?action=view" class="btn btn-outline-warning text-dark btn-sm fw-semibold">
                <i class="bi bi-cpu me-1"></i> Run AI Forecast
            </a>
        </div>
    </div>

    <!-- 4 Top KPI Cards -->
    <div class="row g-3 mb-4">
        <!-- KPI 1: Total Revenue -->
        <div class="col-xl-3 col-md-6">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-success">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small text-uppercase fw-semibold">Total Lifetime Revenue</div>
                        <div class="fs-3 fw-bold text-dark mt-1">
                            $<fmt:formatNumber value="${totalRevenue}" minFractionDigits="2" maxFractionDigits="2" />
                        </div>
                    </div>
                    <div class="bg-success-subtle text-success p-3 rounded-circle fs-4">
                        <i class="bi bi-currency-dollar"></i>
                    </div>
                </div>
                <div class="small text-muted mt-2">
                    Current Month: <strong>$<fmt:formatNumber value="${monthlyRevenue}" minFractionDigits="2" maxFractionDigits="2" /></strong>
                </div>
            </div>
        </div>

        <!-- KPI 2: Total Orders -->
        <div class="col-xl-3 col-md-6">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-primary">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small text-uppercase fw-semibold">Total Transactions</div>
                        <div class="fs-3 fw-bold text-dark mt-1">
                            ${totalOrders}
                        </div>
                    </div>
                    <div class="bg-primary-subtle text-primary p-3 rounded-circle fs-4">
                        <i class="bi bi-receipt"></i>
                    </div>
                </div>
                <div class="small text-muted mt-2">
                    Completed POS customer receipts
                </div>
            </div>
        </div>

        <!-- KPI 3: Catalog Products -->
        <div class="col-xl-3 col-md-6">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-info">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small text-uppercase fw-semibold">Active Products in Catalog</div>
                        <div class="fs-3 fw-bold text-dark mt-1">
                            ${totalProducts}
                        </div>
                    </div>
                    <div class="bg-info-subtle text-info p-3 rounded-circle fs-4">
                        <i class="bi bi-box-seam"></i>
                    </div>
                </div>
                <div class="small text-muted mt-2">
                    Unique active items across 4 categories
                </div>
            </div>
        </div>

        <!-- KPI 4: Low Stock Alerts -->
        <div class="col-xl-3 col-md-6">
            <div class="card p-3 shadow-sm h-100 border-start border-4 ${lowStockCount > 0 ? 'border-danger' : 'border-success'}">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="text-muted small text-uppercase fw-semibold">Stockout Risk Alerts</div>
                        <div class="fs-3 fw-bold ${lowStockCount > 0 ? 'text-danger' : 'text-success'} mt-1">
                            ${lowStockCount}
                        </div>
                    </div>
                    <div class="${lowStockCount > 0 ? 'bg-danger-subtle text-danger' : 'bg-success-subtle text-success'} p-3 rounded-circle fs-4">
                        <i class="bi bi-exclamation-triangle"></i>
                    </div>
                </div>
                <div class="small mt-2">
                    <c:choose>
                        <c:when test="${lowStockCount > 0}">
                            <a href="${pageContext.request.contextPath}/inventory?action=status" class="text-danger fw-semibold text-decoration-none">
                                Review items below safety stock &rarr;
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="text-success">All items above safety buffer</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <!-- Inventory Turnover Efficiency Callout (Data Analytics / Operations Research) -->
    <div class="card bg-white shadow-sm border p-3 mb-4">
        <div class="row align-items-center">
            <div class="col-md-8">
                <h6 class="fw-bold mb-1 d-flex align-items-center">
                    <i class="bi bi-arrow-repeat text-primary me-2 fs-5"></i>
                    Inventory Turnover Ratio (ITR): <span class="badge bg-primary fs-6 ms-2">${inventoryTurnover}x</span>
                </h6>
                <p class="text-muted small mb-0">
                    Reflects the efficiency of working capital cycling. An ITR of <strong>${inventoryTurnover}x</strong> demonstrates healthy product velocity without excessive holding costs.
                </p>
            </div>
            <div class="col-md-4 text-md-end mt-2 mt-md-0">
                <span class="badge bg-light text-dark border p-2 small">
                    <i class="bi bi-check-circle-fill text-success me-1"></i> Operational Efficiency Healthy
                </span>
            </div>
        </div>
    </div>

    <!-- Charts Row: Timeline & Category Breakdown -->
    <div class="row g-4 mb-4">
        <!-- Sales Timeline Chart -->
        <div class="col-lg-8">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-graph-up me-1 text-primary"></i> 14-Day Daily Sales Revenue Trend ($)
                    </h5>
                    <span class="text-muted small">Actual Historical Performance</span>
                </div>
                <div class="card-body">
                    <canvas id="timelineChart" style="height: 300px; width: 100%;"></canvas>
                </div>
            </div>
        </div>

        <!-- Category Distribution Chart -->
        <div class="col-lg-4">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-pie-chart me-1 text-primary"></i> Category Revenue Share
                    </h5>
                </div>
                <div class="card-body d-flex flex-column justify-content-center align-items-center">
                    <div style="height: 240px; width: 240px;">
                        <canvas id="categoryChart"></canvas>
                    </div>
                    <div class="mt-3 w-100">
                        <ul class="list-group list-group-flush small">
                            <c:forEach var="c" items="${categoryBreakdown}">
                                <li class="list-group-item d-flex justify-content-between align-items-center px-0">
                                    <span>${c.categoryName}</span>
                                    <span class="fw-bold">${c.percentage}% ($<fmt:formatNumber value="${c.revenue}" minFractionDigits="0" maxFractionDigits="0"/>)</span>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Tables Row: Top Selling Products & Recent AI Forecast Logs -->
    <div class="row g-4">
        <!-- Fast-Moving Products (Sales Velocity) -->
        <div class="col-lg-7">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-lightning-charge me-1 text-warning"></i> Fast-Moving Products (Velocity & Profit Margins)
                    </h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr class="small text-muted text-uppercase">
                                    <th class="ps-3">SKU</th>
                                    <th>Product Name</th>
                                    <th>Category</th>
                                    <th class="text-center">Units Sold</th>
                                    <th class="text-end">Revenue</th>
                                    <th class="text-end pe-3">Margin</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="tp" items="${topProducts}">
                                    <tr>
                                        <td class="ps-3"><code>${tp.sku}</code></td>
                                        <td class="fw-semibold">${tp.productName}</td>
                                        <td><span class="badge bg-light text-dark border">${tp.categoryName}</span></td>
                                        <td class="text-center fw-bold">${tp.totalUnitsSold}</td>
                                        <td class="text-end fw-semibold">
                                            $<fmt:formatNumber value="${tp.totalRevenue}" minFractionDigits="2" maxFractionDigits="2" />
                                        </td>
                                        <td class="text-end pe-3 text-success fw-bold">
                                            ${tp.grossMarginPercentage}%
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Demand Projections -->
        <div class="col-lg-5">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-graph-up-arrow me-1 text-primary"></i> Recent Demand Projections
                    </h5>
                    <a href="${pageContext.request.contextPath}/forecast?action=view" class="btn btn-outline-primary btn-sm">
                        Forecasting Center &rarr;
                    </a>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-sm table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr class="small text-muted">
                                    <th class="ps-3">Product</th>
                                    <th>Forecast Method</th>
                                    <th class="text-center">Expected Demand</th>
                                    <th class="text-center">Reorder Target</th>
                                    <th class="text-end pe-3">Inventory Health</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty recentForecastLogs}">
                                        <c:forEach var="fl" items="${recentForecastLogs}">
                                            <tr>
                                                <td class="ps-3">
                                                    <div class="fw-semibold text-truncate" style="max-width: 130px;">${fl.productName}</div>
                                                    <small class="text-muted"><code>${fl.sku}</code></small>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${fl.modelUsed == 'LINEAR_REGRESSION'}">
                                                            <span class="badge bg-light text-primary border">Trend Projection</span>
                                                        </c:when>
                                                        <c:when test="${fl.modelUsed == 'RIDGE_REGRESSION'}">
                                                            <span class="badge bg-light text-info border">Ridge Forecast</span>
                                                        </c:when>
                                                        <c:when test="${fl.modelUsed == 'LASSO_REGRESSION'}">
                                                            <span class="badge bg-light text-warning text-dark border">Lasso Forecast</span>
                                                        </c:when>
                                                        <c:when test="${fl.modelUsed == 'RANDOM_FOREST'}">
                                                            <span class="badge bg-light text-success border">Pattern Ensemble</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-light text-dark border">Automated AI</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center fw-semibold">
                                                    ${fl.predictedSales} units
                                                </td>
                                                <td class="text-center fw-bold ${fl.recommendedReorderQty > 0 ? 'text-primary' : 'text-muted'}">
                                                    ${fl.recommendedReorderQty}
                                                </td>
                                                <td class="text-end pe-3">
                                                    <c:choose>
                                                        <c:when test="${fl.alertStatus == 'CRITICAL_STOCKOUT'}">
                                                            <span class="badge bg-danger">Critical</span>
                                                        </c:when>
                                                        <c:when test="${fl.alertStatus == 'LOW_STOCK'}">
                                                            <span class="badge bg-warning text-dark">Low Stock</span>
                                                        </c:when>
                                                        <c:when test="${fl.alertStatus == 'OVERSTOCK'}">
                                                            <span class="badge bg-info text-dark">Overstock</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-success">Optimal</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" class="text-center py-4 text-muted small">
                                                No forecast records logged yet.
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
    </div>
</div>

<!-- Chart.js Scripts -->
<script>
document.addEventListener("DOMContentLoaded", function() {
    // 1. Timeline Line Chart
    const timelineCtx = document.getElementById('timelineChart').getContext('2d');
    const timelineLabels = [
        <c:forEach var="entry" items="${salesTimeline}">
            '${entry.key}',
        </c:forEach>
    ];
    const timelineValues = [
        <c:forEach var="entry" items="${salesTimeline}">
            ${entry.value},
        </c:forEach>
    ];

    new Chart(timelineCtx, {
        type: 'line',
        data: {
            labels: timelineLabels,
            datasets: [{
                label: 'Daily Revenue ($)',
                data: timelineValues,
                borderColor: '#0d6efd',
                backgroundColor: 'rgba(13, 110, 253, 0.1)',
                fill: true,
                tension: 0.3,
                borderWidth: 2,
                pointRadius: 4,
                pointBackgroundColor: '#0d6efd'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) { return '$' + value; }
                    }
                }
            }
        }
    });

    // 2. Category Doughnut Chart
    const catCtx = document.getElementById('categoryChart').getContext('2d');
    const catLabels = [
        <c:forEach var="c" items="${categoryBreakdown}">
            '${c.categoryName}',
        </c:forEach>
    ];
    const catValues = [
        <c:forEach var="c" items="${categoryBreakdown}">
            ${c.revenue},
        </c:forEach>
    ];

    new Chart(catCtx, {
        type: 'doughnut',
        data: {
            labels: catLabels,
            datasets: [{
                data: catValues,
                backgroundColor: ['#0d6efd', '#198754', '#ffc107', '#0dcaf0', '#6c757d'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
});
</script>

<jsp:include page="common/footer.jsp" />
