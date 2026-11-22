<c:set var="pageTitle" value="Lab Worklist"/>
<c:set var="activeNav" value="worklist"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container-xl mt-4">
    <div class="page-header d-lg-flex justify-content-between align-items-end">
        <div>
            <h1 class="section-title h3 mb-1">Lab Worklist</h1>
            <p class="text-muted mb-0"><c:out value="${dateText}"/><c:if test="${isToday}"> <span class="badge bg-teal">Today</span></c:if>
                &middot; ${fn:length(bookings)} appointment(s)</p>
        </div>
        <form action="${ctx}/staff/worklist" method="get" class="d-flex flex-wrap gap-2 mt-3 mt-lg-0">
            <input type="date" name="date" value="${date}" class="form-control form-control-sm" style="max-width: 170px;" aria-label="Date">
            <select name="status" class="form-select form-select-sm" style="max-width: 190px;" aria-label="Status">
                <option value="">All active statuses</option>
                <c:forEach var="s" items="${statuses}">
                    <option value="${s}" ${status == s ? 'selected' : ''}>${fn:replace(s, '_', ' ')}</option>
                </c:forEach>
            </select>
            <button class="btn btn-primary btn-sm" type="submit"><i class="bi bi-funnel"></i> Filter</button>
            <a class="btn btn-outline-secondary btn-sm" href="${ctx}/staff/worklist">Today</a>
        </form>
    </div>

    <c:choose>
        <c:when test="${empty bookings}">
            <div class="rr-card p-5 text-center">
                <i class="bi bi-clipboard2-check fs-1 text-muted"></i>
                <h5 class="mt-3">No appointments</h5>
                <p class="text-muted mb-0">There are no matching appointments for this date.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="rr-card table-responsive">
                <table class="table table-rr table-hover mb-0">
                    <thead>
                    <tr><th>Slot</th><th>Booking</th><th>Patient</th><th>Visit</th><th>Tests</th><th>Status</th><th class="text-end">Action</th></tr>
                    </thead>
                    <tbody>
                    <c:forEach var="b" items="${bookings}">
                        <tr>
                            <td class="fw-semibold small"><c:out value="${b.timeSlot}"/></td>
                            <td><c:out value="${b.bookingRef}"/></td>
                            <td>
                                <div class="fw-semibold"><c:out value="${b.patientName}"/></div>
                                <small class="text-muted"><i class="bi bi-telephone"></i> <c:out value="${b.contactPhone}"/></small>
                            </td>
                            <td class="small">
                                <c:choose>
                                    <c:when test="${b.homeCollection}"><span class="badge bg-info text-dark"><i class="bi bi-house"></i> Home</span><br>
                                        <span class="text-muted"><c:out value="${b.collectionAddress}"/></span></c:when>
                                    <c:otherwise><span class="badge bg-secondary"><i class="bi bi-hospital"></i> Walk-in</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="small"><c:out value="${b.itemSummary}"/><br><span class="text-muted">${b.testCount} test(s)</span></td>
                            <td><span class="badge bg-${b.statusBadge}"><c:out value="${b.statusLabel}"/></span></td>
                            <td class="text-end text-nowrap">
                                <c:choose>
                                    <c:when test="${b.status == 'BOOKED'}">
                                        <form action="${ctx}/staff/bookings/${b.id}/status" method="post" class="d-inline">
                                            ${csrfInput}
                                            <input type="hidden" name="newStatus" value="SAMPLE_COLLECTED">
                                            <input type="hidden" name="date" value="${date}">
                                            <input type="hidden" name="filter" value="${status}">
                                            <button class="btn btn-sm btn-primary" type="submit"><i class="bi bi-droplet-fill"></i> Sample collected</button>
                                        </form>
                                    </c:when>
                                    <c:when test="${b.status == 'SAMPLE_COLLECTED'}">
                                        <form action="${ctx}/staff/bookings/${b.id}/status" method="post" class="d-inline">
                                            ${csrfInput}
                                            <input type="hidden" name="newStatus" value="PROCESSING">
                                            <input type="hidden" name="date" value="${date}">
                                            <input type="hidden" name="filter" value="${status}">
                                            <button class="btn btn-sm btn-outline-primary" type="submit"><i class="bi bi-gear"></i> Start processing</button>
                                        </form>
                                        <a href="${ctx}/staff/results/${b.id}" class="btn btn-sm btn-primary"><i class="bi bi-pencil-square"></i> Enter results</a>
                                    </c:when>
                                    <c:when test="${b.status == 'PROCESSING'}">
                                        <a href="${ctx}/staff/results/${b.id}" class="btn btn-sm btn-primary"><i class="bi bi-pencil-square"></i> Enter results</a>
                                    </c:when>
                                    <c:when test="${b.status == 'COMPLETED'}">
                                        <a href="${ctx}/staff/results/${b.id}" class="btn btn-sm btn-outline-secondary"><i class="bi bi-eye"></i> View</a>
                                        <a href="${ctx}/print/report?id=${b.id}" target="_blank" class="btn btn-sm btn-outline-secondary"><i class="bi bi-printer"></i></a>
                                    </c:when>
                                    <c:otherwise><span class="text-muted small">&ndash;</span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
