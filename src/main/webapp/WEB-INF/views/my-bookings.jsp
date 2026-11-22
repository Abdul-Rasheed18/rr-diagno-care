<c:set var="pageTitle" value="My Bookings"/>
<c:set var="activeNav" value="bookings"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<c:set var="stepNames" value="${fn:split('Booked,Sample Collected,Processing,Report Ready', ',')}"/>

<div class="container mt-4">
    <div class="page-header d-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">My Bookings &amp; Reports</h1>
            <p class="text-muted mb-0">Track your appointments live and download completed reports.</p>
        </div>
        <a href="${ctx}/tests" class="btn btn-primary btn-sm"><i class="bi bi-plus-lg"></i> New booking</a>
    </div>

    <c:if test="${empty bookings}">
        <div class="rr-card p-5 text-center">
            <i class="bi bi-calendar2-x fs-1 text-muted"></i>
            <h5 class="mt-3">No bookings yet</h5>
            <p class="text-muted">Book your first test or health checkup in under a minute.</p>
            <a href="${ctx}/tests" class="btn btn-primary">Browse tests</a>
        </div>
    </c:if>

    <div class="row g-4">
        <c:forEach var="b" items="${bookings}">
            <div class="col-lg-6">
                <div class="rr-card p-4 h-100 d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <div class="small text-muted">Booking ref</div>
                            <div class="fw-bold"><c:out value="${b.bookingRef}"/></div>
                        </div>
                        <span class="badge bg-${b.statusBadge} fs-6"><c:out value="${b.statusLabel}"/></span>
                    </div>

                    <c:choose>
                        <c:when test="${b.status == 'CANCELLED'}">
                            <div class="alert alert-danger py-2 small mb-2"><i class="bi bi-x-octagon"></i> This booking was cancelled.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="tracker">
                                <c:forEach var="label" items="${stepNames}" varStatus="s">
                                    <div class="step ${b.progressStep >= s.count ? 'done' : ''} ${b.progressStep == s.count ? 'current' : ''}">
                                        <div class="dot"><c:if test="${b.progressStep >= s.count}"><i class="bi bi-check-lg"></i></c:if></div>
                                        ${label}
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <table class="print-meta my-2">
                        <tr><td><i class="bi bi-calendar3"></i> Date</td><td class="fw-semibold"><c:out value="${b.appointmentDateText}"/>, <c:out value="${b.timeSlot}"/></td></tr>
                        <tr><td><i class="bi bi-geo-alt"></i> Visit</td><td><c:out value="${b.visitModeLabel}"/></td></tr>
                        <tr><td><i class="bi bi-list-check"></i> Items</td><td><c:out value="${b.itemSummary}"/> <span class="text-muted">(${b.testCount} tests)</span></td></tr>
                        <tr><td><i class="bi bi-currency-rupee"></i> Amount</td>
                            <td><strong>&#8377;<fmt:formatNumber value="${b.totalAmount}" pattern="#,##0.00"/></strong>
                                &middot; <c:out value="${b.paymentModeLabel}"/>
                                <span class="badge ${b.paid ? 'bg-success' : 'bg-warning text-dark'}">${b.paymentStatus}</span></td></tr>
                    </table>

                    <div class="mt-auto d-flex flex-wrap gap-2 pt-2">
                        <a href="${ctx}/patient/bookings/${b.id}" class="btn btn-sm ${b.completed ? 'btn-primary' : 'btn-outline-primary'}">
                            <i class="bi ${b.completed ? 'bi-file-earmark-medical' : 'bi-eye'}"></i> ${b.completed ? 'View report' : 'View details'}
                        </a>
                        <c:if test="${b.completed}">
                            <a href="${ctx}/print/report?id=${b.id}" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-printer"></i> Print report</a>
                        </c:if>
                        <a href="${ctx}/print/receipt?id=${b.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-receipt"></i> Receipt</a>
                        <c:if test="${b.cancellable}">
                            <form action="${ctx}/patient/bookings/${b.id}/cancel" method="post" class="ms-auto"
                                  onsubmit="return confirm('Cancel booking ${b.bookingRef}? This cannot be undone.');">
                                ${csrfInput}
                                <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-x-circle"></i> Cancel</button>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
