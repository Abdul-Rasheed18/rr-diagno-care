<c:set var="pageTitle" value="Results ${booking.bookingRef}"/>
<c:set var="activeNav" value="worklist"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container-xl mt-4">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb small">
            <li class="breadcrumb-item"><a href="${ctx}/staff/worklist?date=${booking.appointmentDateIso}">Worklist</a></li>
            <li class="breadcrumb-item active" aria-current="page">Result entry &middot; <c:out value="${booking.bookingRef}"/></li>
        </ol>
    </nav>

    <div class="row g-4">
        <div class="col-lg-3">
            <div class="rr-card p-4">
                <h6 class="fw-bold">Patient</h6>
                <p class="mb-1 fw-semibold"><c:out value="${booking.patientName}"/></p>
                <p class="small text-muted mb-3">
                    <c:if test="${booking.patientAge > 0}">${booking.patientAge} yrs &middot; </c:if><c:out value="${booking.patientGender}"/><br>
                    <i class="bi bi-telephone"></i> <c:out value="${booking.contactPhone}"/>
                </p>
                <h6 class="fw-bold">Appointment</h6>
                <p class="small mb-3"><c:out value="${booking.appointmentDateText}"/><br><c:out value="${booking.timeSlot}"/><br><c:out value="${booking.visitModeLabel}"/></p>
                <h6 class="fw-bold">Status</h6>
                <span class="badge bg-${booking.statusBadge}"><c:out value="${booking.statusLabel}"/></span>
                <hr>
                <p class="small text-muted mb-0"><i class="bi bi-shield-check"></i> Every save is recorded in the audit trail with your username and timing.</p>
            </div>
        </div>
        <div class="col-lg-9">
            <c:choose>
                <c:when test="${editable}">
                    <form action="${ctx}/staff/results/${booking.id}" method="post" class="rr-card p-4">
                        ${csrfInput}
                        <h5 class="fw-bold mb-3"><i class="bi bi-pencil-square text-teal"></i> Enter measured values</h5>
                        <div class="table-responsive">
                            <table class="table table-rr">
                                <thead><tr><th>Test</th><th style="width: 150px;">Result</th><th>Unit</th><th>Reference</th><th>Remarks</th></tr></thead>
                                <tbody>
                                <c:forEach var="r" items="${booking.results}">
                                    <tr>
                                        <td><div class="fw-semibold"><c:out value="${r.testName}"/></div><small class="text-muted"><c:out value="${r.testCode}"/></small></td>
                                        <td>
                                            <input type="text" name="value_${r.testId}" value="${fn:escapeXml(r.resultValue)}"
                                                   class="form-control form-control-sm result-input" maxlength="50"
                                                   data-min="${r.normalMin}" data-max="${r.normalMax}" aria-label="Result for ${fn:escapeXml(r.testName)}">
                                        </td>
                                        <td class="small"><c:out value="${r.unit}"/></td>
                                        <td class="small text-nowrap"><c:out value="${r.referenceRange}"/></td>
                                        <td><input type="text" name="remark_${r.testId}" value="${fn:escapeXml(r.remarks)}" class="form-control form-control-sm" maxlength="255"></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <div class="mb-3">
                            <label for="reportRemarks" class="form-label fw-semibold">Overall report remarks</label>
                            <textarea id="reportRemarks" name="reportRemarks" rows="2" class="form-control" maxlength="500"
                                      placeholder="e.g. Mildly raised fasting sugar. Please consult your physician.">${fn:escapeXml(booking.reportRemarks)}</textarea>
                        </div>
                        <div class="d-flex flex-wrap gap-2 justify-content-end">
                            <button type="submit" name="action" value="save" class="btn btn-outline-primary"><i class="bi bi-save"></i> Save draft</button>
                            <button type="submit" name="action" value="complete" class="btn btn-primary"
                                    onclick="return confirm('Mark this report COMPLETED? The patient will be able to view and print it.');">
                                <i class="bi bi-check2-circle"></i> Save &amp; mark COMPLETED
                            </button>
                        </div>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="rr-card p-4">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="fw-bold mb-0">Results</h5>
                            <c:if test="${booking.completed}"><a href="${ctx}/print/report?id=${booking.id}" target="_blank" class="btn btn-sm btn-primary"><i class="bi bi-printer"></i> Print</a></c:if>
                        </div>
                        <c:if test="${booking.status == 'BOOKED'}">
                            <div class="alert alert-info small">Mark the sample as collected on the worklist before entering results.</div>
                        </c:if>
                        <div class="table-responsive"><%@ include file="/WEB-INF/views/common/result-table.jsp" %></div>
                        <c:if test="${not empty booking.reportRemarks}"><p class="small mt-3 mb-0"><strong>Remarks:</strong> <c:out value="${booking.reportRemarks}"/></p></c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script>
// Live hint: highlight values outside the reference range while typing
document.querySelectorAll('.result-input').forEach(function (input) {
    function check() {
        var v = parseFloat(input.value.replace(/,/g, ''));
        var min = parseFloat(input.dataset.min), max = parseFloat(input.dataset.max);
        var abnormal = !isNaN(v) && ((!isNaN(min) && v < min) || (!isNaN(max) && v > max));
        input.classList.toggle('is-invalid', abnormal);
        input.classList.toggle('result-abnormal', abnormal);
    }
    input.addEventListener('input', check);
    check();
});
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
