<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Redirect to dashboard (which redirects to login if unauthenticated)
    response.sendRedirect(request.getContextPath() + "/dashboard");
%>
