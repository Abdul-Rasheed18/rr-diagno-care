<c:set var="pageTitle" value="Your Cart"/>
<c:set var="activeNav" value="cart"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container mt-4">
    <div class="page-header d-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">Your Cart</h1>
            <p class="text-muted mb-0">${fn:length(cartItems)} item(s) selected</p>
        </div>
        <a href="${ctx}/tests" class="btn btn-outline-primary btn-sm"><i class="bi bi-plus-lg"></i> Add more tests</a>
    </div>

    <c:choose>
        <c:when test="${empty cartItems}">
            <div class="rr-card p-5 text-center">
                <i class="bi bi-cart-x fs-1 text-muted"></i>
                <h5 class="mt-3">Your cart is empty</h5>
                <p class="text-muted">Browse our tests and health packages to get started.</p>
                <a href="${ctx}/tests" class="btn btn-primary">Browse tests</a>
            </div>
        </c:when>
        <c:otherwise>
            <c:if test="${not empty overlaps}">
                <div class="alert alert-warning">
                    <i class="bi bi-exclamation-circle"></i>
                    These tests are already included in a package in your cart:
                    <strong><c:forEach var="o" items="${overlaps}" varStatus="s"><c:out value="${o}"/>${s.last ? '' : ', '}</c:forEach></strong>.
                    You may remove them to avoid paying twice.
                </div>
            </c:if>
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="rr-card">
                        <table class="table table-rr mb-0">
                            <thead><tr><th>Item</th><th>Category</th><th class="text-end">Price</th><th></th></tr></thead>
                            <tbody>
                            <c:forEach var="item" items="${cartItems}">
                                <tr>
                                    <td>
                                        <div class="fw-semibold">
                                            <c:if test="${item['package']}"><i class="bi bi-box2-heart text-teal"></i></c:if>
                                            <c:out value="${item.name}"/>
                                        </div>
                                        <c:if test="${item['package']}"><small class="text-muted">${item.testCount} tests included</small><br></c:if>
                                        <c:if test="${not empty item.preparationNotes}"><span class="prep-note mt-1"><c:out value="${item.preparationNotes}"/></span></c:if>
                                    </td>
                                    <td><span class="badge bg-teal-soft text-teal"><c:out value="${item.category}"/></span></td>
                                    <td class="text-end fw-semibold">&#8377;<fmt:formatNumber value="${item.price}" pattern="#,##0.00"/></td>
                                    <td class="text-end">
                                        <form action="${ctx}/cart/remove" method="post">
                                            ${csrfInput}
                                            <input type="hidden" name="key" value="${fn:escapeXml(item.key)}">
                                            <button class="btn btn-sm btn-link text-danger" type="submit" title="Remove"><i class="bi bi-trash"></i></button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <form action="${ctx}/cart/clear" method="post" class="mt-2">
                        ${csrfInput}
                        <button class="btn btn-sm btn-outline-secondary" type="submit"><i class="bi bi-x-circle"></i> Clear cart</button>
                    </form>
                </div>
                <div class="col-lg-4">
                    <div class="rr-card p-4">
                        <h5 class="fw-bold mb-3">Order summary</h5>
                        <div class="d-flex justify-content-between mb-2"><span>Items</span><span>${fn:length(cartItems)}</span></div>
                        <div class="d-flex justify-content-between mb-2"><span>Home collection</span><span class="text-success">FREE</span></div>
                        <hr>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <span class="fw-bold">Total payable</span>
                            <span class="price">&#8377;<fmt:formatNumber value="${cartTotal}" pattern="#,##0.00"/></span>
                        </div>
                        <p class="small text-muted"><i class="bi bi-cash-coin"></i> No online payment &mdash; pay at the centre or to our phlebotomist during home collection.</p>
                        <a href="${ctx}/patient/checkout" class="btn btn-primary w-100 btn-lg">Proceed to checkout <i class="bi bi-arrow-right"></i></a>
                        <c:if test="${not loggedIn}"><p class="small text-muted text-center mt-2 mb-0">You will be asked to log in or register.</p></c:if>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
