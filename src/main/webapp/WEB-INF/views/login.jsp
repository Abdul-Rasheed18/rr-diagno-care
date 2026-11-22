<c:set var="pageTitle" value="Login"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="container my-5">
    <div class="row justify-content-center g-4">
        <div class="col-lg-4 d-none d-lg-block">
            <div class="auth-side p-4 h-100 d-flex flex-column justify-content-center">
                <i class="bi bi-heart-pulse-fill display-5"></i>
                <h3 class="fw-bold mt-3">Welcome back</h3>
                <p class="opacity-75">Patients can book tests and download reports. Lab technicians and admins are taken to their dashboards automatically.</p>
                <ul class="small opacity-75 ps-3 mb-0">
                    <li>Track sample status live</li>
                    <li>Abnormal values highlighted</li>
                    <li>Printable receipts &amp; reports</li>
                </ul>
            </div>
        </div>
        <div class="col-lg-5 auth-wrap">
            <div class="rr-card p-4 p-md-5">
                <h1 class="h4 fw-bold mb-1">Sign in to RR Diagno Care</h1>
                <p class="text-muted small mb-4">Use your username and password.</p>

                <c:if test="${param.error != null}">
                    <div class="alert alert-danger py-2 small">
                        <i class="bi bi-exclamation-triangle"></i>
                        <c:choose>
                            <c:when test="${fn:containsIgnoreCase(sessionScope.SPRING_SECURITY_LAST_EXCEPTION.message, 'disabled')}">
                                Your account has been disabled. Please contact the administrator.
                            </c:when>
                            <c:otherwise>Invalid username or password.</c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
                <c:if test="${param.logout != null}">
                    <div class="alert alert-success py-2 small"><i class="bi bi-check-circle"></i> You have been logged out safely.</div>
                </c:if>

                <form action="${ctx}/login" method="post" novalidate>
                    ${csrfInput}
                    <div class="mb-3">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" class="form-control" id="username" name="username" autocomplete="username"
                               value="${fn:escapeXml(registeredUsername)}" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">Password</label>
                        <input type="password" class="form-control" id="password" name="password" autocomplete="current-password" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100 btn-lg">Login</button>
                </form>
                <p class="text-center small mt-4 mb-0">New patient? <a href="${ctx}/register" class="fw-semibold">Create an account</a></p>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
