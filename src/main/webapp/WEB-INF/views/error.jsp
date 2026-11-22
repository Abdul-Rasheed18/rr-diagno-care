<%-- Used by GlobalExceptionHandler, BaseSpringServlet and web.xml <error-page> entries. --%>
<c:set var="code" value="${not empty statusCode ? statusCode : requestScope['javax.servlet.error.status_code']}"/>
<c:if test="${empty code}"><c:set var="code" value="500"/></c:if>
<c:set var="pageTitle" value="${empty errorTitle ? 'Error' : errorTitle}"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-lg-6">
            <div class="rr-card p-5 text-center">
                <c:choose>
                    <c:when test="${code == 404}"><i class="bi bi-signpost-split display-3 text-teal"></i></c:when>
                    <c:when test="${code == 403}"><i class="bi bi-shield-lock display-3 text-warning"></i></c:when>
                    <c:otherwise><i class="bi bi-bandaid display-3 text-danger"></i></c:otherwise>
                </c:choose>
                <div class="text-muted small mt-3">Error ${fn:escapeXml(code)}</div>
                <h1 class="h3 fw-bold">
                    <c:choose>
                        <c:when test="${not empty errorTitle}"><c:out value="${errorTitle}"/></c:when>
                        <c:when test="${code == 404}">Page not found</c:when>
                        <c:when test="${code == 403}">Access denied</c:when>
                        <c:otherwise>Something went wrong</c:otherwise>
                    </c:choose>
                </h1>
                <p class="text-muted">
                    <c:choose>
                        <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
                        <c:when test="${code == 404}">The page you are looking for does not exist or has been moved.</c:when>
                        <c:when test="${code == 403}">You do not have permission to view this page.</c:when>
                        <c:otherwise>An unexpected error occurred. Please try again in a moment.</c:otherwise>
                    </c:choose>
                </p>
                <c:if test="${not empty errorRef}"><p class="small text-muted">Reference: <code><c:out value="${errorRef}"/></code></p></c:if>
                <div class="d-flex justify-content-center gap-2 mt-3">
                    <a href="${ctx}/" class="btn btn-primary"><i class="bi bi-house"></i> Home</a>
                    <a href="javascript:history.back()" class="btn btn-outline-secondary">Go back</a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
