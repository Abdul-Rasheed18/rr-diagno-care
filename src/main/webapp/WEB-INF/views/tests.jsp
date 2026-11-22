<c:set var="pageTitle" value="Tests & Packages"/>
<c:set var="activeNav" value="tests"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%-- Where "Add to cart" returns to: the current filtered catalog URL without the context path --%>
<c:url var="backUrlFull" value="/tests">
    <c:if test="${not empty selectedCategory}"><c:param name="category" value="${selectedCategory}"/></c:if>
    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
</c:url>
<c:set var="backUrl" value="${fn:substring(backUrlFull, fn:length(ctx), fn:length(backUrlFull))}"/>

<div class="container mt-4">
    <div class="page-header d-md-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">Tests &amp; Health Packages</h1>
            <p class="text-muted mb-0">Transparent prices, preparation instructions and reference ranges for every test.</p>
        </div>
        <form action="${ctx}/tests" method="get" class="d-flex gap-2 mt-3 mt-md-0" role="search">
            <c:if test="${not empty selectedCategory}"><input type="hidden" name="category" value="${fn:escapeXml(selectedCategory)}"></c:if>
            <input type="search" name="q" value="${fn:escapeXml(q)}" class="form-control" placeholder="Search by name or code" aria-label="Search">
            <button class="btn btn-primary" type="submit"><i class="bi bi-search"></i></button>
        </form>
    </div>

    <ul class="nav nav-pills flex-wrap gap-2 mb-4">
        <c:url var="allUrl" value="/tests"><c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if></c:url>
        <li class="nav-item"><a class="nav-link ${empty selectedCategory ? 'active' : ''}" href="${fn:escapeXml(allUrl)}">All</a></li>
        <c:forEach var="cat" items="${categories}">
            <c:url var="catUrl" value="/tests">
                <c:param name="category" value="${cat}"/>
                <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
            </c:url>
            <li class="nav-item">
                <a class="nav-link ${selectedCategory == cat ? 'active' : ''}" href="${fn:escapeXml(catUrl)}"><c:out value="${cat}"/></a>
            </li>
        </c:forEach>
    </ul>

    <c:if test="${not empty q}">
        <p class="text-muted">Showing results for &ldquo;<strong><c:out value="${q}"/></strong>&rdquo; &mdash;
            ${fn:length(tests)} test(s), ${fn:length(packages)} package(s). <a href="${ctx}/tests">Clear search</a></p>
    </c:if>

    <%-- ===================== Packages ===================== --%>
    <c:if test="${not empty packages}">
        <h2 class="h5 fw-bold mb-3"><i class="bi bi-box2-heart text-teal"></i> Full Body Health Packages</h2>
        <div class="row g-4 mb-5">
            <c:forEach var="p" items="${packages}">
                <c:set var="key" value="PACKAGE-${p.id}"/>
                <div class="col-md-6 col-xl-4">
                    <div class="rr-card rr-card-hover h-100 p-4 d-flex flex-column">
                        <div class="d-flex justify-content-between">
                            <span class="badge bg-teal-soft text-teal mb-2">Full Body Packages</span>
                            <c:if test="${p.discountPercent > 0}"><span class="badge bg-danger align-self-start">Save ${p.discountPercent}%</span></c:if>
                        </div>
                        <h5 class="fw-bold"><c:out value="${p.name}"/></h5>
                        <p class="small text-muted"><c:out value="${p.description}"/></p>
                        <details class="small mb-3">
                            <summary class="text-teal fw-semibold">${p.testCount} tests included</summary>
                            <ul class="mt-2 ps-3 mb-0">
                                <c:forEach var="t" items="${p.tests}">
                                    <li><c:out value="${t.name}"/> <span class="ref-range">(${t.referenceRange})</span></li>
                                </c:forEach>
                            </ul>
                        </details>
                        <c:if test="${not empty p.preparationNotes}">
                            <div class="prep-note mb-3"><i class="bi bi-info-circle"></i> <c:out value="${p.preparationNotes}"/></div>
                        </c:if>
                        <div class="mt-auto d-flex justify-content-between align-items-center">
                            <div>
                                <span class="price">&#8377;<fmt:formatNumber value="${p.price}" pattern="#,##0.00"/></span>
                                <c:if test="${p.savings > 0}"><br><span class="mrp">&#8377;<fmt:formatNumber value="${p.mrp}" pattern="#,##0.00"/></span></c:if>
                            </div>
                            <c:choose>
                                <c:when test="${cartKeys.contains(key)}">
                                    <a href="${ctx}/cart" class="btn btn-outline-primary btn-sm"><i class="bi bi-check2"></i> In cart</a>
                                </c:when>
                                <c:otherwise>
                                    <form action="${ctx}/cart/add" method="post">
                                        ${csrfInput}
                                        <input type="hidden" name="type" value="PACKAGE">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <input type="hidden" name="back" value="${fn:escapeXml(backUrl)}">
                                        <button class="btn btn-primary btn-sm" type="submit"><i class="bi bi-cart-plus"></i> Add</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <%-- ===================== Individual tests ===================== --%>
    <c:if test="${not empty tests}">
        <h2 class="h5 fw-bold mb-3"><i class="bi bi-eyedropper text-teal"></i> Individual Tests</h2>
        <div class="row g-3">
            <c:forEach var="t" items="${tests}">
                <c:set var="key" value="TEST-${t.id}"/>
                <div class="col-md-6 col-xl-4">
                    <div class="rr-card rr-card-hover h-100 p-3 d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-teal-soft text-teal"><c:out value="${t.category}"/></span>
                            <small class="text-muted"><c:out value="${t.code}"/></small>
                        </div>
                        <h6 class="fw-bold mt-2 mb-1"><c:out value="${t.name}"/></h6>
                        <p class="small text-muted mb-2"><c:out value="${t.description}"/></p>
                        <div class="small mb-2">
                            <div><i class="bi bi-rulers text-teal"></i> Normal range: <strong>${t.referenceRange}</strong></div>
                            <div><i class="bi bi-droplet text-teal"></i> Sample: <c:out value="${t.sampleType}"/> &middot; Report in ${t.turnaroundHours} hrs</div>
                        </div>
                        <c:if test="${not empty t.preparationNotes}">
                            <div class="mb-3">
                                <span class="prep-note ${t.fastingRequired ? 'fw-semibold' : ''}"><i class="bi ${t.fastingRequired ? 'bi-alarm' : 'bi-info-circle'}"></i> <c:out value="${t.preparationNotes}"/></span>
                            </div>
                        </c:if>
                        <div class="mt-auto d-flex justify-content-between align-items-center">
                            <span class="price">&#8377;<fmt:formatNumber value="${t.price}" pattern="#,##0.00"/></span>
                            <c:choose>
                                <c:when test="${cartKeys.contains(key)}">
                                    <a href="${ctx}/cart" class="btn btn-outline-primary btn-sm"><i class="bi bi-check2"></i> In cart</a>
                                </c:when>
                                <c:otherwise>
                                    <form action="${ctx}/cart/add" method="post">
                                        ${csrfInput}
                                        <input type="hidden" name="type" value="TEST">
                                        <input type="hidden" name="id" value="${t.id}">
                                        <input type="hidden" name="back" value="${fn:escapeXml(backUrl)}">
                                        <button class="btn btn-primary btn-sm" type="submit"><i class="bi bi-cart-plus"></i> Add</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${empty tests and empty packages}">
        <div class="rr-card p-5 text-center">
            <i class="bi bi-search fs-1 text-muted"></i>
            <h5 class="mt-3">No tests found</h5>
            <p class="text-muted">Try another keyword or browse all categories.</p>
            <a href="${ctx}/tests" class="btn btn-primary">Show all tests</a>
        </div>
    </c:if>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
