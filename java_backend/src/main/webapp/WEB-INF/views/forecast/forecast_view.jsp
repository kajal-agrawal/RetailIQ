<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - AI Sales Forecasting & Inventory Intelligence" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

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
        <p class="mb-2 fs-6">${forecast.ruleExplanation}</p>
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

    <!-- Row: Chart & Model Comparison -->
    <div class="row g-4 mb-4">
        <!-- Interactive Chart -->
        <div class="col-lg-8">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-graph-up me-1 text-primary"></i> Historical Sales vs. ${horizonDays}-Day Forecast
                    </h5>
                    <span class="badge bg-light text-dark border">
                        Optimal Model: <strong>${forecast.recommendedModel}</strong>
                    </span>
                </div>
                <div class="card-body">
                    <canvas id="forecastChart" style="height: 350px; width: 100%;"></canvas>
                </div>
            </div>
        </div>

        <!-- Comparative Model Benchmark Grid -->
        <div class="col-lg-4">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-speedometer2 me-1 text-primary"></i> Forecast Model Comparison</h5>
                </div>
                <div class="card-body p-3">
                    <!-- Model 1: Linear Regression -->
                    <div class="border rounded p-2 mb-2 bg-light">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="text-primary small">Linear Trend Model</strong>
                            <c:if test="${forecast.recommendedModel == 'LINEAR_REGRESSION'}">
                                <span class="badge bg-success small"><i class="bi bi-trophy-fill me-1"></i> Best</span>
                            </c:if>
                        </div>
                        <div class="small text-muted">
                            Total: <strong>${forecast.linearRegressionTotal}u</strong> | MAE: <strong>${forecast.linearRegressionMae}</strong> | RMSE: <strong>${forecast.linearRegressionRmse}</strong>
                        </div>
                    </div>

                    <!-- Model 2: Ridge Regression -->
                    <div class="border rounded p-2 mb-2 bg-light">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="text-info small">Ridge Regularized Model</strong>
                            <c:if test="${forecast.recommendedModel == 'RIDGE_REGRESSION'}">
                                <span class="badge bg-success small"><i class="bi bi-trophy-fill me-1"></i> Best</span>
                            </c:if>
                        </div>
                        <div class="small text-muted">
                            Total: <strong>${forecast.ridgeRegressionTotal}u</strong> | MAE: <strong>${forecast.ridgeRegressionMae}</strong> | RMSE: <strong>${forecast.ridgeRegressionRmse}</strong>
                        </div>
                    </div>

                    <!-- Model 3: Lasso Regression -->
                    <div class="border rounded p-2 mb-2 bg-light">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="text-warning text-dark small">Lasso Regularized Model</strong>
                            <c:if test="${forecast.recommendedModel == 'LASSO_REGRESSION'}">
                                <span class="badge bg-success small"><i class="bi bi-trophy-fill me-1"></i> Best</span>
                            </c:if>
                        </div>
                        <div class="small text-muted">
                            Total: <strong>${forecast.lassoRegressionTotal}u</strong> | MAE: <strong>${forecast.lassoRegressionMae}</strong> | RMSE: <strong>${forecast.lassoRegressionRmse}</strong>
                        </div>
                    </div>

                    <!-- Model 4: Random Forest -->
                    <div class="border rounded p-2 mb-2 bg-light">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="text-success small">Random Forest Ensemble</strong>
                            <c:if test="${forecast.recommendedModel == 'RANDOM_FOREST'}">
                                <span class="badge bg-success small"><i class="bi bi-trophy-fill me-1"></i> Best</span>
                            </c:if>
                        </div>
                        <div class="small text-muted">
                            Total: <strong>${forecast.randomForestTotal}u</strong> | MAE: <strong>${forecast.randomForestMae}</strong> | RMSE: <strong>${forecast.randomForestRmse}</strong>
                        </div>
                    </div>

                    <div class="alert alert-secondary small mb-0 py-2 mt-3">
                        <i class="bi bi-check-circle-fill text-success me-1"></i>
                        The system automatically selects the forecast model with the lowest prediction error (MAE) for this product.
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

    const lrForecast = [
        <c:forEach var="v" items="${forecast.linearRegressionDaily}">
            ${v},
        </c:forEach>
    ];

    const ridgeForecast = [
        <c:forEach var="v" items="${forecast.ridgeRegressionDaily}">
            ${v},
        </c:forEach>
    ];

    const rfForecast = [
        <c:forEach var="v" items="${forecast.randomForestDaily}">
            ${v},
        </c:forEach>
    ];

    const allLabels = [...historicalDates, ...forecastDates];
    const alignedHistorical = [...historicalData, ...Array(forecastDates.length).fill(null)];
    const padCount = historicalDates.length - 1;
    const lastHistVal = historicalData[historicalData.length - 1];

    const alignedLR = [...Array(padCount).fill(null), lastHistVal, ...lrForecast];
    const alignedRidge = [...Array(padCount).fill(null), lastHistVal, ...ridgeForecast];
    const alignedRF = [...Array(padCount).fill(null), lastHistVal, ...rfForecast];

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: allLabels,
            datasets: [
                {
                    label: 'Actual Sales',
                    data: alignedHistorical,
                    borderColor: '#334155',
                    borderWidth: 2,
                    pointRadius: 3,
                    fill: false
                },
                {
                    label: 'Linear Regression (OLS)',
                    data: alignedLR,
                    borderColor: '#0d6efd',
                    borderDash: [5, 5],
                    borderWidth: 2,
                    pointRadius: 3
                },
                {
                    label: 'Ridge Regression (L2)',
                    data: alignedRidge,
                    borderColor: '#0dcaf0',
                    borderDash: [2, 2],
                    borderWidth: 2,
                    pointRadius: 3
                },
                {
                    label: 'Random Forest (Ensemble)',
                    data: alignedRF,
                    borderColor: '#198754',
                    borderDash: [3, 3],
                    borderWidth: 2,
                    pointRadius: 3
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, title: { display: true, text: 'Units' } },
                x: { title: { display: true, text: 'Date' } }
            }
        }
    });
});
</script>

<jsp:include page="../common/footer.jsp" />
