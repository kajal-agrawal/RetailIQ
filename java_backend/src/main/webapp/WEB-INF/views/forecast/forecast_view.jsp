<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - AI Sales Forecasting & Inventory Intelligence" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<c:set var="activeMae" value="${forecast.recommendedModel == 'LASSO_REGRESSION' ? forecast.lassoRegressionMae : (forecast.recommendedModel == 'RIDGE_REGRESSION' ? forecast.ridgeRegressionMae : (forecast.recommendedModel == 'RANDOM_FOREST' ? forecast.randomForestMae : forecast.linearRegressionMae))}" />

<c:choose>
    <c:when test="${forecast.recommendedModel == 'LASSO_REGRESSION'}">
        <c:set var="activeDaily" value="${forecast.lassoRegressionDaily}" />
    </c:when>
    <c:when test="${forecast.recommendedModel == 'RIDGE_REGRESSION'}">
        <c:set var="activeDaily" value="${forecast.ridgeRegressionDaily}" />
    </c:when>
    <c:when test="${forecast.recommendedModel == 'RANDOM_FOREST'}">
        <c:set var="activeDaily" value="${forecast.randomForestDaily}" />
    </c:when>
    <c:otherwise>
        <c:set var="activeDaily" value="${forecast.linearRegressionDaily}" />
    </c:otherwise>
</c:choose>

<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>

