<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top shadow-sm">
    <div class="container-fluid px-4">
        <a class="navbar-brand d-flex align-items-center" href="${pageContext.request.contextPath}/dashboard">
            <i class="bi bi-cpu-fill text-primary me-2 fs-4"></i>
            <span>Retail<span class="text-primary">IQ</span></span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#retailNavbar">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="retailNavbar">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/dashboard">
                        <i class="bi bi-speedometer2 me-1"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/products?action=list">
                        <i class="bi bi-box-seam me-1"></i> Products
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/sales?action=pos">
                        <i class="bi bi-cart-check me-1"></i> Point of Sale
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/sales?action=history">
                        <i class="bi bi-receipt me-1"></i> Sales History
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/inventory?action=status">
                        <i class="bi bi-boxes me-1"></i> Inventory & Restock
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/inventory?action=logs">
                        <i class="bi bi-journal-text me-1"></i> Audit Trail
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-warning fw-semibold" href="${pageContext.request.contextPath}/forecast?action=view">
                        <i class="bi bi-cpu me-1"></i> AI Forecasting
                    </a>
                </li>
            </ul>
            <div class="d-flex align-items-center text-light">
                <span class="badge bg-secondary me-3">
                    <i class="bi bi-person-circle me-1"></i>
                    ${sessionScope.fullName != null ? sessionScope.fullName : sessionScope.username} 
                    (${sessionScope.userRole})
                </span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-outline-danger btn-sm">
                    <i class="bi bi-box-arrow-right me-1"></i> Logout
                </a>
            </div>
        </div>
    </div>
</nav>
