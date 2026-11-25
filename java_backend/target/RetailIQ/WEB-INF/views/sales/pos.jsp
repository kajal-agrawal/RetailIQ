<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:include page="../common/header.jsp">
    <jsp:param name="title" value="RetailIQ - Point of Sale (POS)" />
</jsp:include>
<jsp:include page="../common/navbar.jsp" />

<div class="container-fluid px-4 py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h2 class="fw-bold mb-1">Point of Sale (POS) Counter</h2>
            <p class="text-muted small mb-0">Select products, build customer cart, and execute atomic transactions</p>
        </div>
    </div>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="row g-4">
        <!-- Left: Product Catalog Selection -->
        <div class="col-lg-7">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3">
                    <div class="row g-2 align-items-center">
                        <div class="col">
                            <h5 class="mb-0 fw-bold">Available Products</h5>
                        </div>
                        <div class="col-auto">
                            <input type="text" id="productSearch" class="form-control form-control-sm" 
                                   placeholder="Search product or SKU..." onkeyup="filterProducts()">
                        </div>
                    </div>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive" style="max-height: 520px; overflow-y: auto;">
                        <table class="table table-hover align-middle mb-0" id="productCatalogTable">
                            <thead class="table-light sticky-top">
                                <tr class="small text-uppercase text-muted">
                                    <th class="ps-3">SKU</th>
                                    <th>Product Name</th>
                                    <th>Price</th>
                                    <th>Available</th>
                                    <th class="text-end pe-3">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${products}">
                                    <tr class="product-row" data-name="${p.productName.toLowerCase()}" data-sku="${p.sku.toLowerCase()}">
                                        <td class="ps-3"><code>${p.sku}</code></td>
                                        <td class="fw-semibold">${p.productName}</td>
                                        <td>$<fmt:formatNumber value="${p.unitPrice}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                        <td>
                                            <span class="badge ${p.currentStock > p.safetyStock ? 'bg-success' : (p.currentStock > 0 ? 'bg-warning text-dark' : 'bg-danger')}">
                                                ${p.currentStock} in stock
                                            </span>
                                        </td>
                                        <td class="text-end pe-3">
                                            <button type="button" class="btn btn-sm btn-outline-primary" 
                                                    data-id="${p.productId}"
                                                    data-name="<c:out value='${p.productName}' />"
                                                    data-price="${p.unitPrice}"
                                                    data-stock="${p.currentStock}"
                                                    onclick="addToCartFromBtn(this)"
                                                    ${p.currentStock <= 0 ? 'disabled' : ''}>
                                                <i class="bi bi-cart-plus me-1"></i> Add
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Current Cart & Checkout -->
        <div class="col-lg-5">
            <div class="card shadow-sm h-100">
                <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                    <h5 class="mb-0 fw-bold"><i class="bi bi-cart3 me-1 text-primary"></i> Customer Cart</h5>
                    <button type="button" class="btn btn-outline-danger btn-sm" onclick="clearCart()">
                        <i class="bi bi-trash"></i> Clear
                    </button>
                </div>
                <div class="card-body p-0">
                    <form id="checkoutForm" action="${pageContext.request.contextPath}/sales?action=checkout" method="POST">
                        <div class="table-responsive" style="min-height: 280px; max-height: 340px; overflow-y: auto;">
                            <table class="table table-sm align-middle mb-0" id="cartTable">
                                <thead class="table-light">
                                    <tr class="small text-muted">
                                        <th class="ps-3">Item</th>
                                        <th style="width: 110px;" class="text-center">Qty</th>
                                        <th class="text-end">Subtotal</th>
                                        <th class="text-center" style="width: 45px;"></th>
                                    </tr>
                                </thead>
                                <tbody id="cartBody">
                                    <tr id="emptyCartRow">
                                        <td colspan="4" class="text-center py-5 text-muted">
                                            <i class="bi bi-basket3 fs-2 d-block mb-2"></i>
                                            Cart is empty. Add products to begin checkout.
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Checkout Summary Footer -->
                        <div class="p-3 bg-light border-top">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-muted">Total Items:</span>
                                <span class="fw-bold" id="totalItemsCount">0</span>
                            </div>
                            <div class="d-flex justify-content-between mb-3">
                                <span class="fs-5 fw-bold">Grand Total:</span>
                                <span class="fs-4 fw-bold text-primary" id="grandTotalDisplay">$0.00</span>
                            </div>

                            <div class="mb-3">
                                <label for="paymentMethod" class="form-label small fw-semibold">Payment Mode</label>
                                <select class="form-select" id="paymentMethod" name="paymentMethod" required>
                                    <option value="CARD" selected>Credit / Debit Card</option>
                                    <option value="UPI">UPI / Digital Wallet</option>
                                    <option value="CASH">Cash</option>
                                </select>
                            </div>

                            <button type="submit" id="checkoutBtn" class="btn btn-success w-100 py-2 fw-semibold" disabled>
                                <i class="bi bi-check-circle-fill me-1"></i> Complete Checkout
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
let cart = {};

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function addToCartFromBtn(btn) {
    const id = parseInt(btn.getAttribute('data-id'));
    const name = btn.getAttribute('data-name');
    const price = parseFloat(btn.getAttribute('data-price'));
    const stock = parseInt(btn.getAttribute('data-stock'));
    addToCart(id, name, price, stock);
}