<div class="container-fluid px-4 py-4">
    <!-- Header & Product Switcher -->
    <div class="row align-items-center mb-4 g-3">
        <div class="col-md-6">
            <h2 class="fw-bold mb-1">
                <i class="bi bi-cpu text-primary me-2"></i>AI Sales Forecasting & Inventory Intelligence
            </h2>
            <p class="text-muted small mb-0">Predictive demand insights and automated inventory optimization recommendations</p>
        </div>
        <div class="col-md-6">
            <form action="${pageContext.request.contextPath}/forecast" method="GET" class="row g-2 justify-content-md-end align-items-center">
                <input type="hidden" name="action" value="view">
                <div class="col-auto">
                    <label for="id" class="small fw-semibold text-muted">Select Product:</label>
                </div>
                <div class="col-auto">
                    <select name="id" id="id" class="form-select form-select-sm fw-semibold" onchange="this.form.submit()">
                        <c:forEach var="p" items="${products}">
                            <option value="${p.productId}" ${selectedProduct.productId == p.productId ? 'selected' : ''}>
                                ${p.productName} (${p.sku})
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-auto">
                    <select name="horizon" class="form-select form-select-sm" onchange="this.form.submit()">
                        <option value="7" ${horizonDays == 7 ? 'selected' : ''}>7-Day Horizon</option>
                        <option value="14" ${horizonDays == 14 ? 'selected' : ''}>14-Day Horizon</option>
                    </select>
                </div>
            </form>
        </div>
    </div>

    <!-- 4 KPI Summary Cards -->
    <div class="row g-3 mb-4">
        <!-- KPI 1: AI Alert Status -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 ${forecast.alertStatus == 'CRITICAL_STOCKOUT' ? 'border-danger' : (forecast.alertStatus == 'LOW_STOCK' ? 'border-warning' : (forecast.alertStatus == 'OVERSTOCK' ? 'border-info' : 'border-success'))}">
                <div class="text-muted small text-uppercase fw-semibold">Inventory Status</div>
                <div class="d-flex align-items-center mt-2">
                    <c:choose>
                        <c:when test="${forecast.alertStatus == 'CRITICAL_STOCKOUT'}">
                            <span class="badge bg-danger fs-6">Critical Stockout</span>
                        </c:when>
                        <c:when test="${forecast.alertStatus == 'LOW_STOCK'}">
                            <span class="badge bg-warning text-dark fs-6">Low Stock Alert</span>
                        </c:when>
                        <c:when test="${forecast.alertStatus == 'OVERSTOCK'}">
                            <span class="badge bg-info text-dark fs-6">Overstocked</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge bg-success fs-6">Optimal Balance</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="small text-muted mt-2">
                    Current Stock: <strong>${selectedProduct.currentStock}</strong> units
                </div>
            </div>
        </div>

        <!-- KPI 2: Recommended Order Qty -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-primary">
                <div class="text-muted small text-uppercase fw-semibold">Recommended Reorder Qty</div>
                <div class="fs-3 fw-bold text-primary mt-1">
                    ${forecast.recommendedOrderQuantity} <span class="fs-6 text-muted">units</span>
                </div>
                <div class="small text-muted mt-1">
                    Action: <code>${forecast.recommendedAction}</code>
                </div>
            </div>
        </div>

        <!-- KPI 3: Demand Stability (CV %) & Kurtosis -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-info">
                <div class="text-muted small text-uppercase fw-semibold">Demand Stability</div>
                <div class="d-flex align-items-baseline gap-2 mt-1">
                    <span class="fs-3 fw-bold text-dark">${forecast.coefficientOfVariation}%</span>
                    <span class="badge ${forecast.demandStability == 'STABLE' ? 'bg-success' : (forecast.demandStability == 'MODERATE' ? 'bg-warning text-dark' : 'bg-danger')}">
                        ${forecast.demandStability}
                    </span>
                </div>
                <div class="small text-muted mt-1">
                    Volatility Index: <strong>${forecast.kurtosis}</strong>
                </div>
            </div>
        </div>

        <!-- KPI 4: Stockout Risk Level -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 ${forecast.poissonRiskLevel == 'CRITICAL' ? 'border-danger' : (forecast.poissonRiskLevel == 'ELEVATED' ? 'border-warning' : 'border-success')}">
                <div class="text-muted small text-uppercase fw-semibold">Stockout Risk Level</div>
                <div class="fs-3 fw-bold ${forecast.poissonRiskLevel == 'CRITICAL' ? 'text-danger' : (forecast.poissonRiskLevel == 'ELEVATED' ? 'text-warning' : 'text-success')} mt-1">
                    ${forecast.poissonStockoutRiskPercentage}%
                </div>
                <div class="small text-muted mt-1">
                    Target Service Level: <strong>95%</strong> | Safety Stock: <strong>${forecast.safetyStock}</strong>
                </div>
            </div>
        </div>
    </div>

    <!-- Automated Inventory Advisory Callout Box -->
    <div class="alert ${forecast.alertStatus == 'CRITICAL_STOCKOUT' ? 'alert-danger' : (forecast.alertStatus == 'LOW_STOCK' ? 'alert-warning' : 'alert-primary')} shadow-sm mb-4">
        <h5 class="alert-heading fw-bold d-flex align-items-center">
            <i class="bi bi-shield-check me-2 fs-4"></i> Automated Inventory Advisory
        </h5>
        <p class="mb-2 fs-6">
            <c:choose>
                <c:when test="${forecast.alertStatus == 'CRITICAL_STOCKOUT'}">
                    <strong>Critical Stockout Warning:</strong> Current warehouse inventory (${selectedProduct.currentStock} units) is at or below the safety threshold (${forecast.safetyStock} units). Immediate supplier replenishment recommended to prevent stockout.
                </c:when>
                <c:when test="${forecast.alertStatus == 'LOW_STOCK'}">
                    <strong>Low Stock Advisory:</strong> Current inventory (${selectedProduct.currentStock} units) has crossed the reorder threshold (${forecast.reorderPoint} units). Standard replenishment order recommended.
                </c:when>
                <c:when test="${forecast.alertStatus == 'OVERSTOCK'}">
                    <strong>Overstock Notice:</strong> Current inventory (${selectedProduct.currentStock} units) significantly exceeds normal operating targets. Recommend pausing purchases and reviewing promotional discounts.
                </c:when>
                <c:otherwise>
                    <strong>Optimal Inventory Level:</strong> Current inventory (${selectedProduct.currentStock} units) is comfortably balanced against projected demand (${forecast.chosenTotalDemand} units). No replenishment required at this time.
                </c:otherwise>
            </c:choose>
        </p>
        <div class="row g-3 pt-2 border-top small text-muted">
            <div class="col-sm-4">
                <i class="bi bi-graph-up text-primary me-1"></i> <strong>Forecasted Demand:</strong> ${forecast.chosenTotalDemand} units (${horizonDays} days)
            </div>
            <div class="col-sm-4">
                <i class="bi bi-shield-lock text-success me-1"></i> <strong>Target Safety Stock:</strong> ${forecast.safetyStock} units
            </div>
            <div class="col-sm-4">
                <i class="bi bi-box-seam text-dark me-1"></i> <strong>Current Warehouse Stock:</strong> ${selectedProduct.currentStock} units
            </div>
        </div>
    </div>

    <!-- Row: Chart & Sales Demand Projections -->
    <div class="row g-4 mb-4">
        <!-- Interactive Chart -->
        <div class="col-lg-8">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-graph-up me-1 text-primary"></i> Historical Sales vs. ${horizonDays}-Day Demand Forecast
                    </h5>
                    <span class="badge bg-success-subtle text-success border border-success px-3 py-2">
                        <i class="bi bi-stars me-1"></i> AI Demand Forecast Active
                    </span>
                </div>
                <div class="card-body">
                    <canvas id="forecastChart" style="height: 350px; width: 100%;"></canvas>
                </div>
            </div>
        </div>

        <!-- Sales & Demand Projections Executive Card -->
        <div class="col-lg-4">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-wallet2 me-2 text-primary"></i>Demand & Revenue Projection</h5>
                </div>
                <div class="card-body p-3">
                    <!-- Expected Revenue Callout -->
                    <div class="p-3 mb-3 bg-light rounded border">
                        <div class="text-muted small text-uppercase fw-semibold">Estimated Forecast Revenue</div>
                        <div class="fs-2 fw-bold text-success mt-1">
                            $<fmt:formatNumber value="${forecast.chosenTotalDemand * selectedProduct.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/>
                        </div>
                        <div class="small text-muted mt-1">
                            Based on projected volume of <strong>${forecast.chosenTotalDemand} units</strong> @ $<fmt:formatNumber value="${selectedProduct.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/> each
                        </div>
                    </div>

                    <!-- Business Metrics Breakdown -->
                    <ul class="list-group list-group-flush small">
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <span class="text-muted"><i class="bi bi-calendar3 me-1 text-primary"></i> Forecast Horizon</span>
                            <span class="fw-bold">${horizonDays} Days</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <span class="text-muted"><i class="bi bi-speedometer me-1 text-info"></i> Daily Sales Velocity</span>
                            <span class="fw-bold">${forecast.meanDailyDemand} units / day</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <span class="text-muted"><i class="bi bi-hourglass-split me-1 text-warning"></i> Estimated Stock Cover</span>
                            <span class="fw-bold">
                                <c:choose>
                                    <c:when test="${forecast.meanDailyDemand > 0}">
                                        <fmt:formatNumber value="${selectedProduct.currentStock / forecast.meanDailyDemand}" maxFractionDigits="0"/> days remaining
                                    </c:when>
                                    <c:otherwise>Ample</c:otherwise>
                                </c:choose>
                            </span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <span class="text-muted"><i class="bi bi-patch-check me-1 text-success"></i> Forecast Confidence</span>
                            <span class="badge bg-success">High Confidence (&plusmn;<fmt:formatNumber value="${activeMae}" maxFractionDigits="1"/> units)</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <span class="text-muted"><i class="bi bi-shield-check me-1 text-primary"></i> Recommended Buffer Target</span>
                            <span class="fw-bold text-primary">${forecast.safetyStock} units</span>
                        </li>
                    </ul>

                    <div class="alert alert-light border small mb-0 py-2 mt-3 text-muted">
                        <i class="bi bi-info-circle me-1 text-primary"></i>
                        Projections recalculate automatically with every new sales transaction.
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Optional Academic Benchmarks View for Faculty Evaluation -->
    <div class="text-center mt-3 mb-2">
        <button class="btn btn-sm btn-link text-decoration-none text-muted" type="button" data-bs-toggle="collapse" data-bs-target="#facultyBenchmarkSection">
            <i class="bi bi-mortarboard me-1"></i> Faculty Evaluation View: Machine Learning Benchmarks
        </button>
        <div class="collapse mt-3 text-start" id="facultyBenchmarkSection">
            <div class="card shadow-sm border">
                <div class="card-header bg-light py-2">
                    <h6 class="mb-0 fw-bold"><i class="bi bi-cpu me-1 text-primary"></i> Multi-Model Benchmark (Syllabus Module II & IV)</h6>
                </div>
                <div class="card-body p-3">
                    <div class="table-responsive">
                        <table class="table table-sm table-bordered align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>Algorithm / Model</th>
                                    <th>Projected Volume</th>
                                    <th>Mean Absolute Error (MAE)</th>
                                    <th>Root Mean Squared Error (RMSE)</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>Ordinary Least Squares (OLS) Linear Regression</td>
                                    <td>${forecast.linearRegressionTotal} units</td>
                                    <td>${forecast.linearRegressionMae}</td>
                                    <td>${forecast.linearRegressionRmse}</td>
                                    <td><c:if test="${forecast.recommendedModel == 'LINEAR_REGRESSION'}"><span class="badge bg-success">Selected Winner</span></c:if></td>
                                </tr>
                                <tr>
                                    <td>Ridge Regression (L2 Regularization)</td>
                                    <td>${forecast.ridgeRegressionTotal} units</td>
                                    <td>${forecast.ridgeRegressionMae}</td>
                                    <td>${forecast.ridgeRegressionRmse}</td>
                                    <td><c:if test="${forecast.recommendedModel == 'RIDGE_REGRESSION'}"><span class="badge bg-success">Selected Winner</span></c:if></td>
                                </tr>
                                <tr>
                                    <td>Lasso Regression (L1 Regularization)</td>
                                    <td>${forecast.lassoRegressionTotal} units</td>
                                    <td>${forecast.lassoRegressionMae}</td>
                                    <td>${forecast.lassoRegressionRmse}</td>
                                    <td><c:if test="${forecast.recommendedModel == 'LASSO_REGRESSION'}"><span class="badge bg-success">Selected Winner</span></c:if></td>
                                </tr>
                                <tr>
                                    <td>Random Forest Regressor (Ensemble)</td>
                                    <td>${forecast.randomForestTotal} units</td>
                                    <td>${forecast.randomForestMae}</td>
                                    <td>${forecast.randomForestRmse}</td>
                                    <td><c:if test="${forecast.recommendedModel == 'RANDOM_FOREST'}"><span class="badge bg-success">Selected Winner</span></c:if></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const ctx = document.getElementById('forecastChart').getContext('2d');

    const historicalDates = [
        <c:forEach var="h" items="${history}" varStatus="status">
            <c:if test="${status.index >= (history.size() - 14)}">
                '${h.date}',
            </c:if>
        </c:forEach>
    ];

    const historicalData = [
        <c:forEach var="h" items="${history}" varStatus="status">
            <c:if test="${status.index >= (history.size() - 14)}">
                ${h.quantity},
            </c:if>
        </c:forEach>
    ];

    const forecastDates = [
        <c:forEach var="d" items="${forecast.forecastDates}">
            '${d}',
        </c:forEach>
    ];

    const projectedForecast = [
        <c:forEach var="v" items="${activeDaily}">
            ${v},
        </c:forEach>
    ];

    const allLabels = [...historicalDates, ...forecastDates];
    const alignedHistorical = [...historicalData, ...Array(forecastDates.length).fill(null)];
    const padCount = historicalDates.length - 1;
    const lastHistVal = historicalData[historicalData.length - 1];

    const alignedForecast = [...Array(padCount).fill(null), lastHistVal, ...projectedForecast];

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: allLabels,
            datasets: [
                {
                    label: 'Actual Sales',
                    data: alignedHistorical,
                    borderColor: '#334155',
                    backgroundColor: 'rgba(51, 65, 85, 0.05)',
                    borderWidth: 2,
                    pointRadius: 4,
                    pointBackgroundColor: '#334155',
                    fill: false
                },
                {
                    label: 'Projected Demand',
                    data: alignedForecast,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.1)',
                    borderDash: [5, 5],
                    borderWidth: 2.5,
                    pointRadius: 4,
                    pointBackgroundColor: '#0d6efd',
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        font: { size: 12, weight: '500' }
                    }
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            scales: {
                y: { 
                    beginAtZero: true, 
                    title: { display: true, text: 'Units Sold' },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: { 
                    title: { display: true, text: 'Date' },
                    grid: { display: false }
                }
            }
        }
    });
});
</script>

<jsp:include page="../common/footer.jsp" />
