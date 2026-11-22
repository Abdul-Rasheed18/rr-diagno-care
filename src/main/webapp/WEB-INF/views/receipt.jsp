<%-- Rendered by BookingReceiptServlet via RequestDispatcher.forward --%>
<c:set var="pageTitle" value="Receipt ${booking.bookingRef}"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container mt-4">
    <c:if test="${justBooked}">
        <div class="rr-card bg-teal-soft p-4 mb-4 no-print text-center">
            <i class="bi bi-check-circle-fill text-teal display-6"></i>
            <h2 class="h4 fw-bold mt-2">Your booking is confirmed!</h2>
            <p class="mb-0 text-muted">Please carry this receipt (printed or on your phone) at the time of sample collection.</p>
        </div>
    </c:if>

    <div class="d-flex justify-content-end gap-2 mb-3 no-print">
        <c:if test="${isPatient}"><a href="${ctx}/patient/bookings" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> My Bookings</a></c:if>
        <c:if test="${isAdmin}"><a href="${ctx}/admin/bookings" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> All bookings</a></c:if>
        <button onclick="window.print()" class="btn btn-primary btn-sm"><i class="bi bi-printer"></i> Print receipt</button>
    </div>

    <div class="print-sheet">
        <div class="print-head d-flex justify-content-between align-items-start">
            <div>
                <div class="lab-name"><i class="bi bi-heart-pulse-fill"></i> RR Diagno Care</div>
                <div class="small text-muted">Main Road, Labbipet, Vijayawada 520010 &middot; +91 98480 00001 &middot; care@rrdiagnocare.in</div>
            </div>
            <div class="text-end">
                <div class="fw-bold text-uppercase small text-muted">Booking Receipt</div>
                <div class="h5 fw-bold mb-0"><c:out value="${booking.bookingRef}"/></div>
                <span class="badge bg-${booking.statusBadge}"><c:out value="${booking.statusLabel}"/></span>
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-sm-6">
                <table class="print-meta">
                    <tr><td>Patient</td><td class="fw-semibold"><c:out value="${booking.patientName}"/></td></tr>
                    <tr><td>Mobile</td><td><c:out value="${booking.contactPhone}"/></td></tr>
                    <tr><td>Email</td><td><c:out value="${booking.patientEmail}"/></td></tr>
                    <tr><td>Booked on</td><td><c:out value="${booking.createdAtText}"/></td></tr>
                </table>
            </div>
            <div class="col-sm-6">
                <table class="print-meta">
                    <tr><td>Appointment</td><td class="fw-semibold"><c:out value="${booking.appointmentDateText}"/></td></tr>
                    <tr><td>Time slot</td><td class="fw-semibold"><c:out value="${booking.timeSlot}"/></td></tr>
                    <tr><td>Visit type</td><td><c:out value="${booking.visitModeLabel}"/></td></tr>
                    <c:if test="${booking.homeCollection}"><tr><td>Address</td><td><c:out value="${booking.collectionAddress}"/></td></tr></c:if>
                </table>
            </div>
        </div>

        <table class="table table-rr">
            <thead><tr><th>#</th><th>Description</th><th>Type</th><th class="text-end">Amount</th></tr></thead>
            <tbody>
            <c:forEach var="it" items="${booking.items}" varStatus="s">
                <tr>
                    <td>${s.count}</td>
                    <td><c:out value="${it.itemName}"/></td>
                    <td><small class="text-muted">${it.itemType == 'PACKAGE' ? 'Health Package' : 'Test'}</small></td>
                    <td class="text-end">&#8377;<fmt:formatNumber value="${it.price}" pattern="#,##0.00"/></td>
                </tr>
            </c:forEach>
            </tbody>
            <tfoot>
            <tr>
                <th colspan="3" class="text-end">Total amount</th>
                <th class="text-end">&#8377;<fmt:formatNumber value="${booking.totalAmount}" pattern="#,##0.00"/></th>
            </tr>
            </tfoot>
        </table>

        <div class="row g-3 align-items-center">
            <div class="col-sm-7">
                <p class="mb-1"><strong>Payment mode:</strong> <c:out value="${booking.paymentModeLabel}"/></p>
                <p class="mb-1"><strong>Payment status:</strong>
                    <span class="badge ${booking.paid ? 'bg-success' : 'bg-warning text-dark'}">${booking.paymentStatus}</span></p>
                <p class="small text-muted mb-0">${fn:length(booking.results)} individual test(s) will be performed.</p>
            </div>
            <div class="col-sm-5 small">
                <div class="p-3 border rounded-3">
                    <strong>Preparation reminder</strong><br>
                    Fasting tests need 10&ndash;12 hours fasting (water allowed). Carry a photo ID and any previous reports.
                </div>
            </div>
        </div>
        <p class="small text-muted text-center mt-4 mb-0">Computer-generated receipt &middot; printed <c:out value="${printedAt}"/> &middot; No signature required.</p>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