function addToCart(productId, name, price, maxStock) {
    productId = parseInt(productId);
    price = parseFloat(price);
    maxStock = parseInt(maxStock);
    
    if (cart[productId]) {
        if (cart[productId].qty + 1 > maxStock) {
            alert("Cannot add more. Reached maximum available stock (" + maxStock + ").");
            return;
        }
        cart[productId].qty += 1;
    } else {
        cart[productId] = { id: productId, name: name, price: price, qty: 1, maxStock: maxStock };
    }
    renderCart();
}

function adjustQty(productId, delta) {
    productId = parseInt(productId);
    if (cart[productId]) {
        updateQty(productId, cart[productId].qty + delta);
    }
}

function updateQty(productId, newQty) {
    productId = parseInt(productId);
    newQty = parseInt(newQty);
    if (isNaN(newQty) || newQty <= 0) {
        removeFromCart(productId);
        return;
    }
    if (newQty > cart[productId].maxStock) {
        alert("Maximum available stock is " + cart[productId].maxStock);
        cart[productId].qty = cart[productId].maxStock;
    } else {
        cart[productId].qty = newQty;
    }
    renderCart();
}

function removeFromCart(productId) {
    productId = parseInt(productId);
    delete cart[productId];
    renderCart();
}

function clearCart() {
    cart = {};
    renderCart();
}

function renderCart() {
    const tbody = document.getElementById("cartBody");
    const checkoutBtn = document.getElementById("checkoutBtn");
    const itemsCountEl = document.getElementById("totalItemsCount");
    const grandTotalEl = document.getElementById("grandTotalDisplay");

    const keys = Object.keys(cart);
    if (keys.length === 0) {
        tbody.innerHTML = 
            '<tr id="emptyCartRow">' +
                '<td colspan="4" class="text-center py-5 text-muted">' +
                    '<i class="bi bi-basket3 fs-2 d-block mb-2"></i>' +
                    'Cart is empty. Add products to begin checkout.' +
                '</td>' +
            '</tr>';
        itemsCountEl.innerText = "0";
        grandTotalEl.innerText = "$0.00";
        checkoutBtn.disabled = true;
        return;
    }

    let html = "";
    let totalItems = 0;
    let grandTotal = 0;

    keys.forEach(function(k) {
        const item = cart[k];
        const subtotal = item.qty * item.price;
        totalItems += item.qty;
        grandTotal += subtotal;

        html += '<tr>' +
            '<td class="ps-3">' +
                '<div class="fw-semibold text-truncate" style="max-width: 170px;">' + escapeHtml(item.name) + '</div>' +
                '<small class="text-muted">$' + item.price.toFixed(2) + ' each</small>' +
                '<input type="hidden" name="productId" value="' + item.id + '">' +
            '</td>' +
            '<td class="text-center">' +
                '<div class="input-group input-group-sm justify-content-center" style="width: 110px; margin: 0 auto;">' +
                    '<button class="btn btn-outline-secondary px-2" type="button" onclick="adjustQty(' + item.id + ', -1)">-</button>' +
                    '<input type="number" min="1" max="' + item.maxStock + '" name="quantity" ' +
                           'class="form-control form-control-sm text-center px-1" ' +
                           'value="' + item.qty + '" ' +
                           'oninput="updateQty(' + item.id + ', this.value)" ' +
                           'onchange="updateQty(' + item.id + ', this.value)">' +
                    '<button class="btn btn-outline-secondary px-2" type="button" onclick="adjustQty(' + item.id + ', 1)">+</button>' +
                '</div>' +
            '</td>' +
            '<td class="text-end fw-semibold">$' + subtotal.toFixed(2) + '</td>' +
            '<td class="text-center">' +
                '<button type="button" class="btn btn-link text-danger p-0" onclick="removeFromCart(' + item.id + ')">' +
                    '<i class="bi bi-x-circle"></i>' +
                '</button>' +
            '</td>' +
        '</tr>';
    });

    tbody.innerHTML = html;
    itemsCountEl.innerText = totalItems;
    grandTotalEl.innerText = "$" + grandTotal.toFixed(2);
    checkoutBtn.disabled = false;
}

function filterProducts() {
    const query = document.getElementById("productSearch").value.toLowerCase();
    const rows = document.querySelectorAll(".product-row");
    rows.forEach(r => {
        const name = r.getAttribute("data-name");
        const sku = r.getAttribute("data-sku");
        if (name.includes(query) || sku.includes(query)) {
            r.style.display = "";
        } else {
            r.style.display = "none";
        }
    });
}
</script>

<jsp:include page="../common/footer.jsp" />
