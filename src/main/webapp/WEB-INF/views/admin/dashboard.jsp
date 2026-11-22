<c:set var="pageTitle" value="Admin Dashboard"/>
<c:set var="activeNav" value="dashboard"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container-xl mt-4">
    <div class="page-header d-md-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">Admin Dashboard</h1>
            <p class="text-muted mb-0">Revenue figures come from the MySQL stored procedure <code>sp_get_daily_revenue</code>.</p>
        </div>
        <form action="${ctx}/admin/dashboard" method="get" class="d-flex gap-2 mt-3 mt-md-0">
            <input type="date" name="date" value="${revenueDate}" class="form-control form-control-sm" aria-label="Revenue date">
            <button class="btn btn-primary btn-sm" type="submit">Show</button>
        </form>
    </div>

    <div class="row g-3 mb-4">
        <div class="col-sm-6 col-xl-3">
            <div class="rr-card stat-card p-3 d-flex gap-3 align-items-center h-100">
                <span class="stat-icon"><i class="bi bi-currency-rupee"></i></span>
                <div>
                    <div class="stat-label">${isToday ? "Today's" : revenue.reportDateText} revenue</div>
                    <div class="stat-value">&#8377;<fmt:formatNumber value="${revenue.totalRevenue}" pattern="#,##0"/></div>
                    <small class="text-success">&#8377;<fmt:formatNumber value="${revenue.collectedRevenue}" pattern="#,##0"/> collected</small>
                    &middot; <small class="text-warning">&#8377;<fmt:formatNumber value="${revenue.pendingRevenue}" pattern="#,##0"/> pending</small>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="rr-card stat-card p-3 d-flex gap-3 align-items-center h-100">
                <span class="stat-icon"><i class="bi bi-calendar-check"></i></span>
                <div>
                    <div class="stat-label">Total bookings</div>
                    <div class="stat-value">${totalBookings}</div>
                    <small class="text-muted">${revenue.totalBookings} on selected day &middot; ${revenue.cancelledBookings} cancelled</small>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="rr-card stat-card p-3 d-flex gap-3 align-items-center h-100">
                <span class="stat-icon"><i class="bi bi-hourglass-split"></i></span>
                <div>
                    <div class="stat-label">Pending reports</div>
                    <div class="stat-value">${pendingReports}</div>
                    <small class="text-muted">${todaysAppointments} appointment(s) today</small>
                </div>
            </div>
        </div>
        <div class="col-sm-6 col-xl-3">
            <div class="rr-card stat-card p-3 d-flex gap-3 align-items-center h-100">
                <span class="stat-icon"><i class="bi bi-people"></i></span>
                <div>
                    <div class="stat-label">Active sessions</div>
                    <div class="stat-value">${activeSessions}</div>
                    <small class="text-muted">${patientCount} registered patients &middot; ${totalRequests} requests</small>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-lg-8">
            <div class="rr-card p-4 h-100">
                <h5 class="fw-bold mb-0">Revenue &ndash; last 7 days</h5>
                <small class="text-muted">Non-cancelled bookings by appointment date (CallableStatement &rarr; sp_get_daily_revenue)</small>
                <div class="trend">
                    <c:forEach var="t" items="${trend}">
                        <div class="bar-wrap" title="${t.reportDateText}: ${t.totalBookings} booking(s)">
                            <div class="bar-value">&#8377;<fmt:formatNumber value="${t.totalRevenue}" pattern="#,##0"/></div>
                            <div class="bar" style="height: <fmt:formatNumber value="${t.totalRevenue * 100 / maxTrend}" maxFractionDigits="0" groupingUsed="false"/>%;"></div>
                            <div class="bar-label">${t.shortDayLabel}</div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="rr-card p-4 h-100">
                <h5 class="fw-bold mb-3">Bookings by status</h5>
                <c:forEach var="e" items="${statusCounts}">
                    <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                        <span>${fn:replace(e.key, '_', ' ')}</span>
                        <a href="${ctx}/admin/bookings?status=${e.key}" class="badge bg-teal text-decoration-none">${e.value}</a>
                    </div>
                </c:forEach>
                <div class="d-grid gap-2 mt-3">
                    <a href="${ctx}/admin/bookings" class="btn btn-outline-primary btn-sm">Manage bookings</a>
                    <a href="${ctx}/admin/export/bookings.csv" class="btn btn-outline-secondary btn-sm"><i class="bi bi-filetype-csv"></i> Export all to CSV</a>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-6">
            <div class="rr-card p-4 h-100">
                <h5 class="fw-bold mb-3">Recent bookings</h5>
                <div class="table-responsive">
                    <table class="table table-rr table-sm mb-0">
                        <thead><tr><th>Ref</th><th>Patient</th><th>Date</th><th class="text-end">Amount</th><th>Status</th></tr></thead>
                        <tbody>
                        <c:forEach var="b" items="${recentBookings}">
                            <tr>
                                <td class="small"><a href="${ctx}/print/receipt?id=${b.id}"><c:out value="${b.bookingRef}"/></a></td>
                                <td class="small"><c:out value="${b.patientName}"/></td>
                                <td class="small"><c:out value="${b.appointmentDateIso}"/></td>
                                <td class="small text-end">&#8377;<fmt:formatNumber value="${b.totalAmount}" pattern="#,##0"/></td>
                                <td><span class="badge bg-${b.statusBadge}"><c:out value="${b.statusLabel}"/></span></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty recentBookings}"><tr><td colspan="5" class="text-muted small">No bookings yet.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="rr-card p-4 h-100">
                <h5 class="fw-bold mb-1">Lab audit trail</h5>
                <small class="text-muted d-block mb-3">Recorded by the Spring AOP <code>LabAuditAspect</code> on every result entry / status update</small>
                <div class="table-responsive">
                    <table class="table table-rr table-sm mb-0">
                        <thead><tr><th>When</th><th>User</th><th>Action</th><th>Details</th><th class="text-end">ms</th></tr></thead>
                        <tbody>
                        <c:forEach var="a" items="${auditLogs}">
                            <tr>
                                <td class="small text-nowrap"><c:out value="${a.createdAtText}"/></td>
                                <td class="small"><c:out value="${a.username}"/></td>
                                <td class="small">
                                    <span class="badge ${a.outcome == 'SUCCESS' ? 'bg-success' : 'bg-danger'}"><c:out value="${a.action}"/></span>
                                </td>
                                <td class="small"><c:out value="${a.details}"/></td>
                                <td class="small text-end">${a.executionMs}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty auditLogs}"><tr><td colspan="5" class="text-muted small">No audit entries yet.</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
