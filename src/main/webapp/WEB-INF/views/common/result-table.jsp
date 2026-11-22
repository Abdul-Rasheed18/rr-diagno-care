<%-- Reusable lab-result table. Expects request/page attribute "booking". Abnormal values are highlighted with c:choose. --%>
<table class="table table-rr align-middle mb-0">
    <thead>
    <tr>
        <th>Test</th>
        <th class="text-center">Result</th>
        <th>Unit</th>
        <th>Reference range</th>
        <th class="text-center">Flag</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="lastCategory" value=""/>
    <c:forEach var="r" items="${booking.results}">
        <c:if test="${r.category != lastCategory}">
            <tr><td colspan="5" class="bg-light fw-bold small text-teal text-uppercase"><c:out value="${r.category}"/></td></tr>
            <c:set var="lastCategory" value="${r.category}"/>
        </c:if>
        <c:set var="isLow" value="${r.numericValue != null and r.normalMin != null and r.numericValue lt r.normalMin}"/>
        <c:set var="isHigh" value="${r.numericValue != null and r.normalMax != null and r.numericValue gt r.normalMax}"/>
        <tr class="${isLow or isHigh ? 'row-abnormal' : ''}">
            <td>
                <div class="fw-semibold"><c:out value="${r.testName}"/></div>
                <c:if test="${not empty r.remarks}"><small class="text-muted"><i class="bi bi-chat-left-text"></i> <c:out value="${r.remarks}"/></small></c:if>
            </td>
            <c:choose>
                <c:when test="${empty r.resultValue}">
                    <td class="text-center text-muted">Pending</td>
                </c:when>
                <c:when test="${isLow or isHigh}">
                    <td class="text-center result-abnormal fs-6"><c:out value="${r.resultValue}"/></td>
                </c:when>
                <c:otherwise>
                    <td class="text-center result-normal"><c:out value="${r.resultValue}"/></td>
                </c:otherwise>
            </c:choose>
            <td class="small"><c:out value="${r.unit}"/></td>
            <td class="small"><c:out value="${r.referenceRange}"/></td>
            <td class="text-center">
                <c:choose>
                    <c:when test="${isLow}"><span class="badge bg-danger"><i class="bi bi-arrow-down"></i> LOW</span></c:when>
                    <c:when test="${isHigh}"><span class="badge bg-danger"><i class="bi bi-arrow-up"></i> HIGH</span></c:when>
                    <c:when test="${not empty r.resultValue}"><span class="badge bg-success">Normal</span></c:when>
                    <c:otherwise>&ndash;</c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
