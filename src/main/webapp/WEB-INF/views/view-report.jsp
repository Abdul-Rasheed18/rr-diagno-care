<c:set var="pageTitle" value="Booking ${booking.bookingRef}"/>
<c:set var="activeNav" value="bookings"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container mt-4">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb small">
            <li class="breadcrumb-item"><a href="${ctx}/patient/bookings">My Bookings</a></li>
            <li class="breadcrumb-item active" aria-current="page"><c:out value="${booking.bookingRef}"/></li>
        </ol>
    </nav>

    <div class="row g-4">
        <div class="col-lg-4">
            <div class="rr-card p-4 mb-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0">Booking details</h5>
                    <span class="badge bg-${booking.statusBadge}"><c:out value="${booking.statusLabel}"/></span>
                </div>
                <table class="print-meta">
                    <tr><td>Reference</td><td class="fw-semibold"><c:out value="${booking.bookingRef}"/></td></tr>
                    <tr><td>Patient</td><td><c:out value="${booking.patientName}"/></td></tr>
                    <tr><td>Date</td><td><c:out value="${booking.appointmentDateText}"/></td></tr>
                    <tr><td>Slot</td><td><c:out value="${booking.timeSlot}"/></td></tr>
                    <tr><td>Visit</td><td><c:out value="${booking.visitModeLabel}"/></td></tr>
                    <c:if test="${booking.homeCollection}"><tr><td>Address</td><td><c:out value="${booking.collectionAddress}"/></td></tr></c:if>
                    <tr><td>Phone</td><td><c:out value="${booking.contactPhone}"/></td></tr>
                    <tr><td>Payment</td><td><c:out value="${booking.paymentModeLabel}"/>
                        <span class="badge ${booking.paid ? 'bg-success' : 'bg-warning text-dark'}">${booking.paymentStatus}</span></td></tr>
                    <tr><td>Booked on</td><td><c:out value="${booking.createdAtText}"/></td></tr>
                </table>
                <hr>
                <h6 class="fw-bold">Items</h6>
                <ul class="list-unstyled small mb-2">
                    <c:forEach var="it" items="${booking.items}">
                        <li class="d-flex justify-content-between py-1">
                            <span><c:if test="${it.itemType == 'PACKAGE'}"><i class="bi bi-box2-heart text-teal"></i> </c:if><c:out value="${it.itemName}"/></span>
                            <span>&#8377;<fmt:formatNumber value="${it.price}" pattern="#,##0.00"/></span>
                        </li>
                    </c:forEach>
                </ul>
                <div class="d-flex justify-content-between fw-bold border-top pt-2">
                    <span>Total</span><span class="text-teal">&#8377;<fmt:formatNumber value="${booking.totalAmount}" pattern="#,##0.00"/></span>
                </div>
                <div class="d-flex flex-wrap gap-2 mt-3">
                    <a href="${ctx}/print/receipt?id=${booking.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-receipt"></i> Receipt</a>
                    <c:if test="${booking.cancellable}">
                        <form action="${ctx}/patient/bookings/${booking.id}/cancel" method="post"
                              onsubmit="return confirm('Cancel this booking? This cannot be undone.');">
                            ${csrfInput}
                            <button class="btn btn-sm btn-outline-danger" type="submit"><i class="bi bi-x-circle"></i> Cancel booking</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </div>

        <div class="col-lg-8">
            <div class="rr-card p-4">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0"><i class="bi bi-file-earmark-medical text-teal"></i> Laboratory report</h5>
                    <c:if test="${booking.completed}">
                        <a href="${ctx}/print/report?id=${booking.id}" target="_blank" class="btn btn-primary btn-sm"><i class="bi bi-printer"></i> Print / Save PDF</a>
                    </c:if>
                </div>
                <c:choose>
                    <c:when test="${booking.completed}">
                        <c:if test="${booking.abnormalCount > 0}">
                            <div class="alert alert-danger py-2 small">
                                <i class="bi bi-exclamation-triangle-fill"></i>
                                <strong>${booking.abnormalCount}</strong> value(s) are outside the normal reference range and are highlighted in red.
                                Please consult your doctor.
                            </div>
                        </c:if>
                        <div class="table-responsive">
                            <%@ include file="/WEB-INF/views/common/result-table.jsp" %>
                        </div>
                        <c:if test="${not empty booking.reportRemarks}">
                            <div class="mt-3 p-3 bg-teal-soft rounded-3 small"><strong>Pathologist remarks:</strong> <c:out value="${booking.reportRemarks}"/></div>
                        </c:if>
                        <p class="small text-muted mt-3 mb-0">Report released on <c:out value="${booking.completedAtText}"/>.</p>
                    </c:when>
                    <c:when test="${booking.status == 'CANCELLED'}">
                        <div class="text-center py-5 text-muted"><i class="bi bi-x-octagon fs-1"></i><p class="mt-2">This booking was cancelled.</p></div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5">
                            <i class="bi bi-hourglass-split fs-1 text-teal"></i>
                            <h6 class="mt-3">Your report is not ready yet</h6>
                            <p class="text-muted small">Current status: <strong><c:out value="${booking.statusLabel}"/></strong>.
                                ${booking.testCount} test(s) will appear here once the lab marks the report COMPLETED.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
