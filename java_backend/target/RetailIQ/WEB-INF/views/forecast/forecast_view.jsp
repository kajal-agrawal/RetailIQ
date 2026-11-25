<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - AI Sales Forecasting & Inventory Intelligence" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<!-- Load Chart.js CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>

<div class="container-fluid px-4 py-4">
    <!-- Header & Product Switcher -->
    <div class="row align-items-center mb-4 g-3">
        <div class="col-md-6">
            <h2 class="fw-bold mb-1">
                <i class="bi bi-cpu text-primary me-2"></i>AI Sales Forecasting & Inventory Rules
            </h2>
            <p class="text-muted small mb-0">Multi-Model Machine Learning (Linear Regression & Random Forest) + IF-THEN AI Rules</p>
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
                        <option value="7" ${horizonDays == 7 ? 'selected' : ''}>7-Day Forecast</option>
                        <option value="14" ${horizonDays == 14 ? 'selected' : ''}>14-Day Forecast</option>
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

        <!-- KPI 3: Dynamic Safety Stock -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-secondary">
                <div class="text-muted small text-uppercase fw-semibold">Safety Stock (95% Service Level)</div>
                <div class="fs-3 fw-bold text-dark mt-1">
                    ${forecast.safetyStock} <span class="fs-6 text-muted">units</span>
                </div>
                <div class="small text-muted mt-1">
                    Reorder Point (ROP): <strong>${forecast.reorderPoint}</strong> units
                </div>
            </div>
        </div>

        <!-- KPI 4: Demand Parameters -->
        <div class="col-md-3">
            <div class="card p-3 shadow-sm h-100 border-start border-4 border-info">
                <div class="text-muted small text-uppercase fw-semibold">Historical Daily Demand</div>
                <div class="fs-3 fw-bold text-dark mt-1">
                    ${forecast.meanDailyDemand} <span class="fs-6 text-muted">&mu; units/day</span>
                </div>
                <div class="small text-muted mt-1">
                    Std Dev (&sigma;): <strong>${forecast.demandStdDev}</strong> | Lead Time: <strong>${selectedProduct.leadTimeDays}d</strong>
                </div>
            </div>
        </div>
    </div>

    <!-- AI Rule Explanation Callout Box -->
    <div class="alert ${forecast.alertStatus == 'CRITICAL_STOCKOUT' ? 'alert-danger' : (forecast.alertStatus == 'LOW_STOCK' ? 'alert-warning' : 'alert-primary')} shadow-sm mb-4">
        <h5 class="alert-heading fw-bold d-flex align-items-center">
            <i class="bi bi-robot me-2 fs-4"></i> AI Recommendation Engine (IF-THEN Expert Rules)
        </h5>
        <p class="mb-1">${forecast.ruleExplanation}</p>
        <hr class="my-2">
        <small class="text-muted">
            Formula applied: <code>Recommended_Qty = (Projected_Demand [${forecast.chosenTotalDemand} units] + Safety_Stock [${forecast.safetyStock} units]) - Current_Stock [${selectedProduct.currentStock} units]</code>
        </small>
    </div>

    <!-- Row: Interactive Chart & Model Comparison -->
    <div class="row g-4">
        <!-- Interactive Demand & Forecast Chart -->
        <div class="col-lg-8">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold">
                        <i class="bi bi-graph-up me-1 text-primary"></i> Historical Sales vs. ${horizonDays}-Day ML Forecast
                    </h5>
                    <span class="badge bg-light text-dark border">
                        Preferred Model: <strong>${forecast.recommendedModel}</strong>
                    </span>
                </div>
                <div class="card-body">
                    <canvas id="forecastChart" style="height: 340px; width: 100%;"></canvas>
                </div>
            </div>
        </div>

        <!-- Model Comparison Cards -->
        <div class="col-lg-4">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <h5 class="mb-0 fw-bold">Model Comparison & Benchmark</h5>
                </div>
                <div class="card-body p-3">
                    <!-- Model 1: Linear Regression -->
                    <div class="border rounded p-3 mb-3 bg-light position-relative">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h6 class="fw-bold mb-0 text-primary">Linear Regression</h6>
                            <c:if test="${forecast.recommendedModel == 'LINEAR_REGRESSION'}">
                                <span class="badge bg-success"><i class="bi bi-trophy-fill me-1"></i> Best Fit</span>
                            </c:if>
                        </div>
                        <div class="small">
                            <div>Total Projected: <strong>${forecast.linearRegressionTotal} units</strong></div>
                            <div>MAE (Mean Absolute Error): <strong>${forecast.linearRegressionMae}</strong></div>
                            <div>RMSE (Root Mean Sq Error): <strong>${forecast.linearRegressionRmse}</strong></div>
                            <div>R&sup2; Score: <strong>${forecast.linearRegressionR2}</strong></div>
                        </div>
                    </div>

                    <!-- Model 2: Random Forest -->
                    <div class="border rounded p-3 mb-3 bg-light position-relative">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h6 class="fw-bold mb-0 text-success">Random Forest Regressor</h6>
                            <c:if test="${forecast.recommendedModel == 'RANDOM_FOREST'}">
                                <span class="badge bg-success"><i class="bi bi-trophy-fill me-1"></i> Best Fit</span>
                            </c:if>
                        </div>
                        <div class="small">
                            <div>Total Projected: <strong>${forecast.randomForestTotal} units</strong></div>
                            <div>MAE (Mean Absolute Error): <strong>${forecast.randomForestMae}</strong></div>
                            <div>RMSE (Root Mean Sq Error): <strong>${forecast.randomForestRmse}</strong></div>
                            <div>R&sup2; Score: <strong>${forecast.randomForestR2}</strong></div>
                        </div>
                    </div>

                    <div class="alert alert-secondary small mb-0 py-2">
                        <i class="bi bi-info-circle me-1"></i>
                        The AI engine chooses the model with the lowest test <strong>MAE (Mean Absolute Error)</strong> to minimize over-prediction risks.
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Chart.js Data Injection and Script -->
<script>
document.addEventListener("DOMContentLoaded", function() {
    const ctx = document.getElementById('forecastChart').getContext('2d');

    // Historical past 14 days dates and quantities
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

    // Forecast dates and values
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

    const rfForecast = [
        <c:forEach var="v" items="${forecast.randomForestDaily}">
            ${v},
        </c:forEach>
    ];

    // Combine labels
    const allLabels = [...historicalDates, ...forecastDates];

    // Align historical line (nulls for future dates)
    const alignedHistorical = [...historicalData, ...Array(forecastDates.length).fill(null)];

    // Align forecast lines (nulls for past dates, start from last historical point for continuous curve)
    const padCount = historicalDates.length - 1;
    const lastHistVal = historicalData[historicalData.length - 1];

    const alignedLR = [...Array(padCount).fill(null), lastHistVal, ...lrForecast];
    const alignedRF = [...Array(padCount).fill(null), lastHistVal, ...rfForecast];

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: allLabels,
            datasets: [
                {
                    label: 'Actual Historical Sales',
                    data: alignedHistorical,
                    borderColor: '#334155',
                    backgroundColor: 'rgba(51, 65, 85, 0.1)',
                    borderWidth: 2,
                    pointRadius: 4,
                    fill: false,
                    tension: 0.2
                },
                {
                    label: 'Linear Regression Forecast',
                    data: alignedLR,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13, 110, 253, 0.05)',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    pointRadius: 4,
                    tension: 0.2
                },
                {
                    label: 'Random Forest Forecast',
                    data: alignedRF,
                    borderColor: '#198754',
                    backgroundColor: 'rgba(25, 135, 84, 0.05)',
                    borderWidth: 2,
                    borderDash: [3, 3],
                    pointRadius: 4,
                    tension: 0.2
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false,
            },
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            if (context.parsed.y !== null) {
                                return context.dataset.label + ': ' + context.parsed.y + ' units';
                            }
                            return null;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Units Sold / Predicted'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Date Timeline'
                    }
                }
            }
        }
    });
});
</script>

<jsp:include page="../common/footer.jsp" />
