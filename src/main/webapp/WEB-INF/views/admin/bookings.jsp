<c:set var="pageTitle" value="All Bookings"/>
<c:set var="activeNav" value="admin-bookings"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<%-- Current filters as a query string (re-used for CSV export and to return after POST actions) --%>
<c:url var="filterUrl" value="">
    <c:if test="${not empty status}"><c:param name="status" value="${status}"/></c:if>
    <c:if test="${not empty payment}"><c:param name="payment" value="${payment}"/></c:if>
    <c:if test="${not empty from}"><c:param name="from" value="${from}"/></c:if>
    <c:if test="${not empty to}"><c:param name="to" value="${to}"/></c:if>
    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
</c:url>
<c:set var="queryString" value="${fn:substringAfter(filterUrl, '?')}"/>

<div class="container-xl mt-4">
    <div class="page-header d-md-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">Bookings</h1>
            <p class="text-muted mb-0">${fn:length(bookings)} booking(s) match the current filters.</p>
        </div>
        <a href="${ctx}/admin/export/bookings.csv${empty queryString ? '' : '?'.concat(fn:escapeXml(queryString))}" class="btn btn-outline-primary btn-sm mt-3 mt-md-0">
            <i class="bi bi-filetype-csv"></i> Export to CSV (Excel)
        </a>
    </div>

    <form action="${ctx}/admin/bookings" method="get" class="rr-card p-3 mb-3">
        <div class="row g-2 align-items-end">
            <div class="col-md-3">
                <label class="form-label small mb-1" for="q">Search</label>
                <input id="q" type="search" name="q" value="${fn:escapeXml(q)}" class="form-control form-control-sm" placeholder="Ref, patient or phone">
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label small mb-1" for="status">Status</label>
                <select id="status" name="status" class="form-select form-select-sm">
                    <option value="">All</option>
                    <c:forEach var="s" items="${statuses}">
                        <option value="${s}" ${status == s ? 'selected' : ''}>${fn:replace(s, '_', ' ')}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label small mb-1" for="payment">Payment</label>
                <select id="payment" name="payment" class="form-select form-select-sm">
                    <option value="">All</option>
                    <option value="PENDING" ${payment == 'PENDING' ? 'selected' : ''}>Pending</option>
                    <option value="PAID" ${payment == 'PAID' ? 'selected' : ''}>Paid</option>
                </select>
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label small mb-1" for="from">From</label>
                <input id="from" type="date" name="from" value="${from}" class="form-control form-control-sm">
            </div>
            <div class="col-6 col-md-2">
                <label class="form-label small mb-1" for="to">To</label>
                <input id="to" type="date" name="to" value="${to}" class="form-control form-control-sm">
            </div>
            <div class="col-md-1 d-grid">
                <button class="btn btn-primary btn-sm" type="submit"><i class="bi bi-funnel"></i></button>
            </div>
        </div>
    </form>

    <div class="rr-card table-responsive">
        <table class="table table-rr table-hover mb-0">
            <thead>
            <tr><th>Booking</th><th>Patient</th><th>Appointment</th><th>Items</th><th class="text-end">Amount</th><th>Payment</th><th>Status</th><th class="text-end">Actions</th></tr>
            </thead>
            <tbody>
            <c:forEach var="b" items="${bookings}">
                <tr>
                    <td><a href="${ctx}/print/receipt?id=${b.id}" class="fw-semibold"><c:out value="${b.bookingRef}"/></a><br>
                        <small class="text-muted"><c:out value="${b.createdAtText}"/></small></td>
                    <td><c:out value="${b.patientName}"/><br><small class="text-muted"><c:out value="${b.contactPhone}"/></small></td>
                    <td class="small"><c:out value="${b.appointmentDateText}"/><br><c:out value="${b.timeSlot}"/><br>
                        <span class="badge ${b.homeCollection ? 'bg-info text-dark' : 'bg-secondary'}">${b.homeCollection ? 'Home' : 'Walk-in'}</span></td>
                    <td class="small" style="max-width: 220px;"><c:out value="${b.itemSummary}"/></td>
                    <td class="text-end fw-semibold">&#8377;<fmt:formatNumber value="${b.totalAmount}" pattern="#,##0.00"/></td>
                    <td class="small">
                        <span class="badge ${b.paid ? 'bg-success' : 'bg-warning text-dark'}">${b.paymentStatus}</span><br>
                        <span class="text-muted"><c:out value="${b.paymentModeLabel}"/></span>
                    </td>
                    <td><span class="badge bg-${b.statusBadge}"><c:out value="${b.statusLabel}"/></span></td>
                    <td class="text-end text-nowrap">
                        <c:if test="${b.status != 'CANCELLED'}">
                            <form action="${ctx}/admin/bookings/${b.id}/payment" method="post" class="d-inline">
                                ${csrfInput}
                                <input type="hidden" name="returnQuery" value="${fn:escapeXml(queryString)}">
                                <button class="btn btn-sm ${b.paid ? 'btn-outline-secondary' : 'btn-success'}" type="submit">
                                    ${b.paid ? 'Mark pending' : 'Mark PAID'}</button>
                            </form>
                        </c:if>
                        <c:if test="${b.completed}">
                            <a href="${ctx}/print/report?id=${b.id}" target="_blank" class="btn btn-sm btn-outline-primary" title="Report"><i class="bi bi-file-earmark-medical"></i></a>
                        </c:if>
                        <c:if test="${b.status != 'CANCELLED' and b.status != 'COMPLETED'}">
                            <form action="${ctx}/admin/bookings/${b.id}/cancel" method="post" class="d-inline"
                                  onsubmit="return confirm('Cancel booking ${b.bookingRef}?');">
                                ${csrfInput}
                                <input type="hidden" name="returnQuery" value="${fn:escapeXml(queryString)}">
                                <button class="btn btn-sm btn-outline-danger" type="submit" title="Cancel"><i class="bi bi-x-circle"></i></button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty bookings}">
                <tr><td colspan="8" class="text-center text-muted py-4">No bookings match these filters.</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
