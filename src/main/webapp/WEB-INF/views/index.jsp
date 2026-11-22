<c:set var="pageTitle" value="Home"/>
<c:set var="activeNav" value="home"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<section class="rr-hero py-5 mb-5">
    <div class="container py-lg-4">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="badge bg-light text-dark mb-3"><i class="bi bi-patch-check-fill text-teal"></i> Trusted diagnostics in Vijayawada</span>
                <h1 class="display-5 mb-3">Book lab tests online.<br>Get accurate reports, fast.</h1>
                <p class="lead mb-4 opacity-75">RR Diagno Care offers ${testCount}+ pathology tests and ${packageCount} full-body health checkups
                    with walk-in visits or safe home sample collection. Pay at the centre &mdash; no online payment needed.</p>
                <form action="${ctx}/tests" method="get" class="search-box d-flex align-items-center" role="search">
                    <i class="bi bi-search text-muted ms-2"></i>
                    <input type="search" name="q" class="form-control form-control-lg" placeholder="Search tests e.g. Thyroid, HbA1c, Creatinine" aria-label="Search tests">
                    <button class="btn btn-primary btn-lg px-4" type="submit">Search</button>
                </form>
                <div class="mt-3 d-flex flex-wrap gap-2">
                    <c:forEach var="cat" items="${categories}">
                        <c:url var="catUrl" value="/tests"><c:param name="category" value="${cat}"/></c:url>
                        <a class="btn btn-sm btn-outline-light category-chip" href="${fn:escapeXml(catUrl)}"><c:out value="${cat}"/></a>
                    </c:forEach>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="rr-card p-4 text-dark">
                    <h5 class="fw-bold mb-3"><i class="bi bi-clock-history text-teal"></i> Centre Timings</h5>
                    <table class="table table-sm mb-3">
                        <tr><td>Monday &ndash; Saturday</td><td class="text-end fw-semibold">7:00 AM &ndash; 8:00 PM</td></tr>
                        <tr><td>Sunday</td><td class="text-end fw-semibold">7:00 AM &ndash; 1:00 PM</td></tr>
                        <tr><td>Home collection slots</td><td class="text-end fw-semibold">7:00 AM &ndash; 6:00 PM</td></tr>
                    </table>
                    <div class="d-flex gap-3 small text-muted">
                        <span><i class="bi bi-droplet-half text-teal"></i> Fasting tests: come before 10 AM</span>
                    </div>
                    <a href="${ctx}/tests" class="btn btn-primary w-100 mt-3"><i class="bi bi-calendar2-plus"></i> Book an appointment</a>
                </div>
            </div>
        </div>
    </div>
</section>

<div class="container">
    <div class="row g-3 mb-5 text-center">
        <div class="col-6 col-md-3"><div class="rr-card p-3 h-100"><i class="bi bi-house-heart fs-2 text-teal"></i><div class="fw-semibold mt-1">Home Sample Collection</div><small class="text-muted">Trained phlebotomists</small></div></div>
        <div class="col-6 col-md-3"><div class="rr-card p-3 h-100"><i class="bi bi-file-earmark-medical fs-2 text-teal"></i><div class="fw-semibold mt-1">Digital Reports</div><small class="text-muted">View &amp; print online</small></div></div>
        <div class="col-6 col-md-3"><div class="rr-card p-3 h-100"><i class="bi bi-cash-coin fs-2 text-teal"></i><div class="fw-semibold mt-1">Pay at Centre</div><small class="text-muted">No online payment</small></div></div>
        <div class="col-6 col-md-3"><div class="rr-card p-3 h-100"><i class="bi bi-shield-check fs-2 text-teal"></i><div class="fw-semibold mt-1">Quality Assured</div><small class="text-muted">Automated analysers</small></div></div>
    </div>

    <div class="d-flex justify-content-between align-items-end mb-3">
        <h2 class="section-title h3 mb-0">Featured Health Checkup Packages</h2>
        <a href="${ctx}/tests?category=Full%20Body%20Packages" class="small">View all <i class="bi bi-arrow-right"></i></a>
    </div>
    <div class="row g-4 mb-4">
        <c:forEach var="p" items="${featuredPackages}">
            <div class="col-md-6 col-lg-4">
                <div class="rr-card rr-card-hover h-100 p-4 d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="fw-bold mb-0"><c:out value="${p.name}"/></h5>
                        <c:if test="${p.discountPercent > 0}"><span class="badge bg-danger">${p.discountPercent}% OFF</span></c:if>
                    </div>
                    <p class="text-muted small"><c:out value="${p.description}"/></p>
                    <p class="small mb-2"><i class="bi bi-list-check text-teal"></i> <strong>${p.testCount} tests included:</strong></p>
                    <ul class="small text-muted ps-3 mb-3">
                        <c:forEach var="t" items="${p.tests}" end="3"><li><c:out value="${t.name}"/></li></c:forEach>
                        <c:if test="${p.testCount > 4}"><li>+ ${p.testCount - 4} more</li></c:if>
                    </ul>
                    <c:if test="${not empty p.preparationNotes}"><div class="prep-note mb-3"><i class="bi bi-info-circle"></i> <c:out value="${p.preparationNotes}"/></div></c:if>
                    <div class="mt-auto d-flex justify-content-between align-items-center">
                        <div>
                            <span class="price">&#8377;<fmt:formatNumber value="${p.price}" pattern="#,##0"/></span>
                            <c:if test="${p.savings > 0}"><span class="mrp ms-1">&#8377;<fmt:formatNumber value="${p.mrp}" pattern="#,##0"/></span></c:if>
                        </div>
                        <form action="${ctx}/cart/add" method="post">
                            ${csrfInput}
                            <input type="hidden" name="type" value="PACKAGE">
                            <input type="hidden" name="id" value="${p.id}">
                            <input type="hidden" name="back" value="/">
                            <button class="btn btn-primary btn-sm" type="submit"><i class="bi bi-cart-plus"></i> Add to cart</button>
                        </form>
                    </div>
                </div>
            </div>
        </c:forEach>
        <c:if test="${empty featuredPackages}">
            <div class="col-12"><div class="alert alert-info">Health packages will be available soon.</div></div>
        </c:if>
    </div>

    <div class="rr-card bg-teal-soft p-4 d-md-flex justify-content-between align-items-center">
        <div>
            <h5 class="fw-bold mb-1">How it works</h5>
            <p class="mb-0 text-muted">1. Add tests to cart &nbsp;&rarr;&nbsp; 2. Pick a date &amp; slot &nbsp;&rarr;&nbsp; 3. Give sample &nbsp;&rarr;&nbsp; 4. Download your report</p>
        </div>
        <a href="${ctx}/tests" class="btn btn-primary mt-3 mt-md-0">Browse all tests</a>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
