<%-- Rendered by ReportPrintServlet: clean print-ready pathology report. --%>
<c:set var="pageTitle" value="Report ${booking.bookingRef}"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3 no-print">
        <a href="javascript:history.back()" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> Back</a>
        <button onclick="window.print()" class="btn btn-primary btn-sm"><i class="bi bi-printer"></i> Print / Save as PDF</button>
    </div>

    <div class="print-sheet">
        <div class="print-head d-flex justify-content-between align-items-start">
            <div>
                <div class="lab-name"><i class="bi bi-heart-pulse-fill"></i> RR Diagno Care</div>
                <div class="small text-muted">Diagnostics &amp; Pathology Laboratory<br>Main Road, Labbipet, Vijayawada 520010 &middot; +91 98480 00001</div>
            </div>
            <div class="text-end">
                <div class="fw-bold text-uppercase small text-muted">Laboratory Test Report</div>
                <c:if test="${draft}"><div class="watermark-draft mt-1">DRAFT &ndash; NOT FINAL (${booking.statusLabel})</div></c:if>
            </div>
        </div>

        <div class="row mb-3">
            <div class="col-sm-6">
                <table class="print-meta">
                    <tr><td>Patient name</td><td class="fw-semibold"><c:out value="${booking.patientName}"/></td></tr>
                    <tr><td>Age / Gender</td><td>
                        <c:choose><c:when test="${booking.patientAge > 0}">${booking.patientAge} yrs</c:when><c:otherwise>&ndash;</c:otherwise></c:choose>
                        / <c:out value="${empty booking.patientGender ? '-' : booking.patientGender}"/></td></tr>
                    <tr><td>Mobile</td><td><c:out value="${booking.contactPhone}"/></td></tr>
                </table>
            </div>
            <div class="col-sm-6">
                <table class="print-meta">
                    <tr><td>Booking ref</td><td class="fw-semibold"><c:out value="${booking.bookingRef}"/></td></tr>
                    <tr><td>Sample date</td><td><c:out value="${booking.appointmentDateText}"/></td></tr>
                    <tr><td>Reported on</td><td><c:out value="${empty booking.completedAtText ? '-' : booking.completedAtText}"/></td></tr>
                </table>
            </div>
        </div>

        <%@ include file="/WEB-INF/views/common/result-table.jsp" %>

        <c:if test="${booking.abnormalCount > 0}">
            <p class="small mt-2 mb-0"><strong class="result-abnormal">Bold red</strong> values are outside the biological reference range
                (${booking.abnormalCount} abnormal finding<c:if test="${booking.abnormalCount > 1}">s</c:if>).</p>
        </c:if>
        <c:if test="${not empty booking.reportRemarks}">
            <div class="mt-3 p-3 border rounded-3 small"><strong>Remarks:</strong> <c:out value="${booking.reportRemarks}"/></div>
        </c:if>

        <div class="d-flex justify-content-between align-items-end mt-5">
            <div class="small text-muted">
                Printed <c:out value="${printedAt}"/><br>
                Results relate only to the sample tested. Please correlate clinically.
            </div>
            <div>
                <div class="text-center small mb-1"><c:out value="${technician}"/></div>
                <div class="signature-line">Lab Technician</div>
            </div>
            <div>
                <div class="text-center small mb-1">Dr. R. Ramesh, MD (Pathology)</div>
                <div class="signature-line">Consultant Pathologist</div>
            </div>
        </div>
        <p class="text-center small text-muted mt-4 mb-0">&mdash; End of report &mdash;</p>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
