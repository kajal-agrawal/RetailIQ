<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Administrator Login" />
</jsp:include>

<div class="container d-flex align-items-center justify-content-center" style="min-height: 85vh;">
    <div class="card shadow p-4" style="width: 100%; max-width: 420px;">
        <div class="text-center mb-4">
            <div class="mb-2">
                <i class="bi bi-cpu-fill text-primary" style="font-size: 3rem;"></i>
            </div>
            <h3 class="fw-bold mb-1">Retail<span class="text-primary">IQ</span></h3>
            <p class="text-muted small">Intelligent Retail & Inventory Management</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show small" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-1"></i> ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${param.msg == 'logged_out'}">
            <div class="alert alert-info alert-dismissible fade show small" role="alert">
                <i class="bi bi-info-circle-fill me-1"></i> You have logged out successfully.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth?action=login" method="POST">
            <div class="mb-3">
                <label for="username" class="form-label small fw-semibold">Username</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-person"></i></span>
                    <input type="text" class="form-control" id="username" name="username" 
                           value="${enteredUsername != null ? enteredUsername : 'admin'}" required autofocus>
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label small fw-semibold">Password</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password" 
                           value="admin123" required>
                </div>
            </div>

            <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold">
                <i class="bi bi-box-arrow-in-right me-1"></i> Sign In
            </button>
        </form>

        <div class="mt-4 pt-3 border-top text-center text-muted small">
            Default credentials: <code>admin</code> / <code>admin123</code>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp" />
